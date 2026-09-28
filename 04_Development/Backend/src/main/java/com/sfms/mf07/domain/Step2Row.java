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

/** Mẫu số 2 — kiểm tra khi chế biến (QĐ 1246 Điều 4). Bảng con, PK UUID (M07-D01). */
@Entity
@Table(name = "step2_rows")
public class Step2Row {

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

    @Column(name = "main_ingredients", length = 500)
    private String mainIngredients;

    @Column(length = 16)
    private String servings;

    @Column(name = "prep_done_at", length = 32)
    private String prepDoneAt;

    @Column(name = "cook_done_at", length = 32)
    private String cookDoneAt;

    @Column(name = "hygiene_staff", nullable = false)
    private boolean hygieneStaff;

    @Column(name = "hygiene_equipment", nullable = false)
    private boolean hygieneEquipment;

    @Column(name = "hygiene_area", nullable = false)
    private boolean hygieneArea;

    @Column(name = "sensory_pass", nullable = false)
    private boolean sensoryPass;

    @Column(name = "core_temp", precision = 5, scale = 2)
    private BigDecimal coreTemp;

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

    public String getMainIngredients() {
        return mainIngredients;
    }

    public void setMainIngredients(String mainIngredients) {
        this.mainIngredients = mainIngredients;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getPrepDoneAt() {
        return prepDoneAt;
    }

    public void setPrepDoneAt(String prepDoneAt) {
        this.prepDoneAt = prepDoneAt;
    }

    public String getCookDoneAt() {
        return cookDoneAt;
    }

    public void setCookDoneAt(String cookDoneAt) {
        this.cookDoneAt = cookDoneAt;
    }

    public boolean isHygieneStaff() {
        return hygieneStaff;
    }

    public void setHygieneStaff(boolean hygieneStaff) {
        this.hygieneStaff = hygieneStaff;
    }

    public boolean isHygieneEquipment() {
        return hygieneEquipment;
    }

    public void setHygieneEquipment(boolean hygieneEquipment) {
        this.hygieneEquipment = hygieneEquipment;
    }

    public boolean isHygieneArea() {
        return hygieneArea;
    }

    public void setHygieneArea(boolean hygieneArea) {
        this.hygieneArea = hygieneArea;
    }

    public boolean isSensoryPass() {
        return sensoryPass;
    }

    public void setSensoryPass(boolean sensoryPass) {
        this.sensoryPass = sensoryPass;
    }

    public BigDecimal getCoreTemp() {
        return coreTemp;
    }

    public void setCoreTemp(BigDecimal coreTemp) {
        this.coreTemp = coreTemp;
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
