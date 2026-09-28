package com.sfms.mf11.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/** {@code violation_files} — ảnh bằng chứng / bản ký (BR-211). TDS-MF11 §5.2, D10. */
@Entity
@Table(name = "violation_files",
        uniqueConstraints = @UniqueConstraint(name = "uq_vfile_code", columnNames = {"violation_id", "code"}),
        indexes = @Index(name = "ix_vfile_violation", columnList = "violation_id,kind"))
public class ViolationFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** D1 — mã nghiệp vụ dùng trong path {@code DELETE .../signed-record/{fileId}}. */
    @JsonProperty("id")
    @Column(nullable = false, length = 24)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "violation_id", nullable = false)
    private ViolationRecord violation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ViolationFileKind kind;

    @Column(nullable = false, length = 200)
    private String name;

    /** D10 — key trả về từ {@code StorageService.store()}, KHÔNG phải URL công khai. */
    @Column(name = "storage_key", nullable = false, length = 400)
    private String storageKey;

    @Column(name = "size_label", length = 20)
    private String sizeLabel;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ViolationRecord getViolation() {
        return violation;
    }

    public void setViolation(ViolationRecord violation) {
        this.violation = violation;
    }

    public ViolationFileKind getKind() {
        return kind;
    }

    public void setKind(ViolationFileKind kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getSizeLabel() {
        return sizeLabel;
    }

    public void setSizeLabel(String sizeLabel) {
        this.sizeLabel = sizeLabel;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
