"use client";

/**
 * GuestCommon/05_ProfileManagement.md (UC-059/UC-138/UC-097) — Web: dropdown ở Topbar
 * + 06_ChangePassword.md (UC-011) + 09_NotificationSettings.md (UC-088) + Logout (UC-035).
 * BR-012: định dạng phone · BR-013: avatar ≤5MB JPG/PNG · BR-006: chính sách mật khẩu ·
 * BR-158/158b: Quiet Hours mặc định 22:00–06:00.
 */

import { useEffect, useRef, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import {
  Bell,
  ChevronDown,
  KeyRound,
  LogOut,
  Upload,
  UserPen,
} from "lucide-react";
import { toast } from "sonner";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label } from "@/components/ui/input";
import { getRoleFromPathname } from "@/lib/roles";
import { CURRENT_SCHOOL } from "@/lib/mock-data";
import { useAuthStore, DEMO_USERS } from "@/lib/stores/auth-store";

/** BR-012: +84xxxxxxxxx hoặc 0xxxxxxxxx (9–10 số sau đầu số) */
const PHONE_RE = /^(\+84|0)\d{9,10}$/;

function Toggle({
  checked,
  onChange,
  disabled,
}: {
  checked: boolean;
  onChange: (v: boolean) => void;
  disabled?: boolean;
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={() => onChange(!checked)}
      className={`relative h-5 w-9 shrink-0 rounded-full transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
        checked ? "bg-primary" : "bg-muted-foreground/30"
      }`}
    >
      <span
        className={`absolute top-0.5 size-4 rounded-full bg-white shadow transition-all ${
          checked ? "left-[18px]" : "left-0.5"
        }`}
      />
    </button>
  );
}

