package com.sfms.mf06.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** DTO của resource {@code receiving-sessions} — TDS-MF06_Backend.md §9.2 (ADR-M06-5/M06-7). */
public final class ReceivingDtos {

    private ReceivingDtos() {
    }

    /**
     * D12 — request KHÔNG có {@code receivedAt}/{@code deviceInfo} máy chủ tự đặt lại,
     * KHÔNG {@code gpsVerified}/{@code status}/{@code supplierStatus} (field dẫn xuất — M06-D07).
     */
    /**
     * BR-071 (10/08/2026) — {@code receivedQuantity} là lượng THỰC NHẬN ở dòng này; bắt buộc
     * và phải &gt; 0 khi {@code rejected = false} (một lô có thể nhận nhiều đợt/nhiều trường,
     * tổng đã nhận không được vượt số lượng lô NCC đăng ký — validate ở service, không ở đây
     * vì cần đọc lô mới biết còn lại bao nhiêu).
     */
    public record ReceivingLineRequest(
            @NotBlank String supplierBatchId, @NotBlank String ingredientId, @NotBlank String storageZone,
            boolean rejected, String rejectReason, List<String> rejectDocumentIds, BigDecimal receivedQuantity
    ) {}

    /**
     * {@code evidencePhotoIds} bổ sung 14/08/2026 — mã ảnh bằng chứng CHUNG cả phiên (BR-072),
     * upload TRƯỚC qua {@code POST /receiving-sessions/reject-documents} (endpoint dùng chung,
     * không riêng cho lô từ chối) rồi gửi mã lên đây — cùng khuôn D10-BIS.3 với
     * {@code ReceivingLineRequest.rejectDocumentIds}. {@code photosCount} giữ lại để tương
     * thích ngược (client cũ chưa cập nhật) nhưng server KHÔNG còn tin số đếm client gửi —
     * khi {@code evidencePhotoIds} có mặt, service tự suy lại {@code photosCount} từ kích
     * thước danh sách thật (D12).
     */
    public record ReceivingSessionCreateRequest(
            @NotBlank String deliveryId, String driverVerificationId,
            @NotNull BigDecimal lat, @NotNull BigDecimal lng, int photosCount,
            List<String> evidencePhotoIds, String deviceInfo,
            @NotNull @Valid List<ReceivingLineRequest> lines
    ) {}

    /**
     * {@code rejectDocumentIds} bổ sung 13/08/2026 — mã ảnh bằng chứng của dòng bị từ chối
     * (BR-072/078). Trước đó response KHÔNG trả ra nên màn hình lịch sử nhận hàng không có cách
     * nào xem lại ảnh đã chụp; xem qua {@code GET /receiving-sessions/reject-documents/{id}/file}.
     */
    public record ReceivingLineResponse(
            String supplierBatchId, String ingredientId, String ingredient, String batchId,
            String storageZone, boolean rejected, String rejectReason, BigDecimal receivedQuantity,
            List<String> rejectDocumentIds
    ) {}

    public record ReceivingSessionResponse(
            String id, String supplier, String supplierId, String supplierStatus, Instant receivedAt,
            int items, List<String> batches, String status, boolean gpsVerified, int photosCount,
            List<String> evidencePhotoIds, String receivedBy, List<ReceivingLineResponse> lines
    ) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponseReceivingSessionResponse",
            description = "PagedResponse<ReceivingSessionResponse>")
    public record PagedResponseReceivingSessionResponse(
            List<ReceivingSessionResponse> items, long total, int page, int pageSize, int pageCount) {}
}
