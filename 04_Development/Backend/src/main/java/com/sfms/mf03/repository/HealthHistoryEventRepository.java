package com.sfms.mf03.repository;

import com.sfms.mf03.domain.HealthHistoryEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * BR-172/ADR-M03-4 — KHÔNG khai {@code delete*}/{@code update*}. Repository chỉ hỗ trợ
 * {@code save} (insert) và đọc; xoá/sửa bị chặn ở DB (RULE + REVOKE), không phải vì thiếu
 * method ở đây.
 */
public interface HealthHistoryEventRepository extends JpaRepository<HealthHistoryEvent, UUID> {

    Page<HealthHistoryEvent> findByStudentIdOrderByAtDesc(UUID studentId, Pageable pageable);

    long countByStudentIdAndType(UUID studentId, com.sfms.common.enums.HealthHistoryEventType type);
}
