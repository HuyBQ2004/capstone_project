package com.sfms.iam.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token opaque — BR-004 v1.9 (sliding 14 ngày) + BR-005 (single session).
 *
 * <p><b>Chỉ lưu hash.</b> Chuỗi thô chỉ tồn tại trong response trả về client; DB không có
 * cột nào chứa nó (MF01-TC-009: {@code SELECT count(*) WHERE token_hash = :rawToken} ⇒ 0).
 *
 * <p>Bất biến V1: {@code UNIQUE(user_id) WHERE revoked_at IS NULL} — tối đa 1 token còn
 * hiệu lực mỗi user. "Quên revoke trước khi insert" trở thành lỗi ngay ở tầng DB thay vì
 * âm thầm cho 2 thiết bị dùng song song.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** SHA-256 (Base64) của token thô. */
    @Column(name = "token_hash", nullable = false, length = 88, unique = true)
    private String tokenHash;

    /** Sliding: mỗi lần rotate thành công ⇒ {@code now + 14 ngày} (KHÔNG phải hạn cũ + 14). */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public boolean isUsable(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
