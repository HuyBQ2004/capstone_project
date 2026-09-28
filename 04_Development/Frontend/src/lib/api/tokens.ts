/**
 * Giữ access token cho phiên hiện tại và nói chuyện với 2 Route Handler
 * `/api/auth/session` + `/api/auth/refresh` (nơi refresh token nằm trong cookie
 * httpOnly — xem `session-cookie.ts`).
 *
 * Access token ở **biến module**, KHÔNG ở `localStorage`/`sessionStorage`:
 *   - reload trang là mất → khôi phục bằng một lần gọi `/api/auth/refresh` lúc
 *     mount (`Providers`), tức cookie httpOnly mới là thứ giữ phiên;
 *   - script chèn qua XSS không đọc được từ storage.
 *
 * Tách khỏi `client.ts` để `auth-store.ts` dùng được mà không tạo vòng import
 * (client → store → client).
 */

let accessToken: string | null = null;

/** Callback cho `client.ts` biết phiên đã chết để dọn store + điều hướng. */
let onSessionExpired: (() => void) | null = null;

export function getAccessToken(): string | null {
  return accessToken;
}

export function setAccessToken(token: string | null): void {
  accessToken = token;
}

export function registerSessionExpiredHandler(handler: (() => void) | null): void {
  onSessionExpired = handler;
}

export function notifySessionExpired(): void {
  accessToken = null;
  onSessionExpired?.();
}

/** Đưa refresh token vừa nhận từ `POST /auth/login` vào cookie httpOnly. */
export async function persistRefreshToken(refreshToken: string): Promise<void> {
  await fetch("/api/auth/session", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
}

/** Xóa cookie refresh (logout, hoặc khi refresh bị backend từ chối). */
export async function clearRefreshToken(): Promise<void> {
  accessToken = null;
  try {
    await fetch("/api/auth/session", { method: "DELETE" });
  } catch {
    // Mạng hỏng lúc logout: cookie còn lại nhưng access token đã xóa ở trên,
    // và refresh token sẽ bị backend revoke. Không có gì để cứu vãn ở đây.
  }
}

/**
 * Single-flight: nhiều request 401 cùng lúc (dashboard bắn 5 query song song) chỉ
 * được phép sinh **một** lần rotate. Không có chốt này thì request thứ hai dùng
 * refresh token vừa bị request thứ nhất rotate mất — BR-004 v1.9 revoke token cũ
 * ngay ⇒ nửa số query trên màn hình fail và người dùng bị đăng xuất oan.
 */
let inFlight: Promise<string | null> | null = null;

export function refreshAccessToken(): Promise<string | null> {
  if (inFlight) return inFlight;

  inFlight = (async () => {
    try {
      const res = await fetch("/api/auth/refresh", { method: "POST" });
      if (!res.ok) return null;
      const { accessToken: fresh } = (await res.json()) as { accessToken?: string };
      if (!fresh) return null;
      accessToken = fresh;
      return fresh;
    } catch {
      return null;
    } finally {
      inFlight = null;
    }
  })();

  return inFlight;
}
