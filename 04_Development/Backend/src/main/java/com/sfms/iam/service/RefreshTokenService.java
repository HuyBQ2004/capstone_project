package com.sfms.iam.service;

import com.sfms.iam.domain.RefreshToken;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.AuthDtos.TokenPairResponse;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.repository.RefreshTokenRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Vòng đời refresh token — BR-004 v1.9 (sliding 14 ngày) + BR-005 (single session).
 *
 * <p>Hai bất biến quyết định độ an toàn của cả hệ thống:
 * <ol>
 *   <li><b>Sliding đúng nghĩa</b>: hạn mới = <b>thời điểm gia hạn + 14 ngày</b>, KHÔNG phải
 *       hạn cũ + 14 ngày. Sai chỗ này thì token sống vô hạn tiệm tiến (MF01-TC-009).</li>
 *   <li><b>Rotate = thay thế</b>: token cũ bị revoke ngay trong cùng transaction. Không revoke
 *       ⇒ token bị đánh cắp vẫn dùng song song tới 14 ngày (MF01-TC-SEC-009).</li>
 * </ol>
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountRepository userRepository;
    private final SecurityPolicyProvider policyProvider;
    private final TokenHasher tokenHasher;
    private final JwtService jwtService;
    private final Clock clock;
    private final Duration slidingWindow;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserAccountRepository userRepository,
                               SecurityPolicyProvider policyProvider,
                               TokenHasher tokenHasher,
                               JwtService jwtService,
                               Clock clock,
                               @Value("${sfms.refresh-token.sliding-days:14}") int slidingDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.policyProvider = policyProvider;
        this.tokenHasher = tokenHasher;
        this.jwtService = jwtService;
        this.clock = clock;
        this.slidingWindow = Duration.ofDays(slidingDays);
    }

    /**
     * BR-005 — cấp phiên mới, <b>revoke toàn bộ phiên cũ TRƯỚC</b>.
     *
     * <p>Thứ tự là bắt buộc: gọi {@code revokeAllForUser} sau khi insert sẽ revoke luôn token
     * vừa cấp ⇒ người dùng bị đăng xuất ngay khi vừa login. MF01-TC-010 dùng {@code InOrder}
     * để bắt đúng lỗi này.
     *
     * @return chuỗi refresh token <b>thô</b> (chỉ tồn tại ở đây và trong response)
     */
    @Transactional
    public String issueForNewSession(UserAccount user) {
        Instant now = clock.instant();
        refreshTokenRepository.revokeAllForUser(user.getId(), now);   // ① trước

        String raw = tokenHasher.generateRawToken();
        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setTokenHash(tokenHasher.hash(raw));
        token.setExpiresAt(now.plus(slidingWindow));
        refreshTokenRepository.saveAndFlush(token);                    // ② sau
        return raw;
    }

    /**
     * BR-004 v1.9 — đổi refresh token còn hạn lấy access token mới và <b>trượt hạn</b>.
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-008} (401, MSG-004) khi token
     *         không tồn tại, đã revoke, đã rotate, hoặc đã hết hạn
     */
    @Transactional
    public TokenPairResponse rotate(String rawRefreshToken) {
        Instant now = clock.instant();
        String hash = tokenHasher.hash(rawRefreshToken);

        RefreshToken existing = refreshTokenRepository
                .findByTokenHashAndRevokedAtIsNull(hash)
                .orElseThrow(IamExceptions::tokenInvalid);

        if (!existing.isUsable(now)) {
            throw IamExceptions.tokenInvalid();
        }

        UserAccount user = userRepository.findById(existing.getUserId())
                .orElseThrow(IamExceptions::tokenInvalid);

        // BR-004: tài khoản bị vô hiệu hóa / đình chỉ trong lúc phiên còn sống ⇒ chặn ngay,
        // không chờ refresh token hết hạn (MF01-TC-INT-006 bước 3).
        if (!user.canLogin()) {
            refreshTokenRepository.revokeAllForUser(user.getId(), now);
            throw IamExceptions.tokenInvalid();
        }
        // BR-002: đang trong thời gian khóa thì refresh cũng phải chết
        // (MF01-TC-SEC-009 nhánh c — nếu không, lockout hoàn toàn vô nghĩa).
        if (user.isLocked(now)) {
            refreshTokenRepository.revokeAllForUser(user.getId(), now);
            throw IamExceptions.tokenInvalid();
        }

        // Rotate = revoke bản ghi cũ + tạo bản ghi mới với hạn trượt từ THỜI ĐIỂM NÀY.
        existing.setRevokedAt(now);
        refreshTokenRepository.saveAndFlush(existing);

        String newRaw = tokenHasher.generateRawToken();
        RefreshToken rotated = new RefreshToken();
        rotated.setUserId(user.getId());
        rotated.setTokenHash(tokenHasher.hash(newRaw));
        rotated.setExpiresAt(now.plus(slidingWindow));   // ⬅ now + 14d, KHÔNG phải hạn cũ + 14d
        refreshTokenRepository.saveAndFlush(rotated);

        int ttl = policyProvider.current().getSessionTimeoutMinutes();
        return new TokenPairResponse(jwtService.issueAccessToken(user, ttl), newRaw);
    }

    /**
     * BR-004 — revoke mọi phiên của user. Gọi từ: logout · lockout · deactivate ·
     * đổi/đặt lại MK · cascade đình chỉ đối tác.
     *
     * @return số token bị revoke ({@code UserAccountDeactivated.revokedRefreshTokens})
     */
    @Transactional
    public int revokeAll(UUID userId) {
        return refreshTokenRepository.revokeAllForUser(userId, clock.instant());
    }

    /** Bất biến TDS §14.1 — dùng cho assertion và cho kiểm tra sức khỏe. */
    @Transactional(readOnly = true)
    public long countUsableSessions(UUID userId) {
        return refreshTokenRepository.countUsable(userId, clock.instant());
    }
}
