package com.sfms.mf09.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.enums.ConsumptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** DTO của {@code meal_consumption_records} (resource mock: "meal-records"). TDS-MF09 §9.2. */
public final class MealRecordDtos {

    private MealRecordDtos() {
    }

    // ───────────────────────── Requests ─────────────────────────

    /**
     * PATCH /meal-records/{code} (UC-080, D12) — <b>chỉ khai field sửa được</b>. Field dẫn xuất
     * ({@code dishes}/{@code assignedMeal}/{@code specialMeal}...) KHÔNG xuất hiện ở đây ⇒
     * Jackson bỏ qua nếu client gửi thêm — KHÔNG merge tay bằng {@code Map<String,Object>}
     * (đó chính là {@code PUT} generic mà CF-M09-9 đã bỏ).
     */
    @Schema(description = "PATCH /meal-records/{code}")
    public record UpdateConsumptionRequest(
            @NotNull ConsumptionStatus consumption,
            String specialMealServed) {}

    @Schema(description = "POST /meal-records/batch (UC-080) — GVCN ghi 1 lượt cho cả lớp")
    public record BatchConsumptionRequest(@NotEmpty List<BatchItem> items) {
        public record BatchItem(
                @Schema(description = "D1 — mã meal_consumption_records") String id,
                @NotNull ConsumptionStatus consumption,
                String specialMealServed) {}
    }

    // ───────────────────────── Response ─────────────────────────

    @Schema(name = "MealRecordResponse")
    public record MealRecordResponse(
            String id,
            LocalDate date,
            String mealType,
            @Schema(description = "D1 — mã học sinh, dùng để lọc theo HS ở FE") String studentId,
            @Schema(description = "Snapshot tên HS lúc ghi — JSON \"student\" (mock)") @JsonProperty("student") String studentName,
            @JsonProperty("class") String clazz,
            @Schema(description = "Snapshot CHUỖI nhãn món — KHÔNG phải mảng (D12, MF05 TC-027 cùng bẫy)")
            String dishes,
            String specialDiet,
            String specialMealServed,
            String consumption,
            Instant recordedAt) {}

    @Schema(name = "PagedResponseMealRecordResponse", description = "PagedResponse<MealRecordResponse>")
    public record PagedResponseMealRecordResponse(
            List<MealRecordResponse> items, long total, int page, int pageSize, int pageCount) {}
}
