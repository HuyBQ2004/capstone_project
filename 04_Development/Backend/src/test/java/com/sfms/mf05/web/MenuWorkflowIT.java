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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF05 — nhóm test lõi BR-061/063/067/068, D1/D5 định danh, hợp đồng phân trang (§2b),
 * unauthenticated. Đối chiếu {@code AbstractIamIntegrationTest} clock cố định tại
 * {@code 2026-07-30T02:00:00Z}.
 */
class MenuWorkflowIT extends AbstractIamIntegrationTest {

    @BeforeEach
    void linkCaterer() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
    }

    private Map<String, Object> baseCreatePayload(String menuDateIso) {
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
        body.put("name", "Thuc don test");
        body.put("gradeId", gradeId);
        body.put("menuDateIso", menuDateIso);
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        return body;
    }

    // ───────────────────────── MF05-TC-001/002 — BR-061 lead time ─────────────────────────

    @Test
    void submitAtExactly3DayLeadTimeSucceeds() throws Exception {
        // Clock cố định 2026-07-30 -> +3 ngày = 2026-08-02
        Map<String, Object> body = baseCreatePayload("2026-08-02");
        String code = createMenu(body);

        MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/submit")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body2 = parse(result);
        assertThat(body2.get("status").asText()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    void submitAt2DayLeadTimeIsBlocked() throws Exception {
        Map<String, Object> body = baseCreatePayload("2026-08-01");   // +2 ngày
        String code = createMenu(body);

        MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/submit")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("MENU-061");
    }

    // ───────────────────────── MF05-TC-003 — BR-063 allergy conflict ─────────────────────────

    @Test
    void submitWithBlockingAllergyConflictIsBlocked() throws Exception {
        UUID student1 = Mf05Fixtures.insertStudent(jdbc, "HS-ALLG-1", "3A", Mf05Fixtures.SCHOOL_CODE);
        UUID student2 = Mf05Fixtures.insertStudent(jdbc, "HS-ALLG-2", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student1, "Tôm");
        Mf05Fixtures.insertVerifiedAllergy(jdbc, student2, "Tôm");

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "canh-bi-do-nau-tom", "portions", 300)));
        Map<String, Object> body = baseCreatePayload(LocalDate.of(2026, 8, 5).toString());
        body.put("blocksData", List.of(block));
        String code = createMenu(body);

        MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/submit")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        var error = parse(result).get("error");
        assertThat(error.get("code").asText()).isEqualTo("MENU-051");
        assertThat(error.get("message").asText()).contains("2 students");
    }

    // ───────────────────────── Happy path — submit → approve → publish ─────────────────────────

    @Test
    void happyPathLifecycle() throws Exception {
        String code = createMenu(baseCreatePayload("2026-08-05"));

        MvcResult submitResult = mvc.perform(post("/api/v1/menus/" + code + "/submit")
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(submitResult.getResponse().getStatus()).isEqualTo(200);

        MvcResult approveResult = mvc.perform(post("/api/v1/menus/" + code + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-0001"))
                        .content(toJson(Map.of("approvalSignature", "SIG-TEST-01"))))
                .andReturn();
        assertThat(approveResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(approveResult).get("status").asText()).isEqualTo("APPROVED");

        MvcResult publishResult = mvc.perform(post("/api/v1/menus/" + code + "/publish")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(publishResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(publishResult).get("status").asText()).isEqualTo("PUBLISHED");

        String status = jdbc.queryForObject("select status from menus where code = ?", String.class, code);
        assertThat(status).isEqualTo("PUBLISHED");
        Boolean approvedAtNotNull = jdbc.queryForObject(
                "select approved_at is not null from menus where code = ?", Boolean.class, code);
        assertThat(approvedAtNotNull).isTrue();
    }

    // ───────────────────────── MF05-TC-005/TC-INT-007 — BR-067 khoá PUBLISHED ─────────────────────────

    @Test
    void publishedMenuCannotBeEditedOrDeleted() throws Exception {
        String code = createMenu(baseCreatePayload("2026-08-05"));
        mvc.perform(post("/api/v1/menus/" + code + "/submit")
                .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)));
        mvc.perform(post("/api/v1/menus/" + code + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authHeaderFor("U-0001"))
                .content(toJson(Map.of("approvalSignature", "SIG-X"))));
        mvc.perform(post("/api/v1/menus/" + code + "/publish").header("Authorization", authHeaderFor("U-0001")));

        MvcResult putResult = mvc.perform(put("/api/v1/menus/" + code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("name", "hacked"))))
                .andReturn();
        assertThat(putResult.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(putResult).get("error").get("code").asText()).isEqualTo("MENU-067");

        MvcResult deleteResult = mvc.perform(delete("/api/v1/menus/" + code)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)))
                .andReturn();
        assertThat(deleteResult.getResponse().getStatus()).isEqualTo(409);

        String name = jdbc.queryForObject("select name from menus where code = ?", String.class, code);
        assertThat(name).isNotEqualTo("hacked");
    }

    // ───────────────────────── MF05-TC-INT-012 — PUT là merge một phần (C8) ─────────────────────────

    @Test
    void putIsPartialMergeNotReplace() throws Exception {
        String code = createMenu(baseCreatePayload("2026-08-05"));
        // Quyết định 06/08/2026 (MenuServiceImpl#computeServings) — `servings` KHÔNG còn nhận
        // nguyên giá trị client gửi (kể cả qua PUT); backend LUÔN tự tính lại từ số HS thật.
        // Baseline lấy ngay sau tạo (đã qua computeServings ở createDraft) — PUT gửi 999 phải
        // KHÔNG đổi giá trị này (đúng hợp đồng partial-merge cho MỌI field khác, nhưng
        // `servings` là field khoá — server tính lại ra CÙNG kết quả, không nhận input).
        Integer servingsBeforePut =
                jdbc.queryForObject("select servings from menus where code = ?", Integer.class, code);

        MvcResult putResult = mvc.perform(put("/api/v1/menus/" + code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(Map.of("servings", 999))))
                .andReturn();
        assertThat(putResult.getResponse().getStatus()).isEqualTo(200);

        // name/grade/blocksData không gửi -> PHẢI giữ nguyên.
        String name = jdbc.queryForObject("select name from menus where code = ?", String.class, code);
        assertThat(name).isEqualTo("Thuc don test");
        Long dishCount = jdbc.queryForObject(
                "select count(*) from menu_dishes md join menus m on md.menu_id = m.id where m.code = ?",
                Long.class, code);
        assertThat(dishCount).isEqualTo(1L);
        Integer servings = jdbc.queryForObject("select servings from menus where code = ?", Integer.class, code);
        assertThat(servings).isEqualTo(servingsBeforePut);   // KHÔNG phải 999 — server khoá field này
    }

    // ───────────────────────── D5/C10 — id trùng -> 409; vắng -> tự sinh ─────────────────────────

    @Test
    void duplicateClientIdReturns409() throws Exception {
        Map<String, Object> body = baseCreatePayload("2026-08-05");
        body.put("id", "MENU-DUP-TEST-01");
        MvcResult first = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        assertThat(first.getResponse().getStatus()).isEqualTo(201);

        MvcResult second = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        assertThat(second.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(second).get("error").get("code").asText()).isEqualTo("MENU-409");
    }

    @Test
    void absentIdGeneratesServerCode() throws Exception {
        String code = createMenu(baseCreatePayload("2026-08-05"));
        assertThat(code).matches("MENU-\\d{4}-\\d+");
    }

    // ───────────────────────── §2b — hợp đồng phân trang ─────────────────────────

    @Test
    void emptyResultStillReturns200WithClampedPaging() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("gradeId", UUID.randomUUID().toString())
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("items")).isEmpty();
        assertThat(body.get("total").asLong()).isEqualTo(0L);
        assertThat(body.get("page").asInt()).isEqualTo(1);
        assertThat(body.get("pageCount").asInt()).isEqualTo(1);
    }

    // ───────────────────────── SEC-001 — không JWT -> 401 ─────────────────────────

    @Test
    void noJwtReturns401() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    // ───────────────────────── SEC-002/003 — sai vai trò -> 403 ─────────────────────────

    @Test
    void parentCannotCreateMenu() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-0005"))
                        .content(toJson(baseCreatePayload("2026-08-05"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    void teacherCannotApprove() throws Exception {
        String code = createMenu(baseCreatePayload("2026-08-05"));
        mvc.perform(post("/api/v1/menus/" + code + "/submit")
                .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)));

        MvcResult result = mvc.perform(post("/api/v1/menus/" + code + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-0004"))
                        .content(toJson(Map.of("approvalSignature", "x"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    // ───────────────────────── SEC-004 — Caterer khác đơn vị -> 403 ─────────────────────────

    @Test
    void catererFromAnotherPartnerCannotEditMenu() throws Exception {
        String code = createMenu(baseCreatePayload("2026-08-05"));

        // U-0003 là service-caterers KHÔNG có partnerId (khác CAT-001 của người tạo) -> 403.
        MvcResult result = mvc.perform(put("/api/v1/menus/" + code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-0003"))
                        .content(toJson(Map.of("name", "đổi bởi người khác"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    // ───────────────────────── Trợ giúp ─────────────────────────

    private String createMenu(Map<String, Object> body) throws Exception {
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
