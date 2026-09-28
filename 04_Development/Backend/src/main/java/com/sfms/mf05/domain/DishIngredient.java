package com.sfms.mf05.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * BOM của một món (BR-069). Bảng con — KHÔNG lộ ra API trực tiếp, không cần cột {@code code}
 * (D1 chỉ ràng buộc tài nguyên có path param).
 *
 * <p>M05-D15 — {@code ingredientCode} KHÔNG khai FK sang {@code catalog_ingredients}: bảng đó
 * thuộc MF06, chưa tồn tại. {@code NULL} = nguyên liệu ngoài danh mục (gia vị/rau thơm).
 */
@Entity
@Table(name = "dish_ingredients")
public class DishIngredient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "ingredient_code", length = 64)
    private String ingredientCode;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    /**
     * PROPOSED (plan "Đơn vị đo lường" §Phase B.8) — đổi từ {@code String} sang
     * {@code BigDecimal} 08/2026: cột cũ không validate được là số, có thể lưu rác.
     * Nullable — nguyên liệu ngoài danh mục (gia vị) có thể chưa định lượng cụ thể.
     */
    @Column(name = "quantity", precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false, length = 16)
    private String unit = "g";

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public String getIngredientCode() {
        return ingredientCode;
    }

    public void setIngredientCode(String ingredientCode) {
        this.ingredientCode = ingredientCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
