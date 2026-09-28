"use client";

import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import type { PurchaseRequestDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

/**
 * Screen Spec 09/10 — Purchase Requests / Purchase Request List (UC-139).
 * BR-041: chỉ NCC Approved được giao hàng · BR-077: đơn phát sinh từ cảnh báo thiếu kho .
 * Chọn 1 dòng → Purchase Request Detail (Accept/Reject + Confirm Delivery Schedule, UC-015).
 *
 * Nối backend thật 05/08/2026: `PurchaseRequestResponse` ban đầu THIẾU `school` (Supplier
 * phục vụ nhiều trường, cần biết giao cho ai) — vá thêm ở backend (D1). `requestedBy` vẫn
 * không có (không cần thiết, đơn đã scope theo trường qua JWT). `items` là mảng chi tiết,
 * không phải số đếm — đếm `.length`.
 */

const columns: ColumnDef<PurchaseRequestDto, unknown>[] = [
  { accessorKey: "id", header: "Mã đơn", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    accessorKey: "school",
    header: "Trường",
    cell: ({ row }) => <span className="max-w-56 truncate">{row.original.school ?? "—"}</span>,
  },
  {
    accessorKey: "requestedDate",
    header: "Ngày yêu cầu",
    cell: ({ row }) => formatVnDate(row.original.requestedDate),
  },
  {
    accessorKey: "deliveryDate",
    header: "Ngày giao yêu cầu",
    cell: ({ row }) => formatVnDate(row.original.deliveryDate),
  },
  {
    id: "items",
    header: "Mặt hàng",
    cell: ({ row }) => `${(row.original.items ?? []).length} mặt hàng`,
  },
  {
    accessorKey: "totalValue",
    header: "Giá trị",
    cell: ({ row }) => (row.original.totalValue ?? 0).toLocaleString("vi-VN"),
  },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
  },
];

export default function PurchaseRequestsPage() {
  return (
    <>
      <PageHeader
        title="Yêu cầu mua hàng"
        description="Đơn từ Service Caterer — giá đã tự tính theo hợp đồng ngay lúc tạo đơn, không đàm phán → NCC Chấp nhận đơn & gán lô hoặc Từ chối; chỉ NCC Approved mới được giao (BR-041)"
      />
      <DataTable<PurchaseRequestDto>
        resource="purchase-requests"
        columns={columns}
        searchPlaceholder="Tìm theo mã đơn…"
        rowHref={(pr) => `/supplier/purchase-requests/${pr.id}`}
      />
    </>
  );
}
