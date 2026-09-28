package com.sfms.mf03.domain;

import com.sfms.common.enums.HealthHistoryEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * APPEND-ONLY (BR-172, ADR-M03-4). {@code @Immutable} — Hibernate không sinh {@code UPDATE}
 * cho entity này (bỏ qua dirty-checking hoàn toàn). Tuyến chặn thứ hai độc lập là DB:
 * {@code RULE hh_no_update/hh_no_delete DO INSTEAD NOTHING} + {@code REVOKE UPDATE, DELETE
 * ... FROM sfms_app} (xem {@code V4__mf03_student_parent.sql}).
 *
 * <p>Không có setter cho các field nghiệp vụ (chỉ ghi 1 lần lúc tạo) — không expose
 * {@code PUT}/{@code DELETE} ở tầng controller (405 {@code STU-172}).
 *
 * <p>D6 — {@link HealthHistoryEventType} import từ {@code common/enums}, KHÔNG khai lại.
 */
@Entity
@Table(name = "health_history_events",
        indexes = @Index(name = "ix_hh_student_at", columnList = "student_id,at"))
@Immutable
public class HealthHistoryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(nullable = false, updatable = false)
    private Instant at = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 24)
    private HealthHistoryEventType type;

    @Column(nullable = false, length = 1000, updatable = false)
    private String event;

    @Column(name = "by_user_id", nullable = false, updatable = false)
    private UUID byUserId;

    protected HealthHistoryEvent() {
        // JPA
    }

    public HealthHistoryEvent(UUID studentId, HealthHistoryEventType type, String event, UUID byUserId) {
        this.studentId = studentId;
        this.type = type;
        this.event = event;
        this.byUserId = byUserId;
        this.at = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Instant getAt() {
        return at;
    }

    public HealthHistoryEventType getType() {
        return type;
    }

    public String getEvent() {
        return event;
    }

    public UUID getByUserId() {
        return byUserId;
    }
}
