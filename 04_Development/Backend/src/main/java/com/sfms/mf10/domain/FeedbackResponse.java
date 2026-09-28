package com.sfms.mf10.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/** {@code feedback_responses} — BR-143/UC-163 (MỚI, CF-M10-4). TDS-MF10_Backend.md §5.2. */
@Entity
@Table(name = "feedback_responses",
        uniqueConstraints = @UniqueConstraint(name = "uq_fbr_school_code", columnNames = {"school_id", "code"}),
        indexes = @Index(name = "ix_fbr_feedback", columnList = "feedback_id"))
public class FeedbackResponse extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;              // 'FBR-2026-001'

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "feedback_id", nullable = false)
    private UUID feedbackId;

    @Column(name = "responder_user_id", nullable = false)
    private UUID responderUserId;     // Service Caterers — FK user_accounts (M10-D04)

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "responded_at", nullable = false)
    private Instant respondedAt = Instant.now();

    /** UC-163 EX2: trả lời đánh giá tiêu cực sau 48h → ghi nhận trễ (không chặn). DẪN XUẤT (M10-D14). */
    @Column(name = "late_response", nullable = false)
    private boolean lateResponse = false;

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

    public UUID getResponderUserId() {
        return responderUserId;
    }

    public void setResponderUserId(UUID responderUserId) {
        this.responderUserId = responderUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Instant respondedAt) {
        this.respondedAt = respondedAt;
    }

    public boolean isLateResponse() {
        return lateResponse;
    }

    public void setLateResponse(boolean lateResponse) {
        this.lateResponse = lateResponse;
    }
}
