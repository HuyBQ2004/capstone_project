package com.sfms.mf05.domain;

/**
 * Vòng đời thực đơn (BR-068, ADR-M05-04). KHÔNG thuộc 8 enum dùng chung của D6 (M05-D03)
 * ⇒ giữ trong module {@code com.sfms.mf05.domain}, giá trị SCREAMING_SNAKE.
 */
public enum MenuStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    PUBLISHED,
    REJECTED
}
