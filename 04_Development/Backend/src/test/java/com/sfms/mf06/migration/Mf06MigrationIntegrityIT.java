package com.sfms.mf06.migration;

import com.sfms.common.code.CodeGenerator;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Nhóm G (🆕 2026-08-03, TestSpec-MF06 §4.2) — {@code INT-045}…{@code INT-050}: hợp đồng với
 * code đã merge (L9–L18, §2.-1). Bảy TC neo đúng một mục L9–L18; sáu trong bảy kiểm thứ làm
 * migration fail / build đỏ ngay lệnh đầu tiên nếu code theo TDS v1.0 — {@code INT-051} (D12)
 * nằm ở {@link com.sfms.mf06.contract.Mf06WriteContractIT} riêng.
 */
class Mf06MigrationIntegrityIT extends AbstractIamIntegrationTest {

    @Autowired
    private CodeGenerator codeGenerator;

    /** INT-045 — D9/M06-D02: 4 cột audit đúng kiểu, KHÔNG uuid; issued_by_user_id/performed_by_user_id
     *  tồn tại và NOT NULL; KHÔNG còn created_by uuid / actor_id. Context Spring khởi động được. */
    @Test
    void auditColumnsMatchBaseEntityContract() {
        // `preparation_records` bỏ khỏi danh sách 12/08/2026 — V43__drop_preparation_records.sql
        // (06/08/2026) đã DROP hẳn bảng này, "Sổ chế biến" trùng nghiệp vụ với Bước 2 kiểm thực
        // ba bước, không service/repository nào còn đọc/ghi.
        List<String> mf06Tables = List.of("catalog_ingredients", "supplier_products", "purchase_requests",
                "purchase_items", "supplier_batches", "deliveries", "receiving_sessions", "receiving_lines",
                "inventory_batches", "issue_slips", "inventory_transactions");

        for (String table : mf06Tables) {
            Map<String, Object> createdAt = jdbc.queryForMap("""
                    select data_type, is_nullable from information_schema.columns
                    where table_name = ? and column_name = 'created_at'
                    """, table);
            assertThat(createdAt.get("data_type")).isEqualTo("timestamp with time zone");
            assertThat(createdAt.get("is_nullable")).isEqualTo("NO");

            Map<String, Object> updatedAt = jdbc.queryForMap("""
                    select data_type, is_nullable from information_schema.columns
                    where table_name = ? and column_name = 'updated_at'
                    """, table);
            assertThat(updatedAt.get("data_type")).isEqualTo("timestamp with time zone");
            assertThat(updatedAt.get("is_nullable")).isEqualTo("YES");

            for (String col : List.of("created_by", "updated_by")) {
                Map<String, Object> row = jdbc.queryForMap("""
                        select data_type, character_maximum_length from information_schema.columns
                        where table_name = ? and column_name = ?
                        """, table, col);
                assertThat(row.get("data_type")).as(table + "." + col).isEqualTo("character varying");
                assertThat(((Number) row.get("character_maximum_length")).intValue()).isEqualTo(180);
            }
        }

        // issue_slips.issued_by_user_id / inventory_transactions.performed_by_user_id — field NGHIỆP VỤ.
        assertColumnUuidNotNull("issue_slips", "issued_by_user_id");
        assertColumnUuidNotNull("inventory_transactions", "performed_by_user_id");

        // KHÔNG còn tên cột cũ (created_by uuid / actor_id) trên 2 bảng đó.
        assertThat(columnExists("issue_slips", "actor_id")).isFalse();
        assertThat(columnExists("inventory_transactions", "actor_id")).isFalse();
    }

    /** INT-046 — L10/L11/M06-D03: FK trỏ bảng CÓ THẬT; supplier_documents chưa từng tồn tại. */
    @Test
    void foreignKeysPointToRealMergedTables() {
        String batchCertFk = jdbc.queryForObject("""
                select ccu.table_name from information_schema.table_constraints tc
                join information_schema.key_column_usage kcu on kcu.constraint_name = tc.constraint_name
                join information_schema.constraint_column_usage ccu on ccu.constraint_name = tc.constraint_name
                where tc.table_name = 'batch_certificates' and kcu.column_name = 'document_id'
                  and tc.constraint_type = 'FOREIGN KEY'
                """, String.class);
        assertThat(batchCertFk).isEqualTo("partner_documents");

        String supplierBatchFk = jdbc.queryForObject("""
                select ccu.table_name from information_schema.table_constraints tc
                join information_schema.key_column_usage kcu on kcu.constraint_name = tc.constraint_name
                join information_schema.constraint_column_usage ccu on ccu.constraint_name = tc.constraint_name
                where tc.table_name = 'supplier_batches' and kcu.column_name = 'product_id'
                  and tc.constraint_type = 'FOREIGN KEY'
                """, String.class);
        assertThat(supplierBatchFk).isEqualTo("supplier_products");

        Integer supplierDocumentsCount = jdbc.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'supplier_documents'", Integer.class);
        assertThat(supplierDocumentsCount).isZero();
    }

