package com.sfms.mf12.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF12-TC-SEC-001 (không JWT → 401 trên mọi endpoint trừ public) + MF12-TC-SEC-002 (non-SysAdmin
 * chặn nhóm quản trị hệ thống) + MF12-TC-SEC-003 (non-MedicalStaff chặn gửi báo cáo) +
 * MF12-TC-SEC-015 (ma trận 7 role × N endpoint khớp §16 100%) — gộp một file: oracle nằm
 * TRONG test (danh sách allow-list khai tay), không đọc từ production, chống tautology.
 *
 * ⚠️ Không phủ {@code /audit-logs} 5 verb ghi (không {@code @PreAuthorize}, luôn 405 IAM-162
 * bất kể vai trò) — đã test kỹ hơn ở {@code AuditImmutabilityIT} (TC-SEC-019). Không phủ
 * {@code POST /sent-reports/incidents} (không {@code @PreAuthorize}, luôn 409 AIS-188 bất kể
 * vai trò, kể cả ẩn danh vẫn 401 trước khi tới nghiệp vụ) — test ở {@code SentReportFlowIT}.
 * {@code TC-SEC-002a/002b} (School Admin × audit-logs) giữ nguyên {@code @Disabled} — chờ Nam
 * chốt CF-21, KHÔNG tự chọn bên (Policy §2).
 */
class PlatformAuthorizationMatrixTest extends AbstractIamIntegrationTest {

    private static final Set<String> ALL_ROLES = Set.of(
            "SYSTEM_ADMIN", "SCHOOL_ADMIN", "TEACHER", "PARENT", "MEDICAL_STAFF",
            "SERVICE_CATERERS", "SUPPLIER");

    private record Endpoint(String method, String path, Set<String> allowedRoles, String body) {
        Endpoint(String method, String path, Set<String> allowedRoles) {
            this(method, path, allowedRoles, "{}");
        }

        /**
         * Body PHẢI hợp lệ theo {@code @Valid} — nếu không, {@code MethodArgumentNotValidException}
         * (400) nổ ra TRƯỚC khi Spring Security AOP proxy kịp chạy {@code @PreAuthorize} (argument
         * resolution nằm NGOÀI method interceptor), khiến mọi vai trò đều nhận 400 thay vì 403 —
         * test sẽ pass giả vì "không phải 403" luôn đúng bất kể oracle.
         */
        MockHttpServletRequestBuilder request() {
            return switch (method) {
                case "GET" -> get(path);
                case "POST" -> post(path).contentType(MediaType.APPLICATION_JSON).content(body);
                case "PUT" -> put(path).contentType(MediaType.APPLICATION_JSON).content(body);
                default -> throw new IllegalStateException("verb chưa hỗ trợ: " + method);
            };
        }
    }

    /** Oracle — khai tay từ {@code TDS-MF12_Backend.md} §16, KHÔNG đọc @PreAuthorize thật. */
    private static List<Endpoint> endpoints() {
        Set<String> sysAdminOnly = Set.of("SYSTEM_ADMIN");
        Set<String> sysAndSchoolAdmin = Set.of("SYSTEM_ADMIN", "SCHOOL_ADMIN");
        Set<String> medicalOnly = Set.of("MEDICAL_STAFF");
        Set<String> sentReportReaders = Set.of("SYSTEM_ADMIN", "SCHOOL_ADMIN", "MEDICAL_STAFF");
        String compliancePeriodBody = "{\"rangeFrom\":\"2026-07-06\",\"rangeTo\":\"2026-07-12\"}";
        return List.of(
                new Endpoint("POST", "/api/v1/ai/chat", ALL_ROLES, "{\"message\":\"xin chao\"}"),
                new Endpoint("GET", "/api/v1/ai/conversations", ALL_ROLES),
                new Endpoint("GET", "/api/v1/audit-logs", sysAndSchoolAdmin),
                new Endpoint("GET", "/api/v1/audit-logs/integrity", sysAdminOnly),
                new Endpoint("GET", "/api/v1/error-logs", sysAdminOnly),
                new Endpoint("GET", "/api/v1/integrations", sysAdminOnly),
                new Endpoint("PUT", "/api/v1/integrations/gemini", sysAdminOnly, "{\"endpoint\":\"https://x\"}"),
                new Endpoint("GET", "/api/v1/report-center", medicalOnly),
                new Endpoint("GET", "/api/v1/security-policies", sysAdminOnly),
                new Endpoint("PUT", "/api/v1/security-policies/SEC-DEFAULT", sysAdminOnly),
                new Endpoint("GET", "/api/v1/sent-reports", sentReportReaders),
                new Endpoint("POST", "/api/v1/sent-reports/compliance", medicalOnly, compliancePeriodBody),
                new Endpoint("POST", "/api/v1/sent-reports/violations", medicalOnly, compliancePeriodBody),
                new Endpoint("GET", "/api/v1/system/activities", sysAdminOnly),
                new Endpoint("GET", "/api/v1/system/status", sysAdminOnly),
                new Endpoint("GET", "/api/v1/system/usage", sysAdminOnly),
                new Endpoint("GET", "/api/v1/system/backups", sysAdminOnly)
        );
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> roleEndpointCombinations() {
        return endpoints().stream().flatMap(ep -> ALL_ROLES.stream()
                .map(role -> org.junit.jupiter.params.provider.Arguments.of(ep, role)));
    }

    @ParameterizedTest(name = "{1} → {0}")
    @MethodSource("roleEndpointCombinations")
    void matrixMatches16(Endpoint endpoint, String role) throws Exception {
        String userCode = switch (role) {
            case "SYSTEM_ADMIN" -> "U-9001";
            case "SCHOOL_ADMIN" -> "U-0001";
            case "TEACHER" -> "U-0004";
            case "MEDICAL_STAFF" -> "U-0002";
            case "SERVICE_CATERERS" -> "U-0003";
            case "PARENT" -> "U-0005";
            case "SUPPLIER" -> "U-0006";
            default -> throw new IllegalStateException();
        };
        MvcResult result = mvc.perform(endpoint.request().header("Authorization", authHeaderFor(userCode)))
                .andReturn();
        int status = result.getResponse().getStatus();
        if (endpoint.allowedRoles().contains(role)) {
            assertThat(status).as(role + " → " + endpoint.method() + " " + endpoint.path())
                    .isNotEqualTo(403);
        } else {
            assertThat(status).as(role + " → " + endpoint.method() + " " + endpoint.path())
                    .isEqualTo(403);
        }
    }

    /** MF12-TC-SEC-001 — không JWT → 401 trên mọi endpoint bảo vệ; public 200. */
    @Test
    void noJwtIsRejectedOnEveryProtectedEndpointPublicEndpointStaysOpen() throws Exception {
        for (Endpoint ep : endpoints()) {
            MvcResult result = mvc.perform(ep.request()).andReturn();
            assertThat(result.getResponse().getStatus())
                    .as(ep.method() + " " + ep.path() + " không JWT")
                    .isEqualTo(401);
            assertThat(parse(result).at("/error/code").asText())
                    .as(ep.method() + " " + ep.path())
                    .isEqualTo("IAM-005");
        }

        MvcResult publicEndpoint = mvc.perform(get("/api/v1/public/password-policy")).andReturn();
        assertThat(publicEndpoint.getResponse().getStatus()).isEqualTo(200);
    }
}
