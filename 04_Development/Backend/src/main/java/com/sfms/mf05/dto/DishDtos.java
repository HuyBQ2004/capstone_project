package com.sfms.mf05.dto;

import com.sfms.mf05.dto.MenuDtos.MenuIngredient;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/** DTO của resource {@code dishes} (thư viện món) — TDS-MF05_Backend.md §9.3.6. */
public final class DishDtos {

    private DishDtos() {
    }

    public record CreateDishRequest(
            @Schema(description = "D5 — slug do client sinh (dishIdOfName())", example = "ca-basa-sot-ca-chua")
            String id,
            @NotBlank String name,
            @NotNull @Schema(description = "MON_CHINH|MON_MAN|CANH|RAU|BUA_PHU|TRANG_MIENG") String category,
            @NotNull BigDecimal kcal,
            @NotNull BigDecimal protein,
            @NotNull BigDecimal fat,
            @NotNull BigDecimal carb,
            List<String> manualAllergens,
            Boolean isActive,
            String defaultSubstituteId,
            List<MenuIngredient> ingredients,
            @Schema(description = "Mô tả món ăn (nguyên liệu chính, hương vị...) — không bắt buộc")
            String description,
            @Schema(description = "Hướng dẫn chế biến — không bắt buộc") String cookingInstructions
    ) {}

    @Schema(description = "PUT /dishes/{id} — merge một phần; dùng riêng {isActive} để ngừng/dùng lại món")
    public record UpdateDishRequestDoc(
            String name, String category, BigDecimal kcal, BigDecimal protein, BigDecimal fat, BigDecimal carb,
            List<String> manualAllergens, String defaultSubstituteId,
            List<MenuIngredient> ingredients, Boolean isActive, String description, String cookingInstructions
    ) {}

    @Schema(name = "DishResponse")
    public record DishResponse(
            String id, String name, String category,
            BigDecimal kcal, BigDecimal protein, BigDecimal fat, BigDecimal carb,
            List<String> manualAllergens, boolean isActive, String defaultSubstituteId,
            List<MenuIngredient> ingredients,
            @Schema(description = "URL công khai ảnh minh hoạ món — null nếu chưa upload") String imageUrl,
            String description, String cookingInstructions
    ) {}

    public record InUseWarning(String code, String message, List<String> menuIds) {}

    public record UpdateDishResult(DishResponse dish, List<InUseWarning> warnings) {}

    @Schema(name = "PagedResponseDishResponse", description = "PagedResponse<DishResponse>")
    public record PagedResponseDishResponse(List<DishResponse> items, long total, int page, int pageSize, int pageCount) {}
}
