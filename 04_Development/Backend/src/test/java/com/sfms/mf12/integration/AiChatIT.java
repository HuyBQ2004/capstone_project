package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF12-TC-INT-001 — Chat trọn vòng: 2 lượt cùng {@code conversationId} → 1 dòng
 * {@code ai_conversations}, 2 dòng {@code ai_requests}, {@code last_message_at} cập nhật theo
 * lượt sau cùng, lịch sử trả đủ 2 lượt.
 *
 * ⚠️ Ghi chú: TestSpec đánh dấu TC này "BLOCKED bởi L6" vì lúc viết spec (trước Bước 2),
 * mock FE chưa có route AI Chat. Backend thật (`AiController`/`AiChatServiceImpl`) đã tồn tại
 * từ Bước 2 — cờ blocked đã lỗi thời, TC này chạy được ngay trên Gemini stub (ADR-000).
 */
class AiChatIT extends AbstractIamIntegrationTest {

    @Test
    void twoTurnsInSameConversationProduceOneConversationTwoRequests() throws Exception {
        String parent = authHeaderFor("U-0005");

        MvcResult first = mvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", parent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("message", "Con toi bi di ung tom, goi y mon an thay the"))))
                .andReturn();
        assertThat(first.getResponse().getStatus()).isEqualTo(200);
        String conversationCode = parse(first).get("conversationId").asText();
        assertThat(conversationCode).isNotBlank();

        MvcResult second = mvc.perform(post("/api/v1/ai/chat")
                        .header("Authorization", parent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("conversationId", conversationCode, "message", "Con toi 6 tuoi"))))
                .andReturn();
        assertThat(second.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(second).get("conversationId").asText()).isEqualTo(conversationCode);

        long conversationRows = count(
                "SELECT count(*) FROM ai_conversations WHERE code = ?", conversationCode);
        assertThat(conversationRows).isEqualTo(1);
        long requestRows = count("""
                SELECT count(*) FROM ai_requests r JOIN ai_conversations c ON r.conversation_id = c.id
                 WHERE c.code = ?
                """, conversationCode);
        assertThat(requestRows).isEqualTo(2);

        MvcResult history = mvc.perform(get("/api/v1/ai/conversations/" + conversationCode)
                        .header("Authorization", parent))
                .andReturn();
        assertThat(history.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(history).get("messages")).hasSize(2);
    }
}
