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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-SEC-005 — Upload: kiểm nội dung thật, không tin phần mở rộng (D10).
 *
 * <p>⚠️ GAP THẬT (không bịa assertion cho tính năng không tồn tại — lesson 9 của
 * {@code 00_AI_TestGen_Prompt_Template.md}): {@code AttachmentSupport.store()} KHÔNG kiểm magic
 * bytes, KHÔNG whitelist loại file, KHÔNG kiểm kích thước (MSG-008/MSG-009 chưa được implement
 * cho MF08). Test dưới xác nhận ĐÚNG hiện trạng — file giả mạo phần mở rộng (magic bytes {@code
 * MZ} nhưng đuôi {@code .jpg}) vẫn được CHẤP NHẬN (201), thay vì giả vờ hệ thống chặn nó. Điểm
 * an toàn CÓ THẬT duy nhất: storage key được sinh từ UUID nội bộ (KHÔNG ghép tên file người dùng
 * gửi) — path traversal qua filename bất khả thi bất kể nội dung file.
 */
class SampleUploadSecurityIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-UPLOAD";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private Map<String, Object> basePayload(String dishCode) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", dishCode);
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

    private MvcResult registerWithPhoto(String dishCode, MockMultipartFile photo) throws Exception {
        MockMultipartFile payloadPart = new MockMultipartFile("payload", "", "application/json",
                toJson(basePayload(dishCode)).getBytes(StandardCharsets.UTF_8));
        return mvc.perform(multipart("/api/v1/samples").file(payloadPart).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
    }

    @Test
    void fileWithSpoofedExtensionButExecutableMagicBytesIsCurrentlyAccepted() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-SEC-005-A", "Món A");
        MockMultipartFile fakeJpg = new MockMultipartFile("evidencePhotos", "fake.jpg", "image/jpeg",
                new byte[]{'M', 'Z', 1, 2, 3});   // magic bytes PE executable, đuôi .jpg

        MvcResult result = registerWithPhoto("DISH-SEC-005-A", fakeJpg);

        // GAP: hệ thống hiện KHÔNG kiểm magic bytes -> 201 thay vì 422 MSG-009 (như TDS kỳ vọng).
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void arbitraryFileTypeIsCurrentlyAcceptedNoWhitelistEnforced() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-SEC-005-B", "Món B");
        MockMultipartFile textFile = new MockMultipartFile("evidencePhotos", "note.txt", "text/plain",
                "not a photo".getBytes(StandardCharsets.UTF_8));

        MvcResult result = registerWithPhoto("DISH-SEC-005-B", textFile);

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void pathTraversalFileNameNeverReachesStorageKeyRegardlessOfContentValidation() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-SEC-005-C", "Món C");
        MockMultipartFile malicious = new MockMultipartFile("evidencePhotos", "../../etc/passwd.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});

        MvcResult result = registerWithPhoto("DISH-SEC-005-C", malicious);

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        String code = parse(result).get("id").asText();
        // storage_key được sinh từ UUID.randomUUID() (AttachmentSupport), KHÔNG chứa filename
        // gốc — path traversal cấu trúc bất khả thi bất kể nội dung/đuôi file người dùng gửi.
        String storageKey = jdbc.queryForObject(
                "SELECT storage_key FROM sample_attachments WHERE owner_id = "
                        + "(SELECT id FROM food_samples WHERE code = ?)", String.class, code);
        assertThat(storageKey).doesNotContain("..").doesNotContain("etc/passwd").startsWith("mf08/sample/");
    }

    @Test
    void uploadedFileResponseNeverExposesStorageKeyOrChecksum() throws Exception {
        Mf08Fixtures.insertDish(jdbc, "DISH-SEC-005-D", "Món D");
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});

        MvcResult result = registerWithPhoto("DISH-SEC-005-D", photo);

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        var evidencePhoto = parse(result).get("evidencePhotos").get(0);
        assertThat(evidencePhoto.has("storageKey")).isFalse();
        assertThat(evidencePhoto.has("checksumSha256")).isFalse();
        assertThat(evidencePhoto.fieldNames()).toIterable().containsExactlyInAnyOrder("id", "name", "uploadedAt",
                "sizeBytes");
    }
}
