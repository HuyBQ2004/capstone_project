package com.sfms.mf11.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

/** DTO của {@code incident-records} (REC). TDS-MF11_Backend.md §9.2, BR-154. */
public final class IncidentRecordDtos {

    private IncidentRecordDtos() {
    }

    @Schema(description = "UC-114 — gộp N báo cáo thô thành 1 biên bản sự cố")
    public record CreateRecordRequest(
            @NotEmpty List<String> sourceReportCodes,
            @NotBlank String content,
            String location,
            Instant occurredAt) {}

    @Schema(description = "PATCH /incident-records/{code} — soạn thảo trước finalize")
    public record UpdateRecordRequest(String content, String location, Instant occurredAt) {}

    @Schema(description = "POST /incident-records/{code}/open-investigation — mở phiên điều tra (UC-114)")
    public record OpenInvestigationRequest(
            @NotBlank String title,
            List<String> symptoms,
            String suspectedMealText,
            java.time.LocalDate mealDate,
            String mealTimeLabel,
            Instant occurredAt,
            /** D1 — mã HỌC SINH bị ảnh hưởng (không phải UUID). */
            @NotEmpty List<String> affectedStudentCodes) {}

    public record CloseNotFoodSafetyRequest(@NotBlank String reason) {}

    @Schema(name = "IncidentRecordResponse")
    public record IncidentRecordResponse(
            String id, String content, String status, String createdByLabel, Instant createdAt,
            String incidentId, String location, Instant occurredAt, boolean pdfExported,
            List<String> sourceReportCodes) {}

    @Schema(name = "PagedResponseIncidentRecordResponse", description = "PagedResponse<IncidentRecordResponse>")
    public record PagedResponseIncidentRecordResponse(
            List<IncidentRecordResponse> items, long total, int page, int pageSize, int pageCount) {}
}
