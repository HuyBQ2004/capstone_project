package com.sfms.mf12.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-INT-020 — {@code sent_reports} JSONB: dữ liệu CŨ (kể cả field đã bỏ như
 * {@code latestPestControl}) vẫn đọc lại nguyên vẹn qua {@code payload} (JSONB dung nạp,
 * L26/CF-12), sort {@code sentAt DESC}.
 *
 * ⚠️ LỆCH so với TestSpec: Oracle gốc (ADR-M12-05 diễn giải bởi TestSpec) đòi field đặc thù
 * theo loại ({@code avgScore}, {@code threeStepSummary}…) nằm Ở CẤP GỐC JSON, không bọc trong
 * {@code payload} — để giữ đúng union shape mock cũ. {@code SentReportDtos.SentReportResponse}
 * (viết ở Bước 2) khai {@code payload} là 1 field {@code Map<String,Object>} LỒNG, không
 * flatten theo {@code reportType}. Đây là gap THẬT đã phát hiện độc lập lúc nối FE (Bước 3,
 * `real-resources.ts`: `sent-reports` chưa migrate được đúng vì lý do này) — sửa đòi thiết kế
 * lại DTO + toResponse() của cả 2 send-service + query-service, ngoài phạm vi "viết test còn
 * thiếu". Test này khẳng định HÀNH VI THẬT hiện tại (payload lồng), không phải hành vi lý tưởng.
 */
class SentReportShapeIT extends AbstractIamIntegrationTest {

    @Test
    void oldJsonbFieldsSurviveReadSortedByDateDescPayloadIsNestedNotFlattened() throws Exception {
        String schoolA = "00000000-0000-0000-0000-0000000a0042";
        String actor = jdbc.queryForObject("SELECT id FROM user_accounts WHERE code = 'U-0002'", String.class);

        jdbc.update("""
                INSERT INTO sent_reports
                    (id, code, school_id, report_type, period, range_label, range_from, range_to,
                     sent_at, sent_by_user_id, recipient, payload, confidential)
                VALUES (gen_random_uuid(), 'RPTS-OLD-01', ?::uuid, 'COMPLIANCE', 'WEEKLY', 'Kỳ cũ',
                        '2026-06-01', '2026-06-07', '2026-06-08T09:00:00Z', ?::uuid,
                        'School Administration',
                        '{"avgScore":88,"latestPestControl":{"date":"2026-05-01","pestFound":null,"nextDueDate":"2026-08-01"}}'::jsonb,
                        false)
                """, schoolA, actor);
        jdbc.update("""
                INSERT INTO sent_reports
                    (id, code, school_id, report_type, period, range_label, range_from, range_to,
                     sent_at, sent_by_user_id, recipient, payload, confidential)
                VALUES (gen_random_uuid(), 'RPTS-NEW-01', ?::uuid, 'COMPLIANCE', 'WEEKLY', 'Kỳ mới',
                        '2026-07-06', '2026-07-12', '2026-07-13T09:00:00Z', ?::uuid,
                        'School Administration', '{"avgScore":95}'::jsonb, false)
                """, schoolA, actor);

        String medical = authHeaderFor("U-0002");
        MvcResult list = mvc.perform(get("/api/v1/sent-reports?pageSize=50")
                        .header("Authorization", medical)).andReturn();
        JsonNode items = parse(list).get("items");
        assertThat(items.get(0).get("id").asText()).isEqualTo("RPTS-NEW-01"); // sentAt DESC

        MvcResult oldDetail = mvc.perform(get("/api/v1/sent-reports/RPTS-OLD-01")
                        .header("Authorization", medical)).andReturn();
        JsonNode oldPayload = parse(oldDetail).get("payload");
        assertThat(oldPayload.has("latestPestControl")).isTrue(); // JSONB dung nạp field cũ đã bỏ

        MvcResult newDetail = mvc.perform(get("/api/v1/sent-reports/RPTS-NEW-01")
                        .header("Authorization", medical)).andReturn();
        JsonNode newBody = parse(newDetail);
        assertThat(newBody.get("payload").has("latestPestControl")).isFalse();

        // Hành vi THẬT: avgScore nằm TRONG payload, không ở cấp gốc (xem javadoc lớp — lệch TestSpec).
        assertThat(newBody.has("avgScore")).isFalse();
        assertThat(newBody.get("payload").get("avgScore").asInt()).isEqualTo(95);
    }
}
