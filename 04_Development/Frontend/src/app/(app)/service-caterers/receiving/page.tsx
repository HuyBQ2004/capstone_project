"use client";

import { usePagedList } from "@/lib/api/hooks";
import { useState } from "react";
import Link from "next/link";
import {
  CalendarDays,
  Camera,
  Download,
  Eye,
  History,
  MapPin,
  PackageCheck,
  PackageX,
  Truck,
  X,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input, Label } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { api } from "@/lib/api/client";
import type { DeliveryDto, DeliveryPersonDto, ReceivingSessionDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/**
 * UC Record Ingredient Receiving — MF06 (Service Caterers).
 * BR-070: xác minh NCC Approved · BR-072: bắt buộc ảnh · BR-073/074: GPS +
 * timestamp tự động, trong khu vực cho phép.
 *
 * Rework 17/07/2026 (nhận xét Minh): GỘP màn "Lịch sử nhận hàng & Báo cáo"
 * (14_ReceivingHistoryAndReports) vào đây — tab chính chỉ hiện các chuyến giao
 * SẮP TỚI / chờ nhận; tab Lịch sử chứa các phiên đã hoàn tất + thống kê +
 * xuất báo cáo theo khoảng ngày (BR-151 truy xuất nguồn gốc).
 *
 * Nối backend thật 05/08/2026: `deliveries`/`receiving-sessions` đã scope theo trường qua
 * JWT (bỏ filter `school`, không tồn tại phía server). `Delivery.list()` VÀ
 * `ReceivingSession.list()` trước đây bỏ qua HOÀN TOÀN `filters`/`search`/`dateFrom`/
 * `dateTo` (bug backend đã vá cùng đợt, xem `03_FE_Full_Wiring_Master_Plan.md`) — nay
 * `not:status=COMPLETED` và khoảng ngày lọc đúng thật. `driver` (tên) không có ở
 * `DeliveryResponse` — chỉ `driverId` (mã) — tra tên qua `delivery-persons`.
 * `deliveryDate`→`receivedAt` (ISO), `lines[]` field tên khớp sẵn không cần đổi.
 */

/* ===== Tab "Sắp nhận hàng" — chuyến giao chưa hoàn tất của trường ===== */

function upcomingColumns(drivers: DeliveryPersonDto[]): ColumnDef<DeliveryDto, unknown>[] {
  return [
    { accessorKey: "id", header: "Mã chuyến giao", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    {
      accessorKey: "scheduledAt",
      header: "Thời gian giao dự kiến",
      cell: ({ row }) => formatVnDateTime(row.original.scheduledAt),
    },
    {
      id: "driver",
      header: "Tài xế · biển số",
      cell: ({ row }) => (
        <span className="text-sm">
          {drivers.find((d) => d.id === row.original.driverId)?.fullName ?? row.original.driverId ?? "—"} · {row.original.vehiclePlate}
        </span>
      ),
    },
    { accessorKey: "purchaseRequestId", header: "Đơn đặt hàng", cell: ({ row }) => row.original.purchaseRequestId ?? "—" },
    { accessorKey: "batches", header: "Số lô (BR-071)" },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex justify-end">
          {row.original.status === "DELIVERED" ? (
            <Link href={`/service-caterers/receiving/new?delivery=${row.original.id}`}>
              <Button size="sm">
                <PackageCheck /> Mở phiên nhận
              </Button>
            </Link>
          ) : (
            <span className="text-xs text-muted-foreground">Chờ hàng tới</span>
          )}
        </div>
      ),
    },
  ];
}

/* ===== Tab "Lịch sử" — phiên đã ghi nhận + báo cáo ===== */

function historyColumns(): ColumnDef<ReceivingSessionDto, unknown>[] {
  return [
    { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    {
      accessorKey: "supplier",
      header: "Nhà cung cấp",
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          <span className="max-w-56 truncate">{row.original.supplier}</span>
          {row.original.supplierStatus === "SUSPENDED" && <Badge variant="critical">Đình chỉ</Badge>}
        </div>
      ),
    },
    {
      accessorKey: "receivedAt",
      header: "Thời điểm giao",
      cell: ({ row }) => formatVnDateTime(row.original.receivedAt),
    },
    { accessorKey: "items", header: "Mặt hàng" },
    {
      id: "evidence",
      header: "Bằng chứng (BR-072/073)",
      cell: ({ row }) => (
        <span className="flex items-center gap-2 text-sm">
          <span className="flex items-center gap-1">
            <Camera className="size-3.5" /> {row.original.photosCount}
          </span>
          <span className="flex items-center gap-1">
            <MapPin className="size-3.5" />
            {row.original.gpsVerified ? <Badge variant="success">GPS</Badge> : <Badge variant="danger">Lỗi</Badge>}
          </span>
        </span>
      ),
    },
    { accessorKey: "receivedBy", header: "Người nhận" },
    { accessorKey: "status", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex justify-end">
          <Link href={`/service-caterers/receiving/${row.original.id}`}>
            <Button variant="ghost" size="sm">
              <Eye /> Xem chi tiết
            </Button>
          </Link>
        </div>
      ),
    },
  ];
}

