package com.sfms.mf13.event;

import com.sfms.mf13.domain.ReportFormat;
import com.sfms.mf13.domain.ReportSlug;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** §7 Domain Event Catalog — payload record theo TDS-MF13 §7.3. */
public final class ReportEvents {

    private ReportEvents() {
    }

    /** BR-177/178 — mọi lần xuất được ghi nhận (audit trail). */
    public record ReportExported(
            String exportCode,
            UUID schoolId,
            ReportSlug reportSlug,
            ReportFormat format,
            LocalDate periodFrom,
            LocalDate periodTo,
            int rowCount,
            UUID generatedByUserId,
            Instant generatedAt) {
    }

    /** BR-177 — tải lại một bản xuất. */
    public record ReportDownloaded(String exportCode, UUID schoolId, UUID downloadedBy, Instant downloadedAt) {
    }

    /** BR-179 — tạo/sửa/xoá/bật-tắt lịch. */
    public record ReportScheduleChanged(
            String scheduleCode, UUID schoolId, ReportSlug reportSlug, ReportFormat format, boolean active,
            UUID changedBy) {
    }

    /** BR-179 — hết 3 lần retry: ghi nhận lỗi + thông báo System Admin. */
    public record ReportScheduleRunFailed(
            String scheduleCode,
            UUID schoolId,
            ReportSlug reportSlug,
            Instant scheduledFor,
            short attempts,
            String errorMessage) {
    }

    /** BR-177 — xoá sau khi vượt hạn lưu trữ (≥12 tháng). */
    public record ReportExportPurged(String exportCode, UUID schoolId, Instant generatedAt, Instant expiresAt,
                                     Instant purgedAt) {
    }
}
