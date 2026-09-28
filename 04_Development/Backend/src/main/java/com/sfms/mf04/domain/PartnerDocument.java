package com.sfms.mf04.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tài liệu tuân thủ đối tác — hợp nhất {@code PARTNERS[].documents} + {@code SUPPLIER_DOCUMENTS}
 * của mock (CF-M04-5). {@code status} KHÔNG có ở đây — dẫn xuất runtime theo BR-047
 * (ADR-M04-4, xem {@link com.sfms.mf04.service.DocumentStatusCalculator}).
 *
 * <p>{@code fileKey} là <b>storageKey riêng tư</b> (D10-BIS mục 2) — KHÔNG phải URL công khai
 * mà {@code StorageService.store()} trả về. Tải xuống qua endpoint có {@code @PreAuthorize}.
 */
@Entity
@Table(name = "partner_documents")
public class PartnerDocument extends BaseEntity {

    /** D1 — mã nghiệp vụ (DOC-GF-01). */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    /** CF-M04-6 — khóa cách ly đa-tenant. JSON trả {@code code} của partner đích (D1). */
    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(nullable = false, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 24)
    private DocumentType documentType;

    @Column(nullable = false, length = 60)
    private String number;

    @Column(name = "issued_by", nullable = false, length = 180)
    private String issuedBy;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    /** {@code null} = "Không thời hạn" (L4/E5, ADR-M04-4). */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** D10-BIS — storageKey riêng tư, giá trị truyền vào {@code StorageService.store(key, file)}. */
    @Column(name = "file_key", nullable = false, length = 255)
    private String fileKey;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "content_type", nullable = false, length = 60)
    private String contentType;

    /** UC-091 — bản mới thay thế bản cũ; bản cũ KHÔNG bị xóa (§4.2, chứng cứ thanh tra ATTP). */
    @Column(name = "superseded_by_document_id")
    private UUID supersededByDocumentId;

    /** PROPOSED — hồ sơ CÁ NHÂN của một nhân viên giao hàng (giấy khám sức khoẻ, xác nhận
     *  kiến thức ATTP). {@code null} = tài liệu cấp công ty (hành vi hiện có). Chứng từ theo
     *  LÔ HÀNG dùng cơ chế khác đã có sẵn — xem {@code batch_certificates} (UC-100/BR-071). */
    @Column(name = "delivery_person_id")
    private UUID deliveryPersonId;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public UUID getSupersededByDocumentId() {
        return supersededByDocumentId;
    }

    public void setSupersededByDocumentId(UUID supersededByDocumentId) {
        this.supersededByDocumentId = supersededByDocumentId;
    }

    public UUID getDeliveryPersonId() {
        return deliveryPersonId;
    }

    public void setDeliveryPersonId(UUID deliveryPersonId) {
        this.deliveryPersonId = deliveryPersonId;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
