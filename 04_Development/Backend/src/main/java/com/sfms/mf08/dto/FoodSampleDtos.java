package com.sfms.mf08.dto;

import com.sfms.common.enums.MealType;
import com.sfms.mf08.dto.CommonDtos.UploadedFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** DTO của {@code food_samples} (UC-069/042/030/076/118). TDS-MF08_Backend.md §9.2. */
public final class FoodSampleDtos {

    private FoodSampleDtos() {
    }

    // ───────────────────────── Requests ─────────────────────────

    @Schema(description = "POST /samples (UC-069) — multipart 'payload' + evidencePhotos[]")
    public record RegisterSampleRequest(
            @Schema(description = "D5 — mã client tự sinh (vd 'SMP-${Date.now()}'); vắng thì server sinh "
                    + "SMP-<yyyyMMdd>-<seq>. Trùng ⇒ 409 SMP-409.")
            String id,
            @NotBlank String inspectionId,           // D1 — mã inspection_sessions.code
            @NotBlank String dishId,                 // D1 — mã dishes.code
            @NotNull MealType mealType,               // D6 (M08-D05)
            @NotBlank String mealShift,
            @Positive int servings,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
            @NotNull String unit,                     // "G" | "ML"
            @NotBlank String container,
            boolean sanitized,
            boolean sealed,
            @NotNull BigDecimal fridgeTemp,
            Instant collectedAt,                       // thời điểm lấy mẫu THẬT (có thể khác lúc submit)
            @NotBlank String storageLocation,
            BigDecimal gpsLat,
            BigDecimal gpsLng) {}

    @Schema(description = "PATCH /samples/{id}/storage (UC-042 AF2) — chuyển tủ / cập nhật nhiệt độ")
    public record UpdateStorageRequest(@NotBlank String storageLocation, BigDecimal fridgeTemp) {}

    @Schema(description = "POST /samples/{id}/dispose (UC-030, BR-111/112) — Mẫu số 5 cột 9/10/12")
    // ⚠️ KHÔNG @NotBlank trên witness/note: TDS §10 đòi mã nghiệp vụ SMP-030/MSG-125 (Mẫu số 5
    // cột 9/10/12) cho case thiếu field, không phải SMP-422/MSG-138 chung chung. Bean validation
    // sẽ THẮNG @Valid nếu khai ở đây (fires trước, che luôn nhánh service) — validate ở
    // FoodSampleServiceImpl.dispose() để trả đúng mã theo §10.
    public record DisposeSampleRequest(Instant disposedAt, String witness, String note) {}

    @Schema(description = "POST /samples/{id}/incident-link (UC-076 AF1) — chỉ metadata")
    public record IncidentLinkRequest(@NotBlank String incidentId) {}

    // ───────────────────────── Response ─────────────────────────

    @Schema(name = "FoodSampleResponse")
    public record FoodSampleResponse(
            String id, String dish, String dishId, String mealType, String meal, String mealShift, int servings,
            BigDecimal amount, String unit, String weight, String container, boolean sanitized, boolean sealed,
            BigDecimal fridgeTemp, boolean tempOutOfRange, Instant collectedAt, String collectedBy,
            String storageLocation, Instant retentionDeadline, long hoursRemaining, String status,
            Instant disposedAt, String disposedBy, String disposalWitness, String disposalNote,
            String labRequestId, Instant retrievedAt, Instant sentToLabAt, String incidentId, String inspectionId,
            List<UploadedFileDto> evidencePhotos) {}

    @Schema(name = "PagedResponseFoodSampleResponse", description = "PagedResponse<FoodSampleResponse> — D11: KHÔNG oneOf, xem M08-D13")
    public record PagedResponseFoodSampleResponse(
            List<FoodSampleResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(description = "GET /samples/{id}/label — dữ liệu nhãn Mẫu số 4 (BR-106, 4 trường bắt buộc)")
    public record Mau4LabelResponse(
            String sampleId, String facility, String dishName, String mealType, String mealShift,
            Instant collectedAt, String collectedBy) {}
}
