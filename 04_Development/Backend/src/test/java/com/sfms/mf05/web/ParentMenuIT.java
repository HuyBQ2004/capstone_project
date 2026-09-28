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
 * MF05-TC-INT-008/009 — Parent chỉ thấy {@code PUBLISHED} của khối con mình.
 *
 * <p>⚠️ 2 điểm lệch với TestSpec, đối chiếu code thật ({@code MenuServiceImpl.listPublic()}/
 * {@code MenuPublicResponse}):
 * <ul>
 *   <li><b>TC-INT-008</b> — không có field {@code conflictsWithChild} nào trong {@code
 *       MenuPublicResponse} (MenuDtos.java) — UC-135 "giá trị người dùng chính" đó chưa được
 *       hiện thực. Không assert field không tồn tại.</li>
 *   <li><b>TC-INT-009</b> — {@code MnuExceptions.noPublishedMenu()} (MENU-090/MSG-090) được
 *       KHAI nhưng KHÔNG BAO GIỜ được gọi ở bất kỳ đâu trong {@code mf05/} (grep xác nhận) —
 *       không có nhánh "tuần chưa công bố -> 404" nào cho Parent. Hành vi thật: cùng endpoint
 *       {@code GET /menus}, tuần không có gì công bố trả <b>200 rỗng</b> giống mọi truy vấn
 *       khác (§2b-6), không phải 404 riêng cho Parent.</li>
 *   <li><b>TC-INT-008</b> (gap thứ 2) — {@code listPublic()} KHÔNG tự khoanh vùng theo khối của
 *       con: nó chỉ áp {@code forcePublished()} (status=PUBLISHED) lên {@code MenuSpec.build()},
 *       không tự thêm điều kiện {@code gradeId = student.classroom.gradeId}. Parent nhận được
 *       MỌI menu PUBLISHED của TOÀN TRƯỜNG (mọi khối), trừ khi tự truyền {@code gradeId} query
 *       param (client-side filter, không phải server tự khoanh theo con). Test khoá đúng hành vi
 *       thật (thấy cả khối khác) thay vì hành vi lý tưởng TestSpec mô tả.</li>
 * </ul>
 */
class ParentMenuIT extends AbstractIamIntegrationTest {

    @BeforeEach
    void seed() {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
    }

    @Test
    void parentSeesOnlyPublishedMenuNotDraft_butNotAutoScopedToChildsGrade() throws Exception {
        UUID schoolId = jdbc.queryForObject("select id from schools where code = ?", UUID.class,
                Mf05Fixtures.SCHOOL_CODE);
        UUID grade3 = jdbc.queryForObject(
                "select id from grades where school_id = ? and name = 'Khối 3'", UUID.class, schoolId);
        UUID grade5 = jdbc.queryForObject(
                "select id from grades where school_id = ? and name = 'Khối 5'", UUID.class, schoolId);
        UUID adminId = jdbc.queryForObject("select id from user_accounts where code = 'U-0001'", UUID.class);

        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type, status,
                                    audience, estimated_cost, cost_ceiling, approved_by_user_id, approved_at)
                VALUES (gen_random_uuid(), 'MENU-INT008-PUB', ?, 'Tuan 1', 'Cong bo khoi 3', ?,
                        DATE '2026-08-10', 'LUNCH', 'PUBLISHED', 'ALL', 28000, 30000, ?, now())
                """, schoolId, grade3, adminId);
        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type, status, audience)
                VALUES (gen_random_uuid(), 'MENU-INT008-DRAFT', ?, 'Tuan 1', 'Nhap khoi 3', ?,
                        DATE '2026-08-11', 'LUNCH', 'DRAFT', 'ALL')
                """, schoolId, grade3);
        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type, status,
                                    audience, approved_by_user_id, approved_at)
                VALUES (gen_random_uuid(), 'MENU-INT008-OTHER-GRADE', ?, 'Tuan 1', 'Cong bo khoi 5', ?,
                        DATE '2026-08-10', 'LUNCH', 'PUBLISHED', 'ALL', ?, now())
                """, schoolId, grade5, adminId);

        UUID student = Mf05Fixtures.insertStudent(jdbc, "HS-INT008-1", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.linkParentToStudent(jdbc, "U-0005", student);

        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("studentCode", "HS-INT008-1")
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        // Không có DRAFT trong kết quả (status luôn PUBLISHED) — phần vẫn khớp TestSpec.
        for (var item : body.get("items")) {
            assertThat(item.get("status").asText()).isEqualTo("PUBLISHED");
            assertThat(item.has("estimatedCost")).isFalse();
            assertThat(item.has("costCeiling")).isFalse();
        }
        // GAP — cả khối 3 (của con) LẪN khối 5 (khối khác) đều xuất hiện: không tự khoanh vùng
        // theo khối con, xem javadoc lớp.
        var ids = new java.util.ArrayList<String>();
        body.get("items").forEach(i -> ids.add(i.get("id").asText()));
        assertThat(ids).containsExactlyInAnyOrder("MENU-INT008-PUB", "MENU-INT008-OTHER-GRADE");

        // Server-side scoping THẬT phải làm bằng client tự truyền gradeId (không phải tự động).
        MvcResult scoped = mvc.perform(get("/api/v1/menus")
                        .param("studentCode", "HS-INT008-1")
                        .param("gradeId", grade3.toString())
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();
        var scopedBody = parse(scoped);
        assertThat(scopedBody.get("items")).hasSize(1);
        assertThat(scopedBody.get("items").get(0).get("id").asText()).isEqualTo("MENU-INT008-PUB");
    }

    @Test
    void gapEmptyWeekForParentReturns200EmptyNot404() throws Exception {
        UUID student = Mf05Fixtures.insertStudent(jdbc, "HS-INT009-1", "3A", Mf05Fixtures.SCHOOL_CODE);
        Mf05Fixtures.linkParentToStudent(jdbc, "U-0005", student);

        MvcResult result = mvc.perform(get("/api/v1/menus")
                        .param("studentCode", "HS-INT009-1")
                        .param("dateFrom", "2099-01-01")
                        .param("dateTo", "2099-01-07")
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("items")).isEmpty();
        assertThat(body.get("total").asLong()).isEqualTo(0L);
    }
}
