package com.sfms.mf06.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.InventoryDtos.InventoryTransactionResponse;
import com.sfms.mf06.dto.InventoryDtos.ManualAdjustmentRequest;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.service.InventoryTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MF06 — {@code /api/v1/inventory-transactions} (UC-064). Sổ APPEND-ONLY (ADR-M06-1/C3):
 * KHÔNG PUT/DELETE — cả hai trả {@code 405 ING-405} tường minh thay vì để Spring 405 mặc định
 * (giữ envelope D2 nhất quán).
 */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/inventory-transactions")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    public InventoryTransactionController(InventoryTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Sổ giao dịch kho", description = "UC-064 · ADR-M06-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<InventoryTransactionResponse> list(@RequestParam Map<String, String> allParams,
                                                              @CurrentUser AuthUser actor) {
        return transactionService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Điều chỉnh tồn thủ công", description = "UC-064 · BR-204")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryTransactionResponse adjust(@Valid @RequestBody ManualAdjustmentRequest request,
                                                @CurrentUser AuthUser actor) {
        return transactionService.adjust(request, actor);
    }

    @Operation(summary = "KHÔNG hỗ trợ — sổ append-only", description = "ADR-M06-1 · C3")
    @PutMapping("/{id}")
    public InventoryTransactionResponse rejectPut(@PathVariable String id) {
        throw IngExceptions.appendOnly();
    }

    @Operation(summary = "KHÔNG hỗ trợ — sổ append-only", description = "ADR-M06-1 · C3")
    @DeleteMapping("/{id}")
    public void rejectDelete(@PathVariable String id) {
        throw IngExceptions.appendOnly();
    }
}
