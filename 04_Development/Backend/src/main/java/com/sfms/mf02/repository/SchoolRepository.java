package com.sfms.mf02.repository;

import com.sfms.mf02.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/** TDS-MF02 §8.2. */
public interface SchoolRepository extends JpaRepository<School, UUID>, JpaSpecificationExecutor<School> {

    /** D1 — path param {@code /schools/SCH-HN-0042}. */
    Optional<School> findByCode(String code);

    boolean existsByCode(String code);

    /** BR-028 (định dạng) + UNIQUE — dùng cho D5 (409 khi trùng). */
    boolean existsByTaxId(String taxId);
}
