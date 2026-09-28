package com.sfms.iam.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-002 — Không có JWT ⇒ 401 trên mọi endpoint có bảo vệ.
 * MF01-TC-SEC-003 — Access token hết hạn ⇒ 401 (không gia hạn ngầm ở access token).
 *
 * <p>Oracle SEC-002: MSG-004 · TDS §10 IAM-005 · TDS §15.2.
 * Oracle SEC-003: BR-004 · MSG-004 · TDS §10 IAM-005 · TDS §17 C3 (TTL từ SecurityPolicy).
 */
class JwtFilterIT extends AbstractIamIntegrationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-SEC-002 — Không JWT ⇒ 401 trên endpoint bảo vệ
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-SEC-002 — không JWT ⇒ 401 IAM-005 trên endpoint bảo vệ")
    class Tc_Sec_002 {

        @Test
        @DisplayName("GET /users không JWT ⇒ 401 IAM-005 + MSG-004")
        void getUsersNoJwt() throws Exception {
            mvc.perform(get("/api/v1/users"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"))
                    .andExpect(jsonPath("$.error.message").value(MSG_004));
        }

        @Test
        @DisplayName("POST /users không JWT ⇒ 401 IAM-005")
        void postUsersNoJwt() throws Exception {
            mvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"a@b.vn\",\"name\":\"X\",\"role\":\"teacher\",\"schoolId\":\"SCH-HN-0042\",\"phone\":\"0912345678\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("GET /staff không JWT ⇒ 401 IAM-005")
        void getStaffNoJwt() throws Exception {
            mvc.perform(get("/api/v1/staff"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("GET /roles không JWT ⇒ 401 IAM-005")
        void getRolesNoJwt() throws Exception {
            mvc.perform(get("/api/v1/roles"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("GET /permissions không JWT ⇒ 401 IAM-005")
        void getPermissionsNoJwt() throws Exception {
            mvc.perform(get("/api/v1/permissions"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("GET /profile/me không JWT ⇒ 401 IAM-005")
        void getProfileNoJwt() throws Exception {
            mvc.perform(get("/api/v1/profile/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("POST /auth/logout không JWT ⇒ 401 IAM-005")
        void logoutNoJwt() throws Exception {
            mvc.perform(post("/api/v1/auth/logout"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("PATCH /account/password không JWT ⇒ 401")
        void changePasswordNoJwt() throws Exception {
            mvc.perform(patch("/api/v1/account/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"currentPassword\":\"old\",\"newPassword\":\"NewPwd@1\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Endpoint public POST /auth/login — không bị 401 khi không có JWT")
        void publicLoginNotBlocked() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", PWD))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Endpoint public POST /auth/forgot-password — không bị 401")
        void publicForgotPasswordNotBlocked() throws Exception {
            mvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Bearer header rỗng ⇒ 401, không 500")
        void emptyBearerReturns401() throws Exception {
            mvc.perform(get("/api/v1/users")
                            .header("Authorization", "Bearer "))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Authorization: Basic ... ⇒ 401 (không phải Bearer JWT)")
        void basicAuthReturns401() throws Exception {
            mvc.perform(get("/api/v1/users")
                            .header("Authorization", "Basic dXNlcjpwYXNz"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-SEC-003 — Access token hết hạn ⇒ 401
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-SEC-003 — access token hết hạn (boundary 30') ⇒ 401 IAM-005")
    class Tc_Sec_003 {

        @Test
        @DisplayName("Token đúng 1800s: exp-iat == 1800 (BR-004 assertion cốt lõi)")
        void accessTokenExpiry1800Seconds() throws Exception {
            com.fasterxml.jackson.databind.JsonNode body = login("toanpq@dtd.edu.vn", PWD);
            String[] parts = body.get("accessToken").asText().split("\\.");
            com.fasterxml.jackson.databind.JsonNode claims =
                    json.readTree(java.util.Base64.getUrlDecoder().decode(parts[1]));

            long diff = claims.get("exp").asLong() - claims.get("iat").asLong();
            org.assertj.core.api.Assertions.assertThat(diff).isEqualTo(1800L);
        }

        @Test
        @DisplayName("(2) T0+29'59\": token vẫn hợp lệ")
        void tokenValidAt29m59s() throws Exception {
            com.fasterxml.jackson.databind.JsonNode body = login("toanpq@dtd.edu.vn", PWD);
            String token = body.get("accessToken").asText();

            // Di chuyển clock tới T0 + 29'59"
            clock.advance(Duration.ofSeconds(29 * 60 + 59));

            mvc.perform(get("/api/v1/profile/me")
                            .header("Authorization", bearer(token)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("(4) T0+30'01\": token hết hạn ⇒ 401 IAM-005 + MSG-004")
        void expiredTokenAt30m01s() throws Exception {
            com.fasterxml.jackson.databind.JsonNode body = login("toanpq@dtd.edu.vn", PWD);
            String token = body.get("accessToken").asText();

            // Di chuyển clock qua mốc hết hạn
            clock.advance(Duration.ofSeconds(30 * 60 + 1));

            mvc.perform(get("/api/v1/profile/me")
                            .header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"))
                    .andExpect(jsonPath("$.error.message").value(MSG_004));
        }

        /**
         * (5) TestSpec MF01-TC-SEC-003 — chứng minh TTL đọc từ {@code SecurityPolicy}, KHÔNG
         * hardcode 30 (TDS §17 C3). Đổi policy còn 10 phút, login MỚI phải phát hành token theo
         * TTL mới; hardcode sẽ khiến màn {@code security-policies} của System Admin thành trang trí.
         */
        @Test
        @DisplayName("(5) sessionTimeoutMinutes=10 → login mới → hết hạn ở phút 11 (TTL không hardcode)")
        void sessionTimeoutIsReadFromPolicyNotHardcoded() throws Exception {
            jdbc.update("UPDATE security_policies SET session_timeout_minutes = 10 WHERE code = 'SEC-DEFAULT'");

            com.fasterxml.jackson.databind.JsonNode body = login("toanpq@dtd.edu.vn", PWD);
            String token = body.get("accessToken").asText();
            String[] parts = token.split("\\.");
            com.fasterxml.jackson.databind.JsonNode claims =
                    json.readTree(java.util.Base64.getUrlDecoder().decode(parts[1]));
            org.assertj.core.api.Assertions.assertThat(
                    claims.get("exp").asLong() - claims.get("iat").asLong()).isEqualTo(600L);

            clock.advance(Duration.ofMinutes(11));

            mvc.perform(get("/api/v1/profile/me").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("Token hết hạn KHÔNG được tự gia hạn — phải dùng /auth/refresh")
        void expiredAccessNotSelfRenewed() throws Exception {
            com.fasterxml.jackson.databind.JsonNode body = login("toanpq@dtd.edu.vn", PWD);
            String token = body.get("accessToken").asText();
            clock.advance(Duration.ofSeconds(30 * 60 + 1));

            // Gọi nhiều lần — không được tự renew
            mvc.perform(get("/api/v1/profile/me")
                    .header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized());

            mvc.perform(get("/api/v1/profile/me")
                    .header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
