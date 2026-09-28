package com.sfms.mf09.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** DTO của {@code GET /allergy-alerts} (UC-103) — dẫn xuất tại chỗ, KHÔNG có bảng riêng. */
public final class AllergyAlertDtos {

    private AllergyAlertDtos() {
    }

    @Schema(name = "AllergyAlertResponse")
    public record AllergyAlertResponse(
            @Schema(description = "D1 — code học sinh") String studentId,
            String name,
            @JsonProperty("class") String clazz,
            @Schema(description = "Giao giữa allergen VERIFIED và allergen của thực đơn hôm nay")
            List<String> allergens,
            @Schema(description = "L20 — CHỈ cờ boolean, KHÔNG mang mã MSG-051 (BR-100 ngoài phạm vi MF09)")
            boolean conflictWithTodayMenu) {}

    @Schema(name = "PagedResponseAllergyAlertResponse", description = "PagedResponse<AllergyAlertResponse>")
    public record PagedResponseAllergyAlertResponse(
            List<AllergyAlertResponse> items, long total, int page, int pageSize, int pageCount) {}
}
