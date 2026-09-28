package com.sfms.mf03.repository;

import com.sfms.mf03.domain.AllergyDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllergyDeclarationRepository extends JpaRepository<AllergyDeclaration, UUID>,
        JpaSpecificationExecutor<AllergyDeclaration> {

    /** D1 — tra cứu theo MÃ NGHIỆP VỤ (`id` của JSON), không phải khoá UUID. */
    Optional<AllergyDeclaration> findByCode(String code);

    List<AllergyDeclaration> findByStudentIdAndStatus(UUID studentId, DeclarationStatus status);

    List<AllergyDeclaration> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndStatus(UUID studentId, DeclarationStatus status);

    @Query("select distinct d.allergen from AllergyDeclaration d "
            + "where d.studentId = :sid and d.status = com.sfms.mf03.domain.DeclarationStatus.VERIFIED")
    List<String> findDistinctVerifiedAllergens(@Param("sid") UUID sid);
}
