package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Chốt 2026-08-02 — GET /students mở cho SERVICE_CATERERS, response RÚT GỌN
 * ({@code StudentDietSummaryResponse}: chỉ id/class/allergies/specialDiet/allergySeverity).
 * Nhu cầu thật: {@code service-caterers/meal-service} + {@code menu-form.tsx} cần biết HS nào
 * dị ứng để tránh phục vụ nhầm món — xác nhận qua code FE thật, không phải suy đoán.
 *
 * <p>{@code SERVICE_CATERERS} gắn 1 trường qua {@code school_id} (giống Teacher/Medical Staff),
 * KHÔNG qua {@code partnerId}/MF04 — xác nhận qua JWT claim thật, nên scope theo trường dùng
 * lại nguyên {@code actor.schoolId()} có sẵn, không cần dữ liệu hợp tác nào khác.
 *
 * <p>V900 không có account service-caterers nào gắn {@code SCH-MF03-0001} (nơi thật sự có dữ
 * liệu học sinh test — do V5 seed), nên tạo synthetic user {@code U-9909} tại chỗ, theo đúng
 * pattern {@code IdorIT}/{@code ParentScopeIT}.
 */
class ServiceCaterersDietSummaryIT extends AbstractIamIntegrationTest {

    private void seedCatererOfMf03School() {
        jdbc.update("INSERT INTO user_accounts (id, code, name, email, role_id, school_id, status, "
                + "email_verified) SELECT gen_random_uuid(), 'U-9909', 'Bếp trưởng (synthetic)', "
                + "'bep.synthetic@example.test', r.id, s.id, 'ACTIVE', true FROM roles r, schools s "
                + "WHERE r.role_key = 'service-caterers' AND s.code = 'SCH-MF03-0001'");
    }

    @Test
    @DisplayName("1 — GET /students (Service Caterers) => 200, response rút gọn, KHÔNG có name/dob")
    void serviceCaterersGetsTrimmedDietSummary() throws Exception {
        seedCatererOfMf03School();

        var result = mvc.perform(get("/api/v1/students").header("Authorization", authHeaderFor("U-9909")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].id").exists())
                .andExpect(jsonPath("$.items[0].class").exists())
                .andExpect(jsonPath("$.items[0].name").doesNotExist())
                .andExpect(jsonPath("$.items[0].dob").doesNotExist())
                .andExpect(jsonPath("$.items[0].parent").doesNotExist())
                .andReturn();

        assertThatJsonHasNoParentOrDobAnywhere(result);
    }

    private void assertThatJsonHasNoParentOrDobAnywhere(org.springframework.test.web.servlet.MvcResult result)
            throws Exception {
        String body = result.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("\"dob\"").doesNotContain("\"parents\"");
    }
}
