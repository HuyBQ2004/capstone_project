package com.sfms.mf06.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf06.fixture.Mf06Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF06-TC-SEC-016 — 🆕 D10-BIS: ảnh bằng chứng/chứng nhận lô là tài liệu RIÊNG TƯ (TestSpec-MF06
 * §4.2). Phạm vi Bước 2 hiện thực: bước 1 (chặn tên file trong {@code rejectDocumentIds}),
 * bước 2/3 (không lộ storageKey/URL công khai qua response), bước 4 (magic-bytes chặn SVG giả
 * PDF ở {@code POST /supplier-batches/{code}/certificates}). Không có endpoint upload tài liệu
 * riêng-tư CHUNG (ngoài certificates) trong phạm vi Bước 2 này — xem báo cáo cuối.
 */
class Mf06DocumentPrivacyIT extends AbstractIamIntegrationTest {

    private String deliveryCode;
    private String batchCode;
    private String ingredientCode;

    @BeforeEach
    void seed() {
        UUID schoolId = Mf06Fixtures.schoolId(jdbc);
        UUID partnerId = Mf06Fixtures.partnerId(jdbc, Mf06Fixtures.SUPPLIER_APPROVED_CODE);
        UUID ingredientId = Mf06Fixtures.insertIngredient(jdbc, "ING-TEST-SEC016", "Test Doc", "kg", "CHILLED", 0.0);
        ingredientCode = "ING-TEST-SEC016";
        UUID productId = Mf06Fixtures.insertSupplierProduct(jdbc, "SP-TEST-SEC016", partnerId, ingredientId);
        Mf06Fixtures.insertSupplierBatch(jdbc, "BATCH-TEST-SEC016", partnerId, productId, BigDecimal.valueOf(10), "kg",
                LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 2), LocalDate.of(2026, 8, 8));
        batchCode = "BATCH-TEST-SEC016";
        Mf06Fixtures.insertDelivery(jdbc, "DLV-TEST-SEC016", schoolId);
        deliveryCode = "DLV-TEST-SEC016";
    }

    /** Bước 1 — rejectDocumentIds mang TÊN FILE (không phải id tài liệu) => 4xx, không đoán ý. */
    @Test
    void rejectDocumentIdsRejectsFilenames() throws Exception {
        String body = """
                {"deliveryId": "%s", "lat": 21.0122, "lng": 105.7801, "photosCount": 3,
                 "lines": [ { "supplierBatchId": "%s", "ingredientId": "%s", "storageZone": "CHILLED",
                              "rejected": true, "rejectReason": "Rau dap", "rejectDocumentIds": ["rej-4412-1.jpg"] } ]}
                """.formatted(deliveryCode, batchCode, ingredientCode);
        MvcResult result = mvc.perform(post("/api/v1/receiving-sessions")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
    }

    /** Bước 2/3 — id tài liệu hợp lệ được chấp nhận; response KHÔNG lộ storageKey/URL công khai. */
    @Test
    void acceptedResponseNeverLeaksStorageKeysOrPublicUrls() throws Exception {
        String body = """
                {"deliveryId": "%s", "lat": 21.0122, "lng": 105.7801, "photosCount": 3,
                 "lines": [ { "supplierBatchId": "%s", "ingredientId": "%s", "storageZone": "CHILLED",
                              "rejected": true, "rejectReason": "Rau dap nat",
                              "rejectDocumentIds": ["DOC-RCV-TEST-01"] } ]}
                """.formatted(deliveryCode, batchCode, ingredientCode);
        MvcResult createResult = mvc.perform(post("/api/v1/receiving-sessions")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        assertThat(createResult.getResponse().getStatus()).isEqualTo(201);
        String code = parse(createResult).get("id").asText();

        MvcResult detailResult = mvc.perform(get("/api/v1/receiving-sessions/" + code)
                        .header("Authorization", authHeaderFor("U-0003")))
                .andReturn();
        String detailBody = detailResult.getResponse().getContentAsString();
        assertThat(detailBody).doesNotContainIgnoringCase("http");
        assertThat(detailBody).doesNotContain("cdn.");
        assertThat(detailBody).doesNotContain("/uploads/");

        // DB lưu storageKey ở reject_photo_keys (JSONB), KHÔNG phải cột response — xác nhận trực tiếp ở DB.
        String storedKeys = jdbc.queryForObject(
                "select reject_photo_keys::text from receiving_lines rl join receiving_sessions rs on rs.id = rl.session_id where rs.code = ?",
                String.class, code);
        assertThat(storedKeys).contains("DOC-RCV-TEST-01");
    }

    /** Bước 4 — file .svg đổi tên .pdf (magic bytes SVG) => 400 + MSG-009 nguyên văn. */
    @Test
    void magicByteValidationBlocksRenamedSvg() throws Exception {
        byte[] svgBytesRenamedAsPdf = "<svg xmlns='http://www.w3.org/2000/svg'><script>alert(1)</script></svg>"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf", svgBytesRenamedAsPdf);

        MvcResult result = mvc.perform(multipart("/api/v1/supplier-batches/" + batchCode + "/certificates")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-0006")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        var body = parse(result);
        assertThat(body.get("error").get("message").asText())
                .isEqualTo("File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.");
    }
}
