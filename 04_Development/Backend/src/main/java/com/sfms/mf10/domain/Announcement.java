package com.sfms.mf10.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** {@code announcements} — UC-017/089/105/125/126, BR-138/144. TDS-MF10_Backend.md §5.2. */
@Entity
@Table(name = "announcements",
        uniqueConstraints = @UniqueConstraint(name = "uq_ann_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_ann_school_status", columnList = "school_id,status"),
                @Index(name = "ix_ann_published", columnList = "school_id,published_at")
        })
public class Announcement extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;              // 'ANN-2026-113'

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    /** CF-M10-7: mã chuẩn hoá để LỌC (PARENT_ALL, PARENT_GRADE_3, TEACHER, MEDICAL_STAFF, CATERER, SUPPLIER). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audience_codes", columnDefinition = "jsonb", nullable = false)
    private List<String> audienceCodes = new ArrayList<>();

    /** Chuỗi HIỂN THỊ, giữ đúng hợp đồng đọc của FE ("Phụ huynh khối 3, Giáo viên"). */
    @Column(name = "audience_label", nullable = false)
    private String audienceLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Column(nullable = false)
    private boolean urgent = false;   // BR-138

    /** D9/L16 — field NGHIỆP VỤ "ai soạn", KHÁC created_by của BaseEntity. FK user_accounts (M10-D04). */
    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    /** M10-D10 (D4/L14): NULL khi DRAFT — KHÔNG phải chuỗi rỗng "". */
    @Column(name = "published_at")
    private Instant publishedAt;

    // M10-D02: KHÔNG khai createdAt/updatedAt — BaseEntity giữ (D9).

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

    public List<String> getAudienceCodes() {
        return audienceCodes;
    }

    public void setAudienceCodes(List<String> audienceCodes) {
        this.audienceCodes = audienceCodes == null ? new ArrayList<>() : audienceCodes;
    }

    public String getAudienceLabel() {
        return audienceLabel;
    }

    public void setAudienceLabel(String audienceLabel) {
        this.audienceLabel = audienceLabel;
    }

    public AnnouncementStatus getStatus() {
        return status;
    }

    public void setStatus(AnnouncementStatus status) {
        this.status = status;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(UUID authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
