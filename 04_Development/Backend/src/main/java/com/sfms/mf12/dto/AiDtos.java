package com.sfms.mf12.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

/** UC-012/UC-119 — Trợ lý AI (BR-156/BR-157). TDS-MF12 §9.3 nhóm (A). */
public final class AiDtos {

    private AiDtos() {
    }

    public record AskRequest(
            String conversationId,
            @NotBlank String message
    ) {}

    public record AiChatResponse(
            String conversationId,
            String reply,
            String model,
            boolean redacted
    ) {}

    public record AiConversationSummary(
            String id,
            String title,
            Instant startedAt,
            Instant lastMessageAt,
            long messageCount
    ) {}

    public record AiMessageView(
            String id,
            String prompt,
            String responseText,
            String status,
            Instant requestedAt
    ) {}

    public record AiConversationDetail(
            String id,
            String title,
            Instant startedAt,
            Instant lastMessageAt,
            List<AiMessageView> messages
    ) {}

    @Schema(name = "PagedResponseAiConversationSummary", description = "PagedResponse<AiConversationSummary>")
    public record PagedResponseAiConversationSummary(
            List<AiConversationSummary> items, long total, int page, int pageSize, int pageCount) {}
}
