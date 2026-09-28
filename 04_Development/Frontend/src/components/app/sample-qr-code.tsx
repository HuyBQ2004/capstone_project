"use client";

import { useEffect, useState } from "react";
import QRCode from "qrcode";

/**
 * QR thật (sinh client-side, không gọi CDN ngoài) — quét ra để tra cứu nhanh khi kiểm tra tủ
 * lưu mẫu hoặc khi có sự cố ATTP, thay cho icon QR tĩnh + text trước đây (không mã hoá gì, chỉ
 * trang trí).
 *
 * BUG tìm thấy 10/08/2026 — trước đây mã hoá THẲNG `value` (chỉ là mã mẫu trần, vd
 * "SMP-20260810-01"), nên camera điện thoại quét ra chỉ hiện được CHUỖI TEXT, không điều
 * hướng đi đâu cả — không đúng kỳ vọng "quét là xem chi tiết mẫu". Nay mã hoá URL ĐẦY ĐỦ tới
 * trang chi tiết mẫu (`/service-caterers/samples/{code}`); trang đó có auth guard sẵn nên quét
 * xong sẽ bắt đăng nhập trước khi xem — đúng hành vi mong đợi, không phải bug.
 */
export function SampleQrCode({ value, size = 96 }: { value: string; size?: number }) {
  const [dataUrl, setDataUrl] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const origin = typeof window !== "undefined" ? window.location.origin : "";
    const url = `${origin}/service-caterers/samples/${encodeURIComponent(value)}`;
    QRCode.toDataURL(url, { width: size, margin: 1 })
      .then((dataUrlResult) => {
        if (!cancelled) setDataUrl(dataUrlResult);
      })
      .catch(() => {
        if (!cancelled) setDataUrl(null);
      });
    return () => {
      cancelled = true;
    };
  }, [value, size]);

  if (!dataUrl) {
    return <div className="animate-pulse rounded-md bg-muted" style={{ width: size, height: size }} />;
  }
  // eslint-disable-next-line @next/next/no-img-element -- data URL, không phải ảnh remote
  return <img src={dataUrl} alt={`Mã QR ${value}`} width={size} height={size} className="rounded-md" />;
}
