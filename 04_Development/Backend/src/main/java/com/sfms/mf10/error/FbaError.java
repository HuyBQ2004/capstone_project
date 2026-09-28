package com.sfms.mf10.error;

/** Mã lỗi MF10 — nguyên văn `TDS-MF10_Backend.md` §10 (tiền tố `FBA-`). */
public final class FbaError {

    private FbaError() {
    }

    public static final String SESSION_EXPIRED = "FBA-004";
    public static final String ACCESS_DENIED = "FBA-005";
    /** 409 · MSG-022 · BR-139 — ngoài cửa sổ 24h. */
    public static final String WINDOW_CLOSED = "FBA-022";
    /** 422 · MSG-134 · BR-140 — thiếu tag khi rating<3. */
    public static final String LOW_RATING_NEEDS_TAG = "FBA-140";
    /** 422 · MSG-138 — validate chung. */
    public static final String VALIDATION_FAILED = "FBA-REQ";
    /** 409 · MSG-135 — trùng phản hồi cùng (người, phiên ăn, món). */
    public static final String DUPLICATE_FEEDBACK = "FBA-DUP";
    /** 409 · MSG-136 · BR-144 — PUBLISHED không được lùi về DRAFT. */
    public static final String INVALID_STATE = "FBA-STATE";
    public static final String NOT_FOUND = "FBA-404";
    /** 409 · MSG-139 · D5 — client gửi id trùng. */
    public static final String DUPLICATE_VALUE = "FBA-409";
    /** 422 · MSG-138 — enum ngoài D6 (type/recipientRoles/mealType). */
    public static final String ENUM_INVALID = "FBA-ENUM";
    /** 415 · MSG-138 — loại file ngoài whitelist. */
    public static final String UNSUPPORTED_MEDIA = "FBA-MEDIA";
    /** 413 · MSG-138 — file > 5 MB. */
    public static final String ATTACHMENT_TOO_LARGE = "FBA-SIZE";
    public static final String RATE_LIMITED = "FBA-429";
}
