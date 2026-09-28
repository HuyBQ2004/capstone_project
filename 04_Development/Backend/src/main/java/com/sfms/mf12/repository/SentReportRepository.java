package com.sfms.mf12.repository;

import com.sfms.mf12.domain.ReportType;
import com.sfms.mf12.domain.SentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SentReportRepository
        extends JpaRepository<SentReport, UUID>, JpaSpecificationExecutor<SentReport> {

    /** UC-190 — lịch sử báo cáo đã gửi của trường. */
    List<SentReport> findBySchoolIdOrderBySentAtDesc(UUID schoolId);

    /** D1 — tra theo mã trong phạm vi trường (khớp `UNIQUE(school_id, code)`). */
    Optional<SentReport> findBySchoolIdAndCode(UUID schoolId, String code);

    /** System Admin (không có {@code schoolId} trong JWT) — tra theo mã trên mọi trường. */
    List<SentReport> findByCode(String code);

    /** BR-177 — dọn báo cáo quá hạn lưu trữ (job, mặc định giữ 12 tháng). */
    List<SentReport> findBySentAtBefore(Instant cutoff);

    boolean existsBySchoolIdAndReportTypeAndRangeFromAndRangeTo(
            UUID schoolId, ReportType type, LocalDate from, LocalDate to);
}
