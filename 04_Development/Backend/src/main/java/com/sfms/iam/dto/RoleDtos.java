package com.sfms.iam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** DTO của {@code /roles} và {@code /permissions} — khớp JSON TDS §9.2. */
public final class RoleDtos {

    private RoleDtos() {
    }

    @Schema(name = "CreateRoleRequest")
    public record CreateRoleRequest(
            @Schema(description = "D5 — mã client tự sinh", example = "ROLE-09")
            String id,

            @NotBlank @Size(max = 100)
            @Schema(example = "Kitchen Auditor") String name,

            @NotBlank @Size(max = 255)
            @Schema(example = "Vai trò tùy chỉnh — kiểm toán bếp độc lập") String description,

            @Schema(description = "Danh sách mã PERM-* (ADR-M04)")
            List<String> permissions
    ) {}

    /**
     * UC-098 — chỉ role custom mới sửa được. Với role {@code system=true}, dịch vụ chặn
     * ngay ở tầng Service với 403 IAM-004 (BR-008, MF01-TC-013).
     */
    @Schema(name = "UpdateRoleRequest")
    public record UpdateRoleRequest(
            @Size(max = 100) String name,
            @Size(max = 255) String description,
            List<String> permissions
    ) {}

    /**
     * TDS §9.2 — {@code users} là <b>field dẫn xuất</b>, tính runtime bằng
     * {@code COUNT(user_accounts WHERE role_id = ...)}, KHÔNG lưu cột (ADR-M02-2).
     *
     * <p>Nó <b>bắt buộc</b> phải có trong response, không phải tùy chọn: màn
     * {@code system-admin/roles} hiện cột "Số người dùng" và guard xóa role đọc chính
     * giá trị này để chặn (MF01-TC-INT-013).
     */
    @Schema(name = "RoleResponse")
    public record RoleResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "ROLE-05") String id,
            @Schema(example = "Teacher") String name,
            @Schema(example = "Giáo viên chủ nhiệm — phân phát bữa ăn") String description,
            @Schema(description = "BR-008 — true ⇒ bất khả xóa/sửa tên") boolean system,
            @Schema(description = "Field dẫn xuất, đếm cả user INACTIVE (BR-008)", example = "42")
            long users,
            @Schema(description = "Danh sách mã PERM-*") List<String> permissions
    ) {}

    @Schema(name = "PermissionResponse")
    public record PermissionResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "PERM-001") String id,
            @Schema(example = "user.create") String name,
            @Schema(example = "User Management") String module,
            @Schema(example = "Tạo tài khoản người dùng (UC-026)") String description
    ) {}
}
