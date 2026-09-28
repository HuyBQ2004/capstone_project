package com.sfms.mf06.domain;

/**
 * BR-204 — mã lý do điều chỉnh tồn thủ công (danh sách định sẵn).
 * {@code EXTRA_USAGE} (13/08/2026, theo Nam) — nấu thực tế dùng nhiều nguyên liệu hơn định
 * mức hệ thống tính (hao hụt sơ chế, phát sinh suất ăn...), migration V73.
 */
public enum ReasonCode {
    SPOILAGE, COUNT_CORRECTION, DAMAGE, RETURN_TO_SUPPLIER, EXTRA_USAGE
}
