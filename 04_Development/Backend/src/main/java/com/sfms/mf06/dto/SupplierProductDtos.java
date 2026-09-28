package com.sfms.mf06.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** DTO của resource {@code supplier-products} (M06-D03 — UC-085, giá NCC báo cho trường). */
public final class SupplierProductDtos {

    private SupplierProductDtos() {
    }

    @io.swagger.v3.oas.annotations.media.Schema(name = "SupplierProductCreateRequest")
    public record CreateRequest(
            String id, @NotBlank String ingredientId, String label, @NotBlank String unit,
            @NotNull @Positive BigDecimal unitPrice, String packaging, @NotNull LocalDate effectiveFrom,
            @io.swagger.v3.oas.annotations.media.Schema(description = "PROPOSED — bao nhiêu đơn vị chuẩn của "
                    + "nguyên liệu = 1 `unit` ở trên. BẮT BUỘC nếu `unit` khác đơn vị chuẩn của nguyên liệu; "
                    + "bỏ trống/1 chỉ hợp lệ khi hai đơn vị trùng nhau")
            BigDecimal conversionToBase,
            @io.swagger.v3.oas.annotations.media.Schema(description = "PROPOSED — ghi chú cách quy đổi, VD "
                    + "\"trung bình, có thể lệch theo mùa vụ\"")
            String conversionNote
    ) {}

    public record UpdateRequest(
            String label, String unit, @Positive BigDecimal unitPrice, String packaging, LocalDate effectiveFrom,
            BigDecimal conversionToBase, String conversionNote
    ) {}

    public record SupplierProductResponse(
            String id, String supplierId, String ingredientId, String label, String unit,
            BigDecimal unitPrice, String packaging, LocalDate effectiveFrom,
            @io.swagger.v3.oas.annotations.media.Schema(description = "PROPOSED — bao nhiêu đơn vị chuẩn của "
                    + "nguyên liệu = 1 unit ở trên")
            BigDecimal conversionToBase,
            String conversionNote
    ) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponseSupplierProductResponse",
            description = "PagedResponse<SupplierProductResponse>")
    public record PagedResponseSupplierProductResponse(
            List<SupplierProductResponse> items, long total, int page, int pageSize, int pageCount) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "IngredientSourcingResponse",
            description = "Thêm 08/2026 (BR-076, panel 'Nhà cung cấp nguyên liệu' màn Phụ huynh) — CHỈ trả tên "
                    + "NCC (giá rẻ nhất, đối tác còn hợp tác), KHÔNG có unitPrice/field thương mại nào khác. "
                    + "Endpoint riêng `/supplier-products/sourcing`, KHÔNG dùng list() gốc — list() có "
                    + "unitPrice, không nên cấp quyền PARENT vào đó.")
    public record IngredientSourcingResponse(
            @io.swagger.v3.oas.annotations.media.Schema(description = "D1 — mã nguyên liệu, khớp với "
                    + "Dish.ingredients[].ingredientId") String ingredientId,
            @io.swagger.v3.oas.annotations.media.Schema(description = "null = chưa có NCC nào còn hợp tác bán "
                    + "nguyên liệu này") String supplierName
    ) {}
}
