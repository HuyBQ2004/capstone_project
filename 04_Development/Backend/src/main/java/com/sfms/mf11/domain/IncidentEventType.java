package com.sfms.mf11.domain;

/** Loại sự kiện trong sổ append-only {@code incident_events} (ADR-M11-2). */
public enum IncidentEventType {
    REPORTED,
    SEVERITY_SET,
    QUARANTINE,
    RELEASE,
    SAMPLE_SENT,
    TRACE_READY,
    STUDENT_RECOVERED,
    CAPA_SUBMITTED,
    CAPA_APPROVED,
    CAPA_EVIDENCE,
    CAPA_VALIDATED,
    REPORT_SENT,
    CLOSED
}
