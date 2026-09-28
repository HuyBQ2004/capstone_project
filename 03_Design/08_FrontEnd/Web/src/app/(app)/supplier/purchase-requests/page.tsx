"use client";

import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import type { PurchaseRequest } from "@/lib/mock-data";

/**
 * Screen Spec 09/10 — Purchase Requests / Purchase Request List (UC-139).
 * BR-041: chỉ NCC Approved được giao hàng · BR-077: đơn phát sinh từ cảnh báo thiếu kho .
 * Chọn 1 dòng → Purchase Request Detail (Accept/Reject + Confirm Delivery Schedule, UC-015).
 */

const columns: ColumnDef<PurchaseRequest, unknown>[] = [
  { accessorKey: "id", header: "Mã đơn", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "school", header: "Trường" },
  { accessorKey: "requestedBy", header: "Nguồn yêu cầu" },
  { accessorKey: "requestedDate", header: "Ngày yêu cầu" },
  { accessorKey: "deliveryDate", header: "Ngày giao yêu cầu" },
  { accessorKey: "items", header: "Mặt hàng" },
  { accessorKey: "totalValue", header: "Giá trị" },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => (
      <StatusBadge
        status={row.original.status}
        label={row.original.status === "PRICE_ACCEPTED" ? "Đã chốt giá — cần giao" : undefined}
      />
    ),
  },
];

export default function PurchaseRequestsPage() {
  return (
    <>
      <PageHeader
        title="Yêu cầu mua hàng"
        description="Đơn từ Service Caterer — gửi giá theo hợp đồng (tự tính từ bảng giá, không đàm phán) → SC chấp nhận hoặc từ chối → chốt giá mới Chấp nhận đơn & lịch giao; chỉ NCC Approved mới được giao (BR-041)"
      />
      <DataTable<PurchaseRequest>
        resource="purchase-requests"
        columns={columns}
        searchPlaceholder="Tìm theo trường, mã đơn…"
        rowHref={(pr) => `/supplier/purchase-requests/${pr.id}`}
      />
    </>
  );
}
