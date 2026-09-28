/**
 * BẢN IN SỔ KIỂM THỰC BA BƯỚC — Mẫu số 1/2/3, Phụ lục 1, QĐ 1246/QĐ-BYT ngày 31/3/2017.
 *
 * Nguồn tách: `review_medicalstaff_compliance_kitcheninspection.md` Mục 19.3 (27/07/2026).
 *
 * Trước đây bản in chỉ tồn tại dưới dạng JSX **nằm trong route
 * `app/(app)/service-caterers/inspections/[id]/print/page.tsx`**. Mục 8.5 điểm 2 cho Medical
 * Staff "dùng lại route in sẵn có phía Service Caterers" — đúng ý muốn không có 2 bản in
 * khác nhau cho cùng 1 phiên, nhưng sai cách thực hiện: bấm "In / Lưu PDF" từ màn Y tế
 * điều hướng thẳng vào route thuộc **layout của role Caterers**, đổi cả sidebar/app shell
 * sang giao diện của role khác.
 *
 * Nguyên tắc rút ra (áp cho mọi trường hợp xem chéo về sau): **không deep-link trực tiếp
 * vào route thuộc layout của role khác** — tách phần dùng chung ra `lib/`/`components/app/`,
 * mỗi role tự gọi trong route của chính mình.
 *
 * Vì vậy phần render được đưa về đây dưới dạng **hàm sinh chuỗi HTML thuần**, không phụ
 * thuộc React/role/layout — dùng được cho cả 2 đường: nhúng vào trang (Caterers) và mở
 * cửa sổ in mới (Medical Staff), cùng khuôn với `buildInspectionRecordHtml` của biên bản
 * kiểm tra bếp (Mục 12.1).
 */

import type { InspectionSession } from "@/lib/mock-data";

/** Số dòng trống cuối mỗi bảng, giữ đúng dáng sổ giấy của mẫu biểu. */
const BLANK_ROWS = 2;

