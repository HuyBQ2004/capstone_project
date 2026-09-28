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
  Plus,
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  CURRENT_SCHOOL,
  type Delivery,
  type ReceivingSession,
} from "@/lib/mock-data";

/**
 * UC Record Ingredient Receiving — MF06 (Service Caterers).
 * BR-070: xác minh NCC Approved · BR-072: bắt buộc ảnh · BR-073/074: GPS +
 * timestamp tự động, trong khu vực cho phép.
 *
 * Rework 17/07/2026 (nhận xét Minh): GỘP màn "Lịch sử nhận hàng & Báo cáo"
 * (14_ReceivingHistoryAndReports) vào đây — tab chính chỉ hiện các chuyến giao
 * SẮP TỚI / chờ nhận; tab Lịch sử chứa các phiên đã hoàn tất + thống kê +
 * xuất báo cáo theo khoảng ngày (BR-151 truy xuất nguồn gốc).
 */

/* ===== Tab "Sắp nhận hàng" — chuyến giao chưa hoàn tất của trường ===== */

const upcomingColumns: ColumnDef<Delivery, unknown>[] = [
  { accessorKey: "id", header: "Mã chuyến giao", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "scheduledAt", header: "Thời gian giao dự kiến" },
  {
    id: "driver",
    header: "Tài xế · biển số",
    cell: ({ row }) => (
      <span className="text-sm">
        {row.original.driver} · {row.original.vehiclePlate}
      </span>
    ),
  },
  { accessorKey: "purchaseRequestId", header: "Đơn đặt hàng", cell: ({ row }) => row.original.purchaseRequestId ?? "—" },
  { accessorKey: "batches", header: "Số lô (BR-071)" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
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

/* ===== Tab "Lịch sử" — phiên đã ghi nhận + báo cáo ===== */

function historyColumns(
  onDetail: (s: ReceivingSession) => void
): ColumnDef<ReceivingSession, unknown>[] {
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
    { accessorKey: "deliveryDate", header: "Thời điểm giao" },
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
    { accessorKey: "status", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex justify-end">
          <Button variant="ghost" size="sm" onClick={() => onDetail(row.original)}>
            <Eye /> Xem chi tiết
          </Button>
        </div>
      ),
    },
  ];
}

