package com.sfms.mf03.dto;

import com.sfms.common.enums.AllergySeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** DTO của {@code /allergy-declarations} (UC-084/093/077/104/036). D6: severity = enum thật. */
public final class AllergyDeclarationDtos {

    private AllergyDeclarationDtos() {
    }

    @Schema(name = "SubmitAllergyRequest")
    public record SubmitAllergyRequest(
            @NotBlank @Schema(description = "D1 — code học sinh") String studentId,
            @NotBlank String category,
            @NotBlank String allergen,
            @NotNull AllergySeverity severity,
            @Schema(description = "D1 — MÃ tài liệu (`HealthDocumentResponse.id`, VD `HDOC-2026-0001`) "
                    + "đã upload qua POST /students/{code}/health-documents; phải thuộc chính học sinh này")
            String proofFileId
    ) {}

    @Schema(name = "RequestInfoRequest")
    public record RequestInfoRequest(
            @NotBlank String reason
    ) {}

    @Schema(name = "AllergyDeclarationResponse")
    public record AllergyDeclarationResponse(
            String id,
            @Schema(description = "D1 — code học sinh") String studentId,
            String category,
            String allergen,
            AllergySeverity severity,
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
