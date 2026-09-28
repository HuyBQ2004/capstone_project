"use client";

/**
 * Khối in lưu mẫu thức ăn theo Phụ lục 2 — QĐ 1246/QĐ-BYT ngày 31/3/2017:
 * - Mau4Label: "Mẫu số 4 — Nhãn mẫu thức ăn lưu" (khung 4 dòng đúng câu chữ luật).
 * - Mau5Sheet: "Mẫu số 5 — Mẫu biểu theo dõi lưu và hủy mẫu thức ăn lưu" (12 cột).
 * Dùng chung cho trang in sổ (/samples/print) và in từng mẫu (/samples/[id]/print).
 */

import { SampleQrCode } from "@/components/app/sample-qr-code";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { useAuthStore } from "@/lib/stores/auth-store";

/**
 * 06/08/2026 — bỏ hằng số module-level cũ (tính 1 lần lúc import từ `CURRENT_SCHOOL` mock) —
 * mọi trường dùng chung 1 tên trường SAI + tên đơn vị suất ăn bịa "Cty TNHH Suất ăn An Bình"
 * trên VĂN BẢN PHÁP LÝ in ra (Mẫu số 5, QĐ 1246/QĐ-BYT). Giờ đọc tên trường thật từ session lúc
 * render; địa chỉ chi tiết KHÔNG có sẵn trong session (chỉ System Admin xem đủ field ở
 * `/schools/{code}`) — để trống thay vì tiếp tục in địa chỉ bịa lên văn bản pháp lý.
 */
export function useLawFacilityLabels(): { facility: string; location: string } {
  const school = useAuthStore((s) => s.user?.school);
  return {
    facility: school ? `Bếp ăn ${school}` : "—",
    location: school ? `Khu bếp chính, trường ${school}` : "—",
  };
}

/**
 * Nhận cả ISO (`FoodSampleDto` thật, nối 05/08/2026) lẫn `dd/MM/yyyy HH:mm`
 * (seed mock cũ) — `formatVnDateTime` chuẩn hoá về `dd/MM/yyyy HH:mm` trước
 * khi tách câu chữ "10 giờ 15 phút, ngày 02 tháng 7 năm 2026" (Mẫu 4).
 */
export function vnTimeWords(v: string | null | undefined): string {
  if (!v) return "";
  const display = formatVnDateTime(v);
  const m = display.match(/(\d{1,2})\/(\d{1,2})\/(\d{4})\s+(\d{1,2}):(\d{2})/);
  if (!m) return display;
  return `${m[4]} giờ ${m[5]} phút, ngày ${m[1]} tháng ${Number(m[2])} năm ${m[3]}`;
}

/**
 * 11/08/2026 (Nam) — tem ĐẶT TRƯỚC (`source=reservation`) chưa có `collectedAt` (chưa lấy mẫu
 * thật), nên dòng "Thời gian lấy" trước đây in RỖNG — dán nhiều tem cùng lúc thì không biết tem
 * nào của ngày/mẫu nào. Đã biết trước NGÀY dự kiến lấy (`collectDate` của phiếu đặt trước) nên in
 * sẵn phần ngày, chỉ chừa chỗ trống để ghi tay GIỜ lúc lấy mẫu thật.
 */
export function vnTimeWordsOrDateOnly(collectedAt: string | null | undefined, collectDate: string | null | undefined): string {
  if (collectedAt) return vnTimeWords(collectedAt);
  if (!collectDate) return "";
  const display = formatVnDate(collectDate);
  const m = display.match(/(\d{1,2})\/(\d{1,2})\/(\d{4})/);
  if (!m) return "";
  return `.......... giờ .......... phút, ngày ${m[1]} tháng ${Number(m[2])} năm ${m[3]}`;
}

/**
 * Trường tối thiểu để in Mẫu 4/5 — widen từ `FoodSample` (mock) sang shape
 * khớp cả `FoodSampleDto` thật (nối 05/08/2026, `collectedAt`/`disposedAt` ISO)
 * lẫn mock cũ (`dd/MM/yyyy HH:mm`), theo mẫu "widen tại component dùng chung"
 * đã áp dụng ở `kitchen-confirm-badge.tsx` (MF07).
 */
