package com.sfms.mf08.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf08.dto.ReceivingAuthorityDtos.PagedResponseReceivingAuthorityResponse;
import com.sfms.mf08.dto.ReceivingAuthorityDtos.ReceivingAuthorityResponse;
import com.sfms.mf08.service.ReceivingAuthorityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF08 — {@code receiving-authorities} (danh mục nơi tiếp nhận mẫu, MF08 sở hữu). */
@Tag(name = "MF08 — Food Sample Retention")
@RestController
@RequestMapping("/api/v1/receiving-authorities")
public class ReceivingAuthorityController {

    private final ReceivingAuthorityService service;

    public ReceivingAuthorityController(ReceivingAuthorityService service) {
        this.service = service;
    }

    @Operation(summary = "Danh mục nơi tiếp nhận mẫu", description = "FE lọc isActive=true")
    @ApiResponse(responseCode = "200", description = "PagedResponse<ReceivingAuthorityResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseReceivingAuthorityResponse.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<ReceivingAuthorityResponse> list(@RequestParam Map<String, String> allParams) {
        return service.list(ListQuery.from(allParams));
    }
}
