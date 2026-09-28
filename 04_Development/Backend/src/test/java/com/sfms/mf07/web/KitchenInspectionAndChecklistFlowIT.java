package com.sfms.mf07.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF07 — {@code checklist-groups} (UC-186/BR-210) + {@code kitchen-inspections} (UC-014,
 * ADR-M07-03) + {@code safety-signoffs} (UC-027) end-to-end.
 */
class KitchenInspectionAndChecklistFlowIT extends AbstractIamIntegrationTest {

    @Test
    void checklistGroupThenKitchenInspectionScoresServerSideAndCaterersConfirm() throws Exception {
        String medical = authHeaderFor("U-0002");   // SCH-HN-0042

        // UC-186 — Medical Staff thêm nhóm checklist với 2 hạng mục, server tự cấp id (D5).
        Map<String, Object> createGroup = Map.of(
                "name", "Nhóm test — Vệ sinh sàn",
                "items", List.of(
                        Map.of("name", "Sàn bếp khô ráo, không đọng nước", "legalNote", "Luật ATTP Điều 28"),
                        Map.of("name", "Thùng rác có nắp, không quá đầy", "legalNote", "Luật ATTP Điều 28.3")));
        MvcResult groupResult = mvc.perform(post("/api/v1/checklist-groups")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createGroup)))
                .andReturn();
        assertThat(groupResult.getResponse().getStatus()).isEqualTo(201);
        JsonNode group = parse(groupResult);
        String item1 = group.get("items").get(0).get("id").asText();
        String item2 = group.get("items").get(1).get("id").asText();

        MvcResult listResult = mvc.perform(get("/api/v1/checklist-groups").header("Authorization", medical)).andReturn();
        assertThat(listResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(listResult)).anyMatch(g -> g.get("id").asText().equals(group.get("id").asText()));

        // UC-014 — chấm điểm bếp: item1 KHÔNG ĐẠT (ảnh+lý do), item2 ĐẠT ⇒ score=50 (CF-26,
        // backend tự tính, KHÔNG tin số client — request này không hề gửi field score).
        Map<String, Object> submitBody = Map.of(
                "pestControlValid", true, "staffHealthValid", true,
                "itemResults", List.of(
                        Map.of("item", item1, "passed", false, "reason", "Nước đọng khu rửa", "photos", 2),
                        Map.of("item", item2, "passed", true)));
        MvcResult submitResult = mvc.perform(post("/api/v1/kitchen-inspections")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(submitBody)))
                .andReturn();
        assertThat(submitResult.getResponse().getStatus()).isEqualTo(201);
        JsonNode inspection = parse(submitResult);
        assertThat(inspection.get("score").asInt()).isEqualTo(50);
        assertThat(inspection.get("result").asText()).isEqualTo("FAILED");           // ADR-M07-03
        assertThat(inspection.get("confirmStatus").asText()).isEqualTo("AWAITING_CATERERS_CONFIRM");
        assertThat(inspection.get("warnings").get(0).get("code").asText()).isEqualTo("MSG-093");
        String inspectionCode = inspection.get("id").asText();

        // Thiếu ảnh/lý do cho mục "Không đạt" phải bị chặn (BR-207 — INSP-016).
        Map<String, Object> invalidBody = Map.of(
                "pestControlValid", true, "staffHealthValid", true,
                "itemResults", List.of(Map.of("item", item1, "passed", false)));
        MvcResult invalid = mvc.perform(post("/api/v1/kitchen-inspections")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidBody)))
                .andReturn();
        assertThat(invalid.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(invalid).get("error").get("code").asText()).isEqualTo("INSP-016");

        // BR-221 — Service Caterers xác nhận hai chiều.
        String caterer = authHeaderFor("U-0003");   // SCH-HN-0042, service-caterers
        MvcResult confirm = mvc.perform(post("/api/v1/kitchen-inspections/" + inspectionCode + "/confirm")
                        .header("Authorization", caterer))
                .andReturn();
        assertThat(confirm.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(confirm).get("confirmStatus").asText()).isEqualTo("CONFIRMED");
    }

    @Test
    void safetySignoffOncePerDay() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");   // SCH-HN-0042, school-admin

        MvcResult first = mvc.perform(post("/api/v1/safety-signoffs")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("note", "Không phát hiện bất thường"))))
                .andReturn();
        assertThat(first.getResponse().getStatus()).isEqualTo(201);

        // uq_signoff_day — ký lần 2 trong cùng ngày phải 409.
        MvcResult second = mvc.perform(post("/api/v1/safety-signoffs")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("note", "Ký lại"))))
                .andReturn();
        assertThat(second.getResponse().getStatus()).isEqualTo(409);
    }
}
