"use client";

/**
 * Nguồn THẬT DUY NHẤT cho cấu hình vận hành cấp trường — `GET /schools/{code}/config`
 * (`SchoolConfigResponse`, 1 OBJECT, KHÔNG phân trang).
 *
 * Gồm: trần chi phí thực đơn (BR-080), bán kính geofence (BR-028), email nhận báo cáo
 * (BR-179), và **số ngày tối thiểu phải nộp duyệt thực đơn trước ngày phục vụ** (BR-061).
 *
 * ## Vì sao `menuLeadTimeDays` phải đọc từ server
 *
 * Backend đã có property `sfms.menu.lead-time-days` (env `MENU_LEAD_TIME_DAYS`, mặc định 3)
 * và `MenuServiceImpl.doSubmit()` chặn theo đúng nó. Nhưng trước 08/08/2026 FE hardcode
 * `minCreate.setDate(getDate() + 3)` ngay trong `menus/page.tsx`, nên:
 *   · đổi cấu hình server KHÔNG có tác dụng gì lên UI — ô lịch vẫn khoá 3 ngày;
 *   · nếu ai đó chỉnh property, hai bên lệch nhau âm thầm.
 * Đúng lớp lỗi "hằng số nhân đôi" đã bỏ ở `NUTRITION_TARGET`. Đừng khai lại số 3 ở FE.
 *
 * Đặt `MENU_LEAD_TIME_DAYS=0` khi chạy backend ⇒ tạo và nộp duyệt được thực đơn NGAY TRONG
 * NGÀY (dùng lúc demo/test, khỏi phải chèn dữ liệu thẳng vào DB).
 */

import { useSubDetail } from "@/lib/api/hooks";
import type { Api } from "@/lib/api/types";
import { useAuthStore } from "@/lib/stores/auth-store";

export type SchoolConfigDto = Api<"SchoolConfigResponse">;

/**
 * Cấu hình trường của phiên đang đăng nhập; `undefined` khi chưa tải xong.
 *
 * ⚠️ Đọc `activeSchoolId`, KHÔNG phải `schoolId` (sửa 08/08/2026). Tài khoản **Đơn vị suất ăn**
 * đăng ký qua hợp tác nên chỉ có `partnerId`, `schoolId` LUÔN null — đo thật: `GET /profile/me`
 * trả `{"role":"service-caterers","schoolId":null,"partnerId":"CAT-201"}`. Hệ quả trước khi sửa:
 * hook này không bao giờ chạy ⇒ `menuLeadTimeDays` mãi `undefined` ⇒ màn **Lịch thực đơn tuần**
 * khoá TOÀN BỘ ô (`minCreate = hôm nay + 36500 ngày`) ⇒ **đúng vai có nhiệm vụ lập thực đơn thì
 * không có nút thêm nào**. Backend nay tự suy trường từ hợp tác đang hiệu lực cho vai này.
 */
export function useSchoolConfig(): SchoolConfigDto | undefined {
  const schoolId = useAuthStore((s) => s.user?.activeSchoolId ?? s.user?.schoolId);
  const { data } = useSubDetail<SchoolConfigDto>("schools", schoolId, "config");
  return data;
}

/**
 * BR-061 — số ngày tối thiểu trước ngày phục vụ.
 *
 * Trả `undefined` khi cấu hình chưa về: nơi gọi phải coi như CHƯA BIẾT và tạm khoá, chứ
 * đừng đoán 3 — đoán lại chính là cái lỗi vừa bỏ.
 */
export function useMenuLeadTimeDays(): number | undefined {
  return useSchoolConfig()?.menuLeadTimeDays;
}
