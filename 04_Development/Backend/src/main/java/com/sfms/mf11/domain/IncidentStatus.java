package com.sfms.mf11.domain;

/** Vòng đời sự cố (INC). BR-147/BR-155. TDS-MF11_Backend.md §5.1/§5.2. */
public enum IncidentStatus {
    SEVERITY_PENDING,
    INVESTIGATING,
    CAPA_PENDING,
    CAPA_APPROVED,
    VALIDATED,
    CLOSED
}
