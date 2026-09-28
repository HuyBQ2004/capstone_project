package com.sfms.mf03.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.dto.AllergyDeclarationDtos.RequestInfoRequest;
import com.sfms.mf03.dto.ChronicConditionDtos.ChronicConditionResponse;
import com.sfms.mf03.dto.ChronicConditionDtos.SubmitChronicConditionRequest;
import com.sfms.mf03.service.ChronicConditionService;
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

/**
 * MF03 — khai báo bệnh lý nền (UC-084/093/077/104/036 module 2). BR-031 gate hiệu lực. Cấu trúc
 * đối xứng {@link AllergyDeclarationController} — TDS §9.1 liệt kê {@code GET/POST
 * /api/v1/chronic-conditions} + {@code POST .../{id}/verify} · {@code /request-info}, trước bản
 * sửa này module chỉ có repository/entity, chưa có service/controller (§0-BIS việc tồn đọng).
 */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping(value = "/api/v1/chronic-conditions", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ChronicConditionController {

    private final ChronicConditionService chronicConditionService;
    private final HealthReviewService healthReviewService;

    public ChronicConditionController(ChronicConditionService chronicConditionService,
                                      HealthReviewService healthReviewService) {
        this.chronicConditionService = chronicConditionService;
        this.healthReviewService = healthReviewService;
    }

    @Operation(summary = "Danh sách khai báo bệnh lý nền", description = "UC-104/036 · BR-031")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','PARENT','TEACHER')")
    @GetMapping
    public PagedResponse<ChronicConditionResponse> list(@RequestParam Map<String, String> allParams,
                                                         @CurrentUser AuthUser actor) {
        return chronicConditionService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Khai báo bệnh lý nền", description = "UC-084 · BR-031 — SEVERE bắt buộc proofFileId")
    @ApiResponse(responseCode = "201", description = "MSG-036")
    @ApiResponse(responseCode = "404", description = "STU-404 — MSG-137 (C4: studentId phải tồn tại)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "STU-031 — MSG-112",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('PARENT')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChronicConditionResponse submit(@Valid @RequestBody SubmitChronicConditionRequest request,
                                           @CurrentUser AuthUser actor) {
        return chronicConditionService.submit(request, actor);
    }

    @Operation(summary = "Nộp lại khai báo (NEEDS_INFO -> PENDING)", description = "UC-093 · BR-031")
    @PreAuthorize("hasRole('PARENT')")
    @PatchMapping("/{id}")
    public ChronicConditionResponse resubmit(@PathVariable("id") String code,
                                             @Valid @RequestBody SubmitChronicConditionRequest request,
                                             @CurrentUser AuthUser actor) {
        return chronicConditionService.resubmit(code, request, actor);
    }

    @Operation(summary = "Xác minh khai báo bệnh lý nền", description = "UC-077 · BR-031")
    @ApiResponse(responseCode = "200", description = "MSG-036")
    @ApiResponse(responseCode = "422", description = "STU-031 — MSG-112",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{id}/verify")
    public ChronicConditionResponse verify(@PathVariable("id") String code, @CurrentUser AuthUser actor) {
        return healthReviewService.verifyChronic(code, actor);
    }

    @Operation(summary = "Yêu cầu bổ sung thông tin", description = "UC-077 · BR-032 · reason bắt buộc")
    @ApiResponse(responseCode = "422", description = "STU-032 — MSG-113",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{id}/request-info")
    public ChronicConditionResponse requestInfo(@PathVariable("id") String code,
                                                @RequestBody RequestInfoRequest request,
                                                @CurrentUser AuthUser actor) {
        return healthReviewService.requestChronicInfo(code, request.reason(), actor);
    }
}
