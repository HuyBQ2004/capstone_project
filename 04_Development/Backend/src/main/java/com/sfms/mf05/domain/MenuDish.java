package com.sfms.mf05.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

/**
 * Một món trong một thực đơn (ánh xạ từ {@code blocksData[].dishes[]} — CF-12).
 * BẮT BUỘC tham chiếu món bằng khóa ({@code dishId}), KHÔNG lưu tên món (ràng buộc C5).
 */
@Entity
@Table(name = "menu_dishes",
        uniqueConstraints = @UniqueConstraint(name = "uq_menu_dish", columnNames = {"menu_id", "dish_id"}))
public class MenuDish extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    /**
     * V55 — mẫu thực đơn đã sinh ra món này trong ô, hoặc {@code null} nếu thêm tay.
     *
     * <p>Lưu UUID trần (không {@code @ManyToOne}): đây là dấu vết NGUỒN GỐC để UI gộp/gỡ
     * món theo mẫu, không phải quan hệ nghiệp vụ cần navigate. Xoá mẫu chỉ mất dấu, không
     * đụng tới thực đơn đã lập (FK {@code ON DELETE SET NULL}).
     */
    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "serving_order", nullable = false)
    private int servingOrder;

    @Column(name = "portions", nullable = false)
    private int portions;

    /** true khi món có ≥1 suất riêng (mock: {@code SeedDish.isSpecialDish}). */
    @Column(name = "is_special_dish", nullable = false)
    private boolean specialDish;

    @OneToMany(mappedBy = "menuDish", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<MenuDishSubstitute> substitutes = new ArrayList<>();

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public int getServingOrder() {
        return servingOrder;
    }

    public void setServingOrder(int servingOrder) {
        this.servingOrder = servingOrder;
    }

    public int getPortions() {
        return portions;
    }

    public void setPortions(int portions) {
        this.portions = portions;
    }

    public boolean isSpecialDish() {
        return specialDish;
    }

    public void setSpecialDish(boolean specialDish) {
        this.specialDish = specialDish;
    }

    public List<MenuDishSubstitute> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(List<MenuDishSubstitute> substitutes) {
        this.substitutes = substitutes == null ? new ArrayList<>() : substitutes;
    }
}
