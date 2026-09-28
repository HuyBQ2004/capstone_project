package com.sfms.common.enums;

/**
 * D6 — loại bữa ăn. Nhãn FE: Bữa sáng / Bữa trưa / <b>Bữa phụ</b>.
 *
 * <p>⚠️ D6 chốt nhãn của {@code SNACK} là <b>"Bữa phụ"</b>, KHÔNG phải "Bữa chiều"
 * (MF07 {@code InspectionSession.meal} đang dùng "Bữa chiều" — CF-8).
 */
public enum MealType {
    BREAKFAST,
    LUNCH,
    SNACK
}
