"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import { Label, Select } from "@/components/ui/input";
import type { OverrideAbuseRow } from "@/lib/mock-data";

/**
 * 49_WeeklyOverrideAbuseReport.md — UC-183 (View Weekly Override Abuse Report).
 * BR-190: báo cáo tự động biên soạn hàng tuần từ StaffViolationRecord
 * (violationType = OVERRIDE_ABUSE); drill down → Violation Registry.
 */

const WEEKS = ["Tất cả", "Tuần 27 (29/06–05/07)", "Tuần 26 (22/06–28/06)"];

const columns: ColumnDef<OverrideAbuseRow, unknown>[] = [
  { accessorKey: "week", header: "Tuần" },
  { accessorKey: "staffName", header: "Nhân viên", cell: ({ row }) => <span className="font-medium">{row.original.staffName}</span> },
  { accessorKey: "staffRole", header: "Vai trò" },
  {
    accessorKey: "overrideCount",
    header: "Số lần override / ngưỡng",
    cell: ({ row }) => (
      <span className={row.original.overrideCount > row.original.threshold ? "font-semibold text-destructive" : ""}>
        {row.original.overrideCount} / {row.original.threshold}
      </span>
    ),
  },
  { accessorKey: "context", header: "Bối cảnh", cell: ({ row }) => <span className="max-w-96 truncate">{row.original.context}</span> },
  {
    accessorKey: "severity",
    header: "Mức độ",
    cell: ({ row }) => <StatusBadge status={row.original.severity} />,
  },
  {
    id: "drill",
    header: "Biên bản (BR-190)",
    cell: ({ row }) =>
      row.original.violationId ? (
        <Link
          href="/school-admin/violations"
          className="text-sm font-medium text-primary underline"
          onClick={(e) => e.stopPropagation()}
        >
          {row.original.violationId} →
        </Link>
      ) : (
        <Badge variant="neutral">Dưới ngưỡng</Badge>
      ),
  },
];

export default function OverrideAbuseReportPage() {
  const [week, setWeek] = useState("Tất cả");
  return (
    <>
      <PageHeader
        title="Báo cáo lạm dụng Override hàng tuần"
        description="UC-183 — tổng hợp tự động từ biên bản vi phạm nhân sự loại OVERRIDE_ABUSE (BR-190); vượt ngưỡng sẽ tự lập biên bản"
        actions={
          <>
            <Link href="/school-admin/violations">
              <Button variant="outline">
                <ArrowLeft /> Violation Registry
              </Button>
            </Link>
            <Button
              variant="outline"
              onClick={() => toast.info("Xuất CSV/PDF báo cáo override abuse (AF1 UC-183, BR-178)")}
            >
              <Download /> Export
            </Button>
          </>
        }
      />

      <Alert variant="info" className="mb-4" title="Ngưỡng cảnh báo (BR-190)">
        Nhân viên vượt quá 3 lần manual override/tuần sẽ bị hệ thống tự động lập biên
        bản vi phạm nhân sự và đưa vào Violation Registry chờ xử lý.
      </Alert>

      <div className="mb-4 flex items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="ovr-week">Chọn tuần</Label>
          <Select id="ovr-week" value={week} onChange={(e) => setWeek(e.target.value)}>
            {WEEKS.map((w) => (
              <option key={w}>{w}</option>
            ))}
          </Select>
        </div>
      </div>

      <DataTable<OverrideAbuseRow>
        resource="override-abuse"
        columns={columns}
        filters={week === "Tất cả" ? undefined : { week }}
        searchPlaceholder="Tìm theo nhân viên, bối cảnh…"
        emptyMessage="Không có trường hợp lạm dụng override nào trong tuần này."
      />
    </>
  );
}
