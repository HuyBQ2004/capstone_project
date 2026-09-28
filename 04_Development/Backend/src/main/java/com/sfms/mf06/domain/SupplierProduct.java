package com.sfms.mf06.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Mặt hàng NCC chào bán (M06-D03 — chưa từng tồn tại, MF06 tự tạo). */
@Entity
@Table(name = "supplier_products",
        uniqueConstraints = @UniqueConstraint(name = "uq_sp_partner_code", columnNames = {"partner_id", "code"}))
public class SupplierProduct extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    /** Tên NCC dùng trên hoá đơn/bao bì — có thể khác tên danh mục chuẩn. */
    @Column(length = 120)
    private String label;

    /**
     * Đơn vị báo giá của DÒNG GIÁ này (đóng vai trò "purchaseUnit") — có thể khác
     * `catalog_ingredients.unit` (baseUnit).
     */
    @Column(nullable = false, length = 20)
    private String unit;

    /**
     * PROPOSED — xem plan "Đơn vị đo lường". Bao nhiêu `catalog_ingredients.unit`
     * (baseUnit) = 1 {@link #unit} (purchaseUnit) CỦA ĐÚNG NCC NÀY — quy đổi này KHÔNG
     * phải hằng số chung cho nguyên liệu (mỗi NCC đóng gói khác nhau, VD "1 bó" của NCC A
     * và NCC B có thể nặng khác nhau), nên sống ở đây chứ không ở `CatalogIngredient`.
     * Mặc định 1 = trùng baseUnit (không cần khai gì thêm).
     */
    @Column(name = "conversion_to_base", nullable = false, precision = 14, scale = 6)
    private BigDecimal conversionToBase = BigDecimal.ONE;

    /** Ghi chú cách quy đổi nếu NCC thấy cần (VD "trung bình, có thể lệch theo mùa vụ"). */
    @Column(name = "conversion_note", length = 255)
    private String conversionNote;

    @Column(name = "unit_price", precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 120)
    private String packaging;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(nullable = false)
    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getPartnerId() { return partnerId; }
    public void setPartnerId(UUID partnerId) { this.partnerId = partnerId; }
    public UUID getIngredientId() { return ingredientId; }
    public void setIngredientId(UUID ingredientId) { this.ingredientId = ingredientId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getConversionToBase() { return conversionToBase; }
    public void setConversionToBase(BigDecimal conversionToBase) { this.conversionToBase = conversionToBase; }
    public String getConversionNote() { return conversionNote; }
    public void setConversionNote(String conversionNote) { this.conversionNote = conversionNote; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getPackaging() { return packaging; }
    public void setPackaging(String packaging) { this.packaging = packaging; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
