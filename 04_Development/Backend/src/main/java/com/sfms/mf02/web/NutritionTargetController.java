package com.sfms.mf02.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf02.dto.NutritionTargetDtos.NutritionTargetResponse;
import com.sfms.mf02.dto.NutritionTargetDtos.UpdateNutritionTargetRequest;
import com.sfms.mf02.service.NutritionTargetService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * MF02 — {@code /api/v1/nutrition-targets}: chuẩn dinh dưỡng bữa trưa theo
 * TRƯỜNG × KHỐI (BR-062), xem migration V54.
 *
 * <p>Trước 07/08/2026 chuẩn này là hằng số trong code MF09 nên không trường nào
 * sửa được, và mọi khối bị chấm cùng một thước ±10% dù {@code menus} đã gắn
 * {@code grade_id} từ V44.
 *
 * <p>Đọc mở cho các vai CẦN so sánh dinh dưỡng: Ban giám hiệu duyệt thực đơn,
 * đơn vị suất ăn lập thực đơn, y tế xem hồ sơ. GHI chỉ Ban giám hiệu — cùng
 * thẩm quyền với {@code school_config.cost_ceiling} (BR-080).
 */
@Tag(name = "MF02 — Nutrition Target (Chuẩn dinh dưỡng theo khối)")
@RestController
@RequestMapping("/api/v1/nutrition-targets")
public class NutritionTargetController {

    private final NutritionTargetService nutritionTargetService;

    public NutritionTargetController(NutritionTargetService nutritionTargetService) {
        this.nutritionTargetService = nutritionTargetService;
    }

    @Operation(summary = "Chuẩn dinh dưỡng mọi khối của trường",
            description = "Khối chưa cấu hình vẫn trả về, với configured=false và 4 chỉ số mặc định")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping
    public List<NutritionTargetResponse> list(@CurrentUser AuthUser actor) {
        return nutritionTargetService.list(actor);
    }

    @Operation(summary = "Đặt chuẩn dinh dưỡng cho một khối (merge một phần)",
            description = "Khối chưa có chuẩn thì tạo mới; field không gửi lấy giá trị mặc định hệ thống")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — khối không tồn tại hoặc khác trường",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PatchMapping("/{gradeId}")
    public NutritionTargetResponse update(@PathVariable UUID gradeId,
                                           @Valid @RequestBody UpdateNutritionTargetRequest request,
                                           @CurrentUser AuthUser actor) {
        return nutritionTargetService.update(gradeId, request, actor);
    }
}
