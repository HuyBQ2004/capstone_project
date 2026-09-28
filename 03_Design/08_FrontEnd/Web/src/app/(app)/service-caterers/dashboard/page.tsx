"use client";

import { useState } from "react";
import Link from "next/link";
import {
  AlertTriangle,
  ArrowRight,
  CheckSquare,
  ClipboardCheck,
  FlaskConical,
  PackageCheck,
  ShieldAlert,
  ShoppingCart,
  Thermometer,
  UtensilsCrossed,
} from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { usePagedList } from "@/lib/api/hooks";
import {
  batchDisplayStatus,
  type InventoryBatch,
  upcomingShortages,
  standardMenus,
  type InventoryTransaction,
  type FoodSample,
  type CorrectiveAction,
  type MenuItem,
  type InspectionSession,
} from "@/lib/mock-data";

/**
 * 01_ServiceCaterersDashboard — redesign 07/07/2026 (nhận xét Minh: bớt rối,
 * thêm biểu đồ). Biểu đồ vẽ SVG/HTML thuần theo skill dataviz:
 * - Line 1 series (suất ăn 7 ngày): màu primary, 2px, hover crosshair+tooltip.
 * - Bar ngang theo TRẠNG THÁI lô kho: status palette (BR-123 semantic cố định),
 *   mỗi dòng có nhãn + số trực tiếp (không dựa màu đơn thuần — đạt CVD floor).
 * Alerts gộp thành 1 card "Cảnh báo & việc cần làm" dạng dòng gọn.
 */

/* ===== Dữ liệu biểu đồ (mock — 7 ngày học gần nhất) ===== */

const MEALS_7D = [
  { label: "24/06", day: "T4", value: 1802 },
  { label: "25/06", day: "T5", value: 1815 },
  { label: "26/06", day: "T6", value: 1788 },
  { label: "29/06", day: "T2", value: 1830 },
  { label: "30/06", day: "T3", value: 1764 }, // dip: bước 2 kiểm thực Failed, 1 lô bị chặn
  { label: "01/07", day: "T4", value: 1841 },
  { label: "02/07", day: "T5", value: 1850 },
];

/* ===== Line chart 1 series — SVG thuần, hover crosshair + tooltip ===== */

