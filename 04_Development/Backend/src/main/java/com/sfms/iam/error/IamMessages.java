package com.sfms.iam.error;

/**
 * Thông điệp MF01 — <b>nguyên văn</b> {@code error_message.md} v1.5 (D7).
 *
 * <p>Backend luôn trả tiếng Anh; FE tra {@code lib/messages.ts} theo {@code error.code}
 * để hiện nhãn tiếng Việt. Không dùng {@code Accept-Language} ở MVP.
 *
 * <p>Hai placeholder được render tại chỗ ném lỗi, KHÔNG hardcode:
 * <ul>
 *   <li>{@code [Minutes]} ← {@code SecurityPolicy.lockoutMinutes} (TestSpec §2 L13)</li>
 *   <li>{@code [Number]} ← {@code countByRoleId}</li>
 * </ul>
 */
public final class IamMessages {

    public static final String MSG_002 = "Invalid credentials. Please check your email and password.";
    public static final String MSG_003 = "Your account has been deactivated. Please contact your administrator.";
    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";
    public static final String MSG_006 = "Registration failed: The email address is already in use.";
    public static final String MSG_008 = "File upload failed: File size exceeds the maximum limit of 10MB.";
    public static final String MSG_009 = "File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.";

    /** {@code [Minutes]} ← {@code SecurityPolicy.lockoutMinutes}. Dùng {@link #lockedFor(int)}. */
    public static final String MSG_076_TEMPLATE =
            "Account temporarily locked due to too many failed login attempts. "
            + "Please try again after [Minutes] minutes.";

    public static final String MSG_102 = "Google account not linked to any SFMS account. "
            + "Please contact your administrator to create your account first.";
    public static final String MSG_103 = "Google authentication failed. Please try again.";

    public static final String MSG_104 = "Password does not meet the security policy. "
            + "It must contain uppercase, lowercase, a number, and a special character.";
    public static final String MSG_105 = "You cannot reuse any of your last 3 passwords. "
            + "Please choose a different one.";

    /** {@code [Number]} ← số user còn gán role. Dùng {@link #roleInUse(long)}. */
    public static final String MSG_106_TEMPLATE =
            "Cannot delete this role: [Number] user(s) are still assigned to it. "
            + "Please reassign them first.";

    // ───────────────────────── Success ─────────────────────────

    /** {@code [Name]} ← tên người dùng. Dùng {@link #loginSuccess(String)}. */
    public static final String MSG_026_TEMPLATE = "Login successful. Welcome back, [Name]!";
    public static final String MSG_027 = "Logout successful. Session terminated safely.";
    public static final String MSG_028 = "Registration request submitted! A verification email has been sent.";
    /** ⚠️ CF-6: dùng cho UC-002/003 theo traceability dù ngữ nghĩa là hồ sơ. */
    public static final String MSG_029 = "Profile updated successfully.";
    public static final String MSG_030 = "Password changed successfully.";
    public static final String MSG_031 = "User account created successfully.";

    public static String lockedFor(int lockoutMinutes) {
        return MSG_076_TEMPLATE.replace("[Minutes]", String.valueOf(lockoutMinutes));
    }

    public static String roleInUse(long assignedUsers) {
        return MSG_106_TEMPLATE.replace("[Number]", String.valueOf(assignedUsers));
    }

    public static String loginSuccess(String name) {
        return MSG_026_TEMPLATE.replace("[Name]", name == null ? "" : name);
    }

    private IamMessages() {
    }
}
