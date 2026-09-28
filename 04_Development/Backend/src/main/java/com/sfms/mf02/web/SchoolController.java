package com.sfms.mf02.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf02.dto.SchoolConfigDtos.SchoolConfigResponse;
import com.sfms.mf02.dto.SchoolConfigDtos.UpdateSchoolConfigRequest;
import com.sfms.mf02.dto.SchoolDtos;
import com.sfms.mf02.dto.SchoolDtos.ChangeSchoolStatusRequest;
import com.sfms.mf02.dto.SchoolDtos.RegisterSchoolRequest;
import com.sfms.mf02.dto.SchoolDtos.SchoolResponse;
import com.sfms.mf02.dto.SchoolDtos.UnlockCoordinateRequest;
import com.sfms.mf02.dto.SchoolDtos.UpdateSchoolRequest;
import com.sfms.mf02.dto.SchoolPolicyDtos.SchoolPolicyResponse;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.service.SchoolConfigService;
import com.sfms.mf02.service.SchoolPolicyService;
import com.sfms.mf02.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sfms.mf02.domain.SchoolDocument;
import com.sfms.mf02.dto.SchoolDtos.SchoolDocumentResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * MF02 — hồ sơ trường học (UC-070/001/095/122/140/137). §16 Authorization Matrix.
 *
 * <p>D8/S1: một {@code @Tag} cho cả controller. S2: {@code @Operation.description} chứa
 * UC ID + BR ID. S3: mỗi mã lỗi §10 có {@code @ApiResponse} trỏ {@link ErrorResponse}.
 */
