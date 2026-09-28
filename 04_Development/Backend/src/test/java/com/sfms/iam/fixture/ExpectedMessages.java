package com.sfms.iam.fixture;

/**
 * Thông điệp kỳ vọng — <b>nguyên văn</b> {@code error_message.md} v1.5, chép từ bảng
 * "Bảng thông điệp kỳ vọng" của TestSpec-MF01 §4.
 *
 * <p><b>Cố tình tách khỏi {@code com.sfms.iam.error.IamMessages} của production.</b> Test phải
 * có oracle độc lập: nếu dùng chung hằng thì đổi sai chuỗi ở production, test vẫn xanh — đúng
 * nghĩa tautology (AP-AI-002).
 *
 * <p>D7 đòi <b>nguyên văn</b> ⇒ mọi assertion dùng {@code equalTo(...)}, KHÔNG {@code contains(...)}.
 */
public final class ExpectedMessages {

    // ───────────────────────── Error ─────────────────────────

    /** MSG-002 · IAM-002 · 401 */
    public static final String MSG_002 = "Invalid credentials. Please check your email and password.";

    /** MSG-003 · IAM-003 · 403 */
    public static final String MSG_003 = "Your account has been deactivated. Please contact your administrator.";

    /** MSG-004 · IAM-005 / IAM-008 · 401 */
    public static final String MSG_004 = "Session expired. Please log in again.";

    /** MSG-005 · IAM-004 · 403 */
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";

    /** MSG-006 · IAM-006 · 409 */
    public static final String MSG_006 = "Registration failed: The email address is already in use.";

    /** MSG-008 · IAM-010 · 422 */
    public static final String MSG_008 = "File upload failed: File size exceeds the maximum limit of 10MB.";

    /** MSG-009 · IAM-010 · 422 */
    public static final String MSG_009 = "File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.";

    /** MSG-102 · IAM-102 · 403 */
    public static final String MSG_102 = "Google account not linked to any SFMS account. "
            + "Please contact your administrator to create your account first.";

    /** MSG-103 · IAM-103 · 401 */
    public static final String MSG_103 = "Google authentication failed. Please try again.";

    /** MSG-104 · IAM-006policy · 422 */
    public static final String MSG_104 = "Password does not meet the security policy. "
            + "It must contain uppercase, lowercase, a number, and a special character.";

    /** MSG-105 · IAM-006reuse · 422 — TestSpec §2 L1/L2 */
    public static final String MSG_105 = "You cannot reuse any of your last 3 passwords. "
            + "Please choose a different one.";

    /** MSG-137 · IAM-404 · 404 — chốt 2026-07-31 (TDS §10) */
    public static final String MSG_137 = "Resource not found.";

    /** MSG-138 · IAM-422 · 422 — chốt 2026-07-31 (TDS §10) */
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";

    // ───────────────────────── Success ─────────────────────────

    public static final String MSG_027 = "Logout successful. Session terminated safely.";
    /** ⚠️ CF-6 — dùng cho UC-002/003 theo traceability dù ngữ nghĩa là hồ sơ. */
    public static final String MSG_029 = "Profile updated successfully.";
    public static final String MSG_030 = "Password changed successfully.";
    public static final String MSG_031 = "User account created successfully.";

    // ───────────────────────── Placeholder ─────────────────────────

    /**
     * MSG-076 · IAM-007 · 423 — {@code [Minutes]} ← {@code SecurityPolicy.lockoutMinutes}
     * (TestSpec §2 <b>L13</b>: KHÔNG hardcode 15).
     */
    public static String msg076(int lockoutMinutes) {
        return "Account temporarily locked due to too many failed login attempts. "
                + "Please try again after " + lockoutMinutes + " minutes.";
    }

    /** MSG-106 · IAM-009 · 409 — {@code [Number]} ← {@code countByRoleId}. */
    public static String msg106(long assignedUsers) {
        return "Cannot delete this role: " + assignedUsers
                + " user(s) are still assigned to it. Please reassign them first.";
    }

    // ───────────────────────── Hai mã CỐ Ý không dùng làm expected ─────────────────────────

    /**
     * MSG-063 — <b>Warning</b> ("Weak password rating. Recommend…"), không chặn được thao tác.
     * TestSpec §2 <b>L1</b>: KHÔNG dùng làm expected của bất kỳ TC chặn nào; thay bằng MSG-104.
     * Khai ở đây để test có thể assert <b>không</b> xuất hiện.
     */
    public static final String MSG_063_NEVER_EXPECTED =
            "Weak password rating. Recommend adding special characters and numbers.";

    /**
     * MSG-062 — không có BR/UC nào định nghĩa "thiết bị lạ" ⇒ <b>thiếu oracle hoàn toàn</b>,
     * không viết TC (TestSpec §9.5 mục 1).
     */
    public static final String MSG_062_NO_ORACLE =
            "Suspicious login attempt detected from a new location/device. Verification code required.";

    private ExpectedMessages() {
    }
}
