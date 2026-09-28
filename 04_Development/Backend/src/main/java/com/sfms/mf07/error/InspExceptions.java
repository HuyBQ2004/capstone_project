package com.sfms.mf07.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF07 — mỗi phương thức khóa cứng (code, HTTP status, message)
 * đúng §10.
 */
public final class InspExceptions {

    private InspExceptions() {
    }

    public static ApiException notFound() {
        return new ApiException(InspError.NOT_FOUND, HttpStatus.NOT_FOUND, InspMessages.MSG_137);
    }

    public static ApiException accessDenied() {
        return new ApiException(InspError.ACCESS_DENIED, HttpStatus.FORBIDDEN, InspMessages.MSG_005);
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(InspError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT, InspMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    /**
     * 409 · D5 — client gửi id/code trùng ở POST, hoặc trùng {@code ux_insp_slot}
     * (BR-079).
     */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(InspError.DUPLICATE_VALUE, HttpStatus.CONFLICT, InspMessages.MSG_139,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    public static ApiException rateLimited() {
        return new ApiException(InspError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, InspMessages.MSG_141);
    }

    public static ApiException checklistIncomplete() {
        return new ApiException(InspError.CHECKLIST_INCOMPLETE, HttpStatus.UNPROCESSABLE_CONTENT, InspMessages.MSG_016);
    }

    public static ApiException coreTempLow() {
        return new ApiException(InspError.CORE_TEMP_LOW, HttpStatus.UNPROCESSABLE_CONTENT, InspMessages.MSG_017);
    }

    public static ApiException holdingTempLow() {
        return new ApiException(InspError.HOLDING_TEMP_LOW, HttpStatus.UNPROCESSABLE_CONTENT, InspMessages.MSG_145);
    }

    public static ApiException batchExpired(String batchCode) {
        String message = InspMessages.MSG_146.replace("[Batch Code]", batchCode);
        return new ApiException(InspError.BATCH_EXPIRED, HttpStatus.UNPROCESSABLE_CONTENT, message,
                List.of(new ErrorResponse.Detail("batchId", batchCode)));
    }

    public static ApiException stepLocked(String stepName) {
        String message = InspMessages.MSG_147.replace("[Step Name]", stepName);
        return new ApiException(InspError.STEP_LOCKED, HttpStatus.CONFLICT, message);
    }

    public static ApiException servingBlocked(String stepName) {
        String message = InspMessages.MSG_129.replace("[Step Name]", stepName);
        return new ApiException(InspError.SERVING_BLOCKED, HttpStatus.CONFLICT, message);
    }

    public static ApiException coSignBlocked(String stepName) {
        String message = InspMessages.MSG_129.replace("[Step Name]", stepName);
        return new ApiException(InspError.CO_SIGN_BLOCKED, HttpStatus.CONFLICT, message);
    }

    public static ApiException evidenceRequired() {
        return new ApiException(InspError.EVIDENCE_REQUIRED, HttpStatus.UNPROCESSABLE_CONTENT, InspMessages.MSG_148);
    }

    public static ApiException outsideGeofence() {
        return new ApiException(InspError.OUTSIDE_GEOFENCE, HttpStatus.UNPROCESSABLE_CONTENT, InspMessages.MSG_149);
    }
}
