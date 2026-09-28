package com.sfms.mf06.service;

import com.sfms.mf06.dto.IngredientDtos.CreateRequest;
import com.sfms.mf06.dto.IngredientDtos.IngredientResponse;
import com.sfms.mf06.dto.IngredientDtos.UpdateRequest;

import java.util.List;

/** M06-D04 — danh mục nguyên liệu dùng chung toàn hệ thống (không school-scoped). */
public interface IngredientService {

    List<IngredientResponse> list();

    /** Tạo nguyên liệu mới — chỉ SYSTEM_ADMIN. */
    IngredientResponse create(CreateRequest request);

    /** Sửa thông tin nguyên liệu — chỉ SYSTEM_ADMIN. */
    IngredientResponse update(String code, UpdateRequest request);

    /** Vô hiệu hoá nguyên liệu (soft-delete) — chỉ SYSTEM_ADMIN. */
    void deactivate(String code);
}
