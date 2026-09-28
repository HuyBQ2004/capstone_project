package com.sfms.mf06.contract;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf06.fixture.Mf06Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF06-TC-INT-051 — 🆕 D12: field DẪN XUẤT và field SAI TÊN/KIỂU phải KHÔNG được ghi vào DB
 * (TestSpec-MF06 §4.2). Bẫy Jackson "200 mà không lưu gì" — TDS v1.0 dính đúng 2 biến thể ở
 * {@code POST /receiving-sessions} (M06-D07).
 *
 * <p>⚠️ Lệch giữa TestSpec (kỳ vọng {@code 400}) và hành vi THẬT của
 * {@code common/error/GlobalExceptionHandler} (đã merge, dùng chung 13 module — trả
 * {@code 422 UNPROCESSABLE_CONTENT} cho mọi lỗi validate, kể cả bean-validation lẫn
 * type-mismatch, giống MF05 {@code MnuExceptions.validationFailed} 422). Theo §0-BIS
 * "code thật > tài liệu", test dưới đây assert {@code 4xx} (không phải 2xx) thay vì hard-code
 * {@code 400} — bản chất cần bảo vệ (KHÔNG được là {@code 2xx}) vẫn giữ nguyên. Xem báo cáo
 * Bước 2 mục "TDS/TestSpec item không thể implement y hệt".
 */
class Mf06WriteContractIT extends AbstractIamIntegrationTest {

    private UUID schoolId;
    private String ingredientCode;
    private String batchCode;
    private String deliveryCode;

