package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.dto.DistributionDtos.ConfirmBatchRequest;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
import com.sfms.mf09.dto.DistributionDtos.DistributionSummaryResponse;
import com.sfms.mf09.dto.DistributionDtos.ErrorReportRequest;
import com.sfms.mf09.dto.DistributionDtos.PlannedDishesResponse;
import com.sfms.common.enums.MealType;

import java.time.LocalDate;
import java.util.List;

/** {@code student-distributions} (UC-016/047/103/132). TDS-MF09_Backend.md §8.1. */
public interface StudentDistributionService {

    /** D11 — {@code PagedResponse<DistributionResponse>} hoặc rút gọn cho Service Caterers. */
    PagedResponse<?> list(ListQuery query, AuthUser actor);

    DistributionResponse getByCode(String code, AuthUser actor);

    /** BR-130/BR-094/BR-120 — {@code POST /{code}/serve}. */
    DistributionResponse serve(String code, AuthUser actor);

    /** UC-016 — {@code POST /confirm}, N học sinh trong 1 transaction. */
    List<DistributionResponse> confirmBatch(ConfirmBatchRequest req, AuthUser actor);

    /** ADR-M09-3 — {@code POST /{code}/revoke}, chỉ trong ngày. */
    DistributionResponse revoke(String code, AuthUser actor);

    /** UC-016 AF1 — {@code POST /{code}/error-report}. */
    DistributionResponse reportError(String code, ErrorReportRequest req, AuthUser actor);

    /** UC-132 — {@code GET /summary}, tính runtime (ADR-M09-2), KHÔNG bảng tổng hợp. */
    List<DistributionSummaryResponse> summary(LocalDate date, AuthUser actor);

    /**
     * 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 1 — {@code GET /planned-dishes}.
     * Suất DỰ KIẾN của học sinh còn PENDING trong (date, class, mealType), dùng LẠI đúng logic
     * thay món của {@code doServe()} nhưng KHÔNG ghi gì (an toàn gọi lại nhiều lần).
     */
    PlannedDishesResponse plannedDishes(LocalDate date, String className, MealType mealType, AuthUser actor);
}
