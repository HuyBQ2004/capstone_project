"use client";

import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Badge } from "@/components/ui/badge";
import type { InventoryTransactionDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";

/**
 * Record Inventory Transactions Log — MF06 (Service Caterers).
 * Sổ nhập/xuất/điều chỉnh append-only; điều chỉnh thủ công kèm mã lý do +
 * chữ ký quản lý (BR-204); xuất kho theo FEFO (BR-075).
 *
 * Nối backend thật 05/08/2026: `type` là `RECEIPT`/`ISSUE`/`ADJUSTMENT` (D6, không phải
 * nhãn tiếng Việt), `timestamp`→`occurredAt` (ISO), `quantity` chuỗi có dấu→`delta` số +
 * `unit` tách riêng, `actor`→`performedBy`, `dishName`→`dish`.
 */

const TYPE_LABEL: Record<string, string> = {
  RECEIPT: "Nhập kho",
  ISSUE: "Xuất kho (FEFO)",
  ADJUSTMENT: "Điều chỉnh",
};

const typeBadge: Record<string, "success" | "info" | "warning"> = {
  RECEIPT: "success",
  ISSUE: "info",
  ADJUSTMENT: "warning",
};

const columns: ColumnDef<InventoryTransactionDto, unknown>[] = [
  { accessorKey: "id", header: "Mã GD", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    accessorKey: "occurredAt",
    header: "Thời điểm",
    cell: ({ row }) => formatVnDateTime(row.original.occurredAt),
  },
  {
    accessorKey: "type",
    header: "Loại",
    cell: ({ row }) => (
      <Badge variant={typeBadge[row.original.type ?? ""] ?? "neutral"}>
        {TYPE_LABEL[row.original.type ?? ""] ?? row.original.type}
      </Badge>
    ),
  },
  { accessorKey: "batchId", header: "Lô" },
  { accessorKey: "ingredient", header: "Nguyên liệu" },
  {
    // BR-076: dòng xuất kho mang theo món đích — đây là liên kết món ↔ lô dùng
    // để dựng bản đồ truy xuất. Nhập kho/Điều chỉnh chưa có mục đích nên bỏ trống.
    id: "traceability",
    header: "Xuất cho món (BR-076)",
    cell: ({ row }) =>
      row.original.dish ? (
        <div className="flex flex-col gap-0.5">
          <span className="font-medium">{row.original.dish}</span>
          <span className="text-xs text-muted-foreground">
            {row.original.mealType ? MEAL_TYPE_LABEL[row.original.mealType as MealType] : ""}
            {row.original.serviceDate ? ` · ${row.original.serviceDate}` : ""}
            {row.original.menuId ? ` · ${row.original.menuId}` : ""}
          </span>
        </div>
      ) : (
        <span className="text-muted-foreground">—</span>
      ),
  },
  {
    accessorKey: "delta",
    header: "Số lượng",
    cell: ({ row }) => {
      const delta = row.original.delta ?? 0;
      return (
        <span className={delta < 0 ? "font-medium text-destructive" : "font-medium text-success"}>
          {delta > 0 ? `+${delta}` : delta} {row.original.unit}
        </span>
      );
    },
  },
  {
    accessorKey: "reasonCode",
    header: "Mã lý do (BR-204)",
    cell: ({ row }) => row.original.reasonCode ?? "—",
  },
  { accessorKey: "performedBy", header: "Người thực hiện" },
  {
    // BR-204: CHỈ giao dịch "Điều chỉnh" thủ công mới bắt buộc xác nhận quản lý
    // (spoilage/waste/audit) — Nhập/Xuất kho bình thường không cần nên hiện "—".
    accessorKey: "managerSignature",
    header: "Xác nhận quản lý (BR-204)",
    cell: ({ row }) =>
      row.original.type === "ADJUSTMENT" ? (
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
      <DataTable<InventoryTransactionDto>
        resource="inventory-transactions"
        columns={columns}
        pageSize={10}
        searchPlaceholder="Tìm theo lô, nguyên liệu…"
      />
    </>
  );
}
