package com.sfms.mf12.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * BR-156/BR-157 — một lượt gọi AI đã archive (ADR-M12-01: cổng AI duy nhất). {@code module}
 * phân biệt nguồn gọi: {@code MF05_MENU} (qua {@code AiAssistPort}) hay {@code MF12_CHAT}
 * (UC-012 trực tiếp).
 */
@Entity
@Table(name = "ai_requests",
       indexes = {
         @Index(name = "ix_ai_req_conv",  columnList = "conversation_id"),
         @Index(name = "ix_ai_req_user",  columnList = "requested_by,requested_at"),
         @Index(name = "ix_ai_req_module",columnList = "module")
       })
public class AiRequest extends BaseEntity {

    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    /** "MF05_MENU" | "MF12_CHAT" — SEARCH_FIELDS theo `00_` §2b-3. */
    @Column(nullable = false, length = 32)
    private String module;

    /** BR-157: lưu nguyên văn prompt gửi đi (đã redact danh tính học sinh). */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Column(nullable = false, length = 48)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AiRequestStatus status;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    /** BR-173/BR-181 — ẩn danh hoá: KHÔNG lưu danh tính học sinh trong prompt. */
    @Column(nullable = false)
    private boolean redacted = true;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public AiRequestStatus getStatus() {
        return status;
    }

    public void setStatus(AiRequestStatus status) {
        this.status = status;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public boolean isRedacted() {
        return redacted;
    }

    public void setRedacted(boolean redacted) {
        this.redacted = redacted;
    }
}
