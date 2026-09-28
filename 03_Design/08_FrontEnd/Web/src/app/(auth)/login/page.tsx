"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, EyeOff } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label } from "@/components/ui/input";
import { Alert } from "@/components/ui/alert";
import { usePagedList } from "@/lib/api/hooks";
import type { SecurityPolicy } from "@/lib/mock-data";
import { ROLE_LIST, type RoleKey } from "@/lib/roles";
import { useAuthStore, DEMO_USERS } from "@/lib/stores/auth-store";
import { cn } from "@/lib/utils";

/**
 * UC Login — GuestCommon/01_Login.md
 * Mock: chọn vai trò → lưu phiên vào Zustand store → vào đúng workspace
 * (chưa nối backend RBAC/JWT).
 * BR-002: khóa 15 phút sau 5 lần sai · BR-010: yêu cầu email đã xác minh.
 */
export default function LoginPage() {
  const router = useRouter();
  const login = useAuthStore((s) => s.login);
  const [role, setRole] = useState<RoleKey>("service-caterers");
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  /**
   * BR-002 — khoá tài khoản sau N lần đăng nhập sai.
   *
   * Đến 22/07/2026 quy tắc này chỉ tồn tại trong một dòng chú thích ở đầu file:
   * không đếm lần sai, không khoá gì cả, trong khi màn "Chính sách bảo mật" của
   * System Admin cho cấu hình đúng hai tham số đó. Nay đọc chính sách thật.
   */
  const { data: policyPage } = usePagedList<SecurityPolicy>("security-policies", { pageSize: 5 });
  const policy = policyPage?.items?.[0];
  const maxAttempts = policy?.maxLoginAttempts ?? 5;
  const lockoutMinutes = policy?.lockoutMinutes ?? 15;

  const [attempts, setAttempts] = useState(0);
  const [lockedUntil, setLockedUntil] = useState<number | null>(null);
  const locked = lockedUntil != null && lockedUntil > Date.now();
  const minutesLeft = locked ? Math.ceil((lockedUntil! - Date.now()) / 60000) : 0;

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (locked) return;
    if (email && !email.includes("@")) {
      const next = attempts + 1;
      setAttempts(next);
      if (next >= maxAttempts) {
        setLockedUntil(Date.now() + lockoutMinutes * 60000);
        setError(
          `Tài khoản tạm khoá ${lockoutMinutes} phút sau ${maxAttempts} lần đăng nhập sai (BR-002).`
        );
        return;
      }
      // MSG-002 — nói rõ còn bao nhiêu lần trước khi bị khoá
      setError(
        `Invalid credentials. Please check your email and password. Còn ${maxAttempts - next} lần thử.`
      );
      return;
    }
    setAttempts(0);
    setLockedUntil(null);
    login(role); // MSG-026: Login successful
    router.push(`/${role}/dashboard`);
  }

  return (
    <Card className="w-full max-w-lg">
      <CardHeader>
        <CardTitle>Đăng nhập</CardTitle>
        <CardDescription>
          Bản mock UI — chọn vai trò để xem workspace tương ứng
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <Alert variant="danger">{error}</Alert>}

          <div className="space-y-1.5">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder={DEMO_USERS[role].email}
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
                placeholder="••••••••"
                className="pr-10"
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

          <div className="space-y-1.5">
            <Label>Vai trò (mock)</Label>
            <div className="grid grid-cols-2 gap-2">
              {ROLE_LIST.map((r) => (
                <button
                  key={r.key}
                  type="button"
                  onClick={() => setRole(r.key)}
                  className={cn(
                    "flex items-center gap-2 rounded-lg border p-2.5 text-left text-sm transition-colors",
                    role === r.key
                      ? "border-primary bg-primary-soft font-medium text-primary"
                      : "hover:bg-muted"
                  )}
                >
                  <r.icon className="size-4 shrink-0" />
                  <span className="truncate">{r.label}</span>
                </button>
              ))}
            </div>
            <p className="text-xs text-muted-foreground">
              Đăng nhập với tài khoản demo: {DEMO_USERS[role].name} ({DEMO_USERS[role].email})
            </p>
          </div>

          <Button type="submit" className="w-full" disabled={locked}>
            {locked ? `Tạm khoá — thử lại sau ${minutesLeft} phút` : "Đăng nhập"}
          </Button>

          <div className="flex items-center justify-between text-sm">
            <Link
              href="/forgot-password"
              className="text-primary hover:underline"
            >
              Quên mật khẩu?
            </Link>
            <Link href="/register" className="text-primary hover:underline">
              Đăng ký (School/Supplier/Caterer)
            </Link>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
