package com.sfms.mf07.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf07.dto.SafetySignoffDtos.PagedResponseSignoffResponse;
import com.sfms.mf07.dto.SafetySignoffDtos.SignoffRequest;
import com.sfms.mf07.dto.SafetySignoffDtos.SignoffResponse;
import com.sfms.mf07.service.SafetySignoffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF07 — {@code safety-signoffs} (UC-027 — BGH ký xác nhận an toàn hằng ngày). */
@Tag(name = "MF07 — Food Safety Inspection & Monitoring")
@RestController
@RequestMapping("/api/v1/safety-signoffs")
public class SafetySignoffController {

    private final SafetySignoffService safetySignoffService;

    public SafetySignoffController(SafetySignoffService safetySignoffService) {
        this.safetySignoffService = safetySignoffService;
    }

    @Operation(summary = "Danh sách ký xác nhận an toàn", description = "UC-027")
    @ApiResponse(responseCode = "200", description = "PagedResponse<SignoffResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseSignoffResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @GetMapping
    public PagedResponse<SignoffResponse> list(@RequestParam Map<String, String> allParams,
                                                @CurrentUser AuthUser actor) {
        return safetySignoffService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Ký xác nhận an toàn hằng ngày", description = "UC-027 bước 5")
    @ApiResponse(responseCode = "201", description = "OK")
    @ApiResponse(responseCode = "409", description = "INSP-409 — đã ký ngày này",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignoffResponse signoff(@RequestBody(required = false) SignoffRequest request, @CurrentUser AuthUser actor) {
        return safetySignoffService.signoff(request == null ? new SignoffRequest(null) : request, actor);
    }
}
