package com.sfms.mf06.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** UC-021/UC-139/UC-015 — vòng đời duyệt giá 1 vòng (CF-M06-5). D1: {@code code} lộ ra JSON. */
@Entity
@Table(name = "purchase_requests",
        uniqueConstraints = @UniqueConstraint(name = "uq_pr_school_code", columnNames = {"school_id", "code"}))
public class PurchaseRequest extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "requested_by_user_id")
    private UUID requestedByUserId;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "total_value", precision = 14, scale = 2)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseRequestStatus status = PurchaseRequestStatus.PENDING;

    @Column(name = "source_menu_id")
    private UUID sourceMenuId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_meal_type", length = 12)
    private MealType sourceMealType;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "quoted_at") private Instant quotedAt;
    @Column(name = "quote_note", length = 500) private String quoteNote;
    @Column(name = "price_accepted_at") private Instant priceAcceptedAt;
    @Column(name = "accepted_at") private Instant acceptedAt;
    @Column(name = "confirmed_delivery_at") private Instant confirmedDeliveryAt;
    @Column(name = "rejected_at") private Instant rejectedAt;
    @Column(name = "cancelled_at") private Instant cancelledAt;
    @Column(name = "cancel_reason", length = 500) private String cancelReason;

    // items -> quan hệ 1-n quản lý qua PurchaseItemRepository (KHÔNG @OneToMany: purchase_items
    // giữ FK thô UUID, không @ManyToOne ngược lại — tránh nạp N+1 không cần thiết ở mọi read).

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public UUID getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(UUID requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    public PurchaseRequestStatus getStatus() { return status; }
    public void setStatus(PurchaseRequestStatus status) { this.status = status; }
    public UUID getSourceMenuId() { return sourceMenuId; }
    public void setSourceMenuId(UUID sourceMenuId) { this.sourceMenuId = sourceMenuId; }
    public MealType getSourceMealType() { return sourceMealType; }
    public void setSourceMealType(MealType sourceMealType) { this.sourceMealType = sourceMealType; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public Instant getQuotedAt() { return quotedAt; }
    public void setQuotedAt(Instant quotedAt) { this.quotedAt = quotedAt; }
    public String getQuoteNote() { return quoteNote; }
    public void setQuoteNote(String quoteNote) { this.quoteNote = quoteNote; }
    public Instant getPriceAcceptedAt() { return priceAcceptedAt; }
    public void setPriceAcceptedAt(Instant priceAcceptedAt) { this.priceAcceptedAt = priceAcceptedAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }
    public Instant getConfirmedDeliveryAt() { return confirmedDeliveryAt; }
    public void setConfirmedDeliveryAt(Instant confirmedDeliveryAt) { this.confirmedDeliveryAt = confirmedDeliveryAt; }
    public Instant getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
}
