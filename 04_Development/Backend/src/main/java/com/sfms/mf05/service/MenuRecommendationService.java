package com.sfms.mf05.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionRequest;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionResponse;

/** UC-075 — BR-064/BR-156, gọi qua {@link com.sfms.mf05.port.AiAssistPort} (MF12, ADR-M05-03). */
public interface MenuRecommendationService {

    /** AI lỗi/timeout/chưa có bean -> {@code AiUnavailableException} (MENU-073), KHÔNG chặn lập tay. */
    AiSuggestionResponse suggest(String menuCode, AiSuggestionRequest request, AuthUser actor);
}
