"use client";

/**
 * Khối in lưu mẫu thức ăn theo Phụ lục 2 — QĐ 1246/QĐ-BYT ngày 31/3/2017:
 * - Mau4Label: "Mẫu số 4 — Nhãn mẫu thức ăn lưu" (khung 4 dòng đúng câu chữ luật).
 * - Mau5Sheet: "Mẫu số 5 — Mẫu biểu theo dõi lưu và hủy mẫu thức ăn lưu" (12 cột).
 * Dùng chung cho trang in sổ (/samples/print) và in từng mẫu (/samples/[id]/print).
 */

import { CURRENT_SCHOOL, type FoodSample } from "@/lib/mock-data";

export const LAW_FACILITY = `Bếp ăn ${CURRENT_SCHOOL.shortName} — Cty TNHH Suất ăn An Bình`;
export const LAW_LOCATION = `Khu bếp chính, ${CURRENT_SCHOOL.address}`;

/** "02/07/2026 10:15" → "10 giờ 15 phút, ngày 02 tháng 7 năm 2026" (câu chữ Mẫu 4). */
export function vnTimeWords(v: string): string {
  const m = v.match(/(\d{1,2})\/(\d{1,2})\/(\d{4})\s+(\d{1,2}):(\d{2})/);
  if (!m) return v;
  return `${m[4]} giờ ${m[5]} phút, ngày ${m[1]} tháng ${Number(m[2])} năm ${m[3]}`;
}

/** CSS bảng biểu luật + chế độ in A4 ngang (ẩn khung app). */
export function SampleLawStyle() {
  return (
    <style>{`
      .law { font-size: 12px; color: #000; }
      .law-head { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 6px; }
      .law-head p { margin: 0 0 2px; }
      .law-title { font-weight: 700; max-width: 320px; text-align: right; }
      .law table { width: 100%; border-collapse: collapse; margin-bottom: 4px; }
      .law th, .law td { border: 1px solid #000; padding: 3px 5px; text-align: center; vertical-align: middle; font-weight: 400; }
      .law th { font-size: 11px; }
      .law td.l { text-align: left; }
      .law .num td { font-style: italic; font-size: 10px; }
      .law-sheet { margin-bottom: 28px; }
      .mau4-box { border: 1px solid #000; padding: 14px 16px; max-width: 560px; }
      .mau4-box p { margin: 0 0 8px; }
      .mau4-caption { font-weight: 700; margin: 0 0 6px; }
      /* Ô chữ ký: chừa khoảng trống KÝ TAY phía trên, họ tên in sẵn ở đáy ô */
      .law td.sig { vertical-align: bottom; height: 52px; }
      .sig-space { display: block; height: 34px; }
      .sig-name { display: block; text-align: center; }
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
    `}</style>
  );
}

/** Mẫu số 4: Nhãn mẫu thức ăn lưu — khung đúng câu chữ Phụ lục 2. */
export function Mau4Label({ sample }: { sample: FoodSample }) {
  return (
    <div className="law-sheet">
      <p className="mau4-caption">Mẫu số 4: Nhãn mẫu thức ăn lưu</p>
      <div className="mau4-box">
        <p>Bữa ăn: {sample.mealShift ?? sample.meal} (sáng/trưa/tối).</p>
        <p>Tên mẫu thức ăn: {sample.dish}</p>
        <p>Thời gian lấy: {vnTimeWords(sample.collectedAt)}</p>
        {/* Họ tên in sẵn — khoảng trống phía trên để ký tay khi in ra */}
        <p style={{ marginBottom: 0 }}>Người lấy mẫu <i>(Họ tên và chữ ký)</i>:</p>
        <span className="sig-space" />
        <p style={{ marginBottom: 0, fontWeight: 600 }}>{sample.collectedBy}</p>
      </div>
    </div>
  );
}

/** Mẫu số 5: Mẫu biểu theo dõi lưu và hủy mẫu thức ăn lưu — 12 cột. */
export function Mau5Sheet({
  samples,
  blankRows = 2,
}: {
  samples: FoodSample[];
  blankRows?: number;
}) {
  return (
    <div className="law-sheet">
      <div className="law-head">
        <div>
          <p>Tên cơ sở: {LAW_FACILITY}</p>
          <p>Địa điểm kiểm tra: {LAW_LOCATION}</p>
        </div>
        <p className="law-title">Mẫu số 5: Mẫu biểu theo dõi lưu và hủy mẫu thức ăn lưu</p>
      </div>
      <table>
        <thead>
          <tr>
            <th>TT</th>
            <th>Tên mẫu thức ăn</th>
            <th><i>Bữa ăn (giờ ăn...)</i></th>
            <th>Số lượng suất ăn</th>
            <th>Khối lượng/ thể tích mẫu<br /><i>(gam/ml)</i></th>
            <th>Dụng cụ chứa mẫu thức ăn lưu</th>
            <th>Nhiệt độ bảo quản mẫu (°C)</th>
            <th>Thời gian lấy mẫu<br /><i>(giờ, ngày, tháng, năm)</i></th>
            <th>Thời gian hủy mẫu<br /><i>(giờ, ngày, tháng, năm)</i></th>
            <th>Ghi chú<br /><i>(chất lượng mẫu thức ăn lưu...)</i></th>
            <th>Người lưu mẫu<br /><i>(ký và ghi rõ họ tên)</i></th>
            <th>Người hủy mẫu<br /><i>(ký và ghi rõ họ tên)</i></th>
          </tr>
          <tr className="num">
            {Array.from({ length: 12 }).map((_, i) => (
              <td key={i}><i>({i + 1})</i></td>
            ))}
          </tr>
        </thead>
        <tbody>
          {samples.map((s, i) => (
            <tr key={s.id}>
              <td>{i + 1}</td>
              <td className="l">{s.dish}</td>
              <td>{s.mealShift ?? s.meal}</td>
              <td>{s.servings ?? ""}</td>
              <td>{s.weight}</td>
              <td className="l">{s.container ?? ""}</td>
              <td>{s.fridgeTemp}</td>
              <td>{s.collectedAt}</td>
              <td>{s.disposedAt ?? ""}</td>
              <td className="l">{s.disposalNote ?? ""}</td>
              {/* (11)(12): họ tên in sẵn ở đáy ô — khoảng trống phía trên để ký tay */}
              <td className="sig">
                <span className="sig-space" />
                <span className="sig-name">{s.collectedBy}</span>
              </td>
              <td className="sig">
                <span className="sig-space" />
                <span className="sig-name">{s.disposedBy ?? ""}</span>
              </td>
            </tr>
          ))}
          {Array.from({ length: blankRows }).map((_, i) => (
            <tr key={`b${i}`}>
              {Array.from({ length: 12 }).map((_, j) => (
                <td key={j}>&nbsp;</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
