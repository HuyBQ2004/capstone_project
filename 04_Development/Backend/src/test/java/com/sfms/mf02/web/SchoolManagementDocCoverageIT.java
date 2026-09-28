package com.sfms.mf02.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Bổ sung phủ tài liệu — 05_Testing/02_IT/MF02_School_Class_Management/School_Management.md.
 *
 * <p>MF02-TC-INT-012 (đăng ký trường + D4), MF02-TC-INT-017 (BR-021 cascade khi INACTIVE),
 * MF02-TC-INT-018 (BR-028 geofence [50,500]).
 */
class SchoolManagementDocCoverageIT extends AbstractIamIntegrationTest {

    /**
     * {@code POST /schools/register} nhận {@code @RequestPart("payload")}, KHÔNG phải JSON thuần.
     *
     * <p>{@code X-Forwarded-For} riêng cho lớp test này: {@link com.sfms.mf02.web.RegisterRateLimiter}
     * là singleton in-process, số đếm 5/min/IP KHÔNG bị xoá bởi clean-schema {@code @BeforeEach} —
     * dùng chung "IP" mặc định với các lớp test khác sẽ flaky theo thứ tự chạy.
     */
    private RequestBuilder registerRequest(Map<String, Object> body) {
        MockMultipartFile payload = new MockMultipartFile("payload", "", MediaType.APPLICATION_JSON_VALUE,
                toJson(body).getBytes(StandardCharsets.UTF_8));
        return multipart("/api/v1/schools/register").file(payload)
                .header("X-Forwarded-For", "203.0.113.42");
    }

    // ───────────────────────── MF02-TC-INT-012 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> {@code SchoolResponse} không có field {@code createdAt} — chỉ
     * {@code registeredAt} ra ngoài (cột {@code created_at} của {@code BaseEntity} là dữ liệu
     * audit nội bộ, không lộ qua API). Phần D4 vì vậy kiểm trên {@code registeredAt}.
     */
    @Test
    @DisplayName("MF02-TC-INT-012 — POST /schools/register (public) ⇒ 201, status khởi tạo PENDING, "
            + "registeredAt định dạng ISO-8601 UTC (D4); coordinateLocked=false vì chưa được duyệt")
    void registerSchoolHappyPathInitialisesPendingWithIsoTimestamp() throws Exception {
        MvcResult result = mvc.perform(registerRequest(Map.of(
                        "id", "SCH-HN-0099", "name", "TH Le Quy Don", "taxId", "0109999888",
                        "email", "lequydon@test.edu.vn", "address", "Ha Noi",
                        "lat", 21.020000, "lng", 105.780000)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        var body = parse(result);
        assertThat(body.get("id").asText()).isEqualTo("SCH-HN-0099");
        assertThat(body.get("status").asText()).isEqualTo("PENDING");
        assertThat(body.get("coordinateLocked").asBoolean()).isFalse();
        assertThat(body.get("registeredAt").asText()).matches("^\\d{4}-\\d{2}-\\d{2}T.*Z$");
        assertThat(body.has("createdAt")).isFalse();   // xem Javadoc — lệch tài liệu

        assertThat(count("SELECT count(*) FROM schools WHERE code = 'SCH-HN-0099' AND status = 'PENDING'"))
                .isEqualTo(1);
    }

    // ───────────────────────── MF02-TC-INT-017 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> (a) thân request thật là {@code {"status":"INACTIVE"}}
     * ({@code ChangeSchoolStatusRequest}), không phải {@code {"active": false}}; (b) MF02 KHÔNG
     * phát sự kiện miền {@code SchoolDeactivated} — {@code SchoolService.changeStatus()} tự đóng
     * băng lớp học trong cùng transaction; (c) không có bản ghi {@code audit_logs} riêng cho hành
     * động này. Test kiểm phần cascade đang thật sự chạy.
     */
    @Test
    @DisplayName("MF02-TC-INT-017 — PATCH /schools/SCH-HN-0042/status {status:INACTIVE} ⇒ 200 và "
            + "BR-021 cascade: toàn bộ lớp của trường chuyển read_only=true, dữ liệu KHÔNG bị xóa")
    void deactivatingSchoolCascadesReadOnlyToItsClassrooms() throws Exception {
        long classroomsBefore = count("SELECT count(*) FROM classrooms c JOIN schools s "
                + "ON s.id = c.school_id WHERE s.code = 'SCH-HN-0042'");
        assertThat(classroomsBefore).isGreaterThan(0);

        MvcResult result = mvc.perform(patch("/api/v1/schools/SCH-HN-0042/status")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "INACTIVE"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).get("status").asText()).isEqualTo("INACTIVE");

        assertThat(count("SELECT count(*) FROM schools WHERE code = 'SCH-HN-0042' AND status = 'INACTIVE'"))
                .isEqualTo(1);
        long readOnlyAfter = count("SELECT count(*) FROM classrooms c JOIN schools s "
                + "ON s.id = c.school_id WHERE s.code = 'SCH-HN-0042' AND c.read_only = true");
        assertThat(readOnlyAfter).isEqualTo(classroomsBefore);
        // "Tạm dừng", KHÔNG xóa — số lớp giữ nguyên.
        assertThat(count("SELECT count(*) FROM classrooms c JOIN schools s "
                + "ON s.id = c.school_id WHERE s.code = 'SCH-HN-0042'")).isEqualTo(classroomsBefore);
    }

    // ───────────────────────── MF02-TC-INT-018 ─────────────────────────

    /**
     * <b>Lệch so với tài liệu:</b> (a) method thật là {@code PATCH}, không phải {@code PUT};
     * (b) giá trị ngoài dải bị chặn bởi bean validation {@code @Min(50) @Max(500)} nên trả
     * <b>422 SCH-422/MSG-138</b> (envelope D2 kèm {@code details}), không phải 400 SCH-013/MSG-107
     * — mã đó chỉ dùng cho lat/lng, được validate thủ công trong {@code SchoolService}.
     */
    @Test
    @DisplayName("MF02-TC-INT-018 — PATCH /schools/SCH-HN-0042/config geofenceRadius=300 ⇒ 200; "
            + "geofenceRadius=30 (<50) ⇒ 422 SCH-422 và giá trị cũ giữ nguyên (BR-028)")
    void geofenceRadiusIsAcceptedInRangeAndRejectedBelowFloor() throws Exception {
        String auth = authHeaderFor("U-0001");

        MvcResult ok = mvc.perform(patch("/api/v1/schools/SCH-HN-0042/config")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("geofenceRadius", 300))))
                .andReturn();
        assertThat(ok.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(ok).get("geofenceRadius").asInt()).isEqualTo(300);
        assertThat(count("SELECT count(*) FROM school_config sc JOIN schools s ON s.id = sc.school_id "
                + "WHERE s.code = 'SCH-HN-0042' AND sc.geofence_radius = 300")).isEqualTo(1);

        MvcResult tooSmall = mvc.perform(patch("/api/v1/schools/SCH-HN-0042/config")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("geofenceRadius", 30))))
                .andReturn();
        assertThat(tooSmall.getResponse().getStatus()).isEqualTo(422);
        var error = parse(tooSmall).get("error");
        assertThat(error.get("code").asText()).isEqualTo("SCH-422");
        assertThat(error.get("details").isArray()).isTrue();

        // Giá trị hợp lệ trước đó không bị ghi đè bởi request bị từ chối.
        assertThat(count("SELECT count(*) FROM school_config sc JOIN schools s ON s.id = sc.school_id "
                + "WHERE s.code = 'SCH-HN-0042' AND sc.geofence_radius = 300")).isEqualTo(1);
    }
}
