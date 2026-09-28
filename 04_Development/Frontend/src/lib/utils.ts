import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Bộ đếm phiên — đảm bảo 2 lần gọi trong cùng mili-giây vẫn khác nhau.
let __idSeq = 0;

/**
 * Sinh ID phía client duy nhất trong phiên (thay `Date.now().slice(-n)` dễ trùng
 * khi 2 thao tác cùng mili-giây hoặc hậu tố quá ngắn — review K3/E5).
 * Giữ nguyên `prefix` theo format cũ (vd "PR-2026", "HS-3A"); hậu tố base36 hoa.
 *
 * Lưu ý: đây là giải pháp tạm cho mock. Khi nối backend, để server cấp phát ID.
 */
export function clientId(prefix: string): string {
  __idSeq += 1;
  const suffix = (Date.now() + __idSeq).toString(36).slice(-5).toUpperCase();
  return `${prefix}-${suffix}`;
}
