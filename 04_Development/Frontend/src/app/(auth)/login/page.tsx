"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label } from "@/components/ui/input";
import { Alert } from "@/components/ui/alert";
import { ApiError } from "@/lib/api/client";
import { apiErrorMessage, errorLabel } from "@/lib/messages";
import { homeRouteFor, useAuthStore } from "@/lib/stores/auth-store";

/**
 * UC-034 Đăng nhập — GuestCommon/01_Login.md
 *
 * 02/08/2026: bỏ picker vai trò giả, gọi `POST /api/v1/auth/login` THẬT (MF01).
 * Vai trò không còn do người dùng chọn mà do JWT quyết định — đây chính là điểm
 * mà bản mock không thể hiện được: chọn "Quản trị hệ thống" ở màn login cũ là
 * vào thẳng workspace System Admin, không ai kiểm.
 *
 * BR-002 (khóa sau N lần sai) và BR-010 (email phải đã xác minh) nay do backend
 * thi hành: `AuthService` đếm `failed_login_count`, đặt `locked_until`, và trả
 * IAM-002 / IAM-003 / IAM-007. Màn này chỉ hiển thị lỗi trả về — KHÔNG tự đếm
 * nữa, vì bộ đếm phía client reset mỗi lần F5 nên chưa bao giờ khóa được ai.
 */
/**
 * BR-002 — số lần thử còn lại trước khi khóa 15 phút.
 *
 * Backend đính kèm trong `details` của IAM-002:
 *   {"code":"IAM-002","message":"Invalid credentials…",
 *    "details":[{"field":"attemptsLeft","value":4}]}
 *
 * Bộ đếm nằm ở `user_accounts.failed_login_count` chứ không ở màn này — bản cũ
 * đếm bằng `useState` nên F5 là về 0 và BR-002 chưa từng khóa được tài khoản nào.
 */
function attemptsSuffix(error: ApiError): string {
  const detail = error.details.find(
    (d): d is { field: string; value: number } =>
      typeof d === "object" &&
      d !== null &&
      (d as { field?: unknown }).field === "attemptsLeft"
  );
  return detail ? ` Còn ${detail.value} lần thử.` : "";
}

/**
 * Chỉ nhận đường dẫn NỘI BỘ. Không lọc thì `?next=//evil.tld` hoặc
 * `?next=https://evil.tld` biến màn đăng nhập thành bàn đạp chuyển hướng mở.
 */
function safeNext(raw: string | null): string | null {
  if (!raw) return null;
  if (!raw.startsWith("/") || raw.startsWith("//")) return null;
  return raw;
}

/**
 * `useSearchParams` buộc phải nằm dưới một ranh giới `Suspense` thì Next mới
 * prerender được trang này (nếu không, `next build` báo lỗi ở bước tĩnh).
 */
