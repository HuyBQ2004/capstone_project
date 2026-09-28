"use client";

import { useState } from "react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label, Select } from "@/components/ui/input";
import { formatVnDateTime } from "@/lib/format";
import type { ErrorLogDto } from "@/lib/api/types";

/**
 * 19_ErrorLogsScreen.md — UC-111 (View Error Logs).
 * Bảng lỗi theo severity/timestamp; chọn 1 dòng → panel chi tiết stack trace,
 * request metadata, session ảnh hưởng (BR-164: metadata nhạy cảm được che).
 */

function ErrorDetailButton({ e }: { e: ErrorLogDto }) {
  const [open, setOpen] = useState(false);
  return (
    <div onClick={(ev) => ev.stopPropagation()}>
      <Button variant="ghost" size="sm" onClick={() => setOpen(true)}>
        Chi tiết
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{e.id} — {e.message}</DialogTitle>
            <DialogDescription>
              {formatVnDateTime(e.timestamp)} · Module {e.module} · Path {e.path}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3 text-sm">
            <div>
              <p className="mb-1 font-semibold">Stack trace</p>
              <pre className="overflow-x-auto rounded-lg bg-muted p-3 text-xs">{e.stackTrace}</pre>
            </div>
            <div>
              <p className="mb-1 font-semibold">Request metadata (BR-164 — credential được che)</p>
              <pre className="overflow-x-auto rounded-lg bg-muted p-3 text-xs">{`path: ${e.path}\nauthorization: ***encrypted***`}</pre>
            </div>
            <div>
              <p className="mb-1 font-semibold">Phiên bị ảnh hưởng</p>
              <p className="text-muted-foreground">{e.sessionInfo}</p>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

const columns: ColumnDef<ErrorLogDto, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    accessorKey: "timestamp",
    header: "Thời điểm",
    cell: ({ row }) => formatVnDateTime(row.original.timestamp),
  },
  { accessorKey: "message", header: "Thông điệp lỗi", cell: ({ row }) => <span className="max-w-96 truncate">{row.original.message}</span> },
  {
    accessorKey: "severity",
    header: "Mức độ",
    cell: ({ row }) => <StatusBadge status={row.original.severity ?? ""} />,
  },
  { accessorKey: "module", header: "Module/Lớp" },
  { accessorKey: "path", header: "Đường dẫn", cell: ({ row }) => <code className="text-xs">{row.original.path}</code> },
  { id: "detail", header: "", cell: ({ row }) => <ErrorDetailButton e={row.original} /> },
];

const SEVERITIES = ["Tất cả", "CRITICAL", "HIGH", "MEDIUM", "LOW"];

export default function ErrorLogsPage() {
  const [severity, setSeverity] = useState("Tất cả");
  return (
    <>
      <PageHeader
        title="Nhật ký lỗi hệ thống"
        description="UC-111 — log lỗi theo severity/timestamp; chọn 1 lỗi để xem stack trace, request metadata và phiên ảnh hưởng (BR-164)"
      />
      <div className="mb-4 flex items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="el-sev">Lọc theo Severity</Label>
          <Select id="el-sev" value={severity} onChange={(e) => setSeverity(e.target.value)}>
            {SEVERITIES.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </Select>
        </div>
      </div>
      <DataTable<ErrorLogDto>
        resource="error-logs"
        columns={columns}
        filters={severity === "Tất cả" ? undefined : { severity }}
        searchPlaceholder="Tìm theo thông điệp lỗi, module…"
        emptyMessage="Không có lỗi nào trong khoảng thời gian đã chọn."
      />
    </>
  );
}
