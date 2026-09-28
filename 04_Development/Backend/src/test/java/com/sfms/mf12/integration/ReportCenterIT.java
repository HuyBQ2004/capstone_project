package com.sfms.mf12.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-INT-019 — {@code GET /report-center} (UC-187 hub) chỉ gom UC-185 (COMPLIANCE) +
 * UC-189 (VIOLATION_SUMMARY); {@code INCIDENT_SUMMARY} (Deprecated 26/07/2026, BR-213) VẪN đọc
 * được qua {@code GET /sent-reports} (lịch sử, BR-177) nhưng KHÔNG xuất hiện ở hub. Chỉ
 * MEDICAL_STAFF gọi được — kể cả SYSTEM_ADMIN cũng 403.
 */
class ReportCenterIT extends AbstractIamIntegrationTest {

    private void seedThreeTypes() {
        String schoolA = "00000000-0000-0000-0000-0000000a0042";
        String actor = jdbc.queryForObject("SELECT id FROM user_accounts WHERE code = 'U-0002'", String.class);
        for (String[] row : List.of(
                new String[] {"RPTS-HUB-01", "COMPLIANCE"},
                new String[] {"RPTS-HUB-02", "VIOLATION_SUMMARY"},
                new String[] {"RPTS-HUB-03", "INCIDENT_SUMMARY"})) {
            jdbc.update("""
                    INSERT INTO sent_reports
                        (id, code, school_id, report_type, period, range_label, range_from, range_to,
                         sent_at, sent_by_user_id, recipient, payload, confidential)
                    VALUES (gen_random_uuid(), ?, ?::uuid, ?, 'WEEKLY', 'Tuần test', '2026-07-06', '2026-07-12',
                            '2026-07-12T09:00:00Z', ?::uuid, 'School Administration', '{}'::jsonb, false)
                    """, row[0], schoolA, row[1], actor);
        }
    }

    @Test
    void hubListsOnlyComplianceAndViolationSummaryHistoryStillReadableAndNonMedicalForbidden() throws Exception {
        seedThreeTypes();
        String medical = authHeaderFor("U-0002");

        MvcResult hub = mvc.perform(get("/api/v1/report-center").header("Authorization", medical)).andReturn();
        assertThat(hub.getResponse().getStatus()).isEqualTo(200);
        JsonNode hubItems = parse(hub).get("items");
        List<String> hubTypes = new java.util.ArrayList<>();
        hubItems.forEach(n -> hubTypes.add(n.get("reportType").asText()));
        assertThat(hubTypes).contains("COMPLIANCE", "VIOLATION_SUMMARY").doesNotContain("INCIDENT_SUMMARY");

        // Lịch sử đầy đủ vẫn đọc được INCIDENT_SUMMARY cũ (BR-177) — chỉ HUB lọc, không phải toàn hệ thống.
        MvcResult history = mvc.perform(get("/api/v1/sent-reports?pageSize=50")
                        .header("Authorization", medical)).andReturn();
        List<String> historyTypes = new java.util.ArrayList<>();
        parse(history).get("items").forEach(n -> historyTypes.add(n.get("reportType").asText()));
        assertThat(historyTypes).contains("INCIDENT_SUMMARY");

        String sysAdmin = authHeaderFor("U-9001");
        MvcResult forbidden = mvc.perform(get("/api/v1/report-center").header("Authorization", sysAdmin))
                .andReturn();
        assertThat(forbidden.getResponse().getStatus()).isEqualTo(403);
    }
}
