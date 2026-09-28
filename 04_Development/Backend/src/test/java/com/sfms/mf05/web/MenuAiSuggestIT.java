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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF05-TC-014 / MF12-TC-INT-022 (L33/M12-D09) — <b>viết lại 2026-08-04</b>: MF12 Bước 2 đã lên
 * {@code GeminiAiAssistAdapter implements com.sfms.mf05.port.AiAssistPort}, nên bean KHÔNG còn
 * rỗng. {@code POST /menus/{code}/ai-suggest} giờ trả <b>200</b> thật (ADR-M05-03 fallback đã
 * hết hiệu lực — MF05 không sửa dòng nào, bean tồn tại là đủ) và sinh 1 dòng {@code ai_requests}
 * với {@code module='MF05_MENU'} (ADR-M12-01/BR-157).
 */
class MenuAiSuggestIT extends AbstractIamIntegrationTest {

    @BeforeEach
    void linkCaterer() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
    }

    @Test
    void aiSuggestNoLongerFallsBackNowThatMf12AdapterBeanExists() throws Exception {
        String code = createMenu();
        long aiRequestsBefore = count("SELECT count(*) FROM ai_requests WHERE module = 'MF05_MENU'");

        MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/ai-suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("mealType", "LUNCH"))))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).get("model").asText()).isNotBlank();

        assertThat(count("SELECT count(*) FROM ai_requests WHERE module = 'MF05_MENU'"))
                .isEqualTo(aiRequestsBefore + 1);

        // UC-075 AF1 — lập thủ công vẫn hoạt động bình thường sau khi gọi AI.
        MvcResult manualUpdate = mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/menus/" + code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("name", "Sửa thủ công sau AI"))))
                .andReturn();
        assertThat(manualUpdate.getResponse().getStatus()).isEqualTo(200);
    }

    private String createMenu() throws Exception {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        String gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                String.class, Mf05Fixtures.SCHOOL_CODE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don AI test");
        body.put("gradeId", gradeId);
        body.put("menuDateIso", "2026-08-05");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));

        MvcResult result = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        var root = parse(result);
        var menu = root.get("menu") == null ? root : root.get("menu");
        return menu.get("id").asText();
    }
}
