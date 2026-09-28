package com.sfms.mf09.error;

/**
 * Thông điệp lỗi MF09 — nguyên văn `error_message.md` v1.8 (D7). TDS-MF09_Backend.md §10.
 *
 * <p>M09-D09: 5 mã dẫn xuất BR đã có MSG chính thức từ v1.5 — MSG-129..133 — KHÔNG phải
 * "mã cục bộ TDS" như bản v1.0 từng ghi nhầm.
 */
public final class SrvMessages {

    private SrvMessages() {
    }

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";
    public static final String MSG_137 = "Resource not found.";
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";
    public static final String MSG_139 = "A record with this identifier already exists.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    /** MSG-021 — BR-130. */
    public static final String MSG_021 = "Duplicate meal distribution: Student has already scanned for this session.";
    /** MSG-129 — BR-094 (MF07). Placeholder [Step Name] thay bằng tên bước thất bại. */
    public static final String MSG_129 = "Meal cannot be served: inspection step [Step Name] did not pass.";
    /** MSG-130 — ràng buộc C3. Placeholder [Meal]/[Date]/[Grade]. */
    public static final String MSG_130 =
            "No approved or published menu found for [Meal] on [Date] for grade [Grade]. "
            + "Meal distribution cannot be recorded.";
    /** MSG-131 — AF1 UC-016. */
    public static final String MSG_131 =
            "Distribution completion is blocked: an unresolved distribution error was reported.";
    /** MSG-132 — ADR-M09-3. */
    public static final String MSG_132 = "Cannot revoke a distribution that was confirmed on a previous day.";
    /** MSG-133 — BR-121/BR-133. */
    public static final String MSG_133 = "Consumption record is locked after the serving day.";
    /** MSG-142 — BR-065, thêm 05/08/2026 (xem `SrvError.ALLERGY_SUBSTITUTE_MISSING`). Placeholder [Dish Name]. */
    public static final String MSG_142 =
            "Meal cannot be served: student has a verified allergy conflict with [Dish Name] and no "
            + "substitute dish is configured. Manual intervention required (BR-065).";

    /** MSG-045 — thành công, UC-016/047 (trả kèm response 200 của /serve và /confirm). */
    public static final String MSG_045 = "Meal distribution list confirmed for class [Class Name].";
    /** 🆕 MSG-143 — BR-105 mở rộng (10/08/2026, `SrvError.SAMPLE_RETENTION_INCOMPLETE`). Placeholder [Dishes]. */
    public static final String MSG_143 = "Meal cannot be served: food sample retention is missing for [Dishes].";
}
