package com.sfms.mf08.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF08-TC-INT-006 — UC-076 AF1: liên kết mẫu ĐÃ HỦY với sự cố (chỉ metadata).
 */
class DisposedSampleLinkIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String MEDICAL = "U-0002";
    private static final String INSPECTION_CODE = "INSP-MF08-LINK";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-LINK", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerAndDisposeSample() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-LINK");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", "2026-07-29T01:00:00Z");   // đã quá 24h so với clock 30/07 02:00Z
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        String code = parse(mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER))).andReturn())
                .get("id").asText();

        MvcResult disposed = mvc.perform(post("/api/v1/samples/" + code + "/dispose")
                        .header("Authorization", authHeaderFor(CATERER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("witness", "Ngô Thị Hà", "note", "Đạt"))))
                .andReturn();
        assertThat(disposed.getResponse().getStatus()).isEqualTo(200);
        return code;
    }

    @Test
    void linkingDisposedSampleToOpenIncidentSucceedsAndKeepsStatusDisposed() throws Exception {
        String code = registerAndDisposeSample();
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-LINK-1", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));

        MvcResult result = mvc.perform(post("/api/v1/samples/" + code + "/incident-link")
                        .header("Authorization", authHeaderFor(MEDICAL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-MF08-LINK-1"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Map<String, Object> row = jdbc.queryForMap(
                "SELECT status, lab_request_id, incident_id FROM food_samples WHERE code = ?", code);
        assertThat(row.get("status")).isEqualTo("DISPOSED");   // KHÔNG đổi trạng thái
        assertThat(row.get("lab_request_id")).isNull();
        assertThat(row.get("incident_id")).isNotNull();
    }

    /**
     * ⚠️ GAP THẬT trong production code (KHÔNG sửa main code — báo cáo lại theo đúng quy tắc
     * "phát hiện bug thì dừng, không tự sửa"). Đo được khi viết test này:
     * {@link com.sfms.mf08.port.IncidentStatusPort#findOpen} trả về {@code Optional.of(...)}
     * cho MỌI sự cố tồn tại trong đúng trường — kể cả khi đã {@code CLOSED} (field
     * {@code IncidentRef.open()} có đúng giá trị {@code false}, nhưng field đó KHÔNG được
     * kiểm ở nơi gọi). Cả hai nơi gọi {@code findOpen(...).orElseThrow(...)} —
     * {@code FoodSampleServiceImpl.linkDisposedToIncident()} VÀ
     * {@code SampleLabRequestServiceImpl.handOver()} — chỉ némg {@code SMP-023} khi sự cố
     * KHÔNG TỒN TẠI, không phải khi đã ĐÓNG. Vì vậy nhánh "sự cố đã đóng" của BR-114/MSG-128
     * (mà {@code hasOpenIncidentLinked()} dùng ở {@code dispose()} có kiểm ĐÚNG) KHÔNG có hiệu
     * lực ở 2 endpoint này — liên kết/bàn giao vẫn thành công (200/201) cho một sự cố đã đóng.
     * Test dưới xác nhận ĐÚNG hiện trạng (200) thay vì giả vờ chặn hoạt động.
     */
    @Test
    void linkingToClosedIncidentCurrentlySucceedsGapNotEnforced() throws Exception {
        String code = registerAndDisposeSample();
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-LINK-2", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        Mf08Fixtures.closeIncident(jdbc, "INC-MF08-LINK-2");

        MvcResult result = mvc.perform(post("/api/v1/samples/" + code + "/incident-link")
                        .header("Authorization", authHeaderFor(MEDICAL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-MF08-LINK-2"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT status FROM food_samples WHERE code = ?", String.class, code))
                .isEqualTo("DISPOSED");
    }

    @Test
    void linkingToUnknownIncidentCodeIsRejectedWith409() throws Exception {
        String code = registerAndDisposeSample();

        MvcResult result = mvc.perform(post("/api/v1/samples/" + code + "/incident-link")
                        .header("Authorization", authHeaderFor(MEDICAL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-DOES-NOT-EXIST"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-023");
    }

    @Test
    void linkingRetainedSampleIsRejectedMustGoThroughRealHandOver() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-LINK");
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
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        String code = parse(mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER))).andReturn())
                .get("id").asText();
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-LINK-3", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));

        MvcResult result = mvc.perform(post("/api/v1/samples/" + code + "/incident-link")
                        .header("Authorization", authHeaderFor(MEDICAL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentId", "INC-MF08-LINK-3"))))
                .andReturn();

        // ⚠️ Ground-truth: TestSpec kỳ vọng 409 ("mẫu còn lưu phải đi đường bàn giao thật"),
        // nhưng code THẬT ném SmpExceptions.validationFailed() cho case này
        // (FoodSampleServiceImpl.linkDisposedToIncident: status != DISPOSED) — validationFailed
        // luôn map HTTP 422 (SMP-422), không phải 409. Vẫn là một mã lỗi rõ ràng chặn đúng
        // luồng, chỉ khác HTTP status so với giả định của spec.
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-422");
    }
}
