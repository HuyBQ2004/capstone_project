/**
 * KẾ HOẠCH SUẤT ĂN RIÊNG của từng học sinh — DẪN XUẤT từ thực đơn, không phải
 * bảng khai tay.
 *
 * Trước 21/07/2026 mỗi vai nhìn một mảng khác nhau cho cùng khái niệm này:
 *   - Service Caterers khai vào `menus.blocksData[].dishes[].substitutes[]` và
 *     `menu-variants` — đây là NƠI DUY NHẤT dữ liệu thật được sinh ra;
 *   - School Administration giám sát trên `special-meal-plans` (seed đóng băng,
 *     không màn nào ghi) ⇒ BGH kết luận "đủ/thiếu kế hoạch" trên dữ liệu chết;
 *   - Phụ huynh xem `SPECIAL_MEAL_PLANS` (1 dòng seed cứng) ⇒ mọi phụ huynh,
 *     mọi đứa con đều thấy y hệt nhau.
 *
 * Nay cả ba đọc chung hàm dưới đây. Kế hoạch = chiếu thực đơn khối qua hồ sơ của
 * chính học sinh, đúng cách khâu phát suất sẽ hiểu (BR-065/BR-082).
 */

import type { MealBlock, PersistedMenu } from "@/components/app/menu-form";
import { allergySubOf } from "@/lib/menu-substitutes";
import { dishAllergensOf, type DishLibraryEntry } from "@/lib/menu-personalization";
import {
  allergenTouches,
  isAllergyVerified,
  variantDishName,
  type MealType,
  type MenuVariant,
  type Student,
} from "@/lib/domain-utils";
import type { DietGroup } from "@/lib/diet-groups";

/** 1 suất riêng mà bếp phải chuẩn bị cho 1 học sinh trong 1 buổi ăn. */
export interface DerivedMealPlan {
  id: string;
  studentId: string;
  studentName: string;
  class: string;
  menuId: string;
  menuLabel: string;
  grade: string;
  mealType: MealType;
  /** Vì sao có suất riêng: "Dị ứng Tôm" / "Ăn chay (Vegetarian)". */
  dietaryCategory: string;
  originalDish: string;
  substituteDish: string;
  mode: "PARTIAL" | "FULL";
  prepNote: string;
  /** BR-063: món thay thế còn chạm tác nhân nào của chính em đó không. */
  allergenOverlapCheck: "PASSED" | "FAILED";
  overlapAllergens: string[];
}

/** Món chạm dị ứng của học sinh mà CHƯA có suất riêng — khoảng trống BR-065. */
export interface MealPlanGap {
  studentId: string;
  studentName: string;
  class: string;
  menuId: string;
  menuLabel: string;
  mealType: MealType;
  dish: string;
  allergens: string[];
}

/** Tác nhân dị ứng có hiệu lực của 1 HS: chỉ tính khi hồ sơ đã Y tế xác minh (BR-031). */
export function effectiveAllergens(s: Student): string[] {
  return isAllergyVerified(s) ? s.allergies : [];
}

/**
 * Nhận `Pick<Student, "specialDiet">` (không phải `Student` đầy đủ) — kể từ khi `students`
 * nối thật (05/08/2026), một số nơi gọi hàm này chỉ có `StudentDto` (shape khác `Student`
 * mock), chỉ cần đúng field `specialDiet` nên nới kiểu tham số thay vì ép kiểu tại chỗ gọi.
 *
 * `dietGroups` — danh mục THẬT từ `GET /diet-groups` (xem `lib/diet-groups.ts`), truyền
 * vào từ nơi gọi (component gọi `useDietGroups()`) — 06/08/2026: hàm này KHÔNG còn tự tra
 * hằng `DIET_GROUPS` (mock-data.ts) vì mảng đó thiếu nhóm thật (VD "Không gluten").
 */
export function dietGroupIdOf(
  s: Pick<Student, "specialDiet">,
  dietGroups: DietGroup[]
): string | undefined {
  return dietGroups.find((g) => g.matchDiet === s.specialDiet)?.id;
}

/** HS bắt buộc phải có suất riêng: dị ứng ĐÃ xác minh, hoặc có chế độ ăn riêng (BR-065). */
export function requiresSpecialMeal(s: Student): boolean {
  return effectiveAllergens(s).length > 0 || Boolean(s.specialDiet);
}

function variantFor(
  variants: MenuVariant[],
  menuId: string,
  mealType: MealType,
  groupId?: string
): MenuVariant | undefined {
  if (!groupId) return undefined;
  return variants.find(
    (v) =>
      v.menuId === menuId &&
      v.mealType === mealType &&
      v.dietGroupId === groupId &&
      v.mode === "FULL"
  );
}

