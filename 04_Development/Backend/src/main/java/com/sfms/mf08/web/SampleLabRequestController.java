package com.sfms.mf08.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.dto.SampleLabRequestDtos.HandOverSamplesRequest;
import com.sfms.mf08.dto.SampleLabRequestDtos.PagedResponseSampleLabRequestResponse;
import com.sfms.mf08.dto.SampleLabRequestDtos.SampleLabRequestResponse;
import com.sfms.mf08.service.SampleLabRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * MF08 — {@code sample-lab-requests} (Thu hồi &amp; bàn giao mẫu, UC-076, BR-113/212). ADR-M08-1.
 */
@Tag(name = "MF08 — Food Sample Retention")
@RestController
@RequestMapping("/api/v1/sample-lab-requests")
public class SampleLabRequestController {

    private final SampleLabRequestService labRequestService;

    public SampleLabRequestController(SampleLabRequestService labRequestService) {
        this.labRequestService = labRequestService;
    }

    @Operation(summary = "Danh sách phiếu bàn giao mẫu", description = "UC-076")
    @ApiResponse(responseCode = "200", description = "PagedResponse<SampleLabRequestResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseSampleLabRequestResponse.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<SampleLabRequestResponse> list(@RequestParam Map<String, String> allParams,
                                                        @CurrentUser AuthUser actor) {
        return labRequestService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết phiếu bàn giao mẫu", description = "UC-076")
    @ApiResponse(responseCode = "404", description = "SMP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{id}")
    public SampleLabRequestResponse detail(@PathVariable("id") String code, @CurrentUser AuthUser actor) {
        return labRequestService.getById(code, actor);
    }

    @Operation(summary = "Thu hồi & bàn giao lô mẫu", description = "UC-076, BR-113/212 — 1 transaction "
            + "(ADR-M08-1), CHỈ Medical Staff (BR-215 Deprecated — CF-M08-2)")
    @ApiResponse(responseCode = "201", description = "OK")
    @ApiResponse(responseCode = "409", description = "SMP-023 (MSG-128) / SMP-113 (mẫu không còn RETAINED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SMP-212 — thiếu chỉ tiêu/biên bản giao nhận",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SampleLabRequestResponse handOver(@Valid @RequestPart("payload") HandOverSamplesRequest payload,
                                             @RequestPart(value = "sealPhotos", required = false) List<MultipartFile> sealPhotos,
                                             @RequestPart(value = "handoverRecords", required = false) List<MultipartFile> handoverRecords,
                                             @RequestPart(value = "labReceipts", required = false) List<MultipartFile> labReceipts,
                                             @CurrentUser AuthUser actor) {
        return labRequestService.handOver(payload, sealPhotos, handoverRecords, labReceipts, actor);
    }

    @Operation(summary = "Tải tệp đính kèm phiếu bàn giao mẫu",
            description = "D10-BIS — ảnh niêm phong/biên bản giao nhận/phiếu nhận lab (BR-212), tài liệu "
                    + "riêng tư nên đi qua endpoint có @PreAuthorize")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{id}/files/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @PathVariable("id") String code, @PathVariable String fileId, @CurrentUser AuthUser actor) {
        // 11/08/2026 — cùng bug/cách vá `FoodSampleController.downloadEvidencePhoto` (Content-Type
        // + inline thay vì attachment mù MIME) — file ở đây có thể là ảnh (niêm phong) hoặc PDF
        // (biên bản/phiếu nhận lab), guessContentTypeFromName xử lý đúng cả hai theo đuôi file.
        var file = labRequestService.downloadFile(code, fileId, actor);
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return ResponseEntity.ok()
                .contentType(contentType != null
                        ? org.springframework.http.MediaType.parseMediaType(contentType)
                        : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Mẫu có thể thu hồi cho một sự cố", description = "BR-149 — cửa sổ điều tra")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @GetMapping("/retrievable")
    public List<FoodSampleResponse> retrievable(@RequestParam String incidentId,
                                                @RequestParam(defaultValue = "false") boolean includeAllDishes,
                                                @CurrentUser AuthUser actor) {
        return labRequestService.retrievableForIncident(incidentId, includeAllDishes, actor);
    }
}
