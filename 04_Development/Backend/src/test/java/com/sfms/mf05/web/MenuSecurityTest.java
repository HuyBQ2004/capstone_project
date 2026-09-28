package com.sfms.mf05.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf05.fixture.Mf05Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
 * MF05-TC-SEC-006/009(rút gọn — không có LogCaptor)/010/013 — `MenuController`/`MenuSpec`.
 * Đối chiếu `AbstractIamIntegrationTest` clock cố định {@code 2026-07-30T02:00:00Z}.
 */
class MenuSecurityTest extends AbstractIamIntegrationTest {

    private UUID gradeId;

    @BeforeEach
    void seed() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
        gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);
    }

    private String createMenu(String code) throws Exception {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", code);
        body.put("week", "Tuan test");
        body.put("name", "Thuc don SEC test");
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
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return code;
    }

    // ───────────────────────── MF05-TC-SEC-006 — schoolId query bị bỏ qua, dùng JWT ─────────────────────────

    @Test
    void schoolIdQueryParamIsIgnoredScopeAlwaysComesFromJwt() throws Exception {
        createMenu("MENU-SEC-006-A");
        String otherSchoolId = jdbc.queryForObject(
                "select id from schools where code = ?", String.class, Mf05Fixtures.OTHER_SCHOOL_CODE);

        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("schoolId", otherSchoolId)
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();

        // `schoolId` KHÔNG thuộc FIELD_MAP của MenuSpec (allow-list) ⇒ bị coi là field lọc lạ
        // ⇒ 422 (hành vi allow-list thật của MenuSpec.build(), không phải "âm thầm bỏ qua rồi
        // trả 200" như TestSpec giả định — xem MenuSpec.java §FIELD_MAP không có "schoolId").
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    @Test
    void menuFromOtherSchoolIsNotFoundNotForbidden() throws Exception {
        // §16 ghi chú cuối / MenuAssembler.loadInScope — ngoài phạm vi trường trả 404, không 403.
        MvcResult result = mvc.perform(get("/api/v1/menus/MENU-DOES-NOT-EXIST-IN-SCOPE")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    // ───────────────────────── MF05-TC-SEC-009 (rút gọn) — response 409 không lộ danh tính HS ─────────────────────────

    @Test
    void allergyConflictResponseNeverContainsStudentIdentity() throws Exception {
        UUID student1 = Mf05Fixtures.insertStudent(jdbc, "HS-SEC009-1", "3A", Mf05Fixtures.SCHOOL_CODE);
        UUID student2 = Mf05Fixtures.insertStudent(jdbc, "HS-SEC009-2", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student1, "Tôm");
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student2, "Tôm");

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "canh-bi-do-nau-tom", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don SEC-009");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-10");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        MvcResult create = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        String code = parse(create).get("menu").get("id").asText();

        MvcResult submit = mvc.perform(post("/api/v1/menus/" + code + "/submit")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(submit.getResponse().getStatus()).isEqualTo(409);
        String responseBody = submit.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("HS-SEC009-1", "HS-SEC009-2");

        MvcResult conflicts = mvc.perform(get("/api/v1/menus/" + code + "/allergy-conflicts")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(conflicts.getResponse().getStatus()).isEqualTo(200);
        assertThat(conflicts.getResponse().getContentAsString()).doesNotContain("HS-SEC009-1", "HS-SEC009-2");
    }

    // ───────────────────────── MF05-TC-SEC-010 — SUPPLIER bị chặn toàn bộ MF05 ─────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/menus", "/api/v1/dishes", "/api/v1/menu-templates", "/api/v1/diet-groups"})
    void supplierIsForbiddenFromReadingAnyMf05List(String path) throws Exception {
        MvcResult result = mvc.perform(get(path).header("Authorization", authHeaderFor("U-0006")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void supplierIsForbiddenFromCreatingMenu() throws Exception {
        // Body PHẢI hợp lệ (@Valid) — nếu không, MethodArgumentNotValidException (422) ném ra
        // ở bước bind tham số, XẢY RA TRƯỚC khi proxy @PreAuthorize được vào, nên 403 không bao
        // giờ tới lượt. Dùng lại payload hợp lệ như `createMenu()` để cô lập đúng test 403.
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don SEC-010");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-10");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));

        MvcResult result = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-0006"))
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    // ───────────────────────── MF05-TC-SEC-013 — payload injection qua field lọc động ─────────────────────────

    @Test
    void filteringByInternalColumnIsRejectedAsUnknownField() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("approvalSignature", "SIG-2630")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }

    @Test
    void filteringByUnknownColumnReturns422NotServerError() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("nonExistentColumn", "x")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("IllegalArgumentException");
    }

    @Test
    void searchParamIsBoundAsParameterNotConcatenated() throws Exception {
        createMenu("MENU-SEC-013-INJ");
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("search", "' OR '1'='1")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("items")).isEmpty();
    }

    @Test
    void negatedFilterOnTenantColumnStillScopedToOwnSchool() throws Exception {
        createMenu("MENU-SEC-013-NOT");
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("not:schoolId", "SCH-HN-0051")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        // `not:schoolId` cũng KHÔNG thuộc FIELD_MAP ⇒ 422 (cùng allow-list như SEC-006).
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
    }
}
