package com.sfms.mf03.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.HealthHistoryDtos.HealthHistoryEventResponse;

import java.util.UUID;

/**
 * UC-146, BR-172 (ADR-M03-4) — append-only. {@link #append} là <b>service nội bộ</b>, KHÔNG
 * expose endpoint ghi công khai; mọi hành động nghiệp vụ (verify/approve/clinical-update) gọi
 * nó trong CÙNG transaction (C5).
 *
 * @version 1.1 — {@link #timeline} nhận {@code studentCode} (D1: mã nghiệp vụ) thay vì UUID nội
 *         bộ (2026-08-02): FE không bao giờ có UUID PK của Student (D1 không lộ UUID ra API),
 *         nên tham số cũ khiến endpoint không thể gọi được từ FE — lệch D1, không phải quyết
 *         định nghiệp vụ.
 */
public interface HealthHistoryService {

    PagedResponse<HealthHistoryEventResponse> timeline(String studentCode, ListQuery query, AuthUser actor);

    void append(UUID studentId, HealthHistoryEventType type, String event, UUID byUserId);
}
