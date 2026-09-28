package com.sfms.mf09.domain;

/**
 * Trạng thái phiên phát suất (ADR-M09-3) — module-local, không thuộc 8 enum dùng chung của D6.
 * Transition {@code PENDING -> SERVED} một chiều trong luồng bình thường; {@code /revoke} là
 * ngoại lệ có kiểm soát (chỉ GVCN, trong ngày, có audit — ADR-M09-3).
 */
public enum DistributionStatus {
    PENDING,
    SERVED
}
