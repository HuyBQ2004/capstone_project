package com.sfms.mf10.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.NotificationDtos.RegisterDeviceTokenRequest;

/** Đăng ký/gỡ token FCM cho push notification (mobile + web). */
public interface DeviceTokenService {

    void register(RegisterDeviceTokenRequest request, AuthUser actor);

    void unregister(String token, AuthUser actor);
}
