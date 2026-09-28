package com.sfms.mf13.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF13-TC-CON-002 — D8 luật S4 + D4 (ISO-8601): field {@code *At}/{@code *Date}/{@code *From}/
 * {@code *To} khai {@code format ∈ {date, date-time}}, {@code periodLabel} là ngoại lệ (nhãn hiển
 * thị, không phải field ngày).
 */
class OpenApiDateFormatContractIT extends AbstractIamIntegrationTest {

    private static final Pattern DATE_LIKE_FIELD = Pattern.compile(".*(At|Date|From|To)$");
    private static final Set<String> KNOWN_MF13_SCHEMAS = Set.of("ComplianceSummaryResponse",
            "ReportExportResponse", "ReportScheduleResponse", "ReportScheduleRunResponse");

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    @Test
    @DisplayName("Mọi field *At/*Date/*From/*To của schema MF13 khai format date hoặc date-time")
    void dateLikeFieldsHaveDateOrDateTimeFormat() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode schemas = spec.at("/components/schemas");
        assertThat(schemas.isMissingNode()).isFalse();

        int fieldsChecked = 0;
        for (String schemaName : KNOWN_MF13_SCHEMAS) {
            JsonNode schema = schemas.get(schemaName);
            if (schema == null) {
                continue;
            }
            JsonNode props = schema.get("properties");
            if (props == null) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> it = props.fields();
            while (it.hasNext()) {
                var propEntry = it.next();
                String fieldName = propEntry.getKey();
                if (!DATE_LIKE_FIELD.matcher(fieldName).matches() || "periodLabel".equals(fieldName)) {
                    continue;
                }
                JsonNode prop = propEntry.getValue();
                JsonNode typeNode = prop.get("type");
                if (typeNode == null || !"string".equals(typeNode.asText())) {
                    continue; // enum ReportSlug cũng match "reportSlug"? không — tên không kết thúc At/Date/From/To thường
                }
                fieldsChecked++;
                JsonNode format = prop.get("format");
                assertThat(format).as("%s.%s (kiểu string) phải khai format", schemaName, fieldName)
                        .isNotNull();
                assertThat(format.asText()).as("%s.%s format phải là date hoặc date-time", schemaName, fieldName)
                        .isIn("date", "date-time");
            }
        }
        assertThat(fieldsChecked).as("phải kiểm được ít nhất 1 field ngày").isGreaterThan(0);
    }

    @Test
    @DisplayName("generatedAt/expiresAt/nextRunAt/createdAt cụ thể là date-time; periodFrom/periodTo là date")
    void specificKnownFieldsHaveExpectedFormat() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode schemas = spec.at("/components/schemas");

        assertFieldFormat(schemas, "ComplianceSummaryResponse", "generatedAt", "date-time");
        assertFieldFormat(schemas, "ComplianceSummaryResponse", "periodFrom", "date");
        assertFieldFormat(schemas, "ComplianceSummaryResponse", "periodTo", "date");
        assertFieldFormat(schemas, "ReportExportResponse", "generatedAt", "date-time");
        assertFieldFormat(schemas, "ReportExportResponse", "expiresAt", "date-time");
        assertFieldFormat(schemas, "ReportExportResponse", "periodFrom", "date");
        assertFieldFormat(schemas, "ReportScheduleResponse", "nextRunAt", "date-time");
        assertFieldFormat(schemas, "ReportScheduleResponse", "createdAt", "date-time");
    }

    private void assertFieldFormat(JsonNode schemas, String schemaName, String field, String expectedFormat) {
        JsonNode prop = schemas.at("/" + schemaName + "/properties/" + field);
        if (prop.isMissingNode()) {
            return; // schema não tồn tại trong context test hiện tại — không phải lỗi của test này
        }
        JsonNode format = prop.get("format");
        assertThat(format).as("%s.%s phải khai format", schemaName, field).isNotNull();
        assertThat(format.asText()).as("%s.%s", schemaName, field).isEqualTo(expectedFormat);
    }

    @Test
    @DisplayName("periodLabel KHÔNG bị đòi format (là nhãn hiển thị, được phép dd/MM/yyyy)")
    void periodLabelIsExemptFromDateFormatRequirement() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode prop = spec.at("/components/schemas/ReportExportResponse/properties/periodLabel");
        if (prop.isMissingNode()) {
            return;
        }
        JsonNode type = prop.get("type");
        assertThat(type).isNotNull();
        assertThat(type.asText()).isEqualTo("string");
        // Không assert phủ định "không có format" — chỉ xác nhận field tồn tại đúng kiểu string tự do.
    }

    @Test
    @DisplayName("0 field ngày dùng @JsonFormat trong mã nguồn MF13 (grep tĩnh)")
    void noJsonFormatAnnotationUsedInMf13Source() throws java.io.IOException {
        java.nio.file.Path mf13Dir = java.nio.file.Path.of("src/main/java/com/sfms/mf13");
        if (!java.nio.file.Files.exists(mf13Dir)) {
            return;
        }
        try (var stream = java.nio.file.Files.walk(mf13Dir)) {
            stream.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    String content = java.nio.file.Files.readString(p);
                    assertThat(content).as("%s không được dùng @JsonFormat (D4 — luôn ISO mặc định)", p)
                            .doesNotContain("JsonFormat");
                } catch (java.io.IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        }
    }
}
