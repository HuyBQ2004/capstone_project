"use client";

import { useMemo, useState } from "react";
import { ChevronLeft, ChevronRight, MoreHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";

/**
 * 13/08/2026 (theo Nam) — trước đây render THẲNG mọi số trang (`Array.from({length:
 * pageCount})`), danh sách nhiều (VD 50+ trang) ra hàng chục nút liền, chỉ tự xuống dòng nhờ
 * `flex-wrap` chứ không rút gọn. Rút gọn kiểu 1 … 4 5 [6] 7 8 … 42 — luôn giữ trang đầu/cuối,
 * 1 khoảng quanh trang hiện tại, còn lại thay bằng dấu "…".
 */
function pageWindow(page: number, pageCount: number, siblings = 1): (number | "ellipsis")[] {
  const totalVisible = siblings * 2 + 5; // đầu + cuối + hiện tại + 2 ellipsis tối đa + siblings
  if (pageCount <= totalVisible) {
    return Array.from({ length: pageCount }, (_, i) => i + 1);
  }
  const left = Math.max(page - siblings, 1);
  const right = Math.min(page + siblings, pageCount);
  const pages: (number | "ellipsis")[] = [1];
  if (left > 2) pages.push("ellipsis");
  for (let n = Math.max(left, 2); n <= Math.min(right, pageCount - 1); n++) pages.push(n);
  if (right < pageCount - 1) pages.push("ellipsis");
  if (pageCount > 1) pages.push(pageCount);
  return pages;
}

/** Hook phân trang client-side cho danh sách mock. */
export function usePagination<T>(data: T[], pageSize = 8) {
  const [page, setPage] = useState(1);
  const pageCount = Math.max(1, Math.ceil(data.length / pageSize));
  const safePage = Math.min(page, pageCount);
  const rows = useMemo(
    () => data.slice((safePage - 1) * pageSize, safePage * pageSize),
    [data, safePage, pageSize]
  );
  return { rows, page: safePage, pageCount, setPage, total: data.length, pageSize };
}

export function Pagination({
  page,
  pageCount,
  total,
  pageSize,
  onPageChange,
}: {
  page: number;
  pageCount: number;
  total: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}) {
  if (pageCount <= 1) return null;
  const from = (page - 1) * pageSize + 1;
  const to = Math.min(page * pageSize, total);

  return (
    <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t pt-4">
      <p className="text-sm text-muted-foreground">
        Hiển thị <span className="font-medium text-foreground">{from}–{to}</span> trong{" "}
        <span className="font-medium text-foreground">{total}</span> bản ghi
      </p>
      <div className="flex flex-wrap items-center gap-1">
        <Button
          variant="outline"
          size="icon"
          disabled={page <= 1}
          onClick={() => onPageChange(page - 1)}
          aria-label="Trang trước"
        >
          <ChevronLeft />
        </Button>
        {pageWindow(page, pageCount).map((n, i) =>
          n === "ellipsis" ? (
            <span
              key={`ellipsis-${i}`}
              className="flex size-9 items-center justify-center text-muted-foreground"
              aria-hidden
            >
              <MoreHorizontal className="size-4" />
            </span>
          ) : (
            <Button
              key={n}
              variant={n === page ? "default" : "outline"}
              size="icon"
              onClick={() => onPageChange(n)}
              aria-label={`Trang ${n}`}
              aria-current={n === page ? "page" : undefined}
            >
              {n}
            </Button>
          )
        )}
        <Button
          variant="outline"
          size="icon"
          disabled={page >= pageCount}
          onClick={() => onPageChange(page + 1)}
          aria-label="Trang sau"
        >
          <ChevronRight />
        </Button>
      </div>
    </div>
  );
}
