package com.sfms.mf04.domain;

/**
 * Vòng đời hồ sơ đối tác (BR-041 + ADR-M04-6). Riêng của MF04 — không thuộc 8 enum
 * dùng chung D6.
 *
 * <p>{@code SUSPENDED} mở rộng so với câu chữ gốc của BR-041 (chỉ nêu Pending/Approved/
 * Rejected) nhưng có ràng buộc C5 của {@code data_model.puml} và tiền lệ mock chống lưng
 * (TDS-MF04 ADR-M04-6, §2.6 P6).
 */
public enum PartnerStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}
