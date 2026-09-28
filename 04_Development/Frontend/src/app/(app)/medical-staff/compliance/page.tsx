"use client";

/**
 * BẢNG GIÁM SÁT TUÂN THỦ ATTP — UC-027 (`23_ComplianceDashboard.md`), Medical Staff.
 *
 * ═══ VÒNG 7 — 27/07/2026 (Mục 14 `review_medicalstaff_compliance_kitcheninspection.md`) ═══
 * **Mục 14 THAY THẾ Mục 10.3** (không phải bổ sung song song): khuôn case-hub 3 vùng
 * ("Tình trạng hôm nay" / "Việc cần xử lý" / "Lịch sử và báo cáo") mượn từ Incident Detail
 * đã bị bác bỏ vì không hợp bản chất màn này:
 *
 *  · Incident Detail quản lý MỘT CA đang chạy qua state machine — mỗi trạng thái cần đúng
 *    1 CTA đưa ca tiến lên. Task-routing là bản chất của màn đó.
 *  · Compliance Dashboard không quản lý "một ca" nào — nó tổng hợp nhiều luồng vận hành
 *    độc lập (kiểm tra bếp, kiểm thực, lưu mẫu, côn trùng) chạy song song ở các module
 *    nguồn riêng. Nhúng "Việc cần xử lý" dạng điều hướng vào đây biến nó thành "bảng lối
 *    tắt", và vì mỗi lối tắt gần như luôn trùng đích với một card trạng thái ngay phía
 *    trên, sinh ra tình trạng 2 con đường tới cùng 1 nơi — người dùng đã tự phát hiện 2
 *    lần liên tiếp khi rà soát.
 *
 * **Vai trò mới**: dashboard THỐNG KÊ/GIÁM SÁT + điểm khởi đầu tạo báo cáo tổng hợp.
 * KHÔNG điều hướng để thực hiện thao tác nghiệp vụ — kiểm tra/kiểm thực/lưu mẫu vẫn thực
 * hiện tại đúng module nguồn. Bốn câu hỏi màn này phải trả lời ngay khi mở:
 *  (1) hôm nay các đầu việc ATTP đã hoàn thành đến đâu; (2) chỉ số trong kỳ tốt hay xấu;
 *  (3) có ngoại lệ nào cần chú ý; (4) có cần tạo/gửi báo cáo tổng hợp không.
 *
 * ═══ Nguyên tắc dữ liệu bắt buộc (Mục 14.4) ═══
 *  1. Không bảng danh sách chi tiết trên dashboard — **kể cả dạng rút gọn cột**; chỉ số +
 *     drill-down link.
 *  2. **Card "bình thường" không chiếm không gian cố định**: nhiệt độ tủ lưu mẫu khi trong
 *     ngưỡng chỉ là 1 dòng gọn trong khối "Lưu mẫu"; ngoài ngưỡng mới nổi thành 1 dòng
 *     đầy đủ ở "Ngoại lệ cần chú ý" (progressive disclosure — trạng thái tốt không cần
 *     diện tích màn hình).
 *  3. Kiểm soát côn trùng (BR-102): chưa chốt quy tắc dữ liệu thì KHÔNG hiển thị phần
 *     trăm và KHÔNG đưa vào bất kỳ phép tính tổng/tỷ lệ nào.
 *  4. Khám sức khỏe NV bếp là chỉ số ĐỊNH KỲ — không tính vào "Hoàn thành X/Y hôm nay";
 *     chỉ hiện cảnh báo khi quá hạn.
 *  5. Không suy ra "chưa kiểm tra hôm nay" từ một phiên gần nhất ở ngày khác — 2 sự kiện
 *     khác nhau, hiển thị tách bạch (`today` và `latestKitchen` là 2 biến riêng).
 *  6. Mọi số phải trả lời được 1 câu hỏi cụ thể — endpoint mock `/compliance-report`
 *     (KPI `summary.modules` cũ) bị loại khỏi màn này vì không gắn với câu hỏi nào và
 *     không đồng bộ bộ lọc kỳ.
 *
 * Biểu đồ dùng **CSS thuần**, không cài thư viện mới — đúng tiền lệ duy nhất của dự án
 * (`system-admin/usage-statistics`). Cài thư viện biểu đồ là quyết định ảnh hưởng nhiều
 * actor, không tự quyết ngầm trong phạm vi một màn (Mục 14.3 điểm 4).
 *
 * Căn cứ giữ nguyên: BR-091 · BR-097 · BR-100/104 · BR-102/103 · BR-110 · MSG-093.
 */

