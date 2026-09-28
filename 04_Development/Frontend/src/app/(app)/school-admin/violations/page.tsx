"use client";

import { useState } from "react";
import Link from "next/link";
import { AlertTriangle, BarChart3 } from "lucide-react";
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
import { useAction } from "@/lib/api/hooks";
import type { ViolationDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { VIOLATION_TYPES } from "@/lib/violation-utils";

/**
 * 47_ViolationRegistry.md + 48_ApproveRejectViolationResolution.md
 * §18 Violation Management (BR-183..BR-190).
 * BR-186: MEDIUM/HIGH bắt buộc BGH phê duyệt; Reject bắt buộc ghi lý do (AF1) ·
 * BR-187: cảnh báo tái diễn (best-effort) · BR-189: registry tổng hợp mọi loại.
 * Bấm từng dòng → chi tiết biên bản (46) /school-admin/violations/[id].
 */

function RejectDialog({ v }: { v: ViolationDto }) {
  const doAction = useAction<ViolationDto>("violations");
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState("");

  return (
    <div onClick={(e) => e.stopPropagation()}>
      <Button variant="destructive" size="sm" onClick={() => setOpen(true)}>
        Từ chối
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Từ chối biên bản xử lý — {v.id}</DialogTitle>
            <DialogDescription>
              Lý do từ chối là bắt buộc (AF1, BR-186) và sẽ được gửi trả Medical Staff để chỉnh sửa lại biên bản.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5">
            <Label htmlFor="vr-reason">Lý do từ chối *</Label>
            <Textarea
              id="vr-reason"
              rows={4}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="VD: Hành động xử lý chưa tương xứng mức độ vi phạm; bổ sung bằng chứng huấn luyện lại…"
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={!reason.trim() || doAction.isPending}
              onClick={() =>
                doAction.mutate(
                  { id: v.id!, action: "resolution/reject", body: { reason } },
                  {
                    onSuccess: () => {
                      toast.info("Đã từ chối biên bản xử lý — trả về Medical Staff kèm lý do (BR-186)");
                      setOpen(false);
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

function ResolutionActions({ v }: { v: ViolationDto }) {
  const doAction = useAction<ViolationDto>("violations");
  if (v.resolution !== "PENDING_APPROVAL") return null;
  return (
    <div className="flex gap-1.5" onClick={(e) => e.stopPropagation()}>
      <Button
        size="sm"
        disabled={doAction.isPending}
        onClick={() =>
          doAction.mutate(
            { id: v.id!, action: "resolution/approve" },
            {
              onSuccess: () =>
                toast.success("Đã phê duyệt biên bản xử lý. Áp dụng hình thức kỷ luật. (MSG-089)"),
            }
          )
        }
      >
        Phê duyệt
      </Button>
      <RejectDialog v={v} />
    </div>
  );
}

const columns: ColumnDef<ViolationDto, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    accessorKey: "subject",
    header: "Nội dung",
    cell: ({ row }) => (
      <span className="flex items-center gap-1.5">
        <span className="max-w-72 truncate">{row.original.subject}</span>
        {row.original.repeatSuspect && (
          <Badge variant="danger" title="Nghi ngờ tái diễn trong 90 ngày (BR-187, best-effort)">
            <AlertTriangle className="mr-1 size-3" /> Tái diễn?
          </Badge>
        )}
      </span>
    ),
  },
  { accessorKey: "recordedByLabel", header: "Người lập" },
  { id: "date", header: "Ngày", cell: ({ row }) => formatVnDate(row.original.date) },
  {
    accessorKey: "severity",
    header: "Mức độ",
    cell: ({ row }) => <StatusBadge status={row.original.severity ?? ""} />,
  },
  {
    accessorKey: "resolution",
    header: "Biên bản xử lý (BR-185)",
    cell: ({ row }) => <StatusBadge status={row.original.resolution ?? ""} />,
  },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
  },
  { id: "actions", header: "", cell: ({ row }) => <ResolutionActions v={row.original} /> },
];

const SEVERITIES = ["Tất cả", "HIGH", "MEDIUM", "LOW"];
const RESOLUTIONS = ["Tất cả", "PENDING_APPROVAL", "APPROVED", "REJECTED"];

export default function ViolationsPage() {
  const [type, setType] = useState("Tất cả");
  const [severity, setSeverity] = useState("Tất cả");
  const [resolution, setResolution] = useState("Tất cả");

  const filters: Record<string, string> = {};
  if (type !== "Tất cả") filters.violationType = type;
  if (severity !== "Tất cả") filters.severity = severity;
  if (resolution !== "Tất cả") filters.resolution = resolution;

  return (
    <>
      <PageHeader
        title="Vi phạm & Xử lý sai phạm"
        description="Biên bản vi phạm bắt buộc có mô tả + ảnh bằng chứng (BR-183/BR-184, MSG-085); mức MEDIUM/HIGH bắt buộc BGH phê duyệt (BR-186)"
        actions={
          <Link href="/school-admin/violations/override-report">
            <Button variant="outline">
              <BarChart3 /> Báo cáo Override tuần (UC-183)
            </Button>
          </Link>
        }
      />

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="vf-type">Nhóm hành vi</Label>
          <Select id="vf-type" value={type} onChange={(e) => setType(e.target.value)}>
            <option value="Tất cả">Tất cả</option>
            {VIOLATION_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="vf-sev">Mức độ</Label>
          <Select id="vf-sev" value={severity} onChange={(e) => setSeverity(e.target.value)}>
            {SEVERITIES.map((s) => <option key={s}>{s}</option>)}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="vf-res">Biên bản xử lý</Label>
          <Select id="vf-res" value={resolution} onChange={(e) => setResolution(e.target.value)}>
            {RESOLUTIONS.map((r) => <option key={r}>{r}</option>)}
          </Select>
        </div>
      </div>

      <DataTable<ViolationDto>
        resource="violations"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo nội dung, người lập…"
        rowHref={(v) => `/school-admin/violations/${v.id}`}
      />
    </>
  );
}
