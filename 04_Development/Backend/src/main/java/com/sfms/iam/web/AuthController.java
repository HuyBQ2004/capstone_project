package com.sfms.iam.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.dto.AuthDtos.ActivateRequest;
import com.sfms.iam.dto.AuthDtos.ForgotPasswordRequest;
import com.sfms.iam.dto.AuthDtos.GoogleLoginRequest;
import com.sfms.iam.dto.AuthDtos.LoginRequest;
import com.sfms.iam.dto.AuthDtos.LoginResponse;
import com.sfms.iam.dto.AuthDtos.MessageResponse;
import com.sfms.iam.dto.AuthDtos.RefreshRequest;
import com.sfms.iam.dto.AuthDtos.ResendActivationRequest;
import com.sfms.iam.dto.AuthDtos.ResetPasswordRequest;
import com.sfms.iam.dto.AuthDtos.TokenPairResponse;
import com.sfms.iam.error.IamMessages;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.iam.service.AccountActivationService;
import com.sfms.iam.service.AuthService;
import com.sfms.iam.service.OAuth2LoginService;
import com.sfms.iam.service.PasswordResetService;
import com.sfms.iam.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MF01 — xác thực. D3: mọi path có tiền tố {@code /api/v1}.
 *
 * <p>D8/S1: một {@code @Tag} cho cả controller. S2: mỗi {@code @Operation.description} chứa
 * UC ID + BR ID. S3: mỗi mã lỗi của TDS §10 có một {@code @ApiResponse} trỏ
 * {@link ErrorResponse}. S8: endpoint public khai {@code @SecurityRequirements} <b>rỗng</b>
 * để ghi đè bearer toàn cục.
 */
