"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";

/**
 * GuestCommon/04_VerifyOTP.md — bước con của UC-032 (Forgot Password),
 * dùng lại khi MSG-062 yêu cầu xác minh thiết bị mới.
 * BR-003: OTP có hạn — Countdown Timer + Resend OTP (disabled khi countdown còn chạy).
 * Xác minh đúng → 03_ResetPassword.
 */
const OTP_SECONDS = 120;

export default function VerifyOtpPage() {
  const router = useRouter();
  const [digits, setDigits] = useState(["", "", "", "", "", ""]);
  const [seconds, setSeconds] = useState(OTP_SECONDS);
  const [verifying, setVerifying] = useState(false);
  const refs = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    if (seconds <= 0) return;
    const t = setInterval(() => setSeconds((s) => s - 1), 1000);
    return () => clearInterval(t);
  }, [seconds]);

  const expired = seconds <= 0;
  const filled = digits.every((d) => d !== "");
  const mm = String(Math.floor(Math.max(seconds, 0) / 60)).padStart(2, "0");
  const ss = String(Math.max(seconds, 0) % 60).padStart(2, "0");

  function handleChange(index: number, value: string) {
    if (!/^\d?$/.test(value)) return;
    const next = [...digits];
    next[index] = value;
    setDigits(next);
    if (value && index < 5) refs.current[index + 1]?.focus();
  }

  function verify() {
    setVerifying(true);
    // Mock xác thực mã — thành công → Reset Password
    setTimeout(() => router.push("/reset-password"), 600);
  }

  function resend() {
    setDigits(["", "", "", "", "", ""]);
    setSeconds(OTP_SECONDS);
    refs.current[0]?.focus();
    toast.success("Đã gửi lại mã OTP mới (BR-003).");
  }

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle>Xác minh OTP</CardTitle>
        <CardDescription>Nhập mã 6 số đã gửi đến email của bạn</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex justify-between gap-2">
          {digits.map((digit, i) => (
            <input
              key={i}
              ref={(el) => {
                refs.current[i] = el;
              }}
              value={digit}
              onChange={(e) => handleChange(i, e.target.value)}
              inputMode="numeric"
              maxLength={1}
              disabled={expired}
              className="h-12 w-12 rounded-lg border bg-card text-center text-xl font-bold focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
            />
          ))}
        </div>

        {/* Countdown Timer (BR-003) */}
        <p className={`text-center text-sm ${expired ? "text-destructive" : "text-muted-foreground"}`}>
          {expired
            ? "Mã OTP đã hết hạn — bấm Gửi lại để nhận mã mới."
            : `Mã có hiệu lực trong ${mm}:${ss}`}
        </p>

        <Button className="w-full" disabled={!filled || expired || verifying} onClick={verify}>
          {verifying ? "Đang xác minh…" : "Xác minh"}
        </Button>
        <p className="text-center text-sm text-muted-foreground">
          Chưa nhận được mã?{" "}
          <button
            className="text-primary hover:underline disabled:cursor-not-allowed disabled:opacity-50"
            disabled={!expired}
            onClick={resend}
            title={!expired ? "Chỉ gửi lại khi mã hiện tại hết hạn" : ""}
          >
            Gửi lại
          </button>
        </p>
        <div className="text-center text-sm">
          <Link href="/forgot-password" className="text-primary hover:underline">
            ← Quay lại
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}
