package com.sfms.iam.error;

/**
 * Mã lỗi MF01 — nguyên văn bảng <b>TDS-MF01 §10</b> (tiền tố {@code IAM-}).
 *
 * <p>Mọi mã ở đây ánh xạ 1-1 về một MSG có thật trong {@code error_message.md} v1.5;
 * nội dung message nằm ở {@link IamMessages}.
 */
public final class IamError {

    /** 401 · MSG-002 — sai thông tin đăng nhập / sai MK hiện tại. */
    public static final String INVALID_CREDENTIALS = "IAM-002";

    /** 403 · MSG-003 — tài khoản INACTIVE/SUSPENDED/chưa xác minh email (BR-010). */
    public static final String ACCOUNT_DEACTIVATED = "IAM-003";

    /** 403 · MSG-005 — sai RBAC; xóa role hệ thống; deactivate primary admin (BR-007/008). */
    public static final String ACCESS_DENIED = "IAM-004";

    /** 401 · MSG-004 — access token hết hạn/không hợp lệ (BR-004). */
    public static final String SESSION_EXPIRED = "IAM-005";

    /** 409 · MSG-006 — email trùng khi tạo user/staff (BR-012). */
    public static final String EMAIL_ALREADY_USED = "IAM-006";

    /** 422 · MSG-104 — MK không đạt chính sách bảo mật (BR-006). */
    public static final String PASSWORD_POLICY = "IAM-006policy";

    /**
     * 422 · MSG-105 — MK trùng 1 trong 3 MK gần nhất (BR-006 thân mục, dẫn BR-015).
     *
     * <p>⚠️ TestSpec §2 L1/L2: KHÔNG dùng {@code IAM-006policy}/MSG-063 cho tình huống này.
     * MSG-063 là <i>Warning</i> ("recommend"), không chặn được thao tác.
     */
    public static final String PASSWORD_REUSE = "IAM-006reuse";

    /** 423 · MSG-076 — khóa tài khoản sau N lần sai (BR-002). */
    public static final String ACCOUNT_LOCKED = "IAM-007";

    /** 401 · MSG-004 — refresh/reset/activation token hết hạn hoặc đã dùng (BR-003/004/011). */
    public static final String TOKEN_INVALID = "IAM-008";

    /** 409 · MSG-106 — xóa role còn user gán, kể cả user đã vô hiệu hóa (BR-008). */
    public static final String ROLE_IN_USE = "IAM-009";

    /** 422 · MSG-008 / MSG-009 — upload ảnh hồ sơ sai dung lượng/định dạng (UC-097). */
    public static final String FILE_UPLOAD = "IAM-010";

    /** 403 · MSG-102 — email Google chưa có tài khoản SFMS (BR-218, KHÔNG tự tạo). */
    public static final String GOOGLE_NOT_LINKED = "IAM-102";

    /** 401 · MSG-103 — OAuth2 token exchange thất bại / bị từ chối (BR-218). */
    public static final String GOOGLE_AUTH_FAILED = "IAM-103";

    /**
     * 404 · MSG-137 — tài nguyên không tồn tại hoặc ngoài phạm vi được phép đọc.
     * <p>Bổ sung 2026-07-31 (TDS §10, đóng TestSpec §2 L15).
     */
    public static final String NOT_FOUND = "IAM-404";

    /**
     * 422 · MSG-138 — DTO thiếu/sai field, sai giá trị enum, sai định dạng ngày.
     * <p>Bổ sung 2026-07-31 (TDS §10, đóng TestSpec §2 L15).
     */
    public static final String VALIDATION_FAILED = "IAM-422";

    /**
     * 409 — {@code body.id} client gửi đã tồn tại (D5: <i>"trùng thì 409 với mã
     * {@code <MODULE>-409}"</i>). Dùng cho {@code POST /users}, {@code POST /staff},
     * {@code POST /roles} và cả trường hợp tên role trùng (UC-098 bước 3).
     */
    public static final String DUPLICATE_CODE = "IAM-409";

    private IamError() {
    }
}
