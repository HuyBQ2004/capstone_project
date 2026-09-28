package com.sfms.mf05.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Suất thay thế của một món trong thực đơn — dùng để hoá giải xung đột dị ứng (BR-063/065). */
@Entity
@Table(name = "menu_dish_substitutes")
public class MenuDishSubstitute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_dish_id", nullable = false)
    private MenuDish menuDish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_dish_id", nullable = false)
    private Dish substituteDish;

    @Column(name = "portions", nullable = false)
    private int portions;

    @Column(name = "note", length = 300)
    private String note;

    public MenuDish getMenuDish() {
        return menuDish;
    }

    public void setMenuDish(MenuDish menuDish) {
        this.menuDish = menuDish;
    }

    public Dish getSubstituteDish() {
        return substituteDish;
    }

    public void setSubstituteDish(Dish substituteDish) {
        this.substituteDish = substituteDish;
    }

    public int getPortions() {
        return portions;
    }

    public void setPortions(int portions) {
        this.portions = portions;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
