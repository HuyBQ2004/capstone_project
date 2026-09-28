package com.sfms.mf06.domain;

/**
 * Vòng đời {@link ClassMealDelivery} — 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2
 * mục (1)+(4). Bếp giao là ĐÃ XONG (không có trạng thái "đang chuẩn bị" trung gian — nếu bếp
 * gọi endpoint thì suất đã tới lớp), Giáo viên xác nhận nhận là bước cuối.
 */
public enum ClassMealDeliveryStatus {
    DELIVERED,
    RECEIVED
}
