package com.sfms.mf05.error;

/**
 * Nội dung message MF05 — nguyên văn {@code error_message.md} (D7: tiếng Anh, không dịch),
 * tra theo {@code TDS-MF05_Backend.md} §10.
 *
 * <p>4 message cuối (MENU-061/065/067/069) là <b>bản soạn sẵn chờ Huy cấp số MSG chính thức</b>
 * (TDS §2.5 mục 1) — dùng nguyên văn draft của TDS §10, không tự đặt khác.
 */
public final class MnuMessages {

    public static final String MSG_004 = "Session expired. Please log in again.";
    public static final String MSG_005 =
            "Access denied: You do not have permission to access this resource.";
    public static final String MSG_137 = "Resource not found.";
    public static final String MSG_138 = "Validation failed: one or more required fields are missing or invalid.";
    public static final String MSG_139 = "A record with this value already exists.";
    public static final String MSG_141 = "Too many requests. Please try again later.";

    public static final String MSG_051 =
            "Allergy conflict detected: Dish [Dish Name] contains allergen [Allergen] "
                    + "affecting [Number] students.";
    public static final String MSG_075 =
            "Dish rotation limit exceeded: [Dish Name] cannot be served more than three times a week.";
    public static final String MSG_073 = "Gemini AI Service is currently unavailable. Please try again later.";
    public static final String MSG_090 =
            "No menu has been published for the selected week yet. Please check back later.";
    public static final String MSG_067 =
            "AI menu analysis: Nutritional diversity is below the required 3-group minimum.";

    /** Draft — MENU-067, BR-067 (chờ Huy cấp số). */
    public static final String MSG_MENU_067 = "An approved or published menu can no longer be edited or deleted.";
    /** Draft — MENU-061, BR-061 (chờ Huy cấp số). */
    public static final String MSG_MENU_061 =
            "A menu must be submitted for approval at least 3 days before its service date.";
    /** Draft — MENU-069, BR-069 (chờ Huy cấp số). */
    public static final String MSG_MENU_069 = "Dish [Dish Name] has incomplete ingredient or allergen declaration.";
    /** Draft — MENU-065, BR-065 (chờ Huy cấp số). */
    public static final String MSG_MENU_065 = "No meal has been planned for diet group [Diet Group] of [Grade].";

    public static final String MSG_039 = "Menu draft submitted for approval successfully.";
    public static final String MSG_040 = "Menu approved and published successfully. Notifications sent to parents.";
    public static final String MSG_074 = "Menu updated successfully.";
    public static final String MSG_071 = "Menu draft was saved successfully.";
    public static final String MSG_072 = "Special meal plan saved successfully.";
    public static final String MSG_029 = "Profile updated successfully.";

    private MnuMessages() {
    }
}
