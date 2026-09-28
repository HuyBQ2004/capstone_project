package com.sfms.mf03.error;

/** Nội dung message MF03 — nguyên văn {@code error_message.md} (D7: tiếng Anh, không dịch). */
public final class StuMessages {

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 =
            "Access denied: You do not have permission to access this resource.";
    public static final String MSG_012 = "Student admission code is invalid or has already been used.";
    /** MSG-142 — Q3 (chốt 02/08): thay MSG-010 của MF02, không có số trong message. */
    public static final String MSG_142 = "This classroom has reached its capacity.";
    public static final String MSG_110 =
            "This parent account is already linked to 5 students. School Administration "
                    + "verification is required.";
    public static final String MSG_111 = "Each student must be linked to at least one parent account.";
    public static final String MSG_112 = "Severe allergy declaration requires supporting medical documentation.";
    public static final String MSG_113 = "A reason is required for this action.";
    /** MSG-144 — Q1/BR-220 (chốt 02/08). MSG-114 NGỪNG DÙNG. */
    public static final String MSG_144 =
            "This dietary type is outside the standard list and requires medical justification "
                    + "before approval.";
    public static final String MSG_115 = "This field cannot be edited through this endpoint.";
    public static final String MSG_116 = "Health history is append-only and cannot be modified or deleted.";
    public static final String MSG_137 = "Resource not found.";
    /** MSG-143 — Q5 (chốt 02/08): KHÔNG dùng MSG-001 ("school data", thuộc MF02). */
    public static final String MSG_143 = "System error while saving student data. Please try again.";
    public static final String MSG_029 = "Profile updated successfully.";
    public static final String MSG_031 = "User account created successfully.";
    public static final String MSG_035 = "Student profiles and parent links imported successfully.";
    public static final String MSG_036 = "Allergy profile updated successfully and alerts set.";
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";
    /** UC-010 — nội dung {@code rows[].message} khi email PH sai định dạng (BR-012), khớp mẫu TDS §9.3. */
    public static final String MSG_PARENT_EMAIL_INVALID = "Parent email format is invalid.";
    /** UC-010 — {@code rows[].message} khi hàng thiếu trường bắt buộc. */
    public static final String MSG_ROW_MISSING_FIELD = "Row is missing one or more required fields.";
    /** UC-010 — commit lần 2 vào đợt import đã {@code COMMITTED}. */
    public static final String MSG_IMPORT_ALREADY_COMMITTED = "This import batch has already been committed.";
    /** MSG-008 — nguyên văn giống {@code IamMessages.MSG_008} (D7: cùng một message toàn hệ thống). */
    public static final String MSG_008 = "File upload failed: File size exceeds the maximum limit of 10MB.";
    /** MSG-009 — nguyên văn giống {@code IamMessages.MSG_009}. */
    public static final String MSG_009 =
            "File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.";

    private StuMessages() {
    }
}
