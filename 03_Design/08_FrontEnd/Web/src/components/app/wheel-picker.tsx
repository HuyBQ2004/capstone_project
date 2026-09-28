"use client";

/**
 * Wheel picker kiểu "đặt báo thức iPhone" (nhận xét Minh 08/07/2026):
 * cột giá trị cuộn dọc, snap vào giữa, vùng chọn được highlight.
 * Dùng cho ngày/giờ trong sổ kiểm thực 3 bước (QĐ 1246) và các form khác.
 * - TimeWheelField: chọn giờ:phút (phút bước 5).
 * - DateWheelField: chọn ngày/tháng/năm (VD hạn sử dụng).
 * - DateTimeWheelField: thanh NGÀY mặc định theo phiên (cuộn ±2 ngày) + giờ:phút.
 */

import { useEffect, useRef, useState } from "react";
import { CalendarClock, Clock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const ITEM_H = 32; // px mỗi dòng
const VISIBLE = 5; // số dòng hiển thị (lẻ để có dòng giữa)
const PAD = ((VISIBLE - 1) / 2) * ITEM_H;

/* ===== 1 cột cuộn snap ===== */

function Wheel({
  items,
  index,
  onIndexChange,
  width = "w-16",
}: {
  items: string[];
  index: number;
  onIndexChange: (i: number) => void;
  width?: string;
}) {
  const ref = useRef<HTMLDivElement | null>(null);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Cuộn tới giá trị ban đầu khi mount (popover mở)
  useEffect(() => {
    ref.current?.scrollTo({ top: index * ITEM_H });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function onScroll() {
    if (timer.current) clearTimeout(timer.current);
    timer.current = setTimeout(() => {
      const el = ref.current;
      if (!el) return;
      const i = Math.max(0, Math.min(items.length - 1, Math.round(el.scrollTop / ITEM_H)));
      if (i !== index) onIndexChange(i);
    }, 120);
  }

  return (
    <div className={cn("relative overflow-hidden", width)} style={{ height: VISIBLE * ITEM_H }}>
      <div
        ref={ref}
        onScroll={onScroll}
        className="h-full snap-y snap-mandatory overflow-y-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        style={{ paddingTop: PAD, paddingBottom: PAD }}
      >
        {items.map((it, i) => (
          <div
            key={`${it}-${i}`}
            className={cn(
              "flex snap-center items-center justify-center text-sm transition-colors",
              i === index ? "font-semibold text-foreground" : "text-muted-foreground/50"
            )}
            style={{ height: ITEM_H }}
          >
            {it}
          </div>
        ))}
      </div>
      {/* Băng chọn giữa — giống iOS */}
      <div
        className="pointer-events-none absolute inset-x-0 top-1/2 -translate-y-1/2 rounded-md border-y border-primary/30 bg-primary/5"
        style={{ height: ITEM_H }}
      />
    </div>
  );
}

/* ===== Popover khung chung ===== */

function WheelPopover({
  open,
  onClose,
  children,
  hint,
}: {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  hint?: string;
}) {
  if (!open) return null;
  return (
    <>
      <div className="fixed inset-0 z-40" aria-hidden onClick={onClose} />
      <div className="absolute left-0 top-full z-50 mt-1 rounded-lg border bg-card p-3 shadow-lg">
        <div className="flex items-center gap-1">{children}</div>
        {hint && <p className="mt-1.5 max-w-52 text-[11px] text-muted-foreground">{hint}</p>}
        <div className="mt-2 flex justify-end">
          <Button type="button" size="sm" onClick={onClose}>Xong</Button>
        </div>
      </div>
    </>
  );
}

const pad2 = (n: number) => String(n).padStart(2, "0");
const HOURS = Array.from({ length: 24 }, (_, i) => pad2(i));
const MINUTES = Array.from({ length: 12 }, (_, i) => pad2(i * 5));

/* ===== Field: chọn GIỜ:PHÚT ===== */

export function TimeWheelField({
  value,
  onChange,
  placeholder = "— : —",
  className,
}: {
  /** "HH:mm" hoặc "". */
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
  className?: string;
}) {
  const [open, setOpen] = useState(false);
  const m = value.match(/(\d{1,2}):(\d{2})/);
  const hi = m ? HOURS.indexOf(pad2(Number(m[1]))) : 10;
  const mi = m ? Math.round(Number(m[2]) / 5) % 12 : 0;

  return (
    <div className={cn("relative", className)}>
      <Button
        type="button"
        variant="outline"
        className={cn("h-8 w-full justify-start px-2 font-normal", !value && "text-muted-foreground")}
        onClick={() => setOpen((v) => !v)}
      >
        <Clock className="size-3.5" /> {value || placeholder}
      </Button>
      <WheelPopover open={open} onClose={() => setOpen(false)}>
        <Wheel items={HOURS} index={Math.max(0, hi)} onIndexChange={(i) => onChange(`${HOURS[i]}:${MINUTES[Math.max(0, mi)]}`)} />
        <span className="pb-1 text-lg font-semibold">:</span>
        <Wheel items={MINUTES} index={Math.max(0, mi)} onIndexChange={(i) => onChange(`${HOURS[Math.max(0, hi)]}:${MINUTES[i]}`)} />
      </WheelPopover>
    </div>
  );
}

/* ===== Field: chọn NGÀY/THÁNG/NĂM (VD hạn sử dụng) ===== */

const DAYS31 = Array.from({ length: 31 }, (_, i) => pad2(i + 1));
const MONTHS = Array.from({ length: 12 }, (_, i) => pad2(i + 1));
const YEARS = ["2025", "2026", "2027", "2028"];

export function DateWheelField({
  value,
  onChange,
  placeholder = "—/—/————",
  className,
}: {
  /** "dd/MM/yyyy" hoặc "". */
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
  className?: string;
}) {
  const [open, setOpen] = useState(false);
  const m = value.match(/(\d{1,2})\/(\d{1,2})\/(\d{4})/);
  const di = m ? Number(m[1]) - 1 : 14;
  const mi = m ? Number(m[2]) - 1 : 6;
  const yi = m ? Math.max(0, YEARS.indexOf(m[3])) : 1;
  const emit = (d: number, mo: number, y: number) =>
    onChange(`${DAYS31[d]}/${MONTHS[mo]}/${YEARS[y]}`);

  return (
    <div className={cn("relative", className)}>
      <Button
        type="button"
        variant="outline"
        className={cn("h-8 w-full justify-start px-2 font-normal", !value && "text-muted-foreground")}
        onClick={() => setOpen((v) => !v)}
      >
        <CalendarClock className="size-3.5" /> {value || placeholder}
      </Button>
      <WheelPopover open={open} onClose={() => setOpen(false)}>
        <Wheel items={DAYS31} index={di} onIndexChange={(i) => emit(i, mi, yi)} />
        <span className="pb-1 text-lg font-semibold">/</span>
        <Wheel items={MONTHS} index={mi} onIndexChange={(i) => emit(di, i, yi)} />
        <span className="pb-1 text-lg font-semibold">/</span>
        <Wheel items={YEARS} index={yi} onIndexChange={(i) => emit(di, mi, i)} width="w-20" />
      </WheelPopover>
    </div>
  );
}

/* ===== Field: NGÀY (mặc định theo phiên, cuộn ±2 ngày) + GIỜ:PHÚT ===== */

function shiftDate(base: string, delta: number): string {
  const m = base.match(/(\d{1,2})\/(\d{1,2})\/(\d{4})/);
  if (!m) return base;
  const d = new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1]) + delta);
  return `${pad2(d.getDate())}/${pad2(d.getMonth() + 1)}/${d.getFullYear()}`;
}

