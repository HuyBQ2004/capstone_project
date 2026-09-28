package com.sfms.mf11.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/** Factory lỗi MF11 — mỗi phương thức khoá cứng (code, HTTP status, message) đúng §10. */
public final class IemExceptions {

    private IemExceptions() {
    }

    public static ApiException notFound() {
        return new ApiException(IemError.NOT_FOUND, HttpStatus.NOT_FOUND, IemMessages.MSG_137);
    }

    public static ApiException accessDenied() {
        return new ApiException(IemError.ACCESS_DENIED, HttpStatus.FORBIDDEN, IemMessages.MSG_005);
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(IemError.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, IemMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    public static ApiException rateLimited() {
        return new ApiException(IemError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, IemMessages.MSG_141);
    }

    /** 409 · ADR-M11-1 — gộp RPT không ở PENDING_TRIAGE. */
    public static ApiException recordSourceNotPending() {
        return new ApiException(IemError.RECORD_SOURCE_NOT_PENDING, HttpStatus.CONFLICT, IemMessages.MSG_149);
    }

    /** 400 · BR-147 — severity ngoài enum hoặc thiếu. */
    public static ApiException severityInvalid() {
        return new ApiException(IemError.SEVERITY_INVALID, HttpStatus.BAD_REQUEST, IemMessages.MSG_150);
    }

    /** 400 · BR-149 — thiếu lý do cách ly. */
    public static ApiException quarantineReasonRequired() {
        return new ApiException(IemError.QUARANTINE_REASON_REQUIRED, HttpStatus.BAD_REQUEST, IemMessages.MSG_151);
    }

    /** 409 · BR-217/ADR-M11-3 — cách ly lại lô đã cách ly. {@code batchCode}/{@code at} thay placeholder. */
    public static ApiException alreadyQuarantined(String batchCode, String at) {
        String message = IemMessages.MSG_152.replace("[Batch Code]", batchCode == null ? "" : batchCode)
                .replace("[Timestamp]", at == null ? "" : at);
        return new ApiException(IemError.ALREADY_QUARANTINED, HttpStatus.CONFLICT, message);
    }

    /** 409 · BR-155/C1 — gate đóng sự cố. Hai số động đặt trong {@code error.details} (D2). */
    public static ApiException closeBlocked(int capaOpen, int studentsNotRecovered) {
        return new ApiException(IemError.CLOSE_BLOCKED, HttpStatus.CONFLICT, IemMessages.MSG_153,
                List.of(new ErrorResponse.Detail("capaOpen", capaOpen),
                        new ErrorResponse.Detail("studentsNotRecovered", studentsNotRecovered)));
    }

    /** 409 · BR-216 — sai vòng đời CAPA hoặc sự cố đã có CAPA. */
    public static ApiException capaInvalidTransition(String incidentCode, String capaCode) {
        String message = IemMessages.MSG_154.replace("[Incident Code]", incidentCode == null ? "" : incidentCode)
                .replace("[CAPA Code]", capaCode == null ? "" : capaCode);
        return new ApiException(IemError.CAPA_INVALID_TRANSITION, HttpStatus.CONFLICT, message);
    }

    /** 409 · BR-217 — còn lô cách ly chưa xử lý xong (đóng sự cố). */
    public static ApiException quarantineNotResolved() {
        return new ApiException(IemError.QUARANTINE_NOT_RESOLVED, HttpStatus.CONFLICT, IemMessages.MSG_155);
    }

    /** 409 · BR-211 — chưa xuất PDF mà đã tải bản ký. */
    public static ApiException pdfNotExported() {
        return new ApiException(IemError.PDF_NOT_EXPORTED, HttpStatus.CONFLICT, IemMessages.MSG_156);
    }

    /** 403 · BR-186 — Medical Staff cố duyệt biên bản ≥ MEDIUM. */
    public static ApiException violationApprovalForbidden() {
        return new ApiException(IemError.VIOLATION_APPROVAL_FORBIDDEN, HttpStatus.FORBIDDEN, IemMessages.MSG_157);
    }

    /** 409 · ADR-M11-8 — hồ sơ đã xuất báo cáo chính thức, chuyển read-only. */
    public static ApiException readOnlyReported() {
        return new ApiException(IemError.READ_ONLY_REPORTED, HttpStatus.CONFLICT, IemMessages.MSG_158);
    }

    /** 409 · BR-211/MSG-095 — thiếu ảnh biên bản đã ký khi đóng/gửi duyệt. */
    public static ApiException signedRecordMissing() {
        return new ApiException(IemError.SIGNED_RECORD_MISSING, HttpStatus.CONFLICT,
                "Cannot close/submit this violation resolution: the signed 3-party record photo has not "
                + "been uploaded yet. Please print, collect signatures, and upload the signed record first.");
    }

    /** 400 · thiếu trường bắt buộc khi gửi RPT / thiếu nội dung biên bản (BR-146). */
    public static ApiException incompleteReport() {
        return new ApiException(IemError.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, IemMessages.MSG_023);
    }

    /** 400 · lập biên bản vi phạm thiếu trường bắt buộc (BR-183/BR-211). */
    public static ApiException incompleteViolation() {
        return new ApiException(IemError.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, IemMessages.MSG_085);
    }

    /** 400 · lý do bắt buộc (reject/rework), field và message tuỳ chỗ gọi. */
    public static ApiException reasonRequired(String field) {
        return new ApiException(IemError.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, IemMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, "reason is required")));
    }
}
