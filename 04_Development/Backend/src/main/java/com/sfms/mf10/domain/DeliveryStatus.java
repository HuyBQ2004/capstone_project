package com.sfms.mf10.domain;

/** BR-159 — trạng thái một lượt gửi (retry ≤3 lần rồi mới {@code FALLBACK_EMAIL}). */
public enum DeliveryStatus {
    QUEUED,
    DEFERRED,
    SENT,
    FAILED,
    FALLBACK_EMAIL
}
