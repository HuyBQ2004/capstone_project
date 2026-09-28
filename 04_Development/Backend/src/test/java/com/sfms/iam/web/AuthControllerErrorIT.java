package com.sfms.iam.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-002 — Login sai MK: envelope D2 + {@code attemptsLeft} + lockout ở lần 5.
 *
 * <p>Oracle: BR-002 · MSG-002 / MSG-076 nguyên văn · TDS §9.2 (401/423) · TDS §10 IAM-002/IAM-007
 * · D2 envelope {@code {error:{code,message,details}}}, {@code details} là mảng, không bao giờ null.
 * Oracle đã chốt 2026-07-31 (TestSpec §2 L3).
 *
 * <p>✅ Mã {@code IAM-007} · {@code IAM-002} · shape {@code details} đã chốt vào TDS-MF01 §9.2 / §10.
 */
class AuthControllerErrorIT extends AbstractIamIntegrationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-INT-002 — Login sai MK: envelope D2 + attemptsLeft + lockout
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-INT-002 — login sai MK: envelope D2 + attemptsLeft + lockout")
    class Tc_Int_002 {

        @Test
        @DisplayName("Lần 1–4 sai: 401 IAM-002 + attemptsLeft trong details (D2 §2 L3 chốt)")
        void wrongPasswordFirstFourAttempts() throws Exception {
            // Lần 1: 4 attempts left
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG1"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-002"))
                    .andExpect(jsonPath("$.error.message").value(MSG_002))
                    .andExpect(jsonPath("$.error.details[0].field").value("attemptsLeft"))
                    .andExpect(jsonPath("$.error.details[0].value").value(4))
                    // D2: KHÔNG được có khóa thứ 4 nằm trực tiếp trong error
                    .andExpect(jsonPath("$.error.attemptsLeft").doesNotExist());

            // Lần 2: 3 attempts left
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG2"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.details[0].value").value(3));

            // Lần 3: 2 attempts left
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG3"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.details[0].value").value(2));

            // Lần 4: 1 attempt left
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG4"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.details[0].value").value(1));
        }

        @Test
        @DisplayName("Lần 5 sai: 423 IAM-007 + lockout 15 phút từ SecurityPolicy (L13: không hardcode)")
        void fifthFailureCausesLockout() throws Exception {
            // Login sai 4 lần trước
            for (int i = 0; i < 4; i++) {
                mvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG" + i))))
                        .andReturn();
            }
            // Lần 5 — phải bị khóa (BR-002: lockoutMinutes=15 từ SEC-DEFAULT)
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG5"))))
                    .andExpect(status().isLocked())                          // 423
                    .andExpect(jsonPath("$.error.code").value("IAM-007"))
                    .andExpect(jsonPath("$.error.message").value(msg076(15)));  // L13: đọc từ policy
        }

        @Test
        @DisplayName("Email không tồn tại cũng trả 401 IAM-002 — không lộ sự tồn tại tài khoản")
        void unknownEmailReturnsSameAs401() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "khong.ton.tai@gmail.com", "password", "AnyPwd1@"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-002"))
                    .andExpect(jsonPath("$.error.message").value(MSG_002));
        }

        @Test
        @DisplayName("Envelope error chỉ có 3 khóa: code, message, details — không có attemptsLeft nằm ngoài")
        void envelopeHasExactlyThreeKeys() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").isMap())
                    .andExpect(jsonPath("$.error.code").exists())
                    .andExpect(jsonPath("$.error.message").exists())
                    .andExpect(jsonPath("$.error.details").isArray())
                    // D2: KHÔNG có khóa thứ 4 tại cấp error
                    .andExpect(jsonPath("$.error.attemptsLeft").doesNotExist());
        }

        @Test
        @DisplayName("Tài khoản INACTIVE login ⇒ 403 IAM-003 + MSG-003 (BR-010)")
        void inactiveAccountBlocked() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "phongvd.parent@gmail.com", "password", PWD))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-003"))
                    .andExpect(jsonPath("$.error.message").value(MSG_003));
        }

        @Test
        @DisplayName("Tài khoản SUSPENDED login ⇒ 403 IAM-003 + MSG-003 (BR-010)")
        void suspendedAccountBlocked() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "yendh@daiduong.vn", "password", PWD))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-003"))
                    .andExpect(jsonPath("$.error.message").value(MSG_003));
        }
    }
}
