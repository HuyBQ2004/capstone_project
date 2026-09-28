"use client";

import * as React from "react";
import { Calendar as CalendarIcon, Search, ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";

export const Input = React.forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement>
>(({ className, type, ...props }, ref) => (
  <input
    type={type}
    ref={ref}
    className={cn(
      "flex h-9 w-full rounded-lg border border-input bg-card px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50",
      className
    )}
    {...props}
  />
));
Input.displayName = "Input";

/** "dd/mm/yyyy" → "yyyy-mm-dd"; chuỗi rỗng nếu không hợp lệ. */
function vnToIso(v: string): string {
  const m = v.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  return m ? `${m[3]}-${m[2]}-${m[1]}` : "";
}

/**
 * Gán value qua setter gốc của HTMLInputElement để React nhận ra giá trị đã đổi.
 * Gán thẳng node.value sẽ bị value-tracker của React coi là "không đổi" và nuốt
 * mất sự kiện change tiếp theo.
 */
function setNativeValue(node: HTMLInputElement, next: string) {
  const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, "value")?.set;
  if (setter) setter.call(node, next);
  else node.value = next;
}

/**
 * Ô nhập ngày định dạng dd/mm/yyyy — gõ tay (tự chèn "/") hoặc bấm icon lịch
 * để chọn từ date picker của trình duyệt. Value luôn là chuỗi "dd/mm/yyyy".
 */
