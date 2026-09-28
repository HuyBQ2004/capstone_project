package com.sfms.mf05.service;

import com.sfms.common.allergen.AllergenSemantics;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.DishIngredient;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.domain.MenuDish;
import com.sfms.mf05.dto.MenuDtos.AllergyConflict;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BR-063 — đối chiếu allergen của mọi món với hồ sơ dị ứng ĐÃ XÁC MINH của khối. Chạy ở CẢ
 * submit và approve (C3). Tính RUNTIME (ADR-M05-02), KHÔNG lưu cột.
 *
 * <p>M05-D14/PDPD — chỉ dùng {@link ReferenceResolver#verifiedAllergenCountsByGrade} (allergen
 * → SỐ ĐẾM), KHÔNG BAO GIỜ đọc/log {@code studentId}/tên.
 */
@Component
public class AllergyConflictChecker {

    private final ReferenceResolver referenceResolver;

    public AllergyConflictChecker(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Toàn bộ xung đột (kể cả đã có suất thay thế — để BGH vẫn thấy cảnh báo khi duyệt).
     *
     * <p><b>Viết lại 13/08/2026.</b> Bản trước duyệt {@code dish.getManualAllergens()} rồi so
     * khớp CHÍNH XÁC với {@code allergenCounts.get(...)}. Hai chỗ hụt, cả hai đều làm server
     * cảnh báo ÍT HƠN frontend:
     *
     * <ol>
     *   <li>{@code manual_allergens} theo đúng định nghĩa chỉ là phần KHAI THÊM BẰNG TAY cho
     *       nguyên liệu ngoài danh mục — BR-063 nói tác nhân chính được DẪN XUẤT từ thành phần.
     *       Món khai thành phần tử tế thì mảng đó rỗng ⇒ server báo 0 xung đột. Nay hợp thêm
     *       {@code catalog_ingredients.allergens} của từng thành phần (cột thêm ở V74).</li>
     *   <li>So khớp chính xác bỏ lọt quan hệ Ô: em khai "Hải sản" không được cảnh báo với món
     *       tag "Tôm" và ngược lại. Nay dùng {@link AllergenSemantics#touches}.</li>
     * </ol>
     *
     * <p>Ghi nhận theo TÁC NHÂN CỦA HỌC SINH (khoá của {@code allergenCounts}), không theo tác
     * nhân của món: số đếm phải là số em bị ảnh hưởng, và một tác nhân của món có thể chạm
     * nhiều tác nhân khai báo khác nhau.
     */
    public List<AllergyConflict> check(Menu menu) {
        Map<String, Integer> allergenCounts =
                referenceResolver.verifiedAllergenCountsByGrade(menu.getSchoolId(), menu.getGradeId());
        if (allergenCounts.isEmpty()) {
            return List.of();
        }
        List<AllergyConflict> conflicts = new ArrayList<>();
        for (MenuDish md : menu.getMenuDishes()) {
            Dish dish = md.getDish();
            boolean hasSubstitute = !md.getSubstitutes().isEmpty();
            Set<String> dishAllergens = allergensOf(dish);
            if (dishAllergens.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Integer> entry : allergenCounts.entrySet()) {
                String studentAllergen = entry.getKey();
                Integer count = entry.getValue();
                if (count == null || count <= 0) {
                    continue;
                }
                boolean touched = dishAllergens.stream()
                        .anyMatch(da -> AllergenSemantics.touches(studentAllergen, da));
                if (touched) {
                    conflicts.add(new AllergyConflict(
                            dish.getCode(), dish.getName(), studentAllergen, count, hasSubstitute));
                }
            }
        }
        return conflicts;
    }

    /** Tác nhân của MỘT món = khai tay ∪ tác nhân của từng thành phần trong danh mục (BR-063). */
    private Set<String> allergensOf(Dish dish) {
        Set<String> out = new LinkedHashSet<>(dish.getManualAllergens());
        List<String> codes = dish.getIngredients().stream()
                .map(DishIngredient::getIngredientCode)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .toList();
        out.addAll(referenceResolver.allergensOfIngredientCodes(codes));
        return out;
    }

    /** true nếu còn xung đột KHÔNG có suất thay thế → chặn submit/approve (ràng buộc C2/C3). */
    public boolean hasBlockingConflict(Menu menu) {
        return check(menu).stream().anyMatch(c -> !c.resolvedBySubstitute());
    }
}
