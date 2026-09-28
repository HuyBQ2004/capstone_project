"use client";

/**
 * Topbar chung — tích hợp Notification Center (GuestCommon/07-08) qua icon chuông
 * và Profile Management dropdown (GuestCommon/05-06-09) theo 13_Guest_Common_Screen_Flow.md
 * mục "Khác biệt bố cục" (Web: dropdown ở Topbar thay vì trang riêng).
 */

import { Menu, Search } from "lucide-react";
import { ChildSwitcher } from "@/components/app/child-switcher";
import { useSidebarStore } from "@/lib/stores/sidebar-store";
import { useAuthStore } from "@/lib/stores/auth-store";
import { NotificationBell } from "@/components/app/notification-bell";
import { UserMenu } from "@/components/app/user-menu";

export function Topbar() {
  const toggleSidebar = useSidebarStore((s) => s.toggle);
  const school = useAuthStore((s) => s.user?.school);
  const schoolId = useAuthStore((s) => s.user?.schoolId);

  return (
    <header className="sticky top-0 z-20 flex h-16 items-center justify-between gap-2 border-b bg-card px-4 sm:px-6">
      <div className="flex min-w-0 items-center gap-2 sm:gap-3">
        <button
          type="button"
          onClick={toggleSidebar}
          aria-label="Mở menu điều hướng"
          className="rounded-lg border p-2 hover:bg-muted lg:hidden"
        >
          <Menu className="size-4" />
        </button>
        <div className="relative hidden md:block">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <input
            placeholder="Tìm kiếm..."
            className="h-9 w-72 rounded-lg border bg-muted pl-8 pr-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          />
        </div>
        {/* Parent: đổi con đang xem (1 phụ huynh có thể có nhiều con cùng trường) */}
        <ChildSwitcher />
      </div>
      <div className="flex shrink-0 items-center gap-2 sm:gap-4">
        {school && (
          <div className="hidden text-right leading-tight lg:block">
            <p className="text-sm font-medium">{school}</p>
            {schoolId && <p className="text-xs text-muted-foreground">{schoolId}</p>}
          </div>
        )}
        <NotificationBell />
        <UserMenu />
      </div>
    </header>
  );
}
