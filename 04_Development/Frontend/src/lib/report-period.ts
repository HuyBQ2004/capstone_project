/**
 * Helper kỳ báo cáo Daily/Weekly dùng chung cho UC-185/UC-188/UC-189
 * (BR-209/BR-213/BR-214): Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật).
 */

/**
 * @deprecated Nối thật 05/08/2026 — `sent-reports` giờ gọi backend thật (`kitchen_inspections`/
 * `violation_records` theo ngày thật của hệ thống), neo "hôm nay" ở 02/07/2026 khiến mọi báo cáo
 * luôn tính sai kỳ so với ngày chạy thực. `periodRange()` dùng `new Date()` thật; giữ hằng số
 * này lại chỉ để không phá import cũ nào còn sót, KHÔNG dùng cho tính toán mới.
 */
export const MOCK_TODAY = new Date(2026, 6, 2);

export type ReportPeriod = "Daily" | "Weekly";

export function parseVNDate(d: string): Date | null {
  const m = d.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
  return m ? new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1])) : null;
}

function fmt(d: Date): string {
  return d.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
}

/** "yyyy-MM-dd" theo giờ ĐỊA PHƯƠNG (không qua `toISOString()`, vốn quy về UTC — lệch ngày
 * gần nửa đêm) — dùng làm `rangeFrom`/`rangeTo` gửi backend (`@NotNull LocalDate`). */
export function toIsoDate(d: Date): string {
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export function periodRange(period: ReportPeriod): { from: Date; to: Date; label: string } {
  const today = new Date();
  if (period === "Daily") {
    return { from: today, to: today, label: `Hôm nay, ${fmt(today)}` };
  }
  const day = today.getDay(); // 0 = CN
  const monday = new Date(today);
  monday.setDate(today.getDate() - ((day + 6) % 7));
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  return { from: monday, to: sunday, label: `Thứ 2 ${fmt(monday)} – Chủ nhật ${fmt(sunday)}` };
}

export function inVNRange(dateStr: string, from: Date, to: Date): boolean {
  const d = parseVNDate(dateStr);
  return d != null && d >= from && d <= to;
}

/** Cặp chuỗi ngày/ngày-giờ gửi hiện tại (dd/MM/yyyy + dd/MM/yyyy HH:mm) */
export function sentStamps(): { date: string; sentAt: string } {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  const date = `${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${now.getFullYear()}`;
  return { date, sentAt: `${date} ${pad(now.getHours())}:${pad(now.getMinutes())}` };
}
