package com.sfms.mf12.web;

import com.sfms.mf12.dto.SecurityPolicyDtos.PublicPasswordPolicyResponse;
import com.sfms.mf12.service.SecurityPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MF12 — {@code GET /public/password-policy} (CF-24, 🆕 §9.2 hàng 12). Public — KHÔNG JWT.
 * Thay 2 màn {@code (auth)} đang phải đọc {@code /security-policies} (endpoint quản trị)
 * trước khi có JWT. Chỉ trả tập con phục vụ validate mật khẩu, KHÔNG lộ ngưỡng khoá tài khoản
 * (BR-002 — {@code maxLoginAttempts}/{@code lockoutMinutes} không có trong response này).
 */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/public/password-policy")
public class PublicPasswordPolicyController {

    private final SecurityPolicyService securityPolicyService;

    public PublicPasswordPolicyController(SecurityPolicyService securityPolicyService) {
        this.securityPolicyService = securityPolicyService;
    }

    @Operation(summary = "Chính sách mật khẩu công khai", description = "BR-006/BR-002 · Public, không cần JWT")
    @GetMapping
    public PublicPasswordPolicyResponse get() {
        return securityPolicyService.getPublicPasswordPolicy();
    }
}
