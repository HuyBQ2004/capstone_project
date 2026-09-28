package com.sfms.mf09.repository;

import com.sfms.mf09.domain.DistributionErrorReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DistributionErrorReportRepository extends JpaRepository<DistributionErrorReport, UUID> {

    /** AF1 UC-016 — còn báo lỗi chưa xử lý cho phiên này (SRV-023). */
    boolean existsByDistributionIdAndResolvedFalse(UUID distributionId);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);
}
