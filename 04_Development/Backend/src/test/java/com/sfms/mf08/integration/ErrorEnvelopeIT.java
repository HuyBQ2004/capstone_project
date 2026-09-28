package com.sfms.mf08.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF08-TC-INT-009 — envelope lỗi D2 (đủ 3 field, {@code details} không bao giờ null) và
 * message tiếng Anh D7.
 *
 * <p>⚠️ Ground-truth: L4/L17 của TestSpec nói TDS §10 (tiếng Việt) "chưa dịch" — grep
 * {@code error_message.md}/{@code SmpMessages.java} THẬT (đã đọc ở bước khảo sát) cho thấy 9 mã
 * dẫn xuất BR ĐÃ có message tiếng Anh chính thức (MSG-119..128) — L17 "hết treo" đúng như spec
 * tự ghi. Test assert tiếng Anh cho TẤT CẢ mã, không giữ ngoại lệ SMP-023 (MSG-128 cũng đã có
 * nguyên văn tiếng Anh, xem {@code SmpMessages.MSG_128}).
 */
class ErrorEnvelopeIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-ERR";
    private static final java.util.regex.Pattern VIETNAMESE_DIACRITIC =
            java.util.regex.Pattern.compile("[àáảãạăâđêôơưÀÁẢÃẠĂÂĐÊÔƠƯ]");

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-ERR", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private void assertEnvelope(JsonNode body, String expectedCode) {
        assertThat(body.has("error")).isTrue();
        JsonNode error = body.get("error");
        assertThat(error.fieldNames()).toIterable().containsExactlyInAnyOrder("code", "message", "details");
        assertThat(error.get("code").asText()).isEqualTo(expectedCode);
        assertThat(error.get("details").isArray()).isTrue();   // NUNCA null (D2)
        assertThat(error.get("message").asText()).doesNotMatch(".*" + VIETNAMESE_DIACRITIC.pattern() + ".*");
    }

    private Map<String, Object> validPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-ERR");
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
        return payload;
    }

    private MockMultipartFile photo() {
        return new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
    }

    @Test
    void smp019AmountTooLowHasEnglishMessageAndFullEnvelope() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.put("amount", 50);
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo())
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertEnvelope(parse(result), "SMP-019");
        assertThat(parse(result).get("error").get("message").asText())
                .isEqualTo("Food sample retention weight too low: Must be at least 100g.");
    }

    @Test
    void smp105InspectionNotPassedHasEnglishMessage() throws Exception {
        Mf08Fixtures.insertFailedInspection(jdbc, "INSP-MF08-ERR-FAIL", Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 31));
        Map<String, Object> payload = validPayload();
        payload.put("inspectionId", "INSP-MF08-ERR-FAIL");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo())
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertEnvelope(parse(result), "SMP-105");
    }

    @Test
    void smp107EvidenceRequiredHasEnglishMessage() throws Exception {
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(validPayload()).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertEnvelope(parse(result), "SMP-107");
    }

    @Test
    void smp203ContainerStateInvalidHasEnglishMessage() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.put("sanitized", false);
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo())
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertEnvelope(parse(result), "SMP-203");
    }

    @Test
    void smp111RetentionNotElapsedHasEnglishMessageWithPlaceholderFilled() throws Exception {
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(validPayload()).getBytes(StandardCharsets.UTF_8));
        MvcResult registered = mvc.perform(multipart("/api/v1/samples").file(part).file(photo())
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        String code = parse(registered).get("id").asText();

        MvcResult dispose = mvc.perform(post("/api/v1/samples/" + code + "/dispose")
                        .header("Authorization", authHeaderFor(CATERER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("witness", "Y tế", "note", "Đạt"))))
                .andReturn();

        assertThat(dispose.getResponse().getStatus()).isEqualTo(409);
        JsonNode error = parse(dispose).get("error");
        assertEnvelope(parse(dispose), "SMP-111");
        // Placeholder [Number] đã được thay bằng số giờ thật (không còn literal "[Number]").
        assertThat(error.get("message").asText()).doesNotContain("[Number]");
        assertThat(error.get("message").asText()).matches(".*\\d+ hour.*");
    }

    @Test
    void smp005AccessDeniedHasEnglishMessage() throws Exception {
        Map<String, Object> handOver = Map.of("incidentId", "INC-X", "sampleIds", java.util.List.of("SMP-X"),
                "handoverType", "LAB", "testCriteria", "x");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(handOver).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mvc.perform(multipart("/api/v1/sample-lab-requests").file(part)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertEnvelope(parse(result), "SMP-005");
        assertThat(parse(result).get("error").get("message").asText())
                .isEqualTo("Access denied: You do not have permission to access this resource.");
    }
}
