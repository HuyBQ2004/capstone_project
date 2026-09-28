package com.sfms.mf04.repository;

import com.sfms.mf04.domain.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, java.util.UUID>,
        JpaSpecificationExecutor<Partner> {

    /** UC-071/067/049 → MSG-006. */
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByTaxId(String taxId);

    boolean existsByCode(String code);

    Optional<Partner> findByCode(String code);
}
