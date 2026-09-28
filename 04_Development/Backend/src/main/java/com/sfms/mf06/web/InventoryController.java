package com.sfms.mf06.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.InventoryDtos.InventoryBatchResponse;
import com.sfms.mf06.dto.InventoryDtos.IssuePlanResponse;
import com.sfms.mf06.dto.InventoryDtos.IssueSlipCreateRequest;
import com.sfms.mf06.dto.InventoryDtos.IssueSlipResponse;
import com.sfms.mf06.dto.InventoryDtos.QuarantineRequest;
import com.sfms.mf06.dto.InventoryDtos.StorageZoneUpdateRequest;
import com.sfms.mf06.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** MF06 — {@code /api/v1/inventory} (UC-045/061/064). TDS-MF06_Backend.md §9. */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Danh sách lô trong kho", description = "UC-045 · BR-075/077/205")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<InventoryBatchResponse> list(@RequestParam Map<String, String> allParams,
                                                        @CurrentUser AuthUser actor) {
        return inventoryService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Kế hoạch xuất kho FEFO", description = "UC-045 · BR-075/077, ADR-M06-4")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @GetMapping("/issue-plan")
    public IssuePlanResponse issuePlan(@RequestParam LocalDate serviceDate, @RequestParam String mealType,
                                        @RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        Map<String, BigDecimal> demand = new LinkedHashMap<>();
        allParams.forEach((k, v) -> {
            if (k.startsWith("demand[") && k.endsWith("]")) {
                demand.put(k.substring(7, k.length() - 1), new BigDecimal(v));
            }
        });
        return inventoryService.issuePlan(serviceDate, MealType.valueOf(mealType), demand, actor);
    }

    @Operation(summary = "Danh sách phiếu xuất", description = "ADR-M06-6")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/issue-slips")
    public PagedResponse<IssueSlipResponse> listIssueSlips(@RequestParam Map<String, String> allParams,
                                                             @CurrentUser AuthUser actor) {
        return inventoryService.listIssueSlips(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Lập phiếu xuất kho", description = "UC-045 · BR-076/077/205/149, ADR-M06-6")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/issue-slips")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueSlipResponse createIssueSlip(@Valid @RequestBody IssueSlipCreateRequest request,
                                              @CurrentUser AuthUser actor) {
        return inventoryService.createIssueSlip(request, actor);
    }

    @Operation(summary = "Chi tiết lô trong kho", description = "UC-045")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/{code}")
    public InventoryBatchResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return inventoryService.detail(code, actor);
    }

    @Operation(summary = "Cập nhật khu vực lưu trữ", description = "UC-061 · BR-202")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PatchMapping("/{code}/storage-zone")
    public InventoryBatchResponse updateStorageZone(@PathVariable String code,
                                                      @Valid @RequestBody StorageZoneUpdateRequest request,
                                                      @CurrentUser AuthUser actor) {
        return inventoryService.updateStorageZone(code, request.storageZone(), actor);
    }

    @Operation(summary = "Cách ly lô", description = "BR-149 — CHỈ Medical Staff")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PatchMapping("/{code}/quarantine")
    public InventoryBatchResponse quarantine(@PathVariable String code, @Valid @RequestBody QuarantineRequest request,
                                              @CurrentUser AuthUser actor) {
        return inventoryService.quarantine(code, request, actor);
    }

    @Operation(summary = "Gỡ cách ly lô", description = "BR-149/ADR-M06-2 — CHỈ Medical Staff, endpoint RIÊNG "
            + "(không PUT mang quarantine:null)")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @DeleteMapping("/{code}/quarantine")
    public InventoryBatchResponse unquarantine(@PathVariable String code, @CurrentUser AuthUser actor) {
        return inventoryService.unquarantine(code, actor);
    }
}
