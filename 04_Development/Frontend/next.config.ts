import type { NextConfig } from "next";
import path from "path";

/**
 * Backend Spring Boot cho MF01 (IAM) + MF02 (School/Class/AcademicYear).
 * Đặt qua `.env.local` khi backend chạy ở máy/cổng khác.
 */
const BACKEND_URL = process.env.SFMS_BACKEND_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  // Docker: build ra .next/standalone (server + node_modules cần thiết) — image runtime khỏi
  // cần copy node_modules đầy đủ (nhẹ hơn nhiều).
  output: "standalone",

  // Có 2 lockfile (thư mục cha 08_FrontEnd và Web/) khiến Next suy nhầm workspace
  // root → cảnh báo + rủi ro tracing sai khi build. Ghim root về chính thư mục Web
  // này (review L2).
  outputFileTracingRoot: path.join(__dirname),

  /*
   * PROXY DEV — `SecurityConfig.java` của backend KHÔNG khai `CorsConfiguration`
   * nào, nên trình duyệt gọi thẳng `http://localhost:8080` sẽ bị chặn ở preflight.
   * Rewrite giữ mọi request ở SAME-ORIGIN (`http://localhost:3000/api/v1/...`)
   * → không có preflight, không cần đụng CORS phía backend.
   *
   * `client.ts` đặt `baseURL: "/api/v1"` (D3) nên khuôn dưới khớp nguyên vẹn.
   * Resource CHƯA có backend vẫn bị `mock-server.ts` chặn TRƯỚC khi ra tới network,
   * nên rewrite này không ảnh hưởng 10 module còn lại.
   */
  async rewrites() {
    return [
      {
        source: "/api/v1/:path*",
        destination: `${BACKEND_URL}/api/v1/:path*`,
      },
      /*
       * Ảnh hồ sơ (UC-097) do `LocalDiskStorageService` phục vụ ở `/media/**`
       * (D10, ADR-000). `avatarUrl` của `GET /profile/me` trỏ về đường này.
       */
      {
        source: "/media/:path*",
        destination: `${BACKEND_URL}/media/:path*`,
      },
      /*
       * Google OAuth2 login — nút "Đăng nhập bằng Google" điều hướng thẳng (top-level
       * navigation, không phải fetch) tới đường này. Rewrite giữ same-origin để dev
       * (localhost:3000) khớp hành vi với Nginx production (xem nginx/nginx.conf).
       */
      {
        source: "/oauth2/:path*",
        destination: `${BACKEND_URL}/oauth2/:path*`,
      },
      {
        source: "/login/oauth2/:path*",
        destination: `${BACKEND_URL}/login/oauth2/:path*`,
      },
      /*
       * Chuông thông báo real-time (SockJS/STOMP, xem lib/notification-socket.ts +
       * WebSocketConfig.java). SockJS tự rớt xuống transport HTTP (xhr-streaming/polling) khi
       * upgrade WebSocket không qua được rewrite dev server — không cần cấu hình gì thêm ở
       * đây để "ép" WS thật, SockJS tự xử lý fallback.
       */
      {
        source: "/ws/:path*",
        destination: `${BACKEND_URL}/ws/:path*`,
      },
    ];
  },
};

export default nextConfig;
