package com.sfms.mf03.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO của {@code POST /students/{code}/health-documents} — quyết định nhóm 04/08/2026: bổ sung
 * đường upload tài liệu y tế còn thiếu (trước đây TOÀN HỆ THỐNG chỉ có {@code POST /profile/me/photo}
 * là multipart thật, xem ghi chú cũ trong {@code real-resources.ts} khối MF03 "allergy-declarations").
 *
 * <p>{@code id} ở response CHÍNH LÀ giá trị dùng làm {@code proofFileId} khi gọi
 * {@code POST /allergy-declarations} hoặc {@code POST /chronic-conditions} mức {@code SEVERE}.
 *
 * <p><b>Sửa 12/08/2026 (D1):</b> {@code id} trước đây là UUID nội bộ ("không có mã nghiệp vụ
 * riêng vì tài liệu y tế không phải resource cấp 1"). Lập luận đó hỏng khi mã này ĐI RA UI: màn
 * duyệt của Y tế hiện nguyên chuỗi UUID ở ô "tài liệu đính kèm". Nay là mã nghiệp vụ
 * {@code HDOC-2026-0001} (V71), ghép được vào
 * {@code GET /students/{code}/health-documents/{docCode}/download} để xem chính file đó.
 */
public final class HealthDocumentDtos {

    private HealthDocumentDtos() {
    }

    @Schema(name = "HealthDocumentResponse")
    public record HealthDocumentResponse(
            @Schema(description = "D1 — mã tài liệu (`HDOC-2026-0001`); dùng thẳng làm proofFileId "
                    + "khi khai báo dị ứng/bệnh nền mức SEVERE và làm documentId của yêu cầu chế độ ăn")
            String id,
            String fileName,
            String contentType,
            long sizeBytes,
            Instant uploadedAt
    ) {}
}
