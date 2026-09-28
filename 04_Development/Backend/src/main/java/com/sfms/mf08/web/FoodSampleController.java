package com.sfms.mf08.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf08.dto.ExportDtos.ExportFormat;
import com.sfms.mf08.dto.ExportDtos.ExportedFile;
import com.sfms.mf08.dto.FoodSampleDtos.DisposeSampleRequest;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.dto.FoodSampleDtos.IncidentLinkRequest;
import com.sfms.mf08.dto.FoodSampleDtos.Mau4LabelResponse;
import com.sfms.mf08.dto.FoodSampleDtos.PagedResponseFoodSampleResponse;
import com.sfms.mf08.dto.FoodSampleDtos.RegisterSampleRequest;
import com.sfms.mf08.dto.FoodSampleDtos.UpdateStorageRequest;
import com.sfms.mf08.error.SmpExceptions;
import com.sfms.mf08.service.FoodSampleService;
import com.sfms.mf08.service.SampleRegisterExportService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * MF08 — {@code samples} (Lưu mẫu thực phẩm, UC-069/042/030/076/118). TDS-MF08_Backend.md
 * §9.2/§16.
 */
@Tag(name = "MF08 — Food Sample Retention")
@RestController
@RequestMapping("/api/v1/samples")
public class FoodSampleController {

    private final FoodSampleService foodSampleService;
    private final SampleRegisterExportService exportService;

    public FoodSampleController(FoodSampleService foodSampleService, SampleRegisterExportService exportService) {
        this.foodSampleService = foodSampleService;
        this.exportService = exportService;
    }

    @Operation(summary = "Danh sách mẫu lưu", description = "UC-118/042")
    @ApiResponse(responseCode = "200", description = "PagedResponse<FoodSampleResponse> — D11 KHÔNG oneOf (M08-D13)",
            content = @Content(schema = @Schema(implementation = PagedResponseFoodSampleResponse.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<FoodSampleResponse> list(@RequestParam Map<String, String> allParams,
                                                  @CurrentUser AuthUser actor) {
        return foodSampleService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết mẫu lưu", description = "UC-118")
    @ApiResponse(responseCode = "404", description = "SMP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{id}")
    public FoodSampleResponse detail(@PathVariable("id") String code, @CurrentUser AuthUser actor) {
        return foodSampleService.getById(code, actor);
    }

    @Operation(summary = "Đăng ký lưu mẫu", description = "UC-069 — cổng BR-105→BR-109→BR-203→BR-107→BR-110 (C4)")
    @ApiResponse(responseCode = "201", description = "MSG-044")
    @ApiResponse(responseCode = "409", description = "SMP-105 (BR-105) / SMP-409 (D5)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SMP-019/SMP-109/SMP-107/SMP-203",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FoodSampleResponse register(@Valid @RequestPart("payload") RegisterSampleRequest payload,
                                       @RequestPart(value = "evidencePhotos", required = false) List<MultipartFile> evidencePhotos,
                                       @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                       @CurrentUser AuthUser actor) {
        return foodSampleService.register(payload, evidencePhotos, userAgent, actor);
    }

    @Operation(summary = "Dữ liệu nhãn Mẫu số 4", description = "UC-069 bước 4 (BR-106)")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @GetMapping("/{id}/label")
    public Mau4LabelResponse label(@PathVariable("id") String code, @CurrentUser AuthUser actor) {
        return foodSampleService.buildLabel(code, actor);
    }

    @Operation(summary = "Tải ảnh bằng chứng lưu mẫu", description = "D10-BIS — tài liệu riêng tư, tải qua endpoint có @PreAuthorize")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{id}/evidence-photos/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadEvidencePhoto(
            @PathVariable("id") String code, @PathVariable String fileId, @CurrentUser AuthUser actor) {
        /*
         * 11/08/2026 — trước đây không set Content-Type (mặc định application/octet-stream)
         * và ép `attachment`, nên trình duyệt mở tab mới ra chuỗi byte nhị phân thay vì hiển
         * thị ảnh. `guessContentTypeFromName` suy MIME từ đuôi file gốc; `inline` để trình
         * duyệt HIỂN THỊ ảnh (cùng cách vá `InspectionController.downloadEvidence` 10/08/2026).
         */
        var file = foodSampleService.downloadEvidencePhoto(code, fileId, actor);
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return ResponseEntity.ok()
                .contentType(contentType != null
                        ? org.springframework.http.MediaType.parseMediaType(contentType)
                        : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Cập nhật vị trí lưu / nhiệt độ", description = "UC-042 AF2 (BR-110, cảnh báo mềm)")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PatchMapping("/{id}/storage")
    public FoodSampleResponse updateStorage(@PathVariable("id") String code,
                                            @Valid @RequestBody UpdateStorageRequest request,
                                            @CurrentUser AuthUser actor) {
        return foodSampleService.updateStorage(code, request, actor);
    }

    @Operation(summary = "Hủy mẫu", description = "UC-030 — BR-111 (hạn lưu 24h) · BR-112 (khóa hủy khi có sự cố mở)")
    @ApiResponse(responseCode = "409", description = "SMP-111 / SMP-112",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SMP-030 (Mẫu số 5 cột 9/10/12 thiếu)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{id}/dispose")
    public FoodSampleResponse dispose(@PathVariable("id") String code,
                                      @Valid @RequestBody DisposeSampleRequest request,
                                      @CurrentUser AuthUser actor) {
        return foodSampleService.dispose(code, request, actor);
    }

    @Operation(summary = "Liên kết mẫu đã hủy với sự cố", description = "UC-076 AF1 — chỉ metadata")
    @ApiResponse(responseCode = "409", description = "SMP-023 — MSG-128 (BR-114)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{id}/incident-link")
    public FoodSampleResponse linkIncident(@PathVariable("id") String code,
                                           @Valid @RequestBody IncidentLinkRequest request,
                                           @CurrentUser AuthUser actor) {
        return foodSampleService.linkDisposedToIncident(code, request, actor);
    }

    @Operation(summary = "Xuất sổ Mẫu số 5", description = "UC-118 — 12 cột theo Phụ lục 2 QĐ 1246/QĐ-BYT")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF')")
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
            throw SmpExceptions.validationFailed("format", "must be xlsx or pdf");
        }
    }
}
