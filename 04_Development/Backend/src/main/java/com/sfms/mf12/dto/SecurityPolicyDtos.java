package com.sfms.mf12.dto;

/** UC-184 — chính sách bảo mật (BR-002/004/006). Bảng+seed thuộc MF01 (CF-6), MF12 sở hữu endpoint. */
public final class SecurityPolicyDtos {

    private SecurityPolicyDtos() {
    }

    public record SecurityPolicyResponse(
            String id,
            int maxLoginAttempts,
            int lockoutMinutes,
            int sessionTimeoutMinutes,
            int passwordMinLength,
            boolean requireSpecialChar,
            boolean requireNumber,
            boolean requireMixedCase,
            boolean noReuseLast3
    ) {}

    /** BR-002/004/006 — 8 tham số ngưỡng; {@code updatedBy}/{@code updatedAt} KHÔNG nhận từ client (D9/D12). */
    public record UpdateSecurityPolicyRequest(
            Integer maxLoginAttempts,
            Integer lockoutMinutes,
            Integer sessionTimeoutMinutes,
            Integer passwordMinLength,
            Boolean requireSpecialChar,
            Boolean requireNumber,
            Boolean requireMixedCase,
            Boolean noReuseLast3
    ) {}

    /**
     * BR-006/BR-002 — GET /public/password-policy (CF-24), KHÔNG cần JWT. Tập con KHÔNG nhạy
     * cảm: KHÔNG có {@code sessionTimeoutMinutes}/{@code updatedBy}/{@code updatedAt}/{@code id}
     * (L21/CF-20). {@code maxLoginAttempts}/{@code lockoutMinutes} CÓ mặt — cần để màn đăng
     * nhập hiển thị "còn N lần thử" (BR-002), không phải ngưỡng nhạy cảm.
     */
    public record PublicPasswordPolicyResponse(
            int passwordMinLength,
            boolean requireSpecialChar,
            boolean requireNumber,
            boolean requireMixedCase,
            boolean noReuseLast3,
            int maxLoginAttempts,
            int lockoutMinutes
    ) {}
}
