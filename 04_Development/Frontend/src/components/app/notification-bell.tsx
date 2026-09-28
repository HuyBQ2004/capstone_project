"use client";

/**
 * GuestCommon/07_NotificationCenterList.md + 08_NotificationDetail.md.
 * Icon chuông ở Topbar → dropdown danh sách (badge đỏ/vàng/xám kèm label,
 * sắp xếp mới nhất trên đầu — BR-158); click item mở chi tiết (nguyên văn MSG
 * đã gửi) + nút deep-link nếu có (Navigation_Guidelines.md mục 3).
 *
 * Nối thật 05/08/2026 (MF10, `notifications` giờ 4/4) — `GET /notifications` đã tự lọc
 * theo VAI đang đăng nhập ở SERVER (`NotificationServiceImpl.listForCurrentUser()` —
 * `isVisibleToRole`), bỏ hẳn filter `audience`/`recipientRoles` phía client (mock cũ phải tự
 * lọc vì mock-server trả trần mọi bản ghi). `read` cũng dẫn xuất server (theo user đang đăng
 * nhập), không phải cột ghi trực tiếp — đánh dấu đã đọc qua `PATCH /{code}/read` (`usePatchAction`,
 * không body). Taxonomy đổi từ 4 giá trị cũ (`ALLERGY|FOOD_SAFETY|NEWS|SYSTEM`) sang ĐÚNG 7 giá
 * trị D6 thật (`NotificationType`) — `TYPE_META` viết lại hoàn toàn.
 * `POST /notifications` KHÔNG TỒN TẠI (ADR-M10-4 — thông báo là hệ quả SERVER tự phát qua
 * `NotificationPort`, không phải hành động người dùng bấm nút) — `lib/notify.ts` (`useNotify()`)
 * đã BỎ HẲN, cùng lúc gỡ khỏi 6 màn còn gọi (xem `real-resources.ts` khối MF10 để biết danh
 * sách + lý do từng chỗ).
 */

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import {
  AlertTriangle,
  Bell,
  ClipboardList,
  Clock,
  Newspaper,
  ShieldAlert,
} from "lucide-react";
import { Badge, type BadgeVariant } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { usePagedList, usePatchAction } from "@/lib/api/hooks";
import { formatVnDateTime } from "@/lib/format";
import type { NotificationDto } from "@/lib/api/types";

const TYPE_META: Record<string, { label: string; variant: BadgeVariant; icon: typeof Bell }> = {
  MENU_PUBLISHED: { label: "Thực đơn mới", variant: "info", icon: Newspaper },
  INSPECTION_ALERT: { label: "Cảnh báo kiểm thực", variant: "warning", icon: AlertTriangle },
  CAPA_ASSIGNED: { label: "Yêu cầu khắc phục", variant: "warning", icon: ClipboardList },
  EXPIRY_ALERT: { label: "Cảnh báo hết hạn", variant: "warning", icon: Clock },
  INCIDENT_ALERT: { label: "Sự cố an toàn thực phẩm", variant: "danger", icon: ShieldAlert },
  FOOD_RECALL: { label: "Thu hồi thực phẩm", variant: "danger", icon: AlertTriangle },
  ALLERGY_ALERT: { label: "Dị ứng", variant: "danger", icon: ShieldAlert },
};
const DEFAULT_META = { label: "Thông báo", variant: "neutral" as BadgeVariant, icon: Bell };

export function NotificationBell() {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<NotificationDto | null>(null);
  const ref = useRef<HTMLDivElement>(null);

  const { data } = usePagedList<NotificationDto>("notifications", { pageSize: 50 });
  const markRead = usePatchAction("notifications");
  const items = data?.items ?? [];
  const unread = items.filter((n) => !n.read).length;

  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  function openDetail(n: NotificationDto) {
    setSelected(n);
    setOpen(false);
    if (!n.read && n.id) markRead.mutate({ id: n.id, action: "read" });
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
                const meta = TYPE_META[n.type ?? ""] ?? DEFAULT_META;
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
                        <span className="text-[11px] text-muted-foreground">{formatVnDateTime(n.sentAt)}</span>
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
                  <Badge variant={(TYPE_META[selected.type ?? ""] ?? DEFAULT_META).variant}>
                    {(TYPE_META[selected.type ?? ""] ?? DEFAULT_META).label}
                  </Badge>
                  <span className="text-xs text-muted-foreground">{formatVnDateTime(selected.sentAt)}</span>
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
