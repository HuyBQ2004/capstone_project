/**
 * Hằng số & helper dùng chung cho module Quản lý Vi phạm — tách ra 19/07/2026 khi
 * refactor `violations/page.tsx` (dialog-chain) sang Master–Detail 4 route theo
 * `review/medical_staff/review_medicalstaff_violations.md` Mục 2.1/QĐ 1:
 *   /violations · /violations/new · /violations/[id] · /violations/reports
 *
 * Không đổi BR/UC nào: BR-183/184 (tự phân loại theo staffName), BR-186 (LOW đóng
 * trực tiếp, MEDIUM/HIGH gửi BGH), BR-211 (PDF → ký tay → tải scan trước khi đóng),
 * BR-188 (biên bản bất biến) giữ nguyên — chỉ đổi cách trình bày/luồng màn hình.
 */

import { CURRENT_SCHOOL, PARTNERS, type UploadedFile, type ViolationRecord } from "@/lib/mock-data";

/**
 * Loại vi phạm — giữ nguyên 4 giá trị đã có (§10.3b field 9; cần đội dự án xác nhận
 * trước khi khóa cứng enum — không tự thêm giá trị mới, đã bỏ "Khác").
 * Mục 2.3: nhãn hiển thị đổi thành "Nhóm hành vi (nếu áp dụng)" — giá trị KHÔNG đổi.
 */
export const VIOLATION_TYPES = [
  { value: "PROCEDURE_SKIP", label: "Bỏ qua bước quy trình bắt buộc" },
  { value: "HYGIENE_VIOLATION", label: "Vi phạm vệ sinh cá nhân" },
  { value: "OVERRIDE_ABUSE", label: "Lạm dụng quyền ghi đè hệ thống" },
  { value: "UNAUTHORIZED_ACTION", label: "Hành động không được ủy quyền" },
];

/**
 * Mức chế tài áp cho **ĐƠN VỊ Nhà thầu** — BR-185 sửa 26/07/2026 (`disciplinaryLevel` →
 * `unitSanctionLevel`). Trước đây đây là thang kỷ luật CÁ NHÂN ("Đình chỉ" một nhân viên);
 * nay Medical Staff không lập biên bản nhắm cá nhân dưới bất kỳ hình thức nào — kỷ luật
 * nhân viên cụ thể là việc nội bộ của Nhà thầu, ngoài phạm vi SFMS (Mục 9.1).
 *
 * Tên hằng số giữ nguyên để không phải sửa đồng loạt điểm gọi; **nhãn** đã đổi sang đúng
 * ngữ nghĩa cấp đơn vị.
 */
export const DISCIPLINARY_LEVELS = [
  { value: "VERBAL_WARNING", label: "Nhắc nhở bằng văn bản" },
  { value: "WRITTEN_WARNING", label: "Cảnh cáo hợp đồng" },
  { value: "SUSPENSION", label: "Phạt vi phạm hợp đồng" },
  { value: "TERMINATION_RECOMMENDATION", label: "Đề xuất chấm dứt hợp đồng" },
];

/** BR-211 field 3: đại diện Nhà thầu — liên kết tài khoản Service Caterers, không nhập tay */
export const CATERER_REPS = PARTNERS.filter((p) => p.type === "Service Caterer").map(
  (p) => `${p.contact.split("—")[0].trim()} (${p.name})`
);

