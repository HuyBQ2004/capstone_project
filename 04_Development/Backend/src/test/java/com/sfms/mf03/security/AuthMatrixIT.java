package com.sfms.mf03.security;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF03-TC-SEC-005 — Ma trận đầy đủ 7 role × endpoint (TDS-MF03 §16 Authorization Matrix).
 *
 * <p>Oracle: TDS §16 (bảng 7 cột × ~20 hàng — nguồn duy nhất) · BR-007 (RBAC 7 role) · D6
 * {@code RoleKey} (kebab-case) · MSG-005. Ô {@code ✅} → KHÔNG 403 (2xx/4xx nghiệp vụ khác đều
 * hợp lệ, tài nguyên placeholder không tồn tại nên 404/422/400 là bình thường); ô {@code ❌} →
 * **403** đúng {@code STU-005}.
 *
 * <p>Bài test này chỉ khoá phần {@code @PreAuthorize} theo ROLE của bảng §16 (đúng phạm vi của
 * "gate quyền" — {@code denyAll}/{@code hasRole}/{@code hasAnyRole}). Phần phạm vi dữ liệu
 * ({@code Own}/{@code Class}/{@code School} trong các ô {@code ✅}) đã có test riêng và SÂU hơn ở
 * {@code ParentScopeIT} (SEC-001), {@code TeacherScopeIT} (SEC-002), {@code CrossSchoolIsolationIT}
 * (SEC-003), {@code IdorIT} (SEC-009) — gộp chung vào ma trận 1 bảng sẽ làm loãng lý do fail.
 *
 * <p>Dùng {@code ZZZ-NOTFOUND} làm mọi path-param: tài nguyên không tồn tại nhưng
 * {@code @PreAuthorize} (Spring Security method interceptor) áp dụng TRƯỚC khi service tra DB, vì
 * vậy vai trò bị chặn phải nhận 403 bất kể tài nguyên có tồn tại hay không (nếu 404 xuất hiện
 * trước 403 thì đúng là lỗi thứ tự — thứ chính TC này bắt được). Body request dùng dữ liệu tối
 * thiểu hợp lệ theo Bean Validation của từng DTO — nếu để JSON rỗng cho request có
 * {@code @NotBlank}, lỗi 400 validate sẽ xảy ra TRƯỚC {@code @PreAuthorize} (đơn giản vì Spring
 * MVC resolve {@code @RequestBody} trước khi gọi qua AOP proxy) và che mất phép thử — đây chính
 * là bẫy mà TestSpec §5.1 Red Gate cảnh báo cho MF03-TC-SEC-005 ("phải FAIL ở ô ✅, không phải chỉ
 * ở ô ❌").
 */
class AuthMatrixIT extends AbstractIamIntegrationTest {

    private static final String PH = "ZZZ-NOTFOUND";

    private record Endpoint(String label, String method, String path, String body, Set<RoleKey> allowed) {
    }

    private static final List<Endpoint> ENDPOINTS = List.of(
            new Endpoint("GET /students", "GET", "/api/v1/students", null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN, RoleKey.MEDICAL_STAFF, RoleKey.TEACHER,
                            RoleKey.PARENT, RoleKey.SERVICE_CATERERS)),
            new Endpoint("GET /students/{code}", "GET", "/api/v1/students/" + PH, null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN, RoleKey.MEDICAL_STAFF, RoleKey.TEACHER, RoleKey.PARENT)),
            new Endpoint("POST /students", "POST", "/api/v1/students",
                    "{\"name\":\"x\",\"dob\":\"2020-01-01\",\"classroomId\":\"" + PH + "\"}",
                    EnumSet.of(RoleKey.SCHOOL_ADMIN)),
            new Endpoint("PATCH /students/{code}", "PATCH", "/api/v1/students/" + PH, "{}",
                    EnumSet.of(RoleKey.SCHOOL_ADMIN)),
            new Endpoint("POST /students/{code}/parents", "POST", "/api/v1/students/" + PH + "/parents",
                    "{\"fullName\":\"x\",\"email\":\"x@example.com\"}",
                    EnumSet.of(RoleKey.SCHOOL_ADMIN)),
            new Endpoint("DELETE /students/{code}/parents/{parentCode}", "DELETE",
                    "/api/v1/students/" + PH + "/parents/" + PH, null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN)),
            new Endpoint("GET /students/template", "GET", "/api/v1/students/template", null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN)),
            new Endpoint("GET /students/{code}/allergy-alerts", "GET",
                    "/api/v1/students/" + PH + "/allergy-alerts", null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN, RoleKey.MEDICAL_STAFF, RoleKey.TEACHER, RoleKey.SERVICE_CATERERS)),
            new Endpoint("GET /parents/{code}/students", "GET", "/api/v1/parents/" + PH + "/students", null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN, RoleKey.MEDICAL_STAFF, RoleKey.PARENT)),
            new Endpoint("GET /allergy-declarations", "GET", "/api/v1/allergy-declarations", null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN, RoleKey.MEDICAL_STAFF, RoleKey.PARENT, RoleKey.TEACHER)),
            new Endpoint("POST /allergy-declarations", "POST", "/api/v1/allergy-declarations",
                    "{\"studentId\":\"" + PH + "\",\"category\":\"x\",\"allergen\":\"x\",\"severity\":\"MILD\"}",
                    EnumSet.of(RoleKey.PARENT)),
            new Endpoint("PATCH /allergy-declarations/{id}", "PATCH", "/api/v1/allergy-declarations/" + PH,
                    "{\"studentId\":\"" + PH + "\",\"category\":\"x\",\"allergen\":\"x\",\"severity\":\"MILD\"}",
                    EnumSet.of(RoleKey.PARENT)),
            new Endpoint("POST /allergy-declarations/{id}/verify", "POST",
                    "/api/v1/allergy-declarations/" + PH + "/verify", "{}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("POST /allergy-declarations/{id}/request-info", "POST",
                    "/api/v1/allergy-declarations/" + PH + "/request-info", "{\"reason\":\"x\"}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("GET /chronic-conditions", "GET", "/api/v1/chronic-conditions", null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN, RoleKey.MEDICAL_STAFF, RoleKey.PARENT, RoleKey.TEACHER)),
            new Endpoint("POST /chronic-conditions", "POST", "/api/v1/chronic-conditions",
                    "{\"studentId\":\"" + PH + "\",\"category\":\"x\",\"conditionName\":\"x\",\"severity\":\"MILD\"}",
                    EnumSet.of(RoleKey.PARENT)),
            new Endpoint("PATCH /chronic-conditions/{id}", "PATCH", "/api/v1/chronic-conditions/" + PH,
                    "{\"studentId\":\"" + PH + "\",\"category\":\"x\",\"conditionName\":\"x\",\"severity\":\"MILD\"}",
                    EnumSet.of(RoleKey.PARENT)),
            new Endpoint("POST /chronic-conditions/{id}/verify", "POST",
                    "/api/v1/chronic-conditions/" + PH + "/verify", "{}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("POST /chronic-conditions/{id}/request-info", "POST",
                    "/api/v1/chronic-conditions/" + PH + "/request-info", "{\"reason\":\"x\"}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("GET /special-meals", "GET", "/api/v1/special-meals", null,
                    EnumSet.of(RoleKey.SCHOOL_ADMIN, RoleKey.MEDICAL_STAFF, RoleKey.PARENT, RoleKey.TEACHER,
                            RoleKey.SERVICE_CATERERS)),
            new Endpoint("POST /special-meals", "POST", "/api/v1/special-meals",
                    "{\"studentId\":\"" + PH + "\",\"dietaryType\":\"Halal\",\"duration\":\"x\",\"justification\":\"x\"}",
                    EnumSet.of(RoleKey.PARENT)),
            new Endpoint("POST /special-meals/{id}/approve", "POST",
                    "/api/v1/special-meals/" + PH + "/approve", "{}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("POST /special-meals/{id}/reject", "POST",
                    "/api/v1/special-meals/" + PH + "/reject", "{}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("GET /students/{code}/health-file", "GET", "/api/v1/students/" + PH + "/health-file", null,
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("PATCH /students/{code}/health-file", "PATCH",
                    "/api/v1/students/" + PH + "/health-file", "{}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF)),
            new Endpoint("GET /students/{code}/health-record", "GET",
                    "/api/v1/students/" + PH + "/health-record", null,
                    EnumSet.of(RoleKey.MEDICAL_STAFF, RoleKey.PARENT)),
            new Endpoint("PUT /students/{code}/health-record", "PUT",
                    "/api/v1/students/" + PH + "/health-record", "{}",
                    EnumSet.of(RoleKey.MEDICAL_STAFF, RoleKey.PARENT)),
            new Endpoint("GET /health-history", "GET", "/api/v1/health-history?studentId=" + PH, null,
                    EnumSet.of(RoleKey.MEDICAL_STAFF, RoleKey.PARENT))
    );

    static Stream<Arguments> matrix() {
        return ENDPOINTS.stream()
                .flatMap(ep -> Stream.of(RoleKey.values()).map(role -> Arguments.of(ep, role)));
    }

    @ParameterizedTest(name = "[{index}] {0} × {1}")
    @MethodSource("matrix")
    @DisplayName("MF03-TC-SEC-005 — §16: mỗi ô role×endpoint khớp ✅/❌ đã khai")
    void enforcesAuthorizationMatrix(Endpoint endpoint, RoleKey role) throws Exception {
        MockHttpServletRequestBuilder builder = buildRequest(endpoint, role);

        var result = mvc.perform(builder);
        int status = result.andReturn().getResponse().getStatus();

        if (endpoint.allowed().contains(role)) {
            assertThat(status)
                    .as("§16 khai ✅ cho %s ở %s — KHÔNG được bị chặn bởi @PreAuthorize (403)",
                            role, endpoint.label())
                    .isNotEqualTo(403);
        } else {
            result.andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.error.code").value("STU-005"));
        }
    }

    private MockHttpServletRequestBuilder buildRequest(Endpoint endpoint, RoleKey role) {
        String path = resolvePath(endpoint, role);
        MockHttpServletRequestBuilder builder = switch (endpoint.method()) {
            case "GET" -> get(path);
            case "POST" -> post(path);
            case "PATCH" -> patch(path);
            case "PUT" -> put(path);
            case "DELETE" -> delete(path);
            default -> throw new IllegalStateException("HTTP method chưa hỗ trợ: " + endpoint.method());
        };
        builder.header("Authorization", authHeaderFor(userCodeOf(role)));
        if (endpoint.body() != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }
        return builder;
    }

    /**
     * {@code GET /parents/{code}/students} là endpoint {@code Own}-scoped cho PARENT (§16) —
     * service so path param với chính mã PH trong JWT (BR-030/C2), nên với placeholder
     * {@code ZZZ-NOTFOUND} vai trò PARENT sẽ bị 403 "sai phạm vi" dù §16 khai ✅, che mất phép
     * thử role-gate (phát hiện ở lần chạy Red Gate đầu tiên — 196 ca, 1 fail ở đúng ô này).
     * Khi actor là PARENT, dùng MÃ CHÍNH MÌNH thay placeholder để tách bạch "role bị chặn"
     * khỏi "đúng role nhưng sai phạm vi" (phạm vi đã có {@code ParentScopeIT} kiểm sâu hơn).
     */
    private String resolvePath(Endpoint endpoint, RoleKey role) {
        if ("GET /parents/{code}/students".equals(endpoint.label()) && role == RoleKey.PARENT) {
            return "/api/v1/parents/" + userCodeOf(RoleKey.PARENT) + "/students";
        }
        return endpoint.path();
    }
}
