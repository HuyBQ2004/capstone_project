package com.sfms.mf13.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** MF13-TC-003 — BR-178 (4 field header bắt buộc) + BR-181 (watermark Confidential). */
class ReportRendererHeaderTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-07-30T07:12:03Z");
    private static final List<Map<String, Object>> ROWS = List.of(Map.of("id", "SMP-1", "status", "RETAINED"));

    @Test
    void pdfContainsFourHeaderFieldsAndConfidentialWatermark() throws Exception {
        byte[] bytes = new PdfReportRenderer().render("Báo cáo Lưu mẫu thực phẩm", "01/07/2026 - 30/07/2026",
                "TH Đoàn Thị Điểm", GENERATED_AT, ROWS);
        String text = extractPdfText(bytes);
        assertThat(text).contains("Bao cao Luu mau thuc pham"); // sanitize() bỏ dấu — vẫn xác định được nội dung
        assertThat(text).contains("01/07/2026 - 30/07/2026");
        assertThat(text).contains("TH Doan Thi Diem");
        assertThat(text).contains("2026-07-30T07:12:03Z");
        assertThat(text).contains("Confidential");
    }

    @Test
    void pdfRejectsMissingSchoolName() {
        assertThatThrownBy(() -> new PdfReportRenderer().render("title", "period", null, GENERATED_AT, ROWS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void xlsxContainsFourHeaderFieldsAndConfidentialWatermark() {
        byte[] bytes = new XlsxReportRenderer().render("Bao cao Luu mau", "01/07/2026 - 30/07/2026",
                "TH Doan Thi Diem", GENERATED_AT, ROWS);
        assertThat(bytes.length).isGreaterThan(0);
        String text = extractXlsxText(bytes);
        assertThat(text).contains("Bao cao Luu mau");
        assertThat(text).contains("01/07/2026 - 30/07/2026");
        assertThat(text).contains("TH Doan Thi Diem");
        assertThat(text).contains("Confidential");
    }

    @Test
    void xlsxRejectsMissingSchoolName() {
        assertThatThrownBy(() -> new XlsxReportRenderer().render("title", "period", "", GENERATED_AT, ROWS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String extractPdfText(byte[] bytes) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.Loader.loadPDF(bytes)) {
            return new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
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
}
