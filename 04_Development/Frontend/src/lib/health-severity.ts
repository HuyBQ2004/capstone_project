/**
 * Mức độ dị ứng/bệnh lý nền — D6 `AllergySeverity` (`common/enums/AllergySeverity.java`):
 * `MILD` | `MODERATE` | `SEVERE`. Dùng chung cho `allergy-declarations` VÀ
 * `chronic-conditions` (hai module dùng CHUNG enum này ở backend — `ChronicConditionDtos`
 * import thẳng `com.sfms.common.enums.AllergySeverity`, không có enum riêng).
 *
 * Trước 04/08/2026, 4 màn Parent/Medical Staff khai severity bằng nhãn tiếng Việt
 * (`"Nhẹ"|"Trung bình"|"Nặng"`) làm GIÁ TRỊ LƯU TRỮ — hoạt động được với mock vì mock
 * không có "backend thật" để lệch, nhưng gửi thẳng lên `POST /allergy-declarations`/
 * `POST /chronic-conditions` sẽ bị Jackson từ chối (400, enum không khớp giá trị).
 * File này là ĐIỂM DUY NHẤT ánh xạ enum ↔ nhãn hiển thị — sửa nhãn thì sửa ở đây,
 * không rải rác từng màn.
 */

import type { Api } from "@/lib/api/types";

export type ApiSeverity = Api<"AllergySeverity">;

export const SEVERITY_LABEL_VI: Record<ApiSeverity, string> = {
  MILD: "Nhẹ",
  MODERATE: "Trung bình",
  SEVERE: "Nặng",
};

export const SEVERITY_BADGE_VARIANT: Record<ApiSeverity, "danger" | "warning" | "info"> = {
  SEVERE: "danger",
  MODERATE: "warning",
  MILD: "info",
};

/** Thứ tự tăng dần theo mức nghiêm trọng — dùng cho radio group form khai báo. */
export const SEVERITY_OPTIONS: ApiSeverity[] = ["MILD", "MODERATE", "SEVERE"];
