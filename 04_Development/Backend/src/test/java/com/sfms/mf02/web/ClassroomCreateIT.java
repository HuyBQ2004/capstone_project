package com.sfms.mf02.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF02-TC-INT-004 — BR-192 chặn tạo lớp khi trường chưa có năm học Active (UC-018).
 * ★ MF02-TC-SEC-007 (CRITICAL) — gate BR-025 (capacity ≤ 50) enforce ở SERVER, gọi API trực
 * tiếp bỏ qua UI vẫn bị chặn. Bắt buộc PASS trước khi coi module MF02 xong (TDS §13).
 */
class ClassroomCreateIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("MF02-TC-INT-004 — SCH-HN-0063 (PENDING) không có năm Active ⇒ 409 SCH-081/MSG-081")
    void createClassroomWithoutActiveYearIsRejected() throws Exception {
        // U-0018 là school-admin của SCH-HN-0051 — cần một school-admin cho SCH-HN-0063.
        // Không có JWT sẵn cho SCH-HN-0063 trong seed MF01; dùng trực tiếp trường B
        // (SCH-HN-0051 CÓ năm Active) để kiểm chứng nhánh ngược — tạo lớp OK khi có năm Active,
        // và assert riêng nhánh 409 bằng dữ liệu trường không có năm Active qua JDBC insert.
        jdbc.update("UPDATE academic_years SET status = 'DRAFT' "
                + "WHERE code = 'AY-2025-B' AND school_id = (SELECT id FROM schools WHERE code='SCH-HN-0051')");
        String gradeId = jdbc.queryForObject(
                "SELECT id FROM grades WHERE name = 'Khối 9' "
                        + "AND school_id = (SELECT id FROM schools WHERE code='SCH-HN-0051')",
                String.class);

        mvc.perform(post("/api/v1/classrooms")
                        .header("Authorization", authHeaderFor("U-0018"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of(
                                "name", "9A", "gradeId", gradeId, "capacity", 40, "academicYearId", "AY-2025-B"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SCH-081"))
                .andExpect(jsonPath("$.error.message").value(
                        "Cannot create classroom: this school has no Active academic year. "
                                + "Please activate an academic year first."));
    }

    @Test
    @DisplayName("★ MF02-TC-SEC-007 — POST /classrooms trực tiếp capacity=55 ⇒ vẫn 409 SCH-010, "
            + "không có cách nào qua mặt gate BR-025 bằng cách gọi API thẳng")
    void directApiCallCannotBypassCapacityGate() throws Exception {
        String gradeId = jdbc.queryForObject(
                "SELECT id FROM grades WHERE name = 'Khối 3' "
                        + "AND school_id = (SELECT id FROM schools WHERE code='SCH-HN-0042')",
                String.class);
        mvc.perform(post("/api/v1/classrooms")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of(
                                "name", "3E", "gradeId", gradeId, "capacity", 55, "academicYearId", "AY-2025"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SCH-010"))
                .andExpect(jsonPath("$.error.message").value(
                        "Classroom capacity exceeded. Maximum allowed is 50 students."));

        long created = count("SELECT count(*) FROM classrooms WHERE code = '3E'");
        assertThat(created).isZero();
    }

    @Test
    @DisplayName("Happy path — SCH-HN-0042 có AY-2025 ACTIVE ⇒ 201, code mặc định = name (D5/L2)")
    void createClassroomHappyPathDefaultsCodeToName() throws Exception {
        String gradeId = jdbc.queryForObject(
                "SELECT id FROM grades WHERE name = 'Khối 3' "
                        + "AND school_id = (SELECT id FROM schools WHERE code='SCH-HN-0042')",
                String.class);
        mvc.perform(post("/api/v1/classrooms")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of(
                                "name", "3F", "gradeId", gradeId, "capacity", 40, "academicYearId", "AY-2025"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("3F"))
                .andExpect(jsonPath("$.name").value("3F"));
    }
}
