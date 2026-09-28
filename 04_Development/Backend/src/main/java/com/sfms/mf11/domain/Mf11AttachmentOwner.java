package com.sfms.mf11.domain;

/** Loại chủ sở hữu của {@link Mf11Attachment} — 1 bảng dùng chung cho mọi ảnh bằng chứng mf11. */
public enum Mf11AttachmentOwner {
    SOURCE_REPORT,
    CORRECTIVE_ACTION,
    /**
     * Ảnh bằng chứng của CHÍNH sự cố (V58, 08/08/2026) — trước đó không có chỗ lưu nên màn
     * "Bổ sung bằng chứng" của Y tế chỉ gửi được một CON SỐ, hồ sơ ghi "N tệp đính kèm" mà
     * không tệp nào tồn tại.
     */
    INCIDENT_EVIDENCE,
    /**
     * Tệp "Kết quả xét nghiệm" của phiên điều tra (10/08/2026) — trước đó nút tải lên chỉ
     * tăng một biến đếm cục bộ ở FE, không gọi API nào, `investigationLabResultFiles` trên
     * `Incident` không bao giờ khớp số tệp thật vì không tệp nào được lưu.
     */
    LAB_RESULT
}
