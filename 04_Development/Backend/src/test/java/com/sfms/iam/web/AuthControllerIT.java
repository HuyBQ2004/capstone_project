package com.sfms.iam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF01-TC-INT-001 · MF01-TC-INT-005 — {@code POST /auth/login}, {@code POST /auth/logout}.
 */
class AuthControllerIT extends AbstractIamIntegrationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-INT-001 — Login happy path: response shape + D1/D2/D4/D6 + trạng thái DB
    //   Oracle: UC-034 bước 8 · BR-004 · MSG-026 · TDS §9.2 · D1/D3/D4/D6
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-INT-001 — login happy path")
    class Tc_Int_001 {

        @Test
        @DisplayName("200 + đúng 6 khóa TDS §9.2; role kebab-case; schoolId là mã nghiệp vụ")
        void loginReturnsExactShape() throws Exception {
            MvcResult result = mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", PWD))))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            assertThat(setOf(body.fieldNames())).containsExactlyInAnyOrder(
                    "accessToken", "refreshToken", "role", "schoolId", "partnerId", "needTermsAccept");

            assertThat(body.get("role").asText()).isEqualTo("school-admin");            // D6
            assertThat(body.get("schoolId").asText()).isEqualTo("SCH-HN-0042");         // D1
            assertThat(body.get("partnerId").isNull()).isTrue();
            assertThat(body.toString()).doesNotContain("passwordHash", "googleSub");
        }

        @Test
        @DisplayName("Claim JWT: sub/uid/role/schoolId/partnerId; exp - iat = 1800s (BR-004)")
        void jwtClaimsMatchPolicy() throws Exception {
            JsonNode body = login("toanpq@dtd.edu.vn", PWD);
            String[] parts = body.get("accessToken").asText().split("\\.");
            JsonNode claims = json.readTree(Base64.getUrlDecoder().decode(parts[1]));

            assertThat(claims.get("sub").asText()).isEqualTo("toanpq@dtd.edu.vn");
            assertThat(claims.has("uid")).isTrue();
            assertThat(claims.get("role").asText()).isEqualTo("school-admin");
            assertThat(claims.get("schoolId").asText()).isEqualTo("SCH-HN-0042");
            assertThat(claims.get("exp").asLong() - claims.get("iat").asLong()).isEqualTo(1800);
        }

        @Test
        @DisplayName("DB: 1 refresh token còn hiệu lực, KHÔNG lưu token thô, expires_at = +14 ngày")
        void refreshTokenPersistedCorrectly() throws Exception {
            JsonNode body = login("toanpq@dtd.edu.vn", PWD);
            String rawRefresh = body.get("refreshToken").asText();

            // ⚠️ Mốc so sánh lấy từ MutableClock, KHÔNG dùng now() của Postgres: đồng hồ test bị
            // ghim ở 2026-07-30T02:00:00Z nên expires_at = +14 ngày = 2026-08-13, trong khi now()
            // là giờ máy thật. Dùng now() thì assertion này tự đỏ kể từ 2026-08-14 (time bomb) —
            // xem cùng lý do đã ghi ở ReportExportGuardIT (MF13).
            long usable = count("""
                SELECT count(*) FROM refresh_tokens
                 WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001')
                   AND revoked_at IS NULL AND expires_at > ?
                """, Timestamp.from(clock.instant()));
            assertThat(usable).isEqualTo(1);

            long rawStored = count("SELECT count(*) FROM refresh_tokens WHERE token_hash = ?", rawRefresh);
            assertThat(rawStored).isZero();

            Boolean withinWindow = jdbc.queryForObject("""
                SELECT (expires_at - created_at)
                       BETWEEN interval '13 days 23 hours' AND interval '14 days 1 hour'
                  FROM refresh_tokens WHERE revoked_at IS NULL
                """, Boolean.class);
            assertThat(withinWindow).isTrue();
        }

        @Test
        @DisplayName("DB: lastLogin cập nhật, counter reset; audit_logs có dòng SUCCESS LOGIN")
        void lastLoginAndAuditUpdated() throws Exception {
            login("toanpq@dtd.edu.vn", PWD);

            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT failed_login_count, locked_until, last_login IS NOT NULL AS has_login "
                  + "FROM user_accounts WHERE code='U-0001'");
            assertThat(row.get("failed_login_count")).isEqualTo(0);
            assertThat(row.get("locked_until")).isNull();
            assertThat(row.get("has_login")).isEqualTo(true);

            long auditRows = count(
                    "SELECT count(*) FROM audit_logs WHERE result='SUCCESS' AND action LIKE '%LOGIN%'");
            assertThat(auditRows).isGreaterThanOrEqualTo(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-INT-005 — Logout ⇒ revoke refresh + MSG-027
    //   Oracle: UC-035 bước 4–5 · BR-004 · MSG-027 · TDS §8.1
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-INT-005 — logout revoke refresh (UC-035)")
    class Tc_Int_005 {

        @Test
        @DisplayName("200 + MSG-027; refresh_tokens.revoked_at IS NOT NULL; 0 refresh còn hiệu lực")
        void logoutRevokesRefreshToken() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);

            mvc.perform(post("/api/v1/auth/logout")
                            .header("Authorization", bearer(login.get("accessToken").asText())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logout successful. Session terminated safely."));

            long usable = count("""
                SELECT count(*) FROM refresh_tokens
                 WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001')
                   AND revoked_at IS NULL
                """);
            assertThat(usable).isZero();
        }

        @Test
        @DisplayName("Refresh sau logout ⇒ 401 IAM-008")
        void refreshAfterLogoutFails() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);

            mvc.perform(post("/api/v1/auth/logout")
                    .header("Authorization", bearer(login.get("accessToken").asText())));

            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", login.get("refreshToken").asText()))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"));
        }

        @Test
        @DisplayName("Logout lần 2 (cùng access token) idempotent — không tạo dòng revoke mới, không 500")
        void logoutTwiceIsIdempotent() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String auth = bearer(login.get("accessToken").asText());

            mvc.perform(post("/api/v1/auth/logout").header("Authorization", auth))
                    .andExpect(status().isOk());

            mvc.perform(post("/api/v1/auth/logout").header("Authorization", auth))
                    .andExpect(status().isOk());
        }

        /** TDS §17 C2 — userId lấy từ JWT, gửi userId khác trong body không có tác dụng. */
        @Test
        @DisplayName("Body userId khác không logout được người khác")
        void bodyUserIdIsIgnored() throws Exception {
            JsonNode loginA = login("toanpq@dtd.edu.vn", PWD);
            login("trangnt@dtd.edu.vn", PWD);   // tạo phiên cho U-0004, dùng để assert nó KHÔNG bị đụng tới

            mvc.perform(post("/api/v1/auth/logout")
                            .header("Authorization", bearer(loginA.get("accessToken").asText()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("userId", "U-0004"))))
                    .andExpect(status().isOk());

            long teacherUsable = count("""
                SELECT count(*) FROM refresh_tokens
                 WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0004')
                   AND revoked_at IS NULL
                """);
            assertThat(teacherUsable).isEqualTo(1);
        }
    }

    private Set<String> setOf(java.util.Iterator<String> it) {
        java.util.Set<String> set = new java.util.HashSet<>();
        it.forEachRemaining(set::add);
        return set;
    }
}
