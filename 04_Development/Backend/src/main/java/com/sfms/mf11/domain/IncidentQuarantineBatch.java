package com.sfms.mf11.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code incident_quarantine_batches} — lô nguyên liệu bị cách ly, MỘT CHIỀU (BR-149/BR-217,
 * ADR-M11-3): không có cột {@code releasedAt}, RULE DB chặn DELETE. TDS-MF11 §5.2.
 */
@Entity
@Table(name = "incident_quarantine_batches",
        uniqueConstraints = @UniqueConstraint(name = "uq_iqb_inc_batch", columnNames = {"incident_id", "batch_id"}),
        indexes = @Index(name = "ix_iqb_batch", columnList = "batch_id"))
public class IncidentQuarantineBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "batch_code", nullable = false, length = 40)
    private String batchCode;

    @Column(nullable = false, length = 300)
    private String reason;

    /**
     * V67 (11/08/2026) — bản sao audit của {@code inventory_batches.quarantine_cause} (MF06,
     * chủ ghi thật qua {@code InventoryBatchPortImpl.markQuarantined}). Lưu song song ở đây để
     * màn chi tiết sự cố hiển thị được mà không cần gọi chéo sang MF06 (ADR-M11-7).
     */
    @Column(length = 24)
    private String cause;

    @Column(name = "quarantined_at", nullable = false)
    private Instant quarantinedAt = Instant.now();

    @Column(name = "quarantined_by_id", nullable = false)
    private UUID quarantinedById;

    public UUID getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Instant getQuarantinedAt() {
        return quarantinedAt;
    }

    public void setQuarantinedAt(Instant quarantinedAt) {
        this.quarantinedAt = quarantinedAt;
    }

    public UUID getQuarantinedById() {
        return quarantinedById;
    }

    public void setQuarantinedById(UUID quarantinedById) {
        this.quarantinedById = quarantinedById;
    }
}
