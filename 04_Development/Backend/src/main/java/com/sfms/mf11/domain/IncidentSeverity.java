package com.sfms.mf11.domain;

/**
 * BR-147 — 3 mức. {@code null} ở DB = chưa phân loại (CF-M11-12); JSON vẫn trả chuỗi
 * {@code "PENDING"} qua serializer riêng ở DTO, KHÔNG phải giá trị của enum này.
 */
public enum IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH
}
