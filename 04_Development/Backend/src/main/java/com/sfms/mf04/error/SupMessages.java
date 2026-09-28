package com.sfms.mf04.error;

/**
 * Thông điệp MF04 — <b>nguyên văn</b> {@code error_message.md} (D7).
 *
 * <p>Backend luôn trả tiếng Anh; FE tra {@code lib/messages.ts} theo {@code error.code}.
 */
public final class SupMessages {

    // ───────────────────────── Error ─────────────────────────

    public static final String MSG_006 = "Registration failed: The email address is already in use.";
    public static final String MSG_007 = "Invalid tax ID format. Please verify and try again.";
    public static final String MSG_008 = "File upload failed: File size exceeds the maximum limit of 10MB.";
    public static final String MSG_009 =
            "File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.";
    public static final String MSG_013 = "Supplier food safety certificate has expired. Onboarding blocked.";
    public static final String MSG_014 = "Face recognition failed: Biometric identity could not be verified.";
    public static final String MSG_025 = "Database transaction failed. Rollback executed.";
    public static final String MSG_113 = "A reason is required for this action.";
    public static final String MSG_117 = "This national ID is already registered for another delivery person.";
    public static final String MSG_118 =
            "An open application already exists, or this document has already been submitted.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    // ───────────────────────── Success / Warning ─────────────────────────

    public static final String MSG_028 = "Registration request submitted! A verification email has been sent.";
    public static final String MSG_029 = "Profile updated successfully.";
    public static final String MSG_031 = "User account created successfully.";
    public static final String MSG_037 = "Supplier onboarding application approved successfully.";
    public static final String MSG_038 = "Delivery staff face biometrics registered successfully.";

    /** Placeholder {@code [Number]} render số ngày còn lại trước khi expiry (BR-047). */
    public static final String MSG_053_TEMPLATE = "Supplier food safety certificate is expiring in %d days.";

    private SupMessages() {
    }
}
