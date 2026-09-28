package com.sfms.mf13.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** BR-178 (PDF) + BR-181 (watermark Confidential). PDFBox — ADR-000 ("PDF: khi có nhu cầu thật"). */
@Component("pdf")
public class PdfReportRenderer implements ReportRenderer {

    @Override
    public byte[] render(String title, String periodLabel, String schoolName, Instant generatedAt,
                         List<Map<String, Object>> rows) {
        if (title == null || title.isBlank() || periodLabel == null || schoolName == null
                || schoolName.isBlank() || generatedAt == null) {
            // BR-178 — 4 field header bắt buộc; thiếu thì KHÔNG render (fail-closed).
            throw new IllegalArgumentException("BR-178: title/periodLabel/schoolName/generatedAt are required");
        }
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50f;
                float y = page.getMediaBox().getHeight() - margin;
                y = writeLine(content, bold, 14f, margin, y, title);
                y = writeLine(content, regular, 10f, margin, y, "Khoang thoi gian: " + periodLabel);
                y = writeLine(content, regular, 10f, margin, y, "Truong: " + schoolName);
                y = writeLine(content, regular, 10f, margin, y, "Xuat luc: " + generatedAt);
                y = writeLine(content, bold, 10f, margin, y, "*** Confidential ***");
                y = writeLine(content, regular, 8f, margin, y,
                        "Aggregated data - no personally identifiable student information (BR-181)");
                y -= 10f;
                for (Map<String, Object> row : rows) {
                    if (y < margin) {
                        break; // đơn giản hoá có chủ đích — 1 trang cho MVP
                    }
                    StringBuilder line = new StringBuilder();
                    row.forEach((k, v) -> line.append(k).append('=').append(v).append("  "));
                    y = writeLine(content, regular, 8f, margin, y, line.toString());
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build report PDF", e);
        }
    }

    @Override
    public String contentType() {
        return "application/pdf";
    }

    @Override
    public String fileExtension() {
        return "pdf";
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
}
