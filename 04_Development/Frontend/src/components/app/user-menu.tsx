"use client";

/**
 * GuestCommon/05_ProfileManagement.md (UC-059/UC-138/UC-097) — Web: dropdown ở Topbar
 * + 06_ChangePassword.md (UC-011) + 09_NotificationSettings.md (UC-088) + Logout (UC-035).
 * BR-012: định dạng phone · BR-013: avatar ≤5MB JPG/PNG · BR-006: chính sách mật khẩu ·
 * BR-158/158b: Quiet Hours mặc định 22:00–06:00.
 */

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import {
  Bell,
  ChevronDown,
  KeyRound,
  LogOut,
  ShieldCheck,
  Upload,
  UserPen,
} from "lucide-react";
import { toast } from "sonner";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
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
import { api } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";
import { UserAvatar } from "@/components/app/user-avatar";
import { useAuthStore } from "@/lib/stores/auth-store";
import { useDetail } from "@/lib/api/hooks";
import type { NotificationPreferenceDto, PartnerDetailDto, UpdatePreferenceDto } from "@/lib/api/types";

/** BR-012: +84xxxxxxxxx hoặc 0xxxxxxxxx (9–10 số sau đầu số) */
const PHONE_RE = /^(\+84|0)\d{9,10}$/;

/**
 * 14/08/2026 (Nam) — trước đây dropdown Topbar có dialog "Cài đặt thông báo" RIÊNG, model
 * đơn giản (push/email + Quiet Hours) nhưng `saveNotifications()` chỉ toast, KHÔNG gọi API
 * nào — hàng giả. Trang `/service-caterers/notification-settings` mới là bản thật (GET/PUT
 * `/notification-preferences/me`), nhưng chỉ có ở 1 role và không sửa được Quiet Hours. Nay
 * gộp: dialog này dùng model THẬT (7 loại `NOTIFICATION_TYPES` khớp `NotificationType` D6 +
 * `emailFallback` + `quietStart`/`quietEnd`) cho MỌI role — Quiet Hours giờ sửa được thật,
 * khác bản trang cũ (chỉ hiển thị tĩnh). Trang riêng đã xoá — đây là nơi duy nhất còn lại.
 */
