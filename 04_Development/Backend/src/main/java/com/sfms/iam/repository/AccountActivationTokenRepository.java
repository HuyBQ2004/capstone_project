package com.sfms.iam.repository;

import com.sfms.iam.domain.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, UUID> {

    /** Tra bằng <b>hash</b> của token thô — DB không bao giờ chứa chuỗi thô. */
    Optional<AccountActivationToken> findByTokenHash(String tokenHash);

    /**
     * UC-154 AF1 — {@code resend} phải <b>vô hiệu token cũ</b> rồi cấp link MỚI, không
     * gia hạn tại chỗ. MF01-TC-020: gia hạn tại chỗ mâu thuẫn câu chữ <i>"gửi lại link mới"</i>
     * và để token cũ sống tiếp.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update AccountActivationToken t set t.usedAt = :now, t.updatedAt = :now "
         + "where t.userId = :uid and t.usedAt is null")
    int invalidateAllForUser(@Param("uid") UUID userId, @Param("now") Instant now);
}
