package com.sfms.mf07.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceResponse;
import com.sfms.mf07.dto.KitchenInspectionDtos.KitchenInspectionResponse;
import com.sfms.mf07.dto.KitchenInspectionDtos.PagedResponseKitchenInspectionResponse;
import com.sfms.mf07.dto.KitchenInspectionDtos.RecordUpdateRequest;
import com.sfms.mf07.dto.KitchenInspectionDtos.SubmitKitchenInspectionRequest;
import com.sfms.mf07.service.KitchenInspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

/** MF07 — {@code kitchen-inspections} (UC-014). TDS-MF07_Backend.md §9.3.6. ADR-M07-03. */
@Tag(name = "MF07 — Food Safety Inspection & Monitoring")
@RestController
@RequestMapping("/api/v1/kitchen-inspections")
public class KitchenInspectionController {

    private final KitchenInspectionService kitchenInspectionService;

    public KitchenInspectionController(KitchenInspectionService kitchenInspectionService) {
        this.kitchenInspectionService = kitchenInspectionService;
    }

    @Operation(summary = "Danh sách kiểm tra bếp", description = "UC-014 · UC-027")
    @ApiResponse(responseCode = "200", description = "PagedResponse<KitchenInspectionResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseKitchenInspectionResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<KitchenInspectionResponse> list(@RequestParam Map<String, String> allParams,
                                                          @CurrentUser AuthUser actor) {
        return kitchenInspectionService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết kiểm tra bếp", description = "UC-014")
    @ApiResponse(responseCode = "404", description = "INSP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}")
    public KitchenInspectionResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return kitchenInspectionService.detail(code, actor);
    }

    @Operation(summary = "Chấm điểm vệ sinh bếp", description = "UC-014 — BR-100/BR-104/BR-207; "
            + "<60% cảnh báo (MSG-093), KHÔNG khóa bếp (ADR-M07-03)")
    @ApiResponse(responseCode = "201", description = "MSG-043")
    @ApiResponse(responseCode = "422", description = "INSP-016 — MSG-016 (BR-207, thiếu ảnh/lý do)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KitchenInspectionResponse submit(@Valid @RequestBody SubmitKitchenInspectionRequest request,
                                             @CurrentUser AuthUser actor) {
        return kitchenInspectionService.submit(request, actor);
    }

    @Operation(summary = "Service Caterers xác nhận kết quả", description = "BR-221 (mới 2026-08-03) — "
            + "xác nhận hai chiều biên bản kiểm tra bếp")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/confirm")
    public KitchenInspectionResponse confirm(@PathVariable String code, @CurrentUser AuthUser actor) {
        return kitchenInspectionService.confirmByCaterers(code, actor);
    }

    @Operation(summary = "Sửa biên bản sự việc", description = "BR-221 mở rộng — Medical Staff sửa nội dung "
            + "biên bản tự sinh lúc nộp checklist; khoá sau khi Caterers đã xác nhận")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "409", description = "INSP-STEP_LOCKED — đã xác nhận, không sửa được nữa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PatchMapping("/{code}/record")
    public KitchenInspectionResponse updateRecord(@PathVariable String code,
                                                    @Valid @RequestBody RecordUpdateRequest request,
                                                    @CurrentUser AuthUser actor) {
        return kitchenInspectionService.updateRecord(code, request.content(), actor);
    }

    @Operation(summary = "Tải lên ảnh bằng chứng theo hạng mục", description = "V68 — ảnh gắn ĐÚNG hạng "
            + "mục checklist (BR-207), không gộp chung cả phiên; chụp trực tiếp qua camera (D10-BIS)")
    @ApiResponse(responseCode = "201", description = "OK")
    @ApiResponse(responseCode = "404", description = "INSP-404 — MSG-137 (phiên hoặc hạng mục không tồn tại)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping(value = "/{code}/evidences/{itemCode}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceResponse uploadItemEvidence(@PathVariable String code, @PathVariable String itemCode,
                                                @RequestPart("file") MultipartFile file,
                                                @RequestParam(required = false) String gps,
                                                @CurrentUser AuthUser actor) {
        return kitchenInspectionService.uploadItemEvidence(code, itemCode, file, gps, actor);
    }

    @Operation(summary = "Danh sách ảnh bằng chứng của phiên", description = "V68 — trước 11/08/2026 "
            + "màn xem lại phiên chỉ hiện SỐ đếm, không tải lại được ảnh thật")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}/evidences")
    public java.util.List<com.sfms.mf07.dto.EvidenceDtos.KitchenEvidenceSummary> listEvidences(
            @PathVariable String code, @CurrentUser AuthUser actor) {
        return kitchenInspectionService.listEvidences(code, actor);
    }

    @Operation(summary = "Tải xuống ảnh bằng chứng", description = "BR-083 · D10-BIS — tải qua endpoint "
            + "có @PreAuthorize + audit, KHÔNG qua CDN/URL công khai")
    @ApiResponse(responseCode = "200", description = "Binary")
    @ApiResponse(responseCode = "404", description = "INSP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}/evidences/{evidenceId}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadEvidence(
            @PathVariable String code, @PathVariable String evidenceId, @CurrentUser AuthUser actor) {
        var file = kitchenInspectionService.downloadEvidence(code, evidenceId, actor);
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return org.springframework.http.ResponseEntity.ok()
                .contentType(contentType != null
                        ? org.springframework.http.MediaType.parseMediaType(contentType)
                        : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }
}
