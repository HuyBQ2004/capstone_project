package com.sfms.mf03.error;

/** Mã lỗi MF03 — nguyên văn bảng TDS-MF03_Backend.md §10 (tiền tố {@code STU-}). */
public final class StuError {

    public static final String SESSION_EXPIRED = "STU-004";
    public static final String ACCESS_DENIED = "STU-005";
    public static final String DUPLICATE_CODE = "STU-012";
    /** 409 · MSG-142 (chốt 02/08, Q3) — BR-025, KHÔNG mượn MSG-010 của MF02. */
    public static final String CLASSROOM_CAPACITY_EXCEEDED = "STU-010";
    /** 409 · MSG-110 — BR-201. */
    public static final String PARENT_LINK_LIMIT = "STU-201";
    /** 422 · MSG-111 — BR-030. */
    public static final String MISSING_PARENT = "STU-030";
    /** 422 · MSG-112 — BR-031. */
    public static final String SEVERE_REQUIRES_PROOF = "STU-031";
    /** 422 · MSG-113 — reason bắt buộc. */
    public static final String REASON_REQUIRED = "STU-032";
    /** 422 · MSG-144 (chốt 02/08, Q1/BR-220) — dietaryType ngoài danh mục chuẩn. */
    public static final String DIET_NEEDS_JUSTIFICATION = "STU-066";
    /** 400 · MSG-115 — field lạ ở /health-file. */
    public static final String HEALTH_FILE_FIELD_NOT_EDITABLE = "STU-094";
    /** 405 · MSG-116 — BR-172 append-only. */
    public static final String HEALTH_HISTORY_APPEND_ONLY = "STU-172";
    public static final String CLASSROOM_READ_ONLY = "STU-193";
    /** 200 (trong {@code rows[]}, không phải HTTP status) · UC-010 — email PH sai định dạng (BR-012). */
    public static final String INVALID_PARENT_EMAIL = "STU-012B";
    /** 200 (trong {@code rows[]}) · UC-010 — thiếu trường bắt buộc ở một hàng import. */
    public static final String IMPORT_ROW_INVALID = "STU-013";
    /** 409 · UC-010 — đợt import đã {@code COMMITTED}, không commit lại lần 2. */
    public static final String IMPORT_ALREADY_COMMITTED = "STU-014";
    public static final String NOT_FOUND = "STU-404";
    /** 500 · MSG-143 (chốt 02/08, Q5) — KHÔNG dùng MSG-001 (wording "school data"). */
    public static final String SYSTEM_ERROR = "STU-500";
    public static final String VALIDATION_FAILED = "STU-422";
    /** 422 · MSG-008 — {@code POST /students/{code}/health-documents} quá dung lượng. */
    public static final String FILE_TOO_LARGE = "STU-008";
    /** 422 · MSG-009 — {@code POST /students/{code}/health-documents} sai định dạng. */
    public static final String UNSUPPORTED_FILE_TYPE = "STU-009";

    private StuError() {
    }
}
