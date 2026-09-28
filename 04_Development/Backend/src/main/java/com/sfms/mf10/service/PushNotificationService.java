package com.sfms.mf10.service;

import java.util.Map;
import java.util.UUID;

/**
 * Gửi push FCM tới TẤT CẢ thiết bị đã đăng ký của một user cụ thể (V34 {@code device_tokens}).
 *
 * <p>⚠️ Giới hạn đã biết (cùng gap ghi ở {@code NotificationPortImpl}): chỉ dùng được khi caller
 * đã biết {@code userId} đích cụ thể — {@code NotificationPort.publish()} (broadcast theo
 * school+role) CHƯA gọi service này, vì MF10 không sở hữu danh sách user theo (school, role) để
 * suy ra ai cần nhận (chờ MF01 expose qua {@code ReferenceResolver}).
 */
public interface PushNotificationService {

    void sendToUser(UUID userId, String title, String body, Map<String, String> data);
}
