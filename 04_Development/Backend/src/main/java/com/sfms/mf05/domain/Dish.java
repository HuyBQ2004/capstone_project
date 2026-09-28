package com.sfms.mf05.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Thư viện món dùng chung toàn hệ thống (KHÔNG gắn {@code school_id} — TDS §5.2).
 * CF-6: KHÔNG có {@code supplier}/{@code kitchen} (mock đã bỏ); CÓ {@code defaultSubstituteId}.
 */
@Entity
@Table(name = "dishes", uniqueConstraints = @UniqueConstraint(name = "uq_dishes_code", columnNames = "code"))
public class Dish extends BaseEntity {

    /** D1 — slug do client sinh, VD {@code "ca-basa-sot-ca-chua"}. UNIQUE toàn hệ thống. */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 24)
    private DishCategory category;

    @Column(name = "kcal", nullable = false, precision = 8, scale = 2)
    private BigDecimal kcal;

    @Column(name = "protein", nullable = false, precision = 8, scale = 2)
    private BigDecimal protein;

    @Column(name = "fat", nullable = false, precision = 8, scale = 2)
    private BigDecimal fat;

    @Column(name = "carb", nullable = false, precision = 8, scale = 2)
    private BigDecimal carb;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "manual_allergens", nullable = false, columnDefinition = "jsonb")
    private List<String> manualAllergens = new ArrayList<>();

    /** URL công khai (D10-BIS, StorageService.store()) — ảnh minh hoạ món, KHÔNG bắt buộc. */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "cooking_instructions", columnDefinition = "text")
    private String cookingInstructions;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** BR-065/CF-6 — món thay thế mặc định cho dị ứng (khớp {@code Dish.defaultSubstituteId}). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_substitute_id")
    private Dish defaultSubstitute;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DishIngredient> ingredients = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishCategory getCategory() {
        return category;
    }

    public void setCategory(DishCategory category) {
        this.category = category;
    }

    public BigDecimal getKcal() {
        return kcal;
    }

    public void setKcal(BigDecimal kcal) {
        this.kcal = kcal;
    }

    public BigDecimal getProtein() {
        return protein;
    }

    public void setProtein(BigDecimal protein) {
        this.protein = protein;
    }

    public BigDecimal getFat() {
        return fat;
    }

    public void setFat(BigDecimal fat) {
        this.fat = fat;
    }

    public BigDecimal getCarb() {
        return carb;
    }

    public void setCarb(BigDecimal carb) {
        this.carb = carb;
    }

    public List<String> getManualAllergens() {
        return manualAllergens;
    }

    public void setManualAllergens(List<String> manualAllergens) {
        this.manualAllergens = manualAllergens == null ? new ArrayList<>() : manualAllergens;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCookingInstructions() {
        return cookingInstructions;
    }

    public void setCookingInstructions(String cookingInstructions) {
        this.cookingInstructions = cookingInstructions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Dish getDefaultSubstitute() {
        return defaultSubstitute;
    }

    public void setDefaultSubstitute(Dish defaultSubstitute) {
        this.defaultSubstitute = defaultSubstitute;
    }

    public List<DishIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<DishIngredient> ingredients) {
        this.ingredients = ingredients == null ? new ArrayList<>() : ingredients;
    }
}
