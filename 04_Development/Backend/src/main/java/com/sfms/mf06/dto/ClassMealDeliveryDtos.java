package com.sfms.mf06.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO của {@code class_meal_deliveries}. 04_Meal_Substitution_Portion_Pipeline_Design.md
 * Phase 2 mục (1)+(4). KHÔNG có breakdown món/suất trong response — gọi riêng
 * {@code GET /student-distributions/planned-dishes?date=&class=&mealType=} (MF09, đã có) để
 * lấy đúng số suất từng món (kể cả món thay thế) cho lớp đó.
 */
public final class ClassMealDeliveryDtos {

    private ClassMealDeliveryDtos() {
    }

    @Schema(description = "POST /class-meal-deliveries (Service Caterers) — bếp xác nhận ĐÃ giao tới lớp")
    public record CreateDeliveryRequest(
            @NotNull LocalDate date,
            @NotBlank @Schema(description = "BREAKFAST|LUNCH|SNACK") String mealType,
            @NotBlank @Schema(description = "D1 — mã lớp") String classroomId,
            String note) {}

    @Schema(name = "ClassMealDeliveryResponse")
    public record ClassMealDeliveryResponse(
            @Schema(description = "D1 — mã nghiệp vụ") String id,
            LocalDate date,
            String mealType,
            @Schema(description = "D1 — mã lớp (khớp className của student-distributions)") String classroomId,
            @Schema(description = "DELIVERED | RECEIVED") String status,
            @Schema(description = "Dẫn xuất — tên người giao") String deliveredBy,
            Instant deliveredAt,
            @Schema(description = "Dẫn xuất — tên người nhận, null nếu chưa nhận") String receivedBy,
            Instant receivedAt,
            String note) {}
}
