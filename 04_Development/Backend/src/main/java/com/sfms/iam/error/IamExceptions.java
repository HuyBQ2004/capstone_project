package com.sfms.iam.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import com.sfms.common.error.SystemMessages;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory cho toàn bộ lỗi MF01 — mỗi phương thức khóa cứng cặp
 * (code, HTTP status, message nguyên văn) đúng như <b>TDS-MF01 §10</b>.
 *
 * <p>
 * Mục đích: không nơi nào trong service được tự ghép chuỗi message; sai một ký
 * tự
 * là mọi TC assert {@code equalTo(...)} (D7 đòi nguyên văn) sẽ đỏ.
 */
public final class IamExceptions {

    /**
     * 401 · IAM-002 · MSG-002. Dùng cho cả sai email lẫn sai MK (SEC-011: không lộ
     * email).
     */
    public static ApiException invalidCredentials() {
        return new ApiException(IamError.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, IamMessages.MSG_002);
    }

    /**
     * 401 · IAM-002 · MSG-002 kèm
     * {@code details:[{field:"attemptsLeft", value:N}]}.
     *
     * <p>
     * Chốt 2026-07-31 (TDS §9.2, đóng TestSpec §2 L3): {@code attemptsLeft} nằm
     * trong
     * {@code details}, KHÔNG phải khóa thứ 4 của envelope. Áp cho <b>cả hai</b>
     * nhánh
     * email tồn tại và không tồn tại — cùng khuôn, cùng dải giá trị (chống
     * enumeration,
     * MF01-TC-SEC-011).
     */
    public static ApiException invalidCredentials(int attemptsLeft) {
        return new ApiException(IamError.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, IamMessages.MSG_002,
                List.of(new ErrorResponse.Detail("attemptsLeft", attemptsLeft)));
    }

    /** 403 · IAM-003 · MSG-003 — BR-010: INACTIVE/SUSPENDED/chưa xác minh email. */
    public static ApiException accountDeactivated() {
        return new ApiException(IamError.ACCOUNT_DEACTIVATED, HttpStatus.FORBIDDEN, IamMessages.MSG_003);
    }

    /** 403 · IAM-004 · MSG-005 — BR-007/BR-008/BR-013. */
    public static ApiException accessDenied() {
        return new ApiException(IamError.ACCESS_DENIED, HttpStatus.FORBIDDEN, IamMessages.MSG_005);
    }

    /** 401 · IAM-005 · MSG-004 — access token hết hạn/không hợp lệ. */
    public static ApiException sessionExpired() {
        return new ApiException(IamError.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED, IamMessages.MSG_004);
    }

    /** 409 · IAM-006 · MSG-006 — BR-012 email duy nhất. */
    public static ApiException emailAlreadyUsed() {
        return new ApiException(IamError.EMAIL_ALREADY_USED, HttpStatus.CONFLICT, IamMessages.MSG_006);
    }

    /** 422 · IAM-006policy · MSG-104 — BR-006 chính sách độ mạnh MK. */
    public static ApiException passwordPolicy() {
        return new ApiException(IamError.PASSWORD_POLICY, HttpStatus.UNPROCESSABLE_CONTENT, IamMessages.MSG_104);
    }

    /** 422 · IAM-006reuse · MSG-105 — BR-006 không trùng 3 MK gần nhất. */
    public static ApiException passwordReuse() {
        return new ApiException(IamError.PASSWORD_REUSE, HttpStatus.UNPROCESSABLE_CONTENT, IamMessages.MSG_105);
    }

    /**
     * 423 · IAM-007 · MSG-076 — BR-002; {@code [Minutes]} đọc từ SecurityPolicy,
     * không hardcode.
     */
    public static ApiException accountLocked(int lockoutMinutes) {
        return new ApiException(IamError.ACCOUNT_LOCKED, HttpStatus.LOCKED,
                IamMessages.lockedFor(lockoutMinutes));
    }

    /**
     * 401 · IAM-008 · MSG-004 — refresh/reset/activation token hết hạn, đã dùng
     * hoặc đã revoke.
     */
    public static ApiException tokenInvalid() {
        return new ApiException(IamError.TOKEN_INVALID, HttpStatus.UNAUTHORIZED, IamMessages.MSG_004);
    }

    /**
     * 409 · IAM-009 · MSG-106 — BR-008; {@code [Number]} = countByRoleId (kể cả
     * user INACTIVE).
     */
    public static ApiException roleInUse(long assignedUsers) {
        return new ApiException(IamError.ROLE_IN_USE, HttpStatus.CONFLICT,
                IamMessages.roleInUse(assignedUsers));
    }

    /**
     * 422 · IAM-010 · MSG-008 — ảnh vượt 10MB (UC-097; CF-4: UC nói 5MB, MSG nói
     * 10MB → theo MSG, D7).
     */
    public static ApiException fileTooLarge() {
        return new ApiException(IamError.FILE_UPLOAD, HttpStatus.UNPROCESSABLE_CONTENT, IamMessages.MSG_008);
    }

    /** 422 · IAM-010 · MSG-009 — định dạng ảnh không hỗ trợ (UC-097). */
    public static ApiException unsupportedFileType() {
        return new ApiException(IamError.FILE_UPLOAD, HttpStatus.UNPROCESSABLE_CONTENT, IamMessages.MSG_009);
    }

    /**
     * 403 · IAM-102 · MSG-102 — BR-218: email Google chưa có tài khoản, KHÔNG tự
     * tạo.
     */
    public static ApiException googleNotLinked() {
        return new ApiException(IamError.GOOGLE_NOT_LINKED, HttpStatus.FORBIDDEN, IamMessages.MSG_102);
    }

    /**
     * 401 · IAM-103 · MSG-103 — BR-218: token exchange lỗi / email chưa verified /
     * sub lệch.
     */
    public static ApiException googleAuthFailed() {
        return new ApiException(IamError.GOOGLE_AUTH_FAILED, HttpStatus.UNAUTHORIZED, IamMessages.MSG_103);
    }

    /** 404 · IAM-404 · MSG-137 (chốt 2026-07-31). */
    public static ApiException notFound() {
        return new ApiException(IamError.NOT_FOUND, HttpStatus.NOT_FOUND, SystemMessages.MSG_137);
    }

    /** 422 · IAM-422 · MSG-138 (chốt 2026-07-31). */
    public static ApiException validationFailed(List<ErrorResponse.Detail> details) {
        return new ApiException(IamError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT,
                SystemMessages.MSG_138, details);
    }

    /**
     * 409 · IAM-409 — D5: {@code body.id} client gửi đã tồn tại; cũng dùng cho tên
     * role trùng
     * (UC-098 bước 5 <i>"name (if modified) is unique"</i>).
     *
     * <p>
     * ⚠️ <b>ORACLE GAP</b> — TDS §10 chưa có dòng nào cho tình huống này;
     * {@code error_message.md}
     * cũng chưa cấp MSG riêng. TestSpec {@code MF01-TC-INT-009} (bước 4) và
     * {@code MF01-TC-013}
     * (bước 3) vì vậy <b>chỉ assert HTTP 409</b>, không assert {@code message}.
     * Tạm dùng MSG-138 (mã dùng chung cho "giá trị không hợp lệ") cho tới khi Nam
     * bổ sung
     * 1 dòng vào TDS §10 — xem báo cáo giả định.
     */
    public static ApiException duplicateCode(String field, Object value) {
        return new ApiException(IamError.DUPLICATE_CODE, HttpStatus.CONFLICT, SystemMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    private IamExceptions() {
    }
}
