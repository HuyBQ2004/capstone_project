package com.sfms.iam.service;

import com.sfms.iam.domain.AccountActivationToken;
import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.repository.AccountActivationTokenRepository;
import com.sfms.iam.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * UC-154 Activate Account &amp; Set Password.
 *
 * <p>Chuyển {@code PENDING_VERIFICATION → ACTIVE} và đặt mật khẩu <b>lần đầu</b> — đây là
 * đường DUY NHẤT để một tài khoản có mật khẩu (invariant TDS §6.3: không set password lúc tạo).
 *
 * <p>Hai lỗi hay gặp mà test bắt:
 * <ul>
 *   <li>chỉ đổi {@code status} mà quên {@code emailVerified = true} ⇒ BR-010 chặn login
 *       ngay sau khi vừa kích hoạt (MF01-TC-INT-007 Expected FAIL)</li>
 *   <li>so sánh {@code <} tại đúng mốc 48h ⇒ chặn oan người bấm link đúng hạn
 *       (MF01-TC-018 nhánh b)</li>
 * </ul>
 *
 * <p>⚠️ <b>MISSING-ORACLE</b> (TestSpec §2 L9): không có MSG nào mang nghĩa "kích hoạt xong"
 * — UC file nói MSG-031 ("account created"), traceability nói MSG-028 ("verification email
 * has been sent"), cả hai đều lệch ngữ nghĩa. Service vì vậy KHÔNG trả success message;
 * controller chỉ trả 200.
 */
@Service
public class AccountActivationService {

    private final AccountActivationTokenRepository tokenRepository;
    private final UserAccountRepository userRepository;
    private final AccountService accountService;
    private final PasswordPolicyValidator policyValidator;
    private final SecurityPolicyProvider policyProvider;
    private final TokenHasher tokenHasher;
    private final EmailService emailService;
    private final AuditService auditService;
    private final Clock clock;
    private final Duration ttl;

    public AccountActivationService(AccountActivationTokenRepository tokenRepository,
                                    UserAccountRepository userRepository,
                                    AccountService accountService,
                                    PasswordPolicyValidator policyValidator,
                                    SecurityPolicyProvider policyProvider,
                                    TokenHasher tokenHasher,
                                    EmailService emailService,
                                    AuditService auditService,
                                    Clock clock,
                                    @Value("${sfms.activation-token.hours:48}") int ttlHours) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.policyValidator = policyValidator;
        this.policyProvider = policyProvider;
        this.tokenHasher = tokenHasher;
        this.emailService = emailService;
        this.auditService = auditService;
        this.clock = clock;
        this.ttl = Duration.ofHours(ttlHours);
    }

    /**
     * Cấp token kích hoạt và gửi email. Gọi ngay sau khi tạo tài khoản (UC-024/026).
     * Vô hiệu mọi token cũ trước — mỗi lúc chỉ một link sống.
     */
    @Transactional
    public String issueAndSend(UserAccount user) {
        Instant now = clock.instant();
        tokenRepository.invalidateAllForUser(user.getId(), now);

        String raw = tokenHasher.generateRawToken();
        AccountActivationToken token = new AccountActivationToken();
        token.setUserId(user.getId());
        token.setTokenHash(tokenHasher.hash(raw));
        token.setExpiresAt(now.plus(ttl));          // BR-011 — 48h
        tokenRepository.saveAndFlush(token);

        emailService.sendActivationEmail(user.getEmail(), user.getName(), raw);
        return raw;
    }

    /**
     * UC-154 Normal Flow — xác minh token, đặt mật khẩu, kích hoạt.
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-008} (401, MSG-004) token hết hạn
     *         hoặc đã dùng · {@code IAM-006policy} (422, MSG-104) mật khẩu yếu
     */
    @Transactional
    public void activate(String rawToken, String newPassword, String ip) {
        Instant now = clock.instant();

        AccountActivationToken token = tokenRepository.findByTokenHash(tokenHasher.hash(rawToken))
                .orElseThrow(IamExceptions::tokenInvalid);

        // Dùng 1 lần + BR-011 48h. Không kiểm ⇒ ai đọc được link trong hộp thư đổi được
        // mật khẩu của tài khoản đã kích hoạt (MF01-TC-019).
        if (!token.isUsable(now)) {
            throw IamExceptions.tokenInvalid();
        }

        UserAccount user = userRepository.findById(token.getUserId())
                .orElseThrow(IamExceptions::tokenInvalid);

        // Tài khoản đã ACTIVE ⇒ luôn từ chối (UC-154 Exceptions "Account Already Active").
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw IamExceptions.tokenInvalid();
        }

        // UC-154 bước 5 dẫn BR-006 — chính sách áp cả ở đường đặt mật khẩu lần đầu.
        SecurityPolicy policy = policyProvider.current();
        policyValidator.validate(newPassword, policy);

        // ⚠️ Thứ tự: đổi status TRƯỚC khi set hash, vì CHECK constraint
        // `ck_pending_has_no_password` cấm PENDING_VERIFICATION có password_hash.
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);                 // BR-010 — thiếu dòng này là login chết
        userRepository.saveAndFlush(user);

        accountService.applyNewPassword(user, newPassword);   // BR-001 BCrypt + ghi lịch sử

        token.setUsedAt(now);
        tokenRepository.saveAndFlush(token);

        auditService.success(user.getEmail(), ip,
                AuditService.ACTIVATE_ACCOUNT, AuditService.RESOURCE_USERS);
    }

    /**
     * UC-154 AF1 — gửi lại link mới, gia hạn thêm 48h (BR-011).
     *
     * <p>Cấp token <b>MỚI</b> và vô hiệu token cũ, KHÔNG gia hạn tại chỗ: gia hạn tại chỗ
     * mâu thuẫn câu chữ <i>"gửi lại link mới"</i> và để chuỗi token cũ sống tiếp
     * (MF01-TC-020 Expected FAIL).
     *
     * <p>Không lộ email tồn tại hay không — email lạ vẫn trả 200, không gửi gì.
     */
    @Transactional
    public void resend(String email) {
        userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == UserStatus.PENDING_VERIFICATION)
                .ifPresent(this::issueAndSend);
    }
}
