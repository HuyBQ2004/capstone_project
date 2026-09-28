package com.sfms.mf12.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-INT-008 — Filter {@code audit-logs}: {@code ip}, khoảng ngày, {@code search} trên
 * đúng {@code SEARCH_FIELDS} ({@code actor,action,resource,ip}), KHÔNG quét {@code entry_hash}.
 */
class AuditFilterIT extends AbstractIamIntegrationTest {

    private void seedFixtures() {
        jdbc.update("""
                INSERT INTO audit_logs (code, timestamp, actor, ip, action, resource, result)
                VALUES ('AUD-FX-001', '2026-07-02T10:18:44Z', 'a@x.vn', '10.20.1.44', 'CREATE FoodSample', 'Food Sample', 'SUCCESS')
                """);
        jdbc.update("""
                INSERT INTO audit_logs (code, timestamp, actor, ip, action, resource, result)
                VALUES ('AUD-FX-002', '2026-07-02T07:46:02Z', 'toanpq@dtd.edu.vn', '10.20.3.12', 'LOGIN', 'Authentication', 'SUCCESS')
                """);
        jdbc.update("""
                INSERT INTO audit_logs (code, timestamp, actor, ip, action, resource, result)
                VALUES ('AUD-FX-003', '2026-07-01T22:14:37Z', 'unknown@mail.com', '203.113.99.87', 'LOGIN', 'Authentication', 'BLOCKED')
                """);
    }

    @Test
    void filtersByIpResultDateRangeAndSearch() throws Exception {
        seedFixtures();
        String admin = authHeaderFor("U-9001");

        MvcResult byIp = mvc.perform(get("/api/v1/audit-logs?ip=203.113.99.87")
                        .header("Authorization", admin)).andReturn();
        JsonNode byIpItems = parse(byIp).get("items");
        assertThat(byIpItems).hasSize(1);
        assertThat(byIpItems.get(0).get("id").asText()).isEqualTo("AUD-FX-003");

        MvcResult byResult = mvc.perform(get("/api/v1/audit-logs?result=BLOCKED")
                        .header("Authorization", admin)).andReturn();
        JsonNode byResultItems = parse(byResult).get("items");
        assertThat(byResultItems).allSatisfy(n -> assertThat(n.get("result").asText()).isEqualTo("BLOCKED"));

        MvcResult byRange = mvc.perform(get("/api/v1/audit-logs?dateFrom=2026-07-01&dateTo=2026-07-02")
                        .header("Authorization", admin)).andReturn();
        JsonNode byRangeItems = parse(byRange).get("items");
        assertThat(codesOf(byRangeItems)).contains("AUD-FX-001");

        MvcResult bySearch = mvc.perform(get("/api/v1/audit-logs?search=LOGIN")
                        .header("Authorization", admin)).andReturn();
        JsonNode bySearchItems = parse(bySearch).get("items");
        assertThat(codesOf(bySearchItems)).contains("AUD-FX-002", "AUD-FX-003").doesNotContain("AUD-FX-001");

        // search KHÔNG được quét entry_hash/prev_hash (TC-SEC-017) — tìm 1 chuỗi bất kỳ, phải = 0.
        MvcResult searchHash = mvc.perform(get("/api/v1/audit-logs?search=" + hashLikeNeedle())
                        .header("Authorization", admin)).andReturn();
        assertThat(parse(searchHash).get("total").asLong()).isEqualTo(0);
    }

    private static String hashLikeNeedle() {
        return "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8";
    }

    private static java.util.List<String> codesOf(JsonNode items) {
        java.util.List<String> out = new java.util.ArrayList<>();
        items.forEach(n -> out.add(n.get("id").asText()));
        return out;
    }
}
