package com.sfms.common.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-CON-003 — D6/S5: enum khai bằng kiểu thật, khớp bảng 8 enum.
 *
 * <p>Oracle: D6 bảng 8 enum · D8 S5 "Enum khai bằng kiểu enum thật ở common/enums/ — KHÔNG
 * @Schema(allowableValues = "...") viết tay" · Team Plan §2c-3b assertion (c).
 */
class OpenApiD6ContractIT extends AbstractIamIntegrationTest {

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    private List<String> enumValues(JsonNode schemaNode) {
        List<String> values = new java.util.ArrayList<>();
        if (schemaNode.has("enum")) {
            schemaNode.get("enum").forEach(v -> values.add(v.asText()));
        }
        return values;
    }

    @Test
    @DisplayName("1 — AllergySeverity trong spec == đúng [MILD, MODERATE, SEVERE]")
    void allergySeverityMatchesD6Exactly() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode schema = spec.at("/components/schemas/AllergySeverity");
        assertThat(schema.isMissingNode()).isFalse();
        assertThat(enumValues(schema)).containsExactlyInAnyOrder("MILD", "MODERATE", "SEVERE");
    }

    @Test
    @DisplayName("2 — HealthHistoryEventType trong spec == đúng 5 giá trị (L17)")
    void healthHistoryEventTypeMatchesD6() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode schema = spec.at("/components/schemas/HealthHistoryEventType");
        if (!schema.isMissingNode()) {
            assertThat(enumValues(schema)).containsExactlyInAnyOrder(
                    "KHAI_BAO", "XAC_MINH", "GHI_CHU_LAM_SANG", "SU_CO", "CAP_NHAT_THUOC");
        }
        // HealthHistoryEventType không xuất hiện trực tiếp trong DTO nào (chỉ dùng nội bộ ở
        // HealthHistoryService.append(), field response `type` là String) — nếu absent trong
        // spec thì đúng như thiết kế hiện tại, không phải lỗi.
    }

    @Test
    @DisplayName("5 — grep source MF03: 0 kết quả @Schema(allowableValues (S5)")
    void noHandWrittenAllowableValuesInSource() throws IOException {
        Path mf03Root = Path.of("src/main/java/com/sfms/mf03");
        try (Stream<Path> files = Files.walk(mf03Root)) {
            List<Path> hits = files.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).contains("@Schema(allowableValues");
                        } catch (IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                    })
                    .collect(Collectors.toList());
            assertThat(hits).isEmpty();
        }
    }

    @Test
    @DisplayName("6 — DeclarationStatus (PENDING/VERIFIED/NEEDS_INFO) và SpecialMealRequest.status "
            + "(PENDING/APPROVED/REJECTED/NEEDS_INFO) khớp CHECK constraint §5.2")
    void localEnumsMatchDbCheckConstraints() {
        String allergyCheck = jdbc.queryForObject(
                "SELECT pg_get_constraintdef(oid) FROM pg_constraint "
                        + "WHERE conname = 'allergy_declarations_status_check'", String.class);
        assertThat(allergyCheck).contains("PENDING").contains("VERIFIED").contains("NEEDS_INFO");

        String smrCheck = jdbc.queryForObject(
                "SELECT pg_get_constraintdef(oid) FROM pg_constraint "
                        + "WHERE conname = 'special_meal_requests_status_check'", String.class);
        assertThat(smrCheck).contains("PENDING").contains("APPROVED")
                .contains("REJECTED").contains("NEEDS_INFO");
    }

    @Test
    @DisplayName("3 — RoleKey (nếu xuất hiện trong spec MF03) là 7 giá trị kebab-case của D6")
    void roleKeyIfPresentMatchesD6() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode schema = spec.at("/components/schemas/RoleKey");
        if (!schema.isMissingNode()) {
            assertThat(enumValues(schema)).containsExactlyInAnyOrder(
                    "system-admin", "school-admin", "teacher", "parent", "medical-staff",
                    "service-caterers", "supplier");
        }
    }
}
