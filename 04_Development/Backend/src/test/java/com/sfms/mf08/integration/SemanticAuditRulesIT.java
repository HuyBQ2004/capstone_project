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
 * MF08-TC-INT-011 — 10 luật {@code semantic-audit.ts} chạm MF08 (chỉ 6/10 chạy thuần trong
 * MF08, còn lại cần MF11 — MF11 đã merge nên chạy được thêm {@code MAU_NGOAI_KHUNG_SU_CO}).
 * MF08-TC-INT-012 — schema: không cột dẫn xuất, có đủ cột {@code review.md} §F đòi.
 */
class SemanticAuditRulesIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String MEDICAL = "U-0002";
    private static final String INSPECTION_CODE = "INSP-MF08-AUDIT";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerSample(String dishCode, String collectedAt) throws Exception {
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
        payload.put("collectedAt", collectedAt);
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

    // ── Mục 3: HAN_LUU_KHONG_DU_24H (script còn tên _72H) ──────────────────────────

    @Test
    void everyRegisteredSampleHasExactlyTwentyFourHourRetentionSpan() throws Exception {
        String code = registerSample("DISH-AUDIT-1", "2026-07-30T01:00:00Z");

        long spanSeconds = jdbc.queryForObject(
                "SELECT EXTRACT(EPOCH FROM (retention_deadline - collected_at))::bigint FROM food_samples "
                        + "WHERE code = ?", Long.class, code);
        assertThat(spanSeconds).isEqualTo(24 * 3600L);

        Long belowMinimum = jdbc.queryForObject(
                "SELECT count(*) FROM food_samples WHERE retention_deadline - collected_at < INTERVAL '24 hours'",
                Long.class);
        assertThat(belowMinimum).isZero();
    }

    // ── GUI_LAB_TRUOC_KHI_LAY / ck_smp_sent_after_collect ───────────────────────────

    @Test
    void checkConstraintBlocksSentToLabBeforeCollection() {
        UUID schoolId = jdbc.queryForObject(
                "SELECT id FROM schools WHERE code = ?", UUID.class, Mf08Fixtures.SCHOOL_CODE);
        assertThatDbInsertFails(() -> jdbc.update("""
                INSERT INTO food_samples (id, code, school_id, inspection_id, dish_id, dish_name, meal_type,
                    meal_shift, servings, sample_amount, sample_unit, container, sanitized, sealed, fridge_temp,
                    collected_at, collected_by, storage_location, retention_deadline, status, sent_to_lab_at)
                VALUES (gen_random_uuid(), 'SMP-AUDIT-BAD-1', ?, gen_random_uuid(), gen_random_uuid(), 'x', 'LUNCH',
                    'x', 1, 120, 'G', 'x', TRUE, TRUE, 4.0,
                    TIMESTAMPTZ '2026-07-06T03:15:00Z', 'x', 'x', TIMESTAMPTZ '2026-07-07T03:15:00Z', 'SENT_TO_LAB',
                    TIMESTAMPTZ '2026-07-06T00:00:00Z')
                """, schoolId));
    }

    // ── HUY_MAU_TRUOC_HAN_24H / ck_smp_dispose_after_deadline ───────────────────────

    @Test
    void checkConstraintBlocksDisposalBeforeRetentionDeadline() {
        UUID schoolId = jdbc.queryForObject(
                "SELECT id FROM schools WHERE code = ?", UUID.class, Mf08Fixtures.SCHOOL_CODE);
        assertThatDbInsertFails(() -> jdbc.update("""
                INSERT INTO food_samples (id, code, school_id, inspection_id, dish_id, dish_name, meal_type,
                    meal_shift, servings, sample_amount, sample_unit, container, sanitized, sealed, fridge_temp,
                    collected_at, collected_by, storage_location, retention_deadline, status,
                    disposed_at, disposed_by, disposal_witness, disposal_note)
                VALUES (gen_random_uuid(), 'SMP-AUDIT-BAD-2', ?, gen_random_uuid(), gen_random_uuid(), 'x', 'LUNCH',
                    'x', 1, 120, 'G', 'x', TRUE, TRUE, 4.0,
                    TIMESTAMPTZ '2026-07-06T03:15:00Z', 'x', 'x', TIMESTAMPTZ '2026-07-07T03:15:00Z', 'DISPOSED',
                    TIMESTAMPTZ '2026-07-07T00:00:00Z', 'x', 'x', 'x')
                """, schoolId));
    }

    // ── PHIEU_TRO_MAU_KHONG_TON_TAI ──────────────────────────────────────────────────

    @Test
    void handOverWithNonExistentSampleCodeIsRejectedWith404() throws Exception {
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-AUDIT-1", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        Map<String, Object> body = Map.of("incidentId", "INC-AUDIT-1",
                "sampleIds", List.of("SMP-KHONG-CO"), "handoverType", "LAB", "testCriteria", "x");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(body).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile record = new MockMultipartFile("handoverRecords", "bb.pdf", "application/pdf",
                new byte[]{'%', 'P', 'D', 'F'});

        MvcResult result = mvc.perform(multipart("/api/v1/sample-lab-requests").file(part).file(record)
                        .header("Authorization", authHeaderFor(MEDICAL)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE code <> 'LABREQ-2026-001'")).isZero();
    }

    // ── PHIEU_KHAC_SU_CO_VOI_MAU ──────────────────────────────────────────────────────

    @Test
    void handingOverSampleAlreadyLinkedToDifferentIncidentIsRejectedAndIncidentIdNotOverwritten() throws Exception {
        UUID incidentA = Mf08Fixtures.insertOpenIncident(jdbc, "INC-AUDIT-2A", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-AUDIT-2B", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        String s1 = registerSample("DISH-AUDIT-2", "2026-07-30T01:00:00Z");
        jdbc.update("UPDATE food_samples SET incident_id = ? WHERE code = ?", incidentA, s1);
        assertThat(handOverFor("INC-AUDIT-2A", s1).getResponse().getStatus()).isEqualTo(201);

        MvcResult result = handOverFor("INC-AUDIT-2B", s1);

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        UUID incidentIdAfter = jdbc.queryForObject(
                "SELECT incident_id FROM food_samples WHERE code = ?", UUID.class, s1);
        assertThat(incidentIdAfter).isEqualTo(incidentA);   // KHÔNG bị ghi đè bởi ca thứ hai
    }

    private MvcResult handOverFor(String incidentCode, String sampleCode) throws Exception {
        Map<String, Object> body = Map.of("incidentId", incidentCode, "sampleIds", List.of(sampleCode),
                "handoverType", "LAB", "testCriteria", "Salmonella");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(body).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile record = new MockMultipartFile("handoverRecords", "bb.pdf", "application/pdf",
                new byte[]{'%', 'P', 'D', 'F'});
        return mvc.perform(multipart("/api/v1/sample-lab-requests").file(part).file(record)
                        .header("Authorization", authHeaderFor(MEDICAL)))
                .andReturn();
    }

    // ── PHIEU_THU_GIU_THIEU_CO_QUAN / ck_labreq_authority ────────────────────────────

    @Test
    void authorityHandoverWithoutAuthorityIdIsRejected() throws Exception {
        Mf08Fixtures.insertOpenIncident(jdbc, "INC-AUDIT-3", Mf08Fixtures.SCHOOL_CODE,
                Instant.parse("2026-07-29T20:00:00Z"));
        String s1 = registerSample("DISH-AUDIT-3", "2026-07-30T01:00:00Z");
        Map<String, Object> body = Map.of("incidentId", "INC-AUDIT-3", "sampleIds", List.of(s1),
                "handoverType", "AUTHORITY");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(body).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile record = new MockMultipartFile("handoverRecords", "bb.pdf", "application/pdf",
                new byte[]{'%', 'P', 'D', 'F'});

        MvcResult result = mvc.perform(multipart("/api/v1/sample-lab-requests").file(part).file(record)
                        .header("Authorization", authHeaderFor(MEDICAL)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(count("SELECT count(*) FROM sample_lab_requests WHERE code <> 'LABREQ-2026-001'")).isZero();
    }

    @Test
    void checkConstraintBlocksAuthorityRowWithoutAuthorityIdAtDbLevel() {
        UUID schoolId = jdbc.queryForObject(
                "SELECT id FROM schools WHERE code = ?", UUID.class, Mf08Fixtures.SCHOOL_CODE);
        assertThatDbInsertFails(() -> jdbc.update("""
                INSERT INTO sample_lab_requests (id, code, school_id, lab_destination, handover_type,
                    requested_by, requested_at, delivered_by, sent_to_lab_at, status)
                VALUES (gen_random_uuid(), 'LABREQ-AUDIT-BAD', ?, 'x', 'AUTHORITY', 'x',
                    now(), 'x', now(), 'SENT_TO_LAB')
                """, schoolId));
    }

    // ───────────────────────── MF08-TC-INT-012 — schema ─────────────────────────

    @Test
    void schemaHasNoDerivedColumnsButHasAllReviewRequiredColumns() {
        Long tableExists = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'food_samples'", Long.class);
        assertThat(tableExists).isEqualTo(1L);   // tiền đề — tránh "xanh giả" khi bảng chưa tồn tại

        List<String> derivedColumns = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'food_samples' "
                        + "AND column_name IN ('hours_remaining','batch_ids','weight')", String.class);
        assertThat(derivedColumns).isEmpty();

        List<String> requiredColumns = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'food_samples' "
                        + "AND column_name IN ('code','sample_amount','sample_unit','inspection_id',"
                        + "'disposal_witness','temp_out_of_range','gps_lat','gps_lng')", String.class);
        assertThat(requiredColumns).containsExactlyInAnyOrder("code", "sample_amount", "sample_unit",
                "inspection_id", "disposal_witness", "temp_out_of_range", "gps_lat", "gps_lng");

        List<String> checkConstraints = jdbc.queryForList(
                "SELECT conname FROM pg_constraint WHERE conrelid = 'food_samples'::regclass AND contype = 'c'",
                String.class);
        assertThat(checkConstraints).contains("ck_smp_amount", "ck_smp_container_state",
                "ck_smp_dispose_after_deadline", "ck_smp_dispose_complete", "ck_smp_sent_has_request",
                "ck_smp_sent_after_collect");

        List<String> labRequestConstraints = jdbc.queryForList(
                "SELECT conname FROM pg_constraint WHERE conrelid = 'sample_lab_requests'::regclass AND contype = 'c'",
                String.class);
        assertThat(labRequestConstraints).contains("ck_labreq_criteria", "ck_labreq_authority");
    }

    @Test
    void uniqueIndexEnforcesOneSchoolCodePair() {
        List<String> indexDefs = jdbc.queryForList(
                "SELECT indexdef FROM pg_indexes WHERE tablename = 'food_samples' AND indexdef LIKE '%school_id%code%'",
                String.class);
        assertThat(indexDefs).isNotEmpty();
    }

    private void assertThatDbInsertFails(Runnable insert) {
        try {
            insert.run();
            throw new AssertionError("Expected CHECK constraint violation but insert succeeded");
        } catch (org.springframework.dao.DataIntegrityViolationException expected) {
            // BR-111/BR-113 chốt ở tầng DB — đúng kỳ vọng.
        }
    }
}
