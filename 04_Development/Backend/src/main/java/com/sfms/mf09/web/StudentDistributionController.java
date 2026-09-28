package com.sfms.mf09.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf09.dto.DistributionDtos;
import com.sfms.mf09.dto.DistributionDtos.ConfirmBatchRequest;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
import com.sfms.mf09.dto.DistributionDtos.DistributionSummaryResponse;
import com.sfms.mf09.dto.DistributionDtos.ErrorReportRequest;
import com.sfms.mf09.dto.DistributionDtos.PlannedDishesResponse;
import com.sfms.mf09.service.StudentDistributionService;
import com.sfms.common.enums.MealType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MF09 — {@code student-distributions} (UC-016/047/103/132). TDS-MF09_Backend.md §9.2/§16.
 * M09-D06: KHÔNG mở {@code /special-meals} ở đây — MF03 sở hữu path đó.
 */
@Tag(name = "MF09 — Meal Serving & Distribution")
@RestController
@RequestMapping("/api/v1/student-distributions")
public class StudentDistributionController {

    private final StudentDistributionService service;

    public StudentDistributionController(StudentDistributionService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách phiên phát suất",
            description = "UC-016/132 — D11: Service Caterers nhận shape RÚT GỌN (KHÔNG PII trẻ em, ADR-M09-6)")
    @ApiResponse(responseCode = "200", description = "PagedResponse theo §2b-1",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(oneOf = {
                            DistributionDtos.PagedResponseDistributionResponse.class,
                            DistributionDtos.PagedResponseDistributionCatererResponse.class})))
    @PreAuthorize("hasAnyRole('TEACHER','SERVICE_CATERERS','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<?> list(@RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết phiên phát suất", description = "UC-016 · BR-120 — Teacher chỉ đọc lớp mình")
    @ApiResponse(responseCode = "404", description = "SRV-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('TEACHER','SERVICE_CATERERS','SCHOOL_ADMIN')")
    @GetMapping("/{code}")
    public DistributionResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.getByCode(code, actor);
    }

    @Operation(summary = "Xác nhận phát suất cho 1 học sinh", description = "UC-016 · BR-120/130/094")
    @ApiResponse(responseCode = "403", description = "SRV-005 (BR-120) / SRV-094 (BR-094 MF07)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SRV-021 (BR-130)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SRV-022 — không có thực đơn (ràng buộc C3)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{code}/serve")
    public DistributionResponse serve(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.serve(code, actor);
    }

    @Operation(summary = "Xác nhận phát suất hàng loạt", description = "UC-016 — 1 transaction, N học sinh")
    @ApiResponse(responseCode = "200", description = "MSG-045")
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/confirm")
    public List<DistributionResponse> confirm(@Valid @RequestBody ConfirmBatchRequest request,
                                              @CurrentUser AuthUser actor) {
        return service.confirmBatch(request, actor);
    }

    @Operation(summary = "Hủy đánh dấu đã phát", description = "ADR-M09-3 — chỉ trong ngày, chỉ GVCN")
    @ApiResponse(responseCode = "409", description = "SRV-024 — hủy sau ngày phục vụ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{code}/revoke")
    public DistributionResponse revoke(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.revoke(code, actor);
    }

    @Operation(summary = "Báo lỗi phân phát sai suất", description = "UC-016 AF1")
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{code}/error-report")
    @ResponseStatus(HttpStatus.CREATED)
    public DistributionResponse errorReport(@PathVariable String code,
                                            @Valid @RequestBody ErrorReportRequest request,
                                            @CurrentUser AuthUser actor) {
        return service.reportError(code, request, actor);
    }

    @Operation(summary = "Suất ăn dự kiến (chưa phát)",
            description = "04_Meal_Substitution_Portion_Pipeline_Design.md Phase 1/2 — dùng LẠI đúng luật thay "
                    + "món của serve()/confirm(), KHÔNG ghi gì (an toàn gọi lại nhiều lần). `class` BẮT BUỘC với "
                    + "Teacher (chỉ xem lớp mình); TÙY CHỌN với School Admin/Service Caterers — bỏ trống để gộp "
                    + "TOÀN TRƯỜNG theo món (Phase 2 — đếm suất bếp cần nấu, kể cả món thay thế).")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @GetMapping("/planned-dishes")
    public PlannedDishesResponse plannedDishes(
            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(value = "class", required = false) String className,
            @RequestParam MealType mealType,
            @CurrentUser AuthUser actor) {
        return service.plannedDishes(date, className, mealType, actor);
    }

    @Operation(summary = "Tổng hợp theo lớp trong ngày", description = "UC-132 — tính runtime (ADR-M09-2)")
    @PreAuthorize("hasAnyRole('SERVICE_CATERERS','SCHOOL_ADMIN','TEACHER')")
    @GetMapping("/summary")
    public List<DistributionSummaryResponse> summary(
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate date,
            @CurrentUser AuthUser actor) {
        return service.summary(date, actor);
    }
}
