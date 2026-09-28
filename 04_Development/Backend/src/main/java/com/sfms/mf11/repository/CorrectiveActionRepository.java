package com.sfms.mf11.repository;

import com.sfms.mf11.domain.CorrectiveAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CorrectiveActionRepository
        extends JpaRepository<CorrectiveAction, UUID>, JpaSpecificationExecutor<CorrectiveAction> {

    Optional<CorrectiveAction> findByCodeAndSchoolId(String code, UUID schoolId);

    Optional<CorrectiveAction> findByIncidentId(UUID incidentId);

    Optional<CorrectiveAction> findByInspectionId(UUID inspectionId);
}