const NOTIFICATION_TYPES: { key: string; label: string; critical?: boolean }[] = [
  { key: "MENU_PUBLISHED", label: "Thực đơn được duyệt / công bố" },
  { key: "INSPECTION_ALERT", label: "Cảnh báo kiểm thực (bước fail, tái kiểm)" },
  { key: "CAPA_ASSIGNED", label: "CAPA đã được Ban giám hiệu duyệt — chờ bếp nhận việc" },
  { key: "EXPIRY_ALERT", label: "Nguyên liệu sắp hết hạn (FEFO)" },
  { key: "INCIDENT_ALERT", label: "Sự cố an toàn thực phẩm", critical: true },
  { key: "FOOD_RECALL", label: "Thu hồi thực phẩm khẩn cấp", critical: true },
  { key: "ALLERGY_ALERT", label: "Cảnh báo dị ứng khi phục vụ", critical: true },
];

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
  const restoreSession = useAuthStore((s) => s.restoreSession);
  const user = storeUser;

  const [open, setOpen] = useState(false);
  const [dialog, setDialog] = useState<"profile" | "password" | "notifications" | "logout" | null>(null);
  const ref = useRef<HTMLDivElement>(null);

  // Edit Profile (UC-097, E1–E6)
  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [avatarName, setAvatarName] = useState("");
  // ⚠️ Hook PHẢI đứng trước mọi early return (rules-of-hooks) — bẫy đã ghi ở
  // `09_Dev_Runbook.md` mục 5, và tôi vừa mắc lại đúng nó khi thêm ô xem trước ảnh.
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  // Change Password (UC-011)
  const [current, setCurrent] = useState("");
  const [newPass, setNewPass] = useState("");
  const [confirm, setConfirm] = useState("");
  const [pwError, setPwError] = useState("");

  // Notification Settings (UC-088) — GET/PUT /notification-preferences/me thật, chỉ tải khi
  // dialog đang mở (đa số người dùng không bao giờ mở, khỏi gọi API thừa mỗi lần load trang).
  const qc = useQueryClient();
  const { data: notifPrefs, isLoading: notifLoading } = useQuery({
    queryKey: ["notification-preferences", "me"],
    queryFn: async () => {
      const { data } = await api.get<NotificationPreferenceDto>("/notification-preferences/me");
      return data;
    },
    enabled: dialog === "notifications",
  });
  const [typeEnabled, setTypeEnabled] = useState<Record<string, boolean>>(
    Object.fromEntries(NOTIFICATION_TYPES.map((t) => [t.key, true]))
  );
  const [emailFallback, setEmailFallback] = useState(true);
  const [quietFrom, setQuietFrom] = useState("22:00");
  const [quietTo, setQuietTo] = useState("06:00");

  useEffect(() => {
    if (!notifPrefs) return;
    if (notifPrefs.typeEnabled) setTypeEnabled((prev) => ({ ...prev, ...notifPrefs.typeEnabled }));
    if (notifPrefs.emailFallback !== undefined) setEmailFallback(notifPrefs.emailFallback);
    if (notifPrefs.quietStart) setQuietFrom(notifPrefs.quietStart);
    if (notifPrefs.quietEnd) setQuietTo(notifPrefs.quietEnd);
  }, [notifPrefs]);

  const saveNotificationsMutation = useMutation({
    meta: { handledError: true },
    mutationFn: async () => {
      const body: UpdatePreferenceDto = { typeEnabled, emailFallback, quietStart: quietFrom, quietEnd: quietTo };
      const { data } = await api.put<NotificationPreferenceDto>("/notification-preferences/me", body);
      return data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["notification-preferences"] });
      toast.success("Đã lưu cài đặt thông báo — cảnh báo khẩn cấp không bị ảnh hưởng bởi Quiet Hours (BR-158).");
      setDialog(null);
    },
    onError: (err) => toast.error(apiErrorMessage(err, "Không lưu được cài đặt thông báo.")),
  });

  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  /*
   * 13/08/2026 (theo Nam) — hồ sơ cá nhân trước đây chỉ hiện tên NGƯỜI (`user.name`), không
   * thấy tên công ty đâu cả với tài khoản NCC/Đơn vị suất ăn — dễ nhầm vì `UserAccount.name`
   * từ 11/08/2026 đã sửa thành tên người đại diện, không còn là tên doanh nghiệp (xem ghi chú
   * ở `PartnerRegistrationServiceImpl`). Tên công ty nằm ở `Partner.name`, đọc thêm qua
   * `GET /partners/me` — CHỈ ĐỌC ở đây, sửa tên công ty là việc của màn hồ sơ đối tác riêng
   * (`supplier/profile`/`service-caterers/profile`), không lẫn vào hồ sơ cá nhân này.
   */
  const isPartnerRole = user?.role === "supplier" || user?.role === "service-caterers";
  const { data: myPartner } = useDetail<PartnerDetailDto>("partners", isPartnerRole ? "me" : undefined);

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
    /*
     * XEM TRƯỚC NGAY. Trước đây chọn file xong chỉ hiện dòng chữ "đã chọn: bdio.jpg" còn vòng
     * tròn vẫn là chữ cái, nên không có cách nào biết mình chọn đúng ảnh chưa — và cũng không
     * phân biệt được với trường hợp tính năng hỏng.
     */
    setAvatarPreview((old) => {
      if (old) URL.revokeObjectURL(old);
      return URL.createObjectURL(file);
    });
  }

  /**
   * UC-097 — `PATCH /api/v1/profile/me` (MF01, thật từ 02/08/2026).
   *
   * `UpdateProfileRequest` chỉ có `name` + `phone`: role/schoolId/status KHÔNG nằm
   * trong DTO, nên không có đường tự nâng quyền qua màn hồ sơ (MF01-TC-SEC-005 a).
   * Ảnh đại diện đi endpoint riêng `POST /profile/me/photo` (multipart).
   */
  async function saveProfile() {
    if (!fullName.trim()) {
      toast.error("Họ tên là bắt buộc.");
      return;
    }
    if (!phoneValid) {
      toast.error("Số điện thoại không đúng định dạng +84/0xx (BR-012).");
      return;
    }
    try {
      await api.patch("/profile/me", {
        name: fullName.trim(),
        // Backend không nhận khoảng trắng trong SĐT (`VN_PHONE_REGEX`).
        ...(phone ? { phone: phone.replace(/[\s.]/g, "") } : {}),
      });
      const file = fileRef.current?.files?.[0];
      if (file) {
        const form = new FormData();
        form.append("file", file);
        await api.post("/profile/me/photo", form);
      }
      // Nạp lại phiên để tên/ảnh mới hiện ngay trên topbar.
      await restoreSession();
      // Ảnh thật từ server đã về, bỏ bản xem trước (và nhả object URL, tránh rò bộ nhớ).
      setAvatarPreview((old) => {
        if (old) URL.revokeObjectURL(old);
        return null;
      });
      toast.success("Profile updated successfully. (MSG-029)");
      setDialog(null);
    } catch (err) {
      toast.error(apiErrorMessage(err, "Không cập nhật được hồ sơ."));
    }
  }

  /**
   * UC-011 — `PATCH /api/v1/account/password`.
   *
   * ⚠️ `ChangePasswordRequest` KHÔNG có `userId`: chủ thể lấy từ JWT. Nhận `userId`
   * từ body cho phép giáo viên đổi mật khẩu của School Admin (MF01-TC-SEC-005 b).
   * BR-006 (chính sách + không trùng 3 MK gần nhất) do backend thi hành — các luật
   * dưới đây chỉ để phản hồi sớm, không phải chốt chặn.
   */
  async function savePassword() {
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
    try {
      await api.patch("/account/password", {
        currentPassword: current,
        newPassword: newPass,
      });
      toast.success("Password changed successfully. (MSG-030) — Các phiên đăng nhập khác đã bị chấm dứt (UC-011).");
      setDialog(null);
      setCurrent(""); setNewPass(""); setConfirm("");
    } catch (err) {
      setPwError(apiErrorMessage(err, "Không đổi được mật khẩu."));
    }
  }

  /**
   * UC-035 — `logout()` nay là async: nó gọi `POST /auth/logout` (revoke refresh
   * token phía server, BR-004) rồi xóa cookie httpOnly. Phải `await` trước khi
   * điều hướng, nếu không `/login` mount lại và `restoreSession()` có thể đổi
   * cookie CHƯA kịp xóa lấy access token mới — người dùng bấm Đăng xuất xong vẫn
   * còn phiên.
   */
  async function doLogout() {
    await logout();
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
        <UserAvatar name={user.name} avatarUrl={user.avatarUrl} className="size-9 text-sm" />
        <div className="hidden text-left leading-tight sm:block">
          <p className="text-sm font-medium">{user.name}</p>
          <p className="text-xs text-muted-foreground">{role?.label}</p>
        </div>
        <ChevronDown className="size-4 text-muted-foreground" />
      </button>

      {open && (
        <div className="absolute right-0 top-full z-40 mt-2 w-72 max-w-[calc(100vw-2rem)] rounded-xl border bg-card p-2 shadow-lg">
          <div className="mb-1 flex items-center gap-3 rounded-lg bg-muted/60 p-3">
            <UserAvatar name={user.name} avatarUrl={user.avatarUrl} className="size-11 text-base" />
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
          {/*
            Điều 7 Chính sách — chủ thể dữ liệu có quyền được biết dữ liệu của mình bị xử lý thế
            nào. Quyền đó chỉ có thật khi chính sách nằm ở chỗ tìm được từ mọi màn, không phải
            chôn trong trang đăng nhập mà người đã đăng nhập chẳng bao giờ quay lại.
          */}
          <Link
            href="/privacy"
            target="_blank"
            rel="noopener noreferrer"
            className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm hover:bg-muted"
            onClick={() => setOpen(false)}
          >
            <ShieldCheck className="size-4" /> Chính sách bảo vệ dữ liệu cá nhân
          </Link>
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
              {user.school ? `${role?.label} · ${user.school}` : role?.label}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <UserAvatar
                name={user.name}
                avatarUrl={user.avatarUrl}
                previewUrl={avatarPreview}
                className="size-14 text-lg"
              />
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
            {isPartnerRole && (
              <div className="space-y-1.5">
                <Label htmlFor="pf-company">Công ty (không sửa được ở đây)</Label>
                <Input id="pf-company" value={myPartner?.name ?? "—"} readOnly disabled />
              </div>
            )}
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

      {/* Notification Settings — UC-088, GET/PUT /notification-preferences/me thật */}
      <Dialog open={dialog === "notifications"} onOpenChange={(o) => !o && setDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cài đặt thông báo (UC-088)</DialogTitle>
            <DialogDescription>
              Quiet Hours chỉ hoãn thông báo thường; cảnh báo CRITICAL (sự cố, thu hồi, dị ứng)
              luôn gửi ngay, không bị ảnh hưởng (BR-138/BR-158).
            </DialogDescription>
          </DialogHeader>
          <div className="max-h-[60vh] space-y-3 overflow-y-auto pr-1">
            {NOTIFICATION_TYPES.map((t) => (
              <div key={t.key} className="flex items-center justify-between gap-3 rounded-lg border p-3">
                <div>
                  <p className="text-sm">{t.label}</p>
                  {t.critical && (
                    <p className="mt-1">
                      <Badge variant="critical">CRITICAL — luôn gửi, không thể tắt (BR-138)</Badge>
                    </p>
                  )}
                </div>
                <Toggle
                  checked={t.critical ? true : (typeEnabled[t.key] ?? true)}
                  onChange={(v) => setTypeEnabled((prev) => ({ ...prev, [t.key]: v }))}
                  disabled={t.critical || notifLoading}
                />
              </div>
            ))}
            <div className="flex items-center justify-between gap-3 rounded-lg border p-3">
              <div>
                <p className="text-sm">Email Notifications (fallback — BR-159)</p>
                <p className="text-xs text-muted-foreground">Tự động chuyển email khi push thất bại</p>
              </div>
              <Toggle checked={emailFallback} onChange={setEmailFallback} disabled={notifLoading} />
            </div>
            <div className="rounded-lg border p-3">
              <p className="mb-2 text-sm">Quiet Hours (BR-158b)</p>
              <div className="flex items-center gap-2">
                <Input type="time" value={quietFrom} onChange={(e) => setQuietFrom(e.target.value)} className="w-32" disabled={notifLoading} />
                <span className="text-sm text-muted-foreground">đến</span>
                <Input type="time" value={quietTo} onChange={(e) => setQuietTo(e.target.value)} className="w-32" disabled={notifLoading} />
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog(null)}>Hủy</Button>
            <Button disabled={notifLoading || saveNotificationsMutation.isPending} onClick={() => saveNotificationsMutation.mutate()}>
              Lưu cài đặt
            </Button>
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
