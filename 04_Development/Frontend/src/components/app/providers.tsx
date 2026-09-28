"use client";

import { useEffect, useRef, useState } from "react";
import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster, toast } from "sonner";
import { api, ApiError } from "@/lib/api/client";
import { requestPushToken } from "@/lib/firebase";
import { apiErrorMessage } from "@/lib/messages";
import { connectNotificationSocket } from "@/lib/notification-socket";
import { useAuthStore } from "@/lib/stores/auth-store";

export function Providers({ children }: { children: React.ReactNode }) {
  /*
   * Khôi phục phiên sau F5 (UC-034 · BR-004).
   *
   * Access token nằm ở memory nên reload là mất; refresh token thì nằm trong
   * cookie httpOnly và sống 14 ngày. Một lần gọi `/api/auth/refresh` lúc mount
   * đổi cookie đó lấy access token mới rồi nạp `/profile/me`.
   *
   * Chưa đăng nhập thì route handler trả 401 và `restoreSession` chỉ đặt trạng
   * thái `anonymous` — không phải lỗi, không toast.
   */
  const restoreSession = useAuthStore((s) => s.restoreSession);
  useEffect(() => {
    void restoreSession();
  }, [restoreSession]);

  /*
   * Web Push (FCM) — đăng ký token cho đúng phiên đang đăng nhập, không riêng lúc bấm nút
   * "Đăng nhập bằng Google" hay password: chạy MỘT chỗ, phản ứng theo `status`, để bắt đủ cả
   * 3 lối vào (password/OAuth/restoreSession sau F5) mà không lặp code ở 3 nơi.
   *
   * `registeredRef` chặn gọi lại mỗi lần re-render; reset khi về "anonymous" để nếu người
   * khác đăng nhập cùng máy (không reload trang) thì token vẫn được gắn lại đúng user mới.
   */
  const status = useAuthStore((s) => s.status);
  const registeredRef = useRef(false);
  useEffect(() => {
    if (status !== "authenticated") {
      registeredRef.current = false;
      return;
    }
    if (registeredRef.current) return;
    registeredRef.current = true;

    void requestPushToken()
      .then((token) => {
        if (!token) return;
        return api.post("/device-tokens", { token, platform: "WEB" });
      })
      .catch(() => {
        // Best-effort — quyền notification bị chặn hoặc backend chưa cấu hình Firebase (SFMS_FCM_ENABLED)
        // không được chặn luồng đăng nhập của người dùng.
      });
  }, [status]);

  /*
   * Lưới an toàn cho MỌI mutation hỏng (07/08/2026).
   *
   * Trước đây không có chỗ nào bắt lỗi mutation ở tầng chung: `defaultOptions`
   * chỉ cấu hình `queries`, axios interceptor cũng không toast. Đợt rà soát đo
   * được 58/131 lời gọi `.mutate()` không kèm `onError` ⇒ hỏng HOÀN TOÀN IM
   * LẶNG. Ca thật: NCC khai mặt hàng sai đơn vị, server trả 422 kèm `details`
   * rất rõ, màn `supplier/products` chỉ có `onSuccess` nên không hiện gì.
   *
   * TanStack Query v5 gọi CẢ HAI đường — handler này rồi mới tới `onError` của
   * từng `mutate` — nên nơi nào ĐÃ tự toast phải khai
   * `meta: { handledError: true }` (qua tham số `MutationOpts` của các factory
   * trong `lib/api/hooks.ts`), nếu không sẽ ra 2 toast chồng nhau.
   */
  const [queryClient] = useState(
    () =>
      new QueryClient({
        /*
         * ĐỌC hỏng cũng phải kêu (08/08/2026).
         *
         * Trước bản này chỉ MUTATION mới có lưới bắt lỗi chung; QUERY hỏng thì `data` là
         * `undefined`, mọi màn viết `data?.items ?? []` nên hiện y hệt "danh sách rỗng".
         * Đo được trong MỘT ngày, ba lỗi khác nhau đều đội lốt "chưa có dữ liệu":
         *   · `GET /menus?grade=Khối 1` → 422 "unknown filter field" ⇒ 3 màn Phụ huynh ghi
         *     "Chưa có thực đơn nào được công bố" trong khi thực đơn đã công bố;
         *   · `GET /grades` → 403 với vai Phụ huynh;
         *   · `GET /partners` rỗng vì lọc sai `type` (không lỗi, nhưng cùng biểu hiện).
         * Mỗi lần đều mất công truy ngược từ màn hình về API mới biết là LỖI chứ không phải
         * RỖNG. Nay toast ngay tại chỗ.
         *
         * BỎ QUA 401: phiên hết hạn đã có `AuthGate` + interceptor lo (đá về `/login`), toast
         * thêm chỉ gây nhiễu khi nhiều query cùng hỏng một lúc.
         */
        queryCache: new QueryCache({
          onError: (error, query) => {
            if (query.meta?.handledError) return;
            if (error instanceof ApiError && error.status === 401) return;
            toast.error(apiErrorMessage(error, "Không tải được dữ liệu cho màn hình này."));
          },
        }),
        mutationCache: new MutationCache({
          onError: (error, _vars, _ctx, mutation) => {
            if (mutation.meta?.handledError) return;
            toast.error(apiErrorMessage(error, "Không thực hiện được thao tác."));
          },
        }),
        defaultOptions: {
          queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false },
        },
      })
  );

  /*
   * Chuông real-time (WebSocket/STOMP, xem NotificationBroadcaster.java) — chỉ áp dụng cho
   * role có `schoolId` (topic đặt tên theo trường); Supplier chỉ có `partnerId`, chưa có
   * schoolId trực tiếp trên profile nên tạm KHÔNG connect — vẫn dùng polling 30s như cũ, không
   * mất tính năng, chỉ chưa tức thời (giới hạn đã biết, ghi rõ để không lặp lại như đã thật).
   */
  const schoolId = useAuthStore((s) => s.user?.schoolId);
  useEffect(() => {
    if (status !== "authenticated" || !schoolId) return;
    return connectNotificationSocket(schoolId, () => {
      void queryClient.invalidateQueries({ queryKey: ["notifications"] });
    });
  }, [status, schoolId, queryClient]);

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <Toaster richColors position="top-right" />
    </QueryClientProvider>
  );
}
