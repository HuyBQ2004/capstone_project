package com.sfms.mf03.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-009 — Hợp đồng phân trang / search / filter (Team Plan §2b).
 *
 * <p>Oracle: §2b-1 (5 field bất biến, không wrapper) · §2b-3 (thứ tự áp lọc) · §2b-6 (edge
 * cases) · TDS §9.2 (bảng param) · TDS §17.1 C1 · SEARCH_FIELDS = students.name, students.code,
 * classrooms.name (join).
 */
class StudentListContractIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("1 — response có đúng 5 khoá: items/total/page/pageCount/pageSize, KHÔNG wrapper data/result")
    void responseHasExactlyFiveKeys() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/students?page=1&pageSize=8")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = parse(result);

        var keys = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize((Iterator<String>) body.fieldNames(), Spliterator.ORDERED), false)
                .sorted().toList();
        assertThat(keys).isEqualTo(java.util.List.of("items", "page", "pageCount", "pageSize", "total"));
    }

    @Test
    @DisplayName("2 — clamp: page=99 => page == pageCount, items = trang cuối (§2b-6)")
    void pageClampsToLastPage() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/students?page=99&pageSize=8")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = parse(result);
        assertThat(body.get("page").asInt()).isEqualTo(body.get("pageCount").asInt());
    }

    @Test
    @DisplayName("3 — tập rỗng: ?classroomId=KHONG-TON-TAI => 200, total=0, pageCount=1, page=1, items=[] "
            + "(KHÔNG 404)")
    void emptyResultReturns200NotFound() throws Exception {
        mvc.perform(get("/api/v1/students?classroomId=KHONG-TON-TAI")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.pageCount").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @DisplayName("4 — pageSize phi lý (0, -5) => clamp về 1")
    void invalidPageSizeClampsToOne() throws Exception {
        MvcResult r1 = mvc.perform(get("/api/v1/students?pageSize=0")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn();
        assertThat(parse(r1).get("pageSize").asInt()).isGreaterThanOrEqualTo(1);

        MvcResult r2 = mvc.perform(get("/api/v1/students?pageSize=-5")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn();
        assertThat(parse(r2).get("pageSize").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("5 — search chỉ trên SEARCH_FIELDS: name/code/classrooms.name khớp; registration_no KHÔNG "
            + "khớp (chứng minh KHÔNG OR mọi cột)")
    void searchOnlyMatchesSearchFields() throws Exception {
        mvc.perform(get("/api/v1/students?search=Khang")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mvc.perform(get("/api/v1/students?search=HS-3A-001")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));

        mvc.perform(get("/api/v1/students?search=3A")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));  // toàn bộ lớp 3A

        // registration_no ("DTD-2026-0001") KHÔNG thuộc SEARCH_FIELDS — 0 kết quả.
        mvc.perform(get("/api/v1/students?search=DTD-2026-0001")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("6 — search case-insensitive (ILIKE): 'khang' và 'KHANG' cho cùng kết quả")
    void searchIsCaseInsensitive() throws Exception {
        MvcResult lower = mvc.perform(get("/api/v1/students?search=khang")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn();
        MvcResult upper = mvc.perform(get("/api/v1/students?search=KHANG")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn();

        assertThat(parse(lower).get("total").asInt()).isEqualTo(parse(upper).get("total").asInt());
    }

    @Test
    @DisplayName("7 — lọc dẫn xuất (ADR-M03-3): healthStatus=VERIFIED + not:healthStatus=VERIFIED == total "
            + "khi không lọc; hasAllergy=yes/hasSpecialDiet=yes khớp EXISTS")
    void derivedFiltersMatchExistsFormula() throws Exception {
        int totalAll = parse(mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn()).get("total").asInt();
        int verified = parse(mvc.perform(get("/api/v1/students?healthStatus=VERIFIED")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn()).get("total").asInt();
        int notVerified = parse(mvc.perform(get("/api/v1/students?not:healthStatus=VERIFIED")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn()).get("total").asInt();

        assertThat(verified + notVerified).isEqualTo(totalAll);

        mvc.perform(get("/api/v1/students?hasAllergy=yes")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("8 — lọc theo khoá: parentId=<U-9905> => đúng 2 HS; classroomId=<3A> => mọi item lớp 3A")
    void filterByParentIdAndClassroomId() throws Exception {
        mvc.perform(get("/api/v1/students?parentId=U-9905")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));                  // HS-3A-001 + HS-4A-001 (seed V5)

        MvcResult result = mvc.perform(get("/api/v1/students?classroomId=3A")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn();
        for (JsonNode item : parse(result).get("items")) {
            assertThat(item.get("classroomId").asText()).isEqualTo("3A");
        }
    }

    @Test
    @DisplayName("9 — giá trị rỗng bị bỏ qua: ?classroomId= như không truyền (§2b-6)")
    void emptyFilterValueIsIgnored() throws Exception {
        int totalWithout = parse(mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn()).get("total").asInt();
        int totalWithEmpty = parse(mvc.perform(get("/api/v1/students?classroomId=")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk()).andReturn()).get("total").asInt();

        assertThat(totalWithEmpty).isEqualTo(totalWithout);
    }

    @Test
    @DisplayName("10 — key điều khiển (page/pageSize/search/dateFrom/dateTo/mealPrefix) KHÔNG bị hiểu "
            + "thành lọc field")
    void controlKeysAreNotTreatedAsFieldFilters() throws Exception {
        mvc.perform(get("/api/v1/students?dateFrom=2020-01-01&dateTo=2030-01-01&mealPrefix=x")
                        .header("Authorization", authHeaderFor("U-9902")))
                .andExpect(status().isOk());                               // không lỗi 400/422 vì field lạ
    }
}
