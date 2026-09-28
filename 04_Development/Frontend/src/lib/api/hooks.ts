"use client";

/**
 * TanStack Query hooks dùng chung cho mọi resource.
 * Server-style pagination: page/pageSize/search/filter đi qua query params,
 * mock adapter xử lý phía "server" (mock-server.ts).
 */

import { useMemo } from "react";
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { api, type ListParams, type Paged } from "./client";
import { specialMenuMeals, type MenuVariant } from "@/lib/domain-utils";

/**
 * Cờ đi kèm mọi factory mutation bên dưới, đọc lại ở `MutationCache.onError`
 * (`components/app/providers.tsx`).
 *
 * Mặc định (không truyền gì) mutation nào hỏng cũng được handler TOÀN CỤC toast
 * — trước 07/08/2026 không có handler này nên 58 lời gọi `.mutate()` không kèm
 * `onError` thất bại HOÀN TOÀN IM LẶNG: bấm Lưu, không toast, dialog đứng im.
 *
 * `handledError: true` = "màn này tự lo lỗi rồi, đừng toast hộ". Bắt buộc phải
 * khai ở những nơi đã tự `onError`/`try…catch`, vì TanStack Query v5 gọi CẢ HAI
 * (handler toàn cục rồi mới tới callback của từng `mutate`) ⇒ không có cờ thì ra
 * 2 toast chồng nhau.
 */
export interface MutationOpts {
  handledError?: boolean;
}

/** Chuyển `MutationOpts` thành `meta` của useMutation (undefined khi không khai gì). */
function metaOf(opts: MutationOpts): Record<string, unknown> | undefined {
  return opts.handledError ? { handledError: true } : undefined;
}

/**
 * `forceFresh`: bỏ qua `staleTime` mặc định (30s) và luôn refetch khi mount —
 * dùng cho những chỗ ĐỌC RỒI GHI NGAY dựa trên dữ liệu vừa đọc (VD: "Tạo
 * chuyến giao" đọc `batchAssignments` của đơn để ghi vào Delivery). Nếu không,
 * `keepPreviousData` sẽ âm thầm trả bản ghi CŨ (không có loading state) trong
 * lúc invalidateQueries của một mutation vừa xong còn đang refetch nền — dễ
 * gặp nhất khi mutation đó `router.push` sang trang này ngay lập tức (nhận xét
 * 23/07/2026: đây là nguyên nhân "gán 2 lô nhưng chuyến giao chỉ lưu 1 lô").
 */
export function usePagedList<T>(
  resource: string,
  params: ListParams = {},
  opts?: { forceFresh?: boolean; enabled?: boolean }
) {
  return useQuery({
    queryKey: [resource, "list", params],
    queryFn: async () => {
      const { data } = await api.get<Paged<T>>(`/${resource}`, { params });
      /*
       * CẮT CỤT IM LẶNG — cảnh báo to (08/08/2026).
       *
       * 211 lời gọi trong FE dùng `pageSize` lớn (100–500) theo kiểu "tải hết về rồi tự lọc/
       * gộp phía client". Hôm nay dữ liệu demo nhỏ nên không lộ; trường thật vài nghìn học
       * sinh thì trang 1 chỉ chứa một phần, phần còn lại **biến mất không dấu vết** — không
       * lỗi, không cảnh báo, chỉ là kết quả tính sai.
       *
       * Nguy hiểm nhất ở các màn dị ứng: thiếu học sinh nghĩa là thiếu cảnh báo dị ứng
       * (BR-063/BR-123). Nơi nào BẮT BUỘC đủ dữ liệu thì dùng `useAllPages()` bên dưới.
       */
      if (data && data.total > (data.items?.length ?? 0)) {
        console.error(
          `[SFMS] "${resource}" bị CẮT CỤT: nhận ${data.items?.length ?? 0}/${data.total} bản ghi ` +
            `(pageSize=${params.pageSize ?? "mặc định"}). Mọi phép lọc/gộp phía client trên tập ` +
            `này đang SAI. Dùng useAllPages() nếu cần đủ dữ liệu, hoặc lọc phía server.`
        );
      }
      return data;
    },
    placeholderData: keepPreviousData,
    ...(opts?.forceFresh ? { staleTime: 0, refetchOnMount: "always" as const } : {}),
    ...(opts?.enabled !== undefined ? { enabled: opts.enabled } : {}),
  });
}

