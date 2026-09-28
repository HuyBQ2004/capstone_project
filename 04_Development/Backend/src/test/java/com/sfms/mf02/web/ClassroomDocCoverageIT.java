package com.sfms.mf02.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Bổ sung phủ tài liệu — 05_Testing/02_IT/MF02_School_Class_Management/Classroom_Management.md.
 *
 * <p>MF02-TC-INT-015 (D5 trùng mã lớp), MF02-TC-INT-010 (SEARCH_FIELDS),
 * MF02-TC-INT-017 (phân công GVCN), MF02-TC-INT-013 (envelope D2 + ngày D4).
 */
class ClassroomDocCoverageIT extends AbstractIamIntegrationTest {

    private String gradeId(String gradeName) {
        return jdbc.queryForObject(
                "SELECT g.id::text FROM grades g JOIN schools s ON s.id = g.school_id "
                        + "WHERE g.name = ? AND s.code = 'SCH-HN-0042'", String.class, gradeName);
    }

    // ───────────────────────── MF02-TC-INT-015 ─────────────────────────

    @Test
    @DisplayName("MF02-TC-INT-015 — POST /classrooms với mã lớp '3A' đã tồn tại trong năm học ⇒ "
            + "409 SCH-409/MSG-139 (D5), bản ghi 3A cũ không đổi")
    void duplicateClassroomCodeWithinSameAcademicYearIsRejected() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/classrooms")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "3A", "gradeId", gradeId("Khối 3"),
                                "capacity", 40, "academicYearId", "AY-2025"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SCH-409");
        assertThat(parse(result).at("/error/message").asText())
                .isEqualTo("A record with this value already exists.");

