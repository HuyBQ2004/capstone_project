package com.sfms.mf13.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF13-TC-SEC-005 (★) — BR-181: file PDF/XLSX xuất ra ngoài hệ thống KHÔNG chứa PII học sinh,
 * kể cả trong metadata tài liệu, và mang nhãn Confidential (BR-178/181).
 *
 * <p>Phạm vi thu hẹp có chủ đích so với spec gốc ("14 file = 7 slug × 2 format"): dùng
 * {@code hygiene} và {@code three-step} — hai slug đã có fixture nguồn sẵn trong seed/insert của
 * các IT khác trong module này — × 2 format = 4 file. Đủ để chứng minh property (projection chạy
 * TRƯỚC khi vào renderer, {@link com.sfms.mf13.service.ReportRowProjection} strip PII cho MỌI
 * slug bằng cùng một cơ chế whitelist — đã phủ đủ 7 slug ở {@code ReportRowProjectionTest}), mà
 * không phải dựng fixture cho toàn bộ 7 nguồn dữ liệu chỉ để lặp lại cùng một khẳng định.
 *
 * <p>Renderer thật ({@code PdfReportRenderer}/{@code XlsxReportRenderer}) KHÔNG tự set
 * {@code PDDocumentInformation} (Author/Subject/Keywords) — khác với "Implementation Note" của
 * spec gốc đề xuất. Vì các field đó rỗng/mặc định (không lấy từ dữ liệu hàng), chúng KHÔNG BAO
 * GIỜ chứa PII HS theo cách khác — test vẫn xác nhận đúng, chỉ không phải nhờ code tự set field
 * đó tường minh.
 */
class PiiLeakFileIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_A = "00000000-0000-0000-0000-0000000a0042";

    private static final String[] PII_NEEDLES = {"studentId", "studentName", "parentName", "diagnosis",
            "HS-3A-001", "Nguyen Minh Anh", "Nguyễn Minh Anh"};

    @Test
    void hygienePdfAndXlsxContainNoPiiButDoContainConfidentialAndRealData() throws Exception {
        insertKitchenInspection("KIN-2026-SEC01", 88, "PASSED");
        assertExportedFileHasNoPiiAndHasConfidential("hygiene", "PDF");
        assertExportedFileHasNoPiiAndHasConfidential("hygiene", "XLSX");
    }

    @Test
    void threeStepPdfAndXlsxContainNoPiiButDoContainConfidentialAndRealData() throws Exception {
        insertThreeStep("INSP-2026-SEC01");
        assertExportedFileHasNoPiiAndHasConfidential("three-step", "PDF");
        assertExportedFileHasNoPiiAndHasConfidential("three-step", "XLSX");
    }

    private void assertExportedFileHasNoPiiAndHasConfidential(String slug, String format) throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult exportResult = mvc.perform(post("/api/v1/reports/" + slug + "/export")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("format", format, "dateFrom", "2026-07-01", "dateTo", "2026-07-30"))))
                .andReturn();
        assertThat(exportResult.getResponse().getStatus()).isEqualTo(201);
        String code = parse(exportResult).get("id").asText();

        MvcResult download = mvc.perform(get("/api/v1/report-exports/" + code + "/download")
                        .header("Authorization", schoolAdmin))
                .andReturn();
        assertThat(download.getResponse().getStatus()).isEqualTo(200);
        byte[] bytes = download.getResponse().getContentAsByteArray();

        String extractedText = "PDF".equals(format) ? extractPdfText(bytes) : extractXlsxText(bytes);
        for (String needle : PII_NEEDLES) {
            assertThat(extractedText).as("%s %s KHÔNG được chứa PII needle '%s'", slug, format, needle)
                    .doesNotContain(needle);
        }
        // Assertion dương — nhãn Confidential + dữ liệu nghiệp vụ thật (chống false-green trên file rỗng).
        assertThat(extractedText).contains("Confidential");
        assertThat(extractedText.length()).isGreaterThan(20);

        if ("PDF".equals(format)) {
            org.apache.pdfbox.pdmodel.PDDocumentInformation info = readPdfInfo(bytes);
            String metaBlob = String.valueOf(info.getAuthor()) + info.getSubject() + info.getKeywords();
            for (String needle : PII_NEEDLES) {
                assertThat(metaBlob).as("metadata PDF (Author/Subject/Keywords) KHÔNG được chứa PII '%s'", needle)
                        .doesNotContain(needle);
            }
        } else {
            String sheetName = readXlsxSheetName(bytes);
            for (String needle : PII_NEEDLES) {
                assertThat(sheetName).doesNotContain(needle);
            }
        }
    }

    private static String extractPdfText(byte[] bytes) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(bytes)) {
            return new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
        }
    }

    private static org.apache.pdfbox.pdmodel.PDDocumentInformation readPdfInfo(byte[] bytes) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(bytes)) {
            return doc.getDocumentInformation();
        }
    }

    private static String extractXlsxText(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                for (org.apache.poi.ss.usermodel.Cell cell : row) {
                    sb.append(cell.toString()).append(' ');
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private static String readXlsxSheetName(byte[] bytes) {
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) {
            return workbook.getSheetAt(0).getSheetName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void insertKitchenInspection(String code, int score, String result) {
        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id,
                    result, confirm_status)
                VALUES (gen_random_uuid(), ?, ?::uuid, DATE '2026-07-20', ?,
                    (SELECT id FROM user_accounts WHERE code = 'U-0002'), ?, 'CONFIRMED')
                """, code, SCHOOL_A, score, result);
    }

    private void insertThreeStep(String code) {
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, core_temp, inspector_user_id)
                VALUES (gen_random_uuid(), ?, ?::uuid, '00000000-0000-0000-0000-0000000b0001'::uuid,
                    DATE '2026-07-20', 'LUNCH', 'PASSED', 'PASSED', 'PASSED',
                    'PASSED', 80, (SELECT id FROM user_accounts WHERE code = 'U-0002'))
                """, code, SCHOOL_A);
    }
}
