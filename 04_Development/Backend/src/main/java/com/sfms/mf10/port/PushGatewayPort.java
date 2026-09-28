package com.sfms.mf10.port;

import java.util.List;
import java.util.Map;

/**
 * Cổng gửi push notification (FCM) — MF10 khai (module tiêu thụ), MF12 implement (module sở
 * hữu credential dịch vụ ngoài, {@code external_integrations}), cùng pattern với
 * {@code com.sfms.mf05.port.AiAssistPort} (MF05 khai, MF12 implement qua {@code GeminiAiAssistAdapter}).
 */
public interface PushGatewayPort {

    /**
     * @return danh sách token trong {@code tokens} mà FCM báo không còn hợp lệ (app đã gỡ/token
     *         hết hạn) — caller (MF10) tự xoá khỏi {@code device_tokens}, port này không biết
     *         schema của MF10.
     */
    List<String> sendToTokens(List<String> tokens, String title, String body, Map<String, String> data);
}
