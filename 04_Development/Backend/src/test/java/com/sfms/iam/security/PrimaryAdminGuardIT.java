package com.sfms.iam.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-008 — Không được deactivate primary System Admin (UC-003 exception).
 *
 * <p>Oracle: UC-003 Preconditions + NF bước 4 + Exceptions · TDS §16 · TDS §6.3 invariant (3) ·
 * TDS §8.1 IAM-004 nếu là primary System Admin · MSG-005.
 * <p>OWASP: A01:2021 · CWE: CWE-284.
 *
 * <p>⚠️ Thiếu oracle bước 3 (TestSpec §4.3 SEC-008): UC-003 không nói về tự-vô-hiệu-hóa admin
 * thường. Bước 3 ghi nhận hành vi thực tế + cần Nam chốt (khuyến nghị chặn).
 */
@DisplayName("MF01-TC-SEC-008 — Không deactivate primary System Admin")
class PrimaryAdminGuardIT extends AbstractIamIntegrationTest {

    // U-9000 = FX-U-SYSADMIN-PRIMARY (primary_admin=TRUE)
    // U-9001 = FX-U-SYSADMIN (primary_admin=FALSE)

    @Nested
    @DisplayName("Guard primary admin — hệ thống không bao giờ mất đường vào quản trị")
    class Tc_Sec_008 {

        @Test
        @DisplayName("(1) System Admin thường vô hiệu hóa primary admin U-9000 → 403 IAM-004 + MSG-005")
        void sysAdminCannotDeactivatePrimaryAdmin() throws Exception {
            mvc.perform(patch("/api/v1/users/U-9000/status")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("active", false))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"))
                    .andExpect(jsonPath("$.error.message").value(MSG_005));

            // DB: U-9000 vẫn ACTIVE
            String status = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE code='U-9000'", String.class);
            assertThat(status).isEqualTo("ACTIVE");

            // Refresh token của U-9000 KHÔNG bị revoke
            long revoked = count(
                    "SELECT count(*) FROM refresh_tokens " +
                    "WHERE user_id = (SELECT id FROM user_accounts WHERE code='U-9000') " +
                    "AND revoked_at IS NOT NULL");
            assertThat(revoked).isZero();
        }

        @Test
        @DisplayName("(2) Primary admin tự vô hiệu hóa chính mình → 403 (guard theo cờ primary, không theo \"ai gọi\")")
        void primaryAdminCannotDeactivateSelf() throws Exception {
            mvc.perform(patch("/api/v1/users/U-9000/status")
                            .header("Authorization", authHeaderFor("U-9000"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("active", false))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"));

            String status = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE code='U-9000'", String.class);
            assertThat(status).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("(3) System Admin thường (U-9001) tự vô hiệu hóa → ⚠️ ghi nhận hành vi (thiếu oracle)")
        void sysAdminCanDeactivateSelf() throws Exception {
            // ⚠️ TestSpec §4.3 SEC-008 bước 3: UC-003 không quy định trường hợp này.
            // TC ghi nhận hành vi thực tế — không đòi implement theo 1 hướng cụ thể.
            // Khuyến nghị (TDS §8.1): nên chặn để tránh mất admin cuối cùng.
            org.springframework.test.web.servlet.MvcResult result =
                    mvc.perform(patch("/api/v1/users/U-9001/status")
                                    .header("Authorization", authHeaderFor("U-9001"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(Map.of("active", false))))
                            .andReturn();

            // Assert: không 500; không crash hệ thống
            assertThat(result.getResponse().getStatus()).isNotEqualTo(500);

            // Ghi nhận để reviewer: hành vi là 200 hoặc 403
            // TODO: Cần Nam chốt — khuyến nghị 403 (tránh tự-khóa admin cuối)
        }

        @Test
        @DisplayName("(4) Sau bước 1 và 2, U-9000 vẫn login được — hệ thống không mất đường vào")
        void primaryAdminLoginStillWorks() throws Exception {
            // Thử deactivate (sẽ bị chặn theo bước 1)
            mvc.perform(patch("/api/v1/users/U-9000/status")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("active", false)))).andReturn();

            // U-9000 vẫn login được
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "root@sfms.vn", "password", PWD))))
                    .andExpect(status().isOk());
        }
    }
}
