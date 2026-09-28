package com.sfms.mf02.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF02-TC-INT-009 — mọi {@code GET} danh sách trả đúng 5 field, {@code page} clamp,
 * {@code pageCount ≥ 1} (Team Plan §2b-1/§2b-6). MF02-TC-INT-011 — D1: {@code id} = mã
 * nghiệp vụ, KHÔNG rò UUID ra response.
 */
class ClassroomListIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("MF02-TC-INT-009 — GET /classrooms trả đủ 5 field phân trang; 10 lớp seed (§3 FX-011..018)")
    void pagedResponseHasFiveFieldsAndCorrectShape() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/classrooms?page=1&pageSize=8")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = parse(result);
        assertThat(body.has("items")).isTrue();
        assertThat(body.has("total")).isTrue();
        assertThat(body.has("page")).isTrue();
        assertThat(body.has("pageSize")).isTrue();
        assertThat(body.has("pageCount")).isTrue();
        // FX-011..015 (10 lớp AY-2025) + FX-016/017 (2 lớp AY-2026) = 12 lớp của SCH-HN-0042
        // (GET /classrooms không lọc theo năm học — TDS §9.1 không tham số hoá theo AY).
        assertThat(body.get("total").asLong()).isEqualTo(12);
        assertThat(body.get("pageCount").asInt()).isEqualTo(2);
        assertThat(body.get("items")).hasSize(8);
    }

    @Test
    @DisplayName("MF02-TC-INT-009 (edge case §2b-6) — page vượt pageCount ⇒ clamp về trang cuối")
    void pageBeyondPageCountIsClamped() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/classrooms?page=99&pageSize=8")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = parse(result);
        assertThat(body.get("page").asInt()).isEqualTo(2); // pageCount=2 với 10 bản ghi/pageSize=8
    }

    @Test
    @DisplayName("MF02-TC-INT-011 — D1: id trả về là code ('3A'), không phải UUID")
    void identityIsBusinessCodeNotUuid() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/classrooms/3A")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = parse(result);
        assertThat(body.get("id").asText()).isEqualTo("3A");
        assertThat(body.toString()).doesNotContainPattern(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("MF02-TC-INT-011 — GET /classrooms/{uuid nội bộ} ⇒ 404 (path param phải là code)")
    void pathParamAsInternalUuidReturns404() throws Exception {
        mvc.perform(get("/api/v1/classrooms/00000000-0000-0000-0000-000000000001")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isNotFound());
    }
}
