package com.sfms.mf06.domain;

/**
 * ADR-M06-2 — DẪN XUẤT từ {@code expiry_date}, KHÔNG lưu cột (CF-M06-11).
 * Giá trị thứ ba là {@code IN_STOCK}, KHÔNG phải {@code ACTIVE} (M06-D11 — sửa 2026-07-29:
 * {@code ACTIVE} đã bị FE dùng cho trạng thái đối tác, gắn cho lô kho sẽ render sai ngữ cảnh).
 */
public enum ShelfStatus {
    IN_STOCK, EXPIRING_SOON, EXPIRED
}
