package com.sfms.mf07.error;

/** Thông điệp lỗi MF07 — nguyên văn `error_message.md` v1.10 (D7). TDS-MF07_Backend.md §10. */
public final class InspMessages {

    private InspMessages() {
    }

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";
    public static final String MSG_137 = "Resource not found.";
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";
    public static final String MSG_139 = "A record with this value already exists.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    /** MSG-016 — BR-095/BR-207. */
    public static final String MSG_016 = "Checklist validation failed: Mandatory safety checks are missing.";
    /** MSG-017 — BR-090. */
    public static final String MSG_017 = "Cooking temperature too low: Meal core temperature must reach 75°C.";
    /** MSG-145 — BR-091. */
    public static final String MSG_145 = "Holding temperature is below the required minimum of 60°C.";
    /** MSG-146 — BR-092. Placeholder {@code [Batch Code]}. */
    public static final String MSG_146 = "Ingredient batch [Batch Code] has expired and cannot be used in this inspection.";
    /** MSG-147 — BR-079. Placeholder {@code [Step Name]}. */
    public static final String MSG_147 = "Step [Step Name] is locked until the previous step has passed.";
    /** MSG-129 — BR-094/BR-098. Placeholder {@code [Step Name]}. */
    public static final String MSG_129 = "Meal cannot be served: inspection step [Step Name] did not pass.";
    /** MSG-148 — BR-083/BR-207. */
    public static final String MSG_148 = "An evidence photo is required for every item marked as not compliant.";
    /** MSG-056 — BR-096/BR-098. Placeholder {@code [Step Name]}. */
    public static final String MSG_056 = "Re-inspection required: Failed safety check during inspection step [Step Name].";
    /** MSG-043 — nộp checklist kiểm tra ATTP thành công (UC-052/065/014). */
    public static final String MSG_043 = "Food safety inspection checklist submitted successfully.";
    /** MSG-093 — điểm bếp &lt; 60% (UC-014, không gắn BR). */
    public static final String MSG_093 = "Kitchen safety score below 60% threshold. Alert sent to School "
            + "Administration — kitchen operations continue as normal.";
    /** MSG-149 — BR-083 mở rộng, GPS nộp bước nằm ngoài geofence trường. */
    public static final String MSG_149 = "Submitted location is outside the school's configured geofence radius.";
}