/**
 * THỰC ĐƠN RIÊNG theo nhóm chế độ ăn.
 *
 * 22/07/2026: đã GỘP vào bảng `Menu` (audience != "ALL") — không còn resource
 * "menu-variants". Hook đọc "menus" rồi chiếu per-buổi qua `specialMenuMeals`, trả
 * về đúng shape `MenuVariant` cũ nên màn render giữ nguyên logic. Dùng thay cho
 * `usePagedList<MenuVariant>("menu-variants", …)`.
 */
export function useSpecialMeals(): {
  variants: MenuVariant[];
  isLoading: boolean;
} {
  const q = usePagedList<{ id: string } & Record<string, unknown>>("menus", {
    pageSize: 500,
  });
  const variants = useMemo(
    () => specialMenuMeals((q.data?.items ?? []) as never),
    [q.data]
  );
  return { variants, isLoading: q.isLoading };
}

/**
 * GET /:resource trả THẲNG 1 object, không bọc `Paged<T>`, không có `:id` — dùng cho cấu
 * hình cấp hệ thống chỉ có đúng 1 bản ghi hiệu lực (VD `security-policies`, MF12 UC-184:
 * `SecurityPolicyController.get()` trả `SecurityPolicyResponse` trần) HOẶC 1 phép tính
 * runtime theo tham số (VD `GET /student-distributions/planned-dishes?date=&class=&mealType=`,
 * MF09 Phase 1 — `params` bắt buộc truyền khi endpoint đọc `@RequestParam`).
 */
export function useSingle<T>(
  resource: string,
  params: ListParams = {},
  opts?: { enabled?: boolean }
) {
  return useQuery({
    queryKey: [resource, "single", params],
    queryFn: async () => {
      const { data } = await api.get<T>(`/${resource}`, { params });
      return data;
    },
    enabled: opts?.enabled ?? true,
  });
}

/**
 * Tải **TOÀN BỘ** bản ghi của một resource bằng cách đi hết các trang.
 *
 * Dùng khi tính đúng ĐÒI HỎI đủ dữ liệu — điển hình là danh sách học sinh và khai báo dị ứng
 * nuôi cảnh báo BR-063/BR-123: thiếu một em là thiếu một cảnh báo dị ứng, và sai kiểu đó không
 * có dấu hiệu gì trên màn hình.
 *
 * KHÔNG dùng cho bảng hiển thị — bảng thì phân trang bằng `DataTable`. Hook này tồn tại cho
 * các phép GỘP/ĐỐI CHIẾU phía client mà hệ thống chưa có endpoint tổng hợp tương ứng.
 *
 * Trả cùng shape với `usePagedList` để đổi qua lại chỉ mất một dòng.
 */
