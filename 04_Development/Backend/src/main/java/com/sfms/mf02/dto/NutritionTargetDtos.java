package com.sfms.mf02.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

/** DTO của {@code /api/v1/nutrition-targets} — chuẩn dinh dưỡng bữa trưa theo khối (BR-062). */
public final class NutritionTargetDtos {

    private NutritionTargetDtos() {
    }

    /**
     * Một dòng = một KHỐI của trường đang đăng nhập. Trả về đủ mọi khối, kể cả khối
     * chưa cấu hình — lúc đó {@code configured=false} và 4 chỉ số là giá trị mặc định.
     *
     * <p>Vì sao không bỏ hẳn khối chưa cấu hình khỏi danh sách: màn cấu hình cần hiện
     * đủ khối để Ban giám hiệu biết mình còn thiếu gì; và người đọc cần biết con số
     * đang thấy là chuẩn riêng của trường hay giá trị mặc định của hệ thống.
     */
    @Schema(name = "NutritionTargetResponse")
    public record NutritionTargetResponse(
            @Schema(description = "UUID của Grade (GET /api/v1/grades)") UUID gradeId,
            @Schema(example = "Khối 3") String gradeName,
            int sortOrder,
            @Schema(description = "false = trường chưa khai, 4 chỉ số dưới là mặc định hệ thống")
            boolean configured,
            BigDecimal kcal,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal carb
    ) {}

    /**
     * Partial update — field không gửi giữ nguyên. Gửi lần đầu cho khối chưa cấu hình
     * thì các field vắng lấy từ giá trị mặc định của hệ thống.
     *
     * <p>{@code @DecimalMin} chặn số không dương ngay ở tầng API, khớp CHECK
     * {@code ck_nutrition_target_positive} ở DB: 0 kcal không phải "chưa khai" mà là
     * dữ liệu sai — "chưa khai" biểu diễn bằng KHÔNG CÓ DÒNG.
     */
    @Schema(name = "UpdateNutritionTargetRequest",
            description = "Partial update — field không gửi giữ nguyên")
    public record UpdateNutritionTargetRequest(
            @DecimalMin(value = "0", inclusive = false) @Schema(example = "715") BigDecimal kcal,
            @DecimalMin(value = "0", inclusive = false) @Schema(example = "28") BigDecimal protein,
            @DecimalMin(value = "0", inclusive = false) @Schema(example = "22") BigDecimal fat,
            @DecimalMin(value = "0", inclusive = false) @Schema(example = "95") BigDecimal carb
    ) {}
}
