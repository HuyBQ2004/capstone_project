package com.sfms.iam.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.dto.AuthDtos.AcceptTermsResponse;
import com.sfms.iam.dto.AuthDtos.ChangePasswordRequest;
import com.sfms.iam.dto.AuthDtos.MessageResponse;
import com.sfms.iam.error.IamMessages;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.iam.service.AccountService;
import com.sfms.iam.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** MF01 — thao tác trên tài khoản của chính mình (UC-011, BR-009). §16: mọi vai trò. */
@Tag(name = "MF01 — User Management & Access Control")
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;
    private final AuthService authService;

    public AccountController(AccountService accountService, AuthService authService) {
        this.accountService = accountService;
        this.authService = authService;
    }

    /**
     * {@code userId} lấy từ JWT. Nếu nhận từ body thì giáo viên đổi được mật khẩu của
     * School Admin = chiếm tài khoản quản trị (MF01-TC-SEC-005 mục b).
     */
    @Operation(
            summary = "Đổi mật khẩu của chính mình",
            description = "UC-011 · BR-001 (BCrypt), BR-006 (chính sách + không trùng 3 MK gần nhất), "
                    + "BR-007 — kết thúc mọi phiên khác sau khi đổi")
    @ApiResponse(responseCode = "200", description = "MSG-030 — đổi mật khẩu thành công")
    @ApiResponse(responseCode = "401", description = "IAM-002 — MSG-002 mật khẩu hiện tại sai; "
            + "IAM-005 — MSG-004 thiếu / hết hạn token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "IAM-006policy — MSG-104 mật khẩu yếu; "
            + "IAM-006reuse — MSG-105 trùng 3 MK gần nhất",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/password")
    public MessageResponse changePassword(@CurrentUser AuthUser actor,
                                          @Valid @RequestBody ChangePasswordRequest request,
                                          HttpServletRequest servletRequest) {
        accountService.changePassword(actor.userId(), request.currentPassword(),
                request.newPassword(), AuthController.clientIp(servletRequest));
        return new MessageResponse(IamMessages.MSG_030);
    }

    @Operation(
            summary = "Chấp nhận Chính sách Bảo mật & Điều khoản Sử dụng",
            description = "UC-034 (AF1) · BR-009 — bắt buộc ở lần đăng nhập đầu tiên; "
                    + "sau khi chấp nhận, needTermsAccept của lần login sau là false")
    @ApiResponse(responseCode = "200", description = "Đã ghi nhận (ISO-8601, D4)")
    @ApiResponse(responseCode = "401", description = "IAM-005 — MSG-004",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/accept-terms")
    public AcceptTermsResponse acceptTerms(@CurrentUser AuthUser actor) {
        return new AcceptTermsResponse(authService.acceptTerms(actor.userId()));
    }
}
