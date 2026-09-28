package com.sfms.mf06.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;
import java.util.UUID;

/** BR-149 — cách ly là trục ĐỘC LẬP với hạn dùng (ADR-M06-2), value object nullable. */
@Embeddable
public class QuarantineInfo {

    @Column(name = "quarantine_at")
    private Instant at;

    @Column(name = "quarantine_by_user_id")
    private UUID byUserId;

    @Column(name = "quarantine_reason", length = 500)
    private String reason;

    @Column(name = "quarantine_incident_id")
    private UUID incidentId;

    /**
     * V67 (11/08/2026) — phân loại lý do, xem {@link QuarantineCause}. Nullable: bản ghi cách ly
     * trước migration này không có phân loại; service coi {@code null} thận trọng như
     * {@code SUPPLIER_LOT_DEFECT} khi quyết định có chặn NCC giao thêm hay không.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quarantine_cause", length = 24)
    private QuarantineCause cause;

    public QuarantineInfo() {
    }

    public QuarantineInfo(Instant at, UUID byUserId, String reason, UUID incidentId, QuarantineCause cause) {
        this.at = at;
        this.byUserId = byUserId;
        this.reason = reason;
        this.incidentId = incidentId;
        this.cause = cause;
    }

    public Instant getAt() { return at; }
    public void setAt(Instant at) { this.at = at; }
    public UUID getByUserId() { return byUserId; }
    public void setByUserId(UUID byUserId) { this.byUserId = byUserId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }
    public QuarantineCause getCause() { return cause; }
    public void setCause(QuarantineCause cause) { this.cause = cause; }
}
