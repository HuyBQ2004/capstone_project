"use client";

/**
 * GuestCommon/07_NotificationCenterList.md + 08_NotificationDetail.md.
 * Icon chuông ở Topbar → dropdown danh sách (badge đỏ/vàng/xám kèm label,
 * sắp xếp mới nhất trên đầu — BR-158); click item mở chi tiết (nguyên văn MSG
 * đã gửi) + nút deep-link nếu có (Navigation_Guidelines.md mục 3).
 */

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { AlertTriangle, Bell, Info, Newspaper, ShieldAlert } from "lucide-react";
import { Badge, type BadgeVariant } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { usePagedList, useUpdate } from "@/lib/api/hooks";
import { useAuthStore } from "@/lib/stores/auth-store";
import type { UserNotification } from "@/lib/mock-data";

const TYPE_META: Record<UserNotification["type"], { label: string; variant: BadgeVariant; icon: typeof Bell }> = {
  ALLERGY: { label: "Dị ứng", variant: "danger", icon: ShieldAlert },
  FOOD_SAFETY: { label: "ATTP", variant: "danger", icon: AlertTriangle },
  NEWS: { label: "Tin tức", variant: "warning", icon: Newspaper },
  SYSTEM: { label: "Hệ thống", variant: "neutral", icon: Info },
};

export function NotificationBell() {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<UserNotification | null>(null);
  const ref = useRef<HTMLDivElement>(null);

  const { data } = usePagedList<UserNotification>("notifications", { pageSize: 50 });
  const update = useUpdate<{ id: string; read?: boolean }>("notifications");
  /**
   * Chỉ hiện thông báo gửi cho VAI đang đăng nhập. Bỏ trống người nhận = thông
   * báo toàn hệ thống (bảo trì, sự cố hạ tầng) nên vai nào cũng thấy.
   */
  const role = useAuthStore((s) => s.user?.role);
  const items = (data?.items ?? []).filter(
    (n) => n.audience === "ALL" || (role != null && !!n.recipientRoles?.includes(role))
  );
  const unread = items.filter((n) => !n.read).length;

  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  function openDetail(n: UserNotification) {
    setSelected(n);
    setOpen(false);
    if (!n.read) update.mutate({ id: n.id, read: true });
  }

  return (
    <div className="relative" ref={ref}>
      <button
        className="relative flex size-9 items-center justify-center rounded-lg border bg-card hover:bg-muted"
        aria-label="Thông báo"
        onClick={() => setOpen((o) => !o)}
      >
        <Bell className="size-4" />
        {unread > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex size-4 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">
            {unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-full z-40 mt-2 w-96 max-w-[calc(100vw-2rem)] rounded-xl border bg-card shadow-lg">
          <div className="flex items-center justify-between border-b px-4 py-2.5">
            <p className="text-sm font-semibold">Thông báo</p>
            <span className="text-xs text-muted-foreground">{unread} chưa đọc</span>
          </div>
          <div className="max-h-96 overflow-y-auto p-1.5">
            {items.length === 0 ? (
              <p className="px-4 py-8 text-center text-sm text-muted-foreground">
                Chưa có thông báo nào — các cảnh báo dị ứng/ATTP sẽ hiển thị tại đây.
              </p>
            ) : (
              items.map((n) => {
                const meta = TYPE_META[n.type];
                return (
                  <button
                    key={n.id}
                    onClick={() => openDetail(n)}
                    className={`flex w-full items-start gap-2.5 rounded-lg px-2.5 py-2.5 text-left hover:bg-muted ${
                      !n.read ? "bg-primary-soft/40" : ""
                    }`}
                  >
                    <meta.icon className="mt-0.5 size-4 shrink-0 text-muted-foreground" />
                    <div className="min-w-0">
                      <div className="mb-0.5 flex items-center gap-1.5">
                        <Badge variant={meta.variant}>{meta.label}</Badge>
                        <span className="text-[11px] text-muted-foreground">{n.sentAt}</span>
                        {!n.read && <span className="size-1.5 rounded-full bg-primary" />}
                      </div>
                      <p className={`truncate text-sm ${!n.read ? "font-medium" : ""}`}>{n.title}</p>
                      <p className="line-clamp-1 text-xs text-muted-foreground">{n.content}</p>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>
      )}

      {/* 08_NotificationDetail */}
      <Dialog open={selected !== null} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent>
          {selected && (
            <>
              <DialogHeader>
                <div className="mb-1 flex items-center gap-2">
                  <Badge variant={TYPE_META[selected.type].variant}>
                    {TYPE_META[selected.type].label}
                  </Badge>
                  <span className="text-xs text-muted-foreground">{selected.sentAt}</span>
                </div>
                <DialogTitle>{selected.title}</DialogTitle>
                <DialogDescription className="sr-only">Chi tiết thông báo</DialogDescription>
              </DialogHeader>
              {/* Nội dung đầy đủ — nguyên văn MSG đã gửi, không viết lại (UI_Design_Guidelines mục 4) */}
              <p className="whitespace-pre-line text-sm leading-6">{selected.content}</p>
              {selected.link && (
                <Button
                  className="w-full"
                  onClick={() => {
                    const href = selected.link!;
                    setSelected(null);
                    router.push(href);
                  }}
                >
                  {selected.linkLabel ?? "Mở liên kết"}
                </Button>
              )}
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
