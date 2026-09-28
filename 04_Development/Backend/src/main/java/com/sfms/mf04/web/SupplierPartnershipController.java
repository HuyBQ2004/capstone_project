package com.sfms.mf04.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf04.dto.PartnershipDtos.PartnershipResponse;
import com.sfms.mf04.service.SupplierPartnershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF04 — quan hệ hợp tác đang hiệu lực (UC-155/159). */
@Tag(name = "MF04 — Supplier & Partner Management")
@RestController
@RequestMapping("/api/v1/supplier-partnerships")
public class SupplierPartnershipController {

    private final SupplierPartnershipService partnershipService;

    public SupplierPartnershipController(SupplierPartnershipService partnershipService) {
        this.partnershipService = partnershipService;
    }

    @Operation(summary = "Danh sách quan hệ hợp tác đang hiệu lực", description = "UC-155/159 — "
            + "docStatus dẫn xuất runtime (ADR-M04-4)")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<PartnershipResponse> list(@RequestParam Map<String, String> allParams,
                                                   @CurrentUser AuthUser actor) {
        return partnershipService.list(ListQuery.from(allParams), actor);
    }
}
