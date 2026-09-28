/**
 * Cá nhân hóa thực đơn theo TỪNG HỌC SINH.
 *
 * Thực đơn được lập theo KHỐI (Menu.grade) và khai báo món thay thế theo NHÓM
 * (dị ứng / chế độ ăn). Màn Phụ huynh và màn phát suất cần biết cụ thể "con tôi
 * ăn gì" — tức phải chiếu thực đơn khối qua hồ sơ của chính học sinh đó.
 *
 * Lưu ý mô hình (xem review_data_model_traceability_for_backend.md, gap T3):
 * kết quả ở đây là DẪN XUẤT tại thời điểm xem, KHÔNG phải bản ghi lịch sử. Khi
 * làm backend, suất thực tế phải được ghi lại lúc phát (ServingDish) để lịch sử
 * không bị viết lại khi học sinh đổi chế độ ăn.
 */

import type { MealBlock } from "@/components/app/menu-form";
import {
  DISH_LIBRARY,
  allergenTouches,
  dishLibraryAllergens,
  gradeAllergyStudents,
  resolveIngredientAllergens,
  type MenuVariant,
  type Student,
} from "@/lib/mock-data";
import { allergySubOf } from "@/lib/menu-substitutes";

/**
 * Toàn bộ tác nhân dị ứng của 1 món = chọn tay ∪ suy từ thành phần.
 * Nhận cả `SubstituteDish` (cùng 2 trường) để món THAY THẾ được soi bằng đúng
 * một luật với món gốc.
 */
export function dishAllergensOf(
  dish: Pick<MealBlock["dishes"][number], "ingredients" | "manualAllergens">
): string[] {
  const set = new Set<string>(dish.manualAllergens);
  for (const ing of dish.ingredients) {
    resolveIngredientAllergens(ing).allergens.forEach((a) => set.add(a));
  }
  return [...set];
}

/** 1 món còn chạm dị ứng của khối mà CHƯA có suất riêng — căn cứ chặn duyệt BR-063. */
export interface UnresolvedAllergyDish {
  mealType: MealBlock["mealType"];
  name: string;
  allergens: string[];
  /** Số HS của khối đã Y tế xác minh bị ảnh hưởng. */
  students: number;
}

/**
 * Soi thực đơn xem còn món nào chạm dị ứng của khối mà chưa lập suất riêng.
 *
 * Dùng cho màn duyệt của Ban Giám hiệu: trước đây màn đó hiển thị một câu hardcode
 * ("món Tôm rim thịt…") trên MỌI thực đơn, nên BGH không biết thật sự vướng món nào.
 * Suất riêng phải là suất của nhóm dị ứng — suất chay không giải quyết dị ứng.
 */
export function unresolvedAllergyDishes(
  grade: string,
  blocks: MealBlock[]
): UnresolvedAllergyDish[] {
  const rows: UnresolvedAllergyDish[] = [];
  for (const block of blocks) {
    for (const dish of block.dishes) {
      const hits = dishAllergensOf(dish)
        .map((a) => ({
          allergen: a,
          students: gradeAllergyStudents(grade, a, { verifiedOnly: true }).length,
        }))
        .filter((h) => h.students > 0);
      if (hits.length === 0 || allergySubOf(dish)) continue;
      rows.push({
        mealType: block.mealType,
        name: dish.name || "(món chưa đặt tên)",
        allergens: hits.map((h) => h.allergen),
        students: Math.max(...hits.map((h) => h.students)),
      });
    }
  }
  return rows;
}

export interface PersonalizedDish {
  /** Tên món học sinh này thực sự được ăn. */
  name: string;
  allergens: string[];
  /** true khi đây là món thay thế, không phải món chung của khối. */
  isSubstitute: boolean;
  /** Món gốc bị thay (chỉ có khi isSubstitute). */
  substitutedFor?: string;
  /** Vì sao được thay: "Dị ứng Tôm" / "Ăn chay (Vegetarian)". */
  reason?: string;
  /**
   * Món gốc chạm dị ứng của HS nhưng CHƯA có món thay thế — phải cảnh báo đỏ,
   * đây là trạng thái không được phép tồn tại khi thực đơn đã duyệt (BR-063).
   */
  unresolvedConflict?: boolean;
}

/**
 * Chiếu 1 buổi ăn của thực đơn khối qua hồ sơ 1 học sinh.
 *
 * @param verifiedAllergens tác nhân ĐÃ được Y tế xác minh của học sinh (BR-031)
 */
export function personalizeBlock(
  block: MealBlock,
  student: Pick<Student, "specialDiet">,
  verifiedAllergens: string[],
  dietGroupId?: string,
  /**
   * Variant TRỌN BỘ của nhóm HS này cho đúng buổi ăn đó (Đợt C). Có variant FULL
   * thì buổi ăn của em là bộ món trong variant, KHÔNG phải thực đơn khối vá vài
   * món — tiểu đường đổi cơm, giảm tinh bột, bỏ tráng miệng gần như cả bữa.
   */
  fullVariant?: Pick<MenuVariant, "dishes" | "mode">
): PersonalizedDish[] {
  if (fullVariant?.mode === "FULL") {
    return fullVariant.dishes.map((vd) => {
      const master = DISH_LIBRARY.find((d) => d.id === vd.dishId);
      const allergens = master ? dishLibraryAllergens(master) : [];
      const hit = verifiedAllergens.filter((sa) =>
        allergens.some((da) => allergenTouches(sa, da))
      );
      return {
        name: master?.name ?? vd.name ?? vd.dishId,
        allergens,
        isSubstitute: true,
        reason: student.specialDiet
          ? `Suất trọn bộ — chế độ ${student.specialDiet}`
          : "Suất trọn bộ theo nhóm",
        // Variant vẫn phải qua cửa dị ứng: hợp chế độ ăn không có nghĩa là an toàn
        // với hồ sơ dị ứng của chính em đó (BR-063).
        unresolvedConflict: hit.length > 0 || undefined,
      };
    });
  }

  return block.dishes.map((dish) => {
    const allergens = dishAllergensOf(dish);
    const hitAllergens = verifiedAllergens.filter((sa) =>
      allergens.some((da) => allergenTouches(sa, da))
    );

    // 1) Dị ứng — suất thay thế trong thực đơn nay CHỈ có loại này.
    if (hitAllergens.length > 0) {
      const allergySub = allergySubOf(dish);
      if (allergySub) {
        return {
          name: allergySub.name,
          allergens: [...allergySub.manualAllergens],
          isSubstitute: true,
          substitutedFor: dish.name,
          reason: `Dị ứng ${hitAllergens.join(", ")}`,
        };
      }
      return {
        name: dish.name,
        allergens,
        isSubstitute: false,
        unresolvedConflict: true,
        reason: `Chứa ${hitAllergens.join(", ")}`,
      };
    }

    /*
     * 2) Chế độ ăn KHÔNG còn xử lý ở đây (22/07/2026).
     *
     * Nhóm chế độ ăn nay có THỰC ĐƠN RIÊNG (`MenuVariant`) — đã xử ở nhánh đầu hàm.
     * Em thuộc nhóm chế độ ăn mà bữa đó chưa có thực đơn riêng thì nhận suất chuẩn
     * và `unresolvedDietGroups()` báo thiếu; không vá tạm bằng cách thay vài món,
     * vì bữa vá dở dang còn nguy hiểm hơn: bếp tưởng đã lo xong cho em đó.
     */

    // 3) Suất chuẩn
    return { name: dish.name, allergens, isSubstitute: false };
  });
}
