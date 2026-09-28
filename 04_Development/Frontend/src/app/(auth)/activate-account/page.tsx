"use client";

import { useState, useEffect, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label } from "@/components/ui/input";
import { Alert } from "@/components/ui/alert";
import { api } from "@/lib/api/client";
import { CheckCircle2, KeyRound } from "lucide-react";

function ActivateAccountForm() {
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

  const minLength = 8;
  const rules = [
    { label: `Tối thiểu ${minLength} ký tự`, ok: password.length >= minLength },
    { label: "Có chữ hoa và chữ thường", ok: /[a-z]/.test(password) && /[A-Z]/.test(password) },
    { label: "Có chữ số", ok: /\d/.test(password) },
    { label: "Có ký tự đặc biệt (!@#$%...)", ok: /[^A-Za-z0-9]/.test(password) },
  ];

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!token.trim()) {
      setError("Vui lòng nhập hoặc sử dụng liên kết chứa mã kích hoạt hợp lệ.");
      return;
    }
    if (rules.some((r) => !r.ok)) {
      setError("Mật khẩu chưa đáp ứng chính sách bảo mật.");
      return;
    }
    if (password !== confirm) {
      setError("Mật khẩu xác nhận không khớp.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      await api.post("/auth/activate", {
        token: token.trim(),
        newPassword: password,
      });
      setDone(true);
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { message?: string } }; message?: string };
      setError(errorObj?.response?.data?.message || errorObj?.message || "Kích hoạt tài khoản thất bại. Mã kích hoạt có thể đã hết hạn hoặc không hợp lệ.");
    } finally {
      setLoading(false);
    }
  }

  if (done) {
    return (
      <Card className="w-full max-w-md shadow-xl border-primary/20">
        <CardHeader className="text-center pb-2">
          <div className="mx-auto size-12 rounded-full bg-emerald-500/10 flex items-center justify-center mb-2">
            <CheckCircle2 className="size-6 text-emerald-600" />
          </div>
          <CardTitle className="text-xl">Kích hoạt Tài khoản Thành công!</CardTitle>
          <CardDescription>
            Mật khẩu của bạn đã được khởi tạo. Bạn có thể sử dụng Email và Mật khẩu vừa đặt để đăng nhập vào hệ thống SFMS.
          </CardDescription>
        </CardHeader>
        <CardContent className="pt-4">
          <Link href="/login">
            <Button className="w-full h-11 text-base shadow-md">Đăng nhập Ngay</Button>
          </Link>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full max-w-md shadow-xl border-border">
      <CardHeader className="space-y-1">
        <div className="flex items-center gap-2">
          <KeyRound className="size-5 text-primary" />
          <CardTitle className="text-xl">Kích hoạt Tài khoản & Đặt mật khẩu</CardTitle>
        </div>
        <CardDescription>
          Thiết lập mật khẩu lần đầu để hoàn tất kích hoạt tài khoản SFMS của bạn (BR-011).
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <Alert variant="danger">{error}</Alert>}

          <div className="space-y-1.5">
            <Label htmlFor="token">Mã kích hoạt (Activation Token)</Label>
            <Input
              id="token"
              type="text"
              placeholder="Nhập mã token hoặc mở qua link email"
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
              placeholder="Nhập mật khẩu mới"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <ul className="space-y-1 text-xs pl-1">
            {rules.map((r) => (
              <li key={r.label} className={r.ok ? "text-emerald-600 font-medium" : "text-muted-foreground"}>
                {r.ok ? "✓" : "○"} {r.label}
              </li>
            ))}
          </ul>

          <div className="space-y-1.5">
            <Label htmlFor="confirm">Xác nhận mật khẩu</Label>
            <Input
              id="confirm"
              type="password"
              placeholder="Nhập lại mật khẩu mới"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              required
            />
          </div>

          <Button type="submit" className="w-full h-11 shadow" disabled={loading}>
            {loading ? "Đang kích hoạt..." : "Kích hoạt Tài khoản & Đặt mật khẩu"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

export default function ActivateAccountPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center">Đang tải trang...</div>}>
      <ActivateAccountForm />
    </Suspense>
  );
}
