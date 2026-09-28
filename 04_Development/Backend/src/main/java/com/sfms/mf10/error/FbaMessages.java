package com.sfms.mf10.error;

/** Thông điệp lỗi MF10 — nguyên văn `error_message.md` (D7). TDS-MF10_Backend.md §10. */
public final class FbaMessages {

    private FbaMessages() {
    }

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";
    /** MSG-022 — BR-139. */
    public static final String MSG_022 = "Feedback submission blocked: Outside the 24-hour limit of meal serving.";
    /** MSG-134 — BR-140. */
    public static final String MSG_134 = "A quality concern tag is required for ratings below 3 stars.";
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";
    /** MSG-135. */
    public static final String MSG_135 = "You have already submitted feedback for this meal.";
    /** MSG-136 — BR-144. */
    public static final String MSG_136 = "A published announcement cannot be reverted to draft.";
    public static final String MSG_137 = "Resource not found.";
    public static final String MSG_139 = "A record with this value already exists.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    // ───────────────────────── Success MSG (§1.10) ─────────────────────────
    public static final String MSG_046 = "Meal feedback submitted successfully. Thank you for your feedback!";
    public static final String MSG_047 = "Announcement published successfully to target audience.";
    public static final String MSG_029 = "Profile updated successfully.";
    /** MSG-057 — thông báo (không phải HTTP error). */
    public static final String MSG_057 = "Negative meal feedback rating (<3 stars) received from parent.";
    /** MSG-058 — thông báo critical tới Medical Staff (BR-142). */
    public static final String MSG_058 = "Illness keywords detected in parent feedback. Potential incident flag.";
}
