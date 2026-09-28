package com.sfms.mf09.port;

import com.sfms.common.enums.MealType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MF05 — món của (ngày, khối, bữa) từ thực đơn APPROVED/PUBLISHED (ràng buộc C3). GIỮ dạng
 * port (M09-D08) vì đây là <b>hành vi</b> của MF05 (truy vấn nhiều bảng + luật chọn bản đã
 * duyệt), khác 5 port khác của TDS v1.0 vốn là tra cứu thuần đã gỡ về {@code ReferenceResolver}.
 */
public interface MenuQueryPort {

    Optional<MenuBlockView> publishedBlock(LocalDate date, UUID gradeId, MealType mealType, UUID schoolId);

    /** Union allergen của mọi món trong 1 thực đơn cụ thể — dùng tính {@code allergyWarning} runtime. */
    List<String> allergensOfMenu(UUID menuId);

    /** Danh sách món của MỘT thực đơn đã biết {@code menuId} (dùng khi {@code serve()} đã có menu snapshot). */
    List<DishView> dishesOfMenu(UUID menuId);

    record MenuBlockView(UUID menuId, String menuCode, List<DishView> dishes) {}

    /**
     * 04_Meal_Substitution_Portion_Pipeline_Design.md — {@code defaultSubstitute*} thêm
     * 05/08/2026: {@code doServe()} từng KHÔNG tra {@code Dish.defaultSubstituteId} dù cột này
     * tồn tại từ V8 — hệ quả là học sinh dị ứng vẫn được ghi nhận ăn ĐÚNG món gây dị ứng, chỉ
     * kèm cờ {@code isSubstitute=true} vô nghĩa (sai lệch với `TestSpec-MF09` FX-009/TC-016,
     * không phải hành vi thiết kế). {@code null} khi món không cấu hình sẵn món thay thế.
     */
    record DishView(UUID dishId, String dishCode, String name, List<String> allergens,
                     UUID defaultSubstituteId, String defaultSubstituteCode, String defaultSubstituteName) {}
}
