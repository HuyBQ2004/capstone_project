package com.sfms.mf12.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/** UC-111 — nhật ký lỗi hệ thống. CF-18: list trả đủ {@code stackTrace}/{@code sessionInfo}. */
public final class ErrorLogDtos {

    private ErrorLogDtos() {
    }

    public record ErrorLogSummary(
            String id,
            Instant timestamp,
            String message,
            String severity,
            String module,
            String path,
            String stackTrace,
            String sessionInfo
    ) {}

    public record ErrorLogDetail(
            String id,
            Instant timestamp,
            String message,
            String severity,
            String module,
            String path,
            String stackTrace,
            String sessionInfo
    ) {}

    @Schema(name = "PagedResponseErrorLogSummary", description = "PagedResponse<ErrorLogSummary>")
    public record PagedResponseErrorLogSummary(
            List<ErrorLogSummary> items, long total, int page, int pageSize, int pageCount) {}
}
