package com.sfms.mf03.domain;

/** TDS-MF03 §5.2/§6.5 — state machine của {@link SpecialMealRequest} (UC-078, BR-065/BR-220). */
public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NEEDS_INFO
}
