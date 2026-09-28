"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label } from "@/components/ui/input";

/**
 * UC-032 Forgot Password — GuestCommon/02_ForgotPassword.md
 * BR-003: token hiệu lực 1 giờ, tối đa 1 yêu cầu / 15 phút / email.
 * Luồng: Send OTP → 04_VerifyOTP → 03_ResetPassword (MSG-030 ở bước cuối).
 */
export default function ForgotPasswordPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [sending, setSending] = useState(false);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSending(true);
    // Mock: sinh token + gửi OTP qua email (BR-003) rồi chuyển màn Verify OTP
    setTimeout(() => router.push("/verify-otp"), 600);
  }

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle>Quên mật khẩu</CardTitle>
        <CardDescription>
          Nhập email đã đăng ký — hệ thống sẽ gửi mã OTP xác minh (hiệu lực 1
          giờ, tối đa 1 yêu cầu/15 phút — BR-003)
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ten@truong.edu.vn"
            />
          </div>
          <Button type="submit" className="w-full" disabled={!email || sending}>
            {sending ? "Đang gửi mã OTP…" : "Gửi mã OTP"}
          </Button>
        </form>
        <div className="mt-4 text-center text-sm">
          <Link href="/login" className="text-primary hover:underline">
            ← Quay lại đăng nhập
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}
