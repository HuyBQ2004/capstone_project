package com.sfms.iam.security;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-006 — Cách ly {@code schoolId}: School Admin trường A không thấy/không sửa staff trường B.
 * MF01-TC-SEC-007 — Cách ly {@code partnerId}: tài khoản NCC chỉ thấy dữ liệu của đối tác mình.
 *
 * <p>Oracle SEC-006: BR-013 · UC-144 NF bước 2 · TDS §16 · MSG-137.
 * Oracle SEC-007: TDS §9.2 claim JWT · TDS §5.2 partner_id · D1.
 * <p>OWASP: A01:2021 · CWE: CWE-566 (SEC-006) / CWE-639 (SEC-007).
 */
class TenantIsolationIT extends AbstractIamIntegrationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-SEC-006 — Cách ly schoolId
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-SEC-006 — schoolId isolation: School Admin trường B không thấy trường A")
    class Tc_Sec_006 {

        private static final String SCHOOL_B_ADMIN = "U-0018";  // school-admin SCH-HN-0051

        @Test
        @DisplayName("(1) GET /staff JWT trường B → chỉ trả nhân viên SCH-HN-0051; total không gồm 11 nhân viên A")
        void staffListFilteredByJwtSchool() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/staff")
                            .header("Authorization", authHeaderFor(SCHOOL_B_ADMIN)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            for (JsonNode item : body.get("items")) {
                // Mọi nhân viên phải thuộc SCH-HN-0051
                assertThat(item.get("schoolId").asText()).isEqualTo("SCH-HN-0051");
            }
            // total không được là 11+ (số nhân viên trường A)
            assertThat(body.get("total").asInt()).isLessThanOrEqualTo(2); // chỉ có U-0019 (trường B)
        }

        @Test
        @DisplayName("(2) GET /staff?schoolId=SCH-HN-0042 JWT trường B → bị bỏ qua, kết quả giống (1)")
        void queryParamSchoolIdIgnored() throws Exception {
            MvcResult r1 = mvc.perform(get("/api/v1/staff")
                    .header("Authorization", authHeaderFor(SCHOOL_B_ADMIN))).andReturn();

            MvcResult r2 = mvc.perform(get("/api/v1/staff?schoolId=SCH-HN-0042")
                    .header("Authorization", authHeaderFor(SCHOOL_B_ADMIN))).andReturn();

            int total1 = parse(r1).get("total").asInt();
            int total2 = parse(r2).get("total").asInt();
            assertThat(total1).isEqualTo(total2);
        }

        @Test
        @DisplayName("(3) GET /staff/STF-001 (nhân viên trường A) JWT trường B → 404 (không 403)")
        void crossSchoolStaffGetReturns404() throws Exception {
            mvc.perform(get("/api/v1/staff/STF-001")
                            .header("Authorization", authHeaderFor(SCHOOL_B_ADMIN)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("IAM-404"))
                    .andExpect(jsonPath("$.error.message").value(MSG_137));
        }

        @Test
        @DisplayName("(4) PATCH /staff/STF-001/status JWT trường B → 404 hoặc 403; status KHÔNG đổi")
        void crossSchoolStaffPatchBlocked() throws Exception {
            String statusBefore = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE staff_code='STF-001'", String.class);

            MvcResult result = mvc.perform(patch("/api/v1/staff/STF-001/status")
                            .header("Authorization", authHeaderFor(SCHOOL_B_ADMIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("active", false))))
                    .andReturn();

            int status = result.getResponse().getStatus();
            assertThat(status).isIn(403, 404);

            // status của STF-001 KHÔNG đổi
            String statusAfter = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE staff_code='STF-001'", String.class);
            assertThat(statusAfter).isEqualTo(statusBefore);
        }

        @Test
        @DisplayName("(5) POST /staff body schoolId=SCH-HN-0042 JWT trường B → bản ghi thuộc SCH-HN-0051 hoặc 403")
        void createStaffCrossSchoolBodyIgnored() throws Exception {
            MvcResult result = mvc.perform(post("/api/v1/staff")
                            .header("Authorization", authHeaderFor(SCHOOL_B_ADMIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "Test Cross School",
                                    "email", "crossschool.sec@ns.edu.vn",
                                    "phone", "0918222399",
                                    "role", "teacher",
                                    "schoolId", "SCH-HN-0042"  // trường A — phải bị bỏ qua
                            ))))
                    .andReturn();

            if (result.getResponse().getStatus() == 201) {
                String schoolCode = jdbc.queryForObject("""
                    SELECT s.code FROM user_accounts u
                    JOIN schools s ON s.id = u.school_id
                    WHERE u.email='crossschool.sec@ns.edu.vn'
                    """, String.class);
                assertThat(schoolCode).isEqualTo("SCH-HN-0051");  // KHÔNG phải SCH-HN-0042
            } else {
                assertThat(result.getResponse().getStatus()).isEqualTo(403);
            }
        }

        @Test
        @DisplayName("(6) GET /staff?search=Trang (nhân viên trường A) JWT trường B → rỗng")
        void searchDoesNotRevealOtherSchoolStaff() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/staff?search=Trang")
                            .header("Authorization", authHeaderFor(SCHOOL_B_ADMIN)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            // Không được trả nhân viên trường A
            for (JsonNode item : body.get("items")) {
                assertThat(item.get("schoolId").asText()).isEqualTo("SCH-HN-0051");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-SEC-007 — Cách ly partnerId
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-SEC-007 — partnerId isolation: NCC chỉ thấy dữ liệu của mình")
    class Tc_Sec_007 {

        @Test
        @DisplayName("JWT claim partnerId = \"SUP-001\" (mã nghiệp vụ D1); schoolId = null")
        void supplierJwtContainsPartnerId() throws Exception {
            JsonNode loginBody = login("sonlt@greenfarm.vn", PWD);  // U-0006, SUP-001
            String[] parts = loginBody.get("accessToken").asText().split("\\.");
            JsonNode claims = json.readTree(java.util.Base64.getUrlDecoder().decode(parts[1]));

            assertThat(claims.get("partnerId").asText()).isEqualTo("SUP-001");  // D1: mã nghiệp vụ
            assertThat(claims.get("schoolId") == null || claims.get("schoolId").isNull()).isTrue();
        }

        @Test
        @DisplayName("GET /profile/me JWT supplier → chỉ trả hồ sơ chính; không chứa partnerId đối tác khác")
        void supplierSeesOwnProfileOnly() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/profile/me")
                            .header("Authorization", authHeaderFor("U-0006")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            assertThat(body.get("email").asText()).isEqualTo("sonlt@greenfarm.vn");
        }

        @Test
        @DisplayName("Supplier GET /users → 403 (§16: supplier không có quyền xem users)")
        void supplierCannotGetUsers() throws Exception {
            mvc.perform(get("/api/v1/users")
                            .header("Authorization", authHeaderFor("U-0006")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"));
        }

        @Test
        @DisplayName("Supplier GET /staff → 403")
        void supplierCannotGetStaff() throws Exception {
            mvc.perform(get("/api/v1/staff")
                            .header("Authorization", authHeaderFor("U-0006")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-004"));
        }

        @Test
        @DisplayName("U-0020 (cũng SUP-001) login → partnerId=SUP-001 trong JWT")
        void secondSupplierSamePartner() throws Exception {
            JsonNode loginBody = login("khanb@greenfarm.vn", PWD);  // U-0020, SUP-001
            String[] parts = loginBody.get("accessToken").asText().split("\\.");
            JsonNode claims = json.readTree(java.util.Base64.getUrlDecoder().decode(parts[1]));
            assertThat(claims.get("partnerId").asText()).isEqualTo("SUP-001");
        }

        @Test
        @DisplayName("U-0007 (SUP-004, SUSPENDED) login → 403 IAM-003")
        void suspendedSupplierBlocked() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("email", "yendh@daiduong.vn", "password", PWD))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("IAM-003"));
        }
    }
}
