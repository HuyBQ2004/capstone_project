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
import { specialMenuMeals, type MenuVariant } from "@/lib/mock-data";

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
  opts?: { forceFresh?: boolean }
) {
  return useQuery({
    queryKey: [resource, "list", params],
    queryFn: async () => {
      const { data } = await api.get<Paged<T>>(`/${resource}`, { params });
      return data;
    },
    placeholderData: keepPreviousData,
    ...(opts?.forceFresh ? { staleTime: 0, refetchOnMount: "always" as const } : {}),
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

export function useCreate<TInput, TOutput = TInput>(resource: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: TInput) => {
      const { data } = await api.post<TOutput>(`/${resource}`, input);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

export function useUpdate<TInput extends { id: string }, TOutput = TInput>(
  resource: string
) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (input: TInput) => {
      const { id, ...fields } = input as { id: string } & Record<string, unknown>;
      const { data } = await api.put<TOutput>(`/${resource}/${id}`, fields);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/** DELETE /:resource/:id — ví dụ xóa role tùy chỉnh (UC-029, BR-008). */
export function useDelete(resource: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      const { data } = await api.delete(`/${resource}/${id}`);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: [resource] }),
  });
}

/** POST tới endpoint hành động (ví dụ /academic-years/:id/activate). */
export function useAction<TOutput = unknown>(resource: string) {
  const qc = useQueryClient();
  return useMutation({
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
