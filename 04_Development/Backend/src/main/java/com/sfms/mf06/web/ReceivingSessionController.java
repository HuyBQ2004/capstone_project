package com.sfms.mf06.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingSessionCreateRequest;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingSessionResponse;
import com.sfms.mf06.dto.ReceivingExportDtos.ExportFormat;
import com.sfms.mf06.dto.ReceivingExportDtos.ExportedFile;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.service.ReceivingReportExportService;
import com.sfms.mf06.service.ReceivingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** MF06 — {@code /api/v1/receiving-sessions} (UC-044/060/072/123). ADR-M06-5/M06-7. */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/receiving-sessions")
public class ReceivingSessionController {

    private final ReceivingSessionService sessionService;
    private final ReceivingReportExportService exportService;

    public ReceivingSessionController(ReceivingSessionService sessionService,
                                       ReceivingReportExportService exportService) {
        this.sessionService = sessionService;
        this.exportService = exportService;
    }

    @Operation(summary = "Danh sách phiên nhận hàng", description = "UC-044 · UC-123 · BR-076")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<ReceivingSessionResponse> list(@RequestParam Map<String, String> allParams,
                                                          @CurrentUser AuthUser actor) {
        return sessionService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chuyến giao đã tới hạn nhận", description = "UC-044")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @GetMapping("/due-deliveries")
    public java.util.List<String> dueDeliveries(@CurrentUser AuthUser actor) {
        return sessionService.dueDeliveries(actor);
    }

    @Operation(summary = "Chi tiết phiên nhận hàng", description = "UC-044 · UC-123")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/{code}")
    public ReceivingSessionResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return sessionService.detail(code, actor);
    }

    @Operation(summary = "Ghi nhận phiên nhận hàng", description = "UC-060 · UC-072 · BR-070/072/073/074/078, "
            + "ADR-M06-5 (MỘT transaction atomic)")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceivingSessionResponse create(@Valid @RequestBody ReceivingSessionCreateRequest request,
                                            @CurrentUser AuthUser actor) {
        return sessionService.create(request, actor);
    }

    @Operation(summary = "Upload ảnh bằng chứng từ chối lô", description = "BR-072/078 · D10-BIS — trả documentId")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping(value = "/reject-documents", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> uploadEvidence(@RequestPart("file") MultipartFile file, @CurrentUser AuthUser actor) {
        return Map.of("documentId", sessionService.uploadEvidence(file, actor));
    }

    @Operation(summary = "Xem ảnh bằng chứng từ chối lô",
            description = "BR-072/078 · D10-BIS mục 2 — tài liệu RIÊNG TƯ, tải qua endpoint có phân "
                    + "quyền + scope theo trường, KHÔNG qua URL công khai. Bổ sung 13/08/2026: trước "
                    + "đó chỉ có đường upload, ảnh chụp lúc từ chối hàng không màn hình nào xem lại được.")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/reject-documents/{documentId}/file")
    public ResponseEntity<org.springframework.core.io.Resource> evidenceFile(
            @PathVariable String documentId, @CurrentUser AuthUser actor) {
        var file = sessionService.downloadEvidence(documentId, actor);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.downloadName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Xuất báo cáo lịch sử nhận hàng", description = "Tab \"Lịch sử & báo cáo\" — BR-151, "
            + "cùng bộ lọc dateFrom/dateTo/search đang áp dụng ở GET /receiving-sessions")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam Map<String, String> allParams,
                                          @RequestParam(defaultValue = "xlsx") String format,
                                          @CurrentUser AuthUser actor) {
        ExportFormat exportFormat = parseFormat(format);
        ExportedFile file = exportService.export(ListQuery.from(allParams), exportFormat, actor);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }

    private static ExportFormat parseFormat(String raw) {
        try {
            return ExportFormat.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IngExceptions.validationFailed("format", "must be xlsx or pdf");
        }
    }
}
