package com.sfms.mf02.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import com.sfms.common.error.SystemMessages;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory cho toàn bộ lỗi MF02 — mỗi phương thức khóa cứng cặp
 * (code, HTTP status, message nguyên văn) đúng như <b>TDS-MF02_Backend.md
 * §10</b>.
 */
public final class SchExceptions {

    /** 409 · SCH-010 · MSG-010 — BR-025: capacity > 50. */
    public static ApiException capacityExceeded() {
        return new ApiException(SchError.CAPACITY_EXCEEDED, HttpStatus.CONFLICT, SchMessages.MSG_010);
    }

    /**
     * 409 · SCH-012 · MSG-140 — BR-219 (chốt 2026-07-31): capacity mới < sĩ số hiện
     * tại.
     */
    public static ApiException capacityBelowEnrolled() {
        return new ApiException(SchError.CAPACITY_BELOW_ENROLLED, HttpStatus.CONFLICT, SchMessages.MSG_140);
    }

    /** 422 · SCH-024 · MSG-109 — BR-024: grade ∉ [1,12]. */
    public static ApiException invalidGrade() {
        return new ApiException(SchError.INVALID_GRADE, HttpStatus.UNPROCESSABLE_CONTENT, SchMessages.MSG_109);
    }

    /** 409 · SCH-081 · MSG-081 — BR-192: chưa có năm học Active. */
    public static ApiException noActiveAcademicYear() {
        return new ApiException(SchError.NO_ACTIVE_ACADEMIC_YEAR, HttpStatus.CONFLICT, SchMessages.MSG_081);
    }

    /** 409 · SCH-011 · MSG-011 — BR-020: GVCN đã chủ nhiệm lớp khác. */
    public static ApiException teacherAlreadyAssigned() {
        return new ApiException(SchError.TEACHER_ALREADY_ASSIGNED, HttpStatus.CONFLICT, SchMessages.MSG_011);
    }

    /** 400 · SCH-007 · MSG-007 — BR-028: mã số thuế sai định dạng. */
    public static ApiException invalidTaxId() {
        return new ApiException(SchError.INVALID_TAX_ID, HttpStatus.BAD_REQUEST, SchMessages.MSG_007);
    }

    /** 400 · SCH-013 · MSG-107 — BR-028: tọa độ ngoài dải hợp lệ. */
    public static ApiException invalidCoordinate(String field, Object value) {
        return new ApiException(SchError.INVALID_COORDINATE, HttpStatus.BAD_REQUEST, SchMessages.MSG_107,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /**
     * 403 · SCH-013 · MSG-108 — BR-028: tọa độ đã khóa, không phải System Admin.
     */
    public static ApiException coordinateLocked() {
        return new ApiException(SchError.COORDINATE_LOCKED, HttpStatus.FORBIDDEN, SchMessages.MSG_108);
    }

    /**
     * 422 · SCH-422 — dữ liệu không hợp lệ, kèm field + giá trị để FE chỉ đúng ô hỏng.
     * Dùng cho các ràng buộc không có mã lỗi nghiệp vụ riêng (VD: kích hoạt năm học đã kết thúc).
     */
    public static ApiException validationFailed(String field, Object value) {
        return new ApiException(SchError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT,
                "Validation failed.", List.of(new ErrorResponse.Detail(field, value)));
    }

    /** 409 · SCH-080 · MSG-080 — BR-191: trường đã có năm học Active. */
    public static ApiException academicYearAlreadyActive() {
        return new ApiException(SchError.ACADEMIC_YEAR_ALREADY_ACTIVE, HttpStatus.CONFLICT, SchMessages.MSG_080);
    }

    /**
     * 429 · SCH-014 · MSG-141 — vượt rate limit {@code POST /schools/register}
     * 5/min/IP.
     */
    public static ApiException rateLimited() {
        return new ApiException(SchError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, SchMessages.MSG_141);
    }

    /** 409 · SCH-015 — BR-010 mở rộng, chưa xác thực email đăng ký thì chưa phê duyệt được. */
    public static ApiException emailNotVerified() {
        return new ApiException(SchError.EMAIL_NOT_VERIFIED, HttpStatus.CONFLICT,
                "Cannot approve: the registrant's email has not been verified yet.");
    }

    /**
     * 403 · SCH-005 · MSG-005 — sai vai trò TRONG CÙNG trường (chốt 2026-07-31,
     * đóng L7).
     */
    public static ApiException accessDenied() {
        return new ApiException(SchError.ACCESS_DENIED, HttpStatus.FORBIDDEN,
                "Access denied: You do not have permission to access this resource.");
    }

    /**
     * 404 · SCH-404 · MSG-137 — không tồn tại HOẶC cross-school (BR-022, chốt
     * 2026-07-31).
     */
    public static ApiException notFound() {
        return new ApiException(SchError.NOT_FOUND, HttpStatus.NOT_FOUND, SystemMessages.MSG_137);
    }

    /**
     * 409 · SCH-409 · MSG-139 — D5 ({@code body.id}/{@code code} trùng) hoặc
     * {@code taxId} trùng
     * (chốt 2026-07-31, đóng L9).
     */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(SchError.DUPLICATE_VALUE, HttpStatus.CONFLICT,
                "A record with this value already exists.",
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /**
     * 409 · SCH-193 — BR-193: lớp thuộc năm học CLOSED/ARCHIVED (read-only).
     * ⚠️ ORACLE GAP — xem Javadoc {@link SchError#CLASSROOM_READ_ONLY}.
     */
    public static ApiException classroomReadOnly() {
        return new ApiException(SchError.CLASSROOM_READ_ONLY, HttpStatus.CONFLICT,
                "This classroom belongs to a closed academic year and is read-only.");
    }

    private SchExceptions() {
    }
}
