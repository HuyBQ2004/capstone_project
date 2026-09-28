package com.sfms.mf05.fixture;

import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.DishCategory;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.domain.MenuDish;
import com.sfms.mf05.domain.MenuDishSubstitute;
import com.sfms.mf05.domain.MenuStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Trợ giúp dựng entity in-memory cho UNIT test (Mockito, không chạm DB) — khác
 * {@code Mf05Fixtures} (JDBC thô, dùng cho Integration/Security test). Đối tượng trả về
 * KHÔNG set 4 field audit của {@code BaseEntity} (L22 — Spring Data/AuditorAware lo, unit
 * test không cần và không có context để điền).
 */
public final class Mf05UnitFixtures {

    public static final UUID SCHOOL_ID = UUID.randomUUID();
    public static final UUID GRADE_ID = UUID.randomUUID();
    public static final UUID PARTNER_ID = UUID.randomUUID();

    private Mf05UnitFixtures() {
    }

    /** Menu DRAFT hợp lệ tối thiểu — không món, ngày phục vụ mặc định today+10 (an toàn BR-061). */
    public static Menu newMenu(Consumer<Menu> customizer) {
        Menu menu = new Menu();
        menu.setCode("MENU-2026-0001");
        menu.setSchoolId(SCHOOL_ID);
        menu.setCreatedBySupplierId(PARTNER_ID);
        menu.setWeekLabel("Tuan test");
        menu.setName("Thuc don test");
        menu.setGradeId(GRADE_ID);
        menu.setMenuDate(LocalDate.now().plusDays(10));
        menu.setMealType(MealType.LUNCH);
        menu.setAudience("ALL");
        menu.setStatus(MenuStatus.DRAFT);
        menu.setServings(300);
        if (customizer != null) {
            customizer.accept(menu);
        }
        return menu;
    }

    public static Dish newDish(String code, String name, DishCategory category, boolean withIngredient,
                                String... manualAllergens) {
        Dish dish = new Dish();
        dish.setCode(code);
        dish.setName(name);
        dish.setCategory(category);
        dish.setKcal(BigDecimal.valueOf(100));
        dish.setProtein(BigDecimal.valueOf(5));
        dish.setFat(BigDecimal.valueOf(5));
        dish.setCarb(BigDecimal.valueOf(10));
        dish.setManualAllergens(java.util.Arrays.asList(manualAllergens));
        dish.setActive(true);
        if (withIngredient) {
            var ing = new com.sfms.mf05.domain.DishIngredient();
            ing.setDish(dish);
            ing.setIngredientCode("ING-" + code.toUpperCase());
            ing.setName(name);
            ing.setQuantity(BigDecimal.valueOf(100));
            ing.setUnit("g");
            dish.getIngredients().add(ing);
        }
        return dish;
    }

    /** Gắn 1 món vào menu, không suất thay thế. */
    public static MenuDish addDish(Menu menu, Dish dish, int portions) {
        MenuDish md = new MenuDish();
        md.setMenu(menu);
        md.setDish(dish);
        md.setServingOrder(menu.getMenuDishes().size());
        md.setPortions(portions);
        menu.getMenuDishes().add(md);
        return md;
    }

    /** Gắn 1 suất thay thế vào món đã có trong menu. */
    public static void addSubstitute(MenuDish menuDish, Dish substituteDish) {
        MenuDishSubstitute sub = new MenuDishSubstitute();
        sub.setMenuDish(menuDish);
        sub.setSubstituteDish(substituteDish);
        sub.setPortions(menuDish.getPortions());
        menuDish.getSubstitutes().add(sub);
    }

    public static AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-0022", "caterer@anbinhcatering.vn",
                RoleKey.SERVICE_CATERERS, null, PARTNER_ID);
    }

    public static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "admin@dtd.edu.vn",
                RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }
}
