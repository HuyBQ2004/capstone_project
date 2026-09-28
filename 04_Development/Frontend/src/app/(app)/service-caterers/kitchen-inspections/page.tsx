"use client";

/**
 * KIỂM TRA AN TOÀN BẾP (UC-014) — LỊCH SỬ, xem cho SERVICE_CATERERS.
 *
 * Thêm 10/08/2026 (Nam) — trước đây backend đã mở quyền đọc cho SERVICE_CATERERS
 * (`KitchenInspectionController` `@PreAuthorize hasAnyRole(...,'SERVICE_CATERERS')`) nhưng
 * KHÔNG có route/trang nào ở phía Caterers, nên vai trò này chưa từng xem được Y tế đã kiểm
 * tra bếp những gì hay biên bản ghi gì — chỉ biết qua thông báo. Trang này CHỈ ĐỌC — mọi thao
 * tác ghi (điền checklist, sửa biên bản) vẫn thuộc riêng Medical Staff; Caterers chỉ có thêm
 * nút "Xác nhận kết quả kiểm tra" ở trang chi tiết (BR-221).
 */

import Link from "next/link";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { KitchenConfirmBadge } from "@/components/app/kitchen-confirm-badge";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { KitchenInspectionDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

export default function CatererKitchenInspectionHistoryPage() {
  const columns: ColumnDef<KitchenInspectionDto, unknown>[] = [
    { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { id: "date", header: "Ngày", cell: ({ row }) => formatVnDate(row.original.date) },
    {
      accessorKey: "score",
      header: "Điểm",
      cell: ({ row }) => (
        <span title="BR-104" className={(row.original.score ?? 0) < 60 ? "font-semibold text-destructive" : ""}>
          {row.original.score ?? 0}%
        </span>
      ),
    },
    { accessorKey: "inspector", header: "Người kiểm tra (Y tế)" },
    {
      accessorKey: "result",
      header: "Kết quả",
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          <StatusBadge status={row.original.result ?? ""} />
          {(row.original.score ?? 0) < 60 && <Badge variant="warning">Đã cảnh báo SA</Badge>}
        </div>
      ),
    },
    {
      id: "confirm",
      header: "Xác nhận",
      cell: ({ row }) => <KitchenConfirmBadge inspection={row.original} />,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div onClick={(e) => e.stopPropagation()}>
          <Link href={`/service-caterers/kitchen-inspections/${row.original.id}`}>
            <Button size="sm" variant="ghost">Chi tiết</Button>
          </Link>
        </div>
      ),
    },
  ];

  return (
    <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title="Kiểm tra an toàn bếp (UC-014)"
        description="Lịch sử các phiên kiểm tra bếp do Y tế trường học thực hiện — xem checklist đã tick, biên bản sự việc, và xác nhận kết quả (BR-221)"
      />
      <DataTable<KitchenInspectionDto>
        resource="kitchen-inspections"
        columns={columns}
        searchPlaceholder="Tìm theo mã, người kiểm tra…"
        emptyMessage="Chưa có phiên kiểm tra bếp nào."
        rowHref={(row) => `/service-caterers/kitchen-inspections/${row.id}`}
      />
    </div>
  );
}
