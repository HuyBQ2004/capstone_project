package com.sfms.mf11.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code incident_events} — sổ sự kiện APPEND-ONLY của một sự cố (ADR-M11-2, C4). Ghi
 * TRONG CÙNG transaction với thay đổi state; RULE ở DB chặn UPDATE/DELETE — entity này
 * KHÔNG {@code extends BaseEntity} (không có audit trail riêng, {@code at}/{@code actorLabel}
 * đã đủ để lần vết) và KHÔNG cung cấp setter cho service gọi sau khi tạo.
 */
@Entity
@Table(name = "incident_events", indexes = @Index(name = "ix_iev_incident_at", columnList = "incident_id,at"))
public class IncidentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 24)
    private String code;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(nullable = false)
    private Instant at;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentEventType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "actor_label", nullable = false, length = 120)
    private String actorLabel;

    protected IncidentEvent() {
        // JPA
    }

    public IncidentEvent(UUID incidentId, String code, Instant at, IncidentEventType type,
                          String description, String actorLabel) {
        this.incidentId = incidentId;
        this.code = code;
        this.at = at;
        this.type = type;
        this.description = description;
        this.actorLabel = actorLabel;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public Instant getAt() {
        return at;
    }

    public IncidentEventType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getActorLabel() {
        return actorLabel;
    }
}
