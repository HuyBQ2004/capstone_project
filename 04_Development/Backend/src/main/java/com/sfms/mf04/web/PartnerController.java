package com.sfms.mf04.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf04.domain.PartnerRegistrationDocument;
import com.sfms.mf04.dto.PartnerDtos.PartnerActionResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerDetailResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerRegistrationDocumentResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerResponse;
import com.sfms.mf04.dto.PartnerDtos.RegisterPartnerRequest;
import com.sfms.mf04.dto.PartnerDtos.RejectPartnerRequest;
import com.sfms.mf04.dto.PartnerDtos.SuspendPartnerRequest;
import com.sfms.mf04.dto.PartnerDtos.UpdatePartnerProfileRequest;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.service.PartnerApprovalService;
import com.sfms.mf04.service.PartnerProfileService;
import com.sfms.mf04.service.PartnerRegistrationService;
import com.sfms.mf04.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.util.List;
import java.util.Map;

/**
 * MF04 — hồ sơ đối tác (UC-071/067/049/007/004 + ADR-M04-6). §16 Authorization Matrix.
 */
@Tag(name = "MF04 — Supplier & Partner Management")
@RestController
@RequestMapping("/api/v1/partners")
public class PartnerController {

    private final PartnerService partnerService;
    private final PartnerRegistrationService registrationService;
    private final PartnerProfileService profileService;
    private final PartnerApprovalService approvalService;
    private final SlidingWindowRateLimiter mf04RegisterRateLimiter;

    public PartnerController(PartnerService partnerService, PartnerRegistrationService registrationService,
                             PartnerProfileService profileService, PartnerApprovalService approvalService,
                             SlidingWindowRateLimiter mf04RegisterRateLimiter) {
        this.partnerService = partnerService;
        this.registrationService = registrationService;
        this.profileService = profileService;
        this.approvalService = approvalService;
        this.mf04RegisterRateLimiter = mf04RegisterRateLimiter;
    }

    @Operation(summary = "Đăng ký tài khoản Nhà cung cấp", description = "UC-071 · BR-040, BR-046 — "
            + "trạng thái khởi tạo PENDING. Rate limit 5/min/IP.")
    @ApiResponse(responseCode = "201", description = "MSG-028")
    @ApiResponse(responseCode = "409", description = "SUP-006/SUP-139 — MSG-006/MSG-139",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SUP-007 — MSG-007 sai định dạng mã số thuế",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "SUP-141 — MSG-141",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @SecurityRequirements
    @PostMapping(value = "/register/supplier", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerResponse registerSupplier(@Valid @RequestPart("payload") RegisterPartnerRequest request,
                                            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
                                            HttpServletRequest servletRequest) {
        guardRateLimit(servletRequest);
        return registrationService.registerSupplier(request, documents);
    }

    @Operation(summary = "Đăng ký tài khoản Đơn vị suất ăn", description = "UC-067 · BR-040, BR-046. "
            + "Rate limit 5/min/IP.")
    @ApiResponse(responseCode = "201", description = "MSG-028")
    @SecurityRequirements
    @PostMapping(value = "/register/caterer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerResponse registerCaterer(@Valid @RequestPart("payload") RegisterPartnerRequest request,
                                           @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
                                           HttpServletRequest servletRequest) {
        guardRateLimit(servletRequest);
        return registrationService.registerCaterer(request, documents);
    }

    @Operation(summary = "Danh sách tài liệu nộp kèm lúc đăng ký", description = "UC-071/067 · 11/08/2026")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping("/{code}/registration-documents")
    public List<PartnerRegistrationDocumentResponse> listRegistrationDocuments(@PathVariable String code) {
        return registrationService.listRegistrationDocuments(code);
    }

    @Operation(summary = "Tải xuống/xem file tài liệu nộp kèm lúc đăng ký", description = "UC-071/067 · 11/08/2026")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping("/{code}/registration-documents/{docCode}/download")
    public ResponseEntity<Resource> downloadRegistrationDocument(@PathVariable String code,
                                                                   @PathVariable String docCode) {
        PartnerRegistrationDocument doc = registrationService.getRegistrationDocumentEntity(docCode);
        Resource file = registrationService.downloadRegistrationDocument(code, docCode);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(file);
    }

    @Operation(summary = "Danh sách đối tác", description = "UC-131 (Supplier) · UC-127 (Service "
            + "Caterers) — lọc theo ?type. School Admin/Medical Staff chỉ thấy đối tác có đơn/hợp tác "
            + "với trường mình (M11-D18 — Medical Staff cần chọn đơn vị suất ăn phụ trách CAPA, BR-152).")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'MEDICAL_STAFF', 'SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<PartnerResponse> list(@RequestParam Map<String, String> allParams,
                                               @CurrentUser AuthUser actor) {
        return partnerService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Hồ sơ của chính đối tác đang đăng nhập", description = "UC-049")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping("/me")
    public PartnerDetailResponse getMine(@CurrentUser AuthUser actor) {
        return profileService.getMine(actor);
    }

    @Operation(summary = "Cập nhật hồ sơ của chính đối tác", description = "UC-049 — chỉ sửa "
            + "companyName/address/email/phone/description")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "409", description = "SUP-006 — MSG-006 email đã dùng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SUPPLIER', 'SERVICE_CATERERS')")
    @PatchMapping("/me")
    public PartnerDetailResponse updateMine(@RequestBody UpdatePartnerProfileRequest request,
                                            @CurrentUser AuthUser actor) {
        return profileService.updateMine(request, actor);
    }

    @Operation(summary = "Chi tiết một đối tác", description = "UC-148 (Supplier) · UC-108 "
            + "(Service Caterers) — path param là mã (D1). Medical Staff xem để đánh giá tuân thủ "
            + "(11/08/2026, theo Nam) — chỉ đọc, cùng phạm vi trường như School Admin.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SUP-404 — MSG-137 (bao gồm ngoài phạm vi trường, M04-D10)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'MEDICAL_STAFF', 'SERVICE_CATERERS')")
    @GetMapping("/{code}")
    public PartnerDetailResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return partnerService.getDetail(code, actor);
    }

