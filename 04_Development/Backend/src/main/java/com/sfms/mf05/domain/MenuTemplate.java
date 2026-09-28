package com.sfms.mf05.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Thực đơn mẫu (CF-5) — áp vào ô (ngày × buổi) khi lập thực đơn. Sửa mẫu KHÔNG cascade vào
 * các {@code Menu} đã áp mẫu trước đó (TDS §9.3.7).
 */
@Entity
@Table(name = "menu_templates",
        uniqueConstraints = @UniqueConstraint(name = "uq_menu_templates_school_code",
                columnNames = {"school_id", "code"}))
public class MenuTemplate extends BaseEntity {

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 16)
    private MealType mealType;

    @Column(name = "audience", nullable = false, length = 32)
    private String audience = "ALL";

    /**
     * V44 — FK UUID tới {@code grades} (mf02), NULLABLE — trống = dùng chung mọi khối (không
     * đổi hành vi cũ). KHÔNG import entity mf02 (AP-AI-005) — cùng khuôn {@link Menu#gradeId}.
     */
    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "note", length = 300)
    private String note;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("servingOrder ASC")
    private List<MenuTemplateDish> dishes = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public UUID getGradeId() {
        return gradeId;
    }

    public void setGradeId(UUID gradeId) {
        this.gradeId = gradeId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<MenuTemplateDish> getDishes() {
        return dishes;
    }

    public void setDishes(List<MenuTemplateDish> dishes) {
        this.dishes = dishes == null ? new ArrayList<>() : dishes;
    }
}
