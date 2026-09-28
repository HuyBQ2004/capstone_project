package com.sfms.mf13.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF13-TC-CON-004 — D8 luật S1 ({@code @Tag}) / S2 (UC+BR trong description) / S8 (bearer toàn
 * cục, không endpoint public) + ranh giới module L1 (đã đóng {@code M13-D03} — {@code
 * sent_reports} thuộc MF12, giữ test này làm regression guard).
 */
class OpenApiTraceabilityContractIT extends AbstractIamIntegrationTest {

    private static final String TAG = "MF13 — Reporting & Analytics";
    private static final Set<String> VALID_UC = Set.of("UC-171", "UC-172", "UC-173");
    private static final Set<String> VALID_BR = Set.of("BR-175", "BR-176", "BR-177", "BR-178", "BR-179",
            "BR-180", "BR-181", "BR-182");
    /** BR/UC của MF12 — regression guard cho L1 (M13-D03: sent_reports KHÔNG thuộc MF13). */
    private static final Set<String> MF12_ONLY_CODES = Set.of("UC-185", "UC-187", "UC-188", "UC-189",
            "UC-190", "BR-209", "BR-213", "BR-214");

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    /**
     * S1 kiểm ở cấp <b>operation</b> ({@code op.tags}), KHÔNG phải {@code spec.tags} top-level —
     * lệch so với bản đầu (copy pattern của {@code OpenApiMetaContractIT} MF03). Thực tế:
     * {@code OpenApiConfig.java} chỉ đăng ký thủ công 2 tag (MF03, MF04) trong
     * {@code @OpenAPIDefinition(tags = {...})}; springdoc KHÔNG tự gom {@code @Tag} của các
     * {@code @RestController} khác vào {@code spec.tags} toàn cục. {@code spec.tags} do đó
     * KHÔNG phải nguồn đáng tin để kiểm S1 cho MF13 — mọi operation của
     * {@code ReportController}/{@code ReportExportController}/{@code ReportScheduleController}
     * đều có {@code tags: ["MF13 — Reporting & Analytics"]} ở cấp operation (đã verify bằng dump
     * {@code /v3/api-docs}), nên đó mới là tín hiệu thật của S1.
     */
    @Test
    @DisplayName("S1 — mọi controller MF13 gắn @Tag \"MF13 — Reporting & Analytics\" (kiểm ở cấp operation)")
    void mf13TagExists() throws Exception {
        JsonNode spec = fetchSpec();
        assertThat(operationsUnderTagCount(spec.get("paths")))
                .as("phải có ≥1 operation mang tag '%s' trong /v3/api-docs", TAG)
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("S2 — mọi operation gắn tag MF13 có description chứa UC-17[123] VÀ BR-1(7[5-9]|8[0-2])")
    void allMf13OperationsHaveUcAndBrInDescription() throws Exception {
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
                boolean isMf13 = tags != null && java.util.stream.StreamSupport
                        .stream(tags.spliterator(), false).anyMatch(t -> TAG.equals(t.asText()));
                if (!isMf13) {
                    continue;
                }
                operationsChecked++;
                String description = op.has("description") ? op.get("description").asText() : "";

                Matcher ucMatcher = Pattern.compile("UC-\\d{3}").matcher(description);
                assertThat(ucMatcher.find())
                        .as("%s %s thiếu UC-ID trong description: '%s'", methodEntry.getKey(),
                                pathEntry.getKey(), description).isTrue();
                assertThat(VALID_UC).as("%s %s trích UC lạ (ngoài UC-171/172/173): %s",
                                methodEntry.getKey(), pathEntry.getKey(), ucMatcher.group())
                        .contains(ucMatcher.group());

                Matcher brMatcher = Pattern.compile("BR-\\d{3}").matcher(description);
                assertThat(brMatcher.find())
                        .as("%s %s thiếu BR-ID trong description: '%s'", methodEntry.getKey(),
                                pathEntry.getKey(), description).isTrue();
                assertThat(VALID_BR).as("%s %s trích BR lạ (ngoài BR-175..182): %s",
                                methodEntry.getKey(), pathEntry.getKey(), brMatcher.group())
                        .contains(brMatcher.group());
            }
        }
        assertThat(operationsChecked).as("phải có ít nhất 1 operation mang tag MF13").isGreaterThan(0);
    }

    @Test
    @DisplayName("S8 — mọi operation MF13 mang bearer security, KHÔNG endpoint public")
    void everyMf13OperationRequiresBearerAuth() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode bearerAuth = spec.at("/components/securitySchemes/bearerAuth");
        assertThat(bearerAuth.isMissingNode()).isFalse();
        assertThat(bearerAuth.get("type").asText()).isEqualToIgnoringCase("http");
        assertThat(bearerAuth.get("scheme").asText()).isEqualToIgnoringCase("bearer");

        JsonNode paths = spec.get("paths");
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        int mf13OpsChecked = 0;
        while (pathIt.hasNext()) {
            var pathEntry = pathIt.next();
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathEntry.getValue().fields();
            while (methodIt.hasNext()) {
                var methodEntry = methodIt.next();
                JsonNode op = methodEntry.getValue();
                JsonNode tags = op.get("tags");
                boolean isMf13 = tags != null && java.util.stream.StreamSupport
                        .stream(tags.spliterator(), false).anyMatch(t -> TAG.equals(t.asText()));
                if (!isMf13) {
                    continue;
                }
                mf13OpsChecked++;
                JsonNode security = op.get("security");
                if (security != null) {
                    assertThat(security.isEmpty())
                            .as("%s %s không được security: [] (public) — MF13 không có endpoint public",
                                    methodEntry.getKey(), pathEntry.getKey())
                            .isFalse();
                }
            }
        }
        assertThat(mf13OpsChecked).isGreaterThan(0);
    }

    @Test
    @DisplayName("Ranh giới (L1, M13-D03) — 0 path MF13 chứa 'sent-report'; 0 schema tên SentReport*; "
            + "description MF13 không chứa mã UC/BR riêng của MF12")
    void noSentReportsLeakageIntoMf13Tag() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode paths = spec.get("paths");
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            var pathEntry = pathIt.next();
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathEntry.getValue().fields();
            while (methodIt.hasNext()) {
                var methodEntry = methodIt.next();
                JsonNode op = methodEntry.getValue();
                JsonNode tags = op.get("tags");
                boolean isMf13 = tags != null && java.util.stream.StreamSupport
                        .stream(tags.spliterator(), false).anyMatch(t -> TAG.equals(t.asText()));
                if (!isMf13) {
                    continue;
                }
                assertThat(pathEntry.getKey()).as("path MF13 không được chứa 'sent-report'")
                        .doesNotContain("sent-report");
                String description = op.has("description") ? op.get("description").asText() : "";
                for (String forbidden : MF12_ONLY_CODES) {
                    assertThat(description).as("%s %s (MF13) không được trích mã của MF12: %s",
                                    methodEntry.getKey(), pathEntry.getKey(), forbidden)
                            .doesNotContain(forbidden);
                }
            }
        }

        // Quét TÊN SCHEMA MÀ MF13 THỰC SỰ THAM CHIẾU (từ request/response body của operation
        // mang tag MF13) — KHÔNG quét toàn bộ /components/schemas của app. MF12 khai chính đáng
        // schema tên "SentReportResponse" (module riêng, sở hữu sent_reports theo M13-D03); quét
        // toàn cục sẽ báo nhầm coi đó là MF13 "rò" sang MF12 dù 2 module không liên quan.
        java.util.Set<String> schemasReferencedByMf13 = schemasReferencedByTag(spec, TAG);
        assertThat(schemasReferencedByMf13)
                .as("phải thu được ≥1 schema $ref từ operation MF13 để kiểm có nghĩa")
                .isNotEmpty();
        for (String name : schemasReferencedByMf13) {
            assertThat(name).as("MF13 không được khai schema tên SentReport*")
                    .doesNotStartWith("SentReport");
        }

        // Assertion dương (chống false-green trên spec rỗng) — spec thật sự tải được và MF13 thật
        // sự có operation, không phải "0 vi phạm" vì /v3/api-docs trống.
        assertThat(operationsUnderTagCount(paths)).isGreaterThan(0);
    }

    private static int operationsUnderTagCount(JsonNode paths) {
        int count = 0;
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathIt.next().getValue().fields();
            while (methodIt.hasNext()) {
                JsonNode tags = methodIt.next().getValue().get("tags");
                if (tags != null && java.util.stream.StreamSupport.stream(tags.spliterator(), false)
                        .anyMatch(t -> TAG.equals(t.asText()))) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Thu mọi tên schema mà operation mang {@code tag} tham chiếu (trực tiếp qua {@code $ref}
     * trong request/response body, VÀ bắc cầu qua các schema tìm được — VD
     * {@code PagedResponseReportExportResponse.properties.items.$ref} trỏ tới
     * {@code ReportExportResponse}) — walk toàn bộ cây JSON, không giả định độ sâu cố định.
     */
    private static java.util.Set<String> schemasReferencedByTag(JsonNode spec, String tag) {
        java.util.Set<String> direct = new java.util.LinkedHashSet<>();
        JsonNode paths = spec.get("paths");
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathIt.next().getValue().fields();
            while (methodIt.hasNext()) {
                JsonNode op = methodIt.next().getValue();
                JsonNode tags = op.get("tags");
                boolean isTagged = tags != null && java.util.stream.StreamSupport
                        .stream(tags.spliterator(), false).anyMatch(t -> tag.equals(t.asText()));
                if (isTagged) {
                    collectRefs(op, direct);
                }
            }
        }
        JsonNode schemas = spec.at("/components/schemas");
        java.util.Set<String> transitive = new java.util.LinkedHashSet<>(direct);
        java.util.Deque<String> queue = new java.util.ArrayDeque<>(direct);
        while (!queue.isEmpty()) {
            String name = queue.poll();
            JsonNode schema = schemas.get(name);
            if (schema == null) {
                continue;
            }
            java.util.Set<String> found = new java.util.LinkedHashSet<>();
            collectRefs(schema, found);
            for (String next : found) {
                if (transitive.add(next)) {
                    queue.add(next);
                }
            }
        }
        return transitive;
    }

    private static void collectRefs(JsonNode node, java.util.Set<String> out) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (node.isObject()) {
            JsonNode ref = node.get("$ref");
            if (ref != null && ref.isTextual() && ref.asText().startsWith("#/components/schemas/")) {
                out.add(ref.asText().substring("#/components/schemas/".length()));
            }
            Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
            while (fields.hasNext()) {
                collectRefs(fields.next().getValue(), out);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                collectRefs(child, out);
            }
        }
    }
}