import { useState } from "react";
import Link from "next/link";
import {
  AlertTriangle,
  ClipboardCheck,
  Download,
  FileText,
  FlaskConical,
  Send,
  Utensils,
} from "lucide-react";
import { toast } from "sonner";
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { usePagedList } from "@/lib/api/hooks";
import type { FoodSampleDto, InspectionSessionDto, KitchenInspectionDto, SentReportDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { useAuthStore } from "@/lib/stores/auth-store";

/**
 * Hôm nay theo dd/MM/yyyy — TÍNH THẬT, không phải hằng số.
 *
 * Trước 08/08/2026 chỗ này là `const TODAY = "02/07/2026"` ("ngày vận hành của bản mock").
 * Hệ quả nặng hơn một cái nhãn sai: `TODAY` còn được dùng để CHỌN phiên kiểm thực, phiên kiểm
 * tra bếp và mẫu lưu "của hôm nay" (xem `todaySession`/`todayKitchen`/`todaySamples`). Đo thật
 * 08/08/2026: màn ghi tiêu đề "02/07/2026" và khối "Tình trạng công việc hôm nay" báo "Kiểm
 * tra bếp — Chưa thực hiện" trong khi phiên kiểm thực hôm đó ĐÃ Đạt cả 3 bước. Tức bảng giám
 * sát tuân thủ đang chấm một ngày cách đây hơn một tháng.
 */
function todayVn(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}
const DAY_MS = 24 * 3600 * 1000;

/** dd/MM/yyyy[ HH:mm] → timestamp UTC (so được với `<input type="date">`) */
function vnDateToTime(d: string): number | null {
  const m = d.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
  return m ? Date.UTC(Number(m[3]), Number(m[2]) - 1, Number(m[1])) : null;
}

function csvCell(v: string | number | boolean | null | undefined): string {
  const s = v == null ? "" : typeof v === "boolean" ? (v ? "Có" : "Không") : String(v);
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

function toCsv(headers: string[], rows: (string | number | boolean | null | undefined)[][]): string {
  return [headers, ...rows].map((r) => r.map(csvCell).join(",")).join("\r\n");
}

/**
 * Tải 1 tệp CSV. BOM là bắt buộc: thiếu nó, Excel trên Windows đọc UTF-8 thành mojibake
 * với toàn bộ tiếng Việt có dấu — mà đây là tệp nộp cho đoàn kiểm tra.
 */
function downloadCsv(fileName: string, content: string) {
  const blob = new Blob([`﻿${content}`], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = fileName;
  a.click();
  URL.revokeObjectURL(url);
}

type PeriodKey = "today" | "7d" | "30d" | "custom";

/** Một cột của biểu đồ CSS thuần (Mục 14.3 điểm 4 — không dùng thư viện biểu đồ). */
function BarColumn({
  label,
  value,
  max,
  caption,
  tone,
}: {
  label: string;
  value: number;
  max: number;
  caption?: string;
  tone: "success" | "warning" | "danger" | "primary";
}) {
  const pct = max > 0 ? Math.max(4, Math.round((value / max) * 100)) : 4;
  const bg =
    tone === "danger"
      ? "bg-destructive"
      : tone === "warning"
        ? "bg-warning"
        : tone === "success"
          ? "bg-success"
          : "bg-primary";
  return (
    <div className="flex min-w-0 flex-1 flex-col items-center gap-1">
      <span className="text-xs font-medium">{value}</span>
      <div className="flex h-28 w-full items-end justify-center">
        <div className={`w-full max-w-10 rounded-t ${bg}`} style={{ height: `${pct}%` }} />
      </div>
      <span className="w-full truncate text-center text-[11px] text-muted-foreground" title={label}>
        {label}
      </span>
      {caption && <span className="text-[11px] text-muted-foreground">{caption}</span>}
    </div>
  );
}

export default function ComplianceDashboardPage() {
  const TODAY = todayVn();
  // Trước 11/08/2026 hardcode "Ngô Thị Hà (Y tế)" ở dòng "Người xuất" của CSV — mọi Y tế
  // trường đều xuất báo cáo mang tên người khác.
  const userName = useAuthStore((s) => s.user?.name) ?? "—";
  const { data: inspData, isLoading: inspLoading } = usePagedList<InspectionSessionDto>("inspections", { pageSize: 50 });
  const { data: kitchenData, isLoading: kitchenLoading } = usePagedList<KitchenInspectionDto>("kitchen-inspections", { pageSize: 50 });
  /*
   * Vòng 9 Mục 17.2 (27/07/2026): resource `pest-control-log` đã bỏ hẳn. Không còn query,
   * không còn khối hiển thị, không còn tệp CSV riêng cho côn trùng — thông tin côn trùng
   * (nếu có) nằm trong 2 mục CLI-3-8/CLI-4-6 của chính phiên kiểm tra bếp.
   */
  const { data: sampleData, isLoading: samplesLoading } = usePagedList<FoodSampleDto>("samples", { pageSize: 100 });
  const { data: sentData } = usePagedList<SentReportDto>("sent-reports", { pageSize: 20 });

  /**
   * Mục 14.3 điểm 2 — bộ lọc kỳ CHỈ áp cho biểu đồ xu hướng + khối Ngoại lệ, đặt ngay
   * trên chúng. Khối "Tình trạng công việc hôm nay" luôn cố định là ngày hiện tại: trộn
   * hai phạm vi thời gian vào một màn mà không tách rõ chính là lỗi đã hạ màn
   * `compliance/history` ở Mục 13.1.
   */
  const [periodKey, setPeriodKey] = useState<PeriodKey>("7d");
  const [customFrom, setCustomFrom] = useState("");
  const [customTo, setCustomTo] = useState("");
  /** Kỳ của báo cáo tổng hợp — độc lập với bộ lọc kỳ của biểu đồ (2 mục đích khác nhau) */
  const [reportPeriod, setReportPeriod] = useState<"Daily" | "Weekly">("Weekly");
  const [exportFrom, setExportFrom] = useState("");
  const [exportTo, setExportTo] = useState("");

  // Chuẩn hoá NGAY sau khi fetch — `date` thật là ISO (`LocalDate.toString()`), phần còn lại
  // của file viết cho định dạng dd/MM/yyyy của mock; field khác cũng optional trên DTO thật
  // (mock cũ non-optional) nên gán default một lần ở đây thay vì rải `?? ` khắp file.
  const inspections = (inspData?.items ?? []).map((i) => ({
    ...i,
    date: formatVnDate(i.date),
    step1: i.step1 ?? "PENDING",
    step2: i.step2 ?? "PENDING",
    step3: i.step3 ?? "PENDING",
    overall: i.overall ?? "PENDING",
  }));
  const kitchen = (kitchenData?.items ?? []).map((k) => ({
    ...k,
    date: formatVnDate(k.date),
    score: k.score ?? 0,
    result: k.result ?? "PENDING",
    staffHealthValid: k.staffHealthValid ?? false,
  }));
  // `collectedAt` thật là ISO Instant (mock cũ `dd/MM/yyyy HH:mm`) — tách sẵn ngày/giờ VN
  // để dùng chung với `vnDateToTime`/`TODAY` như `inspections`/`kitchen` ở trên.
  const samples = (sampleData?.items ?? []).map((s) => ({
    ...s,
    dateVn: formatVnDate(s.collectedAt),
    timeVn: formatVnDateTime(s.collectedAt).split(" ")[1] ?? "—",
    fridgeTemp: s.fridgeTemp ?? 0,
  }));
  const lastSent = (sentData?.items ?? [])[0];

  const todayMs = vnDateToTime(TODAY) ?? Date.now();
  const periodLabel =
    periodKey === "today"
      ? "Hôm nay"
      : periodKey === "7d"
        ? "7 ngày gần nhất"
        : periodKey === "30d"
          ? "30 ngày gần nhất"
          : customFrom || customTo
            ? `${customFrom || "…"} → ${customTo || "…"}`
            : "Tùy chọn kỳ (chưa chọn ngày)";

  function inPeriod(dateStr: string): boolean {
    const t = vnDateToTime(dateStr);
    if (t == null) return false;
    if (periodKey === "today") return t === todayMs;
    if (periodKey === "7d") return t <= todayMs && t > todayMs - 7 * DAY_MS;
    if (periodKey === "30d") return t <= todayMs && t > todayMs - 30 * DAY_MS;
    if (customFrom && t < Date.parse(customFrom)) return false;
    if (customTo && t > Date.parse(customTo)) return false;
    return true;
  }

  // ── Khối 1: TÌNH TRẠNG HÔM NAY (cố định theo ngày hiện tại — Mục 14.3 điểm 2) ──
  const todaySession = inspections.find((i) => i.date === TODAY);
  const todayKitchen = kitchen.find((k) => k.date === TODAY);
  // Mục 14.4 điểm 5 — phiên gần nhất là SỰ KIỆN KHÁC, không được suy ra thành "hôm nay"
  const latestKitchen = kitchen[0];
  const todaySamples = samples.filter((s) => s.dateVn === TODAY);
  const tempSource = todaySamples.length > 0 ? todaySamples : samples.slice(0, 4);
  const tempOut = tempSource.filter((s) => s.fridgeTemp < 2 || s.fridgeTemp > 8);

  const stepsDone = todaySession
    ? [todaySession.step1, todaySession.step2, todaySession.step3].filter((s) => s === "PASSED").length
    : 0;
  const pendingStep = todaySession
    ? [todaySession.step1, todaySession.step2, todaySession.step3].findIndex((s) => s !== "PASSED") + 1
    : 0;
  const samplesValid = todaySamples.filter((s) => s.status !== "DISPOSED").length;

  /**
   * "Hoàn thành X/Y đầu mục hôm nay" — Y = 3 đầu việc VẬN HÀNH HẰNG NGÀY:
   * kiểm tra bếp · kiểm thực ba bước · lưu mẫu.
   * KHÔNG tính khám sức khỏe NV bếp (chỉ số định kỳ — Mục 14.4 điểm 4) và KHÔNG tính
   * kiểm soát côn trùng (chưa chốt quy tắc dữ liệu — Mục 14.4 điểm 3).
   */
  const dailyDone =
    (todayKitchen ? 1 : 0) +
    (todaySession && todaySession.overall !== "IN_PROGRESS" ? 1 : 0) +
    (todaySamples.length > 0 ? 1 : 0);

  // ── Khối 2: biểu đồ theo kỳ ──
  const kitchenInPeriod = kitchen.filter((k) => inPeriod(k.date)).slice().reverse();
  const maxScore = Math.max(100, ...kitchenInPeriod.map((k) => k.score));
  const sessionsInPeriod = inspections.filter((s) => inPeriod(s.date));
  const sessPassed = sessionsInPeriod.filter((s) => s.overall === "PASSED").length;
  const sessRunning = sessionsInPeriod.filter((s) => s.overall === "IN_PROGRESS").length;
  const sessFailed = sessionsInPeriod.filter((s) => s.overall === "FAILED").length;
  const maxSess = Math.max(1, sessPassed, sessRunning, sessFailed);

  /**
   * ── Khối 3: NGOẠI LỆ CẦN CHÚ Ý ──
   * Dựng từ dữ liệu, việc nào hết thì biến mất khỏi màn. Mỗi dòng có drill-down link về
   * đúng module nguồn — đây KHÔNG phải "việc cần xử lý" dạng task-routing đã bị bỏ, mà là
   * đường đi tới chỗ xem chi tiết của một ngoại lệ đã nêu.
   */
  const exceptions: { key: string; text: string; href: string; tone: "danger" | "warning" }[] = [
    ...(todaySession && todaySession.overall === "IN_PROGRESS"
      ? [
        {
          key: "step-pending",
          text: `Kiểm thực hôm nay chưa hoàn thành — đang ở bước ${pendingStep || 3}/3`,
          href: "/medical-staff/inspections",
          tone: "warning" as const,
        },
      ]
      : []),
    ...(sessFailed > 0
      ? [
        {
          key: "sess-failed",
          text: `${sessFailed} phiên kiểm thực có bước không đạt trong kỳ`,
          href: "/medical-staff/inspections",
          tone: "danger" as const,
        },
      ]
      : []),
    ...(!todayKitchen
      ? [
        {
          key: "no-kitchen",
          text: "Kiểm tra bếp hôm nay chưa có bản ghi",
          href: "/medical-staff/conduct-inspection",
          tone: "warning" as const,
        },
      ]
      : []),
    ...(latestKitchen && latestKitchen.score < 60
      ? [
        {
          key: "low-score",
          text: `Điểm vệ sinh phiên ${latestKitchen.id} (${latestKitchen.date}) đạt ${latestKitchen.score}% — dưới ngưỡng 60%, đã cảnh báo Ban giám hiệu`,
          href: "/medical-staff/conduct-inspection/history",
          tone: "danger" as const,
        },
      ]
      : []),
    // Mục 14.4 điểm 2 — nhiệt độ tủ CHỈ xuất hiện ở đây khi ngoài ngưỡng
    ...(tempOut.length > 0
      ? [
        {
          key: "temp",
          text: `Nhiệt độ tủ lưu mẫu ngoài ngưỡng 2–8°C: ${tempOut.map((s) => `${s.id ?? "?"} ${s.fridgeTemp}°C`).join(", ")}`,
          href: "/medical-staff/samples",
          tone: "danger" as const,
        },
      ]
      : []),
    // Mục 14.4 điểm 4 — khám SK NV bếp chỉ hiện khi KHÔNG hợp lệ
    ...(latestKitchen && !latestKitchen.staffHealthValid
      ? [
        {
          key: "staff-health",
          text: `Khám sức khỏe nhân viên bếp không hợp lệ tại phiên ${latestKitchen.id} (${latestKitchen.date})`,
          href: "/medical-staff/conduct-inspection/history",
          tone: "warning" as const,
        },
      ]
      : []),
    // Mục "biên bản kiểm tra bếp chưa đủ 2 xác nhận" ĐÃ BỎ (nối thật 05/08/2026) —
    // `recordConfirmedByMedicalStaffAt`/`recordConfirmedByCaterersAt` không tồn tại trên
    // `KitchenInspectionResponse` thật (tính năng biên bản ký điện tử chưa được xây ở
    // backend, xem ghi chú `conduct-inspection/[id]/record/page.tsx`). `confirmStatus`
    // hai chiều (BR-221, `POST /kitchen-inspections/{code}/confirm`) đã đủ dùng.
  ];

  /**
   * UC-115 AF1 / UC-117 — xuất dữ liệu tuân thủ ra CSV (giữ từ Mục 13.3 điểm 3, Mục 14
   * không thu hồi). Hai ràng buộc phải giữ:
   *  · **Không có cột "Loại kiểm tra"** — BR-208 Deprecated 09/07/2026.
   *  · **Xuất đầy đủ, không lọc ngoại lệ** — ngược với nguyên tắc exception-only của
   *    UC-185, vì đây là hồ sơ nộp cho audit/cơ quan ngoài, không phải bản tóm tắt.
   */
  function exportCompliance() {
    const inExportRange = (d: string) => {
      const t = vnDateToTime(d);
      if (t == null) return false;
      if (exportFrom && t < Date.parse(exportFrom)) return false;
      if (exportTo && t > Date.parse(exportTo)) return false;
      return true;
    };
    const rangeTag = exportFrom || exportTo ? `${exportFrom || "…"}_${exportTo || "…"}` : "toan_bo";
    const auditRows = kitchen.filter((k) => inExportRange(k.date));
    const tempRows = samples.filter((s) => inExportRange(s.dateVn));
    const avg =
      auditRows.length > 0
        ? Math.round(auditRows.reduce((sum, k) => sum + k.score, 0) / auditRows.length)
        : null;

    downloadCsv(
      `01_tong_quan_${rangeTag}.csv`,
      toCsv(
        ["Chỉ tiêu", "Giá trị"],
        [
          ["Khoảng thời gian", exportFrom || exportTo ? `${exportFrom || "không giới hạn"} → ${exportTo || "không giới hạn"}` : "Toàn bộ dữ liệu"],
          ["Số phiên kiểm tra vệ sinh bếp", auditRows.length],
          ["Điểm vệ sinh trung bình (%)", avg ?? "—"],
          ["Số phiên không đạt", auditRows.filter((k) => k.result === "FAILED").length],
          ["Số phép đo nhiệt độ tủ lưu mẫu", tempRows.length],
          ["Số phép đo ngoài ngưỡng 2–8°C", tempRows.filter((s) => s.fridgeTemp < 2 || s.fridgeTemp > 8).length],
          // Vòng 9 Mục 17.2 — bỏ dòng "Số bản ghi kiểm soát côn trùng"
          ["Thời điểm xuất", new Date().toLocaleString("vi-VN")],
          ["Người xuất", userName],
        ]
      )
    );
    downloadCsv(
      `02_kiem_tra_ve_sinh_bep_${rangeTag}.csv`,
      toCsv(
        // KHÔNG có cột "Loại" — BR-208 Deprecated.
        // Vòng 9 Mục 17.2 — bỏ luôn cột "Kiểm soát côn trùng hợp lệ": côn trùng không còn
        // là chỉ số độc lập, nó nằm trong kết quả 2 mục CLI-3-8/CLI-4-6 của chính phiên.
        ["Mã phiên", "Ngày", "Điểm (%)", "Người kiểm tra", "Khám SK NV bếp hợp lệ", "Kết quả", "Xác nhận của bếp"],
        auditRows.map((k) => [
          k.id,
          k.date,
          k.score,
          k.inspector,
          k.staffHealthValid,
          k.result === "PASSED" ? "Đạt" : "Không đạt",
          k.confirmStatus === "CONFIRMED"
            ? `Đã xác nhận${k.confirmedAt ? ` ${formatVnDateTime(k.confirmedAt)}` : ""}`
            : k.confirmStatus === "AWAITING_CATERERS_CONFIRM"
              ? "Chờ xác nhận"
              : "—",
        ])
      )
    );
    downloadCsv(
      `03_nhiet_do_tu_luu_mau_${rangeTag}.csv`,
      toCsv(
        ["Mã mẫu", "Ngày", "Giờ", "Vị trí lưu", "Món", "Nhiệt độ (°C)", "Người ghi nhận", "Kết quả"],
        tempRows.map((s) => [
          s.id ?? "",
          s.dateVn,
          s.timeVn,
          s.storageLocation ?? "Tủ lưu mẫu",
          s.dish ?? "",
          s.fridgeTemp,
          s.collectedBy ?? "",
          s.fridgeTemp >= 2 && s.fridgeTemp <= 8 ? "Đạt" : "Ngoài ngưỡng",
        ])
      )
    );
    // Tệp `04_kiem_soat_con_trung.csv` của bản minh hoạ Mục 13.5 ĐÃ BỎ ở Vòng 9 Mục 17.2 —
    // không còn nguồn dữ liệu nào để xuất, và côn trùng không còn là hồ sơ độc lập.
    toast.success(
      `Đã xuất 3 tệp CSV (${auditRows.length} phiên kiểm tra · ${tempRows.length} phép đo nhiệt độ).`
    );
  }

  const reportHref = `/medical-staff/compliance/report?period=${reportPeriod}`;

  // Khối 1 "Tình trạng công việc hôm nay" đếm phiên kiểm thực/kiểm tra bếp/mẫu
  // lưu. Chưa về mà render thì cả ba đều 0 — đọc thành "hôm nay chưa ai làm gì",
  // ngược hẳn ý nghĩa của một bảng giám sát tuân thủ.
  if (inspLoading || kitchenLoading || samplesLoading) {
    return (
      <>
        <PageHeader
          title="Bảng giám sát tuân thủ ATTP (UC-027)"
          description={`Thống kê & giám sát an toàn thực phẩm — ${TODAY}`}
        />
        <LoadingSkeleton blocks={3} />
      </>
    );
  }

  return (
    <>
      {/* Mục 14.3 điểm 1 — header KHÔNG có nút hành động nào. Nút "Tạo báo cáo tổng hợp"
          bỏ để chỉ còn 1 lối vào soạn báo cáo (khối cuối trang); nút "Kiểm tra an toàn bếp"
          bỏ vì dashboard không điều hướng để thực hiện thao tác nghiệp vụ (Mục 14.1). */}
      <PageHeader
        title="Bảng giám sát tuân thủ ATTP (UC-027)"
        description={`Thống kê & giám sát an toàn thực phẩm — ${TODAY}`}
      />

      {/* ═══════════ KHỐI 1 — TÌNH TRẠNG CÔNG VIỆC HÔM NAY (cố định theo ngày) ═══════════ */}
      <Card className="mb-4">
        <div className="space-y-3 p-5">
          <div className="flex flex-wrap items-baseline justify-between gap-2">
            <h2 className="text-base font-semibold">Tình trạng công việc hôm nay</h2>
            <span className="text-xs text-muted-foreground">
              Cố định theo ngày hiện tại — không đổi theo bộ lọc kỳ bên dưới
            </span>
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            {/* Kiểm tra bếp — Mục 14.4 điểm 5: "hôm nay" và "phiên gần nhất" là 2 dòng riêng */}
            <div className="rounded-xl border p-4">
              <p className="flex items-center gap-2 text-sm font-medium">
                <ClipboardCheck className="size-4" /> Kiểm tra bếp
              </p>
              <p className={`mt-1 text-lg font-semibold ${todayKitchen ? "" : "text-warning"}`}>
                {todayKitchen ? `Đã thực hiện · ${todayKitchen.score}%` : "Chưa thực hiện"}
              </p>
              <p className="text-xs text-muted-foreground">
                {latestKitchen
                  ? `Phiên gần nhất ${latestKitchen.date} · ${latestKitchen.score}%`
                  : "Chưa có phiên nào"}
              </p>
            </div>

            {/* Hồ sơ kiểm thực */}
            <div className="rounded-xl border p-4">
              <p className="flex items-center gap-2 text-sm font-medium">
                <Utensils className="size-4" /> Hồ sơ kiểm thực
              </p>
              <p className="mt-1 text-lg font-semibold">
                {todaySession ? `${stepsDone}/3 bước hoàn thành` : "Chưa có phiên"}
              </p>
              <p className="text-xs text-muted-foreground">
                {!todaySession
                  ? "Chưa có dữ liệu sản xuất hôm nay"
                  : todaySession.overall === "IN_PROGRESS"
                    ? `Bước ${pendingStep || 3} chờ xử lý`
                    : todaySession.overall === "FAILED"
                      ? "Có bước không đạt"
                      : "Đã hoàn tất, tất cả bước đạt"}
              </p>
            </div>

            {/* Lưu mẫu — nhiệt độ tủ khi TRONG ngưỡng chỉ là 1 dòng gọn ở đây (Mục 14.4 điểm 2) */}
            <div className="rounded-xl border p-4">
              <p className="flex items-center gap-2 text-sm font-medium">
                <FlaskConical className="size-4" /> Lưu mẫu
              </p>
              <p className="mt-1 text-lg font-semibold">
                {todaySamples.length > 0 ? `${samplesValid}/${todaySamples.length} hợp lệ` : "Chưa lưu mẫu"}
              </p>
              <p className="text-xs text-muted-foreground">
                {tempSource.length === 0
                  ? "Chưa có phép đo nhiệt độ"
                  : tempOut.length === 0
                    ? "Nhiệt độ tủ trong ngưỡng 2–8°C"
                    : `${tempOut.length} phép đo ngoài ngưỡng — xem mục Ngoại lệ`}
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3 border-t pt-3 text-sm">
            <span>
              Hoàn thành: <b>{dailyDone}/3</b> đầu mục
            </span>
            <span className="text-muted-foreground">·</span>
            <span>
              Cần chú ý:{" "}
              <b className={exceptions.length > 0 ? "text-warning" : ""}>{exceptions.length}</b>
            </span>
            <span
              className="ml-auto text-xs text-muted-foreground"
              title="Mục 14.4 điểm 3/4 — côn trùng chưa chốt quy tắc dữ liệu; khám SK NV bếp là chỉ số định kỳ"
            >
              Không tính kiểm soát côn trùng và khám sức khỏe NV bếp
            </span>
          </div>
        </div>
      </Card>

      {/* ── Bộ lọc kỳ: CHỈ áp cho biểu đồ + ngoại lệ (Mục 14.3 điểm 2) ── */}
      <div className="mb-3 flex flex-wrap items-end gap-2">
        {([
          { key: "today", label: "Hôm nay" },
          { key: "7d", label: "7 ngày" },
          { key: "30d", label: "30 ngày" },
          { key: "custom", label: "Tùy chọn kỳ" },
        ] as const).map((p) => (
          <Button
            key={p.key}
            size="sm"
            variant={periodKey === p.key ? "default" : "outline"}
            onClick={() => setPeriodKey(p.key)}
          >
            {p.label}
          </Button>
        ))}
        {periodKey === "custom" && (
          <>
            <div className="space-y-1">
              <Label htmlFor="pf-from" className="text-xs">Từ ngày</Label>
              <Input id="pf-from" type="date" value={customFrom} onChange={(e) => setCustomFrom(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="pf-to" className="text-xs">Đến ngày</Label>
              <Input id="pf-to" type="date" value={customTo} onChange={(e) => setCustomTo(e.target.value)} className="w-36" />
            </div>
          </>
        )}
        <span className="text-xs text-muted-foreground">Áp cho biểu đồ và mục ngoại lệ bên dưới</span>
      </div>

      {/* ═══════════ KHỐI 2 — BIỂU ĐỒ THEO KỲ (CSS thuần) ═══════════ */}
      <div className="mb-4 grid gap-4 lg:grid-cols-2">
        <Card>
          <div className="space-y-3 p-5">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <h3 className="text-sm font-semibold">Xu hướng điểm vệ sinh bếp</h3>
              <span className="text-xs text-muted-foreground">{periodLabel}</span>
            </div>
            {kitchenInPeriod.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">
                Không có phiên kiểm tra bếp nào trong kỳ.
              </p>
            ) : (
              <>
                <div className="flex items-end gap-2">
                  {kitchenInPeriod.map((k) => (
                    <BarColumn
                      key={k.id}
                      label={k.date.slice(0, 5)}
                      value={k.score}
                      max={maxScore}
                      tone={k.score < 60 ? "danger" : k.score < 80 ? "warning" : "success"}
                    />
                  ))}
                </div>
                <p className="text-xs text-muted-foreground" title="BR-100/BR-104">
                  Trục dọc là điểm checklist từng phiên (%). Ngưỡng cảnh báo 60% — MSG-093.
                </p>
              </>
            )}
          </div>
        </Card>

        <Card>
          <div className="space-y-3 p-5">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <h3 className="text-sm font-semibold">Kiểm thực ba bước</h3>
              <span className="text-xs text-muted-foreground">{periodLabel}</span>
            </div>
            {sessionsInPeriod.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">
                Không có phiên kiểm thực nào trong kỳ.
              </p>
            ) : (
              <>
                <div className="flex items-end gap-6 px-6">
                  <BarColumn label="Đạt" value={sessPassed} max={maxSess} tone="success" />
                  <BarColumn label="Đang thực hiện" value={sessRunning} max={maxSess} tone="primary" />
                  <BarColumn label="Không đạt" value={sessFailed} max={maxSess} tone="danger" />
                </div>
                <p className="text-xs text-muted-foreground" title="BR-091">
                  {sessionsInPeriod.length} phiên trong kỳ — tỷ lệ Đạt/Không đạt cho biết có xu
                  hướng rủi ro hay không.
                </p>
              </>
            )}
          </div>
        </Card>
      </div>

      {/* ═══════════ KHỐI 3 — NGOẠI LỆ CẦN CHÚ Ý ═══════════ */}
      <Card className="mb-4">
        <div className="space-y-3 p-5">
          <div className="flex flex-wrap items-baseline justify-between gap-2">
            <h2 className="text-base font-semibold">Ngoại lệ cần chú ý</h2>
            <span className="text-xs text-muted-foreground">{periodLabel}</span>
          </div>
          {exceptions.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              Không có ngoại lệ nào — mọi chỉ số tuân thủ đang trong ngưỡng.
            </p>
          ) : (
            <div className="space-y-2">
              {exceptions.map((e) => (
                <div key={e.key} className="flex flex-wrap items-center gap-3 rounded-lg border p-3 text-sm">
                  <AlertTriangle
                    className={`size-4 shrink-0 ${e.tone === "danger" ? "text-destructive" : "text-warning"}`}
                  />
                  <span className="min-w-0 flex-1">{e.text}</span>
                  <Link href={e.href} className="text-xs underline underline-offset-2">
                    Xem chi tiết
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </Card>

      {/* ═══════════ KHỐI 4 — BÁO CÁO TỔNG HỢP (lối vào DUY NHẤT để soạn) ═══════════ */}
      <Card>
        <div className="space-y-4 p-5">
          <div>
            <h2 className="text-base font-semibold">Báo cáo tổng hợp</h2>
            <p className="text-xs text-muted-foreground">
              Báo cáo lấy snapshot từ dữ liệu của kỳ đã chọn. Nội dung soạn tại màn riêng —
              dashboard không chứa form nghiệp vụ dài.
            </p>
          </div>

          <div className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="rp-period" className="text-xs">Kỳ báo cáo</Label>
              <Select
                id="rp-period"
                value={reportPeriod}
                onChange={(e) => setReportPeriod(e.target.value as "Daily" | "Weekly")}
                className="w-40"
              >
                <option value="Daily">Hôm nay</option>
                <option value="Weekly">Tuần này</option>
              </Select>
            </div>
            {/* Mục 14.3 điểm 3 — cả 2 nút đều điều hướng sang `compliance/report`, mang theo
                kỳ đã chọn; "Xem trước" mở đúng component tài liệu chỉ-đọc dùng chung với
                "Xem báo cáo" của UC-190. Không soạn inline tại dashboard. */}
            <Link href={`${reportHref}&preview=1`}>
              <Button variant="outline"><FileText /> Xem trước báo cáo</Button>
            </Link>
            <Link href={reportHref}>
              <Button><Send /> Chốt &amp; gửi Ban giám hiệu</Button>
            </Link>
          </div>

          <div className="flex flex-wrap items-center gap-2 border-t pt-3 text-sm">
            <span className="text-muted-foreground">Báo cáo đã gửi gần nhất:</span>
            {lastSent ? (
              <>
                <b>{lastSent.sentAt ? new Date(lastSent.sentAt).toLocaleDateString("vi-VN") : "—"}</b>
                <Badge variant="neutral">{lastSent.rangeLabel}</Badge>
              </>
            ) : (
              <span className="text-muted-foreground">chưa gửi lần nào</span>
            )}
            <Link href="/medical-staff/reports" className="ml-auto">
              <Button variant="outline" size="sm">Xem lịch sử</Button>
            </Link>
          </div>

          {/* ── UC-115/117 — xuất dữ liệu thô cho audit (giữ từ Mục 13.3 điểm 3) ── */}
          <div className="space-y-2 rounded-xl border p-4">
            <div className="flex flex-wrap items-end gap-2">
              <div className="min-w-0 flex-1">
                <p className="text-sm font-medium">Xuất dữ liệu tuân thủ</p>
                <p className="text-xs text-muted-foreground" title="UC-115 AF1 · UC-117">
                  Dữ liệu thô cho hồ sơ lưu trữ / đoàn kiểm tra — xuất <b>đầy đủ</b>, không lọc theo
                  ngoại lệ (khác báo cáo gửi Ban giám hiệu).
                </p>
              </div>
              <div className="space-y-1">
                <Label htmlFor="ex-from" className="text-xs">Từ ngày</Label>
                <Input id="ex-from" type="date" value={exportFrom} onChange={(e) => setExportFrom(e.target.value)} className="w-36" />
              </div>
              <div className="space-y-1">
                <Label htmlFor="ex-to" className="text-xs">Đến ngày</Label>
                <Input id="ex-to" type="date" value={exportTo} onChange={(e) => setExportTo(e.target.value)} className="w-36" />
              </div>
              <Button variant="outline" onClick={exportCompliance}>
                <Download /> Xuất CSV
              </Button>
            </div>
            <p className="text-xs text-muted-foreground">
              Tải về 3 tệp: tổng quan · kiểm tra vệ sinh bếp · nhiệt độ tủ lưu mẫu. Không chọn
              ngày = xuất toàn bộ dữ liệu đang có.
            </p>
          </div>
        </div>
      </Card>
    </>
  );
}
