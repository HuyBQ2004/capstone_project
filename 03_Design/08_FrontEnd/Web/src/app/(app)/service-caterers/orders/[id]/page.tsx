"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Check, PackageCheck, Truck, X } from "lucide-react";
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
import { useDetail, usePagedList, useUpdate } from "@/lib/api/hooks";
import {
  PURCHASE_REQUEST_ITEMS,
  type Delivery,
  type PurchaseRequest,
  type SupplierBatch,
} from "@/lib/mock-data";

/**
 * Chi tiết đơn đặt hàng nguyên liệu (Service Caterers).
 * Luồng (review 25/07/2026 — giá theo HỢP ĐỒNG, duyệt 1 vòng, không đàm phán):
 * PENDING (chờ NCC gửi giá) → QUOTED (giá tự tính từ bảng giá hợp đồng — SC:
 * Chấp nhận giá → PRICE_ACCEPTED, hoặc Từ chối đơn kèm lý do → CANCELLED)
 * → PRICE_ACCEPTED → APPROVED (NCC xác nhận + gán lô BR-071)
 * → Delivery SCHEDULED/IN_TRANSIT → DELIVERED → mở phiên nhận hàng
 * → COMPLETED sau khi phiên nhận hoàn tất.
 */

/**
 * SC không chấp nhận giá (trường hợp bất lợi) — TỪ CHỐI ĐƠN kèm lý do bắt buộc,
 * đơn chuyển CANCELLED. Không có vòng báo giá lại: giá đã chốt theo hợp đồng,
 * cần mua vẫn tạo đơn mới với NCC khác.
 */
