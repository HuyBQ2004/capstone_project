package com.sfms.mf08.error;

/**
 * Thông điệp lỗi MF08 — nguyên văn `error_message.md` v1.10 (D7). TDS-MF08_Backend.md §10.
 *
 * <p>M08-D10: 9 mã dẫn xuất BR đã có MSG chính thức từ v1.5 (29/07/2026) — MSG-119..128 —
 * KHÔNG phải "mã cục bộ TDS" như bản v1.0 từng ghi nhầm.
 */
public final class SmpMessages {

    private SmpMessages() {
    }

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";
    public static final String MSG_137 = "Resource not found.";
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";
    public static final String MSG_139 = "A record with this value already exists.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    /** MSG-019 — BR-203. */
    public static final String MSG_019 = "Food sample retention weight too low: Must be at least 100g.";
    /** MSG-020 — BR-110, cảnh báo mềm (không chặn). */
    public static final String MSG_020 = "Retention refrigerator temperature is out of safe range (2-8°C).";
    /** MSG-044 — thành công, UC-069. */
    public static final String MSG_044 = "Food sample retention registered. Cold storage timer initiated.";
    /** MSG-119 — BR-105. */
    public static final String MSG_119 =
            "Food sample retention is blocked: the meal batch has not passed all 3 inspection steps yet.";
    /** MSG-120 — BR-107. */
    public static final String MSG_120 = "Evidence photo is required for food sample retention.";
    /** MSG-121 — BR-109 (CF-M08-5, KHÔNG dùng MSG-015 của MF06/MF07). */
    public static final String MSG_121 = "Sample retention location is outside the designated kitchen/storage area.";
    /** MSG-122 — BR-111. */
    public static final String MSG_122 = "Minimum retention period of 24 hours has not elapsed. [Number] hour(s) remaining.";
    /** MSG-123 — BR-112. */
    public static final String MSG_123 = "This sample is locked: it is linked to an open food safety incident [Incident ID].";
    /** MSG-124 — BR-113. */
    public static final String MSG_124 = "Sample [Sample Code] is no longer in Retained status and cannot be handed over again.";
    /** MSG-125 — BR-112. */
    public static final String MSG_125 = "Disposal record is incomplete: disposer, witness, and quality note are required.";
    /** MSG-126 — BR-203. */
    public static final String MSG_126 = "Sample container must be sealed, sanitized, and individually labeled per dish.";
    /** MSG-127 — BR-212. */
    public static final String MSG_127 = "Test criteria or the sample handover record is missing.";
    /** MSG-128 — BR-114 (thay việc mượn MSG-023, đóng CF-M08-6). */
    public static final String MSG_128 = "Incident ID does not exist or the incident is already closed.";
}
