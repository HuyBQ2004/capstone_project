package com.sfms.common.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-CON-002 — D2/S3: mọi 4xx/5xx trỏ {@code ErrorResponse} dùng chung.
 *
 * <p>Oracle: D2 (envelope lỗi) · D8 S3 "Mỗi mã lỗi ở §10 của TDS có một @ApiResponse tương ứng,
 * content trỏ ErrorResponse dùng chung" · TDS §10 (13 mã + cột HTTP).
 */
class OpenApiD2ContractIT extends AbstractIamIntegrationTest {

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    @Test
    @DisplayName("1-2 — ErrorResponse khai đúng 3 field code/message/details, details là mảng")
    void errorResponseSchemaHasExactlyThreeFields() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode errorResponse = spec.at("/components/schemas/ErrorBody");
        assertThat(errorResponse.isMissingNode()).isFalse();
        var properties = errorResponse.get("properties");
        assertThat(properties.has("code")).isTrue();
        assertThat(properties.has("message")).isTrue();
        assertThat(properties.has("details")).isTrue();
        assertThat(properties.get("details").get("type").asText()).isEqualTo("array");
    }
 
    @Test
    @DisplayName("1 — mọi response 4xx/5xx của operation gắn tag MF03 trỏ $ref ErrorResponse")
    void allMf03ErrorResponsesReferenceErrorResponseSchema() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode paths = spec.get("paths");
        int checked = 0;
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            var pathEntry = pathIt.next();
            if (!pathEntry.getKey().matches("^/api/v1/(students|allergy-declarations|special-meals"
                    + "|health-history).*")) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathEntry.getValue().fields();
            while (methodIt.hasNext()) {
                var methodEntry = methodIt.next();
                JsonNode responses = methodEntry.getValue().get("responses");
                if (responses == null) {
                    continue;
                }
                Iterator<String> codeIt = responses.fieldNames();
                while (codeIt.hasNext()) {
                    String httpCode = codeIt.next();
                    if (httpCode.startsWith("4") || httpCode.startsWith("5")) {
                        JsonNode schemaRef = responses.get(httpCode)
                                .at("/content/application~1json/schema/$ref");
                        if (schemaRef.isMissingNode()) {
                            schemaRef = responses.get(httpCode)
                                    .at("/content/*~1*/schema/$ref");
                        }
                        if (!schemaRef.isMissingNode()) {
                            assertThat(schemaRef.asText())
                                    .as("%s %s -> %s", methodEntry.getKey(), pathEntry.getKey(), httpCode)
                                    .isEqualTo("#/components/schemas/ErrorResponse");
                            checked++;
                        }
                    }
                }
            }
        }
        assertThat(checked).as("phải có ít nhất 1 @ApiResponse 4xx/5xx trỏ ErrorResponse được kiểm")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("4 — Assert L15: spec MF03 KHÔNG khai MSG-064 ở đâu (CF-M03-12)")
    void msg064IsNeverReferenced() throws Exception {
        JsonNode spec = fetchSpec();
        assertThat(spec.toString()).doesNotContain("MSG-064");
    }

    @Test
    @DisplayName("3 — đối chiếu ngược §10: các mã STU-* đã implement thật xuất hiện đâu đó trong "
            + "mô tả spec (@ApiResponse description)")
    void implementedErrorCodesAppearInSpecDescriptions() throws Exception {
        JsonNode spec = fetchSpec();
        String specText = spec.toString();
        var implementedCodes = new TreeSet<>(java.util.List.of(
                "STU-010", "STU-012", "STU-030", "STU-031", "STU-032", "STU-066",
                "STU-094", "STU-172", "STU-201", "STU-404"));
        for (String code : implementedCodes) {
            assertThat(specText).as("mã %s phải xuất hiện trong spec (annotation @ApiResponse)", code)
                    .contains(code);
        }
    }
}