export function useAllPages<T>(
  resource: string,
  params: ListParams = {},
  opts?: { enabled?: boolean }
) {
  const pageSize = Number(params.pageSize ?? 200);
  return useQuery({
    queryKey: [resource, "all", { ...params, pageSize }],
    queryFn: async () => {
      const first = (await api.get<Paged<T>>(`/${resource}`, { params: { ...params, page: 1, pageSize } })).data;
      const items = [...(first.items ?? [])];
      const pageCount = first.pageCount ?? 1;
      /*
       * Trần cứng 50 trang: hook này phục vụ phép gộp, không phải xuất dữ liệu. Chạm trần
       * nghĩa là dữ liệu đã lớn tới mức PHẢI có endpoint tổng hợp phía server — kêu to thay vì
       * lặng lẽ nã hàng trăm request.
       */
      const MAX_PAGES = 50;
      for (let page = 2; page <= Math.min(pageCount, MAX_PAGES); page++) {
        const next = (await api.get<Paged<T>>(`/${resource}`, { params: { ...params, page, pageSize } })).data;
        items.push(...(next.items ?? []));
      }
      /*
       * NÉM LỖI, không `console.error` (13/08/2026 — Nam: "sau này có hàng nghìn học sinh thì
       * chết?").
       *
       * Bản trước ghi console rồi VẪN TRẢ mảng thiếu. Nơi gọi không có cách nào phân biệt
       * "đủ" với "cụt", nên màn hình vẫn vẽ bình thường bằng dữ liệu sai — và người dùng
       * không mở DevTools. Với BR-063 (`unresolvedAllergyDishes` đọc TOÀN BỘ students +
       * allergy-declarations) thì thiếu một trang là THIẾU MỘT CẢNH BÁO DỊ ỨNG, im lặng.
       *
       * Ném ra thì `QueryCache.onError` (providers.tsx) toast ngay tại màn, và `data` là
       * `undefined` thay vì mảng cụt — hỏng thì phải kêu.
       */
      if (pageCount > MAX_PAGES) {
        throw new Error(
          `"${resource}" có ${pageCount} trang, vượt trần ${MAX_PAGES} của useAllPages — màn này ` +
            `cần endpoint tổng hợp phía server, không thể gộp ở trình duyệt nữa.`
        );
      }
      return { ...first, items, page: 1, pageSize: items.length, pageCount: 1 } as Paged<T>;
    },
    placeholderData: keepPreviousData,
    ...(opts?.enabled !== undefined ? { enabled: opts.enabled } : {}),
  });
}

/**
 * GET /:resource trả THẲNG mảng, không bọc `Paged<T>` — dùng cho danh mục nhỏ,
 * không phân trang (VD `checklist-groups`, UC-186: "chỉ nhóm đang hiệu lực",
 * cùng khuôn với `permissions` — MF01, 16 bản ghi cố định). Đọc nhầm bằng
 * `usePagedList` sẽ ra `data.items === undefined` vì response không có field đó.
 */
export function usePlainList<T>(
  resource: string,
  params: ListParams = {},
  opts?: { enabled?: boolean }
) {
  return useQuery({
    queryKey: [resource, "list", params],
    queryFn: async () => {
      const { data } = await api.get<T[]>(`/${resource}`, { params });
      return data;
    },
    placeholderData: keepPreviousData,
    enabled: opts?.enabled ?? true,
  });
}

export function useDetail<T>(resource: string, id: string | undefined) {
  return useQuery({
    queryKey: [resource, "detail", id],
    queryFn: async () => {
      const { data } = await api.get<T>(`/${resource}/${id}`);
      return data;
    },
    enabled: Boolean(id),
  });
}

/**
 * GET /:resource/:id/:subpath — chi tiết một hồ sơ PHỤ gắn theo id của bản ghi chính
 * (khác resource riêng — đó là `useDetail` trên resource khác). VD
 * `GET /students/{code}/health-file` (UC-051/094, C8 — 4 field lâm sàng TÁCH khỏi
 * `StudentResponse`, xem `lib/api/types.ts` cho lý do).
 */
export function useSubDetail<T>(resource: string, id: string | undefined, subpath: string) {
  return useQuery({
    queryKey: [resource, id, subpath],
    queryFn: async () => {
      const { data } = await api.get<T>(`/${resource}/${id}/${subpath}`);
      return data;
    },
    enabled: Boolean(id),
  });
}

