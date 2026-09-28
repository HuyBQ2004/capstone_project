package com.sfms.mf08.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.common.enums.RoleKey;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF08-TC-SEC-001 (chỉ Medical Staff bàn giao — BR-113/BR-215 Deprecated), MF08-TC-SEC-002
 * (ma trận RBAC 9 endpoint), MF08-TC-SEC-004 (append-only), MF08-TC-SEC-006 (không JWT).
 *
 * <p>⚠️ Bẫy Red Gate (spec §5.1): @PreAuthorize chạy TRƯỚC method body — 403 phải xảy ra kể cả
 * với path param KHÔNG tồn tại. Test cố tình dùng mã "SMP-NONEXISTENT" cho các case role sai để
 * chứng minh guard chặn ở tầng authorization, không lẫn với 404.
 */
class SampleRbacIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String MEDICAL = "U-0002";
    private static final String INSPECTION_CODE = "INSP-MF08-SEC";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-SEC", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerASample() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-SEC");
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
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return parse(result).get("id").asText();
    }

    // ───────────────────────── MF08-TC-SEC-001 ─────────────────────────

    @Test
    void serviceCaterersCannotHandOverSamplesToLab() throws Exception {
        Map<String, Object> handOver = Map.of(
                "incidentId", "INC-DOES-NOT-MATTER", "sampleIds", List.of("SMP-DOES-NOT-MATTER"),
                "handoverType", "LAB", "testCriteria", "Salmonella");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(handOver).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mvc.perform(multipart("/api/v1/sample-lab-requests").file(part)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-005");
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE code <> 'LABREQ-2026-001'")).isZero();
    }

    // ───────────────────────── MF08-TC-SEC-002 — ma trận RBAC ─────────────────────────

    @ParameterizedTest
    @EnumSource(value = RoleKey.class, names = {"SYSTEM_ADMIN", "TEACHER", "PARENT", "SUPPLIER"})
    void nonBusinessRolesAreDeniedOnEveryMf08Endpoint(RoleKey role) throws Exception {
        String userCode = userCodeOf(role);
        String auth = authHeaderFor(userCode);

        assertThat(mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .header("Authorization", auth)).andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/samples/SMP-NONEXISTENT").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/samples/SMP-NONEXISTENT/label").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(patch("/api/v1/samples/SMP-NONEXISTENT/storage")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("storageLocation", "x"))))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(post("/api/v1/samples/SMP-NONEXISTENT/dispose")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("witness", "x", "note", "x"))))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(post("/api/v1/samples/SMP-NONEXISTENT/incident-link")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-X"))))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/samples/export").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/sample-lab-requests").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/receiving-authorities").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void schoolAdminCanReadButCannotWriteSamples() throws Exception {
        String auth = authHeaderFor("U-0001");

        assertThat(mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .header("Authorization", auth)).andReturn().getResponse().getStatus()).isEqualTo(200);
        assertThat(mvc.perform(get("/api/v1/samples/export").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        assertThat(mvc.perform(get("/api/v1/receiving-authorities").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        // Không đăng ký/hủy/bàn giao được — chỉ Service Caterers/Medical Staff.
        assertThat(mvc.perform(post("/api/v1/samples/SMP-NONEXISTENT/dispose")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("witness", "x", "note", "x"))))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(post("/api/v1/samples/SMP-NONEXISTENT/incident-link")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-X"))))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/sample-lab-requests").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(200);   // đọc phiếu bàn giao — được
    }

    @Test
    void medicalStaffCannotRegisterOrDisposeOrViewLabel() throws Exception {
        String auth = authHeaderFor(MEDICAL);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-SEC");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "x");
        payload.put("servings", 10);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "x");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.0);
        payload.put("storageLocation", "x");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        assertThat(mvc.perform(multipart("/api/v1/samples").file(part).header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);

        assertThat(mvc.perform(post("/api/v1/samples/SMP-NONEXISTENT/dispose")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("witness", "x", "note", "x"))))
                .andReturn().getResponse().getStatus()).isEqualTo(403);

        assertThat(mvc.perform(get("/api/v1/samples/SMP-NONEXISTENT/label").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void caterersCannotReadExportOrReceivingAuthoritiesOrLabRequests() throws Exception {
        String auth = authHeaderFor(CATERER);

        assertThat(mvc.perform(get("/api/v1/samples/export").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/receiving-authorities").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(get("/api/v1/sample-lab-requests").header("Authorization", auth))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(post("/api/v1/samples/SMP-NONEXISTENT/incident-link")
                        .header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-X"))))
                .andReturn().getResponse().getStatus()).isEqualTo(403);
    }

    // ───────────────────────── MF08-TC-SEC-004 — append-only ─────────────────────────

    @Test
    void noDeleteEndpointExistsForSamplesOrAttachments() throws Exception {
        String code = registerASample();
        String auth = authHeaderFor(CATERER);
        long before = count("SELECT count(*) FROM food_samples");
        long attachmentsBefore = count("SELECT count(*) FROM sample_attachments");

        int deleteSampleStatus = mvc.perform(delete("/api/v1/samples/" + code).header("Authorization", auth))
                .andReturn().getResponse().getStatus();
        int putSampleStatus = mvc.perform(put("/api/v1/samples/" + code).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn().getResponse().getStatus();

        // Endpoint không tồn tại -> 404/405 (Spring MVC không map được route) — KHÔNG PHẢI 200/204.
        assertThat(deleteSampleStatus).isIn(404, 405);
        assertThat(putSampleStatus).isIn(404, 405);
        assertThat(count("SELECT count(*) FROM food_samples")).isEqualTo(before);
        assertThat(count("SELECT count(*) FROM sample_attachments")).isEqualTo(attachmentsBefore);
    }

    // ───────────────────────── MF08-TC-SEC-006 — không JWT / token hỏng ─────────────────────────

    /**
     * ⚠️ Ground-truth: TestSpec giả định mã lỗi module {@code SMP-004} cho case không JWT.
     * Thực tế 401 "không xác thực" được xử lý ở TẦNG FILTER dùng chung
     * ({@code RestAuthEntryPoints}/{@code SecurityConfig}) TRƯỚC KHI request chạm bất kỳ
     * controller MF08 nào — mã trả về luôn là {@code IAM-005} (MSG-004), giống hệt mọi module
     * khác (xác nhận qua {@code IamExceptions}/{@code RestAuthEntryPoints}). {@code SmpError
     * .SESSION_EXPIRED = "SMP-004"} tồn tại trong hằng số nhưng KHÔNG có method nào trong
     * {@code SmpExceptions} thực sự ném nó — dead code, không phải gap cần test riêng.
     */
    @Test
    void requestsWithoutAuthorizationHeaderAreRejectedWith401() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        JsonNode body = parse(result);
        assertThat(body.get("error").get("code").asText()).isEqualTo("IAM-005");
    }

    @Test
    void malformedOrTamperedTokenIsRejectedWith401() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .header("Authorization", "Bearer this.is.not-a-valid-jwt"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void everyMf08EndpointRequiresAuthentication() throws Exception {
        assertThat(mvc.perform(get("/api/v1/samples/SMP-X")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(mvc.perform(get("/api/v1/sample-lab-requests")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(mvc.perform(get("/api/v1/receiving-authorities")).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
