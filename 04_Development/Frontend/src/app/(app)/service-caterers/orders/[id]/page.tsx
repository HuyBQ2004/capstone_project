"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, PackageCheck, Truck, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label, Textarea } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useAction, useDetail, usePagedList } from "@/lib/api/hooks";
import type { DeliveryDto, DeliveryPersonDto, PurchaseRequestDto, SupplierBatchDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";

/**
 * Chi tiết đơn đặt hàng nguyên liệu (Service Caterers).
 * Luồng (đơn giản hoá 10/08/2026 — giá theo HỢP ĐỒNG, bỏ hẳn vòng báo giá/chấp
 * nhận giá vì không còn gì để đàm phán): PENDING (đơn giá đã tự tính từ hợp đồng
 * ngay lúc tạo, chờ NCC xác nhận lịch giao) → APPROVED (NCC xác nhận + gán lô
 * BR-071) → Delivery SCHEDULED/IN_TRANSIT → DELIVERED → mở phiên nhận hàng →
 * COMPLETED sau khi phiên nhận hoàn tất. SC có thể huỷ đơn bất kỳ lúc nào còn
 * PENDING (kèm lý do bắt buộc) → CANCELLED.
 */

/** SC huỷ đơn khi còn PENDING (chưa cần nữa / đặt nhầm) — kèm lý do bắt buộc để NCC được biết. */
function DeclineOrderDialog({
  pr,
  open,
  onOpenChange,
}: {
  pr: PurchaseRequestDto;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  /**
   * `POST /{code}/cancel` (SERVICE_CATERERS) — subpath thật, nhận `{cancelReason}`.
   * `cancelledAt` do server tự set (D9), không gửi lên.
   */
  const cancelAction = useAction<PurchaseRequestDto>("purchase-requests");
  const [reason, setReason] = useState("");
  const [reasonError, setReasonError] = useState("");

  function submit() {
    if (!reason.trim()) {
      setReasonError("Lý do từ chối không được để trống.");
      return;
    }
    setReasonError("");
    cancelAction.mutate(
      { id: pr.id!, action: "cancel", body: { cancelReason: reason.trim() } },
      {
        onSuccess: () => {
          toast.success(`Đã từ chối đơn ${pr.id} — NCC nhận được lý do kèm thông báo`);
          setReason("");
          onOpenChange(false);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Huỷ đơn đặt hàng</DialogTitle>
          <DialogDescription>
            Đơn {pr.id} — giá theo hợp đồng {pr.totalValue}. Đơn sẽ chuyển{" "}
            <b>Đã hủy</b>; lý do là bắt buộc để NCC được biết. Vẫn cần nguyên liệu
            thì tạo đơn mới.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label htmlFor="dc-reason">Lý do huỷ *</Label>
          <Textarea
            id="dc-reason"
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              if (reasonError && e.target.value.trim()) setReasonError("");
            }}
            aria-invalid={!!reasonError}
            className={reasonError ? "border-destructive focus-visible:ring-destructive" : undefined}
            placeholder="VD: đặt nhầm, trường không còn cần nguyên liệu này"
          />
          {reasonError && <p className="text-xs font-medium text-destructive">{reasonError}</p>}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Đóng</Button>
          <Button variant="destructive" disabled={cancelAction.isPending} onClick={submit}>
            <X /> Huỷ đơn
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatchDto>("supplier-batches", { pageSize: 200 });
  const supplierBatches = supplierBatchesPage?.items ?? [];
  const { data: driversPage } = usePagedList<DeliveryPersonDto>("delivery-persons", { pageSize: 100 });
  const drivers = driversPage?.items ?? [];
  const { id } = use(params);
  const { data: pr, isLoading } = useDetail<PurchaseRequestDto>("purchase-requests", id);
  const { data: deliveriesPage } = usePagedList<DeliveryDto>("deliveries", {
    purchaseRequestId: id,
    pageSize: 5,
  });
  const [declineOpen, setDeclineOpen] = useState(false);

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;
  if (!pr) {
    return (
      <Alert variant="danger" title="Không tìm thấy đơn">
        Đơn {id} không tồn tại.{" "}
        <Link className="underline" href="/service-caterers/orders">
          Về danh sách đơn
        </Link>
      </Alert>
    );
  }

  const items = pr.items ?? [];
  const delivery = deliveriesPage?.items[0];
  const driver = drivers.find((d) => d.id === delivery?.driverId);

  return (
    <>
      <PageHeader
        title={`Đơn đặt hàng ${pr.id}`}
        description={`${pr.supplier ?? "—"} · Đặt ngày ${pr.requestedDate} · Giao yêu cầu ${pr.deliveryDate}`}
        actions={
          <div className="flex gap-2">
            <Link href="/service-caterers/orders">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {pr.status === "PENDING" && (
              <Button variant="destructive" onClick={() => setDeclineOpen(true)}>
                <X /> Huỷ đơn
              </Button>
            )}
            {/*
              * 11/08/2026 — thêm điều kiện `pr.status === "APPROVED"`: trước đây nút này chỉ
              * xét trạng thái CHUYẾN GIAO, không xét đơn đã được NCC duyệt chưa. Lỗ hổng thật
              * trên production (PR-2026-0101 còn PENDING vẫn hiện nút) — NCC có thể đẩy chuyến
              * giao lên "Đã giao" cho một đơn chưa duyệt (backend nay đã chặn ở gốc, đây chỉ là
              * lớp phòng vệ UI để không mời bấm vào việc chắc chắn bị từ chối).
              */}
            {pr.status === "APPROVED" && delivery?.status === "DELIVERED" && (
              <Link href={`/service-caterers/receiving/new?delivery=${delivery.id}`}>
                <Button>
                  <PackageCheck /> Bắt đầu phiên nhận hàng
                </Button>
              </Link>
            )}
          </div>
        }
      />

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <StatusBadge status={pr.status ?? ""} />
        {delivery && (
          <>
            <Badge variant="info">
              <Truck className="mr-1 inline size-3.5" />
              {delivery.id}
            </Badge>
            <StatusBadge status={delivery.status ?? ""} />
          </>
        )}
      </div>

      {pr.status === "PENDING" && (
        <Alert variant="info" className="mb-4" title="Đơn giá đã tự tính theo hợp đồng — chờ NCC xác nhận lịch giao">
          Tổng giá: <b>{pr.totalValue}</b>, lấy tự động từ bảng giá hợp đồng — không cần duyệt
          giá riêng. NCC sẽ gán lô hàng theo từng nguyên liệu (BR-071) và xác nhận lịch giao
          (UC-015). Không còn cần nguyên liệu này thì <b>Huỷ đơn</b> kèm lý do.
        </Alert>
      )}
      {pr.status === "CANCELLED" && (
        <Alert variant="danger" className="mb-4" title="Đơn này đã bị huỷ">
          Lý do đã gửi NCC: “{pr.cancelReason}”.
          Vẫn cần nguyên liệu thì{" "}
          <Link className="font-medium underline" href="/service-caterers/orders/new">
            tạo đơn mới
          </Link>
          .
        </Alert>
      )}
      {pr.status === "REJECTED" && (
        <Alert variant="danger" className="mb-4" title="NCC đã từ chối đơn">
          Liên hệ NCC hoặc tạo đơn mới với nhà cung cấp khác.
        </Alert>
      )}
      {pr.status === "APPROVED" && delivery && delivery.status !== "DELIVERED" && (
        <Alert variant="warning" className="mb-4" title="Hàng đang trên đường">
          NCC đã xác nhận đơn và phân công {driver?.fullName ?? delivery.driverId} ({delivery.vehiclePlate})
          giao lúc {formatVnDateTime(delivery.scheduledAt)}. Phiên nhận hàng chỉ mở khi trạng thái
          giao là <b>Đã giao</b> — NCC tự cập nhật (`PATCH /deliveries/{'{code}'}/status`
          chỉ SUPPLIER, đã bỏ nút &ldquo;mô phỏng&rdquo; ở đây vì gọi thật sẽ 403).
        </Alert>
      )}
      {pr.status === "APPROVED" && delivery?.status === "DELIVERED" && (
        <Alert variant="success" className="mb-4" title="Hàng đã tới — sẵn sàng nhận">
          Chuyến {delivery.id} đã giao tới cổng trường. Nhấn{" "}
          <b>Bắt đầu phiên nhận hàng</b> để xác thực tài xế (BR-051), kiểm tra lô
          và ghi bằng chứng (BR-072/073/074).
        </Alert>
      )}
      {pr.status === "COMPLETED" && (
        <Alert variant="success" className="mb-4" title="Đơn đã hoàn tất">
          Phiên nhận hàng của đơn này đã hoàn tất — xem tại{" "}
          <Link className="font-medium underline" href="/service-caterers/receiving">
            Nhận nguyên liệu
          </Link>
          .
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="lg:col-span-4">
          <CardHeader>
            <CardTitle>Thông tin đơn</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Nhà cung cấp</dt>
                <dd className="text-right font-medium">{pr.supplier ?? "—"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày đặt</dt>
                <dd>{formatVnDate(pr.requestedDate)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày giao yêu cầu</dt>
                <dd className="font-medium">{formatVnDate(pr.deliveryDate)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Giá trị dự kiến</dt>
                <dd>{pr.totalValue}</dd>
              </div>
              {delivery && (
                <>
                  <div className="flex justify-between gap-4">
                    <dt className="text-muted-foreground">Tài xế</dt>
                    <dd>{driver?.fullName ?? delivery.driverId} · {delivery.vehiclePlate}</dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt className="text-muted-foreground">Lịch giao</dt>
                    <dd>{formatVnDateTime(delivery.scheduledAt)}</dd>
                  </div>
                </>
              )}
            </dl>
          </CardContent>
        </Card>

        <Card className="lg:col-span-8">
          <CardHeader>
            <CardTitle>
              Nguyên liệu & lô hàng NCC gán ({items.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            {items.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa có dòng nguyên liệu.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Nguyên liệu</TableHead>
                    <TableHead>Số lượng đặt</TableHead>
                    <TableHead>Đơn giá theo hợp đồng</TableHead>
                    <TableHead>Thành tiền</TableHead>
                    <TableHead>Ghi chú</TableHead>
                    <TableHead>Lô hàng NCC gán (BR-071)</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map((it) => {
                    const batch = supplierBatches.find((b) => b.id === it.supplierBatchId);
                    return (
                      <TableRow key={it.ingredientId}>
                        <TableCell className="font-medium">{it.ingredient}</TableCell>
                        <TableCell>
                          {it.quantity} {it.unit}
                          {it.requestedQuantity != null && it.quantity != null && it.requestedQuantity !== it.quantity && (
                            <span className="block text-xs text-muted-foreground">
                              Bạn yêu cầu {it.requestedQuantity} {it.unit} — NCC làm tròn theo quy cách đóng gói
                              trong hợp đồng (Proposed)
                            </span>
                          )}
                        </TableCell>
                        <TableCell>
                          {it.unitPrice ?? <span className="text-muted-foreground">Chưa có giá hợp đồng</span>}
                        </TableCell>
                        <TableCell className="font-medium">{it.lineTotal ?? "—"}</TableCell>
                        <TableCell>
                          {it.note ? <Badge variant="warning">{it.note}</Badge> : "—"}
                        </TableCell>
                        <TableCell>
                          {batch ? (
                            <span className="text-sm">
                              <Badge variant="success">{batch.id}</Badge>{" "}
                              <span className="text-muted-foreground">
                                Lô {batch.lotNumber} · HSD {formatVnDate(batch.expirationDate)}
                              </span>
                            </span>
                          ) : it.supplierBatchId ? (
                            <Badge variant="info">{it.supplierBatchId}</Badge>
                          ) : (
                            <span className="text-muted-foreground">Chưa gán — chờ NCC xác nhận</span>
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

      <DeclineOrderDialog pr={pr} open={declineOpen} onOpenChange={setDeclineOpen} />
    </>
  );
}
