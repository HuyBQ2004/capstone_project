"use client";

/**
 * SỔ KIỂM THỰC BA BƯỚC — School Admin, thêm 10/08/2026 (Nam).
 *
 * Backend đã mở quyền đọc cho SCHOOL_ADMIN từ trước (`InspectionController`
 * `@PreAuthorize hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')`)
 * nhưng chưa có route nào ở phía School Admin — `school-admin/compliance` chỉ hiện phiên
 * HÔM NAY dạng thẻ, không có sổ lịch sử. Trang này CHỈ ĐỌC — đúng dạng "sổ" theo QĐ
 * 1246/QĐ-BYT: liệt kê từng phiên, bấm vào xem đủ Mẫu 1/2/3 và in được (dùng chung
 * `lib/inspection-sheets.ts` với Medical Staff/Caterers — một khuôn mẫu biểu, không dựng
 * bản in riêng cho vai trò này).
 */

import Link from "next/link";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import type { InspectionSessionDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

export default function SchoolAdminInspectionsPage() {
  const columns: ColumnDef<InspectionSessionDto, unknown>[] = [
    { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { id: "date", header: "Ngày", cell: ({ row }) => formatVnDate(row.original.date) },
    { id: "meal", header: "Bữa", cell: ({ row }) => MEAL_TYPE_LABEL[(row.original.meal ?? "") as MealType] ?? row.original.meal },
    { accessorKey: "step1", header: "Bước 1", cell: ({ row }) => <StatusBadge status={row.original.step1 ?? ""} /> },
    { accessorKey: "step2", header: "Bước 2", cell: ({ row }) => <StatusBadge status={row.original.step2 ?? ""} /> },
    { accessorKey: "step3", header: "Bước 3", cell: ({ row }) => <StatusBadge status={row.original.step3 ?? ""} /> },
    { accessorKey: "inspector", header: "Người thực hiện" },
    { accessorKey: "overall", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.overall ?? ""} /> },
    {
      id: "detail",
      header: "",
      cell: ({ row }) => (
        <Link href={`/school-admin/inspections/${row.original.id}`} onClick={(e) => e.stopPropagation()}>
          <Button size="sm" variant="outline">Chi tiết</Button>
        </Link>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Sổ kiểm thực ba bước (QĐ 1246/QĐ-BYT)"
        description="Xem lại toàn bộ phiên kiểm thực ba bước của trường — chỉ đọc, in được theo Mẫu 1/2/3"
      />
      <DataTable<InspectionSessionDto>
        resource="inspections"
        columns={columns}
        searchPlaceholder="Tìm phiên kiểm thực…"
        rowHref={(row) => `/school-admin/inspections/${row.id}`}
        emptyMessage="Chưa có phiên kiểm thực nào."
      />
    </>
  );
}
