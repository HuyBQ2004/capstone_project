package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF13-TC-SEC-002/003 — BR-176: {@code schoolId} luôn từ JWT (kể cả query param bị bỏ qua);
 * cross-school ⇒ 404 (M13-D04). MF13-TC-SEC-004 — BR-181: 0 field PII học sinh trong JSON.
 */
class ReportRowsIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_A = "00000000-0000-0000-0000-0000000a0042";
    private static final String SCHOOL_B = "00000000-0000-0000-0000-0000000a0051";

    @Test
    void rowsAreScopedToJwtSchoolEvenIfQueryParamSaysOtherwise() throws Exception {
        insertThreeStep("INSP-A-1", SCHOOL_A, 1);
        insertThreeStep("INSP-A-2", SCHOOL_A, 2);
        insertThreeStep("INSP-B-1", SCHOOL_B, 1);

        String schoolAdminA = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/reports/three-step/rows")
                        .header("Authorization", schoolAdminA)
                        .param("pageSize", "100")
                        .param("schoolId", SCHOOL_B)) // cố gắng bypass qua query — PHẢI bị bỏ qua
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("total").asInt()).isEqualTo(2);
        for (var item : body.get("items")) {
            assertThat(item.get("id").asText()).startsWith("INSP-A-");
        }
    }

    @Test
    void responseContainsNoStudentPii() throws Exception {
        insertThreeStep("INSP-A-3", SCHOOL_A, 3);
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/reports/three-step/rows")
                        .header("Authorization", schoolAdmin).param("pageSize", "100"))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        assertThat(json).doesNotContain("studentName", "studentId", "parentName", "diagnosis");
        // assertion dương — chứng minh response KHÔNG rỗng (chống false-green, TestSpec §5.1)
        assertThat(parse(result).get("total").asInt()).isGreaterThan(0);
    }

    @Test
    void serviceCaterersAreDeniedAtMvp() throws Exception {
        String caterer = authHeaderFor("U-0003");
        MvcResult result = mvc.perform(get("/api/v1/reports/receiving/rows").header("Authorization", caterer))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    /**
     * {@code daySeq} lệch ngày mỗi lần gọi — {@code ux_insp_slot(school_id, date, meal_type,
     * partner_id)} chặn 2 phiên trùng slot, kể cả khi cùng school_id trong 1 test.
     */
    private void insertThreeStep(String code, String schoolId, int daySeq) {
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, core_temp, inspector_user_id)
                VALUES (gen_random_uuid(), ?, ?::uuid, '00000000-0000-0000-0000-0000000b0001'::uuid,
                    DATE '2026-07-20' + (? || ' days')::interval, 'LUNCH', 'PASSED', 'PASSED', 'PASSED',
                    'PASSED', 80, (SELECT id FROM user_accounts WHERE code = 'U-0002'))
                """, code, schoolId, daySeq);
    }
}
