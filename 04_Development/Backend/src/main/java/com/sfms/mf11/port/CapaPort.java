package com.sfms.mf11.port;

import java.util.UUID;

/**
 * BR-095 — MF07 gọi khi một bước kiểm thực TRƯỢT, cùng transaction ghi bước trượt
 * ({@code M07-D08}). TDS-MF11_Backend.md §8.4.
 *
 * <p>Đơn giản hoá có chủ đích: <b>1 phiên kiểm thực = tối đa 1 CAPA tự động</b> (không phải
 * 1/bước) — nhiều bước trượt trong cùng phiên chỉ tạo 1 CAPA, gọi lại là idempotent (trả về
 * mã cũ). {@code corrective_actions} không có cột {@code step} riêng để phân biệt.
 */
public interface CapaPort {

    /** @return {@code code} của CAPA (D1 — KHÔNG trả UUID ra ngoài module). Đã tồn tại ⇒ trả mã cũ. */
    String openFromFailedStep(UUID inspectionSessionId, String step, String description,
                               UUID schoolId, UUID actorId);
}
