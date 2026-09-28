package com.sfms.mf12.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * {@code ai_knowledge_documents} — kho tri thức cho AI (RAG), System Admin quản trị.
 * Nguồn bền vững (Postgres) thay cho {@code KNOWLEDGE_BASE} in-memory của rag_service
 * (mất khi container restart) — nội dung {@code active} được gửi kèm mỗi lượt gọi RAG
 * qua {@code context_docs} (xem {@link GeminiClient}). Không có {@code school_id} — dữ
 * liệu cấp nền tảng, dùng chung toàn hệ thống.
 */
@Entity
@Table(name = "ai_knowledge_documents")
public class KnowledgeDocument extends BaseEntity {

    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KnowledgeCategory category;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private boolean active = true;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public KnowledgeCategory getCategory() {
        return category;
    }

    public void setCategory(KnowledgeCategory category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
