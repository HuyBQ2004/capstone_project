package com.sfms.mf05.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sfms.common.enums.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO của resource {@code menus} — TDS-MF05_Backend.md §9.3.1..§9.3.5.
 *
 * <p><b>D11 (M05-D08)</b> — hai shape theo vai trò: {@link MenuResponse} (Caterer/SchoolAdmin/
 * Medical/SysAdmin, đủ field) vs {@link MenuPublicResponse} (Parent/Teacher, bỏ giá + chữ ký
 * duyệt). Hai record {@link PagedResponseMenuResponse}/{@link PagedResponseMenuPublicResponse}
 * tồn tại THUẦN để khai {@code oneOf} trên OpenAPI (springdoc mới sinh {@code items} đúng kiểu
 * cho cả hai — xem {@code common/dto/PagedResponse.java}, KHÔNG đặt {@code name=} cho generic).
 *
 * <p><b>D12 (M05-D09)</b> — {@code dishes}/{@code allergyConflicts} LUÔN là số đếm dẫn xuất,
 * read-only; nội dung món CHỈ đi qua {@code blocksData} ở cả hai chiều (bẫy đã gỡ: KHÔNG có
 * biến thể nào của {@code dishes} là mảng trong payload ghi).
 */
public final class MenuDtos {

    private MenuDtos() {
    }

    // ───────────────────────── Block content (blocksData — CF-12) ─────────────────────────

    @Schema(description = "Một buổi ăn trong ngày (blocksData[]) — nội dung thật của thực đơn")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MenuBlock(
            @Schema(description = "BREAKFAST | LUNCH | SNACK") MealType mealType,
            @Schema(description = "Số suất của buổi này", example = "320") Integer portions,
            String note,
            @Schema(description = "Danh sách món của buổi — D12: nội dung món CHỈ đi qua đây")
            List<MenuBlockDish> dishes
    ) {}

    @Schema(description = "Một món trong blocksData[].dishes[]")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MenuBlockDish(
            @Schema(description = "dishes.code — D1, KHÔNG lưu tên món (C5)", example = "ca-basa-sot-ca-chua")
            @NotBlank String dishId,
            String name,
            String kcal, String protein, String carb, String fat,
            Boolean isSpecialDish,
            List<String> manualAllergens,
            List<MenuIngredient> ingredients,
            List<MenuBlockSubstitute> substitutes,
            @Schema(description = "Số suất của món này trong buổi (mặc định = portions của block)")
            Integer portions,
            @Schema(description = "V55 — mẫu thực đơn đã sinh ra món này (null = thêm tay). "
                    + "UI dùng để gộp món theo mẫu và gỡ đúng mẫu khỏi ô.")
            String templateId
    ) {}

    @Schema(description = "Nguyên liệu của món (dish_ingredients — BR-069)")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MenuIngredient(
            @Schema(description = "NULL/rỗng = ngoài danh mục (gia vị) — hợp lệ ở thư viện món, "
                    + "bị chặn khi lập thực đơn nếu thiếu (BR-069)")
            String ingredientId,
            @NotBlank String name,
            @Schema(description = "PROPOSED (plan \"Đơn vị đo lường\" §Phase B.8) — đổi từ String "
                    + "sang số 08/2026; null = nguyên liệu ngoài danh mục chưa định lượng cụ thể")
            BigDecimal quantity,
            String unit
    ) {}

    @Schema(description = "Suất thay thế cho dị ứng (BR-063/065)")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MenuBlockSubstitute(
            @NotBlank String dishId,
            String name,
            String kcal,
            String portions,
            String note,
            List<String> manualAllergens,
            List<String> allergenIds,
            List<MenuIngredient> ingredients
    ) {}

    // ───────────────────────── Write DTOs ─────────────────────────

