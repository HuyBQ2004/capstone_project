package com.sfms.iam.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Quyền — danh mục <b>cố định 16 bản ghi, chỉ đọc</b> (UC-129, ADR-M04).
 * Không có endpoint tạo/sửa/xóa; seed ở {@code V2__seed_roles_permissions_policy.sql}.
 */
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    /** D1 — {@code PERM-001}. */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 80, unique = true)
    private String name;

    @Column(name = "module", nullable = false, length = 60)
    private String module;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
