"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";

/**
 * Ảnh đại diện — ẢNH THẬT nếu có, chữ cái đầu nếu không.
 *
 * <p><b>Vì sao có component này (09/08/2026).</b> Nam báo "cập nhật ảnh đại diện không thấy gì".
 * Đo bằng API thật: `POST /profile/me/photo` trả <b>200</b>, file lên Cloudflare R2 thật
 * (`https://cdn.sfsm.io.vn/avatars/U-0100.png` → 200, `image/png`), và `GET /profile/me` trả
 * đúng `avatarUrl`. Backend không sai chỗ nào — <b>frontend chưa từng vẽ ảnh đó</b>:
 * `avatarUrl` chỉ nằm trong `auth-store`, còn cả ba chỗ hiển thị (nút topbar, thẻ trong menu,
 * dialog hồ sơ) đều cứng `{user.name.charAt(0)}`.
 *
 * <p>Gom một chỗ để không phải sửa ba nơi lần sau, và để quy tắc dự phòng chỉ có một bản.
 */
export function UserAvatar({
  name,
  avatarUrl,
  className,
  previewUrl,
}: {
  name: string;
  avatarUrl?: string | null;
  /** Kích thước + kiểu, vd. `"size-9 text-sm"`. */
  className?: string;
  /** `URL.createObjectURL(file)` — xem ngay ảnh vừa chọn, trước cả khi bấm Lưu. */
  previewUrl?: string | null;
}) {
  /*
   * Ảnh hỏng (link CDN chết, file bị xoá) thì QUAY VỀ chữ cái, không để lại ô vỡ ảnh — người
   * dùng đọc ô vỡ thành "hệ thống hỏng", trong khi chữ cái vẫn là trạng thái bình thường.
   */
  const [failed, setFailed] = useState(false);
  const src = previewUrl || (failed ? null : avatarUrl);

  return (
    <div
      className={cn(
        "flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-primary-soft font-semibold text-primary",
        className
      )}
    >
      {src ? (
        /* Ảnh ở CDN ngoài (R2), không qua loader của next/image; khai remotePatterns cho một
           ảnh nhỏ là chi phí thừa. */
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={src}
          alt={`Ảnh đại diện của ${name}`}
          className="size-full object-cover"
          onError={() => setFailed(true)}
        />
      ) : (
        name.charAt(0)
      )}
    </div>
  );
}
