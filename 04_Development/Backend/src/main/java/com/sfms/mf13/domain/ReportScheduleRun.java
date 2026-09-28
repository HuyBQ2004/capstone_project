package com.sfms.mf13.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/** BR-179 — một lượt chạy (attempt) của một {@link ReportSchedule}, retry tối đa 3 lần. */
@Entity
@Table(name = "report_schedule_runs",
        uniqueConstraints = @UniqueConstraint(name = "uq_rsr_schedule_slot_attempt",
                columnNames = {"schedule_id", "scheduled_for", "attempt"}))
public class ReportScheduleRun extends BaseEntity {

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "scheduled_for", nullable = false)
    private Instant scheduledFor;

    @Column(nullable = false)
    private short attempt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ScheduleRunStatus status;

    @Column(name = "report_export_id")
    private UUID reportExportId;

    @Column(name = "recipient_count", nullable = false)
    private int recipientCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Instant getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Instant scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public short getAttempt() {
        return attempt;
    }

    public void setAttempt(short attempt) {
        this.attempt = attempt;
    }

    public ScheduleRunStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleRunStatus status) {
        this.status = status;
    }

    public UUID getReportExportId() {
        return reportExportId;
    }

    public void setReportExportId(UUID reportExportId) {
        this.reportExportId = reportExportId;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
