package com.sfms.iam.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-015 — Resource không tồn tại ⇒ 404 + MSG-137.
 * MF01-TC-INT-016 — DTO sai/thiếu field ⇒ 422 + MSG-138.
 *
 * <p>
 * Oracle INT-015: MSG-137 · IAM-404 · D2 envelope · chốt 2026-07-31 TDS §10 (§2
 * L15).
 * Oracle INT-016: MSG-138 · IAM-422 · D2 envelope · chốt 2026-07-31 TDS §10 (§2
 * L15).
 */
class ErrorEnvelopeIT extends AbstractIamIntegrationTest {

        // ═══════════════════════════════════════════════════════════════════════
        // MF01-TC-INT-015 — 404 + MSG-137
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF01-TC-INT-015 — resource không tồn tại ⇒ 404 IAM-404 + MSG-137")
        class Tc_Int_015 {

                /**
                 * {@code (method, path, body)} thuần dữ liệu — KHÔNG dựng
                 * {@code RequestBuilder} ở đây.
                 * {@code @MethodSource} là {@code static} nên chạy TRƯỚC {@code @BeforeEach};
                 * dựng JWT
                 * thật cần {@code jwtService}/{@code userRepository} (instance field, cần
                 * Spring context
                 * đã khởi tạo) nên phải hoãn tới trong thân {@code @ParameterizedTest}.
                 */
                static Stream<Arguments> notFoundRequests() {
                        return Stream.of(
                                        Arguments.of("GET /users/U-9999", "GET", "/api/v1/users/U-9999", (String) null),
                                        Arguments.of("PATCH /users/U-9999/status", "PATCH",
                                                        "/api/v1/users/U-9999/status", "{\"active\":false}"),
                                        Arguments.of("DELETE /roles/ROLE-99", "DELETE", "/api/v1/roles/ROLE-99",
                                                        (String) null),
                                        Arguments.of(
                                                        "GET /users/{validUUID} → 404 vì D1: path param là code, không phải UUID",
                                                        "GET", "/api/v1/users/00000000-1234-5678-abcd-000000000001",
                                                        (String) null));
                }

                @ParameterizedTest(name = "{0}")
                @MethodSource("notFoundRequests")
                @DisplayName("404 IAM-404 + MSG-137 cho mọi endpoint")
                void resourceNotFoundReturns404(String description, String method, String path, String body)
                                throws Exception {
                        var request = switch (method) {
                                case "GET" -> get(path);
                                case "PATCH" -> patch(path);
                                case "DELETE" -> delete(path);
                                default -> throw new IllegalArgumentException(method);
                        };
                        request.header("Authorization", authHeaderFor("U-9001"));
                        if (body != null) {
                                request.contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(body);
                        }

                        mvc.perform(request)
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.error.code").value("IAM-404"))
                                        .andExpect(jsonPath("$.error.message").value(MSG_137))
                                        .andExpect(jsonPath("$.error.details").isArray());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // MF01-TC-INT-016 — 422 + MSG-138
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF01-TC-INT-016 — DTO sai/thiếu field ⇒ 422 IAM-422 + MSG-138")
        class Tc_Int_016 {

                @ParameterizedTest(name = "{0}")
                @MethodSource("invalidRequestCases")
                @DisplayName("422 IAM-422 + MSG-138 + details không null")
                void invalidDtoReturns422(String description, String url, String body) throws Exception {
                        mvc.perform(post(url)
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isUnprocessableContent())
                                        .andExpect(jsonPath("$.error.code").value("IAM-422"))
                                        .andExpect(jsonPath("$.error.message").value(MSG_138))
                                        .andExpect(jsonPath("$.error.details").isArray());
                }

                static Stream<Arguments> invalidRequestCases() {
                        return Stream.of(
                                        Arguments.of(
                                                        "POST /users: thiếu email",
                                                        "/api/v1/users",
                                                        "{\"name\":\"Test\",\"phone\":\"0912345678\",\"role\":\"teacher\",\"schoolId\":\"SCH-HN-0042\"}"),
                                        Arguments.of(
                                                        "POST /users: email không hợp lệ",
                                                        "/api/v1/users",
                                                        "{\"name\":\"Test\",\"email\":\"not-an-email\",\"phone\":\"0912345678\",\"role\":\"teacher\",\"schoolId\":\"SCH-HN-0042\"}"),
                                        Arguments.of(
                                                        "POST /users: thiếu name",
                                                        "/api/v1/users",
                                                        "{\"email\":\"valid@test.vn\",\"phone\":\"0912345678\",\"role\":\"teacher\",\"schoolId\":\"SCH-HN-0042\"}"),
                                        Arguments.of(
                                                        "POST /auth/login: thiếu password",
                                                        "/api/v1/auth/login",
                                                        "{\"email\":\"toanpq@dtd.edu.vn\"}"),
                                        Arguments.of(
                                                        "POST /auth/login: thiếu email",
                                                        "/api/v1/auth/login",
                                                        "{\"password\":\"P@ssw0rd123\"}"),
                                        Arguments.of(
                                                        "POST /auth/refresh: thiếu refreshToken",
                                                        "/api/v1/auth/refresh",
                                                        "{}"));
                }

                @org.junit.jupiter.api.Test
                @DisplayName("422 details không null, là array (D2 envelope)")
                void detailsIsArrayNotNull() throws Exception {
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isUnprocessableContent())
                                        .andExpect(jsonPath("$.error.code").value("IAM-422"))
                                        .andExpect(jsonPath("$.error.details").isArray());
                }

                @org.junit.jupiter.api.Test
                @DisplayName("D4: ngày dạng dd/MM/yyyy ⇒ 422 (chỉ ISO-8601 được chấp nhận)")
                void nonIsoDateFieldRejected() throws Exception {
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                        .content("{\"name\":\"Test\",\"email\":\"d4@dtd.edu.vn\",\"phone\":\"0912345678\",\"role\":\"teacher\",\"schoolId\":\"SCH-HN-0042\",\"birthDate\":\"30/07/2000\"}"))
                                        .andExpect(status().isUnprocessableContent());
                }

