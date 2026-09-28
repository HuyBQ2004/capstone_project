package com.sfms.mf11.repository;

import com.sfms.mf11.domain.ViolationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViolationRecordRepository
        extends JpaRepository<ViolationRecord, UUID>, JpaSpecificationExecutor<ViolationRecord> {

    Optional<ViolationRecord> findByCodeAndSchoolId(String code, UUID schoolId);

    /** BR-187 — tiền lệ tái diễn: cùng đại diện, mức ≥ MEDIUM, trong khoảng ngày (90 ngày lookback). */
    List<ViolationRecord> findByCaterersRepresentativeIdAndDateBetween(
            UUID caterersRepresentativeId, LocalDate from, LocalDate to);

    List<ViolationRecord> findBySourceInspectionId(UUID inspectionId);

    /** BR-214 (MF12 §9.4b, `ComplianceDataPort.violationsIn`) — nguồn báo cáo vi phạm theo kỳ. */
    List<ViolationRecord> findBySchoolIdAndDateBetweenOrderByDateAsc(UUID schoolId, LocalDate from, LocalDate to);
}