export function useCreate<TInput, TOutput = TInput>(
  resource: string,
  opts: MutationOpts = {}
) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async (input: TInput) => {
      const { data } = await api.post<TOutput>(`/${resource}`, input);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/**
 * POST /:resource/:subpath — tạo bản ghi qua một sub-resource cố định, KHÔNG có
 * `:id` (khác `useAction`, vốn đòi id của bản ghi đã tồn tại). MF06 dùng cho
 * `POST /inventory-transactions/adjustments` và `POST /inventory/issue-slips` —
 * cả hai đều là "tạo mới" nhưng backend đặt dưới path con để tách khỏi resource
 * gốc (sổ append-only, không có `POST /inventory-transactions` trần).
 *
 * `invalidate` (13/08/2026): các RESOURCE KHÁC cần làm mới sau khi tạo. Mặc định hook chỉ
 * invalidate `[resource]` — tức đúng chuỗi truyền vào tham số đầu. Ở
 * `supplier/partnerships/[id]/catalog` chuỗi đó là `"partnerships"`, trong khi hai bảng trên
 * màn lại đọc bằng key khác hẳn (`"catalog-change-requests"` và
 * `"partnerships/PTN-200/catalog-items"`) ⇒ gửi đề xuất xong KHÔNG bảng nào tự cập nhật,
 * phải F5 mới thấy. Query key là so khớp theo TIỀN TỐ MẢNG, không phải theo tiền tố chuỗi:
 * `["partnerships"]` không khớp `["partnerships/PTN-200/catalog-items", …]`.
 */
export function useCreateAt<TInput, TOutput = TInput>(
  resource: string,
  subpath: string,
  opts: MutationOpts & { invalidate?: string[] } = {}
) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async (input: TInput) => {
      const { data } = await api.post<TOutput>(`/${resource}/${subpath}`, input);
      return data;
    },
    onSuccess: () => {
      for (const key of [resource, ...(opts.invalidate ?? [])]) {
        qc.invalidateQueries({ queryKey: [key] });
      }
    },
  });
}

