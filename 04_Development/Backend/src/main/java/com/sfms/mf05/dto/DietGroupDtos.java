package com.sfms.mf05.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO của {@code /api/v1/diet-groups} (BR-065, ADR-M05-01) — danh mục nhóm chế độ ăn đặc biệt
 * TOÀN HỆ THỐNG, do đội vận hành/y tế cấu hình trong DB (bảng {@code diet_groups}), KHÔNG phải
 * do frontend tự khai. Thêm 06/08/2026: trước đây FE dùng mảng hằng {@code DIET_GROUPS} khai
 * tay trong {@code mock-data.ts} cho mọi màn chọn nhóm chế độ ăn — không có endpoint nào trả
 * đúng danh mục + giới hạn (`excludeAllergens`/`excludeIngredientKeywords`) thật, nên các cảnh
 * báo "món vướng chế độ ăn X" trên FE dựa vào dữ liệu KHÔNG do Y tế duyệt.
 */
public final class DietGroupDtos {

    private DietGroupDtos() {
    }

    @Schema(name = "DietGroupResponse")
    public record DietGroupResponse(
            @Schema(description = "D1 — mã nhóm, VD 'VEGETARIAN'") String id,
            String label,
            @Schema(description = "Khớp special_meal_requests.dietary_type (case-insensitive, BR-220)")
            String matchDiet,
            List<String> excludeAllergens,
            List<String> excludeIngredientKeywords,
            String note
    ) {}
}