    /** INT-047 — L12/M06-D04: catalog_ingredients tồn tại đủ cột; dish_ingredients khớp 100%. */
    @Test
    void catalogIngredientsBacksDishIngredientsFk() {
        Integer tableCount = jdbc.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'catalog_ingredients'", Integer.class);
        assertThat(tableCount).isEqualTo(1);

        for (String col : List.of("code", "unit", "storage_zone", "refuse_factor")) {
            Integer colCount = jdbc.queryForObject("""
                    select count(*) from information_schema.columns
                    where table_name = 'catalog_ingredients' and column_name = ?
                    """, Integer.class, col);
            assertThat(colCount).as("catalog_ingredients." + col).isEqualTo(1);
        }

        Integer fkCount = jdbc.queryForObject("""
                select count(*) from information_schema.table_constraints
                where table_name = 'dish_ingredients' and constraint_name = 'fk_dish_ing_catalog'
                  and constraint_type = 'FOREIGN KEY'
                """, Integer.class);
        assertThat(fkCount).isEqualTo(1);

        Integer orphanCount = jdbc.queryForObject("""
                select count(*) from dish_ingredients
                where ingredient_code is not null and ingredient_code not in (select code from catalog_ingredients)
                """, Integer.class);
        assertThat(orphanCount).isZero();
    }

    /** INT-048 — L13/M06-D05: code_sequences vẫn PHẲNG (scope, next_value); year-scope hoạt động đúng. */
    @Test
    void codeGeneratorUsesYearScopedScopeStringNotSchemaChange() {
        Integer colCount = jdbc.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'code_sequences'", Integer.class);
        assertThat(colCount).isEqualTo(2);

        String scope2099 = "mf06_test_seq:2099";
        String first = codeGenerator.next(scope2099, "TST-2099", 4);
        String second = codeGenerator.next(scope2099, "TST-2099", 4);
        assertThat(first).isEqualTo("TST-2099-0001");
        assertThat(second).isEqualTo("TST-2099-0002");

        String otherYearScope = "mf06_test_seq:2100";
        String thirdDifferentYear = codeGenerator.next(otherYearScope, "TST-2100", 4);
        assertThat(thirdDifferentYear).isEqualTo("TST-2100-0001");
    }

    /** INT-049 — L14: 404 phải mang tiền tố ING (không DEFAULT_PREFIX IAM), message = MSG-137 nguyên văn. */
    @Test
    void notFoundUsesIngPrefixNotDefaultPrefix() throws Exception {
        String token = authHeaderFor("U-9001");
        List<String> paths = List.of(
                "/api/v1/inventory/KHONG-TON-TAI",
                "/api/v1/purchase-requests/KHONG-TON-TAI",
                "/api/v1/deliveries/KHONG-TON-TAI",
                "/api/v1/supplier-batches/KHONG-TON-TAI",
                "/api/v1/receiving-sessions/KHONG-TON-TAI");
        for (String path : paths) {
            MvcResult result = mvc.perform(get(path).header("Authorization", token)).andReturn();
            assertThat(result.getResponse().getStatus()).as(path).isEqualTo(404);
            var body = parse(result);
            assertThat(body.get("error").get("code").asText()).as(path).isEqualTo("ING-404");
            assertThat(body.get("error").get("message").asText()).as(path).isEqualTo("Resource not found.");
        }

        MvcResult prepResult = mvc.perform(get("/api/v1/preparation-records/KHONG-TON-TAI")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(prepResult.getResponse().getStatus()).isEqualTo(404);
        assertThat(parse(prepResult).get("error").get("code").asText()).isEqualTo("ING-404");
    }

    /** INT-050 — L16/M06-D14: V10/V11 Success, không dòng nào success=false cho V1..V9 (§0-BIS). */
    @Test
    void flywayHistoryAllSuccessNoChecksumMismatch() {
        List<Map<String, Object>> failed = jdbc.queryForList(
                "select version, success from flyway_schema_history where success = false");
        assertThat(failed).isEmpty();

        for (String version : List.of("10", "11")) {
            Boolean success = jdbc.queryForObject(
                    "select success from flyway_schema_history where version = ?", Boolean.class, version);
            assertThat(success).as("V" + version).isTrue();
        }
    }

    private void assertColumnUuidNotNull(String table, String column) {
        Map<String, Object> row = jdbc.queryForMap("""
                select data_type, is_nullable from information_schema.columns
                where table_name = ? and column_name = ?
                """, table, column);
        assertThat(row.get("data_type")).as(table + "." + column).isEqualTo("uuid");
        assertThat(row.get("is_nullable")).as(table + "." + column).isEqualTo("NO");
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbc.queryForObject("""
                select count(*) from information_schema.columns
                where table_name = ? and column_name = ?
                """, Integer.class, table, column);
        return count != null && count > 0;
    }
}
