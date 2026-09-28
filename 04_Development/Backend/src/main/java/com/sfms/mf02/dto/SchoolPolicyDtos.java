package com.sfms.mf02.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO của {@code GET /schools/me/policy} (UC-137, BR-022). */
public final class SchoolPolicyDtos {

    private SchoolPolicyDtos() {
    }

    @Schema(name = "SchoolPolicyResponse")
    public record SchoolPolicyResponse(
            @Schema(description = "D1 — mã nghiệp vụ của trường của actor") String schoolId,
            String schoolName,
            @Schema(description = "BR-028 — bán kính geofence hiện hành (m)") int geofenceRadius,
            java.math.BigDecimal lat,
            java.math.BigDecimal lng
    ) {}
}
