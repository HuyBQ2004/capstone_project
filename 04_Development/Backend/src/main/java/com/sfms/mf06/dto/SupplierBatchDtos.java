package com.sfms.mf06.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** DTO của resource {@code supplier-batches} — BR-071 (UC-081/UC-043/UC-100/UC-107). */
public final class SupplierBatchDtos {

    private SupplierBatchDtos() {
    }

    @io.swagger.v3.oas.annotations.media.Schema(name = "SupplierBatchCreateRequest")
    public record CreateRequest(
            String id, @NotBlank String productId, @NotBlank String lotNumber,
            @NotNull LocalDate manufacturingDate, @NotNull LocalDate packagingDate,
            @NotNull LocalDate expirationDate, @NotNull @Positive BigDecimal quantity, @NotBlank String unit
    ) {}

    public record UpdateRequest(LocalDate expirationDate, BigDecimal quantity, String status) {}

    public record SupplierBatchResponse(
            String id, String productId, String ingredientId, String ingredient, String lotNumber,
            LocalDate manufacturingDate, LocalDate packagingDate, LocalDate expirationDate,
            BigDecimal quantity, String unit, String status, List<String> certificateIds,
            @io.swagger.v3.oas.annotations.media.Schema(description = "BR-071 (10/08/2026) — số lượng "
                    + "CÒN LẠI của lô, giảm dần khi được nhận nhiều đợt/nhiều trường")
            BigDecimal remainingQuantity
    ) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponseSupplierBatchResponse",
            description = "PagedResponse<SupplierBatchResponse>")
    public record PagedResponseSupplierBatchResponse(
            List<SupplierBatchResponse> items, long total, int page, int pageSize, int pageCount) {}
}
