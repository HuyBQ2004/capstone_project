package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * UC-185 (BR-209) trọn vòng: gửi báo cáo tuân thủ → xuất hiện ở {@code GET /sent-reports} →
 * tải file PDF/XLSX thật. Đồng thời chứng minh {@code AIS-092} khi kỳ rỗng (M12-D06).
 */
class SentReportFlowIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_ID = "00000000-0000-0000-0000-0000000a0042";

    @Test
    void sendComplianceReportListsAndDownloads() throws Exception {
        String medical = authHeaderFor("U-0002");

        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id,
                    result, confirm_status)
                VALUES (gen_random_uuid(), 'KIN-2026-TEST01', ?::uuid, DATE '2026-07-20', 85,
                    (SELECT id FROM user_accounts WHERE code = 'U-0002'), 'PASSED', 'CONFIRMED')
                """, SCHOOL_ID);

        MvcResult sent = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("rangeFrom", "2026-07-14", "rangeTo", "2026-07-20",
                                "period", "Weekly"))))
                .andReturn();
        assertThat(sent.getResponse().getStatus()).isEqualTo(201);
        String reportCode = parse(sent).get("id").asText();
        assertThat(parse(sent).get("payload").get("totalInspections").asInt()).isEqualTo(1);

        MvcResult list = mvc.perform(get("/api/v1/sent-reports").header("Authorization", medical)).andReturn();
        assertThat(list.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(list).get("items").toString()).contains(reportCode);

        MvcResult pdf = mvc.perform(get("/api/v1/sent-reports/" + reportCode + "/download?format=pdf")
                        .header("Authorization", medical))
                .andReturn();
        assertThat(pdf.getResponse().getStatus()).isEqualTo(200);
        assertThat(pdf.getResponse().getContentAsByteArray().length).isGreaterThan(0);
        assertThat(pdf.getResponse().getContentType()).isEqualTo("application/pdf");
    }

    @Test
    void sendComplianceReportWithEmptyPeriodIsBlocked() throws Exception {
        String medical = authHeaderFor("U-0002");

        MvcResult result = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("rangeFrom", "2020-01-01", "rangeTo", "2020-01-07"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("AIS-092");
        assertThat(count("SELECT count(*) FROM sent_reports")).isZero();
    }

    @Test
    void deprecatedIncidentReportEndpointAlwaysReturns409() throws Exception {
        String medical = authHeaderFor("U-0002");
        MvcResult result = mvc.perform(post("/api/v1/sent-reports/incidents")
                        .header("Authorization", medical))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("AIS-188");
    }
}
