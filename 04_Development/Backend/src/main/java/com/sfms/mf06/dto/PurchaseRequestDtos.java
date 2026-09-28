package com.sfms.mf06.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** DTO của resource {@code purchase-requests} — UC-021/UC-139/UC-015 (CF-M06-5). */
public final class PurchaseRequestDtos {

    private PurchaseRequestDtos() {
    }

    public record ItemRequest(@NotBlank String ingredientId, @NotNull @Positive BigDecimal quantity,
                              @NotBlank String unit, String note) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PurchaseRequestCreateRequest")
    public record CreateRequest(String id, String supplierId, @NotNull LocalDate deliveryDate,
                                @NotNull @Valid List<ItemRequest> items) {}

    public record ItemResponse(String ingredientId, String ingredient, BigDecimal quantity,
                               @io.swagger.v3.oas.annotations.media.Schema(description = "PROPOSED — số lượng "
                                       + "trường thực cần trước khi quy đổi theo quy cách đóng gói; null nếu "
                                       + "chưa quy đổi (bằng quantity)")
                               BigDecimal requestedQuantity, String unit,
                               BigDecimal unitPrice, BigDecimal lineTotal, String note, String supplierBatchId) {}

    public record PurchaseRequestResponse(
            String id, String schoolId, String school, String supplierId, String supplier,
            LocalDate requestedDate, LocalDate deliveryDate, BigDecimal totalValue, String currency,
            String status, Instant quotedAt, Instant priceAcceptedAt, Instant confirmedDeliveryAt,
            String cancelReason, List<ItemResponse> items
    ) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponsePurchaseRequestResponse",
            description = "PagedResponse<PurchaseRequestResponse>")
    public record PagedResponsePurchaseRequestResponse(
            List<PurchaseRequestResponse> items, long total, int page, int pageSize, int pageCount) {}

    public record CancelRequest(@NotBlank String cancelReason) {}

    /**
     * BR-071 — gán lô hàng NCC cho từng dòng nguyên liệu lúc xác nhận lịch giao.
     * Key = mã nguyên liệu (`ItemResponse.ingredientId`), value = mã lô
     * (`SupplierBatchResponse.id`). Optional: đơn không có dòng chi tiết vẫn
     * confirm-schedule được.
     */
    public record ConfirmScheduleRequest(Map<String, String> batchAssignments) {}

    /**
     * SUPPLIER từ chối đơn — trước khi xác nhận lịch giao (PENDING, "không đủ nguồn
     * cung"). Dùng chung cột `cancel_reason`/`cancelled_at` với {@link CancelRequest} —
     * khác NGƯỜI gọi (SUPPLIER vs SERVICE_CATERERS), không khác ý nghĩa dữ liệu lưu xuống.
     */
    public record RejectRequest(@NotBlank String reason) {}
}
