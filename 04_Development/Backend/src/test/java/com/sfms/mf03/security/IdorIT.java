package com.sfms.mf03.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-SEC-009 — IDOR: không tin {@code studentId}/{@code parentId} từ body hay query.
 * CRITICAL · OWASP A01:2021 · CWE-639.
 *
 * <p>Oracle: TDS §17.1 C2 "userId/role/schoolId đọc từ JWT, không tin body/query" · AP-AI-006 ·
 * MSG-005.
 */
class IdorIT extends AbstractIamIntegrationTest {

    /**
     * ID/code cố định, gọi lại từ nhiều {@code @Test} trong cùng class (không rollback giữa các
     * test — {@link AbstractIamIntegrationTest} dùng DB thật, không {@code @Transactional}).
     * {@code ON CONFLICT DO NOTHING} làm hàm idempotent thay vì insert trùng khóa.
     */
    private String createStudentOfAnotherParent() {
        String studentId = "bbbbbbbb-0000-0000-0000-00000000bbbb";
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT gen_random_uuid(), 'U-9907', 'PH khác 2 (synthetic)', "
                + "'phkhac2.synthetic@example.test', r.id, s.id, 'ACTIVE', true FROM roles r, schools s "
                + "WHERE r.role_key = 'parent' AND s.code = 'SCH-MF03-0001' "
                + "ON CONFLICT (school_id, code) DO NOTHING");
        jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                        + "classroom_id, status) SELECT ?::uuid, 'HS-3B-002', 'REG-3B-002', "
                        + "'HS lớp khác (synthetic)', '2018-02-02', s.id, c.id, 'ACTIVE' "
                        + "FROM schools s JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-0001' AND c.code = '3A' "
                        + "ON CONFLICT (school_id, code) DO NOTHING",
                studentId);
        // code 'U-9907' KHÔNG unique toàn cục — V17 (mf09 seed) cũng dùng lại mã này cho
        // trường khác; phải lọc thêm theo school_id, không chỉ theo code (V11/V17/V21 seed).
        jdbc.update("INSERT INTO student_parents (student_id, parent_user_id) SELECT ?::uuid, u.id "
                + "FROM user_accounts u JOIN schools s ON s.id = u.school_id "
                + "WHERE u.code = 'U-9907' AND s.code = 'SCH-MF03-0001' "
                + "ON CONFLICT (student_id, parent_user_id) DO NOTHING", studentId);
        return studentId;
    }

    @Test
    @DisplayName("1 — POST allergy-declarations {studentId: HS của PH khác, declaredByUserId: giả} "
            + "(JWT parent U-9905) => 403; declared_by_user_id KHÔNG lấy từ body")
    void declaredByIsAlwaysFromJwtNotBody() throws Exception {
        createStudentOfAnotherParent();
        mvc.perform(post("/api/v1/allergy-declarations")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"studentId\":\"HS-3B-002\",\"declaredByUserId\":\"11111111-1111-1111-"
                                + "1111-111111111111\",\"category\":\"x\",\"allergen\":\"y\","
                                + "\"severity\":\"MILD\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }

    @Test
    @DisplayName("2 — POST special-meals {studentId: HS của PH khác} (JWT parent U-9905) => 403")
    void cannotSubmitSpecialMealForOtherParentsStudent() throws Exception {
        createStudentOfAnotherParent();
        mvc.perform(post("/api/v1/special-meals")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("studentId", "HS-3B-002", "dietaryType", "Halal",
                                "duration", "1 kỳ", "justification", "x"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }

    @Test
    @DisplayName("3 — PATCH allergy-declarations/{id} của HS thuộc PH khác (JWT parent U-9905) => 403")
    void cannotResubmitOtherParentsDeclaration() throws Exception {
        String otherStudentId = createStudentOfAnotherParent();
        // code 'U-9907' KHÔNG unique toàn cục (V17 mf09 seed dùng lại) — lọc thêm school_id.
        // `code` NOT NULL từ V71 (D1); `{id}` của URL nay là MÃ nghiệp vụ, không phải khoá UUID.
        jdbc.update("INSERT INTO allergy_declarations (id, code, student_id, category, allergen, severity, "
                + "declared_by_user_id, status) SELECT 'cccccccc-0000-0000-0000-0000000000cc'::uuid, "
                + "'ALG-2026-8801', ?::uuid, 'x', 'y', 'MILD', u.id, 'NEEDS_INFO' "
                + "FROM user_accounts u JOIN schools s ON s.id = u.school_id "
                + "WHERE u.code = 'U-9907' AND s.code = 'SCH-MF03-0001' "
                + "ON CONFLICT (id) DO NOTHING", otherStudentId);

        mvc.perform(patch("/api/v1/allergy-declarations/ALG-2026-8801")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("studentId", "HS-3B-002", "category", "x", "allergen", "z",
                                "severity", "MILD"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }

    @Test
    @DisplayName("4 — POST /students {schoolId: trường B} (JWT school-admin trường A) => "
            + "schoolId trong body bị bỏ qua (DTO không khai field này); HS tạo ở TRƯỜNG A")
    void schoolIdInBodyHasNoField() throws Exception {
        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"dob\":\"2019-01-01\",\"classroomId\":\"3A\","
                                + "\"schoolId\":\"SCH-KHONG-LIEN-QUAN\","
                                + "\"parent\":{\"fullName\":\"PH\",\"email\":\"idor4.synthetic@example.test\","
                                + "\"phone\":\"0912345678\",\"relationship\":\"Bố\"}}"))
                .andExpect(status().isCreated())
                .andReturn();
        String newId = parse(result).get("id").asText();
        String schoolCode = jdbc.queryForObject(
                "SELECT s.code FROM students st JOIN schools s ON s.id = st.school_id WHERE st.code = ?",
                String.class, newId);
        assertThat(schoolCode).isEqualTo("SCH-MF03-0001");
    }

    @Test
    @DisplayName("5 — POST verify {verifiedByUserId: user khác} (JWT medical-staff U-9903) => "
            + "verified_by_user_id = user TRONG JWT, không phải body")
    void verifiedByIsAlwaysFromJwtNotBody() throws Exception {
        // D1 — `{id}` của URL là MÃ nghiệp vụ; khoá UUID chỉ dùng để assert lại trong DB.
        String declCode = jdbc.queryForObject(
                "SELECT code FROM allergy_declarations WHERE allergen = 'Sữa bò'", String.class);
        String declId = jdbc.queryForObject(
                "SELECT id FROM allergy_declarations WHERE allergen = 'Sữa bò'", String.class);
        jdbc.update("UPDATE allergy_declarations SET severity = 'MILD' WHERE id = ?::uuid", declId);

        mvc.perform(post("/api/v1/allergy-declarations/" + declCode + "/verify")
                        .header("Authorization", authHeaderFor("U-9903"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"verifiedByUserId\":\"11111111-1111-1111-1111-111111111111\"}"))
                .andExpect(status().isOk());

        String actualVerifierId = jdbc.queryForObject(
                "SELECT verified_by_user_id FROM allergy_declarations WHERE id = ?::uuid", String.class, declId);
        String medicalStaffId = jdbc.queryForObject(
                "SELECT id FROM user_accounts WHERE code = 'U-9903'", String.class);
        assertThat(actualVerifierId).isEqualTo(medicalStaffId);
        assertThat(actualVerifierId).isNotEqualTo("11111111-1111-1111-1111-111111111111");
    }

    @Test
    @DisplayName("6 — GET health-history?studentId=<mã HS của PH khác> (JWT parent U-9905) => 403")
    void cannotReadOtherParentsHistory() throws Exception {
        // 2026-08-02: studentId nhận mã nghiệp vụ (D1) — "HS-3B-002" là code cố định do
        // createStudentOfAnotherParent() tạo (không phải otherStudentId, đó là UUID nội bộ).
        createStudentOfAnotherParent();
        mvc.perform(get("/api/v1/health-history?studentId=HS-3B-002")
                        .header("Authorization", authHeaderFor("U-9905")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7 — ca hợp lệ: Parent sửa khai báo NEEDS_INFO CỦA CON MÌNH (HS-3A-001, thật sự "
            + "liên kết U-9905) => 200 (không chặn oan)")
    void parentCanResubmitOwnChildsDeclaration() throws Exception {
        String studentId = jdbc.queryForObject(
                "SELECT id FROM students WHERE code = 'HS-3A-001'", String.class);
        jdbc.update("INSERT INTO allergy_declarations (id, code, student_id, category, allergen, severity, "
                + "declared_by_user_id, status) VALUES ('dddddddd-0000-0000-0000-0000000000dd'::uuid, "
                + "'ALG-2026-8802', ?::uuid, 'Đậu & hạt', 'Lạc', 'MODERATE', "
                + "(SELECT id FROM user_accounts WHERE code = 'U-9905'), 'NEEDS_INFO')", studentId);

        mvc.perform(patch("/api/v1/allergy-declarations/ALG-2026-8802")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("studentId", "HS-3A-001", "category", "Đậu & hạt",
                                "allergen", "Lạc", "severity", "MODERATE"))))
                .andExpect(status().isOk());
    }
}
