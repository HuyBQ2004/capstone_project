package com.sfms.mf07.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** Mẫu số 3 — kiểm tra trước khi ăn (QĐ 1246 Điều 5). Bảng con, PK UUID (M07-D01). */
@Entity
@Table(name = "step3_rows")
public class Step3Row {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InspectionSession session;

    @Column(name = "row_order", nullable = false)
    private int rowOrder;

    @Column(nullable = false, length = 48)
    private String shift;

    @Column(name = "dish_id")
    private UUID dishId;

    @Column(name = "dish_name", nullable = false, length = 160)
    private String dishName;

    @Column(length = 16)
    private String servings;

    @Column(name = "divided_done_at", length = 32)
    private String dividedDoneAt;

    @Column(name = "eat_start_at", length = 32)
    private String eatStartAt;

    /**
     * Mẫu 3 cột 7 — dụng cụ chia/chứa đựng/che đậy ĐẠT yêu cầu vệ sinh (V56, 08/08/2026).
     * Trước đó cột 7 chỉ có ô chữ {@link #utensils} để khai TÊN dụng cụ, không trả lời được
     * câu hỏi mà biểu mẫu pháp lý cần: dụng cụ có đạt hay không.
     */
    @Column(name = "utensils_pass", nullable = false)
    private boolean utensilsPass = true;

    /** Ghi chú kèm cột 7 (tuỳ chọn) — loại dụng cụ, hoặc lý do khi Không đạt. */
    @Column(length = 300)
    private String utensils;

    /** BR-091 — nhiệt độ giữ ấm ≥ 60°C. ⚠️ CF-24: FE chưa có ô nhập, có thể NULL. */
    @Column(name = "holding_temp", precision = 5, scale = 2)
    private BigDecimal holdingTemp;

    @Column(name = "sensory_pass", nullable = false)
    private boolean sensoryPass;

    @Column(length = 500)
    private String action;

    @Column(name = "photo_count", nullable = false)
    private int photoCount;

    public UUID getId() {
        return id;
    }

    public InspectionSession getSession() {
        return session;
    }

    public void setSession(InspectionSession session) {
        this.session = session;
    }

    public int getRowOrder() {
        return rowOrder;
    }

    public void setRowOrder(int rowOrder) {
        this.rowOrder = rowOrder;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getDividedDoneAt() {
        return dividedDoneAt;
    }

    public void setDividedDoneAt(String dividedDoneAt) {
        this.dividedDoneAt = dividedDoneAt;
    }

    public String getEatStartAt() {
        return eatStartAt;
    }

    public void setEatStartAt(String eatStartAt) {
        this.eatStartAt = eatStartAt;
    }

    public boolean isUtensilsPass() {
        return utensilsPass;
    }

    public void setUtensilsPass(boolean utensilsPass) {
        this.utensilsPass = utensilsPass;
    }

    public String getUtensils() {
        return utensils;
    }

    public void setUtensils(String utensils) {
        this.utensils = utensils;
    }

    public BigDecimal getHoldingTemp() {
        return holdingTemp;
    }

    public void setHoldingTemp(BigDecimal holdingTemp) {
        this.holdingTemp = holdingTemp;
    }

    public boolean isSensoryPass() {
        return sensoryPass;
    }

    public void setSensoryPass(boolean sensoryPass) {
        this.sensoryPass = sensoryPass;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }
}
