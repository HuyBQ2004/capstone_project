package com.sfms.mf02.error;

/**
 * Thông điệp MF02 — <b>nguyên văn</b> {@code error_message.md} v1.8 (D7).
 *
 * <p>Backend luôn trả tiếng Anh; FE tra {@code lib/messages.ts} theo {@code error.code}.
 */
public final class SchMessages {

    public static final String MSG_001 = "System error while saving school data. Please try again.";
    public static final String MSG_007 = "Invalid tax ID format. Please verify and try again.";
    public static final String MSG_010 = "Classroom capacity exceeded. Maximum allowed is 50 students.";
    public static final String MSG_011 = "Homeroom teacher is already assigned to another classroom.";
    public static final String MSG_080 = "Cannot create Active academic year: an Active academic year "
            + "already exists for this school. Please close it first.";
    public static final String MSG_081 = "Cannot create classroom: this school has no Active academic "
            + "year. Please activate an academic year first.";
    public static final String MSG_107 = "Invalid GPS coordinates. Latitude must be between -90 and 90, "
            + "longitude between -180 and 180.";
    public static final String MSG_108 = "School coordinates are locked. Only a System Administrator "
            + "can change them.";
    public static final String MSG_109 = "Grade level must be between 1 and 12.";
    public static final String MSG_140 = "Classroom capacity cannot be reduced below the current "
            + "number of enrolled students.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    // ───────────────────────── Success ─────────────────────────

    public static final String MSG_029 = "Profile updated successfully.";
    public static final String MSG_032 = "School registered successfully. Pending System Admin approval.";
    public static final String MSG_033 = "Classroom created successfully.";
    public static final String MSG_034 = "Homeroom teacher assigned successfully.";
    public static final String MSG_082 = "Academic year created successfully.";
    public static final String MSG_083 = "Academic year status updated successfully.";
    public static final String MSG_084 = "Students promoted to next academic year successfully.";

    private SchMessages() {
    }
}
