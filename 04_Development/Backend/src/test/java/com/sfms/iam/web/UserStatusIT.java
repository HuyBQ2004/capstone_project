package com.sfms.iam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-006 — Deactivate user ⇒ revoke refresh tức thì + MSG-029 (UC-003).
 *
 * <p>Oracle: UC-003 Postconditions · BR-004 · MSG-029 (⚠️ CF-6 ngữ nghĩa lệch) ·
 * TDS §7.1 event {@code UserAccountDeactivated} · TDS §6.3 transition ACTIVE→INACTIVE.
 *
 * <p>Path param dùng {@code code} (D1), không UUID.
 */
class UserStatusIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("MF01-TC-INT-006 — deactivate revoke refresh tức thì + MSG-029")
    class Tc_Int_006 {

        @Test
        @DisplayName("Deactivate teacher: 200 MSG-029; refresh_tokens revoked; login bị chặn 403")
        void deactivateTeacherRevokesSession() throws Exception {
            // Teacher login trước — lấy refresh token đang sống
            JsonNode teacherLogin = login("trangnt@dtd.edu.vn", PWD);
            String rT = teacherLogin.get("refreshToken").asText();

            // System Admin deactivate U-0004 (Teacher)
            mvc.perform(patch("/api/v1/users/U-0004/status")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("active", false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MSG_029));  // ⚠️ CF-6

            // DB: status = INACTIVE
            String status = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE code='U-0004'", String.class);
            assertThat(status).isEqualTo("INACTIVE");

            // Refresh token bị revoke tức thì (BR-004)
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", rT))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"));

            // Login lại bị chặn (BR-010)
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "trangnt@dtd.edu.vn", "password", PWD))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-003"));
        }

        @Test
        @DisplayName("Reactivate: login lại thành công — INACTIVE → ACTIVE (TDS §6.3)")
        void reactivateAllowsLogin() throws Exception {
            // Deactivate
            mvc.perform(patch("/api/v1/users/U-0004/status")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("active", false)))).andReturn();

            // Reactivate
            mvc.perform(patch("/api/v1/users/U-0004/status")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("active", true))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(MSG_029));

            // Login thành công sau reactivate
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "trangnt@dtd.edu.vn", "password", PWD))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Path param UUID → 404 IAM-404 (D1: path param là mã nghiệp vụ)")
        void uuidPathParamReturns404() throws Exception {
            mvc.perform(patch("/api/v1/users/00000000-0000-0000-0000-000000000001/status")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("active", false))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("IAM-404"))
                    .andExpect(jsonPath("$.error.message").value(MSG_137));
        }

        @Test
        @DisplayName("audit_logs có dòng deactivate (UC-003 Postconditions)")
        void auditLogRecorded() throws Exception {
            mvc.perform(patch("/api/v1/users/U-0004/status")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("active", false)))).andReturn();

            long auditRows = count(
                    "SELECT count(*) FROM audit_logs WHERE action LIKE '%DEACTIVATE%' OR action LIKE '%STATUS%'");
            assertThat(auditRows).isGreaterThanOrEqualTo(1);
        }
    }
}
