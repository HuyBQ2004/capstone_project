"use client";

import { useState } from "react";
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
import { Label, Select, Textarea } from "@/components/ui/input";
import { useAction, usePagedList } from "@/lib/api/hooks";
import type { ChangeRequestDto, PartnerActionDto, PartnerDto } from "@/lib/api/types";
import { PARTNER_TYPE_LABEL, labelOf } from "@/lib/labels";
import { formatVnDate } from "@/lib/format";

/**
 * 02/03_PartnershipRequests.md — MF05 (School Administration).
 * BR-040: duyệt hồ sơ đầy đủ · BR-041: Pending → Approved/Rejected ·
 * BR-048: từ chối bắt buộc ghi lý do · MSG-037 khi duyệt thành công.
 * Chi tiết hồ sơ + giấy tờ xác minh: bấm dòng → /school-admin/partnerships/[id].
 */

function PartnerActions({ partner }: { partner: PartnerDto }) {
  const action = useAction<PartnerActionDto>("partners");
  const [rejectOpen, setRejectOpen] = useState(false);
  const [reason, setReason] = useState("");

  // `id` optional trên DTO nhưng là mã dựng URL `POST /partners/{id}/{approve,reject}` —
  // không có thì không render nút, thay vì gọi vào `/partners//approve`.
  if (partner.status !== "PENDING" || !partner.id) return null;
  const partnerId = partner.id;

  return (
    <div className="flex gap-1.5" onClick={(e) => e.stopPropagation()}>
      <Button
        size="sm"
        disabled={action.isPending}
        onClick={() =>
          action.mutate(
            { id: partnerId, action: "approve" },
            {
              onSuccess: () =>
                toast.success("Đã phê duyệt hồ sơ đăng ký nhà cung cấp. (MSG-037)"),
            }
          )
        }
      >
        Phê duyệt
      </Button>
      <Button variant="destructive" size="sm" onClick={() => setRejectOpen(true)}>
        Từ chối
      </Button>

      <Dialog open={rejectOpen} onOpenChange={setRejectOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Từ chối hồ sơ — {partner.name}</DialogTitle>
            <DialogDescription>
              Lý do từ chối bắt buộc được ghi lại và gửi thông báo cho nhà cung cấp (BR-048).
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5">
            <Label htmlFor="reason">Lý do từ chối *</Label>
            <Textarea
              id="reason"
              rows={4}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="VD: Giấy chứng nhận ATTP đã hết hạn (MSG-013)…"
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectOpen(false)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={!reason.trim() || action.isPending}
              onClick={() =>
                action.mutate(
                  { id: partnerId, action: "reject", body: { reason } },
                  {
                    onSuccess: () => {
                      toast.success("Đã từ chối hồ sơ và ghi lý do (BR-048)");
                      setRejectOpen(false);
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
    </div>
  );
}

/**
 * PROPOSED (chưa có TDS §8 chính thức — xem `Proposal-Partnership-Catalog-Contract.md`):
 * duyệt/từ chối đề xuất thay đổi danh mục sản phẩm-giá của NCC. Duyệt ⇒ backend upsert
 * dòng danh mục đã duyệt (`PartnershipCatalogItem`); từ chối bắt buộc ghi lý do.
 */
function CatalogChangeRequestsPanel() {
  const { data } = usePagedList<ChangeRequestDto>("catalog-change-requests", {
    status: "PENDING",
    pageSize: 100,
  });
  const requests = data?.items ?? [];
  const decide = useAction<ChangeRequestDto>("catalog-change-requests");
  const [rejecting, setRejecting] = useState<ChangeRequestDto | null>(null);
  const [note, setNote] = useState("");

  if (requests.length === 0) return null;

  return (
    <>
      <h2 className="mb-3 mt-8 text-lg font-semibold">
        Đề xuất thay đổi danh mục sản phẩm-giá (Proposed — chờ duyệt)
      </h2>
      <p className="mb-3 text-sm text-muted-foreground">
        Nhà cung cấp chỉ được giao đúng sản phẩm/giá đã duyệt ở đây — mọi thay đổi phải qua
        đề xuất, không tự sửa danh mục chung.
      </p>
      <div className="space-y-2">
        {requests.map((r) => (
          <div key={r.id} className="flex items-center justify-between rounded-lg border p-3">
            <div className="text-sm">
              {/* 10/08/2026 — trường có thể hợp tác NHIỀU NCC; thiếu tên đối tác thì không
                  biết đề xuất này của ai, đặc biệt khi danh sách dài như hiện tại. */}
              <span className="mr-2 rounded bg-muted px-1.5 py-0.5 text-xs font-medium text-muted-foreground">
                {r.partner ?? r.partnerId ?? "NCC không xác định"}
              </span>
              <span className="font-medium">{r.ingredientName ?? r.ingredientId}</span>
              {" — "}
              {(r.requestedUnitPrice ?? 0).toLocaleString("vi-VN")}₫/{r.requestedUnit}
              {r.requestedConversionToBase != null && r.requestedConversionToBase !== 1 && (
                <span className="ml-2 text-xs font-medium text-warning">
                  (NCC khai: 1 {r.requestedUnit} = {r.requestedConversionToBase} {r.ingredientUnit ?? "đơn vị chuẩn"}
                  {r.requestedConversionNote ? ` — ${r.requestedConversionNote}` : ""})
                </span>
              )}
              {r.requestedPackaging ? ` · ${r.requestedPackaging}` : ""}
              {r.requestedPackagingSize != null && (
                <span className="ml-2 text-xs">
                  ({r.requestedAllowPartialPackage
                    ? `cho phép tách lẻ, quy cách ${r.requestedPackagingSize} ${r.requestedUnit}/kiện`
                    : `BẮT BUỘC nguyên kiện — ${r.requestedPackagingSize} ${r.requestedUnit}/kiện, đơn lẻ hơn sẽ bị làm tròn lên`})
                </span>
              )}
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

      <Dialog open={!!rejecting} onOpenChange={(o) => !o && setRejecting(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              Từ chối đề xuất — {rejecting?.partner ?? rejecting?.partnerId} ·{" "}
              {rejecting?.ingredientName ?? rejecting?.ingredientId}
            </DialogTitle>
            <DialogDescription>Lý do từ chối bắt buộc để nhà cung cấp biết cần sửa gì.</DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5">
            <Label htmlFor="ccr-note">Lý do từ chối *</Label>
            <Textarea id="ccr-note" rows={4} value={note} onChange={(e) => setNote(e.target.value)} />
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
    </>
  );
}

const columns: ColumnDef<PartnerDto, unknown>[] = [
  { accessorKey: "name", header: "Đối tác", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  {
    accessorKey: "type",
    header: "Loại",
    cell: ({ row }) => (
      <Badge variant={row.original.type === "SERVICE_CATERER" ? "primary" : "info"}>
        {labelOf(PARTNER_TYPE_LABEL, row.original.type)}
      </Badge>
    ),
  },
  {
    accessorKey: "status",
    header: "Trạng thái (BR-041)",
    cell: ({ row }) => <StatusBadge status={row.original.status} />,
  },
  {
    id: "cert",
    header: "Chứng nhận ATTP (BR-047)",
    cell: ({ row }) => (
      <div className="flex items-center gap-2">
        <span className="text-sm">{formatVnDate(row.original.certExpiry)}</span>
        <StatusBadge status={row.original.certStatus} />
      </div>
    ),
  },
  {
    accessorKey: "performanceScore",
    header: "Điểm hiệu suất",
    cell: ({ row }) => {
      // optional trên DTO — chưa chấm điểm thì hiện "—", không phải "0% + cảnh báo".
      const s = row.original.performanceScore ?? 0;
      if (s <= 0) return "—";
      return (
        <span className="flex items-center gap-1.5">
          <span className={s < 70 ? "font-semibold text-destructive" : ""}>{s}%</span>
          {s < 70 && <Badge variant="warning">Performance Warning</Badge>}
        </span>
      );
    },
  },
  { accessorKey: "appliedAt", header: "Ngày nộp hồ sơ", cell: ({ row }) => formatVnDate(row.original.appliedAt) },
  { id: "actions", header: "", cell: ({ row }) => <PartnerActions partner={row.original} /> },
];

const TYPE_FILTER = ["Tất cả", "RAW_FOOD_SUPPLIER", "SERVICE_CATERER"] as const;
const STATUS_FILTER = ["Tất cả", "PENDING", "APPROVED", "REJECTED", "SUSPENDED"];

export default function PartnershipsPage() {
  const [type, setType] = useState<(typeof TYPE_FILTER)[number]>("Tất cả");
  const [status, setStatus] = useState("Tất cả");
  const filters: Record<string, string> = {};
  if (type !== "Tất cả") filters.type = type;
  if (status !== "Tất cả") filters.status = status;
  return (
    <>
      <PageHeader
        title="Yêu cầu hợp tác"
        description="Bấm từng dòng để xem hồ sơ đầy đủ + giấy tờ xác minh (UC-148/108, BR-040); chỉ đối tác Approved mới được giao nguyên liệu (BR-041); từ chối bắt buộc ghi lý do (BR-048)"
      />
      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="pf-type">Loại đối tác</Label>
          <Select id="pf-type" value={type} onChange={(e) => setType(e.target.value as typeof type)}>
            {TYPE_FILTER.map((x) => (
              <option key={x} value={x}>{x === "Tất cả" ? x : labelOf(PARTNER_TYPE_LABEL, x)}</option>
            ))}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="pf-status">Trạng thái (BR-041)</Label>
          <Select id="pf-status" value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUS_FILTER.map((x) => <option key={x}>{x}</option>)}
          </Select>
        </div>
      </div>
      <DataTable<PartnerDto>
        resource="partners"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên đối tác…"
        rowHref={(p) => `/school-admin/partnerships/${p.id}`}
      />

      <CatalogChangeRequestsPanel />

      {/*
       * Bảng "Lịch sử phê duyệt đối tác" (audit-logs, resource=partners) đã BỎ 13/08/2026
       * theo Nam. Nó không phải nguồn duy nhất của thông tin đó: trạng thái hiện thời nằm ở
       * cột "Trạng thái (BR-041)" ngay bảng trên, còn diễn biến từng hồ sơ (ai duyệt, lý do
       * từ chối) thì mở chi tiết `school-admin/partnerships/{id}` là thấy — đặt thêm một
       * bảng audit thô ở cuối trang chỉ kéo dài trang mà không trả lời thêm câu hỏi nào.
       * Audit log vẫn được ghi nguyên vẹn phía backend (BR-162), chỉ là không render ở đây.
       */}
    </>
  );
}
