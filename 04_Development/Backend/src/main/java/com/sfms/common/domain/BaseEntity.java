package com.sfms.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base entity dùng chung cho 13 module.
 *
 * <p><b>D1</b> — {@code id} ở đây là <b>UUID PK nội bộ, KHÔNG lộ ra API</b>. Mỗi entity
 * nghiệp vụ tự khai thêm cột {@code code VARCHAR(32)} + {@code UNIQUE(school_id, code)}
 * và expose nó qua {@code @JsonProperty("id")}. Path param cũng dùng {@code code}.
 *
 * <p><b>D4</b> — mốc thời gian là {@link Instant} ⇒ Jackson serialize ISO-8601.
 * Tuyệt đối KHÔNG {@code @JsonFormat("dd/MM/yyyy")}.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Người tạo bản ghi — tự động điền qua {@code AuditorAware} (xem
     * {@code iam/security/AuditorAwareConfig}), entity con KHÔNG cần khai báo/gọi gì thêm.
     * Bản ghi seed bằng SQL thô (không qua JPA) sẽ để {@code NULL}.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 180)
    private String createdBy;

    /** Người sửa gần nhất — cùng cơ chế với {@link #createdBy}. */
    @LastModifiedBy
    @Column(name = "updated_by", length = 180)
    private String updatedBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