export function nowStamp(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** Câu tự sinh từ occurredAt (datetime-local "yyyy-MM-ddTHH:mm") — §10.3b field 11 */
export function openingSentence(occurredAt: string): string {
  if (!occurredAt) return "Vào hồi …… ngày ……,";
  const d = new Date(occurredAt);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `Vào hồi ${pad(d.getHours())} giờ ${pad(d.getMinutes())} phút, ngày ${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()},`;
}

/** Mock file tải lên (Mục 2.3 — thay counter/boolean giả bằng file thật có tên/thời điểm) */
export function mockUpload(prefix: string, index: number): UploadedFile {
  return {
    name: `${prefix}_${String(index).padStart(2, "0")}.jpg`,
    uploadedAt: nowStamp(),
    size: `${(1.2 + index * 0.4).toFixed(1)} MB`,
  };
}

/** Danh sách file bằng chứng (tương thích ngược: bản ghi cũ chỉ có evidencePhotos đếm số) */
export function evidenceCount(v: ViolationRecord): number {
  return v.evidenceFiles?.length ?? v.evidencePhotos ?? 0;
}

/** Đã có scan biên bản ký chưa (tương thích ngược với boolean signedRecordPhoto cũ) */
export function hasSignedScan(v: ViolationRecord): boolean {
  return (v.signedRecordFiles?.length ?? 0) > 0 || Boolean(v.signedRecordPhoto);
}

/**
 * Vòng đời hồ sơ biên bản: Đã lập → Chờ xuất PDF → Chờ ký & tải bản ký → Chờ xử lý/duyệt
 * → Đã đóng. Suy ra từ dữ liệu sẵn có, KHÔNG thêm field trạng thái riêng.
 *
 * ═══ ĐẢO NGƯỢC 27/07/2026 — Vòng 6 Mục 12 ═══
 * Vòng 4 Mục 9.2 (26/07) từng đổi sang **2 tick điện tử** (Y tế + Nhà thầu). Ảnh chụp màn
 * đã áp code cho thấy lỗi kiến trúc ngay từ khâu thiết kế: **2 nút không thể cùng nằm 1
 * màn** vì `/medical-staff/violations/[id]` thuộc layout role Medical Staff — Service
 * Caterers không truy cập được, nên nút "Nhà thầu xác nhận" chỉ có thể do Y tế bấm hộ.
 * Và kể cả tách route theo role, `caterersRepresentativeId` vốn chỉ là "Y tế **chọn tên**
 * từ dropdown", không phải Nhà thầu tự đăng nhập — tức xác nhận của bên thứ hai chưa từng
 * là hành động độc lập, bất kể đặt nút ở đâu.
 *
 * Đã cân nhắc 3 hướng (tách route + xác thực thật / giữ 1 màn bấm nối tiếp / quay lại ký
 * tay); người dùng chọn **quay lại PDF + in + ký tay + chụp ảnh**: Biên bản Vi phạm dẫn
 * tới chế tài hợp đồng (BR-185) nên cần bằng chứng ký tay vật lý; và động cơ gốc của
 * Mục 9.2 — tránh chi phí chữ ký số CA — vẫn được giữ, vì ảnh chữ ký tay không phát sinh
 * chi phí đó.
 */
export type ViolationStep = "DRAFTED" | "PDF_EXPORTED" | "SIGNED_UPLOADED" | "PENDING_APPROVAL" | "CLOSED";

export function stepOf(v: ViolationRecord): ViolationStep {
  if (v.status === "CLOSED" || v.resolution === "APPROVED") return "CLOSED";
  if (v.resolution === "PENDING_APPROVAL") return "PENDING_APPROVAL";
  if (hasSignedScan(v)) return "SIGNED_UPLOADED";
  if (v.pdfExported) return "PDF_EXPORTED";
  return "DRAFTED";
}

/**
 * Biên bản đã khoá (đủ bằng chứng ký) → mở bước nhập xử lý.
 * Vòng 6 Mục 12: căn cứ quay lại **ảnh/scan bản ký** (`hasSignedScan`), thay cho 2 mốc tick
 * điện tử của Mục 9.2. Giữ tên hàm để không phải sửa lại điểm gọi lần thứ ba.
 */
export function isRecordLocked(v: ViolationRecord): boolean {
  return hasSignedScan(v);
}

/** Nhãn "Bước hiện tại" cho cột gộp Xử lý + Trạng thái (Mục 2.4/QĐ 4) */
export const STEP_LABEL: Record<ViolationStep, string> = {
  DRAFTED: "Chờ xuất PDF",
  PDF_EXPORTED: "Chờ ký & tải bản ký",
  SIGNED_UPLOADED: "Chờ nhập xử lý",
  PENDING_APPROVAL: "Chờ School Admin duyệt",
  CLOSED: "Đã đóng",
};

/** CTA chính theo bước (Mục 2.2) — chỉ 1 CTA hiển thị tại Hồ sơ VIO */
export const STEP_CTA: Record<ViolationStep, string> = {
  DRAFTED: "Xuất & in biên bản (PDF)",
  PDF_EXPORTED: "Tải scan biên bản đã ký",
  SIGNED_UPLOADED: "Nhập xử lý & đóng hồ sơ",
  PENDING_APPROVAL: "Xem tiến độ duyệt",
  CLOSED: "Xem hồ sơ, PDF và bản ký",
};

/**
 * BR-211 — dựng nội dung "BIÊN BẢN VI PHẠM" theo bố cục mẫu
 * `00_Policies/specialreport2173_1.md`. Cột chữ ký thứ 3 "Người làm chứng" là khoảng
 * trống TĨNH — hệ thống không track nhân chứng (§10 mục 4).
 *
 * Mục 2.2 (19/07/2026): tách phần DỰNG HTML khỏi phần IN — trang Hồ sơ hiển thị bản
 * XEM TRƯỚC ngay trong app trước khi người dùng chủ động bấm In/Lưu PDF (thay
 * `window.print()` gọi thẳng lúc mở cửa sổ như bản cũ).
 */
export function buildRecordHtml(v: ViolationRecord): string {
  const esc = (s: string | undefined) =>
    (s ?? "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  const now = new Date();
  const stamp = `${now.getHours()} giờ ${now.getMinutes()} phút, ngày ${now.getDate()} tháng ${now.getMonth() + 1} năm ${now.getFullYear()}`;
  return `<div class="hdr"><div><b>${esc(CURRENT_SCHOOL.name).toUpperCase()}</b>Y TẾ TRƯỜNG HỌC</div>
<div><b>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</b>Độc lập - Tự do - Hạnh phúc</div></div>
<h1>BIÊN BẢN VI PHẠM</h1>
<p>Hôm nay, tại: <b>${esc(v.locationOfIncident) || "________"}</b> &nbsp;·&nbsp; Vào: ${stamp}</p>
<p><b>Chúng tôi gồm</b></p>
<p>Đại diện Y tế trường học — Ông(bà): <b>${esc(v.recordedBy)}</b></p>
<p>Đại diện Nhà thầu — Ông(bà): <b>${esc(v.caterersRepresentative) || "________"}</b></p>
${v.staffName ? `<p>Nhân viên liên quan: <b>${esc(v.staffName)}</b>${v.staffPosition ? ` — Chức vụ: ${esc(v.staffPosition)}` : ""}</p>` : ""}
<p><b>Cùng nhau ghi nhận sự việc như sau:</b></p>
<p>${esc(v.occurredAt) ? `Vào hồi ${esc(v.occurredAt)}:` : ""}</p>
<div>${v.description ?? ""}</div>
<p>Biên bản được hoàn thành vào ${stamp}. Biên bản được lập thành 01 bản, lưu tại hệ thống SFMS,
có giá trị pháp lý như nhau.</p>
<div class="sig"><div><b>Y TẾ TRƯỜNG HỌC</b><i>(Ký, ghi rõ họ tên)</i></div>
<div><b>NHÀ THẦU</b><i>(Ký, ghi rõ họ tên)</i></div>
<div><b>NGƯỜI LÀM CHỨNG</b><i>(Ký, ghi rõ họ tên — để trống nếu không có)</i></div></div>`;
}

/** CSS dùng chung cho khối xem trước trong app và cửa sổ in */
export const RECORD_PRINT_CSS = `body{font-family:"Times New Roman",serif;max-width:720px;margin:24px auto;line-height:1.6;font-size:15px}
.hdr{display:flex;justify-content:space-between;text-align:center}.hdr b{display:block}
h1{text-align:center;font-size:18px;margin:24px 0}
.sig{display:flex;justify-content:space-between;text-align:center;margin-top:36px}
.sig div{width:32%}.sig i{display:block;margin-top:64px;color:#666}`;

/** Mở tài liệu trong tab riêng — dùng chung cho Xem trước (không in) và In/Lưu PDF (có in) */
function openRecordWindow(v: ViolationRecord): Window | null {
  const w = window.open("", "_blank", "width=820,height=1000");
  if (!w) return null;
  w.document.write(
    `<!DOCTYPE html><html lang="vi"><head><meta charset="utf-8"><title>${v.id} — Biên bản Vi phạm</title>` +
      `<style>${RECORD_PRINT_CSS}</style></head><body>${buildRecordHtml(v)}</body></html>`
  );
  w.document.close();
  return w;
}

/**
 * Mở tài liệu ở tab riêng, CHỈ để xem — không tự gọi in (Mục 2.2, sửa 19/07/2026 vòng 2:
 * trước đây "Xem trước" dựng lại HTML NGAY TRONG trang Hồ sơ, trùng lặp với card dữ liệu
 * đã hiển thị sẵn ở trên — nay tách hẳn ra tab riêng, đúng nghĩa "xem trước file PDF"
 * thay vì lặp nội dung 2 lần trên cùng 1 trang).
 */
export function previewRecord(v: ViolationRecord) {
  openRecordWindow(v)?.focus();
}

/** Mở tài liệu ở tab riêng rồi in ngay (người dùng chủ động bấm) — Mục 2.2 */
export function printRecord(v: ViolationRecord) {
  const w = openRecordWindow(v);
  if (!w) return;
  w.focus();
  w.print();
}
