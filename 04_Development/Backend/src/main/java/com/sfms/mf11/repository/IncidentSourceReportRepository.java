package com.sfms.mf11.repository;

import com.sfms.mf11.domain.IncidentSourceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface IncidentSourceReportRepository
        extends JpaRepository<IncidentSourceReport, UUID>, JpaSpecificationExecutor<IncidentSourceReport> {

    Optional<IncidentSourceReport> findByCodeAndSchoolId(String code, UUID schoolId);
}
