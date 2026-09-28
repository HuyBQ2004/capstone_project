package com.sfms.iam.fixture;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * CASE 2.0 — <b>Props Isolation Pattern</b>.
 *
 * <p>Mỗi {@code @Test} gọi factory để có đối tượng RIÊNG ⇒ không có shared mutable state
 * (chống AP-AI-002 / TestSpec §6 "Props Isolation": grep phải KHÔNG khớp khai báo
 * {@code UserAccount} ở mức field trong {@code *Test.java}).
 */
public final class UserFixture {

    /** BR-001 · TDS §4.3 — cost 12, khớp {@code @Bean} của {@code SecurityConfig}. */
    public static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    public static final String PWD_VALID = "P@ssw0rd123";     // FX-PWD-VALID
    public static final String PWD_WEAK = "password";         // FX-PWD-WEAK

    public static final UUID SCH_HN_0042 = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    public static final UUID SCH_HN_0051 = UUID.fromString("00000000-0000-0000-0000-0000000a0051");
    public static final UUID SUP_001 = UUID.fromString("00000000-0000-0000-0000-0000000b0001");
    public static final UUID SUP_004 = UUID.fromString("00000000-0000-0000-0000-0000000b0004");

    private UserFixture() {
    }

    // ───────────────────────── Role ─────────────────────────

    public static RoleRecord role(String code, String name, RoleKey key, boolean system) {
        RoleRecord role = new RoleRecord();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setName(name);
        role.setDescription(name);
        role.setRoleKey(key == null ? null : key.key());
        role.setSystem(system);
        role.setPermissions(List.of());
        return role;
    }

    /** ROLE-02 School Administration (system=true). */
    public static RoleRecord roleSchoolAdmin() {
        return role("ROLE-02", "School Administration", RoleKey.SCHOOL_ADMIN, true);
    }

    /** ROLE-05 Teacher (system=true). */
    public static RoleRecord roleTeacher() {
        return role("ROLE-05", "Teacher", RoleKey.TEACHER, true);
    }

    /** ROLE-01 System Admin (system=true). */
    public static RoleRecord roleSystemAdmin() {
        return role("ROLE-01", "System Admin", RoleKey.SYSTEM_ADMIN, true);
    }

    /** ROLE-08 Kitchen Auditor — custom, 0 user gán (FX-ROLE-CUSTOM-EMPTY). */
    public static RoleRecord roleCustomEmpty() {
        return role("ROLE-08", "Kitchen Auditor", null, false);
    }

    /** ROLE-09 Ad-hoc Auditor — custom, 3 user gán trong đó 1 INACTIVE (FX-ROLE-CUSTOM-USED). */
    public static RoleRecord roleCustomUsed() {
        return role("ROLE-09", "Ad-hoc Auditor", null, false);
    }

    // ───────────────────────── SecurityPolicy ─────────────────────────

    /** FX-POLICY — SEC-DEFAULT: 5 / 15' / 30' / 8, các cờ = true. */
    public static SecurityPolicy policyDefault() {
        SecurityPolicy policy = new SecurityPolicy();
        policy.setId(UUID.randomUUID());
        policy.setCode("SEC-DEFAULT");
        policy.setMaxLoginAttempts(5);
        policy.setLockoutMinutes(15);
        policy.setSessionTimeoutMinutes(30);
        policy.setPasswordMinLength(8);
        policy.setRequireSpecialChar(true);
        policy.setRequireNumber(true);
        policy.setRequireMixedCase(true);
        policy.setNoReuseLast3(true);
        policy.setActive(true);
        return policy;
    }

    /** FX-POLICY-ALT — chứng minh không hardcode: 3 lần sai / khóa 20 phút (MF01-TC-005). */
    public static SecurityPolicy policyAlt() {
        SecurityPolicy policy = policyDefault();
        policy.setMaxLoginAttempts(3);
        policy.setLockoutMinutes(20);
        return policy;
    }

    // ───────────────────────── UserAccount ─────────────────────────

