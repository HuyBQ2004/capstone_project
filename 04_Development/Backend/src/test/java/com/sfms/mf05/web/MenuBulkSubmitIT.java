package com.sfms.mf05.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf05.fixture.Mf05Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF05-TC-INT-014 — nộp duyệt hàng loạt N=15 request TUẦN TỰ (không có endpoint bulk thật —
 * §9.3.4-B) vẫn đúng, mỗi request là 1 transaction độc lập (1 lỗi không rollback các cái khác).
 */
class MenuBulkSubmitIT extends AbstractIamIntegrationTest {

    private UUID gradeId;

    @BeforeEach
    void seed() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
        gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);
    }

    private String createMenu(String code, LocalDate menuDate, MealTypeWrapper mealType) throws Exception {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", mealType.name());
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", code);
        body.put("week", "Tuan bulk");
        body.put("name", "Thuc don " + code);
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", menuDate.toString());
        body.put("mealType", mealType.name());
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        MvcResult result = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return code;
    }

    private enum MealTypeWrapper { LUNCH, BREAKFAST }

    @Test
    void fifteenSequentialSubmitsAllSucceed() throws Exception {
        // Ngày phục vụ đủ xa (>=3 ngày sau clock cố định 2026-07-30) — dùng 2026-08-15..29 (khác
        // nhau từng ngày để không đụng unique slot (grade,date,mealType,audience)).
        List<String> codes = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            String code = "MENU-BULK-" + i;
            createMenu(code, LocalDate.of(2026, 8, 15).plusDays(i), MealTypeWrapper.LUNCH);
            codes.add(code);
        }

        long start = System.currentTimeMillis();
        for (String code : codes) {
            MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/submit")
                            .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
        }
        long elapsedMs = System.currentTimeMillis() - start;

        Long pendingCount = jdbc.queryForObject(
                "select count(*) from menus where code like 'MENU-BULK-%' and status = 'PENDING_APPROVAL'",
                Long.class);
        assertThat(pendingCount).isEqualTo(15L);
        assertThat(elapsedMs).isLessThan(10_000L);
    }

    @Test
    void oneMenuWithAllergyConflictFailsIndependentlyWithoutRollingBackOthers() throws Exception {
        UUID student1 = Mf05Fixtures.insertStudent(jdbc, "HS-BULK-1", "3A", Mf05Fixtures.SCHOOL_CODE);
        UUID student2 = Mf05Fixtures.insertStudent(jdbc, "HS-BULK-2", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student1, "Tôm");
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student2, "Tôm");

        List<String> okCodes = new java.util.ArrayList<>();
        for (int i = 0; i < 14; i++) {
            String code = "MENU-BULK-OK-" + i;
            createMenu(code, LocalDate.of(2026, 8, 15).plusDays(i), MealTypeWrapper.LUNCH);
            okCodes.add(code);
        }
        // Menu thứ 15 có xung đột dị ứng.
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "BREAKFAST");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "canh-bi-do-nau-tom", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", "MENU-BULK-CONFLICT");
        body.put("week", "Tuan bulk");
        body.put("name", "Thuc don xung dot");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-15");
        body.put("mealType", "BREAKFAST");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        mvc.perform(post("/api/v1/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                .content(toJson(body)));

        for (String code : okCodes) {
            mvc.perform(post("/api/v1/menus/" + code + "/submit")
                    .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)));
        }
        MvcResult conflictResult = mvc.perform(post("/api/v1/menus/MENU-BULK-CONFLICT/submit")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(conflictResult.getResponse().getStatus()).isEqualTo(409);

        Long pendingCount = jdbc.queryForObject(
                "select count(*) from menus where code like 'MENU-BULK-OK-%' and status = 'PENDING_APPROVAL'",
                Long.class);
        assertThat(pendingCount).isEqualTo(14L);
        String conflictStatus = jdbc.queryForObject(
                "select status from menus where code = 'MENU-BULK-CONFLICT'", String.class);
        assertThat(conflictStatus).isEqualTo("DRAFT");
    }
}
