package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-008 (phần tích hợp) — ADR-M03-3 / CF-M03-11: {@code specialDiet} = SMR APPROVED
 * <b>mới nhất</b>.
 *
 * <p>Bổ sung cho {@code mapper.StudentMapperTest} (phần unit thuần của {@code StudentMapper}, chỉ
 * pass-through). Việc CHỌN bản APPROVED mới nhất trong số nhiều bản ghi
 * ({@code SpecialMealRequestRepository.findFirstByStudentIdAndStatusOrderByRequestedAtDesc}, một
 * derived query {@code ORDER BY requested_at DESC LIMIT 1}) là hành vi của tầng repository/DB —
 * chỉ kiểm chứng được thật qua Testcontainers Postgres, không mock được ý nghĩa của
 * {@code ORDER BY}. Test dưới đây tái tạo đúng kịch bản {@code FX-010} của TestSpec.
 *
 * <p>Oracle: ADR-M03-3 + CF-M03-11 · TDS §5.2 (SMR APPROVED gần nhất theo {@code requestedAt}).
 */
class StudentSpecialDietIT extends AbstractIamIntegrationTest {

    private String studentId(String code) {
        return jdbc.queryForObject("SELECT id FROM students WHERE code = ?", String.class, code);
    }

    private String medicalStaffId() {
        return jdbc.queryForObject("SELECT id FROM user_accounts WHERE code = 'U-9903'", String.class);
    }

    @Test
    @DisplayName("1 — HS có 2 SMR APPROVED (Nut-Free cũ hơn, Gluten-Free mới hơn) => "
            + "GET /students/{code} trả specialDiet = bản MỚI NHẤT (Gluten-Free), KHÔNG phải bản cũ")
    void specialDietReflectsMostRecentApprovedRequest() throws Exception {
        String sid = studentId("HS-4A-001");
        String reviewer = medicalStaffId();

        jdbc.update("INSERT INTO special_meal_requests (id, code, student_id, dietary_type, duration, "
                + "justification, requested_by_user_id, requested_at, status, reviewed_by_user_id, "
                + "reviewed_at) VALUES (gen_random_uuid(), 'SMR-2026-9101', ?::uuid, 'Nut-Free', "
                + "'Học kỳ 1', 'Chỉ định bác sĩ cũ (synthetic)', ?::uuid, ?, 'APPROVED', ?::uuid, ?)",
                sid, reviewer, java.sql.Timestamp.from(Instant.parse("2026-01-15T02:00:00Z")), reviewer,
                java.sql.Timestamp.from(Instant.parse("2026-01-15T02:00:00Z")));
        jdbc.update("INSERT INTO special_meal_requests (id, code, student_id, dietary_type, duration, "
                + "justification, requested_by_user_id, requested_at, status, reviewed_by_user_id, "
                + "reviewed_at) VALUES (gen_random_uuid(), 'SMR-2026-9102', ?::uuid, 'Gluten-Free', "
                + "'Học kỳ 1', 'Chỉ định bác sĩ mới (synthetic)', ?::uuid, ?, 'APPROVED', ?::uuid, ?)",
                sid, reviewer, java.sql.Timestamp.from(Instant.parse("2026-06-20T02:00:00Z")), reviewer,
                java.sql.Timestamp.from(Instant.parse("2026-06-20T02:00:00Z")));

        mvc.perform(get("/api/v1/students/HS-4A-001")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialDiet").value("Gluten-Free"))
                .andExpect(jsonPath("$.hasSpecialDiet").value("yes"));
    }

    @Test
    @DisplayName("2 — HS chỉ có SMR PENDING (chưa duyệt) => specialDiet=null, hasSpecialDiet=\"no\" "
            + "(PENDING KHÔNG tính, chỉ APPROVED mới tính — ADR-M03-3)")
    void pendingRequestDoesNotCountAsSpecialDiet() throws Exception {
        String sid = studentId("HS-3A-005");
        String reviewer = medicalStaffId();

        jdbc.update("INSERT INTO special_meal_requests (id, code, student_id, dietary_type, duration, "
                + "justification, requested_by_user_id, requested_at, status) VALUES "
                + "(gen_random_uuid(), 'SMR-2026-9103', ?::uuid, 'Lactose-Free', 'Học kỳ 1', "
                + "'Chỉ định bác sĩ (synthetic)', ?::uuid, ?, 'PENDING')",
                sid, reviewer, java.sql.Timestamp.from(Instant.parse("2026-07-01T02:00:00Z")));

        mvc.perform(get("/api/v1/students/HS-3A-005")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialDiet").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.hasSpecialDiet").value("no"));
    }

    @Test
    @DisplayName("3 — HS không có SMR nào => specialDiet=null, hasSpecialDiet=\"no\"")
    void noRequestAtAllMeansNoSpecialDiet() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-002")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialDiet").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.hasSpecialDiet").value("no"));
    }

    @Test
    @DisplayName("4 — Assert schema: information_schema KHÔNG có cột students.special_diet "
            + "(CF-M03-11 — dẫn xuất, không lưu)")
    void studentsTableHasNoSpecialDietColumn() {
        assertThat(count("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_name = 'students' AND column_name = 'special_diet'")).isZero();
    }
}
