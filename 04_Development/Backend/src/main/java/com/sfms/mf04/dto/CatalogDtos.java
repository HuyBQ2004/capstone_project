package com.sfms.mf04.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO của {@code /partnerships/{code}/catalog-items} và {@code /catalog-change-requests}.
 * PROPOSED — xem {@code Proposal-Partnership-Catalog-Contract.md}, chưa có TDS §9 chính thức.
 */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    @Schema(name = "CatalogItemResponse")
    public record CatalogItemResponse(
            String id,
            String partnershipId,
            String schoolId,
            String ingredientId,
            String ingredientName,
            @Schema(description = "10/08/2026 — đơn vị CHUẨN thật của nguyên liệu (VD \"g\", \"ml\"), để "
                    + "hiện rõ ý nghĩa của conversionToBase thay vì nhãn mù \"đơn vị chuẩn\"")
            String ingredientUnit,
            String unit,
            @Schema(description = "PROPOSED — bao nhiêu đơn vị CHUẨN (ingredientUnit) = 1 unit ở trên")
            BigDecimal conversionToBase,
            String conversionNote,
            BigDecimal approvedUnitPrice,
            String packaging,
            @Schema(description = "Số lượng/1 kiện theo unit — null = không quy đổi") BigDecimal packagingSize,
            @Schema(description = "false = chỉ được đặt bội số packagingSize (nguyên kiện)")
            boolean allowPartialPackage,
            @Schema(type = "string", format = "date") LocalDate effectiveFrom,
            @Schema(type = "string", format = "date") LocalDate effectiveTo,
            @Schema(description = "ACTIVE/INACTIVE") String status
    ) {}

    public record CreateChangeRequest(
            @Schema(description = "D5 — client tự chọn mã, để trống thì server sinh") String id,
            @Schema(description = "Để trống nếu đề xuất sản phẩm mới") String catalogItemId,
            @NotBlank String ingredientId,
            @NotBlank String requestedUnit,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal requestedUnitPrice,
            @Schema(description = "PROPOSED — bao nhiêu đơn vị chuẩn của nguyên liệu = 1 requestedUnit. "
                    + "BẮT BUỘC nếu requestedUnit khác đơn vị chuẩn của nguyên liệu")
            BigDecimal requestedConversionToBase,
            String requestedConversionNote,
            String requestedPackaging,
            BigDecimal requestedPackagingSize,
            Boolean requestedAllowPartialPackage,
            @NotBlank String reason
    ) {}

    @Schema(name = "ChangeRequestResponse")
    public record ChangeRequestResponse(
            String id,
            String partnershipId,
            @Schema(description = "10/08/2026 — trường có thể hợp tác NHIỀU nhà cung cấp; thiếu field "
                    + "này thì màn duyệt (school-admin) không phân biệt được đề xuất của ai")
            String partnerId,
            String partner,
            String schoolId,
            String catalogItemId,
            String ingredientId,
            String ingredientName,
            @Schema(description = "10/08/2026 — đơn vị CHUẨN thật của nguyên liệu, cùng lý do như "
                    + "CatalogItemResponse.ingredientUnit")
            String ingredientUnit,
            String requestedUnit,
            BigDecimal requestedUnitPrice,
            BigDecimal requestedConversionToBase,
            String requestedConversionNote,
            String requestedPackaging,
            BigDecimal requestedPackagingSize,
            boolean requestedAllowPartialPackage,
            String reason,
            @Schema(description = "PENDING/APPROVED/REJECTED") String status,
            Instant decidedAt,
            String decisionNote
    ) {}

    public record DecisionRequest(
            @NotNull Boolean approve,
            String note
    ) {}
}
