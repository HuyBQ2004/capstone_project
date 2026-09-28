package com.sfms.mf06.error;

/**
 * Chuỗi thông điệp tiếng Anh nguyên văn TDS-MF06_Backend.md §10 (D7).
 *
 * <p>MSG-145..MSG-153 là 9 mã MỚI (M06-D10) — TDS §2.5 mục treo #1: nội dung đã chốt nguyên văn
 * ở §10 nhưng CHƯA được commit vào {@code error_message.md} v2.0 (chờ Huy + Nam ký). Dùng
 * ngay chuỗi đã chốt ở đây — KHÔNG chờ, theo hướng dẫn "test assert code + status + message
 * khác rỗng/tiếng Anh" trong lúc chờ (§2.5 #1); một khi commit xong, hằng số này chỉ cần đối
 * chiếu lại (không đổi giá trị vì đã lấy nguyên văn từ TDS §10).
 */
public final class IngMessages {

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";
    public static final String MSG_008 = "File upload failed: File size exceeds the maximum limit of 10MB.";
    public static final String MSG_009 = "File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.";
    public static final String MSG_015 = "Receiving action blocked: GPS location verification failed (out of tolerance).";
    public static final String MSG_016 = "Checklist validation failed: Mandatory safety checks are missing.";
    public static final String MSG_018 = "Inventory shortage: Insufficient stock of ingredient [Ingredient Name].";

    /** 🆕 MSG-145 — ING-070. */
    public static final String MSG_145 = "Receiving blocked: supplier is not in Approved status.";
    /** 🆕 MSG-146 — ING-072. */
    public static final String MSG_146 = "Evidence photos are required to complete a receiving session.";
    /** 🆕 MSG-147 — ING-076. */
    public static final String MSG_147 = "Ingredient cannot be resolved in the catalog — traceability would be broken.";
    public static final String MSG_154 = "Ingredient is not in the school-approved contract catalog for this supplier.";
    /** 🆕 MSG-148 — ING-078. */
    public static final String MSG_148 = "Rejected batch requires a rejection reason and at least one evidence photo.";
    /** 🆕 MSG-149 — ING-071. */
    public static final String MSG_149 = "Batch certification is incomplete or dates are invalid.";
    /** 🆕 MSG-150 — ING-202. */
    public static final String MSG_150 = "Storage zone does not match the required temperature zone of this ingredient.";
    /** 🆕 MSG-151 — ING-204. */
    public static final String MSG_151 = "Manual stock adjustment requires a reason code and manager sign-off.";
    /** 🆕 MSG-152 — ING-205. */
    public static final String MSG_152 = "Batch is expired and locked for issuance.";
    /** 🆕 MSG-153 — ING-149. */
    public static final String MSG_153 = "Batch is under quarantine and cannot be issued or adjusted.";
    /** 🆕 MSG-155 — ING-094 (10/08/2026, BR-094). Placeholder {@code [Step Name]}. */
    public static final String MSG_155 = "Meal cannot be delivered to class: inspection step [Step Name] did not pass.";
    /** 🆕 MSG-156 — ING-105 (10/08/2026, BR-105 mở rộng). Placeholder {@code [Dishes]}. */
    public static final String MSG_156 = "Meal cannot be delivered to class: food sample retention is missing for [Dishes].";
    /** 🆕 MSG-157 — ING-073 (11/08/2026, BR-070 mở rộng). */
    public static final String MSG_157 = "Receiving blocked: the purchase request has not been approved by the supplier yet.";

    public static final String MSG_014 = "Face recognition failed: Biometric identity could not be verified.";
    public static final String MSG_013 = "Supplier food safety certificate has expired. Onboarding blocked.";
    public static final String MSG_139 = "Conflicting state / duplicate issue slip.";
    public static final String MSG_138_APPEND_ONLY = "Inventory transactions are append-only.";
    public static final String MSG_137 = "Resource not found.";

    public static final String MSG_041 = "Ingredient delivery received. Inventory levels updated.";
    public static final String MSG_042 = "Inventory transaction recorded and stock adjusted.";
    public static final String MSG_029 = "Profile updated successfully.";
    public static final String MSG_065 = "Delivery delay alert: Supplier delivery time deviates from schedule.";

    private IngMessages() {
    }
}
