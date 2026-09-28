package com.sfms.mf04.error;

/**
 * Mã lỗi MF04 — nguyên văn bảng {@code TDS-MF04_Backend.md §10} (tiền tố {@code SUP-}).
 *
 * <p>⚠️ <b>NOT_FOUND</b> ({@code SUP-404}) và <b>VALIDATION_FAILED</b> ({@code SUP-422})
 * được sinh <b>tự động</b> bởi {@code common/error/GlobalExceptionHandler} +
 * {@code ModuleCodeResolver} (đăng ký prefix {@code SUP} cho 5 resource MF04) — không cần
 * ném {@link com.sfms.common.error.ApiException} tay cho hai trường hợp này ở service.
 *
 * <p>⚠️ <b>Lệch có chủ đích so với TDS §10</b>: TDS ghi số hiệu validation là {@code SUP-138}
 * (khớp số MSG-138) và không nêu rõ nguyên tắc đặt số cho lỗi 404. Tiền lệ <b>code thật</b> đã
 * merge của MF02/MF03 ({@code SchError.VALIDATION_FAILED = "SCH-422"},
 * {@code StuError.VALIDATION_FAILED = "STU-422"}) đặt số hiệu theo <b>HTTP status</b>
 * (404/422), nhất quán cho cả not-found lẫn validation. Theo thứ tự ưu tiên đã chốt ở
 * {@code 00_Common_Conventions.md} (<i>"code thật ở 04_Development/Backend/ > file quy ước
 * chung > ADR module trong TDS"</i>), MF04 đi theo tiền lệ code thật: {@code SUP-422} thay vì
 * {@code SUP-138}. Nội dung {@code message} (MSG-138, nguyên văn) và hành vi (422) không đổi —
 * chỉ số hiệu mã lỗi khác so với câu chữ TDS.
 */
public final class SupError {

    /** 401 · MSG-004 — access token hết hạn. Lưu ý: handler dùng chung luôn trả {@code IAM-004}. */
    public static final String SESSION_EXPIRED = "SUP-004";

    /** 403 · MSG-005 — sai vai trò TRONG ĐÚNG phạm vi dữ liệu (M04-D10). Tự động qua prefix SUP. */
    public static final String ACCESS_DENIED = "SUP-005";

    /** 409 · MSG-006 — email đã tồn tại (UC-071/067/049, BR-012). */
    public static final String EMAIL_ALREADY_USED = "SUP-006";

    /** 422 · MSG-007 — mã số thuế sai định dạng (UC-152/156/158/170; ⚠️ ngoài §1.4, CF-M04-2). */
    public static final String INVALID_TAX_ID = "SUP-007";

    /** 413 · MSG-008 — file vượt 10MB (tài liệu) / 5MB (ảnh khuôn mặt). */
    public static final String FILE_TOO_LARGE = "SUP-008";

    /** 415 · MSG-009 — sai định dạng file (whitelist PDF/JPG/PNG, kiểm magic bytes). */
    public static final String UNSUPPORTED_FILE_TYPE = "SUP-009";

    /** 409 · MSG-013 — chứng nhận ATTP hết hạn, chặn duyệt/nộp đơn/upload (⚠️ ngoài §1.4). */
    public static final String CERT_EXPIRED = "SUP-013";

    /** 502 · MSG-014 — FRS lỗi/ảnh không đạt (UC-068 AF1, UC-028 AF2; ⚠️ ngoài §1.4). */
    public static final String FACE_RECOGNITION_FAILED = "SUP-014";

    /** 500 · MSG-025 — transaction duyệt/đình chỉ thất bại (⚠️ ngoài §1.4). */
    public static final String SYSTEM_ERROR = "SUP-025";

    /** 422 · MSG-113 — thiếu lý do khi từ chối/đình chỉ (BR-048, ADR-M04-6). */
    public static final String REASON_REQUIRED = "SUP-048";

    /** 409 · MSG-117 — CCCD trùng (BR-050, UC-019 AF1). */
    public static final String NATIONAL_ID_DUPLICATE = "SUP-050";

    /** 409 · MSG-118 — đơn đang mở, hoặc trùng (partner, type, number) (UC-152/156, UC-101 AF3). */
    public static final String BUSINESS_CONFLICT = "SUP-409";

    /** 404 · MSG-137 — không tồn tại HOẶC ngoài phạm vi dữ liệu (M04-D10). Tự động qua prefix SUP. */
    public static final String NOT_FOUND = "SUP-404";

    /** 422 · MSG-138 — Bean Validation chung. Tự động qua prefix SUP (xem Javadoc lớp). */
    public static final String VALIDATION_FAILED = "SUP-422";

    /** 409 · MSG-139 — D5: client gửi {@code id} trùng (KHÁC {@code SUP-409}, M04-D06). */
    public static final String DUPLICATE_VALUE = "SUP-139";

    /** 429 · MSG-141 — vượt rate limit (đăng ký Guest 5/min/IP; face 10/min). */
    public static final String RATE_LIMITED = "SUP-141";

    /**
     * 409 — BR-010 mở rộng (11/08/2026): trường/System Admin cố duyệt (PENDING→APPROVED) một
     * đối tác mà tài khoản do họ tự tạo lúc đăng ký (UC-152/156/158/170) CHƯA xác thực email
     * (chưa bấm link kích hoạt, UC-154). Cùng chốt và cùng cách xử lý gap như
     * {@code SchError.EMAIL_NOT_VERIFIED}.
     *
     * <p>⚠️ ORACLE GAP — chưa có MSG chính thức trong error_message.md cho tình huống này.
     */
    public static final String EMAIL_NOT_VERIFIED = "SUP-015";

    private SupError() {
    }
}
