package com.sfms.mf08.repository;

import com.sfms.mf08.domain.ReceivingAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ReceivingAuthorityRepository extends JpaRepository<ReceivingAuthority, UUID>,
        JpaSpecificationExecutor<ReceivingAuthority> {

    Optional<ReceivingAuthority> findByCode(String code);

    boolean existsByCode(String code);
}
