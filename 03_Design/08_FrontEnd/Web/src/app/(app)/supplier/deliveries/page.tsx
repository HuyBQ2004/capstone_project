"use client";

import { useState } from "react";
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
import { useCreate, useUpdate, usePagedList } from "@/lib/api/hooks";
import {
  type Delivery,
  type PurchaseRequest,
  type SupplierDeliveryPerson,
  type SupplierBatch,
} from "@/lib/mock-data";

/**
 * Screen Spec 18/19/21 — Deliveries / Delivery List / Submit Delivery Information.
 * UC-039 (Manage Deliveries), UC-083 (Submit Delivery Information).
 * BR-071: nộp thông tin giao hàng ≥24h trước giờ giao, kèm ≥1 lô liên kết.
 * BR-050: driver chọn từ nhân viên giao đã đăng ký sinh trắc học.
 * Review 07/2026: lô đã liên kết với đơn hàng khác thì KHÔNG cho chọn;
 * validate theo từng trường (inline error).
 * Nhận xét 23/07/2026: đây giờ là NƠI DUY NHẤT tạo chuyến giao — "Chấp nhận đơn"
 * bên Đơn mua hàng chỉ gán lô theo nguyên liệu (BR-071) và chuyển PR sang
 * APPROVED, không còn tự tạo chuyến giao. Lô liên kết ở đây lấy thẳng từ
 * `pr.batchAssignments` đã gán lúc chấp nhận đơn — không chọn lại tay.
 */

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="text-xs font-medium text-destructive">{message}</p>;
}

/** Ghép "dd/mm/yyyy" + "HH:mm" → Date; trả null nếu không hợp lệ. */
function parseVnDateTime(date: string, time: string): Date | null {
  const m = date.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  const t = time.trim().match(/^(\d{2}):(\d{2})$/);
  if (!m || !t) return null;
  const d = new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1]), Number(t[1]), Number(t[2]));
  return Number.isNaN(d.getTime()) ? null : d;
}

