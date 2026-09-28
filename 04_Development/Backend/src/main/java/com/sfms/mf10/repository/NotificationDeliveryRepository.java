package com.sfms.mf10.repository;

import com.sfms.mf10.domain.DeliveryStatus;
import com.sfms.mf10.domain.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    List<NotificationDelivery> findByStatusInAndNextRetryAtBefore(
            Collection<DeliveryStatus> statuses, Instant now);   // BR-159 scheduler
}
