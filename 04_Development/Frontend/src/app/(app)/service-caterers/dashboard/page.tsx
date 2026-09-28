"use client";

import Link from "next/link";
import {
  AlertTriangle,
  ArrowRight,
  CheckSquare,
  ClipboardCheck,
  FlaskConical,
  ShieldAlert,
  ShoppingCart,
  Thermometer,
  UtensilsCrossed,
} from "lucide-react";
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { usePagedList } from "@/lib/api/hooks";
import type { CorrectiveActionDto, FoodSampleDto, InventoryBatchDto } from "@/lib/api/types";
import {
  standardMenus,
  MEAL_TYPE_LABEL,
  type MenuItem,
  type MealType,
} from "@/lib/domain-utils";
import type { InspectionSessionDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

/** `ShelfStatus.IN_STOCK`/`quarantine` tách riêng ở backend thật — xem `inventory/page.tsx`. */
function displayStatus(b: Pick<InventoryBatchDto, "status" | "quarantine">): string {
  return b.quarantine ? "QUARANTINED" : (b.status ?? "");
}

/**
 * 01_ServiceCaterersDashboard — redesign 07/07/2026 (nhận xét Minh: bớt rối,
 * thêm biểu đồ). Biểu đồ vẽ SVG/HTML thuần theo skill dataviz:
 * - Line 1 series (suất ăn 7 ngày): màu primary, 2px, hover crosshair+tooltip.
 * - Bar ngang theo TRẠNG THÁI lô kho: status palette (BR-123 semantic cố định),
 *   mỗi dòng có nhãn + số trực tiếp (không dựa màu đơn thuần — đạt CVD floor).
 * Alerts gộp thành 1 card "Cảnh báo & việc cần làm" dạng dòng gọn.
 */

/* ===== Dữ liệu biểu đồ (mock — 7 ngày học gần nhất) ===== */

/*
 * `MEALS_7D` + `MealsLineChart` XOÁ 07/08/2026 — xem chú thích tại chỗ card bị bỏ bên
 * dưới. Đây là bộ dữ liệu mock DUY NHẤT còn render ra màn hình sau khi tầng adapter mock
 * (`mock-server.ts`/`db.ts`/`real-resources.ts`) đã bị gỡ.
 */

/* ===== Bar ngang trạng thái kho — HTML thuần, nhãn + số trực tiếp ===== */

function InventoryStatusBars() {
  // Đọc kho qua API: mảng seed đã bị clone khi dựng db nên không phản ánh lô Y tế
  // vừa cách ly — bảng "Cách ly (BR-149)" sẽ mãi hiện số của lúc nạp trang.
  const { data: batchPage, isLoading } = usePagedList<InventoryBatchDto>("inventory", { pageSize: 200 });
  const batches = batchPage?.items ?? [];
  const rows = [
    { key: "IN_STOCK", label: "Đang sử dụng", color: "var(--success)" },
    { key: "EXPIRING_SOON", label: "Sắp hết hạn (FEFO)", color: "var(--warning)" },
    { key: "EXPIRED", label: "Hết hạn — đã khóa", color: "var(--destructive)" },
    { key: "QUARANTINED", label: "Cách ly (BR-149)", color: "var(--critical)" },
  ].map((r) => ({
    ...r,
    count: batches.filter((b) => displayStatus(b) === r.key).length,
  }));
  /*
   * Chia theo TỔNG số lô, không theo lô nhiều nhất (sửa 08/08/2026).
   *
   * Trước đây `max = Math.max(...counts)` nên khi hai nhóm bằng nhau (2 đang dùng + 2 cách ly)
   * CẢ HAI thanh đều dài 100% — đọc thành "mỗi loại chiếm trọn kho", trong khi thực tế mỗi
   * loại là một nửa. Đây là bảng THÀNH PHẦN của kho ("N lô / tổng"), không phải bảng so sánh,
   * nên mẫu số đúng là tổng.
   */
  const total = Math.max(batches.length, 1);

  // Chưa có dữ liệu thì mọi bar dài 0 — trông y hệt "kho sạch, không lô nào cách
  // ly", trong khi BR-149 là thứ không được phép đọc nhầm.
  if (isLoading) {
    return (
      <div className="space-y-3" aria-busy="true">
        {rows.map((r) => (
          <div key={r.key}>
            <div className="mb-1 h-4 w-40 animate-pulse rounded bg-muted" />
            <div className="h-3 w-full animate-pulse rounded-sm bg-muted" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {rows.map((r) => (
        <div key={r.key} className="group">
          <div className="mb-1 flex items-baseline justify-between text-sm">
            <span>{r.label}</span>
            <span className="font-semibold tabular-nums">
              {r.count} lô
              {batches.length > 0 && (
                <span className="ml-1 font-normal text-muted-foreground">
                  ({Math.round((r.count / total) * 100)}%)
                </span>
              )}
            </span>
          </div>
          <div className="h-3 w-full rounded-sm bg-muted">
            <div
              className="h-3 rounded-r-[4px] transition-opacity group-hover:opacity-80"
              style={{
                width: `${(r.count / total) * 100}%`,
                background: r.color,
                minWidth: r.count > 0 ? 8 : 0,
              }}
              title={`${r.label}: ${r.count}/${batches.length} lô`}
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
  const { data: inspectionsPage, isLoading: inspectionsLoading } = usePagedList<InspectionSessionDto>("inspections", { pageSize: 100 });
  const inspections = inspectionsPage?.items ?? [];
  const { data: menusPage, isLoading: menusLoading } = usePagedList<MenuItem>("menus", { pageSize: 100 });
  // Cảnh báo xung đột chỉ trên thực đơn CHUNG của khối; thực đơn riêng không tính lại.
  const menus = standardMenus(menusPage?.items ?? []);
  const { data: capaListPage, isLoading: capaLoading } = usePagedList<CorrectiveActionDto>("corrective-actions", { pageSize: 100 });
  const capaList = capaListPage?.items ?? [];
  const { data: samplesPage, isLoading: samplesLoading } = usePagedList<FoodSampleDto>("samples", { pageSize: 200 });
  const samples = samplesPage?.items ?? [];
  const { data: batchPage, isLoading: inventoryLoading } = usePagedList<InventoryBatchDto>("inventory", { pageSize: 200 });
  const liveBatches = batchPage?.items ?? [];
  const expiring = liveBatches.filter(
    (b) => b.status === "EXPIRING_SOON" || b.status === "EXPIRED"
  );
  const quarantined = liveBatches.filter((b) => b.quarantine != null);
  /*
   * "Đang lưu" phải gồm CẢ mẫu `HOLD` (08/08/2026).
   *
   * `HOLD` = mẫu đang bị Y tế giữ để điều tra — nó vẫn nằm trong tủ và là mẫu BẮT BUỘC phải
   * còn (backend chặn hủy bằng SMP-112). Lọc mỗi `RETAINED` khiến đúng mẫu quan trọng nhất
   * biến mất khỏi ô đếm: đo được ô hiện "1" trong khi tủ có 2 mẫu.
   */
  const retainedSamples = samples.filter((s) => s.status === "RETAINED" || s.status === "HOLD");
  const heldSamples = samples.filter((s) => s.status === "HOLD");
  // Phiên kiểm thực của HÔM NAY (nếu có) — nuôi ô "Kiểm thực hôm nay" bên dưới.
  const todayIso = new Date().toISOString().slice(0, 10);
  const todayInspection = inspections.find((i) => i.date === todayIso);
  /*
   * `InspectionSessionResponse` không có `currentStep` — chỉ có `step1`/`step2`/`step3`
   * (trạng thái từng bước) và `overall`. "Đang ở bước mấy" = số bước đã nộp + 1, chặn ở 3.
   */
  const submittedSteps = todayInspection
    ? [todayInspection.step1, todayInspection.step2, todayInspection.step3].filter(Boolean).length
    : 0;
  const currentStep = Math.min(submittedSteps + 1, 3);
  // BR-216 bỏ SLA/hạn cứng nên không còn trạng thái quá hạn: việc cần làm của đơn
  // vị suất ăn là CAPA đã được BGH duyệt (chờ nhận việc) hoặc đang thực hiện.
  const overdueCapa = capaList.filter(
    (c) => c.status === "APPROVED_FOR_IMPLEMENTATION" || c.status === "IN_PROGRESS"
  );
  const pendingMenuConflicts = menus.filter(
    (m) => m.status === "PENDING_APPROVAL" && m.allergyConflicts > 0
  );
  /**
   * Cảnh báo sớm thiếu nguyên liệu (3 ngày tới) — ĐÃ BỎ khi nối backend thật 05/08/2026.
   * `upcomingShortages()` (mock) quét mảng `MENUS` TĨNH — không nhận tham số thực đơn thật
   * nào, nên nó ĐÃ disconnect khỏi dữ liệu thật từ trước cả đợt nối này. Thay thế đúng cần
   * gọi `GET /inventory/issue-plan` cho TỪNG (ngày × buổi) trong 3 ngày tới — N lần gọi, một
   * tính năng cần thiết kế riêng (xem `07_implement/03_FE_Full_Wiring_Master_Plan.md`), để
   * lại làm việc riêng thay vì cắm tạm dữ liệu sai.
   */
  const upcoming: { menu: { week?: string; grade?: string }; lines: { quantity: number; unit: string; ingredientName: string }[] }[] = [];

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
      text: `${quarantined.length} lô đang cách ly — không xuất kho tới khi sự cố liên quan đóng (BR-149)`,
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

  // Card "Việc cần làm" gộp cảnh báo cách ly/hết hạn/CAPA/xung đột dị ứng. Dữ
  // liệu chưa về ⇒ mảng `tasks` rỗng ⇒ card hiện như "không còn việc gì" — đúng
  // thứ nguy hiểm nhất trên màn này.
  const isLoading =
    inspectionsLoading || menusLoading || capaLoading || samplesLoading || inventoryLoading;

  if (isLoading) {
    return (
      <>
        <PageHeader title="Tổng quan vận hành bếp" />
        <LoadingSkeleton tiles={4} blocks={2} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title="Tổng quan vận hành bếp"
        // Ngày THẬT. Trước 07/08/2026 chỗ này ghi cứng "Thứ Năm, 02/07/2026 · Bữa trưa:
        // 1.850 suất kế hoạch" — một ngày đã trôi qua từ lâu, hiện y nguyên mỗi lần mở.
        description={new Date().toLocaleDateString("vi-VN", {
          weekday: "long",
          day: "2-digit",
          month: "2-digit",
          year: "numeric",
        })}
      />

      {/* Hàng 1 — 4 chỉ số chính */}
      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {/* Tính từ phiên kiểm thực THẬT của hôm nay. Trước đây ghi cứng "Bước 2/3" —
            cùng lỗi mà dashboard Y tế đã bỏ ô này vì nó (xem chú thích ở màn đó). */}
        <StatCard
          label="Kiểm thực hôm nay"
          value={todayInspection ? `Bước ${currentStep}/3` : "Chưa mở phiên"}
          hint={todayInspection ? (todayInspection.overall ?? "") : "Mở phiên trước khi phục vụ (BR-094)"}
          icon={ClipboardCheck}
          tone={todayInspection ? "info" : "warning"}
        />
        <StatCard
          label="Mẫu đang lưu"
          value={retainedSamples.length}
          hint={
            heldSamples.length > 0
              ? `${heldSamples.length} mẫu Y tế đang giữ để điều tra — không được hủy`
              : "Tủ lưu mẫu 4.2°C (2–8°C, BR-110)"
          }
          icon={FlaskConical}
          tone={heldSamples.length > 0 ? "warning" : "primary"}
        />
        {/*
          Ô "Phiên nhận hàng" BỎ 07/08/2026: value="1 đang mở" và hint "GreenFarm — 05:45
          sáng nay" đều ghi cứng — tên nhà cung cấp và giờ giao là dữ liệu bịa, hiện y hệt
          cho mọi trường. Màn này không đọc `receiving-sessions` nên không có gì để thay
          bằng số thật mà không thêm một truy vấn nữa; lối vào đã có ở menu Nhận nguyên
          liệu. Muốn khôi phục: đọc `GET /receiving-sessions` lọc phiên đang mở.
        */}
        <StatCard label="Cảnh báo kho" value={expiring.length + quarantined.length} hint="Sắp hết hạn / hết hạn / cách ly" icon={AlertTriangle} tone="warning" />
      </div>

      {/*
        Card "Suất ăn phục vụ — 7 ngày học gần nhất" BỎ 07/08/2026 cùng `MEALS_7D` và
        `MealsLineChart`: 7 điểm dữ liệu (1802…1850) là số BỊA khai cứng trong file, kèm
        chú thích giải thích một chỗ trũng "ngày 30/06 do kiểm thực Bước 2 không đạt" —
        sự kiện không có thật. Biểu đồ đứng cạnh các ô đọc dữ liệu THẬT nên không ai phân
        biệt được cái nào đo, cái nào bịa.
        Muốn khôi phục: cần một endpoint tổng hợp suất đã phát theo ngày (gộp
        `student_distributions` 7 ngày ở server — KHÔNG gọi `/summary` 7 lần từ client),
        rồi vẽ lại bằng đúng dữ liệu đó.
      */}
      <div className="mb-6 grid gap-6">
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
                  <span className="font-medium">{formatVnDate(i.date)}</span>
                  <span className="text-muted-foreground"> · {MEAL_TYPE_LABEL[(i.meal ?? "") as MealType] ?? i.meal}</span>
                </span>
                {i.coreTemp != null && (
                  <Badge variant={i.coreTemp >= 75 ? "success" : "danger"}>
                    <Thermometer className="mr-0.5 inline size-3" />
                    {i.coreTemp}°C
                  </Badge>
                )}
                <StatusBadge status={i.overall ?? ""} />
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>
    </>
  );
}