export function useUpdate<TInput extends { id: string }, TOutput = TInput>(
  resource: string,
  opts: MutationOpts = {}
) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async (input: TInput) => {
      const { id, ...fields } = input as { id: string } & Record<string, unknown>;
      const { data } = await api.put<TOutput>(`/${resource}/${id}`, fields);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/**
 * PATCH /:resource/:id — dùng cho resource ĐÃ CÓ BACKEND THẬT (`real-resources.ts`).
 *
 * Vì sao không dùng `useUpdate`: `useUpdate` gửi **PUT**, còn toàn bộ endpoint cập
 * nhật của MF01/MF02 khai `@PatchMapping`. Kiểm trên backend đang chạy 02/08/2026:
 *
 *     PUT   /api/v1/classrooms/3A  {"capacity":46}  → 500
 *     PATCH /api/v1/classrooms/3A  {"capacity":46}  → 200
 *
 * (500 chứ không phải 405 — Spring không có handler nào cho PUT ở path này.) Nghĩa
 * là mọi màn còn gọi `useUpdate` trên resource đã migrate sẽ hỏng lặng lẽ ở nút
 * "Lưu". `useUpdate` GIỮ NGUYÊN cho 10 module còn mock — mock nhận PUT.
 */
export function usePatch<TInput extends { id: string }, TOutput = TInput>(
  resource: string,
  opts: MutationOpts = {}
) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async (input: TInput) => {
      const { id, ...fields } = input as { id: string } & Record<string, unknown>;
      const { data } = await api.patch<TOutput>(`/${resource}/${id}`, fields);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/**
 * PATCH tới endpoint hành động: `PATCH /:resource/:id/:action`.
 *
 * Bản PATCH của `useAction` (vốn là POST). Backend dùng cả hai kiểu:
 *   PATCH /users/{code}/status            {active}     — UC-002/003
 *   PATCH /staff/{code}/status            {active}
 *   PATCH /schools/{code}/status          {status}     — UC-001, BR-021
 *   PATCH /classrooms/{code}/homeroom-teacher {teacherId} — UC-008, BR-020/026
 * còn `POST` giữ cho activate/close/promote/unlock (xem `useAction`).
 */
export function usePatchAction<TOutput = unknown>(
  resource: string,
  opts: MutationOpts = {}
) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async ({
      id,
      action,
      body,
    }: {
      id: string;
      action: string;
      body?: Record<string, unknown>;
    }) => {
      const { data } = await api.patch<TOutput>(`/${resource}/${id}/${action}`, body ?? {});
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/**
 * DELETE /:resource/:id — ví dụ xóa role tùy chỉnh (UC-029, BR-008). Nhận thẳng `id`
 * (chuỗi) hoặc `{id, params}` khi cần query string — VD `checklist-groups`:
 * `DELETE /{code}?confirmed=true` (CF-15/BR-210, xoá cứng bị chặn 409 nếu còn phiên
 * tham chiếu, `?confirmed=true` mới cho xoá mềm qua).
 */
export function useDelete(resource: string, opts: MutationOpts = {}) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async (input: string | { id: string; params?: Record<string, unknown> }) => {
      const { id, params } = typeof input === "string" ? { id: input, params: undefined } : input;
      const { data } = await api.delete(`/${resource}/${id}`, { params });
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/** POST tới endpoint hành động (ví dụ /academic-years/:id/activate). */
export function useAction<TOutput = unknown>(
  resource: string,
  opts: MutationOpts = {}
) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async ({
      id,
      action,
      body,
    }: {
      id: string;
      action: string;
      body?: Record<string, unknown>;
    }) => {
      const { data } = await api.post<TOutput>(`/${resource}/${id}/${action}`, body ?? {});
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/**
 * POST/PUT multipart tới `:resource[/:path]` — MF04 là module đầu tiên có endpoint
 * nhận file thật ngoài `/profile/me/photo` (D10-BIS: multipart qua backend, KHÔNG
 * presign). Ba chỗ dùng: `POST /supplier-documents` (meta+file), `PUT
 * /supplier-documents/{code}` (meta+file), `POST /delivery-persons/{code}/face`
 * (image), `POST /partnership-applications[/{code}/resubmit]` (meta+businessLicense?).
 *
 * `parts` là các field multipart: `Blob`/`File` đính kèm nguyên trạng, chuỗi đi
 * thẳng làm text part. Field JSON (`meta`) phải tự bọc bằng
 * `jsonPart(...)` bên dưới để Spring `@RequestPart` nhận đúng `Content-Type`.
 */
export function useUploadMutation<TOutput = unknown>(
  resource: string,
  method: "post" | "put" = "post",
  opts: MutationOpts = {}
) {
  const qc = useQueryClient();
  return useMutation({
    meta: metaOf(opts),
    mutationFn: async ({
      path,
      parts,
    }: {
      /** Nối sau resource, VD `"DOC-01"` hoặc `"APP-2026-01/resubmit"`; bỏ trống = gọi thẳng `/resource`. */
      path?: string;
      /** Giá trị mảng ⇒ nhiều part CÙNG TÊN (VD `@RequestPart("files") List<MultipartFile>`). */
      parts: Record<string, string | Blob | (string | Blob)[] | undefined>;
    }) => {
      const form = new FormData();
      for (const [key, value] of Object.entries(parts)) {
        if (value === undefined) continue;
        if (Array.isArray(value)) {
          for (const v of value) form.append(key, v);
        } else {
          form.append(key, value);
        }
      }
      const url = path ? `/${resource}/${path}` : `/${resource}`;
      const { data } = await api[method]<TOutput>(url, form);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/** Bọc object thành part JSON đúng `Content-Type` cho `@RequestPart` (`meta`). */
export function jsonPart(value: unknown): Blob {
  return new Blob([JSON.stringify(value)], { type: "application/json" });
}
