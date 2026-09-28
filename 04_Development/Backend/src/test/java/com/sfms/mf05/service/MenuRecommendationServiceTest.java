package com.sfms.mf05.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.error.MnuError;
import com.sfms.mf05.port.AiAssistPort;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionRequest;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionResponse;
import com.sfms.common.error.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF05-TC-014/015 — {@code MenuRecommendationServiceImpl} là wrapper mỏng quanh
 * {@code Optional<AiAssistPort>} (ADR-M05-03/M05-D16). TestSpec gốc giả định bean LUÔN vắng ở
 * Bước 2 (nên TC-014 mô tả timeout dẫn tới 503) — nhưng {@code MenuAiSuggestIT} ghi rõ
 * MF12 đã lên {@code GeminiAiAssistAdapter} thật (bean không còn rỗng). Test unit ở đây bám
 * theo CODE THẬT: {@code suggest()} chỉ có 2 nhánh — bean vắng HOẶC bean ném exception (kể cả
 * timeout) đều → 503 MENU-073/MSG-073 (TC-014); bean trả kết quả hợp lệ → trả thẳng, KHÔNG
 * biến đổi gì (TC-015).
 */
class MenuRecommendationServiceTest {

    private static AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-0022", "caterer@anbinhcatering.vn",
                RoleKey.SERVICE_CATERERS, null, UUID.randomUUID());
    }

    // ───────────────────────── MF05-TC-014 — bean vắng HOẶC lỗi (kể cả timeout) → 503 ─────────────────────────

    @Test
    void suggestWithoutAiAssistPortBeanReturns503() {
        MenuRecommendationServiceImpl service = new MenuRecommendationServiceImpl(Optional.empty());
        AiSuggestionRequest request = new AiSuggestionRequest("LUNCH", 650, List.of(), null, List.of());

        assertThatThrownBy(() -> service.suggest("MENU-2026-0001", request, caterer()))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException ex = (ApiException) e;
                    assertThat(ex.getCode()).isEqualTo(MnuError.AI_UNAVAILABLE);
                });
    }

    @Test
    void suggestWhenPortThrowsTimeoutReturns503WithoutPropagatingRawException() {
        AiAssistPort port = mock(AiAssistPort.class);
        when(port.suggest(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Gemini timeout after 10s"));
        MenuRecommendationServiceImpl service = new MenuRecommendationServiceImpl(Optional.of(port));
        AiSuggestionRequest request = new AiSuggestionRequest("LUNCH", 650, List.of(), null, List.of());

        assertThatThrownBy(() -> service.suggest("MENU-2026-0001", request, caterer()))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.AI_UNAVAILABLE));
    }

    // ───────────────────────── MF05-TC-015 — AI happy path: trả thẳng kết quả của port ─────────────────────────

    @Test
    void suggestHappyPathReturnsPortResultUnchanged() {
        AiAssistPort port = mock(AiAssistPort.class);
        AiSuggestionResponse expected = new AiSuggestionResponse("gemini-2.5-flash",
                List.of(new AiAssistPort.Suggestion("dau-phu-sot-ca-chua", "Trong tồn kho, trong trần chi phí",
                        java.math.BigDecimal.valueOf(170), List.of("Đậu nành"), true,
                        java.math.BigDecimal.valueOf(9000))),
                new AiAssistPort.NutritionBreakdown(java.math.BigDecimal.valueOf(650),
                        java.math.BigDecimal.valueOf(30), java.math.BigDecimal.valueOf(20),
                        java.math.BigDecimal.valueOf(70)),
                "AIREQ-0001");
        when(port.suggest(org.mockito.ArgumentMatchers.any())).thenReturn(expected);
        MenuRecommendationServiceImpl service = new MenuRecommendationServiceImpl(Optional.of(port));
        AiSuggestionRequest request = new AiSuggestionRequest("LUNCH", 650, List.of(), null, List.of());

        AiSuggestionResponse result = service.suggest("MENU-2026-0001", request, caterer());

        assertThat(result).isSameAs(expected);
        assertThat(result.suggestions()).hasSize(1);
        assertThat(result.suggestions().get(0).inStock()).isTrue();
    }
}
