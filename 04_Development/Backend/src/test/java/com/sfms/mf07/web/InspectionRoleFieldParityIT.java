package com.sfms.mf07.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF07-TC-SEC-017 — D11 bắt buộc (M07-D13, TestSpec-MF07 §4.2). MF07 lọc theo TẬP DÒNG
 * (BR-099), không phải tập field ⇒ chốt MỘT DTO, KHÔNG {@code oneOf}. TC này là chuông báo
 * cho lần sau ai đó thêm nhánh rút gọn theo vai trò mà quên khai {@code oneOf}.
 *
 * <p>⚠️ Sửa so với văn bản TestSpec gốc: TestSpec viết "U-0003 (service-caterers, SUP-001)" —
 * SAI, {@code SUP-001} là {@code RAW_FOOD_SUPPLIER} (Green Farm), không phải đơn vị suất ăn;
 * V900 thật cũng không gán {@code partner_id} cho U-0003. Chủ thể Caterer đúng trong seed thật
 * là {@code U-0022} (partnerId=CAT-001) — cùng tài khoản đã dùng ở
 * {@link InspectionSessionFlowIT}. {@code safety-signoffs} bị loại khỏi vòng so field vì
 * {@code @PreAuthorize} của nó chỉ cho SCHOOL_ADMIN (TDS §9.2 hàng 19) — "song song 4 vai trò"
 * không áp dụng cho endpoint 1-role.
 */
class InspectionRoleFieldParityIT extends AbstractIamIntegrationTest {

    private static final UUID SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    private static final UUID CATERER_PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0006"); // CAT-001
    private static final UUID OTHER_PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0001");   // SUP-001 (khác đơn vị, chỉ để test cách ly)

    @BeforeEach
    void linkCatererAndSeedRows() {
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF07-SEC-' || substr(gen_random_uuid()::text, 1, 8),
                       p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = 'CAT-001' AND s.code = 'SCH-HN-0042'
                """);

        // BR-099 — 2 phiên của CAT-001 (thấy được bởi U-0022), 1 phiên của đơn vị khác (không).
        // ux_insp_slot = UNIQUE(school_id, date, meal_type, partner_id) ⇒ mỗi phiên 1 buổi khác nhau.
        insertSession("INSP-20260801-L", CATERER_PARTNER_ID, "LUNCH");
        insertSession("INSP-20260801-B", CATERER_PARTNER_ID, "BREAKFAST");
        insertSession("INSP-20260801-S", OTHER_PARTNER_ID, "SNACK");

        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id, result)
                VALUES (gen_random_uuid(), 'KIN-2026-900', ?, DATE '2026-08-01', 80,
                        (SELECT id FROM user_accounts WHERE code = 'U-0002'), 'PASSED')
                """, SCHOOL_ID);
    }

    private void insertSession(String code, UUID partnerId, String mealType) {
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, inspector_user_id)
                VALUES (gen_random_uuid(), ?, ?, ?, DATE '2026-08-01', ?,
                    'PENDING', 'PENDING', 'PENDING', 'IN_PROGRESS',
                    (SELECT id FROM user_accounts WHERE code = 'U-0003'))
                """, code, SCHOOL_ID, partnerId, mealType);
    }

    @Test
    void allFourRolesSeeTheSameFieldSetOnlyRowCountDiffers() throws Exception {
        // U-0022 — service-caterers, KHÔNG @WithMockUser (BR-099 cần partnerId/schoolId thật từ JWT).
        // /inspections: BR-099 lọc theo partnerId ⇒ 3 dòng toàn trường, Caterer chỉ thấy 2 (của CAT-001).
        // System Admin (schoolId=null trong JWT) CHỦ Ý không lọc theo trường (InspectionSpec.build,
        // xem comment ở đó) — nên đếm sống tổng số dòng toàn DB thay vì hardcode 3, vì các seed
        // MF07/MF08 khác (V15_1/V47_1) có thể chèn thêm session cho trường khác bất kỳ lúc nào.
        assertParity("/api/v1/inspections", "inspection_sessions", "U-0022", 3, 2);
        // /kitchen-inspections: KHÔNG lọc theo partner (TDS §9.2 hàng 6) ⇒ cả 4 vai trò thấy 1 dòng.
        assertParity("/api/v1/kitchen-inspections", "kitchen_inspections", "U-0022", 1, 1);
    }

    private void assertParity(String path, String countTable, String catererUserCode, int expectedOthersRows,
                               int expectedCatererRows) throws Exception {
        long expectedSystemAdminRows = count("SELECT COUNT(*) FROM " + countTable);

        JsonNode medical = items(path, "U-0002");
        JsonNode schoolAdmin = items(path, "U-0001");
        JsonNode caterer = items(path, catererUserCode);
        JsonNode systemAdmin = items(path, "U-9001");

        assertThat(medical.size()).isEqualTo(expectedOthersRows);
        assertThat(schoolAdmin.size()).isEqualTo(expectedOthersRows);
        // System Admin thấy TOÀN BỘ NỀN TẢNG (mọi trường), không riêng trường của test này.
        assertThat(systemAdmin.size()).isEqualTo(expectedSystemAdminRows);
        // Medical/SchoolAdmin thấy TOÀN BỘ dòng CỦA TRƯỜNG; Caterer chỉ thấy của mình (BR-099).
        assertThat(caterer.size()).isEqualTo(expectedCatererRows);

        Set<String> keysMedical = fieldNames(medical.get(0));
        Set<String> keysSchoolAdmin = fieldNames(schoolAdmin.get(0));
        Set<String> keysCaterer = fieldNames(caterer.get(0));
        Set<String> keysSystemAdmin = fieldNames(systemAdmin.get(0));

        // D11 — tập FIELD giống hệt nhau ở cả 4 vai trò; chỉ tập DÒNG khác (đã assert ở trên).
        assertThat(keysMedical).isEqualTo(keysSchoolAdmin);
        assertThat(keysMedical).isEqualTo(keysCaterer);
        assertThat(keysMedical).isEqualTo(keysSystemAdmin);
    }

    private JsonNode items(String path, String userCode) throws Exception {
        var result = mvc.perform(get(path + "?page=1&pageSize=20")
                        .header("Authorization", authHeaderFor(userCode)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        return parse(result).get("items");
    }

    private static Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    @Test
    void openApiSchemaKeepsPagedResponsePrefixTypedItemsAndNoOneOf() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode docs = parse(result);

        JsonNode listResponse = docs.at("/paths/~1api~1v1~1inspections/get/responses/200/content")
                .fields().next().getValue().get("schema");
        String ref = listResponse.get("$ref").asText();
        assertThat(ref).endsWith("/PagedResponseInspectionSummaryResponse");   // S7 — tiền tố giữ nguyên

        JsonNode schema = docs.at("/components/schemas/PagedResponseInspectionSummaryResponse");
        JsonNode itemsRef = schema.at("/properties/items/items/$ref");
        assertThat(itemsRef.isMissingNode()).isFalse();                        // items KHÔNG phải {}
        assertThat(schema.has("oneOf")).isFalse();                             // M07-D13 — một DTO duy nhất
    }
}
