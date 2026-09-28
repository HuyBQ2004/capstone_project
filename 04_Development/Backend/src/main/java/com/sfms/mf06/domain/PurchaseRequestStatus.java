package com.sfms.mf06.domain;

/** TDS-MF06_Backend.md §5.1 — vòng đời duyệt giá 1 vòng (CF-M06-5, KHÔNG dual-authorization). */
public enum PurchaseRequestStatus {
    PENDING, QUOTED, PRICE_ACCEPTED, APPROVED, COMPLETED, CANCELLED, REJECTED
}
