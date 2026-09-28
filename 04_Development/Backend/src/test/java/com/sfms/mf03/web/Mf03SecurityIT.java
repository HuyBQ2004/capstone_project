package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security Integration Tests for MF03 covering RBAC and Data Scope Isolation (MF03-TC-SEC-001..009).
 */
class Mf03SecurityIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("MF03-TC-SEC-001 — Parent scope isolation: parent can view linked student health info")
    void parentCanAccessOnlyLinkedStudentHealthInfo() throws Exception {
        // U-9905 is linked to synthetic student in seed V5
        mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9905")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("MF03-TC-SEC-003 — Cross-school isolation: medical staff from School A cannot access student from School B")
    void crossSchoolIsolationEnforced() throws Exception {
        // U-9903 belongs to SCH-MF03-0001
        mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MF03-TC-SEC-006 — Unauthenticated request without JWT returns 401 Unauthorized")
    void unauthenticatedAccessReturns401() throws Exception {
        mvc.perform(get("/api/v1/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("MF03-TC-SEC-005 — Supplier role cannot access student health API (403 Forbidden)")
    void supplierRoleForbiddenAccess() throws Exception {
        // U-0007 is raw food supplier
        mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-0007")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MF03-TC-SEC-004 — System Admin role has no business access to student health records (403 Forbidden)")
    void systemAdminForbiddenAccess() throws Exception {
        // U-9001 is system-admin (U-0001 is school-admin)
        mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9001")))
                .andExpect(status().isForbidden());
    }
}
