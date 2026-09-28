package com.sfms.mf08.domain;

/**
 * BR-111 (sửa 26/07/2026: 72h → <b>24h</b>) — hằng số duy nhất cho ngưỡng lưu tối thiểu.
 * KHÔNG hardcode {@code 24}/{@code 72} rải rác (CF-M08-1). {@code retentionDeadline} chốt
 * lúc đăng ký (ADR-M08-3) — nếu ngưỡng đổi tiếp, hồ sơ CŨ vẫn giữ deadline snapshot của nó.
 */
public final class RetentionPolicy {

    public static final long MIN_RETENTION_HOURS = 24;

    private RetentionPolicy() {
    }
}
