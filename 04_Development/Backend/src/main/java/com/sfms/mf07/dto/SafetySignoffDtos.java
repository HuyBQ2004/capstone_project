package com.sfms.mf07.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO của {@code safety-signoffs} (UC-027 — BGH ký xác nhận an toàn hằng ngày).
 *
 * <p>Tên record đặt {@code @Schema(name=...)} tiền tố {@code SafetySignoff} phòng va chạm
 * đơn giản-tên với module khác (cùng lớp lỗi springdoc-gộp-theo-simple-name đã tìm thấy ở
 * {@code ChecklistDtos.ItemRequest}/{@code ItemResponse} lúc làm Bước 3).
 */
public final class SafetySignoffDtos {

    private SafetySignoffDtos() {
    }

    @Schema(name = "SafetySignoffRequest")
    public record SignoffRequest(String note) {}

    @Schema(name = "SafetySignoffResponse")
    public record SignoffResponse(String id, String date, String signedBy, String signedAt,
                                   String note, Integer hygieneScore, int failedSteps) {}

    @Schema(name = "PagedResponseSafetySignoffResponse")
    public record PagedResponseSignoffResponse(List<SignoffResponse> items, long total, int page, int pageSize, int pageCount) {}
}
