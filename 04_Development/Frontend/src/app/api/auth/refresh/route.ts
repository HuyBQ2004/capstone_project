import { NextResponse, type NextRequest } from "next/server";
import {
  BACKEND_URL,
  REFRESH_COOKIE,
  REFRESH_COOKIE_OPTIONS,
  REFRESH_MAX_AGE_SECONDS,
} from "@/lib/api/session-cookie";

/**
 * Đổi refresh token (đọc từ cookie httpOnly) lấy access token mới.
 *
 * Backend: `POST /api/v1/auth/refresh` → `{accessToken, refreshToken}` (đúng 2 khóa,
 * `AuthDtos.TokenPairResponse`). BR-004 v1.9 — **rotate**: token cũ bị revoke ngay,
 * nên cookie BẮT BUỘC được ghi đè bằng token mới, nếu không lần refresh kế tiếp
 * dùng token đã chết và người dùng bị đá ra giữa phiên.
 *
 * Trả về trình duyệt CHỈ `accessToken`. Refresh token mới đi thẳng vào cookie
 * httpOnly, không lộ ra JS.
 *
 * Hai vai trò:
 *   1. Interceptor 401 của `client.ts` gọi để cấp token mới rồi replay request gốc.
 *   2. `Providers` gọi một lần lúc mount để khôi phục phiên sau khi F5 (access token
 *      nằm ở memory nên reload là mất).
 */
export async function POST(request: NextRequest) {
  const refreshToken = request.cookies.get(REFRESH_COOKIE)?.value;

  if (!refreshToken) {
    // Chưa từng đăng nhập — KHÔNG phải lỗi, chỉ là chưa có phiên.
    return NextResponse.json(
      { error: { code: "IAM-008", message: "No refresh token" } },
      { status: 401 }
    );
  }

  let upstream: Response;
  try {
    upstream = await fetch(`${BACKEND_URL}/api/v1/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    });
  } catch {
    /*
     * Backend chưa chạy. Cố ý trả 503 chứ KHÔNG 401: 401 làm `client.ts` xóa
     * phiên và đá về /login, khiến "quên chạy backend" trông y hệt "token hỏng".
     * 503 giữ nguyên cookie để chỉ cần bật backend lên là F5 dùng tiếp được.
     */
    return NextResponse.json(
      { error: { code: "IAM-503", message: "Auth backend unreachable" } },
      { status: 503 }
    );
  }

  if (!upstream.ok) {
    // Token hết hạn / đã rotate / đã revoke (IAM-008) → dọn cookie chết.
    const dead = NextResponse.json(
      { error: { code: "IAM-008", message: "Refresh token rejected" } },
      { status: 401 }
    );
    dead.cookies.set(REFRESH_COOKIE, "", { ...REFRESH_COOKIE_OPTIONS, maxAge: 0 });
    return dead;
  }

  const pair = (await upstream.json()) as {
    accessToken?: string;
    refreshToken?: string;
  };

  if (!pair.accessToken || !pair.refreshToken) {
    return NextResponse.json(
      { error: { code: "IAM-008", message: "Malformed token pair" } },
      { status: 502 }
    );
  }

  const response = NextResponse.json({ accessToken: pair.accessToken });
  response.cookies.set(REFRESH_COOKIE, pair.refreshToken, {
    ...REFRESH_COOKIE_OPTIONS,
    maxAge: REFRESH_MAX_AGE_SECONDS,
  });
  return response;
}
