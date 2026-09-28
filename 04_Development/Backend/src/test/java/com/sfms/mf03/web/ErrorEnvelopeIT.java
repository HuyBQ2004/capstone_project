package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF03-TC-INT-013 — D2 + D7: envelope lỗi đồng nhất trên MỌI error path.
 *
 * <p>Oracle: D2 ({@code { "error": { code, message, details } } }; {@code details} là mảng,
 * KHÔNG BAO GIỜ {@code null}) · D7 (message tiếng Anh, nguyên văn error_message.md) · TDS §10
 * (13 mã STU-* + cột HTTP).
 *
 * <p>Quét 13/14 dòng bảng §10 có thể kích hoạt thật qua HTTP mà KHÔNG cần mock nội bộ
 * (STU-500 đòi hỏi ép {@code DataAccessException} không xử lý được từ ngoài HTTP mà không mock
 * repository — không triển khai trong lớp {@code @SpringBootTest} full-context này; ghi nhận là
 * KHÔNG được phủ, không giả lập).
 */
class ErrorEnvelopeIT extends AbstractIamIntegrationTest {

    /** Regex ký tự có dấu tiếng Việt phổ biến — D7 assert message KHÔNG chứa dấu. */
    private static final Pattern VIETNAMESE_DIACRITICS = Pattern.compile(
            "[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]",
            Pattern.CASE_INSENSITIVE);

    private void assertEnvelope(MvcResult result, String expectedCode, String expectedMessage) throws Exception {
        var body = parse(result);
        assertThat(body.get("error")).isNotNull();
        assertThat(body.get("error").get("code").asText()).isEqualTo(expectedCode);
        if (expectedMessage != null) {
            assertThat(body.get("error").get("message").asText()).isEqualTo(expectedMessage);
        }
        assertThat(body.get("error").has("details")).isTrue();
        assertThat(body.get("error").get("details").isArray()).isTrue();   // mảng, có thể rỗng, KHÔNG null
        assertThat(body.has("message")).isFalse();                         // KHÔNG shape phẳng của mock
        String message = body.get("error").get("message").asText();
        assertThat(VIETNAMESE_DIACRITICS.matcher(message).find())
                .as("message phải là tiếng Anh (D7): %s", message).isFalse();
    }

