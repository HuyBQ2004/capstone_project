package com.sfms.mf09.port;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.enums.MealType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Hiện thực {@link MenuQueryPort} bằng native query đọc thẳng {@code menus}/{@code menu_dishes}/
 * {@code dishes} (MF05, đã merge V8/V9) — <b>không</b> import entity MF05 (§4b kế hoạch), cùng
 * kỹ thuật {@code ReferenceResolver} dùng cho mọi tra cứu chéo module.
 *
 * <p>{@code M09-D08}: MF05 (Thảo) chưa mở port chính thức — đây là adapter MF09 tự viết trên
 * dữ liệu <b>thật</b> (khác {@code IncidentStatusPortStub} của MF08, vốn stub vì MF11 chưa tồn
 * tại). Thay bằng bean của MF05 khi có, chữ ký {@link MenuQueryPort} không đổi.
 */
@Component
public class MenuQueryPortImpl implements MenuQueryPort {

    @PersistenceContext
    private EntityManager em;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public Optional<MenuBlockView> publishedBlock(LocalDate date, UUID gradeId, MealType mealType, UUID schoolId) {
        if (date == null || gradeId == null || mealType == null || schoolId == null) {
            return Optional.empty();
        }
        // V44 — grade_id là FK UUID thật (menus.grade_id), so sánh trực tiếp — không còn cần
        // tách số bằng regex (substring(grade from '\d+')::int) như trước migration.
        var menuRows = em.createNativeQuery("""
                select id, code from menus
                where school_id = :schoolId and menu_date = :date and meal_type = :mealType
                  and status in ('APPROVED','PUBLISHED') and audience = 'ALL'
                  and grade_id = :gradeId
                order by (status = 'PUBLISHED') desc limit 1
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("date", date)
                .setParameter("mealType", mealType.name())
                .setParameter("gradeId", gradeId)
                .getResultList();
        if (menuRows.isEmpty()) {
            return Optional.empty();
        }
        Object[] menu = (Object[]) menuRows.get(0);
        UUID menuId = (UUID) menu[0];
        String menuCode = (String) menu[1];

        return Optional.of(new MenuBlockView(menuId, menuCode, dishesOfMenu(menuId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishView> dishesOfMenu(UUID menuId) {
        if (menuId == null) {
            return List.of();
        }
        /*
         * 13/08/2026 (Nam) — BUG AN TOÀN THỰC PHẨM, sửa 2 chỗ trong câu này.
         *
         * 1. ALLERGEN: bản cũ chỉ đọc `d.manual_allergens`. Nhưng theo BR-063 tác nhân chính
         *    được DẪN XUẤT từ thành phần món — `manual_allergens` chỉ là phần khai THÊM bằng
         *    tay cho nguyên liệu ngoài danh mục. Món khai thành phần tử tế ("Cá lóc kho tộ" có
         *    nguyên liệu "Cá lóc") thì mảng đó RỖNG ⇒ MF09 kết luận món không chạm dị ứng ⇒
         *    PHÁT MÓN CÁ CHO EM DỊ ỨNG CÁ. Trong khi MF05 (AllergyConflictChecker, viết lại
         *    cùng ngày) đã hợp thêm `catalog_ingredients.allergens` (V74) nên nhìn thấy xung
         *    đột và bắt khai suất thay thế — hai module đọc hai sự thật khác nhau.
         *    Nay hợp cả hai nguồn, cùng phép hợp mà MF05 dùng.
         *
         * 2. MÓN THAY THẾ: bản cũ chỉ lấy `d.default_substitute_id` (mặc định ở THƯ VIỆN MÓN).
         *    Suất thay thế mà người lập thực đơn khai NGAY TRÊN THỰC ĐƠN
         *    (`menu_dish_substitutes` — thứ MF05 dùng để coi xung đột đã hoá giải, và là thứ
         *    hiện trên màn thực đơn: "Bò xào súp lơ · 1 suất") bị bỏ qua hoàn toàn. Hệ quả:
         *    thực đơn khai đủ suất thay vẫn bị MF09 đánh `blocked` (chưa có món thay thế).
         *    Ưu tiên suất khai trên thực đơn, không có mới lùi về mặc định thư viện.
         */
        var dishRows = em.createNativeQuery("""
                select d.id, d.code, d.name,
                       coalesce(
                           (select jsonb_agg(distinct a)
                            from (
                                select jsonb_array_elements_text(d.manual_allergens) as a
                                union
                                select jsonb_array_elements_text(ci.allergens) as a
                                from dish_ingredients di
                                join catalog_ingredients ci on ci.code = di.ingredient_code
                                where di.dish_id = d.id
                            ) merged),
                           '[]'::jsonb)::text,
                       coalesce(sub.id, s.id), coalesce(sub.code, s.code), coalesce(sub.name, s.name)
                from menu_dishes md
                join dishes d on d.id = md.dish_id
                left join dishes s on s.id = d.default_substitute_id
                left join lateral (
                    select ds.id, ds.code, ds.name
                    from menu_dish_substitutes mds
                    join dishes ds on ds.id = mds.substitute_dish_id
                    where mds.menu_dish_id = md.id
                    order by mds.created_at
                    limit 1
                ) sub on true
                where md.menu_id = :menuId
                order by md.serving_order
                """)
                .setParameter("menuId", menuId)
                .getResultList();

        List<DishView> dishes = new ArrayList<>();
        for (Object row : dishRows) {
            Object[] cols = (Object[]) row;
            dishes.add(new DishView((UUID) cols[0], (String) cols[1], (String) cols[2],
                    parseAllergens((String) cols[3]), (UUID) cols[4], (String) cols[5], (String) cols[6]));
        }
        return dishes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> allergensOfMenu(UUID menuId) {
        if (menuId == null) {
            return List.of();
        }
        var rows = em.createNativeQuery("""
                select distinct jsonb_array_elements_text(d.manual_allergens)
                from menu_dishes md join dishes d on d.id = md.dish_id
                where md.menu_id = :menuId
                """)
                .setParameter("menuId", menuId)
                .getResultList();
        List<String> out = new ArrayList<>();
        for (Object r : rows) {
            out.add((String) r);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseAllergens(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }
}
