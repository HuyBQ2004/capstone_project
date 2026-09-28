package com.sfms.mf05.dto;

import com.sfms.common.enums.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** DTO của resource {@code menu-templates} — TDS-MF05_Backend.md §9.3.7. */
public final class MenuTemplateDtos {

    private MenuTemplateDtos() {
    }

    public record CreateMenuTemplateRequest(
            @Schema(description = "D5 — TPL- + slug tên mẫu (client sinh)") String id,
            @NotBlank String name,
            @NotNull MealType mealType,
            String audience,
            @Schema(description = "NULL/rỗng = dùng chung mọi khối; UUID của Grade (GET /api/v1/grades) "
                    + "= chỉ khối đó (nhu cầu dinh dưỡng khác nhau theo khối)") String gradeId,
            @Schema(description = "Có thứ tự = thứ tự dọn món") List<String> dishIds,
            String note,
            Boolean isActive
    ) {}

    @Schema(description = "PUT /menu-templates/{id} — merge một phần; {isActive:false} = ngừng dùng")
    public record UpdateMenuTemplateRequestDoc(
            String name, MealType mealType, String audience, String gradeId, List<String> dishIds, String note,
            Boolean isActive
    ) {}

    @Schema(name = "MenuTemplateResponse")
    public record MenuTemplateResponse(
            String id, String name, String mealType, String audience,
            @Schema(description = "V44 — nhãn hiển thị (Grade.name), null = dùng chung mọi khối "
                    + "(D1 — KHÔNG lộ gradeId UUID nội bộ)") String grade,
            List<String> dishIds, String note, boolean isActive
    ) {}

    @Schema(name = "PagedResponseMenuTemplateResponse", description = "PagedResponse<MenuTemplateResponse>")
    public record PagedResponseMenuTemplateResponse(
            List<MenuTemplateResponse> items, long total, int page, int pageSize, int pageCount) {}
}
