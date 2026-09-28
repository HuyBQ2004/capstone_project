"use client";

/**
 * Nguồn THẬT DUY NHẤT cho chuẩn dinh dưỡng bữa trưa (BR-062) — `GET /nutrition-targets`
 * (nối thật 07/08/2026, xem migration V54 và `mf02/web/NutritionTargetController.java`).
 *
 * Trước đó chuẩn này là MỘT hằng số cho toàn hệ thống, khai TRÙNG ở hai nơi không có cách
 * nào biết nhau đã lệch:
 *   - Backend `MealHistoryServiceImpl` — `private static final`, sửa phải build lại;
 *   - Frontend `lib/mock-data.ts NUTRITION_TARGET` — bản sao thủ công.
 * Trong khi `menus` đã gắn `grade_id` từ V44, nên thực đơn Khối 1 và Khối 5 bị chấm cùng
 * một thước ±10%. Với trường ngoài tiểu học, cảnh báo sai theo hướng NGUY HIỂM: bữa ăn mầm
 * non đủ chuẩn bị đánh dấu là thiếu năng lượng.
 *
 * Mọi màn cần chuẩn phải gọi `useNutritionTarget(gradeLabel)` ở đây, KHÔNG khai lại hằng số.
 */

import { usePlainList } from "@/lib/api/hooks";
import type { Api } from "@/lib/api/types";

export type NutritionTargetDto = Api<"NutritionTargetResponse">;

/** `gradeId`/`gradeName` narrow required ngay tại ranh giới nạp (springdoc khai mọi field optional). */
export interface NutritionTarget {
  gradeId: string;
  gradeName: string;
  sortOrder: number;
  /** `false` = trường chưa khai, 4 chỉ số dưới là mặc định hệ thống. */
  configured: boolean;
  kcal: number;
  protein: number;
  fat: number;
  carb: number;
}

function toNutritionTarget(t: NutritionTargetDto): NutritionTarget {
  return {
    gradeId: t.gradeId ?? "",
    gradeName: t.gradeName ?? "",
    sortOrder: t.sortOrder ?? 0,
    configured: t.configured ?? false,
    kcal: Number(t.kcal ?? 0),
    protein: Number(t.protein ?? 0),
    fat: Number(t.fat ?? 0),
    carb: Number(t.carb ?? 0),
  };
}

/** Chuẩn của MỌI khối trong trường — dùng ở màn cấu hình (Ban giám hiệu). */
export function useNutritionTargets(): { targets: NutritionTarget[]; isLoading: boolean } {
  const { data: dtos = [], isLoading } = usePlainList<NutritionTargetDto>("nutrition-targets");
  return {
    targets: dtos.map(toNutritionTarget).sort((a, b) => a.sortOrder - b.sortOrder),
    isLoading,
  };
}

/**
 * Chuẩn ÁP DỤNG cho một khối, tra theo NHÃN khối (`"Khối 3"`) — nhãn là thứ các màn thực
 * đơn đang cầm (`menu.grade`), còn API khoá theo `gradeId`. Khối không tìm thấy (hoặc đang
 * tải) trả `undefined` để nơi gọi tự quyết hiển thị gì, KHÔNG bịa số mặc định ở FE: giá trị
 * mặc định là quyết định của server (`ReferenceResolver.DEFAULT_NUTRITION_TARGET`), FE đoán
 * lại là tái lập đúng cái lỗi hai-bản-sao vừa bỏ.
 */
export function useNutritionTarget(gradeLabel: string | undefined): NutritionTarget | undefined {
  const { targets } = useNutritionTargets();
  if (!gradeLabel) return undefined;
  return targets.find((t) => t.gradeName === gradeLabel);
}
