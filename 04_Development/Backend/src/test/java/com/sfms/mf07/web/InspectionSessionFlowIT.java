package com.sfms.mf07.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF07 — luồng kiểm thực ba bước end-to-end (UC-052): mở phiên → Bước 1 → Bước 2 → Bước 3 →
 * đồng ký. Chủ thể: {@code U-0022} (service-caterers, partnerId=CAT-001, quan hệ hợp tác
 * ACTIVE với SCH-HN-0042 — xem V900 mục "MF07").
 */
class InspectionSessionFlowIT extends AbstractIamIntegrationTest {

    /**
     * U-0022 (partnerId=CAT-001) không có schoolId trong JWT — cần quan hệ hợp tác ACTIVE để
     * {@code ReferenceResolver.activeSchoolIdForPartner()} resolve được (cùng khuôn
     * {@code Mf05Fixtures.linkCatererToSchool} — KHÔNG chèn tĩnh vào V900 vì
     * {@code uq_ptn_partner_school} là UNIQUE(partner_id, school_id), 2 nguồn cùng chèn ⇒ vỡ).
     */
    @BeforeEach
    void linkCaterer() {
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF07-' || substr(gen_random_uuid()::text, 1, 8),
                       p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = 'CAT-001' AND s.code = 'SCH-HN-0042'
                """);
    }

    @Test
    void happyPathThreeStepsThenCoSign() throws Exception {
        String caterer = authHeaderFor("U-0022");

        // 1) Mở phiên (UC-052) — BR-079: một bữa chỉ 1 phiên gốc.
        Map<String, Object> openBody = Map.of("date", "2026-07-30", "mealType", "LUNCH");
        MvcResult openResult = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(openBody)))
                .andReturn();
        assertThat(openResult.getResponse().getStatus()).isEqualTo(201);
        JsonNode session = parse(openResult);
        String code = session.get("id").asText();
        assertThat(code).startsWith("INSP-20260730-L");
        assertThat(session.get("overall").asText()).isEqualTo("IN_PROGRESS");   // CF-23

        // D5/BR-079 — mở lại đúng slot phải 409.
        MvcResult dup = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(openBody)))
                .andReturn();
        assertThat(dup.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(dup).get("error").get("code").asText()).isEqualTo("INSP-409");

        // BR-079 — Bước 2 khoá tới khi Bước 1 PASSED.
        MvcResult lockedStep2 = mvc.perform(post("/api/v1/inspections/" + code + "/steps/2")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED", "coreTemp", 78))))
                .andReturn();
        assertThat(lockedStep2.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(lockedStep2).get("error").get("code").asText()).isEqualTo("INSP-079");

        // 2) Bước 1 — Mẫu số 1 (fresh row PASSED, không cần ảnh).
        Map<String, Object> step1Body = new LinkedHashMap<>();
        step1Body.put("result", "PASSED");
        step1Body.put("step1Fresh", List.of(Map.of(
                "name", "Cá basa phi lê", "receivedAt", "10/08/2026 06:10", "quantity", "19.2 kg",
                "supplierName", "HTX Green Farm", "hasInvoice", true, "hasVetCert", true,
                "hasQuarantineCert", true, "sensoryPass", true, "quickTest", "NONE", "photoCount", 0)));
        MvcResult step1 = mvc.perform(post("/api/v1/inspections/" + code + "/steps/1")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(step1Body)))
                .andReturn();
        assertThat(step1.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(step1).get("step1").asText()).isEqualTo("PASSED");

        // BR-090 — Bước 2 PASSED nhưng coreTemp < 75 phải bị chặn (422 INSP-017).
        MvcResult lowTemp = mvc.perform(post("/api/v1/inspections/" + code + "/steps/2")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED", "coreTemp", 68))))
                .andReturn();
        assertThat(lowTemp.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(lowTemp).get("error").get("code").asText()).isEqualTo("INSP-017");

        // 3) Bước 2 — nhiệt độ lõi đạt.
        MvcResult step2 = mvc.perform(post("/api/v1/inspections/" + code + "/steps/2")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED", "coreTemp", 78.5))))
                .andReturn();
        assertThat(step2.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(step2).get("overall").asText()).isEqualTo("IN_PROGRESS");

        // 4) Bước 3 — chốt overall PASSED (BR-098), CF-24: không gửi holdingTemp vẫn hợp lệ.
        MvcResult step3 = mvc.perform(post("/api/v1/inspections/" + code + "/steps/3")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED"))))
                .andReturn();
        assertThat(step3.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(step3).get("overall").asText()).isEqualTo("PASSED");

        // 5) Đồng ký — chỉ khi cả 3 bước PASSED (BR-098).
        MvcResult coSign = mvc.perform(post("/api/v1/inspections/" + code + "/co-sign")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("coSignerUserId", "U-0002"))))
                .andReturn();
        assertThat(coSign.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(coSign).get("coSigner").asText()).isEqualTo("Ngô Thị Hà");

        // D9 — 4 cột audit do BaseEntity điền, KHÔNG NULL.
        long audited = count("SELECT COUNT(*) FROM inspection_sessions WHERE code = ? "
                + "AND created_at IS NOT NULL AND updated_at IS NOT NULL", code);
        assertThat(audited).isEqualTo(1);

        // BR-094 — serving-gate trả allowed=true khi phiên PASSED.
        MvcResult gate = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", caterer)
                        .param("date", "2026-07-30").param("mealType", "LUNCH"))
                .andReturn();
        assertThat(gate.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(gate).get("allowed").asBoolean()).isTrue();
    }

    @Test
    void failedStepBlocksServingGateAndAllowsReinspect() throws Exception {
        String caterer = authHeaderFor("U-0022");

        MvcResult openResult = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("date", "2026-07-30", "mealType", "SNACK"))))
                .andReturn();
        String code = parse(openResult).get("id").asText();

        // Bước 1 KHÔNG ĐẠT — bắt buộc ảnh cho dòng sensoryPass=false (BR-083).
        Map<String, Object> failBody = new LinkedHashMap<>();
        failBody.put("result", "FAILED");
        failBody.put("failReason", "Cá bốc mùi ôi thiu");
        failBody.put("failSeverity", "HIGH");
        failBody.put("failWitness", "Ngô Thị Hà");
        // needsNewIngredients=true (13/08/2026) — cá ôi phải loại bỏ + nhập mới, đúng kịch bản
        // test này (kiểm reinspect()); thiếu field mặc định false ⇒ chỉ IN_PROGRESS, không FAILED.
        failBody.put("needsNewIngredients", true);
        failBody.put("step1Fresh", List.of(Map.of(
                "name", "Cá basa phi lê", "receivedAt", "11/08/2026 06:10", "quantity", "10 kg",
                "supplierName", "HTX Green Farm", "hasInvoice", true, "hasVetCert", true,
                "hasQuarantineCert", true, "sensoryPass", false, "quickTest", "FAIL", "photoCount", 0)));
        MvcResult missingEvidence = mvc.perform(post("/api/v1/inspections/" + code + "/steps/1")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(failBody)))
                .andReturn();
        assertThat(missingEvidence.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(missingEvidence).get("error").get("code").asText()).isEqualTo("INSP-083");

        // Có ảnh — chấp nhận, overall chuyển FAILED (CF-25 ghi đủ fail_reason/severity/witness).
        failBody.put("step1Fresh", List.of(Map.of(
                "name", "Cá basa phi lê", "receivedAt", "11/08/2026 06:10", "quantity", "10 kg",
                "supplierName", "HTX Green Farm", "hasInvoice", true, "hasVetCert", true,
                "hasQuarantineCert", true, "sensoryPass", false, "quickTest", "FAIL", "photoCount", 2)));
        MvcResult step1Failed = mvc.perform(post("/api/v1/inspections/" + code + "/steps/1")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(failBody)))
                .andReturn();
        assertThat(step1Failed.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(step1Failed).get("overall").asText()).isEqualTo("FAILED");

        // BR-094 — serving-gate trả allowed=false + INSPECTION_FAILED.
        MvcResult gate = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", caterer)
                        .param("date", "2026-07-30").param("mealType", "SNACK"))
                .andReturn();
        JsonNode gateBody = parse(gate);
        assertThat(gateBody.get("allowed").asBoolean()).isFalse();
        assertThat(gateBody.get("reasonCode").asText()).isEqualTo("INSPECTION_FAILED");

        // BR-096 — tái kiểm tra tạo phiên MỚI, phiên cũ giữ nguyên FAILED.
        MvcResult reinspect = mvc.perform(post("/api/v1/inspections/" + code + "/reinspect")
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(reinspect.getResponse().getStatus()).isEqualTo(201);
        JsonNode fresh = parse(reinspect);
        assertThat(fresh.get("id").asText()).isNotEqualTo(code);
        assertThat(fresh.get("overall").asText()).isEqualTo("IN_PROGRESS");

        long stillFailed = count("SELECT COUNT(*) FROM inspection_sessions WHERE code = ? AND overall = 'FAILED'", code);
        assertThat(stillFailed).isEqualTo(1);
    }
}
