package com.sfms.mf12.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-SEC-009 — {@code audit_logs} chứa PII (actor+IP, BR-173): non-SysAdmin bị chặn (đã
 * cover ở matrix); {@code GET /system/activities} (UC-057, tổng hợp) KHÔNG lộ {@code actor}/
 * {@code ip} từng dòng — chỉ số liệu JVM tổng hợp (CPU/RAM/DB pool/uptime); payload
 * {@code sent-reports} không nhúng dòng audit.
 */
class AuditPrivacyTest extends AbstractIamIntegrationTest {

    @Test
    void systemActivitiesNeverLeaksPerRowActorOrIp() throws Exception {
        login("sysadmin@sfms.vn", PWD); // sinh ít nhất 1 dòng audit có actor/ip thật
        String admin = authHeaderFor("U-9001");

        MvcResult activities = mvc.perform(get("/api/v1/system/activities").header("Authorization", admin))
                .andReturn();
        assertThat(activities.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = parse(activities);
        assertThat(body.has("actor")).isFalse();
        assertThat(body.has("ip")).isFalse();
        String raw = activities.getResponse().getContentAsString();
        assertThat(raw).doesNotContain("sysadmin@sfms.vn");
        // Chỉ số liệu tổng hợp JVM — đúng tập khoá kỳ vọng.
        assertThat(body.has("cpuUsagePercent")).isTrue();
        assertThat(body.has("memoryUsedBytes")).isTrue();
        assertThat(body.has("activeDbConnections")).isTrue();
    }

    @Test
    void sentReportPayloadDoesNotEmbedAuditRows() throws Exception {
        String schoolA = "00000000-0000-0000-0000-0000000a0042";
        String actorId = jdbc.queryForObject("SELECT id FROM user_accounts WHERE code = 'U-0002'", String.class);
        jdbc.update("""
                INSERT INTO sent_reports
                    (id, code, school_id, report_type, period, range_label, range_from, range_to,
                     sent_at, sent_by_user_id, recipient, payload, confidential)
                VALUES (gen_random_uuid(), 'RPTS-PRIV-01', ?::uuid, 'COMPLIANCE', 'WEEKLY', 'Tuần test',
                        '2026-07-06', '2026-07-12', '2026-07-12T09:00:00Z', ?::uuid,
                        'School Administration', '{"avgScore":90}'::jsonb, false)
                """, schoolA, actorId);

        MvcResult detail = mvc.perform(get("/api/v1/sent-reports/RPTS-PRIV-01")
                        .header("Authorization", authHeaderFor("U-0002")))
                .andReturn();
        assertThat(detail.getResponse().getContentAsString().toLowerCase())
                .doesNotContain("entry_hash").doesNotContain("prev_hash").doesNotContain("audit_log");
    }
}
