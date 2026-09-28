package com.sfms.mf11.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf11.dto.IncidentDtos;
import com.sfms.mf11.dto.IncidentDtos.AssessRequest;
import com.sfms.mf11.dto.IncidentDtos.AssessmentDraftRequest;
import com.sfms.mf11.dto.IncidentDtos.BatchDispositionRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleHoldRequest;
import com.sfms.mf11.dto.IncidentDtos.ClosureReadinessResponse;
import com.sfms.mf11.dto.IncidentDtos.CloseRequest;
import com.sfms.mf11.dto.IncidentDtos.EvidenceRequest;
import com.sfms.mf11.dto.IncidentDtos.IncidentEventResponse;
import com.sfms.mf11.dto.IncidentDtos.LabResultRequest;
import com.sfms.mf11.dto.IncidentDtos.QuarantineRequest;
import com.sfms.mf11.dto.IncidentDtos.RootCauseRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleRetrievalRequest;
import com.sfms.mf11.dto.IncidentDtos.SuspectedDishCandidateResponse;
import com.sfms.mf11.dto.IncidentDtos.TraceabilityMapResponse;
import com.sfms.mf11.port.FoodSamplePort;
import com.sfms.mf11.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** MF11 — {@code incidents} (UC-009/013/031/033/041/116/120/121/168). TDS-MF11 §9.2/§16. */
@Tag(name = "MF11 — Incident & Emergency Management")
@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách sự cố",
            description = "D11 — Service Caterers/Teacher nhận shape RÚT GỌN (PDPD, C14)")
    @ApiResponse(responseCode = "200", description = "PagedResponse theo §2b-1 — D11: items oneOf",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(oneOf = {
                            IncidentDtos.PagedResponseIncidentResponse.class,
                            IncidentDtos.PagedResponseIncidentCaterersResponse.class,
                            IncidentDtos.PagedResponseIncidentTeacherResponse.class})))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<?> list(@RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết sự cố", description = "D11 — projection cắt theo vai")
    @ApiResponse(responseCode = "200", description = "D11 — oneOf theo vai",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(oneOf = {
                            IncidentDtos.IncidentResponse.class,
                            IncidentDtos.IncidentCaterersResponse.class,
                            IncidentDtos.IncidentTeacherResponse.class})))
    @ApiResponse(responseCode = "404", description = "IEM-114 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}")
    public Object get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.detail(code, actor);
    }

    @Operation(summary = "Dòng thời gian sự cố", description = "ADR-M11-2 — sổ append-only")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}/events")
    public List<IncidentEventResponse> events(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.events(code, actor);
    }

    @Operation(summary = "Mẫu lưu liên quan sự cố", description = "BR-113, qua FoodSamplePort (MF08)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/samples")
    public List<FoodSamplePort.SampleBrief> samples(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.samples(code, actor);
    }

    @Operation(summary = "Phiếu gửi lab liên quan sự cố", description = "BR-113")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/lab-requests")
    public List<FoodSamplePort.LabRequestBrief> labRequests(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.labRequests(code, actor);
    }

    @Operation(summary = "Món ăn có thể nghi vấn", description = "BR-149 — cửa sổ 24h, qua ServingDishPort (MF09)")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @GetMapping("/{code}/suspected-dish-candidates")
    public List<SuspectedDishCandidateResponse> suspectedDishCandidates(@PathVariable String code,
                                                                        @CurrentUser AuthUser actor) {
        return service.suspectedDishCandidates(code, actor);
    }

    @Operation(summary = "Những em đã ăn một món",
            description = "BR-149 — chiều ngược của khoanh vùng. `date` bỏ trống ⇒ ngày bữa ăn của sự cố. "
                    + "`affectedByThisIncident` đánh dấu em đã khai trong hồ sơ sự cố.")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','SCHOOL_ADMIN')")
    @GetMapping("/{code}/dishes/{dishCode}/students")
    public List<IncidentDtos.ServedStudentResponse> studentsWhoAte(
            @PathVariable String code, @PathVariable String dishCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @CurrentUser AuthUser actor) {
        return service.studentsWhoAte(code, dishCode, date, actor);
    }

    @Operation(summary = "Phân loại mức độ", description = "UC-009, BR-147")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/assess")
    public Object assess(@PathVariable String code, @Valid @RequestBody AssessRequest request,
                         @CurrentUser AuthUser actor) {
        return service.assess(code, request, actor);
    }

    @Operation(summary = "Soạn thảo đánh giá")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PatchMapping("/{code}/assessment-draft")
    public Object assessmentDraft(@PathVariable String code, @RequestBody AssessmentDraftRequest request,
                                  @CurrentUser AuthUser actor) {
        return service.assessmentDraft(code, request, actor);
    }

    @Operation(summary = "Cách ly lô nghi vấn", description = "BR-149/BR-217 — MỘT CHIỀU")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/quarantine-batches")
    public Object quarantineBatches(@PathVariable String code, @Valid @RequestBody QuarantineRequest request,
                                    @CurrentUser AuthUser actor) {
        return service.quarantineBatches(code, request, actor);
    }

    @Operation(summary = "Bản đồ truy xuất", description = "BR-151")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/traceability-map")
    public TraceabilityMapResponse traceabilityMap(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.traceabilityMap(code, actor);
    }

    @Operation(summary = "Bổ sung bằng chứng", description = "BR-150")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/evidence")
    public Object addEvidence(@PathVariable String code, @Valid @RequestBody EvidenceRequest request,
                              @CurrentUser AuthUser actor) {
        return service.addEvidence(code, request, actor);
    }

    @Operation(summary = "Tải lên ảnh bằng chứng sự cố",
            description = "V58 — gọi TRƯỚC POST /{code}/evidence; `fileCount` của bản ghi bằng chứng "
                    + "được chốt theo số ảnh THẬT đã lưu, không theo số client khai.")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping(value = "/{code}/evidence-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadEvidencePhotos(@PathVariable String code,
                                       @RequestPart("files") List<MultipartFile> files,
                                       @CurrentUser AuthUser actor) {
        return service.uploadEvidencePhotos(code, files, actor);
    }

    @Operation(summary = "Tải ảnh bằng chứng sự cố",
            description = "D10-BIS — tài liệu riêng tư, tải qua endpoint có @PreAuthorize")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/evidence-photos/{fileId}")
    public ResponseEntity<Resource> downloadEvidencePhoto(@PathVariable String code, @PathVariable String fileId,
                                                          @CurrentUser AuthUser actor) {
        // 11/08/2026 — trước đây không set Content-Type + ép attachment, trình duyệt mở tab
        // mới ra chuỗi byte nhị phân thay vì hiển thị ảnh (cùng bug đã vá ở mf07/mf08).
        var file = service.downloadEvidencePhoto(code, fileId, actor);
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Liệt kê ảnh bằng chứng sự cố đã lưu",
            description = "10/08/2026 — trước đó chỉ có endpoint tải-theo-id, không có endpoint liệt "
                    + "kê nên màn chi tiết không có fileId nào để hiện lại ảnh")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/evidence-photos")
    public List<IncidentDtos.AttachmentResponse> listEvidencePhotos(@PathVariable String code,
                                                                     @CurrentUser AuthUser actor) {
        return service.listEvidencePhotos(code, actor);
    }

    @Operation(summary = "Chốt bằng chứng")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/evidence/finalize")
    public Object finalizeEvidence(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.finalizeEvidence(code, actor);
    }

    @Operation(summary = "Ghi nhận nguyên nhân gốc", description = "BR-150")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/root-cause")
    public Object rootCause(@PathVariable String code, @Valid @RequestBody RootCauseRequest request,
                            @CurrentUser AuthUser actor) {
        return service.rootCause(code, request, actor);
    }

    @Operation(summary = "Ghi nhận kết quả xét nghiệm", description = "10/08/2026 — nội dung kết quả; "
            + "gọi kèm/sau POST /{code}/lab-result-photos để đính kèm tệp thật")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/lab-result")
    public Object labResult(@PathVariable String code, @Valid @RequestBody LabResultRequest request,
                            @CurrentUser AuthUser actor) {
        return service.labResult(code, request, actor);
    }

    @Operation(summary = "Tải lên tệp kết quả xét nghiệm",
            description = "10/08/2026 — `investigationLabResultFiles` chốt theo số tệp THẬT đã lưu, "
                    + "cùng cách `uploadEvidencePhotos` (V58)")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping(value = "/{code}/lab-result-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadLabResultFiles(@PathVariable String code,
                                       @RequestPart("files") List<MultipartFile> files,
                                       @CurrentUser AuthUser actor) {
        return service.uploadLabResultFiles(code, files, actor);
    }

    @Operation(summary = "Tải tệp kết quả xét nghiệm",
            description = "D10-BIS — tài liệu riêng tư, tải qua endpoint có @PreAuthorize")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/lab-result-photos/{fileId}")
    public ResponseEntity<Resource> downloadLabResultFile(@PathVariable String code, @PathVariable String fileId,
                                                           @CurrentUser AuthUser actor) {
        var file = service.downloadLabResultFile(code, fileId, actor);
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Liệt kê tệp kết quả xét nghiệm đã lưu", description = "10/08/2026")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/lab-result-photos")
    public List<IncidentDtos.AttachmentResponse> listLabResultFiles(@PathVariable String code,
                                                                     @CurrentUser AuthUser actor) {
        return service.listLabResultFiles(code, actor);
    }

    @Operation(summary = "Yêu cầu thu hồi mẫu", description = "BR-113, qua FoodSamplePort (MF08)")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/sample-retrieval")
    public Object sampleRetrieval(@PathVariable String code, @Valid @RequestBody SampleRetrievalRequest request,
                                  @CurrentUser AuthUser actor) {
        return service.sampleRetrieval(code, request, actor);
    }

    @Operation(summary = "Giữ mẫu lưu tại trường để điều tra",
            description = "BR-112/BR-149 — mẫu sang trạng thái HOLD, gắn sự cố và GIA HẠN hạn lưu; "
                    + "bếp hết hủy được tới khi sự cố đóng. Khác /sample-retrieval: KHÔNG mở phiếu gửi lab.")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/sample-hold")
    public Object holdSamples(@PathVariable String code, @Valid @RequestBody SampleHoldRequest request,
                              @CurrentUser AuthUser actor) {
        return service.holdSamples(code, request, actor);
    }

    @Operation(summary = "Xử lý lô cách ly (trả NCC / huỷ bỏ)",
            description = "C7/BR-217 — ghi trạng thái CUỐI cho lô đã cách ly. Đây là điều kiện còn "
                    + "thiếu để ĐÓNG được sự cố: gate `quarantineBatchesUnresolved` đọc cột này. MỘT CHIỀU.")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/quarantine-batches/{batchCode}/disposition")
    public Object disposeQuarantineBatch(@PathVariable String code, @PathVariable String batchCode,
                                          @Valid @RequestBody BatchDispositionRequest request,
                                          @CurrentUser AuthUser actor) {
        return service.disposeQuarantineBatch(code, batchCode, request, actor);
    }

    @Operation(summary = "Xác nhận học sinh đã hồi phục", description = "BR-155 (C7) — 1 trong 3 điều kiện gate đóng sự cố. "
            + "Cả Y tế (trong lúc điều tra) và Ban giám hiệu (rà soát trước khi đóng) đều xác nhận được.")
    @ApiResponse(responseCode = "200", description = "2 vai đều rơi vào default ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','SCHOOL_ADMIN')")
    @PostMapping("/{code}/recovered-students")
    public Object markStudentsRecovered(@PathVariable String code,
                                        @RequestBody java.util.List<String> studentCodes,
                                        @CurrentUser AuthUser actor) {
        return service.markStudentsRecovered(code, studentCodes, actor);
    }

    @Operation(summary = "Xuất báo cáo chính thức", description = "UC-033, BR-154, ADR-M11-8 (khoá 1 chiều)")
    @ApiResponse(responseCode = "200", description = "MEDICAL_STAFF-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/report")
    public Object report(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.report(code, actor);
    }

    @Operation(summary = "Xem báo cáo", description = "UC-121")
    @ApiResponse(responseCode = "200", description = "3 vai đọc đều rơi vào default ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/report")
    public Object viewReport(@PathVariable String code,
                             @RequestParam(defaultValue = "json") String format,
                             @CurrentUser AuthUser actor) {
        return service.detail(code, actor);
    }

    @Operation(summary = "Checklist sẵn sàng đóng", description = "BR-155/C1 — gate đóng sự cố")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @GetMapping("/{code}/closure-readiness")
    public ClosureReadinessResponse closureReadiness(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.closureReadiness(code, actor);
    }

    @Operation(summary = "Đóng sự cố", description = "UC-013, BR-155 — chỉ School Administration")
    @ApiResponse(responseCode = "200", description = "SCHOOL_ADMIN-only ⇒ luôn IncidentResponse",
            content = @Content(schema = @Schema(implementation = IncidentDtos.IncidentResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/close")
    public Object close(@PathVariable String code, @Valid @RequestBody CloseRequest request,
                        @CurrentUser AuthUser actor) {
        return service.close(code, request, actor);
    }
}