    /** Baseline hợp lệ — đồng bộ FX-U-SCHOOLADMIN (TestSpec §3 TDS-05). */
    public static UserAccount base() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setCode("U-0001");                                   // D1
        user.setName("Phạm Quốc Toản");
        user.setEmail("toanpq@dtd.edu.vn");
        user.setPhone("0912345678");                              // BR-012
        user.setPasswordHash(ENCODER.encode(PWD_VALID));          // BR-001
        user.setRole(roleSchoolAdmin());                          // D6 ⇒ JSON "school-admin"
        user.setSchoolId(SCH_HN_0042);
        user.setStatus(UserStatus.ACTIVE);                        // ADR-M03
        user.setEmailVerified(true);                              // BR-010
        user.setFailedLoginCount(0);                              // BR-002
        user.setTermsAcceptedAt(Instant.parse("2026-01-10T02:00:00Z"));   // BR-009, D4
        user.setAccountScope(UserAccount.SCOPE_PLATFORM);
        return user;
    }

    /** Mỗi test tự override phần mình quan tâm — không sửa builder dùng chung. */
    public static UserAccount aUser(Consumer<UserAccount> overrides) {
        UserAccount user = base();
        overrides.accept(user);
        return user;
    }

    /** FX-U-PENDING — STF-009 Hà Thị Lan, chưa xác minh, chưa có mật khẩu. */
    public static UserAccount pending() {
        return aUser(u -> {
            u.setCode("STF-009");
            u.setStaffCode("STF-009");
            u.setAccountScope(UserAccount.SCOPE_SCHOOL_STAFF);
            u.setName("Hà Thị Lan");
            u.setEmail("lanht@dtd.edu.vn");
            u.setRole(roleTeacher());
            u.setStatus(UserStatus.PENDING_VERIFICATION);
            u.setEmailVerified(false);
            u.setPasswordHash(null);                  // invariant TDS §6.3
            u.setTermsAcceptedAt(null);
        });
    }

    /** FX-U-INACTIVE — U-0016 Vũ Đình Phong. */
    public static UserAccount inactive() {
        return aUser(u -> {
            u.setCode("U-0016");
            u.setName("Vũ Đình Phong");
            u.setEmail("phongvd.parent@gmail.com");
            u.setRole(role("ROLE-06", "Parent", RoleKey.PARENT, true));
            u.setSchoolId(null);
            u.setStatus(UserStatus.INACTIVE);
        });
    }

    /** FX-U-SUSPENDED — U-0007 Đặng Hải Yến, đối tác SUP-004 bị đình chỉ. */
    public static UserAccount suspended() {
        return aUser(u -> {
            u.setCode("U-0007");
            u.setName("Đặng Hải Yến");
            u.setEmail("yendh@daiduong.vn");
            u.setRole(role("ROLE-07", "Raw Food Supplier", RoleKey.SUPPLIER, true));
            u.setSchoolId(null);
            u.setPartnerId(SUP_004);
            u.setStatus(UserStatus.SUSPENDED);
        });
    }

    /** FX-U-TEACHER — U-0004 Nguyễn Thu Trang (cũng là STF-001 trên resource /staff). */
    public static UserAccount teacher() {
        return aUser(u -> {
            u.setCode("U-0004");
            u.setStaffCode("STF-001");
            u.setName("Nguyễn Thu Trang");
            u.setEmail("trangnt@dtd.edu.vn");
            u.setRole(roleTeacher());
        });
    }

    /** FX-U-GOOGLE-LINKED — U-0009 Phan Thị Hồng, đã liên kết Google. */
    public static UserAccount googleLinked() {
        return aUser(u -> {
            u.setCode("U-0009");
            u.setStaffCode("STF-002");
            u.setName("Phan Thị Hồng");
            u.setEmail("hongpt@dtd.edu.vn");
            u.setRole(roleTeacher());
            u.setGoogleSub("google-sub-0009");
        });
    }

    /** FX-U-SUPPLIER — U-0006 Lý Thái Sơn, partnerId=SUP-001, schoolId=NULL. */
    public static UserAccount supplier() {
        return aUser(u -> {
            u.setCode("U-0006");
            u.setName("Lý Thái Sơn");
            u.setEmail("sonlt@greenfarm.vn");
            u.setRole(role("ROLE-07", "Raw Food Supplier", RoleKey.SUPPLIER, true));
            u.setSchoolId(null);
            u.setPartnerId(SUP_001);
        });
    }

    /** FX-U-SYSADMIN — U-9001 (fixture MỚI, không có trong mock — TestSpec §2 L10). */
    public static UserAccount systemAdmin() {
        return aUser(u -> {
            u.setCode("U-9001");
            u.setName("SFMS System Admin");
            u.setEmail("sysadmin@sfms.vn");
            u.setRole(roleSystemAdmin());
            u.setSchoolId(null);
        });
    }

    /** FX-U-SYSADMIN-PRIMARY — U-9000, cờ primary cho UC-003 exception. */
    public static UserAccount primarySystemAdmin() {
        return aUser(u -> {
            u.setCode("U-9000");
            u.setName("SFMS Root");
            u.setEmail("root@sfms.vn");
            u.setRole(roleSystemAdmin());
            u.setSchoolId(null);
            u.setPrimaryAdmin(true);
        });
    }
}
