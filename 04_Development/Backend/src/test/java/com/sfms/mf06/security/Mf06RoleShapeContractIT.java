package com.sfms.mf06.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf06.fixture.Mf06Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF06-TC-SEC-014 — 🆕 D11: shape response KHÔNG được đổi theo vai trò (TestSpec-MF06 §4.3).
 * MF06 không có endpoint nào đổi shape theo role (M06-D08) — khác biệt giữa role là TẬP DÒNG,
 * không phải tập field. Test hồi quy: nếu về sau ai thêm nhánh rút gọn theo role như MF02/MF03
 * đã từng làm, test này bắt được ngay.
 */
class Mf06RoleShapeContractIT extends AbstractIamIntegrationTest {

    @BeforeEach
    void seed() {
        UUID schoolId = Mf06Fixtures.schoolId(jdbc);
        UUID partnerId = Mf06Fixtures.partnerId(jdbc, Mf06Fixtures.SUPPLIER_APPROVED_CODE);
        UUID ingredientId = Mf06Fixtures.insertIngredient(jdbc, "ING-TEST-SEC014", "Test Shape", "kg", "CHILLED", 0.0);
        Mf06Fixtures.insertInventoryBatch(jdbc, "BATCH-TEST-SEC014", schoolId, ingredientId, "Test Shape", partnerId,
                BigDecimal.TEN, "kg", "CHILLED", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 1));
    }

    @Test
    void allFourRolesSeeIdenticalFieldSetForInventoryList() throws Exception {
        List<TreeSet<String>> fieldSets = new ArrayList<>();
        for (String userCode : List.of("U-0001", "U-0003", "U-0002", "U-9001")) {
            MvcResult result = mvc.perform(get("/api/v1/inventory").param("page", "1").param("pageSize", "8")
                            .header("Authorization", authHeaderFor(userCode)))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(200);
            JsonNode items = parse(result).get("items");
            assertThat(items).as(userCode + " items").isNotEmpty();
            TreeSet<String> keys = new TreeSet<>();
            items.get(0).fieldNames().forEachRemaining(keys::add);
            fieldSets.add(keys);
        }
        TreeSet<String> reference = fieldSets.get(0);
        for (TreeSet<String> other : fieldSets) {
            assertThat(other).as("field set must be identical across all 4 roles").isEqualTo(reference);
        }
    }

    @Test
    void openApiDeclaresPagedResponsePrefixedSchemasWithTypedItemsNoOneOf() throws Exception {
        MvcResult result = mvc.perform(get("/v3/api-docs")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String body = result.getResponse().getContentAsString();

        assertThat(body).contains("PagedResponseInventoryBatchResponse");
        assertThat(body).contains("PagedResponseReceivingSessionResponse");
        assertThat(body).contains("PagedResponseInventoryTransactionResponse");
        assertThat(body).contains("PagedResponseIssueSlipResponse");

        // M06-D08 — MF06 KHÔNG có endpoint đổi shape ⇒ không schema nào của MF06 khai oneOf.
        assertThat(body).doesNotContain("InventoryBatchResponsePublic");
    }
}
