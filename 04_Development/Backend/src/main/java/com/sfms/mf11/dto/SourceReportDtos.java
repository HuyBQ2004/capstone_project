package com.sfms.mf11.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** DTO của {@code source-reports} (RPT). TDS-MF11_Backend.md §9.2. */
public final class SourceReportDtos {

    private SourceReportDtos() {
    }

    @Schema(description = "UC-073/074 — Teacher/Parent gửi báo cáo thô")
    public record CreateSourceReportRequest(
            @NotBlank String className,
            List<String> symptoms,
            String suspectedMealText,
            /** Bỏ trống ⇒ server lùi về ngày báo cáo làm mốc tạm (Teacher gộp "Ngày ăn" vào
             * {@code description} tự do từ 27/07/2026, không còn field cấu trúc riêng). */
            LocalDate mealDate,
            @NotBlank String description,
            int evidencePhotos,
            /** D1 — mã HỌC SINH (không phải UUID) — chỉ bắt buộc khi Parent gửi (BR-201). */
            String studentCode) {}

    @Schema(name = "SourceReportResponse")
    public record SourceReportResponse(
            String id, String reportedById, String reportedByLabel, Instant reportedAt, String className,
            List<String> symptoms, String suspectedMealText, LocalDate mealDate, String description,
            int evidencePhotos, String severityLevel, String status, String mergedIntoIncidentId,
            String draftRecordId) {}

    @Schema(name = "PagedResponseSourceReportResponse", description = "PagedResponse<SourceReportResponse>")
    public record PagedResponseSourceReportResponse(
            List<SourceReportResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(description = "Cụm gợi ý theo BR-146 — CHỈ cảnh báo, không tự hành động")
    public record ClusterSignalResponse(
            String symptomKey, List<String> reportCodes, int studentCount, Instant windowFrom, Instant windowTo) {}
}
