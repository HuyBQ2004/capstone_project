package com.sfms.mf10.service;

import com.sfms.mf10.domain.UserNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Đẩy thông báo mới qua WebSocket/STOMP ngay khi tạo (bổ sung UC-058/136) — trước đây FE chỉ
 * biết có thông báo mới bằng polling `GET /notifications`.
 *
 * <p>⚠️ Giới hạn đã biết: topic đặt tên theo {@code schoolId} thô, KHÔNG có ACL ở tầng broker
 * chặn subscribe sai trường — ai có JWT hợp lệ (bất kỳ trường nào) về lý thuyết có thể tự ghép
 * đúng topic của trường khác nếu đoán được UUID. Chấp nhận được ở mức MVP vì {@code schoolId}
 * là UUID ngẫu nhiên (không đoán được), nhưng KHÔNG phải kiểm soát truy cập chặt — ghi rõ để
 * không lặp lại giả định "topic riêng = an toàn" khi cần siết thêm (VD `@SubscribeMapping` có
 * kiểm tra Principal, hoặc user-destination `/user/queue/...` theo đúng {@code userId}).
 */
@Component
public class NotificationBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(UserNotification n) {
        if (n.getSchoolId() == null) {
            return;
        }
        String destination = "/topic/notifications/" + n.getSchoolId();
        messagingTemplate.convertAndSend(destination, payload(n));
    }

    private static Payload payload(UserNotification n) {
        List<String> roles = n.getRecipientRoles() == null ? List.of() : n.getRecipientRoles();
        return new Payload(n.getCode(), n.getAudience().name(), roles, n.getType().name(), n.getTitle(),
                n.getContent(), n.getSentAt().toString());
    }

    private record Payload(String id, String audience, List<String> recipientRoles, String type, String title,
                            String content, String sentAt) {}
}
