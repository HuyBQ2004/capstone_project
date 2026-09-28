package com.sfms.mf08.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-INT-008 — {@code id} công khai là mã nghiệp vụ, không UUID (D1).
 */
class PublicIdContractIT extends AbstractIamIntegrationTest {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-D1";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-D1", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerASample() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-D1");
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
        return parse(result).get("id").asText();
    }

    @Test
    void getByBusinessCodeReturns200() throws Exception {
        String code = registerASample();

        MvcResult result = mvc.perform(get("/api/v1/samples/" + code).header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void getByInternalUuidReturns404NotFoundBecauseUuidIsNotAPublicIdentifier() throws Exception {
        registerASample();
        String uuid = jdbc.queryForObject("SELECT id::text FROM food_samples LIMIT 1", String.class);

        MvcResult result = mvc.perform(get("/api/v1/samples/" + uuid).header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    /**
     * ⚠️ Ground-truth: D1 nói "id công khai = mã nghiệp vụ" cho MỌI resource, nhưng
     * {@code sample_attachments} là NGOẠI LỆ có chủ đích (xem javadoc {@code SampleAttachment}):
     * bảng này KHÔNG có cột {@code code}, {@code UploadedFileDto.id()} = UUID nội bộ của bản
     * ghi attachment. Test loại trừ đúng field đó (mảng {@code evidencePhotos[].id}) trước khi
     * khẳng định "không UUID nào khác lộ ra" — không xoá cả bài test vì phần còn lại (id mẫu,
     * dishId, inspectionId) vẫn phải là mã nghiệp vụ.
     */
    @Test
    void noUuidAppearsAnywhereExceptAttachmentIdsInListOrDetailJson() throws Exception {
        registerASample();

        MvcResult list = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        MvcResult detail = mvc.perform(get("/api/v1/samples/" + registerSecondSample())
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(UUID_PATTERN.matcher(stripAttachmentIds(parse(list))).find()).isFalse();
        assertThat(UUID_PATTERN.matcher(stripAttachmentIds(parse(detail))).find()).isFalse();
    }

    private String stripAttachmentIds(JsonNode root) {
        com.fasterxml.jackson.databind.node.ArrayNode items = root.has("items")
                ? (com.fasterxml.jackson.databind.node.ArrayNode) root.get("items")
                : new com.fasterxml.jackson.databind.node.ArrayNode(
                        com.fasterxml.jackson.databind.node.JsonNodeFactory.instance).add(root);
        for (JsonNode item : items) {
            if (item.has("evidencePhotos") && item.get("evidencePhotos").isArray()) {
                ((com.fasterxml.jackson.databind.node.ArrayNode) item.get("evidencePhotos")).forEach(
                        photo -> ((com.fasterxml.jackson.databind.node.ObjectNode) photo).remove("id"));
            }
        }
        return root.toString();
    }

    private String registerSecondSample() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-D1-B", "Canh bí đỏ");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-D1-B");
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
        return parse(result).get("id").asText();
    }

    @Test
    void foreignKeysInResponseAreBusinessCodesNotUuids() throws Exception {
        String code = registerASample();

        JsonNode body = parse(mvc.perform(get("/api/v1/samples/" + code)
                        .header("Authorization", authHeaderFor(CATERER))).andReturn());

        assertThat(body.get("dishId").asText()).isEqualTo("DISH-MF08-D1");
        assertThat(body.get("inspectionId").asText()).isEqualTo(INSPECTION_CODE);
        assertThat(UUID_PATTERN.matcher(body.get("dishId").asText()).matches()).isFalse();
        assertThat(UUID_PATTERN.matcher(body.get("inspectionId").asText()).matches()).isFalse();
    }
}
