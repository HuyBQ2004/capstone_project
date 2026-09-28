package com.sfms.mf09.dto;

import com.sfms.mf09.dto.DistributionDtos.ServingDishView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO của {@code GET /students/{code}/meal-history} (UC-133/053, M09-D07). Projection dẫn xuất
 * từ {@code student_distributions ⋈ serving_dishes ⋈ meal_consumption_records} (CF-M09-7,
 * ADR-M09-2) — KHÔNG bảng {@code meal_history} riêng.
 */
public final class MealHistoryDtos {

    private MealHistoryDtos() {
    }

    @Schema(name = "MealHistoryEntryResponse")
    public record MealHistoryEntryResponse(
            @Schema(description = "D1 — mã phiên phát suất") String id,
            LocalDate date,
            String mealType,
            String status,
            String catererName,
            List<ServingDishView> dishes,
            String consumption) {}

    @Schema(name = "PagedResponseMealHistoryEntryResponse", description = "PagedResponse<MealHistoryEntryResponse>")
    public record PagedResponseMealHistoryEntryResponse(
            List<MealHistoryEntryResponse> items, long total, int page, int pageSize, int pageCount) {}

    /**
     * DTO của {@code GET /students/{code}/nutrition-history} (BR-062) — projection dẫn xuất từ
     * {@code student_distributions ⋈ serving_dishes ⋈ dishes}, CHỈ tính bữa {@code LUNCH}
     * (khớp {@code NUTRITION_TARGET} — chuẩn năng lượng bữa trưa, không phải cả ngày).
     * KHÔNG bảng {@code nutrition_history} riêng, cùng tinh thần ADR-M09-2.
     */
    @Schema(name = "NutritionDayResponse")
    public record NutritionDayResponse(
            LocalDate date, BigDecimal kcal, BigDecimal protein, BigDecimal fat, BigDecimal carb) {}

    /**
     * BR-062 — chuẩn năng lượng bữa trưa nhóm tuổi 6–11 (tiểu học). Hằng số CỐ ĐỊNH vì backend
     * chưa có bảng chuẩn dinh dưỡng theo nhóm tuổi nào khác (grep không thấy) — nếu sau này cần
     * phân theo mầm non/THCS/THPT, đây là chỗ mở rộng.
     */
    @Schema(name = "NutritionTargetResponse")
    public record NutritionTargetResponse(
            BigDecimal kcal, BigDecimal protein, BigDecimal fat, BigDecimal carb) {}

    @Schema(name = "NutritionHistoryResponse")
    public record NutritionHistoryResponse(List<NutritionDayResponse> days, NutritionTargetResponse target) {}
}
