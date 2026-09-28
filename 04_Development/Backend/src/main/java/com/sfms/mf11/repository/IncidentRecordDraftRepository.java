package com.sfms.mf11.repository;

import com.sfms.mf11.domain.IncidentRecordDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface IncidentRecordDraftRepository
        extends JpaRepository<IncidentRecordDraft, UUID>, JpaSpecificationExecutor<IncidentRecordDraft> {

    Optional<IncidentRecordDraft> findByCodeAndSchoolId(String code, UUID schoolId);
}
