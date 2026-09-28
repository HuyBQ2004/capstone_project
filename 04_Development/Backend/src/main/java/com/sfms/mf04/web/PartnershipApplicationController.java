package com.sfms.mf04.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationPortalState;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationResponse;
import com.sfms.mf04.dto.ApplicationDtos.SubmitApplicationRequest;
import com.sfms.mf04.service.PartnershipApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.util.Map;

/** MF04 — đơn hợp tác (UC-152/155/169/170 · UC-156/157/158/159). */
@Tag(name = "MF04 — Supplier & Partner Management")
@RestController
@RequestMapping("/api/v1/partnership-applications")
public class PartnershipApplicationController {

    private final PartnershipApplicationService applicationService;

    public PartnershipApplicationController(PartnershipApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(summary = "Danh sách đơn hợp tác", description = "UC-169/157 (NCC — của mình) · "
            + "School Admin (của trường mình) · System Admin (toàn nền tảng)")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<ApplicationResponse> list(@RequestParam Map<String, String> allParams,
                                                   @CurrentUser AuthUser actor) {
        return applicationService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Trạng thái cổng nộp đơn", description = "UC-155/159 Normal Flow bước 3 — "
            + "NO_APPLICATION | TRACKING | CAN_RESUBMIT")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping("/portal-state")
    public ApplicationPortalState portalState(@CurrentUser AuthUser actor) {
        return applicationService.portalState(actor);
    }

    @Operation(summary = "Nộp đơn hợp tác", description = "UC-152/UC-156 · BR-040 — 1 đơn đang mở/"
            + "(partner, school)")
    @ApiResponse(responseCode = "201", description = "MSG-028")
    @ApiResponse(responseCode = "404", description = "SUP-404 — MSG-137 trường không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SUP-013/SUP-409/SUP-139",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SUP-007 — MSG-007 sai định dạng mã số thuế",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SUPPLIER', 'SERVICE_CATERERS')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submit(@Valid @RequestPart("meta") SubmitApplicationRequest meta,
                                      @RequestPart(value = "businessLicense", required = false) MultipartFile businessLicense,
                                      @CurrentUser AuthUser actor) {
        return applicationService.submit(meta, actor);
    }

    @Operation(summary = "Nộp lại đơn hợp tác", description = "UC-170/UC-158 — chỉ hợp lệ khi đơn "
            + "REJECTED/ACTION_REQUIRED, giữ nguyên mã đơn")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "404", description = "SUP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SUP-409 — đơn không ở trạng thái nộp lại được",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SUPPLIER', 'SERVICE_CATERERS')")
    @PostMapping(value = "/{code}/resubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApplicationResponse resubmit(@PathVariable String code,
                                        @Valid @RequestPart("meta") SubmitApplicationRequest meta,
                                        @RequestPart(value = "businessLicense", required = false) MultipartFile businessLicense,
                                        @CurrentUser AuthUser actor) {
        return applicationService.resubmit(code, meta, actor);
    }
}
