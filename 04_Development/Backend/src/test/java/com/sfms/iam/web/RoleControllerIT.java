package com.sfms.iam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-012 — {@code DELETE /roles/{code}}: 403 role hệ thống · 409 còn user (UC-029).
 * MF01-TC-INT-013 — {@code GET /roles}: {@code users} là field dẫn xuất tính runtime (UC-130).
 *
 * <p>Oracle INT-012: BR-008 · UC-029 NF bước 4+AF1 · MSG-005/MSG-106 · TDS §9.2 DELETE ·
 * TDS ADR-M04.
 *
 * <p>Oracle INT-013: UC-130 NF bước 3 · BR-008 · TDS §9.2 GET /roles · Team Plan §5 (không lưu field dẫn xuất).
 */
class RoleControllerIT extends AbstractIamIntegrationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-INT-012 — DELETE /roles/{code}
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-INT-012 — DELETE /roles/{code}: guard hệ thống + 409 còn user")
    class Tc_Int_012 {

        @Test
        @DisplayName("DELETE role hệ thống ROLE-01 ⇒ 403 IAM-004 + MSG-005; 7 role system vẫn còn")
        void deleteSystemRoleBlocked() throws Exception {
            mvc.perform(delete("/api/v1/roles/ROLE-01")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"))
                    .andExpect(jsonPath("$.error.message").value(MSG_005));

            // 7 role system bất biến (TDS §14.1)
            long systemCount = count("SELECT count(*) FROM roles WHERE system = true");
            assertThat(systemCount).isEqualTo(7);
        }

        @Test
        @DisplayName("DELETE ROLE-09 (custom, 3 user kể cả INACTIVE) ⇒ 409 IAM-009 + MSG-106 render 3")
        void deleteRoleWithUsersReturns409() throws Exception {
            mvc.perform(delete("/api/v1/roles/ROLE-09")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("IAM-009"))
                    .andExpect(jsonPath("$.error.message").value(msg106(3)));  // BR-008: kể cả INACTIVE

            // Bản ghi role còn nguyên
            long roleExists = count("SELECT count(*) FROM roles WHERE code='ROLE-09'");
            assertThat(roleExists).isEqualTo(1);
        }

        @Test
        @DisplayName("DELETE ROLE-08 (custom, 0 user) ⇒ 200; bản ghi xóa; audit +1")
        void deleteEmptyCustomRole() throws Exception {
            mvc.perform(delete("/api/v1/roles/ROLE-08")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk());

            long roleExists = count("SELECT count(*) FROM roles WHERE code='ROLE-08'");
            assertThat(roleExists).isZero();

            long auditRows = count(
                    "SELECT count(*) FROM audit_logs WHERE action LIKE '%DELETE%' OR action LIKE '%ROLE%'");
            assertThat(auditRows).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("DELETE ROLE-08 lần 2 (đã xóa) ⇒ 404 + MSG-137")
        void deleteTwiceReturns404() throws Exception {
            // Xóa lần 1
            mvc.perform(delete("/api/v1/roles/ROLE-08")
                    .header("Authorization", authHeaderFor("U-9001"))).andReturn();

            // Lần 2 — đã không còn
            mvc.perform(delete("/api/v1/roles/ROLE-08")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("IAM-404"))
                    .andExpect(jsonPath("$.error.message").value(MSG_137));
        }

        @Test
        @DisplayName("School Admin DELETE /roles/ROLE-09 ⇒ 403 (§16: chỉ System Admin)")
        void schoolAdminCannotDeleteRole() throws Exception {
            mvc.perform(delete("/api/v1/roles/ROLE-09")
                            .header("Authorization", authHeaderFor("U-0001")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"));
        }

        @Test
        @DisplayName("Path param là code (D1): ROLE-01 — không phải UUID")
        void pathParamIsCodeNotUuid() throws Exception {
            // UUID path param → 404
            mvc.perform(delete("/api/v1/roles/00000000-0000-0000-0000-000000000001")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-INT-013 — GET /roles: users = field dẫn xuất runtime
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-INT-013 — GET /roles: users dẫn xuất runtime (Team Plan §5)")
    class Tc_Int_013 {

        @Test
        @DisplayName("Tạo 1 user role teacher → users của Teacher tăng đúng 1; DB không có cột users")
        void usersFieldIsRuntimeDerived() throws Exception {
            // Lấy users ROLE-TEACHER ban đầu
            MvcResult r1 = mvc.perform(get("/api/v1/roles")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk()).andReturn();

            int initialUsers = getUsersForRole(parse(r1), "ROLE-05");

            // Tạo 1 user mới role teacher
            mvc.perform(post("/api/v1/users")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(toJson(java.util.Map.of(
                            "name", "New Teacher",
                            "email", "newteacher@dtd.edu.vn",
                            "phone", "0911000001",
                            "role", "teacher",
                            "schoolId", "SCH-HN-0042"
                    )))).andReturn();

            // Lấy lại — users phải tăng đúng 1
            MvcResult r2 = mvc.perform(get("/api/v1/roles")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andReturn();

            int afterUsers = getUsersForRole(parse(r2), "ROLE-05");
            assertThat(afterUsers).isEqualTo(initialUsers + 1);
        }

        @Test
        @DisplayName("Deactivate user ⇒ users KHÔNG giảm (BR-008: đếm kể cả INACTIVE)")
        void inactiveUserStillCountedInRoleUsers() throws Exception {
            MvcResult r1 = mvc.perform(get("/api/v1/roles")
                            .header("Authorization", authHeaderFor("U-9001"))).andReturn();
            int beforeUsers = getUsersForRole(parse(r1), "ROLE-05");

            // Deactivate một teacher
            mvc.perform(patch("/api/v1/users/U-0004/status")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(toJson(java.util.Map.of("active", false)))).andReturn();

            MvcResult r2 = mvc.perform(get("/api/v1/roles")
                            .header("Authorization", authHeaderFor("U-9001"))).andReturn();
            int afterUsers = getUsersForRole(parse(r2), "ROLE-05");

            // users KHÔNG giảm (BR-008: kể cả INACTIVE)
            assertThat(afterUsers).isEqualTo(beforeUsers);
        }

        @Test
        @DisplayName("DB không có cột users trong bảng roles (Team Plan §5)")
        void databaseHasNoUsersColumn() throws Exception {
            long colCount = count("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_name='roles' AND column_name IN ('users','user_count')
                """);
            assertThat(colCount).isZero();
        }

        @Test
        @DisplayName("ROLE-01 (System Admin) đúng 3 user gán (U-9000 primary + U-9001 + U-9900 — "
                + "V25, seed system-admin login được cho dev/prod, MF12 Bước 3)")
        void systemAdminRoleHasTwoAssignedUsers() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/roles")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andReturn();
            assertThat(getUsersForRole(parse(result), "ROLE-01")).isEqualTo(3);
        }

        @Test
        @DisplayName("GET /roles: response shape có 5 khóa phân trang + items với id/name/description/system/users/permissions")
        void rolesResponseShape() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/roles")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            assertThat(body.has("items")).isTrue();
            assertThat(body.has("total")).isTrue();
            assertThat(body.has("page")).isTrue();
            assertThat(body.has("pageSize")).isTrue();
            assertThat(body.has("pageCount")).isTrue();

            JsonNode firstItem = body.get("items").get(0);
            assertThat(firstItem.has("id")).isTrue();
            assertThat(firstItem.has("name")).isTrue();
            assertThat(firstItem.has("system")).isTrue();
            assertThat(firstItem.has("users")).isTrue();
            assertThat(firstItem.has("permissions")).isTrue();
        }

        /**
         * ⚠️ So khớp theo {@code id} (= {@code role.code}, VD {@code "ROLE-05"}) — KHÔNG theo
         * {@code RoleKey} kebab-case. {@link com.sfms.iam.mapper.RoleMapper} map {@code id} từ
         * {@code role.code}, không phải từ {@code role.roleKey}; so khớp theo roleKey ("teacher")
         * sẽ không bao giờ khớp bất kỳ {@code id} nào và luôn rơi vào {@code return 0}.
         */
        private int getUsersForRole(JsonNode rolesResponse, String code) {
            for (JsonNode item : rolesResponse.get("items")) {
                if (code.equalsIgnoreCase(item.get("id").asText())) {
                    return item.get("users").asInt();
                }
            }
            throw new IllegalStateException("Role " + code + " không có trong response — kiểm tra seed");
        }
    }
}
