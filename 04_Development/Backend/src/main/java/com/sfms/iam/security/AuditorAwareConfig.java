package com.sfms.iam.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Nguồn actor cho {@code @CreatedBy}/{@code @LastModifiedBy} của
 * {@code common/domain/BaseEntity} — entity con của bất kỳ module nào KHÔNG cần
 * khai báo/gọi gì thêm để có {@code createdBy}/{@code updatedBy}.
 *
 * <p>Request có JWT hợp lệ ⇒ ghi {@code AuthUser.email()} (khớp quy ước hiện có của
 * {@code audit_logs.actor}, xem {@code AuditService}). Không có identity (job nền,
 * event listener, seed) ⇒ {@code "system"} — cùng literal đã dùng ở
 * {@code AccountCascadeHandler}/{@code UserService}.
 */
@Component("auditorAware")
public class AuditorAwareConfig implements AuditorAware<String> {

    private static final String SYSTEM_ACTOR = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthUser user)) {
            return Optional.of(SYSTEM_ACTOR);
        }
        return Optional.of(user.email());
    }
}
