package com.sfms.mf08.dto;

import com.sfms.common.enums.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO của {@code sample_label_reservations} — đặt trước tem Mẫu số 4 cho nhiều ngày/món
 * (quyết định của Nam, 10/08/2026), TRƯỚC khi có đủ dữ liệu để tạo {@code FoodSample} thật.
 */
public final class SampleLabelReservationDtos {

    private SampleLabelReservationDtos() {
    }

    @Schema(description = "Một món cần đặt trước tem cho một ngày/bữa cụ thể")
    public record ReservationItem(
            @NotNull LocalDate collectDate,
            @NotNull MealType mealType,
            @NotBlank String dishId) {}   // D1 — mã dishes.code

    @Schema(description = "POST /sample-label-reservations — đặt trước hàng loạt (nhiều ngày/món cùng lúc)")
    public record BulkCreateRequest(@NotEmpty @Valid List<ReservationItem> items) {}

    @Schema(name = "SampleLabelReservationResponse")
    public record ReservationResponse(
            String id, String dishId, String dish, String mealType, LocalDate collectDate, String status) {}

    @Schema(name = "PagedResponseSampleLabelReservationResponse",
            description = "PagedResponse<SampleLabelReservationResponse>")
    public record PagedResponseReservationResponse(
            List<ReservationResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(description = "GET /sample-label-reservations/{id}/label — dữ liệu in tem trước, CHƯA có collectedAt/collectedBy")
    public record ReservationLabelResponse(
            String id, String facility, String dishName, String mealType, LocalDate collectDate) {}
}
