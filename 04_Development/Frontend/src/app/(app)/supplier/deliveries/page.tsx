"use client";

import { useEffect, useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { DateInput, Input, Label, Select } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { DeliveryDto, DeliveryPersonDto, PurchaseRequestDto, SupplierBatchDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime, toIsoDate } from "@/lib/format";

/**
 * Screen Spec 18/19/21 — Deliveries / Delivery List / Submit Delivery Information.
 * UC-039 (Manage Deliveries), UC-083 (Submit Delivery Information).
 * BR-071: nộp thông tin giao hàng ≥24h trước giờ giao, kèm ≥1 lô liên kết.
 * BR-050: driver chọn từ nhân viên giao đã đăng ký sinh trắc học.
 * Validate theo từng trường (inline error). Luật "lô đã liên kết đơn khác thì KHÔNG cho
 * chọn" (review 07/2026) đã GỠ 12/08/2026 — trái BR-071 và không có ở backend, xem ghi chú
 * trong `submit()`.
 * Nhận xét 23/07/2026: đây giờ là NƠI DUY NHẤT tạo chuyến giao — "Chấp nhận đơn"
 * bên Đơn mua hàng chỉ gán lô theo nguyên liệu (BR-071) và chuyển PR sang
 * APPROVED, không còn tự tạo chuyến giao.
 *
 * Nối backend thật 05/08/2026: `POST /deliveries` trước đây LUÔN 403 (bug backend đã vá,
 * xem `03_FE_Full_Wiring_Master_Plan.md`) — `create()` giờ cần `{purchaseRequestId,
 * scheduledAt (ISO Instant), driverId, vehiclePlate, supplierBatchIds}`. Lô liên kết đọc từ
 * `pr.items[].supplierBatchId` (đã gán lúc Chấp nhận đơn), KHÔNG còn `batchAssignments`
 * (field mock, không tồn tại ở DTO thật). `d.driver` (tên) không có ở response — chỉ có
 * `driverId` (mã) — tra tên qua `delivery-persons`.
 */

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="text-xs font-medium text-destructive">{message}</p>;
}

function makeColumns(drivers: DeliveryPersonDto[]): ColumnDef<DeliveryDto, unknown>[] {
  return [
    { accessorKey: "id", header: "Mã chuyến", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "school", header: "Trường" },
    {
      accessorKey: "scheduledAt",
      header: "Lịch giao",
      cell: ({ row }) => formatVnDateTime(row.original.scheduledAt),
    },
    {
      id: "driver",
      header: "Nhân viên giao (BR-050)",
      cell: ({ row }) => drivers.find((d) => d.id === row.original.driverId)?.fullName ?? row.original.driverId ?? "—",
    },
    { accessorKey: "vehiclePlate", header: "Biển số xe" },
    {
      id: "batches",
      header: "Lô liên kết (BR-071)",
      cell: ({ row }) => row.original.linkedBatches?.length ?? row.original.batches,
    },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
  ];
}

function SubmitDeliveryDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatchDto>("supplier-batches", { pageSize: 200 });
  const supplierBatches = supplierBatchesPage?.items ?? [];
  const { data: deliveryPersonsPage } = usePagedList<DeliveryPersonDto>("delivery-persons", { pageSize: 50 });
  const deliveryPersons = deliveryPersonsPage?.items ?? [];
  const { data: purchaseRequestsPage, isFetching: prsFetching } = usePagedList<PurchaseRequestDto>(
    "purchase-requests",
    { pageSize: 100 },
    { forceFresh: true }
  );
  const purchaseRequests = purchaseRequestsPage?.items ?? [];
  const create = useCreate<
    { purchaseRequestId: string; scheduledAt: string; driverId: string; vehiclePlate: string; supplierBatchIds: string[] },
    DeliveryDto
  >("deliveries");

  const { data: deliveriesPage } = usePagedList<DeliveryDto>("deliveries", { pageSize: 100 }, { forceFresh: true });
  const linkedElsewhere = new Map<string, string>();
  const prsWithDelivery = new Set<string>();
  for (const d of deliveriesPage?.items ?? []) {
    if (d.purchaseRequestId && d.status !== "CANCELLED") prsWithDelivery.add(d.purchaseRequestId);
    if (d.status === "CANCELLED") continue;
    for (const b of d.linkedBatches ?? []) {
      if (!linkedElsewhere.has(b)) linkedElsewhere.set(b, d.purchaseRequestId ?? d.id ?? "");
    }
  }
  // Chỉ đơn đã Chấp nhận (APPROVED, đã gán lô theo BR-071) và CHƯA có chuyến giao.
  const acceptedPrs = purchaseRequests.filter(
    (p) => p.status === "APPROVED" && !prsWithDelivery.has(p.id ?? "")
  );

  const [prId, setPrId] = useState("");
  // `scheduledAt` của backend là THỜI ĐIỂM GIAO DỰ KIẾN, không phải giờ xuất kho ("đã xuất
  // kho" là trạng thái DISPATCHED riêng ở màn chi tiết) — đặt tên/nhãn theo đúng nghĩa đó.
  const [scheduledDate, setScheduledDate] = useState("");
  const [scheduledTime, setScheduledTime] = useState("");
  const [vehiclePlate, setVehiclePlate] = useState("");
  const [driverId, setDriverId] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});

  /*
   * BUG tìm thấy 12/08/2026: `useState(deliveryPersons[0]?.id ?? "")` chỉ chạy MỘT LẦN lúc
   * mount — nhưng `deliveryPersons` từ `usePagedList` lúc đó luôn rỗng (data chưa fetch
   * xong), nên `driverId` bị chốt cứng "" mãi mãi. `<select>` vẫn HIỂN THỊ option đầu tiên
   * (hành vi mặc định của trình duyệt khi `value` không khớp option nào) — nhìn như đã
   * chọn — nhưng state thật vẫn rỗng nên `submit()` luôn báo "Vui lòng chọn nhân viên giao
   * hàng" dù người dùng thấy tên hiện sẵn trên dropdown. Đo thật trên UI 12/08/2026.
   */
  useEffect(() => {
    if (!driverId && deliveryPersons.length > 0) setDriverId(deliveryPersons[0].id ?? "");
  }, [deliveryPersons, driverId]);

  // Mốc sớm nhất được phép chọn theo BR-071 — tính lại mỗi lần render để hint luôn khớp
  // "hiện tại", và dùng chung cho cả thông báo lỗi lẫn `min` của ô giờ/ngày.
  const minScheduledAt = new Date(Date.now() + 24 * 60 * 60 * 1000);

  const selectedPr = purchaseRequests.find((p) => p.id === prId);
  // Lô liên kết KHÔNG chọn tay ở đây — lấy từ `supplierBatchId` NCC đã gán theo
  // từng dòng nguyên liệu lúc "Chấp nhận đơn" (BR-071).
  const assignedBatchIds = [
    ...new Set((selectedPr?.items ?? []).map((it) => it.supplierBatchId).filter((b): b is string => Boolean(b))),
  ];

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function submit() {
    const next: Record<string, string> = {};
    if (prsFetching) {
      setErrors({ pr: "Đang tải danh sách đơn mới nhất — vui lòng đợi rồi thử lại." });
      return;
    }
    if (!prId) next.pr = "Vui lòng chọn đơn mua hàng đã Chấp nhận.";
    if (!vehiclePlate.trim()) next.plate = "Biển số xe không được để trống.";

    let scheduledIso: string | null = null;
    if (!scheduledDate.trim()) next.date = "Ngày giao dự kiến không được để trống.";
    if (!scheduledTime.trim()) next.time = "Giờ giao dự kiến không được để trống.";
    if (scheduledDate.trim() && scheduledTime.trim()) {
      // DateInput lưu "dd/mm/yyyy" (không phải ISO) — ghép thẳng với "T" ra chuỗi
      // "10/08/2026T14:30" mà `new Date()` không parse được (luôn Invalid Date, đo
      // thật 10/08/2026, cùng lớp lỗi D4 với 422 "malformed request body" ở batches).
      const iso = toIsoDate(scheduledDate);
      const d = iso ? new Date(`${iso}T${scheduledTime}`) : new Date(NaN);
      if (Number.isNaN(d.getTime())) {
        next.date = "Ngày/giờ giao dự kiến không hợp lệ.";
      } else if (d.getTime() < Date.now() + 24 * 60 * 60 * 1000) {
        next.date = `BR-071: thông tin giao hàng phải nộp tối thiểu 24 giờ trước thời điểm giao dự kiến — chọn từ ${formatVnDateTime(minScheduledAt.toISOString())} trở đi.`;
      } else {
        scheduledIso = d.toISOString();
      }
    }
    /*
     * 13/08/2026 — GỠ luật "scheduledAt phải đúng ngày `deliveryDate` của đơn" (thêm hôm
     * qua, không có trong BR-071). `deliveryDate` do trường chọn lúc tạo đơn, không bị buộc
     * cách "hôm nay" ≥24h — đơn giao gấp cho ngày mai là hợp lệ. Ép "đúng ngày + cách ≥24h"
     * cùng lúc có thể không còn giờ nào thỏa cả hai, chặn nhầm chuyến giao hợp lệ (đo thật
     * PR-2026-0107: deliveryDate 13/08, hôm đó là 12/08 — cửa sổ hợp lệ chỉ còn vài giờ cuối
     * ngày 13/08, đổi ngày ra ngoài lại vướng luật này). Xem cùng lý do gỡ ở `DeliveryService`.
     */

    const driver = deliveryPersons.find((d) => d.id === driverId);
    if (!driverId || !driver) {
      next.driver = "Vui lòng chọn nhân viên giao hàng (BR-050).";
    } else if (driver.biometricStatus !== "REGISTERED") {
      next.driver = `BR-050: nhân viên "${driver.fullName}" chưa đăng ký sinh trắc học khuôn mặt — không thể phân công giao hàng.`;
    }

    /*
     * 12/08/2026 — GỠ luật "lô đã liên kết đơn khác ⇒ chặn tạo chuyến giao".
     *
     * Luật đó chỉ tồn tại ở FE (review 07/2026) và MÂU THUẪN với chính BR-071 như màn
     * "Chấp nhận đơn & gán lô hàng" phát biểu: *một lô có thể dùng chung cho nhiều đơn, chỉ
     * hết khi hết số lượng còn lại*. Backend (`DeliveryService.create`) cũng không hề kiểm
     * điều này — nó chỉ đòi lô có thật và thuộc đúng NCC. Hệ quả đo được: NCC gán một lô lớn
     * (VD Hành lá còn 2999.982 kg) cho hai đơn nhỏ đúng như hướng dẫn ở bước trước, rồi bị
     * chặn không tạo được chuyến giao — không có đường đi tiếp nào ngoài việc bỏ gán lô.
     *
     * Ràng buộc THẬT là số lượng còn lại của lô, do backend giữ (`remainingQuantity`);
     * FE không tự dựng thêm luật độc quyền lô.
     */
    if (prId && assignedBatchIds.length === 0) {
      next.batches = "Đơn chưa được gán lô hàng ở bước Chấp nhận đơn (BR-071) — quay lại Đơn mua hàng để gán lô trước.";
    }

    setErrors(next);
    if (Object.values(next).some(Boolean) || !scheduledIso || !selectedPr || !driver) return;
    create.mutate(
      {
        purchaseRequestId: selectedPr.id!,
        scheduledAt: scheduledIso,
        driverId: driver.id!,
        vehiclePlate,
        supplierBatchIds: assignedBatchIds,
      },
      {
        onSuccess: () => {
          toast.success("Đã tạo chuyến giao — trạng thái Scheduled, Service Caterers được thông báo (UC-083)");
          onOpenChange(false);
          setPrId(""); setScheduledDate(""); setScheduledTime(""); setVehiclePlate("");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Tạo chuyến giao</DialogTitle>
          <DialogDescription>
            UC-083 — chỉ chọn được đơn đã Chấp nhận (chưa có chuyến giao); nộp ≥24
            giờ trước giờ giao dự kiến (BR-071) — tức từ{" "}
            <span className="font-medium text-foreground">{formatVnDateTime(minScheduledAt.toISOString())}</span>{" "}
            trở đi mới chọn được. Lô liên kết lấy từ lô đã gán khi chấp nhận đơn.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="d-pr">Đơn mua hàng đã Chấp nhận *</Label>
            <Select
              id="d-pr"
              value={prId}
              onChange={(e) => {
                setPrId(e.target.value);
                // Ngày giao đã chốt trong đơn — điền sẵn để NCC chỉ phải chọn giờ.
                const pr = purchaseRequests.find((p) => p.id === e.target.value);
                setScheduledDate(pr?.deliveryDate ? formatVnDate(pr.deliveryDate) : "");
                clearError("pr"); clearError("date"); clearError("batches");
              }}
              aria-invalid={!!errors.pr}
              className={errors.pr ? "border-destructive focus-visible:ring-destructive" : undefined}
            >
              <option value="">— Chọn đơn —</option>
              {acceptedPrs.map((p) => (
                <option key={p.id} value={p.id}>{p.id} — {p.school} (giao {formatVnDate(p.deliveryDate)})</option>
              ))}
            </Select>
            <FieldError message={errors.pr} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="d-date">Ngày giao dự kiến *</Label>
              <DateInput id="d-date" value={scheduledDate} onChange={(e) => { setScheduledDate(e.target.value); clearError("date"); }} />
              <FieldError message={errors.date} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="d-time">Giờ giao dự kiến *</Label>
              <Input id="d-time" type="time" value={scheduledTime} onChange={(e) => { setScheduledTime(e.target.value); clearError("time"); }} />
              <FieldError message={errors.time} />
            </div>
          </div>
          <div className="rounded-lg border border-dashed p-2 text-xs text-muted-foreground">
            BR-071: chỉ chọn được thời điểm giao từ{" "}
            <span className="font-medium text-foreground">{formatVnDateTime(minScheduledAt.toISOString())}</span> trở đi
            (≥24 giờ kể từ bây giờ).
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="d-plate">Biển số xe *</Label>
            <Input
              id="d-plate"
              value={vehiclePlate}
              onChange={(e) => { setVehiclePlate(e.target.value); clearError("plate"); }}
              aria-invalid={!!errors.plate}
              className={errors.plate ? "border-destructive focus-visible:ring-destructive" : undefined}
              placeholder="VD: 29C-123.45"
            />
            <FieldError message={errors.plate} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="d-driver">Nhân viên giao hàng * (BR-050)</Label>
            <Select id="d-driver" value={driverId} onChange={(e) => { setDriverId(e.target.value); clearError("driver"); }}>
              {deliveryPersons.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.fullName} — {d.vehiclePlate} {d.biometricStatus === "REGISTERED" ? "(đã có sinh trắc học)" : "(CHƯA có sinh trắc học)"}
                </option>
              ))}
            </Select>
            <FieldError message={errors.driver} />
          </div>
          <div className="space-y-1.5">
            <Label>Lô nguyên liệu liên kết (BR-071 — gán lúc chấp nhận đơn)</Label>
            <div className={`max-h-40 space-y-1 overflow-y-auto rounded-lg border p-3 text-sm ${errors.batches ? "border-destructive" : "border-input"}`}>
              {!selectedPr ? (
                <p className="text-muted-foreground">Chọn một đơn ở trên để xem lô đã gán.</p>
              ) : assignedBatchIds.length === 0 ? (
                <p className="text-muted-foreground">Đơn này chưa được gán lô hàng.</p>
              ) : (
                (selectedPr.items ?? []).filter((it) => it.supplierBatchId).map((it) => {
                  const batchId = it.supplierBatchId!;
                  const batch = supplierBatches.find((b) => b.id === batchId);
                  const linkedTo = linkedElsewhere.get(batchId);
                  return (
                    <p key={it.ingredientId}>
                      <span className="font-medium">{it.ingredient}</span>: {batchId}
                      {batch && ` · Lô ${batch.lotNumber} · HSD ${formatVnDate(batch.expirationDate)}`}
                      {/* Dùng chung lô cho nhiều đơn là HỢP LỆ (BR-071) — đây là thông tin
                          tham chiếu, không phải lỗi, nên KHÔNG tô đỏ như bản trước. */}
                      {linkedTo && (
                        <span className="text-muted-foreground"> — cũng dùng cho đơn {linkedTo}</span>
                      )}
                    </p>
                  );
                })
              )}
            </div>
            <FieldError message={errors.batches} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending || prsFetching} onClick={submit}>
            {prsFetching ? "Đang tải đơn mới nhất…" : "Tạo chuyến giao"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function DeliveriesPage() {
  const [open, setOpen] = useState(false);
  const { data: driversPage } = usePagedList<DeliveryPersonDto>("delivery-persons", { pageSize: 50 });
  const drivers = driversPage?.items ?? [];
  return (
    <>
      <PageHeader
        title="Giao hàng"
        description="Quản lý chuyến giao (UC-039) — nhân viên giao xác thực khuôn mặt tại cổng trường (BR-051), GPS xác minh trong khu vực cho phép (BR-060)"
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus /> Tạo chuyến giao
          </Button>
        }
      />
      <DataTable<DeliveryDto>
        resource="deliveries"
        columns={makeColumns(drivers)}
        searchPlaceholder="Tìm theo mã chuyến, biển số xe…"
        rowHref={(d) => `/supplier/deliveries/${d.id}`}
      />
      <SubmitDeliveryDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
