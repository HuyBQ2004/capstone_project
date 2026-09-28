"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Loader2, ShieldAlert } from "lucide-react";
import { ApiError } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";
import { homeRouteFor, useAuthStore } from "@/lib/stores/auth-store";

/**
 * UC-191 — đích của `sfms.oauth2.success-redirect-uri` (`GoogleOAuth2SuccessHandler.java`).
 *
 * Token nằm ở URL FRAGMENT (`#accessToken=...&refreshToken=...`), KHÔNG phải query string —
 * backend cố tình chọn fragment để không lọt vào access log/Referer (xem comment handler).
 * Fragment không bao giờ được gửi lên server (kể cả Next.js server component) nên bắt buộc
 * đọc bằng `window.location.hash` ở client, không đọc được qua `searchParams`.
 */
export default function OAuthCallbackPage() {
  const router = useRouter();
  const completeOAuthLogin = useAuthStore((s) => s.completeOAuthLogin);
  const [error, setError] = useState("");

  useEffect(() => {
    const hash = window.location.hash.replace(/^#/, "");
    const params = new URLSearchParams(hash);
    const accessToken = params.get("accessToken");
    const refreshToken = params.get("refreshToken");

    if (!accessToken || !refreshToken) {
      setError("Thiếu token từ Google — vui lòng đăng nhập lại.");
      return;
    }

    completeOAuthLogin(accessToken, refreshToken)
      .then((sessionUser) => {
        // Xoá fragment khỏi URL/history trước khi rời trang (token không nên nằm lại đó).
        router.replace(homeRouteFor(sessionUser));
      })
      .catch((err) => {
        setError(err instanceof ApiError ? apiErrorMessage(err, "Đăng nhập Google thất bại.") : "Không kết nối được máy chủ.");
      });
  }, [completeOAuthLogin, router]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-muted p-4 text-center">
      {error ? (
        <>
          <ShieldAlert className="size-8 text-destructive" />
          <p className="text-sm text-muted-foreground">{error}</p>
          <a href="/login" className="text-sm text-primary hover:underline">
            Về trang đăng nhập
          </a>
        </>
      ) : (
        <>
          <Loader2 className="size-8 animate-spin text-primary" />
          <p className="text-sm text-muted-foreground">Đang hoàn tất đăng nhập…</p>
        </>
      )}
    </div>
  );
}
