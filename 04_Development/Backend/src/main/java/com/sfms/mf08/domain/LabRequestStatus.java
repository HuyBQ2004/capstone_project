package com.sfms.mf08.domain;

/**
 * Vòng đời {@link SampleLabRequest} (ADR-M08-2). {@code SENT_TO_LAB} là trạng thái CUỐI —
 * phiếu ra đời sau khi Medical Staff đã bàn giao xong (BR-113 bản 26/07/2026), nên KHÔNG
 * có endpoint hủy phiếu cho bản ghi mới.
 */
public enum LabRequestStatus {
    SENT_TO_LAB,
    CANCELLED,

    /** @deprecated BR-215 Deprecated — chỉ đọc dữ liệu cũ (CF-M08-2). */
    @Deprecated
    RETRIEVAL_REQUESTED,

    /** @deprecated như trên. */
    @Deprecated
    PENDING_LAB_DELIVERY
}
