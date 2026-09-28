package com.sfms.iam.service;

import com.sfms.iam.domain.PasswordResetToken;
import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.repository.PasswordResetTokenRepository;
import com.sfms.iam.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * UC-032 Forgot Password.
 *
 * <p><b>Nguyên tắc bao trùm: không lộ email có tồn tại hay không</b> (TDS §8.1). Mọi nhánh
 * của {@link #request(String)} trả về giống hệt nhau — email thật, email lạ, tài khoản
 * INACTIVE, hay đang bị rate-limit.
 *
 * <p>⚠️ <b>Xung đột đã giải quyết</b> (TestSpec §9.5 mục 5): UC-032 Exceptions đòi trả
 * <b>MSG-003</b> cho tài khoản deactivated, nhưng TDS §8.1 đòi không lộ email tồn tại.
 * Hai yêu cầu loại trừ nhau. Chọn <b>không-lộ</b> vì UC-032 Exceptions là mô tả UX chứ không
 * phải ràng buộc bảo mật, còn enumeration là rò rỉ PII/PDPD thực sự
 * (MF01-TC-022 bước 5 và MF01-TC-SEC-011 bước 3 đều assert hai nhánh giống hệt nhau).
 * <b>Cần Nam ghi 1 dòng vào TDS §17 để chốt chính thức.</b>
 */
@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserAccountRepository userRepository;
    private final AccountService accountService;
    private final PasswordPolicyValidator policyValidator;
    private final PasswordHistoryService passwordHistoryService;
    private final SecurityPolicyProvider policyProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenHasher tokenHasher;
    private final EmailService emailService;
    private final AuditService auditService;
    private final Clock clock;
    private final Duration ttl;
    private final Duration requestWindow;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserAccountRepository userRepository,
                                AccountService accountService,
                                PasswordPolicyValidator policyValidator,
                                PasswordHistoryService passwordHistoryService,
                                SecurityPolicyProvider policyProvider,
                                RefreshTokenService refreshTokenService,
                                TokenHasher tokenHasher,
                                EmailService emailService,
                                AuditService auditService,
                                Clock clock,
                                @Value("${sfms.reset-token.minutes:60}") int ttlMinutes,
                                @Value("${sfms.reset-token.request-window-minutes:15}") int windowMinutes) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.policyValidator = policyValidator;
        this.passwordHistoryService = passwordHistoryService;
        this.policyProvider = policyProvider;
        this.refreshTokenService = refreshTokenService;
        this.tokenHasher = tokenHasher;
        this.emailService = emailService;
        this.auditService = auditService;
        this.clock = clock;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.requestWindow = Duration.ofMinutes(windowMinutes);
    }

    /**
     * UC-032 bước 1 — BR-003: token 1 giờ, tối đa <b>1 request / 15 phút / email</b>.
     *
     * <p>Không bao giờ ném lỗi ra ngoài: rate-limit, email lạ và tài khoản không login được
     * đều lặng lẽ không gửi gì. Ném lỗi ở bất kỳ nhánh nào là mở kênh dò danh bạ.
     */
    @Transactional
    public void request(String email) {
        Instant now = clock.instant();
        Optional<UserAccount> found = userRepository.findByEmail(email);

        if (found.isEmpty()) {
            return;                       // email lạ — im lặng (MF01-TC-022 bước 4)
        }
        UserAccount user = found.get();
        if (!user.canLogin()) {
            return;                       // INACTIVE/SUSPENDED/PENDING — im lặng (xung đột §9.5-5)
        }
        if (isRateLimited(user, now)) {
            return;                       // BR-003 — im lặng, không email thứ 2
        }

        String raw = tokenHasher.generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(tokenHasher.hash(raw));
        token.setExpiresAt(now.plus(ttl));           // BR-003 — 1 giờ
        tokenRepository.saveAndFlush(token);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), raw);
    }

    /**
     * BR-003 — cửa sổ đúng 15 phút tính từ lần yêu cầu gần nhất.
     * MF01-TC-022: {@code T0+14'59"} bị chặn, {@code T0+15'00"} đi qua.
     */
    private boolean isRateLimited(UserAccount user, Instant now) {
        return tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .map(last -> now.isBefore(last.getCreatedAt().plus(requestWindow)))
                .orElse(false);
    }

    /**
     * UC-032 bước 6 — đặt lại mật khẩu bằng token.
     *
     * <p>Áp <b>đủ</b> BR-006 (chính sách + không trùng 3 MK gần nhất). Đây là lỗi rất thường
     * gặp: policy chỉ kiểm ở {@code changePassword} mà quên ở {@code reset} ⇒ qua đường
     * "quên mật khẩu" đặt được mật khẩu yếu hoặc trùng mật khẩu cũ (MF01-TC-021 Expected FAIL).
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-008} · {@code IAM-006policy}
     *         · {@code IAM-006reuse}
     */
    @Transactional
    public void reset(String rawToken, String newPassword, String ip) {
        Instant now = clock.instant();

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHasher.hash(rawToken))
                .orElseThrow(IamExceptions::tokenInvalid);
        if (!token.isUsable(now)) {
            throw IamExceptions.tokenInvalid();      // BR-003 hết hạn hoặc đã dùng 1 lần
        }

        UserAccount user = userRepository.findById(token.getUserId())
                .orElseThrow(IamExceptions::tokenInvalid);

        SecurityPolicy policy = policyProvider.current();
        policyValidator.validate(newPassword, policy);
        passwordHistoryService.assertNotReused(user.getId(), newPassword, policy.isNoReuseLast3());

        accountService.applyNewPassword(user, newPassword);

        token.setUsedAt(now);
        tokenRepository.saveAndFlush(token);

        // UC-032 bước 6 — "invalidates all other active sessions".
        // Thiếu bước này: kẻ đã chiếm phiên vẫn ở trong hệ thống dù chủ tài khoản đã đổi MK.
        refreshTokenService.revokeAll(user.getId());

        auditService.success(user.getEmail(), ip,
                AuditService.RESET_PASSWORD, AuditService.RESOURCE_AUTH);
    }
}
