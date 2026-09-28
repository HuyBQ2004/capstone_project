package com.sfms.mf03.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-001 — UC-025: tạo HS + liên kết/tạo tài khoản PH + ghi lịch sử {@code KHAI_BAO}.
 *
 * <p>Oracle: BR-030 (≥1 PH/HS) · BR-010/BR-012 (MF01, qua port) · BR-172 (ghi lịch sử tại thời
 * điểm hành động) · MSG-031 "User account created successfully." · MSG-012 "Student admission
 * code is invalid or has already been used." · TDS §9.3 shape 201.
 */
class StudentCreateIT extends AbstractIamIntegrationTest {

    private String mf03SchoolId() {
        return jdbc.queryForObject("SELECT id FROM schools WHERE code = 'SCH-MF03-0001'", String.class);
    }

    private String mf03AcademicYearId() {
        return jdbc.queryForObject("SELECT id FROM academic_years WHERE code = 'AY-MF03-2026'", String.class);
    }

    // NOTE: dùng mã lớp '2C'/'2D' (KHÔNG '2A'/'2B') — V39__bulk_test_seed.sql đã chiếm
    // '2A'/'2B' cho SCH-MF03-0001/AY-MF03-2026, trùng mã sẽ vỡ uq_class_ay_code.
    private void createClassroom(String code) {
        String gradeId = jdbc.queryForObject(
                "SELECT id FROM grades WHERE school_id = ?::uuid AND name = 'Khối 2'", String.class, mf03SchoolId());
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                        + "VALUES (gen_random_uuid(), ?, ?::uuid, ?::uuid, ?, ?::uuid, 45)",
                code, mf03SchoolId(), mf03AcademicYearId(), code, gradeId);
    }

    @Test
    @DisplayName("Scenario 1 — tạo HS mới, PH chưa có tài khoản: 201, id=mã nghiệp vụ (D1), "
            + "healthStatus=NO_DECLARATION (chưa khai báo — ADR-M03-3), allergies=[], "
            + "parentAccountCreated=true, +1 student_parents, +1 health_history_events(KHAI_BAO)")
    void createStudentLinksOrCreatesParentAndAppendsHistory() throws Exception {
        createClassroom("2C");

        MvcResult result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "Nguyễn Gia Huy (synthetic)",
                                "dob", "2019-03-12",
                                "classroomId", "2C",
                                "parent", Map.of(
                                        "fullName", "Nguyễn Văn Sáu (synthetic)",
                                        "email", "saunv.parent.synthetic@example.test",
                                        "phone", "0912345678",
                                        "relationship", "Bố")))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = parse(result);
        String newId = body.get("id").asText();
        assertThat(newId).matches("^HS-\\d[A-Z]-\\d{3}$");                 // D1 — mã nghiệp vụ, KHÔNG UUID
        assertThat(body.get("studentCode").asText()).isEqualTo(newId);
        assertThat(body.get("registrationNo").asText()).isNotBlank();
        assertThat(body.get("dob").asText()).isEqualTo("2019-03-12");      // ISO — D4
        /*
         * NO_DECLARATION chứ KHÔNG phải VERIFIED (test cũ kỳ vọng VERIFIED).
         * `HealthStatusCalculator.computeLabel` đổi có chủ đích 08/08/2026: học sinh vừa tạo,
         * chưa ai khai gì và Y tế chưa xem, mà mang nhãn "Đã xác minh" ở cột Hồ sơ sức khỏe là
         * đúng lớp lỗi "chưa khai ≠ đã xác minh". Code là bên đúng ở đây, kỳ vọng test đã cũ.
         */
        assertThat(body.get("healthStatus").asText()).isEqualTo("NO_DECLARATION");
        assertThat(body.get("allergies").isArray()).isTrue();
        assertThat(body.get("allergies")).isEmpty();
        assertThat(body.get("parentAccountCreated").asBoolean()).isTrue();

        String studentId = jdbc.queryForObject("SELECT id FROM students WHERE code = ?", String.class, newId);
        assertThat(count("SELECT COUNT(*) FROM student_parents WHERE student_id = ?::uuid", studentId))
                .isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM health_history_events WHERE student_id = ?::uuid "
                + "AND type = 'KHAI_BAO'", studentId)).isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM user_accounts WHERE email = "
                + "'saunv.parent.synthetic@example.test'")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Scenario 2 — email PH đã tồn tại (U-9905, đang liên kết 2 HS): 201, "
            + "parentAccountCreated=false, KHÔNG tạo user mới, countLinkedStudents == 3")
    void createStudentReusesExistingParentAccount() throws Exception {
        createClassroom("2D");
        long usersBefore = count("SELECT COUNT(*) FROM user_accounts WHERE email = "
                + "'binhnv.parent.synthetic@example.test'");

        MvcResult result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "Nguyễn Gia Bình (synthetic)",
                                "dob", "2019-06-01",
                                "classroomId", "2D",
                                "parent", Map.of(
                                        "fullName", "Nguyễn Văn Bình (synthetic)",
                                        "email", "binhnv.parent.synthetic@example.test",
                                        "phone", "0912345678",
                                        "relationship", "Bố")))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = parse(result);
        assertThat(body.get("parentAccountCreated").asBoolean()).isFalse();

        long usersAfter = count("SELECT COUNT(*) FROM user_accounts WHERE email = "
                + "'binhnv.parent.synthetic@example.test'");
        assertThat(usersAfter).isEqualTo(usersBefore);                     // không tạo user trùng

        String parentId = jdbc.queryForObject(
                "SELECT id FROM user_accounts WHERE email = 'binhnv.parent.synthetic@example.test'", String.class);
        assertThat(count("SELECT COUNT(*) FROM student_parents WHERE parent_user_id = ?::uuid", parentId))
                .isEqualTo(3L);                                            // 2 seed (FX-002) + 1 mới
    }

    @Test
    @DisplayName("Scenario 3 — id trùng (\"HS-3A-001\" đã tồn tại) => 409 STU-012 + MSG-012 nguyên văn (D5)")
    void createStudentWithDuplicateIdIsRejected() throws Exception {
        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "id", "HS-3A-001",
                                "name", "Trùng mã (synthetic)",
                                "dob", "2019-01-01",
                                "classroomId", "3A",
                                "parent", Map.of(
                                        "fullName", "Phụ huynh mới (synthetic)",
                                        "email", "phmoi.parent.synthetic@example.test",
                                        "phone", "0912345000",
                                        "relationship", "Mẹ")))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-012"))
                .andExpect(jsonPath("$.error.message").value(
                        "Student admission code is invalid or has already been used."));
    }
}
