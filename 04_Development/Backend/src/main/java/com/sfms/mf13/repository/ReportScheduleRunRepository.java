package com.sfms.mf13.repository;

import com.sfms.mf13.domain.ReportScheduleRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportScheduleRunRepository extends JpaRepository<ReportScheduleRun, UUID> {

    Optional<ReportScheduleRun> findTopByScheduleIdAndScheduledForOrderByAttemptDesc(
            UUID scheduleId, Instant scheduledFor);

    Page<ReportScheduleRun> findByScheduleIdOrderByScheduledForDesc(UUID scheduleId, Pageable p);

    List<ReportScheduleRun> findByScheduleIdAndScheduledForOrderByAttemptAsc(UUID scheduleId, Instant scheduledFor);
}
