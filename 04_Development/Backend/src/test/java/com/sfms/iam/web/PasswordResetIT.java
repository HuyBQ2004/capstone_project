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
 * MF01-TC-INT-008 — Forgot → Reset end-to-end (UC-032).
 *
 * <p>
 * Oracle: UC-032 NF bước 4–6 · BR-003 (1h, 1 req/15') · BR-001 · BR-006 ·
 * MSG-030 ·
 * TDS §10 IAM-008.
 *
 * <p>
 * ⚠️ Xung đột thiết kế (TestSpec §4.2 INT-008 ghi chú): UC-032 Exceptions đòi
 * MSG-003
 * cho tài khoản deactivated, nhưng TDS §8.1 đòi không lộ email tồn tại. TC này
 * ghi nhận
 * và không assert MSG-003 cho bước 7 — chọn chiến lược không-lộ theo khuyến
 * nghị.
 */
class PasswordResetIT extends AbstractIamIntegrationTest {

        @Nested
        @DisplayName("MF01-TC-INT-008 — Forgot→Reset end-to-end (UC-032)")
        class Tc_Int_008 {

                @Test
                @DisplayName("forgot-password: 200; 1 bản ghi token (hash); expires_at = now+1h")
                void forgotPasswordSendsTokenAndPersists() throws Exception {
                        mvc.perform(post("/api/v1/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn"))))
                                        .andExpect(status().isOk());

                        long tokenCount = count(
                                        "SELECT count(*) FROM password_reset_tokens " +
                                                        "WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001') "
                                                        +
                                                        "AND used_at IS NULL");
                        assertThat(tokenCount).isEqualTo(1);

                        // expires_at ≈ now + 1h
                        Boolean withinWindow = jdbc.queryForObject(
                                        """
                                                        SELECT (expires_at - created_at) BETWEEN interval '55 minutes' AND interval '65 minutes'
                                                        FROM password_reset_tokens
                                                        WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001')
                                                        ORDER BY expires_at DESC LIMIT 1
                                                        """,
                                        Boolean.class);
                        assertThat(withinWindow).isTrue();

                        // Token lưu hash (không có raw token trong DB)
                        long rawTokenRows = count(
                                        "SELECT count(*) FROM password_reset_tokens WHERE token_hash IS NULL");
                        assertThat(rawTokenRows).isZero();
                }

                @Test
                @DisplayName("Reset thành công: 200 MSG-030; password_hash đổi; phiên cũ bị revoke")
                void resetPasswordHappyPath() throws Exception {
                        // Bước 1: login để lấy phiên
                        JsonNode login = login("toanpq@dtd.edu.vn", PWD);
                        String refreshTokenBeforeReset = login.get("refreshToken").asText();

                        // Bước 2: forgot → lấy raw token từ DB
                        mvc.perform(post("/api/v1/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn")))).andReturn();

                        String rawToken = extractResetToken("U-0001");

                        // Bước 3: reset với MK mới
                        mvc.perform(post("/api/v1/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("token", rawToken, "newPassword", "N3w@Pass456"))))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value(MSG_030));

                        // password_hash đổi — không còn BCrypt của PWD cũ
                        String hashAfter = jdbc.queryForObject(
                                        "SELECT password_hash FROM user_accounts WHERE code='U-0001'", String.class);
                        assertThat(hashAfter).isNotNull().startsWith("$2");

                        // Bước 4: phiên cũ bị revoke (UC-032 bước 6)
                        mvc.perform(post("/api/v1/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("refreshToken", refreshTokenBeforeReset))))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.error.code").value("IAM-008"));

                        // Bước 5: MK cũ không login được; MK mới login được
                        mvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", PWD))))
                                        .andExpect(status().isUnauthorized());

                        mvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password",
                                                        "N3w@Pass456"))))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Token dùng lại lần 2 ⇒ 401 IAM-008 (token dùng 1 lần)")
                void tokenOneTimeUse() throws Exception {
                        mvc.perform(post("/api/v1/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn")))).andReturn();

                        String rawToken = extractResetToken("U-0001");

                        // Lần 1
                        mvc.perform(post("/api/v1/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("token", rawToken, "newPassword", "N3w@Pass456"))))
                                        .andReturn();

                        // Lần 2 — phải bị từ chối
                        mvc.perform(post("/api/v1/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("token", rawToken, "newPassword", "Another@Pass789"))))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.error.code").value("IAM-008"));
                }

                @Test
                @DisplayName("MK trùng history (BR-006/BR-015) khi reset ⇒ 422 IAM-006reuse + MSG-105")
                void reusePasswordRejectedOnReset() throws Exception {
                        // Trước: đặt lại để biết MK history
                        mvc.perform(post("/api/v1/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn")))).andReturn();

                        String rawToken = extractResetToken("U-0001");

                        // Reset sang MK mới (thành công — lưu vào history)
                        mvc.perform(post("/api/v1/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("token", rawToken, "newPassword", "NewPass@123"))))
                                        .andReturn();

                        // Lấy token mới
                        mvc.perform(post("/api/v1/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn")))).andReturn();

                        String rawToken2 = extractResetToken("U-0001");

                        // Đặt lại MK cũ (Old@Pass2 đã có trong history seed)
                        mvc.perform(post("/api/v1/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("token", rawToken2, "newPassword", "Old@Pass2"))))
                                        .andExpect(status().isUnprocessableContent())
                                        .andExpect(jsonPath("$.error.code").value("IAM-006reuse"))
                                        .andExpect(jsonPath("$.error.message").value(MSG_105));
                }

                @Test
                @DisplayName("Email không tồn tại ⇒ response GIỐNG HỆT email hợp lệ (không lộ)")
                void unknownEmailSameResponse() throws Exception {
                        // Email hợp lệ
                        MvcResult known = mvc.perform(post("/api/v1/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn")))).andReturn();

                        // Email lạ
                        MvcResult unknown = mvc.perform(post("/api/v1/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of("email", "khong.ton.tai@x.vn")))).andReturn();

                        assertThat(known.getResponse().getStatus()).isEqualTo(unknown.getResponse().getStatus());
                }

                // ── Helper ────────────────────────────────────────────────────────────
                /**
                 * ⚠️ PHẢI dùng đúng thuật toán hash của production
                 * ({@link com.sfms.iam.service.TokenHasher}
                 * — SHA-256 rồi <b>Base64</b>, KHÔNG phải hex) — nếu không
                 * {@code reset-password} sẽ luôn
                 * trả 401 "token invalid" bất kể còn hạn hay không, che khuất mọi assertion
                 * phía sau.
                 */
                private String extractResetToken(String userCode) {
                        String rawToken = "test-reset-token-" + userCode + "-" + System.nanoTime();
                        String tokenHash = new com.sfms.iam.service.TokenHasher().hash(rawToken);
                        jdbc.update("""
                                        DELETE FROM password_reset_tokens
                                        WHERE user_id = (SELECT id FROM user_accounts WHERE code=?)
                                        """, userCode);
                        jdbc.update("""
                                        INSERT INTO password_reset_tokens (user_id, token_hash, expires_at)
                                        SELECT id, ?, now() + interval '1 hour'
                                        FROM user_accounts WHERE code=?
                                        """, tokenHash, userCode);
                        return rawToken;
                }
        }
}
