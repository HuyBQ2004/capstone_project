"use client";

import { useState } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label } from "@/components/ui/input";
import { Alert } from "@/components/ui/alert";
import { usePagedList } from "@/lib/api/hooks";
import type { SecurityPolicy } from "@/lib/mock-data";

/**
 * UC Reset Password — GuestCommon/03_ResetPassword.md
 * BR-006/BR-015: chính sách độ mạnh, không trùng 3 mật khẩu gần nhất.
 */
export default function ResetPasswordPage() {
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  /**
   * Quy tắc lấy từ CHÍNH SÁCH BẢO MẬT do System Admin đặt (BR-006), không hardcode.
   * Trước 22/07/2026 màn này ghi cứng "Tối thiểu 8 ký tự" trong khi màn Chính sách
   * bảo mật khai đó là tham số cấu hình được — quản trị viên đặt 12 thì màn này
   * vẫn cho qua ở 8.
   */
  const { data: policyPage } = usePagedList<SecurityPolicy>("security-policies", { pageSize: 5 });
  const policy = policyPage?.items?.[0];
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

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (rules.some((r) => !r.ok)) {
      setError("Mật khẩu chưa đáp ứng chính sách bảo mật (BR-006).");
      return;
    }
    if (password !== confirm) {
      setError("Mật khẩu xác nhận không khớp.");
      return;
    }
    setError("");
    setDone(true);
  }

  return (
    <Card className="w-full max-w-md">
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
              Password changed successfully. (MSG-030)
            </Alert>
            <Link href="/login">
              <Button className="w-full">Đăng nhập</Button>
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && <Alert variant="danger">{error}</Alert>}
            <div className="space-y-1.5">
              <Label htmlFor="password">Mật khẩu mới</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <ul className="space-y-1 text-xs">
              {rules.map((r) => (
                <li
                  key={r.label}
                  className={r.ok ? "text-success" : "text-muted-foreground"}
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
              />
            </div>
            <Button type="submit" className="w-full">
              Đặt lại mật khẩu
            </Button>
          </form>
        )}
      </CardContent>
    </Card>
  );
}
