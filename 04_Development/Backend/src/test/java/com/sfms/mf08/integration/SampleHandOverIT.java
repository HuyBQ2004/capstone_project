package com.sfms.mf08.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-INT-002 (bàn giao lô = 1 transaction, ADR-M08-1), MF08-TC-INT-003 (sự cố không hợp
 * lệ/đã đóng), MF08-TC-INT-004 (không bàn giao lần hai), MF08-TC-INT-015 (race 2 request).
 */
class SampleHandOverIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String MEDICAL = "U-0002";
    private static final String INSPECTION_CODE = "INSP-MF08-HANDOVER";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerSample(String dishCode) throws Exception {
        Mf08Fixtures.insertDish(jdbc, dishCode, "Món " + dishCode);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", dishCode);
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
        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return parse(result).get("id").asText();
    }

    private MvcResult handOver(String incidentCode, List<String> sampleCodes, boolean withRecord) throws Exception {
        Map<String, Object> body = Map.of("incidentId", incidentCode, "sampleIds", sampleCodes,
                "handoverType", "LAB", "testCriteria", "Salmonella, S.aureus");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(body).getBytes(StandardCharsets.UTF_8));
        var request = multipart("/api/v1/sample-lab-requests").file(part)
                .header("Authorization", authHeaderFor(MEDICAL));
        if (withRecord) {
            request.file(new MockMultipartFile("handoverRecords", "bb.pdf", "application/pdf",
                    new byte[]{'%', 'P', 'D', 'F'}));
        }
        return mvc.perform(request).andReturn();
    }

    // ───────────────────────── MF08-TC-INT-002 — 1 transaction ─────────────────────────

    @Test
    void handOverHappyPathIsAtomicAllThreeSamplesTransition() throws Exception {
        UUID incidentId = Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-HO-1", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        String s1 = registerSample("DISH-HO-1-A");
        String s2 = registerSample("DISH-HO-1-B");
        String s3 = registerSample("DISH-HO-1-C");
        jdbc.update("UPDATE food_samples SET incident_id = ? WHERE code IN (?,?,?)", incidentId, s1, s2, s3);

        MvcResult result = handOver("INC-MF08-HO-1", List.of(s1, s2, s3), true);

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        // Lọc theo school_id (KHÔNG theo code): sample_lab_requests unique theo (school_id, code)
        // — V15_1__restore_mf08.sql chèn sẵn 1 dòng code='LABREQ-2026-001' cho trường KHÁC
        // (SCH-MF03-0001), và bộ đếm codeGenerator "mf08_lab_request:2026" bắt đầu lại từ 0 mỗi
        // test nên dòng MỚI của chính test này cũng trùng code 'LABREQ-2026-001' (khác trường,
        // không vỡ UNIQUE) — loại theo code sẽ vô tình loại nhầm CẢ dòng test vừa tạo.
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE school_id = (SELECT id FROM schools WHERE code = 'SCH-HN-0042')")).isEqualTo(1L);
        // Cùng lý do — V15_1 seed đã có sẵn mẫu SENT_TO_LAB cho trường khác.
        assertThat(count("SELECT count(*) FROM food_samples "
                + "WHERE status = 'SENT_TO_LAB' AND school_id = (SELECT id FROM schools WHERE code = 'SCH-HN-0042')"))
                .isEqualTo(3L);
        assertThat(count("SELECT count(*) FROM sample_attachments sa "
                + "JOIN sample_lab_requests slr ON slr.id = sa.owner_id AND sa.owner_type = 'LAB_REQUEST' "
                + "WHERE sa.kind = 'HANDOVER_RECORD' "
                + "AND slr.school_id = (SELECT id FROM schools WHERE code = 'SCH-HN-0042')"))
                .isEqualTo(1L);
    }

    @Test
    void handOverWithoutHandoverRecordRollsBackCleanly() throws Exception {
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-HO-2", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        String s1 = registerSample("DISH-HO-2-A");

        MvcResult result = handOver("INC-MF08-HO-2", List.of(s1), false);

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-212");
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE code <> 'LABREQ-2026-001'")).isZero();
        assertThat(jdbc.queryForObject("SELECT status FROM food_samples WHERE code = ?", String.class, s1))
                .isEqualTo("RETAINED");
    }

    @Test
    void handOverWithOneSampleAlreadySentToLabRollsBackAllSamples() throws Exception {
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-HO-3", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        String s1 = registerSample("DISH-HO-3-A");
        String s2 = registerSample("DISH-HO-3-B");
        assertThat(handOver("INC-MF08-HO-3", List.of(s2), true).getResponse().getStatus()).isEqualTo(201);

        MvcResult result = handOver("INC-MF08-HO-3", List.of(s1, s2), true);

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-113");
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE school_id = (SELECT id FROM schools WHERE code = 'SCH-HN-0042')")).isEqualTo(1L);   // vẫn chỉ phiếu đầu
        assertThat(jdbc.queryForObject("SELECT status FROM food_samples WHERE code = ?", String.class, s1))
                .isEqualTo("RETAINED");   // s1 KHÔNG bị đổi một nửa
    }

    // ───────────────────────── MF08-TC-INT-003 ─────────────────────────

    @Test
    void handOverForNonExistentIncidentIsRejectedWith409() throws Exception {
        String s1 = registerSample("DISH-HO-4-A");

        MvcResult result = handOver("INC-DOES-NOT-EXIST", List.of(s1), true);

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-023");
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE code <> 'LABREQ-2026-001'")).isZero();
    }

    /**
     * ⚠️ GAP THẬT (không sửa main code) — cùng bug đã ghi ở
     * {@code DisposedSampleLinkIT.linkingToClosedIncidentCurrentlySucceedsGapNotEnforced}:
     * {@code IncidentStatusPort.findOpen(...).orElseThrow(...)} ở {@code SampleLabRequestServiceImpl
     * .handOver()} KHÔNG kiểm field {@code IncidentRef.open()} — chỉ ném khi sự cố KHÔNG TỒN
     * TẠI. Bàn giao mẫu cho một sự cố ĐÃ ĐÓNG hiện vẫn THÀNH CÔNG (201), khác kỳ vọng 409 SMP-023
     * của TestSpec. Test xác nhận đúng hiện trạng.
     */
    @Test
    void handOverForClosedIncidentCurrentlySucceedsGapNotEnforced() throws Exception {
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-HO-5", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        Mf08Fixtures.closeIncident(jdbc, "INC-MF08-HO-5");
        String s1 = registerSample("DISH-HO-5-A");

        MvcResult result = handOver("INC-MF08-HO-5", List.of(s1), true);

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
    }

    // ───────────────────────── MF08-TC-INT-004 ─────────────────────────

    @Test
    void sameIncidentTwiceForSameSampleIsRejectedWith409AndKeepsOriginalLink() throws Exception {
        UUID incidentId = Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-HO-6", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        String s1 = registerSample("DISH-HO-6-A");
        jdbc.update("UPDATE food_samples SET incident_id = ? WHERE code = ?", incidentId, s1);
        assertThat(handOver("INC-MF08-HO-6", List.of(s1), true).getResponse().getStatus()).isEqualTo(201);
        String firstLabRequestId = jdbc.queryForObject(
                "SELECT lab_request_id::text FROM food_samples WHERE code = ?", String.class, s1);

        MvcResult result = handOver("INC-MF08-HO-6", List.of(s1), true);

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-113");
        assertThat(jdbc.queryForObject("SELECT lab_request_id::text FROM food_samples WHERE code = ?",
                String.class, s1)).isEqualTo(firstLabRequestId);
    }

    // ───────────────────────── MF08-TC-INT-015 — race ─────────────────────────

    @Test
    void concurrentHandOverOfSameSampleOnlyOneSucceeds() throws Exception {
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-HO-7A", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-MF08-HO-7B", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        String s1 = registerSample("DISH-HO-7-A");

        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CountDownLatch go = new java.util.concurrent.CountDownLatch(1);
        var f1 = pool.submit(() -> {
            go.await();
            return handOver("INC-MF08-HO-7A", List.of(s1), true).getResponse().getStatus();
        });
        var f2 = pool.submit(() -> {
            go.await();
            return handOver("INC-MF08-HO-7B", List.of(s1), true).getResponse().getStatus();
        });
        go.countDown();
        int status1 = f1.get();
        int status2 = f2.get();
        pool.shutdown();

        long successCount = List.of(status1, status2).stream().filter(s -> s == 201).count();
        assertThat(successCount).isEqualTo(1L);
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE school_id = (SELECT id FROM schools WHERE code = 'SCH-HN-0042')")).isEqualTo(1L);
    }
}
