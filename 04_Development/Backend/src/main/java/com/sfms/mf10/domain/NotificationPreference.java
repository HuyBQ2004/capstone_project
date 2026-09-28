package com.sfms.mf10.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@code notification_preferences} — UC-088, BR-158b. PK = {@code user_id} ⇒ không cần cột
 * {@code code} (M10-D01). KHÔNG {@code extends BaseEntity}: PK không phải UUID sinh mới, và
 * DDL vẫn có đủ 4 cột audit (D9) — entity này tự giữ vì không kế thừa.
 */
@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @Column(name = "user_id")
    private UUID userId;              // 1 dòng / người — FK user_accounts

    /** M10-D07: 7 khóa của common/enums/NotificationType. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "type_enabled", columnDefinition = "jsonb", nullable = false)
    private Map<String, Boolean> typeEnabled = new LinkedHashMap<>();

    @Column(name = "email_fallback", nullable = false)
    private boolean emailFallback = true;     // BR-159

    @Column(name = "quiet_start", nullable = false)
    private LocalTime quietStart = LocalTime.of(22, 0);   // BR-158

    @Column(name = "quiet_end", nullable = false)
    private LocalTime quietEnd = LocalTime.of(6, 0);

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 180)
    private String createdBy;

    @Column(name = "updated_by", length = 180)
    private String updatedBy;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Map<String, Boolean> getTypeEnabled() {
        return typeEnabled;
    }

    public void setTypeEnabled(Map<String, Boolean> typeEnabled) {
        this.typeEnabled = typeEnabled == null ? new LinkedHashMap<>() : typeEnabled;
    }

    public boolean isEmailFallback() {
        return emailFallback;
    }

    public void setEmailFallback(boolean emailFallback) {
        this.emailFallback = emailFallback;
    }

    public LocalTime getQuietStart() {
        return quietStart;
    }

    public void setQuietStart(LocalTime quietStart) {
        this.quietStart = quietStart;
    }

    public LocalTime getQuietEnd() {
        return quietEnd;
    }

    public void setQuietEnd(LocalTime quietEnd) {
        this.quietEnd = quietEnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
