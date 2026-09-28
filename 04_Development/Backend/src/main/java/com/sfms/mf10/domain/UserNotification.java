package com.sfms.mf10.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** {@code notifications} — UC-058/136, BR-138/158/159. TDS-MF10_Backend.md §5.2 (M10-D01/D07). */
@Entity
@Table(name = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "uq_ntf_code", columnNames = "code"),
        indexes = @Index(name = "ix_ntf_school_sent", columnList = "school_id,sent_at"))
public class UserNotification extends BaseEntity {

    /** M10-D01 — 'NTF-2026-482913'. UNIQUE trên MỘT cột {@code code} chứ không phải
     *  {@code (school_id, code)}: {@code school_id} NULLABLE (thông báo toàn nền tảng) và
     *  UNIQUE không chặn được trùng trên cột NULL. */
    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id")
    private UUID schoolId;            // NULL = thông báo toàn nền tảng

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private NotificationAudience audience;

    /** M10-D08 (D6): RoleKey KEBAB-CASE — ["medical-staff","school-admin"]. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recipient_roles", columnDefinition = "jsonb")
    private List<String> recipientRoles;   // NULL khi audience=ALL

    /** M10-D07 (D6): common/enums/NotificationType — 7 giá trị. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column
    private String link;              // chỉ path nội bộ (§4.3)

    @Column(name = "link_label")
    private String linkLabel;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();

    /** BR-138/158 — M10-D07/M10-D14: dẫn xuất, tính runtime, trả trong JSON. Cảnh báo khẩn
     *  bỏ qua quiet hours VÀ bỏ qua cờ tắt-theo-loại của người dùng. */
    @Transient
    public boolean isCritical() {
        return type == NotificationType.INCIDENT_ALERT
                || type == NotificationType.FOOD_RECALL
                || type == NotificationType.ALLERGY_ALERT;
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

    public NotificationAudience getAudience() {
        return audience;
    }

    public void setAudience(NotificationAudience audience) {
        this.audience = audience;
    }

    public List<String> getRecipientRoles() {
        return recipientRoles;
    }

    public void setRecipientRoles(List<String> recipientRoles) {
        this.recipientRoles = recipientRoles;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkLabel() {
        return linkLabel;
    }

    public void setLinkLabel(String linkLabel) {
        this.linkLabel = linkLabel;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
