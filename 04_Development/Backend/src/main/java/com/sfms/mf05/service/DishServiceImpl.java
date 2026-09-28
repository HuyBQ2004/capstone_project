package com.sfms.mf05.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.DishCategory;
import com.sfms.mf05.domain.DishIngredient;
import com.sfms.mf05.dto.DishDtos.CreateDishRequest;
import com.sfms.mf05.dto.DishDtos.DishResponse;
import com.sfms.mf05.dto.DishDtos.InUseWarning;
import com.sfms.mf05.dto.DishDtos.UpdateDishResult;
import com.sfms.mf05.dto.MenuDtos.MenuIngredient;
import com.sfms.mf05.error.MnuExceptions;
import com.sfms.mf05.mapper.DishMapper;
import com.sfms.mf05.repository.DishRepository;
import com.sfms.mf05.repository.MenuRepository;
import com.sfms.common.storage.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DishServiceImpl implements DishService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final DishRepository dishRepository;
    private final MenuRepository menuRepository;
    /** BR-076 — kiểm nguyên liệu của món có thuộc danh mục System Admin cấu hình không. */
    private final com.sfms.common.reference.ReferenceResolver referenceResolver;
    private final DishMapper dishMapper;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;

    public DishServiceImpl(DishRepository dishRepository, MenuRepository menuRepository,
                            DishMapper dishMapper, ObjectMapper objectMapper, StorageService storageService,
                           com.sfms.common.reference.ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        this.dishRepository = dishRepository;
        this.menuRepository = menuRepository;
        this.dishMapper = dishMapper;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DishResponse> list(ListQuery query) {
        Page<Dish> page = dishRepository.findAll(DishSpec.build(query), query.toPageable());
        List<DishResponse> items = page.getContent().stream().map(dishMapper::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public DishResponse detail(String code) {
        return dishMapper.toResponse(dishRepository.findByCode(code).orElseThrow(MnuExceptions::notFound));
    }

    @Override
    @Transactional
    public DishResponse create(CreateDishRequest request) {
        String code = request.id();
        if (code == null || code.isBlank()) {
            throw MnuExceptions.validationFailed("id", "required (client-generated slug, D5)");
        }
        if (dishRepository.existsByCode(code)) {
            throw MnuExceptions.duplicateValue("id", code);
        }
        Dish dish = new Dish();
        dish.setCode(code);
        dish.setName(request.name());
        dish.setCategory(DishCategory.valueOf(request.category()));
        dish.setKcal(request.kcal());
        dish.setProtein(request.protein());
        dish.setFat(request.fat());
        dish.setCarb(request.carb());
        dish.setManualAllergens(request.manualAllergens());
        dish.setActive(request.isActive() == null || request.isActive());
        dish.setDescription(request.description());
        dish.setCookingInstructions(request.cookingInstructions());
        applyIngredients(dish, request.ingredients());
        if (request.defaultSubstituteId() != null) {
            dish.setDefaultSubstitute(dishRepository.findByCode(request.defaultSubstituteId())
                    .orElseThrow(() -> MnuExceptions.validationFailed("defaultSubstituteId", "unknown dish")));
        }
        dishRepository.saveAndFlush(dish);
        return dishMapper.toResponse(dish);
    }

    @Override
    @Transactional
    public UpdateDishResult update(String code, Map<String, Object> body) {
        Dish dish = dishRepository.findByCode(code).orElseThrow(MnuExceptions::notFound);
        if (body.containsKey("name")) {
            dish.setName(String.valueOf(body.get("name")));
        }
        if (body.containsKey("category")) {
            dish.setCategory(DishCategory.valueOf(String.valueOf(body.get("category"))));
        }
        if (body.containsKey("kcal")) {
            dish.setKcal(toBigDecimal(body.get("kcal")));
        }
        if (body.containsKey("protein")) {
            dish.setProtein(toBigDecimal(body.get("protein")));
        }
        if (body.containsKey("fat")) {
            dish.setFat(toBigDecimal(body.get("fat")));
        }
        if (body.containsKey("carb")) {
            dish.setCarb(toBigDecimal(body.get("carb")));
        }
        if (body.containsKey("manualAllergens")) {
            List<String> allergens = objectMapper.convertValue(body.get("manualAllergens"), new TypeReference<>() {});
            dish.setManualAllergens(allergens);
        }
        if (body.containsKey("description")) {
            Object v = body.get("description");
            dish.setDescription(v == null ? null : String.valueOf(v));
        }
        if (body.containsKey("cookingInstructions")) {
            Object v = body.get("cookingInstructions");
            dish.setCookingInstructions(v == null ? null : String.valueOf(v));
        }
        if (body.containsKey("defaultSubstituteId")) {
            Object v = body.get("defaultSubstituteId");
            dish.setDefaultSubstitute(v == null ? null : dishRepository.findByCode(String.valueOf(v))
                    .orElseThrow(() -> MnuExceptions.validationFailed("defaultSubstituteId", "unknown dish")));
        }
        if (body.containsKey("ingredients")) {
            List<MenuIngredient> ingredients = objectMapper.convertValue(body.get("ingredients"), new TypeReference<>() {});
            applyIngredients(dish, ingredients);
        }
        List<InUseWarning> warnings = List.of();
        if (body.containsKey("isActive")) {
            boolean newActive = Boolean.TRUE.equals(body.get("isActive"));
            dish.setActive(newActive);
            if (!newActive && menuRepository.existsMenuReferencingDish(dish.getCode())) {
                warnings = List.of(new InUseWarning("MENU-DISH-INUSE",
                        "This dish is still used by one or more unfinalized menus.", List.of()));
            }
        }
        dishRepository.saveAndFlush(dish);
        return new UpdateDishResult(dishMapper.toResponse(dish), warnings);
    }

    @Override
    @Transactional
    public void delete(String code) {
        Dish dish = dishRepository.findByCode(code).orElseThrow(MnuExceptions::notFound);
        if (menuRepository.existsMenuReferencingDish(code)) {
            throw MnuExceptions.referencedByMenu("id", code);
        }
        dishRepository.delete(dish);
    }

    @Override
    @Transactional
    public DishResponse uploadImage(String code, MultipartFile file) {
        Dish dish = dishRepository.findByCode(code).orElseThrow(MnuExceptions::notFound);
        if (file == null || file.isEmpty()) {
            throw MnuExceptions.validationFailed("file", "required");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw MnuExceptions.validationFailed("file", "unsupported type — jpeg/png/webp only");
        }
        String key = "dish-images/" + dish.getCode() + suffixOf(file.getContentType());
        dish.setImageUrl(storageService.store(key, file));
        dishRepository.saveAndFlush(dish);
        return dishMapper.toResponse(dish);
    }

    private static String suffixOf(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private void applyIngredients(Dish dish, List<MenuIngredient> ingredients) {
        dish.getIngredients().clear();
        if (ingredients == null) {
            return;
        }
        for (MenuIngredient i : ingredients) {
            /*
             * BR-076 (siết 08/08/2026, Nam) — nguyên liệu của món BẮT BUỘC thuộc danh mục do
             * System Admin cấu hình.
             *
             * Trước đó chỗ này gán thẳng `i.ingredientId()` không kiểm gì. Màn FE đã chặn,
             * nhưng gọi API trực tiếp vẫn tạo được món khai nguyên liệu bằng chữ tự do — và
             * đó KHÔNG phải lỗi lý thuyết: đo DB thật 08/08/2026 có 10/14 dòng
             * `dish_ingredients` với `ingredient_code` NULL. Cả `orders/new` lẫn
             * `inventory/issue` đều BỎ QUA dòng không có mã (`if (!ing.ingredientId) continue`)
             * ⇒ nguyên liệu rơi khỏi chuỗi đặt hàng/xuất kho/truy xuất lô mà không cảnh báo.
             * Đo theo món: "Thịt kho hành" mất 2/2 nguyên liệu, "Cá basa sốt cà chua" mất 2/3.
             */
            String ingredientCode = i.ingredientId();
            if (ingredientCode == null || ingredientCode.isBlank()) {
                throw MnuExceptions.validationFailed("ingredients.ingredientId",
                        "nguyên liệu \"" + i.name() + "\" chưa chọn từ danh mục — món phải khai "
                        + "nguyên liệu thuộc danh mục do System Admin cấu hình (BR-076)");
            }
            if (!referenceResolver.ingredientCodeExists(ingredientCode)) {
                throw MnuExceptions.validationFailed("ingredients.ingredientId",
                        "nguyên liệu \"" + ingredientCode + "\" không có trong danh mục (BR-076)");
            }

            DishIngredient di = new DishIngredient();
            di.setDish(dish);
            di.setIngredientCode(ingredientCode);
            di.setName(i.name());
            di.setQuantity(i.quantity());
            di.setUnit(i.unit() == null ? "g" : i.unit());
            dish.getIngredients().add(di);
        }
    }

    private static BigDecimal toBigDecimal(Object v) {
        return v == null ? null : new BigDecimal(v.toString());
    }
}
