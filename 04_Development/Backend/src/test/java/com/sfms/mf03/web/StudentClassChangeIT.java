package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-006 — C6: tên/lớp luôn join theo {@code studentId}, không theo chuỗi lưu.
 *
 * <p>Oracle: ràng buộc C6 {@code data_model.puml} "Truy xuất nối bằng KHOÁ, không bằng tên" ·
 * luật {@code HOC_SINH_SAI_LOP} + {@code TEN_KHONG_KHOP_HOC_SINH} (semantic-audit.ts §6) ·
 * MSG-029 "Profile updated successfully."
 *
 * <p>⚠️ Bước 4 của TestSpec gốc đối chiếu dữ liệu MF09 ({@code student_distributions}) — MF09
 * KHÔNG tồn tại trong lần implement này (module khác, chưa build). Bước đó BỊ CHẶN, không viết
 * test cho module không tồn tại. Các bước còn lại (1/2/3/5 — chứng minh {@code class}/{@code name}
 * là DẪN XUẤT qua join, không phải cột lưu) implement đầy đủ, là phần cốt lõi của C6 trong phạm
 * vi MF03 tự thân.
 */
class StudentClassChangeIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("1-2 — PATCH classroomId => 200 MSG-029; GET sau đó trả class MỚI (dẫn xuất qua join "
            + "classroom_id, KHÔNG qua chuỗi lưu)")
    void classroomChangeIsReflectedViaJoinNotStoredString() throws Exception {
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                + "SELECT gen_random_uuid(), '3B-C6', s.id, ay.id, '3B-C6', g.id, 45 FROM schools s "
                + "JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 3' "
                + "WHERE s.code = 'SCH-MF03-0001' AND ay.code = 'AY-MF03-2026'");

        // 2026-08-02: UpdateStudentRequest.classroomId nhận key JSON "class" (khớp GET), không
        // còn "classroomId" — bẫy mất dữ liệu thầm lặng đã bắt được ở đợt nối FE MF03.
        mvc.perform(patch("/api/v1/students/HS-3A-002")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("class", "3B-C6"))))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/students/HS-3A-002")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.class").value("3B-C6"));

        // DB — classroom_id thật sự đổi (không phải chỉ response bịa ra).
        String classroomCode = jdbc.queryForObject(
                "SELECT c.code FROM students s JOIN classrooms c ON c.id = s.classroom_id "
                        + "WHERE s.code = 'HS-3A-002'", String.class);
        assertThat(classroomCode).isEqualTo("3B-C6");
    }

    @Test
    @DisplayName("3 — PATCH name => GET trả tên MỚI")
    void nameChangeIsReflected() throws Exception {
        mvc.perform(patch("/api/v1/students/HS-3A-002")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Trần Bảo An Mới (synthetic)"))))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/students/HS-3A-002")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trần Bảo An Mới (synthetic)"));
    }

    @Test
    @DisplayName("5 — Assert schema: students KHÔNG có cột class/parent (§14.1 câu 6, ADR-M03-3)")
    void studentsTableHasNoClassOrParentColumn() {
        assertThat(count("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_name = 'students' AND column_name IN ('class', 'parent')")).isZero();
    }
}
