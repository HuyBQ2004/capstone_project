package com.sfms.mf07.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF07 — nhánh lỗi/biên của {@code InspectionServiceImpl} chưa có TC nào che phủ (đối chiếu
 * {@code TestSpec-MF07_Food_Safety_Inspection.md} với {@link InspectionSessionFlowIT}/
 * {@link InspectionDerivedFieldRejectionIT}/{@link InspectionRoleFieldParityIT} hiện có — các
 * TC này chưa được viết dù đã có oracle rõ trong code THẬT):
 * <ul>
 *   <li>{@code open()} — 403 khi Caterer chưa có quan hệ hợp tác được duyệt (chính bug đã sửa
 *   06/08/2026, xem comment {@code resolveCatererPartnerId}); 422 ngày sai định dạng; 422 ngày
 *   khác hôm nay (gate mới, chỉ cho mở phiên của NGÀY HIỆN TẠI theo {@code Clock}).</li>
 *   <li>{@code submitStep()} — step ngoài {1,2,3}; BR-091 holdingTemp &lt; 60; BR-083 mở rộng
 *   GPS ngoài geofence trường; BR-092/093 mã lô không tồn tại.</li>
 *   <li>{@code coSign()} — 409 khi các bước chưa PASSED hết (BR-098).</li>
 *   <li>{@code reinspect()} — 422 khi phiên gốc chưa FAILED.</li>
 *   <li>{@code detail()}/{@code loadInScope()} — BR-099 cách ly: Caterer không thấy phiên của
 *   đơn vị khác qua {@code GET /{code}} (khác {@code InspectionRoleFieldParityIT}, vốn chỉ kiểm
 *   tập dòng ở {@code GET /inspections} danh sách).</li>
 *   <li>{@code checkGate()} — {@code NO_SESSION}/{@code IN_PROGRESS} (chỉ có {@code PASSED}/
 *   {@code INSPECTION_FAILED} đã được test).</li>
 *   <li>{@code medicalReview()} (BR-222, MF07-TC-022) — 422 thiếu lý do khi FAILED; và ĐIỂM CỐT
 *   LÕI của BR-222 — đánh giá Y tế KHÔNG được ghi đè {@code overall}/serving-gate, KHÔNG sinh
 *   CAPA (khác hẳn BR-095 khi CHÍNH phiên trượt bước).</li>
 * </ul>
 */
class InspectionSessionGapsIT extends AbstractIamIntegrationTest {

    private static final UUID SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    private static final UUID OTHER_PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0001"); // SUP-001

