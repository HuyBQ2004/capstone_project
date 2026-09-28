package com.sfms.mf09.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/** {@code distribution_error_reports} — UC-016 AF1 (báo lỗi phân phát sai suất). */
@Entity
@Table(name = "distribution_error_reports",
        uniqueConstraints = @UniqueConstraint(name = "uq_der_school_code", columnNames = {"school_id", "code"}),
        indexes = @Index(name = "ix_der_dist", columnList = "distribution_id"))
public class DistributionErrorReport extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "distribution_id", nullable = false)
    private UUID distributionId;

    @Column(name = "reported_by_user_id", nullable = false)
    private UUID reportedByUserId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private boolean resolved;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt = Instant.now();   // mốc NGHIỆP VỤ, khác created_at (D9)

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

    public UUID getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(UUID distributionId) {
        this.distributionId = distributionId;
    }

    public UUID getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(UUID reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }
}