export function UserMenu() {
  const router = useRouter();
  const pathname = usePathname();
  const role = getRoleFromPathname(pathname);
  const storeUser = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const user = storeUser ?? (role ? DEMO_USERS[role.key] : null);

  const [open, setOpen] = useState(false);
  const [dialog, setDialog] = useState<"profile" | "password" | "notifications" | "logout" | null>(null);
  const ref = useRef<HTMLDivElement>(null);

  // Edit Profile (UC-097, E1–E6)
  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [avatarName, setAvatarName] = useState("");
  const fileRef = useRef<HTMLInputElement>(null);

  // Change Password (UC-011)
  const [current, setCurrent] = useState("");
  const [newPass, setNewPass] = useState("");
  const [confirm, setConfirm] = useState("");
  const [pwError, setPwError] = useState("");

  // Notification Settings (UC-088)
  const [push, setPush] = useState(true);
  const [email, setEmail] = useState(true);
  const [quietFrom, setQuietFrom] = useState("22:00");
  const [quietTo, setQuietTo] = useState("06:00");

  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  if (!user) return null;

  const phoneValid = phone === "" || PHONE_RE.test(phone);
  const profileChanged = fullName !== user.name || phone !== "" || avatarName !== "";

  const pwRules = [
    { label: "Tối thiểu 8 ký tự", ok: newPass.length >= 8 },
    { label: "Có chữ hoa và chữ thường", ok: /[a-z]/.test(newPass) && /[A-Z]/.test(newPass) },
    { label: "Có chữ số", ok: /\d/.test(newPass) },
    { label: "Có ký tự đặc biệt", ok: /[^A-Za-z0-9]/.test(newPass) },
  ];

  function openProfile() {
    if (!user) return;
    setFullName(user.name);
    setPhone("");
    setAvatarName("");
    setOpen(false);
    setDialog("profile");
  }

  function pickAvatar(file: File | undefined) {
    if (!file) return;
    if (!/\.(jpe?g|png)$/i.test(file.name)) {
      toast.error("Invalid image format. Please upload JPG or PNG. (MSG-009, BR-013)");
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error("Image exceeds the 5MB size limit. (MSG-008, BR-013)");
      return;
    }
    setAvatarName(file.name);
  }

  function saveProfile() {
    if (!fullName.trim()) {
      toast.error("Họ tên là bắt buộc.");
      return;
    }
    if (!phoneValid) {
      toast.error("Số điện thoại không đúng định dạng +84/0xx (BR-012).");
      return;
    }
    toast.success("Profile updated successfully. (MSG-029)");
    setDialog(null);
  }

  function savePassword() {
    if (!current) {
      setPwError("Vui lòng nhập mật khẩu hiện tại.");
      return;
    }
    if (pwRules.some((r) => !r.ok)) {
      setPwError("Mật khẩu mới chưa đạt chính sách bảo mật (MSG-063, BR-006).");
      return;
    }
    if (newPass !== confirm) {
      setPwError("Xác nhận mật khẩu không khớp.");
      return;
    }
    setPwError("");
    toast.success("Password changed successfully. (MSG-030) — Các phiên đăng nhập khác đã bị chấm dứt (UC-011).");
    setDialog(null);
    setCurrent(""); setNewPass(""); setConfirm("");
  }

  function saveNotifications() {
    toast.success("Đã lưu cài đặt thông báo — cảnh báo khẩn cấp không bị ảnh hưởng bởi Quiet Hours (BR-158).");
    setDialog(null);
  }

  function doLogout() {
    logout();
    toast.success("Logged out successfully. (MSG-027)");
    router.push("/login");
  }

  return (
    <div className="relative" ref={ref}>
      <button
        className="flex items-center gap-2.5 rounded-lg p-1 pr-2 hover:bg-muted"
        onClick={() => setOpen((o) => !o)}
        aria-label="Menu cá nhân"
      >
        <div className="flex size-9 items-center justify-center rounded-full bg-primary-soft text-sm font-semibold text-primary">
          {user.name.charAt(0)}
        </div>
        <div className="hidden text-left leading-tight sm:block">
          <p className="text-sm font-medium">{user.name}</p>
          <p className="text-xs text-muted-foreground">{role?.label}</p>
        </div>
        <ChevronDown className="size-4 text-muted-foreground" />
      </button>

      {open && (
        <div className="absolute right-0 top-full z-40 mt-2 w-72 max-w-[calc(100vw-2rem)] rounded-xl border bg-card p-2 shadow-lg">
          <div className="mb-1 flex items-center gap-3 rounded-lg bg-muted/60 p-3">
            <div className="flex size-11 items-center justify-center rounded-full bg-primary-soft text-base font-semibold text-primary">
              {user.name.charAt(0)}
            </div>
            <div className="min-w-0 leading-tight">
              <p className="truncate text-sm font-medium">{user.name}</p>
              <p className="truncate text-xs text-muted-foreground">{user.email}</p>
              <p className="truncate text-xs text-muted-foreground">
                {role?.label}{user.school ? ` · ${user.school}` : ""}
              </p>
            </div>
          </div>
          <button className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm hover:bg-muted" onClick={openProfile}>
            <UserPen className="size-4" /> Hồ sơ cá nhân (Edit Profile)
          </button>
          <button className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm hover:bg-muted" onClick={() => { setOpen(false); setPwError(""); setDialog("password"); }}>
            <KeyRound className="size-4" /> Đổi mật khẩu
          </button>
          <button className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm hover:bg-muted" onClick={() => { setOpen(false); setDialog("notifications"); }}>
            <Bell className="size-4" /> Cài đặt thông báo
          </button>
          <div className="my-1 border-t" />
          <button className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-destructive hover:bg-muted" onClick={() => { setOpen(false); setDialog("logout"); }}>
            <LogOut className="size-4" /> Đăng xuất
          </button>
        </div>
      )}

      {/* Edit Profile — UC-097 (E1–E6) */}
      <Dialog open={dialog === "profile"} onOpenChange={(o) => !o && setDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Hồ sơ cá nhân (UC-097)</DialogTitle>
            <DialogDescription>
              {role?.label} · {user.school ?? CURRENT_SCHOOL.name}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="flex size-14 items-center justify-center rounded-full bg-primary-soft text-lg font-semibold text-primary">
                {user.name.charAt(0)}
              </div>
              <div>
                <input
                  ref={fileRef}
                  type="file"
                  accept=".jpg,.jpeg,.png"
                  className="hidden"
                  onChange={(e) => pickAvatar(e.target.files?.[0])}
                />
                <Button type="button" variant="outline" size="sm" onClick={() => fileRef.current?.click()}>
                  <Upload /> Tải ảnh đại diện
                </Button>
                <p className="mt-1 text-xs text-muted-foreground">
                  JPG/PNG, tối đa 5MB (BR-013){avatarName && ` — đã chọn: ${avatarName}`}
                </p>
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pf-name">Họ và tên *</Label>
              <Input id="pf-name" value={fullName} onChange={(e) => setFullName(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pf-phone">Số điện thoại (BR-012)</Label>
              <Input
                id="pf-phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="+84xxxxxxxxx hoặc 0xxxxxxxxx"
              />
              {!phoneValid && (
                <p className="text-xs text-destructive">Định dạng không hợp lệ (+84 hoặc 0xx — BR-012).</p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pf-email">Email (không thể sửa)</Label>
              <Input id="pf-email" value={user.email} readOnly disabled />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog(null)}>Hủy</Button>
            <Button disabled={!profileChanged || !fullName.trim() || !phoneValid} onClick={saveProfile}>
              Lưu (MSG-029)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Change Password — UC-011 */}
      <Dialog open={dialog === "password"} onOpenChange={(o) => !o && setDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Đổi mật khẩu (UC-011)</DialogTitle>
            <DialogDescription>
              Mật khẩu mới không được trùng 3 mật khẩu gần nhất (BR-006).
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {pwError && <Alert variant="danger">{pwError}</Alert>}
            <div className="space-y-1.5">
              <Label htmlFor="cp-current">Mật khẩu hiện tại *</Label>
              <Input id="cp-current" type="password" value={current} onChange={(e) => setCurrent(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cp-new">Mật khẩu mới *</Label>
              <Input id="cp-new" type="password" value={newPass} onChange={(e) => setNewPass(e.target.value)} />
              <ul className="space-y-0.5 text-xs">
                {pwRules.map((r) => (
                  <li key={r.label} className={r.ok ? "text-success" : "text-muted-foreground"}>
                    {r.ok ? "✓" : "○"} {r.label}
                  </li>
                ))}
              </ul>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cp-confirm">Xác nhận mật khẩu mới *</Label>
              <Input id="cp-confirm" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog(null)}>Hủy</Button>
            <Button disabled={!current || !newPass || !confirm} onClick={savePassword}>
              Lưu mật khẩu (MSG-030)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Notification Settings — UC-088 */}
      <Dialog open={dialog === "notifications"} onOpenChange={(o) => !o && setDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cài đặt thông báo (UC-088)</DialogTitle>
            <DialogDescription>
              Quiet Hours mặc định 22:00–06:00; cảnh báo khẩn cấp không bị ảnh hưởng (BR-158).
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3">
            {/* Review 07/2026: Allergy Warnings & Food Safety Alerts là cảnh báo an toàn
                bắt buộc — KHÔNG cho phép tắt với mọi vai trò (BR-138). */}
            {[
              { label: "Push Notifications", value: push, set: setPush, locked: false, note: "" },
              { label: "Email Notifications (fallback — BR-159)", value: email, set: setEmail, locked: false, note: "" },
              { label: "Allergy Warnings", value: true, set: () => {}, locked: true, note: "Cảnh báo an toàn bắt buộc — không thể tắt với mọi vai trò (BR-138)" },
              { label: "Food Safety Alerts (BR-138)", value: true, set: () => {}, locked: true, note: "Cảnh báo an toàn bắt buộc — không thể tắt với mọi vai trò (BR-138)" },
            ].map((t) => (
              <div key={t.label} className="flex items-center justify-between gap-3 rounded-lg border p-3">
                <div>
                  <p className="text-sm">{t.label}</p>
                  {t.note && <p className="text-xs text-muted-foreground">{t.note}</p>}
                </div>
                <Toggle checked={t.value} onChange={t.set} disabled={t.locked} />
              </div>
            ))}
            <div className="rounded-lg border p-3">
              <p className="mb-2 text-sm">Quiet Hours (BR-158b)</p>
              <div className="flex items-center gap-2">
                <Input type="time" value={quietFrom} onChange={(e) => setQuietFrom(e.target.value)} className="w-32" />
                <span className="text-sm text-muted-foreground">đến</span>
                <Input type="time" value={quietTo} onChange={(e) => setQuietTo(e.target.value)} className="w-32" />
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog(null)}>Hủy</Button>
            <Button onClick={saveNotifications}>Lưu cài đặt</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Logout confirm — UC-035, BR-004 */}
      <Dialog open={dialog === "logout"} onOpenChange={(o) => !o && setDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Đăng xuất (UC-035)</DialogTitle>
            <DialogDescription>
              Bạn có chắc muốn đăng xuất? Phiên làm việc hiện tại sẽ kết thúc (BR-004).
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog(null)}>Hủy</Button>
            <Button variant="destructive" onClick={doLogout}>
              <LogOut /> Đăng xuất (MSG-027)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
