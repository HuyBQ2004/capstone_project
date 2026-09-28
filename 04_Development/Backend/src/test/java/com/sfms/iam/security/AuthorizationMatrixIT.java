package com.sfms.iam.security;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.stream.Stream;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-001 — Ma trận RBAC 7 role × 12 nhóm endpoint (parameterized, 84 tổ hợp).
 *
 * <p>Oracle: TDS §16 Authorization Matrix · BR-007 · BR-013 · BR-008 · MSG-005 · D6 (7 RoleKey).
 *
 * <p>⚠️ Cạm bẫy Red Gate (TestSpec §5.1): ô ❌ rất dễ PASS giả khi service stub throw 500.
 * Ba assert bắt buộc: {@code status().isForbidden()} AND {@code error.code=IAM-004} AND
 * {@code error.message=MSG-005}. Với 3 assert này, 500 ⇒ FAIL đúng như mong đợi.
 */
@DisplayName("MF01-TC-SEC-001 — Ma trận RBAC 7 role × 12 nhóm endpoint (84 tổ hợp)")
class AuthorizationMatrixIT extends AbstractIamIntegrationTest {

    // ─── Mã role chủ thể chuẩn (seed §3 TDS-05) ──────────────────────────────
    private static final String SYS    = "U-9001";  // system-admin
    private static final String SCH    = "U-0001";  // school-admin  SCH-HN-0042
    private static final String MED    = "U-0002";  // medical-staff
    private static final String TCH    = "U-0004";  // teacher
    private static final String CAT    = "U-0003";  // service-caterers
    private static final String SUP    = "U-0006";  // supplier
    private static final String PAR    = "U-0005";  // parent

    // ─── Các request đại diện cho 12 nhóm endpoint (E1..E12 §16) ─────────────

    /** (E5) GET /users — chỉ system-admin */
    @ParameterizedTest(name = "[{index}] {0} → GET /users → {2}")
    @MethodSource("e5_getUsers")
    @DisplayName("E5: GET /users — system-admin ✅; mọi role khác 403")
    void e5_getUsers(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(get("/api/v1/users"), userCode, expectedStatus);
    }

