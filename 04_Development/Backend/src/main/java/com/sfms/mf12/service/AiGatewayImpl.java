package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf12.domain.AiRequest;
import com.sfms.mf12.domain.AiRequestStatus;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.repository.AiConversationRepository;
import com.sfms.mf12.repository.AiRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * BR-156/BR-157 — mọi lời gọi sinh <b>đúng một</b> dòng {@code ai_requests}, trong CÙNG
 * transaction với caller. Ghi archive lỗi ⇒ {@link AisExceptions#aiArchiveFailed()} +
 * rollback (KHÔNG trả phản hồi không lưu vết). AI timeout/lỗi ⇒ vẫn lưu dòng
 * {@code status=TIMEOUT}/{@code FAILED} rồi ném {@link AisExceptions#aiUnavailable()}.
 */
@Service
public class AiGatewayImpl implements AiGateway {

    /** BR-173/BR-181 — ẩn danh hoá mã học sinh dạng {@code HS-xxxx} trước khi gửi AI/lưu trữ. */
    private static final Pattern STUDENT_CODE = Pattern.compile("\\bHS-[A-Za-z0-9-]+\\b");


    private final AiRequestRepository aiRequestRepository;
    private final AiConversationRepository aiConversationRepository;
    private final GeminiClient geminiClient;
    private final KnowledgeDocumentService knowledgeDocumentService;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public AiGatewayImpl(AiRequestRepository aiRequestRepository,
                          AiConversationRepository aiConversationRepository,
                          GeminiClient geminiClient, KnowledgeDocumentService knowledgeDocumentService,
                          ReferenceResolver referenceResolver, CodeGenerator codeGenerator, Clock clock) {
        this.aiRequestRepository = aiRequestRepository;
        this.aiConversationRepository = aiConversationRepository;
        this.geminiClient = geminiClient;
        this.knowledgeDocumentService = knowledgeDocumentService;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AiOutcome generate(UUID conversationId, String module, String rawPrompt, UUID actorUserId) {
        String redactedPrompt = redact(rawPrompt);
        Instant startedAt = clock.instant();

        List<String> context = new ArrayList<>(parentChildContext(actorUserId));
        // Chọn ĐOẠN liên quan trong ngân sách ký tự, thay vì nhét nguyên 8 tài liệu đầu tiên:
        // kho tri thức thật (00_Policies) là 442KB, một mình Thông tư 13/2016 đã 168KB.
        // Xem KnowledgeContextSelector — và lưu ý rag_service KHÔNG hề có truy hồi (không
        // embedding, không vector store; pgvector/langchain có trong requirements nhưng không
        // dòng nào import), nên phần "R" của RAG nằm ở đây.
        context.addAll(knowledgeDocumentService.contextFor(redactedPrompt));

        GeminiClient.GeminiResult geminiResult;
        String responseText = null;
        String modelName = GeminiClient.STUB_MODEL;
        AiRequestStatus status;
        try {
            geminiResult = geminiClient.completeWithModel(redactedPrompt, context);
            responseText = geminiResult.responseText();
            modelName = geminiResult.model();
            status = AiRequestStatus.SUCCESS;
        } catch (GeminiClient.AiTimeoutException e) {
            status = AiRequestStatus.TIMEOUT;
        }
        int latencyMs = (int) Duration.between(startedAt, clock.instant()).toMillis();

        int year = Year.now(clock).getValue();
        AiRequest request = new AiRequest();
        request.setCode(codeGenerator.next("mf12_ai_requests:" + year, "AIQ-" + year, 4));
        request.setConversationId(conversationId);
        request.setModule(module);
        request.setPrompt(redactedPrompt);
        request.setResponseText(responseText);
        request.setModel(modelName);
        request.setStatus(status);
        request.setLatencyMs(latencyMs);
        request.setRequestedBy(actorUserId);
        request.setRequestedAt(startedAt);
        request.setRedacted(true);

        try {
            aiRequestRepository.save(request);
        } catch (RuntimeException e) {
            throw AisExceptions.aiArchiveFailed();
        }

        aiConversationRepository.findById(conversationId)
                .ifPresent(conversation -> conversation.setLastMessageAt(startedAt));

        if (status != AiRequestStatus.SUCCESS) {
            throw AisExceptions.aiUnavailable();
        }
        return new AiOutcome(responseText, modelName, status, latencyMs, request.getCode());
    }

    private static String redact(String prompt) {
        return prompt == null ? "" : STUDENT_CODE.matcher(prompt).replaceAll("[STUDENT_ID]");
    }

    /**
     * Phụ huynh hỏi AI "tình hình con mình" — trường hợp DUY NHẤT được phép gửi dữ liệu học
     * sinh thật (tên, dị ứng, bữa ăn gần đây) cho AI, thay vì redact như mọi prompt khác.
     * An toàn vì {@code linkedStudentIds} chỉ trả về con của CHÍNH {@code actorUserId} đang gọi
     * (server tự tra cứu qua {@code student_parents}, không tin bất kỳ id nào client gửi lên) —
     * nếu actor không phải phụ huynh hoặc không có con liên kết, trả về rỗng, không ảnh hưởng
     * các vai trò khác. Giới hạn ≤5 con (BR-201) + 7 ngày gần nhất, tránh phình prompt.
     */
    private List<String> parentChildContext(UUID actorUserId) {
        List<UUID> childIds = referenceResolver.linkedStudentIds(actorUserId);
        if (childIds.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (UUID childId : childIds) {
            String name = referenceResolver.studentName(childId);
            if (name == null) {
                continue;
            }
            List<String> allergens = referenceResolver.verifiedAllergensOfStudent(childId);
            String diet = referenceResolver.specialDietOfStudent(childId).orElse(null);
            List<String> recentMeals = referenceResolver.servedMealsWithin(childId, Duration.ofDays(7)).stream()
                    .map(m -> m.date() + " (" + m.mealType() + ")")
                    .toList();

            StringBuilder sb = new StringBuilder("Con của phụ huynh đang hỏi — ").append(name).append(": ");
            sb.append(allergens.isEmpty() ? "chưa có dị ứng đã xác minh" : "dị ứng đã xác minh: " + String.join(", ", allergens));
            if (diet != null) {
                sb.append("; chế độ ăn đặc biệt: ").append(diet);
            }
            sb.append("; bữa ăn 7 ngày gần nhất: ")
                    .append(recentMeals.isEmpty() ? "chưa ghi nhận bữa nào" : String.join(", ", recentMeals));
            out.add(sb.toString());
        }
        return out;
    }
}
