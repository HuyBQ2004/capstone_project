"use client";

import { useEffect } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { LogOut, ShieldCheck, X } from "lucide-react";
import { getRoleFromPathname } from "@/lib/roles";
import { useSidebarStore } from "@/lib/stores/sidebar-store";
import { cn } from "@/lib/utils";

export function Sidebar() {
  const pathname = usePathname();
  const role = getRoleFromPathname(pathname);
  const open = useSidebarStore((s) => s.open);
  const close = useSidebarStore((s) => s.close);

  // Mobile: tự đóng drawer khi chuyển trang
  useEffect(() => {
    close();
  }, [pathname, close]);

  if (!role) return null;

  return (
    <>
      {/* Backdrop mobile — bấm ra ngoài để đóng */}
      {open && (
        <div
          className="fixed inset-0 z-30 bg-black/40 lg:hidden"
          onClick={close}
          aria-hidden="true"
        />
      )}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 flex w-64 flex-col bg-sidebar text-sidebar-foreground transition-transform duration-200 lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full"
        )}
      >
      <div className="flex h-16 items-center gap-2.5 border-b border-white/10 px-5">
        <div className="flex size-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
          <ShieldCheck className="size-5" />
        </div>
        <div className="leading-tight">
          <p className="text-base font-bold text-white">SFMS</p>
          <p className="text-[11px] text-sidebar-foreground/80">
            An toàn thực phẩm học đường
          </p>
        </div>
        <button
          type="button"
          onClick={close}
          aria-label="Đóng menu"
          className="ml-auto rounded-lg p-1.5 hover:bg-white/10 lg:hidden"
        >
          <X className="size-5" />
        </button>
      </div>

      <div className="border-b border-white/10 px-5 py-3">
        <p className="text-[11px] uppercase tracking-wide text-sidebar-foreground/60">
          Vai trò
        </p>
        <p className="text-sm font-semibold text-white">{role.label}</p>
      </div>

      {/* Khoảng cách nhóm vừa đủ tách bạch: vai Đơn vị Suất ăn có 16 mục, nới rộng
          hơn là tràn màn hình 1080 và phải cuộn ngay từ đầu. */}
      <nav className="flex-1 space-y-3 overflow-y-auto px-3 py-3">
        {role.nav.map((group, gi) => (
          <div key={gi}>
            {group.title && (
              <p className="mb-1 px-2 text-[11px] font-semibold uppercase tracking-wide text-sidebar-foreground/50">
                {group.title}
              </p>
            )}
            <ul className="space-y-0.5">
              {group.items.map((item) => {
                const active =
                  pathname === item.href ||
                  pathname.startsWith(item.href + "/");
                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      className={cn(
                        "flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm transition-colors",
                        active
                          ? "bg-sidebar-active font-medium text-white"
                          : "hover:bg-white/10 hover:text-white"
                      )}
                    >
                      <item.icon className="size-4 shrink-0" />
                      <span className="truncate">{item.label}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </nav>

      <div className="border-t border-white/10 p-3">
        <Link
          href="/login"
          className="flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm hover:bg-white/10 hover:text-white"
        >
          <LogOut className="size-4" />
          Đăng xuất
        </Link>
      </div>
      </aside>
    </>
  );
}