    @Test
    @DisplayName("STU-004 (401) — không JWT. ⚠️ Xác thực chạy TRƯỚC khi request tới controller "
            + "(Spring Security filter chain), nên mã lỗi thật là IAM-005 (hạ tầng dùng chung "
            + "iam/security/RestAuthEntryPoints — KHÔNG có ngữ cảnh module để trả STU-004; xem "
            + "MF01-TC-SEC-002). Assert theo hành vi THẬT.")
    void stu004NoJwt() throws Exception {
        var result = mvc.perform(get("/api/v1/students")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertEnvelope(result, "IAM-005", "Session expired. Please log in again.");
    }

    @Test
    @DisplayName("STU-005 (403) — Parent chạm HS không liên kết")
    void stu005AccessDenied() throws Exception {
        var result = mvc.perform(get("/api/v1/students/HS-3A-005")
                        .header("Authorization", authHeaderFor("U-9905")))    // U-9905 không liên kết HS-3A-005
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertEnvelope(result, "STU-005",
                "Access denied: You do not have permission to access this resource.");
    }

    @Test
    @DisplayName("STU-010 (409) — lớp đầy theo capacity riêng (MSG-142)")
    void stu010ClassroomFull() throws Exception {
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, "
                + "capacity) SELECT gen_random_uuid(), 'ENV-FULL', s.id, ay.id, 'ENV-FULL', g.id, 1 "
                + "FROM schools s JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 3' "
                + "WHERE s.code = 'SCH-MF03-0001' AND ay.code = 'AY-MF03-2026'");
        jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                + "classroom_id, status) SELECT gen_random_uuid(), 'ENV-FULL-01', 'ENV-REG-01', "
                + "'Lấp đầy (synthetic)', '2018-01-01', s.id, c.id, 'ACTIVE' FROM schools s "
                + "JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-0001' AND c.code = 'ENV-FULL'");

        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("name", "x", "dob", "2019-01-01", "classroomId", "ENV-FULL",
                                "parent", Map.of("fullName", "PH", "email", "envfull.synthetic@example.test",
                                        "phone", "0912345678", "relationship", "Bố")))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertEnvelope(result, "STU-010", "This classroom has reached its capacity.");
    }

    @Test
    @DisplayName("STU-012 (409) — id trùng khi POST /students")
    void stu012DuplicateCode() throws Exception {
        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("id", "HS-3A-001", "name", "x", "dob", "2019-01-01",
                                "classroomId", "3A", "parent", Map.of("fullName", "PH",
                                        "email", "stu012.synthetic@example.test", "phone", "0912345678",
                                        "relationship", "Bố")))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertEnvelope(result, "STU-012", "Student admission code is invalid or has already been used.");
    }

    @Test
    @DisplayName("STU-030 (422) — tạo HS không có PH")
    void stu030MissingParent() throws Exception {
        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"dob\":\"2019-01-01\",\"classroomId\":\"3A\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertEnvelope(result, "STU-030", "Each student must be linked to at least one parent account.");
    }

    @Test
    @DisplayName("STU-031 (422) — verify SEVERE thiếu tài liệu")
    void stu031SevereNeedsProof() throws Exception {
        // D1 — `{id}` của URL là MÃ nghiệp vụ (`ALG-2026-nnnn`), không phải khoá UUID.
        String declCode = jdbc.queryForObject(
                "SELECT code FROM allergy_declarations WHERE allergen = 'Đậu nành'", String.class);
        // ALG-007 seed là MODERATE — ép SEVERE + proof_file_id=NULL để đúng oracle STU-031.
        jdbc.update("UPDATE allergy_declarations SET severity = 'SEVERE', status = 'PENDING', "
                + "proof_file_id = NULL WHERE allergen = 'Đậu nành'");
        var result = mvc.perform(post("/api/v1/allergy-declarations/" + declCode + "/verify")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertEnvelope(result, "STU-031", "Severe allergy declaration requires supporting medical documentation.");
    }

    @Test
    @DisplayName("STU-032 (422) — request-info không lý do")
    void stu032ReasonRequired() throws Exception {
        String declId = jdbc.queryForObject(
                "SELECT id FROM allergy_declarations WHERE allergen = 'Sữa bò'", String.class);
        var result = mvc.perform(post("/api/v1/allergy-declarations/" + declId + "/request-info")
                        .header("Authorization", authHeaderFor("U-9903"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"reason\":\"\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertEnvelope(result, "STU-032", "A reason is required for this action.");
    }

    @Test
    @DisplayName("STU-066 (422) — approve Keto => NEEDS_INFO (BR-220)")
    void stu066DietNeedsJustification() throws Exception {
        String studentId = jdbc.queryForObject(
                "SELECT id FROM students WHERE code = 'HS-3A-002'", String.class);
        jdbc.update("INSERT INTO special_meal_requests (id, code, student_id, dietary_type, duration, "
                + "justification, requested_by_user_id, status) VALUES (gen_random_uuid(), 'SMR-2026-8803', ?::uuid, "
                + "'Keto', 'Học kỳ 1', 'Chỉ định bác sĩ (synthetic)', "
                + "(SELECT id FROM user_accounts WHERE code = 'U-9905'), 'PENDING')", studentId);
        // D1 — `{id}` của URL là MÃ nghiệp vụ, không phải khoá UUID.
        String smrId = jdbc.queryForObject(
                "SELECT code FROM special_meal_requests WHERE dietary_type = 'Keto'", String.class);

        var result = mvc.perform(post("/api/v1/special-meals/" + smrId + "/approve")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertEnvelope(result, "STU-066",
                "This dietary type is outside the standard list and requires medical justification "
                        + "before approval.");
    }

    @Test
    @DisplayName("STU-094 (400) — PATCH health-file field lạ")
    void stu094UnknownField() throws Exception {
        var result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/students/HS-3A-001/health-file")
                        .header("Authorization", authHeaderFor("U-9903"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"allergies\":[]}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertEnvelope(result, "STU-094", "This field cannot be edited through this endpoint.");
    }

    @Test
    @DisplayName("STU-172 (405) — PUT /health-history/{id}")
    void stu172AppendOnly() throws Exception {
        String id = jdbc.queryForObject(
                "SELECT id FROM health_history_events ORDER BY at LIMIT 1", String.class);
        var result = mvc.perform(put("/api/v1/health-history/" + id)
                        .header("Authorization", authHeaderFor("U-9903"))
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(405);
        assertEnvelope(result, "STU-172", "Health history is append-only and cannot be modified or deleted.");
    }

    @Test
    @DisplayName("STU-193 (409) — lớp thuộc năm học CLOSED (MSG thiếu chính thức — chỉ assert code + 409)")
    void stu193ClassroomReadOnly() throws Exception {
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, "
                + "capacity, read_only) SELECT gen_random_uuid(), 'ENV-CLOSED', s.id, ay.id, "
                + "'ENV-CLOSED', g.id, 45, true FROM schools s JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 3' "
                + "WHERE s.code = 'SCH-MF03-0001' AND ay.code = 'AY-MF03-2026'");

        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("name", "x", "dob", "2019-01-01", "classroomId", "ENV-CLOSED",
                                "parent", Map.of("fullName", "PH", "email", "envclosed.synthetic@example.test",
                                        "phone", "0912345678", "relationship", "Bố")))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("STU-193");
        assertThat(parse(result).get("error").has("details")).isTrue();
    }

    @Test
    @DisplayName("STU-201 (409) — liên kết HS thứ 6 vào 1 tài khoản PH")
    void stu201ParentLinkLimit() throws Exception {
        String parentId = "44444444-4444-4444-4444-444444444490";
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT ?::uuid, 'U-9990', 'PH đủ 5 HS (synthetic)', "
                + "'ph5.synthetic@example.test', r.id, s.id, 'ACTIVE', true FROM roles r, schools s "
                + "WHERE r.role_key = 'parent' AND s.code = 'SCH-MF03-0001'", parentId);
        for (int i = 0; i < 5; i++) {
            jdbc.update("INSERT INTO students (id, code, registration_no, name, dob, school_id, "
                    + "classroom_id, status) SELECT gen_random_uuid(), ?, ?, 'HS đủ (synthetic)', "
                    + "'2018-01-01', s.id, c.id, 'ACTIVE' FROM schools s "
                    + "JOIN classrooms c ON c.school_id = s.id WHERE s.code = 'SCH-MF03-0001' AND c.code = '3A'",
                    "ENV5-" + i, "ENV5REG-" + i);
            jdbc.update("INSERT INTO student_parents (student_id, parent_user_id) "
                    + "SELECT (SELECT id FROM students WHERE code = ?), ?::uuid", "ENV5-" + i, parentId);
        }

        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("name", "x", "dob", "2019-01-01", "classroomId", "3A",
                                "parent", Map.of("fullName", "PH đủ 5 HS (synthetic)",
                                        "email", "ph5.synthetic@example.test", "phone", "0912345678",
                                        "relationship", "Bố")))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertEnvelope(result, "STU-201",
                "This parent account is already linked to 5 students. School Administration "
                        + "verification is required.");
    }

    @Test
    @DisplayName("STU-404 (404) — studentId không tồn tại")
    void stu404NotFound() throws Exception {
        var result = mvc.perform(get("/api/v1/students/HS-KHONG-CO")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
        assertEnvelope(result, "STU-404", "Resource not found.");
    }
}
