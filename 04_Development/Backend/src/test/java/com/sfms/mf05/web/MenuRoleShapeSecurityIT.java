package com.sfms.mf05.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf05.fixture.Mf05Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF05-TC-SEC-015 — 🆕 canonical D11 sample test ({@code 02_Module_Completion_Workflow.md} §6):
 * Parent/Teacher gọi {@code GET /menus} KHÔNG được thấy {@code estimatedCost}/{@code costCeiling}
 * /{@code createdBySupplierId}/{@code approvalSignature}/{@code rejectReason}, chỉ nhận bản ghi
 * {@code PUBLISHED}; School Admin thấy đủ field. {@code /v3/api-docs} phải khai {@code oneOf}
 * {@code PagedResponseMenuResponse}/{@code PagedResponseMenuPublicResponse} (S7).
 */
class MenuRoleShapeSecurityIT extends AbstractIamIntegrationTest {

    private UUID gradeId;

    @BeforeEach
    void seedMenus() {
        UUID schoolId = jdbc.queryForObject("select id from schools where code = ?", UUID.class, Mf05Fixtures.SCHOOL_CODE);
        UUID adminId = jdbc.queryForObject("select id from user_accounts where code = 'U-0001'", UUID.class);
        gradeId = jdbc.queryForObject(
                "select id from grades where school_id = ? and name = 'Khối 3'", UUID.class, schoolId);

        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type, status,
                                    audience, estimated_cost, cost_ceiling, approved_by_user_id, approval_signature, approved_at)
                VALUES (gen_random_uuid(), 'MENU-TEST-PUB', ?, 'Tuan 1', 'Thuc don cong bo', ?,
                        DATE '2026-08-10', 'LUNCH', 'PUBLISHED', 'ALL', 28000, 30000, ?, 'SIG-TEST', now())
                """, schoolId, gradeId, adminId);

        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type, status, audience)
                VALUES (gen_random_uuid(), 'MENU-TEST-DRAFT', ?, 'Tuan 1', 'Thuc don nhap', ?,
                        DATE '2026-08-11', 'LUNCH', 'DRAFT', 'ALL')
                """, schoolId, gradeId);

        // Parent (U-0005) không mang schoolId trong JWT — cần liên kết con để resolvePublicSchoolId() chạy.
        UUID studentId = Mf05Fixtures.insertStudent(jdbc, "HS-TEST-SEC015", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.linkParentToStudent(jdbc, "U-0005", studentId);
    }

    @Test
    void parentSeesOnlyPublicShapeAndPublishedOnly() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("gradeId", gradeId.toString())
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("items")).hasSize(1);
        var item = body.get("items").get(0);
        assertThat(item.get("id").asText()).isEqualTo("MENU-TEST-PUB");
        assertThat(item.has("estimatedCost")).isFalse();
        assertThat(item.has("costCeiling")).isFalse();
        assertThat(item.has("createdBySupplierId")).isFalse();
        assertThat(item.has("approvalSignature")).isFalse();
        assertThat(item.has("rejectReason")).isFalse();
        assertThat(item.get("status").asText()).isEqualTo("PUBLISHED");
    }

    @Test
    void teacherSeesOnlyPublicShapeAndPublishedOnly() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("gradeId", gradeId.toString())
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("items")).hasSize(1);
        var item = body.get("items").get(0);
        assertThat(item.get("status").asText()).isEqualTo("PUBLISHED");
        assertThat(item.has("estimatedCost")).isFalse();
        assertThat(item.has("approvalSignature")).isFalse();
    }

    @Test
    void schoolAdminSeesFullShapeIncludingDraft() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("gradeId", gradeId.toString())
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("items")).hasSize(2);
        boolean anyHasCost = false;
        for (var item : body.get("items")) {
            if (item.has("estimatedCost")) {
                anyHasCost = true;
            }
        }
        assertThat(anyHasCost).isTrue();
    }

    @Test
    void openApiDeclaresOneOfWithPagedResponsePrefix() throws Exception {
        MvcResult result = mvc.perform(get("/v3/api-docs")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("PagedResponseMenuResponse");
        assertThat(body).contains("PagedResponseMenuPublicResponse");
    }
}
