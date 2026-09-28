package com.sfms.mf12.domain;

import com.sfms.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;

/** {@code backups} (V35) — BR-160/161: lịch sử sao lưu CSDL. D1: {@code code} lộ ra JSON dưới tên "id". */
@Entity
@Table(name = "backups")
public class Backup extends BaseEntity {

    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BackupStatus status;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** Storage KEY (không phải path đĩa) — {@code StorageService.load(key)}, file đã mã hoá AES-256-GCM. */
    @JsonIgnore
    @Column(name = "file_path", length = 300)
    private String filePath;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

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

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public BackupStatus getStatus() {
        return status;
    }

    public void setStatus(BackupStatus status) {
        this.status = status;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
