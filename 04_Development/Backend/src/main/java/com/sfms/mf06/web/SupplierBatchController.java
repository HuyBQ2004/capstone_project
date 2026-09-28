package com.sfms.mf06.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.SupplierBatchDtos.CreateRequest;
import com.sfms.mf06.dto.SupplierBatchDtos.SupplierBatchResponse;
import com.sfms.mf06.dto.SupplierBatchDtos.UpdateRequest;
import com.sfms.mf06.service.SupplierBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** MF06 — {@code /api/v1/supplier-batches} (UC-081/043/100/107). BR-071. */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/supplier-batches")
public class SupplierBatchController {

    private final SupplierBatchService service;

    public SupplierBatchController(SupplierBatchService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách lô NCC", description = "UC-043 · UC-107")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping
    public PagedResponse<SupplierBatchResponse> list(@RequestParam Map<String, String> allParams,
                                                       @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết lô NCC", description = "UC-043 · UC-107")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping("/{code}")
    public SupplierBatchResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.detail(code, actor);
    }

    @Operation(summary = "Đăng ký lô nguyên liệu", description = "UC-081 · BR-071")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierBatchResponse create(@Valid @RequestBody CreateRequest request, @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Cập nhật lô nguyên liệu", description = "UC-043")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PutMapping("/{code}")
    public SupplierBatchResponse update(@PathVariable String code, @RequestBody UpdateRequest request,
                                         @CurrentUser AuthUser actor) {
        return service.update(code, request, actor);
    }

    @Operation(summary = "Đính kèm chứng chỉ lô", description = "UC-100 · D10-BIS — multipart qua backend")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping(value = "/{code}/certificates", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> attachCertificate(@PathVariable String code, @RequestParam("file") MultipartFile file,
                                                   @CurrentUser AuthUser actor) {
        String documentId = service.uploadCertificate(code, file, actor);
        return Map.of("documentId", documentId);
    }
}
