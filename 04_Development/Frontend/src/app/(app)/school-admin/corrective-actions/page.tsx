"use client";

import { useState } from "react";
import Link from "next/link";
import { Check, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
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
import { Label, Textarea } from "@/components/ui/input";
import { useAction } from "@/lib/api/hooks";
import type { CorrectiveActionDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/**
 * 11_CorrectiveActionReview.md — UC-005 (Approval/Reject Corrective Action).
 * BR-152: quản lý CAPA · BR-153: BGH phê duyệt kết quả khắc phục ·
 * Approve bắt buộc ghi chú xác minh; Reject bắt buộc lý do (EX1) + deadline
 * resubmit mới (AF1). Sau khi toàn bộ CAPA của sự cố Approved → đủ điều kiện
 * đóng sự cố tại Incident Detail (UC-013, BR-155).
 */

function ReviewDialog({ capa }: { capa: CorrectiveActionDto }) {
  const doAction = useAction<CorrectiveActionDto>("corrective-actions");
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState<"approve" | "reject">("approve");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");

  // BR-216: BGH duyệt KẾ HOẠCH khắc phục TRƯỚC khi thực hiện — nên chỉ xử lý CAPA
  // đang ở "Chờ BGH duyệt". (Trước đây gate là SUBMITTED/IN_PROGRESS và Approve ghi
  // thẳng COMPLETED — tức duyệt sau khi đã làm xong, sai thứ tự và chặn luôn bước
  // đánh giá hiệu quả của Y tế.)
  if (capa.status !== "PENDING_ADMIN_APPROVAL") return null;

  function submit(kind: "approve" | "reject") {
    setMode(kind);
    // EX1: thiếu ghi chú/lý do → chặn
    if (!note.trim()) {
      setError(
        kind === "approve"
          ? "Ghi chú xác minh là bắt buộc khi Approve (UC-005)."
          : "Lý do từ chối là bắt buộc (EX1 UC-005)."
      );
      return;
    }
    setError("");
    // `POST .../approve` không nhận body (CapaController) — "Ghi chú xác minh" chỉ còn
    // vai trò GATE nhập liệu (EX1/UC-005), không có cột nào phía backend lưu lại.
    doAction.mutate(
      kind === "approve"
        ? { id: capa.id!, action: "approve" }
        : // Từ chối → trả kế hoạch về DRAFT cho Y tế soạn lại (không phải ngõ cụt REJECTED)
          { id: capa.id!, action: "reject", body: { reason: note } },
      {
        onSuccess: () => {
          // Server tự đồng bộ Incident.status (M11-D21) + ghi sổ sự kiện
          // (IncidentEventLog) cùng transaction — không cần giả lập ở client nữa.
          toast.success(
            kind === "approve"
              ? "Đã duyệt kế hoạch khắc phục. (MSG-043) — đơn vị suất ăn có thể nhận việc và thực hiện."
              : "Đã từ chối kế hoạch — trả về Y tế trường soạn lại kèm lý do."
          );
          setOpen(false);
          setNote("");
        },
      }
    );
  }

  return (
    <div onClick={(e) => e.stopPropagation()}>
      <Button size="sm" onClick={() => setOpen(true)}>
        Duyệt kế hoạch (UC-005)
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Duyệt kế hoạch khắc phục — {capa.id}</DialogTitle>
            <DialogDescription>
              Nguồn: {capa.sourceLabel} · Đơn vị thực hiện: {capa.assigneeLabel}
              {capa.submittedForApprovalAt && <> · Y tế gửi {formatVnDateTime(capa.submittedForApprovalAt)}</>}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-3">
            <div className="rounded-lg border p-3 text-sm">
              <p className="mb-1 font-medium">Kế hoạch khắc phục do Y tế trường lập (BR-152)</p>
              <p className="text-muted-foreground">{capa.description}</p>
            </div>
            <Alert variant="info" title="Duyệt TRƯỚC khi thực hiện (BR-216)">
              Phê duyệt sẽ mở cho đơn vị suất ăn nhận việc và triển khai. Bằng chứng
              khắc phục sẽ do <b>Y tế trường</b> đánh giá hiệu quả ở bước sau (UC-168),
              không quay lại Ban giám hiệu.
            </Alert>
            {capa.rejectReason && (
              <Alert variant="warning" title="Đã từng bị từ chối">
                {capa.rejectReason}
              </Alert>
            )}
            {error && <Alert variant="danger" title="Thiếu thông tin">{error}</Alert>}
            <div className="space-y-1.5">
              <Label htmlFor="capa-note">
                {mode === "approve" ? "Ghi chú xác minh * (bắt buộc khi Approve)" : "Lý do từ chối * (EX1)"}
              </Label>
              <Textarea
                id="capa-note"
                rows={3}
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder={
                  mode === "approve"
                    ? "VD: Đã kiểm tra hiện trường, khu sơ chế đạt yêu cầu…"
                    : "VD: Ảnh bằng chứng chưa thể hiện khu vực kho khô…"
                }
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={doAction.isPending}
              onClick={() => submit("reject")}
            >
              <X /> Từ chối kế hoạch
            </Button>
            <Button disabled={doAction.isPending} onClick={() => submit("approve")}>
              <Check /> Duyệt kế hoạch (MSG-043)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

const columns: ColumnDef<CorrectiveActionDto, unknown>[] = [
  { accessorKey: "id", header: "Mã CAPA", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    id: "source",
    header: "Nguồn phát sinh",
    // Có khoá sự cố thì mở thẳng hồ sơ sự cố — trước đây chỉ hiện chuỗi, BGH phải
    // tự tra mã. CAPA nguồn kiểm thực (BR-095) không có sự cố nên chỉ hiện nhãn.
    cell: ({ row }) =>
      row.original.incidentId ? (
        <Link
          className="underline"
          href={`/school-admin/incidents/${row.original.incidentId}`}
          onClick={(e: React.MouseEvent) => e.stopPropagation()}
        >
          {row.original.sourceLabel}
        </Link>
      ) : (
        <span className="text-muted-foreground">{row.original.sourceLabel}</span>
      ),
  },
  { accessorKey: "description", header: "Nội dung", cell: ({ row }) => <span className="max-w-96 truncate">{row.original.description}</span> },
  { accessorKey: "assigneeLabel", header: "Đơn vị thực hiện" },
  {
    id: "timeline",
    header: "Mốc thời gian",
    cell: ({ row }) => {
      const c = row.original;
      return (
        <span className="block text-xs text-muted-foreground">
          {c.submittedForApprovalAt && <>Gửi duyệt {formatVnDateTime(c.submittedForApprovalAt)}</>}
          {c.approvedAt && <span className="block">Duyệt {formatVnDateTime(c.approvedAt)}</span>}
          {c.evidenceSubmittedAt && <span className="block">Nộp BC {formatVnDateTime(c.evidenceSubmittedAt)}</span>}
          {!c.submittedForApprovalAt && !c.approvedAt && "—"}
        </span>
      );
    },
  },
  {
    accessorKey: "status",
    header: "Trạng thái (BR-152)",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
  },
  { id: "actions", header: "", cell: ({ row }) => <ReviewDialog capa={row.original} /> },
];

export default function CorrectiveActionsReviewPage() {
  return (
    <>
      <PageHeader
        title="Duyệt hành động khắc phục (CAPA)"
        description="UC-005 — BGH xem báo cáo thực hiện + bằng chứng của đơn vị suất ăn rồi Approve/Reject (BR-153); sự cố chỉ đóng được khi toàn bộ CAPA hoàn thành (BR-155)"
      />
      {/* Bấm dòng để mở hồ sơ đầy đủ (backlog B6) — trước đây vai này KHÔNG có màn chi tiết
          nào, quyết định duyệt/nhận việc ra ngay trên dialog của danh sách. */}
      <DataTable<CorrectiveActionDto>
        resource="corrective-actions"
        columns={columns}
        rowHref={(r) => `/school-admin/corrective-actions/${r.id}`}
        searchPlaceholder="Tìm theo mã, nguồn, người thực hiện…"
      />
    </>
  );
}
