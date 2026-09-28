package com.sfms.mf10.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/** {@code notification_reads} — CF-M10-10 (đọc theo TỪNG người, không phải cờ toàn cục). */
@Entity
@Table(name = "notification_reads",
        uniqueConstraints = @UniqueConstraint(name = "uq_ntfread", columnNames = {"notification_id", "user_id"}))
public class NotificationRead extends BaseEntity {

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;              // FK user_accounts (M10-D04)

    @Column(name = "read_at", nullable = false)
    private Instant readAt = Instant.now();

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
