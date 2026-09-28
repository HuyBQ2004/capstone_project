package com.sfms.mf12.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF12-TC-SEC-016 — Phân quyền THEO FIELD (§16 bảng 4 nhóm). 8 hàng gốc, hàng (f)
 * {@code checklist-groups} KHÔNG hiện thực ở đây (bảng/endpoint thuộc MF07 từ {@code M12-D02}
 * — MF07-TestSpec đã phủ) — còn 7/8 hàng thuộc phạm vi MF12.
 */
class FieldLevelAuthorizationTest extends AbstractIamIntegrationTest {

    @Test
    void aValidWriteBySysAdminSucceeds() throws Exception {
        MvcResult r = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpoint\":\"https://x\"}"))
                .andReturn();
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void bConnectedTrueFromClientIsIgnoredButStillReturns200() throws Exception {
        MvcResult r = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpoint\":\"https://x\",\"connected\":true}"))
                .andReturn();
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(r).get("connected").asBoolean()).isFalse();
    }

    @Test
    void cMedicalStaffCannotWriteIntegrations() throws Exception {
        MvcResult r = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", authHeaderFor("U-0002"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpoint\":\"https://x\"}"))
                .andReturn();
        assertThat(r.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void dSysAdminCanWriteSecurityPolicies() throws Exception {
        MvcResult r = mvc.perform(put("/api/v1/security-policies/SEC-DEFAULT")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxLoginAttempts\":6}"))
                .andReturn();
        assertThat(r.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void eSchoolAdminCannotWriteSecurityPolicies() throws Exception {
        MvcResult r = mvc.perform(put("/api/v1/security-policies/SEC-DEFAULT")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxLoginAttempts\":6}"))
                .andReturn();
        assertThat(r.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void gSentByFromClientIsIgnoredTakenFromJwt() throws Exception {
        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id, result)
                SELECT gen_random_uuid(), 'KIN-FL-01', '00000000-0000-0000-0000-0000000a0042',
                       '2026-07-08', 90, u.id, 'PASSED'
                FROM user_accounts u WHERE u.code = 'U-0002'
                """);
        MvcResult r = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", authHeaderFor("U-0002"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rangeFrom\":\"2026-07-06\",\"rangeTo\":\"2026-07-12\","
                                + "\"sentBy\":\"attacker@x.vn\"}"))
                .andReturn();
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        assertThat(parse(r).get("sentBy").asText()).isEqualTo("hant@dtd.edu.vn");
    }

    @Test
    void hSchoolIdFromClientIsIgnoredTakenFromJwt() throws Exception {
        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id, result)
                SELECT gen_random_uuid(), 'KIN-FL-02', '00000000-0000-0000-0000-0000000a0042',
                       '2026-07-08', 90, u.id, 'PASSED'
                FROM user_accounts u WHERE u.code = 'U-0002'
                """);
        MvcResult r = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", authHeaderFor("U-0002")) // medical-staff SCH-HN-0042
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rangeFrom\":\"2026-07-06\",\"rangeTo\":\"2026-07-12\","
                                + "\"schoolId\":\"00000000-0000-0000-0000-0000000a0051\"}"))
                .andReturn();
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        String code = parse(r).get("id").asText();
        String schoolId = jdbc.queryForObject(
                "SELECT school_id::text FROM sent_reports WHERE code = ?", String.class, code);
        assertThat(schoolId).isEqualTo("00000000-0000-0000-0000-0000000a0042"); // trường của JWT, không phải body
    }
}
