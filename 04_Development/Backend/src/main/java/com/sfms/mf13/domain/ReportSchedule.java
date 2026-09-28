package com.sfms.mf13.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * UC-173 — cấu hình lịch sinh &amp; gửi báo cáo định kỳ (BR-179). Người nhận KHÔNG lưu ở đây —
 * đọc {@code school_config.report_emails} qua {@link com.sfms.common.reference.ReferenceResolver}
 * (§4.3 Email egress — người nhận không lấy từ request).
 */
@Entity
@Table(name = "report_schedules",
        uniqueConstraints = @UniqueConstraint(name = "uq_rsc_school_code", columnNames = {"school_id", "code"}),
        indexes = @Index(name = "ix_rsc_due", columnList = "active, next_run_at"))
public class ReportSchedule extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Convert(converter = ReportSlugConverter.class)
    @Column(name = "report_slug", nullable = false, length = 40)
    private ReportSlug reportSlug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ReportFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ReportFormat format;

    @Column(name = "time_of_day", nullable = false)
    private LocalTime timeOfDay;

    @Column(name = "day_of_week")
    private Short dayOfWeek;

    @Column(name = "day_of_month")
    private Short dayOfMonth;

    @Column(name = "zone_id", nullable = false, length = 40)
    private String zoneId = "Asia/Ho_Chi_Minh";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "next_run_at", nullable = false)
    private Instant nextRunAt;

    /** FK nghiệp vụ "ai tạo lịch" — tên riêng, không đụng cột {@code created_by} của BaseEntity. */
    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @JsonProperty("id")
    @Schema(example = "RSC-2026-001")
    public String getPublicId() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public ReportSlug getReportSlug() {
        return reportSlug;
    }

    public void setReportSlug(ReportSlug reportSlug) {
        this.reportSlug = reportSlug;
    }

    public ReportFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(ReportFrequency frequency) {
        this.frequency = frequency;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public void setFormat(ReportFormat format) {
        this.format = format;
    }

    public LocalTime getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(LocalTime timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public Short getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Short dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Short getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Short dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
