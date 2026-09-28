package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-INT-021 — D1: path param dùng {@code code}, JSON trả {@code id}=code (không lộ UUID);
 * PK DB vẫn {@code UUID}; {@code external_integrations} UNIQUE(code), KHÔNG UNIQUE(school_id, code)
 * vì bảng cấu hình cấp nền tảng, không có {@code school_id} (L11/CF-7).
 */
class IdentityContractIT extends AbstractIamIntegrationTest {

    @Test
    void pathAndJsonUseCodeNotUuidPkStaysUuidInDb() throws Exception {
        String admin = authHeaderFor("U-9001");
        login("sysadmin@sfms.vn", PWD);
        String code = jdbc.queryForObject(
                "SELECT code FROM audit_logs ORDER BY timestamp DESC LIMIT 1", String.class);

        MvcResult byCode = mvc.perform(get("/api/v1/audit-logs/" + code).header("Authorization", admin))
                .andReturn();
        assertThat(byCode.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(byCode).get("id").asText()).isEqualTo(code);
        assertThat(parse(byCode).has("code")).isFalse();

        for (String table : List.of("audit_logs", "error_logs", "sent_reports", "checklist_groups",
                "external_integrations")) {
            String dataType = jdbc.queryForObject("""
                    SELECT data_type FROM information_schema.columns
                     WHERE column_name = 'id' AND table_name = ?
                    """, String.class, table);
            assertThat(dataType).as(table + ".id").isEqualTo("uuid");
        }

        MvcResult integrations = mvc.perform(get("/api/v1/integrations").header("Authorization", admin))
                .andReturn();
        List<String> ids = new java.util.ArrayList<>();
        parse(integrations).forEach(n -> ids.add(n.get("id").asText()));
        assertThat(ids).contains("gemini", "gps", "gmail", "firebase");

        // external_integrations: UNIQUE(code), KHÔNG UNIQUE(school_id, code) — không có school_id.
        Long hasSchoolIdColumn = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                 WHERE table_name = 'external_integrations' AND column_name = 'school_id'
                """, Long.class);
        assertThat(hasSchoolIdColumn).isEqualTo(0);
        Long uniqueCodeIndex = jdbc.queryForObject("""
                SELECT count(*) FROM pg_indexes
                 WHERE tablename = 'external_integrations' AND indexdef ILIKE '%UNIQUE%code%'
                """, Long.class);
        assertThat(uniqueCodeIndex).isGreaterThanOrEqualTo(1);

        // Path chỉ nhận code — gửi UUID ngẫu nhiên phải 404, không tự "thông minh" đoán theo id.
        MvcResult byUuid = mvc.perform(get("/api/v1/audit-logs/" + UUID.randomUUID())
                        .header("Authorization", admin))
                .andReturn();
        assertThat(byUuid.getResponse().getStatus()).isEqualTo(404);
    }
}
