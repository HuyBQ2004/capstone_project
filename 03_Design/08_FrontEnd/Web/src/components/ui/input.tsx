"use client";

import * as React from "react";
import { Calendar as CalendarIcon } from "lucide-react";
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
