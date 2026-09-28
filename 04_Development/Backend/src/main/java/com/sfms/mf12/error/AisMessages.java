package com.sfms.mf12.error;

/** Nội dung message MF12 — nguyên văn {@code TDS-MF12_Backend.md} §10 (D7: tiếng Anh). */
public final class AisMessages {

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 =
            "Access denied: You do not have permission to access this resource.";
    public static final String MSG_073 = "Gemini AI Service is currently unavailable. Please try again later.";
    public static final String MSG_157 =
            "Unable to archive the AI conversation - the operation has been cancelled.";
    public static final String MSG_163_FAILED = "Connection test failed.";
    public static final String MSG_163_NOT_TESTED =
            "You must successfully test the connection before saving this configuration.";
    public static final String MSG_164 = "Unable to decrypt the external service credential.";
    public static final String MSG_092 =
            "No kitchen inspection data available for the selected period. Cannot generate compliance report.";
    public static final String MSG_097 =
            "No violations recorded for the selected period. Cannot generate violation summary report.";
    public static final String MSG_096 =
            "No incidents recorded for the selected period. Cannot generate incident summary report.";
    public static final String MSG_188 =
            "Incident summary reports were deprecated on 2026-07-26 and can no longer be created.";
    public static final String MSG_094 =
            "This checklist item/group has been used in saved inspection records. Deleting it will not "
                    + "remove historical data - it will only be hidden from new inspections. Continue?";
    public static final String MSG_162 = "Audit log entries are immutable and cannot be edited or deleted.";

    public static final String MSG_091 = "Compliance report sent successfully.";
    public static final String MSG_099 = "Violation summary report sent successfully.";
    public static final String MSG_029 = "Security policy updated successfully.";

    private AisMessages() {
    }
}