@Tag(name = "MF01 — User Management & Access Control")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final AccountActivationService activationService;
    private final OAuth2LoginService oauth2LoginService;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService,
                          PasswordResetService passwordResetService,
                          AccountActivationService activationService,
                          OAuth2LoginService oauth2LoginService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetService = passwordResetService;
        this.activationService = activationService;
        this.oauth2LoginService = oauth2LoginService;
    }

    // ───────────────────────── UC-034 Login ─────────────────────────

    @Operation(
            summary = "Đăng nhập bằng email và mật khẩu",
            description = "UC-034 · BR-001, BR-002, BR-004, BR-005, BR-009, BR-010")
    @ApiResponse(responseCode = "200", description = "MSG-026 — đăng nhập thành công")
    @ApiResponse(responseCode = "401", description = "IAM-002 — MSG-002 sai thông tin đăng nhập; "
            + "details chứa attemptsLeft (BR-002)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "IAM-003 — MSG-003 tài khoản chưa xác minh / "
            + "đã vô hiệu hóa (BR-010)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "IAM-422 — MSG-138 DTO sai định dạng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "423", description = "IAM-007 — MSG-076 tài khoản bị khóa (BR-002)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest servletRequest) {
        return authService.login(request.email(), request.password(), clientIp(servletRequest));
    }

    // ───────────────────────── UC-191 Login with Google — mobile ─────────────────────────

    /**
     * App mobile (Flutter {@code google_sign_in}) không đi qua redirect flow của
     * {@code GoogleOAuth2SuccessHandler} — SDK trả thẳng id_token, app gửi lên đây.
     * BR-218 vẫn áp nguyên vẹn: chỉ liên kết tài khoản có sẵn, KHÔNG tự tạo mới.
     */
    @Operation(
            summary = "Đăng nhập Google từ app mobile (id_token)",
            description = "UC-191 · BR-218 — id_token lấy từ Google SDK phía client, "
                    + "backend tự xác minh chữ ký (KHÔNG qua redirect flow của bản web)")
    @ApiResponse(responseCode = "200", description = "MSG-026 — đăng nhập thành công")
    @ApiResponse(responseCode = "401", description = "IAM-103 — MSG-103 id_token không hợp lệ / "
            + "email chưa verified / sub lệch",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "IAM-102 — MSG-102 email chưa có tài khoản (BR-218)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "423", description = "IAM-007 — MSG-076 tài khoản bị khóa (BR-002)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping("/google")
    public LoginResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request,
                                         HttpServletRequest servletRequest) {
        return oauth2LoginService.loginWithIdToken(request.idToken(), clientIp(servletRequest));
    }

    // ───────────────────────── BR-004 v1.9 Refresh ─────────────────────────

    @Operation(
            summary = "Đổi refresh token lấy access token mới (trượt hạn 14 ngày)",
            description = "UC-034 · BR-004 (v1.9), BR-005 — refresh sliding; token cũ bị revoke")
    @ApiResponse(responseCode = "200", description = "Cấp cặp token mới")
    @ApiResponse(responseCode = "401", description = "IAM-008 — MSG-004 token hết hạn / đã rotate / "
            + "đã revoke (BR-003/004/011)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping("/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshTokenService.rotate(request.refreshToken());
    }

    // ───────────────────────── UC-035 Logout ─────────────────────────

    /**
     * {@code userId} lấy từ JWT qua {@link CurrentUser} — KHÔNG từ body (TDS §17 C2).
     * Idempotent: gọi lần hai vẫn 200, không tạo dòng revoke mới.
     */
    @Operation(
            summary = "Đăng xuất, vô hiệu hóa refresh token",
            description = "UC-035 · BR-004 — revoke refresh ngay khi logout thủ công")
    @ApiResponse(responseCode = "200", description = "MSG-027 — đăng xuất thành công")
    @ApiResponse(responseCode = "401", description = "IAM-005 — MSG-004 thiếu / hết hạn access token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/logout")
    public MessageResponse logout(@CurrentUser AuthUser actor, HttpServletRequest servletRequest) {
        authService.logout(actor.userId(), actor.email(), clientIp(servletRequest));
        return new MessageResponse(IamMessages.MSG_027);
    }

    // ───────────────────────── UC-032 Forgot / Reset Password ─────────────────────────

    /**
     * Luôn trả 200 với cùng body — email tồn tại, email lạ, tài khoản INACTIVE hay đang bị
     * rate-limit đều không phân biệt được (TDS §8.1, MF01-TC-SEC-011).
     */
    @Operation(
            summary = "Yêu cầu đặt lại mật khẩu",
            description = "UC-032 · BR-003 — token 1 giờ, tối đa 1 yêu cầu / 15 phút / email. "
                    + "Không tiết lộ email có tồn tại hay không.")
    @ApiResponse(responseCode = "200", description = "Đã tiếp nhận (không xác nhận email tồn tại)")
    @ApiResponse(responseCode = "422", description = "IAM-422 — MSG-138 email sai định dạng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.request(request.email());
        return new MessageResponse(IamMessages.MSG_028);
    }

    @Operation(
            summary = "Đặt lại mật khẩu bằng token",
            description = "UC-032 · BR-001 (BCrypt), BR-003 (token 1 giờ, dùng 1 lần), "
                    + "BR-006 (chính sách + không trùng 3 MK gần nhất)")
    @ApiResponse(responseCode = "200", description = "MSG-030 — đổi mật khẩu thành công")
    @ApiResponse(responseCode = "401", description = "IAM-008 — MSG-004 token hết hạn / đã dùng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "IAM-006policy — MSG-104 mật khẩu yếu; "
            + "IAM-006reuse — MSG-105 trùng 3 MK gần nhất",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                         HttpServletRequest servletRequest) {
        passwordResetService.reset(request.token(), request.newPassword(), clientIp(servletRequest));
        return new MessageResponse(IamMessages.MSG_030);
    }

    // ───────────────────────── UC-154 Activate Account ─────────────────────────

    /**
     * ⚠️ <b>Không trả success message</b> — TestSpec §2 L9: UC file nói MSG-031
     * ("account created"), traceability nói MSG-028 ("verification email has been sent"),
     * cả hai đều không mang nghĩa "kích hoạt xong". Chờ Huy/Nam cấp MSG đúng ngữ nghĩa.
     */
    @Operation(
            summary = "Kích hoạt tài khoản và đặt mật khẩu lần đầu",
            description = "UC-154 · BR-010, BR-011 (token 48 giờ, dùng 1 lần) · BR-001 (BCrypt) "
                    + "· BR-006 (chính sách mật khẩu)")
    @ApiResponse(responseCode = "200", description = "Tài khoản chuyển sang ACTIVE, mật khẩu đã đặt")
    @ApiResponse(responseCode = "401", description = "IAM-008 — MSG-004 token hết hạn / đã dùng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "IAM-006policy — MSG-104 mật khẩu yếu",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping("/activate")
    public MessageResponse activate(@Valid @RequestBody ActivateRequest request,
                                    HttpServletRequest servletRequest) {
        activationService.activate(request.token(), request.newPassword(), clientIp(servletRequest));
        return new MessageResponse(null);
    }

    @Operation(
            summary = "Gửi lại link kích hoạt",
            description = "UC-154 (AF1) · BR-011 — cấp token MỚI, gia hạn thêm 48 giờ; "
                    + "token cũ bị vô hiệu")
    @ApiResponse(responseCode = "200", description = "Đã tiếp nhận (không xác nhận email tồn tại)")
    @SecurityRequirements
    @PostMapping("/activate/resend")
    public MessageResponse resendActivation(@Valid @RequestBody ResendActivationRequest request) {
        activationService.resend(request.email());
        return new MessageResponse(IamMessages.MSG_028);
    }

    /** UC-106 — audit log ghi IP. Ưu tiên {@code X-Forwarded-For} khi chạy sau Cloudflare. */
    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
