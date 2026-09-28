package com.sfms.mf06.domain;

/** TDS-MF06_Backend.md §5.1 — RECEIVED_PARTIAL = có >=1 lô bị trả về (UC-072). */
public enum ReceivingStatus {
    IN_PROGRESS, RECEIVED, RECEIVED_PARTIAL, REJECTED
}
