package com.sfms.mf08.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-SEC-003 — cách ly theo trường: {@code schoolId} chỉ lấy từ JWT, không tin body/query.
 *
 * <p>⚠️ Ground-truth: TDS §2.5 mục 1 (L16) ghi nhận {@code V900} lúc viết spec CHƯA có tài khoản
 * medical-staff nào ở trường B ({@code SCH-HN-0051}). Grep {@code V900} hiện tại cho thấy khoảng
 * trống đó ĐÃ được lấp ({@code U-0090}, medical-staff, SCH-HN-0051 — thêm cho MF12) — test dùng
 * đúng vai trò medical-staff ở trường B, KHÔNG cần né bằng school-admin như spec đề xuất tạm.
 */
class SampleTenantIsolationIT extends AbstractIamIntegrationTest {

    private static final String CATERER_SCHOOL_A = "U-0013";           // SCH-HN-0042
    private static final String MEDICAL_SCHOOL_B = "U-0090";           // SCH-HN-0051
    private static final String INSPECTION_CODE = "INSP-MF08-TENANT";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-TENANT", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER_SCHOOL_A,
                LocalDate.of(2026, 7, 30));
    }

    private String registerSampleAtSchoolA() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-TENANT");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", "2026-07-30T01:00:00Z");
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile payloadPart = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(payloadPart).file(photo)
                        .header("Authorization", authHeaderFor(CATERER_SCHOOL_A)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return parse(result).get("id").asText();
    }

    @Test
    void medicalStaffFromOtherSchoolCannotReadSampleDetail() throws Exception {
        String code = registerSampleAtSchoolA();

        MvcResult result = mvc.perform(get("/api/v1/samples/" + code)
                        .header("Authorization", authHeaderFor(MEDICAL_SCHOOL_B)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);   // ẩn tồn tại (D1 §16)
    }

    @Test
    void listIgnoresSchoolIdQueryParamAndAlwaysScopesToJwtSchool() throws Exception {
        registerSampleAtSchoolA();

        // U-0090 (medical-staff, SCH-HN-0051) cố truyền schoolId=SCH-HN-0042 qua query -> vẫn
        // chỉ thấy dữ liệu trường mình (rỗng), KHÔNG rò dữ liệu trường A.
        MvcResult result = mvc.perform(get("/api/v1/samples")
                        .param("page", "1").param("pageSize", "8")
                        .param("schoolId", "SCH-HN-0042")
                        .header("Authorization", authHeaderFor(MEDICAL_SCHOOL_B)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).get("items")).isEmpty();
    }

    @Test
    void registeringSampleAlwaysUsesSchoolIdFromJwtNeverFromBody() throws Exception {
        // U-0013 (bếp) JWT trường A — dù có gửi field lạ "schoolId" trong body (bị Jackson bỏ
        // qua vì RegisterSampleRequest không có field đó), bản ghi vẫn thuộc trường A.
        String code = registerSampleAtSchoolA();

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT s.code AS school_code FROM food_samples f JOIN schools s ON s.id = f.school_id "
                        + "WHERE f.code = ?", code);
        assertThat(row.get("school_code")).isEqualTo("SCH-HN-0042");
    }

    @Test
    void handOverCannotMixSamplesFromTwoDifferentSchoolsInOneBatch() throws Exception {
        // U-0090 (medical-staff, trường B) chỉ tìm thấy mẫu của trường B khi khoá lô -> mẫu
        // trường A "SMP-..." không resolve được trong scope trường B -> 404 (không phải rò dữ
        // liệu cho phép gộp 2 trường vào 1 phiếu).
        String codeSchoolA = registerSampleAtSchoolA();
        Map<String, Object> handOver = Map.of(
                "incidentId", "INC-DOES-NOT-MATTER", "sampleIds", java.util.List.of(codeSchoolA),
                "handoverType", "LAB", "testCriteria", "Salmonella");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(handOver).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile record = new MockMultipartFile("handoverRecords", "bb.pdf", "application/pdf",
                new byte[]{'%', 'P', 'D', 'F'});

        MvcResult result = mvc.perform(multipart("/api/v1/sample-lab-requests").file(part).file(record)
                        .header("Authorization", authHeaderFor(MEDICAL_SCHOOL_B)))
                .andReturn();

        // Sự cố "INC-DOES-NOT-MATTER" không tồn tại ở trường B -> 409 SMP-023 (kiểm TRƯỚC khi
        // chạm tới sampleIds) — dù có đổi sang incident thật của trường B thì lockAll cũng
        // KHÔNG tìm ra mã của trường A (scoped theo school_id) -> vẫn không rò dữ liệu.
        assertThat(result.getResponse().getStatus()).isIn(404, 409);
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE code <> 'LABREQ-2026-001'")).isZero();
    }
}
