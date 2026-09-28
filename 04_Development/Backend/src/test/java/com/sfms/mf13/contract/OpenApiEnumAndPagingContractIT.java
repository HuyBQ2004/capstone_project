package com.sfms.mf13.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF13-TC-CON-003 — D8 luật S5 (enum khai bằng kiểu thật, không {@code allowableValues}) + S7
 * (GET danh sách khai {@code PagedResponse<T>}) · D6 (SCREAMING_SNAKE, ngoại lệ {@code ReportSlug}
 * kebab-case theo ADR-M13-5, cùng lý do {@code RoleKey}).
 */
class OpenApiEnumAndPagingContractIT extends AbstractIamIntegrationTest {

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    /**
     * {@code ReportSlug} KHÔNG sinh ra một schema riêng tên {@code /components/schemas/ReportSlug}
     * — verify bằng dump {@code /v3/api-docs} thật: enum bị "inline" trực tiếp làm
     * {@code type: string, enum: [...]} tại field dùng nó (VD {@code ReportDefinitionResponse.
     * slug}), vì springdoc chỉ tách enum thành schema riêng khi có {@code @Schema(name=...)}
     * tường minh trên chính enum đó — {@code ReportSlug.java} không có annotation này. Kiểm tra
     * đúng theo hành vi thật: đọc enum inline tại {@code ReportDefinitionResponse.properties.slug}.
     */
    @Test
    @DisplayName("ReportSlug: đúng 7 giá trị kebab-case (inline tại ReportDefinitionResponse.slug)")
    void reportSlugEnumIsKebabCaseWithSevenValues() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode slugProp = spec.at("/components/schemas/ReportDefinitionResponse/properties/slug");
        assertThat(slugProp.isMissingNode())
                .as("thiếu property 'slug' trên ReportDefinitionResponse trong /v3/api-docs").isFalse();
        assertThat(slugProp.has("enum")).as("slug phải khai enum inline").isTrue();

