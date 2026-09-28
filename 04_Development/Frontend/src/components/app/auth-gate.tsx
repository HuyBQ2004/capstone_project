"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { Loader2 } from "lucide-react";
import { ROLES } from "@/lib/roles";
import { useAuthStore } from "@/lib/stores/auth-store";

/**
 * Cổng xác thực cho toàn bộ nhóm route `(app)` — thêm 08/08/2026 (P0-2 của
 * `07_implement/11_UI_Audit_Report.md`).
 *
 * Trước bản này KHÔNG có cổng nào: `(app)/layout.tsx` render thẳng `AppShell`, và
 * `AppShell` render thẳng `children`. Store có dọn phiên khi refresh token chết
 * nhưng không ai đọc `status`, nên hai chuyện rất khác nhau lại hiện y hệt nhau:
 *
 *   · phiên chết giữa chừng (đăng xuất ở máy khác ⇒ BR-004 revoke) → mọi query 401
 *     → màn Kho vẫn vẽ đủ khung, đủ nút, chỉ thay dữ liệu bằng "Không có bản ghi
 *     nào." Người trực bếp đọc thành "kho trống" và có thể nhập bù / báo mất hàng.
 *   · kho thật sự rỗng.
 *
 * Cổng này còn dập luôn cơn bão 401 lúc tải trang (P2-9): trước đây children mount
 * ngay khi `status` còn `idle`/`loading`, bắn 7 query song song TRƯỚC khi
 * `/auth/refresh` trả access token ⇒ 401 cả 7, refresh xong retry thêm 7 lần nữa —
 * gấp đôi request và console luôn đỏ, làm lỗi thật chìm trong lỗi giả. Chờ tới
 * `authenticated` mới mount thì mỗi query chỉ đi đúng một lần.
 */
export function AuthGate({ children }: { children: React.ReactNode }) {
  const status = useAuthStore((s) => s.status);
  const role = useAuthStore((s) => s.user?.role);
  const router = useRouter();
  const pathname = usePathname();

  /*
   * Segment đầu của đường dẫn CHÍNH LÀ vai được phép: `/teacher/...`, `/school-admin/...`
   * (xem `ROLES` trong lib/roles.ts). Đường dẫn không thuộc vai nào (`/ai-chat`,
   * `/notifications`…) thì mọi vai đều vào được.
   */
  const pathRole = pathname?.split("/").filter(Boolean)[0];
  const wrongRole =
    status === "authenticated" && !!role && !!pathRole && pathRole in ROLES && pathRole !== role;

  useEffect(() => {
    if (status === "anonymous") {
      /*
       * Mang đường dẫn đang dở sang `?next=` để đăng nhập lại là quay đúng chỗ cũ,
       * không đá về dashboard rồi bắt người dùng tự tìm lại màn đang làm.
       */
      const next = encodeURIComponent(pathname || "/");
      router.replace(`/login?next=${next}`);
      return;
    }
    /*
     * ĐÚNG VAI MỚI ĐƯỢC VÀO — thêm 09/08/2026 sau khi Nam hỏi "sao không chặn khi login role
     * này vào url role khác".
     *
     * Đo thật trước khi sửa: đăng nhập System Admin rồi mở `/teacher/students` thì màn giáo
     * viên hiện ĐẦY ĐỦ, sidebar đổi hẳn sang menu giáo viên. Dữ liệu KHÔNG rò (backend trả 403
     * cho `/students` và `/special-meals` — `@PreAuthorize` làm đúng việc), nhưng người dùng
     * nhận một màn hỏng không giải thích được, và lộ nguyên cấu trúc chức năng của vai khác.
     *
     * Chuyển hướng về dashboard của CHÍNH vai đang đăng nhập thay vì hiện trang "403": người
     * gõ nhầm URL hoặc mở lại bookmark của tài khoản cũ chỉ muốn về chỗ làm việc của mình.
     */
    if (wrongRole) {
      router.replace(`/${role}/dashboard`);
    }
  }, [status, wrongRole, role, pathname, router]);

  if (status === "authenticated" && !wrongRole) return <>{children}</>;

  /*
   * `idle`/`loading` → đang đổi cookie httpOnly lấy access token.
   * `anonymous`  → đã bắn `router.replace` ở trên, giữ nguyên màn chờ trong lúc
   *                điều hướng để không loé ra một màn "rỗng" gây hiểu nhầm.
   */
  return (
    <div
      className="flex min-h-[60vh] items-center justify-center"
      aria-busy="true"
      aria-live="polite"
    >
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Loader2 className="size-4 animate-spin" />
        {status === "anonymous"
          ? "Đang chuyển tới trang đăng nhập…"
          : wrongRole
            ? "Trang này thuộc vai khác — đang đưa bạn về khu vực của mình…"
            : "Đang tải phiên làm việc…"}
      </div>
    </div>
  );
}
