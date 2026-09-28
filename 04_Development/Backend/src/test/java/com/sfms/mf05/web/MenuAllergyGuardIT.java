package com.sfms.mf05.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf05.fixture.Mf05Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF05-TC-INT-006 — BR-063 phải chặn ở CẢ HAI đường vào: {@code POST /submit} (action endpoint)
 * VÀ {@code PUT} với {@code status=PENDING_APPROVAL} (đường FE giai đoạn 1) — nếu chỉ chặn ở
 * action endpoint thì FE hiện tại (dùng PUT) lách được hoàn toàn.
 */
class MenuAllergyGuardIT extends AbstractIamIntegrationTest {

    private UUID gradeId;

    @BeforeEach
    void seed() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
        gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);
    }

    private String createMenuWithShrimpDish() throws Exception {
        UUID student1 = Mf05Fixtures.insertStudent(jdbc, "HS-INT006-1", "3A", Mf05Fixtures.SCHOOL_CODE);
        UUID student2 = Mf05Fixtures.insertStudent(jdbc, "HS-INT006-2", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student1, "Tôm");
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student2, "Tôm");

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "canh-bi-do-nau-tom", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don INT-006");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-10");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        MvcResult result = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        return parse(result).get("menu").get("id").asText();
    }

    @Test
    void submitActionEndpointBlocksAllergyConflictAndKeepsDraft() throws Exception {
        String code = createMenuWithShrimpDish();

        MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/submit")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("MENU-051");
        String status = jdbc.queryForObject("select status from menus where code = ?", String.class, code);
        assertThat(status).isEqualTo("DRAFT");
        Boolean submittedAtNull = jdbc.queryForObject(
                "select submitted_at is null from menus where code = ?", Boolean.class, code);
        assertThat(submittedAtNull).isTrue();
    }

    @Test
    void putWithPendingApprovalStatusAlsoBlocksAllergyConflict() throws Exception {
        String code = createMenuWithShrimpDish();

        MvcResult result = mvc.perform(put("/api/v1/menus/" + code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("status", "PENDING_APPROVAL"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("MENU-051");
        String status = jdbc.queryForObject("select status from menus where code = ?", String.class, code);
        assertThat(status).isEqualTo("DRAFT");
    }

    @Test
    void approveAlsoReRunsAllergyGuard() throws Exception {
        // BR-063 chạy ở CẢ submit lẫn approve (C3) — dựng menu PENDING_APPROVAL bằng SQL thẳng
        // (bỏ qua submit() để mô phỏng ca dị ứng mới phát sinh SAU khi đã nộp).
        String code = createMenuWithShrimpDish();
        jdbc.update("update menus set status = 'PENDING_APPROVAL', submitted_at = now() where code = ?", code);

        MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-0001"))
                        .content(toJson(Map.of("approvalSignature", "SIG-INT006"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("MENU-051");
    }
}
