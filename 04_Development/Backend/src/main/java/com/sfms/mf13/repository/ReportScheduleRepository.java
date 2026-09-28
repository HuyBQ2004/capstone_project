package com.sfms.mf13.repository;

import com.sfms.mf13.domain.ReportSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID> {

    Optional<ReportSchedule> findByCodeAndSchoolId(String code, UUID schoolId);

    boolean existsByCodeAndSchoolId(String code, UUID schoolId);

    Page<ReportSchedule> findBySchoolId(UUID schoolId, Pageable pageable);

    Page<ReportSchedule> findAllBy(Pageable pageable);

    /** ADR-M13-7 — nhiều instance chạy song song không gửi trùng. */
    @Query(value = """
            SELECT * FROM report_schedules
            WHERE active = TRUE AND next_run_at <= :now
            ORDER BY next_run_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<ReportSchedule> lockDue(@Param("now") Instant now, @Param("limit") int limit);
}
