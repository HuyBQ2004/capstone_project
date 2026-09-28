package com.sfms.mf08.security;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-SEC-007 — không rò {@code storage_key}/đường dẫn nội bộ ra response.
 *
 * <p>⚠️ Phạm vi rút gọn so với TDS: spec đề nghị bật log DEBUG rồi grep {@code data/uploads},
 * {@code r2.cloudflarestorage}, {@code "Bearer "} trong log runtime — việc đó cần hạ tầng bắt
 * log riêng (Logback appender trong bộ nhớ) ngoài phạm vi 1 file test độc lập; test này khoá
 * chặt phần {@code có thể verify quyết định}: JSON response KHÔNG chứa field/khoá nào tên
 * {@code storageKey}/{@code checksumSha256}/đường dẫn tuyệt đối, và toàn văn response không
 * match chuỗi khoá nội bộ ({@code data/uploads}, {@code mf08/sample/}).
 */
class InfoLeakIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-LEAK";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-LEAK", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerASample() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-LEAK");
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
        return result.getResponse().getContentAsString();
    }

    @Test
    void registerResponseDoesNotLeakStorageKeyOrAbsolutePath() throws Exception {
        String rawJson = registerASample();

        assertThat(rawJson).doesNotContain("storageKey");
        assertThat(rawJson).doesNotContain("checksumSha256");
        assertThat(rawJson).doesNotContainPattern("(?i)data[\\\\/]uploads");
        assertThat(rawJson).doesNotContain("mf08/sample/");
        assertThat(rawJson).doesNotContainPattern("(?i)r2\\.cloudflarestorage");
    }

    @Test
    void detailResponseDoesNotLeakStorageKeyOrAbsolutePath() throws Exception {
        registerASample();
        MvcResult list = mvc.perform(get("/api/v1/samples").param("page", "1").param("pageSize", "8")
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        String rawJson = list.getResponse().getContentAsString();
        assertThat(rawJson).doesNotContain("storageKey");
        assertThat(rawJson).doesNotContain("checksumSha256");
        assertThat(rawJson).doesNotContainPattern("(?i)data[\\\\/]uploads");
    }
}
