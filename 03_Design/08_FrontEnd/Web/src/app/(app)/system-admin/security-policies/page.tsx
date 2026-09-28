"use client";

import { useEffect, useState } from "react";
import { Save, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label } from "@/components/ui/input";
import { usePagedList, useUpdate } from "@/lib/api/hooks";
import type { SecurityPolicy } from "@/lib/mock-data";

/**
 * 15_SecurityPolicies.md — UC-184 Configure Security Policies (UC mới, audit A8).
 * BR-002: khóa tài khoản sau N lần sai (mặc định 5, lockout 15 phút) ·
 * BR-004: session timeout (mặc định 30 phút) · BR-006: chính sách độ mạnh
 * mật khẩu (độ dài, ký tự đặc biệt, không trùng 3 mật khẩu gần nhất) ·
 * BR-170: mọi thay đổi ghi audit trail · MSG-029 khi lưu thành công.
 */
export default function SecurityPoliciesPage() {
  // Lưu THẬT vào db: màn đặt lại mật khẩu và màn đăng nhập đọc chính bản ghi này,
  // nên đổi ở đây là đổi hành vi thật, không còn là con số trang trí.
  const { data, isLoading } = usePagedList<SecurityPolicy>("security-policies", { pageSize: 5 });
  const policy = data?.items?.[0];
  const update = useUpdate<{ id: string } & Partial<SecurityPolicy>>("security-policies");

  const [maxAttempts, setMaxAttempts] = useState(5);
  const [lockoutMinutes, setLockoutMinutes] = useState(15);
  const [sessionTimeout, setSessionTimeout] = useState(30);
  const [minLength, setMinLength] = useState(8);
  const [requireSpecial, setRequireSpecial] = useState(true);
  const [requireNumber, setRequireNumber] = useState(true);
  const [noReuse, setNoReuse] = useState(true);
  const [error, setError] = useState("");

  const [loaded, setLoaded] = useState(false);
  useEffect(() => {
    if (!policy || loaded) return;
    setMaxAttempts(policy.maxLoginAttempts);
    setLockoutMinutes(policy.lockoutMinutes);
    setSessionTimeout(policy.sessionTimeoutMinutes);
    setMinLength(policy.passwordMinLength);
    setRequireSpecial(policy.requireSpecialChar);
    setRequireNumber(policy.requireNumber);
    setNoReuse(policy.noReuseLast3);
    setLoaded(true);
  }, [policy, loaded]);

  function save() {
    // UC-184 Exceptions: giá trị ngoài khoảng cho phép → validation error
    if (maxAttempts < 3 || maxAttempts > 10) {
      setError("Số lần đăng nhập sai tối đa phải trong khoảng 3–10 (BR-002).");
      return;
    }
    if (lockoutMinutes < 5 || lockoutMinutes > 60) {
      setError("Thời gian khóa tài khoản phải trong khoảng 5–60 phút (BR-002).");
      return;
    }
    if (sessionTimeout < 10 || sessionTimeout > 120) {
      setError("Session timeout phải trong khoảng 10–120 phút (BR-004).");
      return;
    }
    if (minLength < 8 || minLength > 32) {
      setError("Độ dài mật khẩu tối thiểu phải trong khoảng 8–32 ký tự (BR-006).");
      return;
    }
    setError("");
    if (!policy) return;
    update.mutate(
      {
        id: policy.id,
        maxLoginAttempts: maxAttempts,
        lockoutMinutes,
        sessionTimeoutMinutes: sessionTimeout,
        passwordMinLength: minLength,
        requireSpecialChar: requireSpecial,
        requireNumber,
        noReuseLast3: noReuse,
        updatedAt: new Date().toLocaleString("vi-VN"),
        updatedBy: "Nguyễn Hải Đăng (System Admin)",
      },
      {
        onSuccess: () =>
          toast.success("Cập nhật chính sách bảo mật thành công (MSG-029) — áp dụng ngay cho màn đăng nhập và đặt lại mật khẩu"),
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Chính sách bảo mật"
        description="UC-184 — cấu hình tham số BR-002/BR-004/BR-006; mọi thay đổi được ghi audit trail (BR-170)"
      />

      {error && (
        <Alert variant="danger" className="mb-4" title="Giá trị ngoài khoảng cho phép (UC-184 Exceptions)">
          {error}
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Đăng nhập & Khóa tài khoản (BR-002) · Session (BR-004)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="sp-attempts">Số lần đăng nhập sai tối đa trước khi khóa</Label>
              <Input
                id="sp-attempts"
                type="number"
                min={3}
                max={10}
                value={maxAttempts}
                onChange={(e) => setMaxAttempts(Number(e.target.value))}
              />
              <p className="text-xs text-muted-foreground">Mặc định 5 lần (BR-002).</p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sp-lockout">Thời gian khóa tài khoản (phút)</Label>
              <Input
                id="sp-lockout"
                type="number"
                min={5}
                max={60}
                value={lockoutMinutes}
                onChange={(e) => setLockoutMinutes(Number(e.target.value))}
              />
              <p className="text-xs text-muted-foreground">Mặc định 15 phút (BR-002).</p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sp-session">Session timeout (phút)</Label>
              <Input
                id="sp-session"
                type="number"
                min={10}
                max={120}
                value={sessionTimeout}
                onChange={(e) => setSessionTimeout(Number(e.target.value))}
              />
              <p className="text-xs text-muted-foreground">Mặc định 30 phút (BR-004).</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <ShieldCheck className="size-5 text-primary" /> Chính sách mật khẩu (BR-006)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="sp-minlen">Độ dài tối thiểu (ký tự)</Label>
              <Input
                id="sp-minlen"
                type="number"
                min={8}
                max={32}
                value={minLength}
                onChange={(e) => setMinLength(Number(e.target.value))}
              />
            </div>
            <div className="space-y-2 text-sm">
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  className="size-4 accent-primary"
                  checked={requireSpecial}
                  onChange={(e) => setRequireSpecial(e.target.checked)}
                />
                Bắt buộc ký tự đặc biệt
              </label>
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  className="size-4 accent-primary"
                  checked={requireNumber}
                  onChange={(e) => setRequireNumber(e.target.checked)}
                />
                Bắt buộc chữ số
              </label>
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  className="size-4 accent-primary"
                  checked={noReuse}
                  onChange={(e) => setNoReuse(e.target.checked)}
                />
                Không trùng 3 mật khẩu gần nhất (BR-006)
              </label>
            </div>
            <div className="border-t pt-4">
              <Button disabled={update.isPending || isLoading || !policy} onClick={save}>
                <Save /> Lưu chính sách (MSG-029)
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
