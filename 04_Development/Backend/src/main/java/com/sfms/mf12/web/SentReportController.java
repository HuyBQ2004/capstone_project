package com.sfms.mf12.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf12.dto.SentReportDtos.SendComplianceReportRequest;
import com.sfms.mf12.dto.SentReportDtos.SendViolationReportRequest;
import com.sfms.mf12.dto.SentReportDtos.SentReportResponse;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.service.ComplianceReportSendService;
import com.sfms.mf12.service.ReportExporter.ExportedFile;
import com.sfms.mf12.service.SentReportQueryService;
import com.sfms.mf12.service.ViolationReportSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

/** MF12 — {@code sent-reports} (UC-185/188/189/190). {@code M12-D06/D07}. */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/sent-reports")
public class SentReportController {

    private final SentReportQueryService queryService;
    private final ComplianceReportSendService complianceReportSendService;
    private final ViolationReportSendService violationReportSendService;

    public SentReportController(SentReportQueryService queryService,
                                 ComplianceReportSendService complianceReportSendService,
                                 ViolationReportSendService violationReportSendService) {
        this.queryService = queryService;
        this.complianceReportSendService = complianceReportSendService;
        this.violationReportSendService = violationReportSendService;
    }

    @Operation(summary = "Danh sách báo cáo đã gửi", description = "UC-190 · BR-176 lọc theo trường "
            + "(row-level, D11 không áp — TC-CON-007)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<SentReportResponse> list(@RequestParam Map<String, String> allParams,
                                                   @CurrentUser AuthUser actor) {
        return queryService.list(ListQuery.from(allParams), actor.isSystemAdmin() ? null : actor.schoolId());
    }

    @Operation(summary = "Chi tiết báo cáo đã gửi", description = "UC-190 — payload đọc lại NGUYÊN VĂN (ADR-M12-05)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}")
    public SentReportResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return queryService.detail(code, actor.isSystemAdmin() ? null : actor.schoolId())
                .orElseThrow(AisExceptions::notFound);
    }

    @Operation(summary = "Tải file báo cáo", description = "BR-178 — sinh PDF/XLSX THẬT lúc tải")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/download")
    public ResponseEntity<byte[]> download(@PathVariable String code, @RequestParam String format,
                                           @CurrentUser AuthUser actor, HttpServletRequest servletRequest) {
        ExportedFile file = queryService.download(code, format, actor.isSystemAdmin() ? null : actor.schoolId(),
                actor.email(), clientIp(servletRequest));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .body(file.bytes());
    }

    @Operation(summary = "Gửi báo cáo tuân thủ ATTP", description = "UC-185 · BR-209 — M12-D06: đổi từ "
            + "`POST /reports/compliance/send`")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/compliance")
    @ResponseStatus(HttpStatus.CREATED)
    public SentReportResponse sendCompliance(@Valid @RequestBody SendComplianceReportRequest request,
                                             @CurrentUser AuthUser actor, HttpServletRequest servletRequest) {
        return complianceReportSendService.send(actor.schoolId(), request, actor.userId(), actor.email(),
                clientIp(servletRequest));
    }

    @Operation(summary = "Gửi báo cáo tổng hợp vi phạm", description = "UC-189 · BR-214 — M12-D06: đổi từ "
            + "`POST /reports/violations/send`")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/violations")
    @ResponseStatus(HttpStatus.CREATED)
    public SentReportResponse sendViolations(@Valid @RequestBody SendViolationReportRequest request,
                                             @CurrentUser AuthUser actor, HttpServletRequest servletRequest) {
        return violationReportSendService.send(actor.schoolId(), request, actor.userId(), actor.email(),
                clientIp(servletRequest));
    }

    /** BR-170 — audit ghi IP thật. Ưu tiên {@code X-Forwarded-For} khi chạy sau Cloudflare. */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "Gửi báo cáo tổng hợp sự cố (Deprecated)", description = "UC-188/BR-213 — "
            + "Deprecated 26/07/2026 (CF-4/CF-22), luôn 409 AIS-188")
    @PostMapping("/incidents")
    public SentReportResponse sendIncidents() {
        throw AisExceptions.incidentReportDeprecated();
    }
}
