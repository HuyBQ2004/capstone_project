package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-002 — BR-025: sức chứa lớp khi tạo &amp; khi chuyển lớp (✅ Q3 chốt 02/08 — MSG-142).
 *
 * <p>Oracle: BR-025 (MF02) "Mỗi lớp học có sức chứa tối đa 50 học sinh." + invariant TDS-MF02
 * §4/§5.2 "capacity ≤ 50, students ≤ capacity" · MSG-142 "This classroom has reached its
 * capacity." (chốt 02/08 — KHÔNG còn mượn MSG-010 của MF02) · TDS §10 STU-010 = HTTP 409.
 */
class StudentCapacityIT extends AbstractIamIntegrationTest {

    private UUID mf03SchoolId() {
        return UUID.fromString(jdbc.queryForObject(
                "SELECT id FROM schools WHERE code = 'SCH-MF03-0001'", String.class));
    }

    private UUID mf03AcademicYearId() {
        return UUID.fromString(jdbc.queryForObject(
                "SELECT id FROM academic_years WHERE code = 'AY-MF03-2026'", String.class));
    }

    /** Tạo lớp riêng cho test này (KHÔNG dùng chung 3A/4A của V5 — tránh nhiễu test khác). */
    private UUID createClassroom(String code, int capacity, boolean readOnly) {
        UUID id = UUID.randomUUID();
        UUID gradeId = UUID.fromString(jdbc.queryForObject(
                "SELECT id FROM grades WHERE school_id = ? AND name = 'Khối 3'", String.class, mf03SchoolId()));
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, "
                        + "capacity, read_only) VALUES (?::uuid, ?, ?::uuid, ?::uuid, ?, ?::uuid, ?, ?)",
                id, code, mf03SchoolId(), mf03AcademicYearId(), code, gradeId, capacity, readOnly);
        return id;
    }

    private void fillClassroom(UUID classroomId, int count) {
        for (int i = 0; i < count; i++) {
            jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                            + "classroom_id, status) VALUES (gen_random_uuid(), ?, ?, ?, '2018-01-01', "
                            + "?::uuid, ?::uuid, 'ACTIVE')",
                    "FILL-" + classroomId.toString().substring(0, 8) + "-" + i,
                    "FILLREG-" + classroomId.toString().substring(0, 8) + "-" + i,
                    "Học sinh lấp đầy " + i + " (synthetic)", mf03SchoolId(), classroomId);
        }
    }

    private Map<String, Object> createRequest(String classroomCode) {
        return Map.of("name", "Ứng viên nhập học (synthetic)", "dob", "2019-01-01",
                "classroomId", classroomCode,
                "parent", Map.of("fullName", "PH ứng viên (synthetic)",
                        "email", "capacity." + UUID.randomUUID() + "@example.test",
                        "phone", "0912345111", "relationship", "Bố"));
    }

    @Test
    @DisplayName("ca 1 — lớp capacity=50 đầy 50 => 409 STU-010/MSG-142, details {capacity:50, current:50}")
    void classroomAtCapacity50IsRejected() throws Exception {
        UUID id = createClassroom("CAP-50-FULL", 50, false);
        fillClassroom(id, 50);

        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createRequest("CAP-50-FULL"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-010"))
                .andExpect(jsonPath("$.error.message").value("This classroom has reached its capacity."));
    }

    @Test
    @DisplayName("ca 2 — lớp capacity=45 đầy 45 (< 50) => VẪN 409 STU-010/MSG-142 nguyên văn giống ca 1 "
            + "(Q3: message không nhắc số nào)")
    void classroomAtCapacity45IsRejectedWithSameMessageAsCapacity50() throws Exception {
        UUID id45 = createClassroom("CAP-45-FULL", 45, false);
        fillClassroom(id45, 45);
        UUID id50 = createClassroom("CAP-50-FULL-2", 50, false);
        fillClassroom(id50, 50);

        String message45 = parse(mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createRequest("CAP-45-FULL"))))
                .andExpect(status().isConflict())
                .andReturn()).get("error").get("message").asText();

        String message50 = parse(mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createRequest("CAP-50-FULL-2"))))
                .andExpect(status().isConflict())
                .andReturn()).get("error").get("message").asText();

        assertThat(message45).isEqualTo(message50);                        // từng ký tự giống nhau
        assertThat(message45).doesNotContain("50");                        // chốt chặn Q3 — không hard-code số
        assertThat(message45).isEqualTo("This classroom has reached its capacity.");
    }

    @Test
    @DisplayName("ca 3 — lớp 40/45 (còn chỗ) => 201 (biên hợp lệ)")
    void classroomBelowCapacitySucceeds() throws Exception {
        UUID id = createClassroom("CAP-45-40", 45, false);
        fillClassroom(id, 40);

        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createRequest("CAP-45-40"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("ca 4 — PATCH học sinh sang lớp đầy => 409 STU-010, classroom_id KHÔNG đổi (không ghi nửa vời)")
    void patchToFullClassroomDoesNotMutateStudent() throws Exception {
        UUID fullId = createClassroom("CAP-PATCH-FULL", 45, false);
        fillClassroom(fullId, 45);

        String studentCode = "HS-4A-001";                                  // seed V5
        String beforeClassroomId = jdbc.queryForObject(
                "SELECT classroom_id FROM students WHERE code = ?", String.class, studentCode);

        // 2026-08-02: UpdateStudentRequest.classroomId nhận key JSON "class" (khớp GET), không
        // còn "classroomId" — gửi sai key sẽ bị bỏ qua âm thầm và KHÔNG bao giờ chạm nhánh 409
        // này (đúng bẫy đã bắt được ở đợt nối FE MF03).
        mvc.perform(patch("/api/v1/students/" + studentCode)
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("class", "CAP-PATCH-FULL"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-010"));

        String afterClassroomId = jdbc.queryForObject(
                "SELECT classroom_id FROM students WHERE code = ?", String.class, studentCode);
        assertThat(afterClassroomId).isEqualTo(beforeClassroomId);
    }

    @Test
    @DisplayName("ca 5 — lớp thuộc năm học CLOSED (read_only=true) => 409 STU-193 "
            + "(TDS §10: STU-193 thiếu MSG chính thức — chỉ assert code + HTTP 409)")
    void classroomInClosedAcademicYearIsRejected() throws Exception {
        createClassroom("CAP-CLOSED", 45, true);

        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createRequest("CAP-CLOSED"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-193"));
    }
}
