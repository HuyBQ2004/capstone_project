package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.security.MessageDigest;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF13-TC-INT-006/007 — BR-177/178/181/182: xuất → tải, checksum khớp, audit ghi, kỳ rỗng
 * KHÔNG chặn (CF-M13-4/M13-D10).
 */
class ReportExportFlowIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_A = "00000000-0000-0000-0000-0000000a0042";

    @Test
    @DisplayName("MF13-TC-INT-007 — xuất rồi tải: 200 + application/pdf, SHA-256 tính lại khớp "
            + "checksum_sha256 đã lưu")
    void exportThenDownloadRoundTripsWithMatchingChecksum() throws Exception {
        insertKitchenInspection("KIN-2026-EXP01", 85, "PASSED");
        String schoolAdmin = authHeaderFor("U-0001");

        MvcResult exportResult = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("format", "PDF", "dateFrom", "2026-07-01", "dateTo", "2026-07-30"))))
                .andReturn();
        assertThat(exportResult.getResponse().getStatus()).isEqualTo(201);
        var body = parse(exportResult);
        String code = body.get("id").asText();
        assertThat(code).matches("^RPX-\\d{4}-\\d{3}$");
        assertThat(body.get("rowCount").asInt()).isEqualTo(1);
        assertThat(body.get("confidential").asBoolean()).isTrue();

        assertThat(count("SELECT count(*) FROM report_exports WHERE code = ?", code)).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM audit_logs WHERE action = 'EXPORT_REPORT'")).isEqualTo(1);

        MvcResult download = mvc.perform(get("/api/v1/report-exports/" + code + "/download")
                        .header("Authorization", schoolAdmin))
                .andReturn();
        assertThat(download.getResponse().getStatus()).isEqualTo(200);
        assertThat(download.getResponse().getContentType()).isEqualTo("application/pdf");
        byte[] fileBytes = download.getResponse().getContentAsByteArray();
        assertThat(fileBytes.length).isGreaterThan(0);

        String checksum = sha256Hex(fileBytes);
        String storedChecksum = jdbc.queryForObject(
                "SELECT checksum_sha256 FROM report_exports WHERE code = ?", String.class, code);
        assertThat(checksum).isEqualToIgnoringCase(storedChecksum);
    }

    @Test
    @DisplayName("MF13-TC-INT-004 — kỳ rỗng (0 bản ghi) vẫn 201 Created với rowCount = 0 (BR-178)")
    void emptyPeriodExportIsNotBlocked() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(post("/api/v1/reports/three-step/export")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("format", "PDF", "dateFrom", "2020-01-01", "dateTo", "2020-01-02"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(parse(result).get("rowCount").asInt()).isZero();
    }

    @Test
    void medicalStaffCanViewButCannotExport() throws Exception {
        String medical = authHeaderFor("U-0002");
        MvcResult result = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("format", "PDF", "dateFrom", "2026-07-01", "dateTo", "2026-07-30"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("RPT-005");
    }

    @Test
    @DisplayName("MF13-TC-SEC-003 — School Admin trường A tải bản xuất của trường B ⇒ 404 RPT-404/"
            + "MSG-137, không lộ file")
    void crossSchoolDownloadReturns404NotFound() throws Exception {
        insertKitchenInspection("KIN-2026-B01", 90, "PASSED");
        String schoolAdminB = authHeaderFor("U-0018"); // SCH-HN-0051
        MvcResult exportResult = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", schoolAdminB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("format", "PDF", "dateFrom", "2026-07-01", "dateTo", "2026-07-30"))))
                .andReturn();
        // U-0018 has no kitchen_inspections row of its own -> rowCount 0, still succeeds (M13-D10)
        assertThat(exportResult.getResponse().getStatus()).isEqualTo(201);
        String code = parse(exportResult).get("id").asText();

        String schoolAdminA = authHeaderFor("U-0001"); // SCH-HN-0042
        MvcResult download = mvc.perform(get("/api/v1/report-exports/" + code + "/download")
                        .header("Authorization", schoolAdminA))
                .andReturn();
        assertThat(download.getResponse().getStatus()).isEqualTo(404);
        assertThat(parse(download).at("/error/code").asText()).isEqualTo("RPT-404");
    }

    private void insertKitchenInspection(String code, int score, String result) {
        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id,
                    result, confirm_status)
                VALUES (gen_random_uuid(), ?, ?::uuid, DATE '2026-07-20', ?,
                    (SELECT id FROM user_accounts WHERE code = 'U-0002'), ?, 'CONFIRMED')
                """, code, SCHOOL_A, score, result);
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        byte[] raw = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
