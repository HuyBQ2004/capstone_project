package com.sfms.iam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-003 — {@code POST /auth/refresh}: rotate + trượt hạn; token cũ dùng lại ⇒ 401.
 *
 * <p>Oracle: BR-004 (v1.9) · TDS §9.2 · TDS §10 IAM-008 · TestSpec §3 TC-COND-008.
 */
class AuthRefreshIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("MF01-TC-INT-003 — refresh rotate + sliding 14 ngày")
    class Tc_Int_003 {

        @Test
        @DisplayName("Refresh trả access + refresh mới; R1 bị revoke ngay")
        void refreshRotatesToken() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            // Gọi refresh với R1 → nhận R2
            MvcResult r1Result = mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", r1))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andReturn();

            // Gọi lại với R1 sau khi đã rotate — phải bị từ chối
            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", r1))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"))
                    .andExpect(jsonPath("$.error.message").value(MSG_004));
        }

        @Test
        @DisplayName("Refresh thành công: response chỉ có 2 khóa accessToken + refreshToken; claim giữ nguyên")
        void refreshResponseShape() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            MvcResult result = mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", r1))))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            assertThat(body.has("accessToken")).isTrue();
            assertThat(body.has("refreshToken")).isTrue();

            // Giải mã claim — uid/role/schoolId/partnerId giữ nguyên
            String[] parts = body.get("accessToken").asText().split("\\.");
            JsonNode claims = json.readTree(java.util.Base64.getUrlDecoder().decode(parts[1]));
            assertThat(claims.get("role").asText()).isEqualTo("school-admin");   // D6
            assertThat(claims.get("schoolId").asText()).isEqualTo("SCH-HN-0042"); // D1
        }

        @Test
        @DisplayName("DB: chỉ R2 còn hiệu lực; R1 revoked_at IS NOT NULL")
        void databaseStateAfterRotate() throws Exception {
            JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String r1 = login.get("refreshToken").asText();

            // Rotate
            mvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("refreshToken", r1)))).andReturn();

            // DB: chỉ 1 refresh token còn hiệu lực.
            // ⚠️ Mốc so sánh lấy từ MutableClock, KHÔNG dùng now() của Postgres — xem ghi chú
            // cùng lý do ở AuthControllerIT.refreshTokenPersistedCorrectly.
            long usable = count("""
                SELECT count(*) FROM refresh_tokens
                 WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-0001')
                   AND revoked_at IS NULL AND expires_at > ?
                """, Timestamp.from(clock.instant()));
            assertThat(usable).isEqualTo(1);

            // R1 đã revoke
            long r1Revoked = count(
                    "SELECT count(*) FROM refresh_tokens WHERE revoked_at IS NOT NULL " +
                    "AND user_id = (SELECT id FROM user_accounts WHERE code='U-0001')");
            assertThat(r1Revoked).isGreaterThanOrEqualTo(1);
        }

        /**
         * ⚠️ Phải insert đúng HASH của {@code "expired-raw-token"} (không phải chuỗi tùy ý) —
         * nếu không {@code findByTokenHashAndRevokedAtIsNull} không tìm thấy bản ghi và test vẫn
         * "xanh" nhưng vì lý do sai (token lạ), không phải vì hết hạn thật.
         */
        @Test
        @DisplayName("Token hết hạn ⇒ 401 IAM-008 + MSG-004")
        void expiredRefreshTokenRejected() throws Exception {
            String rawToken = "expired-raw-token";
            String hash = new com.sfms.iam.service.TokenHasher().hash(rawToken);

            jdbc.update("""
                INSERT INTO refresh_tokens (user_id, token_hash, expires_at)
                SELECT id, ?, ?::timestamptz - interval '1 second'
                FROM user_accounts WHERE code='U-0001'
                """, hash, clock.instant().toString());

            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", rawToken))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"))
                    .andExpect(jsonPath("$.error.message").value(MSG_004));
        }
    }
}
