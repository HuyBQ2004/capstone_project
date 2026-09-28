package com.sfms.mf11.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf11.dto.CapaDtos.CorrectiveActionResponse;
import com.sfms.mf11.dto.CapaDtos.CreateCapaRequest;
import com.sfms.mf11.dto.CapaDtos.RejectRequest;
import com.sfms.mf11.dto.CapaDtos.ReworkRequest;
import com.sfms.mf11.dto.CapaDtos.SubmitEvidenceRequest;
import com.sfms.mf11.dto.CapaDtos.SubmitForApprovalRequest;
import com.sfms.mf11.service.CapaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
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

/** MF11 — {@code corrective-actions} (CAPA), BR-152/BR-216. TDS-MF11 §9.2/§16. */
@Tag(name = "MF11 — Incident & Emergency Management")
@RestController
@RequestMapping("/api/v1/corrective-actions")
public class CapaController {

    private final CapaService service;

    public CapaController(CapaService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách CAPA", description = "Service Caterers chỉ thấy 'Được giao'")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<CorrectiveActionResponse> list(@RequestParam Map<String, String> allParams,
                                                        @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết CAPA")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}")
    public CorrectiveActionResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.getByCode(code, actor);
    }

    @Operation(summary = "Lập CAPA thủ công", description = "BR-152 — từ sự cố")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CorrectiveActionResponse create(@Valid @RequestBody CreateCapaRequest request, @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Gửi duyệt CAPA",
            description = "BR-095 — CAPA sinh tự động từ kiểm thực trượt chưa có description/đơn vị "
                    + "phụ trách; body cho phép điền/sửa nốt ngay lúc gửi")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/submit-for-approval")
    public CorrectiveActionResponse submitForApproval(@PathVariable String code,
                                                       @RequestBody(required = false) SubmitForApprovalRequest request,
                                                       @CurrentUser AuthUser actor) {
        return service.submitForApproval(code, request, actor);
    }

    @Operation(summary = "Xác nhận hoàn tất CAPA", description = "UC-168")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/validate")
    public CorrectiveActionResponse validate(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.validate(code, actor);
    }

    @Operation(summary = "Yêu cầu làm lại", description = "UC-168 AF1")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/rework")
    public CorrectiveActionResponse rework(@PathVariable String code, @Valid @RequestBody ReworkRequest request,
                                           @CurrentUser AuthUser actor) {
        return service.rework(code, request, actor);
    }

    @Operation(summary = "Duyệt CAPA", description = "BR-153 — School Administration")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/approve")
    public CorrectiveActionResponse approve(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.approve(code, actor);
    }

    @Operation(summary = "Từ chối CAPA")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/reject")
    public CorrectiveActionResponse reject(@PathVariable String code, @Valid @RequestBody RejectRequest request,
                                           @CurrentUser AuthUser actor) {
        return service.reject(code, request, actor);
    }

    @Operation(summary = "Tiếp nhận CAPA", description = "UC-038 — Service Caterers")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/accept")
    public CorrectiveActionResponse accept(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.accept(code, actor);
    }

    @Operation(summary = "Nộp bằng chứng thực hiện", description = "UC-082, MSG-049")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/submit-evidence")
    public CorrectiveActionResponse submitEvidence(@PathVariable String code,
                                                   @RequestBody SubmitEvidenceRequest request,
                                                   @CurrentUser AuthUser actor) {
        return service.submitEvidence(code, request, actor);
    }

    @Operation(summary = "Tải lên ảnh bằng chứng", description = "UC-082 — trước khi nộp bằng chứng (submit-evidence)")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping(value = "/{code}/evidence-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CorrectiveActionResponse uploadEvidence(@PathVariable String code,
                                                   @RequestPart("files") List<MultipartFile> files,
                                                   @CurrentUser AuthUser actor) {
        return service.uploadEvidence(code, files, actor);
    }

    @Operation(summary = "Tải ảnh bằng chứng", description = "D10-BIS — tài liệu riêng tư, tải qua endpoint có @PreAuthorize")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}/evidence-photos/{fileId}")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable String code, @PathVariable String fileId,
                                                       @CurrentUser AuthUser actor) {
        var file = service.downloadEvidence(code, fileId, actor);
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Liệt kê ảnh bằng chứng đã lưu",
            description = "10/08/2026 — trước đó chỉ có endpoint tải-theo-id, không có endpoint liệt "
                    + "kê nên màn chi tiết CAPA không hiện lại được ảnh đã nộp")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}/evidence-photos")
    public List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidence(@PathVariable String code,
                                                                                 @CurrentUser AuthUser actor) {
        return service.listEvidence(code, actor);
    }
}
