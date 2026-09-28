package com.sfms.iam.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Token kích hoạt tài khoản — BR-011 (48 giờ, gửi lại thì +48h), dùng <b>1 lần</b>.
 *
 * <p>Lưu hash; token thô chỉ nằm trong email. MF01-TC-019: dùng lại token đã kích hoạt
 * phải bị từ chối — nếu không, ai đọc được link trong hộp thư (hoặc trong log/Referer)
 * đổi được mật khẩu của tài khoản đã kích hoạt.
 */
@Entity
@Table(name = "account_activation_tokens")
public class AccountActivationToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 88, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    /**
     * BR-011 <i>"chỉ có hiệu lực trong 48 giờ"</i> ⇒ <b>đúng mốc vẫn hợp lệ</b>
     * ({@code now <= expiresAt}). MF01-TC-018 nhánh (b) assert điều này: so sánh {@code <}
     * sẽ chặn oan người bấm link đúng hạn.
     */
    public boolean isUsable(Instant now) {
        return usedAt == null && !now.isAfter(expiresAt);
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

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }
}
