package com.sfms.mf05.web;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf05.fixture.Mf05Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF05-TC-SEC-014 — {@code PUBLISHED} bất khả sửa với MỌI role (BR-067), kể cả {@code
 * SYSTEM_ADMIN}.
 *
 * <p>⚠️ Đối chiếu code thật: {@code @PreAuthorize} của {@code PUT}/{@code DELETE
 * /api/v1/menus/{code}} CHỈ liệt {@code SERVICE_CATERERS}/{@code SCHOOL_ADMIN} (PUT) và {@code
 * SERVICE_CATERERS} (DELETE) — 5 role còn lại (kể cả {@code SYSTEM_ADMIN}) bị chặn ở TẦNG VAI
 * TRÒ (403) TRƯỚC KHI chạm luật bất biến BR-067 trong service, KHÁC giả định của TestSpec
 * ("SYS_ADMIN cũng nhận 409" — thực tế SYS_ADMIN nhận 403 vì không có route access, chưa từng
 * tới được `assertEditable()`). Property THẬT cần khoá: dù mã lỗi khác nhau theo role, KHÔNG
 * role nào — kể cả role có route access — sửa/xoá được menu PUBLISHED (200), và bản ghi DB
 * không đổi sau khi thử đủ 7 role.
 */
class MenuImmutabilityTest extends AbstractIamIntegrationTest {

    private String publishedMenuCode;

    @BeforeEach
    void seedPublishedMenu() throws Exception {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
        UUID schoolId = jdbc.queryForObject("select id from schools where code = ?", UUID.class,
                Mf05Fixtures.SCHOOL_CODE);
        UUID adminId = jdbc.queryForObject("select id from user_accounts where code = 'U-0001'", UUID.class);
        UUID gradeId = jdbc.queryForObject(
                "select id from grades where school_id = ? and name = 'Khối 3'", UUID.class, schoolId);
        publishedMenuCode = "MENU-SEC014-PUB";
        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type, status,
                                    audience, approved_by_user_id, approval_signature, approved_at,
                                    created_by_supplier_id)
                SELECT gen_random_uuid(), ?, ?, 'Tuan 1', 'Thuc don cong bo SEC014', ?,
                       DATE '2026-08-10', 'LUNCH', 'PUBLISHED', 'ALL', ?, 'SIG-SEC014', now(), p.id
                FROM partners p WHERE p.code = ?
                """, publishedMenuCode, schoolId, gradeId, adminId, Mf05Fixtures.CATERER_PARTNER_CODE);
    }

    @ParameterizedTest
    @EnumSource(RoleKey.class)
    void noRoleCanEditOrDeletePublishedMenu(RoleKey role) throws Exception {
        String userCode = userCodeOf(role);

        MvcResult putResult = mvc.perform(put("/api/v1/menus/" + publishedMenuCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(userCode))
                        .content(toJson(Map.of("name", "hacked-by-" + role))))
                .andReturn();
        assertThat(putResult.getResponse().getStatus())
                .as("PUT bởi %s không được 200", role)
                .isNotEqualTo(200);

        MvcResult deleteResult = mvc.perform(delete("/api/v1/menus/" + publishedMenuCode)
                        .header("Authorization", authHeaderFor(userCode)))
                .andReturn();
        assertThat(deleteResult.getResponse().getStatus())
                .as("DELETE bởi %s không được thành công", role)
                .isNotIn(200, 204);
    }

    @Test
    void schoolAdminWithRouteAccessGetsBusinessRule409NotSuccess() throws Exception {
        // SCHOOL_ADMIN CÓ route access tới PUT (@PreAuthorize cho phép) — nên phải chạm được
        // assertEditable()/luật BR-067 thật, khác các role bị chặn từ vòng ngoài.
        MvcResult result = mvc.perform(put("/api/v1/menus/" + publishedMenuCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-0001"))
                        .content(toJson(Map.of("name", "hacked-by-admin"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("MENU-067");
    }

    @Test
    void databaseRowNeverChangesAfterAllAttempts() throws Exception {
        for (RoleKey role : RoleKey.values()) {
            mvc.perform(put("/api/v1/menus/" + publishedMenuCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", authHeaderFor(userCodeOf(role)))
                    .content(toJson(Map.of("name", "hacked-by-" + role))));
        }
        String name = jdbc.queryForObject("select name from menus where code = ?", String.class,
                publishedMenuCode);
        assertThat(name).isEqualTo("Thuc don cong bo SEC014");
    }
}
