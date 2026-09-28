package com.sfms.mf04.service;

/** Trạng thái tài liệu tuân thủ — DẪN XUẤT runtime (ADR-M04-4), không lưu cột. */
public enum DocumentStatus {
    VALID,
    EXPIRING,
    URGENT,
    EXPIRED
}
