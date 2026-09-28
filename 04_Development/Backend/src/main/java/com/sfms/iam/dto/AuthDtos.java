package com.sfms.iam.dto;

import com.sfms.common.enums.RoleKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO của nhóm {@code /auth/**} — khớp <b>nguyên văn</b> JSON của TDS-MF01 §9.2.
 *
 * <p>D8/S4: field ngày khai {@code format}; S5: enum dùng kiểu thật ({@link RoleKey});
 * S6: {@code example} của {@code id} là mã nghiệp vụ, không UUID.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    // ───────────────────────── POST /auth/login (UC-034) ─────────────────────────

    @Schema(name = "LoginRequest")
    public record LoginRequest(
            @Schema(example = "toanpq@dtd.edu.vn")
            @NotBlank @Email String email,

            @Schema(example = "P@ssw0rd123")
            @NotBlank String password
    ) {}

    /**
     * TDS §9.2 — <b>đúng 6 khóa</b>. MF01-TC-INT-001 assert không thừa không thiếu.
     *
     * <p>{@code partnerId} bổ sung 29/07/2026 (review B3): với tài khoản NCC/đơn vị nấu ăn,
     * đây là khóa cách ly dữ liệu đa-tenant mà 12 module downstream lọc theo. Thiếu nó ⇒
     * MF04/MF06/MF08 không có khóa cách ly (MF01-TC-SEC-007).
     */
    @Schema(name = "LoginResponse")
    public record LoginResponse(
            @Schema(description = "JWT access token, TTL = SecurityPolicy.sessionTimeoutMinutes (BR-004)")
            String accessToken,

            @Schema(description = "Refresh token opaque, sliding 14 ngày (BR-004 v1.9)")
            String refreshToken,

            @Schema(description = "RoleKey kebab-case (D6) — FE dùng chính giá trị này để routing")
            RoleKey role,

            @Schema(description = "Mã nghiệp vụ của trường (D1), null với System Admin / đối tác",
                    example = "SCH-HN-0042")
            String schoolId,

            @Schema(description = "Mã nghiệp vụ của đối tác (D1), null với tài khoản nội bộ trường",
                    example = "SUP-001")
            String partnerId,

            @Schema(description = "BR-009 — lần login đầu ⇒ true, FE hiện màn chấp nhận điều khoản")
            boolean needTermsAccept
    ) {}

    // ───────────────────────── POST /auth/google (UC-191 — mobile) ─────────────────────────

    /**
     * App mobile (Flutter {@code google_sign_in}) gửi thẳng id_token lấy được từ
     * Google SDK — khác nhánh web (redirect flow, không có endpoint REST tương ứng).
     */
    @Schema(name = "GoogleLoginRequest")
    public record GoogleLoginRequest(
            @NotBlank String idToken
    ) {}

    // ───────────────────────── POST /auth/refresh (BR-004 v1.9) ─────────────────────────

    @Schema(name = "RefreshRequest")
    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}

    /** TDS §9.2 — <b>đúng 2 khóa</b>. */
    @Schema(name = "TokenPairResponse")
    public record TokenPairResponse(
            String accessToken,
            String refreshToken
    ) {}

    // ───────────────────────── POST /auth/logout (UC-035) ─────────────────────────

    /**
     * ⚠️ KHÔNG có field {@code userId}. Identity đọc từ JWT (TDS §17 C2);
     * nhận {@code userId} từ body cho phép logout hộ người khác (MF01-TC-SEC-005 mục c).
     */
    @Schema(name = "MessageResponse")
    public record MessageResponse(
            @Schema(example = "Logout successful. Session terminated safely.")
            String message
    ) {}

    // ───────────────────────── POST /auth/forgot-password (UC-032) ─────────────────────────

    @Schema(name = "ForgotPasswordRequest")
    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    @Schema(name = "ResetPasswordRequest")
    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank String newPassword
    ) {}

    // ───────────────────────── POST /auth/activate (UC-154) ─────────────────────────

    @Schema(name = "ActivateRequest")
    public record ActivateRequest(
            @NotBlank String token,
            @NotBlank String newPassword
    ) {}

    @Schema(name = "ResendActivationRequest")
    public record ResendActivationRequest(
            @NotBlank @Email String email
    ) {}

    // ───────────────────────── PATCH /account/password (UC-011) ─────────────────────────

    /**
     * ⚠️ KHÔNG có {@code userId}. MF01-TC-SEC-005 mục b: nhận {@code userId} từ body ⇒
     * <b>giáo viên đổi được mật khẩu của School Admin</b> = chiếm tài khoản quản trị.
     */
    @Schema(name = "ChangePasswordRequest")
    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank String newPassword
    ) {}

    // ───────────────────────── BR-009 ─────────────────────────

    @Schema(name = "AcceptTermsResponse")
    public record AcceptTermsResponse(
            @Schema(type = "string", format = "date-time", example = "2026-07-31T02:00:00Z")
            java.time.Instant termsAcceptedAt
    ) {}
}
