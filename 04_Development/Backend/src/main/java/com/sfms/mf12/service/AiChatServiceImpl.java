package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.mf12.domain.AiConversation;
import com.sfms.mf12.domain.AiRequest;
import com.sfms.mf12.dto.AiDtos.AiChatResponse;
import com.sfms.mf12.dto.AiDtos.AiConversationDetail;
import com.sfms.mf12.dto.AiDtos.AiConversationSummary;
import com.sfms.mf12.dto.AiDtos.AiMessageView;
import com.sfms.mf12.dto.AiDtos.AskRequest;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.repository.AiConversationRepository;
import com.sfms.mf12.repository.AiRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** UC-012/UC-119 — chat với AI (BR-157). */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String MODULE = "MF12_CHAT";

    private final AiConversationRepository conversationRepository;
    private final AiRequestRepository requestRepository;
    private final AiGateway aiGateway;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public AiChatServiceImpl(AiConversationRepository conversationRepository, AiRequestRepository requestRepository,
                              AiGateway aiGateway, CodeGenerator codeGenerator, Clock clock) {
        this.conversationRepository = conversationRepository;
        this.requestRepository = requestRepository;
        this.aiGateway = aiGateway;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AiChatResponse ask(AskRequest request, UUID actorUserId) {
        AiConversation conversation = resolveConversation(request.conversationId(), request.message(), actorUserId);
        AiGateway.AiOutcome outcome = aiGateway.generate(conversation.getId(), MODULE, request.message(), actorUserId);
        return new AiChatResponse(conversation.getCode(), outcome.responseText(), outcome.model(), true);
    }

    private AiConversation resolveConversation(String conversationCode, String firstMessage, UUID actorUserId) {
        if (conversationCode != null && !conversationCode.isBlank()) {
            return conversationRepository.findByCodeAndUserId(conversationCode, actorUserId)
                    .orElseThrow(AisExceptions::notFound);
        }
        Instant now = clock.instant();
        int year = Year.now(clock).getValue();
        AiConversation conversation = new AiConversation();
        conversation.setCode(codeGenerator.next("mf12_ai_conversations:" + year, "AIC-" + year, 4));
        conversation.setUserId(actorUserId);
        conversation.setTitle(firstMessage == null ? "Hội thoại AI"
                : firstMessage.substring(0, Math.min(firstMessage.length(), 80)));
        conversation.setStartedAt(now);
        conversation.setLastMessageAt(now);
        return conversationRepository.save(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AiConversationSummary> history(int page, int pageSize, UUID actorUserId) {
        List<AiConversation> all = conversationRepository.findByUserIdOrderByLastMessageAtDesc(actorUserId);
        int fromIndex = Math.min((page - 1) * pageSize, all.size());
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        List<AiConversationSummary> items = all.subList(fromIndex, toIndex).stream()
                .map(c -> new AiConversationSummary(c.getCode(), c.getTitle(), c.getStartedAt(),
                        c.getLastMessageAt(), requestRepository.findByConversationIdOrderByRequestedAtAsc(c.getId()).size()))
                .toList();
        int pageCount = Math.max(1, (int) Math.ceil(all.size() / (double) pageSize));
        return new PagedResponse<>(items, all.size(), Math.min(Math.max(1, page), pageCount), pageSize, pageCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiConversationDetail> conversation(String conversationCode, UUID actorUserId) {
        return conversationRepository.findByCodeAndUserId(conversationCode, actorUserId)
                .map(c -> {
                    List<AiMessageView> messages = requestRepository
                            .findByConversationIdOrderByRequestedAtAsc(c.getId()).stream()
                            .map(this::toMessageView)
                            .toList();
                    return new AiConversationDetail(c.getCode(), c.getTitle(), c.getStartedAt(),
                            c.getLastMessageAt(), messages);
                });
    }

    private AiMessageView toMessageView(AiRequest r) {
        return new AiMessageView(r.getCode(), r.getPrompt(), r.getResponseText(),
                r.getStatus() == null ? null : r.getStatus().name(), r.getRequestedAt());
    }
}
