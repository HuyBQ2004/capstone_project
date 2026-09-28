package com.sfms.mf04.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF04-TC-SEC-001/003 — ADR-M04-3, M04-D10: ngoài phạm vi dữ liệu (partner khác) ⇒ 404 SUP-404,
 * KHÔNG 403 (chống dò danh mục đối thủ bằng cách quét mã). Seed: SUP-001 (U-0006/U-0020) và
 * SUP-004 (U-0007) — db/testseed/V900__test_fixtures.sql.
 */
class MultiTenantIsolationIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("SUP-004 gọi POST /delivery-persons/DP-01/face (DP-01 thuộc SUP-001) ⇒ 404 SUP-404")
    void crossPartnerFaceRegistrationIsNotFound() throws Exception {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
        var image = new org.springframework.mock.web.MockMultipartFile("image", "face.jpg", "image/jpeg", jpeg);

        mvc.perform(multipart("/api/v1/delivery-persons/DP-01/face")
                        .file(image)
                        .header("Authorization", authHeaderFor("U-0007")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SUP-404"))
                .andExpect(jsonPath("$.error.message").value("Resource not found."))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    @DisplayName("SUP-001 đọc GET /supplier-documents chỉ thấy tài liệu của mình, KHÔNG thấy DOC-DD-01 (SUP-004)")
    void documentListIsScopedByOwnerPartner() throws Exception {
        mvc.perform(get("/api/v1/supplier-documents").param("pageSize", "50")
                        .header("Authorization", authHeaderFor("U-0006")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == 'DOC-DD-01')]").doesNotExist())
                .andExpect(jsonPath("$.items[?(@.id == 'DOC-GF-01')]").exists());
    }

    @Test
    @DisplayName("School Admin trường KHÁC duyệt đơn của SUP-005 (chỉ nộp vào SCH-HN-0042) ⇒ 404 SUP-404")
    void approveFromWrongSchoolIsNotFound() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/partners/SUP-005/approve")
                        .header("Authorization", authHeaderFor("U-0018")))   // school-admin SCH-HN-0051
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SUP-404"));
    }

    @Test
    @DisplayName("Teacher gọi GET /partners (sai vai trò, KHÔNG phải sai phạm vi) ⇒ 403 SUP-005")
    void wrongRoleIsForbiddenNotNotFound() throws Exception {
        mvc.perform(get("/api/v1/partners").header("Authorization", authHeaderFor("U-0004")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("SUP-005"));
    }
}
