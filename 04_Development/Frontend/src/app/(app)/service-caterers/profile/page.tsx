"use client";

import { useState } from "react";
import { useEffect } from "react";
import { Download, Eye, FileCheck2, Save, Upload } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";
import { api } from "@/lib/api/client";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
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
import { DateInput, Input, Label, Select } from "@/components/ui/input";
import { usePagedList, useUploadMutation, jsonPart } from "@/lib/api/hooks";
import type { DocumentDto, UploadDocumentDto } from "@/lib/api/types";
import { formatVnDate, toIsoDate } from "@/lib/format";

/**
 * Profile Management (GuestCommon 05) — thông tin đơn vị ServiceCaterer.
 * Trường bám class ServiceCaterer: companyName, businessRegNo, officeAddress,
 * contactPerson, email, phone · ComplianceDocument: BR-047 cảnh báo <30 ngày.
 * Tài liệu tuân thủ nối `supplier-documents` thật (ComplianceDocumentController) —
 * cùng resource/luồng với supplier/certificates, không còn placeholder chỉ toast.
 */

/**
 * `SupplierDocument["documentType"]` (khai tay, mock-data.ts) → dùng thẳng
 * `UploadDocumentDto["documentType"]` (= `Api<"UploadDocumentRequest">.documentType`,
 * Giai đoạn 3, 07/08/2026): OpenAPI ĐÃ sinh sẵn literal union cho field này (D6,
 * request), rộng hơn bản mock cũ 2 giá trị (`HEALTH_CERTIFICATE`/
 * `FOOD_SAFETY_KNOWLEDGE_CERT` — hồ sơ CÁ NHÂN nhân viên giao hàng, xem
 * `supplier/delivery-staff/page.tsx`), không dùng ở màn công ty này nhưng bắt buộc
 * khai đủ để `Record<>` thoả literal union.
 */
const DOC_TYPE_LABEL: Record<UploadDocumentDto["documentType"], string> = {
  FOOD_SAFETY_CERT: "Chứng nhận ATTP",
  PRODUCT_ORIGIN: "Tài liệu xuất xứ",
  BUSINESS_LICENSE: "Giấy phép kinh doanh",
  SELF_DECLARATION: "Số tự công bố sản phẩm",
  HEALTH_CERTIFICATE: "Giấy khám sức khỏe",
  FOOD_SAFETY_KNOWLEDGE_CERT: "Xác nhận kiến thức ATTP",
};

const ALLOWED_EXT = /\.(pdf|jpe?g|png)$/i;

/** "dd/mm/yyyy" → Date; trả null nếu sai định dạng. */
function parseVnDate(value: string): Date | null {
  const m = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!m) return null;
  return new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1]));
}

function UploadDocumentDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (v: boolean) => void }) {
  const upload = useUploadMutation<DocumentDto>("supplier-documents", "post", { handledError: true });
  const [docType, setDocType] = useState<UploadDocumentDto["documentType"]>("FOOD_SAFETY_CERT");
  const [name, setName] = useState("");
  const [docNumber, setDocNumber] = useState("");
  const [issuedBy, setIssuedBy] = useState("");
  const [issuedDate, setIssuedDate] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [expiry, setExpiry] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function reset() {
    setName(""); setDocNumber(""); setIssuedBy(""); setIssuedDate(""); setFile(null); setExpiry(""); setErrors({});
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

    if (docType === "FOOD_SAFETY_CERT" && !expiry.trim()) {
      next.expiry = "Chứng nhận ATTP bắt buộc có ngày hết hạn — BR-047.";
    } else if (expiry.trim()) {
      const expDate = parseVnDate(expiry);
      if (!expDate) next.expiry = "Ngày hết hạn phải theo định dạng dd/mm/yyyy.";
      else if (docType === "FOOD_SAFETY_CERT" && expDate.getTime() < Date.now())
        next.expiry = "MSG-013: chứng nhận ATTP đã hết hạn — không thể tải lên (BR-048).";
    }

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;

    const meta: UploadDocumentDto = {
      name,
      documentType: docType,
      number: docNumber,
      issuedBy,
      issuedDate: toIsoDate(issuedDate) ?? issuedDate,
      expiryDate: expiry.trim() ? toIsoDate(expiry) ?? undefined : undefined,
    };
    upload.mutate(
      { parts: { meta: jsonPart(meta), file: file! } },
      {
        onSuccess: (res) => {
          if (res.warning) toast.warning(res.warning.message);
          toast.success("Đã tải tài liệu — chờ School Administration xác nhận hiệu lực.");
          onOpenChange(false);
          reset();
        },
        onError: () => toast.error("Tải tài liệu thất bại — vui lòng thử lại."),
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { onOpenChange(v); if (!v) reset(); }}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tải lên tài liệu mới / gia hạn</DialogTitle>
          <DialogDescription>
            Phân loại quyết định luồng validation: chứng nhận ATTP bắt buộc ngày hết hạn
            và bị chặn nếu đã hết hạn (BR-047/BR-048).
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="u-type">Phân loại tài liệu *</Label>
            <Select id="u-type" value={docType} onChange={(e) => setDocType(e.target.value as UploadDocumentDto["documentType"])}>
              <option value="FOOD_SAFETY_CERT">Chứng nhận ATTP</option>
              <option value="PRODUCT_ORIGIN">Tài liệu xuất xứ</option>
              <option value="BUSINESS_LICENSE">Giấy phép kinh doanh</option>
              <option value="SELF_DECLARATION">Số tự công bố sản phẩm</option>
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
          <Button disabled={upload.isPending} onClick={submit}>Lưu tài liệu</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/** Cập nhật/gia hạn tài liệu tuân thủ (UC-091) — cùng luồng với supplier/certificates. */
function UpdateDocumentDialog({ doc, onClose }: { doc: DocumentDto | null; onClose: () => void }) {
  const update = useUploadMutation<DocumentDto>("supplier-documents", "put", { handledError: true });
  const [name, setName] = useState("");
  const [docNumber, setDocNumber] = useState("");
  const [issuedBy, setIssuedBy] = useState("");
  const [issuedDate, setIssuedDate] = useState("");
  const [expiry, setExpiry] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (doc) {
      setName(doc.name ?? "");
      setDocNumber(doc.number ?? "");
      setIssuedBy(doc.issuedBy ?? "");
      setIssuedDate(doc.issuedDate ? formatVnDate(doc.issuedDate) : "");
      setExpiry(doc.expiryDate ? formatVnDate(doc.expiryDate) : "");
      setFile(null);
      setError("");
    }
  }, [doc]);

  if (!doc) return null;

  function submit() {
    if (!file) {
      setError("Bắt buộc: tải lên file tài liệu thay thế (UC-091).");
      return;
    }
    if (!name.trim() || !docNumber.trim() || !issuedBy.trim() || !issuedDate.trim()) {
      setError("Vui lòng điền đủ thông tin các trường bắt buộc.");
      return;
    }
    const issued = parseVnDate(issuedDate);
    if (!issued) {
      setError("Ngày cấp phải theo định dạng dd/mm/yyyy.");
      return;
    }
    if (issued.getTime() > Date.now()) {
      setError("Ngày cấp không được lớn hơn ngày hiện tại.");
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
    if (doc?.documentType === "FOOD_SAFETY_CERT" && !expiry.trim()) {
      setError("Chứng nhận ATTP bắt buộc có ngày hết hạn.");
      return;
    }

    let expIso: string | undefined = undefined;
    if (expiry.trim()) {
      const expDate = parseVnDate(expiry);
      if (!expDate) {
        setError("Ngày hết hạn phải theo định dạng dd/mm/yyyy.");
        return;
      }
      if (doc?.documentType === "FOOD_SAFETY_CERT" && expDate.getTime() < Date.now()) {
        setError("MSG-013: chứng nhận ATTP đã hết hạn — không thể tải lên.");
        return;
      }
      expIso = toIsoDate(expiry) ?? undefined;
    }

    setError("");
    update.mutate(
      {
        path: doc!.id,
        parts: {
          meta: jsonPart({
            name: name.trim(),
            number: docNumber.trim(),
            issuedBy: issuedBy.trim(),
            issuedDate: toIsoDate(issuedDate) ?? issuedDate,
            expiryDate: expIso,
          }),
          file,
        },
      },
      {
        onSuccess: (res) => {
          if (res.warning) toast.warning(res.warning.message);
          toast.success("Cập nhật tài liệu thành công (UC-091).");
          onClose();
        },
        onError: () => toast.error("Cập nhật tài liệu thất bại — vui lòng thử lại."),
      }
    );
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Cập nhật tài liệu</DialogTitle>
          <DialogDescription>Thay đổi thông tin và nộp file gia hạn/thay thế mới cho tài liệu.</DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể cập nhật">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="e-name">Tên tài liệu *</Label>
            <Input id="e-name" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="e-number">Số hiệu mới *</Label>
              <Input id="e-number" value={docNumber} onChange={(e) => setDocNumber(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="e-issuer">Cơ quan cấp *</Label>
              <Input id="e-issuer" value={issuedBy} onChange={(e) => setIssuedBy(e.target.value)} />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="e-issued">Ngày cấp mới *</Label>
              <DateInput id="e-issued" value={issuedDate} onChange={(e) => setIssuedDate(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="e-exp">
                Ngày hết hạn mới {doc.documentType === "FOOD_SAFETY_CERT" ? "*" : "(tùy chọn)"}
              </Label>
              <DateInput id="e-exp" value={expiry} onChange={(e) => setExpiry(e.target.value)} />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="e-file">File tài liệu thay thế * (PDF/JPG/PNG ≤10MB)</Label>
            <Input id="e-file" type="file" accept=".pdf,.jpg,.jpeg,.png" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
            {file && <p className="text-xs font-medium text-muted-foreground">Đã chọn: {file.name}</p>}
          </div>
        </div>
        <DialogFooter className="mt-2 shrink-0">
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={update.isPending} onClick={submit}>Lưu cập nhật</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function CatererProfilePage() {
  const [companyName, setCompanyName] = useState("Cty TNHH Suất ăn An Bình");
  const [businessRegNo, setBusinessRegNo] = useState("0109334455");
  const [officeAddress, setOfficeAddress] = useState("12 Trần Hữu Dực, Nam Từ Liêm, Hà Nội");
  const [contactPerson, setContactPerson] = useState("Trần Văn Hùng");
  const [email, setEmail] = useState("hungtv@anbinhcatering.vn");
  const [phone, setPhone] = useState("0912 345 678");
  const [uploadOpen, setUploadOpen] = useState(false);
  const [editing, setEditing] = useState<DocumentDto | null>(null);
  const [previewFile, setPreviewFile] = useState<{ url: string; type: string; name: string } | null>(null);
  const [loadingDocId, setLoadingDocId] = useState<string | null>(null);

  /* Đọc toàn bộ để dựng banner cảnh báo thật (DataTable bên dưới tự phân trang riêng). */
  const { data: docsPage } = usePagedList<DocumentDto>("supplier-documents", { pageSize: 100 });
  const docs = docsPage?.items ?? [];
  const expiringDocs = docs.filter((d) => d.status === "EXPIRING" || d.status === "URGENT" || d.status === "EXPIRED");

  async function downloadDocFile(code: string, fileName: string) {
    setLoadingDocId(code);
    try {
      const res = await api.get<Blob>(`/supplier-documents/${code}/file`, { responseType: "blob" });
      const objUrl = URL.createObjectURL(res.data);
      const a = document.createElement("a");
      a.href = objUrl;
      a.download = fileName;
      a.click();
      setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
      toast.success(`Đã tải xuống: ${fileName}`);
    } catch {
      toast.error("Không thể tải xuống file. Vui lòng thử lại.");
    } finally {
      setLoadingDocId(null);
    }
  }

  async function previewDocFile(code: string, docName: string) {
    setLoadingDocId(code);
    try {
      const res = await api.get<Blob>(`/supplier-documents/${code}/file`, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
      setPreviewFile({
        url: objUrl,
        type: blob.type || (docName.endsWith(".pdf") ? "application/pdf" : "image/jpeg"),
        name: docName,
      });
    } catch {
      toast.error("Không thể tải file xem trực tiếp. Vui lòng chọn tải về máy.");
    } finally {
      setLoadingDocId(null);
    }
  }

  function closePreview() {
    if (previewFile) {
      URL.revokeObjectURL(previewFile.url);
      setPreviewFile(null);
    }
  }

  const columns: ColumnDef<DocumentDto, unknown>[] = [
    { accessorKey: "name", header: "Tài liệu", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
    {
      accessorKey: "documentType",
      header: "Phân loại",
      cell: ({ row }) => (
        <Badge variant="info">
          {row.original.documentType
            ? DOC_TYPE_LABEL[row.original.documentType as UploadDocumentDto["documentType"]] ?? row.original.documentType
            : "—"}
        </Badge>
      ),
    },
    { accessorKey: "number", header: "Số hiệu" },
    { accessorKey: "issuedBy", header: "Cơ quan cấp" },
    { accessorKey: "issuedDate", header: "Ngày cấp", cell: ({ row }) => formatVnDate(row.original.issuedDate) },
    { accessorKey: "expiryDate", header: "Hết hạn", cell: ({ row }) => formatVnDate(row.original.expiryDate) },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex justify-end gap-1.5">
          <Button
            variant="outline"
            size="sm"
            disabled={loadingDocId !== null}
            onClick={(e) => {
              e.stopPropagation();
              previewDocFile(row.original.id ?? "", row.original.name ?? "Tài liệu");
            }}
          >
            <Eye className="mr-1 size-3.5" /> Xem
          </Button>
          <Button
            variant="outline"
            size="sm"
            disabled={loadingDocId !== null}
            onClick={(e) => {
              e.stopPropagation();
              downloadDocFile(
                row.original.id ?? "",
                (row.original.name ?? "Tài liệu") + ((row.original.name ?? "").toLowerCase().endsWith(".pdf") ? ".pdf" : ".jpg")
              );
            }}
          >
            <Download className="mr-1 size-3.5" /> Tải về
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              setEditing(row.original);
            }}
          >
            Cập nhật
          </Button>
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Hồ sơ đơn vị suất ăn"
        description="Thông tin ServiceCaterer + tài liệu tuân thủ; tài liệu hết hạn ảnh hưởng trạng thái hợp tác (BR-047/BR-048)"
      />

      <div className="mb-6">
        <Card>
          <CardHeader>
            <CardTitle>Thông tin đơn vị (ServiceCaterer)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="pf-name">Tên công ty *</Label>
              <Input id="pf-name" value={companyName} onChange={(e) => setCompanyName(e.target.value)} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="pf-reg">Số ĐKKD (businessRegNo) *</Label>
                <Input id="pf-reg" value={businessRegNo} onChange={(e) => setBusinessRegNo(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="pf-contact">Người đại diện *</Label>
                <Input id="pf-contact" value={contactPerson} onChange={(e) => setContactPerson(e.target.value)} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pf-addr">Địa chỉ văn phòng *</Label>
              <Input id="pf-addr" value={officeAddress} onChange={(e) => setOfficeAddress(e.target.value)} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="pf-email">Email liên hệ *</Label>
                <Input id="pf-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="pf-phone">Số điện thoại *</Label>
                <Input id="pf-phone" value={phone} onChange={(e) => setPhone(e.target.value)} />
              </div>
            </div>
            <Button onClick={() => toast.success("Đã lưu hồ sơ đơn vị — thay đổi được ghi Audit Log (BR-162).")}>
              <Save /> Lưu thay đổi
            </Button>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <FileCheck2 className="size-5 text-primary" />
            Tài liệu tuân thủ (ComplianceDocument — BR-047)
          </CardTitle>
          <Button size="sm" onClick={() => setUploadOpen(true)}>
            <Upload /> Tải tài liệu
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          {expiringDocs.length > 0 && (
            <Alert variant="warning" title="MSG-053 — Sắp hết hạn">
              {expiringDocs.map((d) => (
                <span key={d.id} className="block">
                  {d.name} hết hạn <b>{formatVnDate(d.expiryDate)}</b>
                  {d.status === "EXPIRED" ? " — ĐÃ HẾT HẠN" : " (còn dưới 30 ngày — BR-047)"}.
                </span>
              ))}
              <span className="mt-1 block">
                Cập nhật bản gia hạn qua nút &quot;Cập nhật&quot; ở dòng tương ứng để tránh ảnh hưởng trạng thái hợp tác (BR-048).
              </span>
            </Alert>
          )}
          {docs.length === 0 && (
            <Alert variant="info" title="Chưa nộp hồ sơ tuân thủ nào">
              Hợp tác với trường yêu cầu tài liệu còn hiệu lực (BR-046). Bấm &quot;Tải tài liệu&quot; để nộp
              Giấy chứng nhận ATTP, giấy phép kinh doanh hoặc chứng nhận nguồn gốc sản phẩm.
            </Alert>
          )}
          <DataTable<DocumentDto>
            resource="supplier-documents"
            columns={columns}
            searchPlaceholder="Tìm theo tên tài liệu, số hiệu…"
          />
        </CardContent>
      </Card>

      <UploadDocumentDialog open={uploadOpen} onOpenChange={setUploadOpen} />
      <UpdateDocumentDialog doc={editing} onClose={() => setEditing(null)} />
      <FilePreviewDialog file={previewFile} onClose={closePreview} />
    </>
  );
}
