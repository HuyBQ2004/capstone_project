package com.sfms.mf13.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import com.sfms.common.error.SystemMessages;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF13 — mỗi phương thức khoá cứng (code, HTTP status, message)
 * đúng TDS-MF13 §10.
 */
public final class RptExceptions {

    /**
     * 404 · RPT-404 · MSG-137 — không tồn tại HOẶC cross-school (BR-176 / M13-D04).
     */
    public static ApiException notFound() {
        return new ApiException(RptError.NOT_FOUND, HttpStatus.NOT_FOUND, SystemMessages.MSG_137);
    }

    /** 410 · RPT-410 · MSG-137 — bản xuất đã purge sau hạn lưu trữ (BR-177). */
    public static ApiException expired() {
        return new ApiException(RptError.EXPORT_EXPIRED, HttpStatus.GONE, SystemMessages.MSG_137);
    }

    /**
     * 403 · RPT-005 · MSG-005 — sai vai trò cho loại báo cáo (BR-175); scope=all
     * sai quyền (BR-176).
     */
    public static ApiException accessDenied() {
        return new ApiException(RptError.ACCESS_DENIED, HttpStatus.FORBIDDEN,
                "Access denied: You do not have permission to access this resource.");
    }

    /**
     * 422 · RPT-138 · MSG-138 — format sai/period sai/thiếu dayOfWeek-dayOfMonth.
     */
    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(RptError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT,
                SystemMessages.MSG_138, List.of(new ErrorResponse.Detail(field, reason)));
    }

    /**
     * 409 · RPT-409 · MSG-138 — D5: {@code body.id} client gửi đã tồn tại trong
     * trường.
     */
    public static ApiException idConflict(String field, Object value) {
        return new ApiException(RptError.ID_CONFLICT, HttpStatus.CONFLICT, SystemMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    private RptExceptions() {
    }
}
