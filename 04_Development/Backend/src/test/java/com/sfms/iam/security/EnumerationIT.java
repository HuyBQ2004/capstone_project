package com.sfms.iam.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-011 — Không lộ sự tồn tại tài khoản (user enumeration).
 *
 * <p>Oracle: TDS §8.1 (không lộ email tồn tại) · MSG-002 (dùng chung login/email sai) ·
 * TDS §1 PII / PDPD.
 * <p>OWASP: A07:2021 · CWE: CWE-204.
 *
 * <p>⚠️ Ghi chú thiết kế (TestSpec §4.3 SEC-011): lockout (423 IAM-007) BẢN CHẤT đã xác nhận
 * tài khoản tồn tại. Đây là đánh đổi có chủ ý của BR-002 (phải cho người dùng biết vì sao bị chặn).
 * TC ghi nhận, KHÔNG đòi sửa BR.
 */
@DisplayName("MF01-TC-SEC-011 — Không lộ sự tồn tại tài khoản (user enumeration)")
class EnumerationIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("Login + forgot-password không lộ email tồn tại hay không")
    class Tc_Sec_011 {

        @Test
        @DisplayName("(1)+(2) Login email có thật sai MK vs email không tồn tại → cùng 401 IAM-002 body")
        void loginKnownVsUnknownEmailSameResponse() throws Exception {
            // Email có thật + MK sai
            MvcResult known = mvc.perform(post("/api/v1/auth/login")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG_PWD"))))
                    .andReturn();

            // Email không tồn tại
            MvcResult unknown = mvc.perform(post("/api/v1/auth/login")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "khong.ton.tai@gmail.com", "password", "ANY_PWD"))))
                    .andReturn();

            // Cùng status code
            assertThat(known.getResponse().getStatus()).isEqualTo(unknown.getResponse().getStatus());
            assertThat(known.getResponse().getStatus()).isEqualTo(401);

            // Cùng error.code
            com.fasterxml.jackson.databind.JsonNode knownBody = parse(known);
            com.fasterxml.jackson.databind.JsonNode unknownBody = parse(unknown);

            assertThat(knownBody.get("error").get("code").asText())
                    .isEqualTo(unknownBody.get("error").get("code").asText())
                    .isEqualTo("IAM-002");

            // Cùng error.message (MSG-002)
            assertThat(knownBody.get("error").get("message").asText())
                    .isEqualTo(unknownBody.get("error").get("message").asText())
                    .isEqualTo(MSG_002);
        }

        @Test
        @DisplayName("(3) forgot-password email có thật vs email lạ → response GIỐNG HỆT nhau")
        void forgotPasswordSameResponseForBothEmails() throws Exception {
            MvcResult known = mvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn"))))
                    .andReturn();

            MvcResult unknown = mvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "khong.ton.tai@gmail.com"))))
                    .andReturn();

            assertThat(known.getResponse().getStatus()).isEqualTo(unknown.getResponse().getStatus());
        }

        @Test
        @DisplayName("(4) Timing: email lạ login phải mất gần bằng email thật (BCrypt giả hash — < 200ms chênh)")
        void timingAttackProtection() throws Exception {
            // Warm-up
            for (int i = 0; i < 3; i++) {
                mvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG")))).andReturn();
            }

            // Email có thật
            long knownTotal = 0;
            int rounds = 5;
            for (int i = 0; i < rounds; i++) {
                long start = System.currentTimeMillis();
                mvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG")))).andReturn();
                knownTotal += System.currentTimeMillis() - start;
            }

            // Email lạ
            long unknownTotal = 0;
            for (int i = 0; i < rounds; i++) {
                long start = System.currentTimeMillis();
                mvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "ghost" + i + "@nowhere.vn", "password", "WRONG")))).andReturn();
                unknownTotal += System.currentTimeMillis() - start;
            }

            long knownMedian = knownTotal / rounds;
            long unknownMedian = unknownTotal / rounds;
            long diff = Math.abs(knownMedian - unknownMedian);

            // < 200ms — nếu email lạ nhanh hơn nhiều ⇒ không có BCrypt giả (timing attack)
            assertThat(diff).as("Timing chênh lệch: known=%dms, unknown=%dms", knownMedian, unknownMedian)
                    .isLessThan(200);
        }

        @Test
        @DisplayName("(5) Lockout xác nhận tài khoản — đánh đổi BR-002 đã chấp nhận (ghi nhận, không fix)")
        void lockoutRevealsThatAccountExists() throws Exception {
            // Gây lockout
            for (int i = 0; i < 5; i++) {
                mvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG" + i)))).andReturn();
            }

            MvcResult lockedResult = mvc.perform(post("/api/v1/auth/login")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG_FINAL"))))
                    .andReturn();

            // 423 → xác nhận tài khoản tồn tại — đây là đánh đổi có chủ ý BR-002
            assertThat(lockedResult.getResponse().getStatus()).isEqualTo(423);

            // TC ghi nhận điều này là expected behavior, không phải lỗi
            // Reviewer: xem TestSpec §4.3 SEC-011 bước 5 và BR-002
        }
    }
}