const esc = (s: string | number | null | undefined) =>
  String(s ?? "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

const mark = (v: boolean) => (v ? "x" : "");

/** "01/07/2026" → "ngày 01 tháng 07 năm 2026" (đúng câu chữ mẫu biểu). */
function vnDateWords(d: string): string {
  const m = d.match(/(\d{1,2})\/(\d{1,2})\/(\d{4})/);
  return m ? `ngày ${m[1]} tháng ${m[2]} năm ${m[3]}` : d;
}

/** CSS bảng biểu luật + chế độ in (A4 ngang). Dùng chung cho cả 2 đường gọi. */
export const INSPECTION_SHEET_CSS = `
  .law { font-size: 12px; color: #000; }
  .law-head { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 6px; }
  .law-head p { margin: 0 0 2px; }
  .law-title { font-weight: 700; max-width: 300px; text-align: right; }
  .law-section { font-weight: 700; margin: 8px 0 4px; }
  .law table { width: 100%; border-collapse: collapse; margin-bottom: 4px; }
  .law th, .law td { border: 1px solid #000; padding: 3px 5px; text-align: center; vertical-align: middle; font-weight: 400; }
  .law th { font-size: 11px; }
  .law td.l { text-align: left; }
  .law .num td { font-style: italic; font-size: 10px; }
  .law-sheet { margin-bottom: 28px; }
  @media print {
    aside, [data-sonner-toaster] { display: none !important; }
    .pl-64 { padding-left: 0 !important; }
    .pl-64 > header, body header { display: none !important; }
    main { max-width: 100% !important; padding: 0 !important; }
    .no-print { display: none !important; }
    .law-sheet { page-break-after: always; }
    .law-sheet:last-child { page-break-after: auto; }
  }
  @page { size: A4 landscape; margin: 10mm; }
`;

/**
 * Bước ĐÃ ĐẠT mới được xuất bản in — quy tắc có từ 08/07/2026, giữ nguyên: bản in là văn
 * bản pháp lý, không phát hành cho bước chưa đạt/đang dở.
 */
export function printableForms(session: InspectionSession, only?: number): number[] {
  const passed: Record<number, boolean> = {
    1: session.step1 === "PASSED",
    2: session.step2 === "PASSED",
    3: session.step3 === "PASSED",
  };
  const requested = only && [1, 2, 3].includes(only) ? [only] : [1, 2, 3];
  return requested.filter((f) => passed[f]);
}

/** Mẫu nào KHÔNG in được (để màn gọi hiển thị cảnh báo bằng UI của chính nó). */
export function blockedForms(session: InspectionSession, only?: number): number[] {
  const requested = only && [1, 2, 3].includes(only) ? [only] : [1, 2, 3];
  const ok = printableForms(session, only);
  return requested.filter((f) => !ok.includes(f));
}

function sheetHeader(session: InspectionSession, title: string): string {
  const h = session.formHeader;
  return `<div class="law-head"><div>
<p>Tên cơ sở: ${esc(h?.facility ?? "………………………………")}</p>
<p>Người kiểm tra: ${esc(h?.inspector ?? session.inspector)}</p>
<p>Thời gian kiểm tra: ${esc(vnDateWords(h?.checkedAt ?? session.date))}</p>
<p>Địa điểm kiểm tra: ${esc(h?.location ?? "………………………………")}</p>
</div><p class="law-title">${esc(title)}</p></div>`;
}

const blankRows = (cols: number) =>
  Array.from({ length: BLANK_ROWS })
    .map(() => `<tr>${"<td>&nbsp;</td>".repeat(cols)}</tr>`)
    .join("");

const numRow = (n: number) =>
  `<tr class="num">${Array.from({ length: n })
    .map((_, i) => `<td><i>(${i + 1})</i></td>`)
    .join("")}</tr>`;

/** Mẫu số 1 — Bước 1: kiểm tra trước khi chế biến. */
function sheet1(s: InspectionSession): string {
  const fresh = s.step1Fresh ?? [];
  const dry = s.step1Dry ?? [];
  return `<div class="law-sheet">
${sheetHeader(s, "Mẫu số 1: Kiểm tra trước khi chế biến thức ăn (Bước 1)")}
<p class="law-section">I. Thực phẩm tươi sống, đông lạnh: thịt, cá, rau, củ, quả...</p>
<table><thead>
<tr>
<th rowspan="2">TT</th><th rowspan="2">Tên thực phẩm</th>
<th rowspan="2">Thời gian nhập<br><i>(ngày, giờ)</i></th>
<th rowspan="2">Khối lượng<br><i>(kg/lít...)</i></th>
<th colspan="3">Nơi cung cấp</th>
<th rowspan="2">Chứng từ, hóa đơn</th><th rowspan="2">Giấy ĐK VS thú y</th><th rowspan="2">Giấy kiểm dịch</th>
<th colspan="2">Kiểm tra cảm quan<br><i>(màu, mùi vị, trạng thái, bảo quản...)</i></th>
<th colspan="2">Xét nghiệm nhanh (nếu có)<br><i>(vi sinh, hóa lý)</i></th>
<th rowspan="2">Biện pháp xử lý /Ghi chú</th>
</tr>
<tr><th>Tên cơ sở</th><th>Địa chỉ, điện thoại</th><th>Tên người giao hàng</th>
<th><i>Đạt</i></th><th><i>Không đạt</i></th><th><i>Đạt</i></th><th><i>Không đạt</i></th></tr>
${numRow(15)}
</thead><tbody>
${fresh
  .map(
    (r, i) => `<tr><td>${i + 1}</td><td class="l">${esc(r.name)}</td><td>${esc(r.receivedAt)}</td>
<td>${esc(r.quantity)}</td><td class="l">${esc(r.supplierName)}</td><td class="l">${esc(r.supplierContact)}</td>
<td class="l">${esc(r.deliverer)}</td><td>${mark(r.hasInvoice)}</td><td>${mark(r.hasVetCert)}</td>
<td>${mark(r.hasQuarantineCert)}</td><td>${mark(r.sensoryPass)}</td><td>${mark(!r.sensoryPass)}</td>
<td>${r.quickTest === "PASS" ? "x" : ""}</td><td>${r.quickTest === "FAIL" ? "x" : ""}</td>
<td class="l">${esc(r.action)}</td></tr>`
  )
  .join("")}
${blankRows(15)}
</tbody></table>
<p class="law-section">II. Thực phẩm khô và thực phẩm bao gói sẵn, phụ gia thực phẩm:</p>
<table><thead>
<tr>
<th rowspan="2">TT</th><th rowspan="2">Tên thực phẩm</th><th rowspan="2">Tên cơ sở sản xuất</th>
<th rowspan="2">Địa chỉ sản xuất</th><th rowspan="2">Thời gian nhập<br><i>(ngày, giờ)</i></th>
<th rowspan="2">Khối lượng<br><i>(kg/lít...)</i></th>
<th colspan="3">Nơi cung cấp</th>
<th rowspan="2">Hạn sử dụng</th><th rowspan="2">Điều kiện bảo quản<br><i>(T° thường/ lạnh...)</i></th>
<th rowspan="2">Chứng từ, hóa đơn</th>
<th colspan="2">Kiểm tra cảm quan<br><i>(nhãn, bao bì, bảo quản, hạn sử dụng...)</i></th>
<th rowspan="2">Biện pháp xử lý/Ghi chú</th>
</tr>
<tr><th>Tên cơ sở</th><th>Tên chủ giao hàng</th><th>Địa chỉ, điện thoại</th>
<th><i>Đạt</i></th><th><i>Không đạt</i></th></tr>
${numRow(15)}
</thead><tbody>
${dry
  .map(
    (r, i) => `<tr><td>${i + 1}</td><td class="l">${esc(r.name)}</td><td class="l">${esc(r.producer)}</td>
<td class="l">${esc(r.producerAddress)}</td><td>${esc(r.receivedAt)}</td><td>${esc(r.quantity)}</td>
<td class="l">${esc(r.supplierName)}</td><td class="l">${esc(r.deliverer)}</td><td class="l">${esc(r.supplierContact)}</td>
<td>${esc(r.expiry)}</td><td>${esc(r.storage === "Thường" ? "T° thường" : r.storage)}</td>
<td>${mark(r.hasInvoice)}</td><td>${mark(r.sensoryPass)}</td><td>${mark(!r.sensoryPass)}</td>
<td class="l">${esc(r.action)}</td></tr>`
  )
  .join("")}
${blankRows(15)}
</tbody></table></div>`;
}

/** Mẫu số 2 — Bước 2: kiểm tra khi chế biến. */
function sheet2(s: InspectionSession): string {
  const rows = s.step2Rows ?? [];
  return `<div class="law-sheet">
${sheetHeader(s, "Mẫu số 2: Kiểm tra khi chế biến thức ăn (Bước 2)")}
<table><thead>
<tr>
<th rowspan="2">TT</th><th rowspan="2">Ca/bữa ăn<br><i>(Bữa ăn, giờ ăn...)</i></th>
<th rowspan="2">Tên món ăn</th>
<th rowspan="2">Nguyên liệu chính để chế biến<br><i>(tên, số lượng...)</i></th>
<th rowspan="2">Số lượng/ số suất ăn</th>
<th rowspan="2">Thời gian sơ chế xong<br><i>(ngày, giờ)</i></th>
<th rowspan="2">Thời gian chế biến xong<br><i>(ngày, giờ)</i></th>
<th colspan="3">Kiểm tra điều kiện vệ sinh<br><i>(từ thời điểm bắt đầu sơ chế, chế biến cho đến khi thức ăn được chế biến xong)</i></th>
<th colspan="2">Kiểm tra cảm quan thức ăn<br><i>(màu, mùi, vị, trạng thái, bảo quản...)</i></th>
<th rowspan="2">Biện pháp xử lý/Ghi chú</th>
</tr>
<tr><th><i>Người tham gia chế biến</i></th><th><i>Trang thiết bị dụng cụ</i></th>
<th><i>Khu vực chế biến và phụ trợ</i></th><th><i>Đạt</i></th><th><i>Không đạt</i></th></tr>
${numRow(13)}
</thead><tbody>
${rows
  .map(
    (r, i) => `<tr><td>${i + 1}</td><td>${esc(r.shift)}</td><td class="l">${esc(r.dishName)}</td>
<td class="l">${esc(r.mainIngredients)}</td><td>${esc(r.servings)}</td><td>${esc(r.prepDoneAt)}</td>
<td>${esc(r.cookDoneAt)}</td><td>${r.hygieneStaff ? "Đạt" : "Không đạt"}</td>
<td>${r.hygieneEquipment ? "Đạt" : "Không đạt"}</td><td>${r.hygieneArea ? "Đạt" : "Không đạt"}</td>
<td>${mark(r.sensoryPass)}</td><td>${mark(!r.sensoryPass)}</td><td class="l">${esc(r.action)}</td></tr>`
  )
  .join("")}
${blankRows(13)}
</tbody></table></div>`;
}

/** Mẫu số 3 — Bước 3: kiểm tra trước khi ăn. */
function sheet3(s: InspectionSession): string {
  const rows = s.step3Rows ?? [];
  return `<div class="law-sheet">
${sheetHeader(s, "Mẫu số 3: Kiểm tra trước khi ăn (Bước 3)")}
<table><thead>
<tr>
<th rowspan="2">TT</th><th rowspan="2">Ca/bữa ăn<br><i>(Bữa ăn, giờ ăn...)</i></th>
<th rowspan="2">Tên món ăn</th><th rowspan="2">Số lượng suất ăn</th>
<th rowspan="2">Thời gian chia món ăn xong<br><i>(ngày, giờ)</i></th>
<th rowspan="2">Thời gian bắt đầu ăn<br><i>(ngày, giờ)</i></th>
<th rowspan="2">Dụng cụ chia, chứa đựng, che đậy, bảo quản thức ăn</th>
<th colspan="2">Kiểm tra cảm quan món ăn<br><i>(màu, mùi, vị, trạng thái, bảo quản...)</i></th>
<th rowspan="2">Biện pháp xử lý/Ghi chú</th>
</tr>
<tr><th><i>Đạt</i></th><th><i>Không đạt</i></th></tr>
${numRow(10)}
</thead><tbody>
${rows
  .map(
    (r, i) => `<tr><td>${i + 1}</td><td>${esc(r.shift)}</td><td class="l">${esc(r.dishName)}</td>
<td>${esc(r.servings)}</td><td>${esc(r.dividedDoneAt)}</td><td>${esc(r.eatStartAt)}</td>
<td class="l">${esc(r.utensils)}</td><td>${mark(r.sensoryPass)}</td><td>${mark(!r.sensoryPass)}</td>
<td class="l">${esc(r.action)}</td></tr>`
  )
  .join("")}
${blankRows(10)}
</tbody></table></div>`;
}

/**
 * Sinh HTML các mẫu ĐỦ ĐIỀU KIỆN in của phiên. Trả về chuỗi rỗng khi không mẫu nào đạt —
 * màn gọi tự quyết định hiển thị cảnh báo thế nào bằng UI của chính nó.
 */
export function buildInspectionSheetsHtml(session: InspectionSession, only?: number): string {
  const forms = printableForms(session, only);
  const parts: string[] = [];
  if (forms.includes(1)) parts.push(sheet1(session));
  if (forms.includes(2)) parts.push(sheet2(session));
  if (forms.includes(3)) parts.push(sheet3(session));
  return parts.join("\n");
}

/** Mở cửa sổ in độc lập — dùng cho các role KHÔNG sở hữu route in (VD Medical Staff). */
export function openInspectionSheetsPrint(session: InspectionSession, only?: number): boolean {
  const body = buildInspectionSheetsHtml(session, only);
  if (!body) return false;
  const w = window.open("", "_blank", "width=1200,height=900");
  if (!w) return false;
  w.document.write(
    `<!DOCTYPE html><html lang="vi"><head><meta charset="utf-8">` +
      `<title>${esc(session.id)} — Sổ kiểm thực ba bước</title>` +
      `<style>${INSPECTION_SHEET_CSS}</style></head><body><div class="law">${body}</div></body></html>`
  );
  w.document.close();
  w.focus();
  w.print();
  return true;
}
