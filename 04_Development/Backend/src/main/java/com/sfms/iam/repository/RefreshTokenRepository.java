package com.sfms.iam.repository;

import com.sfms.iam.domain.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** TDS §8.2. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Chỉ tra token <b>chưa revoke</b>. Đây là mấu chốt chống replay: sau khi rotate,
     * bản ghi cũ có {@code revoked_at != null} nên truy vấn này không tìm thấy nữa
     * (MF01-TC-SEC-009).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * BR-004/BR-005 — revoke toàn bộ phiên của user. Gọi khi:
     * logout · lockout (BR-002) · deactivate (UC-002/003) · login thiết bị khác (BR-005)
     * · đổi MK (UC-011) · reset MK (UC-032) · cascade đình chỉ đối tác.
     *
     * <p>⚠️ Phải gọi <b>TRƯỚC</b> khi insert token mới. Gọi sau sẽ revoke luôn token vừa cấp
     * ⇒ người dùng bị đăng xuất ngay khi vừa login (MF01-TC-010 dùng {@code InOrder} để bắt).
     *
     * @return số bản ghi bị revoke — dùng cho {@code UserAccountDeactivated.revokedRefreshTokens}
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken t set t.revokedAt = :now, t.updatedAt = :now "
         + "where t.userId = :uid and t.revokedAt is null")
    int revokeAllForUser(@Param("uid") UUID userId, @Param("now") Instant now);

    @Query("select t from RefreshToken t where t.userId = :uid and t.revokedAt is null")
    List<RefreshToken> findActiveByUser(@Param("uid") UUID userId);

    @Query("select count(t) from RefreshToken t "
         + "where t.userId = :uid and t.revokedAt is null and t.expiresAt > :now")
    long countUsable(@Param("uid") UUID userId, @Param("now") Instant now);
}
