package com.sfms.common.enums;

/**
 * D6 — trạng thái đăng ký sinh trắc của người giao hàng (MF04).
 *
 * <p>Nhãn FE: Chưa đăng ký / Đang chờ đăng ký / Đã đăng ký.
 *
 * <p>⚠️ MF04 §9.2 chỉ khai 2 giá trị trong khi seed {@code DP-03} đang ở
 * {@code PENDING_REGISTRATION} ⇒ không có chỗ chứa. D6 chốt 3, kèm định nghĩa chuyển trạng thái:
 * {@code POST /delivery-persons/{id}/face} ⇒ {@code PENDING_REGISTRATION};
 * xác thực sinh trắc thành công ⇒ {@code REGISTERED}.
 */
public enum BiometricStatus {
    NOT_REGISTERED,
    PENDING_REGISTRATION,
    REGISTERED
}
