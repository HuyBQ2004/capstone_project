"use client";

import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import type { IncidentCaterersDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/**
 * UC View Incident Information — MF11 (Service Caterers).
 * BR-149: sự cố → tự cách ly lô nghi vấn · BR-148: cảnh báo ngay tới catering.
 *
 * D11/PDPD — Service Caterers nhận `IncidentCaterersResponse` (rút gọn): KHÔNG có
 * `severity` (đánh giá lâm sàng không lộ ra ngoài trường), chỉ đếm `affectedStudentCount`
 * thay vì tên/lớp học sinh.
 */

const columns: ColumnDef<IncidentCaterersDto, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "title", header: "Sự cố", cell: ({ row }) => <span className="max-w-80 truncate">{row.original.title}</span> },
  { id: "reportedAt", header: "Thời điểm", cell: ({ row }) => formatVnDateTime(row.original.reportedAt) },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
];

export default function CatererIncidentsPage() {
  return (
    <>
      <PageHeader
        title="Sự cố liên quan"
        description="Khi có sự cố, lô thức ăn và nguyên liệu nghi vấn tự động chuyển Quarantine (BR-149); phối hợp CAPA với nhà trường (BR-152)"
      />
      <DataTable<IncidentCaterersDto>
        resource="incidents"
        columns={columns}
        searchPlaceholder="Tìm sự cố…"
        rowHref={(i) => `/service-caterers/incidents/${i.id}`}
      />
    </>
  );
}
