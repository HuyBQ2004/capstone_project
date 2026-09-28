package com.sfms.mf12.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** UC-185/188/189/190 — báo cáo đã gửi (BR-177/178/181, ADR-M12-05). TDS-MF12 §9.3 nhóm (D). */
public final class SentReportDtos {

    private SentReportDtos() {
    }

    /** ADR-M12-05 — {@code payload} là snapshot NGUYÊN VĂN lúc gửi, không tính lại. */
    public record SentReportResponse(
            String id,
            String reportType,
            String period,
            String rangeLabel,
            LocalDate rangeFrom,
            LocalDate rangeTo,
            Instant sentAt,
            String sentBy,
            String recipient,
            String notes,
            boolean confidential,
            boolean hasPdf,
            boolean hasXlsx,
            Map<String, Object> payload
    ) {}

    public record SendComplianceReportRequest(
            @NotNull LocalDate rangeFrom,
            @NotNull LocalDate rangeTo,
            String period,
            String notes,
            @Schema(description = "Nhận định chuyên môn của Y tế — lưu NGUYÊN VĂN vào payload "
                    + "(giống notes), KHÔNG phải field server tính (05_.../06_Sent_Reports_Redesign.md)")
            String assessment
    ) {}

    /**
     * KHÔNG có {@code staffInvolved}/tên nhân viên (QĐ 7, `violations/reports/page.tsx` Mục
     * 10.3 — "báo cáo không nêu tên cá nhân dưới bất kỳ hình thức nào"). Chỉ {@code notes} tự do.
     */
    public record SendViolationReportRequest(
            @NotNull LocalDate rangeFrom,
            @NotNull LocalDate rangeTo,
            String period,
            String notes
    ) {}

    @Schema(name = "PagedResponseSentReportResponse", description = "PagedResponse<SentReportResponse>")
    public record PagedResponseSentReportResponse(
            List<SentReportResponse> items, long total, int page, int pageSize, int pageCount) {}
}
