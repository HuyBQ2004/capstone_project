package com.sfms.mf13.dto;

import com.sfms.mf13.domain.ReportFormat;
import com.sfms.mf13.domain.ReportFrequency;
import com.sfms.mf13.domain.ReportSlug;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** DTO của MF13 — nguyên văn shape TDS-MF13 §9.2. */
public final class ReportDtos {

    private ReportDtos() {
    }

    /** UC-171 — một nhóm chỉ số của {@code GET /compliance-report}. BR-182: mọi field tính từ view. */
    public record ModuleMetric(
            @Schema(example = "sample-retention") ReportSlug slug,
            @Schema(example = "Lưu mẫu thực phẩm") String title,
            @Schema(description = "0..100, tính từ dữ liệu — BR-182", example = "96") int rate,
            @Schema(example = "48") int total,
            @Schema(example = "2") int alerts) {
    }

    /** UC-171 — {@code GET /compliance-report}. CF-M13-7: generatedAt ISO, periodFrom/To máy đọc được. */
    public record ComplianceSummaryResponse(
            Instant generatedAt,
            LocalDate periodFrom,
            LocalDate periodTo,
            String periodLabel,
            @Schema(example = "SCH-HN-0042") String schoolId,
            List<ModuleMetric> modules) {
    }

    /** UC-171 — {@code GET /reports} (danh mục 7 báo cáo, CF-M13-7d). */
    public record ReportDefinitionResponse(
            ReportSlug slug, String title, String group, boolean exportable) {
    }

    /** UC-172 — body {@code POST /reports/{slug}/export}. */
    public record CreateReportExportRequest(
            ReportFormat format,
            LocalDate dateFrom,
            LocalDate dateTo,
            String periodLabel,
            Map<String, Object> filters) {
    }

    /** UC-172 — response của export/history/findByCode. */
    public record ReportExportResponse(
            @Schema(example = "RPX-2026-001") String id,
            ReportSlug reportSlug,
            String reportTitle,
            ReportFormat format,
            LocalDate periodFrom,
            LocalDate periodTo,
            String periodLabel,
            int rowCount,
            boolean confidential,
            long fileSizeBytes,
            String checksumSha256,
            Instant generatedAt,
            String generatedBy,
            Instant expiresAt,
            String downloadUrl) {
    }

    /** Nội dung file đã render — tải qua {@code GET /report-exports/{code}/download}. */
    public record DownloadedFile(String filename, String contentType, byte[] bytes) {
    }

    /** UC-173 — body {@code POST /report-schedules}. D5: {@code id} tùy chọn. */
    public record CreateReportScheduleRequest(
            String id,
            ReportSlug reportSlug,
            ReportFrequency frequency,
            ReportFormat format,
            LocalTime timeOfDay,
            Short dayOfWeek,
            Short dayOfMonth,
            String zoneId) {
    }

    /** UC-173 — body {@code PATCH /report-schedules/{code}}. */
    public record UpdateReportScheduleRequest(
            ReportFrequency frequency,
            ReportFormat format,
            LocalTime timeOfDay,
            Short dayOfWeek,
            Short dayOfMonth,
            String zoneId,
            Boolean active) {
    }

    /** UC-173 — response CRUD lịch. {@code recipients} là field DẪN XUẤT (D12) — đọc từ
     *  {@code school_config.report_emails}, KHÔNG lưu ở entity. */
    public record ReportScheduleResponse(
            @Schema(example = "RSC-2026-001") String id,
            ReportSlug reportSlug,
            ReportFrequency frequency,
            ReportFormat format,
            LocalTime timeOfDay,
            Short dayOfWeek,
            Short dayOfMonth,
            String zoneId,
            boolean active,
            Instant nextRunAt,
            List<String> recipients,
            Instant createdAt) {
    }

    /** UC-173 — kết quả một lượt chạy (BR-179). */
    public record ReportScheduleRunResponse(
            String scheduleCode,
            Instant scheduledFor,
            short attempt,
            String status,
            String reportExportCode,
            int recipientCount,
            String errorMessage,
            Instant startedAt,
            Instant finishedAt) {
    }
}
