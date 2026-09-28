package com.sfms.mf08.domain;

/**
 * Vòng đời {@link FoodSample} (BR-111/112/113, ADR-M08-2).
 *
 * <p>{@code RETRIEVAL_REQUESTED}/{@code PENDING_LAB_DELIVERY} là 2 trạng thái trung gian của
 * quy trình BR-215 (Deprecated 26/07/2026 — CF-M08-2): giữ lại CHỈ để đọc dữ liệu di trú,
 * service KHÔNG BAO GIỜ sinh mới (M08 §17.4 AP-AI-012).
 */
public enum FoodSampleStatus {
    RETAINED,

    /**
     * Đang bị GIỮ để phục vụ điều tra một sự cố — mẫu vẫn nằm trong tủ lưu của trường,
     * CHƯA gửi đi xét nghiệm. Thêm 08/08/2026 (backlog B3, kế hoạch §2.3 — Nam chốt).
     *
     * <p>Vì sao cần một trạng thái riêng: ba trạng thái cũ không diễn đạt được tình huống này.
     * Ghi {@code SENT_TO_LAB} cho mẫu còn nằm trong tủ là NÓI DỐI hồ sơ (và vỡ
     * {@code ck_smp_sent_has_request} vì không có phiếu bàn giao), còn để {@code RETAINED} thì
     * bếp hủy được ngay khi qua mốc BR-111 24h — trùng đúng lúc cửa sổ điều tra BR-149 24h
     * mới bắt đầu.
     *
     * <p>Kèm theo trạng thái này, {@code retention_deadline} được GIA HẠN thêm một chu kỳ
     * BR-111 kể từ lúc giữ. Chốt chặn thật vẫn là {@code incidentId} + sự cố còn mở
     * ({@code FoodSampleServiceImpl.dispose()}); gia hạn deadline chỉ để CHECK ở DB
     * ({@code ck_smp_dispose_after_deadline}) nói cùng một câu chuyện với tầng service.
     */
    HOLD,

    SENT_TO_LAB,
    DISPOSED,

    /** @deprecated BR-215 Deprecated 26/07/2026 — chỉ đọc dữ liệu cũ, KHÔNG sinh mới (CF-M08-2). */
    @Deprecated
    RETRIEVAL_REQUESTED,

    /** @deprecated như trên. */
    @Deprecated
    PENDING_LAB_DELIVERY
}
