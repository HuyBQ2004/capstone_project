package com.sfms.mf13.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 7 báo cáo = 7 route thật của FE ({@code school-admin/reports/[slug]}). kebab-case ở JSON —
 * ADR-M13-5 (ngoại lệ có chủ đích của D6, cùng lý do {@code RoleKey}).
 */
public enum ReportSlug {

    THREE_STEP("three-step"),
    SAMPLE_RETENTION("sample-retention"),
    RECEIVING("receiving"),
    SUPPLIER_CERTIFICATES("supplier-certificates"),
    HYGIENE("hygiene"),
    TRACEABILITY("traceability"),
    SYSTEM_USAGE("system-usage");

    private final String slug;

    ReportSlug(String slug) {
        this.slug = slug;
    }

    @JsonValue
    public String slug() {
        return slug;
    }

    @JsonCreator
    public static ReportSlug from(String raw) {
        return Arrays.stream(values())
                .filter(s -> s.slug.equals(raw))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ReportSlug: " + raw));
    }
}
