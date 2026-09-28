package com.sfms.iam.repository;

import com.sfms.iam.domain.RoleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/** TDS §8.2. */
public interface RoleRecordRepository
        extends JpaRepository<RoleRecord, UUID>, JpaSpecificationExecutor<RoleRecord> {

    Optional<RoleRecord> findByName(String name);

    /** D1 — path param {@code /roles/ROLE-01}. */
    Optional<RoleRecord> findByCode(String code);

    /** D6 — tra role hệ thống theo {@code RoleKey} khi tạo user/staff. */
    Optional<RoleRecord> findByRoleKey(String roleKey);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    long countBySystemIsTrue();
}
