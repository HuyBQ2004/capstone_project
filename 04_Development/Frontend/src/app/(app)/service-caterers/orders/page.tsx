"use client";

import Link from "next/link";
import { Eye, Plus } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import type { PurchaseRequestDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

/**
 * Đặt hàng nguyên liệu (đề xuất Minh 07/2026) — Service Caterers tạo đơn.
 * Đơn giản hoá 10/08/2026 — giá theo HỢP ĐỒNG, tự tính ngay lúc tạo đơn, không
 * còn vòng NCC báo giá / SC chấp nhận giá: PENDING → NCC xác nhận + gán lô
 * (BR-071, APPROVED) hoặc từ chối (CANCELLED/REJECTED) → giao hàng → khi
 * Delivery DELIVERED mới mở được phiên nhận hàng (không tự nhập tay).
 * Dùng chung resource purchase-requests với phía Supplier (UC-139/UC-015).
 *
 * Nối backend thật 05/08/2026: `PurchaseRequestResponse` đã scope theo trường qua JWT —
 * bỏ filter `school` (không tồn tại phía server, đơn của actor đã chỉ thuộc trường mình).
 */

const columns: ColumnDef<PurchaseRequestDto, unknown>[] = [
  { accessorKey: "id", header: "Mã đơn", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    accessorKey: "supplier",
    header: "Nhà cung cấp",
    cell: ({ row }) => <span className="max-w-56 truncate">{row.original.supplier ?? "—"}</span>,
  },
  {
    accessorKey: "requestedDate",
    header: "Ngày đặt",
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
    header: "Giá trị dự kiến",
    cell: ({ row }) => (row.original.totalValue ?? 0).toLocaleString("vi-VN"),
  },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
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
      <DataTable<PurchaseRequestDto>
        resource="purchase-requests"
        columns={columns}
        searchPlaceholder="Tìm theo mã đơn, nhà cung cấp…"
      />
    </>
  );
}
