package com.sfms.mf06.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** DTO của resource {@code deliveries} — UC-083/039/092/109. */
public final class DeliveryDtos {

    private DeliveryDtos() {
    }

    @io.swagger.v3.oas.annotations.media.Schema(name = "DeliveryCreateRequest")
    public record CreateRequest(String id, String purchaseRequestId, @NotNull Instant scheduledAt,
                                String driverId, @NotBlank String vehiclePlate,
                                List<String> supplierBatchIds) {}

    public record StatusUpdateRequest(@NotBlank String status) {}

    public record AlertView(String code, String message) {}

    public record DeliveryResponse(
            String id, String schoolId, String school, String purchaseRequestId, Instant scheduledAt,
            String driverId, String vehiclePlate, String status, Instant statusUpdatedAt, int batches,
            List<String> linkedBatches, AlertView alert
    ) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponseDeliveryResponse",
            description = "PagedResponse<DeliveryResponse>")
    public record PagedResponseDeliveryResponse(
            List<DeliveryResponse> items, long total, int page, int pageSize, int pageCount) {}
}
