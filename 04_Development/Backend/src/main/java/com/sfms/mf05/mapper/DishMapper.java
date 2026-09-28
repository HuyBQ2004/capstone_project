package com.sfms.mf05.mapper;

import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.dto.DishDtos.DishResponse;
import com.sfms.mf05.dto.MenuDtos.MenuIngredient;
import org.springframework.stereotype.Component;

@Component
public class DishMapper {

    public DishResponse toResponse(Dish d) {
        return new DishResponse(
                d.getCode(), d.getName(), d.getCategory().name(),
                d.getKcal(), d.getProtein(), d.getFat(), d.getCarb(),
                d.getManualAllergens(), d.isActive(),
                d.getDefaultSubstitute() == null ? null : d.getDefaultSubstitute().getCode(),
                d.getIngredients().stream()
                        .map(i -> new MenuIngredient(i.getIngredientCode(), i.getName(), i.getQuantity(), i.getUnit()))
                        .toList(),
                d.getImageUrl(), d.getDescription(), d.getCookingInstructions());
    }
}
