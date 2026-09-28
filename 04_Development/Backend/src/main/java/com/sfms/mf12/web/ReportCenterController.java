package com.sfms.mf12.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf12.dto.SentReportDtos.SentReportResponse;
import com.sfms.mf12.service.SentReportQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MF12 — {@code report-center} (UC-187). {@code COND-49}: hub chỉ gom UC-185 (COMPLIANCE) +
 * UC-189 (VIOLATION_SUMMARY) — KHÔNG gồm UC-188 (INCIDENT_SUMMARY, Deprecated 26/07/2026).
 */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/report-center")
public class ReportCenterController {

    private static final int HUB_PAGE_SIZE = 100;

    private final SentReportQueryService sentReportQueryService;

    public ReportCenterController(SentReportQueryService sentReportQueryService) {
        this.sentReportQueryService = sentReportQueryService;
    }

    @Operation(summary = "Trung tâm báo cáo", description = "UC-187 — gom UC-185 + UC-189")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<SentReportResponse> hub(@CurrentUser AuthUser actor) {
        ListQuery query = ListQuery.from(Map.of("pageSize", String.valueOf(HUB_PAGE_SIZE)));
        PagedResponse<SentReportResponse> all = sentReportQueryService.list(query, actor.schoolId());
        List<SentReportResponse> filtered = all.items().stream()
                .filter(r -> "COMPLIANCE".equals(r.reportType()) || "VIOLATION_SUMMARY".equals(r.reportType()))
                .toList();
        return new PagedResponse<>(filtered, filtered.size(), 1, HUB_PAGE_SIZE, 1);
    }
}
