package com.sfms.mf10.port;

import com.sfms.common.code.CodeGenerator;
import com.sfms.mf10.domain.NotificationAudience;
import com.sfms.mf10.domain.UserNotification;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.repository.NotificationRepository;
import com.sfms.mf10.service.NotificationBroadcaster;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;

/**
 * ADR-M10-4 — mọi module (kể cả chính MF10) tạo thông báo qua đây, KHÔNG
 * {@code notificationRepository.save()} trực tiếp.
 *
 * <p>⚠️ Simplification MVP (ghi nợ TDS §2.5, cùng tinh thần {@code M07-D08}): BR-158 (quiet
 * hours) / BR-158b (cấu hình cá nhân) / BR-159 (retry + email fallback) là hành vi PER-USER,
 * cần danh sách user_accounts theo (school, role) mà MF10 KHÔNG sở hữu và MF01 chưa expose
 * qua {@code ReferenceResolver}/Port. Bản MVP này tạo đúng bản ghi {@code notifications}
 * (nguồn sự thật cho UC-058/136/chuông) — pipeline {@code notification_deliveries} (bảng đã
 * có ở V18) để nối khi MF01 có danh sách user theo vai.
 */
@Component
public class NotificationPortImpl implements NotificationPort {

    private final NotificationRepository notificationRepository;
    private final CodeGenerator codeGenerator;
    private final NotificationBroadcaster broadcaster;

    public NotificationPortImpl(NotificationRepository notificationRepository, CodeGenerator codeGenerator,
                                 NotificationBroadcaster broadcaster) {
        this.notificationRepository = notificationRepository;
        this.codeGenerator = codeGenerator;
        this.broadcaster = broadcaster;
    }

    @Override
    @Transactional
    public String publish(NotificationCommand command) {
        UserNotification n = new UserNotification();
        String prefix = "NTF-" + Year.now().getValue();
        n.setCode(codeGenerator.next("mf10_notification:" + Year.now().getValue(), prefix, 6));
        n.setSchoolId(command.schoolId());
        n.setAudience(command.toAllRoles() ? NotificationAudience.ALL : NotificationAudience.ROLES);
        n.setRecipientRoles(command.toAllRoles() ? null : command.recipientRoles());
        n.setType(command.type());
        n.setTitle(command.title());
        n.setContent(command.content());
        n.setLink(command.link());
        n.setLinkLabel(command.linkLabel());
        n.setSentAt(Instant.now());
        notificationRepository.saveAndFlush(n);
        broadcaster.broadcast(n);
        return n.getCode();
    }
}
