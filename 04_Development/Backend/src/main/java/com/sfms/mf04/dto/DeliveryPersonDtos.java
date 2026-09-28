package com.sfms.mf04.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

/** DTO của {@code /delivery-persons} (UC-019/110/068/028) — khớp TDS §9.2. */
public final class DeliveryPersonDtos {

    private DeliveryPersonDtos() {
    }

    @Schema(name = "CreateDeliveryPersonRequest")
    public record CreateDeliveryPersonRequest(
            @Schema(description = "D5 — mã client tự sinh, trùng ⇒ 409 SUP-139") String id,

            @NotBlank @Schema(example = "Lê Thị Hòa") String fullName,

            @NotBlank
            @Pattern(regexp = "^\\d{12}$", message = "national ID must be exactly 12 digits")
            @Schema(description = "BR-050 — CCCD 12 chữ số, duy nhất toàn hệ thống", example = "001099212345")
            String nationalId,

            @NotBlank
            @Pattern(regexp = "^(\\+84|0)[\\d\\s.]{9,12}$")
            @Schema(example = "0905112233") String phone,

            @NotBlank
            @Pattern(regexp = "^\\d{2}[A-Z]{1,2}[-\\s]?\\d{3}\\.?\\d{2}$")
            @Schema(example = "29E-246.80") String vehiclePlate
    ) {}

    @Schema(name = "DeliveryPersonResponse")
    public record DeliveryPersonResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "DP-03") String id,
            @Schema(description = "D1 — mã đối tác") String partnerId,
            String fullName,
            @Schema(description = "PDPD — luôn trả dạng che, VD 0010992xxxxx", example = "0010992xxxxx")
            String nationalId,
            String phone,
            String vehiclePlate,
            @Schema(description = "D6 — common/enums/BiometricStatus, 3 giá trị") String biometricStatus,
            @Schema(type = "string", format = "date-time") Instant biometricRegisteredAt
    ) {}
}