export interface PrintableSample {
  id?: string;
  dish?: string;
  mealShift?: string;
  meal?: string;
  servings?: number;
  weight?: string;
  container?: string;
  fridgeTemp?: number;
  collectedAt?: string;
  /** 11/08/2026 — ngày dự kiến lấy mẫu (biết trước, tem ĐẶT TRƯỚC chưa có `collectedAt`). */
  collectDate?: string;
  collectedBy?: string;
  disposedAt?: string;
  disposedBy?: string;
  disposalNote?: string;
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
      .law tbody tr { break-inside: avoid; page-break-inside: avoid; }
      .law th { font-size: 11px; }
      .law td.l { text-align: left; }
      .law .num td { font-style: italic; font-size: 10px; }
      .law-sheet { margin-bottom: 28px; }
      .mau4-box { border: 1px solid #000; padding: 14px 16px; max-width: 560px; break-inside: avoid; page-break-inside: avoid; }
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

/**
 * Mẫu số 4: Nhãn mẫu thức ăn lưu — khung đúng câu chữ Phụ lục 2. QR (mã mẫu) đi
 * kèm là bổ sung ngoài luật — không thay thế bất kỳ trường bắt buộc nào, chỉ để
 * tra cứu nhanh; mỗi nhãn vẫn đại diện ĐÚNG 1 mẫu/1 dụng cụ lưu riêng (Điều 7.1),
 * kể cả khi in gộp nhiều nhãn trên cùng một tờ (`Mau4LabelSheet`) để tiết kiệm giấy.
 */
export function Mau4Label({ sample }: { sample: PrintableSample }) {
  return (
    <div className="law-sheet">
      <p className="mau4-caption">Mẫu số 4: Nhãn mẫu thức ăn lưu</p>
      <div className="mau4-box" style={{ display: "flex", gap: 14, alignItems: "flex-start", maxWidth: 640 }}>
        <div style={{ flex: 1 }}>
          <p>Bữa ăn: {sample.mealShift ?? sample.meal} (sáng/trưa/tối).</p>
          <p>Tên mẫu thức ăn: {sample.dish}</p>
          <p>Thời gian lấy: {vnTimeWordsOrDateOnly(sample.collectedAt, sample.collectDate)}</p>
          {/* Họ tên in sẵn — khoảng trống phía trên để ký tay khi in ra */}
          <p style={{ marginBottom: 0 }}>Người lấy mẫu <i>(Họ tên và chữ ký)</i>:</p>
          <span className="sig-space" />
          <p style={{ marginBottom: 0, fontWeight: 600 }}>{sample.collectedBy}</p>
        </div>
        {sample.id && (
          <div style={{ textAlign: "center", flexShrink: 0 }}>
            <SampleQrCode value={sample.id} size={64} />
            <p style={{ margin: "2px 0 0", fontSize: 10 }}>{sample.id}</p>
          </div>
        )}
      </div>
    </div>
  );
}

/**
 * In GỘP nhiều nhãn Mẫu số 4 trên cùng một tờ (tiết kiệm giấy so với mở từng
 * mẫu in riêng) — mỗi nhãn vẫn đủ thông tin độc lập để cắt rời, dán riêng vào
 * ĐÚNG dụng cụ lưu của món đó (Điều 7.1/7.3 vẫn yêu cầu 1 dụng cụ/1 nhãn/1 món).
 */
export function Mau4LabelSheet({ samples }: { samples: PrintableSample[] }) {
  return (
    <div className="law-sheet" style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
      {samples.map((s) => (
        <div key={s.id} className="mau4-box" style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
          <div style={{ flex: 1 }}>
            <p className="mau4-caption" style={{ fontSize: 11 }}>Mẫu số 4: Nhãn mẫu thức ăn lưu</p>
            <p>Bữa ăn: {s.mealShift ?? s.meal} (sáng/trưa/tối).</p>
            <p>Tên mẫu thức ăn: {s.dish}</p>
            <p>Thời gian lấy: {vnTimeWordsOrDateOnly(s.collectedAt, s.collectDate)}</p>
            <p style={{ marginBottom: 0 }}>Người lấy mẫu <i>(Họ tên và chữ ký)</i>:</p>
            <span className="sig-space" />
            <p style={{ marginBottom: 0, fontWeight: 600 }}>{s.collectedBy}</p>
          </div>
          {s.id && (
            <div style={{ textAlign: "center", flexShrink: 0 }}>
              <SampleQrCode value={s.id} size={56} />
              <p style={{ margin: "2px 0 0", fontSize: 10 }}>{s.id}</p>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}

/** Mẫu số 5: Mẫu biểu theo dõi lưu và hủy mẫu thức ăn lưu — 12 cột. */
export function Mau5Sheet({
  samples,
  blankRows = 2,
}: {
  samples: PrintableSample[];
  blankRows?: number;
}) {
  const { facility, location } = useLawFacilityLabels();
  return (
    <div className="law-sheet">
      <div className="law-head">
        <div>
          <p>Tên cơ sở: {facility}</p>
          <p>Địa điểm kiểm tra: {location}</p>
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
              <td>{formatVnDateTime(s.collectedAt)}</td>
              <td>{s.disposedAt ? formatVnDateTime(s.disposedAt) : ""}</td>
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
