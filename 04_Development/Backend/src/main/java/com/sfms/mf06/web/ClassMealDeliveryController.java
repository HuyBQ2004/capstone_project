package com.sfms.mf06.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.ClassMealDeliveryDtos.ClassMealDeliveryResponse;
import com.sfms.mf06.dto.ClassMealDeliveryDtos.CreateDeliveryRequest;
import com.sfms.mf06.service.ClassMealDeliveryService;
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

/**
 * MF06 — {@code class-meal-deliveries} (bếp→lớp).
 * 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2 mục (1)+(4).
 */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/class-meal-deliveries")
public class ClassMealDeliveryController {

    private final ClassMealDeliveryService service;

    public ClassMealDeliveryController(ClassMealDeliveryService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách giao/nhận suất ăn theo lớp",
            description = "Teacher chỉ thấy lớp mình (BR-120); School Admin/Service Caterers thấy toàn trường")
    @PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @GetMapping
    public List<ClassMealDeliveryResponse> list(
            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam String mealType,
            @RequestParam(value = "class", required = false) String classroomId,
            @CurrentUser AuthUser actor) {
        return service.list(date, mealType, classroomId, actor);
    }

    @Operation(summary = "Xác nhận đã giao suất tới lớp", description = "Service Caterers — idempotent theo (ngày, bữa, lớp)")
    @ApiResponse(responseCode = "409", description = "Đã có bản ghi giao cho (ngày, bữa, lớp) này",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassMealDeliveryResponse deliver(@Valid @RequestBody CreateDeliveryRequest request,
                                             @CurrentUser AuthUser actor) {
        return service.deliver(request, actor);
    }

    @Operation(summary = "Xác nhận đã nhận đủ suất", description = "Teacher (đúng lớp mình) — idempotent")
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{code}/receive")
    public ClassMealDeliveryResponse receive(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.receive(code, actor);
    }
}
