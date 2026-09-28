package com.sfms.iam.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.dto.RoleDtos.PermissionResponse;
import com.sfms.iam.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF01 — danh mục quyền (UC-129). Chỉ đọc, 16 bản ghi cố định (ADR-M04). */
@Tag(name = "MF01 — User Management & Access Control")
@RestController
@RequestMapping("/api/v1/permissions")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(
            summary = "Danh mục quyền hệ thống",
            description = "UC-129 · BR-007 — danh mục cố định 16 bản ghi PERM-001…PERM-016, "
                    + "chỉ đọc; không có endpoint tạo/sửa/xóa")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public PagedResponse<PermissionResponse> list(@RequestParam Map<String, String> allParams) {
        return permissionService.list(ListQuery.from(allParams));
    }
}
