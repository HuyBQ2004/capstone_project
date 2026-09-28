package com.sfms.mf05.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.MenuTemplate;
import com.sfms.mf05.domain.MenuTemplateDish;
import com.sfms.mf05.dto.MenuTemplateDtos.CreateMenuTemplateRequest;
import com.sfms.mf05.dto.MenuTemplateDtos.MenuTemplateResponse;
import com.sfms.mf05.error.MnuExceptions;
import com.sfms.mf05.mapper.MenuTemplateMapper;
import com.sfms.mf05.repository.DishRepository;
import com.sfms.mf05.repository.MenuTemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MenuTemplateServiceImpl implements MenuTemplateService {

    private final MenuTemplateRepository menuTemplateRepository;
    private final DishRepository dishRepository;
    private final MenuTemplateMapper mapper;
    private final MenuAssembler assembler;
    private final ReferenceResolver referenceResolver;

    public MenuTemplateServiceImpl(MenuTemplateRepository menuTemplateRepository, DishRepository dishRepository,
                                    MenuTemplateMapper mapper, MenuAssembler assembler,
                                    ReferenceResolver referenceResolver) {
        this.menuTemplateRepository = menuTemplateRepository;
        this.dishRepository = dishRepository;
        this.mapper = mapper;
        this.assembler = assembler;
        this.referenceResolver = referenceResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MenuTemplateResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Page<MenuTemplate> page = menuTemplateRepository.findAll(MenuTemplateSpec.build(query, schoolId), query.toPageable());
        List<MenuTemplateResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public MenuTemplateResponse detail(String code, AuthUser actor) {
        return toResponse(loadInScope(code, actor));
    }

    private MenuTemplateResponse toResponse(MenuTemplate template) {
        return mapper.toResponse(template, referenceResolver.gradeLabel(template.getGradeId()));
    }

    /** V44 — parse {@code gradeId} (UUID string) từ request; {@code null}/rỗng = dùng chung mọi khối. */
    private UUID parseGradeId(String gradeId) {
        if (gradeId == null || gradeId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(gradeId.trim());
        } catch (IllegalArgumentException e) {
            throw MnuExceptions.validationFailed("gradeId", "must be a valid Grade UUID");
        }
    }

    @Override
    @Transactional
    public MenuTemplateResponse create(CreateMenuTemplateRequest request, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        String code = request.id();
        if (code == null || code.isBlank()) {
            throw MnuExceptions.validationFailed("id", "required (TPL-<slug>, client-generated, D5)");
        }
        if (menuTemplateRepository.existsBySchoolIdAndCode(schoolId, code)) {
            throw MnuExceptions.duplicateValue("id", code);
        }
        MenuTemplate template = new MenuTemplate();
        template.setSchoolId(schoolId);
        template.setCode(code);
        template.setName(request.name());
        template.setMealType(request.mealType());
        template.setAudience(request.audience() == null || request.audience().isBlank() ? "ALL" : request.audience());
        template.setGradeId(parseGradeId(request.gradeId()));
        template.setNote(request.note());
        template.setActive(request.isActive() == null || request.isActive());
        applyDishIds(template, request.dishIds());
        menuTemplateRepository.saveAndFlush(template);
        return toResponse(template);
    }

    @Override
    @Transactional
    public MenuTemplateResponse update(String code, Map<String, Object> body, AuthUser actor) {
        MenuTemplate template = loadInScope(code, actor);
        if (body.containsKey("name")) {
            template.setName(String.valueOf(body.get("name")));
        }
        if (body.containsKey("mealType")) {
            template.setMealType(MealType.valueOf(String.valueOf(body.get("mealType"))));
        }
        if (body.containsKey("audience")) {
            Object v = body.get("audience");
            template.setAudience(v == null || String.valueOf(v).isBlank() ? "ALL" : String.valueOf(v));
        }
        if (body.containsKey("gradeId")) {
            Object v = body.get("gradeId");
            template.setGradeId(v == null ? null : parseGradeId(String.valueOf(v)));
        }
        if (body.containsKey("note")) {
            Object v = body.get("note");
            template.setNote(v == null ? null : String.valueOf(v));
        }
        if (body.containsKey("isActive")) {
            template.setActive(Boolean.TRUE.equals(body.get("isActive")));
        }
        if (body.containsKey("dishIds")) {
            @SuppressWarnings("unchecked")
            List<String> dishIds = (List<String>) body.get("dishIds");
            applyDishIds(template, dishIds);
        }
        // Sửa mẫu KHÔNG cascade vào Menu đã áp mẫu trước đó (TDS §9.3.7) — không chạm menus ở đây.
        menuTemplateRepository.saveAndFlush(template);
        return toResponse(template);
    }

    @Override
    @Transactional
    public void delete(String code, AuthUser actor) {
        MenuTemplate template = loadInScope(code, actor);
        menuTemplateRepository.delete(template);
    }

    private MenuTemplate loadInScope(String code, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        return menuTemplateRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(MnuExceptions::notFound);
    }

    private void applyDishIds(MenuTemplate template, List<String> dishIds) {
        if (!template.getDishes().isEmpty()) {
            template.getDishes().clear();
            menuTemplateRepository.saveAndFlush(template);
        } else {
            template.getDishes().clear();
        }
        if (dishIds == null) {
            return;
        }
        List<String> distinctDishIds = dishIds.stream().distinct().toList();
        int order = 0;
        for (String dishId : distinctDishIds) {
            Dish dish = dishRepository.findByCode(dishId)
                    .orElseThrow(() -> MnuExceptions.validationFailed("dishIds", "unknown dish: " + dishId));
            MenuTemplateDish td = new MenuTemplateDish();
            td.setTemplate(template);
            td.setDish(dish);
            td.setServingOrder(order++);
            template.getDishes().add(td);
        }
    }
}
