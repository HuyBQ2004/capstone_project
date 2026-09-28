package com.sfms.iam.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.dto.RoleDtos.CreateRoleRequest;
import com.sfms.iam.dto.RoleDtos.RoleResponse;
import com.sfms.iam.dto.RoleDtos.UpdateRoleRequest;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.iam.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * MF01 — vai trò RBAC (UC-022/029/098/130). §16: chỉ System Admin.
 *
 * <p>⚠️ {@code @PreAuthorize} ở đây chỉ chặn <b>sai vai trò</b>. Guard BR-008 (role hệ thống
 * bất khả xóa/sửa tên; role còn user không xóa được) nằm ở <b>tầng Service</b> — xem
 * {@link RoleService}. MF01-TC-011 nêu rõ: guard chỉ ở controller thì gọi API trực tiếp
 * bỏ qua UI vẫn xóa được role hệ thống.
 */
@Tag(name = "MF01 — User Management & Access Control")
@RestController
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(
            summary = "Danh sách vai trò",
            description = "UC-130 · BR-007, BR-008 — field `users` tính runtime bằng "
                    + "COUNT(user_accounts WHERE role_id = ...), không lưu cột (ADR-M02-2); "
                    + "đếm cả user INACTIVE")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public PagedResponse<RoleResponse> list(@RequestParam Map<String, String> allParams) {
        return roleService.list(ListQuery.from(allParams));
    }

    @Operation(summary = "Chi tiết một vai trò", description = "UC-130 · BR-008")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "IAM-404 — MSG-137 không tìm thấy",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{code}")
    public RoleResponse get(@PathVariable String code) {
        return roleService.getByCode(code);
    }

    @Operation(
            summary = "Tạo vai trò tùy chỉnh",
            description = "UC-022 · BR-008 — vai trò tạo qua API luôn là custom (system=false); "
                    + "không có đường tạo vai trò hệ thống")
    @ApiResponse(responseCode = "201", description = "MSG-031")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "IAM-409 — tên hoặc body.id đã tồn tại (D5)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request,
                               @CurrentUser AuthUser actor,
                               HttpServletRequest servletRequest) {
        return roleService.create(request, actor, AuthController.clientIp(servletRequest));
    }

    @Operation(
            summary = "Cập nhật vai trò tùy chỉnh",
            description = "UC-098 · BR-008 — vai trò hệ thống bất khả sửa tên; "
                    + "tên vai trò phải duy nhất")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005: vai trò hệ thống (BR-008)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "IAM-404 — MSG-137 không tìm thấy",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "IAM-409 — tên vai trò đã tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{code}")
    public RoleResponse update(@PathVariable String code,
                               @Valid @RequestBody UpdateRoleRequest request,
                               @CurrentUser AuthUser actor,
                               HttpServletRequest servletRequest) {
        return roleService.update(code, request, actor, AuthController.clientIp(servletRequest));
    }

    @Operation(
            summary = "Xóa vai trò tùy chỉnh",
            description = "UC-029 · BR-008 — chặn nếu system=true (403) hoặc còn user gán (409, "
                    + "kể cả user đã vô hiệu hóa)")
    @ApiResponse(responseCode = "200", description = "Đã xóa")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005: vai trò hệ thống (BR-008)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "IAM-404 — MSG-137 không tìm thấy",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "IAM-009 — MSG-106 còn [Number] user gán",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{code}")
    public void delete(@PathVariable String code,
                       @CurrentUser AuthUser actor,
                       HttpServletRequest servletRequest) {
        roleService.delete(code, actor, AuthController.clientIp(servletRequest));
    }
}
