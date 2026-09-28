package com.sfms.iam.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.RoleKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Tài khoản người dùng — trung tâm của MF01, được cả 12 module còn lại tin cậy qua JWT.
 *
 * <p>{@code passwordHash} và {@code googleSub} là <b>PII nhạy cảm</b>: đánh dấu
 * {@link JsonIgnore} để dù có ai map thẳng entity ra response thì cũng không rò
 * (MF01-TC-SEC-010). Đường đúng vẫn là qua DTO ở {@code iam/dto/}.
 */
@Entity
@Table(name = "user_accounts")
public class UserAccount extends BaseEntity {

    /** D1 — mã trên resource {@code /users}: {@code U-0001}. */
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    /**
     * D1 — mã trên resource {@code /staff}: {@code STF-001}. {@code null} nếu người này không
     * phải nhân viên cấp trường.
     *
     * <p>Cùng một con người có hai mã vì mock giữ hai không gian mã ({@code STF-001} và
     * {@code U-0004} đều là Nguyễn Thu Trang) — xem ghi chú trong {@code V1__init_schema.sql}.
     */
    @Column(name = "staff_code", length = 32)
    private String staffCode;

    /**
     * {@code PLATFORM} — xuất hiện ở {@code GET /users}.
     * {@code SCHOOL_STAFF} — lời mời nhân viên chưa thành tài khoản nền tảng, chỉ ở {@code /staff}.
     */
    @Column(name = "account_scope", nullable = false, length = 16)
    private String accountScope = SCOPE_PLATFORM;

    public static final String SCOPE_PLATFORM = "PLATFORM";
    public static final String SCOPE_SCHOOL_STAFF = "SCHOOL_STAFF";

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** BR-012 — hợp lệ & duy nhất (UNIQUE ở tầng DB, không chỉ ở Service). */
    @Column(name = "email", nullable = false, length = 180, unique = true)
    private String email;

    /** BR-012 — định dạng +84 / 0xx. */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * BR-001 — BCrypt strength 12. {@code null} khi {@code PENDING_VERIFICATION}
     * hoặc tài khoản Google-only (BR-218: phiên Google không có MK cục bộ).
     */
    @JsonIgnore
    @Column(name = "password_hash", length = 72)
    private String passwordHash;

    /** BR-218 — định danh Google, set <b>một lần</b> ở lần login Google đầu tiên. */
    @JsonIgnore
    @Column(name = "google_sub", length = 64, unique = true)
    private String googleSub;

    /** BR-007 — vai trò RBAC. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleRecord role;

    /** BR-013 — trường trực thuộc; {@code null} với System Admin / tài khoản đối tác. */
    @Column(name = "school_id")
    private UUID schoolId;

    /**
     * Khóa cách ly dữ liệu đa-tenant của NCC (MF04 ADR-M04-6) và là cơ chế DUY NHẤT
     * để cascade đình chỉ đối tác kéo theo khóa tài khoản. {@code null} với tài khoản nội bộ trường.
     */
    @Column(name = "partner_id")
    private UUID partnerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    /** BR-010 — chỉ login sau khi xác minh email. */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /** BR-002 — đếm sai liên tiếp. */
    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;

    /** BR-002 — thời điểm hết khóa. */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    /** BR-009 — chấp nhận Chính sách Bảo mật & Điều khoản ở lần login đầu. */
    @Column(name = "terms_accepted_at")
    private Instant termsAcceptedAt;

    @Column(name = "last_login")
    private Instant lastLogin;

    /** UC-003 — tài khoản System Admin gốc, không bao giờ được vô hiệu hóa. */
    @Column(name = "primary_admin", nullable = false)
    private boolean primaryAdmin = false;

    /** UC-097 — ảnh hồ sơ. */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // ───────────────────────── Hành vi miền ─────────────────────────

    /** D1 — JSON trả {@code code} dưới tên {@code id}. */
    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    /** D6 — giá trị {@code role} trả ra API và đưa vào claim JWT (kebab-case). */
    public RoleKey roleKey() {
        return role == null ? null : role.roleKeyEnum();
    }

    /** BR-002 — còn trong thời gian khóa? So sánh {@code isBefore} ⇒ đúng mốc là ĐÃ MỞ. */
    public boolean isLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    /** BR-010 — điều kiện đủ để login: ACTIVE và email đã xác minh. */
    public boolean canLogin() {
        return status.canLogin() && emailVerified;
    }

    /** BR-009 — lần login đầu ⇒ FE phải hiện màn chấp nhận điều khoản. */
    public boolean needTermsAccept() {
        return termsAcceptedAt == null;
    }

    /** BR-002 — login thành công thì reset counter và gỡ khóa. */
    public void resetLockout() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    // ───────────────────────── Getters / setters ─────────────────────────

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getAccountScope() {
        return accountScope;
    }

    public void setAccountScope(String accountScope) {
        this.accountScope = accountScope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    public RoleRecord getRole() {
        return role;
    }

    public void setRole(RoleRecord role) {
        this.role = role;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public UUID getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(UUID partnerId) {
        this.partnerId = partnerId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(int failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Instant getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public void setTermsAcceptedAt(Instant termsAcceptedAt) {
        this.termsAcceptedAt = termsAcceptedAt;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isPrimaryAdmin() {
        return primaryAdmin;
    }

    public void setPrimaryAdmin(boolean primaryAdmin) {
        this.primaryAdmin = primaryAdmin;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * SEC-012 — {@code toString()} KHÔNG bao giờ in {@code passwordHash}/{@code googleSub}.
     * Một dòng {@code log.debug("user={}", user)} là đủ để lộ credential vào log.
     */
    @Override
    public String toString() {
        return "UserAccount{code=" + code + ", email=" + email + ", status=" + status + "}";
    }
}
