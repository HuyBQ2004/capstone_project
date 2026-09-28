"use client";

/**
 * Ghi sổ LỊCH SỬ SỨC KHỎE học sinh — append-only (BR-172).
 *
 * `health-history` được thiết kế là sổ chỉ-thêm: mọi thay đổi hồ sơ y tế của học
 * sinh phải để lại vết, không sửa/xoá. Nhưng đến 21/07/2026 không mốc nghiệp vụ
 * nào ghi vào đó — màn lịch sử của Y tế chỉ đọc mấy dòng seed, nên đúng những
 * hành động cần vết nhất (xác minh hồ sơ dị ứng, duyệt chế độ ăn) lại không có vết.
 *
 * Dùng hàm này tại thời điểm hành động xảy ra, KHÔNG dựng lại từ trạng thái hiện
 * tại — dựng lại thì mất mốc thời gian và mất người thực hiện.
 */

import { useCreate } from "@/lib/api/hooks";
import type { HealthHistoryEvent } from "@/lib/mock-data";

export function useAppendHealthHistory() {
  const create = useCreate<Partial<HealthHistoryEvent>>("health-history");
  return (e: {
    studentId: string;
    type: HealthHistoryEvent["type"];
    event: string;
    by: string;
  }) =>
    create.mutate({
      id: `HH-${String(Date.now()).slice(-6)}`,
      at: new Date().toLocaleString("vi-VN"),
      ...e,
    });
}
