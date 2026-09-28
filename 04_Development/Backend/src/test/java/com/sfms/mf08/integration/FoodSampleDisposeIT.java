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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF08-TC-INT-005 — khoá/mở khoá hủy mẫu theo vòng đời sự cố (BR-112), dùng {@code IncidentStatusPortImpl}
 * THẬT (MF11 đã merge) thay vì stub.
 */
class FoodSampleDisposeIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-DISPOSE";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-DISPOSE", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerSample() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-DISPOSE");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        // Clock cố định của AbstractIamIntegrationTest: 2026-07-30T02:00:00Z. Đặt collectedAt
        // 25h trước để mẫu đã QUÁ HẠN lưu 24h ngay từ đầu test (BR-111 không còn là biến số).
        payload.put("collectedAt", "2026-07-29T01:00:00Z");
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return parse(result).get("id").asText();
    }

    private MvcResult dispose(String code) throws Exception {
        return mvc.perform(post("/api/v1/samples/" + code + "/dispose")
                        .header("Authorization", authHeaderFor(CATERER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("witness", "Ngô Thị Hà", "note", "Mẫu đạt"))))
                .andReturn();
    }

    @Test
    void disposeIsBlockedThenUnlockedAfterIncidentCloses() throws Exception {
        String code = registerSample();
        UUID incidentId = Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-DISPOSE-1", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        jdbc.update("UPDATE food_samples SET incident_id = ? WHERE code = ?", incidentId, code);

        MvcResult blocked = dispose(code);
        assertThat(blocked.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(blocked).get("error").get("code").asText()).isEqualTo("SMP-112");

        Mf08Fixtures.closeIncident(jdbc, "INC-MF08-DISPOSE-1");

        MvcResult unlocked = dispose(code);
        assertThat(unlocked.getResponse().getStatus()).isEqualTo(200);
        Map<String, Object> row = jdbc.queryForMap(
                "SELECT status, disposed_at, disposed_by, disposal_witness, disposal_note FROM food_samples "
                        + "WHERE code = ?", code);
        assertThat(row.get("status")).isEqualTo("DISPOSED");
        assertThat(row.get("disposed_at")).isNotNull();
        assertThat(row.get("disposed_by")).isNotNull();
        assertThat(row.get("disposal_witness")).isEqualTo("Ngô Thị Hà");
        assertThat(row.get("disposal_note")).isEqualTo("Mẫu đạt");
    }

    @Test
    void disposeOfSampleWithoutAnyLinkedIncidentSucceedsAfterDeadline() throws Exception {
        String code = registerSample();   // không gắn incidentId

        MvcResult result = dispose(code);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void retentionGateStillAppliesEvenWithoutIncident() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-DISPOSE-2", "Canh bí đỏ");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-DISPOSE-2");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        // collectedAt gần hiện tại -> CHƯA đủ 24h khi test chạy (clock=2026-07-30T02:00:00Z).
        payload.put("collectedAt", "2026-07-30T01:00:00Z");
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        String code = parse(mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER))).andReturn())
                .get("id").asText();

        MvcResult result = dispose(code);

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-111");
    }
}
