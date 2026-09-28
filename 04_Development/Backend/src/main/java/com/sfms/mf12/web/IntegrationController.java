package com.sfms.mf12.web;

import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf12.dto.IntegrationDtos.ConnectionTestResult;
import com.sfms.mf12.dto.IntegrationDtos.IntegrationResponse;
import com.sfms.mf12.dto.IntegrationDtos.SaveIntegrationRequest;
import com.sfms.mf12.dto.IntegrationDtos.TestConnectionRequest;
import com.sfms.mf12.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** MF12 — {@code integrations} (UC-040/UC-149). BR-163/BR-164. */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @Operation(summary = "Danh sách dịch vụ tích hợp", description = "UC-040 · credential luôn masked (BR-164)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping
    public List<IntegrationResponse> list() {
        return integrationService.list();
    }

    @Operation(summary = "Kiểm tra kết nối", description = "BR-163 — ADR-000 stub, timeout mô phỏng 5s")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping("/{code}/test-connection")
    public ConnectionTestResult testConnection(@PathVariable String code,
                                               @RequestBody(required = false) TestConnectionRequest request,
                                               @CurrentUser AuthUser actor, HttpServletRequest servletRequest) {
        return integrationService.testConnection(code, request, actor.email(), clientIp(servletRequest));
    }

    @Operation(summary = "Lưu cấu hình tích hợp", description = "BR-163 (bắt buộc test PASS ≤10 phút) · "
            + "BR-164 (mã hoá envelope). `connected`/`lastTestedAt`/`lastTestPassed` là field DẪN XUẤT — client "
            + "gửi lên bị bỏ qua (D12)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PutMapping("/{code}")
    public IntegrationResponse save(@PathVariable String code, @Valid @RequestBody SaveIntegrationRequest request,
                                    @CurrentUser AuthUser actor, HttpServletRequest servletRequest) {
        return integrationService.save(code, request, actor.email(), clientIp(servletRequest));
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
