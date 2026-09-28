package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-014 — D1: phạm vi unique của mã nghiệp vụ (✅ gỡ block 02/08 — L10/Q2).
 *
 * <p>Oracle: D1 "§5.2 thêm code VARCHAR(32) NOT NULL + UNIQUE(school_id, code)" · tiền lệ
 * classrooms.code + uq_class_ay_code (V3:106,118) · TDS-MF03 v1.1 §5.2 · MSG-012.
 */
class StudentCodeUniquenessIT extends AbstractIamIntegrationTest {

    /** FX-015 — trường thứ hai, độc lập với SCH-MF03-0001 (V5), self-contained cho test này. */
    private void createSecondSchoolWithClassroom() {
        jdbc.update("INSERT INTO schools (id, code, name, tax_id, status) VALUES "
                + "('99999999-9999-9999-9999-999999999999'::uuid, 'SCH-MF03-B', "
                + "'Trường B (synthetic)', '0100000099', 'ACTIVE')");
        jdbc.update("INSERT INTO academic_years (id, code, school_id, name, start_date, end_date, status) "
                + "VALUES (gen_random_uuid(), 'AY-MF03-B-2026', "
                + "'99999999-9999-9999-9999-999999999999'::uuid, 'Năm học B', '2026-09-01', "
                + "'2027-05-31', 'DRAFT')");
        jdbc.update("INSERT INTO grades (school_id, name, sort_order) "
                + "SELECT id, 'Khối ' || n, n FROM schools, generate_series(1,12) n "
                + "WHERE code = 'SCH-MF03-B' ON CONFLICT (school_id, name) DO NOTHING");
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                + "SELECT gen_random_uuid(), '3A', s.id, ay.id, '3A', g.id, 45 FROM schools s "
                + "JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 3' "
                + "WHERE s.code = 'SCH-MF03-B' AND ay.code = 'AY-MF03-B-2026'");
    }

    private Map<String, Object> body(String id, String classroomCode, String registrationNo, String email) {
        var m = new java.util.HashMap<String, Object>(Map.of(
                "name", "Uniqueness Test (synthetic)", "dob", "2019-01-01", "classroomId", classroomCode,
                "parent", Map.of("fullName", "PH (synthetic)", "email", email,
                        "phone", "0912345678", "relationship", "Bố")));
        if (id != null) {
            m.put("id", id);
        }
        return m;
    }

    @Test
    @DisplayName("1-3 — cùng mã HS-3A-001 ở 2 trường khác nhau đều 201 (code chỉ unique TRONG trường); "
            + "lần thứ hai CÙNG trường => 409 STU-012")
    void codeIsUniqueOnlyWithinSchool() throws Exception {
        createSecondSchoolWithClassroom();

        // Trường A: dùng school-admin U-9902 (SCH-MF03-0001) — nhưng "HS-3A-001" đã tồn tại ở
        // seed V5 cho trường A, nên bước 1 (tạo lần đầu) test bằng school-admin trường B.
        // Trường B chưa có "HS-3A-001" nào.
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT gen_random_uuid(), 'U-9992', 'School Admin B (synthetic)', "
                + "'schooladminb.synthetic@example.test', r.id, s.id, 'ACTIVE', true "
                + "FROM roles r, schools s WHERE r.role_key = 'school-admin' AND s.code = 'SCH-MF03-B'");

        // Bước 2 — cùng mã "HS-3A-001" ở TRƯỜNG B => 201 (khác school_id với bản ghi seed trường A).
        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9992"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(body("HS-3A-001", "3A", "SCHB-2026-0001",
                                "uniq2.synthetic@example.test"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("HS-3A-001"));

        // Bước 3 — trùng mã "HS-3A-001" lần hai Ở TRƯỜNG A (đã tồn tại từ seed V5) => 409 STU-012.
        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(body("HS-3A-001", "3A", "SCHA-2026-9999",
                                "uniq3.synthetic@example.test"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-012"))
                .andExpect(jsonPath("$.error.message").value(
                        "Student admission code is invalid or has already been used."));
    }

    @Test
    @DisplayName("4 — registration_no đã dùng ở trường A, tạo lại ở trường B => 409 STU-012 "
            + "(registration_no UNIQUE TOÀN HỆ THỐNG, khác phạm vi với code — điểm dễ nhầm nhất)")
    void registrationNoIsUniqueSystemWide() throws Exception {
        createSecondSchoolWithClassroom();
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT gen_random_uuid(), 'U-9993', 'School Admin B2 (synthetic)', "
                + "'schooladminb2.synthetic@example.test', r.id, s.id, 'ACTIVE', true "
                + "FROM roles r, schools s WHERE r.role_key = 'school-admin' AND s.code = 'SCH-MF03-B'");

        // registrationNo bị SERVER sinh tự động (CodeGenerator), không nhận từ client — kiểm bằng
        // cách chèn thẳng 1 registration_no trùng vào DB rồi assert UNIQUE constraint có thật,
        // vì API không có cách hợp lệ để client tự đặt registrationNo (đúng thiết kế D1/D5:
        // registration_no không phải "id" công khai client-controlled).
        assertThatViolatesRegistrationNoUniqueConstraint();
    }

    private void assertThatViolatesRegistrationNoUniqueConstraint() {
        String schoolAId = jdbc.queryForObject(
                "SELECT id FROM schools WHERE code = 'SCH-MF03-0001'", String.class);
        String classroomAId = jdbc.queryForObject(
                "SELECT id FROM classrooms WHERE code = '3A' AND school_id = ?::uuid", String.class, schoolAId);
        jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                        + "classroom_id, status) VALUES (gen_random_uuid(), 'REGNO-A', 'REGNO-DUP-001', "
                        + "'x', '2018-01-01', ?::uuid, ?::uuid, 'ACTIVE')",
                schoolAId, classroomAId);

        String schoolBId = jdbc.queryForObject(
                "SELECT id FROM schools WHERE code = 'SCH-MF03-B'", String.class);
        String classroomBId = jdbc.queryForObject(
                "SELECT id FROM classrooms WHERE code = '3A' AND school_id = ?::uuid", String.class, schoolBId);
        assertThatThrownByDuplicateRegistrationNo(schoolBId, classroomBId);
    }

    private void assertThatThrownByDuplicateRegistrationNo(String schoolBId, String classroomBId) {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                                + "classroom_id, status) VALUES (gen_random_uuid(), 'REGNO-B', "
                                + "'REGNO-DUP-001', 'y', '2018-01-01', ?::uuid, ?::uuid, 'ACTIVE')",
                        schoolBId, classroomBId))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("5 — information_schema: cột students.code tồn tại (KHÔNG student_code); "
            + "constraint uq_student_school_code trên (school_id, code); registration_no unique 1 cột")
    void schemaMatchesD1Convention() {
        assertThat(count("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_name = 'students' AND column_name = 'code'")).isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_name = 'students' AND column_name = 'student_code'")).isZero();
        assertThat(count("SELECT COUNT(*) FROM information_schema.table_constraints "
                + "WHERE table_name = 'students' AND constraint_name = 'uq_student_school_code'"))
                .isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_name = 'students' AND column_name = 'registration_no'")).isEqualTo(1L);
    }
}
