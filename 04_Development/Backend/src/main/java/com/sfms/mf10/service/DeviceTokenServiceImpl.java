package com.sfms.mf10.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.DevicePlatform;
import com.sfms.mf10.domain.DeviceToken;
import com.sfms.mf10.dto.NotificationDtos.RegisterDeviceTokenRequest;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository repository;

    public DeviceTokenServiceImpl(DeviceTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void register(RegisterDeviceTokenRequest request, AuthUser actor) {
        DevicePlatform platform = parsePlatform(request.platform());

        // Upsert theo token — 1 thiết bị/browser đăng nhập account khác thì gắn lại user_id mới
        // (đăng xuất không xoá token, chỉ ghi đè khi có người khác đăng nhập cùng thiết bị).
        DeviceToken deviceToken = repository.findByToken(request.token()).orElseGet(DeviceToken::new);
        deviceToken.setToken(request.token());
        deviceToken.setUserId(actor.userId());
        deviceToken.setPlatform(platform);
        deviceToken.setLastUsedAt(Instant.now());
        repository.saveAndFlush(deviceToken);
    }

    @Override
    @Transactional
    public void unregister(String token, AuthUser actor) {
        DeviceToken deviceToken = repository.findByToken(token).orElse(null);
        if (deviceToken == null) {
            return;   // idempotent
        }
        if (!deviceToken.getUserId().equals(actor.userId())) {
            throw FbaExceptions.accessDenied();
        }
        repository.deleteByToken(token);
    }

    private static DevicePlatform parsePlatform(String raw) {
        try {
            return DevicePlatform.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw FbaExceptions.enumInvalid("platform", "must be ANDROID|IOS|WEB");
        }
    }
}
