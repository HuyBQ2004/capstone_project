import * as React from "react";
import { cn } from "@/lib/utils";

export function Table({
  className,
  ...props
}: React.HTMLAttributes<HTMLTableElement>) {
  return (
    /*
     * Bảng CUỘN NGANG khi hẹp, không co cột (quy ước §12.5 review Medical Staff).
     *
     * Trước 22/07/2026 bảng chỉ có `w-full`: gặp bảng nhiều cột mà một cột là văn
     * bản dài, trình duyệt co các cột ngắn lại tới mức bẻ chữ từng dòng — "CAPA-
     * 2026-033" xuống thành 3 dòng, hàng cao gấp năm, mà nội dung vẫn tràn ra
     * ngoài thẻ. `min-w` buộc bảng giữ bề ngang tối thiểu và cho cuộn ngang thay
     * vì bóp cột.
     */
    <div className="relative w-full overflow-x-auto">
      <table
        className={cn("w-full min-w-[46rem] caption-bottom text-sm", className)}
        {...props}
      />
    </div>
  );
}

export function TableHeader({
  className,
  ...props
}: React.HTMLAttributes<HTMLTableSectionElement>) {
  return <thead className={cn("[&_tr]:border-b", className)} {...props} />;
}

export function TableBody({
  className,
  ...props
}: React.HTMLAttributes<HTMLTableSectionElement>) {
  return (
    <tbody className={cn("[&_tr:last-child]:border-0", className)} {...props} />
  );
}

export function TableRow({
  className,
  ...props
}: React.HTMLAttributes<HTMLTableRowElement>) {
  return (
    <tr
      className={cn("border-b transition-colors hover:bg-muted/60", className)}
      {...props}
    />
  );
}

export function TableHead({
  className,
  ...props
}: React.ThHTMLAttributes<HTMLTableCellElement>) {
  return (
    <th
      className={cn(
        "h-10 px-3 text-left align-bottom text-xs font-semibold uppercase tracking-wide text-muted-foreground",
        className
      )}
      {...props}
    />
  );
}

export function TableCell({
  className,
  ...props
}: React.TdHTMLAttributes<HTMLTableCellElement>) {
  return (
    /*
     * `align-top`: ô nhiều dòng nằm cạnh ô một dòng thì căn giữa làm lệch mắt.
     *
     * `[&_.truncate]:block`: 20 chỗ trong dự án viết `<span className="max-w-96
     * truncate">`, mà `truncate` (overflow+ellipsis) KHÔNG có tác dụng trên phần tử
     * inline — chuỗi dài vẫn tràn hết cỡ và bóp các cột còn lại. Ép về block ngay
     * tại đây rẻ hơn sửa 20 chỗ và không chỗ nào bị bỏ sót.
     */
    <td className={cn("px-3 py-2.5 align-top [&_.truncate]:block", className)} {...props} />
  );
}
