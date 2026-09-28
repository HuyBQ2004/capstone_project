"use client";

import { useState } from "react";
import { Upload } from "lucide-react";
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
import { Alert } from "@/components/ui/alert";
import { DateInput, Input, Label, Select } from "@/components/ui/input";
import { useCreate, useUpdate } from "@/lib/api/hooks";
import type { SupplierDocument } from "@/lib/mock-data";

/**
 * Screen Spec 04/05/06/25/26 — Compliance Documents / Food Safety Certificates /
 * Product Origin Documents / Upload Supplier Certificates / Update Documents.
 * UC-167 (Manage), UC-101/102 (Upload), UC-091 (Update).
 * BR-046: duy trì tài liệu hợp lệ · BR-047: cảnh báo Expiring Soon <30 ngày (MSG-053) ·
 * BR-048 + MSG-013: chứng nhận ATTP hết hạn bị chặn · MSG-008/009: file >10MB / sai định dạng.
 */

const DOC_TYPE_LABEL: Record<SupplierDocument["documentType"], string> = {
  FOOD_SAFETY_CERT: "Chứng nhận ATTP",
  PRODUCT_ORIGIN: "Tài liệu xuất xứ",
  BUSINESS_LICENSE: "Giấy phép kinh doanh",
  SELF_DECLARATION: "Số tự công bố sản phẩm",
};

const ALLOWED_EXT = /\.(pdf|jpe?g|png)$/i;

/** "dd/mm/yyyy" → Date; trả null nếu sai định dạng. */
function parseVnDate(value: string): Date | null {
  const m = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!m) return null;
  return new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1]));
}

const DAY_MS = 24 * 60 * 60 * 1000;

function UploadCertificateDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Partial<SupplierDocument>>("supplier-documents");
  const [docType, setDocType] = useState<SupplierDocument["documentType"]>("FOOD_SAFETY_CERT");
  const [name, setName] = useState("");
  const [docNumber, setDocNumber] = useState("");
  const [issuedBy, setIssuedBy] = useState("");
  const [issuedDate, setIssuedDate] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [expiry, setExpiry] = useState("");
  // Review 07/2026: validate theo từng trường — lỗi hiện ngay dưới trường tương ứng.
  const [errors, setErrors] = useState<Record<string, string>>({});

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function submit() {
    const next: Record<string, string> = {};
    if (!name.trim()) next.name = "Tên tài liệu không được để trống.";
    if (!docNumber.trim()) next.number = "Số hiệu không được để trống.";
    if (!issuedBy.trim()) next.issuer = "Cơ quan cấp không được để trống.";

    const issued = parseVnDate(issuedDate);
    if (!issuedDate.trim()) next.issued = "Ngày cấp không được để trống.";
    else if (!issued) next.issued = "Ngày cấp phải theo định dạng dd/mm/yyyy.";
    else if (issued.getTime() > Date.now()) next.issued = "Ngày cấp không được lớn hơn ngày hiện tại.";

    if (!file) next.file = "Vui lòng tải lên file tài liệu (BR-046).";
    else if (!ALLOWED_EXT.test(file.name)) next.file = "MSG-009: chỉ chấp nhận PDF/JPG/PNG.";
    else if (file.size > 10 * 1024 * 1024) next.file = "MSG-008: file vượt quá 10MB.";

    // Ngày hết hạn: bắt buộc với Chứng nhận ATTP (UC-101), tùy chọn với tài liệu khác.
    let expDate: Date | null = null;
    if (docType === "FOOD_SAFETY_CERT" && !expiry.trim()) {
      next.expiry = "Chứng nhận ATTP bắt buộc có ngày hết hạn — BR-047.";
    } else if (expiry.trim()) {
      expDate = parseVnDate(expiry);
      if (!expDate) next.expiry = "Ngày hết hạn phải theo định dạng dd/mm/yyyy.";
      else if (docType === "FOOD_SAFETY_CERT" && expDate.getTime() < Date.now())
        next.expiry = "MSG-013: chứng nhận ATTP đã hết hạn — không thể tải lên (BR-048).";
    }

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;
    const expiringSoon = expDate !== null && expDate.getTime() - Date.now() < 30 * DAY_MS;
    create.mutate(
      {
        id: `DOC-GF-${String(Date.now()).slice(-4)}`,
        name,
        documentType: docType,
        number: docNumber,
        issuedBy,
        issuedDate,
        expiryDate: expiry.trim() || "Không thời hạn",
        status: expiringSoon ? "EXPIRING" : "VALID",
      },
      {
        onSuccess: () => {
          if (expiringSoon) {
            toast.warning("MSG-053: tài liệu sẽ hết hạn trong vòng 30 ngày (BR-047)");
          }
          toast.success("MSG-029: đã lưu tài liệu tuân thủ (UC-101/UC-102)");
          onOpenChange(false);
          setName(""); setDocNumber(""); setIssuedBy(""); setIssuedDate(""); setFile(null); setExpiry("");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tải tài liệu tuân thủ</DialogTitle>
          <DialogDescription>
            UC-101/UC-102 — phân loại quyết định luồng validation: chứng nhận ATTP bắt
            buộc ngày hết hạn và bị chặn nếu đã hết hạn (BR-048/MSG-013).
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="u-type">Phân loại tài liệu *</Label>
            <Select id="u-type" value={docType} onChange={(e) => setDocType(e.target.value as SupplierDocument["documentType"])}>
              <option value="FOOD_SAFETY_CERT">Food Safety Certificate — chứng nhận ATTP (UC-101)</option>
              <option value="PRODUCT_ORIGIN">Product Origin Document — VietGAP/GlobalGAP/Organic (UC-102)</option>
              <option value="BUSINESS_LICENSE">Business License — giấy phép kinh doanh (GPKD)</option>
              <option value="SELF_DECLARATION">Self Declaration — số tự công bố sản phẩm</option>
            </Select>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="u-name">Tên tài liệu *</Label>
              <Input
                id="u-name"
                value={name}
                onChange={(e) => { setName(e.target.value); clearError("name"); }}
                aria-invalid={!!errors.name}
                className={errors.name ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="VD: Chứng nhận VietGAP — Rau củ"
              />
              {errors.name && <p className="text-xs font-medium text-destructive">{errors.name}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="u-number">Số hiệu *</Label>
              <Input
                id="u-number"
                value={docNumber}
                onChange={(e) => { setDocNumber(e.target.value); clearError("number"); }}
                aria-invalid={!!errors.number}
                className={errors.number ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="VD: ATTP-2026/00123"
              />
              {errors.number && <p className="text-xs font-medium text-destructive">{errors.number}</p>}
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="u-issuer">Cơ quan cấp *</Label>
              <Input
                id="u-issuer"
                value={issuedBy}
                onChange={(e) => { setIssuedBy(e.target.value); clearError("issuer"); }}
                aria-invalid={!!errors.issuer}
                className={errors.issuer ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="VD: Chi cục ATVSTP Hà Nội"
              />
              {errors.issuer && <p className="text-xs font-medium text-destructive">{errors.issuer}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="u-issued">Ngày cấp *</Label>
              <DateInput id="u-issued" value={issuedDate} onChange={(e) => { setIssuedDate(e.target.value); clearError("issued"); }} />
              {errors.issued && <p className="text-xs font-medium text-destructive">{errors.issued}</p>}
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="u-file">File * (PDF/JPG/PNG ≤10MB)</Label>
              <Input
                id="u-file"
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                onChange={(e) => { setFile(e.target.files?.[0] ?? null); clearError("file"); }}
                aria-invalid={!!errors.file}
                className={errors.file ? "border-destructive focus-visible:ring-destructive" : undefined}
              />
              {file && <p className="text-xs text-muted-foreground">Đã chọn: {file.name}</p>}
              {errors.file && <p className="text-xs font-medium text-destructive">{errors.file}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="u-exp">
                Ngày hết hạn {docType === "FOOD_SAFETY_CERT" ? "*" : "(tùy chọn)"}
              </Label>
              <DateInput id="u-exp" value={expiry} onChange={(e) => { setExpiry(e.target.value); clearError("expiry"); }} />
              {errors.expiry && <p className="text-xs font-medium text-destructive">{errors.expiry}</p>}
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Lưu tài liệu</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function UpdateDocumentDialog({
  doc,
  onClose,
}: {
  doc: SupplierDocument | null;
  onClose: () => void;
}) {
  const update = useUpdate<{ id: string } & Partial<SupplierDocument>>("supplier-documents");
  const [file, setFile] = useState<File | null>(null);
  const [expiry, setExpiry] = useState("");
  const [error, setError] = useState("");

  if (!doc) return null;

  function submit() {
    if (!file || !expiry.trim()) {
      setError("Bắt buộc: tải lên file thay thế và ngày hết hạn mới (UC-091).");
      return;
    }
    if (!ALLOWED_EXT.test(file.name)) {
      setError("MSG-009: định dạng không hỗ trợ — chỉ chấp nhận PDF/JPG/PNG.");
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      setError("MSG-008: file vượt quá 10MB.");
      return;
    }
    const expDate = parseVnDate(expiry);
    if (!expDate) {
      setError("Ngày hết hạn mới phải theo định dạng dd/mm/yyyy (BR-047).");
      return;
    }
    setError("");
    const expiringSoon = expDate.getTime() - Date.now() < 30 * DAY_MS;
    update.mutate(
      { id: doc!.id, expiryDate: expiry, status: expiringSoon ? "EXPIRING" : "VALID" },
      {
        onSuccess: () => {
          if (expiringSoon) {
            toast.warning("MSG-053: ngày hết hạn mới vẫn còn dưới 30 ngày (BR-047)");
          }
          toast.success("MSG-029: cập nhật tài liệu thành công (UC-091) — nếu thay bản hết hạn bằng bản hợp lệ, trạng thái tự chuyển Valid (BR-048)");
          onClose();
          setFile(null); setExpiry("");
        },
      }
    );
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cập nhật tài liệu</DialogTitle>
          <DialogDescription>
            UC-091 — thay file và ngày hết hạn cho: <b>{doc.name}</b> (hiện hết hạn {doc.expiryDate}).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể cập nhật">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="e-file">File thay thế * (PDF/JPG/PNG ≤10MB)</Label>
            <Input
              id="e-file"
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            />
            {file && <p className="text-xs text-muted-foreground">Đã chọn: {file.name}</p>}
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="e-exp">Ngày hết hạn mới *</Label>
            <DateInput id="e-exp" value={expiry} onChange={(e) => setExpiry(e.target.value)} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={update.isPending} onClick={submit}>Lưu cập nhật</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function CertificatesPage() {
  const [uploadOpen, setUploadOpen] = useState(false);
  const [editing, setEditing] = useState<SupplierDocument | null>(null);

  const columns: ColumnDef<SupplierDocument, unknown>[] = [
    { accessorKey: "name", header: "Tài liệu", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
    {
      accessorKey: "documentType",
      header: "Phân loại",
      cell: ({ row }) => <Badge variant="info">{DOC_TYPE_LABEL[row.original.documentType]}</Badge>,
    },
    { accessorKey: "number", header: "Số hiệu" },
    { accessorKey: "issuedBy", header: "Cơ quan cấp" },
    { accessorKey: "issuedDate", header: "Ngày cấp" },
    { accessorKey: "expiryDate", header: "Hết hạn" },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <Button
          variant="outline"
          size="sm"
          onClick={(e) => {
            e.stopPropagation();
            setEditing(row.original);
          }}
        >
          Cập nhật (UC-091)
        </Button>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Chứng nhận & Giấy phép"
        description="Hồ sơ tuân thủ (UC-167) — cảnh báo Expiring Soon khi còn dưới 30 ngày (BR-047/MSG-053); tài liệu hết hạn dẫn tới đình chỉ nhận đơn (BR-048)"
        actions={
          <Button onClick={() => setUploadOpen(true)}>
            <Upload /> Tải tài liệu mới
          </Button>
        }
      />

      <Alert variant="warning" className="mb-4" title="MSG-053 — Sắp hết hạn">
        Chứng nhận VietGAP — Rau củ hết hạn <b>28/07/2026</b> (còn dưới 30 ngày — BR-047).
        Cập nhật bản gia hạn qua nút “Cập nhật (UC-091)” để tránh bị chặn liên kết lô (BR-048).
      </Alert>

      <DataTable<SupplierDocument>
        resource="supplier-documents"
        columns={columns}
        searchPlaceholder="Tìm theo tên tài liệu, số hiệu…"
      />

      <UploadCertificateDialog open={uploadOpen} onOpenChange={setUploadOpen} />
      <UpdateDocumentDialog doc={editing} onClose={() => setEditing(null)} />
    </>
  );
}
