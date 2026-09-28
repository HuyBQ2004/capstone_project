package com.sfms.mf02.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf02.dto.ClassroomDtos.CreateClassroomRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Bổ sung phủ tài liệu — 05_Testing/02_IT/MF02_School_Class_Management/Security_Access_Control.md.
 *
 * <p>MF02-TC-SEC-003 (401 khi thiếu/hết hạn JWT), MF02-TC-SEC-004 (phạm vi trường lấy từ JWT),
 * MF02-TC-SEC-006 (BR-028 bắt buộc lý do khi unlock), MF02-TC-SEC-008 (Supplier/Parent),
 * MF02-TC-SEC-010 (chống SQL injection ở tham số search).
 */
class Mf02AccessControlDocCoverageIT extends AbstractIamIntegrationTest {

    // ───────────────────────── MF02-TC-SEC-003 ─────────────────────────

    @Test
    @DisplayName("MF02-TC-SEC-003 — GET /classrooms không có Authorization ⇒ 401 IAM-005/MSG-004; "
            + "JWT đã hết hạn cũng 401, không tự gia hạn")
    void missingAndExpiredJwtAreBothRejectedWith401() throws Exception {
        MvcResult noHeader = mvc.perform(get("/api/v1/classrooms")).andReturn();
        assertThat(noHeader.getResponse().getStatus()).isEqualTo(401);
        assertThat(parse(noHeader).at("/error/code").asText()).isEqualTo("IAM-005");
        assertThat(parse(noHeader).at("/error/message").asText())
                .isEqualTo("Session expired. Please log in again.");
        assertThat(noHeader.getResponse().getContentAsString()).doesNotContain("\"items\"");

        // Token thật, còn hạn ⇒ 200 (assertion dương: 401 ở trên đúng là do thiếu token).
        String token = login("toanpq@dtd.edu.vn", PWD).get("accessToken").asText();
        assertThat(mvc.perform(get("/api/v1/classrooms").header("Authorization", bearer(token)))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        // Vượt qua mốc hết hạn phiên mặc định (30') ⇒ chính token đó thành 401.
        clock.advance(Duration.ofSeconds(30 * 60 + 1));
        MvcResult expired = mvc.perform(get("/api/v1/classrooms")
                        .header("Authorization", bearer(token)))
                .andReturn();
        assertThat(expired.getResponse().getStatus()).isEqualTo(401);
        assertThat(parse(expired).at("/error/code").asText()).isEqualTo("IAM-005");
    }

    // ───────────────────────── MF02-TC-SEC-004 ─────────────────────────

    @Test
    @DisplayName("MF02-TC-SEC-004 — phạm vi trường lấy từ claim JWT: CreateClassroomRequest KHÔNG có "
            + "field schoolId; School Admin trường B tạo lớp ⇒ luôn thuộc SCH-HN-0051, trỏ năm học "
            + "của trường A ⇒ 404 SCH-404")
    void schoolScopeComesFromJwtNotFromPayload() throws Exception {
        // Không có đường nào để client khai schoolId — DTO không nhận field đó (chặn từ gốc CWE-639).
        assertThat(Arrays.stream(CreateClassroomRequest.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName))
                .doesNotContain("schoolId");

        String schoolBGradeId = jdbc.queryForObject(
                "SELECT g.id::text FROM grades g JOIN schools s ON s.id = g.school_id "
                        + "WHERE g.name = 'Khối 1' AND s.code = 'SCH-HN-0051'", String.class);
        String authB = authHeaderFor("U-0018");   // School Admin của SCH-HN-0051

        MvcResult created = mvc.perform(post("/api/v1/classrooms")
                        .header("Authorization", authB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "1Z", "gradeId", schoolBGradeId,
                                "capacity", 40, "academicYearId", "AY-2025-B"))))
                .andReturn();
        assertThat(created.getResponse().getStatus()).isEqualTo(201);
        assertThat(count("SELECT count(*) FROM classrooms c JOIN schools s ON s.id = c.school_id "
                + "WHERE c.code = '1Z' AND s.code = 'SCH-HN-0051'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM classrooms c JOIN schools s ON s.id = c.school_id "
                + "WHERE c.code = '1Z' AND s.code = 'SCH-HN-0042'")).isZero();

