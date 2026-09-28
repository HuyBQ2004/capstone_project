package com.sfms.iam.repository;

import com.sfms.iam.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/** UC-129 — danh mục chỉ đọc; không có phương thức ghi nào được dùng. */
public interface PermissionRepository
        extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {
}
