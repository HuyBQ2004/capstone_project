package com.sfms.iam.service;

import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * UC-011 Change Password.
 *
 * <p>Chuỗi kiểm tra bắt buộc, đủ cả ba (bỏ sót một là một TC đỏ):
 * <ol>
 *   <li>BR-002/MSG-002 — mật khẩu hiện tại đúng</li>
 *   <li>BR-006/MSG-104 — mật khẩu mới đạt chính sách đọc từ {@code SecurityPolicy}</li>
 *   <li>BR-006/MSG-105 — không trùng 3 mật khẩu gần nhất</li>
 * </ol>
 *
 * <p>Sau khi đổi: {@code revokeAll} — UC-011 <i>"terminates all other active sessions"</i>
 * (MF01-TC-SEC-009 nhánh f).
 */
@Service
public class AccountService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator policyValidator;
    private final PasswordHistoryService passwordHistoryService;
    private final SecurityPolicyProvider policyProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    public AccountService(UserAccountRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          PasswordPolicyValidator policyValidator,
                          PasswordHistoryService passwordHistoryService,
                          SecurityPolicyProvider policyProvider,
                          RefreshTokenService refreshTokenService,
                          AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyValidator = policyValidator;
        this.passwordHistoryService = passwordHistoryService;
        this.policyProvider = policyProvider;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
    }

    /**
     * @param userId lấy từ JWT — KHÔNG từ body (TDS §17 C2). Nhận từ body cho phép giáo viên
     *               đổi mật khẩu của School Admin (MF01-TC-SEC-005 mục b).
     * @throws com.sfms.common.error.ApiException {@code IAM-002} · {@code IAM-006policy}
     *         · {@code IAM-006reuse}
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword, String ip) {
        UserAccount user = userRepository.findById(userId).orElseThrow(IamExceptions::notFound);
        SecurityPolicy policy = policyProvider.current();

        // ① Mật khẩu hiện tại. Tài khoản Google-only (passwordHash = NULL) không đi đường này.
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            auditService.blocked(user.getEmail(), ip,
                    AuditService.CHANGE_PASSWORD, AuditService.RESOURCE_AUTH);
            throw IamExceptions.invalidCredentials();
        }

        // ② BR-006 chính sách · ③ BR-006 không trùng 3 MK gần nhất
        policyValidator.validate(newPassword, policy);
        passwordHistoryService.assertNotReused(userId, newPassword, policy.isNoReuseLast3());

        applyNewPassword(user, newPassword);

        // UC-011 — kết thúc mọi phiên khác
        refreshTokenService.revokeAll(userId);
        auditService.success(user.getEmail(), ip,
                AuditService.CHANGE_PASSWORD, AuditService.RESOURCE_AUTH);
    }

    /**
     * Đặt mật khẩu mới: BCrypt (BR-001) + ghi lịch sử (BR-006).
     * Dùng chung bởi UC-011, UC-032 (reset) và UC-154 (activation) — một chỗ duy nhất
     * để không có đường nào bỏ qua việc hash hoặc bỏ qua việc ghi lịch sử.
     */
    @Transactional
    public void applyNewPassword(UserAccount user, String rawNewPassword) {
        String hash = passwordEncoder.encode(rawNewPassword);
        user.setPasswordHash(hash);
        userRepository.saveAndFlush(user);
        passwordHistoryService.record(user.getId(), hash);
    }
}
