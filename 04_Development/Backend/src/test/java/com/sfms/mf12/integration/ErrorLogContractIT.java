package com.sfms.mf12.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-INT-007 — {@code GET /error-logs} (danh sách) trả ĐỦ {@code stackTrace} +
 * {@code sessionInfo}, khớp {@code GET /error-logs/{code}} (detail) — CF-18, không lược field
 * để tối ưu payload (FE render trực tiếp ở bảng, không gọi detail).
 */
class ErrorLogContractIT extends AbstractIamIntegrationTest {

    @Test
    void listReturnsFullStackTraceAndSessionInfoMatchingDetail() throws Exception {
        jdbc.update("""
                INSERT INTO error_logs (code, message, severity, module, path, stack_trace, session_info)
                VALUES ('ERR-5521', 'Gemini AI request timeout sau 10s', 'HIGH',
                        'MenuRecommendationService', '/api/v1/menus/MENU-01/ai-suggest',
                        'TimeoutException: upstream deadline exceeded\\n  at GeminiClient.generate',
                        '1 phiên Service Caterers bị ảnh hưởng')
                """);
        String admin = authHeaderFor("U-9001");

        MvcResult list = mvc.perform(get("/api/v1/error-logs?pageSize=50")
                        .header("Authorization", admin)).andReturn();
        assertThat(list.getResponse().getStatus()).isEqualTo(200);
        JsonNode item = parse(list).get("items").get(0);
        assertThat(item.get("stackTrace").asText()).isNotBlank();
        assertThat(item.get("sessionInfo").asText()).isNotBlank();
        assertThat(item.get("message").asText()).isEqualTo("Gemini AI request timeout sau 10s");

        MvcResult detail = mvc.perform(get("/api/v1/error-logs/ERR-5521")
                        .header("Authorization", admin)).andReturn();
        assertThat(detail.getResponse().getStatus()).isEqualTo(200);
        JsonNode detailBody = parse(detail);

        Set<String> listKeys = new HashSet<>();
        item.fieldNames().forEachRemaining(listKeys::add);
        Set<String> detailKeys = new HashSet<>();
        detailBody.fieldNames().forEachRemaining(detailKeys::add);
        assertThat(listKeys).isEqualTo(detailKeys);
        assertThat(item.get("stackTrace").asText()).isEqualTo(detailBody.get("stackTrace").asText());
    }
}