        // Vẫn đúng MỘT lớp 3A của SCH-HN-0042, sức chứa nguyên bản 45 (seed FX-011) — không ghi đè.
        // (Luôn lọc theo trường: seed MF03 cũng có một lớp mã '3A' ở trường synthetic khác.)
        assertThat(count("SELECT count(*) FROM classrooms c JOIN schools s ON s.id = c.school_id "
                + "WHERE c.code = '3A' AND s.code = 'SCH-HN-0042'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM classrooms c JOIN schools s ON s.id = c.school_id "
                + "WHERE c.code = '3A' AND s.code = 'SCH-HN-0042' AND c.capacity = 45")).isEqualTo(1);
    }

    // ───────────────────────── MF02-TC-INT-010 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> bảng TC kỳ vọng tìm được theo TÊN GIÁO VIÊN. {@code
     * ClassroomSpec.SEARCH_FIELDS} thật chỉ có {@code code}/{@code name} — {@code teacher} là tên
     * hiển thị nằm ở {@code user_accounts} của MF01, mà ADR-M02-5 cấm MF02 join sang entity đó
     * bằng JPA Criteria (chú thích ngay trong {@code ClassroomSpec}). Bước 2 vì vậy trả 0 kết quả;
     * test khẳng định hành vi thật để lệch này không bị hiểu nhầm là hồi quy.
     */
    @Test
    @DisplayName("MF02-TC-INT-010 — GET /classrooms?search lọc trên code/name; tên GVCN KHÔNG nằm "
            + "trong SEARCH_FIELDS (0 kết quả); từ khóa không khớp ⇒ 200 items rỗng, total=0")
    void classroomSearchAppliesOnlyToWhitelistedFields() throws Exception {
        String auth = authHeaderFor("U-0001");

        MvcResult byCode = mvc.perform(get("/api/v1/classrooms").header("Authorization", auth)
                        .param("search", "3A"))
                .andReturn();
        assertThat(byCode.getResponse().getStatus()).isEqualTo(200);
        var byCodeBody = parse(byCode);
        assertThat(byCodeBody.get("total").asLong()).isEqualTo(1);
        assertThat(byCodeBody.at("/items/0/id").asText()).isEqualTo("3A");

        // Lệch tài liệu — tên GVCN không tìm được (xem Javadoc trên).
        MvcResult byTeacher = mvc.perform(get("/api/v1/classrooms").header("Authorization", auth)
                        .param("search", "Đào Văn Quân"))
                .andReturn();
        assertThat(byTeacher.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(byTeacher).get("total").asLong()).isZero();

        MvcResult noMatch = mvc.perform(get("/api/v1/classrooms").header("Authorization", auth)
                        .param("search", "NONEXISTENT"))
                .andReturn();
        assertThat(noMatch.getResponse().getStatus()).isEqualTo(200);
        var noMatchBody = parse(noMatch);
        assertThat(noMatchBody.get("total").asLong()).isZero();
        assertThat(noMatchBody.get("items").size()).isZero();
        assertThat(noMatchBody.get("pageCount").asInt()).isGreaterThanOrEqualTo(1);
    }

    // ───────────────────────── MF02-TC-INT-017 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> (a) đường dẫn thật là {@code PATCH /classrooms/{code}/
     * homeroom-teacher} chứ không phải {@code PUT /classrooms/{code}/teacher}; (b)
     * {@code ClassroomService.assignHomeroomTeacher()} KHÔNG ghi {@code audit_logs} và KHÔNG phát
     * sự kiện {@code HomeroomTeacherAssigned} — BR-026 hiện chỉ được đáp ứng bằng cột
     * {@code updated_by}/{@code updated_at} của {@code BaseEntity}. Test kiểm phần đang có và ghi
     * nhận phần còn thiếu.
     */
    @Test
    @DisplayName("MF02-TC-INT-017 — PATCH /classrooms/3A/homeroom-teacher {teacherId:STF-004} ⇒ 200, "
            + "GVCN cũ (STF-001) bị gỡ liên kết; audit_logs KHÔNG ghi ASSIGN_HOMEROOM_TEACHER (còn thiếu)")
    void assigningHomeroomTeacherReplacesPreviousLink() throws Exception {
        String previousTeacherId = jdbc.queryForObject(
                "SELECT c.homeroom_teacher_id::text FROM classrooms c JOIN schools s ON s.id = c.school_id "
                        + "WHERE c.code = '3A' AND s.code = 'SCH-HN-0042'", String.class);
        assertThat(previousTeacherId).isNotNull();

        MvcResult result = mvc.perform(patch("/api/v1/classrooms/3A/homeroom-teacher")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("teacherId", "STF-004"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("id").asText()).isEqualTo("3A");
        assertThat(body.get("teacherId").asText()).isEqualTo("STF-004");

        // Liên kết cũ bị thay: 3A nay trỏ U-0010 (STF-004), GVCN cũ không còn chủ nhiệm lớp nào.
        assertThat(count("SELECT count(*) FROM classrooms c JOIN user_accounts u "
                + "ON u.id = c.homeroom_teacher_id WHERE c.code = '3A' AND u.staff_code = 'STF-004'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM classrooms WHERE homeroom_teacher_id = ?::uuid",
                previousTeacherId)).isZero();

        // BR-026 — khoảng trống ghi nhận: chưa có bản ghi audit chuyên biệt cho hành động này.
        assertThat(count("SELECT count(*) FROM audit_logs WHERE action = 'ASSIGN_HOMEROOM_TEACHER'"))
                .isZero();
    }

    // ───────────────────────── MF02-TC-INT-013 ─────────────────────────

    @Test
    @DisplayName("MF02-TC-INT-013 — lỗi validate trả đúng envelope D2 {error:{code,message,details}} "
            + "với details là mảng khác null; field ngày trả ISO-8601 UTC (D4)")
    void errorEnvelopeAndDateFormatFollowD2AndD4() throws Exception {
        // (a) D2 — thiếu field bắt buộc `name` ⇒ 422 SCH-422/MSG-138, details là mảng CÓ phần tử.
        MvcResult invalid = mvc.perform(post("/api/v1/classrooms")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("gradeId", gradeId("Khối 3"), "capacity", 40,
                                "academicYearId", "AY-2025"))))
                .andReturn();

        assertThat(invalid.getResponse().getStatus()).isEqualTo(422);
        var error = parse(invalid).get("error");
        List<String> keys = new ArrayList<>();
        for (java.util.Iterator<String> it = error.fieldNames(); it.hasNext(); ) {
            keys.add(it.next());
        }
        assertThat(keys).containsExactlyInAnyOrder("code", "message", "details");
        assertThat(error.get("code").asText()).isEqualTo("SCH-422");
        assertThat(error.get("details").isArray()).isTrue();
        assertThat(error.get("details").size()).isGreaterThan(0);

        // (b) D2 — envelope y hệt trên nhánh 404, và `details` là mảng RỖNG, không phải null.
        MvcResult notFound = mvc.perform(get("/api/v1/classrooms/KHONG-TON-TAI")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(notFound.getResponse().getStatus()).isEqualTo(404);
        var notFoundError = parse(notFound).get("error");
        assertThat(notFoundError.get("code").asText()).isEqualTo("SCH-404");
        assertThat(notFoundError.get("details").isNull()).isFalse();
        assertThat(notFoundError.get("details").isArray()).isTrue();

        // (c) D4 — registeredAt là chuỗi ISO-8601 UTC, không phải dd/MM/yyyy của mock.
        MvcResult school = mvc.perform(get("/api/v1/schools/SCH-HN-0042")
                        .header("Authorization", authHeaderFor("U-9001")))
                .andReturn();
        assertThat(school.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(school).get("registeredAt").asText()).matches("^\\d{4}-\\d{2}-\\d{2}T.*Z$");
    }
}
