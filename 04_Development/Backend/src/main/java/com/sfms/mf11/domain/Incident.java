package com.sfms.mf11.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
 * {@code incidents} — sự cố ATTP (INC), trung tâm điều tra. TDS-MF11_Backend.md §5.2.
 *
 * <p>ADR-M11-1 — {@code incidentRecordId} BẮT BUỘC, mở từ đúng 1 {@link IncidentRecordDraft}
 * đã {@code COMPLETED}. C10/ADR-M11-8 — sau khi {@code reportExported=true}, mọi ghi (trừ
 * {@code close} và sổ sự kiện) phải bị chặn ở tầng service (409 {@code IEM-RO}).
 */
@Entity
@Table(name = "incidents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_inc_code", columnNames = {"school_id", "code"}),
                @UniqueConstraint(name = "uq_inc_record", columnNames = {"incident_record_id"})},
        indexes = {
                @Index(name = "ix_inc_school_status", columnList = "school_id,status"),
                @Index(name = "ix_inc_reported_at", columnList = "reported_at"),
                @Index(name = "ix_inc_severity", columnList = "school_id,severity")})
public class Incident extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 24)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "incident_record_id", nullable = false)
    private UUID incidentRecordId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "reported_by_label", nullable = false, length = 200)
    private String reportedByLabel;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.SEVERITY_PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> symptoms = new ArrayList<>();

    @Column(name = "suspected_meal_text", length = 300)
    private String suspectedMealText;

    @Column(name = "meal_date")
    private LocalDate mealDate;

    @Column(name = "meal_time_label", length = 60)
    private String mealTimeLabel;

    @Column(name = "first_aid_notes", columnDefinition = "text")
    private String firstAidNotes;

    @Column(name = "clinical_risk_assessment", columnDefinition = "text")
    private String clinicalRiskAssessment;

    @Column(name = "investigation_lab_result", columnDefinition = "text")
    private String investigationLabResult;

    @Column(name = "investigation_lab_result_files", nullable = false)
    private int investigationLabResultFiles;

    @Column(name = "root_cause", columnDefinition = "text")
    private String rootCause;

    @Enumerated(EnumType.STRING)
    @Column(name = "root_cause_status", length = 12)
    private RootCauseStatus rootCauseStatus;

    @Column(name = "evidence_final", nullable = false)
    private boolean evidenceFinal;

    @Column(name = "measures_draft", columnDefinition = "text")
    private String measuresDraft;

    @Column(name = "official_clinical_notes", columnDefinition = "text")
    private String officialClinicalNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "traceability_map_status", nullable = false, length = 12)
    private TraceMapStatus traceabilityMapStatus = TraceMapStatus.COMPILING;

    @Column(name = "report_exported", nullable = false)
    private boolean reportExported;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by_id")
    private UUID closedById;

    @Column(name = "closing_statement", columnDefinition = "text")
    private String closingStatement;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<IncidentAffectedStudent> affectedStudents = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<IncidentSuspectedDish> suspectedDishes = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IncidentQuarantineBatch> quarantineBatches = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IncidentEvidence> evidences = new ArrayList<>();

    /** Append-only (ADR-M11-2) — KHÔNG dùng {@code CascadeType.REMOVE}/cập nhật qua đây. */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", insertable = false, updatable = false)
    @OrderBy("at ASC")
    private List<IncidentEvent> events = new ArrayList<>();

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

    public UUID getIncidentRecordId() {
        return incidentRecordId;
    }

    public void setIncidentRecordId(UUID incidentRecordId) {
        this.incidentRecordId = incidentRecordId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getReportedByLabel() {
        return reportedByLabel;
    }

    public void setReportedByLabel(String reportedByLabel) {
        this.reportedByLabel = reportedByLabel;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
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

    public String getMealTimeLabel() {
        return mealTimeLabel;
    }

    public void setMealTimeLabel(String mealTimeLabel) {
        this.mealTimeLabel = mealTimeLabel;
    }

    public String getFirstAidNotes() {
        return firstAidNotes;
    }

    public void setFirstAidNotes(String firstAidNotes) {
        this.firstAidNotes = firstAidNotes;
    }

    public String getClinicalRiskAssessment() {
        return clinicalRiskAssessment;
    }

    public void setClinicalRiskAssessment(String clinicalRiskAssessment) {
        this.clinicalRiskAssessment = clinicalRiskAssessment;
    }

    public String getInvestigationLabResult() {
        return investigationLabResult;
    }

    public void setInvestigationLabResult(String investigationLabResult) {
        this.investigationLabResult = investigationLabResult;
    }

    public int getInvestigationLabResultFiles() {
        return investigationLabResultFiles;
    }

    public void setInvestigationLabResultFiles(int investigationLabResultFiles) {
        this.investigationLabResultFiles = investigationLabResultFiles;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public RootCauseStatus getRootCauseStatus() {
        return rootCauseStatus;
    }

    public void setRootCauseStatus(RootCauseStatus rootCauseStatus) {
        this.rootCauseStatus = rootCauseStatus;
    }

    public boolean isEvidenceFinal() {
        return evidenceFinal;
    }

    public void setEvidenceFinal(boolean evidenceFinal) {
        this.evidenceFinal = evidenceFinal;
    }

    public String getMeasuresDraft() {
        return measuresDraft;
    }

    public void setMeasuresDraft(String measuresDraft) {
        this.measuresDraft = measuresDraft;
    }

    public String getOfficialClinicalNotes() {
        return officialClinicalNotes;
    }

    public void setOfficialClinicalNotes(String officialClinicalNotes) {
        this.officialClinicalNotes = officialClinicalNotes;
    }

    public TraceMapStatus getTraceabilityMapStatus() {
        return traceabilityMapStatus;
    }

    public void setTraceabilityMapStatus(TraceMapStatus traceabilityMapStatus) {
        this.traceabilityMapStatus = traceabilityMapStatus;
    }

    public boolean isReportExported() {
        return reportExported;
    }

    public void setReportExported(boolean reportExported) {
        this.reportExported = reportExported;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public UUID getClosedById() {
        return closedById;
    }

    public void setClosedById(UUID closedById) {
        this.closedById = closedById;
    }

    public String getClosingStatement() {
        return closingStatement;
    }

    public void setClosingStatement(String closingStatement) {
        this.closingStatement = closingStatement;
    }

    public List<IncidentAffectedStudent> getAffectedStudents() {
        return affectedStudents;
    }

    public List<IncidentSuspectedDish> getSuspectedDishes() {
        return suspectedDishes;
    }

    public List<IncidentQuarantineBatch> getQuarantineBatches() {
        return quarantineBatches;
    }

    public List<IncidentEvidence> getEvidences() {
        return evidences;
    }

    public List<IncidentEvent> getEvents() {
        return events;
    }
}
