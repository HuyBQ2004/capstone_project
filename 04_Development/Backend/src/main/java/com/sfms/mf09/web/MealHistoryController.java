package com.sfms.mf09.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf09.dto.MealHistoryDtos.MealHistoryEntryResponse;
import com.sfms.mf09.dto.MealHistoryDtos.NutritionHistoryResponse;
import com.sfms.mf09.service.MealHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * MF09 — {@code /api/v1/students/{code}/meal-history} + {@code /special-meal-records}
 * (UC-133/053/142, M09-D07).
 *
 * <p>⚠️ Controller RIÊNG của MF09, KHÔNG phải mở rộng {@code StudentController} của MF03.
 * Path gốc {@code /students} do MF03 sở hữu (2 controller đã có: {@code StudentController} @
 * {@code /api/v1/students}, {@code StudentHealthFileController} @ {@code /api/v1/students/{code}}
 * — cả hai dùng {@code {code}}, controller này theo đúng khuôn đó cho 2 sub-path riêng, Spring
 * cho phép vì không trùng path/method). <b>Cần Huy (chủ MF03) xác nhận trước khi merge</b>
 * (§2.1 mục B của {@code 02_Module_Completion_Workflow.md}).
 */
@Tag(name = "MF09 — Meal Serving & Distribution")
@RestController
@RequestMapping("/api/v1/students/{code}")
public class MealHistoryController {

    private final MealHistoryService service;

    public MealHistoryController(MealHistoryService service) {
        this.service = service;
    }

    @Operation(summary = "Lịch sử bữa ăn của học sinh",
            description = "UC-133/053 · BR-201/BR-120 · BR-149 (Y tế điều tra sự cố — trong phạm vi trường)")
    // MEDICAL_STAFF thêm 08/08/2026 (backlog B4): điều tra sự cố ATTP phải trả lời được "đứa trẻ
    // này đã ăn gì", mà đường DUY NHẤT tra việc đó là đây — trước đó y tế nhận 403 và không có
    // màn nào thay thế. Phạm vi cắt ở `resolveAndGuard` (chỉ trong trường của mình).
    @PreAuthorize("hasAnyRole('PARENT','TEACHER','MEDICAL_STAFF')")
    @GetMapping("/meal-history")
    public PagedResponse<MealHistoryEntryResponse> mealHistory(@PathVariable String code,
                                                                @RequestParam Map<String, String> allParams,
                                                                @CurrentUser AuthUser actor) {
        return service.list(code, ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết 1 lần phát suất trong lịch sử", description = "UC-133 chi tiết")
    @PreAuthorize("hasAnyRole('PARENT','TEACHER','MEDICAL_STAFF')")
    @GetMapping("/meal-history/{distributionCode}")
    public MealHistoryEntryResponse mealHistoryDetail(@PathVariable String code,
                                                       @PathVariable String distributionCode,
                                                       @CurrentUser AuthUser actor) {
        return service.detail(code, distributionCode, actor);
    }

    @Operation(summary = "Suất ăn đặc biệt trong lịch sử", description = "UC-142 — chỉ Phụ huynh (con mình)")
    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/special-meal-records")
    public PagedResponse<MealHistoryEntryResponse> specialMealRecords(@PathVariable String code,
                                                                       @RequestParam Map<String, String> allParams,
                                                                       @CurrentUser AuthUser actor) {
        return service.specialMealRecords(code, ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Lịch sử dinh dưỡng bữa trưa", description = "BR-062 — kcal/protein/fat/carb thật từ dishes")
    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/nutrition-history")
    public NutritionHistoryResponse nutritionHistory(
            @PathVariable String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @CurrentUser AuthUser actor) {
        return service.nutritionHistory(code, from, to, actor);
    }
}
