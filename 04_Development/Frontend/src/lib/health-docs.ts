/**
 * Helper dùng chung cho cụm màn "Hồ sơ Sức khỏe & Dị ứng" — tách ra 26/07/2026 theo
 * `review/medical_staff/review_medicalstaff_healthrecord.md` Mục 4:
 *
 * 1. `openHealthDocument` (Mục 4.3 điểm 2): mở TÀI LIỆU THẬT do phụ huynh đính kèm —
 *    duyệt Dị ứng, duyệt Bệnh lý nền, Hồ sơ chi tiết (UC-051/094), Yêu cầu bữa ăn đặc
 *    biệt (UC-078).
 *
 * 2. `latestApprovedMeal` (Mục 4.6 điểm 4): `SpecialMealRequest` là NGUỒN DUY NHẤT cho
 *    "chế độ ăn đặc biệt" — lấy bản ghi ĐÃ DUYỆT gần nhất theo học sinh, thay cho field
 *    rời `Student.specialDiet` (đã deprecated, chỉ còn các actor khác đọc tạm cho tới
 *    lượt sweep riêng của họ).
 */

import { toast } from "sonner";
import { api } from "@/lib/api/client";
import type { SpecialMealRequestDto } from "@/lib/api/types";

/**
 * Mở tài liệu y tế THẬT ở tab riêng.
 *
 * Bản trước của hàm này (`openDocPreview`, 26/07/2026) chỉ nhận TÊN FILE và tự vẽ một
 * trang HTML "Nội dung tài liệu (ảnh/PDF scan)" — trình xem GIẢ, không hề đọc file. Lý do
 * lúc đó là backend chưa có đường tải xuống; nay đã có
 * `GET /students/{studentCode}/health-documents/{documentCode}/download` (12/08/2026).
 *
 * Tải qua `api` (axios) chứ không đặt thẳng URL vào `window.open`: tài liệu y tế là RIÊNG
 * TƯ, endpoint có `@PreAuthorize` và JWT nằm ở header do interceptor gắn — một tab mới mở
 * bằng URL trần sẽ không mang token và nhận 401. Cùng khuôn `openDocWithAuth` của màn hồ sơ
 * đối tác (`school-admin/partnerships/[id]`).
 */
export async function openHealthDocument(
  studentCode: string,
  documentCode: string,
  fileName?: string
) {
  try {
    const res = await api.get<Blob>(
      `/students/${encodeURIComponent(studentCode)}/health-documents/${encodeURIComponent(documentCode)}/download`,
      { responseType: "blob" }
    );
    const objUrl = URL.createObjectURL(res.data);
    window.open(objUrl, "_blank");
    // Thu hồi trễ: tab mới cần giữ blob sống đủ lâu để render xong PDF/ảnh.
    setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
  } catch {
    toast.error(`Không mở được tài liệu ${fileName ?? documentCode}. Vui lòng thử lại.`);
  }
}

/**
 * dd/MM/yyyy → khoá sort yyyyMMdd (ngày kiểu VN không sort được bằng localeCompare thô).
 * `SpecialMealRequestResponse.date` THẬT là ISO `yyyy-MM-dd` — đã sort đúng bằng so chuỗi,
 * nhánh regex chỉ còn cho dữ liệu kiểu VN nếu nơi gọi tự định dạng trước.
 */
function dateKey(d: string | undefined): string {
  if (!d) return "";
  const m = d.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
  return m ? `${m[3]}${m[2]}${m[1]}` : d;
}

/** Bản ghi yêu cầu bữa ăn đặc biệt ĐÃ DUYỆT gần nhất của 1 học sinh (Mục 4.6 điểm 4) */
export function latestApprovedMeal(
  requests: SpecialMealRequestDto[] | undefined,
  studentId: string
): SpecialMealRequestDto | undefined {
  return (requests ?? [])
    .filter((r) => r.studentId === studentId && r.status === "APPROVED")
    .sort((a, b) => dateKey(b.date).localeCompare(dateKey(a.date)))[0];
}
