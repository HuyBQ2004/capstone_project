package com.sfms.mf02.domain;

/**
 * Vòng đời trường học (TDS-MF02 §6.4, §5.2). KHÔNG thuộc 8 enum dùng chung của D6
 * — chỉ dùng trong MF02, khai tại đây (không phải {@code common/enums}).
 *
 * <p>{@code PENDING} là mặc định khi đăng ký (UC-070) — chốt 2026-07-31, ghi đè
 * default {@code ACTIVE} của stub MF01.
 */
public enum SchoolStatus {
    PENDING,
    ACTIVE,
    INACTIVE
}
