package com.sfms.mf03.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-SEC-004 — System Admin KHÔNG có quyền nghiệp vụ; Supplier/Guest không quyền nào.
 * HIGH · OWASP A01:2021 · CWE-269. Legal: BR-173/PDPD — least privilege trên dữ liệu Restricted.
 *
 * <p>Oracle: TDS §16 "System Admin KHÔNG có quyền nghiệp vụ trên hồ sơ học sinh" và "Supplier /
 * Guest: không có bất kỳ quyền nào trên MF03" · MSG-005 · D6 {@code RoleKey}.
 *
 * <p>Quét các endpoint MF03 THẬT SỰ TỒN TẠI trong lần implement này (không phải đủ 20 của TDS
 * §16 — 8 endpoint chưa implement, ngoài phạm vi đợt này).
 */
class AdminNoBusinessAccessIT extends AbstractIamIntegrationTest {

    @ParameterizedTest(name = "{0} {1} — system-admin (U-9001) => 403 STU-005")
    @CsvSource({
            "GET, /api/v1/students",
            "GET, /api/v1/students/HS-3A-001",
            "GET, /api/v1/students/HS-3A-001/allergy-alerts",
            "GET, /api/v1/allergy-declarations",
            "GET, /api/v1/special-meals",
            "GET, /api/v1/health-history?studentId=00000000-0000-0000-0000-000000000000"
    })
    @DisplayName("1 — system-admin bị chặn ở mọi endpoint MF03, kể cả GET /students")
    void systemAdminIsForbiddenEverywhere(String method, String path) throws Exception {
        var request = "GET".equals(method) ? get(path) : post(path);
        mvc.perform(request.header("Authorization", authHeaderFor("U-9001")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }

    @ParameterizedTest(name = "{0} {1} — supplier (U-0007) => 403 STU-005")
    @CsvSource({
            "GET, /api/v1/students",
            "GET, /api/v1/allergy-declarations",
            "GET, /api/v1/special-meals"
    })
    @DisplayName("2 — supplier bị chặn ở tất cả")
    void supplierIsForbiddenEverywhere(String method, String path) throws Exception {
        mvc.perform(get(path).header("Authorization", authHeaderFor("U-0007")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STU-005"));
    }

    @Test
    @DisplayName("3 — không JWT (Guest) => 401 STU-004 (xem thêm UnauthenticatedIT)")
    void guestIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4 — không có backdoor impersonate/asUser: header đó bị bỏ qua hoàn toàn")
    void noImpersonationBackdoor() throws Exception {
        // Gửi header giả danh + đóng vai actor khác — actor vẫn là system-admin theo JWT, vẫn 403.
        mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .header("X-Impersonate-User", "U-9902")
                        .header("asUser", "U-9902"))
                .andExpect(status().isForbidden());
    }
}
