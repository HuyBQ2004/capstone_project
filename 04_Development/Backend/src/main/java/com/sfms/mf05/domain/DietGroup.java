package com.sfms.mf05.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Nhóm chế độ ăn đặc biệt — danh mục toàn hệ thống (BR-065, ADR-M05-01).
 *
 * <p>M05-D05 — {@code matchDiet} là chuỗi tự do khớp với {@code special_meal_requests
 * .dietary_type} của MF03 (danh mục MỞ, BR-220/{@code StandardDietCatalog}). MF05 KHÔNG
 * khai enum cho danh mục này.
 */
@Entity
@Table(name = "diet_groups", uniqueConstraints = @UniqueConstraint(name = "uq_diet_groups_code", columnNames = "code"))
public class DietGroup extends BaseEntity {

    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    @Column(name = "label", nullable = false, length = 64)
    private String label;

    @Column(name = "match_diet", length = 64)
    private String matchDiet;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exclude_allergens", nullable = false, columnDefinition = "jsonb")
    private List<String> excludeAllergens = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exclude_ingredient_keywords", nullable = false, columnDefinition = "jsonb")
    private List<String> excludeIngredientKeywords = new ArrayList<>();

    @Column(name = "note", length = 300)
    private String note;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMatchDiet() {
        return matchDiet;
    }

    public void setMatchDiet(String matchDiet) {
        this.matchDiet = matchDiet;
    }

    public List<String> getExcludeAllergens() {
        return excludeAllergens;
    }

    public void setExcludeAllergens(List<String> excludeAllergens) {
        this.excludeAllergens = excludeAllergens == null ? new ArrayList<>() : excludeAllergens;
    }

    public List<String> getExcludeIngredientKeywords() {
        return excludeIngredientKeywords;
    }

    public void setExcludeIngredientKeywords(List<String> excludeIngredientKeywords) {
        this.excludeIngredientKeywords = excludeIngredientKeywords == null
                ? new ArrayList<>() : excludeIngredientKeywords;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
