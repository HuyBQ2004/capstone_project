"use client";

import { useState, useEffect, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label } from "@/components/ui/input";
import { Alert } from "@/components/ui/alert";
import { api } from "@/lib/api/client";
import { useSingle } from "@/lib/api/hooks";
import type { SecurityPolicyDto } from "@/lib/api/types";

function ResetPasswordForm() {
  const searchParams = useSearchParams();
  const rawTokenParam = searchParams.get("token") || "";

  const [token, setToken] = useState(rawTokenParam);
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (rawTokenParam) {
      setToken(rawTokenParam);
    }
  }, [rawTokenParam]);

  /*
   * PHÁT HIỆN NHÂN TIỆN (Giai đoạn 3, 07/08/2026), 2 lớp:
   * 1) Trang này trước đó gọi `usePagedList<SecurityPolicy>("security-policies", …)` —
   *    nhưng `GET /security-policies` trả THẲNG 1 object (UC-184, KHÔNG bọc
   *    PagedResponse, xem chú thích `SecurityPolicyDto` ở `lib/api/types.ts`).
   *    `usePagedList` luôn đọc `.items` (undefined ở đây) nên `policy` luôn
   *    `undefined`. Đổi sang `useSingle` (đúng mẫu
   *    `system-admin/security-policies/page.tsx` đã dùng) — đúng SHAPE hơn.
   * 2) NHƯNG `SecurityPolicyController` yêu cầu `hasRole('SYSTEM_ADMIN')`
   *    (`@PreAuthorize`) — trang Đặt lại mật khẩu này KHÔNG đăng nhập (auth flow
   *    công khai), nên gọi vẫn LUÔN 401/403 dù đã sửa (1). Form vì vậy vẫn rơi về
   *    default cứng `?? 8`/`?? true` bên dưới — CHƯA sửa được triệt để, cần BE mở
   *    endpoint public-safe riêng (hoặc chấp nhận default cứng là chủ ý, vì độ dài
   *    tối thiểu 8 ký tự luôn ≤ cấu hình thật theo BR-006 nên không nới lỏng bảo
   *    mật). Ghi chú lại, KHÔNG tự bịa quyền truy cập mới.
   */
  const { data: policy } = useSingle<SecurityPolicyDto>("security-policies");
  const minLength = policy?.passwordMinLength ?? 8;

  const rules = [
    { label: `Tối thiểu ${minLength} ký tự`, ok: password.length >= minLength, on: true },
    {
      label: "Có chữ hoa và chữ thường",
      ok: /[a-z]/.test(password) && /[A-Z]/.test(password),
      on: policy?.requireMixedCase ?? true,
    },
    { label: "Có chữ số", ok: /\d/.test(password), on: policy?.requireNumber ?? true },
    {
      label: "Có ký tự đặc biệt",
      ok: /[^A-Za-z0-9]/.test(password),
      on: policy?.requireSpecialChar ?? true,
    },
  ].filter((r) => r.on);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!token.trim()) {
      setError("Mã token đặt lại mật khẩu không được để trống.");
      return;
    }
    if (rules.some((r) => !r.ok)) {
      setError("Mật khẩu chưa đáp ứng chính sách bảo mật (BR-006).");
      return;
    }
    if (password !== confirm) {
      setError("Mật khẩu xác nhận không khớp.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      await api.post("/auth/reset-password", {
        token: token.trim(),
        newPassword: password,
      });
      setDone(true);
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { message?: string } }; message?: string };
      setError(errorObj?.response?.data?.message || errorObj?.message || "Đặt lại mật khẩu thất bại. Mã token có thể đã hết hạn.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Card className="w-full max-w-md shadow-xl border-border">
      <CardHeader>
        <CardTitle>Đặt lại mật khẩu</CardTitle>
        <CardDescription>
          Mật khẩu mới không được trùng 3 mật khẩu gần nhất (BR-006)
        </CardDescription>
      </CardHeader>
      <CardContent>
        {done ? (
          <div className="space-y-4">
            <Alert variant="success">
              Đổi mật khẩu thành công! Bạn có thể sử dụng mật khẩu mới để đăng nhập.
            </Alert>
            <Link href="/login">
              <Button className="w-full h-11">Đăng nhập</Button>
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && <Alert variant="danger">{error}</Alert>}

            <div className="space-y-1.5">
              <Label htmlFor="token">Mã token đặt lại mật khẩu</Label>
              <Input
                id="token"
                type="text"
                placeholder="Nhập token hoặc mở qua liên kết email"
                value={token}
                onChange={(e) => setToken(e.target.value)}
                required
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="password">Mật khẩu mới</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <ul className="space-y-1 text-xs">
              {rules.map((r) => (
                <li
                  key={r.label}
                  className={r.ok ? "text-emerald-600 font-medium" : "text-muted-foreground"}
                >
                  {r.ok ? "✓" : "○"} {r.label}
                </li>
              ))}
            </ul>

            <div className="space-y-1.5">
              <Label htmlFor="confirm">Xác nhận mật khẩu</Label>
              <Input
                id="confirm"
                type="password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                required
              />
            </div>

            <Button type="submit" className="w-full h-11 shadow" disabled={loading}>
              {loading ? "Đang xử lý..." : "Đặt lại mật khẩu"}
            </Button>
          </form>
        )}
      </CardContent>
    </Card>
  );
}

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center">Đang tải trang...</div>}>
      <ResetPasswordForm />
    </Suspense>
  );
}
