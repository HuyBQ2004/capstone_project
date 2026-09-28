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

import java.time.Instant;
import java.util.UUID;

/**
 * {@code corrective_actions} — CAPA, vòng đời 6 trạng thái (BR-216, C6). TDS-MF11 §5.2.
 *
 * <p>{@code incidentId} XOR {@code inspectionId} — đúng 1 nguồn phát sinh (BR-152, DB CHECK
 * {@code ck_capa_source}). 1 Incident = 1 CAPA (partial unique index).
 */
@Entity
@Table(name = "corrective_actions",
        uniqueConstraints = @UniqueConstraint(name = "uq_capa_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_capa_status", columnList = "school_id,status"),
                @Index(name = "ix_capa_incident", columnList = "incident_id"),
                @Index(name = "ix_capa_inspection", columnList = "inspection_id"),
                @Index(name = "ix_capa_assignee", columnList = "assignee_supplier_id")})
public class CorrectiveAction extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 24)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "source_label", nullable = false, length = 200)
    private String sourceLabel;

    @Column(name = "incident_id")
    private UUID incidentId;

    @Column(name = "inspection_id")
    private UUID inspectionId;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "assignee_label", nullable = false, length = 120)
    private String assigneeLabel;

    @Column(name = "assignee_supplier_id")
    private UUID assigneeSupplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CapaStatus status = CapaStatus.DRAFT;

    @Column(name = "submitted_for_approval_at")
    private Instant submittedForApprovalAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "evidence_submitted_at")
    private Instant evidenceSubmittedAt;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "evidence_note", columnDefinition = "text")
    private String evidenceNote;

    @Column(name = "evidence_photos", nullable = false)
    private int evidencePhotos;

    @Column(name = "rework_reason", length = 500)
    private String reworkReason;

    @Column(name = "rework_requested_at")
    private Instant reworkRequestedAt;

    /** @deprecated BR-216 (21/07/2026) bỏ SLA 12h. Nullable, read-only — CF-M11-21(d). */
    @Deprecated
    @Column(length = 40, updatable = false)
    private String deadline;

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

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
    }

    public UUID getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(UUID inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssigneeLabel() {
        return assigneeLabel;
    }

    public void setAssigneeLabel(String assigneeLabel) {
        this.assigneeLabel = assigneeLabel;
    }

    public UUID getAssigneeSupplierId() {
        return assigneeSupplierId;
    }

    public void setAssigneeSupplierId(UUID assigneeSupplierId) {
        this.assigneeSupplierId = assigneeSupplierId;
    }

    public CapaStatus getStatus() {
        return status;
    }

    public void setStatus(CapaStatus status) {
        this.status = status;
    }

    public Instant getSubmittedForApprovalAt() {
        return submittedForApprovalAt;
    }

    public void setSubmittedForApprovalAt(Instant submittedForApprovalAt) {
        this.submittedForApprovalAt = submittedForApprovalAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Instant getEvidenceSubmittedAt() {
        return evidenceSubmittedAt;
    }

    public void setEvidenceSubmittedAt(Instant evidenceSubmittedAt) {
        this.evidenceSubmittedAt = evidenceSubmittedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getEvidenceNote() {
        return evidenceNote;
    }

    public void setEvidenceNote(String evidenceNote) {
        this.evidenceNote = evidenceNote;
    }

    public int getEvidencePhotos() {
        return evidencePhotos;
    }

    public void setEvidencePhotos(int evidencePhotos) {
        this.evidencePhotos = evidencePhotos;
    }

    public String getReworkReason() {
        return reworkReason;
    }

    public void setReworkReason(String reworkReason) {
        this.reworkReason = reworkReason;
    }

    public Instant getReworkRequestedAt() {
        return reworkRequestedAt;
    }

    public void setReworkRequestedAt(Instant reworkRequestedAt) {
        this.reworkRequestedAt = reworkRequestedAt;
    }

    @Deprecated
    public String getDeadline() {
        return deadline;
    }
}
