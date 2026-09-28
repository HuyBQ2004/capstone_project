package com.sfms.mf07.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceResponse;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceSummary;
import com.sfms.mf07.dto.InspectionDtos.CoSignRequest;
import com.sfms.mf07.dto.InspectionDtos.InspectionSessionResponse;
import com.sfms.mf07.dto.InspectionDtos.MedicalReviewRequest;
import com.sfms.mf07.dto.InspectionDtos.OpenSessionRequest;
import com.sfms.mf07.dto.InspectionDtos.PagedResponseInspectionSummaryResponse;
import com.sfms.mf07.dto.InspectionDtos.ServingGateResponse;
import com.sfms.mf07.dto.InspectionDtos.SubmitStepRequest;
import com.sfms.mf07.service.EvidenceService;
import com.sfms.mf07.service.InspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import java.time.LocalDate;
import java.util.Map;

/**
 * MF07 — {@code inspections} (Kiểm thực ba bước, UC-052/UC-065/UC-112/UC-124).
 * TDS-MF07_Backend.md §9.2/§16. D8/S1: một {@code @Tag}.
 */
@Tag(name = "MF07 — Food Safety Inspection & Monitoring")
@RestController
@RequestMapping("/api/v1/inspections")
public class InspectionController {

    private final InspectionService inspectionService;
    private final EvidenceService evidenceService;

    public InspectionController(InspectionService inspectionService, EvidenceService evidenceService) {
        this.inspectionService = inspectionService;
        this.evidenceService = evidenceService;
    }

    @Operation(summary = "Danh sách phiên kiểm thực", description = "UC-112 (Medical/SchoolAdmin, toàn "
            + "trường) · UC-124 (Caterer, chỉ phiên của mình — BR-099)")
    @ApiResponse(responseCode = "200", description = "PagedResponse<InspectionSessionResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseInspectionSummaryResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<InspectionSessionResponse> list(@RequestParam Map<String, String> allParams,
                                                          @CurrentUser AuthUser actor) {
        return inspectionService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết phiên kiểm thực", description = "UC-112 · UC-124")
    @ApiResponse(responseCode = "404", description = "INSP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}")
    public InspectionSessionResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return inspectionService.detail(code, actor);
    }

    @Operation(summary = "Mở phiên kiểm thực ba bước", description = "UC-052 — BR-079 (một bữa chỉ 1 "
            + "phiên gốc còn hiệu lực)")
    @ApiResponse(responseCode = "201", description = "OK")
    @ApiResponse(responseCode = "409", description = "INSP-409 — MSG-139 (D5, BR-079)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionSessionResponse open(@Valid @RequestBody OpenSessionRequest request, @CurrentUser AuthUser actor) {
        return inspectionService.open(request, actor);
    }

    @Operation(summary = "Nộp một bước kiểm thực", description = "UC-052 · UC-065 — BR-079 (khóa tuần "
            + "tự) · BR-083 (ảnh bắt buộc) · BR-090/BR-091 (ngưỡng nhiệt độ) · BR-092 (lô quá hạn)")
    @ApiResponse(responseCode = "200", description = "MSG-043")
    @ApiResponse(responseCode = "409", description = "INSP-079 — MSG-147 (BR-079)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "INSP-017/INSP-091/INSP-092/INSP-083",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/steps/{step}")
    public InspectionSessionResponse submitStep(@PathVariable String code, @PathVariable int step,
                                                 @Valid @RequestBody SubmitStepRequest request,
                                                 @CurrentUser AuthUser actor) {
        return inspectionService.submitStep(code, step, request, actor);
    }

    @Operation(summary = "Đồng ký biên bản kiểm thực", description = "UC-052 bước 5 — BR-099, chỉ khi "
            + "cả 3 bước PASSED (BR-098)")
    @ApiResponse(responseCode = "200", description = "MSG-043")
    @ApiResponse(responseCode = "409", description = "INSP-098 — MSG-129 (BR-098)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SERVICE_CATERERS','SCHOOL_ADMIN')")
    @PostMapping("/{code}/co-sign")
    public InspectionSessionResponse coSign(@PathVariable String code, @Valid @RequestBody CoSignRequest request,
                                             @CurrentUser AuthUser actor) {
        return inspectionService.coSign(code, request, actor);
    }

    @Operation(summary = "Tái kiểm tra sau khi trượt", description = "UC-052 AF1 — BR-096, tạo phiên "
            + "MỚI, phiên cũ giữ nguyên (§4.2 bất biến)")
    @ApiResponse(responseCode = "201", description = "MSG-056")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/reinspect")
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionSessionResponse reinspect(@PathVariable String code, @CurrentUser AuthUser actor) {
        return inspectionService.reinspect(code, actor);
    }

