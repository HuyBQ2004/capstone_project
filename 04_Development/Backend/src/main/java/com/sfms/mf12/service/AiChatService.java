package com.sfms.mf12.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.mf12.dto.AiDtos.AiChatResponse;
import com.sfms.mf12.dto.AiDtos.AiConversationDetail;
import com.sfms.mf12.dto.AiDtos.AiConversationSummary;
import com.sfms.mf12.dto.AiDtos.AskRequest;

import java.util.Optional;
import java.util.UUID;

/** UC-012/UC-119 — trợ lý AI (BR-157). */
public interface AiChatService {

    /** UC-012 — MSG-073 khi dịch vụ không khả dụng (UC-012 AF1). */
    AiChatResponse ask(AskRequest request, UUID actorUserId);

    /** UC-119 — BR-157; chỉ hội thoại CỦA CHÍNH người dùng. */
    PagedResponse<AiConversationSummary> history(int page, int pageSize, UUID actorUserId);

    Optional<AiConversationDetail> conversation(String conversationCode, UUID actorUserId);
}