export default function ReceivingPage() {
  const { data: receivingSessionsPage, isLoading: sessionsLoading } = usePagedList<ReceivingSessionDto>("receiving-sessions", { pageSize: 100 });
  const receivingSessions = receivingSessionsPage?.items ?? [];
  const { data: driversPage } = usePagedList<DeliveryPersonDto>("delivery-persons", { pageSize: 50 });
  const drivers = driversPage?.items ?? [];
  const [view, setView] = useState<"upcoming" | "history">("upcoming");
  // Khoảng ngày cho lịch sử + xuất báo cáo (yyyy-mm-dd — input type="date")
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [exporting, setExporting] = useState(false);

  const received = receivingSessions.filter((s) => s.status === "RECEIVED").length;
  // "Có lô từ chối" = phiên bị từ chối TOÀN BỘ + phiên nhận một phần (RECEIVED_PARTIAL)
  const rejected = receivingSessions.filter(
    (s) => s.status === "REJECTED" || s.status === "RECEIVED_PARTIAL"
  ).length;

  const rangeLabel =
    fromDate || toDate
      ? `từ ${fromDate ? fromDate.split("-").reverse().join("/") : "đầu sổ"} đến ${toDate ? toDate.split("-").reverse().join("/") : "nay"}`
      : "toàn bộ sổ";

  /** `GET /receiving-sessions/export` — cùng bộ lọc dateFrom/dateTo đang áp dụng ở bảng bên dưới. */
  async function exportReport() {
    setExporting(true);
    try {
      const res = await api.get<Blob>("/receiving-sessions/export", {
        params: { dateFrom: fromDate || undefined, dateTo: toDate || undefined, format: "xlsx" },
        responseType: "blob",
      });
      const objUrl = URL.createObjectURL(res.data);
      const a = document.createElement("a");
      a.href = objUrl;
      a.download = "lich-su-nhan-hang.xlsx";
      a.click();
      setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
      toast.success(`Đã xuất báo cáo nhận hàng (${rangeLabel}).`);
    } catch {
      toast.error("Không thể xuất báo cáo. Vui lòng thử lại.");
    } finally {
      setExporting(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Nhận nguyên liệu"
        description="Hệ thống tự xác minh NCC Approved trước khi mở phiên (BR-070); ảnh bắt buộc (BR-072); lịch sử + báo cáo nằm ở tab Lịch sử"
      />

      <Tabs value={view} onValueChange={(v) => setView(v as typeof view)} className="mb-4">
        <TabsList>
          <TabsTrigger value="upcoming">
            <Truck className="mr-1.5 size-4" /> Sắp nhận hàng
          </TabsTrigger>
          <TabsTrigger value="history">
            <History className="mr-1.5 size-4" /> Lịch sử & báo cáo
          </TabsTrigger>
        </TabsList>
      </Tabs>

      {view === "upcoming" ? (
        <DataTable<DeliveryDto>
          resource="deliveries"
          columns={upcomingColumns(drivers)}
          filters={{ "not:status": "COMPLETED" }}
          searchPlaceholder="Tìm theo mã chuyến, biển số…"
          emptyMessage="Không có chuyến giao nào sắp tới — tạo đơn đặt hàng để NCC lên lịch giao."
        />
      ) : (
        <>
          {/* Hai ô đầu đếm từ `receiving-sessions`; chưa về mà hiện 0 thì trông
              như chưa nhận hàng lần nào — hai bảng bên dưới đã có skeleton riêng. */}
          <div className="mb-6 grid gap-4 sm:grid-cols-3">
            {sessionsLoading ? (
              <>
                <Card className="h-24 animate-pulse bg-muted/40" />
                <Card className="h-24 animate-pulse bg-muted/40" />
              </>
            ) : (
              <>
                <StatCard label="Phiên nhận thành công" value={received} hint="30 ngày gần nhất" icon={PackageCheck} tone="success" />
                <StatCard label="Phiên có lô từ chối" value={rejected} hint="NCC được thông báo tự động" icon={PackageX} tone="warning" />
              </>
            )}
            {/*
              Ô "Tỷ lệ GPS hợp lệ" BỎ 07/08/2026: value="100%" ghi cứng, và hint nói
              "Geofence 200m" trong khi bán kính là CẤU HÌNH theo trường
              (`school_config.geofence_radius`, BR-028, 50–500m) — hai con số đều sai với
              trường nào không dùng mặc định. Muốn khôi phục: cần backend trả tỉ lệ phiên
              đạt geofence; hiện `receiving_sessions` không có field tổng hợp nào cho việc đó.
            */}
          </div>

          <DataTable<ReceivingSessionDto>
            resource="receiving-sessions"
            columns={historyColumns()}
            filters={{ dateFrom: fromDate || undefined, dateTo: toDate || undefined }}
            searchPlaceholder="Tìm theo mã phiên…"
            toolbar={
              <div className="flex flex-wrap items-center gap-1.5">
                <CalendarDays className="size-4 text-muted-foreground" />
                <Label htmlFor="rcv-from" className="text-xs text-muted-foreground">Từ</Label>
                <Input
                  id="rcv-from"
                  type="date"
                  className="w-36"
                  value={fromDate}
                  onChange={(e) => setFromDate(e.target.value)}
                />
                <Label htmlFor="rcv-to" className="text-xs text-muted-foreground">đến</Label>
                <Input
                  id="rcv-to"
                  type="date"
                  className="w-36"
                  value={toDate}
                  onChange={(e) => setToDate(e.target.value)}
                />
                {(fromDate || toDate) && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      setFromDate("");
                      setToDate("");
                    }}
                  >
                    <X /> Bỏ lọc
                  </Button>
                )}
                <Button variant="outline" size="sm" disabled={exporting} onClick={exportReport}>
                  <Download /> {exporting ? "Đang xuất…" : "Xuất báo cáo"}
                </Button>
              </div>
            }
          />
        </>
      )}

    </>
  );
}
