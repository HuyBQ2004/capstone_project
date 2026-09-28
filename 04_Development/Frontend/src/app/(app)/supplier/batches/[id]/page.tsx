"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Eye, History, Pencil, Upload } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";
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
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { api } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";
import { useDetail, usePagedList, useUpdate, useUploadMutation } from "@/lib/api/hooks";
import type { DeliveryDto, SupplierBatchDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";

const BATCH_STATUS = ["ACTIVE", "DELIVERED", "EXPIRED", "CANCELLED"] as const;
const BATCH_STATUS_LABEL: Record<string, string> = {
  ACTIVE: "Đang hiệu lực",
  DELIVERED: "Đã giao hết",
  EXPIRED: "Hết hạn",
  CANCELLED: "Huỷ lô",
};

/**
 * 13/08/2026 (theo Nam) — `PUT /supplier-batches/{code}` đã tồn tại từ trước (chỉ nhận
 * expirationDate/quantity/status — KHÔNG sửa được số lô/NSX/nguyên liệu, đó là định danh lô)
 * nhưng chưa có form nào gọi tới. Server tự chặn hạ `remainingQuantity` xuống dưới phần đã
 * cấp phát (xem `SupplierBatchService.update`) — FE không cần validate lại, chỉ hiện lỗi
 * server trả về.
 */
function EditBatchDialog({
  batch,
  open,
  onOpenChange,
}: {
  batch: SupplierBatchDto;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const update = useUpdate<{ id: string } & Record<string, unknown>, SupplierBatchDto>("supplier-batches", {
    handledError: true,
  });
  const [expirationDate, setExpirationDate] = useState(batch.expirationDate ?? "");
  const [quantity, setQuantity] = useState(String(batch.quantity ?? ""));
  const [status, setStatus] = useState(batch.status ?? "ACTIVE");
  const [error, setError] = useState("");

  function submit() {
    const qty = Number(quantity);
    if (!quantity.trim() || Number.isNaN(qty) || qty <= 0) {
      setError("Số lượng phải là số dương.");
      return;
    }
    setError("");
    update.mutate(
      { id: batch.id ?? "", expirationDate, quantity: qty, status },
      {
        onSuccess: () => {
          toast.success(`Đã cập nhật lô ${batch.lotNumber}.`);
          onOpenChange(false);
        },
        onError: (err) => setError(apiErrorMessage(err, "Không cập nhật được lô hàng.")),
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Sửa lô {batch.lotNumber}</DialogTitle>
          <DialogDescription>
            Chỉ sửa được hạn sử dụng, số lượng và trạng thái — số lô/ngày sản xuất/nguyên liệu
            là định danh lô, không sửa được sau khi tạo.
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể cập nhật">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="eb-exp">Hạn sử dụng</Label>
            <Input id="eb-exp" type="date" value={expirationDate} onChange={(e) => setExpirationDate(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="eb-qty">Số lượng ({batch.unit})</Label>
            <Input id="eb-qty" inputMode="decimal" value={quantity} onChange={(e) => setQuantity(e.target.value)} />
            <p className="text-xs text-muted-foreground">
              Còn lại hiện tại: {batch.remainingQuantity ?? batch.quantity} {batch.unit} — hệ thống tự chặn nếu hạ
              số lượng xuống dưới phần đã giao (BR-071).
            </p>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="eb-status">Trạng thái</Label>
            <Select id="eb-status" value={status} onChange={(e) => setStatus(e.target.value)}>
              {BATCH_STATUS.map((s) => (
                <option key={s} value={s}>{BATCH_STATUS_LABEL[s]}</option>
              ))}
            </Select>
            {status === "CANCELLED" && (
              <p className="text-xs text-warning">
                Chỉ huỷ được lô CHƯA từng được nhận (còn lại = số lượng đăng ký) — lô đã giao
                một phần bị chặn huỷ để không xoá dấu vết truy xuất nguồn gốc (BR-076).
              </p>
            )}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={update.isPending} onClick={submit}>Lưu</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/**
 * Screen Spec 14/16/17 — Batch Detail / Upload Batch Certificates / View Batch History.
 * UC-043 (Manage), UC-100 (Upload Batch Certificates), UC-107 (View Batch History).
 * BR-071 (chứng nhận lô), BR-076 (truy xuất nguồn gốc), MSG-009 (sai định dạng file).
 * Review 07/2026: lịch sử lô (UC-107) chỉ hiển thị các sự kiện của CHÍNH lô đang xem
 * (không liệt kê tất cả lô).
 *
 * Nối backend thật 05/08/2026: dialog tải chứng chỉ TRƯỚC ĐÂY chỉ hiện toast, KHÔNG gọi API
 * nào — nay gọi thật `POST /supplier-batches/{code}/certificates` (multipart, 1 file/lần,
 * `useUploadMutation`). Đây là bản ghi TÀI LIỆU MỚI (`partner_documents`, `document_type=
 * PRODUCT_ORIGIN`), KHÔNG tham chiếu tài liệu tuân thủ có sẵn ở `supplier-documents` — và
 * KHÔNG nhận "ngày hết hạn" (field đó không tồn tại ở backend, đã bỏ khỏi form). Response chỉ
 * trả mã tài liệu (`certificateIds: string[]` trên `SupplierBatchResponse`), không có API đọc
 * lại tên/ngày hết hạn của từng chứng chỉ — panel dưới chỉ hiện được MÃ, không hiện tên/HSD.
 */

const ALLOWED_EXT = /\.(pdf|jpe?g|png)$/i;

function UploadBatchCertDialog({
  batch,
  open,
  onOpenChange,
}: {
  batch: SupplierBatchDto;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const upload = useUploadMutation<{ documentId: string }>("supplier-batches");
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState("");

  function submit() {
    if (!file) {
      setError("Bắt buộc: tải lên file chứng chỉ (BR-071).");
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
    setError("");
    upload.mutate(
      { path: `${batch.id}/certificates`, parts: { file } },
      {
        onSuccess: (res) => {
          toast.success(`Đã liên kết chứng chỉ ${res.documentId} vào lô ${batch.lotNumber} (UC-100/BR-071)`);
          onOpenChange(false);
          setFile(null);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tải chứng chỉ cho lô hàng</DialogTitle>
          <DialogDescription>
            Lô đang chọn: <b>{batch.lotNumber} — {batch.ingredient}</b> (chỉ đọc).
            File PDF/JPG/PNG, tối đa 10MB (MSG-008/009).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể tải lên">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="c-file">File chứng chỉ * (PDF/JPG/PNG ≤10MB)</Label>
            <Input
              id="c-file"
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            />
            {file && <p className="text-xs text-muted-foreground">Đã chọn: {file.name}</p>}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={upload.isPending} onClick={submit}>Lưu chứng chỉ</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function BatchDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { data: deliveriesPage } = usePagedList<DeliveryDto>("deliveries", { pageSize: 100 });
  const deliveries = deliveriesPage?.items ?? [];
  const { id } = use(params);
  const { data: batch, isLoading } = useDetail<SupplierBatchDto>("supplier-batches", id);
  const [uploadOpen, setUploadOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [previewFile, setPreviewFile] = useState<{ url: string; type: string; name: string } | null>(null);
  const [loadingDocId, setLoadingDocId] = useState<string | null>(null);

  if (isLoading) return <p className="text-sm text-muted-foreground">Đang tải chi tiết lô…</p>;
  if (!batch) return <Alert variant="danger" title="Không tìm thấy lô">Lô {id} không tồn tại.</Alert>;

  const certificateIds = batch.certificateIds ?? [];

  /**
   * Chứng chỉ lô lưu chung bảng `partner_documents` với tài liệu tuân thủ (D10-BIS —
   * `SupplierBatchService.uploadCertificate`, `document_type=PRODUCT_ORIGIN`), nên đọc lại
   * qua CHÍNH endpoint `GET /supplier-documents/{code}/file` (RIÊNG TƯ, không CDN/URL công
   * khai) — trước đây panel chỉ hiện mã (`Badge`), không có cách nào bấm xem file thật.
   */
  async function previewCertificate(docId: string) {
    setLoadingDocId(docId);
    try {
      const res = await api.get<Blob>(`/supplier-documents/${docId}/file`, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
      setPreviewFile({ url: objUrl, type: blob.type, name: docId });
    } catch {
      toast.error("Không thể tải file chứng chỉ để xem — vui lòng thử lại.");
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

  return (
    <>
      <PageHeader
        title={`Lô ${batch.lotNumber}`}
        description={`${batch.ingredient} · ${batch.quantity} ${batch.unit} — chi tiết lô theo BR-071`}
        actions={
          <div className="flex gap-2">
            <Link href="/supplier/batches">
              <Button variant="outline"><ArrowLeft /> Danh sách lô</Button>
            </Link>
            <Button variant="outline" onClick={() => setShowHistory((v) => !v)}>
              <History /> Lịch sử lô (UC-107)
            </Button>
            <Button variant="outline" onClick={() => setEditOpen(true)}>
              <Pencil /> Sửa lô
            </Button>
            <Button onClick={() => setUploadOpen(true)}>
              <Upload /> Tải chứng chỉ lô (UC-100)
            </Button>
          </div>
        }
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Thông tin lô (BR-071)</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
              <dt className="text-muted-foreground">Nguyên liệu</dt>
              <dd className="font-medium">{batch.ingredient}</dd>
              <dt className="text-muted-foreground">Số lô</dt>
              <dd className="font-medium">{batch.lotNumber}</dd>
              <dt className="text-muted-foreground">Ngày sản xuất</dt>
              <dd>{formatVnDate(batch.manufacturingDate)}</dd>
              <dt className="text-muted-foreground">Ngày đóng gói</dt>
              <dd>{formatVnDate(batch.packagingDate)}</dd>
              <dt className="text-muted-foreground">Hạn sử dụng</dt>
              <dd>{formatVnDate(batch.expirationDate)}</dd>
              <dt className="text-muted-foreground">Số lượng</dt>
              <dd>{batch.quantity} {batch.unit}</dd>
              <dt className="text-muted-foreground">Trạng thái</dt>
              <dd><StatusBadge status={batch.status ?? ""} /></dd>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Chứng chỉ liên kết (BR-046/BR-071)</CardTitle></CardHeader>
          <CardContent>
            {certificateIds.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa có chứng chỉ liên kết — tải lên qua nút phía trên.</p>
            ) : (
              <div className="flex flex-wrap gap-1.5">
                {certificateIds.map((docId) => (
                  <Button
                    key={docId}
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={loadingDocId === docId}
                    onClick={() => previewCertificate(docId)}
                  >
                    <Eye className="size-3.5" /> {loadingDocId === docId ? "Đang tải…" : docId}
                  </Button>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {showHistory && (
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>Lịch sử lô {batch.lotNumber} (UC-107 — chỉ đọc, BR-076)</CardTitle>
          </CardHeader>
          <CardContent>
            {/* Review 07/2026: chỉ các sự kiện của chính lô này — phục vụ truy xuất nguồn gốc. */}
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Thời điểm</TableHead>
                  <TableHead>Sự kiện</TableHead>
                  <TableHead>Chi tiết</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow>
                  <TableCell>{formatVnDate(batch.manufacturingDate)}</TableCell>
                  <TableCell className="font-medium">Sản xuất</TableCell>
                  <TableCell className="text-muted-foreground">{batch.ingredient} — NSX theo hồ sơ lô (BR-071)</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>{formatVnDate(batch.packagingDate)}</TableCell>
                  <TableCell className="font-medium">Đóng gói & đăng ký lô (UC-081)</TableCell>
                  <TableCell className="text-muted-foreground">Số lượng {batch.quantity} {batch.unit} · HSD {formatVnDate(batch.expirationDate)}</TableCell>
                </TableRow>
                {certificateIds.map((docId) => (
                  <TableRow key={docId}>
                    <TableCell>—</TableCell>
                    <TableCell className="font-medium">Liên kết chứng chỉ (UC-100)</TableCell>
                    <TableCell className="text-muted-foreground">{docId}</TableCell>
                  </TableRow>
                ))}
                {deliveries.filter((dl) => dl.linkedBatches?.includes(batch.id ?? "")).map((dl) => (
                  <TableRow key={dl.id}>
                    <TableCell>{formatVnDateTime(dl.scheduledAt)}</TableCell>
                    <TableCell className="font-medium">Liên kết chuyến giao (UC-083)</TableCell>
                    <TableCell className="text-muted-foreground">
                      {dl.id}{dl.purchaseRequestId ? ` — đơn ${dl.purchaseRequestId}` : ""} · {dl.school}
                    </TableCell>
                  </TableRow>
                ))}
                <TableRow>
                  <TableCell>Hiện tại</TableCell>
                  <TableCell className="font-medium">Trạng thái</TableCell>
                  <TableCell><StatusBadge status={batch.status ?? ""} /></TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      <UploadBatchCertDialog batch={batch} open={uploadOpen} onOpenChange={setUploadOpen} />
      <EditBatchDialog batch={batch} open={editOpen} onOpenChange={setEditOpen} />
      <FilePreviewDialog file={previewFile} onClose={closePreview} />
    </>
  );
}
