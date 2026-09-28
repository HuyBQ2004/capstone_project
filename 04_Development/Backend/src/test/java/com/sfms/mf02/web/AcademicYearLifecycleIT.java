package com.sfms.mf02.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Bổ sung phủ tài liệu — 05_Testing/02_IT/MF02_School_Class_Management/Academic_Year_Management.md.
 *
 * <p>MF02-TC-INT-005 (đóng năm ⇒ lớp read-only), MF02-TC-INT-006 (BR-194 sao chép cấu trúc lớp),
 * MF02-TC-INT-007 (UC-177 lên lớp có loại trừ), MF02-TC-INT-016 (BR-195 năm ARCHIVED chỉ đọc).
 *
 * <p>Fixture V900: SCH-HN-0042 có AY-2025 (ACTIVE, 10 lớp) · AY-2026 (DRAFT, 6A/6B) ·
 * AY-2024 (CLOSED) · AY-2023 (ARCHIVED). U-0001 là School Admin của trường này.
 */
class AcademicYearLifecycleIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_A = "SCH-HN-0042";

    // ───────────────────────── MF02-TC-INT-005 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> bảng TC ghi bước 2 trả "400 SCH-014". Mã thật ném
     * {@code SchExceptions.classroomReadOnly()} = <b>409 SCH-193</b> ({@code ClassroomService
     * .update()}); {@code SCH-014} trong {@code SchError} là mã rate-limit của đăng ký trường,
     * không liên quan. Test kiểm hành vi thật.
     */
    @Test
    @DisplayName("MF02-TC-INT-005 — POST /academic-years/AY-2025/close ⇒ 200 CLOSED, mọi lớp của "
            + "năm đó readOnly=true; sửa lớp 3A sau đó ⇒ 409 SCH-193 (tài liệu ghi 400 SCH-014)")
    void closingAcademicYearCascadesReadOnlyToClassrooms() throws Exception {
        long classroomsInYear = count(
                "SELECT count(*) FROM classrooms c JOIN academic_years ay ON ay.id = c.academic_year_id "
                        + "WHERE ay.code = 'AY-2025'");
        assertThat(classroomsInYear).isGreaterThan(0);

        mvc.perform(post("/api/v1/academic-years/AY-2025/close")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.status").value("CLOSED"));

        assertThat(count("SELECT count(*) FROM academic_years WHERE code = 'AY-2025' AND status = 'CLOSED'"))
                .isEqualTo(1);
        long readOnly = count(
                "SELECT count(*) FROM classrooms c JOIN academic_years ay ON ay.id = c.academic_year_id "
                        + "WHERE ay.code = 'AY-2025' AND c.read_only = true");
        assertThat(readOnly).isEqualTo(classroomsInYear);

        MvcResult update = mvc.perform(patch("/api/v1/classrooms/3A")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("capacity", 40))))
                .andReturn();
        assertThat(update.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(update).at("/error/code").asText()).isEqualTo("SCH-193");

        // Không có thay đổi nào lọt xuống DB. (Lọc theo trường — seed MF03 cũng có một lớp '3A'
        // ở trường synthetic khác, đếm toàn cục sẽ ra 2.)
        assertThat(count("SELECT count(*) FROM classrooms c JOIN schools s ON s.id = c.school_id "
                + "WHERE c.code = '3A' AND s.code = ? AND c.capacity = 45", SCHOOL_A)).isEqualTo(1);
    }

    // ───────────────────────── MF02-TC-INT-006 ─────────────────────────

    @Test
    @DisplayName("MF02-TC-INT-006 — POST /academic-years copyStructure=true ⇒ 201, lớp của năm "
            + "liền trước được sao chép nguyên grade/name/capacity, sĩ số khởi tạo 0 (BR-194)")
    void createAcademicYearCopiesClassStructureWithZeroStudents() throws Exception {
        // Năm liền trước của 2027-09-05 là AY-2026 (bắt đầu 2026-09-05) — 2 lớp 6A (50) và 6B (45).
        List<Map<String, Object>> previous = jdbc.queryForList(
                "SELECT c.code, c.capacity FROM classrooms c JOIN academic_years ay "
                        + "ON ay.id = c.academic_year_id WHERE ay.code = 'AY-2026' ORDER BY c.code");
        assertThat(previous).hasSize(2);

        MvcResult created = mvc.perform(post("/api/v1/academic-years")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("id", "AY-2027", "name", "Năm học 2027-2028",
                                "startDate", "2027-09-05", "endDate", "2028-05-31",
                                "copyStructure", true))))
                .andReturn();

        assertThat(created.getResponse().getStatus()).isEqualTo(201);
        var body = parse(created);
        assertThat(body.get("id").asText()).isEqualTo("AY-2027");
        assertThat(body.get("status").asText()).isEqualTo("DRAFT");
        assertThat(body.get("copyStructure").asBoolean()).isTrue();
        assertThat(body.get("classes").asLong()).isEqualTo(2);
        // BR-194 — sao chép CẤU TRÚC, KHÔNG chuyển học sinh.
        assertThat(body.get("students").asLong()).isZero();

        List<Map<String, Object>> copied = jdbc.queryForList(
                "SELECT c.code, c.capacity, c.read_only, c.homeroom_teacher_id FROM classrooms c "
                        + "JOIN academic_years ay ON ay.id = c.academic_year_id "
                        + "WHERE ay.code = 'AY-2027' ORDER BY c.code");
        assertThat(copied).hasSize(2);
        for (int i = 0; i < 2; i++) {
            assertThat(copied.get(i).get("code")).isEqualTo(previous.get(i).get("code"));
            assertThat(copied.get(i).get("capacity")).isEqualTo(previous.get(i).get("capacity"));
            assertThat(copied.get(i).get("read_only")).isEqualTo(Boolean.FALSE);
            assertThat(copied.get(i).get("homeroom_teacher_id")).isNull();   // GVCN KHÔNG copy
        }
        assertThat(count("SELECT count(*) FROM students s JOIN classrooms c ON c.id = s.classroom_id "
                + "JOIN academic_years ay ON ay.id = c.academic_year_id WHERE ay.code = 'AY-2027'")).isZero();
    }

    // ───────────────────────── MF02-TC-INT-007 ─────────────────────────

    @Test
    @DisplayName("MF02-TC-INT-007 — POST /academic-years/AY-2026/promote 5A→6A loại trừ HS-5A-005 "
            + "⇒ 200 promoted=2/excluded=1; HS bị loại trừ ở nguyên lớp cũ, hồ sơ sức khỏe nguyên vẹn")
    void promoteStudentsHonoursExclusionList() throws Exception {
        String classroom5A = classroomId("5A");
        seedStudent("HS-5A-001", "DTD-2027-0001", "Nguyễn An (synthetic)", classroom5A);
        seedStudent("HS-5A-002", "DTD-2027-0002", "Trần Bình (synthetic)", classroom5A);
        seedStudent("HS-5A-005", "DTD-2027-0005", "Lê Cường (synthetic)", classroom5A);
        long healthDocsBefore = count("SELECT count(*) FROM health_documents");

        MvcResult result = mvc.perform(post("/api/v1/academic-years/AY-2026/promote")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "sourceAcademicYearId", "AY-2025",
                                "mappings", List.of(Map.of("sourceClassroomId", "5A",
                                        "targetClassroomId", "6A")),
                                "excludeStudentIds", List.of("HS-5A-005")))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("promoted").asLong()).isEqualTo(2);
        assertThat(body.get("excluded").asLong()).isEqualTo(1);
        assertThat(body.get("targetAcademicYearId").asText()).isEqualTo("AY-2026");

        String classroom6A = classroomId("6A");
        assertThat(count("SELECT count(*) FROM students WHERE classroom_id = ?::uuid AND code IN "
                + "('HS-5A-001','HS-5A-002')", classroom6A)).isEqualTo(2);
        // HS bị loại trừ vẫn nằm ở lớp nguồn.
        assertThat(count("SELECT count(*) FROM students WHERE classroom_id = ?::uuid AND code = 'HS-5A-005'",
                classroom5A)).isEqualTo(1);
        // Hồ sơ sức khỏe KHÔNG bị đụng tới (BR-194).
        assertThat(count("SELECT count(*) FROM health_documents")).isEqualTo(healthDocsBefore);
    }

    // ───────────────────────── MF02-TC-INT-016 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> bước 2 ("update năm ARCHIVED ⇒ 403") không kiểm được vì MF02
     * KHÔNG có endpoint sửa năm học nào — {@code AcademicYearController} chỉ khai GET list, GET
     * detail, POST create/activate/close/promote. Tính bất biến của năm ARCHIVED vì vậy đến từ
     * việc VẮNG MẶT đường ghi, không phải từ một kiểm tra 403; test khẳng định đúng điều đó.
     */
    @Test
    @DisplayName("MF02-TC-INT-016 — GET /academic-years?status=ARCHIVED ⇒ 200 trả AY-2023 (BR-195 "
            + "vẫn truy vấn được); không tồn tại đường ghi lên năm học ⇒ PATCH bị từ chối, dữ liệu không đổi")
    void archivedAcademicYearsAreQueryableButNotWritable() throws Exception {
        MvcResult listed = mvc.perform(get("/api/v1/academic-years")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .param("status", "ARCHIVED"))
                .andReturn();

        assertThat(listed.getResponse().getStatus()).isEqualTo(200);
        var body = parse(listed);
        assertThat(body.get("total").asLong()).isEqualTo(1);
        assertThat(body.get("items").size()).isEqualTo(1);
        assertThat(body.at("/items/0/id").asText()).isEqualTo("AY-2023");
        assertThat(body.at("/items/0/status").asText()).isEqualTo("ARCHIVED");

        MvcResult write = mvc.perform(patch("/api/v1/academic-years/AY-2023")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "bị sửa"))))
                .andReturn();
        assertThat(write.getResponse().getStatus()).isIn(404, 405);
        assertThat(count("SELECT count(*) FROM academic_years WHERE code = 'AY-2023' "
                + "AND status = 'ARCHIVED' AND name NOT LIKE '%sửa%'")).isEqualTo(1);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    private String classroomId(String code) {
        return jdbc.queryForObject(
                "SELECT c.id::text FROM classrooms c JOIN schools s ON s.id = c.school_id "
                        + "WHERE c.code = ? AND s.code = ?", String.class, code, SCHOOL_A);
    }

    private void seedStudent(String code, String registrationNo, String name, String classroomId) {
        jdbc.update("""
                INSERT INTO students (id, code, registration_no, name, dob, school_id, classroom_id, status)
                VALUES (gen_random_uuid(), ?, ?, ?, DATE '2016-05-10',
                    (SELECT id FROM schools WHERE code = 'SCH-HN-0042'), ?::uuid, 'ACTIVE')
                """, code, registrationNo, name, classroomId);
    }
}
