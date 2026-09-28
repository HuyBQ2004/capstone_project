package com.sfms.mf03.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.dto.SpecialMealRequestDtos.ApproveSpecialMealRequest;
import com.sfms.mf03.dto.SpecialMealRequestDtos.RejectSpecialMealRequest;
import com.sfms.mf03.dto.SpecialMealRequestDtos.SpecialMealRequestResponse;
import com.sfms.mf03.dto.SpecialMealRequestDtos.SubmitSpecialMealRequest;
import com.sfms.mf03.service.SpecialMealRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF03 — chế độ ăn đặc biệt (UC-078/084/036). BR-065/BR-220. */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping(value = "/api/v1/special-meals", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class SpecialMealRequestController {

    private final SpecialMealRequestService service;

    public SpecialMealRequestController(SpecialMealRequestService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách yêu cầu chế độ ăn", description = "UC-036 · BR-065")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','PARENT','TEACHER','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<SpecialMealRequestResponse> list(@RequestParam Map<String, String> allParams,
                                                           @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Gửi yêu cầu chế độ ăn đặc biệt", description = "UC-084 · BR-065")
    @PreAuthorize("hasRole('PARENT')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpecialMealRequestResponse submit(@Valid @RequestBody SubmitSpecialMealRequest request,
                                             @CurrentUser AuthUser actor) {
        return service.submit(request, actor);
    }

    @Operation(summary = "Duyệt yêu cầu chế độ ăn",
            description = "UC-078 · BR-065, BR-220 — ngoài danh mục chuẩn ⇒ 422 STU-066, "
                    + "chuyển NEEDS_INFO (không REJECTED)")
    @ApiResponse(responseCode = "422", description = "STU-066 — MSG-144",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{id}/approve")
    public SpecialMealRequestResponse approve(@PathVariable("id") String code,
                                              @RequestBody(required = false) ApproveSpecialMealRequest request,
                                              @CurrentUser AuthUser actor) {
        String note = request == null ? null : request.note();
        return service.approve(code, note, actor);
    }

    @Operation(summary = "Từ chối yêu cầu chế độ ăn", description = "UC-078 · BR-032 · reason bắt buộc")
    @ApiResponse(responseCode = "422", description = "STU-032 — MSG-113",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping("/{id}/reject")
    public SpecialMealRequestResponse reject(@PathVariable("id") String code,
                                             @Valid @RequestBody RejectSpecialMealRequest request,
                                             @CurrentUser AuthUser actor) {
        return service.reject(code, request.reason(), actor);
    }
}
