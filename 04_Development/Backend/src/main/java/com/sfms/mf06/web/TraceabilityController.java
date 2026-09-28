package com.sfms.mf06.web;

import com.sfms.common.enums.MealType;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.TraceabilityDtos.DishBatchesResponse;
import com.sfms.mf06.service.TraceabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** MF06 — {@code /api/v1/traceability/dish-batches} (BR-076, MF11 dùng cho bản đồ truy xuất). */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/traceability")
public class TraceabilityController {

    private final TraceabilityService service;

    public TraceabilityController(TraceabilityService service) {
        this.service = service;
    }

    @Operation(summary = "Truy xuất món -> lô nguyên liệu", description = "BR-076 · C6 — nối bằng KHOÁ")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/dish-batches")
    public DishBatchesResponse dishBatches(@RequestParam String dishId, @RequestParam LocalDate serviceDate,
                                            @RequestParam String mealType, @CurrentUser AuthUser actor) {
        return service.dishBatches(dishId, serviceDate, MealType.valueOf(mealType), actor);
    }
}
