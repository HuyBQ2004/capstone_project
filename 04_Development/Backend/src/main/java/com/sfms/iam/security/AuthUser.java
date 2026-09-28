package com.sfms.iam.security;

import com.sfms.common.enums.RoleKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Identity của request — <b>dựng 100% từ claim của access token do backend phát hành</b>.
 *
 * <p>TDS §17 <b>C2</b>: <i>"KHÔNG tin {@code userId}/{@code role} từ body/query"</i>.
 * TDS §17.4 <b>AP-AI-006</b>: đọc role/userId từ body ⇒ reject.
 *
 * <p>Không có setter và không có đường nào để controller/service ghi đè. Mọi service nhận
 * đối tượng này thay vì nhận {@code userId} tham số từ request. MF01-TC-SEC-005 chứng minh
 * 7 đường tấn công qua body/query/header đều không đổi được identity.
 *
 * @param userId    UUID nội bộ (claim {@code uid}) — dùng cho truy vấn, không lộ ra API
 * @param code      mã nghiệp vụ công khai ({@code U-0001}) — D1
 * @param email     claim {@code sub}
 * @param role      claim {@code role}, kebab-case (D6)
 * @param schoolId  UUID trường; {@code null} với System Admin / tài khoản đối tác
 * @param partnerId UUID đối tác; khóa cách ly đa-tenant của MF04/MF06/MF08
 */
public record AuthUser(
        UUID userId,
        String code,
        String email,
        RoleKey role,
        UUID schoolId,
        UUID partnerId
) {

    public Collection<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    public boolean isSystemAdmin() {
        return role == RoleKey.SYSTEM_ADMIN;
    }

    public boolean isSchoolAdmin() {
        return role == RoleKey.SCHOOL_ADMIN;
    }

    /** BR-013 / MF01-TC-SEC-006 — cách ly theo trường của chính JWT, không theo param. */
    public boolean belongsToSchool(UUID target) {
        return schoolId != null && schoolId.equals(target);
    }
}
