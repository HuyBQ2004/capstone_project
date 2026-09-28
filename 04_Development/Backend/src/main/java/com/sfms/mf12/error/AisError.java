package com.sfms.mf12.error;

/**
 * Mã lỗi MF12 — nguyên văn bảng {@code TDS-MF12_Backend.md} §10 (tiền tố {@code AIS-},
 * `M12-D10`). Hai resource của MF01 ({@code audit-logs}, {@code security-policies}) giữ
 * tiền tố {@code IAM-} — {@code ModuleCodeResolver} tự resolve, KHÔNG dùng hằng ở đây cho
 * chúng (xem {@link #AUDIT_APPEND_ONLY}/{@link #SECURITY_POLICY_OUT_OF_RANGE} — hằng cục bộ
 * vì {@code IamError} không khai 2 mã đặc thù nghiệp vụ này).
 */
public final class AisError {

    public static final String SESSION_EXPIRED = "AIS-004";
    public static final String ACCESS_DENIED = "AIS-005";

    /** 503 · MSG-073 — AI timeout 10s / lỗi kết nối (UC-012 AF1). */
    public static final String AI_UNAVAILABLE = "AIS-073";
    /** 500 — ghi `ai_requests` thất bại → rollback (BR-157). */
    public static final String AI_ARCHIVE_FAILED = "AIS-157";
    /** 422/409 — test connection FAIL, hoặc save khi chưa test (BR-163). */
    public static final String INTEGRATION_NOT_TESTED = "AIS-163";
    /** 500 — sai key_version/khoá ENV (BR-164). */
    public static final String CREDENTIAL_DECRYPT_FAILED = "AIS-164";
    /** 422 · MSG-092 — UC-185 kỳ rỗng, chặn gửi (BR-209). */
    public static final String NO_COMPLIANCE_DATA = "AIS-092";
    /** 422 · MSG-097 — UC-189 kỳ rỗng, chặn gửi (BR-214). */
    public static final String NO_VIOLATION_DATA = "AIS-097";
    /** 422 · MSG-096 — UC-188 kỳ rỗng (giá trị lịch sử, CF-4). */
    public static final String NO_INCIDENT_DATA = "AIS-096";
    /** 409 — UC-188/BR-213 Deprecated 26/07/2026. */
    public static final String INCIDENT_REPORT_DEPRECATED = "AIS-188";
    /** 409 · MSG-094 — xoá checklist item/group đã tham chiếu, chưa `confirmed=true` (BR-210). */
    public static final String CHECKLIST_REFERENCED = "AIS-094";

    /** 405 — {@code audit-logs} thuộc MF01, `ModuleCodeResolver` tự gán {@code IAM} (M12-D10). */
    public static final String AUDIT_APPEND_ONLY = "IAM-162";
    /** 422 — {@code security-policies} thuộc MF01, giữ tiền tố {@code IAM} (M12-D10). */
    public static final String SECURITY_POLICY_OUT_OF_RANGE = "IAM-184";

    private AisError() {
    }
}
