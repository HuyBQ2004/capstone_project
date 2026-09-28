package com.sfms.iam.domain;

/**
 * ADR-M03 — vòng đời tài khoản.
 *
 * <pre>
 * [*] → PENDING_VERIFICATION → ACTIVE → INACTIVE  (deactivate quản trị, UC-002/003)
 *                                     → SUSPENDED (đình chỉ đối tác theo partnerId)
 *       INACTIVE  → ACTIVE (reactivate)
 *       SUSPENDED → ACTIVE (gỡ đình chỉ)
 * </pre>
 *
 * <p>Phân biệt {@code INACTIVE} và {@code SUSPENDED} là có chủ đích: khi gỡ đình chỉ NCC,
 * hệ thống phải biết tài khoản nào nên mở lại. Dùng chung một trạng thái sẽ mất phân biệt đó
 * (MF01-TC-INT-014 Expected FAIL).
 */
public enum UserStatus {

    /** Vừa tạo (UC-024/026); chưa xác minh email, chưa có mật khẩu (BR-010/011). */
    PENDING_VERIFICATION,

    /** Đã kích hoạt qua UC-154. Trạng thái duy nhất được phép login (BR-010). */
    ACTIVE,

    /** Bị quản trị vô hiệu hóa (UC-002/003). */
    INACTIVE,

    /** Khóa tạm do đình chỉ đối tác ({@code partnerId}) — cascade từ MF04. */
    SUSPENDED;

    /** BR-010: chỉ ACTIVE + email đã xác minh mới login được. */
    public boolean canLogin() {
        return this == ACTIVE;
    }
}