        // Trỏ tới năm học của trường A — tra cứu luôn kèm schoolId của JWT ⇒ 404, không tạo được.
        MvcResult crossSchool = mvc.perform(post("/api/v1/classrooms")
                        .header("Authorization", authB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "1Y", "gradeId", schoolBGradeId,
                                "capacity", 40, "academicYearId", "AY-2025"))))
                .andReturn();
        assertThat(crossSchool.getResponse().getStatus()).isEqualTo(404);
        assertThat(parse(crossSchool).at("/error/code").asText()).isEqualTo("SCH-404");
        assertThat(count("SELECT count(*) FROM classrooms WHERE code = '1Y'")).isZero();
    }

    // ───────────────────────── MF02-TC-SEC-006 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> bảng TC ghi mã {@code IAM-422}. Mã thật do
     * {@code ModuleCodeResolver} suy từ segment đầu của path ({@code schools} ⇒ {@code SCH}) nên là
     * <b>SCH-422</b>; thông điệp vẫn là MSG-138 dùng chung.
     */
    @Test
    @DisplayName("MF02-TC-SEC-006 — System Admin POST /schools/{code}/coordinate/unlock với thân rỗng "
            + "⇒ 422 SCH-422/MSG-138 (lý do văn bản là bắt buộc, BR-028); tọa độ vẫn khóa")
    void unlockCoordinateRequiresWrittenJustificationEvenForSystemAdmin() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/schools/SCH-HN-0042/coordinate/unlock")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        var error = parse(result).get("error");
        assertThat(error.get("code").asText()).isEqualTo("SCH-422");
        assertThat(error.get("message").asText())
                .isEqualTo("Validation failed: one or more required fields are missing or invalid.");
        assertThat(error.get("details").isArray()).isTrue();

        // Không mở khóa: bản ghi vẫn coordinate_locked = true.
        assertThat(count("SELECT count(*) FROM schools WHERE code = 'SCH-HN-0042' "
                + "AND coordinate_locked = true")).isEqualTo(1);
    }

    // ───────────────────────── MF02-TC-SEC-008 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> (a) mã 403 của MF02 là {@code SCH-005} (tiền tố suy theo path),
     * không phải {@code IAM-004}; (b) {@code GET /schools} CỐ Ý mở cho Supplier — bản rút gọn
     * id/name/status, ép ACTIVE-only (chốt 2026-08-02, xem Javadoc {@code SchoolController.list}) —
     * nên Supplier nhận 200 chứ không phải 403. Parent vẫn bị chặn ở cả ba nhóm endpoint.
     */
    @Test
    @DisplayName("MF02-TC-SEC-008 — Parent bị 403 SCH-005 ở /classrooms, /academic-years, /schools; "
            + "Supplier 403 ở /classrooms và /academic-years nhưng 200 ở /schools (danh bạ rút gọn)")
    void externalSupplierAndParentRolesAreRejected() throws Exception {
        String parent = authHeaderFor("U-0005");
        for (String path : new String[]{"/api/v1/classrooms", "/api/v1/academic-years", "/api/v1/schools"}) {
            MvcResult result = mvc.perform(get(path).header("Authorization", parent)).andReturn();
            assertThat(result.getResponse().getStatus()).as("parent %s", path).isEqualTo(403);
            assertThat(parse(result).at("/error/code").asText()).isEqualTo("SCH-005");
        }

        String supplier = authHeaderFor("U-0006");
        for (String path : new String[]{"/api/v1/classrooms", "/api/v1/academic-years"}) {
            MvcResult result = mvc.perform(get(path).header("Authorization", supplier)).andReturn();
            assertThat(result.getResponse().getStatus()).as("supplier %s", path).isEqualTo(403);
            assertThat(parse(result).at("/error/code").asText()).isEqualTo("SCH-005");
        }

        MvcResult directory = mvc.perform(get("/api/v1/schools").header("Authorization", supplier)).andReturn();
        assertThat(directory.getResponse().getStatus()).isEqualTo(200);
        // Bản rút gọn — KHÔNG lộ taxId/tọa độ cho vai ngoài trường.
        assertThat(directory.getResponse().getContentAsString()).doesNotContain("taxId");
    }

    // ───────────────────────── MF02-TC-SEC-010 ─────────────────────────

    @Test
    @DisplayName("MF02-TC-SEC-010 — payload SQL injection ở ?search của /classrooms và /schools ⇒ "
            + "200, 0 kết quả, KHÔNG 500 và bảng dữ liệu còn nguyên (JPA Specification tham số hóa)")
    void sqlInjectionPayloadsInSearchAreHandledSafely() throws Exception {
        long classroomsBefore = count("SELECT count(*) FROM classrooms");
        long schoolsBefore = count("SELECT count(*) FROM schools");

        String[] payloads = {"' OR 1=1 --", "'; DROP TABLE classrooms; --", "%", "_", "\\"};

        for (String payload : payloads) {
            MvcResult classrooms = mvc.perform(get("/api/v1/classrooms")
                            .header("Authorization", authHeaderFor("U-0001"))
                            .param("search", payload))
                    .andReturn();
            assertThat(classrooms.getResponse().getStatus()).as("classrooms search=%s", payload).isEqualTo(200);
            // `%`/`_` được escape ⇒ là ký tự thường, KHÔNG phải wildcard trả cả bảng.
            assertThat(parse(classrooms).get("total").asLong()).as("classrooms search=%s", payload).isZero();

            MvcResult schools = mvc.perform(get("/api/v1/schools")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .param("search", payload))
                    .andReturn();
            assertThat(schools.getResponse().getStatus()).as("schools search=%s", payload).isEqualTo(200);
            assertThat(parse(schools).get("total").asLong()).as("schools search=%s", payload).isZero();
        }

        assertThat(count("SELECT count(*) FROM classrooms")).isEqualTo(classroomsBefore);
        assertThat(count("SELECT count(*) FROM schools")).isEqualTo(schoolsBefore);
    }
}