export const DateInput = React.forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement>
>(({ className, value, defaultValue, disabled, onChange, ...props }, ref) => {
  const textRef = React.useRef<HTMLInputElement | null>(null);
  const pickerRef = React.useRef<HTMLInputElement | null>(null);

  function handleType(e: React.ChangeEvent<HTMLInputElement>) {
    const digits = e.target.value.replace(/\D/g, "").slice(0, 8);
    let out = digits;
    if (digits.length > 4) out = `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
    else if (digits.length > 2) out = `${digits.slice(0, 2)}/${digits.slice(2)}`;
    e.target.value = out;
    onChange?.(e);
  }

  function handlePick(e: React.ChangeEvent<HTMLInputElement>) {
    if (!e.target.value) return;
    const [y, m, d] = e.target.value.split("-");
    const node = textRef.current;
    if (!node) return;
    /*
     * Không gán ngược "dd/mm/yyyy" vào chính ô type="date": trình duyệt sanitize
     * mọi chuỗi không phải yyyy-mm-dd về rỗng, nên parent chỉ nhận được "".
     * Ghi vào ô text hiển thị qua native setter rồi bắn "input" để React chạy
     * đúng onChange của nó — dùng chung đường chuẩn hóa với lúc gõ tay.
     */
    setNativeValue(node, `${d}/${m}/${y}`);
    node.dispatchEvent(new Event("input", { bubbles: true }));
  }

  function openPicker() {
    const p = pickerRef.current;
    if (!p) return;
    const current = value !== undefined ? String(value ?? "") : textRef.current?.value ?? "";
    p.value = vnToIso(current);
    if (typeof p.showPicker === "function") p.showPicker();
    else p.click();
  }

  return (
    <div className={cn("relative", className)}>
      <Input
        ref={(node) => {
          textRef.current = node;
          if (typeof ref === "function") ref(node);
          else if (ref) ref.current = node;
        }}
        inputMode="numeric"
        placeholder="dd/mm/yyyy"
        maxLength={10}
        className="pr-9"
        value={value}
        defaultValue={defaultValue}
        disabled={disabled}
        onChange={handleType}
        {...props}
      />
      <input
        ref={pickerRef}
        type="date"
        tabIndex={-1}
        aria-hidden="true"
        onChange={handlePick}
        className="pointer-events-none absolute bottom-0 right-0 size-0 opacity-0"
      />
      <button
        type="button"
        disabled={disabled}
        onClick={openPicker}
        aria-label="Chọn ngày từ lịch"
        className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground disabled:pointer-events-none disabled:opacity-50"
      >
        <CalendarIcon className="size-4" />
      </button>
    </div>
  );
});
DateInput.displayName = "DateInput";

export function Label({
  className,
  ...props
}: React.LabelHTMLAttributes<HTMLLabelElement>) {
  return (
    <label
      className={cn("text-sm font-medium text-foreground", className)}
      {...props}
    />
  );
}

export const Select = React.forwardRef<
  HTMLSelectElement,
  React.SelectHTMLAttributes<HTMLSelectElement>
>(({ className, ...props }, ref) => (
  <select
    ref={ref}
    className={cn(
      "flex h-9 w-full rounded-lg border border-input bg-card px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50",
      className
    )}
    {...props}
  />
));
Select.displayName = "Select";

export const Textarea = React.forwardRef<
  HTMLTextAreaElement,
  React.TextareaHTMLAttributes<HTMLTextAreaElement>
>(({ className, ...props }, ref) => (
  <textarea
    ref={ref}
    className={cn(
      "flex min-h-[80px] w-full rounded-lg border border-input bg-card px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50",
      className
    )}
    {...props}
  />
));
Textarea.displayName = "Textarea";

export type SearchableSelectOption = string | { value: string; label: string };

function normalizeOption(opt: SearchableSelectOption): { value: string; label: string } {
  return typeof opt === "string" ? { value: opt, label: opt } : opt;
}

/**
 * `options` chấp nhận `string[]` (value === label, dùng cho danh mục tên đơn thuần — VD đơn vị
 * tính, khu lưu trữ) HOẶC `{value, label}[]` (11/08/2026 — khi value là ID/mã nội bộ khác với
 * tên hiển thị, VD chọn nguyên liệu theo `ingredientId` nhưng hiện `ingredientName`). Trước đây
 * chỉ nhận `string[]` nên các trang có value≠label (ID vs tên) phải tự viết lại combobox riêng
 * thay vì dùng component chung này.
 */
export function SearchableSelect({
  value,
  onChange,
  options,
  placeholder = "— Chọn —",
  searchPlaceholder = "Tìm kiếm...",
  disabled = false,
}: {
  value: string;
  onChange: (value: string) => void;
  options: SearchableSelectOption[];
  placeholder?: string;
  searchPlaceholder?: string;
  disabled?: boolean;
}) {
  const [open, setOpen] = React.useState(false);
  const [search, setSearch] = React.useState("");
  const containerRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const normalized = options.map(normalizeOption);
  const filtered = normalized.filter((opt) =>
    opt.label.toLowerCase().includes(search.toLowerCase())
  );
  const selectedLabel = normalized.find((opt) => opt.value === value)?.label ?? value;

  return (
    <div className="relative w-full" ref={containerRef}>
      <button
        type="button"
        disabled={disabled}
        onClick={() => setOpen(!open)}
        className="flex h-9 w-full items-center justify-between gap-2 rounded-lg border border-input bg-card px-3 py-1 text-sm shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50 text-left"
      >
        {/*
         * `min-w-0 truncate` (12/08/2026): nút cao CỐ ĐỊNH `h-9` nhưng nhãn trước đây được phép
         * xuống dòng — nhãn dài (VD lô hàng ở `supplier/purchase-requests/[id]`:
         * "BATCH-AB6B9128 · Lô HANHLA-0810 · HSD 30/10/2026 · còn 2999.982 kg") tràn ra NGOÀI
         * khung, đè lên dòng trên/dưới. `truncate` chỉ ăn khi phần tử có `min-w-0` trong flex.
         * `title` để vẫn đọc được đầy đủ khi bị cắt.
         */}
        <span
          className={cn("min-w-0 truncate", value ? "text-foreground" : "text-muted-foreground")}
          title={value ? selectedLabel : undefined}
        >
          {value ? selectedLabel : placeholder}
        </span>
        <ChevronDown className="size-4 opacity-50 shrink-0" />
      </button>

      {open && (
        <div className="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-lg border border-input bg-card p-1 shadow-md">
          <div className="flex items-center border-b px-2 pb-1 pt-0.5">
            <Search className="size-3.5 mr-2 text-muted-foreground shrink-0" />
            <input
              type="text"
              placeholder={searchPlaceholder}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-transparent py-1 text-xs outline-none placeholder:text-muted-foreground"
              autoFocus
            />
          </div>
          <div className="mt-1 max-h-48 overflow-y-auto">
            {filtered.length === 0 ? (
              <p className="p-2 text-center text-xs text-muted-foreground">Không tìm thấy kết quả.</p>
            ) : (
              filtered.map((opt) => (
                <button
                  key={opt.value}
                  type="button"
                  onClick={() => {
                    onChange(opt.value);
                    setOpen(false);
                    setSearch("");
                  }}
                  className={cn(
                    "flex w-full items-center rounded-md px-2 py-1.5 text-xs text-left transition-colors hover:bg-muted hover:text-foreground",
                    value === opt.value && "bg-muted font-semibold"
                  )}
                >
                  {opt.label}
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
