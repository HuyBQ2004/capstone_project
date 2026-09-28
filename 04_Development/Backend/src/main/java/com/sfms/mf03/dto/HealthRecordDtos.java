package com.sfms.mf03.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/** DTO của {@code /students/{id}/health-record} — CF-M03-9 (resource mới). */
public final class HealthRecordDtos {

    private HealthRecordDtos() {
    }

    /**
     * Biên khớp CHECK {@code student_health_records} ở {@code V4__mf03_student_parent.sql}
     * ({@code height_cm BETWEEN 50 AND 220}, {@code weight_kg BETWEEN 5 AND 150}) — validate ở
     * DTO để trả 422 STU-422/MSG-138 tử tế; CHECK ở DB là chốt chặn cuối (nếu chỉ dựa CHECK,
     * client nhận 500 thay vì lỗi có ý nghĩa — cùng nguyên tắc đã áp cho BR-031/STU-031).
     */
    @Schema(name = "HealthRecordRequest")
    public record HealthRecordRequest(
            @DecimalMin(value = "50", message = "heightCm must be between 50 and 220")
            @DecimalMax(value = "220", message = "heightCm must be between 50 and 220")
            BigDecimal heightCm,

            @DecimalMin(value = "5", message = "weightKg must be between 5 and 150")
            @DecimalMax(value = "150", message = "weightKg must be between 5 and 150")
            BigDecimal weightKg,

            String bloodType
    ) {}

    @Schema(name = "HealthRecordResponse")
    public record HealthRecordResponse(
            String studentId,
            BigDecimal heightCm,
            BigDecimal weightKg,
            String bloodType,
            @Schema(description = "Field dẫn xuất — weightKg/(heightCm/100)^2 (ADR-M03-3)")
            BigDecimal bmi,
            String status
    ) {}
}
