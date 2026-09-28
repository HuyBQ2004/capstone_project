/**
 * Hằng dùng chung giữa hai Route Handler `/api/auth/session` và `/api/auth/refresh`.
 *
 * Tách khỏi `client.ts` có chủ đích: file này chỉ chạy phía SERVER (Node runtime
 * của Next), còn `client.ts` chạy phía trình duyệt. Refresh token KHÔNG bao giờ
 * được import vào bundle client.
 */

/** Tên cookie httpOnly giữ refresh token (BR-004 v1.9 — sliding 14 ngày). */
export const REFRESH_COOKIE = "sfms_rt";

/** 14 ngày, khớp TTL refresh token của backend (`RefreshTokenService`). */
export const REFRESH_MAX_AGE_SECONDS = 14 * 24 * 60 * 60;

/**
 * `secure` chỉ bật ở production: dev chạy `http://localhost:3000`, đặt `secure`
 * ở đây thì trình duyệt lặng lẽ bỏ cookie và mọi lần F5 đều văng ra `/login`.
 */
export const REFRESH_COOKIE_OPTIONS = {
  httpOnly: true,
  sameSite: "lax",
  path: "/",
  secure: process.env.NODE_ENV === "production",
} as const;

/** Backend Spring Boot — cùng biến môi trường với rewrite ở `next.config.ts`. */
export const BACKEND_URL = process.env.SFMS_BACKEND_URL ?? "http://localhost:8080";