    /** Cùng khuôn {@code linkCaterer()} của {@link InspectionSessionFlowIT} — gọi TAY (không
     *  {@code @BeforeEach}) vì một số TC ở đây cố tình KHÔNG muốn có quan hệ hợp tác. */
    private void linkCaterer() {
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF07-GAP-' || substr(gen_random_uuid()::text, 1, 8),
                       p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = 'CAT-001' AND s.code = 'SCH-HN-0042'
                """);
    }

    // ═════════════════════════ open() ═════════════════════════

    @Test
    void openIsRejectedWithAccessDeniedWhenCatererHasNoApprovedPartnership() throws Exception {
        // U-0003 — service-caterers của SCH-HN-0042 nhưng KHÔNG có quan hệ hợp tác nào trong
        // seed (xem comment cuối V900 "MF07 — KHÔNG thêm supplier_partnerships tĩnh"). Không
        // gọi linkCaterer() ⇒ resolveCatererPartnerId() trả null ⇒ open() phải chặn 403,
        // không phải NullPointerException/500.
        MvcResult result = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("date", "2026-07-30", "mealType", "LUNCH"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("INSP-005");
    }

    @Test
    void openRejectsInvalidDateFormat() throws Exception {
        linkCaterer();
        MvcResult result = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", authHeaderFor("U-0022"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("date", "not-a-date", "mealType", "LUNCH"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        JsonNode error = parse(result).get("error");
        assertThat(error.get("code").asText()).isEqualTo("INSP-001");
        assertThat(error.get("details").get(0).get("field").asText()).isEqualTo("date");
    }

    @Test
    void openRejectsDateOtherThanTheFixedClockToday() throws Exception {
        // Gate mới (không có trong TestSpec gốc, code là nguồn đúng theo hướng dẫn) — chỉ cho
        // mở phiên của NGÀY HIỆN TẠI theo Clock giả (2026-07-30, AbstractIamIntegrationTest).
        linkCaterer();
        MvcResult result = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", authHeaderFor("U-0022"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("date", "2026-08-01", "mealType", "LUNCH"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        JsonNode error = parse(result).get("error");
        assertThat(error.get("code").asText()).isEqualTo("INSP-001");
        assertThat(error.get("details").get(0).get("field").asText()).isEqualTo("date");
    }

    // ═════════════════════════ submitStep() ═════════════════════════

    @Test
    void submitStepRejectsStepNumberOutsideOneToThree() throws Exception {
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "LUNCH");

        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/steps/4")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        JsonNode error = parse(result).get("error");
        assertThat(error.get("code").asText()).isEqualTo("INSP-001");
        assertThat(error.get("details").get(0).get("field").asText()).isEqualTo("step");
    }

    @Test
    void submitStepRejectsHoldingTempBelowSixty() throws Exception {
        // BR-091 — Bước 3 PASSED nhưng holdingTemp < 60 phải bị chặn 422 INSP-091 (chưa test —
        // InspectionSessionFlowIT chỉ phủ nhánh KHÔNG gửi holdingTemp, CF-24).
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "SNACK");
        passStep1(caterer, code);
        passStep2(caterer, code);

        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/steps/3")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED", "holdingTemp", 45))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("INSP-091");

        long stillPending = count("SELECT COUNT(*) FROM inspection_sessions WHERE code = ? AND step3 = 'PENDING'", code);
        assertThat(stillPending).isEqualTo(1);   // BR-091 chặn thì step3 KHÔNG được ghi PASSED
    }

    @Test
    void submitStepRejectsGpsOutsideSchoolGeofence() throws Exception {
        // BR-083 mở rộng (10/08/2026) — toạ độ nộp bước phải trong geofence trường. SCH-HN-0042
        // ở (21.0312, 105.7638), bán kính 200m (school_config). Gửi toạ độ cách xa hàng chục km.
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "BREAKFAST");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("result", "PASSED");
        body.put("gpsLat", 22.0);
        body.put("gpsLng", 106.0);
        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/steps/1")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("INSP-149");
    }

    @Test
    void submitStepRejectsUnknownBatchCode() throws Exception {
        // BR-092/093 (backlog A1, sửa 08/08/2026) — mã lô không tra được phải báo lỗi rõ, KHÔNG
        // im lặng nuốt lỗi (bug cũ đã sửa, xem comment resolveBatchId()).
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "LUNCH");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("result", "PASSED");
        body.put("step1Fresh", List.of(Map.of(
                "name", "Cá basa phi lê", "batchId", "BATCH-DOES-NOT-EXIST",
                "receivedAt", "30/07/2026 06:10", "quantity", "10 kg",
                "supplierName", "HTX Green Farm", "sensoryPass", true, "photoCount", 0)));
        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/steps/1")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        JsonNode error = parse(result).get("error");
        assertThat(error.get("code").asText()).isEqualTo("INSP-001");
        assertThat(error.get("details").get(0).get("field").asText()).isEqualTo("batchId");
    }

    // ═════════════════════════ coSign() / reinspect() ═════════════════════════

    @Test
    void coSignIsBlockedWhenStepsAreNotAllPassed() throws Exception {
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "LUNCH");   // vẫn IN_PROGRESS, chưa nộp bước nào

        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/co-sign")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("coSignerUserId", "U-0002"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("INSP-098");
    }

    @Test
    void reinspectRejectsSessionThatIsNotFailed() throws Exception {
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "SNACK");   // IN_PROGRESS, chưa FAILED

        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/reinspect")
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("INSP-001");
    }

    // ═════════════════════════ BR-099 cách ly ở GET /{code} ═════════════════════════

    @Test
    void detailReturnsNotFoundForSessionOfAnotherPartner() throws Exception {
        linkCaterer();
        // Phiên thuộc đơn vị KHÁC (OTHER_PARTNER_ID = SUP-001) — cùng trường nhưng khác partner.
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, inspector_user_id)
                VALUES (gen_random_uuid(), 'INSP-GAP-OTHER-1', ?, ?, DATE '2026-07-30', 'LUNCH',
                    'PENDING', 'PENDING', 'PENDING', 'IN_PROGRESS',
                    (SELECT id FROM user_accounts WHERE code = 'U-0003'))
                """, SCHOOL_ID, OTHER_PARTNER_ID);

