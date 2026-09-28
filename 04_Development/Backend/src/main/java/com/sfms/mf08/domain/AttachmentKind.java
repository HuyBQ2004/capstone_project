package com.sfms.mf08.domain;

/**
 * ADR-M08-4 — 4 loại file bằng chứng: {@code EVIDENCE_PHOTO} (BR-107, lúc đăng ký mẫu),
 * {@code SEAL_PHOTO} (niêm phong lúc bàn giao), {@code HANDOVER_RECORD} (biên bản giao nhận,
 * BR-212), {@code LAB_RECEIPT} (phiếu nhận của phòng kiểm nghiệm, BR-212).
 */
public enum AttachmentKind {
    EVIDENCE_PHOTO, SEAL_PHOTO, HANDOVER_RECORD, LAB_RECEIPT
}
