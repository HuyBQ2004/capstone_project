package com.sfms.mf11.error;

/** Mã lỗi MF11 — nguyên văn `TDS-MF11_Backend.md` §10 (tiền tố `IEM-`). */
public final class IemError {

    private IemError() {
    }

    public static final String SESSION_EXPIRED = "IEM-004";
    public static final String ACCESS_DENIED = "IEM-005";
    public static final String NOT_FOUND = "IEM-404";
    public static final String VALIDATION_FAILED = "IEM-001";
    public static final String RATE_LIMITED = "IEM-429";

    /** 409 · MSG-149 — ADR-M11-1, gộp RPT không ở PENDING_TRIAGE. */
    public static final String RECORD_SOURCE_NOT_PENDING = "IEM-114";
    /** 400 · MSG-150 — BR-147, severity ngoài enum / chưa chọn mức. */
    public static final String SEVERITY_INVALID = "IEM-147";
    /** 400 · MSG-151 — BR-149, thiếu lý do cách ly. */
    public static final String QUARANTINE_REASON_REQUIRED = "IEM-149";
    /** 409 · MSG-152 — BR-217, cách ly lại lô đã cách ly. */
    public static final String ALREADY_QUARANTINED = "IEM-149B";
    /** 409 · MSG-153 — BR-155 + C1, gate đóng sự cố. */
    public static final String CLOSE_BLOCKED = "IEM-155";
    /** 409 · MSG-154 — BR-216, sai vòng đời CAPA / đã có CAPA cho sự cố này. */
    public static final String CAPA_INVALID_TRANSITION = "IEM-216";
    /** 409 · MSG-155 — BR-217, còn lô cách ly chưa xử lý xong. */
    public static final String QUARANTINE_NOT_RESOLVED = "IEM-217";
    /** 409 · MSG-156 — BR-211, chưa xuất PDF mà đã tải bản ký. */
    public static final String PDF_NOT_EXPORTED = "IEM-211";
    /** 403 · MSG-157 — BR-186, Medical Staff cố duyệt vi phạm ≥ MEDIUM. */
    public static final String VIOLATION_APPROVAL_FORBIDDEN = "IEM-186";
    /** 409 · MSG-158 — ADR-M11-8, hồ sơ đã xuất báo cáo chính thức. */
    public static final String READ_ONLY_REPORTED = "IEM-RO";
    /** 409 · MSG-095 (dùng chung MF07/MF11) — BR-211, thiếu ảnh biên bản đã ký. */
    public static final String SIGNED_RECORD_MISSING = "IEM-095";
}