export default function LoginPage() {
  return (
    <Suspense fallback={<Card className="w-full max-w-lg h-96 animate-pulse bg-muted/40" />}>
      <LoginForm />
    </Suspense>
  );
}

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const loginWithPassword = useAuthStore((s) => s.loginWithPassword);
  const status = useAuthStore((s) => s.status);
  const user = useAuthStore((s) => s.user);
  const sessionExpired = useAuthStore((s) => s.sessionExpired);
  const clearSessionExpired = useAuthStore((s) => s.clearSessionExpired);

  const next = safeNext(searchParams.get("next"));

  /*
   * Đang có phiên mà vào thẳng `/login` thì đá về workspace — trước đây vẫn hiện
   * form, người dùng tưởng mình đã bị đăng xuất (mục "Ghi chú nhỏ" của 11_UI_Audit).
   */
  useEffect(() => {
    if (status === "authenticated" && user) {
      router.replace(next ?? homeRouteFor(user));
    }
  }, [status, user, next, router]);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  /*
   * Lỗi trả về từ luồng ĐĂNG NHẬP GOOGLE (`?oauthError=IAM-102`).
   *
   * Trước 09/08/2026 backend ghi thẳng envelope JSON ra response ở giữa chuỗi redirect, nên
   * người dùng thấy một trang trắng đầy `{"error":{"code":"IAM-102",…}}` và không có đường quay
   * lại — triệu chứng "đăng nhập Google không được". Thực tế Google xác thực THÀNH CÔNG, chỉ là
   * email đó chưa liên kết tài khoản SFMS nào; nay nói bằng tiếng Việt ngay tại đây.
   */
  const oauthError = searchParams.get("oauthError");
  useEffect(() => {
    if (oauthError) {
      setError(errorLabel(oauthError, "Đăng nhập Google thất bại. Vui lòng thử lại."));
    }
  }, [oauthError]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      const sessionUser = await loginWithPassword(email.trim(), password);
      clearSessionExpired();
      // MSG-026 — Login successful. Vai trò lấy từ /profile/me, không từ form.
      // `next` = màn đang làm dở lúc phiên chết, do `AuthGate` đính vào URL.
      router.push(next ?? homeRouteFor(sessionUser));
    } catch (err) {
      if (err instanceof ApiError) {
        /*
         * D7: backend trả `code` + `message` tiếng Anh nguyên văn `error_message.md`.
         * `apiErrorMessage` tra nhãn tiếng Việt theo `code`, dùng `message` của
         * server làm fallback khi mã chưa có bản dịch — không bịa chuỗi ở đây.
         */
        setError(apiErrorMessage(err, "Đăng nhập thất bại.") + attemptsSuffix(err));
      } else {
        setError("Không kết nối được máy chủ. Kiểm tra backend đã chạy ở cổng 8080 chưa.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Card className="w-full max-w-lg">
      <CardHeader>
        <CardTitle>Đăng nhập</CardTitle>
        <CardDescription>
          Nhập email và mật khẩu tài khoản SFMS của bạn
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {sessionExpired && !error && (
            <Alert variant="warning">
              Phiên làm việc đã kết thúc — có thể do bạn đăng xuất ở thiết bị khác, hoặc
              phiên đã quá hạn. Dữ liệu của bạn KHÔNG mất; đăng nhập lại để xem tiếp.
            </Alert>
          )}
          {error && <Alert variant="danger">{error}</Alert>}

          <div className="space-y-1.5">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              autoComplete="username"
              required
              placeholder="ten.ban@truong.edu.vn"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="password">Mật khẩu</Label>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? "text" : "password"}
                autoComplete="current-password"
                required
                placeholder="••••••••"
                className="pr-10"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button
                type="button"
                aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                onClick={() => setShowPassword((v) => !v)}
              >
                {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
              </button>
            </div>
          </div>

          <Button type="submit" className="w-full" disabled={submitting}>
            {submitting ? (
              <>
                <Loader2 className="size-4 animate-spin" />
                Đang đăng nhập…
              </>
            ) : (
              "Đăng nhập"
            )}
          </Button>

          <div className="flex items-center justify-between text-sm">
            <Link href="/forgot-password" className="text-primary hover:underline">
              Quên mật khẩu?
            </Link>
            <Link href="/register" className="text-primary hover:underline">
              Đăng ký (School/Supplier/Caterer)
            </Link>
          </div>
        </form>

        <div className="my-4 flex items-center gap-3 text-xs text-muted-foreground">
          <div className="h-px flex-1 bg-border" />
          <span>HOẶC</span>
          <div className="h-px flex-1 bg-border" />
        </div>

        {/*
         * UC-191 — top-level navigation (KHÔNG phải fetch/XHR): rời hẳn trang để
         * Spring Security OAuth2 Client tự điều khiển redirect tới Google rồi
         * callback về (TDS §17 C5, KHÔNG tự viết flow tay). Path tương đối vì
         * next.config.ts rewrite (dev) / nginx.conf (prod) đều đã trỏ đúng backend.
         */}
        <a
          href="/oauth2/authorization/google"
          className="inline-flex w-full items-center justify-center gap-2 rounded-md border border-border bg-background px-4 py-2 text-sm font-medium hover:bg-muted"
        >
          <svg className="size-4" viewBox="0 0 24 24" aria-hidden="true">
            <path fill="#4285F4" d="M23.49 12.27c0-.79-.07-1.54-.19-2.27H12v4.51h6.47c-.29 1.48-1.14 2.73-2.4 3.58v2.98h3.86c2.26-2.09 3.56-5.17 3.56-8.8Z" />
            <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.86-2.98c-1.08.72-2.45 1.15-4.07 1.15-3.13 0-5.78-2.11-6.73-4.96H1.3v3.09C3.26 21.3 7.31 24 12 24Z" />
            <path fill="#FBBC05" d="M5.27 14.3c-.24-.72-.38-1.49-.38-2.3s.14-1.58.38-2.3V6.61H1.3A11.96 11.96 0 0 0 0 12c0 1.93.46 3.76 1.3 5.39l3.97-3.09Z" />
            <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.3 6.61l3.97 3.09C6.22 6.86 8.87 4.75 12 4.75Z" />
          </svg>
          Đăng nhập bằng Google
        </a>
      </CardContent>
    </Card>
  );
}
