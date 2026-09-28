package com.sfms.mf12.web;

import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf12.dto.SecurityPolicyDtos.SecurityPolicyResponse;
import com.sfms.mf12.dto.SecurityPolicyDtos.UpdateSecurityPolicyRequest;
import com.sfms.mf12.service.SecurityPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** MF12 — {@code security-policies} (UC-184). Bảng+seed thuộc MF01 (CF-6), MF12 sở hữu endpoint. */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/security-policies")
public class SecurityPolicyController {

    private final SecurityPolicyService securityPolicyService;

    public SecurityPolicyController(SecurityPolicyService securityPolicyService) {
        this.securityPolicyService = securityPolicyService;
    }

    @Operation(summary = "Chính sách bảo mật hiện hành", description = "UC-184")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping
    public SecurityPolicyResponse get() {
        return securityPolicyService.get();
    }

    @Operation(summary = "Cập nhật chính sách bảo mật", description = "UC-184 bước 4 — MSG-029; "
            + "IAM-184 khi ngoài khoảng cho phép")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PutMapping("/{code}")
    public SecurityPolicyResponse update(@PathVariable String code, @RequestBody UpdateSecurityPolicyRequest request,
                                         @CurrentUser AuthUser actor, HttpServletRequest servletRequest) {
        return securityPolicyService.update(request, actor.email(), clientIp(servletRequest));
    }

    /** BR-170 — audit ghi IP thật. Ưu tiên {@code X-Forwarded-For} khi chạy sau Cloudflare. */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
