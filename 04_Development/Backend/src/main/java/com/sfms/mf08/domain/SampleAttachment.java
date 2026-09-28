package com.sfms.mf08.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * {@code sample_attachments} — file bằng chứng BR-107/BR-212 (ADR-M08-4). Không có {@code code}:
 * attachment KHÔNG lộ qua path param nào (D1 chỉ đòi {@code code} cho resource có path param).
 *
 * <p>D10-BIS — JSON trả {@code id} tài liệu, TUYỆT ĐỐI không trả {@code storageKey}.
 * {@code uploadedAt}/{@code uploadedBy} của TDS v1.0 trùng nghĩa {@code created_at}/
 * {@code created_by} của {@link BaseEntity} (M08-D02) — DTO map từ đó, không khai cột riêng.
 */
@Entity
@Table(name = "sample_attachments",
        indexes = @Index(name = "ix_att_owner", columnList = "owner_type,owner_id,kind"))
public class SampleAttachment extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 16)
    private AttachmentOwner ownerType;   // SAMPLE | LAB_REQUEST

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AttachmentKind kind;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    // ⛔ D9 / M08-D02 — KHÔNG khai createdAt/updatedAt/createdBy/updatedBy ở đây (BaseEntity).
    // uploadedAt <- getCreatedAt(); uploadedBy <- getCreatedBy() ở tầng DTO mapper.

    public AttachmentOwner getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(AttachmentOwner ownerType) {
        this.ownerType = ownerType;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public AttachmentKind getKind() {
        return kind;
    }

    public void setKind(AttachmentKind kind) {
        this.kind = kind;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public void setChecksumSha256(String checksumSha256) {
        this.checksumSha256 = checksumSha256;
    }
}