    /**
     * D12 — FE gửi kèm field dẫn xuất ({@code dishes}, {@code allergyConflicts}, {@code createdBy}…)
     * mà DTO này KHÔNG khai — {@code @JsonIgnoreProperties(ignoreUnknown = true)} để Jackson
     * BỎ QUA im lặng thay vì 422 (vi phạm C11/M05-D09 nếu chặn thay vì bỏ qua).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "POST /api/v1/menus — lập thực đơn nháp (UC-020) hoặc thực đơn riêng (UC-023)")
    public record CreateMenuRequest(
            @Schema(description = "D5 — mã client tự sinh; vắng thì server sinh MENU-<năm>-<seq>. "
                    + "Thực đơn riêng BẮT BUỘC khuôn {menuCode}--{dietGroupCode} (C10)",
                    example = "MENU-2026-1042")
            String id,
            @Schema(description = "Nhãn ngày hiển thị", example = "Thứ 4 — 05/08/2026") String week,
            @NotBlank String name,
            @NotBlank @Schema(description = "V44 — UUID của Grade (GET /api/v1/grades)",
                    example = "3b6f1e2e-9c2a-4e33-8e3a-000000000003") String gradeId,
            @NotNull @Schema(example = "2026-08-05") LocalDate menuDateIso,
            @NotNull MealType mealType,
            @Schema(description = "DRAFT (mặc định) hoặc PENDING_APPROVAL (nộp duyệt ngay)")
            String status,
            @Schema(description = "\"ALL\" (thực đơn chung) hoặc mã DietGroup (thực đơn riêng)")
            String audience,
            String dietGroupId,
            BigDecimal estimatedCost,
            Integer servings,
            @Schema(description = "Bắt buộc khi thực đơn riêng thêm SAU khi thực đơn chung đã công bố")
            String reason,
            List<MenuBlock> blocksData
    ) {}

    @Schema(description = "PUT /api/v1/menus/{id} — merge MỘT PHẦN (C8), field không gửi giữ nguyên")
    public record UpdateMenuRequestDoc(
            String week, String name, String gradeId, LocalDate menuDateIso, MealType mealType,
            String audience, String dietGroupId, List<MenuBlock> blocksData, Integer servings,
            BigDecimal estimatedCost, String reason,
            @Schema(description = "Field phân quyền theo vai trò — xem §16 phần cuối") String status,
            String approvalSignature, String rejectReason, String submittedAt
    ) {}

    public record ApproveMenuRequest(String approvalSignature) {}

    public record RejectMenuRequest(@NotBlank String rejectReason) {}

    public record ApplyTemplateRequest(@NotBlank String templateId, MealType mealType, Integer portions) {}

    // ───────────────────────── Read DTOs (D11) ─────────────────────────

    @Schema(name = "MenuResponse", description = "Caterer/SchoolAdmin/Medical/SysAdmin — đủ field")
    public record MenuResponse(
            @Schema(description = "Mã nghiệp vụ (D1)", example = "MENU-2026-30") String id,
            String week,
            String name,
            @Schema(description = "V44 — nhãn hiển thị (Grade.name), VD \"Khối 3\" (D1 — KHÔNG lộ "
                    + "gradeId UUID nội bộ; ghi dữ liệu dùng gradeId lấy từ GET /api/v1/grades)")
            String grade,
            LocalDate menuDateIso,
            String status,
            @Schema(description = "Dẫn xuất — số món, KHÔNG BAO GIỜ nhận mảng (D12)", example = "4") int dishes,
            @Schema(description = "Dẫn xuất — tính runtime (ADR-M05-02)", example = "0") int allergyConflicts,
            @Schema(description = "Dẫn xuất (13/08/2026) — TÊN các món còn chạm dị ứng mà CHƯA có "
                    + "suất thay thế, tức đúng tập chặn duyệt của BR-063. Thêm vì màn duyệt cần nêu "
                    + "đích danh món; trước đây chỉ có số đếm nên frontend phải TẢI TOÀN BỘ sổ học "
                    + "sinh + khai báo dị ứng về trình duyệt để tự tính lại.")
            List<String> allergyConflictDishes,
            BigDecimal estimatedCost,
            BigDecimal costCeiling,
            Instant submittedAt,
            @Schema(description = "Dẫn xuất — nhãn đơn vị cung cấp") String createdBy,
            @Schema(description = "Dẫn xuất — mã đơn vị cung cấp (D1)") String createdBySupplierId,
            @Schema(description = "Dẫn xuất — nhãn người duyệt") String approvedBy,
            String approvalSignature,
            Instant approvedAt,
            String rejectReason,
            String audience,
            String dietGroupId,
            String reason,
            Integer servings,
            List<MenuBlock> blocksData
    ) {}

    @Schema(name = "MenuPublicResponse", description = "Parent/Teacher — bỏ giá + chữ ký duyệt (D11/M05-D08), "
            + "chỉ trả bản ghi PUBLISHED")
    public record MenuPublicResponse(
            String id,
            String week,
            String name,
            String grade,
            LocalDate menuDateIso,
            String status,
            int dishes,
            int allergyConflicts,
            Instant submittedAt,
            String createdBy,
            String audience,
            String dietGroupId,
            String reason,
            Integer servings,
            List<MenuBlock> blocksData
    ) {}

    public record Warning(String code, String message) {}

    public record CreateResult(MenuResponse menu, List<Warning> warnings) {}

    @Schema(name = "PagedResponseMenuResponse", description = "PagedResponse<MenuResponse>")
    public record PagedResponseMenuResponse(
            List<MenuResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "PagedResponseMenuPublicResponse", description = "PagedResponse<MenuPublicResponse>")
    public record PagedResponseMenuPublicResponse(
            List<MenuPublicResponse> items, long total, int page, int pageSize, int pageCount) {}

    // ───────────────────────── Allergy conflict / AI (§9.3.9/§9.3.10) ─────────────────────────

    public record AllergyConflict(
            String dishId, String dishName, String allergen,
            int affectedStudentCount, boolean resolvedBySubstitute) {}

    public record AllergyConflictListResponse(List<AllergyConflict> conflicts) {}

    // ───────────────────────── Cảnh báo trước theo khối (05_...design.md) ─────────────────────────

    @Schema(description = "1 allergen đã XÁC MINH (VERIFIED) và số HS trong khối — CHỈ số đếm, "
            + "KHÔNG BAO GIỜ studentId/tên (PDPD)")
    public record AllergenCount(String allergen, int count) {}

    @Schema(description = "1 nhóm chế độ ăn ĐÃ DUYỆT (APPROVED) và số HS trong khối — chỉ số đếm")
    public record DietGroupCount(String dietGroupCode, String dietGroupLabel, long students) {}

    @Schema(name = "GradeSummaryResponse", description = "GET /menus/grade-summary — dị ứng/chế độ ăn của "
            + "MỘT khối, dùng khi lập Thực đơn mẫu/thực đơn cho khối đó (KHÔNG gắn ngày/buổi cụ thể)")
    public record GradeSummaryResponse(String grade, List<AllergenCount> allergens, List<DietGroupCount> dietGroups) {}

    @Schema(description = "1 nhóm chế độ ăn cần menu cho đúng (khối, ngày, buổi) này — covered=true nếu "
            + "đã có menu (KHÔNG REJECTED) audience=nhóm đó")
    public record DietCoverageItem(String dietGroupCode, String dietGroupLabel, long students, boolean covered) {}

    @Schema(name = "DietCoverageResponse", description = "GET /menus/diet-coverage — checklist \"cần menu cho "
            + "nhóm nào\" của đúng (khối, ngày, buổi) — BR-065, xem DietCoverageValidator")
    public record DietCoverageResponse(String grade, LocalDate menuDateIso, String mealType,
                                        List<DietCoverageItem> items) {}
}
