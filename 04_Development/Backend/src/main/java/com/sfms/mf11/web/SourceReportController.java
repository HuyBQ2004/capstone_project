package com.sfms.mf11.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf11.dto.SourceReportDtos.ClusterSignalResponse;
import com.sfms.mf11.dto.SourceReportDtos.CreateSourceReportRequest;
import com.sfms.mf11.dto.SourceReportDtos.SourceReportResponse;
import com.sfms.mf11.service.SourceReportService;
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

import java.util.List;
import java.util.Map;

/** MF11 — {@code source-reports} (UC-073/074/114). TDS-MF11_Backend.md §9.2/§16. */
@Tag(name = "MF11 — Incident & Emergency Management")
@RestController
@RequestMapping("/api/v1/source-reports")
public class SourceReportController {

    private final SourceReportService service;

    public SourceReportController(SourceReportService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách báo cáo thô", description = "UC-114 — Teacher chỉ thấy lớp chủ nhiệm")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<SourceReportResponse> list(@RequestParam Map<String, String> allParams,
                                                     @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết báo cáo thô")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','MEDICAL_STAFF')")
    @GetMapping("/{code}")
    public SourceReportResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.getByCode(code, actor);
    }

    @Operation(summary = "Gửi báo cáo lo ngại ATTP", description = "UC-073 (Parent) / UC-074 (Teacher), BR-146")
    @PreAuthorize("hasAnyRole('TEACHER','PARENT')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SourceReportResponse create(@Valid @RequestBody CreateSourceReportRequest request,
                                       @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Đóng — không liên quan ATTP", description = "Medical Staff triage")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/close-not-food-safety")
    public SourceReportResponse closeNotFoodSafety(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.closeNotFoodSafety(code, actor);
    }

    @Operation(summary = "Cụm gợi ý", description = "BR-146 — CHỈ cảnh báo, không tự hành động")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @GetMapping("/cluster-signals")
    public List<ClusterSignalResponse> clusterSignals(@CurrentUser AuthUser actor) {
        return service.clusterSignals(actor);
    }

    @Operation(summary = "Bổ sung ảnh bằng chứng", description = "UC-073/074 — chỉ chính người đã gửi báo cáo")
    @PreAuthorize("hasAnyRole('TEACHER','PARENT')")
    @PostMapping(value = "/{code}/evidence-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SourceReportResponse uploadEvidence(@PathVariable String code,
                                               @RequestPart("files") List<MultipartFile> files,
                                               @CurrentUser AuthUser actor) {
        return service.uploadEvidence(code, files, actor);
    }

    @Operation(summary = "Tải ảnh bằng chứng", description = "D10-BIS — tài liệu riêng tư, tải qua endpoint có @PreAuthorize")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','MEDICAL_STAFF','PARENT')")
    @GetMapping("/{code}/evidence-photos/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadEvidence(@PathVariable String code,
                                                                                   @PathVariable String fileId,
                                                                                   @CurrentUser AuthUser actor) {
        var file = service.downloadEvidence(code, fileId, actor);
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Liệt kê ảnh bằng chứng đã lưu",
            description = "11/08/2026 — trước đó chỉ có endpoint tải-theo-id, không có endpoint liệt "
                    + "kê nên không hiện lại được ảnh đã nộp")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','MEDICAL_STAFF','PARENT')")
    @GetMapping("/{code}/evidence-photos")
    public List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidence(@PathVariable String code,
                                                                                  @CurrentUser AuthUser actor) {
        return service.listEvidence(code, actor);
    }
}
