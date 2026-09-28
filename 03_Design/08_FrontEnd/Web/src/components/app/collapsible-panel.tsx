"use client";

/**
 * Mục 11.10 (đợt 2) — Collapsible Panel (Foundation): full-width row, thu gọn hiện
 * tiêu đề + tóm tắt; cho phép mở NHIỀU panel cùng lúc (không accordion single-open).
 * Tách thành component dùng chung 15/07/2026 (review incident Mục 12.8.1): áp dụng
 * cho cả 17_InvestigationWorkspace lẫn 26_IncidentDetail — dùng chung 1 pattern,
 * không tạo pattern riêng cho từng màn.
 *
 * Vòng 4 design pattern (Mục 10.3 QĐ 6, 19/07/2026): dòng tóm tắt trạng thái hiển
 * thị CẢ KHI ĐANG MỞ (trước chỉ hiện lúc đóng) — áp toàn dự án qua component chung.
 *
 * 27/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 8.4 điểm 2):
 * hỗ trợ thêm CHẾ ĐỘ CONTROLLED (props `open`/`onOpenChange`) để màn Kiểm tra an toàn
 * bếp làm được hành vi "bấm Tiếp theo → đóng nhóm hiện tại, mở nhóm kế tiếp". Không
 * truyền 2 props này thì component chạy y hệt trước (uncontrolled) — mọi nơi đang dùng
 * không cần sửa.
 */

import { useState, type ReactNode } from "react";
import { ChevronDown, ChevronRight } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function CollapsiblePanel({
  title,
  summary,
  defaultOpen = false,
  titleClassName,
  open: openProp,
  onOpenChange,
  children,
}: {
  title: ReactNode;
  summary: ReactNode;
  defaultOpen?: boolean;
  /** Mục 12.8.4 — cho phép tô màu warning #B45309 cho tiêu đề khối cần chú ý (BR-149) */
  titleClassName?: string;
  /** Controlled mode (tuỳ chọn): truyền cả 2 props để bên ngoài điều khiển đóng/mở */
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
  children: ReactNode;
}) {
  const [openState, setOpenState] = useState(defaultOpen);
  const controlled = openProp !== undefined;
  const open = controlled ? openProp : openState;
  const toggle = () => {
    if (controlled) onOpenChange?.(!open);
    else setOpenState((v) => !v);
  };
  return (
    <Card>
      <CardHeader>
        <button
          type="button"
          onClick={toggle}
          className="flex w-full items-center justify-between gap-3 text-left"
        >
          <CardTitle className={`flex items-center gap-2 ${titleClassName ?? ""}`}>
            {open ? <ChevronDown className="size-4 shrink-0" /> : <ChevronRight className="size-4 shrink-0" />}
            {title}
          </CardTitle>
          {/* Mục 10.3 QĐ 6 (vòng 4) — summary hiện cả khi mở, không chỉ lúc đóng */}
          <span className="truncate text-xs text-muted-foreground">{summary}</span>
        </button>
      </CardHeader>
      {open && <CardContent className="space-y-3">{children}</CardContent>}
    </Card>
  );
}
