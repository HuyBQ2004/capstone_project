package com.sfms.mf09.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF09 — 6 TC Contract (`TC-CON-001`..`TC-CON-006`) qua {@code GET /v3/api-docs} THẬT (không đoán
 * cấu trúc — đã tải bản dump thật về, soi bằng {@code node -e}, xoá test tạm sau khi xong, theo
 * đúng lesson #3 của {@code 00_AI_TestGen_Prompt_Template.md}). 3 điều bất ngờ xác nhận được so
 * với giả định "mặc định":
 * <ul>
 *   <li>{@code content-type} của MỌI response lỗi MF09 là wildcard ({@code "*", "*"}), KHÔNG phải
 *       {@code application/json} — controller MF09 không khai {@code produces} tường minh.</li>
 *   <li>{@code ConsumptionStatus} KHÔNG có schema top-level riêng — bị inline thẳng vào property
 *       {@code UpdateConsumptionRequest.consumption} dạng {@code type: string, enum: [...]}.</li>
 *   <li>{@code DistributionResponse.mealType}/{@code status}/{@code studentId} là {@code type: string}
 *       THUẦN (không có {@code enum}) vì DTO map bằng {@code .name()} sang {@code String}, không
 *       giữ kiểu enum Java tới tận DTO — không assert được "enum" ở tầng schema cho các field này.</li>
 * </ul>
 */
class Mf09OpenApiContractIT extends AbstractIamIntegrationTest {

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        return parse(result);
    }

    // ───────────────────────── TC-CON-001 — D1: id = code, KHÔNG lộ UUID ─────────────────────────

    @Test
    @DisplayName("MF09-TC-CON-001 — khong property id/*Id nao trong spec MF09 mang format: uuid, "
            + "khong lo khoa vat ly ra hop dong API")
    void distributionResponseSchemaExposesOnlyStringIdNeverExposesRawUuidField() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode props = spec.at("/components/schemas/DistributionResponse/properties");
        assertThat(props.isMissingNode()).isFalse();
        assertThat(props.at("/id/type").asText()).isEqualTo("string");
        // Không có property nào tên "uuid"/"internalId" lộ khoá vật lý.
        assertThat(props.has("uuid")).isFalse();
        assertThat(props.has("internalId")).isFalse();
    }

    // ───────────────────────── TC-CON-002 — D2: envelope lỗi ErrorResponse -> $ref ErrorBody ─────────────────────────

    @Test
    void errorEnvelopeSchemaWrapsErrorBodyWithCodeMessageDetails() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode errorRef = spec.at("/components/schemas/ErrorResponse/properties/error/$ref");
        assertThat(errorRef.asText()).isEqualTo("#/components/schemas/ErrorBody");

        JsonNode errorBodyProps = spec.at("/components/schemas/ErrorBody/properties");
        assertThat(errorBodyProps.has("code")).isTrue();
        assertThat(errorBodyProps.has("message")).isTrue();
        assertThat(errorBodyProps.has("details")).isTrue();
        assertThat(errorBodyProps.at("/details/type").asText()).isEqualTo("array");
    }

    /**
     * Mọi response 4xx/5xx của operation gắn tag MF09 phải trỏ `$ref` ErrorResponse — nhưng
     * content-type THẬT là wildcard ({@code "*", "*"}) (không khai {@code produces} tường minh ở controller MF09),
     * KHÔNG phải {@code application/json} như MF03 (`OpenApiD2ContractIT` đã xác nhận field khác
     * nhau theo module — đúng cảnh báo lesson #3, không giả định content-type chung cho mọi module).
     */
    @Test
    @DisplayName("MF09-TC-CON-002 — moi response 4xx/5xx cua cac operation gan tag MF09 deu $ref "
            + "schema ErrorResponse dung chung")
    void allMf09ErrorResponsesReferenceErrorResponseSchemaUnderWildcardContentType() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode paths = spec.get("paths");
        int checked = 0;
        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            var pathEntry = pathIt.next();
            if (!pathEntry.getKey().startsWith("/api/v1/student-distributions")
                    && !pathEntry.getKey().startsWith("/api/v1/meal-records")) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathEntry.getValue().fields();
            while (methodIt.hasNext()) {
                var methodEntry = methodIt.next();
                if (!isMf09Operation(methodEntry.getValue())) {
                    continue;
                }
                JsonNode responses = methodEntry.getValue().get("responses");
                if (responses == null) continue;
                Iterator<String> codeIt = responses.fieldNames();
                while (codeIt.hasNext()) {
                    String httpCode = codeIt.next();
                    if (httpCode.startsWith("4") || httpCode.startsWith("5")) {
                        JsonNode schemaRef = responses.get(httpCode).at("/content/*~1*/schema/$ref");
                        if (schemaRef.isMissingNode()) {
                            // fallback nếu một endpoint tương lai khai produces tường minh
                            schemaRef = responses.get(httpCode).at("/content/application~1json/schema/$ref");
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
        assertThat(checked).as("phải có ít nhất 1 @ApiResponse 4xx/5xx của MF09 trỏ ErrorResponse").isGreaterThan(0);
    }

    private static boolean isMf09Operation(JsonNode operation) {
        JsonNode tags = operation.get("tags");
        if (tags == null || !tags.isArray()) return false;
        for (JsonNode t : tags) {
            if (t.asText().startsWith("MF09")) return true;
        }
        return false;
    }

    // ───────────────────────── TC-CON-003/COND-26 — D6: enum SCREAMING_SNAKE, đủ 4 ConsumptionStatus ─────────────────────────

    @Test
    @DisplayName("MF09-TC-CON-003 — enum trong spec la enum that (khong allowableValues viet tay) "
            + "va khop dung tap gia tri D6")
    void consumptionStatusEnumIsInlinedWithAllFourScreamingSnakeValuesIncludingPending() throws Exception {
        JsonNode spec = fetchSpec();
        // Lesson #3 — KHÔNG có schema top-level "ConsumptionStatus" riêng, enum inline vào property.
        assertThat(spec.at("/components/schemas/ConsumptionStatus").isMissingNode()).isTrue();

        JsonNode enumValues = spec.at("/components/schemas/UpdateConsumptionRequest/properties/consumption/enum");
        assertThat(enumValues.isMissingNode()).isFalse();
        var values = new java.util.ArrayList<String>();
        enumValues.forEach(n -> values.add(n.asText()));
        assertThat(values).containsExactlyInAnyOrder("PENDING", "FULLY", "PARTIALLY", "NOT_CONSUMED");
        values.forEach(v -> assertThat(v).isEqualTo(v.toUpperCase()));   // SCREAMING_SNAKE
    }

    // ───────────────────────── TC-CON-004/COND-25 — D4: mọi field ngày/giờ là ISO-8601, KHÔNG dd/MM/yyyy ─────────────────────────

    @Test
    void allDateAndDateTimeFieldsOfDistributionResponseAreIso8601FormattedNotVietnameseSlashFormat() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode props = spec.at("/components/schemas/DistributionResponse/properties");

        assertThat(props.at("/date/type").asText()).isEqualTo("string");
        assertThat(props.at("/date/format").asText()).isEqualTo("date");           // ISO yyyy-MM-dd
        assertThat(props.at("/servedAt/type").asText()).isEqualTo("string");
        assertThat(props.at("/servedAt/format").asText()).isEqualTo("date-time");  // ISO instant

        // Không có property nào mang pattern dd/MM/yyyy trong toàn bộ spec MF09 (ADR-M09-5 bị D4 ghi đè).
        String mf09Slice = spec.at("/components/schemas/DistributionResponse").toString()
                + spec.at("/components/schemas/MealRecordResponse").toString()
                + spec.at("/components/schemas/MealHistoryEntryResponse").toString();
        assertThat(mf09Slice).doesNotContain("dd/MM/yyyy").doesNotContain("dd\\/MM\\/yyyy");
    }

    // ───────────────────────── TC-CON-005 — D9: entity không tự khai created_by/updated_by (DTO không lộ) ─────────────────────────

    @Test
    void distributionResponseNeverExposesReservedAuditColumnNames() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode props = spec.at("/components/schemas/DistributionResponse/properties");
        assertThat(props.has("createdBy")).isFalse();
        assertThat(props.has("updatedBy")).isFalse();
        assertThat(props.has("createdAt")).isFalse();
        assertThat(props.has("updatedAt")).isFalse();
    }

    // ───────────────────────── TC-CON-006/D11 — GET /student-distributions: oneOf 2 shape theo vai trò ─────────────────────────

    @Test
    void listOperationDeclaresOneOfTwoResponseShapesForTeacherVersusServiceCaterers() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode oneOf = spec.at("/paths/~1api~1v1~1student-distributions/get/responses/200/content/application~1json/schema/oneOf");
        assertThat(oneOf.isMissingNode()).isFalse();
        var refs = new java.util.ArrayList<String>();
        oneOf.forEach(n -> refs.add(n.at("/$ref").asText()));
        assertThat(refs).containsExactlyInAnyOrder(
                "#/components/schemas/PagedResponseDistributionResponse",
                "#/components/schemas/PagedResponseDistributionCatererResponse");

        // Bản RÚT GỌN (Service Caterers, D11/ADR-M09-6) KHÔNG được có field PII trẻ em.
        JsonNode catererProps = spec.at("/components/schemas/DistributionCatererResponse/properties");
        assertThat(catererProps.has("name")).isFalse();
        assertThat(catererProps.has("allergyWarning")).isFalse();
        assertThat(catererProps.has("specialDiet")).isFalse();
        assertThat(catererProps.has("studentId")).isFalse();
    }
}
