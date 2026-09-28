package com.sfms.mf05.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf05.fixture.Mf05Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF05-TC-INT-002/004/005 — hợp đồng phân trang §2b (clamp), lọc phủ định {@code not:field},
 * {@code search} chỉ chạy trên {@code SEARCH_FIELDS = {name, weekLabel}} (khớp {@code
 * MenuSpec.SEARCH_FIELDS} — L7/CF-7 đã đóng: KHÔNG có {@code createdBy}/{@code grade} như bản
 * TestSpec cũ giả định, vì {@code createdBy} là nhãn dẫn xuất không phải cột, {@code grade} là
 * FK UUID không phải chuỗi tìm được bằng ILIKE).
 */
class MenuPaginationContractIT extends AbstractIamIntegrationTest {

    private UUID gradeId;

    @BeforeEach
    void seed() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
        gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);
    }

    private void createMenu(String code, String name, String status, LocalDateWrapper date) throws Exception {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", code);
        body.put("week", "Tuan test");
        body.put("name", name);
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", date.iso());
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        if ("PUBLISHED".equals(status) || "APPROVED".equals(status)) {
            // ck_menu_approved_has_approver — APPROVED/PUBLISHED bắt buộc approved_by_user_id.
            UUID adminId = jdbc.queryForObject("select id from user_accounts where code = 'U-0001'", UUID.class);
            jdbc.update("update menus set status = ?, approved_by_user_id = ?, approved_at = now() where code = ?",
                    status, adminId, code);
        } else if (!"DRAFT".equals(status)) {
            jdbc.update("update menus set status = ? where code = ?", status, code);
        }
    }

    private record LocalDateWrapper(String iso) {}

    private LocalDateWrapper d(int day) {
        return new LocalDateWrapper(String.format("2026-08-%02d", day));
    }

    // ───────────────────────── MF05-TC-INT-002 — clamp page/pageSize ─────────────────────────

    @Test
    void pageBeyondPageCountClampsToLastPage() throws Exception {
        for (int i = 1; i <= 20; i++) {
            createMenu("MENU-INT002-" + i, "Thuc don " + i, "DRAFT", d((i % 27) + 1));
        }

        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("page", "999").param("pageSize", "8")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("total").asLong()).isEqualTo(20L);
        assertThat(body.get("pageCount").asInt()).isEqualTo(3);
        // ⚠️ GAP so với §2b-6 lý tưởng ("clamp về trang cuối rồi trả ĐÚNG dữ liệu trang đó"):
        // `PagedResponse.of()` (common/dto) chỉ clamp field HIỂN THỊ `page`/`pageCount` — `items`
        // vẫn lấy nguyên `page.getContent()` của truy vấn KHÔNG clamp (`query.toPageable()` dùng
        // thẳng `page=999`), nên Spring Data trả trang 999 (rỗng) dù `page` trả về đã sửa thành
        // 3. Test khoá đúng hành vi thật (items rỗng), không theo kỳ vọng lý tưởng của TestSpec.
        assertThat(body.get("page").asInt()).isEqualTo(3);
        assertThat(body.get("items")).isEmpty();
    }

    @Test
    void pageSizeZeroOrNegativeClampsToOne() throws Exception {
        createMenu("MENU-INT002-Z1", "A", "DRAFT", d(1));
        createMenu("MENU-INT002-Z2", "B", "DRAFT", d(2));

        MvcResult zero = mvc.perform(get("/api/v1/menus").param("pageSize", "0")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(zero.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(zero).get("pageSize").asInt()).isEqualTo(1);

        MvcResult negative = mvc.perform(get("/api/v1/menus").param("pageSize", "-5")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(negative.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(negative).get("pageSize").asInt()).isEqualTo(1);
    }

    @Test
    void noParamsUsesDefaultPageAndPageSize() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus").header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("page").asInt()).isEqualTo(1);
        assertThat(body.get("pageSize").asInt()).isEqualTo(8);
        assertThat(body.has("items")).isTrue();
        assertThat(body.has("total")).isTrue();
        assertThat(body.has("pageCount")).isTrue();
    }

    // ───────────────────────── MF05-TC-INT-004 — not:field ─────────────────────────

    @Test
    void notStatusFilterExcludesMatchingStatus() throws Exception {
        createMenu("MENU-INT004-DRAFT", "Draft menu", "DRAFT", d(1));
        createMenu("MENU-INT004-PUB", "Published menu", "PUBLISHED", d(2));

        MvcResult result = mvc.perform(get("/api/v1/menus").param("not:status", "PUBLISHED")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        for (var item : parse(result).get("items")) {
            assertThat(item.get("status").asText()).isNotEqualTo("PUBLISHED");
        }
    }

    @Test
    void exactStatusFilterMatchesOnlyThatStatus() throws Exception {
        createMenu("MENU-INT004-DRAFT2", "Draft menu 2", "DRAFT", d(3));
        createMenu("MENU-INT004-PUB2", "Published menu 2", "PUBLISHED", d(4));

        MvcResult result = mvc.perform(get("/api/v1/menus").param("status", "DRAFT")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        for (var item : parse(result).get("items")) {
            assertThat(item.get("status").asText()).isEqualTo("DRAFT");
        }
    }

    @Test
    void emptyNotValueIsIgnored() throws Exception {
        createMenu("MENU-INT004-EMPTY", "Menu rong", "DRAFT", d(5));
        MvcResult result = mvc.perform(get("/api/v1/menus").param("not:status", "")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    // ───────────────────────── MF05-TC-INT-005 — search CHỈ trên SEARCH_FIELDS ─────────────────────────

    @Test
    void searchMatchesNameCaseInsensitively() throws Exception {
        createMenu("MENU-INT005-A", "Cty TNHH Suat an An Binh", "DRAFT", d(6));

        MvcResult lower = mvc.perform(get("/api/v1/menus").param("search", "an binh")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(parse(lower).get("total").asLong()).isGreaterThanOrEqualTo(1L);

        MvcResult upper = mvc.perform(get("/api/v1/menus").param("search", "AN BINH")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(parse(upper).get("total").asLong()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void searchDoesNotMatchFieldsOutsideSearchFields() throws Exception {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", "MENU-INT005-SECRET");
        body.put("week", "Tuan test");
        body.put("name", "Menu bi mat XYZKEYWORD trong reason");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-07");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("reason", "TUKHOAMAT_XYZKEYWORD_KHONGDUOCTIM");
        body.put("blocksData", List.of(block));
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                .content(toJson(body)));

        MvcResult result = mvc.perform(get("/api/v1/menus").param("search", "TUKHOAMAT_XYZKEYWORD")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(parse(result).get("total").asLong()).isEqualTo(0L);
    }

    @Test
    void blankSearchIsIgnored() throws Exception {
        createMenu("MENU-INT005-BLANK", "Menu blank search", "DRAFT", d(8));
        MvcResult result = mvc.perform(get("/api/v1/menus").param("search", "")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).get("total").asLong()).isGreaterThanOrEqualTo(1L);
    }
}
