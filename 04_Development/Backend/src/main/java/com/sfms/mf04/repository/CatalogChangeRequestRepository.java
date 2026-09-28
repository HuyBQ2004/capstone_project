package com.sfms.mf04.repository;

import com.sfms.mf04.domain.CatalogChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CatalogChangeRequestRepository extends JpaRepository<CatalogChangeRequest, UUID>,
        JpaSpecificationExecutor<CatalogChangeRequest> {

    boolean existsByCode(String code);

    Optional<CatalogChangeRequest> findByCode(String code);
}
