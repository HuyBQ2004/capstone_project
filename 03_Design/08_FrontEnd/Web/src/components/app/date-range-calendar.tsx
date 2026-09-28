"use client";

/**
 * DateRangeCalendar — lịch tháng chọn KHOẢNG ngày (nhận xét Minh 07/07/2026):
 * click ngày bắt đầu → click ngày kết thúc → cả khoảng được bôi màu nổi lên.
 * Click ngày mới khi đã đủ 2 đầu → bắt đầu chọn lại. Click ngày < ngày bắt
 * đầu → dời ngày bắt đầu. Dùng cho "Tuần áp dụng" khi tạo thực đơn (BR-061).
 */

import { useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const DAY_HEADERS = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];
const MONTH_LABEL = new Intl.DateTimeFormat("vi-VN", { month: "long", year: "numeric" });

function startOfDay(d: Date): Date {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate());
}

function sameDay(a: Date | null, b: Date | null): boolean {
  return (
    a != null &&
    b != null &&
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

/** Các ô của lưới tháng — luôn 6 hàng × 7 cột, bắt đầu từ Thứ 2. */
function monthGrid(view: Date): Date[] {
  const first = new Date(view.getFullYear(), view.getMonth(), 1);
  // getDay(): CN=0..T7=6 → offset từ Thứ 2
  const offset = (first.getDay() + 6) % 7;
  const cells: Date[] = [];
  for (let i = 0; i < 42; i++) {
    cells.push(new Date(view.getFullYear(), view.getMonth(), 1 - offset + i));
  }
  return cells;
}

export interface DateRangeCalendarProps {
  start: Date | null;
  end: Date | null;
  onChange: (start: Date | null, end: Date | null) => void;
  /** Ngày sớm nhất được phép chọn (VD: hôm nay + 3 ngày theo BR-061). */
  minDate?: Date;
  className?: string;
}

export function DateRangeCalendar({
  start,
  end,
  onChange,
  minDate,
  className,
}: DateRangeCalendarProps) {
  const [view, setView] = useState<Date>(start ?? minDate ?? new Date());
  const min = minDate ? startOfDay(minDate) : null;
  const today = startOfDay(new Date());

  function pick(day: Date) {
    if (min && day < min) return;
    if (!start || (start && end)) {
      onChange(day, null);
      return;
    }
    if (day < start) {
      onChange(day, null);
      return;
    }
    onChange(start, day);
  }

  const cells = monthGrid(view);

  return (
    <div className={cn("w-72 rounded-lg border bg-card p-3 shadow-sm", className)}>
      <div className="mb-2 flex items-center justify-between">
        <p className="text-sm font-semibold capitalize">{MONTH_LABEL.format(view)}</p>
        <div className="flex gap-1">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label="Tháng trước"
            onClick={() => setView(new Date(view.getFullYear(), view.getMonth() - 1, 1))}
          >
            <ChevronLeft />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label="Tháng sau"
            onClick={() => setView(new Date(view.getFullYear(), view.getMonth() + 1, 1))}
          >
            <ChevronRight />
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-7 text-center text-[11px] font-semibold uppercase text-muted-foreground">
        {DAY_HEADERS.map((h) => (
          <div key={h} className="py-1">{h}</div>
        ))}
      </div>

      <div className="grid grid-cols-7">
        {cells.map((day) => {
          const outsideMonth = day.getMonth() !== view.getMonth();
          const disabled = min != null && day < min;
          const isStart = sameDay(day, start);
          const isEnd = sameDay(day, end);
          const inRange =
            start != null && end != null && day > start && day < end;
          return (
            <button
              key={day.toISOString()}
              type="button"
              disabled={disabled}
              onClick={() => pick(day)}
              className={cn(
                "mx-auto my-0.5 flex size-8 items-center justify-center rounded-full text-sm transition-colors",
                outsideMonth && "text-muted-foreground/40",
                disabled && "cursor-not-allowed opacity-30",
                !disabled && !isStart && !isEnd && "hover:bg-muted",
                inRange && "rounded-none bg-primary/15 text-foreground",
                (isStart || isEnd) && "bg-primary font-semibold text-primary-foreground",
                sameDay(day, today) && !isStart && !isEnd && !inRange && "ring-1 ring-primary/50"
              )}
            >
              {day.getDate()}
            </button>
          );
        })}
      </div>

      <p className="mt-2 text-xs text-muted-foreground">
        {!start
          ? "Chọn ngày bắt đầu…"
          : !end
            ? `Bắt đầu ${start.toLocaleDateString("vi-VN")} — chọn ngày kết thúc…`
            : `Áp dụng từ ${start.toLocaleDateString("vi-VN")} đến ${end.toLocaleDateString("vi-VN")}`}
      </p>
    </div>
  );
}
