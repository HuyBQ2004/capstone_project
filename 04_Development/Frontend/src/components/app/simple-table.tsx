"use client";

import Link from "next/link";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { apiErrorMessage } from "@/lib/messages";
import { cn } from "@/lib/utils";

/**
 * Bảng dùng chung cho dữ liệu ĐÃ CÓ SẴN trong trang — bổ khuyết giữa `DataTable` và việc tự
 * dựng `<Table>` bằng tay.
 *
 * ## Vì sao có component này
 *
 * Đo 08/08/2026: FE có **ba tầng bảng không tương đương**.
 *   · 46 file dùng `DataTable` — tự gọi API theo `resource`, có phân trang, ô tìm kiếm,
 *     skeleton, empty state, click-vào-dòng;
 *   · **47 file** dùng thẳng primitives `ui/table` — chỉ có khung và màu, mọi thứ khác tự viết;
 *   · 2 màn còn dựng `<table>` thô với class chép tay.
 *
 * Nhóm 47 file không sai khi tránh `DataTable`: nó KHOÁ theo `resource` và **tự fetch**, nên
 * không dùng được cho bảng nhỏ mà dữ liệu đã nằm sẵn trong trang (đã lọc/gộp/nạp kèm màn
 * khác). Nhưng hệ quả là mỗi màn tự chế lại: câu "không có bản ghi" mỗi nơi một kiểu, không
 * có skeleton, và **không phân biệt "lỗi" với "rỗng"** — đúng lớp lỗi khiến ba sự cố khác
 * nhau trong cùng một ngày đều hiện thành bảng trống.
 *
 * `SimpleTable` nhận sẵn `rows`, KHÔNG fetch, và lo bốn trạng thái: đang tải · lỗi · rỗng ·
 * có dữ liệu.
 *
 * ## Bề ngang
 *
 * `ui/table` áp `min-w-[46rem]` cho MỌI bảng — hợp lý với bảng nhiều cột ở vùng nội dung
 * rộng, nhưng bảng 2–3 cột nhét trong card nửa màn hình thì 736px là quá rộng ⇒ cuộn ngang dù
 * nội dung tí xíu. Ở đây bề ngang tối thiểu suy theo SỐ CỘT.
 */

export interface SimpleColumn<T> {
  /** Nhãn cột. Để trống cho cột nút/hành động. */
  header?: React.ReactNode;
  /** Nội dung ô. */
  cell: (row: T, index: number) => React.ReactNode;
  /** Class cho cả `<th>` và `<td>` — dùng cho căn phải, cố định bề ngang… */
  className?: string;
}

export interface SimpleTableProps<T> {
  columns: SimpleColumn<T>[];
  rows: T[];
  /** Khoá React của từng dòng — bắt buộc để không dùng index (dòng đổi chỗ là mất state). */
  rowKey: (row: T, index: number) => string;
  isLoading?: boolean;
  isError?: boolean;
  error?: unknown;
  /** Câu hiển thị khi KHÔNG có bản ghi nào — khác hẳn câu báo lỗi bên dưới. */
  emptyMessage?: React.ReactNode;
  /** Bấm vào dòng để mở chi tiết. */
  rowHref?: (row: T) => string;
  /** Số dòng skeleton lúc đang tải. */
  skeletonRows?: number;
  className?: string;
}

export function SimpleTable<T>({
  columns,
  rows,
  rowKey,
  isLoading = false,
  isError = false,
  error,
  emptyMessage = "Không có bản ghi nào.",
  rowHref,
  skeletonRows = 3,
  className,
}: SimpleTableProps<T>) {
  /*
   * Bảng ít cột thì đừng ép rộng. Ngưỡng chọn theo thực tế các card dashboard: 2–3 cột nằm
   * vừa nửa màn hình 1440px (~44rem) nên không cần bề ngang tối thiểu.
   */
  const minWidthClass =
    columns.length <= 3 ? "min-w-0" : columns.length <= 5 ? "min-w-[34rem]" : "min-w-[46rem]";

  const body = () => {
    if (isLoading) {
      return Array.from({ length: skeletonRows }).map((_, i) => (
        <TableRow key={`sk-${i}`}>
          <TableCell colSpan={columns.length}>
            <div className="h-5 w-full animate-pulse rounded bg-muted" />
          </TableCell>
        </TableRow>
      ));
    }
    if (isError) {
      /*
       * "Không tải được" KHÁC "không có bản ghi". Gộp hai cái làm một chính là thứ khiến người
       * dùng tin là dữ liệu trống rồi thao tác sai theo (nhập lại, báo mất hàng).
       */
      return (
        <TableRow>
          <TableCell colSpan={columns.length} className="py-8 text-center">
            <span className="block font-medium text-destructive">
              Không tải được dữ liệu — đây KHÔNG phải danh sách rỗng.
            </span>
            <span className="mt-1 block text-xs text-muted-foreground">
              {apiErrorMessage(error, "Thử tải lại trang; nếu vẫn lỗi, báo quản trị hệ thống.")}
            </span>
          </TableCell>
        </TableRow>
      );
    }
    if (rows.length === 0) {
      return (
        <TableRow>
          <TableCell colSpan={columns.length} className="py-8 text-center text-muted-foreground">
            {emptyMessage}
          </TableCell>
        </TableRow>
      );
    }
    return rows.map((row, i) => {
      const href = rowHref?.(row);
      return (
        <TableRow key={rowKey(row, i)} className={cn(href && "cursor-pointer")}>
          {columns.map((col, ci) => (
            <TableCell key={ci} className={col.className}>
              {/*
                * Link bọc TỪNG Ô chứ không bọc `<tr>`: `<a>` không phải con hợp lệ của `<tr>`,
                * bọc sai thì trình duyệt tự gỡ ra khỏi bảng và bố cục vỡ.
                */}
              {href ? (
                <Link href={href} className="block">
                  {col.cell(row, i)}
                </Link>
              ) : (
                col.cell(row, i)
              )}
            </TableCell>
          ))}
        </TableRow>
      );
    });
  };

  return (
    <Table minWidthClass={minWidthClass} className={className}>
      <TableHeader>
        <TableRow>
          {columns.map((col, i) => (
            <TableHead key={i} className={col.className}>
              {col.header}
            </TableHead>
          ))}
        </TableRow>
      </TableHeader>
      <TableBody>{body()}</TableBody>
    </Table>
  );
}
