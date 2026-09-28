package com.sfms.mf13.service;

import com.sfms.mf13.domain.ReportSlug;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADR-M13-4 (BR-181) — mọi hàng ra khỏi MF13 đi qua đây trước khi rời module (JSON/PDF/XLSX).
 * <b>Whitelist tường minh</b> theo slug (cột nguồn → tên JSON camelCase) — KHÔNG blacklist.
 * Cột nào KHÔNG có trong whitelist bị loại, kể cả khi view nguồn (vô tình) trả thêm cột PII
 * (fail-closed, TC-UNIT-001/SEC-004).
 */
@Component
public class ReportRowProjection {

    /** {@code cột view (snake_case) -> tên JSON (camelCase)}, theo đúng thứ tự hiển thị. */
    private static final Map<ReportSlug, Map<String, String>> WHITELIST = new EnumMap<>(ReportSlug.class);

    static {
        Map<String, String> threeStep = new LinkedHashMap<>();
        threeStep.put("id", "id");
        threeStep.put("date", "date");
        threeStep.put("meal_type", "mealType");
        threeStep.put("step1", "step1");
        threeStep.put("step2", "step2");
        threeStep.put("step3", "step3");
        threeStep.put("core_temp", "coreTemp");
        threeStep.put("holding_temp", "holdingTemp");
        threeStep.put("overall", "overall");
        threeStep.put("inspector", "inspector");
        threeStep.put("dish", "dish");
        WHITELIST.put(ReportSlug.THREE_STEP, threeStep);

        Map<String, String> sampleRetention = new LinkedHashMap<>();
        sampleRetention.put("id", "id");
        sampleRetention.put("dish", "dish");
        sampleRetention.put("meal_type", "mealType");
        sampleRetention.put("meal_shift", "mealShift");
        sampleRetention.put("sample_amount", "sampleAmount");
        sampleRetention.put("sample_unit", "sampleUnit");
        sampleRetention.put("retention_deadline", "retentionDeadline");
        sampleRetention.put("fridge_temp", "fridgeTemp");
        sampleRetention.put("status", "status");
        sampleRetention.put("collected_at", "collectedAt");
        WHITELIST.put(ReportSlug.SAMPLE_RETENTION, sampleRetention);

        Map<String, String> receiving = new LinkedHashMap<>();
        receiving.put("id", "id");
        receiving.put("supplier", "supplier");
        receiving.put("delivery_date", "deliveryDate");
        receiving.put("items", "items");
        receiving.put("gps_verified", "gpsVerified");
        receiving.put("photos_count", "photosCount");
        receiving.put("status", "status");
        WHITELIST.put(ReportSlug.RECEIVING, receiving);

        Map<String, String> supplierCert = new LinkedHashMap<>();
        supplierCert.put("id", "id");
        supplierCert.put("name", "name");
        supplierCert.put("partner_type", "partnerType");
        supplierCert.put("cert_expiry", "certExpiry");
        supplierCert.put("cert_status", "certStatus");
        supplierCert.put("status", "status");
        WHITELIST.put(ReportSlug.SUPPLIER_CERTIFICATES, supplierCert);

        Map<String, String> hygiene = new LinkedHashMap<>();
        hygiene.put("id", "id");
        hygiene.put("date", "date");
        hygiene.put("score", "score");
        hygiene.put("staff_health_valid", "staffHealthValid");
        hygiene.put("pest_control_valid", "pestControlValid");
        hygiene.put("inspector", "inspector");
        hygiene.put("result", "result");
        WHITELIST.put(ReportSlug.HYGIENE, hygiene);

        Map<String, String> traceability = new LinkedHashMap<>();
        traceability.put("id", "id");
        traceability.put("ingredient", "ingredient");
        traceability.put("supplier", "supplier");
        traceability.put("received_date", "receivedDate");
        traceability.put("expiry_date", "expiryDate");
        traceability.put("storage_zone", "storageZone");
        traceability.put("status", "status");
        WHITELIST.put(ReportSlug.TRACEABILITY, traceability);

        // BR-181: chứa TÊN nhân sự, KHÔNG phải HS -> hợp lệ, vẫn Confidential.
        Map<String, String> systemUsage = new LinkedHashMap<>();
        systemUsage.put("id", "id");
        systemUsage.put("name", "name");
        systemUsage.put("role", "role");
        systemUsage.put("status", "status");
        systemUsage.put("last_login", "lastLogin");
        WHITELIST.put(ReportSlug.SYSTEM_USAGE, systemUsage);
    }

    /** @return bản sao chỉ chứa cột whitelist của {@code slug}, tên JSON camelCase. Fail-closed. */
    public Map<String, Object> project(ReportSlug slug, Map<String, Object> rawRow) {
        Map<String, String> mapping = WHITELIST.get(slug);
        Map<String, Object> out = new LinkedHashMap<>();
        if (mapping == null || rawRow == null) {
            return out;
        }
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (rawRow.containsKey(entry.getKey())) {
                out.put(entry.getValue(), rawRow.get(entry.getKey()));
            }
        }
        return out;
    }
}
