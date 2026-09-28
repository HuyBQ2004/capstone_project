package com.sfms.mf08.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-SEC-009 — D11: ba vai trò (school-admin, medical-staff, service-caterers) đọc được
 * {@code GET /samples} phải thấy CÙNG tập field (khác biệt là ở tập DÒNG theo {@code schoolId},
 * không phải tập field) — M08-D13. Khuôn phỏng theo {@code Mf06RoleShapeContractIT}.
 */
class RoleFieldParityIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String MEDICAL = "U-0002";
    private static final String SCHOOL_ADMIN = "U-0001";
    private static final String INSPECTION_CODE = "INSP-MF08-PARITY";

    @BeforeEach
    void seed() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-PARITY", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-PARITY");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", "2026-07-30T01:00:00Z");
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile payloadPart = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(payloadPart).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void allThreeRolesSeeIdenticalFieldSetForSampleList() throws Exception {
        List<TreeMap<String, String>> fieldTypeMaps = new ArrayList<>();
        for (String userCode : List.of(SCHOOL_ADMIN, MEDICAL, CATERER)) {
            MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                            .header("Authorization", authHeaderFor(userCode)))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(200);
            JsonNode items = parse(result).get("items");
            assertThat(items).as(userCode + " items").isNotEmpty();
            TreeMap<String, String> keysWithTypes = new TreeMap<>();
            items.get(0).fields().forEachRemaining(e -> keysWithTypes.put(e.getKey(), e.getValue().getNodeType().name()));
            fieldTypeMaps.add(keysWithTypes);
        }
        TreeMap<String, String> reference = fieldTypeMaps.get(0);
        for (TreeMap<String, String> other : fieldTypeMaps) {
            // So cả TÊN lẫn KIỂU của từng field — không chỉ tên (yêu cầu rõ của TestSpec).
            assertThat(other).as("field name+type set must be identical across all 3 roles").isEqualTo(reference);
        }
    }

    @Test
    void allThreeRolesSeeSameTotalCountForSameSchoolData() throws Exception {
        long total = -1;
        for (String userCode : List.of(SCHOOL_ADMIN, MEDICAL, CATERER)) {
            MvcResult result = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                            .header("Authorization", authHeaderFor(userCode)))
                    .andReturn();
            JsonNode body = parse(result);
            assertThat(body.has("items")).isTrue();
            assertThat(body.has("total")).isTrue();
            assertThat(body.has("page")).isTrue();
            assertThat(body.has("pageSize")).isTrue();
            assertThat(body.has("pageCount")).isTrue();
            if (total == -1) {
                total = body.get("total").asLong();
            } else {
                assertThat(body.get("total").asLong()).as(userCode + " total").isEqualTo(total);
            }
        }
    }

    @Test
    void openApiDeclaresPagedResponsePrefixedSchemaWithTypedItemsAndNoOneOf() throws Exception {
        MvcResult result = mvc.perform(get("/v3/api-docs")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String body = result.getResponse().getContentAsString();

        assertThat(body).contains("PagedResponseFoodSampleResponse");
        assertThat(body).contains("PagedResponseSampleLabRequestResponse");
        assertThat(body).contains("PagedResponseReceivingAuthorityResponse");
        // S7 — items gõ đúng $ref tới FoodSampleResponse, không phải "{}" (PagedResponse<?> trần).
        int idx = body.indexOf("\"PagedResponseFoodSampleResponse\"");
        assertThat(idx).isGreaterThan(-1);

        // M08-D13 — MF08 KHÔNG khai oneOf nào (không có nhánh rút gọn theo role).
        assertThat(body).doesNotContain("FoodSampleResponsePublic");
    }
}
