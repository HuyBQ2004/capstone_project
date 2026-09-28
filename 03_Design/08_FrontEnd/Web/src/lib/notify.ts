"use client";

/**
 * Sinh thông báo tại các mốc nghiệp vụ THẬT.
 *
 * Trước 21/07/2026 `notifications` chỉ được component chuông đánh dấu đã đọc —
 * không màn nào tạo bản ghi mới. Trong khi đó nhiều màn lại khẳng định với người
 * dùng rằng thông báo đã được gửi ("đã gửi thông báo tới phụ huynh", "Y tế trường
 * được thông báo tự động — MSG-058"): lời hứa suông, không có gì đi đâu cả.
 *
 * Từ 22/07/2026 mỗi thông báo BẮT BUỘC khai vai nhận (`to`). Trước đó model không
 * có trường người nhận nên mọi vai dùng chung một hộp — phụ huynh thấy việc của Y
 * tế, đơn vị suất ăn thấy thông báo gửi Ban giám hiệu.
 */

import { useCreate } from "@/lib/api/hooks";
import type { RoleKey } from "@/lib/roles";
import type { UserNotification } from "@/lib/mock-data";

export interface NotifyInput {
  /** Vai nhận — bắt buộc. Muốn gửi toàn hệ thống thì khai `to: "ALL"` chứ không bỏ trống. */
  to: RoleKey[] | "ALL";
  type: UserNotification["type"];
  title: string;
  content: string;
  link?: string;
  linkLabel?: string;
}

export function useNotify() {
  const create = useCreate<Partial<UserNotification>>("notifications");
  return (n: NotifyInput) =>
    create.mutate({
      id: `NTF-${String(Date.now()).slice(-6)}`,
      sentAt: new Date().toLocaleString("vi-VN"),
      read: false,
      audience: n.to === "ALL" ? "ALL" : "ROLES",
      recipientRoles: n.to === "ALL" ? undefined : n.to,
      type: n.type,
      title: n.title,
      content: n.content,
      link: n.link,
      linkLabel: n.linkLabel,
    });
}
