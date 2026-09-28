"use client";

/**
 * 11/08/2026 (theo Nam) — Y tế trường xem (CHỈ ĐỌC) danh mục cung ứng + hồ sơ tuân thủ của
 * nhà cung cấp nguyên liệu VÀ đơn vị suất ăn đã liên kết với trường, phục vụ đánh giá tuân
 * thủ (BR-046/047 hạn chứng nhận ATTP). Không có action sửa/xoá/duyệt nào ở màn này — quyền
 * đó vẫn thuộc System Admin/School Admin. Cùng khuôn hiển thị với
 * `service-caterers/suppliers/page.tsx`, khác ở 2 điểm:
 *   1. Không lọc `type` — hiện CẢ nhà cung cấp nguyên liệu lẫn đơn vị suất ăn.
 *   2. Dialog hiện hồ sơ tuân thủ (giấy ATTP/ĐKKD, hạn dùng, trạng thái) thay vì bảng giá sản
 *      phẩm — Y tế cần biết "còn hợp lệ không", không cần biết giá.
 */

import { useState } from "react";
import { Building2, CheckCircle2, Download, Eye, FileText, Mail, MapPin, Phone, Search, ShieldCheck, Store, Tag, XCircle } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, usePagedList } from "@/lib/api/hooks";
import { api } from "@/lib/api/client";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";
import type { PartnerDetailDto, PartnerDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

/** Cùng cách mở file có auth với `school-admin/partnerships/[id]` — D10-BIS, không URL công khai. */
async function openDocWithAuth(
  downloadUrl: string,
  fileName: string,
  setPreviewFile: (f: { url: string; type: string; name: string } | null) => void
) {
  try {
    const res = await api.get<Blob>(downloadUrl, { responseType: "blob" });
    const blob = res.data;
    const objUrl = URL.createObjectURL(blob);
    setPreviewFile({
      url: objUrl,
      type: blob.type || (fileName.endsWith(".pdf") ? "application/pdf" : "image/jpeg"),
      name: fileName,
    });
    setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
  } catch {
    toast.error("Không thể tải file. Vui lòng thử lại.");
  }
}

const TYPE_LABEL: Record<string, string> = {
  RAW_FOOD_SUPPLIER: "Nhà cung cấp nguyên liệu",
  SERVICE_CATERER: "Đơn vị suất ăn",
};

function PartnerComplianceDetail({
  partner,
  setPreviewFile,
}: {
  partner: PartnerDto;
  setPreviewFile: (f: { url: string; type: string; name: string } | null) => void;
}) {
  const { data: detailData } = useDetail<PartnerDetailDto>("partners", partner.id ?? "");
  const detail = detailData ?? (partner as unknown as PartnerDetailDto);
  const documents = detail.documents ?? [];

  return (
    <div className="space-y-4">
      <div className="space-y-2 rounded-xl border bg-muted/40 p-4 text-sm">
        <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
          <div className="flex items-center gap-2">
            <Building2 className="size-4 shrink-0 text-muted-foreground" />
            <span className="font-semibold text-foreground">{detail.name || partner.name}</span>
            <span className="text-xs text-muted-foreground">({partner.id})</span>
          </div>
          {detail.taxId && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Tag className="size-4 shrink-0" />
              <span>MST: {detail.taxId}</span>
            </div>
          )}
          {detail.phone && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Phone className="size-4 shrink-0" />
              <span>{detail.phone}</span>
            </div>
          )}
          {detail.email && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Mail className="size-4 shrink-0" />
              <span>{detail.email}</span>
            </div>
          )}
        </div>
        {detail.address && (
          <div className="flex items-start gap-2 pt-1 text-xs text-muted-foreground">
            <MapPin className="mt-0.5 size-3.5 shrink-0" />
            <span>{detail.address}</span>
          </div>
        )}
      </div>

      <div className="overflow-hidden rounded-xl border bg-card">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/60">
              <TableHead>Tài liệu</TableHead>
              <TableHead>Số hiệu</TableHead>
              <TableHead>Hết hạn</TableHead>
              <TableHead className="w-32">Trạng thái</TableHead>
              <TableHead className="w-24" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {documents.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="h-20 text-center text-muted-foreground">
                  Chưa có tài liệu tuân thủ nào được tải lên.
                </TableCell>
              </TableRow>
            ) : (
              documents.map((d) => (
                <TableRow key={d.id}>
                  <TableCell className="font-medium">
                    <div className="flex items-center gap-1.5">
                      <FileText className="size-3.5 text-muted-foreground" /> {d.name || "—"}
                    </div>
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">{d.number || "—"}</TableCell>
                  <TableCell className="text-xs">{d.expiry ? formatVnDate(d.expiry) : "Vô thời hạn"}</TableCell>
                  <TableCell>{d.status ? <StatusBadge status={d.status} /> : "—"}</TableCell>
                  <TableCell>
                    {d.id && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => openDocWithAuth(`/supplier-documents/${d.id}/file`, d.name ?? d.id ?? "tai-lieu", setPreviewFile)}
                      >
                        <Eye className="size-3.5 mr-1" /> Xem
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}

export default function MedicalStaffSuppliersPage() {
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<PartnerDto | null>(null);
  const [previewFile, setPreviewFile] = useState<{ url: string; type: string; name: string } | null>(null);

  // Không lọc `type` — Y tế cần thấy cả NCC nguyên liệu lẫn đơn vị suất ăn (khác
  // `service-caterers/suppliers` chỉ lọc RAW_FOOD_SUPPLIER).
  const { data: partnerPage, isLoading } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const partners = partnerPage?.items ?? [];

  const filtered = partners.filter(
    (p) =>
      p.name?.toLowerCase().includes(search.trim().toLowerCase()) ||
      p.id?.toLowerCase().includes(search.trim().toLowerCase())
  );

  return (
    <>
      <PageHeader
        title="Danh mục cung ứng & Hồ sơ đối tác"
        description="Đối tác (NCC nguyên liệu, đơn vị suất ăn) đã liên kết với trường — xem hồ sơ tuân thủ để đánh giá (chỉ đọc)"
      />

      <Card>
        <CardHeader className="flex-row flex-wrap items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-lg">
            <Store className="size-5 text-primary" />
            <span>Đối tác liên kết với trường ({filtered.length})</span>
          </CardTitle>
          <div className="relative w-72">
            <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              className="h-9 pl-8 text-sm"
              placeholder="Tìm theo tên, mã đối tác…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="animate-pulse py-12 text-center text-muted-foreground">Đang tải danh sách đối tác…</div>
          ) : filtered.length === 0 ? (
            <div className="py-12 text-center text-muted-foreground">Không tìm thấy đối tác nào.</div>
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
              {filtered.map((partner, idx) => {
                const isActive = partner.status === "APPROVED" || partner.status === "ACTIVE";
                return (
                  <Card
                    key={partner.id || idx}
                    className="flex flex-col justify-between overflow-hidden transition-all hover:border-primary/50 hover:shadow-md"
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <CardTitle className="text-base font-semibold leading-tight">{partner.name}</CardTitle>
                          <CardDescription className="mt-0.5 font-mono text-xs">{partner.id}</CardDescription>
                        </div>
                        <Badge variant={isActive ? "success" : "neutral"} className="shrink-0">
                          {isActive ? (
                            <span className="flex items-center gap-1"><CheckCircle2 className="size-3" /> Hợp tác</span>
                          ) : (
                            <span className="flex items-center gap-1"><XCircle className="size-3" /> {partner.status || "Khác"}</span>
                          )}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-2.5 text-xs text-muted-foreground">
                      <div className="flex items-center gap-2">
                        <Tag className="size-3.5 shrink-0" />
                        <span>Loại: {TYPE_LABEL[partner.type ?? ""] ?? partner.type ?? "—"}</span>
                      </div>
                      {partner.certStatus && (
                        <div className="flex items-center gap-2">
                          <ShieldCheck className="size-3.5 shrink-0 text-emerald-500" />
                          <span>Chứng nhận ATTP: {partner.certStatus}</span>
                        </div>
                      )}
                    </CardContent>
                    <div className="border-t bg-muted/20 p-3 text-right">
                      <button
                        type="button"
                        className="w-full rounded-md border px-3 py-1.5 text-sm font-medium hover:bg-muted"
                        onClick={() => setSelected(partner)}
                      >
                        Xem hồ sơ tuân thủ
                      </button>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={selected != null} onOpenChange={(open) => !open && setSelected(null)}>
        <DialogContent className="max-h-[85vh] max-w-3xl overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-lg">
              <ShieldCheck className="size-5 text-primary" />
              <span>Hồ sơ tuân thủ — {selected?.name}</span>
            </DialogTitle>
            <DialogDescription>
              Tài liệu tuân thủ (ATTP/ĐKKD) của {selected?.name} ({selected?.id}) — đánh giá theo BR-046/047.
            </DialogDescription>
          </DialogHeader>
          {selected && <PartnerComplianceDetail partner={selected} setPreviewFile={setPreviewFile} />}
        </DialogContent>
      </Dialog>

      <FilePreviewDialog file={previewFile} onClose={() => setPreviewFile(null)} />
    </>
  );
}
