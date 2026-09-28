package com.sfms.mf12.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF12-TC-SEC-005 — IDOR: user A đọc hội thoại AI của user B → 403, kể cả SYSTEM_ADMIN (§16:
 * {@code ✅ Own} ở mọi cột, không có ngoại lệ admin đọc-tất-cả).
 */
class AiAccessSecurityTest extends AbstractIamIntegrationTest {

    private String startConversationAs(String userCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", authHeaderFor(userCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("message", "Cau hoi rieng cua " + userCode))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        return parse(result).get("conversationId").asText();
    }

    @Test
    void userCannotReadAnotherUsersConversationNotEvenSystemAdmin() throws Exception {
        String catererConversation = startConversationAs("U-0003"); // service-caterers
        String medicalConversation = startConversationAs("U-0002"); // medical-staff

        MvcResult crossRead = mvc.perform(get("/api/v1/ai/conversations/" + catererConversation)
                        .header("Authorization", authHeaderFor("U-0002")))
                .andReturn();
        assertThat(crossRead.getResponse().getStatus()).isEqualTo(404);

        MvcResult adminRead = mvc.perform(get("/api/v1/ai/conversations/" + medicalConversation)
                        .header("Authorization", authHeaderFor("U-9001")))
                .andReturn();
        assertThat(adminRead.getResponse().getStatus()).isEqualTo(404);

        // Đối chứng dương — chủ hội thoại tự đọc được.
        MvcResult ownRead = mvc.perform(get("/api/v1/ai/conversations/" + catererConversation)
                        .header("Authorization", authHeaderFor("U-0003")))
                .andReturn();
        assertThat(ownRead.getResponse().getStatus()).isEqualTo(200);
    }
}
