package com.sfms.mf03.domain;

/**
 * TDS-MF03 §5.2 — gate hiệu lực per-declaration (ADR-M03-2, BR-031). Cục bộ MF03 (không thuộc
 * 8 enum D6) vì không dùng chung với module khác.
 */
public enum DeclarationStatus {
    PENDING,
    VERIFIED,
    NEEDS_INFO
}
