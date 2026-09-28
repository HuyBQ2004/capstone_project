package com.sfms.mf08.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.dto.ExportDtos.ExportFormat;
import com.sfms.mf08.dto.ExportDtos.ExportedFile;
import com.sfms.mf08.repository.FoodSampleRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Hiện thực {@link SampleRegisterExportService} — xuất SỔ THEO DÕI LƯU VÀ HỦY MẪU THỨC ĂN LƯU
 * (Mẫu số 5, Phụ lục 2 QĐ 1246/QĐ-BYT), đúng 12 cột, theo bộ lọc đang áp dụng ở {@code GET
 * /samples} (UC-118). File nhị phân THẬT — POI cho XLSX, PDFBox cho PDF (không phải CSV giả lập).
 *
 * <p>CF-M08-9: cột 10 (ghi chú) ghép thêm nhân chứng khi xuất — {@code disposal_witness} vẫn là
 * cột riêng trong DB (truy vấn/thống kê được), bản in chỉ GHÉP để đúng khuôn 12 cột của Mẫu biểu.
 */
@Service
public class SampleRegisterExportServiceImpl implements SampleRegisterExportService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.UTC);
    private static final String[] HEADERS = {
            "STT", "Tên mẫu thức ăn lưu", "Bữa ăn", "Số suất ăn", "Khối lượng/thể tích mẫu lưu",
            "Dụng cụ chứa mẫu lưu", "Nhiệt độ bảo quản (°C)", "Thời gian lấy mẫu", "Thời gian hủy mẫu",
            "Ghi chú", "Người lấy mẫu", "Người hủy mẫu"
    };

    private final FoodSampleRepository foodSampleRepository;
    private final ReferenceResolver referenceResolver;

    public SampleRegisterExportServiceImpl(FoodSampleRepository foodSampleRepository,
                                           ReferenceResolver referenceResolver) {
        this.foodSampleRepository = foodSampleRepository;
        this.referenceResolver = referenceResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportedFile export(ListQuery query, ExportFormat format, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        // Export không phân trang — lấy toàn bộ tập đã lọc (khuyến nghị TDS §4.4: bắt buộc
        // dateFrom/dateTo khi > 1.000 dòng; kiểm ngưỡng đó ở tầng controller/rate-limit).
        List<FoodSample> rows = foodSampleRepository.findAll(FoodSampleSpec.build(query, schoolId, null, null)).stream()
                .sorted(java.util.Comparator.comparing(FoodSample::getCollectedAt))
                .toList();

        return format == ExportFormat.PDF ? exportPdf(rows) : exportXlsx(rows);
    }

    private ExportedFile exportXlsx(List<FoodSample> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Mẫu số 5");
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            int stt = 1;
            for (FoodSample s : rows) {
                Row row = sheet.createRow(rowIndex++);
                writeColumns(row, stt++, s);
            }
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ExportedFile("mau-so-5-luu-mau.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Mẫu số 5 XLSX export", e);
        }
    }

    private void writeColumns(Row row, int stt, FoodSample s) {
        row.createCell(0).setCellValue(stt);
        row.createCell(1).setCellValue(nullToEmpty(s.getDishName()));
        row.createCell(2).setCellValue(mealCell(s));
        row.createCell(3).setCellValue(s.getServings());
        row.createCell(4).setCellValue(amountCell(s));
        row.createCell(5).setCellValue(nullToEmpty(s.getContainer()));
        row.createCell(6).setCellValue(s.getFridgeTemp() == null ? "" : s.getFridgeTemp().toPlainString());
        row.createCell(7).setCellValue(s.getCollectedAt() == null ? "" : TS.format(s.getCollectedAt()));
        row.createCell(8).setCellValue(s.getDisposedAt() == null ? "" : TS.format(s.getDisposedAt()));
        row.createCell(9).setCellValue(noteCell(s));
        row.createCell(10).setCellValue(nullToEmpty(s.getCollectedBy()));
        row.createCell(11).setCellValue(nullToEmpty(s.getDisposedBy()));
    }

    private ExportedFile exportPdf(List<FoodSample> rows) {
        try (PDDocument document = new PDDocument()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float margin = 30f;
            float leading = 12f;
            float fontSize = 7f;

            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            float y = page.getMediaBox().getHeight() - margin;
            y = writeTitle(content, boldFont, margin, y);
            y = writePdfRow(content, font, margin, y, leading, fontSize, HEADERS, true);

            int stt = 1;
            for (FoodSample s : rows) {
                if (y < margin + leading) {
                    content.close();
                    page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - margin;
                    y = writePdfRow(content, font, margin, y, leading, fontSize, HEADERS, true);
                }
                String[] values = {
                        String.valueOf(stt++), nullToEmpty(s.getDishName()), mealCell(s),
                        String.valueOf(s.getServings()), amountCell(s), nullToEmpty(s.getContainer()),
                        s.getFridgeTemp() == null ? "" : s.getFridgeTemp().toPlainString(),
                        s.getCollectedAt() == null ? "" : TS.format(s.getCollectedAt()),
                        s.getDisposedAt() == null ? "" : TS.format(s.getDisposedAt()),
                        noteCell(s), nullToEmpty(s.getCollectedBy()), nullToEmpty(s.getDisposedBy())
                };
                y = writePdfRow(content, font, margin, y, leading, fontSize, values, false);
            }
            content.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new ExportedFile("mau-so-5-luu-mau.pdf", "application/pdf", out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Mẫu số 5 PDF export", e);
        }
    }

    private static float writeTitle(PDPageContentStream content, PDType1Font boldFont, float margin, float y)
            throws IOException {
        content.beginText();
        content.setFont(boldFont, 11f);
        content.newLineAtOffset(margin, y);
        content.showText("SO THEO DOI LUU VA HUY MAU THUC AN LUU (Mau so 5 - QD 1246/QD-BYT)");
        content.endText();
        return y - 20f;
    }

    private static float writePdfRow(PDPageContentStream content, PDType1Font font, float margin, float y,
                                     float leading, float fontSize, String[] values, boolean header)
            throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(margin, y);
        // Bảng đơn giản dạng văn bản có phân cách " | " — đủ để xuất tất cả 12 cột trên khổ A4
        // ngang, đọc được trên mọi trình xem PDF (đơn giản hoá có chủ đích, không kẻ ô lưới).
        content.showText(String.join(" | ", values));
        content.endText();
        return y - leading;
    }

    private static String mealCell(FoodSample s) {
        String type = s.getMealType() == null ? "" : s.getMealType().name();
        String shift = nullToEmpty(s.getMealShift());
        return (type + " " + shift).trim();
    }

    private static String amountCell(FoodSample s) {
        if (s.getSampleAmount() == null || s.getSampleUnit() == null) {
            return "";
        }
        return s.getSampleAmount().stripTrailingZeros().toPlainString() + " "
                + (s.getSampleUnit().name().equals("G") ? "g" : "ml");
    }

    /** CF-M08-9 — ghép nhân chứng vào ghi chú CHỈ ở bản in, KHÔNG đổi cột DB. */
    private static String noteCell(FoodSample s) {
        String note = nullToEmpty(s.getDisposalNote());
        if (s.getDisposalWitness() != null && !s.getDisposalWitness().isBlank()) {
            String suffix = "nhân chứng: " + s.getDisposalWitness();
            note = note.isBlank() ? suffix : note + " — " + suffix;
        }
        return note;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private UUID resolveSchoolId(AuthUser actor) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId()).orElse(null);
        }
        return null;
    }
}
