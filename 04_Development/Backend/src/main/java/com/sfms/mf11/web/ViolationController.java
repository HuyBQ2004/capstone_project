package com.sfms.mf11.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf11.dto.ViolationDtos.CreateViolationRequest;
import com.sfms.mf11.dto.ViolationDtos.OverrideAbuseRow;
import com.sfms.mf11.dto.ViolationDtos.RejectResolutionRequest;
import com.sfms.mf11.dto.ViolationDtos.ResolutionRequest;
import com.sfms.mf11.dto.ViolationDtos.ViolationResponse;
import com.sfms.mf11.dto.ViolationDtos.ViolationSummaryResponse;
import com.sfms.mf11.service.ViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** MF11 — {@code violations} (UC-179/180/181/182/183), BR-183/185/186/187/189/190/211. TDS-MF11 §9.2/§16. */
@Tag(name = "MF11 — Incident & Emergency Management")
@RestController
@RequestMapping("/api/v1/violations")
public class ViolationController {

    private final ViolationService service;

    public ViolationController(ViolationService service) {
        this.service = service;
    }

    @Operation(summary = "Sổ vi phạm", description = "UC-181, BR-189 — Service Caterers chỉ thấy của đơn vị mình")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<ViolationResponse> list(@RequestParam Map<String, String> allParams,
                                                 @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết biên bản vi phạm")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}")
    public ViolationResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.getByCode(code, actor);
    }

    @Operation(summary = "Lập biên bản vi phạm", description = "UC-179, BR-183 — cấp đơn vị")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ViolationResponse create(@Valid @RequestBody CreateViolationRequest request, @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Danh sách đại diện Nhà thầu", description = "M11-D19 — để chọn khi lập biên bản")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @GetMapping("/caterer-representatives")
    public java.util.List<com.sfms.common.reference.ReferenceResolver.UserBrief> catererRepresentatives(
            @CurrentUser AuthUser actor) {
        return service.catererRepresentatives(actor);
    }

    @Operation(summary = "Xuất bản PDF biên bản", description = "BR-211 — chuẩn bị để ký tay")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','SERVICE_CATERERS')")
    @PostMapping("/{code}/pdf")
    public ViolationResponse exportPdf(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.exportPdf(code, actor);
    }

    @Operation(summary = "Tải ảnh biên bản đã ký", description = "BR-211 — gate MSG-095/IEM-211")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','SERVICE_CATERERS')")
    @PostMapping(value = "/{code}/signed-record", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ViolationResponse uploadSignedRecord(@PathVariable String code, @RequestPart("file") MultipartFile file,
                                                @CurrentUser AuthUser actor) {
        return service.uploadSignedRecord(code, file, actor);
    }

    @Operation(summary = "Tải ảnh bằng chứng", description = "UC-179, BR-183/184 — nhiều ảnh, sau khi đã tạo biên bản")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','SERVICE_CATERERS')")
    @PostMapping(value = "/{code}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ViolationResponse uploadEvidence(@PathVariable String code,
                                            @RequestPart("files") java.util.List<MultipartFile> files,
                                            @CurrentUser AuthUser actor) {
        return service.uploadEvidence(code, files, actor);
    }

    @Operation(summary = "Gỡ ảnh biên bản đã ký", description = "Chỉ khi chưa gửi duyệt")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @DeleteMapping("/{code}/signed-record/{fileId}")
    public ViolationResponse deleteSignedRecord(@PathVariable String code, @PathVariable String fileId,
                                                @CurrentUser AuthUser actor) {
        return service.deleteSignedRecord(code, fileId, actor);
    }

    @Operation(summary = "Nhập xử lý vi phạm", description = "UC-180, BR-185/186")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/resolution")
    public ViolationResponse submitResolution(@PathVariable String code, @Valid @RequestBody ResolutionRequest request,
                                              @CurrentUser AuthUser actor) {
        return service.submitResolution(code, request, actor);
    }

    @Operation(summary = "Duyệt xử lý vi phạm", description = "UC-182, BR-186 — School Administration")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/resolution/approve")
    public ViolationResponse approveResolution(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.approveResolution(code, actor);
    }

    @Operation(summary = "Từ chối xử lý vi phạm")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/resolution/reject")
    public ViolationResponse rejectResolution(@PathVariable String code,
                                              @Valid @RequestBody RejectResolutionRequest request,
                                              @CurrentUser AuthUser actor) {
        return service.rejectResolution(code, request, actor);
    }

    @Operation(summary = "Báo cáo lạm dụng override tuần", description = "UC-183, BR-190 — dẫn xuất runtime")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @GetMapping("/override-abuse-report")
    public List<OverrideAbuseRow> overrideAbuseReport(@RequestParam(required = false) String week,
                                                       @CurrentUser AuthUser actor) {
        return service.overrideAbuseReport(week, actor);
    }

    @Operation(summary = "Tổng hợp vi phạm theo kỳ", description = "UC-189/BR-214 — cấp số liệu cho MF13")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/summary")
    public ViolationSummaryResponse summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @CurrentUser AuthUser actor) {
        return service.summary(dateFrom, dateTo, actor);
    }
}
