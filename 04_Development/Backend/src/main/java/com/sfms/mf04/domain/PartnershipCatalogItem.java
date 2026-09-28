package com.sfms.mf04.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Một dòng sản phẩm-giá ĐÃ ĐƯỢC TRƯỜNG DUYỆT cho một hợp đồng hợp tác cụ thể
 * ({@link SupplierPartnership}) — khác {@code supplier_products} (mf06), vốn là danh mục
 * toàn cục của NCC dùng chung cho mọi trường.
 *
 * <p><b>PROPOSED — chưa có BR/UC chính thức.</b> Xem {@code 07_implement/
 * MF04_Supplier_Partner_Management/Proposal-Partnership-Catalog-Contract.md}. Sinh ra khi
 * một {@link CatalogChangeRequest} được duyệt.
 */
@Entity
@Table(name = "partnership_catalog_items")
public class PartnershipCatalogItem extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "partnership_id", nullable = false)
    private UUID partnershipId;

    /** Denormalized từ {@link SupplierPartnership} — cùng khuôn CF-M04-6 (lọc tenant trực tiếp). */
    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    /** Đơn vị đã duyệt cho hợp đồng này (đóng vai trò "purchaseUnit") — có thể khác đơn vị
     *  chuẩn của nguyên liệu ({@code catalog_ingredients.unit}, "baseUnit"). */
    @Column(nullable = false, length = 20)
    private String unit;

    /**
     * PROPOSED — xem plan "Đơn vị đo lường". Bao nhiêu baseUnit = 1 {@link #unit} CỦA
     * ĐÚNG HỢP ĐỒNG NÀY — quy đổi riêng theo cách NCC này đóng gói, KHÔNG phải hằng số
     * chung cho nguyên liệu. Mặc định 1 = trùng baseUnit.
     */
    @Column(name = "conversion_to_base", nullable = false, precision = 14, scale = 6)
    private BigDecimal conversionToBase = BigDecimal.ONE;

    @Column(name = "conversion_note", length = 255)
    private String conversionNote;

    @Column(name = "approved_unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal approvedUnitPrice;

    @Column(length = 120)
    private String packaging;

    /** Số lượng/1 kiện theo {@code unit} (VD 50 = bao 50kg). {@code null} = không quy đổi. */
    @Column(name = "packaging_size", precision = 12, scale = 3)
    private BigDecimal packagingSize;

    /** {@code false} = chỉ được đặt bội số {@code packagingSize} (bắt buộc nguyên kiện). */
    @Column(name = "allow_partial_package", nullable = false)
    private boolean allowPartialPackage = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CatalogItemStatus status = CatalogItemStatus.ACTIVE;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getPartnershipId() { return partnershipId; }
    public void setPartnershipId(UUID partnershipId) { this.partnershipId = partnershipId; }
    public UUID getPartnerId() { return partnerId; }
    public void setPartnerId(UUID partnerId) { this.partnerId = partnerId; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public UUID getIngredientId() { return ingredientId; }
    public void setIngredientId(UUID ingredientId) { this.ingredientId = ingredientId; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getConversionToBase() { return conversionToBase; }
    public void setConversionToBase(BigDecimal conversionToBase) { this.conversionToBase = conversionToBase; }
    public String getConversionNote() { return conversionNote; }
    public void setConversionNote(String conversionNote) { this.conversionNote = conversionNote; }
    public BigDecimal getApprovedUnitPrice() { return approvedUnitPrice; }
    public void setApprovedUnitPrice(BigDecimal approvedUnitPrice) { this.approvedUnitPrice = approvedUnitPrice; }
    public String getPackaging() { return packaging; }
    public void setPackaging(String packaging) { this.packaging = packaging; }
    public BigDecimal getPackagingSize() { return packagingSize; }
    public void setPackagingSize(BigDecimal packagingSize) { this.packagingSize = packagingSize; }
    public boolean isAllowPartialPackage() { return allowPartialPackage; }
    public void setAllowPartialPackage(boolean allowPartialPackage) { this.allowPartialPackage = allowPartialPackage; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public CatalogItemStatus getStatus() { return status; }
    public void setStatus(CatalogItemStatus status) { this.status = status; }
}
