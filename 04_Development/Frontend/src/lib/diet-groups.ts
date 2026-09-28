"use client";

/**
 * Nguồn THẬT DUY NHẤT cho danh mục nhóm chế độ ăn đặc biệt — `GET /diet-groups`
 * (nối thật 06/08/2026, xem Javadoc `DietGroupDto` ở `lib/api/types.ts`).
 *
 * Trước đây nhiều màn (Bữa ăn đặc biệt, Lịch thực đơn tuần, Duyệt thực đơn,
 * Bảng phục vụ bữa ăn…) tự import hằng số `DIET_GROUPS` khai tay ở
 * `mock-data.ts` để dựng dropdown/label — mảng đó là seed KHÔNG do Y tế duyệt,
 * phát hiện thiếu hẳn nhóm "Không gluten" đang có thật trong DB (06/08/2026).
 * Mọi màn chọn/lọc theo nhóm chế độ ăn phải gọi `useDietGroups()` ở đây thay vì
 * đọc thẳng hằng số mock — xem `service-caterers/menu-templates/page.tsx` (màn
 * đầu tiên áp dụng, 06/08/2026) để biết mẫu `toDietGroup()` ban đầu.
 */

import { usePlainList } from "@/lib/api/hooks";
import type { DietGroupDto } from "@/lib/api/types";

/**
 * Giai đoạn 3 (07/08/2026) — `DietGroup` dẫn xuất trực tiếp từ `DietGroupDto` bằng
 * `Pick<>` thay vì khai tay độc lập (interface cũ ở `mock-data.ts` đã xoá) — tự động
 * bắt lỗi nếu DTO đổi field, không cần contract-check riêng. `id`/`label` narrow
 * required NGAY TẠI `toDietGroup()` (ranh giới nạp dữ liệu), các field còn lại giữ
 * optional vì bản chất tuỳ chọn (không phải mọi nhóm đều loại trừ allergen/từ khoá).
 */
export type DietGroup = Pick<
  DietGroupDto,
  "matchDiet" | "excludeAllergens" | "excludeIngredientKeywords" | "note"
> & {
  id: string;
  label: string;
};

/** `DietGroupDto` (API thật) → `DietGroup` (id/label narrow required). */
export function toDietGroup(g: DietGroupDto): DietGroup {
  return {
    id: g.id ?? "",
    label: g.label ?? g.id ?? "",
    matchDiet: g.matchDiet,
    excludeAllergens: g.excludeAllergens,
    excludeIngredientKeywords: g.excludeIngredientKeywords,
    note: g.note,
  };
}

/** Danh mục nhóm chế độ ăn THẬT, đã quy về shape `DietGroup` — dùng ở mọi màn chọn/lọc. */
export function useDietGroups(): DietGroup[] {
  const { data: dtos = [] } = usePlainList<DietGroupDto>("diet-groups");
  return dtos.map(toDietGroup);
}
