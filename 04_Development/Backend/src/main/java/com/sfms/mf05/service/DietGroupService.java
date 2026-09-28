package com.sfms.mf05.service;

import com.sfms.mf05.dto.DietGroupDtos.DietGroupResponse;
import com.sfms.mf05.repository.DietGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** BR-065 — danh mục {@code diet_groups} TOÀN HỆ THỐNG, chỉ đọc (cấu hình qua DB, không CRUD FE). */
@Service
public class DietGroupService {

    private final DietGroupRepository dietGroupRepository;

    public DietGroupService(DietGroupRepository dietGroupRepository) {
        this.dietGroupRepository = dietGroupRepository;
    }

    @Transactional(readOnly = true)
    public List<DietGroupResponse> list() {
        return dietGroupRepository.findAll().stream()
                .map(g -> new DietGroupResponse(g.getCode(), g.getLabel(), g.getMatchDiet(),
                        g.getExcludeAllergens(), g.getExcludeIngredientKeywords(), g.getNote()))
                .toList();
    }
}
