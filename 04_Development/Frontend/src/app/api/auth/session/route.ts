import { NextResponse, type NextRequest } from "next/server";
import {
  REFRESH_COOKIE,
  REFRESH_COOKIE_OPTIONS,
  REFRESH_MAX_AGE_SECONDS,
} from "@/lib/api/session-cookie";

/**
 * Ghi/xóa refresh token vào cookie **httpOnly** — JavaScript của trang không đọc
 * được, nên XSS lấy được access token (sống ~30 phút, chỉ nằm trong biến JS) cũng
 * KHÔNG lấy được thứ dùng để tự cấp lại token vô hạn.
 *
 * Đây là lý do refresh token không nằm ở `localStorage`. Access token thì có ở
 * memory — chấp nhận được vì nó ngắn hạn và mất khi reload.
 *
 * ⚠️ Path `/api/auth/*` KHÔNG trùng với rewrite `/api/v1/:path*` ở `next.config.ts`.
 * `rewrites()` trả về mảng ⇒ Next xếp vào nhóm `afterFiles`, tức Route Handler này
 * (route thật trên filesystem) luôn thắng.
 */
export async function POST(request: NextRequest) {
  let refreshToken: unknown;
  try {
    ({ refreshToken } = await request.json());
  } catch {
    return NextResponse.json({ error: { message: "Malformed body" } }, { status: 400 });
  }

  if (typeof refreshToken !== "string" || refreshToken.length === 0) {
    return NextResponse.json(
      { error: { message: "refreshToken is required" } },
      { status: 400 }
    );
  }

  const response = new NextResponse(null, { status: 204 });
  response.cookies.set(REFRESH_COOKIE, refreshToken, {
    ...REFRESH_COOKIE_OPTIONS,
    maxAge: REFRESH_MAX_AGE_SECONDS,
  });
  return response;
}

/**
 * Logout. Chỉ xóa cookie phía trình duyệt — việc revoke token phía server do
 * `POST /api/v1/auth/logout` làm (cần access token, nên gọi từ `client.ts`).
 */
export async function DELETE() {
  const response = new NextResponse(null, { status: 204 });
  response.cookies.set(REFRESH_COOKIE, "", { ...REFRESH_COOKIE_OPTIONS, maxAge: 0 });
  return response;
}
