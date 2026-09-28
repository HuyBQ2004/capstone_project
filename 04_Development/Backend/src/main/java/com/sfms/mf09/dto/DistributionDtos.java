package com.sfms.mf09.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** DTO của {@code student_distributions}/{@code serving_dishes}. TDS-MF09_Backend.md §9.2. */
public final class DistributionDtos {

    private DistributionDtos() {
    }

    // ───────────────────────── Requests ─────────────────────────

    @Schema(description = "POST /student-distributions/confirm (UC-016) — 1 transaction, N học sinh")
    public record ConfirmBatchRequest(
            @NotEmpty @Schema(description = "D1 — mã (code) các phiên student_distributions cần xác nhận PENDING->SERVED")
            List<String> distributionIds) {}

    @Schema(description = "POST /student-distributions/{code}/error-report (UC-016 AF1)")
    public record ErrorReportRequest(@NotBlank String reason) {}

    // ───────────────────────── Response ─────────────────────────

    @Schema(name = "ServingDishView")
    public record ServingDishView(
            String dishId,               // D1 — code của dishes
            String dishName,
            boolean isSubstitute,
            String substitutedForDishName,
            String reason) {}

    /**
     * Shape ĐẦY ĐỦ — Teacher/School Admin/Medical (đọc PII: {@code name}, {@code allergyWarning},
     * {@code specialDiet}). C8: giữ nguyên tên field mock ({@code name}, {@code class}); ngày/giờ
     * ISO-8601 (D4/M09-D04, ghi đè ADR-M09-5).
     */
    @Schema(name = "DistributionResponse")
    public record DistributionResponse(
            @Schema(description = "D1 — mã nghiệp vụ") String id,
            LocalDate date,
            @JsonProperty("class") @Schema(description = "D1 — code lớp học") String clazz,
            String mealType,
            @Schema(description = "D1 — code học sinh") String studentId,
            String name,
            String status,
            Instant servedAt,
            String catererName,
            @Schema(description = "Field dẫn xuất (ADR-M09-2) — allergen VERIFIED giao với món trong phiên")
            List<String> allergyWarning,
            @Schema(description = "Field dẫn xuất (ADR-M09-2, BR-066)") String specialDiet,
            @Schema(description = "yes|no — field dẫn xuất") String isSpecial,
            @Schema(description = "ADR-M09-1 — sự kiện đóng băng, KHÔNG dẫn xuất") List<ServingDishView> servedDishes
    ) {}

    /**
     * Shape RÚT GỌN — Service Caterers (D11, ADR-M09-6): số suất phải nấu, KHÔNG thấy tên/PII
     * dị ứng của trẻ. Đúng ranh giới §6 workflow: "lọc dòng không cần oneOf, lọc field thì cần".
     */
    @Schema(name = "DistributionCatererResponse", description = "D11 — rút gọn cho Service Caterers, KHÔNG PII trẻ em")
    public record DistributionCatererResponse(
            String id,
            LocalDate date,
            @JsonProperty("class") String clazz,
            String mealType,
            String status,
            @Schema(description = "yes|no — field dẫn xuất") String isSpecial
    ) {}

    /**
     * Bản sao CHỈ-ĐỂ-SINH-TÀI-LIỆU của {@code PagedResponse<DistributionResponse>} và
     * {@code PagedResponse<DistributionCatererResponse>} — D11 (mẫu {@code StudentDtos}, MF03).
     * Tên schema giữ tiền tố {@code PagedResponse} (S7).
     */
    @Schema(name = "PagedResponseDistributionResponse", description = "PagedResponse<DistributionResponse>")
    public record PagedResponseDistributionResponse(
            List<DistributionResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "PagedResponseDistributionCatererResponse",
            description = "PagedResponse<DistributionCatererResponse> — chỉ Service Caterers")
    public record PagedResponseDistributionCatererResponse(
            List<DistributionCatererResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "DistributionSummaryResponse", description = "GET /student-distributions/summary (UC-132)")
    public record DistributionSummaryResponse(
            @JsonProperty("class") String clazz,
            int studentCount,
            int specialMealCount,
            @Schema(description = "PENDING | IN_PROGRESS | COMPLETED — tính runtime") String status) {}

    /**
     * GET /student-distributions/planned-dishes (04_Meal_Substitution_Portion_Pipeline_Design.md
     * Phase 1) — suất DỰ KIẾN của học sinh còn PENDING, dùng LẠI đúng logic thay món của
     * {@code doServe()} (KHÔNG viết lại luật ở nơi khác), nhưng KHÔNG ghi gì — an toàn gọi lại
     * nhiều lần. Khác {@code servedDishes} (ADR-M09-1, sự kiện đã đóng băng sau khi phát).
     */
    @Schema(name = "PlannedDishRow", description = "Suất dự kiến của 1 học sinh (chưa phát)")
    public record PlannedDishRow(
            @Schema(description = "D1 — mã phiên phát suất (PENDING)") String distributionId,
            @Schema(description = "D1 — mã học sinh") String studentId,
            String studentName,
            @Schema(description = "D1 — code lớp (Phase 2 — gộp lại theo khối ở FE khi xem toàn trường)")
            String className,
            @Schema(description = "Mã món SẼ PHÁT (món thay thế nếu isSubstitute=true, món gốc nếu không)")
            String dishId,
            String dishName,
            boolean isSubstitute,
            @Schema(description = "Tên món GỐC bị dị ứng — chỉ có khi isSubstitute=true")
            String substitutedForDishName,
            String reason,
            @Schema(description = "true = dị ứng trùng món nhưng CHƯA có món thay thế cấu hình (BR-065) — "
                    + "serve()/confirm() sẽ chặn (SRV-026), cần can thiệp tay")
            boolean blocked,
            @Schema(description = "menus.audience của thực đơn em này được định tuyến tới: 'ALL' = thực đơn "
                    + "chung (nồi lớn), còn lại là mã nhóm chế độ ăn (BR-082 — nấu nồi riêng, dụng cụ riêng). "
                    + "KHÁC chế độ ăn của bản thân HS: em có chế độ ăn riêng vẫn nhận 'ALL' khi thực đơn chung "
                    + "không vi phạm giới hạn của nhóm.")
            String menuAudience) {}

    @Schema(name = "PlannedDishSummary", description = "Gộp theo món — số suất bếp cần nấu, kể cả món thay thế")
    public record PlannedDishSummary(
            String dishId,
            String dishName,
            @Schema(description = "Tổng số suất món này (bao gồm cả suất thay thế của HS khác)") int count,
            @Schema(description = "Trong đó bao nhiêu suất là THAY THẾ (không phải món gốc của thực đơn)")
            int substituteCount,
            @Schema(description = "Trong đó bao nhiêu suất BỊ CHẶN (dị ứng trùng món, chưa có món thay thế cấu "
                    + "hình — KHÔNG được phát cho tới khi xử lý tay, BR-065/SRV-026)")
            int blockedCount) {}

    @Schema(name = "PlannedDishesResponse")
    public record PlannedDishesResponse(List<PlannedDishRow> perStudent, List<PlannedDishSummary> byDish) {}
}
