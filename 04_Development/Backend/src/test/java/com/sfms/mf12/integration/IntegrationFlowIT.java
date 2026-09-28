package com.sfms.mf12.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF12-TC-INT-016 — Luồng UC-040 trọn vẹn: test-connection → save → credential ciphertext
 * (không plaintext) → {@code GET /integrations} và {@code GET /system/status} đọc CÙNG một
 * trạng thái (L14/CF-10 — một nguồn duy nhất, không tự sinh trạng thái riêng).
 */
class IntegrationFlowIT extends AbstractIamIntegrationTest {

    @Test
    void testConnectionThenSaveProducesConsistentStateAcrossBothEndpoints() throws Exception {
        String admin = authHeaderFor("U-9001");

        MvcResult test = mvc.perform(post("/api/v1/integrations/gemini/test-connection")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "apiKey", "AIzaTEST-not-a-real-key"))))
                .andReturn();
        assertThat(test.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(test).get("passed").asBoolean()).isTrue();

        MvcResult save = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "apiKey", "AIzaTEST-not-a-real-key"))))
                .andReturn();
        assertThat(save.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(save).get("connected").asBoolean()).isTrue();

        // (3) — cột credential là ciphertext, KHÔNG còn dấu vết plaintext.
        Long plaintextLeftovers = count(
                "SELECT count(*) FROM external_integrations WHERE credential_cipher::text LIKE '%AIzaTEST%'");
        assertThat(plaintextLeftovers).isEqualTo(0);

        // (4) — GET /integrations: credentialMasked có, apiKey KHÔNG xuất hiện.
        MvcResult list = mvc.perform(get("/api/v1/integrations").header("Authorization", admin)).andReturn();
        JsonNode gemini = null;
        for (JsonNode n : parse(list)) {
            if ("gemini".equals(n.get("id").asText())) {
                gemini = n;
            }
        }
        assertThat(gemini).isNotNull();
        assertThat(gemini.get("credentialMasked").asText()).isNotBlank();
        assertThat(list.getResponse().getContentAsString()).doesNotContain("apiKey");
        assertThat(list.getResponse().getContentAsString()).doesNotContain("AIzaTEST");

        // (5) — GET /system/status đọc CÙNG bảng external_integrations, khớp 100% với (4).
        MvcResult status = mvc.perform(get("/api/v1/system/status").header("Authorization", admin)).andReturn();
        assertThat(status.getResponse().getStatus()).isEqualTo(200);
        JsonNode geminiStatus = null;
        for (JsonNode n : parse(status).get("externalServices")) {
            if ("gemini".equals(n.get("code").asText())) {
                geminiStatus = n;
            }
        }
        assertThat(geminiStatus).isNotNull();
        assertThat(geminiStatus.get("connected").asBoolean()).isEqualTo(gemini.get("connected").asBoolean());
        assertThat(geminiStatus.get("connected").asBoolean()).isTrue();
    }
}
