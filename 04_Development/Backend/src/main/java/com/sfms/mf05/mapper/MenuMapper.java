package com.sfms.mf05.mapper;

import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.domain.MenuDish;
import com.sfms.mf05.domain.MenuDishSubstitute;
import com.sfms.mf05.dto.MenuDtos.MenuBlock;
import com.sfms.mf05.dto.MenuDtos.MenuBlockDish;
import com.sfms.mf05.dto.MenuDtos.MenuBlockSubstitute;
import com.sfms.mf05.dto.MenuDtos.MenuIngredient;
import com.sfms.mf05.dto.MenuDtos.MenuPublicResponse;
import com.sfms.mf05.dto.MenuDtos.MenuResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hand-written mapper (KHÔNG MapStruct) — {@code blocksData} là tái dựng lồng nhau từ
 * {@code menu_dishes}/{@code menu_dish_substitutes}/{@code dish_ingredients} (CF-12), việc
 * MapStruct không tự động hoá tốt bằng code tay minh bạch.
 *
 * <p>Vì {@code ux_menu_all_per_slot} ràng buộc một {@code Menu} = đúng MỘT ô (ngày×khối×buổi),
 * {@code blocksData} luôn có <b>đúng 1 phần tử</b> khớp {@code Menu.mealType}/{@code servings}.
 */
@Component
public class MenuMapper {

    /** D11/M05-D08 — shape đủ field (Caterer/SchoolAdmin/Medical/SysAdmin). */
    public MenuResponse toResponse(Menu m, int dishCount, int allergyConflicts,
                                    java.util.List<String> allergyConflictDishes,
                                    String createdByLabel, String createdBySupplierCode,
                                    String approvedByLabel, String gradeName) {
        return new MenuResponse(
                m.getCode(), m.getWeekLabel(), m.getName(), gradeName, m.getMenuDate(),
                m.getStatus().name(), dishCount, allergyConflicts, allergyConflictDishes,
                m.getEstimatedCost(), m.getCostCeiling(), m.getSubmittedAt(),
                createdByLabel, createdBySupplierCode, approvedByLabel,
                m.getApprovalSignature(), m.getApprovedAt(), m.getRejectReason(),
                m.getAudience(), dietGroupCode(m), m.getReason(), m.getServings(),
                toBlocksData(m));
    }

    /** D11/M05-D08 — shape rút gọn (Parent/Teacher): bỏ giá + chữ ký duyệt, chỉ PUBLISHED. */
    public MenuPublicResponse toPublicResponse(Menu m, int dishCount, int allergyConflicts,
                                                String createdByLabel, String gradeName) {
        return new MenuPublicResponse(
                m.getCode(), m.getWeekLabel(), m.getName(), gradeName, m.getMenuDate(),
                m.getStatus().name(), dishCount, allergyConflicts, m.getSubmittedAt(),
                createdByLabel, m.getAudience(), dietGroupCode(m), m.getReason(), m.getServings(),
                toBlocksData(m));
    }

    private String dietGroupCode(Menu m) {
        // Người gọi (service) chịu trách nhiệm resolve UUID -> code trước khi tạo response nếu
        // cần độ chính xác cao hơn; ở đây audience đã LÀ code (ADR-M05-01: audience=DietGroup.code).
        return "ALL".equals(m.getAudience()) ? null : m.getAudience();
    }

    public List<MenuBlock> toBlocksData(Menu m) {
        List<MenuBlockDish> dishDtos = m.getMenuDishes().stream()
                .map(this::toBlockDish)
                .toList();
        return List.of(new MenuBlock(m.getMealType(), m.getServings(), null, dishDtos));
    }

    private MenuBlockDish toBlockDish(MenuDish md) {
        Dish dish = md.getDish();
        List<MenuIngredient> ingredients = dish.getIngredients().stream()
                .map(i -> new MenuIngredient(i.getIngredientCode(), i.getName(), i.getQuantity(), i.getUnit()))
                .toList();
        List<MenuBlockSubstitute> substitutes = md.getSubstitutes().stream()
                .map(this::toSubstitute)
                .toList();
        return new MenuBlockDish(
                dish.getCode(), dish.getName(),
                dish.getKcal() == null ? null : dish.getKcal().toPlainString(),
                dish.getProtein() == null ? null : dish.getProtein().toPlainString(),
                dish.getCarb() == null ? null : dish.getCarb().toPlainString(),
                dish.getFat() == null ? null : dish.getFat().toPlainString(),
                md.isSpecialDish(), dish.getManualAllergens(), ingredients, substitutes, md.getPortions(),
                md.getTemplateId() == null ? null : md.getTemplateId().toString());
    }

    private MenuBlockSubstitute toSubstitute(MenuDishSubstitute sub) {
        Dish dish = sub.getSubstituteDish();
        List<MenuIngredient> ingredients = dish.getIngredients().stream()
                .map(i -> new MenuIngredient(i.getIngredientCode(), i.getName(), i.getQuantity(), i.getUnit()))
                .toList();
        return new MenuBlockSubstitute(
                dish.getCode(), dish.getName(),
                dish.getKcal() == null ? null : dish.getKcal().toPlainString(),
                String.valueOf(sub.getPortions()), sub.getNote(),
                dish.getManualAllergens(), dish.getManualAllergens(), ingredients);
    }
}
