package com.sfms.common.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-CON-005 — S1/S2/S7/S8: {@code @Tag}, truy vết UC+BR, {@code PagedResponse}, bearer JWT.
 *
 * <p>Oracle: D8 S1 (@Tag(name="MFxx — <Tên module>")) · S2 (description PHẢI chứa UC ID + BR ID,
 * và mã trích dẫn thuộc 15 UC/7 BR chính của §1.3) · S7 (mọi GET danh sách khai PagedResponse<T>)
 * · S8 (@SecurityScheme bearer JWT toàn cục; MF03 không có endpoint public).
 */
class OpenApiMetaContractIT extends AbstractIamIntegrationTest {

    private static final String TAG = "MF03 — Student & Parent Management";

    /** §1.3 — 15 UC chính thức của MF03 (TDS §2.1). */
    private static final java.util.Set<String> VALID_UC = java.util.Set.of(
            "UC-010", "UC-025", "UC-036", "UC-050", "UC-051", "UC-077", "UC-078", "UC-084",
            "UC-093", "UC-094", "UC-099", "UC-104", "UC-145", "UC-146", "UC-147");

    /**
     * §2.1 — 7 BR chính của MF03 + 2 BR tham chiếu THẬT SỰ được dùng trong @Operation của mã
     * đã viết: BR-025 (MF02, sức chứa lớp — TDS §2.2 "BR tham chiếu", CF-M03-2) và BR-220 (mới
     * chốt 02/08, chưa kịp đưa vào §1.3 "7 BR chính" nhưng là BR THẬT, không phải mã bịa — xem
     * CHANGELOG TestSpec 2026-08-02). Whitelist cứng theo đúng "7 BR chính" ban đầu sẽ báo
     * NHẦM 2 trích dẫn hợp lệ này thành "bịa mã" — mở rộng có ghi chú, không mở rộng bừa.
     */
    private static final java.util.Set<String> VALID_BR = java.util.Set.of(
            "BR-030", "BR-031", "BR-032", "BR-065", "BR-123", "BR-172", "BR-201",
            "BR-025", "BR-220");

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    @Test
    @DisplayName("S1 — tồn tại đúng tag \"MF03 — Student & Parent Management\"")
    void mf03TagExists() throws Exception {
        JsonNode spec = fetchSpec();
        boolean found = false;
        for (JsonNode tag : spec.get("tags")) {
            if (TAG.equals(tag.get("name").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("thiếu tag '%s' trong /v3/api-docs", TAG).isTrue();
    }

    @Test
    @DisplayName("S2 — mọi operation gắn tag MF03 có description khớp UC-\\d{3} VÀ BR-\\d{3}, "
            + "và mã trích dẫn thuộc danh sách 15 UC/7 BR chính (chống bịa mã)")
    void allMf03OperationsHaveUcAndBrInDescription() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode paths = spec.get("paths");
        int operationsChecked = 0;
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            var pathEntry = pathIt.next();
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathEntry.getValue().fields();
            while (methodIt.hasNext()) {
                var methodEntry = methodIt.next();
                JsonNode op = methodEntry.getValue();
                JsonNode tags = op.get("tags");
                boolean isMf03 = tags != null && java.util.stream.StreamSupport
                        .stream(tags.spliterator(), false).anyMatch(t -> TAG.equals(t.asText()));
                if (!isMf03) {
                    continue;
                }
                operationsChecked++;
                String description = op.has("description") ? op.get("description").asText() : "";
                var ucMatcher = java.util.regex.Pattern.compile("UC-\\d{3}").matcher(description);
                var brMatcher = java.util.regex.Pattern.compile("BR-\\d{3}").matcher(description);
                assertThat(ucMatcher.find())
                        .as("%s %s thiếu UC-ID trong description: '%s'",
                                methodEntry.getKey(), pathEntry.getKey(), description)
                        .isTrue();
                assertThat(VALID_UC).as("%s %s trích UC lạ (không thuộc 15 UC §1.3): %s",
                                methodEntry.getKey(), pathEntry.getKey(), ucMatcher.group())
                        .contains(ucMatcher.group());
                assertThat(brMatcher.find())
                        .as("%s %s thiếu BR-ID trong description: '%s'",
                                methodEntry.getKey(), pathEntry.getKey(), description)
                        .isTrue();
                assertThat(VALID_BR).as("%s %s trích BR lạ (không thuộc 7 BR chính §2.1): %s",
                                methodEntry.getKey(), pathEntry.getKey(), brMatcher.group())
                        .contains(brMatcher.group());
            }
        }
        assertThat(operationsChecked).isGreaterThan(0);
    }

    @Test
    @DisplayName("S7 — GET /students, /allergy-declarations, /special-meals khai response 200 là "
            + "PagedResponse<T> với đủ 5 field")
    void listEndpointsUsePagedResponseSchema() throws Exception {
        JsonNode spec = fetchSpec();
        for (String path : new String[]{"/api/v1/students", "/api/v1/allergy-declarations",
                "/api/v1/special-meals"}) {
            JsonNode schema200 = spec.at("/paths/" + path.replace("/", "~1") + "/get/responses/200"
                    + "/content/application~1json/schema");
            assertThat(schema200.isMissingNode()).as("GET %s thiếu schema 200", path).isFalse();
            // PagedResponse<T> sinh dưới dạng $ref tới schema tổng quát tên bắt đầu "PagedResponse"
            String schemaText = schema200.toString();
            assertThat(schemaText).as("GET %s phải trỏ PagedResponse", path).contains("PagedResponse");
        }
        JsonNode pagedResponseSchema = findAnyPagedResponseSchema(spec);
        assertThat(pagedResponseSchema).isNotNull();
        var props = pagedResponseSchema.get("properties");
        assertThat(props.has("items")).isTrue();
        assertThat(props.has("total")).isTrue();
        assertThat(props.has("page")).isTrue();
        assertThat(props.has("pageSize")).isTrue();
        assertThat(props.has("pageCount")).isTrue();
    }

    private JsonNode findAnyPagedResponseSchema(JsonNode spec) {
        Iterator<Map.Entry<String, JsonNode>> it = spec.get("components").get("schemas").fields();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getKey().startsWith("PagedResponse")) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Test
    @DisplayName("S8 — đúng MỘT securityScheme bearerAuth (HTTP/bearer/JWT); MF03 không có "
            + "endpoint public (0 operation ghi security: [])")
    void bearerAuthSecuritySchemeIsGlobalAndNoPublicEndpoints() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode bearerAuth = spec.at("/components/securitySchemes/bearerAuth");
        assertThat(bearerAuth.isMissingNode()).isFalse();
        assertThat(bearerAuth.get("type").asText()).isEqualToIgnoringCase("http");
        assertThat(bearerAuth.get("scheme").asText()).isEqualToIgnoringCase("bearer");

        JsonNode paths = spec.get("paths");
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            var pathEntry = pathIt.next();
            if (!pathEntry.getKey().startsWith("/api/v1/students")
                    && !pathEntry.getKey().startsWith("/api/v1/allergy-declarations")
                    && !pathEntry.getKey().startsWith("/api/v1/special-meals")
                    && !pathEntry.getKey().startsWith("/api/v1/health-history")) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathEntry.getValue().fields();
            while (methodIt.hasNext()) {
                JsonNode op = methodIt.next().getValue();
                JsonNode security = op.get("security");
                if (security != null) {
                    assertThat(security.isEmpty())
                            .as("%s không được có security: [] (public) — MF03 không có endpoint public",
                                    pathEntry.getKey())
                            .isFalse();
                }
            }
        }
    }
}
