package com.sfms.mf10.port;

import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;

/**
 * Cửa DUY NHẤT để mọi module phát thông báo (ADR-M10-4). M10-D06: port DUY NHẤT MF10 expose
 * — HÀNH VI (áp BR-138/158/158b/159 tập trung), không phải tra cứu. Bốn module chờ nó: MF05,
 * MF06, MF07, MF11 (+ MF12 §9.3.9 đã ghi rõ "gọi qua interface").
 */
public interface NotificationPort {

    /** @return {@code code} của thông báo đã tạo (D1 — KHÔNG trả UUID ra ngoài module). */
    String publish(NotificationCommand command);
}
