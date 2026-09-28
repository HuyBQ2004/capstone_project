package com.sfms.mf03.repository;

import com.sfms.mf03.domain.ChronicConditionDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChronicConditionDeclarationRepository
        extends JpaRepository<ChronicConditionDeclaration, UUID>,
        JpaSpecificationExecutor<ChronicConditionDeclaration> {

    /** D1 — tra cứu theo MÃ NGHIỆP VỤ (`id` của JSON), không phải khoá UUID. */
    Optional<ChronicConditionDeclaration> findByCode(String code);

    List<ChronicConditionDeclaration> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndStatus(UUID studentId, DeclarationStatus status);
}
