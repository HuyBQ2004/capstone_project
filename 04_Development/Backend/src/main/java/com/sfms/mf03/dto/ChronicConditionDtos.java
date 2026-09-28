package com.sfms.mf03.dto;

import com.sfms.common.enums.AllergySeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * DTO của {@code /chronic-conditions} (UC-084/093/077 module bệnh lý nền) — cấu trúc đối xứng
 * {@link AllergyDeclarationDtos}, cùng gate BR-031 (Y tế xác minh trước khi có hiệu lực).
 */
public final class ChronicConditionDtos {

    private ChronicConditionDtos() {
    }

    @Schema(name = "SubmitChronicConditionRequest")
    public record SubmitChronicConditionRequest(
            @NotBlank @Schema(description = "D1 — code học sinh") String studentId,
            @NotBlank String category,
            @NotBlank String conditionName,
            @NotNull AllergySeverity severity,
            String notes,
            @Schema(description = "D1 — MÃ tài liệu (`HealthDocumentResponse.id`, VD `HDOC-2026-0001`) "
                    + "đã upload qua POST /students/{code}/health-documents; phải thuộc chính học sinh này")
            String proofFileId
    ) {}

    @Schema(name = "ChronicConditionResponse")
    public record ChronicConditionResponse(
            String id,
            @Schema(description = "D1 — code học sinh") String studentId,
            String category,
            String conditionName,
            AllergySeverity severity,
            String notes,
            String status,
            Instant declaredAt,
            @Schema(description = "D1 — MÃ tài liệu minh chứng; ghép vào "
                    + "`GET /students/{studentId}/health-documents/{proofFileId}/download` để xem file")
            String proofFileId,
            @Schema(description = "Field dẫn xuất — tên file minh chứng, null nếu không đính kèm")
            String proofFileName,
            String reviewNote,
            String verifiedBy,
            Instant verifiedAt
    ) {}
}
