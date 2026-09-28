"use client";

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Ban, Check, Download, Eye, FileCheck2, FileText, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
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
import { Label, Textarea } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, useAction, usePagedList, usePlainList } from "@/lib/api/hooks";
import type {
  CatalogItemDto,
  ChangeRequestDto,
  PartnerActionDto,
  PartnerDetailDto,
  PartnershipDto,
} from "@/lib/api/types";
import { PARTNER_TYPE_LABEL, labelOf } from "@/lib/labels";
import { formatVnDate } from "@/lib/format";
import { api } from "@/lib/api/client";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";
import { Handshake, ClipboardCheck, BarChart3, ListChecks } from "lucide-react";

/** Mirror `SchoolDocItem` (system-admin/schools/[id]) — `PartnerRegistrationDocumentResponse` (11/08/2026). */
interface PartnerRegistrationDocItem {
  id: string;
  name: string;
  documentType: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  status: string;
  uploadedAt: string;
  downloadUrl: string;
}

/**
 * [Proposed] Danh mục nguyên liệu cung ứng CỦA RIÊNG đối tác này — gộp "Danh mục đã duyệt"
 * (trước đây chỉ xem được ở `supplier/partnerships/[id]/catalog`, phía NCC) và "Đề xuất đang
 * chờ" (trước đây chỉ hiện gộp chung mọi đối tác ở `school-admin/partnerships` ngoài danh
 * sách — khó biết đề xuất nào của NCC nào khi trường hợp tác nhiều NCC). Cùng API đã có sẵn,
 * chỉ lọc theo đúng partner đang xem thay vì hiện tất cả.
 */
