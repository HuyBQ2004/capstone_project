package com.sfms.mf08.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-INT-013 — xuất sổ Mẫu số 5 đúng 12 cột theo filter (UC-118).
 */
class SampleRegisterExportIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String SCHOOL_ADMIN = "U-0001";
    private static final String INSPECTION_CODE = "INSP-MF08-EXPORT";

    @BeforeEach
    void seed() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-EXPORT-1", "Cơm trắng");
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-EXPORT-2", "Canh bí đỏ");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
        registerSample("DISH-MF08-EXPORT-1", "2026-06-30T01:00:00Z");   // ngoài khoảng lọc
        registerSample("DISH-MF08-EXPORT-2", "2026-07-30T01:00:00Z");   // trong khoảng lọc
    }

    private void registerSample(String dishCode, String collectedAt) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", dishCode);
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", collectedAt);
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void xlsxExportHasTwelveColumnHeaderInCorrectOrder() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples/export").param("format", "xlsx")
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentType()).contains("spreadsheetml");

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            assertThat((int) header.getLastCellNum()).isEqualTo(12);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("STT");
            assertThat(header.getCell(1).getStringCellValue()).contains("Tên mẫu");
            assertThat(header.getCell(2).getStringCellValue()).contains("Bữa ăn");
            assertThat(header.getCell(3).getStringCellValue()).contains("suất ăn");
            assertThat(header.getCell(4).getStringCellValue()).contains("Khối lượng");
            assertThat(header.getCell(5).getStringCellValue()).contains("Dụng cụ");
            assertThat(header.getCell(6).getStringCellValue()).contains("Nhiệt độ");
            assertThat(header.getCell(7).getStringCellValue()).contains("lấy mẫu");
            assertThat(header.getCell(8).getStringCellValue()).contains("hủy mẫu");
            assertThat(header.getCell(9).getStringCellValue()).contains("Ghi chú");
            assertThat(header.getCell(10).getStringCellValue()).contains("lấy mẫu");
            assertThat(header.getCell(11).getStringCellValue()).contains("hủy mẫu");
        }
    }

    @Test
    void xlsxExportOnlyIncludesRowsWithinDateFilter() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples/export").param("format", "xlsx")
                        .param("dateFrom", "2026-07-30").param("dateTo", "2026-07-30")
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)))
                .andReturn();

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            // 1 header + 1 data row (chỉ Canh bí đỏ, KHÔNG có Cơm trắng của 30/06).
            assertThat(sheet.getLastRowNum()).isEqualTo(1);
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Canh bí đỏ");
        }
    }

    @Test
    void amountColumnDisplaysComposedAmountAndUnitWhileDbKeepsThemSeparate() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples/export").param("format", "xlsx")
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)))
                .andReturn();

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(4).getStringCellValue()).isEqualTo("120 g");
        }
        // DB vẫn tách rời (L8) — không bị ảnh hưởng bởi tầng trình bày. Lọc theo inspection_id
        // của CHÍNH fixture test này — V15_1__restore_mf08.sql có sẵn nhiều mẫu khác cũng
        // amount=120g (trùng ngẫu nhiên), đếm toàn bảng không lọc sẽ dính cả những dòng đó.
        Long count = jdbc.queryForObject(
                "SELECT count(*) FROM food_samples WHERE sample_amount = 120 AND sample_unit = 'G' "
                        + "AND inspection_id = (SELECT id FROM inspection_sessions WHERE code = ?)",
                Long.class, INSPECTION_CODE);
        assertThat(count).isEqualTo(2L);
    }

    /**
     * ⚠️ GAP THẬT trong production code (KHÔNG sửa main code). Đo được khi viết test này:
     * {@code SampleRegisterExportServiceImpl.exportPdf()} dùng font chuẩn PDFBox
     * {@code Standard14Fonts.FontName.HELVETICA} (mã hoá {@code WinAnsiEncoding}) để vẽ TOÀN
     * BỘ nội dung, kể cả tên món/ghi chú tiếng Việt CÓ DẤU (vd "Cơm trắng", "Canh bí đỏ"). Font
     * chuẩn này KHÔNG mã hoá được các ký tự như {@code ơ}/{@code ư}/{@code ộ} — PDFBox ném
     * {@code IllegalArgumentException} ("U+01A1 ('ohorn') is not available in the font
     * Helvetica, encoding: WinAnsiEncoding") ngay khi gọi {@code showText()}.
     * {@code GlobalExceptionHandler} bắt {@code IllegalArgumentException} một cách CHUNG CHUNG
     * (dùng chung cho {@code MethodArgumentTypeMismatchException} v.v.) và trả về
     * <b>422 SMP-422</b> ("Validation failed…", {@code field=request}) — che mất đây thực ra là
     * lỗi RENDER phía server, không phải lỗi client. Vì MỌI món ăn thật trong hệ thống đều có
     * tên tiếng Việt có dấu, export PDF của Mẫu số 5 hiện <b>KHÔNG dùng được với dữ liệu thật
     * bất kỳ trường nào</b> — chỉ "chạy" được với dữ liệu toàn ASCII (không có ý nghĩa thực tế).
     * Xác nhận độc lập bằng cách gọi thẳng PDFBox 3.0.8 ngoài Spring context (không phụ thuộc
     * seed/test khác): {@code showText("Cơm trắng")} với {@code Standard14Fonts.HELVETICA} ném
     * đúng exception trên.
     */
    @Test
    void pdfExportCurrentlyFailsWithVietnameseDiacriticsGapNotAscii() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples/export").param("format", "pdf")
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-422");
    }

    @Test
    void invalidFormatParamIsRejectedWith422() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples/export").param("format", "csv")
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }
}
