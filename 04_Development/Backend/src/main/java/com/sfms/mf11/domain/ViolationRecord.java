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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code violation_records} — biên bản vi phạm cấp ĐƠN VỊ Nhà thầu (BR-183/185/187 sửa
 * 26/07/2026). TDS-MF11 §5.2. 3 cột {@code staffName}/{@code staffPosition}/
 * {@code disciplinaryLevel} chỉ đọc dữ liệu lịch sử (CF-M11-4) — KHÔNG có trong DTO ghi.
 */
@Entity
@Table(name = "violation_records",
        uniqueConstraints = @UniqueConstraint(name = "uq_vio_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_vio_school_date", columnList = "school_id,date"),
                @Index(name = "ix_vio_type_date", columnList = "violation_type,date"),
                @Index(name = "ix_vio_rep_severity", columnList = "caterers_representative_id,severity,date"),
                @Index(name = "ix_vio_incident", columnList = "related_incident_id")})
public class ViolationRecord extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 24)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 20)
    private String type = "Quy trình";

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(name = "recorded_by_id", nullable = false)
    private UUID recordedById;

    @Column(name = "recorded_by_label", nullable = false, length = 120)
    private String recordedByLabel;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ViolationSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResolutionStatus resolution = ResolutionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ViolationStatus status = ViolationStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", length = 24)
    private ViolationType violationType;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "location_of_incident", nullable = false, length = 200)
    private String locationOfIncident;

    @Column(name = "caterers_representative_id", nullable = false)
    private UUID caterersRepresentativeId;

    @Column(name = "caterers_representative_label", nullable = false, length = 160)
    private String caterersRepresentativeLabel;

    @Column(name = "witness_label", length = 120)
    private String witnessLabel;

    @Column(name = "self_reported", nullable = false)
    private boolean selfReported;

    @Column(name = "repeat_suspect", nullable = false)
    private boolean repeatSuspect;

    @Column(name = "related_incident_id")
    private UUID relatedIncidentId;

    @Column(name = "source_inspection_id")
    private UUID sourceInspectionId;

    @Column(name = "pdf_exported", nullable = false)
    private boolean pdfExported;

    @Column(name = "pdf_generated_at")
    private Instant pdfGeneratedAt;

    @Column(name = "pdf_version", nullable = false)
    private int pdfVersion;

    @Column(name = "action_taken", columnDefinition = "text")
    private String actionTaken;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_sanction_level", length = 30)
    private UnitSanctionLevel unitSanctionLevel;

    @Column(name = "handled_by_id")
    private UUID handledById;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /** @deprecated CF-M11-4 — chỉ đọc dữ liệu lịch sử, KHÔNG ghi mới. */
    @Deprecated
    @Column(name = "staff_name", length = 120, updatable = false)
    private String staffName;

    @Deprecated
    @Column(name = "staff_position", length = 60, updatable = false)
    private String staffPosition;

    @Deprecated
    @Column(name = "disciplinary_level", length = 40, updatable = false)
    private String disciplinaryLevel;

    @OneToMany(mappedBy = "violation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ViolationFile> files = new ArrayList<>();

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

    public String getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public UUID getRecordedById() {
        return recordedById;
    }

    public void setRecordedById(UUID recordedById) {
        this.recordedById = recordedById;
    }

    public String getRecordedByLabel() {
        return recordedByLabel;
    }

    public void setRecordedByLabel(String recordedByLabel) {
        this.recordedByLabel = recordedByLabel;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ViolationSeverity severity) {
        this.severity = severity;
    }

    public ResolutionStatus getResolution() {
        return resolution;
    }

    public void setResolution(ResolutionStatus resolution) {
        this.resolution = resolution;
    }

    public ViolationStatus getStatus() {
        return status;
    }

    public void setStatus(ViolationStatus status) {
        this.status = status;
    }

    public ViolationType getViolationType() {
        return violationType;
    }

    public void setViolationType(ViolationType violationType) {
        this.violationType = violationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getLocationOfIncident() {
        return locationOfIncident;
    }

    public void setLocationOfIncident(String locationOfIncident) {
        this.locationOfIncident = locationOfIncident;
    }

    public UUID getCaterersRepresentativeId() {
        return caterersRepresentativeId;
    }

    public void setCaterersRepresentativeId(UUID caterersRepresentativeId) {
        this.caterersRepresentativeId = caterersRepresentativeId;
    }

    public String getCaterersRepresentativeLabel() {
        return caterersRepresentativeLabel;
    }

    public void setCaterersRepresentativeLabel(String caterersRepresentativeLabel) {
        this.caterersRepresentativeLabel = caterersRepresentativeLabel;
    }

    public String getWitnessLabel() {
        return witnessLabel;
    }

    public void setWitnessLabel(String witnessLabel) {
        this.witnessLabel = witnessLabel;
    }

    public boolean isSelfReported() {
        return selfReported;
    }

    public void setSelfReported(boolean selfReported) {
        this.selfReported = selfReported;
    }

    public boolean isRepeatSuspect() {
        return repeatSuspect;
    }

    public void setRepeatSuspect(boolean repeatSuspect) {
        this.repeatSuspect = repeatSuspect;
    }

    public UUID getRelatedIncidentId() {
        return relatedIncidentId;
    }

    public void setRelatedIncidentId(UUID relatedIncidentId) {
        this.relatedIncidentId = relatedIncidentId;
    }

    public UUID getSourceInspectionId() {
        return sourceInspectionId;
    }

    public void setSourceInspectionId(UUID sourceInspectionId) {
        this.sourceInspectionId = sourceInspectionId;
    }

    public boolean isPdfExported() {
        return pdfExported;
    }

    public void setPdfExported(boolean pdfExported) {
        this.pdfExported = pdfExported;
    }

    public Instant getPdfGeneratedAt() {
        return pdfGeneratedAt;
    }

    public void setPdfGeneratedAt(Instant pdfGeneratedAt) {
        this.pdfGeneratedAt = pdfGeneratedAt;
    }

    public int getPdfVersion() {
        return pdfVersion;
    }

    public void setPdfVersion(int pdfVersion) {
        this.pdfVersion = pdfVersion;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public UnitSanctionLevel getUnitSanctionLevel() {
        return unitSanctionLevel;
    }

    public void setUnitSanctionLevel(UnitSanctionLevel unitSanctionLevel) {
        this.unitSanctionLevel = unitSanctionLevel;
    }

    public UUID getHandledById() {
        return handledById;
    }

    public void setHandledById(UUID handledById) {
        this.handledById = handledById;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    @Deprecated
    public String getStaffName() {
        return staffName;
    }

    @Deprecated
    public String getStaffPosition() {
        return staffPosition;
    }

    @Deprecated
    public String getDisciplinaryLevel() {
        return disciplinaryLevel;
    }

    public List<ViolationFile> getFiles() {
        return files;
    }
}
