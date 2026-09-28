package com.sfms.iam.repository;

import com.sfms.iam.domain.SecurityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SecurityPolicyRepository extends JpaRepository<SecurityPolicy, UUID> {

    /** Chính sách đang áp dụng — nguồn tham số BR-002/004/006 (TDS §17 C3). */
    Optional<SecurityPolicy> findFirstByActiveIsTrue();

    Optional<SecurityPolicy> findByCode(String code);
}
