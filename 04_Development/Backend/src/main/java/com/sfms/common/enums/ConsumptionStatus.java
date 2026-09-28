package com.sfms.common.enums;

/**
 * D6 — trạng thái tiêu thụ suất ăn (MF09).
 *
 * <p>Nhãn FE: Chưa ghi nhận / Ăn hết / Ăn một phần / Không ăn.
 *
 * <p>⚠️ MF09 §9.2 message {@code SRV-001} từng ghi <i>"must be one of FULLY, PARTIALLY,
 * NOT_CONSUMED"</i> — <b>bỏ sót {@code PENDING}</b>, vốn đúng là trạng thái khởi tạo.
 * D6 chốt 4 giá trị.
 */
public enum ConsumptionStatus {
    PENDING,
    FULLY,
    PARTIALLY,
    NOT_CONSUMED
}
