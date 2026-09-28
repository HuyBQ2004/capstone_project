package com.sfms.mf09.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf09.dto.MealRecordDtos.BatchConsumptionRequest;
import com.sfms.mf09.dto.MealRecordDtos.MealRecordResponse;
import com.sfms.mf09.dto.MealRecordDtos.PagedResponseMealRecordResponse;
import com.sfms.mf09.dto.MealRecordDtos.UpdateConsumptionRequest;
import com.sfms.mf09.service.MealConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** MF09 — {@code meal-records} (UC-080/121/125/133). TDS-MF09_Backend.md §9.2/§16. */
@Tag(name = "MF09 — Meal Serving & Distribution")
@RestController
@RequestMapping("/api/v1/meal-records")
public class MealRecordController {

    private final MealConsumptionService service;

    public MealRecordController(MealConsumptionService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách ghi nhận tiêu thụ", description = "UC-121 — Teacher lớp mình")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PagedResponseMealRecordResponse.class)))
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping
    public PagedResponse<MealRecordResponse> list(@RequestParam Map<String, String> allParams,
                                                   @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Cập nhật mức tiêu thụ", description = "UC-080 · D12 — chỉ nhận field sửa được")
    @PreAuthorize("hasRole('TEACHER')")
    @PatchMapping("/{code}")
    public MealRecordResponse update(@PathVariable String code, @Valid @RequestBody UpdateConsumptionRequest request,
                                     @CurrentUser AuthUser actor) {
        return service.update(code, request, actor);
    }

    @Operation(summary = "Ghi nhận hàng loạt cho cả lớp", description = "UC-080 — 1 transaction, N học sinh")
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/batch")
    public List<MealRecordResponse> batch(@Valid @RequestBody BatchConsumptionRequest request,
                                          @CurrentUser AuthUser actor) {
        return service.updateBatch(request, actor);
    }
}
