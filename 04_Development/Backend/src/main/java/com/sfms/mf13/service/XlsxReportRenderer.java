package com.sfms.mf13.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** BR-178 (XLSX) + BR-181 (watermark Confidential). Apache POI. */
@Component("xlsx")
public class XlsxReportRenderer implements ReportRenderer {

    @Override
    public byte[] render(String title, String periodLabel, String schoolName, Instant generatedAt,
                         List<Map<String, Object>> rows) {
        if (title == null || title.isBlank() || periodLabel == null || schoolName == null
                || schoolName.isBlank() || generatedAt == null) {
            throw new IllegalArgumentException("BR-178: title/periodLabel/schoolName/generatedAt are required");
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Report");
            int r = 0;
            writeRow(sheet, r++, title, "");
            writeRow(sheet, r++, "Khoang thoi gian", periodLabel);
            writeRow(sheet, r++, "Truong", schoolName);
            writeRow(sheet, r++, "Xuat luc", String.valueOf(generatedAt));
            writeRow(sheet, r++, "Confidential", "Aggregated data - no PII (BR-181)");
            r++;

            List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());
            if (!columns.isEmpty()) {
                Row header = sheet.createRow(r++);
                for (int c = 0; c < columns.size(); c++) {
                    header.createCell(c).setCellValue(columns.get(c));
                }
                for (Map<String, Object> row : rows) {
                    Row dataRow = sheet.createRow(r++);
                    for (int c = 0; c < columns.size(); c++) {
                        Object value = row.get(columns.get(c));
                        dataRow.createCell(c).setCellValue(value == null ? "" : String.valueOf(value));
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build report XLSX", e);
        }
    }

    @Override
    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String fileExtension() {
        return "xlsx";
    }

    private static void writeRow(XSSFSheet sheet, int index, String label, String value) {
        Row row = sheet.createRow(index);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
}
