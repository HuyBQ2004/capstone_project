package com.sfms.mf11.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code incident_records} — biên bản sự cố (REC), soạn thủ công theo mẫu
 * {@code specialreport2173_1.md} (BR-154). TDS-MF11_Backend.md §5.2.
 *
 * <p>ADR-M11-1 — chuỗi bắt buộc {@code RPT (n) → REC (1) → INC (0..1)}: {@link Incident}
 * chỉ được mở từ một REC ở trạng thái {@link RecordDraftStatus#COMPLETED}.
 */
@Entity
@Table(name = "incident_records",
        uniqueConstraints = @UniqueConstraint(name = "uq_rec_code", columnNames = {"school_id", "code"}))
public class IncidentRecordDraft extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 24)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 28)
    private RecordDraftStatus status = RecordDraftStatus.DRAFT;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column(name = "incident_id")
    private UUID incidentId;

    @Column(length = 200)
    private String location;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "pdf_exported", nullable = false)
    private boolean pdfExported;

    /** N-1: các RPT đã gộp vào biên bản này (ADR-M11-1). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "incident_record_sources", joinColumns = @JoinColumn(name = "record_id"))
    @Column(name = "report_id")
    private List<UUID> sourceReportIds = new ArrayList<>();

    public List<UUID> getSourceReportIds() {
        return sourceReportIds;
    }

    public void setSourceReportIds(List<UUID> sourceReportIds) {
        this.sourceReportIds = sourceReportIds;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public RecordDraftStatus getStatus() {
        return status;
    }

    public void setStatus(RecordDraftStatus status) {
        this.status = status;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public boolean isPdfExported() {
        return pdfExported;
    }

    public void setPdfExported(boolean pdfExported) {
        this.pdfExported = pdfExported;
    }
}
