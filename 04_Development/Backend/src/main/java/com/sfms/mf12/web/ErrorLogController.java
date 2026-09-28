package com.sfms.mf12.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf12.dto.ErrorLogDtos.ErrorLogDetail;
import com.sfms.mf12.dto.ErrorLogDtos.ErrorLogSummary;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.service.ErrorLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF12 — {@code error-logs} (UC-111). CF-18: list trả đủ {@code stackTrace}/{@code sessionInfo}. */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/error-logs")
public class ErrorLogController {

    private final ErrorLogQueryService errorLogQueryService;

    public ErrorLogController(ErrorLogQueryService errorLogQueryService) {
        this.errorLogQueryService = errorLogQueryService;
    }

    @Operation(summary = "Danh sách nhật ký lỗi", description = "UC-111")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping
    public PagedResponse<ErrorLogSummary> list(@RequestParam Map<String, String> allParams) {
        return errorLogQueryService.list(ListQuery.from(allParams));
    }

    @Operation(summary = "Chi tiết một dòng lỗi", description = "UC-111 bước 5")
    @ApiResponse(responseCode = "404", description = "AIS-404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/{code}")
    public ErrorLogDetail detail(@PathVariable String code) {
        return errorLogQueryService.detail(code).orElseThrow(AisExceptions::notFound);
    }
}
