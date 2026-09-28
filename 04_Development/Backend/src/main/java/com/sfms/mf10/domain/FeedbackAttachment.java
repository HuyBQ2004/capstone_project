package com.sfms.mf10.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * {@code feedback_attachments} — M10-D11 (D10/D10-BIS, L8/F2). Ảnh phản hồi là dữ liệu
 * RIÊNG TƯ (§4.3): lưu {@code storageKey} nội bộ, JSON KHÔNG bao giờ trả URL công khai.
 */
@Entity
@Table(name = "feedback_attachments",
        uniqueConstraints = @UniqueConstraint(name = "uq_fbatt_school_code", columnNames = {"school_id", "code"}),
        indexes = @Index(name = "ix_fbatt_feedback", columnList = "feedback_id"))
public class FeedbackAttachment extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;              // 'FBATT-2026-001'

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "feedback_id", nullable = false)
    private UUID feedbackId;

    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;       // đã kiểm magic bytes

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;           // ≤ 5 MB

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;

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

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(UUID feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public UUID getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(UUID uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }
}