    static Stream<Arguments> e5_getUsers() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 403),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E6) POST /users — chỉ system-admin */
    @ParameterizedTest(name = "[{index}] {0} → POST /users → {2}")
    @MethodSource("e6_postUsers")
    @DisplayName("E6: POST /users — system-admin ✅; mọi role khác 403")
    void e6_postUsers(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\",\"email\":\"x@x.vn\",\"phone\":\"0912345678\",\"role\":\"teacher\",\"schoolId\":\"SCH-HN-0042\"}"),
                userCode, expectedStatus);
    }

    static Stream<Arguments> e6_postUsers() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 201),
                Arguments.of("school-admin", SCH, 403),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E7) PATCH /users/{code}/status — chỉ system-admin */
    @ParameterizedTest(name = "[{index}] {0} → PATCH /users/U-0004/status → {2}")
    @MethodSource("e7_patchUserStatus")
    @DisplayName("E7: PATCH /users/{code}/status — system-admin ✅; mọi role khác 403")
    void e7_patchUserStatus(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(patch("/api/v1/users/U-0004/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"),
                userCode, expectedStatus);
    }

    static Stream<Arguments> e7_patchUserStatus() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 403),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E8) GET /staff — system-admin và school-admin */
    @ParameterizedTest(name = "[{index}] {0} → GET /staff → {2}")
    @MethodSource("e8_getStaff")
    @DisplayName("E8: GET /staff — system-admin ✅ All; school-admin ✅ School; mọi role khác 403")
    void e8_getStaff(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(get("/api/v1/staff"), userCode, expectedStatus);
    }

    static Stream<Arguments> e8_getStaff() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 200),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E9) POST /staff — chỉ school-admin; system-admin bị chặn */
    @ParameterizedTest(name = "[{index}] {0} → POST /staff → {2}")
    @MethodSource("e9_postStaff")
    @DisplayName("E9: POST /staff — school-admin ✅ School; system-admin ❌; mọi role khác 403")
    void e9_postStaff(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(post("/api/v1/staff")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\",\"email\":\"e9@test.vn\",\"phone\":\"0912345699\",\"role\":\"teacher\"}"),
                userCode, expectedStatus);
    }

    static Stream<Arguments> e9_postStaff() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 403),   // §16: SYS ADMIN ❌
                Arguments.of("school-admin", SCH, 201),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E10) PATCH /staff/{code}/status — chỉ school-admin; system-admin bị chặn */
    @ParameterizedTest(name = "[{index}] {0} → PATCH /staff/STF-001/status → {2}")
    @MethodSource("e10_patchStaffStatus")
    @DisplayName("E10: PATCH /staff/{code}/status — school-admin ✅; system-admin ❌; mọi role khác 403")
    void e10_patchStaffStatus(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(patch("/api/v1/staff/STF-001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"),
                userCode, expectedStatus);
    }

    static Stream<Arguments> e10_patchStaffStatus() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 403),   // §16: SYS ADMIN ❌
                Arguments.of("school-admin", SCH, 200),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E11) GET /roles, GET /permissions — chỉ system-admin */
    @ParameterizedTest(name = "[{index}] {0} → GET /roles → {2}")
    @MethodSource("e11_getRolesPermissions")
    @DisplayName("E11: GET /roles + /permissions — system-admin ✅; mọi role khác 403")
    void e11_getRolesPermissions(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(get("/api/v1/roles"), userCode, expectedStatus);
    }

    static Stream<Arguments> e11_getRolesPermissions() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 403),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E12) DELETE /roles — chỉ system-admin (BR-008) */
    @ParameterizedTest(name = "[{index}] {0} → DELETE /roles/ROLE-08 → {2}")
    @MethodSource("e12_deleteRoles")
    @DisplayName("E12: DELETE /roles — system-admin ✅; mọi role khác 403")
    void e12_deleteRoles(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(delete("/api/v1/roles/ROLE-08"), userCode, expectedStatus);
    }

    static Stream<Arguments> e12_deleteRoles() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 403),
                Arguments.of("medical-staff", MED, 403),
                Arguments.of("teacher",       TCH, 403),
                Arguments.of("service-caterers", CAT, 403),
                Arguments.of("supplier",      SUP, 403),
                Arguments.of("parent",        PAR, 403)
        );
    }

    /** (E3) GET /profile/me — mọi role đều được (Own) */
    @ParameterizedTest(name = "[{index}] {0} → GET /profile/me → {2}")
    @MethodSource("e3_getProfile")
    @DisplayName("E3: GET /profile/me — mọi role ✅ Own")
    void e3_getProfile(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(get("/api/v1/profile/me"), userCode, expectedStatus);
    }

    static Stream<Arguments> e3_getProfile() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 200),
                Arguments.of("medical-staff", MED, 200),
                Arguments.of("teacher",       TCH, 200),
                Arguments.of("service-caterers", CAT, 200),
                Arguments.of("supplier",      SUP, 200),
                Arguments.of("parent",        PAR, 200)
        );
    }

    /** (E2) PATCH /account/password — mọi role đều được (Own) */
    @ParameterizedTest(name = "[{index}] {0} → PATCH /account/password → {2}")
    @MethodSource("e2_changePassword")
    @DisplayName("E2: PATCH /account/password — mọi role ✅ Own")
    void e2_changePassword(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(patch("/api/v1/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + PWD + "\",\"newPassword\":\"N3wPwd@Matrix1\"}"),
                userCode, expectedStatus);
    }

    static Stream<Arguments> e2_changePassword() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 200),
                Arguments.of("medical-staff", MED, 200),
                Arguments.of("teacher",       TCH, 200),
                Arguments.of("service-caterers", CAT, 200),
                Arguments.of("supplier",      SUP, 200),
                Arguments.of("parent",        PAR, 200)
        );
    }

    /** (E4) PATCH /profile/me — mọi role đều được (Own) */
    @ParameterizedTest(name = "[{index}] {0} → PATCH /profile/me → {2}")
    @MethodSource("e4_updateProfile")
    @DisplayName("E4: PATCH /profile/me — mọi role ✅ Own")
    void e4_updateProfile(String role, String userCode, int expectedStatus) throws Exception {
        doRequest(patch("/api/v1/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Đổi tên qua ma trận\"}"),
                userCode, expectedStatus);
    }

    static Stream<Arguments> e4_updateProfile() {
        return Stream.of(
                Arguments.of("system-admin", SYS, 200),
                Arguments.of("school-admin", SCH, 200),
                Arguments.of("medical-staff", MED, 200),
                Arguments.of("teacher",       TCH, 200),
                Arguments.of("service-caterers", CAT, 200),
                Arguments.of("supplier",      SUP, 200),
                Arguments.of("parent",        PAR, 200)
        );
    }

    /**
     * (E1) {@code POST /auth/login · /refresh · /logout} — public, không phân biệt vai trò.
     * Không dùng {@code doRequest} (yêu cầu JWT có sẵn) vì đây chính là endpoint PHÁT HÀNH JWT.
     */
    @ParameterizedTest(name = "[{index}] {0} → login/refresh/logout → 200")
    @MethodSource("e1_publicAuthEndpoints")
    @DisplayName("E1: POST /auth/login · /refresh · /logout — mọi role ✅ (public)")
    void e1_publicAuthEndpoints(String role, String email) throws Exception {
        var login = login(email, PWD);
        String accessToken = login.get("accessToken").asText();
        String refreshToken = login.get("refreshToken").asText();

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/auth/logout").header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk());
    }

    static Stream<Arguments> e1_publicAuthEndpoints() {
        return Stream.of(
                Arguments.of("system-admin", "sysadmin@sfms.vn"),
                Arguments.of("school-admin", "toanpq@dtd.edu.vn"),
                Arguments.of("medical-staff", "hant@dtd.edu.vn"),
                Arguments.of("teacher", "trangnt@dtd.edu.vn"),
                Arguments.of("service-caterers", "hungtv@anbinhcatering.vn"),
                Arguments.of("supplier", "sonlt@greenfarm.vn"),
                Arguments.of("parent", "binhnv.parent@gmail.com")
        );
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private void doRequest(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            String userCode,
            int expectedStatus) throws Exception {

        var resultActions = mvc.perform(request.header("Authorization", authHeaderFor(userCode)));

        if (expectedStatus == 403) {
            resultActions
                    .andExpect(status().isForbidden())
                    // ⚠️ 3 assert bắt buộc theo §5.1 để tránh AP-AI-002 (stub 500 ≠ 403)
                    .andExpect(jsonPath("$.error.code").value("IAM-004"))
                    .andExpect(jsonPath("$.error.message").value(MSG_005));
        } else if (expectedStatus == 200 || expectedStatus == 201) {
            resultActions.andExpect(status().is(expectedStatus));
        } else {
            resultActions.andExpect(status().is(expectedStatus));
        }
    }
}
