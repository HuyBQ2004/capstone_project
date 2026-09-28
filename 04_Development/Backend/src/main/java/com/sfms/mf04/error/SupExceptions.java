package com.sfms.mf04.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import com.sfms.common.error.SystemMessages;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory cho toàn bộ lỗi MF04 — mỗi phương thức khóa cứng cặp (code, HTTP
 * status, message
 * nguyên văn) đúng như {@code TDS-MF04_Backend.md §10} (trừ số hiệu 404/422,
 * xem {@link SupError}).
 */
public final class SupExceptions {

    /** 409 · SUP-006 · MSG-006 — BR-012: email đã tồn tại. */
    public static ApiException emailAlreadyUsed() {
        return new ApiException(SupError.EMAIL_ALREADY_USED, HttpStatus.CONFLICT, SupMessages.MSG_006);
    }

    /** 422 · SUP-007 · MSG-007 — taxId sai định dạng (⚠️ ngoài §1.4, CF-M04-2). */
    public static ApiException invalidTaxId() {
        return new ApiException(SupError.INVALID_TAX_ID, HttpStatus.UNPROCESSABLE_CONTENT, SupMessages.MSG_007);
    }

    /** 413 · SUP-008 · MSG-008 — file vượt dung lượng cho phép. */
    public static ApiException fileTooLarge() {
        return new ApiException(SupError.FILE_TOO_LARGE, HttpStatus.CONTENT_TOO_LARGE, SupMessages.MSG_008);
    }

    /** 415 · SUP-009 · MSG-009 — sai định dạng/magic bytes file. */
    public static ApiException unsupportedFileType() {
        return new ApiException(SupError.UNSUPPORTED_FILE_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                SupMessages.MSG_009);
    }

    /**
     * 409 · SUP-013 · MSG-013 — BR-046/048: chứng nhận ATTP hết hạn, chặn
     * onboarding/upload.
     */
    public static ApiException certExpired() {
        return new ApiException(SupError.CERT_EXPIRED, HttpStatus.CONFLICT, SupMessages.MSG_013);
    }

    /**
     * 502 · SUP-014 · MSG-014 — FRS lỗi/ảnh không đạt (UC-068 AF1 / UC-028 AF2).
     */
    public static ApiException faceRecognitionFailed() {
        return new ApiException(SupError.FACE_RECOGNITION_FAILED, HttpStatus.BAD_GATEWAY, SupMessages.MSG_014);
    }

    /**
     * 422 · SUP-048 · MSG-113 — BR-048/ADR-M04-6: thiếu lý do khi từ chối/đình chỉ.
     */
    public static ApiException reasonRequired() {
        return new ApiException(SupError.REASON_REQUIRED, HttpStatus.UNPROCESSABLE_CONTENT, SupMessages.MSG_113);
    }

    /** 409 · SUP-050 · MSG-117 — BR-050: CCCD trùng. */
    public static ApiException nationalIdDuplicate() {
        return new ApiException(SupError.NATIONAL_ID_DUPLICATE, HttpStatus.CONFLICT, SupMessages.MSG_117);
    }

    /**
     * 409 · SUP-409 · MSG-118 — đơn đang mở, hoặc trùng (partner, type, number).
     */
    public static ApiException businessConflict() {
        return new ApiException(SupError.BUSINESS_CONFLICT, HttpStatus.CONFLICT, SupMessages.MSG_118);
    }

    /**
     * 409 — BR-EXCLUSIVE-CATERER (chốt 08/08/2026): đơn vị suất ăn chỉ hợp tác với
     * ĐÚNG MỘT
     * trường, và một trường chỉ có ĐÚNG MỘT đơn vị suất ăn tại một thời điểm.
     *
     * <p>
     * Dùng {@code BUSINESS_CONFLICT} nhưng THÔNG ĐIỆP RIÊNG: {@code MSG_118} chung
     * chung
     * ("thao tác xung đột") không nói được vì sao, mà đây là chỗ người dùng chắc
     * chắn thắc
     * mắc — nhà cung cấp thực phẩm nộp cho nhiều trường thì được, họ thì không.
     */
    public static ApiException catererExclusivity(String detail) {
        return new ApiException(SupError.BUSINESS_CONFLICT, HttpStatus.CONFLICT, detail);
    }

    /**
     * 404 · SUP-404 · MSG-137 — không tồn tại HOẶC ngoài phạm vi dữ liệu (M04-D10).
     */
    public static ApiException notFound() {
        return new ApiException(SupError.NOT_FOUND, HttpStatus.NOT_FOUND, SystemMessages.MSG_137);
    }

    /**
     * 422 · SUP-422 · MSG-138 — dùng khi service tự validate (khác Bean Validation
     * tự động của
     * {@code GlobalExceptionHandler}) và cần liệt kê {@code details} theo field.
     */
    public static ApiException validationFailed(String field, Object value) {
        return new ApiException(SupError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT,
                SystemMessages.MSG_138, List.of(new ErrorResponse.Detail(field, value)));
    }

    /**
     * 409 · SUP-139 · MSG-139 — D5: client gửi {@code id} trùng (M04-D06, KHÁC
     * {@code SUP-409}).
     */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(SupError.DUPLICATE_VALUE, HttpStatus.CONFLICT,
                "A record with this value already exists.",
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /** 429 · SUP-141 · MSG-141 — vượt rate limit. */
    public static ApiException rateLimited() {
        return new ApiException(SupError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, SupMessages.MSG_141);
    }

    /** 409 · SUP-015 — BR-010 mở rộng, chưa xác thực email đăng ký thì chưa duyệt được. */
    public static ApiException emailNotVerified() {
        return new ApiException(SupError.EMAIL_NOT_VERIFIED, HttpStatus.CONFLICT,
                "Cannot approve: the registrant's email has not been verified yet.");
    }

    private SupExceptions() {
    }
}
