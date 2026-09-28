package com.sfms.mf06.fixture;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Trợ giúp seed dữ liệu test MF06 qua JDBC thô, dựng trên V900 (schools/partners/users đã có).
 * Mỗi test tự dựng dữ liệu cần, cách ly theo {@code @BeforeEach clean+migrate} của
 * {@code AbstractIamIntegrationTest}.
 */
public final class Mf06Fixtures {

    public static final String SCHOOL_CODE = "SCH-HN-0042";
    public static final String SUPPLIER_APPROVED_CODE = "SUP-001";
    public static final String SUPPLIER_SUSPENDED_CODE = "SUP-004";

    private Mf06Fixtures() {
    }

    public static UUID schoolId(JdbcTemplate jdbc) {
        return jdbc.queryForObject("select id from schools where code = ?", UUID.class, SCHOOL_CODE);
    }

    public static UUID partnerId(JdbcTemplate jdbc, String code) {
        return jdbc.queryForObject("select id from partners where code = ?", UUID.class, code);
    }

    public static UUID userId(JdbcTemplate jdbc, String code) {
        return jdbc.queryForObject("select id from user_accounts where code = ?", UUID.class, code);
    }

    public static UUID insertIngredient(JdbcTemplate jdbc, String code, String name, String unit, String zone,
                                         double refuseFactor) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO catalog_ingredients (id, code, name, unit, storage_zone, refuse_factor)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, code, name, unit, zone, BigDecimal.valueOf(refuseFactor));
        return id;
    }

    public static UUID insertSupplierProduct(JdbcTemplate jdbc, String code, UUID partnerId, UUID ingredientId) {
        UUID id = UUID.randomUUID();
        // `unit`/`effective_from` NOT NULL từ V37__mf06_supplier_products_fields.sql, `conversion_to_base`
        // NOT NULL từ V52__mf04_mf06_conversion_to_base_proposed.sql — cả 3 không có default ở DB.
        jdbc.update("""
                INSERT INTO supplier_products
                    (id, code, partner_id, ingredient_id, unit_price, unit, effective_from, conversion_to_base)
                VALUES (?, ?, ?, ?, 100000, 'kg', CURRENT_DATE, 1)
                """, id, code, partnerId, ingredientId);
        return id;
    }

    /** Lô còn nguyên (ACTIVE, remaining_quantity = quantity) — sẵn sàng nhận hàng. */
    public static UUID insertSupplierBatch(JdbcTemplate jdbc, String code, UUID partnerId, UUID productId,
                                            BigDecimal quantity, String unit, LocalDate mfg, LocalDate pack, LocalDate exp) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO supplier_batches (id, code, partner_id, product_id, lot_number, manufacturing_date,
                                               packaging_date, expiration_date, quantity, unit, status,
                                               remaining_quantity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)
                """, id, code, partnerId, productId, "LOT-" + code, mfg, pack, exp, quantity, unit, quantity);
        return id;
    }

    /**
     * Status mặc định 'DELIVERED' (đổi 13/08/2026 — trước là 'DISPATCHED'): từ khi
     * `ReceivingSessionService.create()` chặn theo `delivery.status` (chỉ mở phiên nhận khi
     * NCC đã "Đã giao"), các test gọi thẳng `POST /receiving-sessions` cần fixture ở đúng
     * tiền điều kiện đó — không phải hạ chuẩn test, mà fixture trước giờ tình cờ mô phỏng
     * sai trạng thái (chưa ai kiểm tới lúc thêm gate này).
     */
    public static UUID insertDelivery(JdbcTemplate jdbc, String code, UUID schoolId) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO deliveries (id, code, school_id, scheduled_at, vehicle_plate, status)
                VALUES (?, ?, ?, now(), '29C-000.00', 'DELIVERED')
                """, id, code, schoolId);
        return id;
    }

    public static UUID insertInventoryBatch(JdbcTemplate jdbc, String code, UUID schoolId, UUID ingredientId,
                                              String ingredientLabel, UUID supplierId, BigDecimal initialQuantity,
                                              String unit, String zone, LocalDate receivedDate, LocalDate expiryDate) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO inventory_batches (id, code, school_id, ingredient_id, ingredient_label, supplier_id,
                                                initial_quantity, unit, storage_zone, received_date, expiry_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, code, schoolId, ingredientId, ingredientLabel, supplierId, initialQuantity, unit, zone,
                receivedDate, expiryDate);
        return id;
    }
}
