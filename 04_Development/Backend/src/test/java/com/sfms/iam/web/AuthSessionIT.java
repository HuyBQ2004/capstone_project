package com.sfms.iam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.sql.Timestamp;
import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-004 — Single session end-to-end: login thiết bị 2 ⇒ refresh thiết bị 1 chết.
 *
 * <p>Oracle: BR-005 · BR-004 · TDS §14.1 bất biến ≤1 refresh/user.
 *
 * <p>⚠️ Ghi chú kiến trúc (TestSpec §2 L4): Access token cũ của A vẫn hợp lệ tới khi hết 30'
 * (stateless JWT). Đây là giới hạn kiến trúc đã chấp nhận (ADR-M01 Option A), không phải bug.
 */
class AuthSessionIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("MF01-TC-INT-004 — single session: login thiết bị 2 vô hiệu refresh thiết bị 1")
    class Tc_Int_004 {

        @Test
        @DisplayName("Login B revoke refresh A — refresh A2 sau đó ⇒ 401; refresh B vẫn OK")
        void loginDeviceBRevokesDeviceA() throws Exception {
            // 1. Login (thiết bị A) → R_A
            JsonNode loginA = login("toanpq@dtd.edu.vn", PWD);
            String rA = loginA.get("refreshToken").asText();

            // 2. refresh(R_A) → R_A2 (chứng minh A đang hoạt động)
            JsonNode rotateA = parse(mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", rA))))
                    .andExpect(status().isOk())
                    .andReturn());
            String rA2 = rotateA.get("refreshToken").asText();

            // 3. Login (thiết bị B, cùng tài khoản) → R_B
            JsonNode loginB = login("toanpq@dtd.edu.vn", PWD);
            String rB = loginB.get("refreshToken").asText();

            // 4. refresh(R_A2) ⇒ 401 — phiên A bị vô hiệu đúng lúc B login (BR-005)
            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", rA2))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"));

            // 5. refresh(R_B) ⇒ thành công
            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", rB))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Bất biến DB: không có user nào có >1 refresh token còn hiệu lực (TDS §14.1)")
        void dbInvariantMaxOneActiveRefreshPerUser() throws Exception {
            // Login 2 lần liên tiếp cùng tài khoản
            login("toanpq@dtd.edu.vn", PWD);
            login("toanpq@dtd.edu.vn", PWD);

            // ⚠️ Mốc so sánh lấy từ MutableClock, KHÔNG dùng now() của Postgres. Với now(), từ
            // 2026-08-14 mệnh đề expires_at > now() lọc SẠCH mọi dòng (expires_at = clock ghim
            // + 14 ngày = 2026-08-13) ⇒ subquery rỗng ⇒ assertion luôn xanh mà không còn kiểm
            // được gì: chốt bất biến chết âm thầm, không bắt được regression BR-005 nữa.
            long violations = count("""
                SELECT count(*) FROM (
                    SELECT user_id, count(*) AS cnt FROM refresh_tokens
                     WHERE revoked_at IS NULL AND expires_at > ?
                     GROUP BY user_id HAVING count(*) > 1
                ) x
                """, Timestamp.from(clock.instant()));
            assertThat(violations).isZero();
        }
    }
}
