package com.sfms.mf02.error;

/**
 * Mã lỗi MF02 — nguyên văn bảng <b>TDS-MF02_Backend.md §10</b> (tiền tố {@code SCH-}).
 *
 * <p>Mọi mã ở đây ánh xạ 1-1 về một MSG có thật trong {@code error_message.md} (v1.8);
 * nội dung message nằm ở {@link SchMessages}.
 */
public final class SchError {

    /** 500 · MSG-001 — lỗi lưu dữ liệu trường (UC-001/095). */
    public static final String SYSTEM_ERROR = "SCH-001";

    /** 401 · MSG-004 — access token hết hạn (dùng chung hạ tầng auth). */
    public static final String SESSION_EXPIRED = "SCH-004";

    /** 403 · MSG-005 — sai vai trò TRONG CÙNG trường (chốt 2026-07-31, đóng L7). */
    public static final String ACCESS_DENIED = "SCH-005";

    /** 400 · MSG-007 — mã số thuế sai định dạng (UC-070, BR-028). */
    public static final String INVALID_TAX_ID = "SCH-007";

    /** 409 · MSG-010 — capacity > 50 (BR-025). */
    public static final String CAPACITY_EXCEEDED = "SCH-010";

    /** 409 · MSG-011 — GVCN đã chủ nhiệm lớp khác (BR-020). */
    public static final String TEACHER_ALREADY_ASSIGNED = "SCH-011";

    /** 409 · MSG-140 — capacity mới &lt; sĩ số hiện tại (BR-219, chốt 2026-07-31, đóng L8b). */
    public static final String CAPACITY_BELOW_ENROLLED = "SCH-012";

    /** 400 · MSG-107 — tọa độ ngoài dải hợp lệ (BR-028). */
    public static final String INVALID_COORDINATE = "SCH-013";

    /** 403 · MSG-108 — tọa độ đã khóa, không phải System Admin (BR-028). */
    public static final String COORDINATE_LOCKED = "SCH-013";

    /** 429 · MSG-141 — vượt rate limit {@code POST /schools/register} (chốt 2026-07-31). */
    public static final String RATE_LIMITED = "SCH-014";

    /**
     * 409 — BR-010 mở rộng (11/08/2026): System Admin cố phê duyệt (PENDING→ACTIVE) một trường
     * mà tài khoản school-admin tự tạo lúc đăng ký (UC-070) CHƯA xác thực email (chưa bấm link
     * kích hoạt, UC-154). Trước đó có thể duyệt bừa hồ sơ với email không có thật/gõ sai.
     *
     * <p>⚠️ ORACLE GAP — chưa có MSG chính thức trong error_message.md cho tình huống này,
     * cùng cách xử lý gap như {@link #CLASSROOM_READ_ONLY}: dùng message mô tả trực tiếp.
     */
    public static final String EMAIL_NOT_VERIFIED = "SCH-015";

    /** 422 · MSG-109 — grade ∉ [1,12] (BR-024). */
    public static final String INVALID_GRADE = "SCH-024";

    /** 409 · MSG-080 — trường đã có năm học Active (BR-191). */
    public static final String ACADEMIC_YEAR_ALREADY_ACTIVE = "SCH-080";

    /** 409 · MSG-081 — chưa có năm học Active, không tạo được lớp (BR-192). */
    public static final String NO_ACTIVE_ACADEMIC_YEAR = "SCH-081";

    /** 404 · MSG-137 — không tồn tại hoặc cross-school (BR-022, chốt 2026-07-31, đóng L7). */
    public static final String NOT_FOUND = "SCH-404";

    /** 422 · MSG-138 — DTO sai định dạng/giá trị. */
    public static final String VALIDATION_FAILED = "SCH-422";

    /**
     * 409 · MSG-139 — D5: {@code body.id}/{@code code} client gửi trùng, hoặc {@code taxId}
     * trùng (chốt 2026-07-31, đóng L9).
     */
    public static final String DUPLICATE_VALUE = "SCH-409";

    /**
     * 409 — lớp thuộc năm học CLOSED/ARCHIVED, không đổi GVCN/thêm HS (BR-193).
     *
     * <p>⚠️ <b>ORACLE GAP</b> — TDS-MF02 §10 chưa có dòng nào cho tình huống này (chỉ nêu ở
     * §13 TC-INT-003 "409, không assert message"); {@code error_message.md} cũng chưa cấp MSG
     * riêng. Dùng tạm message mô tả trực tiếp cho tới khi Nam bổ sung 1 dòng vào TDS §10 +
     * error_message.md — giống cách {@code IamExceptions.duplicateCode()} đã xử lý gap tương tự.
     */
    public static final String CLASSROOM_READ_ONLY = "SCH-193";

    private SchError() {
    }
}
