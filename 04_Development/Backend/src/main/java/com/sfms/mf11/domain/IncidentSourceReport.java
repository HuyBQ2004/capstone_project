package com.sfms.mf11.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code incident_source_reports} — báo cáo thô (RPT) do Teacher/Parent gửi (UC-073/074).
 * TDS-MF11_Backend.md §5.2.
 *
 * <p><b>BR-146 (sửa 20/07/2026, C2):</b> chỉ ghi nhận + phát tín hiệu gợi ý, KHÔNG tự tạo
 * {@link Incident}, KHÔNG tự cách ly lô. Gộp thành {@link IncidentRecordDraft} là thao tác
 * thủ công (Medical Staff).
 */
@Entity
@Table(name = "incident_source_reports",
        uniqueConstraints = @UniqueConstraint(name = "uq_rpt_code", columnNames = {"school_id", "code"}),
        indexes = @Index(name = "ix_rpt_status", columnList = "school_id,status,reported_at"))
public class IncidentSourceReport extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 24)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "reported_by_id", nullable = false)
    private UUID reportedById;

    @Column(name = "reported_by_label", nullable = false, length = 120)
    private String reportedByLabel;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    @Column(name = "class_name", length = 20)
    private String className;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> symptoms = new ArrayList<>();

    @Column(name = "suspected_meal_text", length = 300)
    private String suspectedMealText;

    @Column(name = "meal_date")
    private LocalDate mealDate;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "evidence_photos", nullable = false)
    private int evidencePhotos;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", length = 8)
    private IncidentSeverity severityLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 28)
    private SourceReportStatus status = SourceReportStatus.PENDING_TRIAGE;

    @Column(name = "merged_into_incident_id")
    private UUID mergedIntoIncidentId;

    @Column(name = "draft_record_id")
    private UUID draftRecordId;

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

    public UUID getReportedById() {
        return reportedById;
    }

    public void setReportedById(UUID reportedById) {
        this.reportedById = reportedById;
    }

    public String getReportedByLabel() {
        return reportedByLabel;
    }

    public void setReportedByLabel(String reportedByLabel) {
        this.reportedByLabel = reportedByLabel;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public String getSuspectedMealText() {
        return suspectedMealText;
    }

    public void setSuspectedMealText(String suspectedMealText) {
        this.suspectedMealText = suspectedMealText;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEvidencePhotos() {
        return evidencePhotos;
    }

    public void setEvidencePhotos(int evidencePhotos) {
        this.evidencePhotos = evidencePhotos;
    }

    public IncidentSeverity getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(IncidentSeverity severityLevel) {
        this.severityLevel = severityLevel;
    }

    public SourceReportStatus getStatus() {
        return status;
    }

    public void setStatus(SourceReportStatus status) {
        this.status = status;
    }

    public UUID getMergedIntoIncidentId() {
        return mergedIntoIncidentId;
    }

    public void setMergedIntoIncidentId(UUID mergedIntoIncidentId) {
        this.mergedIntoIncidentId = mergedIntoIncidentId;
    }

    public UUID getDraftRecordId() {
        return draftRecordId;
    }

    public void setDraftRecordId(UUID draftRecordId) {
        this.draftRecordId = draftRecordId;
    }
}
