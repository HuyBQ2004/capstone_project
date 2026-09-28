package com.sfms.iam.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Chính sách bảo mật — <b>nguồn tham số duy nhất</b> cho BR-002/004/006.
 *
 * <p>TDS §17 <b>C3</b>: mọi ngưỡng đọc từ đây, KHÔNG hardcode. Đó là lý do
 * MF01-TC-005 đổi policy sang {@code maxLoginAttempts=3, lockoutMinutes=20} và
 * MF01-TC-SEC-003 bước 5 đổi {@code sessionTimeoutMinutes=10}: nếu hardcode thì
 * màn {@code system-admin/security-policies} chỉ là trang trí.
 */
@Entity
@Table(name = "security_policies")
public class SecurityPolicy extends BaseEntity {

    /** D1 — {@code SEC-DEFAULT}. */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    /** BR-002 — số lần sai liên tiếp trước khi khóa. */
    @Column(name = "max_login_attempts", nullable = false)
    private int maxLoginAttempts;

    /** BR-002 — thời lượng khóa; cũng là giá trị render vào {@code [Minutes]} của MSG-076. */
    @Column(name = "lockout_minutes", nullable = false)
    private int lockoutMinutes;

    /** BR-004 — TTL access token (mặc định 30'). */
    @Column(name = "session_timeout_minutes", nullable = false)
    private int sessionTimeoutMinutes;

    /** BR-006 — độ dài tối thiểu. */
    @Column(name = "password_min_length", nullable = false)
    private int passwordMinLength;

    @Column(name = "require_special_char", nullable = false)
    private boolean requireSpecialChar;

    @Column(name = "require_number", nullable = false)
    private boolean requireNumber;

    @Column(name = "require_mixed_case", nullable = false)
    private boolean requireMixedCase;

    /** BR-006 thân mục (dẫn BR-015) — không dùng lại 3 MK gần nhất. */
    @Column(name = "no_reuse_last3", nullable = false)
    private boolean noReuseLast3;

    /** Đúng 1 bản ghi {@code active=true} tại một thời điểm (UNIQUE partial ở V1). */
    @Column(name = "active", nullable = false)
    private boolean active;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }

    public int getLockoutMinutes() {
        return lockoutMinutes;
    }

    public void setLockoutMinutes(int lockoutMinutes) {
        this.lockoutMinutes = lockoutMinutes;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public boolean isRequireSpecialChar() {
        return requireSpecialChar;
    }

    public void setRequireSpecialChar(boolean requireSpecialChar) {
        this.requireSpecialChar = requireSpecialChar;
    }

    public boolean isRequireNumber() {
        return requireNumber;
    }

    public void setRequireNumber(boolean requireNumber) {
        this.requireNumber = requireNumber;
    }

    public boolean isRequireMixedCase() {
        return requireMixedCase;
    }

    public void setRequireMixedCase(boolean requireMixedCase) {
        this.requireMixedCase = requireMixedCase;
    }

    public boolean isNoReuseLast3() {
        return noReuseLast3;
    }

    public void setNoReuseLast3(boolean noReuseLast3) {
        this.noReuseLast3 = noReuseLast3;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
