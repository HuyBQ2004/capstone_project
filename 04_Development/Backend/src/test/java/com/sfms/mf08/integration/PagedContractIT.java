package com.sfms.mf08.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-INT-007 — hợp đồng phân trang §2b trên {@code GET /samples}: 5 field, clamp, tập rỗng
 * ⇒ 200 không 404, {@code search} chỉ trên {@code SEARCH_FIELDS}, lọc {@code mealType}/ngày.
 */
class PagedContractIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-PAGED";

    @BeforeEach
    void seed() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-PAGED-1", "Cơm trắng");
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-PAGED-2", "Canh bí đỏ");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
        registerSample("DISH-MF08-PAGED-1", "2026-07-06T01:00:00Z");
    }

    private void registerSample(String dishCode, String collectedAt) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", dishCode);
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", collectedAt);
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile payloadPart = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(payloadPart).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void listHasExactlyFivePagingFieldsNoWrapper() throws Exception {
        JsonNode body = parse(mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .header("Authorization", authHeaderFor(CATERER))).andReturn());

        assertThat(body.fieldNames()).toIterable().containsExactlyInAnyOrder(
                "items", "total", "page", "pageSize", "pageCount");
    }

    @Test
    void pageBeyondPageCountIsClampedNotFourOhFour() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "99").param("pageSize", "8")
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = parse(result);
        assertThat(body.get("page").asInt()).isEqualTo(body.get("pageCount").asInt());
    }

    @Test
    void emptyResultSetIsTwoHundredWithPageCountAtLeastOne() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .param("status", "DISPOSED").param("dateFrom", "2030-01-01")
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = parse(result);
        assertThat(body.get("items")).isEmpty();
        assertThat(body.get("total").asLong()).isZero();
        assertThat(body.get("page").asInt()).isEqualTo(1);
        assertThat(body.get("pageCount").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void pageSizeZeroIsClampedToAtLeastOne() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "0")
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).get("pageSize").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void dateFromDateToFiltersOnCollectedAt() throws Exception {
        registerSample("DISH-MF08-PAGED-2", "2026-07-10T01:00:00Z");

        MvcResult result = mvc.perform(get("/api/v1/samples")
                        .param("page", "1").param("pageSize", "8")
                        .param("dateFrom", "2026-07-06").param("dateTo", "2026-07-06")
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        JsonNode body = parse(result);
        assertThat(body.get("total").asLong()).isEqualTo(1L);
        assertThat(body.get("items").get(0).get("dish").asText()).isEqualTo("Cơm trắng");
    }

    @Test
    void mealTypeFilterIsExactEnumMatch() throws Exception {
        MvcResult matching = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .param("mealType", "LUNCH").header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(parse(matching).get("total").asLong()).isEqualTo(1L);

        MvcResult notMatching = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .param("mealType", "BREAKFAST").header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(parse(notMatching).get("total").asLong()).isZero();
    }

    @Test
    void searchOnContainerFieldFindsNothingBecauseNotInSearchFields() throws Exception {
        // "container" KHÔNG thuộc SEARCH_FIELDS (chỉ code/dishName/collectedBy, M08-D12).
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .param("search", "inox").header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(parse(result).get("total").asLong()).isZero();
    }

    @Test
    void searchOnCollectedByFieldMatchesRegisteringCatererName() throws Exception {
        // U-0013 = "Hoàng Minh Đức" (collectedBy) — collected_by CÓ trong SEARCH_FIELDS.
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .param("search", "Minh Đức").header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(parse(result).get("total").asLong()).isEqualTo(1L);
    }

    @Test
    void blankFieldFilterIsIgnoredNotAppliedAsPredicate() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .param("status", "").header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).get("total").asLong()).isEqualTo(1L);
    }
}
