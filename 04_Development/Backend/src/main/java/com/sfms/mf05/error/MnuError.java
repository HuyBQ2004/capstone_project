package com.sfms.mf05.error;

/**
 * Mã lỗi MF05 — nguyên văn bảng {@code TDS-MF05_Backend.md} §10 (tiền tố {@code MENU-}).
 *
 * <p>⚠️ {@code ModuleCodeResolver} có comment cũ ghi placeholder {@code MNU} — TDS §10 chốt
 * (M05-D07) giữ {@code MENU-} vì đã dùng nhất quán ở §9/§13/§15 và toàn bộ TestSpec.
 *
 * <p>4 mã còn đang chờ Huy cấp số MSG chính thức ({@code MENU-061/065/067/069}) — test chỉ
 * assert {@code $.error.code}, KHÔNG assert {@code $.error.message} cho 4 mã này cho tới khi
 * có số (TDS §2.5 mục 1, không chặn Approve).
 */
public final class MnuError {

    public static final String SESSION_EXPIRED = "MENU-004";
    public static final String ACCESS_DENIED = "MENU-005";
    public static final String NOT_FOUND = "MENU-404";
    public static final String VALIDATION_FAILED = "MENU-422";
    public static final String DUPLICATE_VALUE = "MENU-409";
    public static final String RATE_LIMITED = "MENU-429";

    /** 409 · MSG-051 — BR-063, xung đột dị ứng không có suất thay thế (chặn cứng). */
    public static final String ALLERGY_CONFLICT = "MENU-051";
    /** 409 · MSG-075 — món mặn (MON_MAN) lặp > 3 lần/tuần. */
    public static final String DISH_ROTATION_EXCEEDED = "MENU-075";
    /** 503 · MSG-073 — Gemini AI không khả dụng / timeout 10s / chưa có bean AiAssistPort. */
    public static final String AI_UNAVAILABLE = "MENU-073";
    /** 404 · MSG-090 — Parent xem tuần chưa công bố thực đơn. */
    public static final String NO_PUBLISHED_MENU = "MENU-090";
    /** 422 · MSG-067 — cảnh báo dinh dưỡng (KHÔNG chặn), trả trong {@code warnings[]}. */
    public static final String NUTRITION_WARNING = "MENU-067c";

    /** 409 · chờ Huy cấp MSG — BR-067, sửa/xóa thực đơn APPROVED/PUBLISHED. */
    public static final String MENU_LOCKED = "MENU-067";
    /** 422 · chờ Huy cấp MSG — BR-061, nộp duyệt trễ hạn (&lt; 3 ngày). */
    public static final String LEAD_TIME_VIOLATION = "MENU-061";
    /** 422 · chờ Huy cấp MSG — BR-069, món thiếu khai nguyên liệu/allergen. */
    public static final String INCOMPLETE_DECLARATION = "MENU-069";
    /** 422 · chờ Huy cấp MSG — BR-065, nhóm chế độ ăn không có suất. */
    public static final String DIET_COVERAGE_GAP = "MENU-065";

    private MnuError() {
    }
}
