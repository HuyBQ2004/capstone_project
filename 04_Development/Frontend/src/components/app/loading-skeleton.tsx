import { Card } from "@/components/ui/card";

/**
 * Khung xám chờ dữ liệu, dùng chung cho các màn ĐỌC API mà không đi qua
 * `DataTable` (DataTable đã tự có skeleton riêng ở phần thân bảng).
 *
 * Vì sao cần (đợt rà soát 07/08/2026): 24 màn render thẳng `data ?? []` mà không
 * xét `isLoading`. Trong nhịp đầu tiên mảng còn rỗng nên dashboard hiện số **0**
 * và bảng hiện "Không có bản ghi nào" — người dùng đọc thành "hôm nay không có
 * gì", trong khi thật ra dữ liệu chưa về. Đây là hiểu nhầm nguy hiểm ở các màn
 * đếm sự cố / suất ăn đặc biệt.
 *
 * `tiles` = số ô KPI phía trên (0 = không có), `blocks` = số khối nội dung bên dưới.
 */
export function LoadingSkeleton({
  tiles = 0,
  blocks = 1,
}: {
  tiles?: number;
  blocks?: number;
}) {
  return (
    <div className="space-y-6" aria-busy="true" aria-live="polite">
      <span className="sr-only">Đang tải dữ liệu…</span>
      {tiles > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: tiles }).map((_, i) => (
            <Card key={i} className="h-28 animate-pulse bg-muted/40" />
          ))}
        </div>
      )}
      {Array.from({ length: blocks }).map((_, i) => (
        <Card key={i} className="h-64 animate-pulse bg-muted/40" />
      ))}
    </div>
  );
}
