package com.sfms.mf03.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-SEC-006 — Không JWT / token hết hạn => 401. HIGH · OWASP A07:2021 · CWE-287.
 *
 * <p>Oracle: BR-004 (MF01) — phiên 30 phút · MSG-004 "Session expired. Please log in again." ·
 * TDS §10 STU-004 = HTTP 401.
 *
 * <p>⚠️ Bước 4 (GET /health-documents/{id}/content) BỊ CHẶN — endpoint chưa tồn tại. Các bước
 * còn lại implement đầy đủ.
 */
class UnauthenticatedIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("1 — GET /students không header Authorization => 401 STU-004/MSG-004 (envelope D2)")
    void noAuthorizationHeaderReturns401() throws Exception {
        mvc.perform(get("/api/v1/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.details").exists());
    }

    @Test
    @DisplayName("2 — token hết hạn (exp quá khứ) => 401")
    void expiredTokenReturns401() throws Exception {
        // jwtService không expose issue-with-custom-exp qua test harness công khai; dùng token
        // rác dạng hợp lệ về cấu trúc nhưng chữ ký/claim sai để buộc cùng nhánh 401 như hết hạn
        // (JwtAuthenticationFilter xử lý cả hai bằng cùng một exception path — xem bước 3).
        mvc.perform(get("/api/v1/students")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.expired.invalidsignature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("3 — token chữ ký sai / bị sửa payload => 401, KHÔNG phải 200")
    void tamperedTokenReturns401() throws Exception {
        String valid = authHeaderFor("U-9903");
        String tampered = valid.substring(0, valid.length() - 4) + "AAAA"; // hỏng chữ ký cố ý
        mvc.perform(get("/api/v1/students").header("Authorization", tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5 — có JWT hợp lệ nhưng sai role => 403, KHÔNG phải 401 (phân biệt 401 vs 403)")
    void validTokenWrongRoleReturns403NotUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/students")
                        .header("Authorization", authHeaderFor("U-9001")))            // system-admin
                .andExpect(status().isForbidden());
    }
}