function MealsLineChart() {
  const [hover, setHover] = useState<number | null>(null);

  const VB_W = 600;
  const VB_H = 220;
  const PAD = { l: 46, r: 14, t: 14, b: 30 };
  const innerW = VB_W - PAD.l - PAD.r;
  const innerH = VB_H - PAD.t - PAD.b;

  const values = MEALS_7D.map((d) => d.value);
  const yMin = Math.floor((Math.min(...values) - 30) / 50) * 50; // 1700
  const yMax = Math.ceil((Math.max(...values) + 20) / 50) * 50; // 1900
  const x = (i: number) => PAD.l + (i / (MEALS_7D.length - 1)) * innerW;
  const y = (v: number) => PAD.t + (1 - (v - yMin) / (yMax - yMin)) * innerH;

  const linePath = MEALS_7D.map((d, i) => `${i === 0 ? "M" : "L"}${x(i)},${y(d.value)}`).join(" ");
  const areaPath = `${linePath} L${x(MEALS_7D.length - 1)},${PAD.t + innerH} L${x(0)},${PAD.t + innerH} Z`;

  const yTicks = [yMin, (yMin + yMax) / 2, yMax];

  function onMove(e: React.MouseEvent<SVGSVGElement>) {
    const rect = e.currentTarget.getBoundingClientRect();
    const px = ((e.clientX - rect.left) / rect.width) * VB_W;
    const i = Math.round(((px - PAD.l) / innerW) * (MEALS_7D.length - 1));
    setHover(Math.max(0, Math.min(MEALS_7D.length - 1, i)));
  }

  const h = hover != null ? MEALS_7D[hover] : null;

  return (
    <div className="relative">
      <svg
        viewBox={`0 0 ${VB_W} ${VB_H}`}
        className="h-auto w-full"
        role="img"
        aria-label="Số suất ăn phục vụ 7 ngày học gần nhất"
        onMouseMove={onMove}
        onMouseLeave={() => setHover(null)}
      >
        {/* Grid ngang recessive + nhãn trục y (text token, không màu series) */}
        {yTicks.map((t) => (
          <g key={t}>
            <line
              x1={PAD.l}
              x2={VB_W - PAD.r}
              y1={y(t)}
              y2={y(t)}
              style={{ stroke: "var(--border)" }}
              strokeWidth={1}
            />
            <text
              x={PAD.l - 8}
              y={y(t) + 3.5}
              textAnchor="end"
              fontSize={11}
              style={{ fill: "var(--muted-foreground)" }}
            >
              {t.toLocaleString("vi-VN")}
            </text>
          </g>
        ))}
        {/* Nhãn trục x */}
        {MEALS_7D.map((d, i) => (
          <text
            key={d.label}
            x={x(i)}
            y={VB_H - 8}
            textAnchor="middle"
            fontSize={11}
            style={{ fill: "var(--muted-foreground)" }}
          >
            {d.label}
          </text>
        ))}

        {/* Area mờ + line 2px màu primary (1 series — không cần legend) */}
        <path d={areaPath} style={{ fill: "var(--primary)" }} opacity={0.08} />
        <path
          d={linePath}
          fill="none"
          style={{ stroke: "var(--primary)" }}
          strokeWidth={2}
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        {/* Crosshair + marker khi hover */}
        {h && hover != null && (
          <g>
            <line
              x1={x(hover)}
              x2={x(hover)}
              y1={PAD.t}
              y2={PAD.t + innerH}
              style={{ stroke: "var(--muted-foreground)" }}
              strokeWidth={1}
              strokeDasharray="3 3"
            />
            <circle
              cx={x(hover)}
              cy={y(h.value)}
              r={5}
              style={{ fill: "var(--primary)", stroke: "var(--card)" }}
              strokeWidth={2}
            />
          </g>
        )}
      </svg>

      {/* Tooltip theo điểm gần nhất */}
      {h && hover != null && (
        <div
          className="pointer-events-none absolute z-10 -translate-x-1/2 -translate-y-full rounded-md border bg-card px-2.5 py-1.5 text-xs shadow-md"
          style={{
            left: `${(x(hover) / VB_W) * 100}%`,
            top: `${(y(h.value) / VB_H) * 100}%`,
            marginTop: -8,
          }}
        >
          <p className="font-medium">{h.day} {h.label}</p>
          <p className="text-muted-foreground">{h.value.toLocaleString("vi-VN")} suất phục vụ</p>
        </div>
      )}
    </div>
  );
}

/* ===== Bar ngang trạng thái kho — HTML thuần, nhãn + số trực tiếp ===== */

function InventoryStatusBars() {
  // Đọc kho qua API: mảng seed đã bị clone khi dựng db nên không phản ánh lô Y tế
  // vừa cách ly — bảng "Cách ly (BR-149)" sẽ mãi hiện số của lúc nạp trang.
  const { data: batchPage } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
  const batches = batchPage?.items ?? [];
  const rows = [
    { key: "ACTIVE", label: "Đang sử dụng", color: "var(--success)" },
    { key: "EXPIRING_SOON", label: "Sắp hết hạn (FEFO)", color: "var(--warning)" },
    { key: "EXPIRED", label: "Hết hạn — đã khóa", color: "var(--destructive)" },
    { key: "QUARANTINED", label: "Cách ly (BR-149)", color: "var(--critical)" },
  ].map((r) => ({
    ...r,
    count: batches.filter((b) => batchDisplayStatus(b) === r.key).length,
  }));
  const max = Math.max(...rows.map((r) => r.count), 1);

  return (
    <div className="space-y-3">
      {rows.map((r) => (
        <div key={r.key} className="group">
          <div className="mb-1 flex items-baseline justify-between text-sm">
            <span>{r.label}</span>
            <span className="font-semibold tabular-nums">{r.count} lô</span>
          </div>
          <div className="h-3 w-full rounded-sm bg-muted">
            <div
              className="h-3 rounded-r-[4px] transition-opacity group-hover:opacity-80"
              style={{
                width: `${(r.count / max) * 100}%`,
                background: r.color,
                minWidth: r.count > 0 ? 8 : 0,
              }}
              title={`${r.label}: ${r.count} lô`}
            />
          </div>
        </div>
      ))}
      <p className="pt-1 text-xs text-muted-foreground">
        Tổng {batches.length} lô trong kho ·{" "}
        <Link href="/service-caterers/inventory" className="font-medium text-primary underline">
          mở kho nguyên liệu
        </Link>
      </p>
    </div>
  );
}

