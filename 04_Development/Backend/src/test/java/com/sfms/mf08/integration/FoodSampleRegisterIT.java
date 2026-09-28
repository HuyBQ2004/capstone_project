package com.sfms.mf08.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-INT-001 (UC-069 happy path end-to-end), MF08-TC-INT-016 (Điều 7.1 — 1 mẫu/món/phiên,
 * kể cả race), MF08-TC-INT-017 (D9 — {@code created_by} do {@code AuditorAware} điền).
 */
class FoodSampleRegisterIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";       // service-caterers, SCH-HN-0042 (V900)
    private static final String INSPECTION_CODE = "INSP-MF08-INT-001";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-001", "Cơm trắng");
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-002", "Canh bí đỏ");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                java.time.LocalDate.of(2026, 7, 30));
    }

    private Map<String, Object> validPayload(String dishId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", dishId);
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa — 10:15");
        payload.put("servings", 1850);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox có nắp kín");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        // Clock cố định của AbstractIamIntegrationTest là 2026-07-30T02:00:00Z — collectedAt
        // PHẢI ở quá khứ so với mốc đó để hoursRemaining tất định (nếu collectedAt ở tương lai,
        // deadline = collectedAt+24h cũng dịch theo, hoursRemaining sẽ > 24 và phụ thuộc đồng hồ
        // hệ thống chạy test, không tất định).
        payload.put("collectedAt", "2026-07-30T01:00:00Z");
        payload.put("storageLocation", "Tủ lưu mẫu số 1 — ngăn 1");
        return payload;
    }

    private MvcResult register(Map<String, Object> payload, String authHeader) throws Exception {
        MockMultipartFile payloadPart = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3});
        return mvc.perform(multipart("/api/v1/samples").file(payloadPart).file(photo)
                        .header("Authorization", authHeader))
                .andReturn();
    }

    // ───────────────────────── MF08-TC-INT-001 ─────────────────────────

    @Test
    void registerEndToEndReturns201WithMsg044AndCorrectDbState() throws Exception {
        String caterer = authHeaderFor(CATERER);

        MvcResult result = register(validPayload("DISH-MF08-001"), caterer);

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode body = parse(result);
        String code = body.get("id").asText();
        assertThat(code).matches("^SMP-\\d{8}-\\d{2}$");   // D1/M08-D09 — khuôn mã
        assertThat(body.get("status").asText()).isEqualTo("RETAINED");
        // collectedAt=01:00Z, deadline=collectedAt+24h=2026-07-31T01:00Z; clock cố định 02:00Z
        // ngày 30/07 -> còn 23h -> ceil((23h*3600)/3600) = 23 (công thức FoodSampleAssembler).
        assertThat(body.get("hoursRemaining").asLong()).isEqualTo(23L);
        assertThat(body.get("evidencePhotos")).hasSize(1);

        // DB: span retention_deadline - collected_at đúng 24h (BR-111), temp_out_of_range=false.
        Map<String, Object> row = jdbc.queryForMap(
                "SELECT status, EXTRACT(EPOCH FROM (retention_deadline - collected_at)) AS span_seconds, "
                        + "temp_out_of_range, inspection_id FROM food_samples WHERE code = ?", code);
        assertThat(row.get("status")).isEqualTo("RETAINED");
        assertThat(((Number) row.get("span_seconds")).longValue()).isEqualTo(24 * 3600L);
        assertThat((Boolean) row.get("temp_out_of_range")).isFalse();
        assertThat(row.get("inspection_id")).isNotNull();

        long attachmentCount = count(
                "SELECT count(*) FROM sample_attachments WHERE owner_type='SAMPLE' AND kind='EVIDENCE_PHOTO' "
                        + "AND owner_id = (SELECT id FROM food_samples WHERE code = ?)", code);
        assertThat(attachmentCount).isEqualTo(1L);
    }

    // ───────────────────────── MF08-TC-INT-016 — Điều 7.1 ─────────────────────────

    @Test
    void secondRegistrationForSameInspectionAndDishIsRejected() throws Exception {
        String caterer = authHeaderFor(CATERER);
        MvcResult first = register(validPayload("DISH-MF08-001"), caterer);
        assertThat(first.getResponse().getStatus()).isEqualTo(201);

        MvcResult second = register(validPayload("DISH-MF08-001"), caterer);

        assertThat(second.getResponse().getStatus()).isEqualTo(409);
    }

    @Test
    void differentDishInSameInspectionIsAllowed() throws Exception {
        String caterer = authHeaderFor(CATERER);
        assertThat(register(validPayload("DISH-MF08-001"), caterer).getResponse().getStatus()).isEqualTo(201);

        MvcResult second = register(validPayload("DISH-MF08-002"), caterer);

        assertThat(second.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void sameDishInDifferentInspectionIsAllowed() throws Exception {
        Mf08Fixtures.insertPassedInspection(jdbc, "INSP-MF08-INT-001-B", Mf08Fixtures.SCHOOL_CODE, CATERER,
                java.time.LocalDate.of(2026, 7, 31));
        String caterer = authHeaderFor(CATERER);
        assertThat(register(validPayload("DISH-MF08-001"), caterer).getResponse().getStatus()).isEqualTo(201);

        Map<String, Object> secondInspectionPayload = validPayload("DISH-MF08-001");
        secondInspectionPayload.put("inspectionId", "INSP-MF08-INT-001-B");
        MvcResult second = register(secondInspectionPayload, caterer);

        assertThat(second.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void concurrentRegistrationForSameInspectionAndDishOnlyOneSucceeds() throws Exception {
        String caterer = authHeaderFor(CATERER);
        int threads = 2;
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.concurrent.CountDownLatch ready = new java.util.concurrent.CountDownLatch(threads);
        java.util.concurrent.CountDownLatch go = new java.util.concurrent.CountDownLatch(1);
        java.util.List<java.util.concurrent.Future<Integer>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                return register(validPayload("DISH-MF08-001"), caterer).getResponse().getStatus();
            }));
        }
        ready.await();
        go.countDown();
        int successCount = 0;
        for (var f : futures) {
            if (f.get() == 201) {
                successCount++;
            }
        }
        pool.shutdown();

        assertThat(successCount).isEqualTo(1);
        long total = count("SELECT count(*) FROM food_samples WHERE dish_id = "
                + "(SELECT id FROM dishes WHERE code = 'DISH-MF08-001')");
        assertThat(total).isEqualTo(1L);
    }

    // ───────────────────────── MF08-TC-INT-017 — D9 audit columns ─────────────────────────

    @Test
    void createdByIsFilledByAuditorAwareNotSetManuallyByService() throws Exception {
        String caterer = authHeaderFor(CATERER);
        MvcResult result = register(validPayload("DISH-MF08-001"), caterer);
        String code = parse(result).get("id").asText();

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT created_by, updated_by, created_at FROM food_samples WHERE code = ?", code);
        assertThat(row.get("created_by")).isEqualTo("duchm@dtd.edu.vn");
        assertThat(row.get("created_at")).isNotNull();

        // grep: service KHÔNG tự gọi setCreatedBy trong toàn bộ package (D9).
        assertThat(grepSourceForSetCreatedBy()).isEmpty();
    }

    @Test
    void updatingStorageChangesUpdatedByButKeepsCreatedByUnchanged() throws Exception {
        String caterer = authHeaderFor(CATERER);
        String code = parse(register(validPayload("DISH-MF08-001"), caterer)).get("id").asText();

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/samples/" + code + "/storage")
                        .header("Authorization", caterer)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("storageLocation", "Tủ lưu mẫu số 2"))))
                .andReturn();

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT created_by, updated_by FROM food_samples WHERE code = ?", code);
        assertThat(row.get("created_by")).isEqualTo("duchm@dtd.edu.vn");
        assertThat(row.get("updated_by")).isEqualTo("duchm@dtd.edu.vn");
    }

    private java.util.List<String> grepSourceForSetCreatedBy() throws Exception {
        java.nio.file.Path dir = java.nio.file.Path.of("src/main/java/com/sfms/mf08");
        if (!java.nio.file.Files.isDirectory(dir)) {
            return java.util.List.of();   // môi trường chạy từ jar, không có source tree — bỏ qua
        }
        try (var walk = java.nio.file.Files.walk(dir)) {
            return walk.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return java.nio.file.Files.readAllLines(p).stream()
                                    .filter(line -> line.contains("setCreatedBy") || line.contains("setUpdatedBy"))
                                    .map(line -> p + ": " + line.trim());
                        } catch (Exception e) {
                            return java.util.stream.Stream.empty();
                        }
                    })
                    .toList();
        }
    }
}
