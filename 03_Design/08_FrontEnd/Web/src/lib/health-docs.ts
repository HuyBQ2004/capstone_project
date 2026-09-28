/**
 * Helper dùng chung cho cụm màn "Hồ sơ Sức khỏe & Dị ứng" — tách ra 26/07/2026 theo
 * `review/medical_staff/review_medicalstaff_healthrecord.md` Mục 4:
 *
 * 1. `openDocPreview` (Mục 4.3 điểm 2): thay toàn bộ `toast.info("(mock) …")` bằng mở
 *    TÀI LIỆU THẬT ở tab riêng (mock: dựng trang xem tài liệu có tên file + khung nội
 *    dung) — áp dụng đồng loạt cho 3+1 điểm: duyệt Dị ứng, duyệt Bệnh lý nền, Hồ sơ
 *    chi tiết (UC-051/094), Yêu cầu bữa ăn đặc biệt (UC-078).
 *
 * 2. `latestApprovedMeal` (Mục 4.6 điểm 4): `SpecialMealRequest` là NGUỒN DUY NHẤT cho
 *    "chế độ ăn đặc biệt" — lấy bản ghi ĐÃ DUYỆT gần nhất theo học sinh, thay cho field
 *    rời `Student.specialDiet` (đã deprecated, chỉ còn các actor khác đọc tạm cho tới
 *    lượt sweep riêng của họ).
 */

import type { SpecialMealRequest } from "@/lib/mock-data";

/** Mở tài liệu y tế ở tab riêng — mock viewer, không còn toast giả (Mục 4.3 điểm 2) */
export function openDocPreview(fileName: string) {
  const w = window.open("", "_blank", "width=860,height=1000");
  if (!w) return;
  const esc = fileName.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  w.document.write(`<!DOCTYPE html><html lang="vi"><head><meta charset="utf-8"><title>${esc}</title>
<style>body{font-family:system-ui,sans-serif;margin:0;background:#525659}
.bar{background:#323639;color:#fff;padding:10px 16px;font-size:14px;display:flex;justify-content:space-between}
.page{background:#fff;max-width:720px;min-height:960px;margin:24px auto;padding:48px;box-shadow:0 2px 12px rgba(0,0,0,.4)}
.ph{border:2px dashed #cbd5e1;border-radius:8px;padding:120px 24px;text-align:center;color:#64748b;margin-top:32px}</style></head>
<body><div class="bar"><span>${esc}</span><span>Trình xem tài liệu (mock — bản thật đọc file từ kho lưu trữ)</span></div>
<div class="page"><h2 style="margin-top:0">${esc}</h2>
<p style="color:#475569">Tài liệu y tế do phụ huynh đính kèm khi khai báo — nội dung mẫu phục vụ prototype.</p>
<div class="ph">Nội dung tài liệu (ảnh/PDF scan)</div></div></body></html>`);
  w.document.close();
}

/** dd/MM/yyyy → khoá sort yyyyMMdd (ngày kiểu VN không sort được bằng localeCompare thô) */
function dateKey(d: string): string {
  const m = d.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
  return m ? `${m[3]}${m[2]}${m[1]}` : d;
}

/** Bản ghi yêu cầu bữa ăn đặc biệt ĐÃ DUYỆT gần nhất của 1 học sinh (Mục 4.6 điểm 4) */
export function latestApprovedMeal(
  requests: SpecialMealRequest[] | undefined,
  studentId: string
): SpecialMealRequest | undefined {
  return (requests ?? [])
    .filter((r) => r.studentId === studentId && r.status === "APPROVED")
    .sort((a, b) => dateKey(b.date).localeCompare(dateKey(a.date)))[0];
}
