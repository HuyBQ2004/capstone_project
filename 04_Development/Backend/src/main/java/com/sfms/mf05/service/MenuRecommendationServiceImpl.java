package com.sfms.mf05.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.error.MnuExceptions;
import com.sfms.mf05.port.AiAssistPort;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionRequest;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ADR-M05-03 — MF05 KHÔNG tự tích hợp Gemini SDK; gọi qua {@link AiAssistPort} do <b>MF12</b>
 * cung cấp (Sóng 3). M05-D16: ở Bước 2 của MF05, MF12 CHƯA implement bean này ⇒
 * {@code Optional<AiAssistPort>} rỗng ⇒ LUÔN trả {@code 503 MENU-073}/MSG-073 — đúng hành vi
 * fallback bắt buộc (UC-075 AF1, KHÔNG chặn lập thủ công).
 */
@Service
public class MenuRecommendationServiceImpl implements MenuRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(MenuRecommendationServiceImpl.class);

    private final Optional<AiAssistPort> aiAssistPort;

    public MenuRecommendationServiceImpl(Optional<AiAssistPort> aiAssistPort) {
        this.aiAssistPort = aiAssistPort;
    }

    @Override
    public AiSuggestionResponse suggest(String menuCode, AiSuggestionRequest request, AuthUser actor) {
        if (aiAssistPort.isEmpty()) {
            // BR-157 — MF12 chưa lên bean; vẫn phát log để đối chiếu khi MF12 archive.
            log.info("AiMenuSuggestionRequested menuCode={} actor={} (no AiAssistPort bean — MENU-073)",
                    menuCode, actor.email());
            throw MnuExceptions.aiUnavailable();
        }
        try {
            return aiAssistPort.get().suggest(request);
        } catch (Exception e) {
            log.warn("AiAssistPort.suggest failed for menuCode={}: {}", menuCode, e.getMessage());
            throw MnuExceptions.aiUnavailable();
        }
    }
}
