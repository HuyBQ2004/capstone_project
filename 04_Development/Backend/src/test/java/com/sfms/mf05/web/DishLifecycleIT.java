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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF05-TC-INT-013 — xóa món đang được thực đơn tham chiếu → 409; món KHÔNG tham chiếu → xóa
 * được; {@code isActive=false} là đường khuyến nghị thay thế (không đứt truy xuất thực đơn cũ).
 */
class DishLifecycleIT extends AbstractIamIntegrationTest {

    @BeforeEach
    void seed() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
    }

    @Test
    void deletingDishReferencedByMenuIsBlocked() throws Exception {
        UUID gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don INT-013");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-10");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        mvc.perform(post("/api/v1/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                .content(toJson(body)));

        MvcResult result = mvc.perform(delete("/api/v1/dishes/com-trang")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        Boolean stillExists = jdbc.queryForObject(
                "select exists(select 1 from dishes where code = 'com-trang')", Boolean.class);
        assertThat(stillExists).isTrue();
    }

    @Test
    void deletingUnreferencedDishSucceeds() throws Exception {
        MvcResult result = mvc.perform(delete("/api/v1/dishes/trai-cay-theo-mua")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isIn(200, 204);
        Boolean stillExists = jdbc.queryForObject(
                "select exists(select 1 from dishes where code = 'trai-cay-theo-mua')", Boolean.class);
        assertThat(stillExists).isFalse();
    }

    @Test
    void deactivatingDishKeepsItVisibleAndOldMenusStillResolveIt() throws Exception {
        UUID gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "su-su-xao", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don INT-013-inactive");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-11");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        MvcResult create = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        String menuCode = parse(create).get("menu").get("id").asText();

        MvcResult deactivate = mvc.perform(put("/api/v1/dishes/su-su-xao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("isActive", false))))
                .andReturn();
        assertThat(deactivate.getResponse().getStatus()).isEqualTo(200);

        // pageSize lớn — mặc định 8 (DEFAULT_PAGE_SIZE) + `toPageable()` KHÔNG sort theo `code`
        // (common/query §0-BIS "Sort.unsorted() mặc định toàn cục") nên món cần tìm có thể rơi
        // ra ngoài trang 1 nếu chỉ dùng mặc định.
        MvcResult listDishes = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/dishes")
                        .param("pageSize", "50")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        boolean stillListed = false;
        for (var item : parse(listDishes).get("items")) {
            if ("su-su-xao".equals(item.get("id").asText())) {
                stillListed = true;
            }
        }
        assertThat(stillListed).isTrue();

        MvcResult oldMenu = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/menus/" + menuCode)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        var dishes = parse(oldMenu).get("blocksData").get(0).get("dishes");
        assertThat(dishes).hasSize(1);
        assertThat(dishes.get(0).get("dishId").asText()).isEqualTo("su-su-xao");
    }
}
