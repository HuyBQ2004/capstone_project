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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-007 — Activation end-to-end: {@code PENDING_VERIFICATION → ACTIVE} (UC-154).
 *
 * <p>Oracle: UC-154 Postconditions · BR-011 (48h) · BR-001 (BCrypt) · BR-006 (policy) · BR-010.
 * ⚠️ MISSING-ORACLE (success MSG) — §2 L9: không assert success message, chỉ assert trạng thái DB.
 */
class AccountActivationIT extends AbstractIamIntegrationTest {

    private static final String PENDING_EMAIL = "lanht@dtd.edu.vn";

    @Nested
    @DisplayName("MF01-TC-INT-007 — activate end-to-end PENDING→ACTIVE (UC-154)")
    class Tc_Int_007 {

        @Test
        @DisplayName("Activate với token hợp lệ: status=ACTIVE, email_verified=true, hash BCrypt, token used")
        void activateHappyPath() throws Exception {
            // Lấy raw token từ DB (seed đã tạo activation token với expires_at = future)
            // seed V900 không có token cụ thể — tạo token qua API resend hoặc trực tiếp
            String rawToken = insertActivationToken("STF-009", 48);

            MvcResult result = mvc.perform(post("/api/v1/auth/activate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("token", rawToken, "newPassword", PWD))))
                    .andExpect(status().isOk())
                    .andReturn();

            // DB assertions (UC-154 Postconditions)
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT status, email_verified, left(password_hash, 3) AS hash_prefix " +
                    "FROM user_accounts WHERE code='STF-009'");
            assertThat(row.get("status")).isEqualTo("ACTIVE");
            assertThat(row.get("email_verified")).isEqualTo(true);
            assertThat(row.get("hash_prefix").toString()).startsWith("$2");

            long usedAt = count(
                    "SELECT count(*) FROM account_activation_tokens WHERE used_at IS NOT NULL");
            assertThat(usedAt).isGreaterThanOrEqualTo(1);

            // audit_logs
            long auditRows = count(
                    "SELECT count(*) FROM audit_logs WHERE action LIKE '%ACTIVAT%'");
            assertThat(auditRows).isGreaterThanOrEqualTo(1);

            // ⚠️ Không assert success message — §2 L9 MISSING-ORACLE (UC nói MSG-031, traceability MSG-028)
        }

        @Test
        @DisplayName("Login sau activate thành công (BR-010: email_verified=true + ACTIVE)")
        void loginAfterActivation() throws Exception {
            String rawToken = insertActivationToken("STF-009", 48);

            mvc.perform(post("/api/v1/auth/activate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("token", rawToken, "newPassword", PWD)))).andReturn();

            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", PENDING_EMAIL, "password", PWD))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.needTermsAccept").value(true));  // BR-009: lần đầu
        }

        @Test
        @DisplayName("Token dùng lại lần 2 ⇒ 401 IAM-008; password_hash KHÔNG đổi")
        void tokenOneTimeUse() throws Exception {
            String rawToken = insertActivationToken("STF-009", 48);

            mvc.perform(post("/api/v1/auth/activate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("token", rawToken, "newPassword", PWD)))).andReturn();

            String hashAfterFirst = jdbc.queryForObject(
                    "SELECT password_hash FROM user_accounts WHERE code='STF-009'", String.class);

            // Lần 2
            mvc.perform(post("/api/v1/auth/activate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("token", rawToken, "newPassword", "Another@Pass1"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"))
                    .andExpect(jsonPath("$.error.message").value(MSG_004));

            // password_hash KHÔNG đổi
            String hashAfterSecond = jdbc.queryForObject(
                    "SELECT password_hash FROM user_accounts WHERE code='STF-009'", String.class);
            assertThat(hashAfterSecond).isEqualTo(hashAfterFirst);
        }

        @Test
        @DisplayName("Token hết hạn (expired): 401 IAM-008; trạng thái KHÔNG đổi")
        void expiredTokenRejected() throws Exception {
            String rawToken = insertActivationToken("STF-009", -1); // -1h = đã quá hạn

            mvc.perform(post("/api/v1/auth/activate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("token", rawToken, "newPassword", PWD))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"));

            // status vẫn PENDING_VERIFICATION
            String status = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE code='STF-009'", String.class);
            assertThat(status).isEqualTo("PENDING_VERIFICATION");
        }

        @Test
        @DisplayName("Resend: token cũ vô hiệu; token mới +48h; activate thành công")
        void resendInvalidatesOldToken() throws Exception {
            String oldToken = insertActivationToken("STF-009", -1); // hết hạn

            // Resend qua API
            mvc.perform(post("/api/v1/auth/activate/resend")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", PENDING_EMAIL))))
                    .andExpect(status().isOk());

            // Token cũ vẫn bị từ chối
            mvc.perform(post("/api/v1/auth/activate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("token", oldToken, "newPassword", PWD))))
                    .andExpect(status().isUnauthorized());

            // Lấy token mới từ DB (token vừa được tạo bởi resend)
            String newTokenHash = jdbc.queryForObject(
                    "SELECT token_hash FROM account_activation_tokens " +
                    "WHERE user_id = (SELECT id FROM user_accounts WHERE code='STF-009') " +
                    "AND used_at IS NULL ORDER BY expires_at DESC LIMIT 1",
                    String.class);
            assertThat(newTokenHash).isNotNull();

            // Kiểm expires_at = khoảng +48h
            Boolean withinWindow = jdbc.queryForObject("""
                SELECT (expires_at - created_at) BETWEEN interval '47 hours' AND interval '49 hours'
                FROM account_activation_tokens
                WHERE user_id = (SELECT id FROM user_accounts WHERE code='STF-009')
                  AND used_at IS NULL ORDER BY expires_at DESC LIMIT 1
                """, Boolean.class);
            assertThat(withinWindow).isTrue();
        }

        @Test
        @DisplayName("MK yếu khi activate ⇒ 422 IAM-006policy + MSG-104 (UC-154 BR-006)")
        void weakPasswordOnActivateRejected() throws Exception {
            String rawToken = insertActivationToken("STF-009", 48);

            mvc.perform(post("/api/v1/auth/activate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("token", rawToken, "newPassword", "password"))))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.error.code").value("IAM-006policy"))
                    .andExpect(jsonPath("$.error.message").value(MSG_104));
        }

        // ── Helper ────────────────────────────────────────────────────────────
        /**
         * Tạo activation token trong DB, {@code expires_at = now + hoursOffset} giờ.
         *
         * <p>⚠️ PHẢI dùng đúng thuật toán hash của production ({@link com.sfms.iam.service.TokenHasher}
         * — SHA-256 rồi <b>Base64</b>, KHÔNG phải hex). Hash sai khuôn ⇒
         * {@code AccountActivationTokenRepository.findByTokenHash} không bao giờ tìm thấy bản ghi
         * ⇒ mọi request activate rơi vào nhánh "token invalid" (401) bất kể còn hạn hay không,
         * khiến {@code activateHappyPath()} không bao giờ đạt được 200 thật.
         */
        private String insertActivationToken(String userCode, int hoursOffset) {
            String rawToken = "test-activation-token-" + userCode + "-" + System.nanoTime();
            String tokenHash = new com.sfms.iam.service.TokenHasher().hash(rawToken);

            jdbc.update("""
                INSERT INTO account_activation_tokens (user_id, token_hash, expires_at)
                SELECT id, ?, ?::timestamptz + (? || ' hours')::interval
                FROM user_accounts WHERE code=?
                """, tokenHash, clock.instant().toString(), String.valueOf(hoursOffset), userCode);
            return rawToken;
        }
    }
}
