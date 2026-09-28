"use client";

import Link from "next/link";
import { Eye, Plus } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { CURRENT_SCHOOL, type PurchaseRequest } from "@/lib/mock-data";

/**
 * Đặt hàng nguyên liệu (đề xuất Minh 07/2026) — Service Caterers tạo đơn.
 * Review 25/07/2026 — giá theo HỢP ĐỒNG, duyệt 1 vòng: PENDING → NCC gửi giá
 * tự tính từ bảng giá (QUOTED) → SC chấp nhận (PRICE_ACCEPTED) hoặc từ chối
 * (CANCELLED) → NCC xác nhận + gán lô (BR-071) → giao hàng → khi Delivery
 * DELIVERED mới mở được phiên nhận hàng (không tự nhập tay).
 * Dùng chung resource purchase-requests với phía Supplier (UC-139/UC-015).
 */

const columns: ColumnDef<PurchaseRequest, unknown>[] = [
  { accessorKey: "id", header: "Mã đơn", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    accessorKey: "supplier",
    header: "Nhà cung cấp",
    cell: ({ row }) => <span className="max-w-56 truncate">{row.original.supplier ?? "—"}</span>,
  },
  { accessorKey: "requestedDate", header: "Ngày đặt" },
  { accessorKey: "deliveryDate", header: "Ngày giao yêu cầu" },
  { accessorKey: "items", header: "Mặt hàng" },
  { accessorKey: "totalValue", header: "Giá trị dự kiến" },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => (
      <StatusBadge
        status={row.original.status}
        label={row.original.status === "PRICE_ACCEPTED" ? "Đã chốt giá — chờ giao" : undefined}
      />
    ),
  },
  {
    id: "actions",
    header: "",
    cell: ({ row }) => (
      <div className="flex justify-end">
        <Link href={`/service-caterers/orders/${row.original.id}`}>
          <Button variant="ghost" size="sm">
            <Eye /> Xem chi tiết
          </Button>
        </Link>
      </div>
    ),
  },
];

export default function CatererOrdersPage() {
  return (
    <>
      <PageHeader
        title="Đặt hàng nguyên liệu"
        description="Gửi đơn → NCC gửi giá theo hợp đồng (không đàm phán) → duyệt giá (chấp nhận hoặc từ chối đơn kèm lý do) → NCC gán lô (BR-071) & xác nhận lịch giao; hàng giao tới (Đã giao) mới mở được phiên nhận"
        actions={
          <Link href="/service-caterers/orders/new">
            <Button>
              <Plus /> Tạo đơn đặt hàng
            </Button>
          </Link>
        }
      />
      <DataTable<PurchaseRequest>
        resource="purchase-requests"
        columns={columns}
        filters={{ school: CURRENT_SCHOOL.shortName }}
        searchPlaceholder="Tìm theo mã đơn, nhà cung cấp…"
      />
    </>
  );
}
