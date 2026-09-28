/**
 * BIÊN BẢN KIỂM TRA AN TOÀN BẾP — cùng khuôn "BIÊN BẢN VI PHẠM" (`lib/violation-utils.ts`,
 * `buildRecordHtml`), chỉ đổi tiêu đề + nội dung nguồn (checklist bếp thay vì hồ sơ vi phạm)
 * theo đúng yêu cầu 10/08/2026 (Nam). Tái dùng `RECORD_PRINT_CSS` — không dựng CSS riêng.
 *
 * Nội dung THÂN biên bản là `recordContent` — tự sinh ở backend lúc nộp checklist
 * (`KitchenInspectionServiceImpl.autoRecordContent`), Medical Staff sửa được qua
 * `PATCH /kitchen-inspections/{code}/record` trước khi Caterers xác nhận.
 */

import { RECORD_PRINT_CSS } from "@/lib/violation-utils";
import { formatVnDate } from "@/lib/format";

/** Field thật trên backend (`KitchenInspectionResponse`) — inline vì chưa có trong
 *  schema.d.ts, XOÁ ép kiểu này khi gen lại được. */
export type KitchenRecordSource = {
  id: string;
  date: string;
  inspector?: string;
  score: number;
  recordContent?: string;
  confirmStatus?: string;
  confirmedBy?: string;
  confirmedAt?: string;
};

function esc(s: string | undefined): string {
  return (s ?? "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

export function buildKitchenRecordHtml(k: KitchenRecordSource, schoolName: string): string {
  const now = new Date();
  const nowStamp = `${now.getHours()} giờ ${now.getMinutes()} phút, ngày ${now.getDate()} tháng ${now.getMonth() + 1} năm ${now.getFullYear()}`;
  return `<div class="hdr"><div><b>${esc(schoolName).toUpperCase()}</b>Y TẾ TRƯỜNG HỌC</div>
<div><b>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</b>Độc lập - Tự do - Hạnh phúc</div></div>
<h1>BIÊN BẢN KIỂM TRA AN TOÀN BẾP</h1>
<p>Phiên kiểm tra: <b>${esc(k.id)}</b> &nbsp;·&nbsp; Ngày kiểm tra: ${esc(formatVnDate(k.date))} &nbsp;·&nbsp; Điểm: <b>${k.score}%</b></p>
<p><b>Chúng tôi gồm</b></p>
<p>Đại diện Y tế trường học — Ông(bà): <b>${esc(k.inspector) || "________"}</b></p>
<p>Đại diện Nhà thầu — Ông(bà): <b>${esc(k.confirmedBy) || "________ (chưa xác nhận)"}</b></p>
<p><b>Cùng nhau ghi nhận sự việc như sau:</b></p>
<div style="white-space:pre-wrap">${esc(k.recordContent) || "(chưa có nội dung)"}</div>
<p>Biên bản được hoàn thành vào ${nowStamp}. Biên bản được lập thành 01 bản, lưu tại hệ thống SFMS,
có giá trị pháp lý như nhau.</p>
<div class="sig"><div><b>Y TẾ TRƯỜNG HỌC</b><i>(Ký, ghi rõ họ tên)</i></div>
<div><b>NHÀ THẦU</b><i>(Ký, ghi rõ họ tên)</i></div>
<div><b>NGƯỜI LÀM CHỨNG</b><i>(Ký, ghi rõ họ tên — để trống nếu không có)</i></div></div>`;
}

function openRecordWindow(k: KitchenRecordSource, schoolName: string): Window | null {
  const w = window.open("", "_blank", "width=820,height=1000");
  if (!w) return null;
  w.document.write(
    `<!DOCTYPE html><html lang="vi"><head><meta charset="utf-8"><title>${k.id} — Biên bản kiểm tra bếp</title>` +
      `<style>${RECORD_PRINT_CSS}</style></head><body>${buildKitchenRecordHtml(k, schoolName)}</body></html>`
  );
  w.document.close();
  return w;
}

export function previewKitchenRecord(k: KitchenRecordSource, schoolName: string) {
  openRecordWindow(k, schoolName)?.focus();
}

export function printKitchenRecord(k: KitchenRecordSource, schoolName: string) {
  const w = openRecordWindow(k, schoolName);
  if (!w) return;
  w.focus();
  w.print();
}