function DeclineOrderDialog({
  pr,
  open,
  onOpenChange,
}: {
  pr: PurchaseRequest;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const updatePr = useUpdate<{ id: string } & Partial<PurchaseRequest>>("purchase-requests");
  const [reason, setReason] = useState("");
  const [reasonError, setReasonError] = useState("");

  function submit() {
    if (!reason.trim()) {
      setReasonError("Lý do từ chối không được để trống.");
      return;
    }
    setReasonError("");
    updatePr.mutate(
      {
        id: pr.id,
        status: "CANCELLED",
        cancelReason: reason.trim(),
        cancelledAt: new Date().toLocaleString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" }),
      },
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
          <DialogTitle>Từ chối đơn — không chấp nhận giá</DialogTitle>
          <DialogDescription>
            Đơn {pr.id} — giá theo hợp đồng {pr.totalValue}. Đơn sẽ chuyển{" "}
            <b>Đã hủy</b>; lý do là bắt buộc để NCC được biết. Vẫn cần nguyên liệu
            thì tạo đơn mới với nhà cung cấp khác.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label htmlFor="dc-reason">Lý do từ chối *</Label>
          <Textarea
            id="dc-reason"
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              if (reasonError && e.target.value.trim()) setReasonError("");
            }}
            aria-invalid={!!reasonError}
            className={reasonError ? "border-destructive focus-visible:ring-destructive" : undefined}
            placeholder="VD: giá cao hơn phụ lục hợp đồng đã ký, chuyển đặt NCC khác"
          />
          {reasonError && <p className="text-xs font-medium text-destructive">{reasonError}</p>}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button variant="destructive" disabled={updatePr.isPending} onClick={submit}>
            <X /> Từ chối đơn
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatch>("supplier-batches", { pageSize: 200 });
  const supplierBatches = supplierBatchesPage?.items ?? [];
  const { id } = use(params);
  const { data: pr, isLoading } = useDetail<PurchaseRequest>("purchase-requests", id);
  const { data: deliveriesPage } = usePagedList<Delivery>("deliveries", {
    purchaseRequestId: id,
    pageSize: 5,
  });
  const updateDelivery = useUpdate<{ id: string } & Partial<Delivery>>("deliveries");
  const updatePr = useUpdate<{ id: string } & Partial<PurchaseRequest>>("purchase-requests");
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

  const items = pr.itemsDetail ?? PURCHASE_REQUEST_ITEMS[pr.id] ?? [];
  const delivery = deliveriesPage?.items[0];
  const assignments = delivery?.batchAssignments ?? pr.batchAssignments ?? {};
  /** SC chấp nhận báo giá → chốt giá, NCC được mở bước xác nhận lịch giao (UC-015). */
  function acceptQuote() {
    updatePr.mutate(
      {
        id: pr!.id,
        status: "PRICE_ACCEPTED",
        priceAcceptedAt: new Date().toLocaleString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" }),
      },
      {
        onSuccess: () =>
          toast.success(`Đã chấp nhận giá ${pr!.totalValue} theo hợp đồng — chờ NCC xác nhận lịch giao (UC-015)`),
      }
    );
  }

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
            {pr.status === "QUOTED" && (
              <>
                <Button variant="destructive" onClick={() => setDeclineOpen(true)}>
                  <X /> Từ chối đơn
                </Button>
                <Button disabled={updatePr.isPending} onClick={acceptQuote}>
                  <Check /> Chấp nhận giá
                </Button>
              </>
            )}
            {delivery?.status === "DELIVERED" && pr.status !== "COMPLETED" && (
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
        <StatusBadge
          status={pr.status}
          label={pr.status === "PRICE_ACCEPTED" ? "Đã chốt giá — chờ giao" : undefined}
        />
        {delivery && (
          <>
            <Badge variant="info">
              <Truck className="mr-1 inline size-3.5" />
              {delivery.id}
            </Badge>
            <StatusBadge status={delivery.status} />
          </>
        )}
      </div>

      {pr.status === "PENDING" && (
        <Alert variant="info" className="mb-4" title="Chờ nhà cung cấp gửi giá theo hợp đồng">
          Đơn đã gửi tới NCC — NCC sẽ xác nhận và gửi giá tự tính từ bảng giá đã
          chốt theo hợp đồng để bạn duyệt trước khi chốt đơn.
        </Alert>
      )}
      {pr.status === "QUOTED" && (
        <Alert variant="warning" className="mb-4" title="NCC đã gửi giá theo hợp đồng — cần bạn duyệt">
          Tổng giá: <b>{pr.totalValue}</b> (gửi lúc {pr.quotedAt}). Xem đơn giá
          từng nguyên liệu ở bảng dưới, rồi <b>Chấp nhận giá</b> để chốt đơn; nếu
          bất lợi thì <b>Từ chối đơn</b> kèm lý do — không có vòng báo giá lại,
          giá đã theo hợp đồng.
          {pr.quoteNote && (
            <span className="mt-1 block text-muted-foreground">Ghi chú của NCC: {pr.quoteNote}</span>
          )}
        </Alert>
      )}
      {pr.status === "CANCELLED" && (
        <Alert variant="danger" className="mb-4" title="Bạn đã từ chối đơn này">
          Lý do đã gửi NCC: “{pr.cancelReason}”{pr.cancelledAt && <> (lúc {pr.cancelledAt})</>}.
          Vẫn cần nguyên liệu thì{" "}
          <Link className="font-medium underline" href="/service-caterers/orders/new">
            tạo đơn mới
          </Link>{" "}
          với nhà cung cấp khác.
        </Alert>
      )}
      {pr.status === "PRICE_ACCEPTED" && (
        <Alert variant="success" className="mb-4" title="Đã chốt giá — chờ NCC xác nhận lịch giao">
          Bạn đã chấp nhận giá {pr.totalValue} theo hợp đồng lúc {pr.priceAcceptedAt}. NCC sẽ
          gán lô hàng theo từng nguyên liệu (BR-071) và xác nhận lịch giao (UC-015).
        </Alert>
      )}
      {pr.status === "REJECTED" && (
        <Alert variant="danger" className="mb-4" title="NCC đã từ chối đơn">
          Liên hệ NCC hoặc tạo đơn mới với nhà cung cấp khác.
        </Alert>
      )}
      {pr.status === "APPROVED" && delivery && delivery.status !== "DELIVERED" && (
        <Alert variant="warning" className="mb-4" title="Hàng đang trên đường">
          NCC đã xác nhận đơn và phân công {delivery.driver} ({delivery.vehiclePlate})
          giao lúc {delivery.scheduledAt}. Phiên nhận hàng chỉ mở khi trạng thái
          giao là <b>Đã giao</b>.
          <span className="mt-2 block">
            <Button
              size="sm"
              variant="outline"
              disabled={updateDelivery.isPending}
              onClick={() =>
                updateDelivery.mutate(
                  { id: delivery.id, status: "DELIVERED" },
                  {
                    onSuccess: () =>
                      toast.success(`(Mô phỏng phía NCC) ${delivery.id} đã giao tới cổng trường — có thể mở phiên nhận hàng.`),
                  }
                )
              }
            >
              Mô phỏng: NCC giao hàng tới (demo)
            </Button>
          </span>
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
                <dt className="text-muted-foreground">Người đặt</dt>
                <dd className="text-right">{pr.requestedBy ?? "—"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày đặt</dt>
                <dd>{pr.requestedDate}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày giao yêu cầu</dt>
                <dd className="font-medium">{pr.deliveryDate}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Giá trị dự kiến</dt>
                <dd>{pr.totalValue}</dd>
              </div>
              {delivery && (
                <>
                  <div className="flex justify-between gap-4">
                    <dt className="text-muted-foreground">Tài xế</dt>
                    <dd>{delivery.driver} · {delivery.vehiclePlate}</dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt className="text-muted-foreground">Lịch giao</dt>
                    <dd>{delivery.scheduledAt}</dd>
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
                    const batchId = assignments[it.name];
                    const batch = supplierBatches.find((b) => b.id === batchId);
                    return (
                      <TableRow key={it.name}>
                        <TableCell className="font-medium">{it.name}</TableCell>
                        <TableCell>{it.quantity}</TableCell>
                        <TableCell>
                          {it.unitPrice ?? <span className="text-muted-foreground">Chờ NCC gửi giá</span>}
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
                                Lô {batch.lotNumber} · HSD {batch.expirationDate}
                              </span>
                            </span>
                          ) : batchId ? (
                            <Badge variant="info">{batchId}</Badge>
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
