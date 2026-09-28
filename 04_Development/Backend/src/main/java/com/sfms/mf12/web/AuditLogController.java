package com.sfms.mf12.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf12.dto.AuditLogDtos.AuditLogResponse;
import com.sfms.mf12.dto.AuditLogDtos.IntegrityCheckResponse;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.service.AuditLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MF12 — {@code audit-logs} (UC-106). Bảng thuộc MF01 (`M12-D01`), MF12 sở hữu nghiệp vụ.
 * BR-162: append-only — {@code PUT}/{@code PATCH}/{@code DELETE}/{@code POST} khai TƯỜNG MINH
 * để trả 405 {@code IAM-162} thay vì để Spring MVC trả 405 mặc định không có body (C5).
 */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    public AuditLogController(AuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    @Operation(summary = "Danh sách nhật ký kiểm toán", description = "UC-106 · BR-176: School Admin chỉ "
            + "thấy scope trường mình (CF-25)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @GetMapping
    public PagedResponse<AuditLogResponse> list(@RequestParam Map<String, String> allParams,
                                                 @CurrentUser AuthUser actor) {
        return auditLogQueryService.list(ListQuery.from(allParams), actor.isSystemAdmin() ? null : actor.schoolId());
    }

    @Operation(summary = "Chi tiết một dòng audit", description = "UC-106")
    @ApiResponse(responseCode = "404", description = "IAM-404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @GetMapping("/{code}")
    public AuditLogResponse detail(@PathVariable String code) {
        return auditLogQueryService.detail(code).orElseThrow(AisExceptions::notFound);
    }

    @Operation(summary = "Toàn vẹn chuỗi hash", description = "BR-172 — ADR-M12-02 (M12-D13)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/integrity")
    public IntegrityCheckResponse integrity() {
        return auditLogQueryService.verifyIntegrity();
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "BR-162 · luôn 405 IAM-162")
    @ApiResponse(responseCode = "405", description = "IAM-162",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public AuditLogResponse create(@RequestBody(required = false) Map<String, Object> body) {
        throw AisExceptions.auditAppendOnly();
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "BR-162 · luôn 405 IAM-162")
    @ApiResponse(responseCode = "405", description = "IAM-162",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{code}")
    public AuditLogResponse update(@PathVariable String code, @RequestBody(required = false) Map<String, Object> body) {
        throw AisExceptions.auditAppendOnly();
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "BR-162 · luôn 405 IAM-162")
    @ApiResponse(responseCode = "405", description = "IAM-162",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{code}")
    public AuditLogResponse patch(@PathVariable String code, @RequestBody(required = false) Map<String, Object> body) {
        throw AisExceptions.auditAppendOnly();
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "BR-162 · luôn 405 IAM-162")
    @ApiResponse(responseCode = "405", description = "IAM-162",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{code}")
    public void delete(@PathVariable String code) {
        throw AisExceptions.auditAppendOnly();
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "BR-162 · luôn 405 IAM-162 (xoá hàng loạt)")
    @ApiResponse(responseCode = "405", description = "IAM-162",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping
    public void deleteAll() {
        throw AisExceptions.auditAppendOnly();
    }
}
