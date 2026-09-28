package com.sfms.mf06.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** ADR-M06-1 — sổ APPEND-ONLY: không PUT, không DELETE (C3/ING-405). */
@Entity
@Table(name = "inventory_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uq_tx_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_tx_batch", columnList = "batch_id"),
                @Index(name = "ix_tx_slip", columnList = "issue_slip_id"),
                @Index(name = "ix_tx_service", columnList = "service_date,meal_type"),
                @Index(name = "ix_tx_trace", columnList = "menu_id,dish_id")
        })
public class InventoryTransaction extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TransactionType type;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    /** Có dấu: + nhập, - xuất/điều chỉnh giảm. */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal delta;

    @Column(nullable = false, length = 12)
    private String unit;

    /** D9/M06-D02 — ai THỰC HIỆN giao dịch, KHÔNG dùng tên reserved `created_by`/`actor_id`. */
    @Column(name = "performed_by_user_id", nullable = false)
    private UUID performedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 24)
    private ReasonCode reasonCode;

    @Column(name = "manager_signature", length = 180)
    private String managerSignature;

    @Column(name = "issue_slip_id")
    private UUID issueSlipId;

    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "dish_id")
    private UUID dishId;

    @Column(name = "dish_label", length = 180)
    private String dishLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", length = 12)
    private MealType mealType;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public UUID getIngredientId() { return ingredientId; }
    public void setIngredientId(UUID ingredientId) { this.ingredientId = ingredientId; }
    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal delta) { this.delta = delta; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public UUID getPerformedByUserId() { return performedByUserId; }
    public void setPerformedByUserId(UUID performedByUserId) { this.performedByUserId = performedByUserId; }
    public ReasonCode getReasonCode() { return reasonCode; }
    public void setReasonCode(ReasonCode reasonCode) { this.reasonCode = reasonCode; }
    public String getManagerSignature() { return managerSignature; }
    public void setManagerSignature(String managerSignature) { this.managerSignature = managerSignature; }
    public UUID getIssueSlipId() { return issueSlipId; }
    public void setIssueSlipId(UUID issueSlipId) { this.issueSlipId = issueSlipId; }
    public UUID getMenuId() { return menuId; }
    public void setMenuId(UUID menuId) { this.menuId = menuId; }
    public UUID getDishId() { return dishId; }
    public void setDishId(UUID dishId) { this.dishId = dishId; }
    public String getDishLabel() { return dishLabel; }
    public void setDishLabel(String dishLabel) { this.dishLabel = dishLabel; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
}