    @BeforeEach
    void seed() {
        schoolId = Mf06Fixtures.schoolId(jdbc);
        UUID partnerId = Mf06Fixtures.partnerId(jdbc, Mf06Fixtures.SUPPLIER_APPROVED_CODE);
        UUID ingredientId = Mf06Fixtures.insertIngredient(jdbc, "ING-TEST-D12", "Test Pork", "kg", "CHILLED", 0.05);
        ingredientCode = "ING-TEST-D12";
        UUID productId = Mf06Fixtures.insertSupplierProduct(jdbc, "SP-TEST-D12", partnerId, ingredientId);
        Mf06Fixtures.insertSupplierBatch(jdbc, "BATCH-TEST-D12", partnerId, productId, BigDecimal.valueOf(40), "kg",
                LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 2), LocalDate.of(2026, 8, 8));
        batchCode = "BATCH-TEST-D12";
        Mf06Fixtures.insertDelivery(jdbc, "DLV-TEST-D12", schoolId);
        deliveryCode = "DLV-TEST-D12";
    }

    /** a + f — field dẫn xuất bị bỏ qua, server ghi giá trị của chính nó (BR-073, JWT identity). */
    @Test
    void derivedFieldsAreIgnoredServerWritesItsOwnValues() throws Exception {
        String body = """
                {
                  "deliveryId": "%s", "lat": 21.0122, "lng": 105.7801, "photosCount": 3,
                  "deviceInfo": "test-device",
                  "receivedAt": "2020-01-01T00:00:00Z", "gpsVerified": true,
                  "status": "RECEIVED", "supplierStatus": "APPROVED",
                  "lines": [ { "supplierBatchId": "%s", "ingredientId": "%s", "storageZone": "CHILLED", "rejected": false,
                               "receivedQuantity": 40 } ]
                }
                """.formatted(deliveryCode, batchCode, ingredientCode);
        MvcResult result = mvc.perform(post("/api/v1/receiving-sessions")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);

        Instant receivedAt = jdbc.queryForObject(
                "select received_at from receiving_sessions where delivery_id = (select id from deliveries where code = ?)",
                Instant.class, deliveryCode);
        // clock của AbstractIamIntegrationTest cố định 2026-07-30T02:00:00Z — KHÔNG phải 2020 client gửi.
        assertThat(receivedAt).isEqualTo(Instant.parse("2026-07-30T02:00:00Z"));

        // f — performedBy (dẫn xuất từ JWT) không nhận được từ client.
        String adjustBody = """
                {"batchId": "%s", "delta": -1, "unit": "kg", "reasonCode": "SPOILAGE",
                 "managerSignature": "Test Manager", "performedBy": "U-9999"}
                """.formatted(batchCode);
        MvcResult adjustResult = mvc.perform(post("/api/v1/inventory-transactions/adjustments")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(adjustBody))
                .andReturn();
        assertThat(adjustResult.getResponse().getStatus()).isEqualTo(201);
        UUID actorId = Mf06Fixtures.userId(jdbc, "U-0003");
        UUID performedBy = jdbc.queryForObject(
                "select performed_by_user_id from inventory_transactions where reason_code = 'SPOILAGE'", UUID.class);
        assertThat(performedBy).isEqualTo(actorId);
    }

    /** b — đúng khái niệm, SAI TÊN (batchId thay vì supplierBatchId): phải 4xx, KHÔNG 201 rỗng. */
    @Test
    void wrongFieldNameIsRejectedNotSilentlyAccepted() throws Exception {
        long before = countReceivingLines();
        String body = """
                {"deliveryId": "%s", "lat": 21.0122, "lng": 105.7801, "photosCount": 3,
                 "lines": [ { "batchId": "%s", "ingredientId": "%s", "storageZone": "CHILLED", "rejected": false } ]}
                """.formatted(deliveryCode, batchCode, ingredientCode);
        MvcResult result = mvc.perform(post("/api/v1/receiving-sessions")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        assertThat(countReceivingLines()).isEqualTo(before);
    }

    /** c — đúng tên, SAI KIỂU (object thay vì string): 4xx, không 201. */
    @Test
    void wrongFieldTypeIsRejected() throws Exception {
        long before = countReceivingLines();
        String body = """
                {"deliveryId": "%s", "lat": 21.0122, "lng": 105.7801, "photosCount": 3,
                 "lines": [ { "supplierBatchId": "%s", "ingredientId": {"code": "%s"}, "storageZone": "CHILLED", "rejected": false } ]}
                """.formatted(deliveryCode, batchCode, ingredientCode);
        MvcResult result = mvc.perform(post("/api/v1/receiving-sessions")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        assertThat(countReceivingLines()).isEqualTo(before);
    }

    /** d — 3 field dẫn xuất gửi lên, thiếu storageZone bắt buộc: 4xx, inventory_batches không đổi. */
    @Test
    void storageZoneUpdateRejectsMissingRequiredFieldDespiteDerivedFieldsPresent() throws Exception {
        UUID ingredientId = Mf06Fixtures.insertIngredient(jdbc, "ING-TEST-D12-B", "Test Zone", "kg", "DRY", 0.0);
        Mf06Fixtures.insertInventoryBatch(jdbc, "BATCH-TEST-ZONE", schoolId, ingredientId, "Test Zone",
                Mf06Fixtures.partnerId(jdbc, Mf06Fixtures.SUPPLIER_APPROVED_CODE), BigDecimal.TEN, "kg", "DRY",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 1));
        String before = jdbc.queryForObject("select storage_zone from inventory_batches where code = ?",
                String.class, "BATCH-TEST-ZONE");

        String body = "{\"remaining\": 99, \"status\": \"IN_STOCK\", \"isLocked\": false}";
        MvcResult result = mvc.perform(patch("/api/v1/inventory/BATCH-TEST-ZONE/storage-zone")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        String after = jdbc.queryForObject("select storage_zone from inventory_batches where code = ?",
                String.class, "BATCH-TEST-ZONE");
        assertThat(after).isEqualTo(before);
        // Cột remaining/status KHÔNG tồn tại trên bảng (ADR-M06-1/M06-2) — kiểm bằng information_schema.
        Integer remainingCol = jdbc.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'inventory_batches' and column_name = 'remaining'",
                Integer.class);
        assertThat(remainingCol).isZero();
    }

    /** e — delta là CHUỖI đã format ("-2 lít") thay vì number: 4xx, không ghi dòng nào (M06-D06). */
    @Test
    void formattedStringDeltaIsRejected() throws Exception {
        UUID ingredientId = Mf06Fixtures.insertIngredient(jdbc, "ING-TEST-D12-C", "Test Oil", "lít", "DRY", 0.0);
        Mf06Fixtures.insertInventoryBatch(jdbc, "BATCH-TEST-DELTA", schoolId, ingredientId, "Test Oil",
                Mf06Fixtures.partnerId(jdbc, Mf06Fixtures.SUPPLIER_APPROVED_CODE), BigDecimal.TEN, "lít", "DRY",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 1));
        long before = countTransactions();
        String body = """
                {"batchId": "BATCH-TEST-DELTA", "delta": "-2 lít", "unit": "lít",
                 "reasonCode": "SPOILAGE", "managerSignature": "Test Manager"}
                """;
        MvcResult result = mvc.perform(post("/api/v1/inventory-transactions/adjustments")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        assertThat(countTransactions()).isEqualTo(before);
    }

    private long countReceivingLines() {
        Long count = jdbc.queryForObject("select count(*) from receiving_lines", Long.class);
        return count == null ? 0 : count;
    }

    private long countTransactions() {
        Long count = jdbc.queryForObject("select count(*) from inventory_transactions", Long.class);
        return count == null ? 0 : count;
    }
}
