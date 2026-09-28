package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF13-TC-INT-002 (★) — BR-182 (rate tính từ nguồn, KHÔNG hằng số) + BR-176 (cách ly trường).
 * MF13-TC-INT-003 — D4: {@code generatedAt} ISO-8601, không {@code dd/MM/yyyy}.
 */
class ComplianceReportIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_A = "00000000-0000-0000-0000-0000000a0042";

    @Test
    void rateIsComputedFromSourceNotHardcoded() throws Exception {
        // 4 mẫu, 1 quá hạn (OVERDUE) -> rate = 75, alerts = 1 (BR-182 — KHÔNG phải hằng số 100 của mock)
        for (int i = 0; i < 3; i++) {
            insertFoodSample("SMP-OK-" + i, "RETAINED");
        }
        insertFoodSample("SMP-BAD-0", "OVERDUE");

        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin)
                        .param("dateFrom", "2026-06-01").param("dateTo", "2026-07-30"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        var sampleRetention = findModule(body, "sample-retention");
        assertThat(sampleRetention.get("total").asInt()).isEqualTo(4);
        assertThat(sampleRetention.get("alerts").asInt()).isEqualTo(1);
        assertThat(sampleRetention.get("rate").asInt()).isEqualTo(75);

        // D4 — ISO-8601, không phải "dd/MM/yyyy HH:mm" của mock (CF-M13-7b)
        assertThat(body.get("generatedAt").asText()).matches("^\\d{4}-\\d{2}-\\d{2}T.*Z$");
        assertThat(body.get("periodFrom").asText()).isEqualTo("2026-06-01");
        assertThat(body.get("periodTo").asText()).isEqualTo("2026-07-30");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF13-TC-SEC-007 (bước 1) — School Admin xin allSchools=true "
            + "⇒ 403 RPT-005/MSG-005")
    void schoolAdminCannotRequestAllSchoolsScope() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin).param("allSchools", "true"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("RPT-005");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF13-TC-SEC-007 (bước 2) — System Admin xin allSchools=true "
            + "⇒ 200 tổng hợp toàn nền tảng (BR-176)")
    void systemAdminCanRequestAllSchoolsScope() throws Exception {
        String sysAdmin = authHeaderFor("U-9001");
        MvcResult result = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", sysAdmin).param("allSchools", "true"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/compliance-report")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    private com.fasterxml.jackson.databind.JsonNode findModule(com.fasterxml.jackson.databind.JsonNode body,
                                                                String slug) {
        for (var module : body.get("modules")) {
            if (module.get("slug").asText().equals(slug)) {
                return module;
            }
        }
        throw new AssertionError("module " + slug + " not found in response: " + body);
    }

    private void insertFoodSample(String code, String status) {
        jdbc.update("""
                INSERT INTO food_samples (id, code, school_id, inspection_id, dish_id, dish_name, meal_type,
                    meal_shift, servings, sample_amount, sample_unit, container, sanitized, sealed, fridge_temp,
                    collected_at, collected_by, storage_location, retention_deadline, status)
                VALUES (gen_random_uuid(), ?, ?::uuid, gen_random_uuid(), gen_random_uuid(), 'Canh bi do',
                    'LUNCH', 'Trua', 100, 150, 'G', 'Hop nhua', true, true, 4.0,
                    TIMESTAMPTZ '2026-07-06T10:30:00Z', 'Nguyen Van A', 'Tu lanh bep', now() + INTERVAL '1 day', ?)
                """, code, SCHOOL_A, status);
    }
}
