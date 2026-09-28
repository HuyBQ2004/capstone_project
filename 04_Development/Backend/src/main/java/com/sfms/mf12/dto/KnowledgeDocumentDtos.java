package com.sfms.mf12.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** UC-012/UC-119 mở rộng — Kho tri thức AI (System Admin quản trị, gửi kèm mỗi lượt gọi RAG). */
public final class KnowledgeDocumentDtos {

    private KnowledgeDocumentDtos() {
    }

    public record CreateKnowledgeDocumentRequest(
            @NotBlank String title,
            @NotNull String category,
            @NotBlank String content
    ) {}

    public record KnowledgeDocumentResponse(
            String id,
            String title,
            String category,
            String content,
            boolean active,
            Instant createdAt,
            String createdBy
    ) {}
}
