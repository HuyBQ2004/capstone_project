package com.sfms.mf04.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Đề xuất của NCC để thêm/sửa một dòng {@link PartnershipCatalogItem} — chờ School
 * Administration duyệt/từ chối trước khi có hiệu lực. {@code catalogItemId = null} nghĩa là
 * đề xuất SẢN PHẨM MỚI (chưa từng có dòng danh mục nào cho ingredient này ở hợp đồng đó).
 *
 * <p><b>PROPOSED — chưa có BR/UC chính thức.</b> Xem {@code 07_implement/
 * MF04_Supplier_Partner_Management/Proposal-Partnership-Catalog-Contract.md}.
 */
@Entity
@Table(name = "catalog_change_requests")
public class CatalogChangeRequest extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "partnership_id", nullable = false)
    private UUID partnershipId;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** {@code null} = đề xuất sản phẩm mới, chưa có dòng danh mục nào để sửa. */
    @Column(name = "catalog_item_id")
    private UUID catalogItemId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "requested_unit", nullable = false, length = 20)
    private String requestedUnit;

    @Column(name = "requested_unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal requestedUnitPrice;

    /** PROPOSED — xem plan "Đơn vị đo lường". Bao nhiêu baseUnit = 1 {@link #requestedUnit}. */
    @Column(name = "requested_conversion_to_base", nullable = false, precision = 14, scale = 6)
    private BigDecimal requestedConversionToBase = BigDecimal.ONE;

    @Column(name = "requested_conversion_note", length = 255)
    private String requestedConversionNote;

    @Column(name = "requested_packaging", length = 120)
    private String requestedPackaging;

    @Column(name = "requested_packaging_size", precision = 12, scale = 3)
    private BigDecimal requestedPackagingSize;

    @Column(name = "requested_allow_partial_package", nullable = false)
    private boolean requestedAllowPartialPackage = true;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CatalogChangeStatus status = CatalogChangeStatus.PENDING;

    @Column(name = "decided_by_user_id")
    private UUID decidedByUserId;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

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
    public UUID getCatalogItemId() { return catalogItemId; }
    public void setCatalogItemId(UUID catalogItemId) { this.catalogItemId = catalogItemId; }
    public UUID getIngredientId() { return ingredientId; }
    public void setIngredientId(UUID ingredientId) { this.ingredientId = ingredientId; }
    public String getRequestedUnit() { return requestedUnit; }
    public void setRequestedUnit(String requestedUnit) { this.requestedUnit = requestedUnit; }
    public BigDecimal getRequestedUnitPrice() { return requestedUnitPrice; }
    public void setRequestedUnitPrice(BigDecimal requestedUnitPrice) { this.requestedUnitPrice = requestedUnitPrice; }
    public BigDecimal getRequestedConversionToBase() { return requestedConversionToBase; }
    public void setRequestedConversionToBase(BigDecimal requestedConversionToBase) { this.requestedConversionToBase = requestedConversionToBase; }
    public String getRequestedConversionNote() { return requestedConversionNote; }
    public void setRequestedConversionNote(String requestedConversionNote) { this.requestedConversionNote = requestedConversionNote; }
    public String getRequestedPackaging() { return requestedPackaging; }
    public void setRequestedPackaging(String requestedPackaging) { this.requestedPackaging = requestedPackaging; }
    public BigDecimal getRequestedPackagingSize() { return requestedPackagingSize; }
    public void setRequestedPackagingSize(BigDecimal requestedPackagingSize) { this.requestedPackagingSize = requestedPackagingSize; }
    public boolean isRequestedAllowPartialPackage() { return requestedAllowPartialPackage; }
    public void setRequestedAllowPartialPackage(boolean requestedAllowPartialPackage) { this.requestedAllowPartialPackage = requestedAllowPartialPackage; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public CatalogChangeStatus getStatus() { return status; }
    public void setStatus(CatalogChangeStatus status) { this.status = status; }
    public UUID getDecidedByUserId() { return decidedByUserId; }
    public void setDecidedByUserId(UUID decidedByUserId) { this.decidedByUserId = decidedByUserId; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
