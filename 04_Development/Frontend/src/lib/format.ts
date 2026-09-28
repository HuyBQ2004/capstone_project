/**
 * Định dạng ngày/giờ ở TẦNG RENDER.
 *
 * Quy ước D4 (`07_implement/00_Common_Conventions.md`): JSON của API luôn dùng
 * ISO-8601 (`yyyy-MM-dd` cho ngày, `...Z` cho thời điểm). Chuỗi hiển thị kiểu
 * `dd/MM/yyyy` chỉ được sinh ra ngay lúc render — KHÔNG lưu vào state, KHÔNG
 * gửi lên trong payload.
 *
 * Lỗi mà module này tồn tại để ngăn: trước đây FE gửi
 * `toLocaleString("vi-VN")` lên server và lưu chuỗi đã format vào bản ghi, nên
 * dữ liệu mất kiểu ngày — không sắp xếp, không so sánh, không lọc khoảng được.
 *
 * Seed `mock-data.ts` hiện vẫn là `dd/MM/yyyy` (chuyển đổi thuộc bước sinh
 * Flyway seed, xem `review_fix_plan.md`), nên các hàm dưới đây nhận cả hai dạng
 * và trả nguyên chuỗi nếu không parse được — mock cũ vẫn hiển thị đúng.
 */

const VN_DATE = /^(\d{2})\/(\d{2})\/(\d{4})/;

/** `2026-07-29` | `2026-07-29T02:15:00Z` → `29/07/2026`. */
export function formatVnDate(value: string | null | undefined): string {
  if (!value) return "—";
  if (VN_DATE.test(value)) return value.slice(0, 10); // seed cũ, đã đúng dạng
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
}

/** `2026-07-29T02:15:00Z` → `29/07/2026 09:15` (giờ địa phương). */
export function formatVnDateTime(value: string | null | undefined): string {
  if (!value) return "—";
  if (VN_DATE.test(value)) return value; // seed cũ
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return `${formatVnDate(value)} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/**
 * `29/07/2026` → `2026-07-29` — dùng khi cần gửi giá trị người dùng nhập
 * (hoặc giá trị seed cũ) lên API.
 */
export function toIsoDate(value: string | null | undefined): string | null {
  if (!value) return null;
  const m = value.match(VN_DATE);
  if (m) return `${m[3]}-${m[2]}-${m[1]}`;
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? null : d.toISOString().slice(0, 10);
}

function pad(n: number): string {
  return String(n).padStart(2, "0");
}
