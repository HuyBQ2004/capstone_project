"use client";

import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Badge } from "@/components/ui/badge";
import { MEAL_TYPE_LABEL, type InventoryTransaction } from "@/lib/mock-data";

/**
 * Record Inventory Transactions Log — MF06 (Service Caterers).
 * Sổ nhập/xuất/điều chỉnh append-only; điều chỉnh thủ công kèm mã lý do +
 * chữ ký quản lý (BR-204); xuất kho theo FEFO (BR-075).
 */

const typeBadge: Record<InventoryTransaction["type"], "success" | "info" | "warning"> = {
  "Nhập kho": "success",
  "Xuất kho (FEFO)": "info",
  "Điều chỉnh": "warning",
};

const columns: ColumnDef<InventoryTransaction, unknown>[] = [
  { accessorKey: "id", header: "Mã GD", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "timestamp", header: "Thời điểm" },
  {
    accessorKey: "type",
    header: "Loại",
    cell: ({ row }) => <Badge variant={typeBadge[row.original.type]}>{row.original.type}</Badge>,
  },
  { accessorKey: "batchId", header: "Lô" },
  { accessorKey: "ingredient", header: "Nguyên liệu" },
  {
    // BR-076: dòng xuất kho mang theo món đích — đây là liên kết món ↔ lô dùng
    // để dựng bản đồ truy xuất. Nhập kho/Điều chỉnh chưa có mục đích nên bỏ trống.
    id: "traceability",
    header: "Xuất cho món (BR-076)",
    cell: ({ row }) =>
      row.original.dishName ? (
        <div className="flex flex-col gap-0.5">
          <span className="font-medium">{row.original.dishName}</span>
          <span className="text-xs text-muted-foreground">
            {row.original.mealType ? MEAL_TYPE_LABEL[row.original.mealType] : ""}
            {row.original.serviceDate ? ` · ${row.original.serviceDate}` : ""}
            {row.original.menuId ? ` · ${row.original.menuId}` : ""}
          </span>
        </div>
      ) : (
        <span className="text-muted-foreground">—</span>
      ),
  },
  {
    accessorKey: "quantity",
    header: "Số lượng",
    cell: ({ row }) => (
      <span className={row.original.quantity.startsWith("-") ? "font-medium text-destructive" : "font-medium text-success"}>
        {row.original.quantity}
      </span>
    ),
  },
  {
    accessorKey: "reasonCode",
    header: "Mã lý do (BR-204)",
    cell: ({ row }) => row.original.reasonCode ?? "—",
  },
  { accessorKey: "actor", header: "Người thực hiện" },
  {
    // BR-204: CHỈ giao dịch "Điều chỉnh" thủ công mới bắt buộc xác nhận quản lý
    // (spoilage/waste/audit) — Nhập/Xuất kho bình thường không cần nên hiện "—".
    accessorKey: "managerSignature",
    header: "Xác nhận quản lý (BR-204)",
    cell: ({ row }) =>
      row.original.type === "Điều chỉnh" ? (
        row.original.managerSignature ? (
          <Badge variant="success">{row.original.managerSignature}</Badge>
        ) : (
          <Badge variant="danger">Thiếu — bị chặn</Badge>
        )
      ) : (
        <span className="text-muted-foreground">— không cần</span>
      ),
  },
];

export default function InventoryTransactionsPage() {
  return (
    <>
      <PageHeader
        title="Giao dịch kho"
        description="Sổ nhập/xuất/điều chỉnh không thể sửa xóa (append-only); mọi điều chỉnh thủ công phải có mã lý do và ký quản lý (BR-204)"
      />
      <DataTable<InventoryTransaction>
        resource="inventory-transactions"
        columns={columns}
        pageSize={10}
        searchPlaceholder="Tìm theo lô, nguyên liệu…"
      />
    </>
  );
}
