package com.sfms.mf07.dto;

/**
 * D10-BIS — ảnh bằng chứng là tài liệu RIÊNG TƯ: JSON chỉ trả {@code id} tài liệu, tải xuống
 * qua endpoint có {@code @PreAuthorize} + audit (TDS §9.2 hàng 30), KHÔNG trả URL công khai.
 */
public final class EvidenceDtos {

    private EvidenceDtos() {
    }

    public record UploadEvidenceRequest(String ownerType, String ownerId, String gps) {}

    public record EvidenceResponse(String id, String fileName, long sizeBytes, String uploadedAt) {}

    /**
     * {@code GET /inspections/{code}/evidences} (10/08/2026) — ảnh gom theo (phiên, loại
     * bước): {@code ownerType} = SESSION | STEP1_FRESH | STEP1_DRY | STEP2 | STEP3, KHÔNG
     * phân biệt được từng dòng trong bước (nhiều dòng cùng bước dùng chung 1 "kho ảnh" —
     * xem cảnh báo trong `EvidenceServiceImpl.listForSession`).
     */
    public record EvidenceSummary(String id, String ownerType, String fileName, String uploadedAt) {}

    /**
     * {@code GET /kitchen-inspections/{code}/evidences} (13/08/2026) — KHÁC {@link EvidenceSummary}:
     * ảnh kiểm tra bếp gắn theo TỪNG hạng mục checklist thật (`ownerId` = {@code ChecklistItemResult
     * .resultId}, xem {@code KitchenInspectionServiceImpl.uploadItemEvidence}), nên trả kèm được
     * {@code itemCode} — trước đây bỏ phí liên kết đã lưu sẵn, chỉ trả danh sách phẳng cả phiên
     * (màn xem lại không gắn được ảnh vào đúng hạng mục Không đạt).
     */
    public record KitchenEvidenceSummary(String id, String itemCode, String fileName, String uploadedAt) {}
}
