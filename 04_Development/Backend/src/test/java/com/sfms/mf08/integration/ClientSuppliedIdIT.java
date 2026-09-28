package com.sfms.mf08.integration;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-INT-010 — client gửi {@code id}: dùng làm {@code code}, trùng thì 409 (D5).
 */
class ClientSuppliedIdIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-D5";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-D5-1", "Cơm trắng");
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-D5-2", "Canh bí đỏ");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private Map<String, Object> payload(String clientId, String dishCode) {
        Map<String, Object> p = new LinkedHashMap<>();
        if (clientId != null) {
            p.put("id", clientId);
        }
        p.put("inspectionId", INSPECTION_CODE);
        p.put("dishId", dishCode);
        p.put("mealType", "LUNCH");
        p.put("mealShift", "Bữa trưa");
        p.put("servings", 100);
        p.put("amount", 120);
        p.put("unit", "G");
        p.put("container", "Hộp inox");
        p.put("sanitized", true);
        p.put("sealed", true);
        p.put("fridgeTemp", 4.2);
        p.put("collectedAt", "2026-07-30T01:00:00Z");
        p.put("storageLocation", "Tủ 1");
        return p;
    }

    private MvcResult register(Map<String, Object> body) throws Exception {
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(body).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        return mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
    }

    @Test
    void clientSuppliedIdIsUsedAsCode() throws Exception {
        MvcResult result = register(payload("SMP-20260707-99", "DISH-MF08-D5-1"));

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(parse(result).get("id").asText()).isEqualTo("SMP-20260707-99");
        assertThat(count("SELECT count(*) FROM food_samples WHERE code = 'SMP-20260707-99'")).isEqualTo(1L);
    }

    @Test
    void duplicateClientSuppliedIdIsRejectedWith409() throws Exception {
        assertThat(register(payload("SMP-20260707-77", "DISH-MF08-D5-1")).getResponse().getStatus())
                .isEqualTo(201);

        Mf08Fixtures.insertPassedInspection(jdbc, "INSP-MF08-D5-B", Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 31));
        Map<String, Object> second = payload("SMP-20260707-77", "DISH-MF08-D5-2");
        second.put("inspectionId", "INSP-MF08-D5-B");   // tránh đụng uq_smp_inspection_dish, chỉ test trùng id
        MvcResult result = register(second);

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("SMP-409");
        assertThat(count("SELECT count(*) FROM food_samples WHERE code = 'SMP-20260707-77'")).isEqualTo(1L);
    }

    @Test
    void absentIdIsServerGeneratedInStandardFormat() throws Exception {
        MvcResult result = register(payload(null, "DISH-MF08-D5-1"));

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        String code = parse(result).get("id").asText();
        assertThat(code).matches("^SMP-\\d{8}-\\d{2}$");
        assertThat(count("SELECT count(*) FROM food_samples WHERE code = ?", code)).isEqualTo(1L);
    }

    @Test
    void twoRegistrationsWithoutIdGetDifferentServerGeneratedCodes() throws Exception {
        String first = parse(register(payload(null, "DISH-MF08-D5-1"))).get("id").asText();

        Mf08Fixtures.insertPassedInspection(jdbc, "INSP-MF08-D5-C", Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 31));
        Map<String, Object> secondPayload = payload(null, "DISH-MF08-D5-1");
        secondPayload.put("inspectionId", "INSP-MF08-D5-C");
        String second = parse(register(secondPayload)).get("id").asText();

        assertThat(first).isNotEqualTo(second);
    }
}