                @org.junit.jupiter.api.Test
                @DisplayName("D6: role không phải kebab-case ⇒ 422")
                void nonKebabRoleRejected() throws Exception {
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                        .content("{\"name\":\"Test\",\"email\":\"d6@dtd.edu.vn\",\"phone\":\"0912345678\",\"role\":\"TEACHER\",\"schoolId\":\"SCH-HN-0042\"}"))
                                        .andExpect(status().isUnprocessableContent());
                }

                @org.junit.jupiter.api.Test
                @DisplayName("Malformed JSON body ⇒ 422 (không 500)")
                void malformedJsonReturns422Not500() throws Exception {
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                        .content("{ malformed json"))
                                        .andExpect(status().isUnprocessableContent());
                }

                @org.junit.jupiter.api.Test
                @DisplayName("Error response KHÔNG chứa stack trace hoặc tên class Java")
                void errorResponseNoStackTrace() throws Exception {
                        org.springframework.test.web.servlet.MvcResult result = mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andReturn();

                        String body = result.getResponse().getContentAsString();
                        org.assertj.core.api.Assertions.assertThat(body)
                                        .doesNotContain("at com.sfms")
                                        .doesNotContain("Exception")
                                        .doesNotContain("StackTrace");
                }
        }

        // ────────────────────────────────────────────────────────────────────────
        // Inline variants (non-parameterized) for INT-015 cross-school + known
        // resources
        // ────────────────────────────────────────────────────────────────────────
        @Nested
        @DisplayName("INT-015 bổ sung: cross-school staff ⇒ 404 (không 403) — không xác nhận sự tồn tại")
        class CrossSchoolNotFound {

                @org.junit.jupiter.api.Test
                @DisplayName("School Admin trường B GET /staff/STF-001 (nhân viên trường A) ⇒ 404")
                void crossSchoolStaffGet404() throws Exception {
                        mvc.perform(get("/api/v1/staff/STF-001")
                                        .header("Authorization", authHeaderFor("U-0018"))) // School Admin trường B
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.error.code").value("IAM-404"))
                                        .andExpect(jsonPath("$.error.message").value(MSG_137));
                }
        }
}
