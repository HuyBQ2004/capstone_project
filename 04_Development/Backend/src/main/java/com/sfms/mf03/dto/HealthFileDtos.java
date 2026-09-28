package com.sfms.mf03.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO của {@code /students/{id}/health-file} — UC-051/094. Chỉ 4 field lâm sàng (C8). */
public final class HealthFileDtos {

    private HealthFileDtos() {
    }

    /**
     * ⚠️ C8/COND-009 — CHỈ 4 field này. Field khai báo (allergies/specialDiet/healthStatus/
     * allergySeverity) gửi kèm → 400 {@code STU-094} (kiểm ở service qua raw JSON map, xem
     * {@code StudentHealthFileService.appendClinical}).
     */
    @Schema(name = "UpdateHealthFileRequest")
    public record UpdateHealthFileRequest(
            String medicalConditions,
            String medications,
            String emergencyContact,
            String emergencyInstructions
    ) {}

    /**
     * D1 — tham chiếu tài liệu y tế đủ để UI vừa hiện tên vừa mở được file.
     *
     * <p>Trước 12/08/2026 {@code medicalDocuments} là {@code List<String>} chứa UUID thô, nên màn
     * hồ sơ chi tiết chỉ in được "Đã đính kèm (mã 3f2a1b0c…)": không tên, không mở được.
     */
    @Schema(name = "HealthDocumentRef")
    public record HealthDocumentRef(
            @Schema(description = "Mã tài liệu (`HDOC-2026-0001`) — ghép vào "
                    + "`GET /students/{studentId}/health-documents/{id}/download`")
            String id,
            String fileName
    ) {}

    @Schema(name = "HealthFileResponse")
    public record HealthFileResponse(
            String studentId,
            String medicalConditions,
            String medications,
            String emergencyContact,
            String emergencyInstructions,
            java.util.List<HealthDocumentRef> medicalDocuments
    ) {}
}
