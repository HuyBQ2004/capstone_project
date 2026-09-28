package com.sfms.mf10.service;

import com.sfms.mf10.domain.DeviceToken;
import com.sfms.mf10.port.PushGatewayPort;
import com.sfms.mf10.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushGatewayPort pushGatewayPort;

    public PushNotificationServiceImpl(DeviceTokenRepository deviceTokenRepository,
                                        PushGatewayPort pushGatewayPort) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.pushGatewayPort = pushGatewayPort;
    }

    @Override
    @Transactional
    public void sendToUser(UUID userId, String title, String body, Map<String, String> data) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findByUserId(userId);
        if (deviceTokens.isEmpty()) {
            return;
        }
        List<String> tokens = deviceTokens.stream().map(DeviceToken::getToken).toList();
        List<String> invalidTokens = pushGatewayPort.sendToTokens(tokens, title, body, data);
        invalidTokens.forEach(deviceTokenRepository::deleteByToken);
    }
}