/** Chiếu 1 buổi ăn qua 1 học sinh → các suất riêng em đó được nhận. */
function planOfBlock(
  student: Student,
  menu: PersistedMenu,
  block: MealBlock,
  variants: MenuVariant[],
  dishLibrary: DishLibraryEntry[],
  dietGroups: DietGroup[],
  out: { plans: DerivedMealPlan[]; gaps: MealPlanGap[] }
) {
  const allergens = effectiveAllergens(student);
  const groupId = dietGroupIdOf(student, dietGroups);
  const base = {
    studentId: student.id,
    studentName: student.name,
    class: student.class,
    menuId: menu.id,
    menuLabel: menu.week,
    grade: menu.grade,
    mealType: block.mealType,
  };

  // 1) Suất TRỌN BỘ của nhóm — thay cả bữa nên không xét từng món nữa
  const full = variantFor(variants, menu.id, block.mealType, groupId);
  if (full) {
    const names = full.dishes.map((d) => variantDishName(d, dishLibrary));
    const overlap = [
      ...new Set(
        full.dishes.flatMap((d) => {
          const master = dishLibrary.find((x) => x.id === d.dishId);
          if (!master) return [];
          const da = dishAllergensOf(master);
          return allergens.filter((sa) => da.some((x) => allergenTouches(sa, x)));
        })
      ),
    ];
    out.plans.push({
      ...base,
      id: `${menu.id}:${block.mealType}:${student.id}:FULL`,
      dietaryCategory: student.specialDiet ?? "Chế độ riêng",
      originalDish: "(cả bữa)",
      substituteDish: names.join(", "),
      mode: "FULL",
      prepNote: full.note ?? "",
      allergenOverlapCheck: overlap.length === 0 ? "PASSED" : "FAILED",
      overlapAllergens: overlap,
    });
    return;
  }

  // 2) Thay từng món lẻ
  block.dishes.forEach((dish, di) => {
    const dishAllergens = dishAllergensOf(dish);
    const hit = allergens.filter((sa) => dishAllergens.some((da) => allergenTouches(sa, da)));

    if (hit.length > 0) {
      const sub = allergySubOf(dish);
      if (!sub) {
        out.gaps.push({
          ...base,
          dish: dish.name || `Món #${di + 1}`,
          allergens: hit,
        });
        return;
      }
      // Món thay thế phải an toàn với TOÀN BỘ hồ sơ dị ứng của em, không chỉ
      // tác nhân trùng món gốc (em dị ứng cả tôm lẫn cá → thay bằng cá vẫn sai).
      const subAllergens = dishAllergensOf(sub);
      const overlap = allergens.filter((sa) => subAllergens.some((da) => allergenTouches(sa, da)));
      out.plans.push({
        ...base,
        id: `${menu.id}:${block.mealType}:${student.id}:${di}:ALLERGY`,
        dietaryCategory: `Dị ứng ${hit.join(", ")}`,
        originalDish: dish.name,
        substituteDish: sub.name,
        mode: "PARTIAL",
        prepNote: sub.note,
        allergenOverlapCheck: overlap.length === 0 ? "PASSED" : "FAILED",
        overlapAllergens: overlap,
      });
      return;
    }

    /*
     * 3) Chế độ ăn KHÔNG sinh kế hoạch ở mức từng món nữa (22/07/2026).
     *
     * Nhóm chế độ ăn có THỰC ĐƠN RIÊNG cho cả bữa (`MenuVariant` mode FULL) — đã
     * xử lý ở nhánh đầu `planOfBlock`. Em thuộc nhóm mà bữa đó chưa có thực đơn
     * riêng thì rơi vào `gaps`, không vá tạm bằng vài món lẻ.
     */
  });
}

/**
 * Kế hoạch suất riêng của một tập học sinh trên một tập thực đơn.
 * Chỉ xét thực đơn của ĐÚNG khối em đó học.
 */
export function derivedMealPlans(
  students: Student[],
  menus: PersistedMenu[],
  variants: MenuVariant[],
  gradeOfClass: (c: string) => string,
  /**
   * Thư viện món THẬT (`usePagedList<Dish>("dishes", …)`) — trước 06/08/2026 việc soi
   * dị ứng của suất TRỌN BỘ đọc thẳng hằng `DISH_LIBRARY` (seed đóng băng) nên món mới
   * khai qua màn Thư viện món không bao giờ được đối chiếu.
   */
  dishLibrary: DishLibraryEntry[],
  /**
   * Danh mục nhóm chế độ ăn THẬT (`useDietGroups()`, `GET /diet-groups`) — trước
   * 06/08/2026 việc quy `Student.specialDiet` về nhóm đọc thẳng hằng `DIET_GROUPS`
   * (seed đóng băng, thiếu nhóm thật) nên HS thuộc nhóm mới khai (VD "Không gluten")
   * không bao giờ khớp được suất TRỌN BỘ của nhóm đó.
   */
  dietGroups: DietGroup[]
): { plans: DerivedMealPlan[]; gaps: MealPlanGap[] } {
  const out = { plans: [] as DerivedMealPlan[], gaps: [] as MealPlanGap[] };
  for (const student of students) {
    if (!requiresSpecialMeal(student)) continue;
    const grade = gradeOfClass(student.class);
    for (const menu of menus) {
      if (menu.grade !== grade) continue;
      for (const block of menu.blocksData ?? []) {
        planOfBlock(student, menu, block, variants, dishLibrary, dietGroups, out);
      }
    }
  }
  return out;
}
