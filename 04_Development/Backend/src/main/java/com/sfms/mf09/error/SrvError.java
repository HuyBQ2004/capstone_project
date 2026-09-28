package com.sfms.mf09.error;

/** Mã lỗi MF09 — nguyên văn `TDS-MF09_Backend.md` §10 (tiền tố `SRV-`). */
public final class SrvError {

    private SrvError() {
    }

    public static final String SESSION_EXPIRED = "SRV-004";
    public static final String ACCESS_DENIED = "SRV-005";
    public static final String NOT_FOUND = "SRV-404";
    public static final String VALIDATION_FAILED = "SRV-001";
    public static final String DUPLICATE_VALUE = "SRV-409";
    public static final String RATE_LIMITED = "SRV-429";

    /** 409 · MSG-021 — BR-130, xác nhận phát lần 2 trong cùng (ngày, bữa). */
    public static final String DUPLICATE_DISTRIBUTION = "SRV-021";
    /** 403 · MSG-129 — BR-094 (MF07), bước kiểm thực FAILED. */
    public static final String INSPECTION_GATE_BLOCKED = "SRV-094";
    /** 422 · MSG-130 — không có thực đơn APPROVED/PUBLISHED cho (ngày, khối, bữa). */
    public static final String NO_MENU = "SRV-022";
    /** 409 · MSG-131 — còn báo lỗi phân phát chưa xử lý (AF1 UC-016). */
    public static final String UNRESOLVED_ERROR_REPORT = "SRV-023";
    /** 409 · MSG-132 — hủy đánh dấu sau ngày phục vụ. */
    public static final String REVOKE_TOO_LATE = "SRV-024";
    /** 409 · MSG-133 — sửa tiêu thụ sau ngày phục vụ (đã vào tóm tắt BR-133). */
    public static final String CONSUMPTION_LOCKED = "SRV-025";
    /**
     * 422 · MSG-142 — BR-065, thêm 05/08/2026 khi vá bug `doServe()` không tra
     * `defaultSubstituteId`: học sinh dị ứng trùng món nhưng món đó KHÔNG có món thay thế cấu
     * hình sẵn — chặn phát, không âm thầm phát món gây dị ứng.
     */
    public static final String ALLERGY_SUBSTITUTE_MISSING = "SRV-026";
    /** 403 · MSG-143 — BR-105 mở rộng (10/08/2026), chưa lưu mẫu đủ mọi món của bữa. */
    public static final String SAMPLE_RETENTION_INCOMPLETE = "SRV-105";
}
