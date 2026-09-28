package com.sfms.mf06.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.PurchaseRequestDtos.CancelRequest;
import com.sfms.mf06.dto.PurchaseRequestDtos.ConfirmScheduleRequest;
import com.sfms.mf06.dto.PurchaseRequestDtos.CreateRequest;
import com.sfms.mf06.dto.PurchaseRequestDtos.PurchaseRequestResponse;
import com.sfms.mf06.dto.PurchaseRequestDtos.RejectRequest;
import com.sfms.mf06.service.PurchaseRequestService;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * MF06 — {@code /api/v1/purchase-requests} (UC-021/139/015). CF-M06-5: giá theo hợp đồng,
 * không đàm phán (10/08/2026) — bỏ bước NCC báo giá / trường chấp nhận giá; NCC xác nhận
 * lịch giao thẳng từ PENDING.
 */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestService service;

    public PurchaseRequestController(PurchaseRequestService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách đơn mua", description = "UC-139")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping
    public PagedResponse<PurchaseRequestResponse> list(@RequestParam Map<String, String> allParams,
                                                         @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết đơn mua", description = "UC-139")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping("/{code}")
    public PurchaseRequestResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.detail(code, actor);
    }

    @Operation(summary = "Tạo đề nghị mua hàng", description = "UC-021 · BR-077")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseRequestResponse create(@Valid @RequestBody CreateRequest request, @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "NCC xác nhận lịch giao", description = "UC-015 · BR-071 — gán lô hàng theo nguyên liệu; " +
            "giá theo hợp đồng đã tự tính lúc tạo đơn, không còn bước báo giá/chấp nhận giá riêng")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping("/{code}/confirm-schedule")
    public PurchaseRequestResponse confirmSchedule(@PathVariable String code,
                                                     @RequestBody(required = false) ConfirmScheduleRequest request,
                                                     @CurrentUser AuthUser actor) {
        return service.confirmSchedule(code, request, actor);
    }

    @Operation(summary = "Trường huỷ đơn mua", description = "CF-M06-5 — bắt buộc cancelReason")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/cancel")
    public PurchaseRequestResponse cancel(@PathVariable String code, @Valid @RequestBody CancelRequest request,
                                           @CurrentUser AuthUser actor) {
        return service.cancel(code, request, actor);
    }

    @Operation(summary = "NCC từ chối đơn mua", description = "CF-M06-5 — PENDING, bắt buộc reason")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping("/{code}/reject")
    public PurchaseRequestResponse reject(@PathVariable String code, @Valid @RequestBody RejectRequest request,
                                           @CurrentUser AuthUser actor) {
        return service.reject(code, request, actor);
    }
}
