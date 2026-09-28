package com.sfms.mf05.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_template_dishes")
public class MenuTemplateDish extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private MenuTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "serving_order", nullable = false)
    private int servingOrder;

    public MenuTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MenuTemplate template) {
        this.template = template;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getServingOrder() {
        return servingOrder;
    }

    public void setServingOrder(int servingOrder) {
        this.servingOrder = servingOrder;
    }
}
