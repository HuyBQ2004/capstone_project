"use client";

import { useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { LogOut, ShieldCheck, X } from "lucide-react";
import { ROLES, getRoleFromPathname } from "@/lib/roles";
import { useAuthStore } from "@/lib/stores/auth-store";
import { useSidebarStore } from "@/lib/stores/sidebar-store";
import { usePagedList } from "@/lib/api/hooks";
import type { PartnershipDto } from "@/lib/api/types";
import { cn } from "@/lib/utils";

export function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const routeRole = getRoleFromPathname(pathname);
  const role = routeRole ?? (user?.role ? ROLES[user.role] : undefined);
  const open = useSidebarStore((s) => s.open);
  const close = useSidebarStore((s) => s.close);

  /*
   * Đơn vị suất ăn CHƯA hợp tác trường nào (BR-EXCLUSIVE-CATERER — tối đa 1 trường,
   * partnership_workspace.tsx dòng ~329 cùng điều kiện `status === "APPROVED"`) thì gần
   * hết nav (kho, thực đơn, kiểm thực...) dẫn tới trang bị chặn vì không có schoolId để
   * scope dữ liệu — rối hơn là giúp. Chỉ hiện "Hồ sơ đơn vị"/"Trường hợp tác" tới khi có
   * hợp tác thật (11/08/2026, theo Nam).
   */
  const isServiceCaterers = role?.key === "service-caterers";
  const { data: partnershipsPage } = usePagedList<PartnershipDto>(
    "supplier-partnerships",
    { pageSize: 100 },
    { enabled: isServiceCaterers }
  );
  const hasApprovedPartnership =
    !isServiceCaterers || (partnershipsPage?.items ?? []).some((p) => p.status === "APPROVED");
  const nav =
    isServiceCaterers && !hasApprovedPartnership && role
      ? role.nav.map((group) => ({
          ...group,
          items: group.items.filter(
            (item) => item.href === "/service-caterers/profile" || item.href === "/service-caterers/partnerships"
          ),
        })).filter((group) => group.items.length > 0)
      : role?.nav;

  async function handleLogout() {
    await logout();
    router.push("/login");
  }

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
        {nav?.map((group, gi) => (
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
        <button
          type="button"
          onClick={handleLogout}
          className="flex w-full items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm hover:bg-white/10 hover:text-white"
        >
          <LogOut className="size-4" />
          Đăng xuất
        </button>
      </div>
      </aside>
    </>
  );
}
