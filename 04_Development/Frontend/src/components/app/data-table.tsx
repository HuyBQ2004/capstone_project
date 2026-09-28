"use client";

/**
 * DataTable dùng chung — TanStack Table (manual pagination) + TanStack Query.
 * Chuẩn hóa mọi trang danh sách: search phía server (mock), phân trang,
 * trạng thái loading/empty, và click-row đi tới trang chi tiết.
 */

import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  flexRender,
  getCoreRowModel,
  useReactTable,
  type ColumnDef,
} from "@tanstack/react-table";
import { Search } from "lucide-react";
import { usePagedList } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import type { ListParams } from "@/lib/api/client";
import { Pagination } from "@/components/app/pagination";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { cn } from "@/lib/utils";

export type { ColumnDef };

interface DataTableProps<T> {
  /** Tên resource REST (khớp db.ts), ví dụ "announcements". */
  resource: string;
  columns: ColumnDef<T, unknown>[];
  /** Lọc chính xác bổ sung, ví dụ { status: "PENDING" }. */
  filters?: ListParams;
  pageSize?: number;
  searchPlaceholder?: string;
  /** Nếu có, click 1 hàng sẽ điều hướng tới href này. */
  rowHref?: (row: T) => string;
  /** Vùng công cụ bên phải ô search (nút lọc, export...). */
  toolbar?: React.ReactNode;
  emptyMessage?: string;
}

export function DataTable<T>({
  resource,
  columns,
  filters = {},
  pageSize = 8,
  searchPlaceholder = "Tìm kiếm…",
  rowHref,
  toolbar,
  emptyMessage = "Không có bản ghi nào.",
}: DataTableProps<T>) {
  const router = useRouter();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");

  const { data, isLoading, isFetching, isError, error } = usePagedList<T>(resource, {
    page,
    pageSize,
    search: search || undefined,
    ...filters,
  });

  const rows = data?.items ?? [];
  const table = useReactTable({
    data: rows,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    pageCount: data?.pageCount ?? 1,
  });

  return (
    <Card>
      <CardContent className="pt-5">
        <div className="mb-4 flex flex-wrap items-center gap-2">
          <div className="relative w-full max-w-xs">
            <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(1);
              }}
              placeholder={searchPlaceholder}
              className="pl-8"
            />
          </div>
          {toolbar}
          {isFetching && !isLoading && (
            <span className="ml-auto text-xs text-muted-foreground">Đang tải…</span>
          )}
        </div>

        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((hg) => (
              <TableRow key={hg.id}>
                {hg.headers.map((h) => (
                  <TableHead key={h.id}>
                    {h.isPlaceholder
                      ? null
                      : flexRender(h.column.columnDef.header, h.getContext())}
                  </TableHead>
                ))}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={columns.length}>
                    <div className="h-5 w-full animate-pulse rounded bg-muted" />
                  </TableCell>
                </TableRow>
              ))
            ) : isError ? (
              /*
                * "Không tải được" KHÁC "không có bản ghi" (08/08/2026). Trước đây query hỏng
                * cũng rơi vào nhánh rỗng bên dưới, nên 422/403 hiện thành "Không có bản ghi
                * nào." — người dùng tin là dữ liệu trống và thao tác sai theo. Ba lỗi trong
                * cùng một ngày đội lốt kiểu này.
                */
              <TableRow>
                <TableCell colSpan={columns.length} className="py-10 text-center">
                  <span className="block font-medium text-destructive">
                    Không tải được dữ liệu — đây KHÔNG phải danh sách rỗng.
                  </span>
                  <span className="mt-1 block text-xs text-muted-foreground">
                    {apiErrorMessage(error, "Thử tải lại trang; nếu vẫn lỗi, báo quản trị hệ thống.")}
                  </span>
                </TableCell>
              </TableRow>
            ) : rows.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className="py-10 text-center text-muted-foreground"
                >
                  {emptyMessage}
                </TableCell>
              </TableRow>
            ) : (
              table.getRowModel().rows.map((row) => (
                <TableRow
                  key={row.id}
                  className={cn(rowHref && "cursor-pointer")}
                  onClick={
                    rowHref
                      ? () => router.push(rowHref(row.original))
                      : undefined
                  }
                >
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id}>
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        <Pagination
          page={data?.page ?? 1}
          pageCount={data?.pageCount ?? 1}
          total={data?.total ?? 0}
          pageSize={data?.pageSize ?? pageSize}
          onPageChange={setPage}
        />
      </CardContent>
    </Card>
  );
}
