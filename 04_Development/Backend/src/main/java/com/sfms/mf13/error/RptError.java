package com.sfms.mf13.error;

/** Mã lỗi MF13 — nguyên văn bảng {@code TDS-MF13_Backend.md} §10 (tiền tố {@code RPT-}). */
public final class RptError {

    public static final String SESSION_EXPIRED = "RPT-004";
    public static final String ACCESS_DENIED = "RPT-005";
    /** 404 — {@code slug} ngoài 7 giá trị; {@code RPX-…}/{@code RSC-…} không tồn tại hoặc cross-school. */
    public static final String NOT_FOUND = "RPT-404";
    /** 410 — bản xuất đã purge sau hạn lưu trữ ≥12 tháng (BR-177). */
    public static final String EXPORT_EXPIRED = "RPT-410";
    /** 422 — MSG-138 (BR-178/179/182). */
    public static final String VALIDATION_FAILED = "RPT-138";
    /** 409 — D5, {@code body.id} client gửi đã tồn tại. */
    public static final String ID_CONFLICT = "RPT-409";

    private RptError() {
    }
}
