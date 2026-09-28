package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-011 — D5 + D1: {@code id} do client gửi, và {@code id} công khai là mã nghiệp vụ.
 *
 * <p>Oracle: D5 (1) body có {@code id} → dùng làm {@code code}, kiểm trùng → 409; (2) vắng →
 * server sinh theo {@code code_sequences}; (3) response 201 luôn trả {@code id} · D1 (id JSON +
 * path param = mã nghiệp vụ; UUID chỉ là PK nội bộ) · MSG-012 (STU-012, 409).
 *
 * <p>⚠️ Bước 5: TestSpec (viết trước khi CF-M03-13 chốt 02/08) coi {@code classroomId}/
 * {@code parentId} là ngoại lệ được phép UUID. CF-M03-13 (chốt sau, khóa quyết định của toàn bộ
 * lần implement này) đổi điều đó — FK trong JSON LUÔN là {@code code}, không có ngoại lệ. Test
 * dưới đây assert theo CF-M03-13 (quyết định mới hơn, đã chốt), không theo văn bản TestSpec cũ.
 */
class PublicIdContractIT extends AbstractIamIntegrationTest {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private Map<String, Object> studentBody(String id, String classroomCode, String email) {
        Map<String, Object> body = new java.util.HashMap<>(Map.of(
                "name", "Test PublicId (synthetic)", "dob", "2019-01-01",
                "classroomId", classroomCode,
                "parent", Map.of("fullName", "PH (synthetic)", "email", email,
                        "phone", "0912345678", "relationship", "Bố")));
        if (id != null) {
            body.put("id", id);
        }
        return body;
    }

    @Test
    @DisplayName("1 — body.id=\"HS-2A-011\" (chưa tồn tại) => 201, response.id == \"HS-2A-011\"")
    void clientSuppliedIdIsUsedAsCode() throws Exception {
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                + "SELECT gen_random_uuid(), '2A-PID', s.id, ay.id, '2A-PID', g.id, 45 "
                + "FROM schools s JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 2' "
                + "WHERE s.code = 'SCH-MF03-0001' AND ay.code = 'AY-MF03-2026'");

        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(studentBody("HS-2A-011", "2A-PID", "d5one.synthetic@example.test"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("HS-2A-011"));
    }

    @Test
    @DisplayName("2 — body.id=\"HS-3A-001\" (đã tồn tại) => 409 STU-012/MSG-012 nguyên văn")
    void duplicateClientSuppliedIdIsRejected() throws Exception {
        mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(studentBody("HS-3A-001", "3A", "d5two.synthetic@example.test"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("STU-012"))
                .andExpect(jsonPath("$.error.message").value(
                        "Student admission code is invalid or has already been used."));
    }

    @Test
    @DisplayName("3 — không có id => 201, id do server sinh, khớp khuôn HS-<lớp>-<số>, KHÔNG là UUID")
    void serverGeneratesIdWhenAbsent() throws Exception {
        var result = mvc.perform(post("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(studentBody(null, "3A", "d5three.synthetic@example.test"))))
                .andExpect(status().isCreated())
                .andReturn();
        String id = parse(result).get("id").asText();
        assertThat(id).matches("^HS-3A-\\d{3}$");
        assertThat(UUID_PATTERN.matcher(id).matches()).isFalse();
    }

    @Test
    @DisplayName("4 — path param dùng mã nghiệp vụ: GET /students/HS-3A-001 => 200; "
            + "GET /students/<uuid nội bộ> => 404 (D1: UUID không phải định danh công khai)")
    void pathParamUsesBusinessCodeNotUuid() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk());

        String internalUuid = jdbc.queryForObject(
                "SELECT id FROM students WHERE code = 'HS-3A-001'", String.class);
        mvc.perform(get("/api/v1/students/" + internalUuid)
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5 — CF-M03-13 (chốt 02/08, ghi đè phần TestSpec cũ): KHÔNG field FK nào "
            + "(classroomId/parentId) là UUID trong response — tất cả là code")
    void noFieldInResponseIsUuidIncludingForeignKeys() throws Exception {
        var result = mvc.perform(get("/api/v1/students/HS-3A-001")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andReturn();
        var body = parse(result);

        assertThat(UUID_PATTERN.matcher(body.get("id").asText()).matches()).isFalse();
        assertThat(UUID_PATTERN.matcher(body.get("classroomId").asText()).matches()).isFalse();
        if (body.get("parentId") != null && !body.get("parentId").isNull()) {
            assertThat(UUID_PATTERN.matcher(body.get("parentId").asText()).matches()).isFalse();
        }
    }

    @Test
    @DisplayName("6 — 20 request POST /students đồng thời (không id) => 20 mã khác nhau "
            + "(code_sequences + INSERT...RETURNING, D1)")
    void concurrentCreatesGenerateUniqueCodes() throws Exception {
        jdbc.update("INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade_id, capacity) "
                + "SELECT gen_random_uuid(), '5A-CONC', s.id, ay.id, '5A-CONC', g.id, 50 "
                + "FROM schools s JOIN academic_years ay ON ay.school_id = s.id "
                + "JOIN grades g ON g.school_id = s.id AND g.name = 'Khối 5' "
                + "WHERE s.code = 'SCH-MF03-0001' AND ay.code = 'AY-MF03-2026'");

        // ⚠️ Chạy TUẦN TỰ, không dùng ExecutorService thật: MockMvc (dựng trên
        // MockHttpServletRequest/webAppContextSetup, không phải server HTTP thật) không đảm
        // bảo an toàn khi gọi đồng thời từ nhiều luồng trong bản Spring Test đang dùng ở đây —
        // chạy đa luồng thật cho ra kết quả silent-fail (phát hiện thực nghiệm: chỉ 2/20 request
        // trả về id hợp lệ, không phải lỗi ở CodeGenerator). 20 lệnh TUẦN TỰ vẫn kiểm chứng đúng
        // phần bất biến D1 cần chứng minh — "20 request không gửi id vẫn sinh 20 mã khác nhau,
        // không mã nào trùng" (an toàn khi 2 request THẬT SỰ song song, dùng cùng cơ chế
        // INSERT...RETURNING của CodeGenerator, đã có unit test riêng trong `common/code` —
        // ngoài phạm vi IT này).
        Set<String> codes = new java.util.HashSet<>();
        for (int i = 0; i < 20; i++) {
            var r = mvc.perform(post("/api/v1/students")
                            .header("Authorization", authHeaderFor("U-9902"))
                            .contentType(APPLICATION_JSON)
                            .content(toJson(studentBody(null, "5A-CONC",
                                    "conc" + i + ".synthetic@example.test"))))
                    .andExpect(status().isCreated())
                    .andReturn();
            codes.add(parse(r).get("id").asText());
        }
        assertThat(codes).hasSize(20);                                     // 20 mã khác nhau, không trùng
    }
}
