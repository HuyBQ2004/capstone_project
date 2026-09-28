package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.dto.MealHistoryDtos.MealHistoryEntryResponse;
import com.sfms.mf09.dto.MealHistoryDtos.NutritionHistoryResponse;

import java.time.LocalDate;

/**
 * {@code GET /students/{code}/meal-history} (UC-133/053, M09-D07) — projection dẫn xuất từ
 * {@code student_distributions ⋈ serving_dishes ⋈ meal_consumption_records} (CF-M09-7,
 * ADR-M09-2). KHÔNG bảng {@code meal_history} riêng.
 */
public interface MealHistoryService {

    PagedResponse<MealHistoryEntryResponse> list(String studentCode, ListQuery query, AuthUser actor);

    MealHistoryEntryResponse detail(String studentCode, String distributionCode, AuthUser actor);

    /** UC-142 — lọc {@code isSpecial=yes} trong lịch sử (CF-M09-7). */
    PagedResponse<MealHistoryEntryResponse> specialMealRecords(String studentCode, ListQuery query, AuthUser actor);

    /** BR-062 — kcal/protein/fat/carb bữa trưa theo ngày, dẫn xuất từ dishes thật. */
    NutritionHistoryResponse nutritionHistory(String studentCode, LocalDate from, LocalDate to, AuthUser actor);
}
