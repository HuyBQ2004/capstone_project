package com.sfms.mf11.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * {@code mf11_attachments} — file bằng chứng dùng CHUNG cho mọi "chủ sở hữu" trong mf11
 * (SOURCE_REPORT, CORRECTIVE_ACTION), thay vì mỗi module tự có 1 bảng riêng như
 * {@code violation_files}/{@code incident_evidence} cũ. Cùng pattern {@code sample_attachments}
 * (mf08). D10-BIS — tài liệu RIÊNG TƯ: chỉ lưu {@code storageKey}, không có URL công khai.
 */
@Entity
@Table(name = "mf11_attachments",
        indexes = @Index(name = "ix_mf11_att_owner", columnList = "owner_type,owner_id"))
public class Mf11Attachment extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 24)
    private Mf11AttachmentOwner ownerType;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    public Mf11AttachmentOwner getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(Mf11AttachmentOwner ownerType) {
        this.ownerType = ownerType;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
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
