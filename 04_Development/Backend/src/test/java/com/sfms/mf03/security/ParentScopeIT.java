package com.sfms.mf03.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
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
 * MF03-TC-SEC-001 — Phạm vi dữ liệu của Parent (chỉ con mình). CRITICAL · OWASP A01:2021 ·
 * CWE-639. Legal: BR-173 (PDPD) — lộ hồ sơ sức khỏe trẻ em cho phụ huynh khác là sự cố dữ liệu
 * cá nhân.
 *
 * <p>Oracle: BR-030 (quan hệ HS↔PH là cơ sở phạm vi) · TDS §16 cột PARENT = Own · MSG-005.
 *
 * <p>⚠️ Bước 3/4 (GET /parents/{id}/students) và bước 7 (GET /health-documents/{id}/content)
 * BỊ CHẶN — 2 endpoint đó chưa tồn tại trong lần implement này (ngoài phạm vi đợt này theo chỉ
 * đạo). Không viết test cho endpoint không tồn tại; các bước còn lại implement đầy đủ với
 * assertion phạm vi dữ liệu THẬT (không chỉ status code).
 */
class ParentScopeIT extends AbstractIamIntegrationTest {

    /**
     * Học sinh KHÔNG thuộc U-9905 — tạo riêng, phụ huynh khác. ID/code cố định, gọi lại từ nhiều
     * {@code @Test} trong cùng class (không rollback giữa các test — {@link
     * AbstractIamIntegrationTest} dùng DB thật, không {@code @Transactional}). {@code ON CONFLICT
     * DO NOTHING} làm hàm idempotent thay vì insert trùng khóa.
     */
    private String createStudentOfAnotherParent() {
        String studentId = "aaaaaaaa-0000-0000-0000-00000000aaaa";
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT gen_random_uuid(), 'U-9906', 'PH khác (synthetic)', "
                + "'phkhac.synthetic@example.test', r.id, s.id, 'ACTIVE', true FROM roles r, schools s "
                + "WHERE r.role_key = 'parent' AND s.code = 'SCH-MF03-0001' "
                + "ON CONFLICT (school_id, code) DO NOTHING");
        jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                        + "classroom_id, status) SELECT ?::uuid, 'HS-3A-999', 'REG-3A-999', "
                        + "'Lý Gia Bảo (synthetic)', '2018-02-02', s.id, c.id, 'ACTIVE' "
                        + "FROM schools s JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-0001' AND c.code = '3A' "
                        + "ON CONFLICT (school_id, code) DO NOTHING",
                studentId);
        jdbc.update("INSERT INTO student_parents (student_id, parent_user_id) SELECT ?::uuid, "
                + "(SELECT id FROM user_accounts WHERE code = 'U-9906') "
                + "ON CONFLICT (student_id, parent_user_id) DO NOTHING", studentId);
        return studentId;
    }

    @Test
    @DisplayName("1 — GET /students/{id} của HS thuộc PH khác => 403 STU-005/MSG-005")
    void cannotReadOtherParentsStudentDetail() throws Exception {
        createStudentOfAnotherParent();
        mvc.perform(get("/api/v1/students/HS-3A-999")
                        .header("Authorization", authHeaderFor("U-9905")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"))
                .andExpect(jsonPath("$.error.message").value(
                        "Access denied: You do not have permission to access this resource."));
    }

    @Test
    @DisplayName("2 — POST /allergy-declarations cho HS thuộc PH khác => 403; 0 dòng được ghi")
    void cannotDeclareAllergyForOtherParentsStudent() throws Exception {
        createStudentOfAnotherParent();
        long before = count("SELECT COUNT(*) FROM allergy_declarations");

        mvc.perform(post("/api/v1/allergy-declarations")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("studentId", "HS-3A-999", "category", "Hải sản",
                                "allergen", "Cua", "severity", "MILD"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));

        assertThat(count("SELECT COUNT(*) FROM allergy_declarations")).isEqualTo(before);
    }

    @Test
    @DisplayName("5 — GET /students (JWT parent) chỉ trả HS đã liên kết; total==2, "
            + "KHÔNG rò tổng số HS toàn trường (assert DỮ LIỆU thật, không chỉ status)")
    void listOnlyReturnsLinkedChildren() throws Exception {
        createStudentOfAnotherParent();                                    // +1 HS ngoài phạm vi trong trường

        var result = mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9905")))
                .andExpect(status().isOk())
                .andReturn();
        var body = parse(result);

        assertThat(body.get("total").asInt()).isEqualTo(2);                // HS-3A-001 + HS-4A-001 (V5)
        var codes = new java.util.HashSet<String>();
        for (var item : body.get("items")) {
            codes.add(item.get("id").asText());
        }
        assertThat(codes).containsExactlyInAnyOrder("HS-3A-001", "HS-4A-001");
        assertThat(codes).doesNotContain("HS-3A-999");                     // KHÔNG rò HS của PH khác

        long totalStudentsInSchool = count("SELECT COUNT(*) FROM students WHERE school_id = "
                + "(SELECT id FROM schools WHERE code = 'SCH-MF03-0001')");
        assertThat(body.get("total").asInt()).isLessThan((int) totalStudentsInSchool); // không rò tổng trường
    }

    @Test
    @DisplayName("6 — GET /health-history?studentId=<mã HS của PH khác> => 403")
    void cannotReadOtherParentsStudentHistory() throws Exception {
        // 2026-08-02: studentId nhận mã nghiệp vụ (D1) — "HS-3A-999" là code cố định do
        // createStudentOfAnotherParent() tạo (không phải otherStudentId, đó là UUID nội bộ).
        createStudentOfAnotherParent();
        mvc.perform(get("/api/v1/health-history?studentId=HS-3A-999")
                        .header("Authorization", authHeaderFor("U-9905")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }
}
