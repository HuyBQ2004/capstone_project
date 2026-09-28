"use client";

/**
 * Chuông thông báo real-time qua WebSocket/STOMP (bổ sung UC-058/136, MF10) — thay/gia cố
 * polling 30s (`staleTime` của QueryClient) bằng đẩy tức thời khi `NotificationPortImpl.publish()`
 * chạy (xem `NotificationBroadcaster.java`).
 *
 * SockJS cần path tương đối `/ws` (dev: `next.config.ts` rewrite sang backend; prod: nginx.conf
 * location riêng) — KHÔNG dùng `ws://`/`wss://` trực tiếp vì SockJS tự chọn transport (kể cả
 * XHR polling khi WebSocket bị chặn), việc đó cần cùng-origin HTTP, không phải raw socket URL.
 */

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "@/lib/api/tokens";

let client: Client | null = null;

/**
 * @param schoolId trường của user đang đăng nhập — quy ước topic `/topic/notifications/{schoolId}`
 * @param onMessage gọi lại mỗi khi có thông báo mới (FE tự quyết làm gì — ở đây: invalidate query)
 */
export function connectNotificationSocket(schoolId: string, onMessage: () => void): () => void {
  const token = getAccessToken();
  if (!token) return () => {};

  client = new Client({
    webSocketFactory: () => new SockJS(`/ws?token=${encodeURIComponent(token)}`),
    reconnectDelay: 5000,
    onConnect: () => {
      client?.subscribe(`/topic/notifications/${schoolId}`, () => onMessage());
    },
  });
  client.activate();

  return () => {
    client?.deactivate();
    client = null;
  };
}
