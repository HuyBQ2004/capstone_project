"use client";

/**
 * Ghi sổ SỰ KIỆN SỰ CỐ — append-only.
 *
 * Dòng thời gian điều tra từng đi qua hai đời sai:
 *   1. Mảng hardcode diễn biến của riêng INC-2026-014, hiện y hệt cho mọi sự cố.
 *   2. Dẫn xuất từ các cột trạng thái của `Incident` — đúng theo từng sự cố, nhưng
 *      **mất mốc thời gian** vì mô hình chỉ lưu trạng thái cuối, không lưu lúc nào
 *      chuyển, nên nhiều dòng phải hiển thị "—".
 *
 * Bảng sự kiện riêng là cách duy nhất giữ được cả hai: đúng sự cố VÀ đúng thời điểm.
 * Gọi hàm này ngay tại lúc hành động xảy ra, không dựng lại về sau.
 */

import { useCreate } from "@/lib/api/hooks";
import type { IncidentEvent } from "@/lib/mock-data";

export function useAppendIncidentEvent() {
  const create = useCreate<Partial<IncidentEvent>>("incident-events");
  return (e: {
    incidentId: string;
    type: IncidentEvent["type"];
    description: string;
    actor: string;
  }) =>
    create.mutate({
      id: `IEV-${String(Date.now()).slice(-6)}`,
      at: new Date().toLocaleString("vi-VN"),
      ...e,
    });
}
