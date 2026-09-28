package com.sfms.mf12.service;

import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.repository.SecurityPolicyRepository;
import com.sfms.iam.service.AuditService;
import com.sfms.mf12.dto.SecurityPolicyDtos.PublicPasswordPolicyResponse;
import com.sfms.mf12.dto.SecurityPolicyDtos.SecurityPolicyResponse;
import com.sfms.mf12.dto.SecurityPolicyDtos.UpdateSecurityPolicyRequest;
import com.sfms.mf12.error.AisExceptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-184 — CF-6: bảng {@code security_policies} + seed {@code SEC-DEFAULT} thuộc MF01; MF12
 * đọc/ghi thẳng {@code iam.repository.SecurityPolicyRepository} (cùng tiền lệ M12-D08 với
 * audit log — MF12 là chủ nghiệp vụ UC-184, không nhân bản entity).
 */
@Service
public class SecurityPolicyServiceImpl implements SecurityPolicyService {

    // BR-002/BR-004/BR-006 — khoảng cho phép.
    private static final int MIN_LOGIN_ATTEMPTS = 3;
    private static final int MAX_LOGIN_ATTEMPTS = 10;
    private static final int MIN_LOCKOUT_MINUTES = 5;
    private static final int MAX_LOCKOUT_MINUTES = 60;
    private static final int MIN_SESSION_TIMEOUT = 5;
    private static final int MAX_SESSION_TIMEOUT = 120;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 32;

    private final SecurityPolicyRepository repository;
    private final AuditService auditService;

    public SecurityPolicyServiceImpl(SecurityPolicyRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityPolicyResponse get() {
        return toResponse(activePolicy());
    }

    @Override
    @Transactional
    public SecurityPolicyResponse update(UpdateSecurityPolicyRequest request, String actorEmail, String ip) {
        SecurityPolicy policy = activePolicy();

        if (request.maxLoginAttempts() != null) {
            requireInRange("maxLoginAttempts", request.maxLoginAttempts(), MIN_LOGIN_ATTEMPTS, MAX_LOGIN_ATTEMPTS);
            policy.setMaxLoginAttempts(request.maxLoginAttempts());
        }
        if (request.lockoutMinutes() != null) {
            requireInRange("lockoutMinutes", request.lockoutMinutes(), MIN_LOCKOUT_MINUTES, MAX_LOCKOUT_MINUTES);
            policy.setLockoutMinutes(request.lockoutMinutes());
        }
        if (request.sessionTimeoutMinutes() != null) {
            requireInRange("sessionTimeoutMinutes", request.sessionTimeoutMinutes(), MIN_SESSION_TIMEOUT,
                    MAX_SESSION_TIMEOUT);
            policy.setSessionTimeoutMinutes(request.sessionTimeoutMinutes());
        }
        if (request.passwordMinLength() != null) {
            requireInRange("passwordMinLength", request.passwordMinLength(), MIN_PASSWORD_LENGTH,
                    MAX_PASSWORD_LENGTH);
            policy.setPasswordMinLength(request.passwordMinLength());
        }
        if (request.requireSpecialChar() != null) {
            policy.setRequireSpecialChar(request.requireSpecialChar());
        }
        if (request.requireNumber() != null) {
            policy.setRequireNumber(request.requireNumber());
        }
        if (request.requireMixedCase() != null) {
            policy.setRequireMixedCase(request.requireMixedCase());
        }
        if (request.noReuseLast3() != null) {
            policy.setNoReuseLast3(request.noReuseLast3());
        }

        // D9/D12 — updatedBy/updatedAt do AuditorAware điền, KHÔNG nhận từ client dù client gửi lên.
        SecurityPolicyResponse response = toResponse(repository.save(policy));
        auditService.success(actorEmail, ip, "UPDATE security-policy", "Security Policies");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicPasswordPolicyResponse getPublicPasswordPolicy() {
        SecurityPolicy policy = activePolicy();
        return new PublicPasswordPolicyResponse(policy.getPasswordMinLength(), policy.isRequireSpecialChar(),
                policy.isRequireNumber(), policy.isRequireMixedCase(), policy.isNoReuseLast3(),
                policy.getMaxLoginAttempts(), policy.getLockoutMinutes());
    }

    private SecurityPolicy activePolicy() {
        return repository.findFirstByActiveIsTrue().orElseThrow(AisExceptions::notFound);
    }

    private static void requireInRange(String field, int value, int min, int max) {
        if (value < min || value > max) {
            throw AisExceptions.securityPolicyOutOfRange(field, min + "–" + max);
        }
    }

    private SecurityPolicyResponse toResponse(SecurityPolicy p) {
        return new SecurityPolicyResponse(p.getCode(), p.getMaxLoginAttempts(), p.getLockoutMinutes(),
                p.getSessionTimeoutMinutes(), p.getPasswordMinLength(), p.isRequireSpecialChar(),
                p.isRequireNumber(), p.isRequireMixedCase(), p.isNoReuseLast3());
    }
}
