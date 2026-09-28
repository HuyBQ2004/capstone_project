package com.sfms.mf11.error;

/**
 * Thông điệp lỗi MF11 — nguyên văn `error_message.md` v1.11 (D7). TDS-MF11_Backend.md §10.1/§10.2.
 *
 * <p>MSG-149…158 cấp cho 10 mã cục bộ IEM-* (04/08/2026, Huy). MSG-159/160/161 thay MSG-029
 * dùng sai ngữ nghĩa ở UC-005/009/013 (H5/CF-M11-6).
 */
public final class IemMessages {

    private IemMessages() {
    }

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 = "Access denied: You do not have permission to access this resource.";
    public static final String MSG_137 = "Resource not found.";
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    public static final String MSG_149 =
            "An incident record can only be created from a source report that is pending triage.";
    public static final String MSG_150 = "Incident severity must be one of LOW, MEDIUM or HIGH.";
    public static final String MSG_151 = "A quarantine reason is required before a batch can be quarantined.";
    /** Placeholder [Batch Code]/[Timestamp] — thay bằng {@code String.replace}. */
    public static final String MSG_152 =
            "Batch [Batch Code] was already quarantined at [Timestamp]. This action cannot be undone.";
    /** Số động ({@code capaOpen}/{@code studentsNotRecovered}) đặt trong {@code error.details} (D2). */
    public static final String MSG_153 =
            "This incident cannot be closed: [Number] corrective actions are still open and [Number] "
            + "affected students have not been confirmed as recovered.";
    public static final String MSG_154 =
            "Invalid corrective action transition, or incident [Incident Code] already has "
            + "corrective action [CAPA Code].";
    public static final String MSG_155 = "This incident cannot be closed: quarantined batches have not "
            + "reached a final state.";
    public static final String MSG_156 =
            "Export and print the violation record PDF for signature before uploading the signed copy.";
    public static final String MSG_157 = "Violations rated MEDIUM or HIGH must be approved by School "
            + "Administration.";
    public static final String MSG_158 = "This incident is read-only: the official report has already "
            + "been submitted.";

    /** UC-009 — thay MSG-029 (H5/CF-M11-6). */
    public static final String MSG_159 = "Incident severity assigned successfully. Investigation is now in progress.";
    /** UC-005 — thay MSG-029/MSG-043. */
    public static final String MSG_160 =
            "Corrective action plan approved. The assigned unit can now begin implementation.";
    /** UC-013 — thay MSG-029/MSG-046. */
    public static final String MSG_161 = "Incident closed successfully. The case record is now read-only.";

    /** MSG-048 — UC-073/074 gửi RPT, UC-114 mở phiên điều tra. */
    public static final String MSG_048 =
            "Suspected food safety incident report logged. Investigation map generated.";
    public static final String MSG_100 = "Incident record draft saved successfully.";
    public static final String MSG_101 = "Incident record finalized successfully. Ready for review.";
    public static final String MSG_049 = "Corrective actions plan submitted successfully for review.";
    public static final String MSG_086 = "Violation record saved successfully.";
    public static final String MSG_087 = "Violation resolution submitted for approval.";
    public static final String MSG_089 = "Violation resolution approved. Disciplinary action applied.";
    /** MSG-088 — BR-187, warning KHÔNG chặn. */
    public static final String MSG_088 = "Possible repeat violation detected: staff name matches a prior "
            + "violation within the lookback window. Manual verification required.";
    /** MSG-023 — thiếu trường bắt buộc khi gửi RPT / thiếu nội dung biên bản. */
    public static final String MSG_023 =
            "Incident report incomplete: Symptoms, date of ingestion, and dishes are required.";
    /** MSG-085 — lập biên bản vi phạm thiếu trường bắt buộc. */
    public static final String MSG_085 =
            "Violation record submission blocked: required fields (description, evidence photo) are missing.";
}
