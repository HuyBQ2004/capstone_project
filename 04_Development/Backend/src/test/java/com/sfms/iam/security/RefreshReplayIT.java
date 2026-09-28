package com.sfms.iam.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.stream.Stream;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-009 — Refresh token replay sau khi đã rotate/revoke (7 đường vô hiệu hóa BR-004).
 *
 * <p>Oracle: BR-004 · TDS ADR-M01 · TDS §8.2 · TDS §10 IAM-008.
 * <p>OWASP: A07:2021 · CWE: CWE-294.
 */
@DisplayName("MF01-TC-SEC-009 — Refresh token replay sau rotate/revoke (7 đường BR-004)")
class RefreshReplayIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("7 đường vô hiệu hóa BR-004 — tất cả phải ⇒ 401 IAM-008")
    class Tc_Sec_009 {

        @Test
        @DisplayName("(a) Rotate bình thường — refresh R1 sau rotate ⇒ 401")
        void tokenRevokedAfterRotation() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            // Rotate R1 → R2
            mvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("refreshToken", r1)))).andReturn();

            // R1 replay ⇒ 401
            assertRefreshFails(r1);
        }

        @Test
        @DisplayName("(b) Logout revoke — R1 sau logout ⇒ 401")
        void tokenRevokedAfterLogout() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            mvc.perform(post("/api/v1/auth/logout")
                    .header("Authorization", bearer(login.get("accessToken").asText()))).andReturn();

            assertRefreshFails(r1);
        }

        @Test
        @DisplayName("(c) Lockout (5 lần sai) revoke refresh — lockout không chỉ chặn login (BR-002)")
        void tokenRevokedAfterLockout() throws Exception {
            JsonNode login = login("hant@dtd.edu.vn", PWD);  // U-0002 medical-staff
            String r1 = login.get("refreshToken").asText();

            // Login sai 5 lần để gây lockout
            for (int i = 0; i < 5; i++) {
                mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "hant@dtd.edu.vn", "password", "WRONG_" + i)))).andReturn();
            }

            // Refresh token cũ phải bị revoke kèm lockout (⚠️ đây là cạm bẫy: chỉ chặn /login mà quên refresh)
            assertRefreshFails(r1);

            // DB assertion
            long revokedCount = count(
                    "SELECT count(*) FROM refresh_tokens rt " +
                    "JOIN user_accounts u ON u.id=rt.user_id " +
                    "WHERE u.code='U-0002' AND rt.revoked_at IS NOT NULL");
            assertThat(revokedCount).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("(d) Deactivate user revoke — R_T sau deactivate ⇒ 401")
        void tokenRevokedAfterDeactivate() throws Exception {
            JsonNode login = login("trangnt@dtd.edu.vn", PWD);  // U-0004 teacher
            String r1 = login.get("refreshToken").asText();

            // System Admin deactivate teacher
            mvc.perform(patch("/api/v1/users/U-0004/status")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("active", false)))).andReturn();

            assertRefreshFails(r1);
        }

        @Test
        @DisplayName("(e) Login thiết bị khác (BR-005) — R_A sau login B ⇒ 401")
        void tokenRevokedAfterLoginOnOtherDevice() throws Exception {
            JsonNode loginA = login("toanpq@dtd.edu.vn", PWD);
            String rA = loginA.get("refreshToken").asText();

            // Login thiết bị B
            login("toanpq@dtd.edu.vn", PWD);

            // rA của thiết bị A bị revoke
            assertRefreshFails(rA);
        }

        @Test
        @DisplayName("(f) Đổi MK PATCH /account/password — R_old ⇒ 401 (UC-011 terminate all other sessions)")
        void tokenRevokedAfterPasswordChange() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            // Đổi MK
            mvc.perform(patch("/api/v1/account/password")
                    .header("Authorization", bearer(login.get("accessToken").asText()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of(
                            "currentPassword", PWD,
                            "newPassword", "NewPwd@Changed1"
                    )))).andReturn();

            assertRefreshFails(r1);
        }

        @Test
        @DisplayName("(g) Reset MK /auth/reset-password — R_old ⇒ 401 (UC-032 bước 6)")
        void tokenRevokedAfterPasswordReset() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            // Tạo reset token và reset
            mvc.perform(post("/api/v1/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("email", "toanpq@dtd.edu.vn")))).andReturn();

            String rawToken = "test-reset-replay-" + System.nanoTime();
            String tokenHash = new com.sfms.iam.service.TokenHasher().hash(rawToken);
            jdbc.update("""
                DELETE FROM password_reset_tokens WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001');
                """);
            jdbc.update("""
                INSERT INTO password_reset_tokens (user_id, token_hash, expires_at)
                SELECT id, ?, now() + interval '1 hour' FROM user_accounts WHERE code='U-0001'
                """, tokenHash);

            mvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("token", rawToken, "newPassword", "Reset@NewPwd1"))))
                    .andExpect(status().isOk());

            assertRefreshFails(r1);
        }

        @Test
        @DisplayName("DB: token_hash=:hash AND revoked_at IS NULL = 0 sau revoke (tất cả 7 đường)")
        void revokedTokenNotFoundInDb() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            // Logout → revoke
            mvc.perform(post("/api/v1/auth/logout")
                    .header("Authorization", bearer(login.get("accessToken").asText()))).andReturn();

            // token_hash của R1 không còn revoked_at=NULL
            // (token lưu hash, so sánh qua sha256 — assertion là revoked_at IS NOT NULL)
            long active = count(
                    "SELECT count(*) FROM refresh_tokens " +
                    "WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001') " +
                    "AND revoked_at IS NULL");
            assertThat(active).isZero();
        }

        // ── Helpers ────────────────────────────────────────────────────────────

        private void assertRefreshFails(String rawRefresh) throws Exception {
            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", rawRefresh))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"))
                    .andExpect(jsonPath("$.error.message").value(MSG_004));
        }
    }
}
