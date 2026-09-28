package com.sfms.mf10.repository;

import com.sfms.mf10.domain.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, UUID> {

    boolean existsByNotificationIdAndUserId(UUID notificationId, UUID userId);

    List<NotificationRead> findByUserIdAndNotificationIdIn(UUID userId, Set<UUID> notificationIds);
}
