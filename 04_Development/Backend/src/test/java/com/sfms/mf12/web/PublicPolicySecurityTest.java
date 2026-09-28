package com.sfms.mf12.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-SEC-013 — {@code GET /public/password-policy} trả tập con KHÔNG nhạy cảm, KHÔNG cần
 * JWT; resource gốc {@code /security-policies} KHÔNG bị hạ quyền (L21/CF-20 — giải pháp đúng
 * là thêm endpoint mới, không mở resource quản trị).
 */
class PublicPolicySecurityTest extends AbstractIamIntegrationTest {

    @Test
    void publicEndpointExposesOnlyNonSensitiveSubsetAdminResourceStaysProtected() throws Exception {
        MvcResult publicResult = mvc.perform(get("/api/v1/public/password-policy")).andReturn();
        assertThat(publicResult.getResponse().getStatus()).isEqualTo(200);

        Set<String> keys = new HashSet<>();
        parse(publicResult).fieldNames().forEachRemaining(keys::add);
        assertThat(keys).isEqualTo(Set.of("passwordMinLength", "requireSpecialChar", "requireNumber",
                "requireMixedCase", "noReuseLast3", "maxLoginAttempts", "lockoutMinutes"));
        assertThat(keys).doesNotContain("sessionTimeoutMinutes", "updatedBy", "updatedAt", "id", "code");

        // Resource gốc VẪN đòi JWT — không hạ quyền để "sửa" bài toán public.
        MvcResult adminResourceNoJwt = mvc.perform(get("/api/v1/security-policies")).andReturn();
        assertThat(adminResourceNoJwt.getResponse().getStatus()).isEqualTo(401);
    }
}
