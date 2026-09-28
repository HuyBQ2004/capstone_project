package com.sfms.mf02.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF02-TC-SEC-002 — cross-school ⇒ 404 SCH-404/MSG-137, KHÔNG 403 (BR-022, chốt 2026-07-31,
 * đóng §2 L7 TestSpec-MF02 — nhất quán MF01 {@code StaffService.requireInScope()}).
 *
 * <p>MF02-TC-SEC-009 — Teacher xem chính sách trường mình OK; cross-school qua endpoint khác
 * cũng 404.
 */
class CrossSchoolIsolationIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("MF02-TC-SEC-002 — School Admin trường A (U-0001) đọc lớp 9Z của trường B ⇒ "
            + "404 SCH-404/MSG-137, không phải 403")
    void schoolAdminCannotReadClassroomOfAnotherSchool() throws Exception {
        mvc.perform(get("/api/v1/classrooms/9Z")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SCH-404"))
                .andExpect(jsonPath("$.error.message").value("Resource not found."));
    }

    @Test
    @DisplayName("MF02-TC-SEC-002 — School Admin trường A không PATCH được lớp 9Z của trường B "
            + "(homeroom-teacher) ⇒ 404")
    void schoolAdminCannotWriteClassroomOfAnotherSchool() throws Exception {
        mvc.perform(patch("/api/v1/classrooms/9Z/homeroom-teacher")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of("teacherId", "STF-101"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SCH-404"));
    }

    @Test
    @DisplayName("MF02-TC-SEC-009 — Teacher (U-0004, SCH-HN-0042) xem chính sách trường mình ⇒ 200")
    void teacherCanViewOwnSchoolPolicy() throws Exception {
        mvc.perform(get("/api/v1/schools/me/policy")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolId").value("SCH-HN-0042"));
    }

    @Test
    @DisplayName("MF02-TC-SEC-002 — School Admin trường A không xem được config trường B ⇒ 404")
    void schoolAdminCannotReadConfigOfAnotherSchool() throws Exception {
        mvc.perform(get("/api/v1/schools/SCH-HN-0051/config")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SCH-404"));
    }
}
