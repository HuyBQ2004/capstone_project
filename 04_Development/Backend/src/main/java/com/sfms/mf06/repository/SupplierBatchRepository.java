package com.sfms.mf06.repository;

import com.sfms.mf06.domain.SupplierBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SupplierBatchRepository extends JpaRepository<SupplierBatch, UUID>,
        JpaSpecificationExecutor<SupplierBatch> {

    Optional<SupplierBatch> findByPartnerIdAndCode(UUID partnerId, String code);

    Optional<SupplierBatch> findByCode(String code);

    boolean existsByPartnerIdAndCode(UUID partnerId, String code);
}
