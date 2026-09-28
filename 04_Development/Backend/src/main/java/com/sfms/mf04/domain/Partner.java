package com.sfms.mf04.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.PartnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Hồ sơ công ty đối tác (UC-071/067 đăng ký, UC-007/004 duyệt). Bảng {@code partners}
 * ĐÃ TỒN TẠI từ {@code V1__init_schema.sql} (stub cho FK {@code user_accounts.partner_id})
 * — migration MF04 ({@code V6}) chỉ {@code ALTER}, không {@code CREATE} lại (M04-D14).
 * Index/CHECK thật nằm ở migration SQL ({@code spring.jpa.hibernate.ddl-auto=validate}).
 *
 * <p>{@code certExpiry}/{@code certStatus} KHÔNG có ở đây — dẫn xuất runtime từ
 * {@link PartnerDocument} (ADR-M04-4), tính ở tầng service/mapper.
 */
@Entity
@Table(name = "partners")
public class Partner extends BaseEntity {

    /** D1 — mã nghiệp vụ công khai (SUP-001 / CAT-001). UNIQUE toàn cục (stub V1). */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    /** Cột stub V1 tên {@code partner_type} — KHÔNG đổi thành {@code type} (phá ck_partner_type). */
    @Enumerated(EnumType.STRING)
    @Column(name = "partner_type", nullable = false, length = 32)
    private PartnerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PartnerStatus status = PartnerStatus.PENDING;

    @Column(name = "tax_id", nullable = false, unique = true, length = 20)
    private String taxId;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 180)
    private String contact;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 1000)
    private String description;

    /** CF-M04-4 — dữ liệu ĐẦU VÀO do nghiệp vụ ghi nhận, KHÔNG tự tính công thức. */
    @Column(name = "performance_score", precision = 5, scale = 2)
    private BigDecimal performanceScore;

    /** UC-108 — chỉ có ý nghĩa với {@code SERVICE_CATERER} (ck_partner_site_score). */
    @Column(name = "site_inspection_score", precision = 5, scale = 2)
    private BigDecimal siteInspectionScore;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt = Instant.now();

    /** BR-048 — bắt buộc khi {@code status = REJECTED} (ck_partner_reject_reason). */
    @Column(name = "reject_reason", length = 1000)
    private String rejectReason;

    /** ADR-M04-6 — bắt buộc khi {@code status = SUSPENDED} (ck_partner_suspend_reason). */
    @Column(name = "suspend_reason", length = 1000)
    private String suspendReason;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartnerType getType() {
        return type;
    }

    public void setType(PartnerType type) {
        this.type = type;
    }

    public PartnerStatus getStatus() {
        return status;
    }

    public void setStatus(PartnerStatus status) {
        this.status = status;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(BigDecimal performanceScore) {
        this.performanceScore = performanceScore;
    }

    public BigDecimal getSiteInspectionScore() {
        return siteInspectionScore;
    }

    public void setSiteInspectionScore(BigDecimal siteInspectionScore) {
        this.siteInspectionScore = siteInspectionScore;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getSuspendReason() {
        return suspendReason;
    }

    public void setSuspendReason(String suspendReason) {
        this.suspendReason = suspendReason;
    }
}
