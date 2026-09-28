package com.sfms.mf03.dto;

import com.sfms.common.enums.AllergySeverity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/** DTO của {@code /students/{id}/allergy-alerts} — BR-123, MỚI (phục vụ MF09). */
public final class AllergyAlertDtos {

    private AllergyAlertDtos() {
    }

    @Schema(name = "AllergyAlert")
    public record AllergyAlert(String allergen, AllergySeverity severity, Instant verifiedAt) {}

    @Schema(name = "AllergyAlertsResponse")
    public record AllergyAlertsResponse(
            String studentId,
            String studentName,
            String clazz,
            List<AllergyAlert> alerts,
            String approvedDiet,
            boolean hasSevereAllergy
    ) {}
}
