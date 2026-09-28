"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowDownUp, Eye, PackageMinus } from "lucide-react";
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
import { useCreate, usePagedList } from "@/lib/api/hooks";
import {
  batchDisplayStatus, type InventoryBatch, type InventoryTransaction } from "@/lib/mock-data";

/**
 * UC Manage Inventory — MF06 (Service Caterers).
 * BR-075: FEFO · BR-202: khớp khu vực Dry/Chilled/Frozen · BR-205: khóa lô hết hạn ·
 * BR-204: điều chỉnh thủ công bắt buộc mã lý do + ký quản lý.
 */

const zoneBadge: Record<InventoryBatch["storageZone"], "info" | "primary" | "neutral"> = {
  Dry: "neutral",
  Chilled: "info",
  Frozen: "primary",
};

const REASON_CODES = [
  { value: "spoilage", label: "spoilage — hư hỏng/ôi thiu" },
  { value: "count-correction", label: "count-correction — chênh lệch kiểm kê" },
  { value: "damage", label: "damage — hỏng vỡ khi lưu kho" },
  { value: "return-to-supplier", label: "return-to-supplier — trả NCC" },
];

function batchColumns(
  onDetail: (b: InventoryBatch) => void
): ColumnDef<InventoryBatch, unknown>[] {
  return [
    { accessorKey: "id", header: "Mã lô", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "ingredient", header: "Nguyên liệu" },
    { accessorKey: "supplier", header: "NCC" },
    { accessorKey: "quantity", header: "Số lượng" },
    {
      accessorKey: "storageZone",
      header: "Khu vực (BR-202)",
      cell: ({ row }) => <Badge variant={zoneBadge[row.original.storageZone]}>{row.original.storageZone}</Badge>,
    },
    { accessorKey: "receivedDate", header: "Ngày nhận" },
    { accessorKey: "expiryDate", header: "Hạn dùng (FEFO — BR-075)" },
    { accessorKey: "status", header: "Trạng thái (BR-205)", cell: ({ row }) => <StatusBadge status={batchDisplayStatus(row.original)} /> },
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
  const create = useCreate<Partial<InventoryTransaction>>("inventory-transactions");
  const [batchId, setBatchId] = useState("");
  const [qty, setQty] = useState("");
  const [reasonCode, setReasonCode] = useState("");
  const [managerSignature, setManagerSignature] = useState("");
  const [error, setError] = useState("");

  // BR-205: lô hết hạn/cách ly không điều chỉnh xuất dùng — chỉ hiển thị lô thao tác được.
  // Đọc qua API: mảng seed `INVENTORY` đã bị clone khi dựng db nên không bao giờ
  // phản ánh lô Y tế vừa khoá — bếp sẽ vẫn điều chỉnh được lô đang bị cách ly.
  const { data: batchData } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
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
    const unit = batch?.quantity.replace(/^[\d.,\s]+/, "") ?? "kg";
    create.mutate(
      {
        id: `TXN-${String(Date.now()).slice(-6)}`,
        timestamp: new Date().toLocaleString("vi-VN"),
        type: "Điều chỉnh",
        batchId,
        ingredient: batch?.ingredient ?? "",
        quantity: `${Number(qty) > 0 ? "+" : ""}${qty} ${unit}`,
        reasonCode,
        actor: "Trần Văn Hùng",
        managerSignature,
      },
      {
        onSuccess: () => {
          toast.success(`Đã ghi điều chỉnh ${batchId} (${reasonCode}) — ký: ${managerSignature} (BR-204).`);
          onOpenChange(false);
          setBatchId("");
          setQty("");
          setReasonCode("");
          setManagerSignature("");
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
                  {b.id} — {b.ingredient} ({b.quantity})
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
  const [detail, setDetail] = useState<InventoryBatch | null>(null);
  const { data: batchData } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
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

      <DataTable<InventoryBatch>
        resource="inventory"
        columns={batchColumns(setDetail)}
        pageSize={10}
        searchPlaceholder="Tìm theo nguyên liệu, mã lô…"
      />
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
                  ["Nguyên liệu", detail.ingredient],
                  ["Nhà cung cấp", detail.supplier],
                  ["Số lượng tồn", detail.quantity],
                  ["Ngày nhận", detail.receivedDate],
                  ["Hạn dùng (FEFO — BR-075)", detail.expiryDate],
                ] as const
              ).map(([label, value]) => (
                <div key={label} className="flex justify-between gap-4 border-b pb-2 last:border-0">
                  <span className="text-muted-foreground">{label}</span>
                  <span className="text-right font-medium">{value}</span>
                </div>
              ))}
              <div className="flex justify-between gap-4 border-b pb-2">
                <span className="text-muted-foreground">Khu vực lưu (BR-202)</span>
                <Badge variant={zoneBadge[detail.storageZone]}>{detail.storageZone}</Badge>
              </div>
              <div className="flex justify-between gap-4">
                <span className="text-muted-foreground">Trạng thái (BR-205)</span>
                <StatusBadge status={batchDisplayStatus(detail)} />
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
                    {detail.quarantine.by} · {detail.quarantine.at}
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