    @Operation(summary = "Phê duyệt đối tác", description = "UC-007/UC-004 · BR-040, BR-041 — "
            + "chặn khi còn tài liệu ATTP hết hạn")
    @ApiResponse(responseCode = "200", description = "MSG-037")
    @ApiResponse(responseCode = "404", description = "SUP-404 — MSG-137 (chưa có đơn với trường mình, M04-D10)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SUP-013 — MSG-013 chứng nhận ATTP hết hạn",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/approve")
    public PartnerActionResponse approve(@PathVariable String code, @CurrentUser AuthUser actor) {
        return approvalService.approve(code, actor);
    }

    @Operation(summary = "Từ chối đối tác", description = "UC-007/UC-004 · BR-048 — lý do bắt buộc")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "422", description = "SUP-048 — MSG-113 thiếu lý do",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/reject")
    public PartnerActionResponse reject(@PathVariable String code, @RequestBody RejectPartnerRequest request,
                                        @CurrentUser AuthUser actor) {
        return approvalService.reject(code, request.reason(), actor);
    }

    @Operation(summary = "Đình chỉ hợp tác", description = "ADR-M04-6, ràng buộc C5 — cascade khóa "
            + "tài khoản trong CÙNG transaction")
    @ApiResponse(responseCode = "200", description = "OK — trả kèm suspendedAccounts")
    @ApiResponse(responseCode = "422", description = "SUP-048 — MSG-113 thiếu lý do",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/suspend")
    public PartnerActionResponse suspend(@PathVariable String code, @RequestBody SuspendPartnerRequest request,
                                         @CurrentUser AuthUser actor) {
        return approvalService.suspend(code, request.reason(), actor);
    }

    @Operation(summary = "Khôi phục hợp tác", description = "ADR-M04-6 — chặn nếu còn tài liệu URGENT/EXPIRED")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "409", description = "SUP-013 — còn tài liệu URGENT/EXPIRED",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/reinstate")
    public PartnerActionResponse reinstate(@PathVariable String code, @CurrentUser AuthUser actor) {
        return approvalService.reinstate(code, actor);
    }

    private void guardRateLimit(HttpServletRequest request) {
        if (!mf04RegisterRateLimiter.tryAcquire(clientIp(request))) {
            throw SupExceptions.rateLimited();
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
