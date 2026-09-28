package com.sfms.mf06.domain;

/**
 * TDS-MF06_Backend.md §5.1.
 * {@code CANCELLED} (13/08/2026, theo Nam) — huỷ lô (soft), giữ nguyên bản ghi cho truy xuất
 * nguồn gốc (BR-076), migration V72.
 */
public enum SupplierBatchStatus {
    ACTIVE, DELIVERED, EXPIRED, CANCELLED
}
