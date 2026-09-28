package com.sfms.mf03.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** DTO của {@code /health-history} — UC-146, BR-172. Chỉ GET; KHÔNG PUT/DELETE (ADR-M03-4). */
public final class HealthHistoryDtos {

    private HealthHistoryDtos() {
    }

    @Schema(name = "HealthHistoryEventResponse")
    public record HealthHistoryEventResponse(
            String id,
            String studentId,
            Instant at,
            String type,
            String event,
            String by
    ) {}
}
