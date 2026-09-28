"use client";

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Check, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
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
import { Label, SearchableSelect, Textarea } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useAction, useDetail, usePagedList } from "@/lib/api/hooks";
import type { DeliveryDto, PurchaseRequestDto, SupplierBatchDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";

/**
 * Screen Spec 11 — Purchase Request Detail.
 * UC-139 (View Purchase Requests).
 * BR-071: mỗi nguyên liệu phải gán 1 lô hàng đã đăng ký (số lô/NSX/HSD/chứng chỉ)
 * ngay khi Chấp nhận đơn — nhưng việc CHỌN TÀI XẾ + LỊCH GIAO (UC-015, BR-050,
 * BR-071 lead-time ≥24h) tách sang mục Giao hàng.
 *
 * Đơn giản hoá 10/08/2026 — bỏ hẳn bước "gửi giá / chấp nhận giá": đơn giá đã tự
 * tính theo hợp đồng ngay lúc SC tạo đơn (server-side, {@code
 * PurchaseRequestService.create()}), không còn gì để NCC báo hay SC duyệt. NCC chỉ
 * còn 1 thao tác — Chấp nhận đơn + gán lô (BR-071) — hoặc Từ chối nếu không đủ nguồn cung.
 */

const fmtVnd = (n: number) => `${Math.round(n).toLocaleString("vi-VN")} ₫`;

/**
 * NCC chấp nhận đơn (giá đã tự tính theo hợp đồng lúc tạo đơn) → gán lô hàng cho
 * từng nguyên liệu (BR-071) và chuyển đơn sang APPROVED qua `POST /confirm-schedule`.
 */
function AcceptOrderDialog({
  pr,
  open,
  onOpenChange,
}: {
  pr: PurchaseRequestDto;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatchDto>("supplier-batches", { pageSize: 200 }, { forceFresh: true });
  const supplierBatches = supplierBatchesPage?.items ?? [];
  const router = useRouter();
  const confirmSchedule = useAction<PurchaseRequestDto>("purchase-requests");
  const items = pr.items ?? [];
  const [assignments, setAssignments] = useState<Record<string, string>>(() =>
    Object.fromEntries(items.filter((it) => it.supplierBatchId).map((it) => [it.ingredientId ?? "", it.supplierBatchId ?? ""]))
  );
  const [error, setError] = useState("");

  /*
   * 11/08/2026 — BỎ hẳn kiểu loại-trừ "lô đã dùng cho đơn/chuyến giao khác thì không cho
   * chọn nữa" (`committedElsewhere` cũ). Đó là hiểu sai mô hình: `SupplierBatchResponse.
   * remainingQuantity` (BR-071) ghi rõ "giảm dần khi được nhận nhiều đợt/nhiều trường" — một
   * lô ĐƯỢC PHÉP gán cho nhiều đơn cùng lúc, chỉ hết khi `remainingQuantity` về 0. Bản cũ
   * chặn cứng ngay từ vòng gán khiến lô không bao giờ tới được kịch bản "chia nhiều đợt" mà
   * `remainingQuantity` được sinh ra để phục vụ (đo thật trên production).
   * Điều kiện gán còn lại: lô ACTIVE, còn hàng, và ĐÚNG NGUYÊN LIỆU của dòng đang gán — trước
   * đây dropdown còn cho chọn cả lô của nguyên liệu KHÁC (nhóm "Lô khác" bên dưới), khớp đúng
   * lỗi anh Nam báo (PR-2026-0101): gán được lô "Gạo tẻ" cho dòng "Thịt heo nạc". Backend nay
   * cũng tự kiểm lại (`PurchaseRequestService.confirmSchedule`), đây chỉ là hàng rào UI.
   */
  const availableBatches = supplierBatches.filter(
    (b) => b.status === "ACTIVE" && (b.remainingQuantity ?? b.quantity ?? 0) > 0
  );

  function submit() {
    if (pr.status === "APPROVED") {
      setError("Đơn đã APPROVED — sửa lại lô hàng gán chưa được backend hỗ trợ (cần bổ sung endpoint riêng).");
      return;
    }
    const missing = items.filter((it) => !assignments[it.ingredientId ?? ""]);
    if (missing.length > 0) {
      setError(
        `BR-071: chưa gán lô hàng cho ${missing.length} nguyên liệu (${missing
          .map((m) => m.ingredient)
          .join(", ")}) — mỗi nguyên liệu phải gắn 1 lô đã đăng ký số lô/NSX/HSD/chứng chỉ.`
      );
      return;
    }
    // Hàng rào phụ — dropdown đã giới hạn theo đúng nguyên liệu, nhưng vẫn kiểm lại đề phòng
    // dữ liệu cũ (lô gán từ trước khi có bộ lọc này) còn sót lại trong `assignments`.
    const mismatched = items.filter((it) => {
      const batch = supplierBatches.find((b) => b.id === assignments[it.ingredientId ?? ""]);
      return batch && batch.ingredientId !== it.ingredientId;
    });
    if (mismatched.length > 0) {
      setError(
        `Lô đã chọn cho ${mismatched.map((m) => m.ingredient).join(", ")} không đúng nguyên liệu của dòng này — vui lòng chọn lại lô khớp nguyên liệu.`
      );
      return;
    }
    setError("");
    confirmSchedule.mutate(
      { id: pr.id!, action: "confirm-schedule", body: { batchAssignments: assignments } },
      {
        onSuccess: () => {
          toast.success(
            `Đã chấp nhận đơn ${pr.id} và gán lô hàng theo nguyên liệu (BR-071) — tạo chuyến giao ở mục Giao hàng`
          );
          onOpenChange(false);
          router.push("/supplier/deliveries");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            {pr.status === "APPROVED" ? "Sửa lô hàng đã gán" : "Chấp nhận đơn & gán lô hàng"}
          </DialogTitle>
          <DialogDescription>
            Đơn {pr.id}. Mỗi nguyên liệu phải gán 1 lô hàng ĐÚNG NGUYÊN LIỆU, còn hiệu lực và
            còn hàng (BR-071) — một lô có thể dùng chung cho nhiều đơn, chỉ hết khi hết số
            lượng còn lại. Sau khi
            {pr.status === "APPROVED" ? " lưu" : " chấp nhận"}, sang mục <b>Giao hàng</b>{" "}
            để tạo chuyến giao (chọn tài xế + lịch giao — UC-015).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể chấp nhận đơn">{error}</Alert>}
        <div className="space-y-2">
          <Label>Gán lô hàng theo từng nguyên liệu * (BR-071)</Label>
          {items.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              Đơn không có dòng nguyên liệu chi tiết.
            </p>
          ) : (
            items.map((it) => {
              // ĐÚNG nguyên liệu của dòng này — bỏ hẳn nhóm "Lô khác" (nguyên liệu khác) từng
              // cho gán nhầm (VD lô "Gạo tẻ" cho dòng "Thịt heo nạc", đo thật PR-2026-0101).
              const options = availableBatches
                .filter((b) => b.ingredientId === it.ingredientId)
                .map((b) => ({
                  value: b.id ?? "",
                  label: `${b.id} · Lô ${b.lotNumber} · HSD ${formatVnDate(b.expirationDate)} · còn ${b.remainingQuantity ?? b.quantity} ${b.unit}`,
                }));
              const currentId = assignments[it.ingredientId ?? ""];
              const currentStale =
                currentId && !options.some((o) => o.value === currentId)
                  ? supplierBatches.find((b) => b.id === currentId)
                  : undefined;
              return (
                /*
                 * BUG tìm thấy 13/08/2026 (đo trực tiếp DOM ở viewport hẹp <640px, Playwright):
                 * `sm:grid-cols-[1fr_1.4fr]` chỉ áp từ breakpoint `sm` (640px) trở lên — dưới
                 * mức đó grid rơi về 1 CỘT NGẦM ĐỊNH, không có `grid-template-columns` tường
                 * minh, nên track được đo theo NỘI DUNG (auto-sizing theo max-content). Nhãn
                 * lô đã chọn trong `SearchableSelect` là 1 dòng không xuống dòng (`truncate`)
                 * — khi mất khung cột giới hạn, trình duyệt tính "kích thước tự nhiên" của
                 * chuỗi đó (~470px cho nhãn dài) làm bề rộng cột, kéo cả `<button>` (w-full)
                 * lẫn `DialogContent` cha (cũng `grid` không khai cột) phình theo, tràn ra
                 * ngoài khung dialog (khớp đúng ảnh chụp: nút "Chấp nhận đơn" bị cắt chữ, xuất
                 * hiện thanh cuộn ngang). Sửa: bỏ hẳn ngưỡng `sm:`, khai 2 cột TƯỜNG MINH bằng
                 * `minmax(0, ...)` — bắt buộc track co được về 0 bất kể kích cỡ nội dung, để
                 * `min-w-0 truncate` đã có sẵn trong `SearchableSelect` thực sự phát huy tác
                 * dụng ở MỌI viewport, không chỉ ≥640px.
                 */
                <div key={it.ingredientId} className="grid grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)] items-center gap-2">
                  <p className="text-sm">
                    <span className="font-medium">{it.ingredient}</span>{" "}
                    <span className="text-muted-foreground">· {it.quantity} {it.unit}</span>
                  </p>
                  <SearchableSelect
                    value={assignments[it.ingredientId ?? ""] ?? ""}
                    onChange={(v) => setAssignments((prev) => ({ ...prev, [it.ingredientId ?? ""]: v }))}
                    placeholder="— Chọn lô đã đăng ký —"
                    searchPlaceholder="Tìm theo mã lô…"
                    options={
                      currentStale
                        ? [
                            {
                              value: currentStale.id ?? "",
                              label: `${currentStale.id} · ${currentStale.ingredient} — hết hàng/sai nguyên liệu, cần đổi lô`,
                            },
                            ...options,
                          ]
                        : options
                    }
                  />
                </div>
              );
            })
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={confirmSchedule.isPending || pr.status === "APPROVED"} onClick={submit}>
            {pr.status === "APPROVED" ? "Lưu lô hàng (chưa hỗ trợ)" : "Chấp nhận đơn"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function RejectDialog({
  pr,
  open,
  onOpenChange,
}: {
  pr: PurchaseRequestDto;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const router = useRouter();
  const rejectAction = useAction<PurchaseRequestDto>("purchase-requests");
  const [reason, setReason] = useState("");
  const [reasonError, setReasonError] = useState("");

  function submit() {
    if (!reason.trim()) {
      setReasonError("Lý do từ chối không được để trống.");
      return;
    }
    setReasonError("");
    rejectAction.mutate(
      { id: pr.id!, action: "reject", body: { reason: reason.trim() } },
      {
        onSuccess: () => {
          toast.success(`Đã từ chối đơn ${pr.id} kèm lý do`);
          onOpenChange(false);
          router.push("/supplier/purchase-requests");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Từ chối đơn mua hàng</DialogTitle>
          <DialogDescription>
            Lý do từ chối là bắt buộc — Service Caterers sẽ nhận được lý do kèm thông báo.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label htmlFor="r-reason">Lý do từ chối *</Label>
          <Textarea
            id="r-reason"
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              if (reasonError && e.target.value.trim()) setReasonError("");
            }}
            aria-invalid={!!reasonError}
            className={reasonError ? "border-destructive focus-visible:ring-destructive" : undefined}
            placeholder="VD: không đủ nguồn cung trong khung ngày yêu cầu"
          />
          {reasonError && <p className="text-xs font-medium text-destructive">{reasonError}</p>}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button variant="destructive" disabled={rejectAction.isPending} onClick={submit}>Từ chối đơn</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function PurchaseRequestDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: pr, isLoading } = useDetail<PurchaseRequestDto>("purchase-requests", id);
  const { data: deliveriesPage } = usePagedList<DeliveryDto>(
    "deliveries",
    { purchaseRequestId: id, pageSize: 1 },
    { forceFresh: true }
  );
  const hasDelivery = (deliveriesPage?.items.length ?? 0) > 0;
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);

  if (isLoading) return <p className="text-sm text-muted-foreground">Đang tải chi tiết đơn…</p>;
  if (!pr) return <Alert variant="danger" title="Không tìm thấy đơn">Đơn {id} không tồn tại.</Alert>;

  const items = pr.items ?? [];

  return (
    <>
      <PageHeader
        title={pr.id ?? ""}
        description="Chi tiết đơn mua hàng (UC-139) — giá đã tự tính theo hợp đồng lúc tạo đơn → NCC chấp nhận + gán lô (BR-071) hoặc từ chối → tạo chuyến giao ở mục Giao hàng (UC-015)"
        actions={
          <div className="flex gap-2">
            <Link href="/supplier/purchase-requests">
              <Button variant="outline"><ArrowLeft /> Danh sách đơn</Button>
            </Link>
            {pr.status === "PENDING" && (
              <>
                <Button variant="destructive" onClick={() => setRejectOpen(true)}>
                  <X /> Từ chối
                </Button>
                <Button onClick={() => setConfirmOpen(true)}>
                  <Check /> Chấp nhận đơn
                </Button>
              </>
            )}
            {pr.status === "APPROVED" && !hasDelivery && (
              <Button variant="outline" onClick={() => setConfirmOpen(true)}>
                <Check /> Sửa lô hàng đã gán
              </Button>
            )}
          </div>
        }
      />

      {pr.status === "PENDING" && (
        <Alert variant="info" className="mb-4" title="Đơn mới — giá đã tự tính theo hợp đồng">
          Service Caterer đã gửi lô nguyên liệu cần mua với giá {fmtVnd(pr.totalValue ?? 0)} tra tự
          động từ hợp đồng. Nhấn <b>Chấp nhận đơn</b> để gán lô hàng theo từng nguyên liệu
          (BR-071), hoặc <b>Từ chối</b> nếu không đủ nguồn cung.
        </Alert>
      )}
      {pr.status === "CANCELLED" && (
        <Alert variant="danger" className="mb-4" title="Đơn đã bị từ chối/huỷ">
          {pr.cancelReason ?? "Không có lý do chi tiết."}
        </Alert>
      )}
      {pr.status === "APPROVED" && !hasDelivery && (
        <Alert variant="info" className="mb-4" title="Đã chấp nhận đơn — chưa có chuyến giao">
          Lô hàng đã gán theo BR-071. Vào mục{" "}
          <Link className="font-medium underline" href="/supplier/deliveries">
            Giao hàng
          </Link>{" "}
          để tạo chuyến giao (chọn tài xế + lịch giao — UC-015). Nếu mục Giao hàng
          báo lô đang gán đã bị dùng cho đơn khác, nhấn <b>Sửa lô hàng đã gán</b>{" "}
          ở trên để đổi lô.
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="lg:col-span-4">
          <CardHeader><CardTitle>Thông tin đơn</CardTitle></CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã đơn</dt>
                <dd className="font-medium">{pr.id}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Trường</dt>
                <dd className="text-right font-medium">{pr.school ?? "—"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày yêu cầu</dt>
                <dd>{formatVnDate(pr.requestedDate)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Hạn giao hàng</dt>
                <dd className="font-medium">{formatVnDate(pr.deliveryDate)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Giá trị</dt>
                <dd>{fmtVnd(pr.totalValue ?? 0)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Trạng thái</dt>
                <dd>
                  <StatusBadge status={pr.status ?? ""} />
                </dd>
              </div>
              {pr.confirmedDeliveryAt && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Ngày giờ giao đã xác nhận</dt>
                  <dd className="font-medium">{formatVnDateTime(pr.confirmedDeliveryAt)}</dd>
                </div>
              )}
            </dl>
          </CardContent>
        </Card>

        <Card className="lg:col-span-8">
          <CardHeader>
            <CardTitle>Danh sách nguyên liệu yêu cầu ({items.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {items.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa có dòng nguyên liệu cho đơn này.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Nguyên liệu</TableHead>
                    <TableHead>Khối lượng yêu cầu</TableHead>
                    <TableHead>Đơn giá</TableHead>
                    <TableHead>Thành tiền</TableHead>
                    <TableHead>Ghi chú</TableHead>
                    <TableHead>Lô đã gán (BR-071)</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map((it) => (
                    <TableRow key={it.ingredientId}>
                      <TableCell className="font-medium">{it.ingredient}</TableCell>
                      <TableCell>
                        {it.quantity} {it.unit}
                        {it.requestedQuantity != null && it.quantity != null && it.requestedQuantity !== it.quantity && (
                          <span className="block text-xs text-muted-foreground">
                            Yêu cầu {it.requestedQuantity} {it.unit} — làm tròn theo quy cách đóng gói (Proposed)
                          </span>
                        )}
                      </TableCell>
                      <TableCell>{it.unitPrice != null ? fmtVnd(it.unitPrice) : "—"}</TableCell>
                      <TableCell className="font-medium">{it.lineTotal != null ? fmtVnd(it.lineTotal) : "—"}</TableCell>
                      <TableCell>
                        {it.note ? <Badge variant="warning">{it.note}</Badge> : "—"}
                      </TableCell>
                      <TableCell>
                        {it.supplierBatchId ? (
                          <Badge variant="success">{it.supplierBatchId}</Badge>
                        ) : (
                          <span className="text-muted-foreground">Gán khi chấp nhận đơn</span>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

      <AcceptOrderDialog pr={pr} open={confirmOpen} onOpenChange={setConfirmOpen} />
      <RejectDialog pr={pr} open={rejectOpen} onOpenChange={setRejectOpen} />
    </>
  );
}
