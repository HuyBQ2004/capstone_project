package com.sfms.mf09.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF09 — mỗi phương thức khóa cứng (code, HTTP status, message)
 * đúng §10.
 */
public final class SrvExceptions {

    private SrvExceptions() {
    }

    public static ApiException notFound() {
        return new ApiException(SrvError.NOT_FOUND, HttpStatus.NOT_FOUND, SrvMessages.MSG_137);
    }

    public static ApiException accessDenied() {
        return new ApiException(SrvError.ACCESS_DENIED, HttpStatus.FORBIDDEN, SrvMessages.MSG_005);
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(SrvError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT, SrvMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    /**
     * 409 · D5 — client gửi id/code trùng ở POST (không dùng ở MF09 hiện tại, giữ
     * để nhất quán §10).
     */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(SrvError.DUPLICATE_VALUE, HttpStatus.CONFLICT, SrvMessages.MSG_139,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /** 409 · BR-130 — xác nhận phát lần 2 trong cùng (ngày, bữa). */
    public static ApiException duplicateDistribution() {
        return new ApiException(SrvError.DUPLICATE_DISTRIBUTION, HttpStatus.CONFLICT, SrvMessages.MSG_021);
    }

    /**
     * 403 · BR-094 (MF07) — bước kiểm thực thất bại. {@code stepName} thay [Step
     * Name].
     */
    public static ApiException inspectionGateBlocked(String stepName) {
        String message = SrvMessages.MSG_129.replace("[Step Name]", stepName == null ? "" : stepName);
        return new ApiException(SrvError.INSPECTION_GATE_BLOCKED, HttpStatus.FORBIDDEN, message);
    }

    /** 403 · BR-105 mở rộng (10/08/2026) — chưa lưu mẫu đủ mọi món của bữa. */
    public static ApiException sampleRetentionIncomplete(List<String> missingDishNames) {
        String message = SrvMessages.MSG_143.replace("[Dishes]", String.join(", ", missingDishNames));
        return new ApiException(SrvError.SAMPLE_RETENTION_INCOMPLETE, HttpStatus.FORBIDDEN, message);
    }

    /** 422 · Ràng buộc C3 — không có thực đơn cho (ngày, khối, bữa). */
    public static ApiException noMenu(String meal, String date, String grade) {
        String message = SrvMessages.MSG_130
                .replace("[Meal]", meal == null ? "" : meal)
                .replace("[Date]", date == null ? "" : date)
                .replace("[Grade]", grade == null ? "" : grade);
        return new ApiException(SrvError.NO_MENU, HttpStatus.UNPROCESSABLE_CONTENT, message);
    }

    /** 409 · AF1 UC-016 — còn báo lỗi phân phát chưa xử lý. */
    public static ApiException unresolvedErrorReport() {
        return new ApiException(SrvError.UNRESOLVED_ERROR_REPORT, HttpStatus.CONFLICT, SrvMessages.MSG_131);
    }

    /** 409 · ADR-M09-3 — hủy đánh dấu sau ngày phục vụ. */
    public static ApiException revokeTooLate() {
        return new ApiException(SrvError.REVOKE_TOO_LATE, HttpStatus.CONFLICT, SrvMessages.MSG_132);
    }

    /** 409 · BR-121/BR-133 — sửa tiêu thụ sau ngày phục vụ. */
    public static ApiException consumptionLocked() {
        return new ApiException(SrvError.CONSUMPTION_LOCKED, HttpStatus.CONFLICT, SrvMessages.MSG_133);
    }

    public static ApiException rateLimited() {
        return new ApiException(SrvError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, SrvMessages.MSG_141);
    }

    /**
     * 422 · BR-065 — dị ứng trùng món, món KHÔNG có món thay thế cấu hình sẵn. Chặn
     * phát.
     */
    public static ApiException allergySubstituteMissing(String dishName) {
        String message = SrvMessages.MSG_142.replace("[Dish Name]", dishName == null ? "" : dishName);
        return new ApiException(SrvError.ALLERGY_SUBSTITUTE_MISSING, HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
