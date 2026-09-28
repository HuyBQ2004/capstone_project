package com.sfms.mf06.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** BR-071 — lô nguyên liệu phía NCC, đăng ký trước khi giao. */
@Entity
@Table(name = "supplier_batches",
        uniqueConstraints = @UniqueConstraint(name = "uq_sb_partner_code", columnNames = {"partner_id", "code"}))
public class SupplierBatch extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "lot_number", nullable = false, length = 60)
    private String lotNumber;

    @Column(name = "manufacturing_date", nullable = false)
    private LocalDate manufacturingDate;

    @Column(name = "packaging_date", nullable = false)
    private LocalDate packagingDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, length = 12)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SupplierBatchStatus status = SupplierBatchStatus.ACTIVE;

    /** BR-071 (10/08/2026) — số lượng CÒN LẠI của lô; giảm dần khi được nhận nhiều đợt/nhiều trường. */
    @Column(name = "remaining_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal remainingQuantity = BigDecimal.ZERO;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getPartnerId() { return partnerId; }
    public void setPartnerId(UUID partnerId) { this.partnerId = partnerId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }
    public LocalDate getPackagingDate() { return packagingDate; }
    public void setPackagingDate(LocalDate packagingDate) { this.packagingDate = packagingDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public SupplierBatchStatus getStatus() { return status; }
    public void setStatus(SupplierBatchStatus status) { this.status = status; }
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) { this.remainingQuantity = remainingQuantity; }
}
