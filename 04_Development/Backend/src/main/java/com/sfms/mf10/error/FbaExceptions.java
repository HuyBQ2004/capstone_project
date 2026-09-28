package com.sfms.mf10.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF10 — mỗi phương thức khóa cứng (code, HTTP status, message)
 * đúng §10.
 */
public final class FbaExceptions {

    private FbaExceptions() {
    }

    public static ApiException notFound() {
        return new ApiException(FbaError.NOT_FOUND, HttpStatus.NOT_FOUND, FbaMessages.MSG_137);
    }

    public static ApiException accessDenied() {
        return new ApiException(FbaError.ACCESS_DENIED, HttpStatus.FORBIDDEN, FbaMessages.MSG_005);
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(FbaError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT, FbaMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    /** 409 · BR-139 — ngoài cửa sổ 24h (POST/PATCH/DELETE feedback). */
    public static ApiException windowClosed() {
        return new ApiException(FbaError.WINDOW_CLOSED, HttpStatus.CONFLICT, FbaMessages.MSG_022);
    }

    /** 422 · BR-140 — rating<3 mà tags rỗng (Parent). */
    public static ApiException lowRatingNeedsTag() {
        return new ApiException(FbaError.LOW_RATING_NEEDS_TAG, HttpStatus.UNPROCESSABLE_CONTENT, FbaMessages.MSG_134,
                List.of(new ErrorResponse.Detail("tags", "must not be empty when rating < 3")));
    }

    /** 409 — trùng phản hồi cho cùng (người, phiên ăn, món, học sinh). */
    public static ApiException duplicateFeedback() {
        return new ApiException(FbaError.DUPLICATE_FEEDBACK, HttpStatus.CONFLICT, FbaMessages.MSG_135);
    }

    /** 409 · BR-144 — PUBLISHED → DRAFT không hợp lệ. */
    public static ApiException invalidState() {
        return new ApiException(FbaError.INVALID_STATE, HttpStatus.CONFLICT, FbaMessages.MSG_136);
    }

    /** 409 · D5 — client gửi id trùng. */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(FbaError.DUPLICATE_VALUE, HttpStatus.CONFLICT, FbaMessages.MSG_139,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /** 422 · M10-D07/D08/D09 — enum ngoài D6. */
    public static ApiException enumInvalid(String field, String reason) {
        return new ApiException(FbaError.ENUM_INVALID, HttpStatus.UNPROCESSABLE_CONTENT, FbaMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    /** 415 · M10-D11 — loại file ngoài whitelist / magic bytes không khớp. */
    public static ApiException unsupportedMedia() {
        return new ApiException(FbaError.UNSUPPORTED_MEDIA, HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported attachment type.");
    }

    /** 413 · M10-D11 — file > 5 MB. */
    public static ApiException attachmentTooLarge() {
        return new ApiException(FbaError.ATTACHMENT_TOO_LARGE, HttpStatus.CONTENT_TOO_LARGE,
                "Attachment exceeds the 5 MB limit.");
    }

    public static ApiException rateLimited() {
        return new ApiException(FbaError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, FbaMessages.MSG_141);
    }
}
