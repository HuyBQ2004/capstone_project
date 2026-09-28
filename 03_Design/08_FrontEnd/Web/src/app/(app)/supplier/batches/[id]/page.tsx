"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, History, Upload } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
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
import { DateInput, Input, Label } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, usePagedList } from "@/lib/api/hooks";
import { type SupplierBatch, type SupplierDocument,
  type Delivery,
} from "@/lib/mock-data";

/**
 * Screen Spec 14/16/17 — Batch Detail / Upload Batch Certificates / View Batch History.
 * UC-043 (Manage), UC-100 (Upload Batch Certificates), UC-107 (View Batch History).
 * BR-071 (chứng nhận lô), BR-076 (truy xuất nguồn gốc), MSG-009 (sai định dạng file).
 * Review 07/2026: lịch sử lô (UC-107) chỉ hiển thị các sự kiện của CHÍNH lô đang xem
 * (không liệt kê tất cả lô).
 */

const ALLOWED_EXT = /\.(pdf|jpe?g|png)$/i;

function UploadBatchCertDialog({
  batch,
  open,
  onOpenChange,
}: {
  batch: SupplierBatch;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const [file, setFile] = useState<File | null>(null);
  const [expiry, setExpiry] = useState("");
  const [error, setError] = useState("");

  function submit() {
    if (!file || !expiry.trim()) {
      setError("Bắt buộc: tải lên file chứng chỉ và ngày hết hạn (BR-071).");
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
    toast.success(`Đã liên kết chứng chỉ "${file.name}" vào lô ${batch.lotNumber} (UC-100/BR-071)`);
    onOpenChange(false);
    setFile(null);
    setExpiry("");
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tải chứng chỉ cho lô hàng</DialogTitle>
          <DialogDescription>
            Lô đang chọn: <b>{batch.lotNumber} — {batch.productName}</b> (chỉ đọc).
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
          <div className="space-y-1.5">
            <Label htmlFor="c-exp">Ngày hết hạn chứng chỉ *</Label>
            <DateInput id="c-exp" value={expiry} onChange={(e) => setExpiry(e.target.value)} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button onClick={submit}>Lưu chứng chỉ</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function BatchDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { data: deliveriesPage } = usePagedList<Delivery>("deliveries", { pageSize: 100 });
  const deliveries = deliveriesPage?.items ?? [];
  const { data: supplierDocsPage } = usePagedList<SupplierDocument>("supplier-documents", { pageSize: 100 });
  const supplierDocs = supplierDocsPage?.items ?? [];
  const { id } = use(params);
  const { data: batch, isLoading } = useDetail<SupplierBatch>("supplier-batches", id);
  const [uploadOpen, setUploadOpen] = useState(false);
  const [showHistory, setShowHistory] = useState(false);

  if (isLoading) return <p className="text-sm text-muted-foreground">Đang tải chi tiết lô…</p>;
  if (!batch) return <Alert variant="danger" title="Không tìm thấy lô">Lô {id} không tồn tại.</Alert>;

  const linkedDocs = supplierDocs.filter((d) => batch.certificates.includes(d.id));

  return (
    <>
      <PageHeader
        title={`Lô ${batch.lotNumber}`}
        description={`${batch.productName} · ${batch.quantity} — chi tiết lô theo BR-071`}
        actions={
          <div className="flex gap-2">
            <Link href="/supplier/batches">
              <Button variant="outline"><ArrowLeft /> Danh sách lô</Button>
            </Link>
            <Button variant="outline" onClick={() => setShowHistory((v) => !v)}>
              <History /> Lịch sử lô (UC-107)
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
              <dd className="font-medium">{batch.productName}</dd>
              <dt className="text-muted-foreground">Số lô</dt>
              <dd className="font-medium">{batch.lotNumber}</dd>
              <dt className="text-muted-foreground">Ngày sản xuất</dt>
              <dd>{batch.manufacturingDate}</dd>
              <dt className="text-muted-foreground">Ngày đóng gói</dt>
              <dd>{batch.packagingDate}</dd>
              <dt className="text-muted-foreground">Hạn sử dụng</dt>
              <dd>{batch.expirationDate}</dd>
              <dt className="text-muted-foreground">Số lượng</dt>
              <dd>{batch.quantity}</dd>
              <dt className="text-muted-foreground">Trạng thái</dt>
              <dd><StatusBadge status={batch.status} /></dd>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Chứng chỉ liên kết (BR-046/BR-071)</CardTitle></CardHeader>
          <CardContent>
            {linkedDocs.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa có chứng chỉ liên kết — tải lên qua nút phía trên.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tài liệu</TableHead>
                    <TableHead>Hết hạn</TableHead>
                    <TableHead>Trạng thái</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {linkedDocs.map((d) => (
                    <TableRow key={d.id}>
                      <TableCell className="font-medium">{d.name}</TableCell>
                      <TableCell>{d.expiryDate}</TableCell>
                      <TableCell><StatusBadge status={d.status} /></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
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
                  <TableCell>{batch.manufacturingDate}</TableCell>
                  <TableCell className="font-medium">Sản xuất</TableCell>
                  <TableCell className="text-muted-foreground">{batch.productName} — NSX theo hồ sơ lô (BR-071)</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>{batch.packagingDate}</TableCell>
                  <TableCell className="font-medium">Đóng gói & đăng ký lô (UC-081)</TableCell>
                  <TableCell className="text-muted-foreground">Số lượng {batch.quantity} · HSD {batch.expirationDate}</TableCell>
                </TableRow>
                {linkedDocs.map((d) => (
                  <TableRow key={d.id}>
                    <TableCell>{d.issuedDate}</TableCell>
                    <TableCell className="font-medium">Liên kết chứng chỉ (UC-100)</TableCell>
                    <TableCell className="text-muted-foreground">{d.name} — hết hạn {d.expiryDate}</TableCell>
                  </TableRow>
                ))}
                {deliveries.filter((dl) => dl.linkedBatches?.includes(batch.id)).map((dl) => (
                  <TableRow key={dl.id}>
                    <TableCell>{dl.scheduledAt}</TableCell>
                    <TableCell className="font-medium">Liên kết chuyến giao (UC-083)</TableCell>
                    <TableCell className="text-muted-foreground">
                      {dl.id}{dl.purchaseRequestId ? ` — đơn ${dl.purchaseRequestId}` : ""} · {dl.school}
                    </TableCell>
                  </TableRow>
                ))}
                <TableRow>
                  <TableCell>Hiện tại</TableCell>
                  <TableCell className="font-medium">Trạng thái</TableCell>
                  <TableCell><StatusBadge status={batch.status} /></TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      <UploadBatchCertDialog batch={batch} open={uploadOpen} onOpenChange={setUploadOpen} />
       </>
  );
}
