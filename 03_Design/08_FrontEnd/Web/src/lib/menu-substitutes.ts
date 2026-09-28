/**
 * Truy cập SUẤT THAY THẾ của một món trong thực đơn — nguồn duy nhất.
 *
 * Từ 22/07/2026 suất thay thế trong thực đơn CHỈ phục vụ DỊ ỨNG: món chạm tác nhân
 * của học sinh nào thì thay đúng món đó. Chế độ ăn (chay, Halal, tiểu đường, không
 * lactose…) chuyển hẳn sang `MenuVariant` — một THỰC ĐƠN RIÊNG đầy đủ cho cả bữa.
 *
 * Vì sao tách: dị ứng là loại trừ MỘT tác nhân khỏi MỘT món, còn chế độ ăn thay đổi
 * cả cấu trúc bữa. Trước đó cả hai dùng chung cơ chế "thay từng món theo nhóm", nên
 * tầng này phải mang khái niệm `groupId`/`targetType` để hai màn không ghi đè nhau
 * (khai suất Chay từng XOÁ TRẮNG suất dị ứng đã khai trước đó). Bỏ chế độ ăn khỏi
 * đây thì khái niệm nhóm không còn lý do tồn tại — mỗi món có nhiều nhất MỘT suất
 * thay thế, tránh mọi tác nhân mà món gốc đang chạm.
 */

import type { DishForm, SubstituteDish } from "@/components/app/menu-form";

/** Toàn bộ suất thay thế của 1 món (hiện tối đa 1 — suất dị ứng). */
export function subsOf(dish: Pick<DishForm, "substitutes">): SubstituteDish[] {
  return dish.substitutes ?? [];
}

/** Suất dị ứng của món, nếu đã khai. */
export function allergySubOf(
  dish: Pick<DishForm, "substitutes">
): SubstituteDish | undefined {
  return subsOf(dish)[0];
}

/**
 * Ghi suất dị ứng của món. Trả về DishForm mới (không mutate).
 * Đây là hàm thay cho việc gán đè `dish.substitute = {...}` trước đây.
 */
export function upsertSub(dish: DishForm, sub: SubstituteDish): DishForm {
  return { ...dish, substitutes: [sub], isSpecialDish: true };
}

/** Gỡ suất dị ứng của món. */
export function removeSub(dish: DishForm): DishForm {
  return { ...dish, substitutes: [], isSpecialDish: false };
}

/** Tổng số suất đặc biệt của 1 món. */
export function totalSubPortions(dish: Pick<DishForm, "substitutes">): number {
  return subsOf(dish).reduce((sum, s) => sum + (Number(s.portions) || 0), 0);
}
