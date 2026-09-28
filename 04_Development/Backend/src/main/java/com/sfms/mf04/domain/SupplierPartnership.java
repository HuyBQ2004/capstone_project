package com.sfms.mf04.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Quan hệ hợp tác ĐANG hiệu lực partner × school — sinh ra khi {@link PartnershipApplication}
 * được duyệt (ADR-M04-2). {@code docStatus} KHÔNG có ở đây — dẫn xuất runtime (ADR-M04-4).
 */
@Entity
@Table(name = "supplier_partnerships")
public class SupplierPartnership extends BaseEntity {

    /** D1 — mã nghiệp vụ (PTN-01). */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    /** CF-M04-6 — khóa cách ly đa-tenant. */
    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    /** JSON: {@code startDate} (M04-D04). */
    @Column
    private LocalDate since;

    /** JSON: {@code endDate}; {@code null} = còn hiệu lực (M04-D04, cột thêm 2026-08-02). */
    @Column
    private LocalDate until;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PartnershipStatus status = PartnershipStatus.PENDING;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(UUID partnerId) {
        this.partnerId = partnerId;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDate getSince() {
        return since;
    }

    public void setSince(LocalDate since) {
        this.since = since;
    }

    public LocalDate getUntil() {
        return until;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }

    public PartnershipStatus getStatus() {
        return status;
    }

    public void setStatus(PartnershipStatus status) {
        this.status = status;
    }
}
