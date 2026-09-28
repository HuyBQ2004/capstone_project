"use client";

/**
 * Child Switcher — chỉ hiển thị cho actor Parent (route /parent/*).
 * Phụ huynh có nhiều con học cùng trường chọn con đang xem; mọi màn hình
 * Parent (dashboard, sức khỏe, dị ứng, bữa ăn…) đổi theo con được chọn.
 */

import { useEffect, useRef, useState } from "react";
import { usePathname } from "next/navigation";
import { Check, ChevronDown, UserRound } from "lucide-react";
import { toast } from "sonner";
import { useChildStore, useActiveChild, useParentChildren } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

export function ChildSwitcher() {
  const pathname = usePathname();
  const child = useActiveChild();
  const children = useParentChildren();
  const setActiveChild = useChildStore((s) => s.setActiveChild);
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  // Chỉ áp dụng cho actor Parent
  if (!pathname.startsWith("/parent")) return null;

  function pick(studentId: string) {
    const next = children.find((c) => c.studentId === studentId);
    setActiveChild(studentId);
    setOpen(false);
    if (next && next.studentId !== child.studentId) {
      toast.success(`Đang xem thông tin của ${next.name} — Lớp ${next.class}.`);
    }
  }

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-label="Đổi con đang xem"
        className="flex items-center gap-2 rounded-lg border px-3 py-1.5 text-sm hover:bg-muted"
      >
        <span className="flex size-6 items-center justify-center rounded-full bg-primary-soft text-primary">
          <UserRound className="size-3.5" />
        </span>
        <span className="min-w-0 text-left leading-tight">
          <span className="block max-w-28 truncate font-medium sm:max-w-44">{child.name}</span>
          <span className="block max-w-28 truncate text-xs text-muted-foreground sm:max-w-44">
            Lớp {child.class} · {child.studentId}
          </span>
        </span>
        <ChevronDown className="size-4 text-muted-foreground" />
      </button>

      {open && (
        <div className="absolute left-0 top-full z-40 mt-2 w-72 max-w-[calc(100vw-2rem)] rounded-xl border bg-card p-2 shadow-lg">
          <p className="px-2 pb-1.5 pt-1 text-xs font-medium uppercase text-muted-foreground">
            Con của bạn ({children.length})
          </p>
          {children.map((c) => {
            const active = c.studentId === child.studentId;
            return (
              <button
                key={c.studentId}
                type="button"
                onClick={() => pick(c.studentId)}
                className={cn(
                  "flex w-full items-center gap-2.5 rounded-lg px-2.5 py-2 text-left text-sm hover:bg-muted",
                  active && "bg-primary/5"
                )}
              >
                <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-primary-soft text-sm font-semibold text-primary">
                  {c.name.charAt(0)}
                </span>
                <span className="min-w-0 flex-1 leading-tight">
                  <span className="block truncate font-medium">{c.name}</span>
                  <span className="block truncate text-xs text-muted-foreground">
                    Lớp {c.class} · {c.studentId} · {c.school}
                  </span>
                </span>
                {active && <Check className="size-4 shrink-0 text-primary" />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
