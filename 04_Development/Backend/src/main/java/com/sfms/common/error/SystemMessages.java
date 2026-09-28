package com.sfms.common.error;

/**
 * Thông điệp hệ thống <b>dùng chung mọi module</b> — nguyên văn {@code error_message.md} v1.5 (D7).
 *
 * <p>Chỉ chứa các MSG mà {@code error_message.md} ghi rõ "Dùng chung mọi module".
 * MSG riêng của MF01 nằm ở {@code com.sfms.iam.error.IamMessages}.
 */
public final class SystemMessages {

    /** MSG-137 — tài nguyên không tồn tại hoặc nằm ngoài phạm vi dữ liệu được phép đọc. */
    public static final String MSG_137 = "Resource not found.";

    /** MSG-138 — DTO sai định dạng, sai giá trị enum, hoặc thiếu field bắt buộc. */
    public static final String MSG_138 =
            "Validation failed: one or more required fields are missing or invalid.";

    private SystemMessages() {
    }
}
