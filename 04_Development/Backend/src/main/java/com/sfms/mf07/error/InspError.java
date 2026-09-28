package com.sfms.mf07.error;

/** Mã lỗi MF07 — nguyên văn `TDS-MF07_Backend.md` §10 (tiền tố `INSP-`). */
public final class InspError {

    private InspError() {
    }

    public static final String SESSION_EXPIRED = "INSP-004";
    public static final String ACCESS_DENIED = "INSP-005";
    public static final String NOT_FOUND = "INSP-404";
    public static final String VALIDATION_FAILED = "INSP-001";
    public static final String DUPLICATE_VALUE = "INSP-409";
    public static final String RATE_LIMITED = "INSP-429";

    /** 422 · MSG-016 — BR-095, BR-207. */
    public static final String CHECKLIST_INCOMPLETE = "INSP-016";
    /** 422 · MSG-017 — BR-090, coreTemp &lt; 75 ở Bước 2. */
    public static final String CORE_TEMP_LOW = "INSP-017";
    /** 422 · MSG-145 — BR-091, holdingTemp &lt; 60 ở Bước 3. */
    public static final String HOLDING_TEMP_LOW = "INSP-091";
    /** 422 · MSG-146 — BR-092, Bước 1 dùng lô quá HSD. */
    public static final String BATCH_EXPIRED = "INSP-092";
    /** 409 · MSG-147 — BR-079, nộp bước khi bước liền trước chưa PASSED. */
    public static final String STEP_LOCKED = "INSP-079";
    /** 409 · MSG-129 — BR-094, MF09 hỏi serving-gate khi phiên FAILED. */
    public static final String SERVING_BLOCKED = "INSP-094";
    /** 409 · MSG-129 — BR-098, co-sign khi còn bước chưa PASSED. */
    public static final String CO_SIGN_BLOCKED = "INSP-098";
    /** 422 · MSG-148 — BR-083, BR-207, thiếu ảnh bằng chứng cho mục "Không đạt". */
    public static final String EVIDENCE_REQUIRED = "INSP-083";
    /** 422 · MSG-149 — BR-083 mở rộng, GPS nộp bước nằm ngoài geofence trường. */
    public static final String OUTSIDE_GEOFENCE = "INSP-149";
}
