package com.sfms.mf05.mapper;

import com.sfms.mf05.domain.MenuTemplate;
import com.sfms.mf05.dto.MenuTemplateDtos.MenuTemplateResponse;
import org.springframework.stereotype.Component;

@Component
public class MenuTemplateMapper {

    public MenuTemplateResponse toResponse(MenuTemplate t, String gradeName) {
        return new MenuTemplateResponse(
                t.getCode(), t.getName(), t.getMealType().name(), t.getAudience(), gradeName,
                t.getDishes().stream().map(td -> td.getDish().getCode()).toList(),
                t.getNote(), t.isActive());
    }
}
