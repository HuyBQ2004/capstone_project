package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.AuthDtos.LoginResponse;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-034 Login · UC-035 Logout — cổng vào của mọi actor SFMS.
 *
 * <p><b>Thứ tự guard</b> (TDS §6.1) là một phần của đặc tả, không phải chi tiết cài đặt:
 * <ol>
 *   <li>Tìm user → không có ⇒ MSG-002 (KHÔNG 404 — chống enumeration)</li>
 *   <li>BR-010: {@code canLogin()} → INACTIVE/SUSPENDED/chưa xác minh ⇒ MSG-003</li>
 *   <li>BR-002: đang khóa ⇒ MSG-076 (<b>thắng cả mật khẩu đúng</b>)</li>
 *   <li>BR-001: so khớp BCrypt</li>
 * </ol>
 * Guard BR-010 chạy <b>trước</b> {@code bCrypt.matches()} vì tài khoản
 * {@code PENDING_VERIFICATION} có {@code passwordHash = NULL} — đảo thứ tự là NPE
 * (MF01-TC-006).
 */
@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityPolicyProvider policyProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final ReferenceResolver refs;
    private final Clock clock;

    /**
     * Hash giả để so khớp khi email không tồn tại — giữ thời gian phản hồi tương đương
     * nhánh email có thật. Không có nó, email lạ trả về nhanh hơn rõ rệt ⇒ timing attack
     * (MF01-TC-SEC-011 bước 4: chênh trung vị phải &lt; 50ms).
     */
    private static final String DUMMY_BCRYPT_HASH =
            "$2a$12$C6UzMDM.H6dfI/f/IKcEeO1WlF0eqEKuaqPo1qmM4RTLuWZ8h9Bhy";

    public AuthService(UserAccountRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SecurityPolicyProvider policyProvider,
                       RefreshTokenService refreshTokenService,
                       JwtService jwtService,
                       AuditService auditService,
                       ReferenceResolver refs,
                       Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyProvider = policyProvider;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.refs = refs;
        this.clock = clock;
    }

    /**
     * UC-034 — đăng nhập bằng email/mật khẩu.
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-002} (401, kèm
     *         {@code details.attemptsLeft}) · {@code IAM-003} (403) · {@code IAM-007} (423)
     */
    @Transactional(noRollbackFor = ApiException.class)
    public LoginResponse login(String email, String rawPassword, String ip) {
        Instant now = clock.instant();
        SecurityPolicy policy = policyProvider.current();
        Optional<UserAccount> found = userRepository.findByEmail(email);

        // ── Email không tồn tại ────────────────────────────────────────────────
        // Vẫn chạy BCrypt trên hash giả (timing) và vẫn trả `attemptsLeft` theo cùng
        // khuôn (shape) như email có thật — chốt TDS §9.2 2026-07-31.
        if (found.isEmpty()) {
            passwordEncoder.matches(rawPassword, DUMMY_BCRYPT_HASH);
            auditService.blocked(email, ip, AuditService.LOGIN, AuditService.RESOURCE_AUTH);
            throw IamExceptions.invalidCredentials(policy.getMaxLoginAttempts() - 1);
        }

        UserAccount user = found.get();

        // ── ① BR-010 — chỉ ACTIVE + email đã xác minh mới login được ───────────
        // Chạy TRƯỚC bCrypt: PENDING_VERIFICATION có passwordHash = NULL.
        // Cũng KHÔNG tăng failedLoginCount — chưa xác minh ≠ sai MK, không lockout oan.
        if (!user.canLogin()) {
            auditService.blocked(user.getEmail(), ip, AuditService.LOGIN, AuditService.RESOURCE_AUTH);
            throw IamExceptions.accountDeactivated();
        }

        // ── ② BR-002 — đang trong thời gian khóa ───────────────────────────────
        // Đặt TRƯỚC so khớp mật khẩu: khóa phải thắng cả mật khẩu đúng (MF01-TC-004 bước 3).
        if (user.isLocked(now)) {
            auditService.blocked(user.getEmail(), ip, AuditService.LOGIN, AuditService.RESOURCE_AUTH);
            throw IamExceptions.accountLocked(policy.getLockoutMinutes());
        }

        // ── ③ BR-001 — so khớp BCrypt ──────────────────────────────────────────
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return handleFailedAttempt(user, ip, policy, now);
        }

        return completeLogin(user, ip, policy, now, AuditService.LOGIN);
    }

    /** BR-002 — tăng counter, khóa khi chạm ngưỡng, revoke phiên đang mở. */
    private LoginResponse handleFailedAttempt(UserAccount user, String ip,
                                              SecurityPolicy policy, Instant now) {
        int attempts = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(attempts);

        if (attempts >= policy.getMaxLoginAttempts()) {
            user.setLockedUntil(now.plus(Duration.ofMinutes(policy.getLockoutMinutes())));
            userRepository.saveAndFlush(user);
            // BR-004: lockout ⇒ revoke refresh ngay. Bỏ bước này thì kẻ đã có refresh token
            // vẫn cấp được access token suốt 14 ngày ⇒ BR-002 vô nghĩa (MF01-TC-SEC-009 c).
            refreshTokenService.revokeAll(user.getId());
            auditService.blocked(user.getEmail(), ip, AuditService.LOGIN, AuditService.RESOURCE_AUTH);
            throw IamExceptions.accountLocked(policy.getLockoutMinutes());
        }

        userRepository.saveAndFlush(user);
        auditService.blocked(user.getEmail(), ip, AuditService.LOGIN, AuditService.RESOURCE_AUTH);
        throw IamExceptions.invalidCredentials(policy.getMaxLoginAttempts() - attempts);
    }

    /** Dùng chung cho login thường (UC-034) và login Google (UC-191). */
    LoginResponse completeLogin(UserAccount user, String ip, SecurityPolicy policy,
                                Instant now, String auditAction) {
        /*
         * Bổ sung 11/08/2026 — trường liên kết (nếu có) phải đang ACTIVE.
         *
         * Lỗ hổng đo thật: user activate email xong (`canLogin() == true`) là login được
         * ngay, kể cả khi School.status vẫn PENDING (chưa được System Admin duyệt — UC-070)
         * hoặc đã chuyển INACTIVE sau đó (UC-072/BR-021 chỉ đóng băng classroom, không revoke
         * quyền login của user). Guard BR-010 ở trên chỉ đọc UserAccount, không tầng nào
         * trong luồng login tham chiếu School — thêm bước này để chặn đúng lỗ hổng đó.
         * System Admin (schoolId == null) không bị áp guard này.
         *
         * `schoolStatus == null` (không tìm thấy hàng `schools`) CHO QUA thay vì chặn — IAM
         * cố tình decoupled khỏi entity MF02 (xem Javadoc `ReferenceResolver`), và fixture
         * IAM test (`UserFixture.base()`, schoolId=SCH_HN_0042) không seed bảng `schools`.
         * Chặn cả trường hợp null sẽ đỏ toàn bộ IT login test hiện có mà không bắt thêm được
         * lỗ hổng nào thật — orphan schoolId (school bị xoá) là lỗi toàn vẹn dữ liệu riêng,
         * không phải thứ guard đăng nhập này nhắm tới.
         */
        String schoolStatus = refs.schoolStatus(user.getSchoolId());
        if (user.getSchoolId() != null && schoolStatus != null && !"ACTIVE".equals(schoolStatus)) {
            auditService.blocked(user.getEmail(), ip, auditAction, AuditService.RESOURCE_AUTH);
            throw IamExceptions.accountDeactivated();
        }

        boolean needTermsAccept = user.needTermsAccept();   // BR-009 — đọc TRƯỚC khi cập nhật

        user.resetLockout();                                 // BR-002 reset counter
        user.setLastLogin(now);
        userRepository.saveAndFlush(user);

        // BR-005 — revoke phiên cũ rồi mới cấp phiên mới (thứ tự bên trong service đó).
        String refreshToken = refreshTokenService.issueForNewSession(user);
        String accessToken = jwtService.issueAccessToken(user, policy.getSessionTimeoutMinutes());

        auditService.success(user.getEmail(), ip, auditAction, AuditService.RESOURCE_AUTH);

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.roleKey(),                                  // D6 kebab-case
                refs.schoolCode(user.getSchoolId()),             // D1 mã nghiệp vụ
                refs.partnerCode(user.getPartnerId()),           // D1 khóa cách ly đa-tenant
                needTermsAccept                                  // BR-009
        );
    }

    /**
     * UC-035 — logout. {@code userId} lấy từ JWT, KHÔNG từ body (TDS §17 C2).
     *
     * <p>Idempotent: gọi lần hai không lỗi, không tạo dòng revoke mới
     * (MF01-TC-INT-005 bước 4).
     */
    @Transactional
    public void logout(UUID userId, String actorEmail, String ip) {
        refreshTokenService.revokeAll(userId);
        auditService.success(actorEmail, ip, AuditService.LOGOUT, AuditService.RESOURCE_AUTH);
    }

    /** BR-009 — ghi nhận người dùng đã chấp nhận Chính sách & Điều khoản. */
    @Transactional
    public Instant acceptTerms(UUID userId) {
        UserAccount user = userRepository.findById(userId).orElseThrow(IamExceptions::notFound);
        if (user.getTermsAcceptedAt() == null) {
            user.setTermsAcceptedAt(clock.instant());
            userRepository.saveAndFlush(user);
        }
        return user.getTermsAcceptedAt();
    }
}
