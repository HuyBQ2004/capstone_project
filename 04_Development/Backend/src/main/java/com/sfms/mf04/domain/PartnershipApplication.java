package com.sfms.mf04.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Đơn hợp tác của một NCC với MỘT trường cụ thể (ADR-M04-2). Tách khỏi {@link Partner}
 * (hồ sơ công ty, chỉ một bản) vì một NCC hợp tác với nhiều trường, mỗi trường duyệt độc lập.
 */
@Entity
@Table(name = "partnership_applications")
public class PartnershipApplication extends BaseEntity {

    /** D1 — mã nghiệp vụ (APP-2026-04). */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    /** CF-M04-9 — khóa cách ly đa-tenant. */
    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    /** CF-M04-9 — trường nhận đơn; UNIQUE(partner_id, school_id) khi đang mở (xem migration). */
    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "company_name", nullable = false, length = 180)
    private String companyName;

    @Column(name = "tax_id", nullable = false, length = 20)
    private String taxId;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /** BR-048 — phản hồi của School Admin khi từ chối/yêu cầu bổ sung. */
    @Column(length = 1000)
    private String comment;

    /** Field NGHIỆP VỤ "ai duyệt" — khác {@code updated_by} reserved của BaseEntity (D9). */
    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
