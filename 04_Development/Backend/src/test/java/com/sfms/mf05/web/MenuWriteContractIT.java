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
 * MF05-TC-027 — 🆕 canonical D12 sample test ({@code 02_Module_Completion_Workflow.md} §6):
 * {@code dishes} là SỐ ĐẾM dẫn xuất; gửi MẢNG (bẫy "cùng tên khác kiểu") phải bị BỎ QUA im
 * lặng, KHÔNG lưu, KHÔNG lỗi. Field dẫn xuất khác ({@code allergyConflicts}, {@code createdBy},
 * {@code approvedAt}, {@code costCeiling}) cũng phải bị bỏ qua/tự tính lại từ server.
 */
class MenuWriteContractIT extends AbstractIamIntegrationTest {

    @BeforeEach
    void linkCaterer() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
    }

    @Test
    void arrayDishesFieldIsIgnoredAndDerivedFieldsAreNeverTrusted() throws Exception {
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
        body.put("name", "Thuc don test D12");
        body.put("gradeId", gradeId);
        body.put("menuDateIso", "2026-09-01");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        // ⚠️ Bẫy D12 — "dishes" ở ĐÂY là MẢNG món, không phải số đếm response. Phải bị bỏ qua.
        body.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        body.put("allergyConflicts", 99);
        body.put("createdBy", "Kẻ mạo danh");
        body.put("createdBySupplierId", "SUP-999");
        body.put("approvedAt", "2020-01-01T00:00:00Z");
        body.put("costCeiling", 999999);

        MvcResult result = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        var root = parse(result);
        var menu = root.get("menu") == null ? root : root.get("menu");   // CreateResult{menu,warnings}

        // dishes trong RESPONSE luôn là SỐ, tính lại từ blocksData (1 món) — KHÔNG phải mảng FE gửi.
        assertThat(menu.get("dishes").isNumber()).isTrue();
        assertThat(menu.get("dishes").asInt()).isEqualTo(1);

        String code = menu.get("id").asText();

        // DB: đúng 1 dòng menu_dishes (không phải giá trị FE gửi lên bị lưu thẳng).
        Long dishRowCount = jdbc.queryForObject(
                "select count(*) from menu_dishes md join menus m on md.menu_id = m.id where m.code = ?",
                Long.class, code);
        assertThat(dishRowCount).isEqualTo(1L);

        // allergyConflicts KHÔNG có cột riêng — tính runtime, không phải 99 FE gửi.
        assertThat(menu.get("allergyConflicts").asInt()).isNotEqualTo(99);

        // createdBySupplierId LUÔN lấy từ JWT (CAT-001), KHÔNG phải "SUP-999" FE gửi.
        assertThat(menu.get("createdBySupplierId").asText()).isEqualTo(Mf05Fixtures.CATERER_PARTNER_CODE);

        // approvedAt KHÔNG được set bởi field lạ trong body — vẫn null vì menu chưa qua duyệt.
        assertThat(menu.get("approvedAt").isNull()).isTrue();

        // costCeiling LUÔN chép từ school_config lúc TẠO (30000, V900 seed) — KHÔNG phải 999999.
        assertThat(menu.get("costCeiling").asInt()).isEqualTo(30000);
        assertThat(menu.get("costCeiling").asInt()).isNotEqualTo(999999);

        Integer dbCostCeiling = jdbc.queryForObject(
                "select cost_ceiling::int from menus where code = ?", Integer.class, code);
        assertThat(dbCostCeiling).isEqualTo(30000);
    }
}