@Tag(name = "MF02 — School & Class Management")
@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

    private final SchoolService schoolService;
    private final SchoolConfigService schoolConfigService;
    private final SchoolPolicyService schoolPolicyService;
    private final RegisterRateLimiter rateLimiter;

    public SchoolController(SchoolService schoolService,
                            SchoolConfigService schoolConfigService,
                            SchoolPolicyService schoolPolicyService,
                            RegisterRateLimiter rateLimiter) {
        this.schoolService = schoolService;
        this.schoolConfigService = schoolConfigService;
        this.schoolPolicyService = schoolPolicyService;
        this.rateLimiter = rateLimiter;
    }

    @Operation(
            summary = "Đăng ký trường học mới",
            description = "UC-070 · BR-028 — trạng thái khởi tạo PENDING, chờ System Admin duyệt. "
                    + "Rate limit 5/min/IP.")
    @ApiResponse(responseCode = "201", description = "MSG-032")
    @ApiResponse(responseCode = "400", description = "SCH-007/SCH-013 — MSG-007/MSG-107 sai định "
            + "dạng/tọa độ", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SCH-409 — MSG-139 trùng id/taxId (D5)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "SCH-014 — MSG-141 vượt rate limit",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolResponse register(@Valid @RequestPart("payload") RegisterSchoolRequest request,
                                   @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
                                   HttpServletRequest servletRequest) {
        if (!rateLimiter.tryAcquire(clientIp(servletRequest))) {
            throw SchExceptions.rateLimited();
        }
        return schoolService.register(request, documents);
    }

    @Operation(summary = "Danh sách trường học", description = "UC-140 — System Admin: toàn "
            + "bộ field, mọi trạng thái. Supplier VÀ Service Caterers (UC chọn trường xin hợp "
            + "tác, MF04): rút gọn id/name/status, ép ACTIVE-only, tối thiểu hóa dữ liệu "
            + "(chốt 2026-08-02; thêm SERVICE_CATERERS 2026-08-08).")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1 — "
            + "PagedResponse<SchoolResponse> (System Admin) hoặc PagedResponse<SchoolDirectoryResponse> (Supplier)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(oneOf = {
                            SchoolDtos.PagedResponseSchoolResponse.class,
                            SchoolDtos.PagedResponseSchoolDirectoryResponse.class})))
    /*
     * SERVICE_CATERERS thêm 08/08/2026: vai này cũng phải CHỌN TRƯỜNG khi nộp đơn hợp tác
     * (`POST /partnership-applications` vốn đã cấp quyền cho họ từ trước), nhưng không đọc
     * được danh sách trường thì ô chọn rỗng — không nộp nổi đơn.
     *
     * An toàn: nhánh dưới đã tự trả `directory()` cho mọi vai không phải System Admin —
     * rút gọn id/name/status và ép ACTIVE-only, không lộ thêm gì.
     */
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<?> list(@RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        ListQuery query = ListQuery.from(allParams);
        return actor.isSystemAdmin() ? schoolService.list(query) : schoolService.directory(query);
    }

    @Operation(summary = "Chi tiết một trường học", description = "UC-122")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137 không tìm thấy",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/{code}")
    public SchoolResponse get(@PathVariable String code) {
        return schoolService.getByCode(code);
    }

    @Operation(
            summary = "Cập nhật hồ sơ trường học",
            description = "UC-095 · BR-028 — sửa lat/lng khi coordinateLocked=true bị từ chối")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "403", description = "SCH-013 — MSG-108 tọa độ đã khóa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PatchMapping("/{code}")
    public SchoolResponse update(@PathVariable String code, @Valid @RequestBody UpdateSchoolRequest request) {
        return schoolService.update(code, request);
    }

    @Operation(summary = "Kích hoạt/vô hiệu hóa trường học", description = "UC-001 · BR-021 — "
            + "cascade tạm dừng lớp + tài khoản khi INACTIVE")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PatchMapping("/{code}/status")
    public SchoolResponse changeStatus(@PathVariable String code,
                                       @Valid @RequestBody ChangeSchoolStatusRequest request) {
        return schoolService.changeStatus(code, request.status());
    }

    @Operation(summary = "Mở khóa tọa độ GPS", description = "BR-028 — chỉ System Admin, bắt "
            + "buộc lý do văn bản, ghi audit")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping("/{code}/coordinate/unlock")
    public SchoolResponse unlockCoordinate(@PathVariable String code,
                                           @Valid @RequestBody UnlockCoordinateRequest request,
                                           @CurrentUser AuthUser actor) {
        return schoolService.unlockCoordinate(code, request.reason(), actor);
    }

    @Operation(summary = "Xem cấu hình trường học", description = "UC-095 · BR-028 (geofence), "
            + "BR-080 (costCeiling, miền MF05, chỉ tham chiếu)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137 (bao gồm cross-school, BR-022)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    // SERVICE_CATERERS thêm 06/08/2026 — MenuForm cần đọc costCeiling (BR-080, chỉ tham chiếu,
    // không PII/không nhạy cảm) để hiện trần chi phí lúc soạn thực đơn; PATCH vẫn CHỈ System/School
    // Admin (không đổi cấu hình).
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'SERVICE_CATERERS')")
    @GetMapping("/{code}/config")
    public SchoolConfigResponse getConfig(@PathVariable String code, @CurrentUser AuthUser actor) {
        return schoolConfigService.get(code, actor);
    }

    @Operation(summary = "Cập nhật cấu hình trường học", description = "UC-095 · BR-028")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @PatchMapping("/{code}/config")
    public SchoolConfigResponse updateConfig(@PathVariable String code,
                                             @Valid @RequestBody UpdateSchoolConfigRequest request,
                                             @CurrentUser AuthUser actor) {
        return schoolConfigService.update(code, request, actor);
    }

    @Operation(summary = "Danh sách tài liệu đính kèm của trường học", description = "UC-122")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping("/{code}/documents")
    public List<SchoolDocumentResponse> listDocuments(@PathVariable String code) {
        return schoolService.listDocuments(code);
    }

    @Operation(summary = "Tải lên tài liệu đính kèm cho trường học", description = "UC-070 / UC-122")
    @ApiResponse(responseCode = "201", description = "Created")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @PostMapping(value = "/{code}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolDocumentResponse uploadDocument(
            @PathVariable String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestPart("file") MultipartFile file) {
        return schoolService.uploadDocument(code, name, documentType, file);
    }

    @Operation(summary = "Tải xuống/xem file tài liệu đính kèm", description = "UC-122")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping("/{code}/documents/{docCode}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String code, @PathVariable String docCode) {
        SchoolDocument doc = schoolService.getDocumentEntity(docCode);
        Resource file = schoolService.downloadDocument(code, docCode);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(file);
    }

    @Operation(summary = "Xem chính sách trường của chính mình", description = "UC-137 · BR-022 — "
            + "chỉ trường trong JWT, KHÔNG nhận schoolId qua param")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137 (actor không thuộc trường nào)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'MEDICAL_STAFF', 'SERVICE_CATERERS')")
    @GetMapping("/me/policy")
    public SchoolPolicyResponse getMyPolicy(@CurrentUser AuthUser actor) {
        return schoolPolicyService.getForCurrentUser(actor);
    }

    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
