package com.sfms.mf07.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** DTO của {@code kitchen-inspections} (UC-014). TDS-MF07_Backend.md §9.3.6. */
public final class KitchenInspectionDtos {

    private KitchenInspectionDtos() {
    }

    public record ItemResultRequest(@NotNull String item, @NotNull Boolean passed,
                                     String reason, Integer photos) {}

    @Schema(description = "score do FE gửi bị BỎ QUA — backend tính lại từ itemResults (CF-26)")
    public record SubmitKitchenInspectionRequest(
            boolean pestControlValid, boolean staffHealthValid,
            @NotNull List<ItemResultRequest> itemResults) {}

    public record ItemFindingDto(String item, int photos, String reason) {}

    public record ItemResultDto(String item, boolean passed) {}

    public record Warning(String code, String message) {}

    /** {@code PATCH /kitchen-inspections/{code}/record} — biên bản sự việc (BR-221 mở rộng). */
    public record RecordUpdateRequest(@NotBlank String content) {}

    @Schema(name = "KitchenInspectionResponse")
    public record KitchenInspectionResponse(
            String id, String date, int score, String inspector, boolean pestControlValid,
            boolean staffHealthValid, String result, List<String> failedItems,
            List<ItemFindingDto> itemFindings, int photosCount, String confirmStatus,
            List<ItemResultDto> itemResults, List<Warning> warnings,
            String confirmedBy, String confirmedAt, String recordContent) {}

    @Schema(name = "PagedResponseKitchenInspectionResponse", description = "PagedResponse<KitchenInspectionResponse>")
    public record PagedResponseKitchenInspectionResponse(
            List<KitchenInspectionResponse> items, long total, int page, int pageSize, int pageCount) {}
}
