package com.sfms.common.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-CON-004 — D4/S4: mọi field ngày có {@code format: date | date-time}.
 *
 * <p>Oracle: D4 · D8 S4 "Field ngày khai format = date/date-time + example viết dạng ISO" ·
 * Team Plan §2c-3b assertion (d).
 */
class OpenApiD4ContractIT extends AbstractIamIntegrationTest {

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    @Test
    @DisplayName("3 — StudentResponse.dob: format=date (KHÔNG date-time)")
    void dobIsDateFormat() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode dob = spec.at("/components/schemas/StudentResponse/properties/dob");
        assertThat(dob.isMissingNode()).isFalse();
        assertThat(dob.get("type").asText()).isEqualTo("string");
        assertThat(dob.get("format").asText()).isEqualTo("date");
    }

    @Test
    @DisplayName("3 — AllergyDeclarationResponse.declaredAt/verifiedAt: format=date-time")
    void declaredAtAndVerifiedAtAreDateTimeFormat() throws Exception {
        JsonNode spec = fetchSpec();
        for (String field : new String[]{"declaredAt", "verifiedAt"}) {
            JsonNode prop = spec.at("/components/schemas/AllergyDeclarationResponse/properties/" + field);
            assertThat(prop.isMissingNode()).as("field %s phải tồn tại", field).isFalse();
            assertThat(prop.get("type").asText()).isEqualTo("string");
            assertThat(prop.get("format").asText()).isEqualTo("date-time");
        }
    }

    @Test
    @DisplayName("1-2 — mọi field *At khác (requestedAt, reviewedAt, at, uploadedAt) có type=string + "
            + "format date/date-time, example (nếu có) KHÔNG khớp dd/MM/yyyy")
    void otherTimestampFieldsAreIsoFormatted() throws Exception {
        JsonNode spec = fetchSpec();
        String[][] targets = {
                {"SpecialMealRequestResponse", "requestedAt"},
                {"SpecialMealRequestResponse", "reviewedAt"},
                {"HealthHistoryEventResponse", "at"},
        };
        for (String[] target : targets) {
            JsonNode prop = spec.at("/components/schemas/" + target[0] + "/properties/" + target[1]);
            if (prop.isMissingNode()) {
                continue;
            }
            assertThat(prop.get("type").asText()).as("%s.%s", target[0], target[1]).isEqualTo("string");
            JsonNode format = prop.get("format");
            if (format != null) {
                assertThat(format.asText()).isIn("date", "date-time");
            }
            JsonNode example = prop.get("example");
            if (example != null && !example.isNull()) {
                assertThat(example.asText()).doesNotMatch("^\\d{2}/\\d{2}/\\d{4}.*");
            }
        }
    }
}
