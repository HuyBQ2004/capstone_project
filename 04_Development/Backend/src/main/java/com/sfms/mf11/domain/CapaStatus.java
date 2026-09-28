package com.sfms.mf11.domain;

/** BR-216 — vòng đời 6 trạng thái, chuyển CHỈ qua 6 endpoint hành động (C6). */
public enum CapaStatus {
    DRAFT,
    PENDING_ADMIN_APPROVAL,
    APPROVED_FOR_IMPLEMENTATION,
    IN_PROGRESS,
    SUBMITTED_EVIDENCE,
    COMPLETED
}
