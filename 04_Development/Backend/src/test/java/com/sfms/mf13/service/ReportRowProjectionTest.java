package com.sfms.mf13.service;

import com.sfms.mf13.domain.ReportSlug;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** MF13-TC-004 / TC-UNIT-001 — BR-181 whitelist cột, fail-closed (ADR-M13-4). */
class ReportRowProjectionTest {

    private final ReportRowProjection projection = new ReportRowProjection();

    @Test
    void stripsPiiColumnsInjectedByUpstreamView() {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("id", "SMP-20260706-01");
        raw.put("dish", "Canh bí đỏ");
        raw.put("meal_type", "LUNCH");
        raw.put("meal_shift", "Trưa 06/07");
        raw.put("sample_amount", 150);
        raw.put("sample_unit", "G");
        raw.put("retention_deadline", "2026-07-07T10:30:00Z");
        raw.put("fridge_temp", 4);
        raw.put("status", "RETAINED");
        raw.put("collected_at", "2026-07-06T10:30:00Z");
        // giả lập module nguồn VÔ TÌNH thêm cột PII học sinh vào view
        raw.put("studentName", "Nguyễn Văn A");
        raw.put("studentId", "HS-0001");

        Map<String, Object> out = projection.project(ReportSlug.SAMPLE_RETENTION, raw);

        assertThat(out).doesNotContainKeys("studentName", "studentId");
        assertThat(out.keySet()).containsExactly("id", "dish", "mealType", "mealShift", "sampleAmount",
                "sampleUnit", "retentionDeadline", "fridgeTemp", "status", "collectedAt");
    }

    @Test
    void unknownSlugYieldsEmptyMap() {
        Map<String, Object> out = projection.project(ReportSlug.HYGIENE, Map.of("date", "2026-07-01"));
        assertThat(out).containsKey("date");
        assertThat(out).doesNotContainKey("unknownColumn");
    }

    @Test
    void nullRowYieldsEmptyMap() {
        assertThat(projection.project(ReportSlug.THREE_STEP, null)).isEmpty();
    }
}
