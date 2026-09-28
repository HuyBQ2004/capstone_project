package com.sfms.iam.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.dto.UserDtos.CreateUserRequest;
import com.sfms.iam.dto.UserDtos.ToggleStatusRequest;
import com.sfms.iam.dto.UserDtos.UserResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.iam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MF01 — quản lý tài khoản phạm vi <b>toàn hệ thống</b> (§16: chỉ System
 * Admin).
 *
 * <p>
 * {@code @PreAuthorize} đặt ở tầng server; không dựa vào việc FE có hiện menu
 * hay không.
 * MF01-TC-SEC-001 kiểm 84 tổ hợp role × endpoint.
 *
 * <p>
 * D1: path param là {@code {code}} (mã nghiệp vụ {@code U-0001}), gọi bằng UUID
 * ⇒ 404.
 */
@Tag(name = "MF01 — User Management & Access Control")
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        @Operation(summary = "Danh sách tài khoản toàn nền tảng", description = "UC-153 · BR-007 — phân trang theo hợp đồng §2b-1; "
                        + "search chỉ chạy trên name, email, phone")
        @ApiResponse(responseCode = "200", description = "Luôn 200 kể cả khi tập rỗng (§2b-6)")
        @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò (BR-007)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @GetMapping
        public PagedResponse<UserResponse> list(@RequestParam Map<String, String> allParams) {
                return userService.list(ListQuery.from(allParams));
        }

        @Operation(summary = "Chi tiết một tài khoản", description = "UC-153 · BR-007 — path param là mã nghiệp vụ (D1), không phải UUID")
        @ApiResponse(responseCode = "200", description = "OK")
        @ApiResponse(responseCode = "404", description = "IAM-404 — MSG-137 không tìm thấy", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @GetMapping("/{code}")
        public UserResponse get(@PathVariable String code) {
                return userService.getByCode(code);
        }

        @Operation(summary = "Tạo tài khoản người dùng", description = "UC-026 · BR-008, BR-010, BR-012, BR-013 — status do server quyết định "
                        + "(PENDING_VERIFICATION); không đặt mật khẩu lúc tạo (chỉ UC-154)")
        @ApiResponse(responseCode = "201", description = "MSG-031 — tạo tài khoản thành công")
        @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò (BR-013)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "409", description = "IAM-006 — MSG-006 email đã dùng (BR-012); "
                        + "IAM-409 — body.id trùng (D5)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "422", description = "IAM-422 — MSG-138 DTO sai (email/SĐT/enum/ngày)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public UserResponse create(@Valid @RequestBody CreateUserRequest request,
                        @CurrentUser AuthUser actor,
                        HttpServletRequest servletRequest) {
                return userService.create(request, actor, AuthController.clientIp(servletRequest));
        }

        @Operation(summary = "Kích hoạt / vô hiệu hóa tài khoản", description = "UC-003 · BR-007, BR-008, BR-004 — vô hiệu hóa revoke refresh token "
                        + "tức thì; KHÔNG được vô hiệu hóa primary System Admin")
        @ApiResponse(responseCode = "200", description = "MSG-029")
        @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò, "
                        + "hoặc đích là primary System Admin (UC-003 exception)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "IAM-404 — MSG-137 không tìm thấy", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @PatchMapping("/{code}/status")
        public com.sfms.iam.dto.AuthDtos.MessageResponse toggleStatus(@PathVariable String code,
                        @Valid @RequestBody ToggleStatusRequest request,
                        @CurrentUser AuthUser actor,
                        HttpServletRequest servletRequest) {
                userService.toggleStatus(code, request.active(), actor,
                                AuthController.clientIp(servletRequest));
                return new com.sfms.iam.dto.AuthDtos.MessageResponse(com.sfms.iam.error.IamMessages.MSG_029);
        }
}
