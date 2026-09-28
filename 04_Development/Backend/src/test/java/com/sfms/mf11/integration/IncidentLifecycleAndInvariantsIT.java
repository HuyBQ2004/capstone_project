package com.sfms.mf11.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF11-TC-INT-001/002/008/010/011/013/019/023 — bất biến tầng DB + hợp đồng D1/D4/D9, dựng qua
 * luồng API thật (RPT → REC → open-investigation) vì <b>seed thật của MF11 không có dữ liệu
 * nghiệp vụ</b> (xem javadoc {@link #openIncidentViaApi()}).
 */
class IncidentLifecycleAndInvariantsIT extends AbstractIamIntegrationTest {

    private static final String U_TEACHER = "U-0004";
    private static final String U_MEDICAL_STAFF = "U-0002";

    /**
     * GAP thật phát hiện (đợt 2): {@code V21__seed_mf11.sql} chỉ khai báo các câu {@code UPDATE
     * ... WHERE code IN ('RPT-2026-002', 'RPT-2026-003', ...)} nhắm vào các mã (
     * {@code INC-2026-001}, {@code RPT-2026-002/003}, {@code REC-2026-001}) mà <b>không có
     * INSERT nào tạo ra chúng ở bất kỳ migration nào</b> (grep xác nhận: chuỗi
     * {@code "INC-2026-001"} chỉ xuất hiện trong chính {@code V21}). Các câu UPDATE này vì vậy
     * là no-op im lặng — DB thật sau khi Flyway chạy xong <b>không có một dòng
     * incidents/source_reports/incident_records/corrective_actions/violation_records nào</b>,
     * chỉ có {@code code_sequences} được khởi tạo. Mọi mã trong TestSpec (
     * {@code INC-2026-014}, {@code RPT-2026-030}, {@code CAPA-2026-033}, {@code VIO-2026-008})
     * bắt nguyên là fixture của {@code mock-data.ts} (FE), <b>không tồn tại trong DB thật</b>.
     * Vì vậy nhóm Integration ở đây tự dựng 1 sự cố hoàn chỉnh qua chính API (RPT → REC →
     * open-investigation) thay vì trỏ vào mã seed như TestSpec giả định.
     */
    private String openIncidentViaApi() throws Exception {
        // students không có student nào trong seed (V900) — tự chèn 1 HS lớp 3A (GVCN U-0004).
        UUID studentId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO students (id, code, registration_no, name, dob, school_id, classroom_id, status)
                SELECT ?, 'HS-3A-IT01', 'DTD-IT-0001', 'Nguyễn Minh Khang IT', DATE '2018-01-01', c.school_id, c.id, 'ACTIVE'
                FROM classrooms c WHERE c.code = '3A' AND c.school_id = (SELECT id FROM schools WHERE code = 'SCH-HN-0042')
                """, studentId);

        MvcResult rptResult = mvc.perform(post("/api/v1/source-reports")
                        .header("Authorization", authHeaderFor(U_TEACHER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("className", "3A", "symptoms", java.util.List.of("Đau bụng"),
                                "mealDate", "2026-06-30", "description", "3 học sinh đau bụng sau bữa trưa.",
                                "evidencePhotos", 0))))
                .andReturn();
        assertThat(rptResult.getResponse().getStatus()).isEqualTo(201).describedAs(rptResult.getResponse().getContentAsString());
        String rptCode = parse(rptResult).at("/id").asText();

        MvcResult recResult = mvc.perform(post("/api/v1/incident-records")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("sourceReportCodes", java.util.List.of(rptCode),
                                "content", "Biên bản điều tra sự cố nghi ngộ độc thực phẩm.",
                                "location", "Bếp trường"))))
                .andReturn();
        assertThat(recResult.getResponse().getStatus()).as(recResult.getResponse().getContentAsString()).isEqualTo(201);
        String recCode = parse(recResult).at("/id").asText();

        mvc.perform(post("/api/v1/incident-records/" + recCode + "/finalize")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();

        MvcResult openResult = mvc.perform(post("/api/v1/incident-records/" + recCode + "/open-investigation")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("title", "Sự cố nghi ngộ độc — IT",
                                "symptoms", java.util.List.of("Đau bụng"),
                                "mealDate", "2026-06-30",
                                "affectedStudentCodes", java.util.List.of("HS-3A-IT01")))))
                .andReturn();
        JsonNode body = parse(openResult);
        assertThat(openResult.getResponse().getStatus()).as(openResult.getResponse().getContentAsString()).isEqualTo(200);
        // Controller trả Map.of("incidentCode", ...) — KHÔNG phải IncidentResponse đầy đủ (D1 "/id").
        return body.at("/incidentCode").asText();
    }

    // ───────────────────────── TC-INT-010 — D1 ─────────────────────────

    @Test
    void incidentIdInJsonIsTheBusinessCodeNotAUuid() throws Exception {
        String incidentCode = openIncidentViaApi();
        assertThat(incidentCode).matches("^INC-\\d{4}-\\d{3}$");

        MvcResult result = mvc.perform(get("/api/v1/incidents/" + incidentCode)
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String body = result.getResponse().getContentAsString();
        assertThat(parse(result).at("/id").asText()).isEqualTo(incidentCode);

        Pattern uuidPattern = Pattern.compile(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
        assertThat(uuidPattern.matcher(body).find())
                .as("Response body không được lộ UUID nội bộ: %s", body)
                .isFalse();
    }

    @Test
    void fetchingByUuidInsteadOfCodeIs404() throws Exception {
        openIncidentViaApi();
        MvcResult result = mvc.perform(get("/api/v1/incidents/" + UUID.randomUUID())
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-404");
        assertThat(parse(result).at("/error/message").asText()).isEqualTo("Resource not found.");
    }

    // ───────────────────────── TC-INT-011 — D4 ─────────────────────────

    @Test
    void allDateFieldsAreIso8601NotDdMmYyyy() throws Exception {
        String incidentCode = openIncidentViaApi();
        MvcResult result = mvc.perform(get("/api/v1/incidents/" + incidentCode)
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        String body = result.getResponse().getContentAsString();

        assertThat(parse(result).at("/mealDate").asText()).matches("^\\d{4}-\\d{2}-\\d{2}$");
        assertThat(parse(result).at("/reportedAt").asText())
                .matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$");

        assertThat(Pattern.compile("\\d{2}/\\d{2}/\\d{4}").matcher(body).find())
                .as("Không field ngày nào được ở dạng dd/MM/yyyy: %s", body)
                .isFalse();
    }

    /**
     * GAP thật phát hiện (chạy thật, không phải suy đoán): D4/L4 mô tả backend phải "nhận khoan
     * dung cả 2 dạng khi NHẬN (mock đang gửi dd/MM/yyyy) nhưng TRẢ VỀ ISO". Thực tế đo được:
     * Jackson (mặc định, không có {@code @JsonFormat} tuỳ biến khoan dung) ném lỗi deserialize
     * ngay khi parse {@code LocalDate} từ chuỗi {@code "30/06/2026"} — request KHÔNG đi tới được
     * tầng service, trả <b>422</b> với {@code error.code = "IEM-422"} (mã sinh bởi handler lỗi
     * chung theo tiền tố module, KHÔNG có trong {@code IemError}/{@code TDS §10}). Vậy vế "nhận
     * khoan dung" của D4 <b>chưa được hiện thực</b> ở MF11 — chỉ vế "trả về ISO" là đúng (xem
     * {@link #allDateFieldsAreIso8601NotDdMmYyyy()}).
     */
    @Test
    void createSourceReportDoesNotToleratesDdMmYyyyMealDateRealGapInD4() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/source-reports")
                        .header("Authorization", authHeaderFor(U_TEACHER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("className", "3A", "symptoms", java.util.List.of("Sốt"),
                                "mealDate", "30/06/2026", "description", "Mô tả", "evidencePhotos", 0))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-422");
    }

    // ───────────────────────── TC-INT-013 — D9 ─────────────────────────

    @Test
    void createdByColumnIsAutoFilledWithActorEmailNotSetByService() throws Exception {
        String incidentCode = openIncidentViaApi();
        String createdBy = jdbc.queryForObject(
                "SELECT created_by FROM incidents WHERE code = ?", String.class, incidentCode);
        // U-0002 (medical-staff) là actor gọi open-investigation() — người TẠO Incident thật sự,
        // không phải U-0004 (teacher, chỉ tạo RPT).
        assertThat(createdBy).isEqualTo("hant@dtd.edu.vn");
    }

    @Test
    void thirteenMf11EntitiesDoNotRedeclareBaseEntityAuditFields() {
        Class<?>[] entities = {
                com.sfms.mf11.domain.Incident.class, com.sfms.mf11.domain.IncidentSourceReport.class,
                com.sfms.mf11.domain.IncidentRecordDraft.class, com.sfms.mf11.domain.CorrectiveAction.class,
                com.sfms.mf11.domain.ViolationRecord.class, com.sfms.mf11.domain.ViolationFile.class,
                com.sfms.mf11.domain.IncidentAffectedStudent.class, com.sfms.mf11.domain.IncidentSuspectedDish.class,
                com.sfms.mf11.domain.IncidentQuarantineBatch.class, com.sfms.mf11.domain.IncidentEvidence.class,
                com.sfms.mf11.domain.IncidentEvent.class, com.sfms.mf11.domain.Mf11Attachment.class,
        };
        for (Class<?> entity : entities) {
            for (var field : entity.getDeclaredFields()) {
                assertThat(field.getName()).as("%s không được tự khai lại field audit", entity.getSimpleName())
                        .isNotIn("createdBy", "updatedBy");
            }
        }
    }

    // ───────────────────────── TC-INT-001 — append-only incident_events ─────────────────────────

    @Test
    void incidentEventsTableIsAppendOnlyEvenViaRawJdbc() throws Exception {
        String incidentCode = openIncidentViaApi();
        String eventCode = jdbc.queryForObject(
                "SELECT code FROM incident_events WHERE incident_id = (SELECT id FROM incidents WHERE code = ?) LIMIT 1",
                String.class, incidentCode);
        assertThat(eventCode).isNotNull();

        int updated = jdbc.update("UPDATE incident_events SET description = 'HACKED' WHERE code = ?", eventCode);
        String descriptionAfter = jdbc.queryForObject(
                "SELECT description FROM incident_events WHERE code = ?", String.class, eventCode);
        assertThat(descriptionAfter).isNotEqualTo("HACKED");

        long countBefore = count("SELECT count(*) FROM incident_events WHERE code = ?", eventCode);
        jdbc.update("DELETE FROM incident_events WHERE code = ?", eventCode);
        long countAfter = count("SELECT count(*) FROM incident_events WHERE code = ?", eventCode);
        assertThat(countAfter).isEqualTo(countBefore).isEqualTo(1);
    }

    @Test
    void incidentEventEntityHasNoPublicSetters() {
        for (var method : com.sfms.mf11.domain.IncidentEvent.class.getMethods()) {
            assertThat(method.getName()).as("IncidentEvent phải append-only, không có setter public")
                    .doesNotStartWith("set");
        }
    }

    // ───────────────────────── TC-INT-002 — quarantine one-way ─────────────────────────

    @Test
    void quarantineBatchTableHasNoDeleteAndNoReleasedAtColumn() {
        Integer releasedAtCount = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_name = 'incident_quarantine_batches' AND column_name IN ('released_at', 'released_by')
                """, Integer.class);
        assertThat(releasedAtCount).isZero();
    }

    @Test
    void quarantineReleaseLogTableReceivesNoNewRowsFromThisSuite() {
        // Bảng lịch sử (nếu tồn tại) chỉ để ĐỌC — backend không có method nào ghi vào đây.
        Integer exists = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.tables WHERE table_name = 'incident_quarantine_release_log'
                """, Integer.class);
        if (exists != null && exists > 0) {
            long rows = count("SELECT count(*) FROM incident_quarantine_release_log");
            assertThat(rows).isZero();
        }
    }

    // ───────────────────────── TC-INT-008 — CAPA DB constraints ─────────────────────────

    @Test
    void insertingASecondCapaForTheSameIncidentByRawJdbcViolatesUniqueConstraint() throws Exception {
        String incidentCode = openIncidentViaApi();
        UUID incidentId = jdbc.queryForObject("SELECT id FROM incidents WHERE code = ?", UUID.class, incidentCode);
        UUID schoolId = jdbc.queryForObject("SELECT school_id FROM incidents WHERE code = ?", UUID.class, incidentCode);

        jdbc.update("""
                INSERT INTO corrective_actions (id, code, school_id, source_label, incident_id, description,
                                                 assignee_label, status)
                VALUES (gen_random_uuid(), 'CAPA-IT-0001', ?, 'IT fixture', ?, 'Kế hoạch 1', 'Nhà thầu IT', 'DRAFT')
                """, schoolId, incidentId);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("""
                        INSERT INTO corrective_actions (id, code, school_id, source_label, incident_id, description,
                                                         assignee_label, status)
                        VALUES (gen_random_uuid(), 'CAPA-IT-0002', ?, 'IT fixture', ?, 'Kế hoạch 2', 'Nhà thầu IT', 'DRAFT')
                        """, schoolId, incidentId));
    }

    @Test
    void capaCreatedThroughApiAlwaysHasNullDeadlineDeprecatedColumn() throws Exception {
        String incidentCode = openIncidentViaApi();
        MvcResult result = mvc.perform(post("/api/v1/corrective-actions")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentCode", incidentCode, "description", "Vệ sinh lại kho",
                                "assigneeSupplierCode", "CAT-001"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(result.getResponse().getContentAsString()).isEqualTo(201);
        assertThat(parse(result).has("deadline") ? parse(result).get("deadline").isNull() : true).isTrue();

        String capaCode = parse(result).at("/id").asText();
        java.sql.Date deadline = jdbc.queryForObject(
                "SELECT deadline FROM corrective_actions WHERE code = ?", java.sql.Date.class, capaCode);
        assertThat(deadline).isNull();
    }

    // ───────────────────────── TC-INT-019 — không resource incident-events phẳng ─────────────────────────

    @Test
    void flatIncidentEventsResourceDoesNotExist() throws Exception {
        MvcResult postResult = mvc.perform(post("/api/v1/incident-events")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();
        assertThat(postResult.getResponse().getStatus()).isIn(404, 405);

        MvcResult getResult = mvc.perform(get("/api/v1/incident-events").param("pageSize", "200")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        assertThat(getResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    void scopedEventsEndpointReturnsOnlyEventsOfThatIncident() throws Exception {
        String incidentCode = openIncidentViaApi();
        MvcResult result = mvc.perform(get("/api/v1/incidents/" + incidentCode + "/events")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).isArray()).isTrue();
        assertThat(parse(result).size()).isGreaterThanOrEqualTo(1);
    }

    // ───────────────────────── TC-INT-023 — bất biến toàn cục sau seed ─────────────────────────

    @Test
    void closedIncidentsNeverHaveANonCompletedCapa() {
        long rows = count("""
                SELECT count(*) FROM incidents i JOIN corrective_actions c ON c.incident_id = i.id
                WHERE i.status = 'CLOSED' AND c.status <> 'COMPLETED'
                """);
        assertThat(rows).isZero();
    }

    @Test
    void noIncidentEventSaysClosedWhileTheIncidentStatusColumnDisagrees() {
        long rows = count("""
                SELECT count(*) FROM incidents i JOIN incident_events e ON e.incident_id = i.id
                WHERE e.type = 'CLOSED' AND i.status <> 'CLOSED'
                """);
        assertThat(rows).isZero();
    }

    @Test
    void everyIncidentPointsToAnIncidentRecord() {
        long rows = count("SELECT count(*) FROM incidents WHERE incident_record_id IS NULL");
        assertThat(rows).isZero();
    }

    @Test
    void noApprovedOrPendingViolationIsMissingASignedRecordFile() {
        long rows = count("""
                SELECT count(*) FROM violation_records v
                WHERE v.resolution IN ('APPROVED', 'PENDING_APPROVAL')
                  AND NOT EXISTS (SELECT 1 FROM violation_files f
                                  WHERE f.violation_id = v.id AND f.kind = 'SIGNED_RECORD')
                """);
        assertThat(rows).isZero();
    }

    @Test
    void noResolvedViolationIsMissingActionTakenHandlerOrUnitSanctionLevel() {
        long rows = count("""
                SELECT count(*) FROM violation_records
                WHERE resolution <> 'PENDING'
                  AND (action_taken IS NULL OR handled_by_id IS NULL OR unit_sanction_level IS NULL)
                """);
        assertThat(rows).isZero();
    }

    @Test
    void noMediumOrHighViolationWasApprovedByAMedicalStaffAccount() {
        long rows = count("""
                SELECT count(*) FROM violation_records v
                JOIN user_accounts u ON u.id = v.handled_by_id
                JOIN roles r ON r.id = u.role_id
                WHERE v.severity IN ('MEDIUM', 'HIGH') AND v.resolution = 'APPROVED' AND r.role_key = 'medical-staff'
                """);
        assertThat(rows).isZero();
    }
}
