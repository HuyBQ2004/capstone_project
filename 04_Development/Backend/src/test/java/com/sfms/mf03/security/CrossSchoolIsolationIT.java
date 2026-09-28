package com.sfms.mf03.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-SEC-003 — Cross-school isolation. CRITICAL · OWASP A01:2021 · CWE-639.
 *
 * <p>Oracle: BR-022 (phạm vi theo trường, kế thừa MF02) · TDS §16 (School = chỉ trong trường
 * mình, {@code schoolId} trong JWT) · MSG-005.
 */
class CrossSchoolIsolationIT extends AbstractIamIntegrationTest {

    /** FX-015 — trường B {@code SCH-MF03-B9}, có HS-9Z-001. */
    @BeforeEach
    void setUpSchoolB() {
        jdbc.update("INSERT INTO schools (id, code, name, tax_id, status) VALUES "
                + "('88888888-8888-8888-8888-888888888899'::uuid, 'SCH-MF03-B9', "
                + "'Trường B (synthetic, SEC-003)', '0100000098', 'ACTIVE')");
        jdbc.update("INSERT INTO academic_years (id, code, school_id, name, start_date, end_date, status) "
                + "VALUES (gen_random_uuid(), 'AY-MF03-B9', "
                + "'88888888-8888-8888-8888-888888888899'::uuid, 'Năm B9', '2026-09-01', "
                + "'2027-05-31', 'ACTIVE')");
        jdbc.update("INSERT INTO grades (school_id, name, sort_order) "
                + "SELECT id, 'Khối ' || n, n FROM schools, generate_series(1,12) n "
                + "WHERE code = 'SCH-MF03-B9' ON CONFLICT (school_id, name) DO NOTHING");
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                + "SELECT gen_random_uuid(), '9Z', s.id, ay.id, '9Z', g.id, 45 FROM schools s "
                + "JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 9' "
                + "WHERE s.code = 'SCH-MF03-B9' AND ay.code = 'AY-MF03-B9'");
        jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                + "classroom_id, status) SELECT gen_random_uuid(), 'HS-9Z-001', 'REG-9Z-001', "
                + "'HS trường B (synthetic)', '2018-01-01', s.id, c.id, 'ACTIVE' FROM schools s "
                + "JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-B9' AND c.code = '9Z'");
    }

    @Test
    @DisplayName("1 — GET /students/HS-9Z-001 (JWT medical-staff trường A) => 403 STU-005")
    void cannotReadStudentDetailFromOtherSchool() throws Exception {
        mvc.perform(get("/api/v1/students/HS-9Z-001")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }

    @Test
    @DisplayName("2 — GET /students (JWT school-admin trường A) => KHÔNG item nào thuộc trường B; "
            + "total chỉ đếm trường A")
    void listExcludesOtherSchoolStudents() throws Exception {
        var result = mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andReturn();
        var body = parse(result);
        for (var item : body.get("items")) {
            assertThat(item.get("id").asText()).isNotEqualTo("HS-9Z-001");
        }
        long schoolAStudents = count("SELECT COUNT(*) FROM students WHERE school_id = "
                + "(SELECT id FROM schools WHERE code = 'SCH-MF03-0001')");
        assertThat((long) body.get("total").asInt()).isEqualTo(schoolAStudents);
    }

    @Test
    @DisplayName("3 — POST /students {classroomId: <lớp trường B>} (JWT school-admin trường A) => "
            + "404 (lớp không thuộc trường của actor — C2/BR-022); 0 HS nào trỏ classroom xuyên trường")
    void cannotCreateStudentInOtherSchoolsClassroom() throws Exception {
        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("name", "x", "dob", "2019-01-01", "classroomId", "9Z",
                                "parent", Map.of("fullName", "PH", "email", "sec003.synthetic@example.test",
                                        "phone", "0912345678", "relationship", "Bố")))))
                .andExpect(status().isNotFound());

        // Bất biến DB: không HS nào được ghi trỏ classroom thuộc trường khác school_id của chính nó.
        long crossSchoolRows = count("SELECT COUNT(*) FROM students s JOIN classrooms c "
                + "ON c.id = s.classroom_id WHERE s.school_id != c.school_id");
        assertThat(crossSchoolRows).isZero();
    }

    @Test
    @DisplayName("4 — tấn công tin body: gửi schoolId trường B trong body (DTO không có field này) "
            + "=> bị bỏ qua hoàn toàn, HS vẫn tạo ở trường A")
    void schoolIdInBodyIsIgnored() throws Exception {
        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"dob\":\"2019-01-01\",\"classroomId\":\"3A\","
                                + "\"schoolId\":\"SCH-MF03-B9\","
                                + "\"parent\":{\"fullName\":\"PH\",\"email\":\"sec003b.synthetic@example.test\","
                                + "\"phone\":\"0912345678\",\"relationship\":\"Bố\"}}"))
                .andExpect(status().isCreated())
                .andReturn();
        String newId = parse(result).get("id").asText();
        String actualSchoolCode = jdbc.queryForObject(
                "SELECT s.code FROM students st JOIN schools s ON s.id = st.school_id "
                        + "WHERE st.code = ?", String.class, newId);
        assertThat(actualSchoolCode).isEqualTo("SCH-MF03-0001");           // luôn = schoolId JWT, KHÔNG body
    }

    @Test
    @DisplayName("5 — GET /health-history?studentId=<mã HS trường B> (JWT trường A) => 403")
    void cannotReadHealthHistoryOfOtherSchoolStudent() throws Exception {
        // 2026-08-02: studentId nhận mã nghiệp vụ (D1), không phải UUID nội bộ — FE không bao
        // giờ có UUID PK của Student để gửi lên.
        mvc.perform(get("/api/v1/health-history?studentId=HS-9Z-001")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }
}