export function DateTimeWheelField({
  value,
  onChange,
  baseDate,
  placeholder = "chọn ngày, giờ…",
  className,
}: {
  /** "dd/MM/yyyy HH:mm" hoặc "". */
  value: string;
  onChange: (v: string) => void;
  /** Ngày mặc định (ngày mở phiên kiểm thực) — thanh ngày cuộn quanh ±2 ngày. */
  baseDate: string;
  placeholder?: string;
  className?: string;
}) {
  const [open, setOpen] = useState(false);
  const dayOptions = [-2, -1, 0, 1, 2].map((d) => shiftDate(baseDate, d));

  const dm = value.match(/(\d{1,2}\/\d{1,2}\/\d{4})/);
  const tm = value.match(/(\d{1,2}):(\d{2})/);
  const dIdx = dm ? Math.max(0, dayOptions.indexOf(dm[1])) : 2; // mặc định = ngày phiên
  const hIdx = tm ? HOURS.indexOf(pad2(Number(tm[1]))) : 8;
  const mIdx = tm ? Math.round(Number(tm[2]) / 5) % 12 : 0;
  const emit = (d: number, h: number, mi2: number) =>
    onChange(`${dayOptions[d]} ${HOURS[h]}:${MINUTES[mi2]}`);

  return (
    <div className={cn("relative", className)}>
      <Button
        type="button"
        variant="outline"
        className={cn("h-8 w-full justify-start px-2 font-normal", !value && "text-muted-foreground")}
        onClick={() => setOpen((v) => !v)}
      >
        <CalendarClock className="size-3.5" /> {value || placeholder}
      </Button>
      <WheelPopover
        open={open}
        onClose={() => setOpen(false)}
        hint="Ngày mặc định là ngày mở phiên — cuộn để chỉnh trong ±2 ngày."
      >
        <Wheel items={dayOptions} index={dIdx} onIndexChange={(i) => emit(i, Math.max(0, hIdx), Math.max(0, mIdx))} width="w-28" />
        <Wheel items={HOURS} index={Math.max(0, hIdx)} onIndexChange={(i) => emit(dIdx, i, Math.max(0, mIdx))} />
        <span className="pb-1 text-lg font-semibold">:</span>
        <Wheel items={MINUTES} index={Math.max(0, mIdx)} onIndexChange={(i) => emit(dIdx, Math.max(0, hIdx), i)} />
      </WheelPopover>
    </div>
  );
}
