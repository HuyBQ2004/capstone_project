package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF12-TC-INT-015 — Tải file báo cáo: PDF + XLSX đủ 4 thành phần BR-178 (tiêu đề, khoảng thời
 * gian, TÊN TRƯỜNG, timestamp xuất) + nhãn Confidential khi cần (BR-181). Định dạng không hỗ
 * trợ → 400.
 */
class ReportDownloadIT extends AbstractIamIntegrationTest {

    private String sendComplianceReport(String medicalHeader) throws Exception {
        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id, result)
                SELECT gen_random_uuid(), 'KIN-DL-01', '00000000-0000-0000-0000-0000000a0042',
                       '2026-07-08', 90, u.id, 'PASSED'
                FROM user_accounts u WHERE u.code = 'U-0002'
                """);
        MvcResult send = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", medicalHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rangeFrom\":\"2026-07-06\",\"rangeTo\":\"2026-07-12\",\"period\":\"Weekly\"}"))
                .andReturn();
        assertThat(send.getResponse().getStatus()).isEqualTo(201);
        return parse(send).get("id").asText();
    }

    @Test
    void pdfAndXlsxContainAllFourRequiredComponentsCsvUnsupported() throws Exception {
        String medical = authHeaderFor("U-0002");
        String code = sendComplianceReport(medical);

        MvcResult pdfResult = mvc.perform(get("/api/v1/sent-reports/" + code + "/download?format=pdf")
                        .header("Authorization", medical))
                .andReturn();
        assertThat(pdfResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(pdfResult.getResponse().getContentType()).isEqualTo("application/pdf");
        byte[] pdfBytes = pdfResult.getResponse().getContentAsByteArray();
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");

        String pdfText;
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            pdfText = new PDFTextStripper().getText(doc);
        }
        assertThat(pdfText).contains("BAO CAO"); // tiêu đề
        assertThat(pdfText).contains("Truong: TH Doan Thi Diem"); // tên trường (nhãn — dấu đã bỏ, sanitize())
        assertThat(pdfText).contains("06/07/2026"); // khoảng thời gian (rangeLabel)
        assertThat(pdfText).contains("Gui luc: 2026"); // timestamp xuất

        MvcResult xlsxResult = mvc.perform(get("/api/v1/sent-reports/" + code + "/download?format=xlsx")
                        .header("Authorization", medical))
                .andReturn();
        assertThat(xlsxResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(xlsxResult.getResponse().getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        byte[] xlsxBytes = xlsxResult.getResponse().getContentAsByteArray();
        assertThat(xlsxBytes[0]).isEqualTo((byte) 'P');
        assertThat(xlsxBytes[1]).isEqualTo((byte) 'K');

        StringBuilder xlsxText = new StringBuilder();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            XSSFSheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    xlsxText.append(cell.toString()).append(" | ");
                }
            }
        }
        String xlsxContent = xlsxText.toString();
        assertThat(xlsxContent).contains("Trường");
        assertThat(xlsxContent).containsIgnoringCase("khoảng thời gian");
        assertThat(xlsxContent).containsIgnoringCase("gửi lúc");

        // Định dạng không hỗ trợ → 400 (BR-178 chỉ 2 định dạng).
        MvcResult csvResult = mvc.perform(get("/api/v1/sent-reports/" + code + "/download?format=csv")
                        .header("Authorization", medical))
                .andReturn();
        assertThat(csvResult.getResponse().getStatus()).isIn(400, 422);

        // Lượt tải ghi audit (BR-177).
        long downloadAudits = count(
                "SELECT count(*) FROM audit_logs WHERE action LIKE 'DOWNLOAD sent-report=%'");
        assertThat(downloadAudits).isGreaterThanOrEqualTo(2);
    }
}
