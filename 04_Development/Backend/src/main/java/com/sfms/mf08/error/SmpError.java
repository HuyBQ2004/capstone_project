package com.sfms.mf08.error;

/** Mã lỗi MF08 — nguyên văn `TDS-MF08_Backend.md` §10 (tiền tố `SMP-`). */
public final class SmpError {

    private SmpError() {
    }

    public static final String SESSION_EXPIRED = "SMP-004";
    public static final String ACCESS_DENIED = "SMP-005";
    public static final String NOT_FOUND = "SMP-404";
    public static final String VALIDATION_FAILED = "SMP-422";
    public static final String DUPLICATE_VALUE = "SMP-409";
    public static final String RATE_LIMITED = "SMP-429";

    /** 422 · MSG-019 — BR-203, khối lượng dưới ngưỡng. */
    public static final String AMOUNT_TOO_LOW = "SMP-019";
    /** 200 (cảnh báo mềm) · MSG-020 — BR-110, nhiệt độ ngoài [2,8]. */
    public static final String TEMP_OUT_OF_RANGE = "SMP-020";
    /** 409 · MSG-128 — BR-114, sự cố không tồn tại/đã đóng (CF-M08-6). */
    public static final String INCIDENT_INVALID = "SMP-023";
    /** 409 · MSG-119 — BR-105, phiên kiểm thực chưa PASSED 3/3. */
    public static final String INSPECTION_NOT_PASSED = "SMP-105";
    /** 422 · MSG-120 — BR-107, thiếu ảnh bằng chứng. */
    public static final String EVIDENCE_REQUIRED = "SMP-107";
    /** 422 · MSG-121 — BR-109, GPS ngoài geofence. */
    public static final String OUTSIDE_GEOFENCE = "SMP-109";
    /** 409 · MSG-122 — BR-111, hủy trước hạn lưu 24h. */
    public static final String RETENTION_NOT_ELAPSED = "SMP-111";
    /** 409 · MSG-123 — BR-112, mẫu đang khóa hủy (sự cố mở). */
    public static final String DISPOSAL_LOCKED = "SMP-112";
    /** 409 · MSG-124 — BR-113, mẫu không còn RETAINED khi bàn giao. */
    public static final String NOT_RETAINED = "SMP-113";
    /** 422 · MSG-125 — BR-112, thiếu field bắt buộc khi hủy (cột 9/10/12). */
    public static final String DISPOSAL_INCOMPLETE = "SMP-030";
    /** 422 · MSG-126 — BR-203, dụng cụ chưa tiệt trùng/niêm phong. */
    public static final String CONTAINER_STATE_INVALID = "SMP-203";
    /** 422 · MSG-127 — BR-212, thiếu chỉ tiêu kiểm nghiệm / biên bản giao nhận. */
    public static final String HANDOVER_EVIDENCE_MISSING = "SMP-212";
}
