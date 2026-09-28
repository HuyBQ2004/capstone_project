package com.sfms.mf05.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF05 — mỗi phương thức khóa cứng (code, HTTP status, message
 * nguyên văn) đúng
 * {@code TDS-MF05_Backend.md} §10. Copy khuôn {@code StuExceptions} (MF03).
 */
public final class MnuExceptions {

    public static ApiException notFound() {
        return new ApiException(MnuError.NOT_FOUND, HttpStatus.NOT_FOUND, MnuMessages.MSG_137);
    }

    public static ApiException accessDenied() {
        return new ApiException(MnuError.ACCESS_DENIED, HttpStatus.FORBIDDEN, MnuMessages.MSG_005);
    }

    /** 409 · MENU-409 · MSG-139 — D5, client gửi {@code id} trùng ở POST. */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(MnuError.DUPLICATE_VALUE, HttpStatus.CONFLICT, MnuMessages.MSG_139,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /**
     * 409 · MENU-409 — TDS §9.5 mục 11, xóa món/mẫu đang được thực đơn tham chiếu.
     */
    public static ApiException referencedByMenu(String field, Object value) {
        return new ApiException(MnuError.DUPLICATE_VALUE, HttpStatus.CONFLICT,
                "This item is still referenced by at least one menu and cannot be deleted.",
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /**
     * 409 · MENU-409 — pre-check thân thiện thay cho lỗi DB thô
     * (ux_menu_all_per_slot /
     * ux_menu_diet_per_slot, V8/V33): đã có menu KHÔNG REJECTED cho đúng (khối,
     * ngày, buổi,
     * audience) này rồi.
     */
    public static ApiException duplicateSlot(String gradeLabel, String mealType, String audience) {
        String audienceLabel = "ALL".equals(audience) ? "thực đơn chung" : "thực đơn nhóm " + audience;
        return new ApiException(MnuError.DUPLICATE_VALUE, HttpStatus.CONFLICT,
                "A menu already exists for this (grade, date, mealType, audience) slot — "
                        + audienceLabel + ", " + gradeLabel + ", " + mealType + ".",
                List.of(new ErrorResponse.Detail("gradeId", gradeLabel),
                        new ErrorResponse.Detail("mealType", mealType),
                        new ErrorResponse.Detail("audience", audience)));
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(MnuError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT, MnuMessages.MSG_138,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    public static ApiException rateLimited() {
        return new ApiException(MnuError.RATE_LIMITED, HttpStatus.TOO_MANY_REQUESTS, MnuMessages.MSG_141);
    }

    /**
     * 409 · MENU-051 · MSG-051 — BR-063. {@code details} chỉ chứa
     * {@code dishName}/{@code allergen}
     * /{@code affectedStudentCount} — KHÔNG BAO GIỜ {@code studentId}/tên (PDPD,
     * M05-D14).
     */
    public static ApiException allergyConflict(String dishName, String allergen, int affectedStudentCount) {
        String message = MnuMessages.MSG_051
                .replace("[Dish Name]", dishName)
                .replace("[Allergen]", allergen)
                .replace("[Number]", String.valueOf(affectedStudentCount));
        return new ApiException(MnuError.ALLERGY_CONFLICT, HttpStatus.CONFLICT, message,
                List.of(new ErrorResponse.Detail("dishName", dishName),
                        new ErrorResponse.Detail("allergen", allergen),
                        new ErrorResponse.Detail("affectedStudentCount", affectedStudentCount)));
    }

    /** 409 · MENU-075 · MSG-075 — món mặn (MON_MAN) lặp > 3 lần/tuần. */
    public static ApiException dishRotationExceeded(String dishName) {
        String message = MnuMessages.MSG_075.replace("[Dish Name]", dishName);
        return new ApiException(MnuError.DISH_ROTATION_EXCEEDED, HttpStatus.CONFLICT, message,
                List.of(new ErrorResponse.Detail("dishName", dishName)));
    }

    /** 503 · MENU-073 · MSG-073 — ADR-M05-03, BR-156. */
    public static ApiException aiUnavailable() {
        return new ApiException(MnuError.AI_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, MnuMessages.MSG_073);
    }

    /** 404 · MENU-090 · MSG-090 — UC-135, Parent xem tuần chưa công bố. */
    public static ApiException noPublishedMenu() {
        return new ApiException(MnuError.NO_PUBLISHED_MENU, HttpStatus.NOT_FOUND, MnuMessages.MSG_090);
    }

    /**
     * 409 · MENU-067 · draft MSG (chờ Huy cấp số) — BR-067, sửa/xóa
     * APPROVED/PUBLISHED.
     */
    public static ApiException menuLocked() {
        return new ApiException(MnuError.MENU_LOCKED, HttpStatus.CONFLICT, MnuMessages.MSG_MENU_067);
    }

    /** 422 · MENU-061 · draft MSG (chờ Huy cấp số) — BR-061, nộp duyệt trễ hạn. */
    public static ApiException leadTimeViolation() {
        return new ApiException(MnuError.LEAD_TIME_VIOLATION, HttpStatus.UNPROCESSABLE_CONTENT,
                MnuMessages.MSG_MENU_061);
    }

    /**
     * 422 · MENU-069 · draft MSG (chờ Huy cấp số) — BR-069, món thiếu khai
     * BOM/allergen.
     */
    public static ApiException incompleteDeclaration(String dishName) {
        String message = MnuMessages.MSG_MENU_069.replace("[Dish Name]", dishName);
        return new ApiException(MnuError.INCOMPLETE_DECLARATION, HttpStatus.UNPROCESSABLE_CONTENT, message,
                List.of(new ErrorResponse.Detail("dishName", dishName)));
    }

    /**
     * 422 · MENU-065 · draft MSG (chờ Huy cấp số) — BR-065, nhóm chế độ ăn không có
     * suất.
     */
    public static ApiException dietCoverageGap(String dietGroupLabel, String gradeLabel) {
        String message = MnuMessages.MSG_MENU_065
                .replace("[Diet Group]", dietGroupLabel)
                .replace("[Grade]", gradeLabel);
        return new ApiException(MnuError.DIET_COVERAGE_GAP, HttpStatus.UNPROCESSABLE_CONTENT, message,
                List.of(new ErrorResponse.Detail("dietGroup", dietGroupLabel),
                        new ErrorResponse.Detail("gradeId", gradeLabel)));
    }

    private MnuExceptions() {
    }
}
