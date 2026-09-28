"use client";

/**
 * NHÃN TRẠNG THÁI XÁC NHẬN CỦA ĐƠN VỊ SUẤT ĂN — phiên kiểm tra an toàn bếp (UC-014).
 *
 * Tách ra dùng chung theo `review_medicalstaff_compliance_kitcheninspection.md` Mục 19.4
 * (27/07/2026): route Detail mới phải hiển thị tình trạng xác nhận **bằng đúng bộ nhãn**
 * mà cột "Xác nhận của bếp" ở màn danh sách đang dùng — *"không tạo nhãn thứ hai"*.
 *
 * Hai dạng hiển thị cùng suy ra từ MỘT bảng ánh xạ, nên không thể lệch nhau:
 *  · `KitchenConfirmBadge` — badge ngắn cho cột bảng;
 *  · `kitchenConfirmSentence` — câu đầy đủ cho khối trạng thái ở đầu route Detail.
 *
 * Nhánh thứ ba ("Phiên cũ") dành cho bản ghi tạo TRƯỚC khi có cơ chế xác nhận (Mục 8.4) —
 * `confirmStatus` không tồn tại. Từ 27/07/2026 mọi phiên mới đều có giá trị này (xem bug
 * fix ở `mock-server.ts`, Mục 19.4), nên nhánh đó chỉ còn phục vụ dữ liệu lịch sử.
 */

import { Badge } from "@/components/ui/badge";
import type { KitchenInspection } from "@/lib/mock-data";

type ConfirmState = "CONFIRMED" | "AWAITING" | "LEGACY";

function stateOf(k: Pick<KitchenInspection, "confirmStatus">): ConfirmState {
  if (k.confirmStatus === "CONFIRMED") return "CONFIRMED";
  if (k.confirmStatus === "AWAITING_CATERERS_CONFIRM") return "AWAITING";
  return "LEGACY";
}

const SHORT: Record<ConfirmState, { label: string; variant: "success" | "warning" | "neutral" }> = {
  CONFIRMED: { label: "Caterers đã xác nhận", variant: "success" },
  AWAITING: { label: "Chờ Caterers xác nhận", variant: "warning" },
  LEGACY: { label: "Phiên cũ (trước cơ chế xác nhận)", variant: "neutral" },
};

const SENTENCE: Record<ConfirmState, string> = {
  CONFIRMED: "Service Caterers đã xác nhận kết quả",
  AWAITING: "Đã hoàn thành — đang chờ Service Caterers xác nhận",
  LEGACY: "Phiên cũ — tạo trước khi có cơ chế xác nhận của đơn vị suất ăn",
};

export function KitchenConfirmBadge({ inspection }: { inspection: Pick<KitchenInspection, "confirmStatus"> }) {
  const s = SHORT[stateOf(inspection)];
  return <Badge variant={s.variant}>{s.label}</Badge>;
}

/** Câu đầy đủ cho khối trạng thái ở đầu route Detail (Mục 19.4). */
export function kitchenConfirmSentence(k: Pick<KitchenInspection, "confirmStatus">): string {
  return SENTENCE[stateOf(k)];
}

/** Tông màu khối trạng thái — dùng chung thang màu với badge để không lệch tín hiệu. */
export function kitchenConfirmTone(k: Pick<KitchenInspection, "confirmStatus">): "success" | "warning" | "neutral" {
  return SHORT[stateOf(k)].variant;
}
