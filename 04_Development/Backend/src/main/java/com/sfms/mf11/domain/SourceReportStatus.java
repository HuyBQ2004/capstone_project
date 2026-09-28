package com.sfms.mf11.domain;

/** Vòng đời báo cáo thô (RPT). TDS-MF11_Backend.md §5.1/§5.2. */
public enum SourceReportStatus {
    PENDING_TRIAGE,
    DRAFTING,
    DRAFTED,
    MERGED,
    CLOSED_NOT_FOOD_SAFETY
}