function PartnerCatalogSection({ partnerId }: { partnerId: string }) {
  const { data: partnershipsPage, isLoading: loadingPartnerships } = usePagedList<PartnershipDto>(
    "supplier-partnerships",
    { pageSize: 100 }
  );
  const partnership = (partnershipsPage?.items ?? []).find((p) => p.partnerId === partnerId);

  const { data: itemsPage } = usePagedList<CatalogItemDto>(
    partnership ? `partnerships/${partnership.id}/catalog-items` : "",
    { pageSize: 100 },
    { enabled: !!partnership }
  );
  const items = itemsPage?.items ?? [];

  const { data: requestsPage } = usePagedList<ChangeRequestDto>("catalog-change-requests", { pageSize: 200 });
  const requests = (requestsPage?.items ?? []).filter((r) => r.partnerId === partnerId && r.status === "PENDING");

  const decide = useAction<ChangeRequestDto>("catalog-change-requests");
  const [rejecting, setRejecting] = useState<ChangeRequestDto | null>(null);
  const [note, setNote] = useState("");

  if (loadingPartnerships) {
    return <Card className="mt-6 h-40 animate-pulse bg-muted/40" />;
  }
  if (!partnership) {
    // Đối tác chưa từng có hợp đồng hợp tác (supplier_partnerships) — hồ sơ còn PENDING/REJECTED.
    return null;
  }

  return (
    <Card className="mt-6">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <ListChecks className="size-5 text-primary" /> Danh mục nguyên liệu cung ứng
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div>
          <p className="mb-2 text-sm font-medium">Các nguyên liệu được trường duyệt</p>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nguyên liệu</TableHead>
                <TableHead className="text-right">Đơn giá</TableHead>
                <TableHead>Quy cách</TableHead>
                <TableHead>Hiệu lực</TableHead>
                <TableHead>Trạng thái</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {items.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-center text-muted-foreground">
                    Chưa có nguyên liệu nào được duyệt cho đối tác này.
                  </TableCell>
                </TableRow>
              ) : (
                items.map((i) => (
                  <TableRow key={i.id}>
                    <TableCell className="font-medium">{i.ingredientName ?? i.ingredientId}</TableCell>
                    <TableCell className="text-right">
                      {(i.approvedUnitPrice ?? 0).toLocaleString("vi-VN")}₫/{i.unit}
                    </TableCell>
                    <TableCell className="text-muted-foreground">{i.packaging ?? "—"}</TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {i.effectiveFrom ? `từ ${formatVnDate(i.effectiveFrom)}` : "—"}
                      {i.effectiveTo ? ` đến ${formatVnDate(i.effectiveTo)}` : ""}
                    </TableCell>
                    <TableCell><StatusBadge status={i.status ?? ""} /></TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>

        {requests.length > 0 && (
          <div>
            <p className="mb-2 text-sm font-medium">Đề xuất thay đổi danh mục — chờ duyệt</p>
            <div className="space-y-2">
              {requests.map((r) => (
                <div key={r.id} className="flex items-center justify-between rounded-lg border p-3">
                  <div className="text-sm">
                    <span className="font-medium">{r.ingredientName ?? r.ingredientId}</span>
                    {" — "}
                    {(r.requestedUnitPrice ?? 0).toLocaleString("vi-VN")}₫/{r.requestedUnit}
                    {r.requestedPackaging ? ` · ${r.requestedPackaging}` : ""}
                    <p className="text-muted-foreground">{r.reason}</p>
                  </div>
                  <div className="flex gap-1.5">
                    <Button
                      size="sm"
                      disabled={decide.isPending}
                      onClick={() =>
                        decide.mutate(
                          { id: r.id!, action: "decision", body: { approve: true } },
                          { onSuccess: () => toast.success("Đã duyệt — danh mục có hiệu lực.") }
                        )
                      }
                    >
                      Duyệt
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => setRejecting(r)}>
                      Từ chối
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>

      <Dialog open={!!rejecting} onOpenChange={(o) => !o && setRejecting(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Từ chối đề xuất — {rejecting?.ingredientName ?? rejecting?.ingredientId}</DialogTitle>
            <DialogDescription>Lý do từ chối bắt buộc để nhà cung cấp biết cần sửa gì.</DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5">
            <Label htmlFor="pc-note">Lý do từ chối *</Label>
            <Textarea id="pc-note" rows={4} value={note} onChange={(e) => setNote(e.target.value)} />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejecting(null)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={!note.trim() || decide.isPending}
              onClick={() =>
                rejecting &&
                decide.mutate(
                  { id: rejecting.id!, action: "decision", body: { approve: false, note } },
                  {
                    onSuccess: () => {
                      toast.success("Đã từ chối đề xuất và ghi lý do.");
                      setRejecting(null);
                      setNote("");
                    },
                  }
                )
              }
            >
              Xác nhận từ chối
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  );
}

/**
 * 04_ServiceCaterersDetail.md (UC-108, UC-004) + 05_SupplierDetail.md
 * (UC-148, UC-007) + 38_VerificationDocuments.md (bundled).
 * BR-040: hồ sơ đầy đủ mới được duyệt · BR-047: kiểm tra hạn chứng nhận ATTP ·
 * BR-048: từ chối bắt buộc ghi lý do · MSG-037 khi duyệt thành công.
 */
export default function PartnerDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  // `GET /partners/{id}` trả `PartnerDetailResponse` — shape KHÁC list (`PartnerResponse`):
  // `taxId`/`documents`/`email`/`phone`/`description` chỉ có ở đây. Dùng đúng type detail để
  // `tsc` chặn việc đọc nhầm chiều ngược lại (list đọc field detail-only).
  const { data: p, isLoading } = useDetail<PartnerDetailDto>("partners", id);
  const action = useAction<PartnerActionDto>("partners");
  // 11/08/2026 — tài liệu nộp kèm lúc đăng ký (UC-071/067), khác `p.documents`
  // (`PartnerDocumentSummary`, hồ sơ tuân thủ đầy đủ nộp sau khi đăng nhập).
  const { data: registrationDocs = [] } = usePlainList<PartnerRegistrationDocItem>(
    `partners/${id}/registration-documents`
  );
  const [previewFile, setPreviewFile] = useState<{ url: string; type: string; name: string } | null>(null);

  async function openDocWithAuth(downloadUrl: string, fileName: string, inline: boolean) {
    try {
      const res = await api.get<Blob>(downloadUrl, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
      if (inline) {
        setPreviewFile({
          url: objUrl,
          type: blob.type || (fileName.endsWith(".pdf") ? "application/pdf" : "image/jpeg"),
          name: fileName,
        });
      } else {
        const a = document.createElement("a");
        a.href = objUrl;
        a.download = fileName;
        a.click();
      }
      setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
    } catch {
      toast.error("Không thể tải file. Vui lòng thử lại.");
    }
  }
  /**
   * ⚠️ Cascade khóa tài khoản (ADR-M04-6) nay CHẠY THẬT phía server — không còn cần
   * vòng lặp `GET /users` từ phiên School Admin như bản cũ (vốn hỏng vì
   * `UserController` chỉ `SYSTEM_ADMIN`). `POST /partners/{code}/suspend` tự khóa
   * mọi tài khoản có `partnerId` này TRONG CÙNG transaction và trả về
   * `suspendedAccounts` — dùng số đó để báo cho School Admin biết đã khóa bao nhiêu
   * tài khoản, KHÔNG tự gọi `/users` nữa.
   *
   * Còn thiếu (MF01 owner việc): `POST /{code}/reinstate` KHÔNG tự mở lại tài khoản
   * đã khóa — vẫn cần System Admin mở tay tại `/system-admin/users` như trước.
   */
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState("");
  /**
   * Đình chỉ đối tác. Trạng thái SUSPENDED vốn chỉ tồn tại trong seed: màn nhận
   * hàng đã hiển thị cảnh báo "Đình chỉ" và chặn nhận, nhưng KHÔNG màn nào đặt
   * được cờ đó — tức quy tắc có mà không thi hành được.
   */
  const [suspending, setSuspending] = useState(false);
  const [suspendReason, setSuspendReason] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!p) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy hồ sơ đối tác.{" "}
        <Link className="underline" href="/school-admin/partnerships">Về danh sách</Link>
      </Card>
    );
  }

  const isCaterer = p.type === "SERVICE_CATERER";
  // `documents`/`name`/`contact`… optional trên `PartnerDetailResponse` (springdoc) — hồ sơ
  // chưa nộp giấy tờ nào thì mảng vắng mặt, không phải rỗng.
  const hasExpiredDoc = (p.documents ?? []).some((d) => d.status === "URGENT");
  const emailVerified = p.registrantEmailVerified ?? false;

  return (
    <>
      <PageHeader
        title={p.name ?? "—"}
        description={`${p.id} · ${labelOf(PARTNER_TYPE_LABEL, p.type)} · Nộp hồ sơ ${formatVnDate(p.appliedAt)} · MST ${p.taxId}`}
        actions={
          <>
            <Link href="/school-admin/partnerships">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {p.status === "PENDING" && !rejecting && (
              <>
                <Button
                  disabled={action.isPending || hasExpiredDoc || !emailVerified}
                  title={
                    hasExpiredDoc
                      ? "Chứng nhận hết hạn — không thể duyệt (BR-047)"
                      : !emailVerified
                        ? "Chờ đối tác xác thực email đăng ký trước khi phê duyệt (SUP-015)"
                        : ""
                  }
                  onClick={() =>
                    action.mutate(
                      { id: p.id!, action: "approve" },
                      {
                        onSuccess: () => {
                          toast.success("Đã phê duyệt hồ sơ đăng ký nhà cung cấp. (MSG-037)");
                          router.push("/school-admin/partnerships");
                        },
                      }
                    )
                  }
                >
                  <Check /> Phê duyệt (UC-{isCaterer ? "004" : "007"})
                </Button>
                <Button variant="destructive" onClick={() => setRejecting(true)}>
                  <X /> Từ chối
                </Button>
              </>
            )}
            {p.status === "APPROVED" && !suspending && (
              <Button variant="destructive" onClick={() => setSuspending(true)}>
                <Ban /> Đình chỉ hợp tác
              </Button>
            )}
            {p.status === "SUSPENDED" && (
              <Button
                disabled={action.isPending || hasExpiredDoc}
                title={hasExpiredDoc ? "Chứng nhận hết hạn — không khôi phục được (BR-047)" : ""}
                onClick={() =>
                  action.mutate(
                    { id: p.id!, action: "reinstate" },
                    {
                      onSuccess: () => {
                        // `useNotify()` (POST /notifications giả lập) đã BỎ — không có POST
                        // thật (ADR-M10-4), `PartnershipService` (MF04) CHƯA gọi
                        // `NotificationPort.publish()` (ghi nợ đợt sau). Đối tác xem trạng thái
                        // mới qua `partnerships` (đã nối thật).
                        toast.success(
                          "Đã khôi phục hợp tác với đối tác. Tài khoản đăng nhập của đối tác do System Admin mở lại tại Tài khoản người dùng."
                        );
                      },
                    }
                  )
                }
              >
                <Check /> Khôi phục hợp tác
              </Button>
            )}
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={p.status} />
        <Badge variant={isCaterer ? "primary" : "info"}>{labelOf(PARTNER_TYPE_LABEL, p.type)}</Badge>
        <StatusBadge status={p.certStatus} />
        {emailVerified ? (
          <Badge variant="success">Email đăng ký đã xác thực</Badge>
        ) : (
          <Badge variant="warning">Email đăng ký chưa xác thực</Badge>
        )}
      </div>

      {p.status === "PENDING" && !emailVerified && (
        <Alert variant="warning" className="mb-6" title="Chưa thể phê duyệt">
          Tài khoản đối tác tự tạo lúc đăng ký (UC-071/067) chưa bấm link kích hoạt gửi qua
          email để xác thực. Phê duyệt bị chặn (SUP-015) cho tới khi email được xác thực —
          tránh duyệt nhầm hồ sơ dùng email không có thật/gõ sai (BR-010 mở rộng).
        </Alert>
      )}

      {suspending && (
        <Card className="mb-4 border-destructive/40">
          <CardContent className="space-y-3 pt-5">
            <Label>Lý do đình chỉ *</Label>
            <Textarea
              rows={3}
              value={suspendReason}
              onChange={(e) => setSuspendReason(e.target.value)}
              placeholder="VD: lô hàng không đạt kiểm tra tiếp nhận nhiều lần; chứng nhận ATTP hết hiệu lực…"
            />
            <p className="text-xs text-muted-foreground">
              Đối tác bị đình chỉ không nhận được đơn đặt hàng mới; phiếu nhận hàng từ đối
              tác này sẽ bị cảnh báo tại màn Tiếp nhận nguyên liệu.
            </p>
            <div className="flex gap-2">
              <Button
                variant="destructive"
                disabled={action.isPending || suspendReason.trim() === ""}
                onClick={() =>
                  action.mutate(
                    { id: p.id!, action: "suspend", body: { reason: suspendReason.trim() } },
                    {
                      onSuccess: (res) => {
                        // `useNotify()` giả lập đã BỎ — cùng lý do nhánh khôi phục ở trên.
                        toast.success(
                          `Đã đình chỉ hợp tác — đã thông báo tới bếp và nhà cung cấp. Đã khóa ${res.suspendedAccounts ?? 0} tài khoản đăng nhập của đối tác (ADR-M04-6).`
                        );
                        setSuspending(false);
                        setSuspendReason("");
                      },
                    }
                  )
                }
              >
                Xác nhận đình chỉ
              </Button>
              <Button variant="outline" onClick={() => setSuspending(false)}>Hủy</Button>
            </div>
          </CardContent>
        </Card>
      )}

      {p.status === "SUSPENDED" && p.suspendReason && (
        <Alert variant="danger" className="mb-4" title="Đối tác đang bị đình chỉ">
          {p.suspendReason}
        </Alert>
      )}

      {hasExpiredDoc && (
        <Alert variant="danger" className="mb-4" title="Chứng nhận hết hạn (BR-047, MSG-013)">
          Hồ sơ có giấy chứng nhận đã/sắp hết hạn khẩn cấp — nhà cung cấp hết hạn chứng
          nhận ATTP sẽ bị đình chỉ tự động.
        </Alert>
      )}

      {rejecting && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Lý do từ chối * (BR-048)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label htmlFor="pt-reason">Lý do sẽ được ghi lại và gửi cho đối tác</Label>
              <Textarea
                id="pt-reason"
                rows={4}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="VD: Giấy chứng nhận ATTP đã hết hạn (MSG-013)…"
              />
            </div>
            <div className="flex gap-2">
              <Button
                variant="destructive"
                disabled={!reason.trim() || action.isPending}
                onClick={() =>
                  action.mutate(
                    { id: p.id!, action: "reject", body: { reason } },
                    {
                      onSuccess: () => {
                        toast.success("Đã từ chối hồ sơ và ghi lý do (BR-048)");
                        router.push("/school-admin/partnerships");
                      },
                    }
                  )
                }
              >
                Xác nhận từ chối
              </Button>
              <Button variant="outline" onClick={() => setRejecting(false)}>Hủy</Button>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="Điểm hiệu suất"
          value={(p.performanceScore ?? 0) > 0 ? `${p.performanceScore}%` : "Chưa có"}
          icon={BarChart3}
          tone={(p.performanceScore ?? 0) >= 70 || (p.performanceScore ?? 0) === 0 ? "success" : "danger"}
        />
        {isCaterer && (
          <StatCard
            label="Điểm khảo sát cơ sở (UC-108)"
            value={p.siteInspectionScore != null ? `${p.siteInspectionScore}%` : "Chưa khảo sát"}
            icon={ClipboardCheck}
            tone="info"
          />
        )}
        <StatCard label="Liên hệ" value={p.contact ?? "—"} icon={Handshake} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Thông tin doanh nghiệp</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã số thuế</dt>
                <dd className="font-medium">{p.taxId}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Địa chỉ</dt>
                <dd className="text-right font-medium">{p.address}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày nộp hồ sơ</dt>
                <dd className="font-medium">{formatVnDate(p.appliedAt)}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Hạn chứng nhận ATTP (BR-047)</dt>
                <dd className="font-medium">{formatVnDate(p.certExpiry)}</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileCheck2 className="size-5 text-primary" />
              Giấy tờ xác minh (38_VerificationDocuments, BR-047)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Loại giấy tờ</TableHead>
                  <TableHead>Số hiệu</TableHead>
                  <TableHead>Hết hạn</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead className="w-28" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {(p.documents ?? []).map((d) => (
                  <TableRow key={d.number}>
                    <TableCell className="font-medium">{d.name}</TableCell>
                    <TableCell>{d.number}</TableCell>
                    <TableCell>{formatVnDate(d.expiry)}</TableCell>
                    <TableCell><StatusBadge status={d.status} /></TableCell>
                    <TableCell>
                      {d.id && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => openDocWithAuth(`/supplier-documents/${d.id}/file`, d.name ?? d.id ?? "tai-lieu", true)}
                        >
                          <Eye className="size-3.5 mr-1" /> Xem
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>

      {/* 11/08/2026 — tài liệu nộp kèm lúc đăng ký (UC-071/067), lưu thật ở partner_registration_documents */}
      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="size-5 text-primary" /> Tài liệu nộp kèm lúc đăng ký
          </CardTitle>
        </CardHeader>
        <CardContent>
          {registrationDocs.length === 0 ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              Đối tác chưa đính kèm tài liệu nào lúc đăng ký.
            </p>
          ) : (
            <div className="divide-y rounded-lg border">
              {registrationDocs.map((doc) => {
                const formattedSize =
                  typeof doc.fileSize === "number" ? `${(doc.fileSize / (1024 * 1024)).toFixed(1)} MB` : "—";
                return (
                  <div key={doc.id} className="flex flex-col sm:flex-row sm:items-center justify-between p-3.5 gap-3">
                    <div className="flex items-start gap-3">
                      <div className="p-2 rounded bg-muted/60 text-primary shrink-0">
                        <FileText className="size-5" />
                      </div>
                      <div>
                        <h4 className="font-medium text-sm">{doc.name}</h4>
                        <p className="text-xs text-muted-foreground flex items-center gap-2 mt-0.5">
                          <span>{doc.fileName}</span> • <span>{formattedSize}</span> •{" "}
                          <span>Đăng tải: {formatVnDate(doc.uploadedAt)}</span>
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
                      <Button variant="outline" size="sm" onClick={() => openDocWithAuth(doc.downloadUrl, doc.fileName, true)}>
                        <Eye className="size-3.5 mr-1" /> Xem
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => openDocWithAuth(doc.downloadUrl, doc.fileName, false)}>
                        <Download className="size-3.5 mr-1" /> Tải về
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Danh mục nguyên liệu chỉ áp dụng cho NCC nguyên liệu — đơn vị suất ăn không cung ứng
          nguyên liệu cho trường nên không có hợp đồng catalog. */}
      {!isCaterer && <PartnerCatalogSection partnerId={p.id ?? id} />}

      <FilePreviewDialog file={previewFile} onClose={() => setPreviewFile(null)} />
    </>
  );
}
