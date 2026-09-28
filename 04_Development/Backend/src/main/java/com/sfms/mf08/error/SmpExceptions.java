package com.sfms.mf08.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF08 — mỗi phương thức khóa cứng (code, HTTP status, message)
 * đúng §10.
 */
public final class SmpExceptions {

    private SmpExceptions() {
    }

    public static ApiException notFound() {
        return new ApiException(SmpError.NOT_FOUND, HttpStatus.NOT_FOUND, SmpMessages.MSG_137);
    }

    public static ApiException accessDenied() {
        return new ApiException(SmpError.ACCESS_DENIED, HttpStatus.FORBIDDEN, SmpMessages.MSG_005);
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(SmpError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT, SmpMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    /** 409 · D5 — client gửi id/code trùng ở POST. */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(SmpError.DUPLICATE_VALUE, HttpStatus.CONFLICT, SmpMessages.MSG_139,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /** 422 · BR-203 — khối lượng dưới ngưỡng 100g/150ml. */
    public static ApiException amountTooLow() {
        return new ApiException(SmpError.AMOUNT_TOO_LOW, HttpStatus.UNPROCESSABLE_CONTENT, SmpMessages.MSG_019);
    }

    /** 409 · BR-105 — phiên kiểm thực chưa PASSED 3/3. */
    public static ApiException inspectionNotPassed() {
        return new ApiException(SmpError.INSPECTION_NOT_PASSED, HttpStatus.CONFLICT, SmpMessages.MSG_119);
    }

    /** 422 · BR-107 — thiếu ảnh bằng chứng. */
    public static ApiException evidenceRequired() {
        return new ApiException(SmpError.EVIDENCE_REQUIRED, HttpStatus.UNPROCESSABLE_CONTENT, SmpMessages.MSG_120);
    }

    /** 422 · BR-109 — GPS ngoài geofence. */
    public static ApiException outsideGeofence() {
        return new ApiException(SmpError.OUTSIDE_GEOFENCE, HttpStatus.UNPROCESSABLE_CONTENT, SmpMessages.MSG_121);
    }

    /**
     * 409 · BR-111 — chưa hết hạn lưu tối thiểu. {@code hoursLeft} thay placeholder
     * {@code [Number]}.
     */
    public static ApiException retentionNotElapsed(long hoursLeft) {
        String message = SmpMessages.MSG_122.replace("[Number]", String.valueOf(Math.max(0, hoursLeft)));
        return new ApiException(SmpError.RETENTION_NOT_ELAPSED, HttpStatus.CONFLICT, message);
    }

    /** 409 · BR-112 — mẫu đang khóa hủy (liên kết sự cố mở). */
    public static ApiException disposalLocked(String incidentRef) {
        String message = SmpMessages.MSG_123.replace("[Incident ID]", incidentRef == null ? "" : incidentRef);
        return new ApiException(SmpError.DISPOSAL_LOCKED, HttpStatus.CONFLICT, message);
    }

    /** 409 · BR-113 — mẫu không còn RETAINED khi bàn giao. */
    public static ApiException notRetained(String sampleCode) {
        String message = SmpMessages.MSG_124.replace("[Sample Code]", sampleCode == null ? "" : sampleCode);
        return new ApiException(SmpError.NOT_RETAINED, HttpStatus.CONFLICT, message);
    }

    /** 422 · BR-112 — thiếu field bắt buộc khi hủy mẫu. */
    public static ApiException disposalIncomplete() {
        return new ApiException(SmpError.DISPOSAL_INCOMPLETE, HttpStatus.UNPROCESSABLE_CONTENT, SmpMessages.MSG_125);
    }

    /** 422 · BR-203 — dụng cụ chưa tiệt trùng/niêm phong. */
    public static ApiException containerStateInvalid() {
        return new ApiException(SmpError.CONTAINER_STATE_INVALID, HttpStatus.UNPROCESSABLE_CONTENT,
                SmpMessages.MSG_126);
    }

    /** 422 · BR-212 — thiếu chỉ tiêu kiểm nghiệm hoặc biên bản giao nhận. */
    public static ApiException handoverEvidenceMissing() {
        return new ApiException(SmpError.HANDOVER_EVIDENCE_MISSING, HttpStatus.UNPROCESSABLE_CONTENT,
                SmpMessages.MSG_127);
    }

    /**
     * 409 · BR-114 — sự cố không tồn tại/đã đóng (CF-M08-6, MSG-128 KHÔNG mượn
     * MSG-023).
     */
    public static ApiException incidentInvalid() {
        return new ApiException(SmpError.INCIDENT_INVALID, HttpStatus.CONFLICT, SmpMessages.MSG_128);
    }

    public static ApiException rateLimited() {
        return new ApiException(SmpError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, SmpMessages.MSG_141);
    }
}
