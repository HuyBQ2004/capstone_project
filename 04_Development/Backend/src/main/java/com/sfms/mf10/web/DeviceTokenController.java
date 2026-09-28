package com.sfms.mf10.web;

import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf10.dto.NotificationDtos.RegisterDeviceTokenRequest;
import com.sfms.mf10.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** MF10 — {@code device-tokens} (V34). Đăng ký token FCM cho push notification (mobile + web). */
@Tag(name = "MF10 — Feedback & Announcement")
@RestController
@RequestMapping("/api/v1/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService service;

    public DeviceTokenController(DeviceTokenService service) {
        this.service = service;
    }

    @Operation(summary = "Đăng ký token FCM cho thiết bị/browser hiện tại",
            description = "Upsert theo token — gọi lại mỗi lần app/web khởi động hoặc token refresh")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@Valid @RequestBody RegisterDeviceTokenRequest request, @CurrentUser AuthUser actor) {
        service.register(request, actor);
    }

    @Operation(summary = "Gỡ token (đăng xuất khỏi thiết bị này)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @DeleteMapping("/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable String token, @CurrentUser AuthUser actor) {
        service.unregister(token, actor);
    }
}