/* ===== Trang chính ===== */

export default function ServiceCaterersDashboard() {
  const { data: inspectionsPage } = usePagedList<InspectionSession>("inspections", { pageSize: 100 });
  const inspections = inspectionsPage?.items ?? [];
  const { data: menusPage } = usePagedList<MenuItem>("menus", { pageSize: 100 });
  // Cảnh báo xung đột chỉ trên thực đơn CHUNG của khối; thực đơn riêng không tính lại.
  const menus = standardMenus(menusPage?.items ?? []);
  const { data: capaListPage } = usePagedList<CorrectiveAction>("corrective-actions", { pageSize: 100 });
  const capaList = capaListPage?.items ?? [];
  const { data: samplesPage } = usePagedList<FoodSample>("samples", { pageSize: 200 });
  const samples = samplesPage?.items ?? [];
  // Giao dịch kho SỐNG — các hàm tính FEFO/thiếu hàng mặc định dùng mảng SEED,
  // không truyền vào thì màn hình tính trên tồn lúc nạp module.
  const { data: txPage } = usePagedList<InventoryTransaction>("inventory-transactions", {
    pageSize: 500,
  });
  const liveTx = txPage?.items ?? [];
  const { data: batchPage } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
  const liveBatches = batchPage?.items ?? [];
  const expiring = liveBatches.filter(
    (b) => b.status === "EXPIRING_SOON" || b.status === "EXPIRED"
  );
  const quarantined = liveBatches.filter((b) => b.quarantine != null);
  const retainedSamples = samples.filter((s) => s.status === "RETAINED");
  // BR-216 bỏ SLA/hạn cứng nên không còn trạng thái quá hạn: việc cần làm của đơn
  // vị suất ăn là CAPA đã được BGH duyệt (chờ nhận việc) hoặc đang thực hiện.
  const overdueCapa = capaList.filter(
    (c) => c.status === "APPROVED_FOR_IMPLEMENTATION" || c.status === "IN_PROGRESS"
  );
  const pendingMenuConflicts = menus.filter(
    (m) => m.status === "PENDING_APPROVAL" && m.allergyConflicts > 0
  );
  /**
   * Cảnh báo sớm thiếu nguyên liệu: quét thực đơn đã duyệt của 3 ngày tới.
   * Trước đây thiếu hàng chỉ lộ ra khi bấm Xuất kho — tức sát giờ nấu, quá muộn
   * để đặt (NCC cần ≥24h, BR-071). Thực đơn vốn phải lập trước ≥3 ngày (BR-061)
   * nên dữ liệu để cảnh báo đã có sẵn.
   */
  const upcoming = upcomingShortages(3, liveTx);

  // Việc cần làm — gộp mọi cảnh báo về 1 card dòng gọn (bớt rối so với alert lớn)
  const tasks = [
    ...upcoming.map((u) => ({
      icon: ShoppingCart,
      chip: "bg-warning-soft text-warning",
      text: `Thực đơn ${u.menu.week} (${u.menu.grade}) thiếu ${u.lines
        .slice(0, 3)
        .map((l) => `${l.quantity} ${l.unit} ${l.ingredientName.toLowerCase()}`)
        .join(", ")}${
        u.lines.length > 3 ? ` và ${u.lines.length - 3} nguyên liệu khác` : ""
      } — đặt hàng trước khi tới ngày phục vụ (BR-071/BR-077)`,
      href: "/service-caterers/inventory/issue",
    })),
    quarantined.length > 0 && {
      icon: ShieldAlert,
      chip: "bg-critical-soft text-critical",
      text: `${quarantined.length} lô đang cách ly do sự cố INC-2026-014 — không xuất kho tới khi sự cố đóng (BR-149)`,
      href: "/service-caterers/inventory",
    },
    expiring.length > 0 && {
      icon: AlertTriangle,
      chip: "bg-warning-soft text-warning",
      text: `${expiring.length} lô sắp hết hạn/hết hạn — xuất trước theo FEFO (BR-075, MSG-052)`,
      href: "/service-caterers/inventory",
    },
    overdueCapa.length > 0 && {
      icon: CheckSquare,
      chip: "bg-destructive-soft text-destructive",
      text: `${overdueCapa.length} hành động khắc phục cần xử lý — nhận việc hoặc nộp bằng chứng (BR-096)`,
      href: "/service-caterers/corrective-actions",
    },
    pendingMenuConflicts.length > 0 && {
      icon: UtensilsCrossed,
      chip: "bg-info-soft text-info",
      text: `${pendingMenuConflicts.length} thực đơn chờ duyệt còn xung đột dị ứng — chuẩn bị món thay thế (BR-063)`,
      href: "/service-caterers/menus",
    },
  ].filter(Boolean) as {
    icon: typeof AlertTriangle;
    chip: string;
    text: string;
    href: string;
  }[];

  return (
    <>
      <PageHeader
        title="Tổng quan vận hành bếp"
        description="Thứ Năm, 02/07/2026 · Bữa trưa: 1.850 suất kế hoạch"
      />

      {/* Hàng 1 — 4 chỉ số chính */}
      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Kiểm thực hôm nay" value="Bước 2/3" hint="Bữa trưa đang chế biến" icon={ClipboardCheck} tone="info" />
        <StatCard label="Mẫu đang lưu" value={retainedSamples.length} hint="Tủ lưu mẫu 4.2°C (2–8°C, BR-110)" icon={FlaskConical} tone="primary" />
        <StatCard label="Phiên nhận hàng" value="1 đang mở" hint="GreenFarm — 05:45 sáng nay" icon={PackageCheck} tone="success" />
        <StatCard label="Cảnh báo kho" value={expiring.length + quarantined.length} hint="Sắp hết hạn / hết hạn / cách ly" icon={AlertTriangle} tone="warning" />
      </div>

      {/* Hàng 2 — biểu đồ */}
      <div className="mb-6 grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader className="pb-2">
            <CardTitle>Suất ăn phục vụ — 7 ngày học gần nhất</CardTitle>
          </CardHeader>
          <CardContent>
            <MealsLineChart />
            <p className="mt-1 text-xs text-muted-foreground">
              Ngày 30/06 giảm còn 1.764 suất do kiểm thực Bước 2 không đạt, 1 lô bị chặn
              phục vụ (BR-094).
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle>Kho theo trạng thái lô (BR-205)</CardTitle>
          </CardHeader>
          <CardContent>
            <InventoryStatusBars />
          </CardContent>
        </Card>
      </div>

      {/* Hàng 3 — việc cần làm + kiểm thực gần đây (dạng dòng gọn) */}
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle>Cảnh báo & việc cần làm ({tasks.length})</CardTitle>
          </CardHeader>
          <CardContent className="space-y-1.5">
            {tasks.map((t) => (
              <Link
                key={t.text}
                href={t.href}
                className="flex items-center gap-3 rounded-lg border p-2.5 transition-colors hover:bg-muted/60"
              >
                <span className={`flex size-8 shrink-0 items-center justify-center rounded-lg ${t.chip}`}>
                  <t.icon className="size-4" />
                </span>
                <span className="flex-1 text-sm">{t.text}</span>
                <ArrowRight className="size-4 shrink-0 text-muted-foreground" />
              </Link>
            ))}
            {tasks.length === 0 && (
              <p className="text-sm text-muted-foreground">Không có cảnh báo nào — vận hành ổn định.</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex-row items-center justify-between pb-2">
            <CardTitle>Kiểm thực ba bước gần đây</CardTitle>
            <Link href="/service-caterers/inspections">
              <Button variant="ghost" size="sm">Xem tất cả →</Button>
            </Link>
          </CardHeader>
          <CardContent className="space-y-1.5">
            {inspections.slice(0, 4).map((i) => (
              <Link
                key={i.id}
                href={`/service-caterers/inspections/${i.id}`}
                className="flex items-center gap-3 rounded-lg border p-2.5 transition-colors hover:bg-muted/60"
              >
                <span className="flex-1 text-sm">
                  <span className="font-medium">{i.date}</span>
                  <span className="text-muted-foreground"> · {i.meal}</span>
                </span>
                {i.coreTemp != null && (
                  <Badge variant={i.coreTemp >= 75 ? "success" : "danger"}>
                    <Thermometer className="mr-0.5 inline size-3" />
                    {i.coreTemp}°C
                  </Badge>
                )}
                <StatusBadge status={i.overall} />
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>
    </>
  );
}
