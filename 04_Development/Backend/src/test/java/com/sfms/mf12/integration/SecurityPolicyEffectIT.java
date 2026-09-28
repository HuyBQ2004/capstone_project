package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF12-TC-INT-014 — liên module MF12↔MF01: {@code PUT /security-policies} có hiệu lực ở LẦN
 * ĐĂNG NHẬP KẾ TIẾP ngay lập tức (BR-002), không cần restart — {@code SecurityPolicyProvider}
 * đọc DB tươi mỗi lần, không cache.
 */
class SecurityPolicyEffectIT extends AbstractIamIntegrationTest {

    private static final String TARGET_EMAIL = "hant@dtd.edu.vn"; // medical-staff, U-0002

    private MvcResult attemptWrongLogin() throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", TARGET_EMAIL, "password", "wrong-password"))))
                .andReturn();
    }

    private void resetLockoutState() {
        jdbc.update("UPDATE user_accounts SET failed_login_count = 0, locked_until = NULL WHERE email = ?",
                TARGET_EMAIL);
    }

    @Test
    void changingMaxLoginAttemptsAffectsLockoutThresholdImmediately() throws Exception {
        String admin = authHeaderFor("U-9001");

        // (1) — hạ ngưỡng xuống 3, kỳ vọng khoá ở lần sai thứ 3 (không phải 5 mặc định).
        MvcResult policyUpdate = mvc.perform(put("/api/v1/security-policies/SEC-DEFAULT")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("maxLoginAttempts", 3))))
                .andReturn();
        assertThat(policyUpdate.getResponse().getStatus()).isEqualTo(200);

        MvcResult attempt1 = attemptWrongLogin();
        MvcResult attempt2 = attemptWrongLogin();
        MvcResult attempt3 = attemptWrongLogin();
        assertThat(attempt1.getResponse().getStatus()).isEqualTo(401);
        assertThat(attempt2.getResponse().getStatus()).isEqualTo(401);
        assertThat(attempt3.getResponse().getStatus()).isEqualTo(423);

        // Audit ghi lại thao tác đổi policy VÀ lần khoá tài khoản (BR-170/BR-002).
        long lockedAuditRows = count(
                "SELECT count(*) FROM audit_logs WHERE resource = 'Authentication' AND result = 'BLOCKED'");
        assertThat(lockedAuditRows).isGreaterThanOrEqualTo(3);
        long policyAuditRows = count(
                "SELECT count(*) FROM audit_logs WHERE resource = 'Security Policies'");
        assertThat(policyAuditRows).isGreaterThanOrEqualTo(1);

        // (4) — trả ngưỡng về 5, mở khoá thủ công, xác nhận lần sai thứ 3 KHÔNG còn khoá,
        // chỉ khoá đúng ở lần thứ 5.
        resetLockoutState();
        MvcResult restorePolicy = mvc.perform(put("/api/v1/security-policies/SEC-DEFAULT")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("maxLoginAttempts", 5))))
                .andReturn();
        assertThat(restorePolicy.getResponse().getStatus()).isEqualTo(200);

        MvcResult second1 = attemptWrongLogin();
        MvcResult second2 = attemptWrongLogin();
        MvcResult second3 = attemptWrongLogin();
        assertThat(second3.getResponse().getStatus()).isEqualTo(401); // KHÔNG khoá ở lần 3 nữa
        MvcResult second4 = attemptWrongLogin();
        MvcResult second5 = attemptWrongLogin();
        assertThat(second4.getResponse().getStatus()).isEqualTo(401);
        assertThat(second5.getResponse().getStatus()).isEqualTo(423); // khoá đúng ở lần 5
    }
}