    @Operation(summary = "Đánh giá độc lập của Y tế", description = "UC-112 — BR-222 (mới 2026-08-03), "
            + "KHÔNG ghi đè overall, KHÔNG đổi kết quả BR-094/BR-098")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "422", description = "INSP-001 — thiếu medicalReviewReason khi FAILED",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{code}/medical-review")
    public InspectionSessionResponse medicalReview(@PathVariable String code,
                                                    @Valid @RequestBody MedicalReviewRequest request,
                                                    @CurrentUser AuthUser actor) {
        return inspectionService.medicalReview(code, request, actor);
    }

    @Operation(summary = "Cổng chặn phục vụ bữa ăn", description = "BR-094 — MF09 gọi trước khi cho "
            + "phép ghi nhận suất ăn")
    @ApiResponse(responseCode = "200", description = "OK — {allowed, reasonCode, sessionId}")
    @PreAuthorize("hasAnyRole('SERVICE_CATERERS','TEACHER','SCHOOL_ADMIN','SYSTEM_ADMIN')")
    @GetMapping("/serving-gate")
    public ServingGateResponse servingGate(@RequestParam LocalDate date, @RequestParam String mealType,
                                            @CurrentUser AuthUser actor) {
        return inspectionService.checkGate(date, mealType, actor);
    }

    @Operation(summary = "Tải lên ảnh bằng chứng", description = "D10-BIS — ảnh bằng chứng là tài liệu "
            + "RIÊNG TƯ; ADR-M07-04: sha256 chống sửa ảnh")
    @ApiResponse(responseCode = "201", description = "OK")
    @PreAuthorize("hasAnyRole('SERVICE_CATERERS','MEDICAL_STAFF')")
    @PostMapping(value = "/{code}/evidences", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceResponse uploadEvidence(@PathVariable String code, @RequestParam String ownerType,
                                            @RequestParam String ownerId, @RequestPart("file") MultipartFile file,
                                            @RequestParam(required = false) String gps, @CurrentUser AuthUser actor) {
        return evidenceService.upload(ownerType, ownerId, file, gps, actor);
    }

    @Operation(summary = "Tải lên ảnh bằng chứng theo phiên", description = "D1 — frontend không có "
            + "UUID nội bộ để gọi POST /{code}/evidences (ownerId) trực tiếp; endpoint này tự resolve "
            + "ownerId = UUID của phiên {code}, ownerType một trong STEP1_FRESH/STEP1_DRY/STEP2/STEP3/SESSION")
    @ApiResponse(responseCode = "201", description = "OK")
    @PreAuthorize("hasAnyRole('SERVICE_CATERERS','MEDICAL_STAFF')")
    @PostMapping(value = "/{code}/evidences/{ownerType}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceResponse uploadSessionEvidence(@PathVariable String code, @PathVariable String ownerType,
                                                   @RequestPart("file") MultipartFile file,
                                                   @RequestParam(required = false) String gps,
                                                   @CurrentUser AuthUser actor) {
        return evidenceService.uploadForSession(code, ownerType, file, gps, actor);
    }

    @Operation(summary = "Tải xuống ảnh bằng chứng", description = "BR-083 · D10-BIS — tải qua endpoint "
            + "có @PreAuthorize + audit, KHÔNG qua CDN/URL công khai")
    @ApiResponse(responseCode = "200", description = "Binary")
    @ApiResponse(responseCode = "404", description = "INSP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}/evidences/{evidenceId}")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable String code, @PathVariable String evidenceId,
                                                       @CurrentUser AuthUser actor) {
        var file = evidenceService.download(code, evidenceId, actor);
        /*
         * Sửa 10/08/2026 — response trước đây KHÔNG set `Content-Type` (Spring rơi về
         * `application/octet-stream`) và ép `Content-Disposition: attachment`. Component
         * xem ảnh (`components/app/evidence-gallery.tsx`) tải blob rồi mở
         * `<a href={blobUrl} target="_blank">` — không có Content-Type đúng, trình duyệt
         * không biết là ảnh nên mở tab mới ra chuỗi byte nhị phân thay vì hiển thị ảnh.
         * `guessContentTypeFromName` suy MIME từ đuôi file gốc (`fileName`, VD "evidence-0.jpg");
         * `inline` thay `attachment` để trình duyệt HIỂN THỊ ảnh thay vì ép tải xuống.
         */
        String contentType = java.net.URLConnection.guessContentTypeFromName(file.fileName());
        return ResponseEntity.ok()
                .contentType(contentType != null
                        ? org.springframework.http.MediaType.parseMediaType(contentType)
                        : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Danh sách ảnh bằng chứng của phiên", description = "10/08/2026 — trước đây "
            + "ảnh upload xong không có cách nào liệt kê lại; gom theo (phiên, loại bước), KHÔNG phân "
            + "biệt được từng dòng trong bước (xem EvidenceSummary)")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')")
    @GetMapping("/{code}/evidences")
    public java.util.List<EvidenceSummary> listEvidences(@PathVariable String code, @CurrentUser AuthUser actor) {
        return evidenceService.listForSession(code, actor);
    }
}
