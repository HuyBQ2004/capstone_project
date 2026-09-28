package com.sfms.mf11.domain;

/** Vòng đời biên bản sự cố (REC). TDS-MF11_Backend.md §5.1/§5.2. */
public enum RecordDraftStatus {
    DRAFT,
    COMPLETED,
    OPENED,
    CLOSED_NOT_FOOD_SAFETY
}
