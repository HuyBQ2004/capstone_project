package com.sfms.mf13.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.ComplianceSummaryResponse;
import com.sfms.mf13.dto.ReportDtos.ReportDefinitionResponse;
import com.sfms.mf13.service.AnalyticsQueryService;
import com.sfms.mf13.service.ReportCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** MF13 — UC-171 (BR-175/176/180/181/182). §9.1 #1-#3. */
@Tag(name = "MF13 — Reporting & Analytics")
@RestController
@RequestMapping("/api/v1")
public class ReportController {

    private final AnalyticsQueryService analyticsQueryService;
    private final ReportCatalogService catalogService;

    public ReportController(AnalyticsQueryService analyticsQueryService, ReportCatalogService catalogService) {
        this.analyticsQueryService = analyticsQueryService;
        this.catalogService = catalogService;
    }

    @Operation(summary = "Xem dashboard tổng hợp tuân thủ",
            description = "UC-171 · BR-175, BR-176, BR-180, BR-181, BR-182")
    @ApiResponse(responseCode = "200", description = "Số liệu tổng hợp, tuổi ≤ 5 phút (BR-180)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/compliance-report")
    public ComplianceSummaryResponse complianceReport(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(name = "allSchools", defaultValue = "false") boolean allSchools,
            @CurrentUser AuthUser actor) {
        return analyticsQueryService.complianceSummary(actor, dateFrom, dateTo, allSchools);
    }

    @Operation(summary = "Danh mục báo cáo", description = "UC-171 · BR-175 — thay 2 dòng hardcode FE (CF-M13-7d)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/reports")
    public List<ReportDefinitionResponse> reports(@CurrentUser AuthUser actor) {
        return catalogService.listFor(actor);
    }

    @Operation(summary = "Bảng chi tiết một báo cáo", description = "UC-171 · BR-175, BR-176, BR-181")
    @ApiResponse(responseCode = "200", description = "PagedResponse<Map<String,Object>> — 7 shape khác nhau theo slug (D11)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/reports/{slug}/rows")
    public PagedResponse<Map<String, Object>> rows(@PathVariable ReportSlug slug,
                                                    @RequestParam Map<String, String> allParams,
                                                    @CurrentUser AuthUser actor) {
        ListQuery query = ListQuery.from(allParams);
        Page<Map<String, Object>> page = analyticsQueryService.rows(actor, slug, query.dateFrom(), query.dateTo(),
                query.toPageable());
        return PagedResponse.of(page, query.page(), query.pageSize());
    }
}
