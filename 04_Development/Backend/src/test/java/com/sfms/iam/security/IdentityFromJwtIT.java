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
 * MF01-TC-SEC-005 — Identity lấy từ JWT, KHÔNG tin body/query (leo quyền qua payload).
 *
 * <p>Oracle: TDS §17 C2 · TDS §17.4 AP-AI-006 · BR-007.
 * <p>OWASP: A01:2021 · CWE: CWE-639.
 *
 * <p>JWT = FX-JWT-TEACHER (U-0004). Tất cả thao tác phải chỉ ảnh hưởng U-0004, không U-0001.
 */
@DisplayName("MF01-TC-SEC-005 — Identity từ JWT: body/query userId bị bỏ qua")
class IdentityFromJwtIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("7 vector leo quyền qua body/query — tất cả phải bị chặn hoặc áp dụng cho đúng subject JWT")
    class Tc_Sec_005 {

        private static final String TEACHER_CODE = "U-0004";
        private static final String SCHOOL_ADMIN_CODE = "U-0001";

        @Test
        @DisplayName("(a) PATCH /profile/me body userId=U-0001 → sửa hồ sơ U-0004, U-0001 không đổi")
        void patchProfileBodyUserIdIgnored() throws Exception {
            // Ghi nhớ updated_at của U-0001 trước
            Object updatedAtBefore = jdbc.queryForObject(
                    "SELECT updated_at FROM user_accounts WHERE code=?", Object.class, SCHOOL_ADMIN_CODE);

            mvc.perform(patch("/api/v1/profile/me")
                            .header("Authorization", authHeaderFor(TEACHER_CODE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("name", "Tên Đổi", "userId", SCHOOL_ADMIN_CODE))))
                    .andExpect(status().isOk());

            // U-0001 KHÔNG bị sửa
            Object updatedAtAfter = jdbc.queryForObject(
                    "SELECT updated_at FROM user_accounts WHERE code=?", Object.class, SCHOOL_ADMIN_CODE);
            assertThat(updatedAtBefore).isEqualTo(updatedAtAfter);

            // U-0004 được sửa
            String nameAfter = jdbc.queryForObject(
                    "SELECT name FROM user_accounts WHERE code=?", String.class, TEACHER_CODE);
            assertThat(nameAfter).isEqualTo("Tên Đổi");
        }

        @Test
        @DisplayName("(b) PATCH /account/password body userId=U-0001 → đổi MK U-0004; U-0001.hash không đổi")
        void changePasswordBodyUserIdIgnored() throws Exception {
            String hashBefore = jdbc.queryForObject(
                    "SELECT password_hash FROM user_accounts WHERE code=?", String.class, SCHOOL_ADMIN_CODE);

            mvc.perform(patch("/api/v1/account/password")
                            .header("Authorization", authHeaderFor(TEACHER_CODE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "userId", SCHOOL_ADMIN_CODE,
                                    "currentPassword", PWD,
                                    "newPassword", "Changed@Teacher1"
                            ))))
                    .andExpect(status().isOk());

            // U-0001 password_hash KHÔNG đổi
            String hashAfter = jdbc.queryForObject(
                    "SELECT password_hash FROM user_accounts WHERE code=?", String.class, SCHOOL_ADMIN_CODE);
            assertThat(hashAfter).isEqualTo(hashBefore);
        }

        @Test
        @DisplayName("(c) POST /auth/logout body userId=U-0001 → revoke refresh U-0004; phiên U-0001 còn sống")
        void logoutBodyUserIdIgnored() throws Exception {
            // Login cả 2 tài khoản
            com.fasterxml.jackson.databind.JsonNode adminLogin = login("toanpq@dtd.edu.vn", PWD);
            String adminRefresh = adminLogin.get("refreshToken").asText();

            // Teacher logout — gửi userId của admin trong body
            mvc.perform(post("/api/v1/auth/logout")
                            .header("Authorization", authHeaderFor(TEACHER_CODE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("userId", SCHOOL_ADMIN_CODE))))
                    .andExpect(status().isOk());

            // Admin phiên KHÔNG bị revoke
            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", adminRefresh))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("(d) GET /users?role=system-admin JWT teacher → 403 (không phải danh sách rỗng)")
        void teacherCannotQuerySystemAdmins() throws Exception {
            mvc.perform(get("/api/v1/users?role=system-admin")
                            .header("Authorization", authHeaderFor(TEACHER_CODE)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"))
                    .andExpect(jsonPath("$.error.message").value(MSG_005));
        }

        @Test
        @DisplayName("(e) POST /users body role=system-admin JWT teacher → 403 (tầng quyền, trước validate role)")
        void teacherCannotCreateSystemAdmin() throws Exception {
            mvc.perform(post("/api/v1/users")
                            .header("Authorization", authHeaderFor(TEACHER_CODE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "X",
                                    "email", "x@x.vn",
                                    "phone", "0912345678",
                                    "role", "system-admin",
                                    "schoolId", "SCH-HN-0042"
                            ))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"));
        }

        @Test
        @DisplayName("(f) GET /profile/me?userId=U-0001 → trả hồ sơ U-0004 (query param bị bỏ qua)")
        void queryParamUserIdIgnoredInProfile() throws Exception {
            org.springframework.test.web.servlet.MvcResult result = mvc.perform(get("/api/v1/profile/me?userId=U-0001")
                            .header("Authorization", authHeaderFor(TEACHER_CODE)))
                    .andExpect(status().isOk())
                    .andReturn();

            com.fasterxml.jackson.databind.JsonNode body = parse(result);
            // Phải trả hồ sơ của U-0004 (Nguyễn Thu Trang), không phải U-0001
            assertThat(body.get("email").asText()).isEqualTo("trangnt@dtd.edu.vn");
        }

        @Test
        @DisplayName("(g) Header X-User-Role: system-admin + JWT teacher → GET /users ⇒ 403")
        void customRoleHeaderIgnored() throws Exception {
            mvc.perform(get("/api/v1/users")
                            .header("Authorization", authHeaderFor(TEACHER_CODE))
                            .header("X-User-Role", "system-admin"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"))
                    .andExpect(jsonPath("$.error.message").value(MSG_005));
        }
    }
}
