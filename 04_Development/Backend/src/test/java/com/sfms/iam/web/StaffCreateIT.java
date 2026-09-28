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
 * MF01-TC-INT-010 — {@code POST /staff}: chỉ School Administration, trong trường mình (UC-024, BR-013).
 *
 * <p>Oracle: BR-013 · UC-024 NF bước 4 · MSG-006/MSG-031/MSG-005 · TDS §16.
 * <p>Nhóm "gọi API trực tiếp bỏ qua UI" — guard phải ở tầng server, không phải dropdown UI.
 */
class StaffCreateIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("MF01-TC-INT-010 — POST /staff: chỉ School Admin, trường mình (BR-013)")
    class Tc_Int_010 {

        /** TDS §9.2 — 201 trả thẳng {@code UserResponse}, không có field {@code message}. */
        @Test
        @DisplayName("School Admin trường A tạo teacher trường A ⇒ 201; school_id từ JWT (TDS §17 C2)")
        void schoolAdminCreatesTeacherOwnSchool() throws Exception {
            mvc.perform(post("/api/v1/staff")
                            .header("Authorization", authHeaderFor("U-0001"))  // school-admin SCH-HN-0042
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "Nhân Viên Mới",
                                    "email", "nhanvien@dtd.edu.vn",
                                    "phone", "0918222333",
                                    "role", "teacher"
                            ))))
                    .andExpect(status().isCreated());

            // DB: school_id = SCH-HN-0042 (lấy từ JWT, không từ body)
            String schoolCode = jdbc.queryForObject("""
                SELECT s.code FROM user_accounts u
                JOIN schools s ON s.id = u.school_id
                WHERE u.email='nhanvien@dtd.edu.vn'
                """, String.class);
            assertThat(schoolCode).isEqualTo("SCH-HN-0042");

            // status = PENDING_VERIFICATION
            String status = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE email='nhanvien@dtd.edu.vn'", String.class);
            assertThat(status).isEqualTo("PENDING_VERIFICATION");
        }

        @Test
        @DisplayName("Body kèm schoolId trường khác → bị bỏ qua hoặc 403 (TDS §17 C2); bản ghi vẫn thuộc trường A")
        void schoolIdInBodyIsIgnoredOrRejected() throws Exception {
            MvcResult result = mvc.perform(post("/api/v1/staff")
                            .header("Authorization", authHeaderFor("U-0001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "Test Cross School",
                                    "email", "crossschool@dtd.edu.vn",
                                    "phone", "0918222344",
                                    "role", "teacher",
                                    "schoolId", "SCH-HN-0051"  // trường khác — phải bị bỏ qua
                            ))))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 201) {
                // Nếu implement chọn bỏ qua: bản ghi vẫn thuộc SCH-HN-0042
                String schoolCode = jdbc.queryForObject("""
                    SELECT s.code FROM user_accounts u
                    JOIN schools s ON s.id = u.school_id
                    WHERE u.email='crossschool@dtd.edu.vn'
                    """, String.class);
                assertThat(schoolCode).isEqualTo("SCH-HN-0042");
            } else {
                // Nếu implement chọn chặn: 403
                assertThat(status).isEqualTo(403);
                JsonNode body = parse(result);
                assertThat(body.get("error").get("code").asText()).isEqualTo("IAM-004");
            }
        }

        @Test
        @DisplayName("Tạo role system-admin qua /staff ⇒ 403 IAM-004 (UC-024 bước 4: chỉ role cấp trường)")
        void systemAdminRoleRejectedOnStaff() throws Exception {
            mvc.perform(post("/api/v1/staff")
                            .header("Authorization", authHeaderFor("U-0001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "Leo Quyen",
                                    "email", "leoquyen@dtd.edu.vn",
                                    "phone", "0918222355",
                                    "role", "system-admin"
                            ))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"))
                    .andExpect(jsonPath("$.error.message").value(MSG_005));
        }

        @Test
        @DisplayName("Tạo role parent / supplier / service-caterers ⇒ 403 (không phải role cấp trường)")
        void nonSchoolRolesRejected() throws Exception {
            // 13/08/2026 (theo Nam) — service-caterers KHÔNG còn tạo được qua /staff: đó là tài
            // khoản đối tác, phải qua đăng ký + duyệt hợp tác (UC-101/UC-106), không phải
            // "nhân viên trường". Trước đây role này lọt qua isSchoolLevelStaffRole().
            for (String role : new String[]{"parent", "supplier", "service-caterers"}) {
                mvc.perform(post("/api/v1/staff")
                                .header("Authorization", authHeaderFor("U-0001"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "name", "Test Role " + role,
                                        "email", "role." + role + "@dtd.edu.vn",
                                        "phone", "0918222366",
                                        "role", role
                                ))))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.error.code").value("IAM-004"));
            }
        }

        @Test
        @DisplayName("System Admin gọi POST /staff ⇒ 403 (§16: System Admin dùng POST /users)")
        void sysAdminCannotUseStaffEndpoint() throws Exception {
            mvc.perform(post("/api/v1/staff")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "SysAdmin Attempt",
                                    "email", "sysattempt@dtd.edu.vn",
                                    "phone", "0918222377",
                                    "role", "teacher"
                            ))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"));
        }

        @Test
        @DisplayName("Teacher gọi POST /staff ⇒ 403 IAM-004")
        void teacherCannotCreateStaff() throws Exception {
            mvc.perform(post("/api/v1/staff")
                            .header("Authorization", authHeaderFor("U-0004"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "Teacher Attempt",
                                    "email", "teachattempt@dtd.edu.vn",
                                    "phone", "0918222388",
                                    "role", "teacher"
                            ))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"));
        }
    }
}