const columns: ColumnDef<Delivery, unknown>[] = [
  { accessorKey: "id", header: "Mã chuyến", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "school", header: "Trường" },
  { accessorKey: "scheduledAt", header: "Lịch giao" },
  { accessorKey: "driver", header: "Nhân viên giao (BR-050)" },
  { accessorKey: "vehiclePlate", header: "Biển số xe" },
  {
    id: "batches",
    header: "Lô liên kết (BR-071)",
    cell: ({ row }) => row.original.linkedBatches?.length ?? row.original.batches,
  },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

function SubmitDeliveryDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatch>("supplier-batches", { pageSize: 200 });
  const supplierBatches = supplierBatchesPage?.items ?? [];
  const { data: deliveryPersonsPage } = usePagedList<SupplierDeliveryPerson>("delivery-persons", { pageSize: 50 });
  const deliveryPersons = deliveryPersonsPage?.items ?? [];
  // forceFresh: dialog này ĐỌC `batchAssignments` của đơn rồi GHI NGAY vào
  // Delivery — không được phép dùng bản cache cũ (nhận xét 23/07/2026: đây là
  // chỗ đã gây ra lỗi "gán 2 lô nhưng chuyến giao chỉ lưu 1 lô", vì thường mở
  // trang này ngay sau khi vừa chấp nhận/sửa lô ở trang Đơn mua hàng — nếu
  // dùng cache, `invalidateQueries` của bước trước có thể chưa refetch xong).
  const { data: purchaseRequestsPage, isFetching: prsFetching } = usePagedList<PurchaseRequest>(
    "purchase-requests",
    { pageSize: 100 },
    { forceFresh: true }
  );
  const purchaseRequests = purchaseRequestsPage?.items ?? [];
  const create = useCreate<Partial<Delivery>>("deliveries");
  const updatePr = useUpdate<{ id: string } & Partial<PurchaseRequest>>("purchase-requests");

  // Review 07/2026 → nhận xét 23/07/2026: lô đã liên kết với chuyến giao/đơn hàng
  // khác → không cho chọn lại; đơn đã có chuyến giao rồi thì không hiện lại ở đây.
  const { data: deliveriesPage } = usePagedList<Delivery>("deliveries", { pageSize: 100 }, { forceFresh: true });
  const linkedElsewhere = new Map<string, string>();
  const prsWithDelivery = new Set<string>();
  for (const d of deliveriesPage?.items ?? []) {
    // Chuyến đã Hủy không tính là "đã có chuyến giao" — cho phép tạo chuyến
    // giao mới thay thế (VD: chuyến trước bị lưu sai lô do lỗi 23/07/2026).
    if (d.purchaseRequestId && d.status !== "CANCELLED") prsWithDelivery.add(d.purchaseRequestId);
    if (d.status === "CANCELLED") continue;
    for (const b of d.linkedBatches ?? []) {
      if (!linkedElsewhere.has(b)) linkedElsewhere.set(b, d.purchaseRequestId ?? d.id);
    }
  }
  // Chỉ đơn đã Chấp nhận (APPROVED, đã gán lô theo BR-071) và CHƯA có chuyến giao.
  const acceptedPrs = purchaseRequests.filter(
    (p) => p.status === "APPROVED" && !prsWithDelivery.has(p.id)
  );

  const [prId, setPrId] = useState("");
  const [dispatchDate, setDispatchDate] = useState("");
  const [dispatchTime, setDispatchTime] = useState("");
  const [vehiclePlate, setVehiclePlate] = useState("");
  const [driverId, setDriverId] = useState(deliveryPersons[0]?.id ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});

  const selectedPr = purchaseRequests.find((p) => p.id === prId);
  // Lô liên kết KHÔNG chọn tay ở đây nữa — lấy thẳng từ batchAssignments mà NCC
  // đã gán theo từng nguyên liệu lúc "Chấp nhận đơn" (BR-071).
  const assignedBatchIds = [...new Set(Object.values(selectedPr?.batchAssignments ?? {}))];

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function submit() {
    const next: Record<string, string> = {};
    // Chặn ghi dựa trên dữ liệu đang refetch — tránh lặp lại lỗi "gán 2 lô
    // nhưng chuyến giao chỉ lưu 1 lô" do dùng bản cache cũ chưa kịp làm mới.
    if (prsFetching) {
      setErrors({ pr: "Đang tải danh sách đơn mới nhất — vui lòng đợi rồi thử lại." });
      return;
    }
    if (!prId) next.pr = "Vui lòng chọn đơn mua hàng đã Chấp nhận.";
    if (!vehiclePlate.trim()) next.plate = "Biển số xe không được để trống.";

    const dispatch = parseVnDateTime(dispatchDate, dispatchTime);
    if (!dispatchDate.trim()) next.date = "Ngày xuất kho không được để trống.";
    if (!dispatchTime.trim()) next.time = "Giờ xuất kho không được để trống.";
    if (dispatchDate.trim() && dispatchTime.trim()) {
      if (!dispatch) {
        next.date = "Ngày xuất kho phải theo định dạng dd/mm/yyyy và giờ theo HH:mm.";
      } else if (dispatch < new Date(Date.now() + 24 * 60 * 60 * 1000)) {
        next.date = "BR-071: thông tin giao hàng phải nộp tối thiểu 24 giờ trước thời điểm giao dự kiến.";
      }
    }

    const driver = deliveryPersons.find((d) => d.id === driverId);
    if (!driverId || !driver) {
      next.driver = "Vui lòng chọn nhân viên giao hàng (BR-050).";
    } else if (driver.biometricStatus !== "REGISTERED") {
      next.driver = `BR-050: nhân viên "${driver.fullName}" chưa đăng ký sinh trắc học khuôn mặt — không thể phân công giao hàng.`;
    }

    if (prId && assignedBatchIds.length === 0) {
      next.batches = "Đơn chưa được gán lô hàng ở bước Chấp nhận đơn (BR-071) — quay lại Đơn mua hàng để gán lô trước.";
    } else if (assignedBatchIds.length > 0) {
      const conflict = assignedBatchIds.find((b) => linkedElsewhere.has(b));
      if (conflict) next.batches = `Lô ${conflict} đã được liên kết với đơn khác — kiểm tra lại gán lô của đơn này.`;
    }

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;
    const pr = selectedPr;
    if (!pr || !driver) return;
    const scheduledAt = `${dispatchDate} ${dispatchTime}`;
    create.mutate(
      {
        id: `DLV-2026-${String(Date.now()).slice(-4)}`,
        school: pr.school,
        scheduledAt,
        driver: driver.fullName,
        vehiclePlate,
        batches: assignedBatchIds.length,
        status: "SCHEDULED",
        purchaseRequestId: pr.id,
        linkedBatches: assignedBatchIds,
        batchAssignments: pr.batchAssignments,
      },
      {
        onSuccess: () => {
          updatePr.mutate({ id: pr.id, confirmedDeliveryAt: scheduledAt });
          toast.success("Đã tạo chuyến giao — trạng thái Scheduled, Service Caterers được thông báo (UC-083)");
          onOpenChange(false);
          setPrId(""); setDispatchDate(""); setDispatchTime(""); setVehiclePlate("");
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
            giờ trước giờ giao (BR-071). Lô liên kết lấy từ lô đã gán khi chấp
            nhận đơn.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="d-pr">Đơn mua hàng đã Chấp nhận *</Label>
            <Select
              id="d-pr"
              value={prId}
              onChange={(e) => { setPrId(e.target.value); clearError("pr"); clearError("batches"); }}
              aria-invalid={!!errors.pr}
              className={errors.pr ? "border-destructive focus-visible:ring-destructive" : undefined}
            >
              <option value="">— Chọn đơn —</option>
              {acceptedPrs.map((p) => (
                <option key={p.id} value={p.id}>{p.id} — {p.school} (giao {p.deliveryDate})</option>
              ))}
            </Select>
            <FieldError message={errors.pr} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="d-date">Ngày xuất kho * (≥24h)</Label>
              <DateInput id="d-date" value={dispatchDate} onChange={(e) => { setDispatchDate(e.target.value); clearError("date"); }} />
              <FieldError message={errors.date} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="d-time">Giờ xuất kho *</Label>
              <Input id="d-time" type="time" value={dispatchTime} onChange={(e) => { setDispatchTime(e.target.value); clearError("time"); }} />
              <FieldError message={errors.time} />
            </div>
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
                Object.entries(selectedPr.batchAssignments ?? {}).map(([ingredient, batchId]) => {
                  const batch = supplierBatches.find((b) => b.id === batchId);
                  const linkedTo = linkedElsewhere.get(batchId);
                  return (
                    <p key={ingredient} className={linkedTo ? "text-destructive" : undefined}>
                      <span className="font-medium">{ingredient}</span>: {batchId}
                      {batch && ` · Lô ${batch.lotNumber} · HSD ${batch.expirationDate}`}
                      {linkedTo && ` — đã liên kết với đơn ${linkedTo}`}
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
      <DataTable<Delivery>
        resource="deliveries"
        columns={columns}
        searchPlaceholder="Tìm theo trường, nhân viên giao…"
        rowHref={(d) => `/supplier/deliveries/${d.id}`}
      />
      <SubmitDeliveryDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
