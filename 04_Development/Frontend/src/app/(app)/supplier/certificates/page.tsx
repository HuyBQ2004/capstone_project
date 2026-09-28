"use client";

import { useState, useEffect } from "react";
import { Upload, Eye, Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";
import { api } from "@/lib/api/client";
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
import { usePagedList, useUploadMutation, jsonPart } from "@/lib/api/hooks";
import type { DocumentDto, UploadDocumentDto } from "@/lib/api/types";
import { formatVnDate, toIsoDate } from "@/lib/format";

/**
 * Screen Spec 04/05/06/25/26 — Compliance Documents / Food Safety Certificates /
 * Product Origin Documents / Upload Supplier Certificates / Update Documents.
 * UC-167 (Manage), UC-101/102 (Upload), UC-091 (Update).
 * BR-046: duy trì tài liệu hợp lệ · BR-047: cảnh báo Expiring Soon <30 ngày (MSG-053) ·
 * BR-048 + MSG-013: chứng nhận ATTP hết hạn bị chặn · MSG-008/009: file >10MB / sai định dạng.
 */

/**
 * `SupplierDocument["documentType"]` (khai tay, mock-data.ts, XOÁ 07/08/2026) → dùng
 * thẳng `UploadDocumentDto["documentType"]` (= `Api<"UploadDocumentRequest">.documentType`) —
 * OpenAPI ĐÃ sinh sẵn literal union (D6), rộng hơn bản mock cũ 2 giá trị
 * (`HEALTH_CERTIFICATE`/`FOOD_SAFETY_KNOWLEDGE_CERT` — hồ sơ CÁ NHÂN nhân viên giao
 * hàng, không dùng ở màn NCC công ty này nhưng bắt buộc khai đủ cho `Record<>`).
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

function UploadCertificateDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const upload = useUploadMutation<DocumentDto>("supplier-documents", "post", { handledError: true });
  const [docType, setDocType] = useState<UploadDocumentDto["documentType"]>("FOOD_SAFETY_CERT");
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
    // status/warning (EXPIRING/URGENT/EXPIRED) nay do server tính (ADR-M04-4), FE chỉ
    // còn giữ validate chặn nộp chứng nhận ATTP đã hết hạn/thiếu hạn (BR-047/BR-048).
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
          toast.success("MSG-029: đã lưu tài liệu tuân thủ (UC-101/UC-102)");
          onOpenChange(false);
          setName(""); setDocNumber(""); setIssuedBy(""); setIssuedDate(""); setFile(null); setExpiry("");
        },
        onError: () => toast.error("Tải tài liệu thất bại — vui lòng thử lại."),
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
            <Select id="u-type" value={docType} onChange={(e) => setDocType(e.target.value as UploadDocumentDto["documentType"])}>
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
          <Button disabled={upload.isPending} onClick={submit}>Lưu tài liệu</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function UpdateDocumentDialog({
  doc,
  onClose,
}: {
  doc: DocumentDto | null;
  onClose: () => void;
}) {
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
          toast.success("MSG-029: cập nhật tài liệu thành công (UC-091)");
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
          <DialogDescription>
            Thay đổi thông tin và nộp file gia hạn/thay thế mới cho tài liệu.
          </DialogDescription>
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
            <Input
              id="e-file"
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            />
            {file && <p className="text-xs text-muted-foreground font-medium">Đã chọn: {file.name}</p>}
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


export default function CertificatesPage() {
  const [uploadOpen, setUploadOpen] = useState(false);
  const [editing, setEditing] = useState<DocumentDto | null>(null);

  const [previewFile, setPreviewFile] = useState<{ url: string; type: string; name: string } | null>(null);
  const [loadingDocId, setLoadingDocId] = useState<string | null>(null);

  /*
   * Đọc danh sách THẬT để dựng banner cảnh báo. `DataTable` bên dưới tự gọi API riêng của nó
   * (phân trang 8 dòng); ở đây cần TOÀN BỘ để không bỏ sót tài liệu sắp hết hạn ở trang 2.
   */
  const { data: docsPage } = usePagedList<DocumentDto>("supplier-documents", { pageSize: 100 });
  const docs = docsPage?.items ?? [];
  const expiringDocs = docs.filter(
    (d) => d.status === "EXPIRING" || d.status === "URGENT" || d.status === "EXPIRED"
  );

  async function downloadDocFile(code: string, fileName: string) {
    setLoadingDocId(code);
    try {
      const res = await api.get<Blob>(`/supplier-documents/${code}/file`, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
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
        name: docName
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
        <div className="flex gap-1.5 justify-end">
          <Button
            variant="outline"
            size="sm"
            disabled={loadingDocId !== null}
            onClick={(e) => {
              e.stopPropagation();
              previewDocFile(row.original.id ?? "", row.original.name ?? "Tài liệu");
            }}
          >
            <Eye className="size-3.5 mr-1" /> Xem
          </Button>
          <Button
            variant="outline"
            size="sm"
            disabled={loadingDocId !== null}
            onClick={(e) => {
              e.stopPropagation();
              downloadDocFile(row.original.id ?? "", (row.original.name ?? "Tài liệu") + ((row.original.name ?? "").toLowerCase().endsWith(".pdf") ? ".pdf" : ".jpg"));
            }}
          >
            <Download className="size-3.5 mr-1" /> Tải về
          </Button>
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
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Chứng nhận & Giấy phép"
        description="Hồ sơ tuân thủ (UC-167) — cảnh báo Sắp hết hạn khi còn dưới 6 tháng, Khẩn cấp khi còn dưới 30 ngày (BR-047/MSG-053, Điều 37 Luật ATTP 2010); tài liệu hết hạn dẫn tới đình chỉ nhận đơn (BR-048)"
        actions={
          <Button onClick={() => setUploadOpen(true)}>
            <Upload /> Tải tài liệu mới
          </Button>
        }
      />

      {/*
        * 08/08/2026 — banner này TRƯỚC ĐÂY LÀ CHUỖI IN CỨNG: "Chứng nhận VietGAP — Rau củ hết
        * hạn 28/07/2026". Nó hiện ra kể cả khi NCC chưa nộp tài liệu nào (bảng ngay dưới ghi
        * "Không có bản ghi nào"), và bản thân ngày 28/07/2026 đã QUÁ HẠN 11 ngày mà vẫn ghi
        * "còn dưới 30 ngày". Trên màn hồ sơ tuân thủ — thứ quyết định NCC có bị đình chỉ nhận
        * đơn hay không (BR-048) — dữ liệu bịa là không chấp nhận được.
        *
        * Nay tính từ dữ liệu THẬT: `status` do server tính sẵn (ADR-M04-4), FE không tự so ngày.
        */}
      {expiringDocs.length > 0 && (
        <Alert variant="warning" className="mb-4" title="MSG-053 — Sắp hết hạn">
          {expiringDocs.map((d) => (
            <span key={d.id} className="block">
              {d.name} hết hạn <b>{formatVnDate(d.expiryDate)}</b>
              {d.status === "EXPIRED"
                ? " — ĐÃ HẾT HẠN."
                : d.status === "URGENT"
                  ? " (còn dưới 30 ngày — khẩn cấp, BR-047)."
                  : " (còn dưới 6 tháng — BR-047, Điều 37 Luật ATTP 2010)."}
            </span>
          ))}
          <span className="mt-1 block">
            Cập nhật bản gia hạn qua nút “Cập nhật (UC-091)” để tránh bị chặn liên kết lô (BR-048).
          </span>
        </Alert>
      )}
      {docs.length === 0 && (
        <Alert variant="info" className="mb-4" title="Chưa nộp hồ sơ tuân thủ nào">
          Hợp tác với trường yêu cầu tài liệu còn hiệu lực (BR-046). Bấm “Tải tài liệu mới” để nộp
          Giấy chứng nhận ATTP, giấy phép kinh doanh hoặc chứng nhận nguồn gốc sản phẩm.
        </Alert>
      )}

      <DataTable<DocumentDto>
        resource="supplier-documents"
        columns={columns}
        searchPlaceholder="Tìm theo tên tài liệu, số hiệu…"
      />

      <UploadCertificateDialog open={uploadOpen} onOpenChange={setUploadOpen} />
      <UpdateDocumentDialog doc={editing} onClose={() => setEditing(null)} />
      <FilePreviewDialog file={previewFile} onClose={closePreview} />
    </>
  );
}
