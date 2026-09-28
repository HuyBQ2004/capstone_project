package com.sfms.mf13.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.CreateReportExportRequest;
import com.sfms.mf13.dto.ReportDtos.DownloadedFile;
import com.sfms.mf13.dto.ReportDtos.ReportExportResponse;
import com.sfms.mf13.error.RptExceptions;
import com.sfms.mf13.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF13 — UC-172 (BR-177/178/181/182). §9.1 #4-#7. */
@Tag(name = "MF13 — Reporting & Analytics")
@RestController
@RequestMapping("/api/v1")
public class ReportExportController {

    private final ReportExportService reportExportService;

    public ReportExportController(ReportExportService reportExportService) {
        this.reportExportService = reportExportService;
    }

    @Operation(summary = "Xuất báo cáo", description = "UC-172 · BR-177, BR-178, BR-181, BR-182 — "
            + "kỳ rỗng KHÔNG chặn, trả rowCount=0 (CF-M13-4/M13-D10)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @PostMapping("/reports/{slug}/export")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportExportResponse export(@PathVariable ReportSlug slug,
                                       @Valid @RequestBody CreateReportExportRequest request,
                                       @CurrentUser AuthUser actor) {
        return reportExportService.export(actor, slug, request.format(), request.dateFrom(), request.dateTo(),
                request.periodLabel(), request.filters());
    }

    @Operation(summary = "Lịch sử xuất báo cáo", description = "UC-172 · BR-177")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @GetMapping("/report-exports")
    public PagedResponse<ReportExportResponse> history(@RequestParam Map<String, String> allParams,
                                                        @RequestParam(name = "reportSlug", required = false)
                                                        ReportSlug slug,
                                                        @RequestParam(name = "scope", required = false)
                                                        String scope,
                                                        @CurrentUser AuthUser actor) {
        ListQuery query = ListQuery.from(allParams);
        boolean allSchools = "all".equalsIgnoreCase(scope);
        Page<ReportExportResponse> page = reportExportService.history(actor, slug, allSchools,
                query.toPageable());
        return PagedResponse.of(page, query.page(), query.pageSize());
    }

    @Operation(summary = "Chi tiết một bản xuất", description = "UC-172 · BR-177")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @GetMapping("/report-exports/{code}")
    public ReportExportResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return reportExportService.findByCode(actor, code).orElseThrow(RptExceptions::notFound);
    }

    @Operation(summary = "Tải file bản xuất", description = "UC-172 · BR-177, BR-178, BR-181 — "
            + "checksum đối chiếu trước khi trả (ADR-M13-3)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @GetMapping("/report-exports/{code}/download")
    public ResponseEntity<byte[]> download(@PathVariable String code, @CurrentUser AuthUser actor) {
        DownloadedFile file = reportExportService.download(actor, code);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .body(file.bytes());
    }
}