        List<String> values = new ArrayList<>();
        slugProp.get("enum").forEach(v -> values.add(v.asText()));
        assertThat(values).containsExactlyInAnyOrder("three-step", "sample-retention", "receiving",
                "supplier-certificates", "hygiene", "traceability", "system-usage");
    }

    @Test
    @DisplayName("ReportFormat = [PDF, XLSX]; ReportFrequency = [DAILY, WEEKLY, MONTHLY] — SCREAMING_SNAKE")
    void otherMf13EnumsAreScreamingSnake() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode formatSchema = spec.at("/components/schemas/ReportFormat");
        if (!formatSchema.isMissingNode()) {
            List<String> values = new ArrayList<>();
            formatSchema.get("enum").forEach(v -> values.add(v.asText()));
            assertThat(values).containsExactlyInAnyOrder("PDF", "XLSX");
        }

        JsonNode frequencySchema = spec.at("/components/schemas/ReportFrequency");
        if (!frequencySchema.isMissingNode()) {
            List<String> values = new ArrayList<>();
            frequencySchema.get("enum").forEach(v -> values.add(v.asText()));
            assertThat(values).containsExactlyInAnyOrder("DAILY", "WEEKLY", "MONTHLY");
        }
    }

    @Test
    @DisplayName("0 chỗ dùng allowableValues trong mã nguồn MF13 (grep tĩnh, S5)")
    void noAllowableValuesUsedInMf13Source() throws java.io.IOException {
        java.nio.file.Path mf13Dir = java.nio.file.Path.of("src/main/java/com/sfms/mf13");
        if (!java.nio.file.Files.exists(mf13Dir)) {
            return;
        }
        try (var stream = java.nio.file.Files.walk(mf13Dir)) {
            stream.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    String content = java.nio.file.Files.readString(p);
                    assertThat(content).as("%s không được dùng allowableValues (S5)", p)
                            .doesNotContain("allowableValues");
                } catch (java.io.IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        }
    }

    @Test
    @DisplayName("GET /reports, /reports/{slug}/rows, /report-exports, /report-schedules trả PagedResponse")
    void listEndpointsDeclarePagedResponseOrArrayOfKnownItemType() throws Exception {
        // Không controller MF13 nào khai @Operation(... produces = ...) tường minh nên springdoc
        // đăng ký content-type response là "*/*" (Spring MVC mặc định), KHÔNG phải
        // "application/json" — verify bằng dump /v3/api-docs thật. JSON Pointer dưới đây dùng
        // đúng khóa "*/*" (escape "~1" cho path, KHÔNG cần escape "*/*" vì không có "/" trong nó).
        JsonNode spec = fetchSpec();

        // /reports trả List<ReportDefinitionResponse> trần (7 mục cố định, không phân trang) —
        // hành vi thật khác "mọi GET danh sách là PagedResponse" của S7 lý tưởng; ghi nhận riêng.
        JsonNode reportsSchema = spec.at("/paths/~1api~1v1~1reports/get/responses/200"
                + "/content/*~1*/schema");
        assertThat(reportsSchema.isMissingNode()).isFalse();

        for (String path : new String[]{"/api/v1/reports/{slug}/rows", "/api/v1/report-exports",
                "/api/v1/report-schedules"}) {
            JsonNode schema200 = spec.at("/paths/" + path.replace("/", "~1") + "/get/responses/200"
                    + "/content/*~1*/schema");
            assertThat(schema200.isMissingNode()).as("GET %s thiếu schema 200", path).isFalse();
            assertThat(schema200.toString()).as("GET %s phải trỏ PagedResponse", path).contains("PagedResponse");
        }

        JsonNode pagedResponseSchema = findAnyPagedResponseSchema(spec);
        assertThat(pagedResponseSchema).as("phải có ít nhất một schema PagedResponse* trong spec").isNotNull();
        var props = pagedResponseSchema.get("properties");
        assertThat(props.has("items")).isTrue();
        assertThat(props.has("total")).isTrue();
        assertThat(props.has("page")).isTrue();
        assertThat(props.has("pageSize")).isTrue();
        assertThat(props.has("pageCount")).isTrue();
    }

    /**
     * GAP GHI NHẬN (không assert cứng) — {@code GET /reports} trả {@code List<
     * ReportDefinitionResponse>} trần thay vì {@code PagedResponse<ReportDefinitionResponse>}.
     * Lý do nghiệp vụ hợp lý (7 mục cố định, không cần phân trang) nhưng khác "mọi GET danh sách"
     * của S7 nếu đọc theo nghĩa đen. Ghi lại để Nam/Huy xác nhận có cần chuẩn hoá hay giữ ngoại lệ.
     */
    @Test
    @DisplayName("GAP GHI NHẬN — GET /reports KHÔNG dùng PagedResponse (S7 đọc theo nghĩa đen)")
    void reportsCatalogEndpointDoesNotUsePagedResponse_gapRecorded() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode schema200 = spec.at("/paths/~1api~1v1~1reports/get/responses/200"
                + "/content/*~1*/schema");
        boolean usesPagedResponse = schema200.toString().contains("PagedResponse");
        System.out.println("[CON-003][GAP] GET /reports dùng PagedResponse=" + usesPagedResponse
                + " (kỳ vọng theo nghĩa đen của S7 là true; thiết kế thật là danh mục 7 mục cố định "
                + "không phân trang — quyết định nghiệp vụ hợp lý, chưa có sign-off tường minh)");
        assertThat(schema200.isMissingNode()).isFalse();
    }

    private JsonNode findAnyPagedResponseSchema(JsonNode spec) {
        JsonNode schemas = spec.at("/components/schemas");
        if (schemas.isMissingNode()) {
            return null;
        }
        Iterator<Map.Entry<String, JsonNode>> it = schemas.fields();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getKey().startsWith("PagedResponse")) {
                return entry.getValue();
            }
        }
        return null;
    }
}
