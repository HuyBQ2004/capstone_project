package com.sfms.mf04.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/** DTO của {@code /supplier-partnerships} (UC-155/159) — TDS §9.2 (bổ sung 2026-07-29). */
public final class PartnershipDtos {

    private PartnershipDtos() {
    }

    @Schema(name = "PartnershipResponse")
    public record PartnershipResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "PTN-01") String id,
            @Schema(description = "D1 — mã đối tác") String partnerId,
            @Schema(description = "D1 — mã trường") String schoolId,
            String school,
            String deliveryAddress,
            @Schema(description = "PENDING/APPROVED/SUSPENDED") String status,
            @Schema(description = "Dẫn xuất — ADR-M04-4") String docStatus,
            @Schema(type = "string", format = "date") LocalDate startDate,
            @Schema(type = "string", format = "date", description = "null = còn hiệu lực (M04-D04)")
            LocalDate endDate
    ) {}
}
