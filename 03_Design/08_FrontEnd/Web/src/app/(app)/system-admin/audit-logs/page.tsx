"use client";

import { useState } from "react";
import { Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import type { AuditLog } from "@/lib/mock-data";

/**
 * UC-View Audit Logs — MF12 (System Admin).
 * BR-162/BR-172: audit trail bất biến, không thể sửa/xóa · BR-170: ghi mọi hành
 * động quan trọng · BR-174: truy vấn lịch sử vận hành.
 */

const columns: ColumnDef<AuditLog, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "timestamp", header: "Thời điểm" },
  { accessorKey: "actor", header: "Người thực hiện" },
  {
    accessorKey: "ip",
    header: "Địa chỉ IP",
    cell: ({ row }) => <code className="text-xs">{row.original.ip ?? "—"}</code>,
  },
  { accessorKey: "action", header: "Hành động", cell: ({ row }) => <span className="max-w-96 truncate">{row.original.action}</span> },
  { accessorKey: "resource", header: "Tài nguyên" },
  {
    accessorKey: "result",
    header: "Kết quả",
    cell: ({ row }) =>
      row.original.result === "SUCCESS" ? (
        <Badge variant="success">Thành công</Badge>
      ) : (
        <Badge variant="danger">Bị chặn</Badge>
      ),
  },
];

const RESULT_FILTER = ["Tất cả", "SUCCESS"];

export default function AuditLogsPage() {
  const [ip, setIp] = useState("");
  const [result, setResult] = useState("Tất cả");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const filters: Record<string, string> = {};
  if (ip.trim()) filters.ip = ip.trim();
  if (result !== "Tất cả") filters.result = result;
  return (
    <>
      <PageHeader
        title="Nhật ký kiểm toán"
        description="Nhật ký bất biến (BR-162) — mọi thao tác mock (tạo/cập nhật/duyệt) trong phiên này cũng được ghi tự động vào đây (BR-170)"
        actions={
          <Button variant="outline" onClick={() => toast.info("Xuất nhật ký theo Log Retention Policy (BR-171)")}>
            <Download /> Xuất nhật ký
          </Button>
        }
      />
      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="al-from">Từ ngày (BR-171)</Label>
          <Input id="al-from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="al-to">Đến ngày</Label>
          <Input id="al-to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="al-ip">Địa chỉ IP</Label>
          <Input id="al-ip" value={ip} onChange={(e) => setIp(e.target.value)} placeholder="VD: 10.20.0.1" />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="al-result">Kết quả</Label>
          <Select id="al-result" value={result} onChange={(e) => setResult(e.target.value)}>
            {RESULT_FILTER.map((r) => <option key={r}>{r}</option>)}
          </Select>
        </div>
      </div>
      <DataTable<AuditLog>
        resource="audit-logs"
        columns={columns}
        filters={filters}
        pageSize={10}
        searchPlaceholder="Tìm theo người thực hiện, hành động, tài nguyên…"
      />
    </>
  );
}