        MvcResult result = mvc.perform(get("/api/v1/inspections/INSP-GAP-OTHER-1")
                        .header("Authorization", authHeaderFor("U-0022")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("INSP-404");
    }

    @Test
    void detailReturnsNotFoundForUnknownCode() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/inspections/INSP-DOES-NOT-EXIST")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("INSP-404");
    }

    // ═════════════════════════ checkGate() — 4 reasonCode ═════════════════════════

    @Test
    void servingGateReturnsNoSessionReasonWhenSlotHasNoSession() throws Exception {
        linkCaterer();
        MvcResult result = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", authHeaderFor("U-0022"))
                        .param("date", "2026-07-30").param("mealType", "BREAKFAST"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = parse(result);
        assertThat(body.get("allowed").asBoolean()).isFalse();
        assertThat(body.get("reasonCode").asText()).isEqualTo("NO_SESSION");
        assertThat(body.get("sessionId").isNull()).isTrue();
    }

    @Test
    void servingGateReturnsInProgressReasonWhileStepsAreNotYetDecided() throws Exception {
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        openSession(caterer, "LUNCH");   // mở nhưng chưa nộp bước nào ⇒ overall IN_PROGRESS

        MvcResult result = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", caterer)
                        .param("date", "2026-07-30").param("mealType", "LUNCH"))
                .andReturn();
        JsonNode body = parse(result);
        assertThat(body.get("allowed").asBoolean()).isFalse();
        assertThat(body.get("reasonCode").asText()).isEqualTo("IN_PROGRESS");
    }

    // ═════════════════════════ medicalReview() (BR-222, MF07-TC-022) ═════════════════════════

    @Test
    void medicalReviewFailedWithoutReasonIsRejected() throws Exception {
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "LUNCH");

        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/medical-review")
                        .header("Authorization", authHeaderFor("U-0002"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("medicalReviewResult", "FAILED"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        JsonNode error = parse(result).get("error");
        assertThat(error.get("code").asText()).isEqualTo("INSP-001");
        assertThat(error.get("details").get(0).get("field").asText()).isEqualTo("medicalReviewReason");
    }

    @Test
    void medicalReviewFailedNeverOverwritesOverallOrServingGateOrGeneratesCapa() throws Exception {
        // BR-222 — điểm cốt lõi: đánh giá độc lập của Y tế là chỉ số SONG SONG, khác hẳn BR-095
        // (khi CHÍNH phiên kiểm thực trượt bước thì overall=FAILED + CAPA tự sinh, xem
        // InspectionSessionFlowIT#failedStepBlocksServingGateAndAllowsReinspect).
        linkCaterer();
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "SNACK");   // IN_PROGRESS, chưa nộp bước nào

        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/medical-review")
                        .header("Authorization", authHeaderFor("U-0002"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("medicalReviewResult", "FAILED",
                                "medicalReviewReason", "Nghi ngờ dụng cụ chưa tiệt trùng"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = parse(result);
        assertThat(body.get("medicalReviewResult").asText()).isEqualTo("FAILED");
        // Overall KHÔNG bị ghi đè bởi đánh giá Y tế.
        assertThat(body.get("overall").asText()).isEqualTo("IN_PROGRESS");

        long stillInProgress = count("SELECT COUNT(*) FROM inspection_sessions WHERE code = ? AND overall = 'IN_PROGRESS'", code);
        assertThat(stillInProgress).isEqualTo(1);

        // Serving-gate KHÔNG chuyển allowed=false vì medicalReviewResult=FAILED (chỉ overall
        // mới quyết định BR-094) — vẫn IN_PROGRESS như trước khi có đánh giá Y tế.
        MvcResult gate = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", caterer)
                        .param("date", "2026-07-30").param("mealType", "SNACK"))
                .andReturn();
        assertThat(parse(gate).get("reasonCode").asText()).isEqualTo("IN_PROGRESS");

        // KHÔNG CAPA nào được sinh từ đánh giá Y tế (khác BR-095).
        long capaCount = count("SELECT COUNT(*) FROM corrective_actions ca "
                + "JOIN inspection_sessions s ON s.id = ca.inspection_id WHERE s.code = ?", code);
        assertThat(capaCount).isEqualTo(0);
    }

    // ═════════════════════════ Tiện ích cục bộ ═════════════════════════

    private String openSession(String authHeader, String mealType) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("date", "2026-07-30", "mealType", mealType))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return parse(result).get("id").asText();
    }

    private void passStep1(String authHeader, String code) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/steps/1")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    private void passStep2(String authHeader, String code) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/steps/2")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED", "coreTemp", 78))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
