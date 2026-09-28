package com.sfms.iam.service;

import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.error.IamExceptions;
import org.springframework.stereotype.Service;

/**
 * BR-006 — kiểm tra độ mạnh mật khẩu theo <b>tham số đọc từ {@link SecurityPolicy}</b>.
 *
 * <p>MF01-TC-002 bước 4 ghi bản ghi policy mới {@code passwordMinLength=12} rồi assert
 * {@code "P@ssw0rd123"} (11 ký tự) <b>phải</b> bị từ chối — đó là bằng chứng giá trị đến từ DB
 * chứ không phải hằng trong code (TDS §17 C3).
 *
 * <p>Vi phạm ⇒ {@code IAM-006policy} + <b>MSG-104</b>, HTTP 422.
 * ⚠️ KHÔNG dùng MSG-063: đó là <i>Warning</i> ("recommend"), không chặn được thao tác
 * (TestSpec §2 L1).
 */
@Service
public class PasswordPolicyValidator {

    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:'\",.<>/?`~\\";

    /**
     * @throws com.sfms.common.error.ApiException {@code IAM-006policy} (422, MSG-104)
     *         nếu mật khẩu không đạt bất kỳ tiêu chí nào của policy
     */
    public void validate(String rawPassword, SecurityPolicy policy) {
        if (rawPassword == null || rawPassword.length() < policy.getPasswordMinLength()) {
            throw IamExceptions.passwordPolicy();
        }
        if (policy.isRequireMixedCase()
                && !(hasUpper(rawPassword) && hasLower(rawPassword))) {
            throw IamExceptions.passwordPolicy();
        }
        if (policy.isRequireNumber() && rawPassword.chars().noneMatch(Character::isDigit)) {
            throw IamExceptions.passwordPolicy();
        }
        if (policy.isRequireSpecialChar()
                && rawPassword.chars().noneMatch(c -> SPECIAL_CHARS.indexOf(c) >= 0)) {
            throw IamExceptions.passwordPolicy();
        }
    }

    private boolean hasUpper(String s) {
        return s.chars().anyMatch(Character::isUpperCase);
    }

    private boolean hasLower(String s) {
        return s.chars().anyMatch(Character::isLowerCase);
    }
}
