"use client";

import { useState } from "react";
import { Ban, Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
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
import { DateInput, Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import {
  CURRENT_SUPPLIER_ID,
  productLabel,
  type SupplierDocument, type SupplierProduct, type SupplierBatch
} from "@/lib/mock-data";

/**
 * Screen Spec 12/13/15 — Ingredient Batches / Batch List / Submit Batch Information.
 * UC-043 (Manage Ingredient Batches), UC-081 (Submit Batch Information).
 * BR-071: lô bắt buộc kèm số lô, NSX, ngày đóng gói, HSD, số lượng, chứng chỉ liên kết.
 * BR-048 + MSG-013: chặn nộp nếu chứng chỉ liên kết đã hết hạn.
 * Review 07/2026: "Số lượng" tách thành Số + Đơn vị tính; validate theo từng trường.
 */

/** Đơn vị tính cho số lượng lô — Review 07/2026. */
const QUANTITY_UNITS = ["kg", "g", "lít", "ml", "quả", "hộp", "bao", "thùng"];

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="text-xs font-medium text-destructive">{message}</p>;
}

/**
 * Vòng đời lô (Minh 25/07/2026): Hoạt động → Đang giao (gán vào chuyến) →
 * Đã giao (trường nhận) / trở về Hoạt động (bị trả về hoặc chuyến hủy);
 * Hết hạn theo HSD; Đã hủy khi NCC tự hủy lô — chỉ hủy được lô đang Hoạt động.
 */
function makeColumns(onCancel: (b: SupplierBatch) => void): ColumnDef<SupplierBatch, unknown>[] {
  return [
    { accessorKey: "lotNumber", header: "Số lô (BR-071)", cell: ({ row }) => <span className="font-medium">{row.original.lotNumber}</span> },
    { accessorKey: "productName", header: "Mặt hàng" },
    { accessorKey: "quantity", header: "Số lượng" },
    { accessorKey: "packagingDate", header: "Ngày đóng gói" },
    { accessorKey: "expirationDate", header: "Hạn sử dụng" },
    {
      id: "certificates",
      header: "Chứng chỉ liên kết",
      cell: ({ row }) => <Badge variant="info">{row.original.certificates.length} tài liệu</Badge>,
    },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex justify-end">
          {row.original.status === "ACTIVE" && (
            <Button
              variant="ghost"
              size="sm"
              className="text-destructive hover:text-destructive"
              onClick={(e) => {
                // Hàng có rowHref — chặn click lan ra điều hướng chi tiết lô
                e.stopPropagation();
                onCancel(row.original);
              }}
            >
              <Ban /> Hủy lô
            </Button>
          )}
        </div>
      ),
    },
  ];
}

/** NCC tự hủy lô đã đăng ký (chỉ lô Hoạt động) — lý do bắt buộc để lưu hồ sơ. */
function CancelBatchDialog({
  batch,
  onOpenChange,
}: {
  batch: SupplierBatch | null;
  onOpenChange: (v: boolean) => void;
}) {
  const update = useUpdate<{ id: string } & Partial<SupplierBatch>>("supplier-batches");
  const [reason, setReason] = useState("");
  const [reasonError, setReasonError] = useState("");

  function submit() {
    if (!reason.trim()) {
      setReasonError("Lý do hủy lô không được để trống.");
      return;
    }
    setReasonError("");
    update.mutate(
      { id: batch!.id, status: "CANCELLED" },
      {
        onSuccess: () => {
          toast.success(`Đã hủy lô ${batch!.lotNumber} (${batch!.productName ?? batch!.id}) — lô không còn gán được vào đơn hàng.`);
          setReason("");
          onOpenChange(false);
        },
      }
    );
  }

  return (
    <Dialog open={batch != null} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Hủy lô {batch?.lotNumber}</DialogTitle>
          <DialogDescription>
            {batch?.productName ?? batch?.id} · {batch?.quantity} · HSD {batch?.expirationDate}.
            Lô đã hủy không gán được vào đơn hàng nữa; thao tác không hoàn tác.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label htmlFor="cb-reason">Lý do hủy *</Label>
          <Textarea
            id="cb-reason"
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              if (reasonError && e.target.value.trim()) setReasonError("");
            }}
            aria-invalid={!!reasonError}
            className={reasonError ? "border-destructive focus-visible:ring-destructive" : undefined}
            placeholder="VD: hàng hỏng trong kho, sai thông tin đăng ký…"
          />
          {reasonError && <p className="text-xs font-medium text-destructive">{reasonError}</p>}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Đóng</Button>
          <Button variant="destructive" disabled={update.isPending} onClick={submit}>
            <Ban /> Hủy lô
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/** Chuyển "dd/mm/yyyy" → Date; trả null nếu không hợp lệ. */
function parseVnDate(value: string): Date | null {
  const m = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!m) return null;
  const d = new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1]));
  return Number.isNaN(d.getTime()) ? null : d;
}

function SubmitBatchDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: docsPage } = usePagedList<SupplierDocument>("supplier-documents", { pageSize: 100 });
  const supplierDocs = docsPage?.items ?? [];
  const create = useCreate<Partial<SupplierBatch>>("supplier-batches");
  /*
   * Lô phải thuộc về một SẢN PHẨM đã khai, không gõ tên tự do. Tên tự do là chỗ
   * đứt của cả chuỗi truy xuất: trường nhận hàng phải dò tên để đoán ra nguyên
   * liệu nào trong danh mục, lệch một chữ là lô vào kho mà không nối được về đâu.
   */
  const { data: productPage } = usePagedList<SupplierProduct>("supplier-products", {
    pageSize: 200,
    supplierId: CURRENT_SUPPLIER_ID,
  });
  const products = productPage?.items ?? [];
  const [productId, setProductId] = useState("");
  const [lotNumber, setLotNumber] = useState("");
  // Review 07/2026: tách "Số lượng" thành Số + Đơn vị tính.
  const [quantityValue, setQuantityValue] = useState("");
  const [quantityUnit, setQuantityUnit] = useState("kg");
  const [manufacturingDate, setManufacturingDate] = useState("");
  const [packagingDate, setPackagingDate] = useState("");
  const [expirationDate, setExpirationDate] = useState("");
  const [certIds, setCertIds] = useState<string[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function toggleCert(id: string) {
    setCertIds((prev) => (prev.includes(id) ? prev.filter((c) => c !== id) : [...prev, id]));
    clearError("certs");
  }

  function reset() {
    setProductId(""); setLotNumber(""); setQuantityValue(""); setQuantityUnit("kg");
    setManufacturingDate(""); setPackagingDate(""); setExpirationDate("");
    setCertIds([]); setErrors({});
  }

  function submit() {
    const next: Record<string, string> = {};
    if (!productId) next.name = "Chọn sản phẩm — lô hàng phải thuộc về một mặt hàng đã khai.";
    if (!lotNumber.trim()) next.lot = "Số lô không được để trống.";

    if (!quantityValue.trim()) next.qty = "Số lượng không được để trống.";
    else if (!/^\d+([.,]\d+)?$/.test(quantityValue.trim()) || Number(quantityValue.replace(",", ".")) <= 0)
      next.qty = "Số lượng phải là số lớn hơn 0.";

    const mfg = parseVnDate(manufacturingDate);
    const pkg = parseVnDate(packagingDate);
    const exp = parseVnDate(expirationDate);
    if (!manufacturingDate.trim()) next.mfg = "Ngày sản xuất không được để trống.";
    else if (!mfg) next.mfg = "Ngày sản xuất phải theo định dạng dd/mm/yyyy.";
    if (!packagingDate.trim()) next.pkg = "Ngày đóng gói không được để trống.";
    else if (!pkg) next.pkg = "Ngày đóng gói phải theo định dạng dd/mm/yyyy.";
    else if (mfg && pkg <= mfg) next.pkg = "Ngày đóng gói phải sau ngày sản xuất (UC-081).";
    if (!expirationDate.trim()) next.exp = "Hạn sử dụng không được để trống.";
    else if (!exp) next.exp = "Hạn sử dụng phải theo định dạng dd/mm/yyyy.";
    else if ((pkg && exp <= pkg) || (mfg && exp <= mfg))
      next.exp = "Hạn sử dụng phải sau ngày sản xuất và ngày đóng gói (UC-081).";

    if (certIds.length === 0) next.certs = "Lô hàng bắt buộc liên kết ≥1 chứng chỉ an toàn (BR-071/BR-046).";
    else {
      const expiredCert = supplierDocs.find((d) => certIds.includes(d.id) && d.status === "EXPIRED");
      if (expiredCert) next.certs = `MSG-013: chứng chỉ "${expiredCert.name}" đã hết hạn — không thể liên kết vào lô (BR-048).`;
    }

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;
    create.mutate(
      {
        id: `BATCH-GF-${String(Date.now()).slice(-4)}`,
        productId,
        // Cột "Mặt hàng" trong danh sách đọc `productName` — bản ghi seed được
        // backfill 1 lần lúc module load (mock-data.ts), nhưng lô tạo mới qua
        // dialog này không đi qua backfill đó nên phải tự set, nếu không cột
        // sẽ trống với mọi lô vừa nộp.
        productName: productLabel(productId),
        lotNumber,
        manufacturingDate,
        packagingDate,
        expirationDate,
        quantity: `${quantityValue.trim().replace(",", ".")} ${quantityUnit}`,
        certificates: certIds,
        status: "ACTIVE",
      },
      {
        onSuccess: () => {
          toast.success("Đã đăng ký lô nguyên liệu kèm chứng chỉ (BR-071)");
          onOpenChange(false);
          reset();
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Nộp thông tin lô nguyên liệu</DialogTitle>
          <DialogDescription>
            UC-081 — mỗi lô bắt buộc kèm: số lô, ngày sản xuất, ngày đóng gói, hạn sử
            dụng, số lượng và chứng chỉ liên kết (BR-071).
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="b-name">Mặt hàng *</Label>
              <Select
                id="b-name"
                value={productId}
                onChange={(e) => { setProductId(e.target.value); clearError("name"); }}
                aria-invalid={!!errors.name}
                className={errors.name ? "border-destructive focus-visible:ring-destructive" : undefined}
              >
                <option value="">— Chọn mặt hàng đang cung cấp —</option>
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {productLabel(p.id)} · {p.unitPrice.toLocaleString("vi-VN")}₫/{p.unit}
                  </option>
                ))}
              </Select>
              <FieldError message={errors.name} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="b-lot">Số lô *</Label>
              <Input
                id="b-lot"
                value={lotNumber}
                onChange={(e) => { setLotNumber(e.target.value); clearError("lot"); }}
                aria-invalid={!!errors.lot}
                className={errors.lot ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="VD: GF-RC-260703"
              />
              <FieldError message={errors.lot} />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="b-mfg">Ngày sản xuất *</Label>
              <DateInput id="b-mfg" value={manufacturingDate} onChange={(e) => { setManufacturingDate(e.target.value); clearError("mfg"); }} />
              <FieldError message={errors.mfg} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="b-pkg">Ngày đóng gói *</Label>
              <DateInput id="b-pkg" value={packagingDate} onChange={(e) => { setPackagingDate(e.target.value); clearError("pkg"); }} />
              <p className="text-xs text-muted-foreground">Phải sau ngày sản xuất</p>
              <FieldError message={errors.pkg} />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="b-exp">Hạn sử dụng *</Label>
              <DateInput id="b-exp" value={expirationDate} onChange={(e) => { setExpirationDate(e.target.value); clearError("exp"); }} />
              <p className="text-xs text-muted-foreground">Phải sau ngày sản xuất & ngày đóng gói</p>
              <FieldError message={errors.exp} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="b-qty">Số lượng *</Label>
              <div className="flex gap-2">
                <Input
                  id="b-qty"
                  inputMode="decimal"
                  value={quantityValue}
                  onChange={(e) => { setQuantityValue(e.target.value); clearError("qty"); }}
                  aria-invalid={!!errors.qty}
                  className={`flex-1 ${errors.qty ? "border-destructive focus-visible:ring-destructive" : ""}`}
                  placeholder="VD: 30"
                />
                <Select
                  aria-label="Đơn vị tính"
                  value={quantityUnit}
                  onChange={(e) => setQuantityUnit(e.target.value)}
                  className="w-24"
                >
                  {QUANTITY_UNITS.map((u) => (
                    <option key={u} value={u}>{u}</option>
                  ))}
                </Select>
              </div>
              <FieldError message={errors.qty} />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label>Chứng chỉ an toàn liên kết * (từ Hồ sơ tuân thủ — BR-046)</Label>
            <div className={`space-y-1 rounded-lg border p-3 ${errors.certs ? "border-destructive" : "border-input"}`}>
              {supplierDocs.filter((d) => d.documentType !== "BUSINESS_LICENSE").map((d) => (
                <label key={d.id} className="flex cursor-pointer items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={certIds.includes(d.id)}
                    onChange={() => toggleCert(d.id)}
                  />
                  <span className="flex-1">{d.name}</span>
                  <StatusBadge status={d.status} />
                </label>
              ))}
            </div>
            <FieldError message={errors.certs} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo lô hàng</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function BatchesPage() {
  const [open, setOpen] = useState(false);
  const [cancelFor, setCancelFor] = useState<SupplierBatch | null>(null);
  return (
    <>
      <PageHeader
        title="Lô nguyên liệu"
        description="Lô xuất đi đăng ký trước khi giao — truy xuất nguồn gốc theo số lô (BR-071/BR-076); vòng đời: Hoạt động → Đang giao → Đã giao / trả về Hoạt động; chứng chỉ hết hạn bị chặn liên kết (BR-048/MSG-013)"
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus /> Tạo lô hàng mới
          </Button>
        }
      />
      <DataTable<SupplierBatch>
        resource="supplier-batches"
        columns={makeColumns(setCancelFor)}
        searchPlaceholder="Tìm theo nguyên liệu, số lô…"
        rowHref={(b) => `/supplier/batches/${b.id}`}
      />
      <SubmitBatchDialog open={open} onOpenChange={setOpen} />
      <CancelBatchDialog batch={cancelFor} onOpenChange={(v) => !v && setCancelFor(null)} />
    </>
  );
}
