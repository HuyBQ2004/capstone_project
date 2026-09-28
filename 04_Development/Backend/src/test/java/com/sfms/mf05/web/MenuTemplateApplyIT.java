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

/**
 * MF05-TC-INT-011 — {@code POST /menus/{code}/apply-template} tự đổ {@code
 * defaultSubstituteId} của món.
 *
 * <p>⚠️ {@code menu_templates} school-scoped ({@code findBySchoolIdAndCode}); fixture {@code
 * TPL-STD-B} (V9_1) thuộc trường tổng hợp riêng {@code SCH-MF05-0001}, KHÔNG phải {@code
 * SCH-HN-0042} (trường dùng chung của mọi IT khác qua {@code Mf05Fixtures.SCHOOL_CODE}) — dùng
 * thẳng {@code TPL-STD-B} cho menu ở {@code SCH-HN-0042} sẽ 404. Test này tự seed MỘT mẫu riêng
 * đúng trường {@code SCH-HN-0042}, giữ nguyên ý định TC (món có {@code defaultSubstituteId}
 * tự đổ khi áp mẫu) bằng {@code ca-basa-sot-ca-chua} (seed sẵn {@code defaultSubstituteId =
 * dau-phu-sot-ca-chua}, xem {@code V9_1__restore_mf05.sql}).
 */
class MenuTemplateApplyIT extends AbstractIamIntegrationTest {

    private UUID gradeId;
    private String draftMenuCode;
    private static final String TEMPLATE_CODE = "TPL-INT011-LOCAL";

    @BeforeEach
    void seed() throws Exception {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
        UUID schoolId = jdbc.queryForObject("select id from schools where code = ?", UUID.class,
                Mf05Fixtures.SCHOOL_CODE);
        gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);

        UUID templateId = java.util.UUID.randomUUID();
        jdbc.update("""
                INSERT INTO menu_templates (id, code, school_id, name, meal_type, audience, is_active)
                VALUES (?, ?, ?, 'Mau rieng INT-011', 'LUNCH', 'ALL', TRUE)
                """, templateId, TEMPLATE_CODE, schoolId);
        jdbc.update("""
                INSERT INTO menu_template_dishes (template_id, dish_id, serving_order)
                SELECT ?, d.id, 1 FROM dishes d WHERE d.code = 'ca-basa-sot-ca-chua'
                """, templateId);
        jdbc.update("""
                INSERT INTO menu_template_dishes (template_id, dish_id, serving_order)
                SELECT ?, d.id, 2 FROM dishes d WHERE d.code = 'com-trang'
                """, templateId);
        jdbc.update("""
                INSERT INTO menu_template_dishes (template_id, dish_id, serving_order)
                SELECT ?, d.id, 3 FROM dishes d WHERE d.code = 'canh-mong-toi'
                """, templateId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don INT-011");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-10");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of());
        MvcResult result = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        draftMenuCode = parse(result).get("menu").get("id").asText();
    }

    @Test
    void applyTemplateFillsDishesAndAutoAttachesDefaultSubstitute() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/menus/" + draftMenuCode + "/apply-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("templateId", TEMPLATE_CODE))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        var dishes = body.get("blocksData").get(0).get("dishes");
        assertThat(dishes).hasSize(3);

        Long dishRowCount = jdbc.queryForObject(
                "select count(*) from menu_dishes md join menus m on md.menu_id = m.id where m.code = ?",
                Long.class, draftMenuCode);
        assertThat(dishRowCount).isEqualTo(3L);

        boolean fishHasSubstitute = false;
        for (var dish : dishes) {
            if ("ca-basa-sot-ca-chua".equals(dish.get("dishId").asText())) {
                assertThat(dish.get("substitutes")).hasSize(1);
                assertThat(dish.get("substitutes").get(0).get("dishId").asText())
                        .isEqualTo("dau-phu-sot-ca-chua");
                fishHasSubstitute = true;
            }
        }
        assertThat(fishHasSubstitute).isTrue();
    }

    @Test
    void applyTemplateOnPublishedMenuIsLocked() throws Exception {
        UUID adminId = jdbc.queryForObject("select id from user_accounts where code = 'U-0001'", UUID.class);
        jdbc.update("update menus set status = 'PUBLISHED', approved_by_user_id = ?, approved_at = now() "
                + "where code = ?", adminId, draftMenuCode);

        MvcResult result = mvc.perform(post("/api/v1/menus/" + draftMenuCode + "/apply-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("templateId", TEMPLATE_CODE))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("MENU-067");
    }
}
