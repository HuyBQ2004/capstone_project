"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Ban, RefreshCw } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert } from "@/components/ui/alert";
import { Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, usePatchAction, useUpdate, usePagedList } from "@/lib/api/hooks";
import type { DeliveryDto, DeliveryPersonDto, SupplierBatchDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";

/**
 * Screen Spec 20/23/24 — Delivery Detail / Update Delivery Status / View Delivery Batch Records.
 * UC-039 (Manage Deliveries), UC-092 (Update Delivery Status), UC-109 (View Delivery Batch Records).
 * BR-135: trễ >30 phút so với lịch → cảnh báo Service Caterers (MSG-065) · MSG-029: cập nhật thành công.
 * Trạng thái theo Screen Spec 23: Dispatched / In Transit / Delayed / Delivered.
 *
 * Nối backend thật 05/08/2026: `PATCH /deliveries/{code}/status` — KHÔNG `PUT` — chỉ nhận
 * `{status}`, server tự set `statusUpdatedAt` (D9, không nhận ngày giờ tự nhập). `driver`
 * (tên) không có ở response — chỉ `driverId` (mã) — tra tên qua `delivery-persons`.
 */

const NEXT_STATUSES = ["DISPATCHED", "IN_TRANSIT", "DELAYED", "DELIVERED"] as const;

const STATUS_LABEL: Record<(typeof NEXT_STATUSES)[number], string> = {
  DISPATCHED: "Đã xuất kho (Dispatched)",
  IN_TRANSIT: "Đang giao (In Transit)",
  DELAYED: "Trễ lịch (Delayed)",
  DELIVERED: "Đã giao (Delivered)",
};

function UpdateStatusDialog({
  delivery,
  open,
  onOpenChange,
}: {
  delivery: DeliveryDto;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const patchStatus = usePatchAction<DeliveryDto>("deliveries");
  const [status, setStatus] = useState<(typeof NEXT_STATUSES)[number]>("DISPATCHED");

  function submit() {
    patchStatus.mutate(
      { id: delivery.id!, action: "status", body: { status } },
      {
        onSuccess: () => {
          if (status === "DELAYED") {
            toast.warning("MSG-065: chuyến giao trễ so với lịch — hệ thống tự cảnh báo Service Caterers (BR-135)");
          }
          toast.success("MSG-029: cập nhật trạng thái thành công, đã ghi audit trail (UC-092)");
          onOpenChange(false);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cập nhật trạng thái chuyến {delivery.id}</DialogTitle>
          <DialogDescription>
            UC-092 — lệch &gt;30 phút so với lịch sẽ tự động gửi cảnh báo MSG-065 tới
            Service Caterers (BR-135). Thời điểm cập nhật do hệ thống tự ghi.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="flex items-center gap-2 text-sm">
            <span className="text-muted-foreground">Trạng thái hiện tại:</span>
            <StatusBadge status={delivery.status ?? ""} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="u-status">Trạng thái mới *</Label>
            <Select id="u-status" value={status} onChange={(e) => setStatus(e.target.value as (typeof NEXT_STATUSES)[number])}>
              {NEXT_STATUSES.map((s) => (
                <option key={s} value={s}>{STATUS_LABEL[s]}</option>
              ))}
            </Select>
          </div>
          {status === "DELAYED" && (
            <Alert variant="warning" title="MSG-065">
              Chọn “Trễ lịch” sẽ kích hoạt cảnh báo tự động tới Service Caterers (BR-135).
            </Alert>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={patchStatus.isPending} onClick={submit}>Lưu trạng thái</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function DeliveryDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatchDto>("supplier-batches", { pageSize: 200 });
  const supplierBatches = supplierBatchesPage?.items ?? [];
  const { data: driversPage } = usePagedList<DeliveryPersonDto>("delivery-persons", { pageSize: 50 });
  const drivers = driversPage?.items ?? [];
  const { id } = use(params);
  const { data: delivery, isLoading } = useDetail<DeliveryDto>("deliveries", id);
  const patchStatus = usePatchAction<DeliveryDto>("deliveries");
  const updateBatch = useUpdate<{ id: string } & Partial<SupplierBatchDto>>("supplier-batches");
  const [statusOpen, setStatusOpen] = useState(false);

  if (isLoading) return <p className="text-sm text-muted-foreground">Đang tải chi tiết chuyến giao…</p>;
  if (!delivery) return <Alert variant="danger" title="Không tìm thấy chuyến giao">Chuyến {id} không tồn tại.</Alert>;

  const linkedBatches = supplierBatches.filter((b) => delivery.linkedBatches?.includes(b.id ?? ""));
  const canMutate = delivery.status === "SCHEDULED";
  const driver = drivers.find((d) => d.id === delivery.driverId);

  function cancelDelivery() {
    patchStatus.mutate(
      { id: delivery!.id!, action: "status", body: { status: "CANCELLED" } },
      {
        onSuccess: () => {
          // Chuyến hủy → lô hết cam kết giao, trả về "Hoạt động" để gán đơn khác
          for (const batchId of delivery!.linkedBatches ?? []) {
            updateBatch.mutate({ id: batchId, status: "ACTIVE" });
          }
          toast.success(`Đã hủy chuyến ${delivery!.id} (UC-039 — chỉ khi đang Scheduled) — lô trong chuyến trở về Hoạt động`);
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title={`Chuyến ${delivery.id}`}
        description="Chi tiết chuyến giao hàng (UC-039)"
        actions={
          <div className="flex gap-2">
            <Link href="/supplier/deliveries">
              <Button variant="outline"><ArrowLeft /> Danh sách</Button>
            </Link>
            {canMutate && (
              <Button variant="destructive" disabled={patchStatus.isPending} onClick={cancelDelivery}>
                <Ban /> Hủy chuyến
              </Button>
            )}
            {delivery.status !== "CANCELLED" && delivery.status !== "DELIVERED" && (
              <Button onClick={() => setStatusOpen(true)}>
                <RefreshCw /> Cập nhật trạng thái (UC-092)
              </Button>
            )}
          </div>
        }
      />

      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="lg:col-span-5">
          <CardHeader><CardTitle>Thông tin chuyến giao</CardTitle></CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Trường nhận</dt>
                <dd className="font-medium text-right">{delivery.school}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Đơn mua hàng</dt>
                <dd>
                  {delivery.purchaseRequestId ? (
                    <Link className="font-medium text-primary underline-offset-2 hover:underline" href={`/supplier/purchase-requests/${delivery.purchaseRequestId}`}>
                      {delivery.purchaseRequestId}
                    </Link>
                  ) : "—"}
                </dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Lịch giao</dt>
                <dd className="font-medium">{formatVnDateTime(delivery.scheduledAt)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Nhân viên giao (BR-050)</dt>
                <dd>{driver?.fullName ?? delivery.driverId ?? "—"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Biển số xe</dt>
                <dd>{delivery.vehiclePlate}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Trạng thái</dt>
                <dd><StatusBadge status={delivery.status ?? ""} /></dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Cập nhật trạng thái lúc (UC-092)</dt>
                <dd>{formatVnDateTime(delivery.statusUpdatedAt) || "—"}</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card className="lg:col-span-7">
          <CardHeader>
            <CardTitle>Bản ghi lô hàng của chuyến (UC-109 — BR-071)</CardTitle>
          </CardHeader>
          <CardContent>
            {linkedBatches.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Chưa có lô liên kết — bổ sung khi nộp thông tin giao hàng (UC-083).
              </p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Số lô</TableHead>
                    <TableHead>Nguyên liệu</TableHead>
                    <TableHead>Số lượng</TableHead>
                    <TableHead>HSD</TableHead>
                    <TableHead>Trạng thái</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {linkedBatches.map((b) => (
                    <TableRow key={b.id}>
                      <TableCell>
                        <Link className="font-medium text-primary underline-offset-2 hover:underline" href={`/supplier/batches/${b.id}`}>
                          {b.lotNumber}
                        </Link>
                      </TableCell>
                      <TableCell>{b.ingredient}</TableCell>
                      <TableCell>{b.quantity} {b.unit}</TableCell>
                      <TableCell>{formatVnDate(b.expirationDate)}</TableCell>
                      <TableCell><StatusBadge status={b.status ?? ""} /></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

      <UpdateStatusDialog delivery={delivery} open={statusOpen} onOpenChange={setStatusOpen} />
    </>
  );
}
