package com.sfms.mf03.mapper;

import com.sfms.mf03.domain.DeclarationStatus;

import java.util.List;

/**
 * ADR-M03-3 — {@code healthStatus} là DẪN XUẤT (KHÔNG lưu cột, CF-M03-10). Công thức:
 * {@code NEEDS_INFO > PENDING > VERIFIED}; không có khai báo nào (allergy lẫn chronic
 * condition) ⇒ {@code VERIFIED} (khớp seed mock — HS không dị ứng vẫn "đã duyệt").
 *
 * <p>Tách thành class riêng (không nhét vào mapper/service) để test đơn vị trực tiếp
 * (MF03-TC-004, Decision Table 6 hàng).
 */
public final class HealthStatusCalculator {

    /**
     * Giá trị DẪN XUẤT riêng cho trường hợp "chưa khai báo gì" — KHÔNG phải hằng của
     * {@link DeclarationStatus} (enum đó dùng cho bản ghi khai báo THẬT và có CHECK ở DB).
     *
     * <p>Vì sao tách (08/08/2026): {@link #compute} trả {@code VERIFIED} khi học sinh không có
     * khai báo nào — luật này kế thừa từ seed mock ("HS không dị ứng vẫn đã duyệt"). Trên màn
     * Học sinh của BGH, học sinh vừa tạo xong đã mang nhãn <b>"Đã xác minh"</b> ở cột Hồ sơ sức
     * khỏe dù chưa ai khai gì và Y tế chưa xem. Đây đúng lớp lỗi "chưa khai ≠ đã xác minh" mà
     * dự án đã bỏ ở {@code NUTRITION_TARGET} — và ở đây nguy hiểm hơn vì nó nói về hồ sơ dị ứng.
     */
    public static final String NO_DECLARATION = "NO_DECLARATION";

    private HealthStatusCalculator() {
    }

    /**
     * Nhãn trạng thái hồ sơ sức khỏe để TRẢ RA API — phân biệt "chưa khai báo" với "đã xác minh".
     *
     * <p>{@link #compute} giữ nguyên hành vi cũ (còn dùng ở nơi cần đúng một giá trị
     * {@link DeclarationStatus}, và MF03-TC-004 khoá bảng quyết định 6 hàng của nó).
     */
    public static String computeLabel(List<DeclarationStatus> allergyStatuses,
                                       List<DeclarationStatus> chronicStatuses) {
        boolean noneDeclared = (allergyStatuses == null || allergyStatuses.isEmpty())
                && (chronicStatuses == null || chronicStatuses.isEmpty());
        return noneDeclared ? NO_DECLARATION : compute(allergyStatuses, chronicStatuses).name();
    }

    /**
     * @param allergyStatuses status của mọi {@code allergy_declarations} của học sinh
     * @param chronicStatuses status của mọi {@code chronic_condition_declarations} của học sinh
     *                        (công thức phải quét CẢ HAI bảng — MF03-TC-004 hàng #6)
     */
    public static DeclarationStatus compute(List<DeclarationStatus> allergyStatuses,
                                             List<DeclarationStatus> chronicStatuses) {
        boolean anyNeedsInfo = contains(allergyStatuses, DeclarationStatus.NEEDS_INFO)
                || contains(chronicStatuses, DeclarationStatus.NEEDS_INFO);
        if (anyNeedsInfo) {
            return DeclarationStatus.NEEDS_INFO;
        }
        boolean anyPending = contains(allergyStatuses, DeclarationStatus.PENDING)
                || contains(chronicStatuses, DeclarationStatus.PENDING);
        if (anyPending) {
            return DeclarationStatus.PENDING;
        }
        // VERIFIED nếu có >=1 VERIFIED, HOẶC không có khai báo nào cả (tập rỗng).
        return DeclarationStatus.VERIFIED;
    }

    private static boolean contains(List<DeclarationStatus> statuses, DeclarationStatus target) {
        return statuses != null && statuses.stream().anyMatch(s -> s == target);
    }
}
