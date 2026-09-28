package com.sfms.common.allergen;

import java.util.Map;

/**
 * Quan hệ CHA–CON giữa các tác nhân dị ứng (BR-063).
 *
 * <p>"Hải sản" là tác nhân Ô (umbrella): học sinh khai dị ứng "Hải sản" phải được cảnh báo với
 * mọi món chứa Tôm/Cua, và ngược lại em khai "Tôm" phải được cảnh báo với món chỉ tag chung
 * "Hải sản". So khớp chuỗi chính xác sẽ bỏ lọt cả hai chiều.
 *
 * <p><b>Vì sao lớp này tồn tại (13/08/2026):</b> luật trên đã có ở frontend
 * ({@code allergenTouches}, lib/domain-utils.ts) nhưng CHƯA có ở backend —
 * {@code AllergyConflictChecker} so khớp bằng {@code Map.get(allergen)} chính xác. Khi chuyển
 * BR-063 về server (bỏ việc tải cả sổ học sinh về trình duyệt), thiếu lớp này thì server sẽ
 * cảnh báo ÍT HƠN frontend cũ — tức là một bước lùi về an toàn, không phải tối ưu.
 *
 * <p>Chuyển từ {@code mf05.service} sang {@code common} ngày 13/08/2026: MF09 (phát suất)
 * cũng phải dùng ĐÚNG luật này, xem {@code StudentDistributionServiceImpl.planDishes()}.
 * Trước đó MF09 so khớp chuỗi chính xác nên bỏ lọt quan hệ Ô — món tag "Hải sản" vẫn phát
 * cho em khai dị ứng "Tôm".
 *
 * <p>⚠️ NỢ KỸ THUẬT — bảng này đang tồn tại ở HAI nơi bằng HAI ngôn ngữ. Đúng ra tác nhân dị
 * ứng phải là một BẢNG {@code allergens(code, parent_code)} như {@code catalog_ingredients}
 * vừa làm với {@code allergens} (V74), để sửa một chỗ là cả hệ thống theo. Ghi nợ cho đợt sau;
 * đổi ở đây thì phải đổi cả {@code ALLERGEN_UMBRELLA} bên frontend.
 */
public final class AllergenSemantics {

    /** Tác nhân con → tác nhân Ô chứa nó. Khớp 1:1 {@code ALLERGEN_UMBRELLA} của frontend. */
    private static final Map<String, String> UMBRELLA = Map.of(
            "Tôm", "Hải sản",
            "Cua", "Hải sản"
    );

    private AllergenSemantics() {
    }

    /**
     * Tác nhân của HỌC SINH có "chạm" tác nhân của MÓN không — chỉ chạm khi trùng nhau, hoặc
     * khi một bên là tác nhân Ô của bên kia.
     */
    public static boolean touches(String studentAllergen, String dishAllergen) {
        if (studentAllergen == null || dishAllergen == null) {
            return false;
        }
        if (studentAllergen.equals(dishAllergen)) {
            return true;
        }
        return dishAllergen.equals(UMBRELLA.get(studentAllergen))
                || studentAllergen.equals(UMBRELLA.get(dishAllergen));
    }
}
