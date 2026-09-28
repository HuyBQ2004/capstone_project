package com.sfms.iam.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.RoleKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Vai trò RBAC (BR-007, BR-008).
 *
 * <p><b>Không có cột {@code users}.</b> Số người dùng là <b>field dẫn xuất</b>, tính runtime
 * bằng {@code COUNT(user_accounts WHERE role_id = ...)} — ADR-M02-2 cấm lưu field dẫn xuất.
 * MF01-TC-INT-013 assert điều này qua {@code information_schema.columns}.
 */
@Entity
@Table(name = "roles")
public class RoleRecord extends BaseEntity {

    /** D1 — mã nghiệp vụ công khai: {@code ROLE-01}. */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    /** Nhãn BR-007 để hiển thị ({@code "School Administration"}). UC-098: phải duy nhất. */
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    /**
     * D6 — giá trị dùng trong JWT/JSON/URL ({@code school-admin}). Chỉ 7 role hệ thống có;
     * role custom là {@code null} (không map vào RBAC nền).
     */
    @Column(name = "role_key", length = 32)
    private String roleKey;

    /** BR-008 — {@code true} ⇒ bất khả xóa và bất khả sửa tên. */
    @Column(name = "system", nullable = false)
    private boolean system;

    /** ADR-M04 — danh sách mã {@code PERM-*}, lưu JSONB đúng như mô hình mock. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", nullable = false, columnDefinition = "jsonb")
    private List<String> permissions = new ArrayList<>();

    /** D1 — JSON trả {@code code} dưới tên {@code id}; UUID nội bộ không lộ ra API. */
    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    public RoleKey roleKeyEnum() {
        return roleKey == null ? null : RoleKey.from(roleKey);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions == null ? new ArrayList<>() : new ArrayList<>(permissions);
    }
}
