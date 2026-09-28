package com.sfms.mf03.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Factory lỗi MF03 — mỗi phương thức khóa cứng (code, HTTP status, message
 * nguyên văn) đúng
 * TDS-MF03_Backend.md §10. Copy khuôn {@code SchExceptions} (MF02).
 */
public final class StuExceptions {

    public static ApiException accessDenied() {
        return new ApiException(StuError.ACCESS_DENIED, HttpStatus.FORBIDDEN, StuMessages.MSG_005);
    }

    public static ApiException notFound() {
        return new ApiException(StuError.NOT_FOUND, HttpStatus.NOT_FOUND, StuMessages.MSG_137);
    }

    public static ApiException duplicateCode(String field, Object value) {
        return new ApiException(StuError.DUPLICATE_CODE, HttpStatus.CONFLICT, StuMessages.MSG_012,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /** 409 · STU-010 · MSG-142 — BR-025 (chốt 02/08, Q3). */
    public static ApiException classroomCapacityExceeded(int capacity, long current) {
        return new ApiException(StuError.CLASSROOM_CAPACITY_EXCEEDED, HttpStatus.CONFLICT,
                StuMessages.MSG_142,
                List.of(new ErrorResponse.Detail("classroomId",
                        Map.of("field", "classroomId", "capacity", capacity, "current", current))));
    }

    /** 409 · STU-201 · MSG-110 — BR-201. */
    public static ApiException parentLinkLimitReached() {
        return new ApiException(StuError.PARENT_LINK_LIMIT, HttpStatus.CONFLICT, StuMessages.MSG_110);
    }

    /** 422 · STU-030 · MSG-111 — BR-030. */
    public static ApiException missingParent() {
        return new ApiException(StuError.MISSING_PARENT, HttpStatus.UNPROCESSABLE_CONTENT,
                StuMessages.MSG_111, List.of(new ErrorResponse.Detail("parent.email", "required")));
    }

    /**
     * 422 · STU-031 · MSG-112 — BR-031, semantic-audit
     * XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG.
     */
    public static ApiException severeRequiresProof() {
        return new ApiException(StuError.SEVERE_REQUIRES_PROOF, HttpStatus.UNPROCESSABLE_CONTENT,
                StuMessages.MSG_112);
    }

    /** 422 · STU-032 · MSG-113 — reject/request-info thiếu lý do. */
    public static ApiException reasonRequired() {
        return new ApiException(StuError.REASON_REQUIRED, HttpStatus.UNPROCESSABLE_CONTENT,
                StuMessages.MSG_113);
    }

    /**
     * 422 · STU-066 · MSG-144 — BR-220 (chốt 02/08, Q1). details.standardDiets =
     * danh mục chuẩn.
     */
    public static ApiException dietNeedsJustification(List<String> standardDiets) {
        return new ApiException(StuError.DIET_NEEDS_JUSTIFICATION, HttpStatus.UNPROCESSABLE_CONTENT,
                StuMessages.MSG_144,
                List.of(new ErrorResponse.Detail("standardDiets", standardDiets)));
    }

    /** 400 · STU-094 · MSG-115 — field lạ ở PATCH /health-file (C8). */
    public static ApiException healthFileFieldNotEditable(String field) {
        return new ApiException(StuError.HEALTH_FILE_FIELD_NOT_EDITABLE, HttpStatus.BAD_REQUEST,
                StuMessages.MSG_115, List.of(new ErrorResponse.Detail("field", field)));
    }

    /** 405 · STU-172 · MSG-116 — BR-172 append-only (ADR-M03-4). */
    public static ApiException healthHistoryAppendOnly() {
        return new ApiException(StuError.HEALTH_HISTORY_APPEND_ONLY, HttpStatus.METHOD_NOT_ALLOWED,
                StuMessages.MSG_116);
    }

    /**
     * 409 · STU-193 · MSG nguồn = "—" (TDS §10 ghi nhận thiếu MSG chính thức, báo
     * Huy) —
     * BR-193 (MF02): lớp thuộc năm học CLOSED/ARCHIVED, read-only. Message giữ
     * nguyên văn với
     * {@code SchExceptions.classroomReadOnly()} (MF02) để nhất quán xuyên module.
     */
    public static ApiException classroomReadOnly() {
        return new ApiException(StuError.CLASSROOM_READ_ONLY, HttpStatus.CONFLICT,
                "This classroom belongs to a closed academic year and is read-only.");
    }

    /**
     * 409 · STU-014 — commit lần 2 vào đợt import đã {@code COMMITTED} (ADR-M03-6).
     */
    public static ApiException importAlreadyCommitted() {
        return new ApiException(StuError.IMPORT_ALREADY_COMMITTED, HttpStatus.CONFLICT,
                StuMessages.MSG_IMPORT_ALREADY_COMMITTED);
    }

    /** 500 · STU-500 · MSG-143 (chốt 02/08, Q5). */
    public static ApiException systemError() {
        return new ApiException(StuError.SYSTEM_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
                StuMessages.MSG_143);
    }

    /**
     * 422 · STU-008 · MSG-008 — {@code POST /students/{code}/health-documents} quá
     * 10MB.
     */
    public static ApiException fileTooLarge() {
        return new ApiException(StuError.FILE_TOO_LARGE, HttpStatus.UNPROCESSABLE_CONTENT,
                StuMessages.MSG_008);
    }

    /**
     * 422 · STU-009 · MSG-009 — {@code POST /students/{code}/health-documents} sai
     * định dạng.
     */
    public static ApiException unsupportedFileType() {
        return new ApiException(StuError.UNSUPPORTED_FILE_TYPE, HttpStatus.UNPROCESSABLE_CONTENT,
                StuMessages.MSG_009);
    }

    private StuExceptions() {
    }
}
