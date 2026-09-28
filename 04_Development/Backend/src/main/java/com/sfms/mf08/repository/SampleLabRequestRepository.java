package com.sfms.mf08.repository;

import com.sfms.mf08.domain.SampleLabRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SampleLabRequestRepository extends JpaRepository<SampleLabRequest, UUID>,
        JpaSpecificationExecutor<SampleLabRequest> {

    Optional<SampleLabRequest> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    List<SampleLabRequest> findByIncidentIdOrderByRequestedAtDesc(UUID incidentId);
}
