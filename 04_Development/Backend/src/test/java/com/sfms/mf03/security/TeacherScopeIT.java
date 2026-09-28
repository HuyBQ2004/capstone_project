package com.sfms.mf03.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-SEC-002 — Phạm vi Teacher: lớp chủ nhiệm, và KHÔNG tải tài liệu y tế. CRITICAL ·
 * OWASP A01:2021 · CWE-285.
 *
 * <p>Oracle: BR-123 (Teacher cần cảnh báo để phục vụ an toàn) · TDS §16 "Teacher xem được cảnh
 * báo dị ứng nhưng KHÔNG tải được tài liệu y tế gốc" · ADR-M03-5 · MSG-005 · D6
 * {@code AllergySeverity} (L11).
 *
 * <p>⚠️ Bước 5 (GET /health-documents/{id}/content) BỊ CHẶN — endpoint chưa tồn tại (ngoài
 * phạm vi đợt này). Các bước còn lại implement đầy đủ.
 * <p>⚠️ TestSpec dùng {@code ?class=3A}; implementation hiện tại hỗ trợ lọc theo
 * {@code ?classroomId=<code>} (khoá) — dùng tham số đó thay thế, cùng ý nghĩa nghiệp vụ
 * (lọc theo lớp), khác tên query param.
 */
class TeacherScopeIT extends AbstractIamIntegrationTest {

    private String teacherU9904;

    @BeforeEach
    void assignHomeroomTeacher() {
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT gen_random_uuid(), 'U-9904', 'Nguyễn Thu Trang (synthetic)', "
                + "'trangnt.teacher.synthetic@example.test', r.id, s.id, 'ACTIVE', true FROM roles r, "
                + "schools s WHERE r.role_key = 'teacher' AND s.code = 'SCH-MF03-0001'");
        jdbc.update("UPDATE classrooms SET homeroom_teacher_id = "
                + "(SELECT id FROM user_accounts WHERE code = 'U-9904') "
                + "WHERE code = '3A' AND school_id = (SELECT id FROM schools WHERE code = 'SCH-MF03-0001')");
        teacherU9904 = "U-9904";
    }

    @Test
    @DisplayName("1 — GET /students?classroomId=3A (JWT teacher chủ nhiệm 3A) => 200, mọi item thuộc lớp 3A")
    void listScopedToOwnHomeroomClass() throws Exception {
        var result = mvc.perform(get("/api/v1/students?classroomId=3A")
                        .header("Authorization", authHeaderFor(teacherU9904)))
                .andExpect(status().isOk())
                .andReturn();
        for (var item : parse(result).get("items")) {
            assertThat(item.get("classroomId").asText()).isEqualTo("3A");
        }
    }

    @Test
    @DisplayName("2 — HS-3A-001 (ALG-001/002 SEVERE VERIFIED) => allergySeverity==\"SEVERE\" (D6, "
            + "KHÔNG \"Severe\" — L11) để FE tô đỏ (BR-123)")
    void severityFieldUsesD6Enum() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001")
                        .header("Authorization", authHeaderFor(teacherU9904)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergySeverity").value("SEVERE"));
    }

    @Test
    @DisplayName("3 — GET /students?classroomId=<lớp khác 3B> (teacher chủ nhiệm 3A) => KHÔNG trả HS lớp khác "
            + "(giữ nguyên scope 3A dù cố lọc sang lớp khác — C2)")
    void cannotFilterIntoAnotherClassroom() throws Exception {
        // D — mã lớp '3D' (KHÔNG dùng '3B': đã bị V39__bulk_test_seed.sql chiếm cho
        // SCH-MF03-0001/AY-MF03-2026 — trùng sẽ vỡ uq_class_ay_code).
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                + "SELECT gen_random_uuid(), '3D', s.id, ay.id, '3D', g.id, 45 FROM schools s "
                + "JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 3' "
                + "WHERE s.code = 'SCH-MF03-0001' AND ay.code = 'AY-MF03-2026'");
        jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                + "classroom_id, status) SELECT gen_random_uuid(), 'HS-3D-777', 'REG-3D-777', "
                + "'HS lớp 3D (synthetic)', '2018-01-01', s.id, c.id, 'ACTIVE' FROM schools s "
                + "JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-0001' AND c.code = '3D'");

        var result = mvc.perform(get("/api/v1/students?classroomId=3D")
                        .header("Authorization", authHeaderFor(teacherU9904)))
                .andExpect(status().isOk())
                .andReturn();
        for (var item : parse(result).get("items")) {
            assertThat(item.get("classroomId").asText()).isEqualTo("3A"); // scope KHÔNG đổi theo filter
        }
    }

    @Test
    @DisplayName("4 — GET /students/HS-3B-002 (lớp khác) => 403")
    void cannotReadStudentDetailOutsideOwnClass() throws Exception {
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                + "SELECT gen_random_uuid(), '3C', s.id, ay.id, '3C', g.id, 45 FROM schools s "
                + "JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 3' "
                + "WHERE s.code = 'SCH-MF03-0001' AND ay.code = 'AY-MF03-2026'");
        jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                + "classroom_id, status) SELECT gen_random_uuid(), 'HS-3C-001', 'REG-3C-001', "
                + "'HS lớp 3C (synthetic)', '2018-01-01', s.id, c.id, 'ACTIVE' FROM schools s "
                + "JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-0001' AND c.code = '3C'");

        mvc.perform(get("/api/v1/students/HS-3C-001")
                        .header("Authorization", authHeaderFor(teacherU9904)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }

    @Test
    @DisplayName("6 — GET /students/{id}/health-file (teacher) => 403 (§16: chỉ Medical Staff)")
    void teacherCannotReadClinicalFile() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001/health-file")
                        .header("Authorization", authHeaderFor(teacherU9904)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7 — GET /students/{id}/allergy-alerts (teacher) => 200 (§16: Teacher = Class)")
    void teacherCanReadAllergyAlertsOfOwnClass() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001/allergy-alerts")
                        .header("Authorization", authHeaderFor(teacherU9904)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("8 — GET /students KHÔNG truyền lớp (teacher) => chỉ HS lớp 3A (không trả toàn "
            + "trường rồi để FE lọc)")
    void listWithoutFilterStillScopedToOwnClass() throws Exception {
        var result = mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor(teacherU9904)))
                .andExpect(status().isOk())
                .andReturn();
        for (var item : parse(result).get("items")) {
            assertThat(item.get("classroomId").asText()).isEqualTo("3A");
        }
    }
}
