package com.sfms.iam.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-010 — Không lộ MK/token/googleSub trong response (toàn bộ DTO response MF01).
 *
 * <p>Oracle: BR-001 · TDS §1 Data Classification · TDS §14.2 · TDS §12.1 · D1 (UUID không lộ).
 * <p>OWASP: A02:2021 · CWE: CWE-200.
 */
@DisplayName("MF01-TC-SEC-010 — Không lộ sensititve field trong response (toàn DTO MF01)")
class SensitiveFieldLeakIT extends AbstractIamIntegrationTest {

    private static final List<String> FORBIDDEN_KEYS = List.of(
            "passwordHash", "password_hash", "password",
            "googleSub", "google_sub",
            "tokenHash", "token_hash",
            "clientSecret", "client-secret"
    );

    private static final Pattern UUID_REGEX =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Nested
    @DisplayName("Tất cả endpoint MF01 không lộ sensitive field")
    class Tc_Sec_010 {

        @Test
        @DisplayName("POST /auth/login response không chứa passwordHash/googleSub; id không phải UUID")
        void loginResponseNoSensitiveData() throws Exception {
            MvcResult result = mvc.perform(post("/api/v1/auth/login")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", PWD))))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("GET /profile/me response không chứa sensitive field")
        void profileMeNoSensitiveLeak() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/profile/me")
                            .header("Authorization", authHeaderFor("U-0001")))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("GET /users response không chứa sensitive field; id = mã nghiệp vụ D1")
        void userListNoSensitiveLeak() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("POST /users response không chứa sensitive field")
        void createUserNoSensitiveLeak() throws Exception {
            MvcResult result = mvc.perform(post("/api/v1/users")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "Test Leak",
                                    "email", "testleak@dtd.edu.vn",
                                    "phone", "0912111999",
                                    "role", "teacher",
                                    "schoolId", "SCH-HN-0042"
                            ))))
                    .andExpect(status().isCreated()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("GET /users/{code} response không chứa sensitive field")
        void getUserDetailNoSensitiveLeak() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users/U-0001")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("GET /staff response không chứa sensitive field")
        void staffListNoSensitiveLeak() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/staff")
                            .header("Authorization", authHeaderFor("U-0001")))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("GET /roles response không chứa sensitive field")
        void rolesNoSensitiveLeak() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/roles")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Error response 401/403/404/422 không chứa stack trace / SQL / tên class Java")
        void errorResponseNoStackTrace() throws Exception {
            // 403 error
            MvcResult r403 = mvc.perform(get("/api/v1/users")
                    .header("Authorization", authHeaderFor("U-0004"))).andReturn();
            assertNoServerErrorLeak(r403.getResponse().getContentAsString());

            // 404 error
            MvcResult r404 = mvc.perform(get("/api/v1/users/U-9999")
                    .header("Authorization", authHeaderFor("U-9001"))).andReturn();
            assertNoServerErrorLeak(r404.getResponse().getContentAsString());

            // 422 error
            MvcResult r422 = mvc.perform(post("/api/v1/users")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content("{}")).andReturn();
            assertNoServerErrorLeak(r422.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("POST /auth/refresh response không chứa sensitive field")
        void refreshResponseNoSensitiveLeak() throws Exception {
            JsonNode loginBody = login("toanpq@dtd.edu.vn", PWD);
            String r1 = loginBody.get("refreshToken").asText();

            MvcResult result = mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", r1))))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("GET /permissions response không chứa sensitive field")
        void permissionsNoSensitiveLeak() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/permissions")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk()).andReturn();

            assertNoSensitiveLeak(result.getResponse().getContentAsString());
        }

        // ── Assertions ──────────────────────────────────────────────────────

        private void assertNoSensitiveLeak(String jsonBody) {
            // 1. Không có field nhạy cảm
            FORBIDDEN_KEYS.forEach(k ->
                    assertThat(jsonBody)
                            .as("Response không được chứa key '%s'", k)
                            .doesNotContain("\"" + k + "\""));

            // 2. D1: không lộ UUID nội bộ trong field id (bất kỳ `"id":"<uuid>"`)
            var matcher = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(jsonBody);
            while (matcher.find()) {
                String idValue = matcher.group(1);
                assertThat(idValue)
                        .as("Field 'id' không được là UUID nội bộ: '%s'", idValue)
                        .doesNotMatch(UUID_REGEX.pattern());
            }
        }

        private void assertNoServerErrorLeak(String jsonBody) {
            assertThat(jsonBody)
                    .doesNotContain("at com.sfms")
                    .doesNotContain("Exception")
                    .doesNotContain("SELECT")
                    .doesNotContain("org.postgresql")
                    .doesNotContain("HibernateException")
                    .doesNotContain("StackTrace")
                    .doesNotContain("trace");
        }
    }
}
