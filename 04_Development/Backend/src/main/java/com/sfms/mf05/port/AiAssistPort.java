package com.sfms.mf05.port;

import java.math.BigDecimal;
import java.util.List;

/**
 * UC-075 — cổng gọi AI (Gemini) cho gợi ý thực đơn (BR-064/BR-156). ADR-M05-03: MF05 KHÔNG
 * tự tích hợp SDK/API key Gemini, gọi qua interface do <b>MF12</b> cung cấp (Sóng 3,
 * M05-D16). Ở Bước 2 của MF05, MF12 CHƯA implement bean này — {@code Optional<AiAssistPort>}
 * rỗng ⇒ {@code POST /menus/{code}/ai-suggest} luôn trả {@code 503 MENU-073}/MSG-073
 * (ADR-M05-03 fallback bắt buộc, KHÔNG chặn lập thủ công).
 *
 * <p>Interface này là <b>stub khai hộ MF12</b> — khi MF12 lên, họ implement
 * {@code @Component} class riêng, MF05 không cần sửa gì (chỉ cần bean tồn tại).
 */
public interface AiAssistPort {

    /** Timeout 10s (§4.1) — implementation của MF12 tự chịu trách nhiệm timeout nội bộ. */
    AiSuggestionResponse suggest(AiSuggestionRequest request);

    record AiSuggestionRequest(
            String mealType,
            Integer targetKcal,
            List<String> excludeAllergens,
            BigDecimal costCeiling,
            List<InStockIngredient> inStock
    ) {}

    record InStockIngredient(String ingredientCode, String name, BigDecimal quantity) {}

    record AiSuggestionResponse(
            String model,
            List<Suggestion> suggestions,
            NutritionBreakdown nutritionBreakdown,
            String archivedRequestId
    ) {}

    record Suggestion(String dishId, String reason, BigDecimal kcal,
                       List<String> allergens, boolean inStock, BigDecimal estimatedCost) {}

    record NutritionBreakdown(BigDecimal kcal, BigDecimal protein, BigDecimal fat, BigDecimal carb) {}
}