export default function ReceivingPage() {
  const { data: receivingSessionsPage } = usePagedList<ReceivingSession>("receiving", { pageSize: 100 });
  const receivingSessions = receivingSessionsPage?.items ?? [];
  const [view, setView] = useState<"upcoming" | "history">("upcoming");
  const [detail, setDetail] = useState<ReceivingSession | null>(null);
  // Khoảng ngày cho lịch sử + xuất báo cáo (yyyy-mm-dd — input type="date")
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");

  const received = receivingSessions.filter((s) => s.status === "RECEIVED").length;
  // "Có lô từ chối" = phiên bị từ chối TOÀN BỘ + phiên nhận một phần (RECEIVED_PARTIAL)
  const rejected = receivingSessions.filter(
    (s) => s.status === "REJECTED" || s.status === "RECEIVED_PARTIAL"
  ).length;

  const rangeLabel =
    fromDate || toDate
      ? `từ ${fromDate ? fromDate.split("-").reverse().join("/") : "đầu sổ"} đến ${toDate ? toDate.split("-").reverse().join("/") : "nay"}`
      : "toàn bộ sổ";

  return (
    <>
      <PageHeader
        title="Nhận nguyên liệu"
        description="Hệ thống tự xác minh NCC Approved trước khi mở phiên (BR-070); ảnh bắt buộc (BR-072); lịch sử + báo cáo nằm ở tab Lịch sử"
        actions={
          <Link href="/service-caterers/receiving/new">
            <Button>
              <Plus /> Phiên nhận hàng mới
            </Button>
          </Link>
        }
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
        <DataTable<Delivery>
          resource="deliveries"
          columns={upcomingColumns}
          filters={{ school: CURRENT_SCHOOL.shortName, "not:status": "COMPLETED" }}
          searchPlaceholder="Tìm theo mã chuyến, tài xế, đơn hàng…"
          emptyMessage="Không có chuyến giao nào sắp tới — tạo đơn đặt hàng để NCC lên lịch giao."
        />
      ) : (
        <>
          <div className="mb-6 grid gap-4 sm:grid-cols-3">
            <StatCard label="Phiên nhận thành công" value={received} hint="30 ngày gần nhất" icon={PackageCheck} tone="success" />
            <StatCard label="Phiên có lô từ chối" value={rejected} hint="NCC được thông báo tự động" icon={PackageX} tone="warning" />
            <StatCard label="Tỷ lệ GPS hợp lệ" value="100%" hint="Geofence 200m (BR-074)" icon={MapPin} tone="primary" />
          </div>

          <DataTable<ReceivingSession>
            resource="receiving"
            columns={historyColumns(setDetail)}
            filters={{ dateFrom: fromDate || undefined, dateTo: toDate || undefined }}
            searchPlaceholder="Tìm theo NCC, mã phiên, mã lô…"
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
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    toast.success(
                      `Đã xuất báo cáo nhận hàng (${rangeLabel}) — file PDF kèm ảnh bằng chứng và GPS log (BR-151).`
                    )
                  }
                >
                  <Download /> Xuất báo cáo
                </Button>
              </div>
            }
          />
        </>
      )}

      {/* Chi tiết phiên nhận hàng (nhận xét Minh 17/07 — cột Xem chi tiết) */}
      <Dialog open={detail != null} onOpenChange={(v) => !v && setDetail(null)}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Phiên nhận hàng {detail?.id}</DialogTitle>
            <DialogDescription>
              Hồ sơ phục vụ truy xuất nguồn gốc khi có sự cố (BR-151).
            </DialogDescription>
          </DialogHeader>
          {detail && (
            <div className="space-y-2 text-sm">
              {(
                [
                  ["Nhà cung cấp", detail.supplier],
                  ["Thời điểm giao", detail.deliveryDate],
                  ["Số mặt hàng nhận", String(detail.items)],
                  ["Người nhận", detail.receivedBy],
                  ["Ảnh bằng chứng (BR-072)", `${detail.photosCount} ảnh`],
                ] as const
              ).map(([label, value]) => (
                <div key={label} className="flex justify-between gap-4 border-b pb-2 last:border-0">
                  <span className="text-muted-foreground">{label}</span>
                  <span className="text-right font-medium">{value}</span>
                </div>
              ))}
              <div className="flex justify-between gap-4 border-b pb-2">
                <span className="text-muted-foreground">GPS (BR-073/074)</span>
                {detail.gpsVerified ? (
                  <Badge variant="success">Trong geofence 200m</Badge>
                ) : (
                  <Badge variant="danger">Lỗi (MSG-015)</Badge>
                )}
              </div>
              <div className="flex justify-between gap-4 border-b pb-2">
                <span className="text-muted-foreground">Kết quả</span>
                <StatusBadge status={detail.status} />
              </div>
              {/* Chi tiết nguyên liệu — lô (Minh 25/07/2026): phiên mới có `lines`,
                  phiên seed cũ fallback về danh sách mã lô trần (dedupe). */}
              {detail.lines && detail.lines.length > 0 ? (
                <div className="space-y-1.5 pt-1">
                  <span className="text-muted-foreground">Nguyên liệu — lô hàng (BR-071)</span>
                  <div className="space-y-1">
                    {detail.lines.map((l, i) => (
                      <div
                        key={`${l.batchId}-${i}`}
                        className={`rounded-md border px-3 py-2 ${l.rejected ? "border-destructive/50 bg-destructive/5" : ""}`}
                      >
                        <div className="flex items-center justify-between gap-2">
                          <span className="font-medium">{l.ingredient}</span>
                          <span className="flex items-center gap-1.5">
                            <Badge variant="neutral">{l.batchId}</Badge>
                            {l.rejected ? (
                              <Badge variant="danger">Trả về NCC</Badge>
                            ) : (
                              <Badge variant="success">Đã nhập kho</Badge>
                            )}
                          </span>
                        </div>
                        {l.rejected && l.rejectReason && (
                          <p className="mt-1 text-xs text-destructive">Lý do: {l.rejectReason}</p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="space-y-1.5 pt-1">
                  <span className="text-muted-foreground">Lô đã nhập kho (BR-071)</span>
                  <div className="flex flex-wrap gap-1">
                    {detail.batches.length > 0
                      ? [...new Set(detail.batches)].map((b) => (
                          <Badge key={b} variant="neutral">{b}</Badge>
                        ))
                      : "—"}
                  </div>
                </div>
              )}
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setDetail(null)}>Đóng</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
