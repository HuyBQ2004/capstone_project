package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF13-TC-INT-012 (⛔ L5 3 ô Service Caterers, không chặn phần còn lại) — UC-171 + BR-175:
 * {@code GET /api/v1/reports} trả danh mục theo quyền qua HTTP thật (CF-M13-7d — thay 2 dòng
 * hardcode FE).
 *
 * <p>Chú ý D8/S7: {@code ReportController.reports()} khai kiểu trả {@code List<
 * ReportDefinitionResponse>}, KHÔNG phải {@code PagedResponse<T>} — endpoint 7 mục cố định
 * không cần phân trang. Test theo đúng shape thật (mảng JSON trần), không giả định
 * {@code PagedResponse} 5 field như phần "Expected Result" gốc của TestSpec giả định.
 */
class ReportCatalogIT extends AbstractIamIntegrationTest {

    @Test
    void schoolAdminSeesAllSevenIncludingTraceabilityAndSystemUsage() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/reports").header("Authorization", schoolAdmin)).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        var body = parse(result);
        assertThat(body).hasSize(7);
        var slugs = new java.util.ArrayList<String>();
        body.forEach(item -> slugs.add(item.get("slug").asText()));
        assertThat(slugs).contains("traceability", "system-usage");
    }

    @Test
    void systemAdminSeesAllSeven() throws Exception {
        String sysAdmin = authHeaderFor("U-9001");
        MvcResult result = mvc.perform(get("/api/v1/reports").header("Authorization", sysAdmin)).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result)).hasSize(7);
    }

    @Test
    void medicalStaffSeesSixWithExportableFalseEverywhere() throws Exception {
        String medical = authHeaderFor("U-0002");
        MvcResult result = mvc.perform(get("/api/v1/reports").header("Authorization", medical)).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        var body = parse(result);
        assertThat(body).hasSize(6);
        for (var item : body) {
            assertThat(item.get("exportable").asBoolean())
                    .as("medical-staff không được exportable=true ở bất kỳ mục nào").isFalse();
        }
    }

    @Test
    void serviceCaterersAndOtherRolesAreDeniedAtMvp() throws Exception {
        // ⛔ L5 — TDS chưa liệt Service Caterers là Actor của UC-171 → 403, không phải danh mục rỗng.
        String caterer = authHeaderFor("U-0003");
        MvcResult result = mvc.perform(get("/api/v1/reports").header("Authorization", caterer)).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("RPT-005");
    }

    @Test
    void teacherIsDenied() throws Exception {
        String teacher = authHeaderFor("U-0004");
        MvcResult result = mvc.perform(get("/api/v1/reports").header("Authorization", teacher)).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }
}
