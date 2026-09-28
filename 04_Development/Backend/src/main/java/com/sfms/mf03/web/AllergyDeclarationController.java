package com.sfms.mf03.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.dto.AllergyDeclarationDtos.AllergyDeclarationResponse;
import com.sfms.mf03.dto.AllergyDeclarationDtos.RequestInfoRequest;
import com.sfms.mf03.dto.AllergyDeclarationDtos.SubmitAllergyRequest;
import com.sfms.mf03.service.AllergyDeclarationService;
import com.sfms.mf03.service.HealthReviewService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF03 — khai báo dị ứng (UC-084/093/077/104/036). BR-031 gate hiệu lực. */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping(value = "/api/v1/allergy-declarations", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class AllergyDeclarationController {

    private final AllergyDeclarationService allergyDeclarationService;
    private final HealthReviewService healthReviewService;

    public AllergyDeclarationController(AllergyDeclarationService allergyDeclarationService,
                                        HealthReviewService healthReviewService) {
        this.allergyDeclarationService = allergyDeclarationService;
        this.healthReviewService = healthReviewService;
    }

    @Operation(summary = "Danh sách khai báo dị ứng", description = "UC-104/036 · BR-031")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','PARENT','TEACHER')")
    @GetMapping
    public PagedResponse<AllergyDeclarationResponse> list(@RequestParam Map<String, String> allParams,
                                                           @CurrentUser AuthUser actor) {
        return healthReviewService.listAllergyDeclarations(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Khai báo dị ứng", description = "UC-084 · BR-031 — SEVERE bắt buộc proofFileId")
    @ApiResponse(responseCode = "201", description = "MSG-036")
    @ApiResponse(responseCode = "404", description = "STU-404 — MSG-137 (C4: studentId phải tồn tại)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "STU-031 — MSG-112",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('PARENT')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AllergyDeclarationResponse submit(@Valid @RequestBody SubmitAllergyRequest request,
                                             @CurrentUser AuthUser actor) {
        return allergyDeclarationService.submit(request, actor);
    }

    @Operation(summary = "Nộp lại khai báo (NEEDS_INFO -> PENDING)", description = "UC-093 · BR-031")
    @PreAuthorize("hasRole('PARENT')")
    @PatchMapping("/{id}")
    public AllergyDeclarationResponse resubmit(@PathVariable("id") String code,
                                               @Valid @RequestBody SubmitAllergyRequest request,
                                               @CurrentUser AuthUser actor) {
        return allergyDeclarationService.resubmit(code, request, actor);
    }

    @Operation(summary = "Xác minh khai báo dị ứng",
            description = "UC-077 · BR-031 -> BR-123 — chỉ VERIFIED mới bật cảnh báo BR-123")
    @ApiResponse(responseCode = "200", description = "MSG-036")
    @ApiResponse(responseCode = "422", description = "STU-031 — MSG-112",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{id}/verify")
    public AllergyDeclarationResponse verify(@PathVariable("id") String code, @CurrentUser AuthUser actor) {
        return healthReviewService.verifyAllergy(code, actor);
    }

    @Operation(summary = "Yêu cầu bổ sung thông tin", description = "UC-077 · BR-032 · reason bắt buộc")
    @ApiResponse(responseCode = "422", description = "STU-032 — MSG-113",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{id}/request-info")
    public AllergyDeclarationResponse requestInfo(@PathVariable("id") String code,
                                                  @RequestBody RequestInfoRequest request,
                                                  @CurrentUser AuthUser actor) {
        return healthReviewService.requestAllergyInfo(code, request.reason(), actor);
    }
}
