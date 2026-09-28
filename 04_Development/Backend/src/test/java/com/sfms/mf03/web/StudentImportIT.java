package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Item 4/ADR-M03-6 — {@code POST /students/import/validate} + {@code /import/{importId}/commit}
 * (UC-010, BR-032). Trước bản sửa này {@code StudentImportService} không tồn tại — mọi call tới
 * 2 endpoint này 404 (§0-BIS việc tồn đọng #4).
 *
 * <p>13/08/2026 (Nam) — lớp test này ĐANG ĐỎ từ commit 50c3025: lúc đó parser đổi từ CSV sang
 * {@code .xlsx} ({@code WorkbookFactory}) nhưng test vẫn upload {@code text/csv} ⇒ 500
 * "Can't open workbook - unsupported file type: UNKNOWN". Không ai thấy vì job CI
 * {@code backend-test} chỉ chạy {@code mvn test} (surefire = unit test), KHÔNG chạy failsafe nên
 * *IT.java không bao giờ chạy trên CI. Nay dựng file Excel thật bằng POI, và thêm case 6/7 khoá
 * đúng 2 bug production: nhập lại file cũ nhân bản học sinh, và import vượt sức chứa lớp.
 */
class StudentImportIT extends AbstractIamIntegrationTest {

    private static final String[] TEMPLATE_LABELS = {
            "Họ tên học sinh *", "Ngày sinh * (yyyy-mm-dd)", "Mã lớp * (chọn từ danh sách)",
            "Họ tên phụ huynh *", "Email phụ huynh *", "SĐT phụ huynh", "Quan hệ với học sinh"};

    /**
     * Dựng file {@code .xlsx} đúng khuôn parser đọc: dòng 0 là header (parser đọc THEO VỊ TRÍ
     * cột nên nhãn không quan trọng), mỗi phần tử {@code rows} là một dòng 7 cột phân tách bằng
     * dấu phẩy — giữ nguyên cách viết dữ liệu test cũ cho dễ đọc.
     */
    private static MockMultipartFile xlsx(String... rows) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Học sinh");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_LABELS.length; i++) {
                header.createCell(i).setCellValue(TEMPLATE_LABELS[i]);
            }
            for (int r = 0; r < rows.length; r++) {
                String[] cols = rows[r].split(",", -1);
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < cols.length; c++) {
                    row.createCell(c).setCellValue(cols[c]);
                }
            }
            wb.write(bos);
        }
        return new MockMultipartFile("file", "import.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray());
    }

    @Test
    @DisplayName("1 — validate() file 1 hàng hợp lệ + 1 hàng email PH sai định dạng => "
            + "200, validCount=1, invalidCount=1, KHÔNG ghi students")
    void validateReportsPerRowErrorsWithoutWriting() throws Exception {
        long studentsBefore = count("SELECT COUNT(*) FROM students");
        MockMultipartFile file = xlsx(
                "Nguyễn Văn Import1 (synthetic),2019-01-15,3A,Nguyễn Thị PH1 (synthetic),"
                        + "ph1.synthetic@example.com,0911111111,Mẹ",
                "Nguyễn Văn Import2 (synthetic),2019-02-20,3A,Nguyễn Thị PH2 (synthetic),"
                        + "khong-phai-email,0922222222,Bố");

        mvc.perform(multipart("/api/v1/students/import/validate")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.validCount").value(1))
                .andExpect(jsonPath("$.invalidCount").value(1))
                .andExpect(jsonPath("$.rows[1].valid").value(false))
                .andExpect(jsonPath("$.rows[1].code").value("STU-012B"));

        assertThat(count("SELECT COUNT(*) FROM students")).isEqualTo(studentsBefore);
    }

    @Test
    @DisplayName("2 — validate() rồi commit() => 200 MSG-035, đúng 1 học sinh + 1 liên kết PH mới được ghi; "
            + "commit lần 2 => 409 STU-014 (không import 2 lần cùng batch)")
    void validateThenCommitWritesExactlyOnce() throws Exception {
        long studentsBefore = count("SELECT COUNT(*) FROM students");
        MockMultipartFile file = xlsx(
                "Nguyễn Văn Import3 (synthetic),2019-03-10,3A,Nguyễn Thị PH3 (synthetic),"
                        + "ph3.synthetic@example.com,0933333333,Mẹ");

        String validateBody = mvc.perform(multipart("/api/v1/students/import/validate")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String importId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(validateBody).get("importId").asText();

        assertThat(count("SELECT COUNT(*) FROM students")).isEqualTo(studentsBefore);   // chưa ghi

        mvc.perform(post("/api/v1/students/import/" + importId + "/commit")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(1))
                .andExpect(jsonPath("$.newParentAccounts").value(1))
                .andExpect(jsonPath("$.message").value(
                        "Student profiles and parent links imported successfully."));

        assertThat(count("SELECT COUNT(*) FROM students")).isEqualTo(studentsBefore + 1);
        assertThat(count("SELECT COUNT(*) FROM user_accounts WHERE email = 'ph3.synthetic@example.com'"))
                .isEqualTo(1);

        mvc.perform(post("/api/v1/students/import/" + importId + "/commit")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-014"));

        // Commit lần 2 KHÔNG ghi thêm — vẫn đúng +1 so với trước khi import.
        assertThat(count("SELECT COUNT(*) FROM students")).isEqualTo(studentsBefore + 1);
    }

    @Test
    @DisplayName("3 — GET /students/template (School Admin) => 200, workbook .xlsx header đúng 7 cột")
    void templateDownloadHasExpectedHeader() throws Exception {
        byte[] body = mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/v1/students/template")
                                .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(body))) {
            Row header = wb.getSheetAt(0).getRow(0);
            for (int i = 0; i < TEMPLATE_LABELS.length; i++) {
                assertThat(header.getCell(i).getStringCellValue()).isEqualTo(TEMPLATE_LABELS[i]);
            }
        }
    }

    /**
     * MF03-TC-INT-007 bước 5 — rollback all-or-nothing: dòng 1 hợp lệ độc lập, dòng 2 gán PH đã
     * đủ 5 liên kết (BR-201/STU-201) — bắn ở giữa vòng lặp {@code commit()} của
     * {@code StudentImportServiceImpl}. Vì {@code commit()} chạy MỘT {@code @Transactional} bao
     * toàn bộ vòng lặp, dòng 1 (đã insert TRƯỚC dòng 2 trong cùng transaction) phải bị cuốn theo
     * rollback — không được "nửa vời" (CF-M03-8).
     */
    @Test
    @DisplayName("4 — commit() dòng 2 vi phạm BR-201 (PH đã đủ 5 HS) => 409 STU-201, "
            + "TOÀN BỘ batch rollback (kể cả dòng 1 hợp lệ đã insert trước đó trong cùng transaction)")
    void commitRollsBackEntireBatchWhenAnyRowViolatesParentLinkLimit() throws Exception {
        String fullParentId = "44444444-4444-4444-4444-444444444491";
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT ?::uuid, 'U-9991', 'PH đủ 5 HS import (synthetic)', "
                + "'ph5.import.synthetic@example.test', r.id, s.id, 'ACTIVE', true FROM roles r, schools s "
                + "WHERE r.role_key = 'parent' AND s.code = 'SCH-MF03-0001'", fullParentId);
        for (int i = 0; i < 5; i++) {
            jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                    + "classroom_id, status) SELECT gen_random_uuid(), ?, ?, 'HS đủ import (synthetic)', "
                    + "'2018-01-01', s.id, c.id, 'ACTIVE' FROM schools s "
                    + "JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-0001' AND c.code = '3A'",
                    "IMPFULL-" + i, "IMPFULLREG-" + i);
            jdbc.update("INSERT INTO student_parents (student_id, parent_user_id) "
                    + "SELECT (SELECT id FROM students WHERE code = ?), ?::uuid", "IMPFULL-" + i, fullParentId);
        }

        long studentsBefore = count("SELECT COUNT(*) FROM students");
        MockMultipartFile file = xlsx(
                "Nguyễn Văn Import4 (synthetic),2019-04-01,3A,Nguyễn Thị PH4 (synthetic),"
                        + "ph4.rollback.synthetic@example.com,0944444444,Mẹ",
                "Nguyễn Văn Import5 (synthetic),2019-05-01,3A,PH đủ 5 HS import (synthetic),"
                        + "ph5.import.synthetic@example.test,0955555555,Bố");

        String validateBody = mvc.perform(multipart("/api/v1/students/import/validate")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validCount").value(2))   // cả 2 dòng hợp lệ theo validate()
                                                                  // — BR-201 chỉ lộ ra lúc commit()
                .andReturn().getResponse().getContentAsString();
        String importId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(validateBody).get("importId").asText();

        mvc.perform(post("/api/v1/students/import/" + importId + "/commit")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-201"));

        assertThat(count("SELECT COUNT(*) FROM students")).as(
                        "toàn bộ batch phải rollback — dòng 1 hợp lệ KHÔNG được để lại dù insert trước "
                                + "dòng 2 lỗi trong cùng transaction")
                .isEqualTo(studentsBefore);
    }

    /**
     * BUG PRODUCTION 13/08/2026 — "nhập excel xong nó lặp 2 lần". {@code validate()} chỉ dò trùng
     * bằng {@code seenWithinFile} (HashSet dựng mới mỗi request) nên chỉ thấy trùng TRONG một
     * file; upload lại đúng file đã import ⇒ mọi dòng "hợp lệ" ⇒ nhân bản toàn bộ học sinh
     * (Hồ Quỳnh Vy có cả HS-1A-016 lẫn HS-1A-045 trên production).
     */
    @Test
    @DisplayName("6 — nhập lại ĐÚNG file đã commit => validate đánh trùng (STU-012), "
            + "commit không tạo thêm học sinh nào")
    void reimportingTheSameFileDoesNotDuplicateStudents() throws Exception {
        MockMultipartFile file = xlsx(
                "Nguyễn Văn Import6 (synthetic),2019-06-15,3A,Nguyễn Thị PH6 (synthetic),"
                        + "ph6.synthetic@example.com,0966666666,Mẹ");

        commitImport(file);
        long afterFirstImport = count("SELECT COUNT(*) FROM students");

        // Lần 2 — cùng file, cùng nội dung.
        mvc.perform(multipart("/api/v1/students/import/validate")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validCount").value(0))
                .andExpect(jsonPath("$.duplicateCount").value(1))
                .andExpect(jsonPath("$.rows[0].valid").value(false))
                .andExpect(jsonPath("$.rows[0].code").value("STU-012"));

        commitImport(file);
        assertThat(count("SELECT COUNT(*) FROM students"))
                .as("nhập lại file cũ phải là no-op, không nhân bản học sinh")
                .isEqualTo(afterFirstImport);
    }

    /**
     * BUG PRODUCTION 13/08/2026 — "vượt quá cả sức chứa". Thêm từng học sinh một thì
     * {@code StudentServiceImpl.create()} chặn đúng BR-025, nhưng {@code commit()} của import
     * không hề đọc {@code classrooms.capacity} nên đẩy sĩ số vượt bao nhiêu cũng lọt.
     */
    @Test
    @DisplayName("7 — import vượt sức chứa lớp => dòng thừa bị đánh STU-010 ở validate, "
            + "commit chỉ ghi đúng số chỗ còn trống (BR-025)")
    void importCannotPushClassroomOverCapacity() throws Exception {
        // Ghim capacity lớp 3A = sĩ số hiện tại + 1 ⇒ chỉ còn đúng 1 chỗ cho 2 dòng dưới.
        jdbc.update("UPDATE classrooms SET capacity = (SELECT COUNT(*) + 1 FROM students s "
                + "WHERE s.classroom_id = classrooms.id) WHERE code = '3A' AND school_id = "
                + "(SELECT id FROM schools WHERE code = 'SCH-MF03-0001')");
        long studentsBefore = count("SELECT COUNT(*) FROM students");

        MockMultipartFile file = xlsx(
                "Nguyễn Văn Import7 (synthetic),2019-07-01,3A,Nguyễn Thị PH7 (synthetic),"
                        + "ph7.synthetic@example.com,0977777777,Mẹ",
                "Nguyễn Văn Import8 (synthetic),2019-08-01,3A,Nguyễn Thị PH8 (synthetic),"
                        + "ph8.synthetic@example.com,0988888888,Bố");

        mvc.perform(multipart("/api/v1/students/import/validate")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validCount").value(1))
                .andExpect(jsonPath("$.invalidCount").value(1))
                .andExpect(jsonPath("$.rows[1].valid").value(false))
                .andExpect(jsonPath("$.rows[1].code").value("STU-010"));

        commitImport(file);
        assertThat(count("SELECT COUNT(*) FROM students"))
                .as("chỉ được lấp đúng 1 chỗ trống còn lại, không vượt capacity")
                .isEqualTo(studentsBefore + 1);
    }

    /**
     * D1 — hệ quả của việc dọn học sinh nhân bản trên production
     * ({@code scripts/cleanup_duplicate_students.sql}). Mã HS trước đây suy từ SĨ SỐ
     * ({@code countByClassroomId() + 1}) nên xoá bớt học sinh là bộ đếm tụt về số ĐÃ CẤP ⇒ học
     * sinh kế tiếp nhận mã trùng ⇒ vỡ {@code uq_student_school_code}. Nay dùng
     * {@link com.sfms.mf03.service.StudentCodeAllocator} (code_sequences, chỉ tiến).
     */
    @Test
    @DisplayName("8 — xoá học sinh giữa dãy rồi nhập tiếp => mã mới KHÔNG trùng mã đã cấp "
            + "(bộ đếm không lùi theo sĩ số)")
    void studentCodeIsNotReusedAfterDeletion() throws Exception {
        commitImport("Nguyễn Văn Import9 (synthetic),2019-09-01,3A,Nguyễn Thị PH9 (synthetic),"
                + "ph9.synthetic@example.com,0999999991,Mẹ");
        commitImport("Nguyễn Văn Import10 (synthetic),2019-10-01,3A,Nguyễn Thị PH10 (synthetic),"
                + "ph10.synthetic@example.com,0999999992,Mẹ");
        String highestCode = jdbc.queryForObject(
                "SELECT code FROM students WHERE name = 'Nguyễn Văn Import10 (synthetic)'", String.class);

        // Mô phỏng đúng thao tác dọn của scripts/cleanup_duplicate_students.sql.
        // health_history_events vừa KHÔNG có ON DELETE CASCADE (V4 dòng 178), vừa append-only
        // bằng RULE `hh_no_delete ... DO INSTEAD NOTHING` (V4 dòng 206, BR-172/ADR-M03-4) —
        // tức DELETE trên nó KHÔNG báo lỗi mà lặng lẽ xoá 0 dòng, rồi DELETE students mới vỡ
        // khoá ngoại. Phải tắt rule quanh thao tác dọn; DDL trong Postgres có transaction nên
        // rollback là rule tự bật lại.
        jdbc.update("ALTER TABLE health_history_events DISABLE RULE hh_no_delete");
        try {
            jdbc.update("DELETE FROM health_history_events WHERE student_id = "
                    + "(SELECT id FROM students WHERE name = 'Nguyễn Văn Import9 (synthetic)')");
            jdbc.update("DELETE FROM students WHERE name = 'Nguyễn Văn Import9 (synthetic)'");
        } finally {
            jdbc.update("ALTER TABLE health_history_events ENABLE RULE hh_no_delete");
        }

        commitImport("Nguyễn Văn Import11 (synthetic),2019-11-01,3A,Nguyễn Thị PH11 (synthetic),"
                + "ph11.synthetic@example.com,0999999993,Mẹ");

        String newCode = jdbc.queryForObject(
                "SELECT code FROM students WHERE name = 'Nguyễn Văn Import11 (synthetic)'", String.class);
        assertThat(newCode)
                .as("mã mới phải LỚN HƠN mã cao nhất đã cấp, không lấp lại lỗ hổng vừa xoá")
                .isGreaterThan(highestCode);
        assertThat(count("SELECT COUNT(*) FROM students WHERE code = '" + newCode + "'"))
                .as("không được cấp trùng mã đang tồn tại")
                .isEqualTo(1);
    }

    /** validate() → commit() cho một dòng dữ liệu; tiện cho các test chỉ cần ghi 1 học sinh. */
    private void commitImport(String row) throws Exception {
        commitImport(xlsx(row));
    }

    /** validate() → commit() cho cùng một file; trả về body của commit. */
    private String commitImport(MockMultipartFile file) throws Exception {
        String validateBody = mvc.perform(multipart("/api/v1/students/import/validate")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String importId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(validateBody).get("importId").asText();
        return mvc.perform(post("/api/v1/students/import/" + importId + "/commit")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    /** MF03-TC-INT-007 bước 6 — commit với importId không tồn tại => 404 STU-404 (MSG-137). */
    @Test
    @DisplayName("5 — commit() với importId không tồn tại => 404 STU-404")
    void commitWithUnknownImportIdReturnsNotFound() throws Exception {
        mvc.perform(post("/api/v1/students/import/IMP-2026-9999/commit")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("STU-404"));
    }
}
