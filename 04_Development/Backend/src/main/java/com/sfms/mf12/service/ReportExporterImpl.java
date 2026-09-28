package com.sfms.mf12.service;

import com.sfms.mf12.domain.SentReport;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * BR-178 — mỗi file gồm: tiêu đề, khoảng thời gian, tên trường, timestamp xuất (BR-181 đánh
 * dấu Confidential). Nội dung đặc thù từng loại đọc từ {@code payload} (ADR-M12-05, snapshot
 * nguyên văn lúc gửi — KHÔNG tính lại).
 */
@Service
public class ReportExporterImpl implements ReportExporter {

    @Override
    public ExportedFile renderPdf(SentReport report, String schoolName) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50f;
                float y = page.getMediaBox().getHeight() - margin;
                y = writeLine(content, bold, 14f, margin, y, title(report));
                y = writeLine(content, regular, 10f, margin, y, "Truong: " + sanitize(schoolName));
                y = writeLine(content, regular, 10f, margin, y, "Khoảng thời gian: " + report.getRangeLabel());
                y = writeLine(content, regular, 10f, margin, y, "Gửi lúc: " + report.getSentAt());
                if (report.isConfidential()) {
                    y = writeLine(content, bold, 10f, margin, y, "*** CONFIDENTIAL ***");
                }
                y -= 10f;
                for (Map.Entry<String, Object> entry : report.getPayload().entrySet()) {
                    y = writeLine(content, regular, 9f, margin, y, entry.getKey() + ": " + entry.getValue());
                    if (y < margin) {
                        break; // đơn giản hoá có chủ đích — 1 trang, đủ cho snapshot MF12
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new ExportedFile(report.getCode() + ".pdf", "application/pdf", out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build sent-report PDF", e);
        }
    }

    @Override
    public ExportedFile renderXlsx(SentReport report, String schoolName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(report.getCode());
            int r = 0;
            writeRow(sheet, r++, title(report), "");
            writeRow(sheet, r++, "Trường", schoolName);
            writeRow(sheet, r++, "Khoảng thời gian", report.getRangeLabel());
            writeRow(sheet, r++, "Gửi lúc", String.valueOf(report.getSentAt()));
            writeRow(sheet, r++, "Confidential", String.valueOf(report.isConfidential()));
            r++;
            for (Map.Entry<String, Object> entry : report.getPayload().entrySet()) {
                writeRow(sheet, r++, entry.getKey(), String.valueOf(entry.getValue()));
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ExportedFile(report.getCode() + ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build sent-report XLSX", e);
        }
    }

    private static void writeRow(XSSFSheet sheet, int index, String label, String value) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private static float writeLine(PDPageContentStream content, PDType1Font font, float size, float margin,
                                   float y, String text) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(margin, y);
        content.showText(sanitize(text));
        content.endText();
        return y - (size + 4f);
    }

    /** WinAnsiEncoding của Standard14Fonts không có dấu tiếng Việt — bỏ dấu cho an toàn hiển thị. */
    private static String sanitize(String text) {
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
    }

    private static String title(SentReport report) {
        return switch (report.getReportType()) {
            case COMPLIANCE -> "BAO CAO TUAN THU ATTP";
            case VIOLATION_SUMMARY -> "BAO CAO TONG HOP VI PHAM";
            case INCIDENT_SUMMARY -> "BAO CAO TONG HOP SU CO";
            case SYSTEM_HEALTH -> "BAO CAO TINH TRANG HE THONG";
        };
    }
}
