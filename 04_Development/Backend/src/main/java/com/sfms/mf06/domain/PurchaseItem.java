package com.sfms.mf06.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** Dòng hàng của {@link PurchaseRequest}. {@code lineTotal} dẫn xuất — không cột. */
@Entity
@Table(name = "purchase_items")
public class PurchaseItem extends BaseEntity {

    @Column(name = "purchase_request_id", nullable = false)
    private UUID purchaseRequestId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "ingredient_label", nullable = false, length = 180)
    private String ingredientLabel;

    /** Số lượng ĐÃ QUY ĐỔI theo quy cách đóng gói của hợp đồng — số lượng thực đặt (PROPOSED,
     *  xem Proposal-Partnership-Catalog-Contract.md §Phase 2). Bằng {@code requestedQuantity}
     *  khi không có thông tin đóng gói hoặc sản phẩm cho phép tách lẻ. */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    /** Số lượng trường THỰC SỰ CẦN trước khi quy đổi — {@code null} ở dữ liệu tạo trước khi
     *  có tính năng này. PROPOSED. */
    @Column(name = "requested_quantity", precision = 12, scale = 3)
    private BigDecimal requestedQuantity;

    @Column(nullable = false, length = 12)
    private String unit;

    @Column(name = "unit_price", precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 255)
    private String note;

    /** BR-071 — lô NCC gán cho dòng này lúc confirm-schedule; NULL trước đó. */
    @Column(name = "supplier_batch_id")
    private UUID supplierBatchId;

    public UUID getPurchaseRequestId() { return purchaseRequestId; }
    public void setPurchaseRequestId(UUID purchaseRequestId) { this.purchaseRequestId = purchaseRequestId; }
    public UUID getIngredientId() { return ingredientId; }
    public void setIngredientId(UUID ingredientId) { this.ingredientId = ingredientId; }
    public String getIngredientLabel() { return ingredientLabel; }
    public void setIngredientLabel(String ingredientLabel) { this.ingredientLabel = ingredientLabel; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(BigDecimal requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public UUID getSupplierBatchId() { return supplierBatchId; }
    public void setSupplierBatchId(UUID supplierBatchId) { this.supplierBatchId = supplierBatchId; }

    /** Dẫn xuất — {@code quantity * unitPrice}, KHÔNG cột (ADR-M02-2). */
    public BigDecimal lineTotal() {
        return unitPrice == null ? null : quantity.multiply(unitPrice);
    }
}
