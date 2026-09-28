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
import { useUpdate } from "@/lib/api/hooks";
import type { Partner } from "@/lib/mock-data";

/**
 * 02/03_PartnershipRequests.md — MF05 (School Administration).
 * BR-040: duyệt hồ sơ đầy đủ · BR-041: Pending → Approved/Rejected ·
 * BR-048: từ chối bắt buộc ghi lý do · MSG-037 khi duyệt thành công.
 * Chi tiết hồ sơ + giấy tờ xác minh: bấm dòng → /school-admin/partnerships/[id].
 */

function PartnerActions({ partner }: { partner: Partner }) {
  const update = useUpdate<{ id: string; status?: string; rejectReason?: string }>("partners");
  const [rejectOpen, setRejectOpen] = useState(false);
  const [reason, setReason] = useState("");

  if (partner.status !== "PENDING") return null;

  return (
    <div className="flex gap-1.5" onClick={(e) => e.stopPropagation()}>
      <Button
        size="sm"
        disabled={update.isPending}
        onClick={() =>
          update.mutate(
            { id: partner.id, status: "APPROVED" },
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
              disabled={!reason.trim() || update.isPending}
              onClick={() =>
                update.mutate(
                  { id: partner.id, status: "REJECTED", rejectReason: reason },
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

const columns: ColumnDef<Partner, unknown>[] = [
  { accessorKey: "name", header: "Đối tác", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  {
    accessorKey: "type",
    header: "Loại",
    cell: ({ row }) => (
      <Badge variant={row.original.type === "Service Caterer" ? "primary" : "info"}>
        {row.original.type}
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
        <span className="text-sm">{row.original.certExpiry}</span>
        <StatusBadge status={row.original.certStatus} />
      </div>
    ),
  },
  {
    accessorKey: "performanceScore",
    header: "Điểm hiệu suất",
    cell: ({ row }) => {
      const s = row.original.performanceScore;
      if (s <= 0) return "—";
      return (
        <span className="flex items-center gap-1.5">
          <span className={s < 70 ? "font-semibold text-destructive" : ""}>{s}%</span>
          {s < 70 && <Badge variant="warning">Performance Warning</Badge>}
        </span>
      );
    },
  },
  { accessorKey: "appliedAt", header: "Ngày nộp hồ sơ" },
  { id: "actions", header: "", cell: ({ row }) => <PartnerActions partner={row.original} /> },
];

const TYPE_FILTER = ["Tất cả", "Supplier", "Service Caterer"];
const STATUS_FILTER = ["Tất cả", "PENDING", "APPROVED", "REJECTED", "SUSPENDED"];

export default function PartnershipsPage() {
  const [type, setType] = useState("Tất cả");
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
          <Select id="pf-type" value={type} onChange={(e) => setType(e.target.value)}>
            {TYPE_FILTER.map((x) => <option key={x}>{x}</option>)}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="pf-status">Trạng thái (BR-041)</Label>
          <Select id="pf-status" value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUS_FILTER.map((x) => <option key={x}>{x}</option>)}
          </Select>
        </div>
      </div>
      <DataTable<Partner>
        resource="partners"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên đối tác…"
        rowHref={(p) => `/school-admin/partnerships/${p.id}`}
      />

      <h2 className="mb-3 mt-8 text-lg font-semibold">
        Lịch sử phê duyệt đối tác (UC-106 — Partnership Approval History)
      </h2>
      <p className="mb-3 text-sm text-muted-foreground">
        Trích từ audit log bất biến (BR-162), lọc Resource = Supplier Management — phạm vi trường mình (BR-176).
      </p>
      <DataTable<{ id: string; timestamp: string; actor: string; action: string; result: string }>
        resource="audit-logs"
        columns={[
          { accessorKey: "timestamp", header: "Thời điểm" },
          { accessorKey: "actor", header: "Người thực hiện" },
          { accessorKey: "action", header: "Hành động" },
        ]}
        filters={{ resource: "Supplier Management" }}
        pageSize={5}
        searchPlaceholder="Tìm trong lịch sử phê duyệt…"
        emptyMessage="Chưa có bản ghi phê duyệt đối tác nào."
      />
    </>
  );
}
