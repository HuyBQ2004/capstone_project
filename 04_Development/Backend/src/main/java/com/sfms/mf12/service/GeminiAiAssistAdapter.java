package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.mf05.port.AiAssistPort;
import com.sfms.mf12.domain.AiConversation;
import com.sfms.mf12.repository.AiConversationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * ADR-M12-01 — cổng AI duy nhất của hệ thống. {@code M12-D09}: KHÔNG khai interface mới —
 * implement {@link AiAssistPort} mà MF05 đã khai hộ (bean tồn tại là MF05 tự hết fallback
 * {@code 503 MENU-073}, MF05 không sửa dòng nào).
 *
 * <p>{@link AiAssistPort#suggest} không mang tham số actor (chữ ký do MF05 khai, §5 "không
 * refactor") ⇒ archive dùng actor hệ thống (System Admin ACTIVE đầu tiên) thay vì actor thật
 * của caterer đang thao tác — giới hạn đã biết của ranh giới interface này.
 */
@Component
public class GeminiAiAssistAdapter implements AiAssistPort {

    private static final String MODULE = "MF05_MENU";

    private final AiGateway aiGateway;
    private final AiConversationRepository conversationRepository;
    private final UserAccountRepository userAccountRepository;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public GeminiAiAssistAdapter(AiGateway aiGateway, AiConversationRepository conversationRepository,
                                  UserAccountRepository userAccountRepository, CodeGenerator codeGenerator,
                                  Clock clock) {
        this.aiGateway = aiGateway;
        this.conversationRepository = conversationRepository;
        this.userAccountRepository = userAccountRepository;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AiSuggestionResponse suggest(AiSuggestionRequest request) {
        UUID systemActorId = userAccountRepository
                .findFirstByRole_RoleKeyAndStatus("system-admin", UserStatus.ACTIVE)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No ACTIVE system-admin account to attribute MF05 AI suggestion archive to"));

        AiConversation conversation = newConversation(systemActorId);
        String prompt = buildPrompt(request);

        AiGateway.AiOutcome outcome = aiGateway.generate(conversation.getId(), MODULE, prompt, systemActorId);

        List<Suggestion> suggestions = request.inStock() == null || request.inStock().isEmpty()
                ? List.of()
                : request.inStock().stream()
                        .filter(i -> !isExcluded(i.name(), request.excludeAllergens()))
                        .limit(3)
                        .map(i -> new Suggestion(i.ingredientCode(),
                                "Gợi ý dựa trên nguyên liệu tồn kho (stub — ADR-000)",
                                BigDecimal.valueOf(request.targetKcal() == null ? 0 : request.targetKcal()),
                                List.of(), true, request.costCeiling()))
                        .toList();

        NutritionBreakdown nutrition = new NutritionBreakdown(
                BigDecimal.valueOf(request.targetKcal() == null ? 0 : request.targetKcal()),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        return new AiSuggestionResponse(outcome.model(), suggestions, nutrition, outcome.requestCode());
    }

    private AiConversation newConversation(UUID actorId) {
        Instant now = clock.instant();
        int year = Year.now(clock).getValue();
        AiConversation conversation = new AiConversation();
        conversation.setCode(codeGenerator.next("mf12_ai_conversations:" + year, "AIC-" + year, 4));
        conversation.setUserId(actorId);
        conversation.setTitle("MF05 — Gợi ý thực đơn AI");
        conversation.setStartedAt(now);
        conversation.setLastMessageAt(now);
        return conversationRepository.save(conversation);
    }

    private static String buildPrompt(AiSuggestionRequest request) {
        return "Gợi ý thực đơn: mealType=" + request.mealType()
                + ", targetKcal=" + request.targetKcal()
                + ", excludeAllergens=" + request.excludeAllergens()
                + ", costCeiling=" + request.costCeiling();
    }

    private static boolean isExcluded(String ingredientName, List<String> excludeAllergens) {
        if (excludeAllergens == null || ingredientName == null) {
            return false;
        }
        String lower = ingredientName.toLowerCase();
        return excludeAllergens.stream().anyMatch(a -> a != null && lower.contains(a.toLowerCase()));
    }
}
