package com.sfms.iam.repository;

import com.sfms.iam.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** BR-003 rate-limit — bản ghi gần nhất của user để tính cửa sổ 15 phút. */
    Optional<PasswordResetToken> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PasswordResetToken t set t.usedAt = :now, t.updatedAt = :now "
         + "where t.userId = :uid and t.usedAt is null")
    int invalidateAllForUser(@Param("uid") UUID userId, @Param("now") Instant now);
}
