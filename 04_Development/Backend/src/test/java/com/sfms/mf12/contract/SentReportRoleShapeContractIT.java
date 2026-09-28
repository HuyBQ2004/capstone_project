package com.sfms.mf12.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-CON-007 — D11: {@code GET /sent-reports} khác nhau ở tập DÒNG (BR-176), không phải
 * tập FIELD, nên KHÔNG cần {@code oneOf}. Service Caterers chưa được cấp Actor (chờ Huy/CF-M13-3)
 * nên nhánh (5) của TestSpec chưa test được — đây là phần D11 áp dụng được ngay bây giờ.
 */
class SentReportRoleShapeContractIT extends AbstractIamIntegrationTest {

    private static final UUID SCHOOL_A = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    private static final UUID SCHOOL_B = UUID.fromString("00000000-0000-0000-0000-0000000a0051");

    private void seedReport(String code, UUID schoolId, UUID sentByUserId) {
        jdbc.update("""
                INSERT INTO sent_reports
                    (id, code, school_id, report_type, period, range_label, range_from, range_to,
                     sent_at, sent_by_user_id, recipient, notes, payload, confidential)
                VALUES (gen_random_uuid(), ?, ?, 'COMPLIANCE', 'WEEKLY', 'Tuần test', '2026-07-06', '2026-07-12',
                        '2026-07-12T09:00:00Z', ?, 'School Administration', 'ghi chú test',
                        '{"totalInspections":1,"averageScore":90.0,"passCount":1,"failCount":0,"inspections":[]}'::jsonb,
                        false)
                """, code, schoolId, sentByUserId);
    }

    @Test
    void schoolAdminAndMedicalStaffSeeOnlyOwnSchoolSameFieldSetAsSystemAdmin() throws Exception {
        UUID schoolAdminA = jdbc.queryForObject(
                "SELECT id FROM user_accounts WHERE code = 'U-0001'", UUID.class);
        UUID schoolAdminB = jdbc.queryForObject(
                "SELECT id FROM user_accounts WHERE code = 'U-0018'", UUID.class);
        seedReport("RPTS-TEST-A1", SCHOOL_A, schoolAdminA);
        seedReport("RPTS-TEST-B1", SCHOOL_B, schoolAdminB);

        String sysAdmin = authHeaderFor("U-9001");
        String schoolAdminAToken = authHeaderFor("U-0001");
        String medicalStaffAToken = authHeaderFor("U-0002");

        MvcResult asSysAdmin = mvc.perform(get("/api/v1/sent-reports?pageSize=50")
                        .header("Authorization", sysAdmin)).andReturn();
        MvcResult asSchoolAdminA = mvc.perform(get("/api/v1/sent-reports?pageSize=50")
                        .header("Authorization", schoolAdminAToken)).andReturn();
        MvcResult asMedicalA = mvc.perform(get("/api/v1/sent-reports?pageSize=50")
                        .header("Authorization", medicalStaffAToken)).andReturn();

        assertThat(asSysAdmin.getResponse().getStatus()).isEqualTo(200);
        assertThat(asSchoolAdminA.getResponse().getStatus()).isEqualTo(200);
        assertThat(asMedicalA.getResponse().getStatus()).isEqualTo(200);

        JsonNode sysAdminItems = parse(asSysAdmin).get("items");
        JsonNode schoolAdminAItems = parse(asSchoolAdminA).get("items");
        JsonNode medicalAItems = parse(asMedicalA).get("items");

        // Đối chứng dương — SysAdmin thấy CẢ HAI trường.
        assertThat(idsOf(sysAdminItems)).contains("RPTS-TEST-A1", "RPTS-TEST-B1");

        // (2) vs (1) — tập KHOÁ giống hệt, tập ID khác: School Admin A chỉ thấy trường mình.
        assertThat(idsOf(schoolAdminAItems)).contains("RPTS-TEST-A1").doesNotContain("RPTS-TEST-B1");
        assertThat(fieldKeysOf(schoolAdminAItems.get(0))).isEqualTo(fieldKeysOf(sysAdminItems.get(0)));

        // (3) — Medical Staff cùng trường A: tập khoá bằng nhau, cùng thấy đúng báo cáo trường A.
        assertThat(idsOf(medicalAItems)).contains("RPTS-TEST-A1").doesNotContain("RPTS-TEST-B1");
        assertThat(fieldKeysOf(medicalAItems.get(0))).isEqualTo(fieldKeysOf(sysAdminItems.get(0)));
    }

    @Test
    void openApiSchemaKeepsPagedResponsePrefixAndTypedItems() throws Exception {
        MvcResult apiDocs = mvc.perform(get("/v3/api-docs")).andReturn();
        assertThat(apiDocs.getResponse().getStatus()).isEqualTo(200);
        JsonNode root = parse(apiDocs);

        JsonNode listOp = root.at("/paths/~1api~1v1~1sent-reports/get");
        assertThat(listOp.isMissingNode()).isFalse();
        String schemaRef = listOp.at("/responses/200/content/*~1*/schema/$ref").asText();
        assertThat(schemaRef).isNotBlank();
        String schemaName = schemaRef.substring(schemaRef.lastIndexOf('/') + 1);
        // S7 — tên schema giữ tiền tố PagedResponse, KHÔNG rỗng "{}"
        assertThat(schemaName).startsWith("PagedResponse");

        JsonNode schema = root.at("/components/schemas/" + schemaName);
        assertThat(schema.isMissingNode()).isFalse();
        JsonNode itemsSchema = schema.at("/properties/items/items");
        // items PHẢI trỏ một $ref kiểu thật, KHÔNG phải object rỗng {}
        assertThat(itemsSchema.has("$ref") || itemsSchema.has("type")).isTrue();
        assertThat(itemsSchema.toString()).isNotEqualTo("{}");
    }

    private static List<String> idsOf(JsonNode items) {
        return StreamSupport.stream(items.spliterator(), false)
                .map(n -> n.get("id").asText())
                .toList();
    }

    private static List<String> fieldKeysOf(JsonNode item) {
        return StreamSupport.stream(java.util.Spliterators.spliteratorUnknownSize(item.fieldNames(), 0), false)
                .sorted()
                .toList();
    }
}
