package com.sfms.mf06.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.ReceivingLine;
import com.sfms.mf06.domain.ReceivingSession;
import com.sfms.mf06.domain.SupplierProduct;
import com.sfms.mf06.dto.ReceivingExportDtos.ExportFormat;
import com.sfms.mf06.dto.ReceivingExportDtos.ExportedFile;
import com.sfms.mf06.repository.ReceivingSessionRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import jakarta.persistence.criteria.Predicate;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Xuất báo cáo tab "Lịch sử &amp; báo cáo" (trang receiving) — quyết định của Nam, 10/08/2026:
 * nút "Xuất báo cáo" trước đó chỉ toast, không gọi gì thật (không có endpoint nào phía mf06).
 * Mỗi DÒNG là một dòng nguyên liệu (`ReceivingLine`) của một phiên nhận — chi tiết hơn 1
 * dòng/phiên để còn truy vết được từng lô/nguyên liệu (BR-151), cùng khuôn với export Mẫu số 5
 * (MF08, {@code SampleRegisterExportServiceImpl}) mà server này đã dùng ổn định.
 */
@Service
public class ReceivingReportExportServiceImpl implements ReceivingReportExportService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.UTC);
    private static final String[] HEADERS = {
            "STT", "Mã phiên nhận", "Nhà cung cấp", "Thời gian nhận", "Người nhận", "Nguyên liệu",
            "Đơn vị", "Khu vực lưu", "Số lượng nhận", "Đơn giá", "Thành tiền", "Kết quả dòng",
            "Lý do từ chối", "GPS xác thực"
    };
    /**
     * PDFBox `Standard14Fonts` (WinAnsiEncoding) không mã hoá được dấu tiếng Việt — dùng
     * `HEADERS` (có dấu) cho tiêu đề PDF sẽ ném lỗi ngay khi `showText()`. Bản ASCII riêng
     * CHỈ cho PDF; XLSX (POI, Unicode đầy đủ) vẫn dùng `HEADERS` có dấu như bình thường.
     */
    private static final String[] HEADERS_PDF = {
            "STT", "Ma phien nhan", "Nha cung cap", "Thoi gian nhan", "Nguoi nhan", "Nguyen lieu",
            "Don vi", "Khu vuc luu", "So luong nhan", "Don gia", "Thanh tien", "Ket qua dong",
            "Ly do tu choi", "GPS xac thuc"
    };

    private final ReceivingSessionRepository sessionRepository;
    private final SupplierBatchRepository supplierBatchRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ReferenceResolver referenceResolver;
    private final Clock clock;

    public ReceivingReportExportServiceImpl(ReceivingSessionRepository sessionRepository,
                                             SupplierBatchRepository supplierBatchRepository,
                                             SupplierProductRepository supplierProductRepository,
                                             ReferenceResolver referenceResolver, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.supplierBatchRepository = supplierBatchRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.referenceResolver = referenceResolver;
        this.clock = clock;
    }

    private record ExportRow(String sessionCode, String supplier, java.time.Instant receivedAt, String receivedBy,
                              String ingredient, String unit, String storageZone, String receivedQuantity,
                              java.math.BigDecimal unitPrice, java.math.BigDecimal lineTotal, boolean rejected,
                              String rejectReason, boolean gpsVerified) {}

    @Override
    @Transactional(readOnly = true)
    public ExportedFile export(ListQuery query, ExportFormat format, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        Specification<ReceivingSession> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("receivedAt"),
                        query.dateFrom().atStartOfDay(clock.getZone()).toInstant()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThan(root.get("receivedAt"),
                        query.dateTo().plusDays(1).atStartOfDay(clock.getZone()).toInstant()));
            }
            if (query.hasSearch()) {
                predicates.add(com.sfms.common.query.SpecSupport.searchPredicate(root, cb, query.search(),
                        List.of("code")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<ReceivingSession> sessions = sessionRepository.findAll(spec).stream()
                .sorted(java.util.Comparator.comparing(ReceivingSession::getReceivedAt))
                .toList();

        List<ExportRow> rows = new ArrayList<>();
        for (ReceivingSession s : sessions) {
            String supplier = referenceResolver.partnerName(s.getSupplierId());
            var user = referenceResolver.userById(s.getReceivedByUserId());
            String receivedBy = user == null ? null : user[2];
            for (ReceivingLine l : s.getLines()) {
                /*
                 * 11/08/2026 — thêm đơn vị + đơn giá + thành tiền (yêu cầu Nam): trước đây báo
                 * cáo chỉ có số lượng thô, không tra được giá trị mỗi dòng nhận — tra qua
                 * `supplierBatchId` → `SupplierBatch.productId` → `SupplierProduct` (đơn giá
                 * hợp đồng, cùng nguồn với `PurchaseItem.unitPrice` lúc tạo đơn).
                 */
                SupplierProduct product = l.getSupplierBatchId() == null ? null
                        : supplierBatchRepository.findById(l.getSupplierBatchId())
                                .flatMap(b -> supplierProductRepository.findById(b.getProductId()))
                                .orElse(null);
                String unit = product == null ? "" : product.getUnit();
                java.math.BigDecimal unitPrice = product == null ? null : product.getUnitPrice();
                java.math.BigDecimal lineTotal = !l.isRejected() && unitPrice != null && l.getReceivedQuantity() != null
                        ? unitPrice.multiply(l.getReceivedQuantity())
                        : null;
                rows.add(new ExportRow(s.getCode(), supplier, s.getReceivedAt(), receivedBy, l.getIngredientLabel(),
                        unit, l.getStorageZone() == null ? "" : l.getStorageZone().name(),
                        l.isRejected() || l.getReceivedQuantity() == null ? "" : l.getReceivedQuantity().stripTrailingZeros().toPlainString(),
                        unitPrice, lineTotal,
                        l.isRejected(), l.getRejectReason(), s.isGpsVerified()));
            }
        }

        // 11/08/2026 — ghi rõ khoảng thời gian đã lọc lên đầu file: trước đây file xuất ra
        // không có dòng nào nói cho người đọc biết đây là "toàn bộ sổ" hay chỉ một khoảng
        // ngày, dễ hiểu nhầm là báo cáo đầy đủ.
        String rangeLabel = rangeLabelOf(query);
        return format == ExportFormat.PDF ? exportPdf(rows, rangeLabel) : exportXlsx(rows, rangeLabel);
    }

    private static final DateTimeFormatter DATE_VN = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static String rangeLabelOf(ListQuery query) {
        if (query.dateFrom() == null && query.dateTo() == null) {
            return "Toàn bộ sổ";
        }
        String from = query.dateFrom() == null ? "đầu sổ" : DATE_VN.format(query.dateFrom());
        String to = query.dateTo() == null ? "nay" : DATE_VN.format(query.dateTo());
        return "Từ " + from + " đến " + to;
    }

    private ExportedFile exportXlsx(List<ExportRow> rows, String rangeLabel) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Lịch sử nhận hàng");
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            Row rangeRow = sheet.createRow(0);
            Cell rangeCell = rangeRow.createCell(0);
            rangeCell.setCellValue("Khoảng thời gian: " + rangeLabel);
            rangeCell.setCellStyle(headerStyle);

            Row header = sheet.createRow(1);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 2;
            int stt = 1;
            for (ExportRow r : rows) {
                Row row = sheet.createRow(rowIndex++);
                writeColumns(row, stt++, r);
            }
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ExportedFile("lich-su-nhan-hang.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build receiving history XLSX export", e);
        }
    }

    private void writeColumns(Row row, int stt, ExportRow r) {
        row.createCell(0).setCellValue(stt);
        row.createCell(1).setCellValue(nullToEmpty(r.sessionCode()));
        row.createCell(2).setCellValue(nullToEmpty(r.supplier()));
        row.createCell(3).setCellValue(r.receivedAt() == null ? "" : TS.format(r.receivedAt()));
        row.createCell(4).setCellValue(nullToEmpty(r.receivedBy()));
        row.createCell(5).setCellValue(nullToEmpty(r.ingredient()));
        row.createCell(6).setCellValue(nullToEmpty(r.unit()));
        row.createCell(7).setCellValue(nullToEmpty(r.storageZone()));
        row.createCell(8).setCellValue(nullToEmpty(r.receivedQuantity()));
        row.createCell(9).setCellValue(r.unitPrice() == null ? "" : r.unitPrice().stripTrailingZeros().toPlainString());
        row.createCell(10).setCellValue(r.lineTotal() == null ? "" : r.lineTotal().stripTrailingZeros().toPlainString());
        row.createCell(11).setCellValue(r.rejected() ? "Từ chối" : "Đạt");
        row.createCell(12).setCellValue(nullToEmpty(r.rejectReason()));
        row.createCell(13).setCellValue(r.gpsVerified() ? "Đã xác thực" : "Chưa xác thực");
    }

    private ExportedFile exportPdf(List<ExportRow> rows, String rangeLabel) {
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
            y = writeTitle(content, boldFont, margin, y, rangeLabel);
            y = writePdfRow(content, font, margin, y, leading, fontSize, HEADERS_PDF);

            int stt = 1;
            for (ExportRow r : rows) {
                if (y < margin + leading) {
                    content.close();
                    page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - margin;
                    y = writePdfRow(content, font, margin, y, leading, fontSize, HEADERS_PDF);
                }
                // `stripDiacritics()` — PDFBox Standard14Fonts (WinAnsiEncoding) không mã hoá
                // được dấu tiếng Việt; tên nguyên liệu/NCC/người nhận là dữ liệu ĐỘNG (không
                // kiểm soát được như tiêu đề tĩnh), giữ nguyên sẽ ném lỗi khi `showText()`.
                String[] values = {
                        String.valueOf(stt++), stripDiacritics(r.sessionCode()), stripDiacritics(r.supplier()),
                        r.receivedAt() == null ? "" : TS.format(r.receivedAt()), stripDiacritics(r.receivedBy()),
                        stripDiacritics(r.ingredient()), nullToEmpty(r.unit()), nullToEmpty(r.storageZone()),
                        nullToEmpty(r.receivedQuantity()),
                        r.unitPrice() == null ? "" : r.unitPrice().stripTrailingZeros().toPlainString(),
                        r.lineTotal() == null ? "" : r.lineTotal().stripTrailingZeros().toPlainString(),
                        r.rejected() ? "Tu choi" : "Dat", stripDiacritics(r.rejectReason()),
                        r.gpsVerified() ? "Da xac thuc" : "Chua xac thuc"
                };
                y = writePdfRow(content, font, margin, y, leading, fontSize, values);
            }
            content.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new ExportedFile("lich-su-nhan-hang.pdf", "application/pdf", out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build receiving history PDF export", e);
        }
    }

    private static float writeTitle(PDPageContentStream content, PDType1Font boldFont, float margin, float y,
                                     String rangeLabel) throws IOException {
        content.beginText();
        content.setFont(boldFont, 11f);
        content.newLineAtOffset(margin, y);
        content.showText("BAO CAO LICH SU NHAN HANG (BR-151) - " + stripDiacritics(rangeLabel));
        content.endText();
        return y - 20f;
    }

    private static float writePdfRow(PDPageContentStream content, PDType1Font font, float margin, float y,
                                      float leading, float fontSize, String[] values) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(margin, y);
        content.showText(String.join(" | ", values));
        content.endText();
        return y - leading;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** Bỏ dấu tiếng Việt (NFD + xoá combining marks + "đ/Đ") — chỉ dùng cho PDF, xem chú thích ở trên. */
    private static String stripDiacritics(String s) {
        if (s == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.replace('đ', 'd').replace('Đ', 'D');
    }
}
