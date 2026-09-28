package com.sfms.mf10.repository;

import com.sfms.mf10.domain.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<UserNotification, UUID> {

    Optional<UserNotification> findByCode(String code);

    /**
     * UC-136/058 — mọi thông báo hiển thị được với một trường (trường mình + toàn nền tảng).
     * ⚠️ Simplification MVP (ghi nợ TDS §2.5): lọc theo VAI (audience=ROLES) và phân trang
     * thực hiện ở tầng Java (NotificationService), KHÔNG đẩy xuống DB — JSONB
     * {@code recipient_roles} không có index phù hợp cho containment query qua Criteria API
     * mà không đổi kiểu cột. Khối lượng một trường (≤ vài nghìn thông báo) chấp nhận được ở MVP.
     */
    @Query("select n from UserNotification n where (n.schoolId = :schoolId or n.schoolId is null) "
            + "order by n.sentAt desc")
    List<UserNotification> findVisibleToSchool(@Param("schoolId") UUID schoolId);
}
