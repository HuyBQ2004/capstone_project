package com.sfms.mf13.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** MF13-TC-SEC-001 (★) — §16 Authorization Matrix, gọi API trực tiếp bỏ qua UI. */
class AuthorizationMatrixIT extends AbstractIamIntegrationTest {

    @ParameterizedTest(name = "{0} {1} {2} -> {3}")
    @CsvSource({
            "U-9001, GET,  /api/v1/compliance-report?allSchools=true, 200",
            "U-0001, GET,  /api/v1/compliance-report,                 200",
            "U-0001, GET,  /api/v1/compliance-report?allSchools=true, 403",
            "U-0002, GET,  /api/v1/reports/hygiene/rows,               200",
            "U-0002, GET,  /api/v1/reports/system-usage/rows,          403",
            "U-0003, GET,  /api/v1/reports/receiving/rows,             403",
            "U-0004, GET,  /api/v1/compliance-report,                 403",
            "U-0005, GET,  /api/v1/reports,                            403",
            "U-0006, GET,  /api/v1/report-exports,                     403"
    })
    void endpointRespectsAuthorizationMatrix(String userCode, String method, String path, int expectedStatus)
            throws Exception {
        String auth = authHeaderFor(userCode);
        MockHttpServletRequestBuilder builder = "GET".equals(method) ? get(path) : post(path);
        MvcResult result = mvc.perform(builder.header("Authorization", auth)).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
    }

    @org.junit.jupiter.api.Test
    void missingJwtIsRejectedWith401() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/compliance-report")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @org.junit.jupiter.api.Test
    void medicalStaffCannotExport() throws Exception {
        String medical = authHeaderFor("U-0002");
        MvcResult result = mvc.perform(post("/api/v1/reports/hygiene/export").header("Authorization", medical)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"format\":\"PDF\",\"dateFrom\":\"2026-07-01\",\"dateTo\":\"2026-07-30\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }
}
