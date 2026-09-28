"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowDownUp, Eye, History, PackageMinus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCreateAt, usePagedList } from "@/lib/api/hooks";
import type { InventoryBatchDto, InventoryTransactionDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";

const TX_TYPE_LABEL: Record<string, string> = { RECEIPT: "Nhập kho", ISSUE: "Xuất kho", ADJUSTMENT: "Điều chỉnh" };
const TX_TYPE_BADGE: Record<string, "success" | "warning" | "neutral"> = {
  RECEIPT: "success",
  ISSUE: "warning",
  ADJUSTMENT: "neutral",
};

function txColumns(): ColumnDef<InventoryTransactionDto, unknown>[] {
  return [
    { accessorKey: "occurredAt", header: "Thời điểm", cell: ({ row }) => formatVnDateTime(row.original.occurredAt) },
    {
      accessorKey: "type",
      header: "Loại",
      cell: ({ row }) => (
        <Badge variant={TX_TYPE_BADGE[row.original.type ?? ""] ?? "neutral"}>
          {TX_TYPE_LABEL[row.original.type ?? ""] ?? row.original.type}
        </Badge>
      ),
    },
    { accessorKey: "ingredient", header: "Nguyên liệu" },
    {
      accessorKey: "delta",
      header: "Số lượng",
      cell: ({ row }) => {
        const delta = row.original.delta ?? 0;
        return (
          <span className={delta >= 0 ? "text-success" : "text-destructive"}>
            {delta >= 0 ? "+" : ""}
            {delta} {row.original.unit}
          </span>
        );
      },
    },
    { accessorKey: "dish", header: "Món / bữa", cell: ({ row }) => row.original.dish ?? (row.original.reasonCode ?? "—") },
    { accessorKey: "performedBy", header: "Người thực hiện" },
  ];
}

/**
 * UC Manage Inventory — MF06 (Service Caterers).
 * BR-075: FEFO · BR-202: khớp khu vực DRY/CHILLED/FROZEN · BR-205: khóa lô hết hạn ·
 * BR-204: điều chỉnh thủ công bắt buộc mã lý do + ký quản lý.
 *
 * Nối backend thật 05/08/2026: `quantity`/`unit` tách riêng (BigDecimal + chuỗi đơn vị),
 * KHÔNG còn là 1 chuỗi `"40 kg"` như mock — `formatQty()` ghép lại để hiển thị.
 * `storageZone` là SCREAMING_SNAKE (`DRY`/`CHILLED`/`FROZEN`, D6), không phải `"Dry"`.
 * `status` (`IN_STOCK`/`EXPIRING_SOON`/`EXPIRED`) và `quarantine` (khoá riêng, BR-149) là
 * 2 field ĐỘC LẬP — `displayStatus()` cũ gộp chúng lại, thay bằng `displayStatus()`
 * cùng logic nhưng đọc field thật.
 */

function formatQty(quantity: number | undefined, unit: string | undefined): string {
  return `${quantity ?? 0} ${unit ?? ""}`.trim();
}

function displayStatus(b: Pick<InventoryBatchDto, "status" | "quarantine">): string {
  return b.quarantine ? "QUARANTINED" : (b.status ?? "");
}

const zoneBadge: Record<string, "info" | "primary" | "neutral"> = {
  DRY: "neutral",
  CHILLED: "info",
  FROZEN: "primary",
};

/**
 * Giá trị `value` PHẢI khớp enum Java `ReasonCode` (SCREAMING_SNAKE, D6) — backend
 * nhận `POST /inventory-transactions/adjustments` với `reasonCode` kiểu enum, gửi
 * chuỗi sai case (`"spoilage"` thay vì `"SPOILAGE"`) là 400 ngay ở Jackson, không
 * lặng lẽ như lệch tên field.
 */
const REASON_CODES = [
  { value: "SPOILAGE", label: "SPOILAGE — hư hỏng/ôi thiu" },
  { value: "COUNT_CORRECTION", label: "COUNT_CORRECTION — chênh lệch kiểm kê" },
  { value: "DAMAGE", label: "DAMAGE — hỏng vỡ khi lưu kho" },
  { value: "RETURN_TO_SUPPLIER", label: "RETURN_TO_SUPPLIER — trả NCC" },
  // 13/08/2026 — nấu thực tế cần nhiều nguyên liệu hơn định mức hệ thống tính (BR-204).
  { value: "EXTRA_USAGE", label: "EXTRA_USAGE — dùng nhiều hơn định mức khi nấu" },
];

function batchColumns(
  onDetail: (b: InventoryBatchDto) => void
): ColumnDef<InventoryBatchDto, unknown>[] {
  return [
    { accessorKey: "id", header: "Mã lô", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "ingredient", header: "Nguyên liệu" },
    { accessorKey: "supplier", header: "NCC" },
    {
      accessorKey: "quantity",
      header: "Số lượng",
      cell: ({ row }) => formatQty(row.original.remaining ?? row.original.quantity, row.original.unit),
    },
    {
      accessorKey: "storageZone",
      header: "Khu vực (BR-202)",
      cell: ({ row }) => (
        <Badge variant={zoneBadge[row.original.storageZone ?? ""]}>{row.original.storageZone}</Badge>
      ),
    },
    {
      accessorKey: "receivedDate",
      header: "Ngày nhận",
      cell: ({ row }) => formatVnDate(row.original.receivedDate),
    },
    {
      accessorKey: "expiryDate",
      header: "Hạn dùng (FEFO — BR-075)",
      cell: ({ row }) => formatVnDate(row.original.expiryDate),
    },
    { accessorKey: "status", header: "Trạng thái (BR-205)", cell: ({ row }) => <StatusBadge status={displayStatus(row.original)} /> },
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

function AdjustStockDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  /**
   * `POST /inventory-transactions/adjustments` (SERVICE_CATERERS) — subpath thật,
   * KHÔNG phải `POST /inventory-transactions` trần (sổ append-only không có verb
   * tạo trực tiếp). Request thật chỉ nhận `{batchId, delta, unit, reasonCode,
   * managerSignature}` — `id`/`timestamp`/`type`/`ingredient`/`actor` đều do server
   * tự sinh (D1/D9), gửi thêm KHÔNG lỗi (Jackson bỏ field lạ) nhưng vô nghĩa.
   */
  const create = useCreateAt<{
    batchId: string;
    delta: number;
    unit: string;
    reasonCode: string;
    managerSignature: string;
    serviceDate?: string;
    mealType?: string;
  }>("inventory-transactions", "adjustments");
  const [batchId, setBatchId] = useState("");
  const [qty, setQty] = useState("");
  const [reasonCode, setReasonCode] = useState("");
  const [managerSignature, setManagerSignature] = useState("");
  // 13/08/2026 — tùy chọn: gắn điều chỉnh (thường là EXTRA_USAGE) vào đúng bữa/ngày để
  // kiểm thực Bước 1 auto-fill và "Phạm vi bữa ăn" nhận diện được, giống hệt xuất kho FEFO
  // (xem step1RowsOfIssue() trong domain-utils.ts).
  const [serviceDate, setServiceDate] = useState("");
  const [mealType, setMealType] = useState("");
  const [error, setError] = useState("");

  // BR-205: lô hết hạn/cách ly không điều chỉnh xuất dùng — chỉ hiển thị lô thao tác được.
  // Đọc qua API: mảng seed `INVENTORY` đã bị clone khi dựng db nên không bao giờ
  // phản ánh lô Y tế vừa khoá — bếp sẽ vẫn điều chỉnh được lô đang bị cách ly.
  const { data: batchData } = usePagedList<InventoryBatchDto>("inventory", { pageSize: 200 });
  const adjustableBatches = (batchData?.items ?? []).filter((b) => b.quarantine == null);
  const batch = adjustableBatches.find((b) => b.id === batchId);

  function submit() {
    if (!batchId || !Number(qty)) {
      setError("Chọn lô và nhập lượng điều chỉnh (âm để giảm, dương để tăng).");
      return;
    }
    // BR-204: bắt buộc mã lý do + chữ ký quản lý
    if (!reasonCode) {
      setError("Mã lý do là bắt buộc cho điều chỉnh thủ công (BR-204).");
      return;
    }
    if (!managerSignature.trim()) {
      setError("Điều chỉnh thủ công phải có xác nhận (ký) của quản lý (BR-204).");
      return;
    }
    setError("");
    const unit = batch?.unit ?? "kg";
    create.mutate(
      {
        batchId,
        delta: Number(qty),
        unit,
        reasonCode,
        managerSignature,
        serviceDate: serviceDate || undefined,
        mealType: mealType || undefined,
      },
      {
        onSuccess: () => {
          toast.success(`Đã ghi điều chỉnh ${batchId} (${reasonCode}) — ký: ${managerSignature} (BR-204).`);
          onOpenChange(false);
          setBatchId("");
          setQty("");
          setReasonCode("");
          setManagerSignature("");
          setServiceDate("");
          setMealType("");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Điều chỉnh tồn kho thủ công (BR-204)</DialogTitle>
          <DialogDescription>
            Bắt buộc mã lý do + xác nhận quản lý; mọi điều chỉnh ghi vào
            Inventory Transactions Log (append-only).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể điều chỉnh">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="adj-batch">Lô nguyên liệu *</Label>
            <Select id="adj-batch" value={batchId} onChange={(e) => setBatchId(e.target.value)}>
              <option value="">— Chọn lô —</option>
              {adjustableBatches.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.id} — {b.ingredient} ({formatQty(b.remaining ?? b.quantity, b.unit)})
                </option>
              ))}
            </Select>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="adj-qty">Lượng điều chỉnh * (VD: -2)</Label>
              <Input id="adj-qty" type="number" value={qty} onChange={(e) => setQty(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="adj-reason">Mã lý do * (BR-204)</Label>
              <Select id="adj-reason" value={reasonCode} onChange={(e) => setReasonCode(e.target.value)}>
                <option value="">— Chọn mã —</option>
                {REASON_CODES.map((r) => (
                  <option key={r.value} value={r.value}>{r.label}</option>
                ))}
              </Select>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="adj-sign">Quản lý xác nhận (ký) * (BR-204)</Label>
            <Input
              id="adj-sign"
              value={managerSignature}
              onChange={(e) => setManagerSignature(e.target.value)}
              placeholder="VD: Lê Thị Mai"
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="adj-date">Ngày phục vụ (tùy chọn)</Label>
              <Input
                id="adj-date"
                type="date"
                value={serviceDate}
                onChange={(e) => setServiceDate(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="adj-meal">Buổi ăn (tùy chọn)</Label>
              <Select id="adj-meal" value={mealType} onChange={(e) => setMealType(e.target.value)}>
                <option value="">— Không gắn bữa —</option>
                {Object.entries(MEAL_TYPE_LABEL).map(([mt, label]) => (
                  <option key={mt} value={mt}>{label}</option>
                ))}
              </Select>
            </div>
          </div>
          {(serviceDate || mealType) && (
            <p className="text-xs text-muted-foreground">
              Điều chỉnh gắn với{" "}
              {mealType ? MEAL_TYPE_LABEL[mealType as MealType] : "buổi ăn (chưa chọn)"}{" "}
              {serviceDate ? `ngày ${serviceDate.split("-").reverse().join("/")}` : "(chưa chọn ngày)"} — sẽ được
              nhận diện ở Bước 1 kiểm thực và Phạm vi bữa ăn, giống xuất kho FEFO.
            </p>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Ghi điều chỉnh</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function InventoryPage() {
  const [adjustOpen, setAdjustOpen] = useState(false);
  const [detail, setDetail] = useState<InventoryBatchDto | null>(null);
  const [view, setView] = useState<"batches" | "ledger">("batches");
  const { data: batchData } = usePagedList<InventoryBatchDto>("inventory", { pageSize: 200 });
  const lockedBatches = (batchData?.items ?? []).filter((b) => b.quarantine != null);

  return (
    <>
      <PageHeader
        title="Kho nguyên liệu"
        description="Lô hết hạn bị khóa tự động, không thể chọn xuất kho (BR-205); xuất kho ưu tiên FEFO (BR-075)"
        actions={
          <>
            <Button variant="outline" onClick={() => setAdjustOpen(true)}>
              <ArrowDownUp /> Điều chỉnh (BR-204)
            </Button>
            <Link href="/service-caterers/inventory/issue">
              <Button>
                <PackageMinus /> Xuất kho FEFO
              </Button>
            </Link>
          </>
        }
      />
      {lockedBatches.length > 0 && (
        <Alert variant="danger" className="mb-6" title={`${lockedBatches.length} lô đang bị cách ly (BR-149)`}>
          {lockedBatches.map((b) => (
            <p key={b.id} className="mt-0.5">
              <b>{b.id}</b> — {b.ingredient}: {b.quarantine?.reason}
              {b.quarantine?.incidentId && (
                <>
                  {" "}
                  <Link
                    href={`/service-caterers/incidents/${b.quarantine.incidentId}`}
                    className="underline underline-offset-2"
                  >
                    ({b.quarantine.incidentId})
                  </Link>
                </>
              )}
            </p>
          ))}
          <p className="mt-1.5 text-xs">
            Các lô này không xuất kho và không điều chỉnh được cho tới khi Y tế mở khoá theo
            kết luận điều tra. Cần dùng gấp thì liên hệ Y tế, đừng chờ — thực đơn dùng nguyên
            liệu này sẽ báo thiếu hàng.
          </p>
        </Alert>
      )}

      <Tabs value={view} onValueChange={(v) => setView(v as typeof view)} className="mb-4">
        <TabsList>
          <TabsTrigger value="batches">Danh sách lô</TabsTrigger>
          <TabsTrigger value="ledger">
            <History className="mr-1.5 size-4" /> Lịch sử giao dịch kho
          </TabsTrigger>
        </TabsList>
      </Tabs>

      {view === "batches" ? (
        <DataTable<InventoryBatchDto>
          resource="inventory"
          columns={batchColumns(setDetail)}
          pageSize={10}
          searchPlaceholder="Tìm theo nguyên liệu, mã lô…"
        />
      ) : (
        <DataTable<InventoryTransactionDto>
          resource="inventory-transactions"
          columns={txColumns()}
          pageSize={10}
          searchPlaceholder="Tìm theo nguyên liệu…"
          emptyMessage="Chưa có giao dịch kho nào — nhận hàng/xuất kho/điều chỉnh sẽ ghi vào sổ này."
        />
      )}
      <AdjustStockDialog open={adjustOpen} onOpenChange={setAdjustOpen} />

      {/* Chi tiết lô kho (nhận xét Minh 17/07 — cột Xem chi tiết) */}
      <Dialog open={detail != null} onOpenChange={(v) => !v && setDetail(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Lô {detail?.id}</DialogTitle>
            <DialogDescription>
              Thông tin lô nguyên liệu trong kho — nguồn truy xuất khi có sự cố (BR-151).
            </DialogDescription>
          </DialogHeader>
          {detail && (
            <div className="space-y-2 text-sm">
              {(
                [
                  ["Nguyên liệu", detail.ingredient ?? "—"],
                  ["Nhà cung cấp", detail.supplier ?? "—"],
                  ["Số lượng tồn", formatQty(detail.remaining ?? detail.quantity, detail.unit)],
                  ["Ngày nhận", formatVnDate(detail.receivedDate)],
                  ["Hạn dùng (FEFO — BR-075)", formatVnDate(detail.expiryDate)],
                ] as const
              ).map(([label, value]) => (
                <div key={label} className="flex justify-between gap-4 border-b pb-2 last:border-0">
                  <span className="text-muted-foreground">{label}</span>
                  <span className="text-right font-medium">{value}</span>
                </div>
              ))}
              <div className="flex justify-between gap-4 border-b pb-2">
                <span className="text-muted-foreground">Khu vực lưu (BR-202)</span>
                <Badge variant={zoneBadge[detail.storageZone ?? ""]}>{detail.storageZone}</Badge>
              </div>
              <div className="flex justify-between gap-4">
                <span className="text-muted-foreground">Trạng thái (BR-205)</span>
                <StatusBadge status={displayStatus(detail)} />
              </div>
              {/*
                Lô đang bị cách ly: trước đây bếp chỉ thấy nó biến mất khỏi danh
                sách điều chỉnh và không xuất kho được, không biết ai khoá, vì sự
                cố nào, hay phải chờ ai mở. Cách ly là quyết định của người khác
                áp lên kho của mình, nên tối thiểu phải nói rõ lý do và nguồn.
              */}
              {detail.quarantine && (
                <div className="mt-1 space-y-1.5 rounded-lg border border-destructive/40 bg-destructive/5 p-3 text-xs">
                  <p className="font-medium text-destructive">Lô đang bị cách ly (BR-149)</p>
                  <p>{detail.quarantine.reason}</p>
                  <p className="text-muted-foreground">
                    {detail.quarantine.by} · {formatVnDateTime(detail.quarantine.at)}
                  </p>
                  {detail.quarantine.incidentId && (
                    <p className="text-muted-foreground">
                      Theo sự cố{" "}
                      <Link
                        href={`/service-caterers/incidents/${detail.quarantine.incidentId}`}
                        className="font-medium underline underline-offset-2"
                      >
                        {detail.quarantine.incidentId}
                      </Link>
                    </p>
                  )}
                  <p className="text-muted-foreground">
                    Chỉ Y tế mở khoá được, sau khi có kết luận điều tra. Bếp không tự nhả.
                  </p>
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
