package com.sfms.mf06.dto;

import java.time.LocalDate;
import java.util.List;

/** {@code GET /traceability/dish-batches} — BR-076, MF11 dùng cho bản đồ truy xuất sự cố. */
public final class TraceabilityDtos {

    private TraceabilityDtos() {
    }

    public record BatchTrace(String batchId, String ingredientId, String ingredient,
                              String supplierId, String supplier, LocalDate expiryDate, boolean isLocked) {}

    public record DishBatchesResponse(String dishId, String dish, String menuId, LocalDate serviceDate,
                                       String mealType, List<BatchTrace> batches) {}
}
