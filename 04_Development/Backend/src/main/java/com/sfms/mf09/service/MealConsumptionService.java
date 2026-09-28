package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.dto.MealRecordDtos.BatchConsumptionRequest;
import com.sfms.mf09.dto.MealRecordDtos.MealRecordResponse;
import com.sfms.mf09.dto.MealRecordDtos.UpdateConsumptionRequest;

import java.util.List;

/** {@code meal-records} (UC-080/121/125/133). TDS-MF09_Backend.md §8.1. */
public interface MealConsumptionService {

    PagedResponse<MealRecordResponse> list(ListQuery query, AuthUser actor);

    /** D12 — chỉ nhận field sửa được ({@code consumption}, {@code specialMealServed}). */
    MealRecordResponse update(String code, UpdateConsumptionRequest req, AuthUser actor);

    List<MealRecordResponse> updateBatch(BatchConsumptionRequest req, AuthUser actor);
}
