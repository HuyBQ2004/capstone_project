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
 * MF05-TC-SEC-005 — BR-201 (Parent liên kết tối đa 5 hồ sơ HS). Endpoint thật KHÔNG có
 * {@code GET /api/v1/parent/menus} riêng (đã chuyển verb — L19/khảo sát code): Parent gọi
 * CHUNG {@code GET /api/v1/menus?studentCode=...}, backend tự chọn {@link
 * com.sfms.mf05.service.MenuServiceImpl#listPublic} khi role là PARENT (D11/M05-D08). Test khoá
 * theo hành vi THẬT của {@code resolvePublicSchoolId()}: {@code studentCode} không liên kết với
 * Parent hiện tại bị BỎ QUA (không throw, không leak) — request rơi về "trường đầu tiên của
 * chính Parent" thay vì trường của học sinh lạ, nên phụ huynh KHÔNG BAO GIỜ thấy dữ liệu của
 * trường/khối gắn với HS không phải con mình.
 */
class ParentAccessSecurityTest extends AbstractIamIntegrationTest {

    @BeforeEach
    void seed() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
    }

    @Test
    void parentQueryingUnlinkedStudentCodeNeverSeesThatStudentsSchoolData() throws Exception {
        // U-0005 (parent) được liên kết với HS-SEC005-OWN (SCH-HN-0042, Khối 3, seed sẵn).
        UUID ownChild = Mf05Fixtures.insertStudent(jdbc, "HS-SEC005-OWN", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.linkParentToStudent(jdbc, "U-0005", ownChild);

        // Trường khác hoàn toàn, KHÔNG liên kết với U-0005.
        Mf05Fixtures.insertStudent(jdbc, "HS-SEC005-STRANGER", "9Z", Mf05Fixtures.OTHER_SCHOOL_CODE);

        UUID otherSchoolId = jdbc.queryForObject(
                "select id from schools where code = ?", UUID.class, Mf05Fixtures.OTHER_SCHOOL_CODE);
        UUID otherSchoolAdminId = jdbc.queryForObject(
                "select id from user_accounts where code = 'U-0018'", UUID.class);
        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type, status,
                                    audience, approved_by_user_id, approved_at)
                SELECT gen_random_uuid(), 'MENU-SEC005-STRANGER', ?, 'Tuan la', 'Thuc don truong khac',
                       g.id, DATE '2026-08-10', 'LUNCH', 'PUBLISHED', 'ALL', ?, now()
                FROM grades g WHERE g.school_id = ? AND g.name = 'Khối 9'
                """, otherSchoolId, otherSchoolAdminId, otherSchoolId);

        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("studentCode", "HS-SEC005-STRANGER")
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        for (var item : body.get("items")) {
            assertThat(item.get("id").asText()).isNotEqualTo("MENU-SEC005-STRANGER");
        }
    }

    @Test
    void parentDetailViewOfOwnChildStudentWorksNormally() throws Exception {
        UUID ownChild = Mf05Fixtures.insertStudent(jdbc, "HS-SEC005-OWN2", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.linkParentToStudent(jdbc, "U-0005", ownChild);

        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("studentCode", "HS-SEC005-OWN2")
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
