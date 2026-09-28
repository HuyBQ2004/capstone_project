package com.sfms.mf12.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/** UC-106 — nhật ký kiểm toán (BR-162/170/171/172/174). Bảng thuộc MF01, MF12 sở hữu endpoint. */
public final class AuditLogDtos {

    private AuditLogDtos() {
    }

    public record AuditLogResponse(
            String id,
            Instant timestamp,
            String actor,
            String ip,
            String action,
            String resource,
            String result
    ) {}

    /** BR-172 — {@code brokenAt} là {@code code} dòng đầu tiên gãy chuỗi, {@code null} nếu lành. */
    public record IntegrityCheckResponse(
            boolean valid,
            String brokenAt
    ) {}

    @Schema(name = "PagedResponseAuditLogResponse", description = "PagedResponse<AuditLogResponse>")
    public record PagedResponseAuditLogResponse(
            List<AuditLogResponse> items, long total, int page, int pageSize, int pageCount) {}
}
