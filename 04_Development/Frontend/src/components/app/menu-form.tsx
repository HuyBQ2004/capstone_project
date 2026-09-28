"use client";

import { useEffect, useState, useMemo } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  ArrowLeft,
  CalendarDays,
  Copy,
  Plus,
  Replace,
  Save,
  Send,
  Soup,
  Sparkles,
  Trash2,
  Users,
  UtensilsCrossed,
  X,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, SearchableSelect, Select, Textarea } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCreate, useDelete, usePagedList, usePlainList, useUpdate, useAllPages } from "@/lib/api/hooks";
import { useSchoolConfig } from "@/lib/school-config";
import { api } from "@/lib/api/client";
import type {
  CatalogItemDto,
  ClassroomDto,
  IngredientDto,
  InventoryBatchDto,
  PartnerDto,
  StudentDto,
  SupplierProductDto,
} from "@/lib/api/types";
import { studentDtoToMock } from "@/lib/student-adapter";
import { supplierProductDtosToMock } from "@/lib/supplier-product-adapter";
import { formatVnDate } from "@/lib/format";
import { apiErrorMessage } from "@/lib/messages";
import { cn } from "@/lib/utils";
import {
  toBaseQuantity,
  toPurchaseQuantity,
  bestPriceOf,
  dishCostPerServing,
  substituteCostDelta,
  estimateMenuCostPerServing,
  type SeedMenu,
  gradeLevelsOf,
  gradeEnrollmentOf,
  ALLERGENS,
  categoryLabel,
  allergenTouches,
  dietCoverageGaps,
  dishConflictsWithDiet,
  dietGroupMembersInGrade,
  dietGroupsNeededByGrade,
  dishIdOfName,
  MEAL_TYPE_LABEL,
  specialMenuMeals,
  specialMenuIdOfMeal,
  standardMenus,
  variantDishName,
  type MenuVariant,
  type MenuVariantDish,
  gradeAllergyStudents,
  gradeAllergySummary,
  isAllergyVerified,
  resolveIngredientAllergens,
  type Dish,
  type MenuItem,
  type Student,
} from "@/lib/domain-utils";
import { useMeasureUnits } from "@/lib/measure-units";
import {
  allergySubOf,
  removeSub,
  subsOf,
  totalSubPortions,
  upsertSub,
} from "@/lib/menu-substitutes";
import { useDietGroups, type DietGroup } from "@/lib/diet-groups";
import { useNutritionTarget } from "@/lib/nutrition-target";
import { useGradeId } from "@/lib/grades";
import type { MenuTemplate } from "@/lib/menu-template-adapter";

/**
 * MenuForm — form THỰC ĐƠN NGÀY theo khối, dùng chung cho TẠO MỚI và CHỈNH SỬA
 * (03_CreateMenuWorkspace + 06_UpdateMenuScreen).
 *
 * Rework 11/07/2026 theo voice review "Thạch Bàn 2":
 * 1. Bỏ mô hình tuần/range + chips thứ (không phân biệt được Thứ 2 tuần này vs
 *    tuần sau, không lặp chu kỳ được) → MỖI NGÀY = 1 THỰC ĐƠN. Lặp lại xử lý
 *    bằng "Sao chép món từ thực đơn khác".
 * 2. "Bữa phụ" đổi thành "Bữa xế chiều" (ăn nhẹ trước khi về — thầy yêu cầu giữ).
 * 3. Món thay thế được kiểm tra lại với TOÀN BỘ hồ sơ dị ứng của chính các học
 *    sinh bị ảnh hưởng (em dị ứng cả tôm lẫn cá → món cá không được làm món thay
 *    thế cho món tôm của em đó).
 *
 * BR-061: ngày áp dụng cách hôm nay ≥3 ngày · BR-069: thành phần + định lượng ·
 * BR-062 dinh dưỡng · BR-063: cảnh báo dị ứng theo khối + món thay thế kèm SỐ
 * SUẤT đặc biệt · BR-067: chỉ Draft/Rejected được sửa.
 * 11/07 (lần 2): BỎ trần chi phí (BR-080) khỏi màn Service Caterers — chi phí
 * chỉ còn là thông tin tham khảo, không chặn nộp.
 */

type MealType = "BREAKFAST" | "LUNCH" | "SNACK";

const MEAL_TYPES: { value: MealType; label: string }[] = [
  { value: "BREAKFAST", label: "Bữa sáng" },
  { value: "LUNCH", label: "Bữa trưa" },
  // 25/07/2026 (Minh): 3 bữa gọi là Sáng / Trưa / Chiều — "Bữa xế chiều" (11/07) đổi ngắn lại.
  { value: "SNACK", label: "Bữa chiều" },
];

const mealLabel = (t: MealType) => MEAL_TYPES.find((m) => m.value === t)?.label ?? t;

const WEEKDAY_LABELS = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];

export interface IngredientRow {
  name: string;
  quantity: string;
  unit: string;
  /**
   * BR-076: khoá join chuẩn về INGREDIENT_CATALOG, set khi chọn từ danh mục.
   * Optional để đọc được record cũ; khi thiếu thì tra theo tên rồi mới dò từ khoá.
   */
  ingredientId?: string;
}

/** Nhóm suất ăn đặc biệt mà món thay thế phục vụ — "ALLERGY" hoặc DietGroup.id. */
export const SPECIAL_GROUP_ALLERGY = "ALLERGY";

/**
 * Nhận xét Minh 11/07 (lần 2): chỉ Nháp/Bị từ chối mới sửa được — đã nộp là chỉ xem.
 *
 * <p>Đặt ở đây (thay vì để bản sao riêng trong `service-caterers/menus/page.tsx` như trước)
 * vì 13/08/2026 phát hiện MenuForm KHÔNG hề áp luật này: lịch tuần ẩn nút "Sửa" của bữa đã
 * duyệt, nhưng mở thẳng route `[id]/edit` thì vẫn gõ và lưu đè được. Hai nơi dùng chung một
 * hằng thì không lệch nhau nữa.
 */
export const MENU_EDITABLE_STATUSES = new Set(["DRAFT", "REJECTED"]);

/** Món phụ/thay thế cho suất ăn đặc biệt — có SỐ SUẤT + NHÓM + định lượng riêng. */
export interface SubstituteDish {
  /**
   * Khoá về THƯ VIỆN MÓN. Suất thay thế cũng là một món — thiếu khoá thì không
   * cộng được dinh dưỡng và không dẫn xuất được allergen theo cùng đường với món
   * chính, phải quay lại so tên chuỗi.
   */
  dishId?: string;
  name: string;
  ingredients: IngredientRow[];
  manualAllergens: string[];
  kcal: string;
  portions: string;
  /**
   * Các tác nhân mà suất riêng này tránh, lấy từ xung đột dị ứng của món gốc.
   *
   * Chốt 22/07/2026: suất thay thế trong thực đơn CHỈ phục vụ dị ứng. Chế độ ăn
   * (chay, Halal, tiểu đường…) nay là MỘT THỰC ĐƠN RIÊNG — xem `MenuVariant` —
   * chứ không thay từng món nữa, vì chế độ ăn đổi cả cấu trúc bữa chứ không loại
   * trừ một tác nhân khỏi một món.
   */
  allergenIds?: string[];
  note: string;
}

export interface DishForm {
  /**
   * Khoá về THƯ VIỆN MÓN (`Dish.id`) — set khi chọn món từ thư viện. Món gõ tay
   * không có id: khi đó thực đơn vẫn chạy nhưng không map ngược được về dish
   * master (thống kê dinh dưỡng, gợi ý AI BR-156, màn Phụ huynh).
   */
  dishId?: string;
  name: string;
  ingredients: IngredientRow[];
  manualAllergens: string[];
  kcal: string;
  protein: string;
  carb: string;
  fat: string;
  /**
   * Số suất của RIÊNG món này. Trước đây mọi món trong bữa dùng chung một số suất
   * cấp thực đơn; tách theo món để khai đúng khi các món phục vụ số lượng khác nhau
   * (vd món chính cả khối, món tráng miệng chỉ một phần lớp). Trống ⇒ lấy mặc định
   * = "Số suất của khối" ở đầu form. Lưu CHUỖI để ô trống không bị ép về 0.
   * (22/07/2026 — Minh: chuyển số suất từ cấp thực đơn xuống cấp món "cho dễ".)
   */
  servings?: string;
  isSpecialDish: boolean;
  /**
   * G9 đợt 2 (25/07/2026): món này được đổ vào bữa từ THỰC ĐƠN MẪU nào — một ô
   * lịch chứa được NHIỀU thực đơn mẫu, gỡ một thực đơn khỏi bữa là gỡ đúng các
   * món mang templateId đó, không đụng món của thực đơn khác trong cùng bữa.
   */
  templateId?: string;
  /**
   * Món thay thế theo TỪNG NHÓM — một món có thể phục vụ nhiều nhóm cùng lúc
   * (vd vừa có suất dị ứng, vừa có suất chay). Truy cập qua `lib/menu-substitutes`,
   * KHÔNG đọc/ghi trực tiếp.
   */
  substitutes?: SubstituteDish[];
}

/**
 * 1 buổi ăn trong NGÀY = 1 loại bữa + danh sách món. `days` chỉ còn để đọc
 * được bản ghi cũ (mô hình tuần) — không dùng nữa.
 */
export interface MealBlock {
  mealType: MealType;
  dishes: DishForm[];
  /**
   * Chỉ dùng cho THỰC ĐƠN RIÊNG (menu.audience != "ALL"): ghi chú chế biến của
   * buổi đó cho nhóm chế độ ăn (vd "nấu nồi riêng, không dùng chung muôi").
   */
  note?: string;
  /** Chỉ dùng cho thực đơn riêng: số suất dự kiến của nhóm cho buổi đó. */
  portions?: number;
  /**
   * G9 (25/07/2026): buổi này dựng từ THỰC ĐƠN MẪU nào — ô lịch tuần hiện TÊN MẪU
   * thay vì liệt kê món, bấm vào mới sổ toàn bộ bữa. Giữ tham chiếu cả khi người
   * dùng đã sửa món khác đi (lịch so bộ món với mẫu để badge "Đã tùy chỉnh").
   * `templateName` đóng băng tại thời điểm áp — mẫu đổi tên/xóa vẫn đọc được lịch cũ.
   * @deprecated đợt 2 cùng ngày: một buổi chứa NHIỀU thực đơn mẫu → dùng `templates`.
   */
  templateId?: string;
  /** @deprecated xem `templates`. */
  templateName?: string;
  /**
   * G9 đợt 2 (Minh 25/07/2026): danh sách THỰC ĐƠN MẪU đã thêm vào buổi này —
   * ô lịch liệt kê đúng các tên này ("Thực đơn tiêu chuẩn A/B/C…") + nút thêm.
   * Tên đóng băng lúc thêm; món của từng thực đơn nhận diện qua `DishForm.templateId`.
   */
  templates?: { templateId: string; templateName: string }[];
  /** @deprecated legacy mô hình tuần — giữ để không vỡ record cũ trong phiên. */
  days?: string[];
}

/** Record menu trong mock db — MenuItem + dữ liệu form đầy đủ để chỉnh sửa lại. */
export type PersistedMenu = MenuItem & {
  /**
   * V44 — UUID của khối, khoá GHI của `CreateMenuRequest`/`UpdateMenuRequest`.
   * `MenuItem.grade` (nhãn "Khối 3") giữ nguyên cho chiều ĐỌC/hiển thị: server trả nhãn,
   * D1 không lộ UUID ra ngoài. Gửi request thì dùng `gradeId` — lấy qua
   * `useGradeId(label)` ở `lib/grades.ts`.
   */
  gradeId?: string;
  mealType?: string;
  blocksData?: MealBlock[];
  servings?: number;
  /**
   * BR-063 (13/08/2026) — TÊN các món còn chạm dị ứng mà CHƯA có suất thay thế, do SERVER tính
   * (`MenuResponse.allergyConflictDishes`). Đi cặp với `allergyConflicts` (số đếm) đã có.
   *
   * Trước field này, màn duyệt phải tải TOÀN BỘ sổ học sinh + khai báo dị ứng của trường về
   * trình duyệt rồi tự chạy lại `unresolvedAllergyDishes()` chỉ để lấy mấy cái tên món.
   */
  allergyConflictDishes?: string[];
  /** @deprecated legacy mô hình tuần. */
  rangeStartIso?: string;
  /** @deprecated legacy mô hình tuần. */
  rangeEndIso?: string;
};

/** Dòng thành phần rỗng — export để màn Bữa ăn đặc biệt khởi tạo cùng kiểu dữ liệu. */
export function emptyIngredient(): IngredientRow {
  return { name: "", quantity: "", unit: "g" };
}

const MEAL_SORT: Record<MealType, number> = { BREAKFAST: 0, LUNCH: 1, SNACK: 2 };

/** THƯ VIỆN MÓN (`Dish`) → `DishForm` để nhét thẳng vào blocksData của thực đơn riêng. */
export function dishFormFromLibrary(d: Dish): DishForm {
  return {
    dishId: d.id,
    name: d.name,
    // Backend trả `quantity` là số (PROPOSED §Phase B.8, trước là chuỗi) — ép về chuỗi
    // cho state form, cùng khuôn `kcal: String(d.kcal)` bên dưới.
    ingredients: d.ingredients.map((i) => ({ name: i.name, quantity: String(i.quantity ?? ""), unit: i.unit, ingredientId: i.ingredientId })),
    manualAllergens: [...d.manualAllergens],
    kcal: String(d.kcal),
    protein: String(d.protein),
    carb: String(d.carb),
    fat: String(d.fat),
    isSpecialDish: false,
    substitutes: [],
  };
}

/**
 * THƯ VIỆN MÓN → suất thay thế (G10). Suất thay thế cũng là một món thật trong
 * thư viện — mang đủ BOM + kcal + `dishId` để dinh dưỡng/truy xuất lô đi cùng
 * đường với món chính. Dùng ở 2 chỗ: prefill khi mở suất dị ứng của món có
 * `defaultSubstituteId`, và khi áp Thực đơn mẫu (tự đổ substitute theo mẫu).
 */
export function substituteFromLibraryDish(d: Dish, portions: string): SubstituteDish {
  return {
    dishId: d.id,
    name: d.name,
    // Backend trả `quantity` là số (PROPOSED §Phase B.8, trước là chuỗi) — ép về chuỗi
    // cho state form, cùng khuôn `kcal: String(d.kcal)` bên dưới.
    ingredients: d.ingredients.map((i) => ({ name: i.name, quantity: String(i.quantity ?? ""), unit: i.unit, ingredientId: i.ingredientId })),
    manualAllergens: [...d.manualAllergens],
    kcal: String(d.kcal),
    portions,
    note: "",
  };
}

/**
 * Món thay thế MẶC ĐỊNH của 1 món trong form, tra từ thư viện (G10 — review
 * 25/07/2026). Món gõ tay không có `dishId` thì thử khớp đúng tên — cùng cách
 * các màn khác nối ngược về dish master.
 */
export function defaultSubstituteOf(dish: Pick<DishForm, "dishId" | "name">, library: Dish[]): Dish | undefined {
  const base =
    library.find((x) => x.id === dish.dishId) ??
    library.find((x) => x.name.trim().toLowerCase() === dish.name.trim().toLowerCase());
  if (!base?.defaultSubstituteId) return undefined;
  return library.find((x) => x.id === base.defaultSubstituteId);
}

/**
 * THỰC ĐƠN MẪU → MealBlock (G9). Nguồn duy nhất cho nút "Áp dụng mẫu" trên Lịch
 * thực đơn tuần: đổ món từ thư viện theo mẫu, và với món chạm dị ứng của KHỐI
 * đang xếp thì tự đổ luôn suất thay thế từ `Dish.defaultSubstituteId` (G10) —
 * đúng mục tiêu "chọn mẫu xong không phải nhập lại gì, chỉ chỉnh khi tuần này khác".
 */
export function mealBlockFromTemplate(
  tpl: MenuTemplate,
  mealType: MealType,
  grade: string,
  library: Dish[],
  students: Student[]
): MealBlock {
  const dishes = tpl.dishIds
    .map((id) => library.find((x) => x.id === id))
    // Món đã bị gỡ khỏi thư viện thì bỏ qua — màn Thực đơn mẫu có badge cảnh báo riêng.
    .filter((x): x is Dish => x != null)
    .map((lib) => {
      // Gắn templateId từng món: gỡ thực đơn khỏi bữa là gỡ đúng món của nó.
      const df = { ...dishFormFromLibrary(lib), templateId: tpl.id };
      const st = dishState(df, grade, students);
      if (st.conflicts.length === 0) return df;
      const sub = defaultSubstituteOf(df, library);
      if (!sub) return df; // vẫn phải khai tay — form sẽ chặn nộp như luồng hiện có (BR-063)
      return {
        ...df,
        isSpecialDish: true,
        substitutes: [
          {
            ...substituteFromLibraryDish(sub, String(st.affected.length)),
            allergenIds: st.conflicts.map((c) => c.allergen),
          },
        ],
      };
    });
  return {
    mealType,
    dishes,
    templates: [{ templateId: tpl.id, templateName: tpl.name }],
  };
}

function emptyDish(): DishForm {
  return {
    name: "",
    ingredients: [emptyIngredient()],
    manualAllergens: [],
    kcal: "",
    protein: "",
    carb: "",
    fat: "",
    isSpecialDish: false,
    substitutes: [],
  };
}

function emptySubstitute(portions: string): SubstituteDish {
  return {
    name: "",
    ingredients: [emptyIngredient()],
    manualAllergens: [],
    kcal: "",
    portions,
    note: "",
  };
}

/** Định dạng dd/MM/yyyy cố định (không phụ thuộc locale). */
function fmtDateFull(d: Date): string {
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

/** "2026-07-20" → nhãn "Thứ 2 — 20/07/2026". */
function dayLabelOfIso(iso: string): string {
  const d = new Date(`${iso}T00:00:00`);
  return `${WEEKDAY_LABELS[d.getDay()]} — ${fmtDateFull(d)}`;
}

/** Offset tuần của 1 ngày ISO so với tuần hiện tại — giữ đúng tuần khi Quay lại lưới. */
function weekOffsetOfIso(iso: string): number {
  if (!iso) return 0;
  const mondayOf = (d: Date) => {
    const x = new Date(d);
    x.setHours(0, 0, 0, 0);
    x.setDate(x.getDate() - ((x.getDay() + 6) % 7));
    return x;
  };
  const target = mondayOf(new Date(`${iso}T00:00:00`));
  const current = mondayOf(new Date());
  return Math.round((target.getTime() - current.getTime()) / (7 * 86_400_000));
}

/** yyyy-mm-dd của (hôm nay + n ngày) — cho min của input date (BR-061). */
function isoAfterDays(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() + n);
  const p = (x: number) => String(x).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

/**
 * Allergen của 1 thành phần — CHỈ khi tên khớp chính xác 1 mục trong catalog
 * (tức người dùng đã chọn xong từ combobox). Không nhận diện từ khóa lúc đang
 * gõ dở; thành phần tự do ngoài catalog → khai tác nhân bằng combobox thủ công.
 */
function allergensOfIngredient(name: string, ingredientId?: string): string[] {
  return resolveIngredientAllergens({ name, ingredientId }).allergens;
}

/** Toàn bộ allergen của món = auto (thành phần đã chọn từ catalog) ∪ chọn tay. */
function dishAllergens(ingredients: IngredientRow[], manual: string[]): string[] {
  const set = new Set<string>(manual);
  for (const ing of ingredients) {
    if (ing.name.trim()) allergensOfIngredient(ing.name).forEach((a) => set.add(a));
  }
  return [...set];
}

// `allergenTouches` (ô Hải sản 2 chiều) nay dùng chung từ mock-data để món GỐC và
// món THAY THẾ áp cùng một luật — trước đây chỉ món thay thế xét ô nên món gốc lọt.

/**
 * Sĩ số thật của một khối = tổng `Classroom.students` các lớp thuộc khối.
 * Dùng làm số suất mặc định thay cho hằng 380 (vốn xấp xỉ TOÀN TRƯỜNG chứ không
 * phải 1 khối — Khối 3 thực tế chỉ 78 HS).
 */
/**
 * Sĩ số khối — đọc lớp học TỪ DB, không từ mảng seed tĩnh: Ban giám hiệu vừa mở
 * lớp mới hay sửa sĩ số thì màn lập thực đơn phải thấy ngay.
 */
function useClassrooms() {
  const { data } = usePagedList<ClassroomDto>("classrooms", { pageSize: 200 });
  return data?.items ?? [];
}

/** Học sinh của khối bị ảnh hưởng bởi các allergen xung đột (unique theo HS). */
function affectedStudents(
  grade: string,
  conflictAllergens: string[],
  students: Student[]
): { student: Student; matched: string[] }[] {
  const map = new Map<string, { student: Student; matched: string[] }>();
  for (const allergen of conflictAllergens) {
    // verifiedOnly: danh sách này dẫn tới số suất thay thế bắt buộc (BR-065)
    for (const s of gradeAllergyStudents(grade, allergen, { verifiedOnly: true }, students)) {
      const entry = map.get(s.id) ?? { student: s, matched: [] };
      entry.matched.push(allergen);
      map.set(s.id, entry);
    }
  }
  return [...map.values()];
}

/**
 * Trạng thái kiểm tra dị ứng của 1 món (dùng chung form + checklist nộp).
 * Export 25/07/2026: lịch tuần tự ghi bản ghi thực đơn khi thêm/gỡ thực đơn mẫu
 * (không qua MenuForm nữa) nên cần tính `allergyConflicts` bằng đúng công thức này.
 *
 * `students` BẮT BUỘC là dữ liệu THẬT (`usePagedList<StudentDto>("students")` qua
 * `studentDtoToMock()`) — hằng seed `STUDENTS` đã ngừng dùng làm nguồn 07/08/2026.
 */
export function dishState(dish: DishForm, grade: string, students: Student[]) {
  const auto = dishAllergens(dish.ingredients, []);
  const all = dishAllergens(dish.ingredients, dish.manualAllergens);
  // Xung đột = tác nhân của món CHẠM tác nhân của HS đã xác minh (xét ô Hải sản
  // 2 chiều, không so khớp chuỗi chính xác). Chỉ HS đã Y tế xác minh mới là căn
  // cứ CHẶN duyệt (BR-031/065); khai báo chưa xác minh đưa vào `advisory` để
  // cảnh báo mà không chặn.
  const conflicts = all
    .map((a) => {
      const hits = grade ? gradeAllergyStudents(grade, a, { verifiedOnly: true }, students) : [];
      return {
        allergen: a,
        students: hits.length,
        classes: [...new Set(hits.map((s) => s.class))],
      };
    })
    .filter((c) => c.students > 0);
  // Khai báo CHƯA xác minh chạm món này — hiển thị cảnh báo, không chặn.
  const advisory = grade
    ? all
        .map((a) => ({
          allergen: a,
          students: gradeAllergyStudents(grade, a, {}, students).filter((s) => !isAllergyVerified(s)).length,
        }))
        .filter((c) => c.students > 0)
    : [];
  const affected = grade ? affectedStudents(grade, conflicts.map((c) => c.allergen), students) : [];
  /**
   * Voice review: món thay thế phải an toàn với TOÀN BỘ hồ sơ dị ứng của chính
   * các em bị ảnh hưởng — không chỉ tác nhân trùng món gốc. (VD: em dị ứng cả
   * tôm lẫn cá → thay món tôm bằng món cá vẫn phải bị chặn.)
   */
  const affectedAllAllergens = [
    ...new Set(affected.flatMap((r) => r.student.allergies)),
  ];
  // Màn thực đơn phụ trách SUẤT DỊ ỨNG; suất theo nhóm chế độ ăn do màn
  // "Bữa ăn đặc biệt" quản — đọc/ghi tách nhóm nên hai màn không đè nhau.
  const allergySub = allergySubOf(dish);
  const subAllergens = allergySub
    ? dishAllergens(allergySub.ingredients, allergySub.manualAllergens)
    : [];
  const subStillConflicts = affectedAllAllergens.filter((sa) =>
    subAllergens.some((da) => allergenTouches(sa, da))
  );
  const subComplete =
    allergySub != null &&
    allergySub.name.trim() !== "" &&
    allergySub.kcal.trim() !== "" &&
    Number(allergySub.portions) > 0 &&
    allergySub.ingredients.some((r) => r.name.trim() && r.quantity);
  const conflictResolved =
    conflicts.length === 0 || (subComplete && subStillConflicts.length === 0);
  const specialComplete = !dish.isSpecialDish || subComplete;
  return {
    auto,
    all,
    /** Suất DỊ ỨNG của món — loại suất duy nhất còn nằm trong thực đơn. */
    allergySub,
    conflicts,
    advisory,
    affected,
    affectedAllAllergens,
    subStillConflicts,
    subComplete,
    conflictResolved,
    specialComplete,
  };
}

/** Gợi ý AI (UC-075) — sau ADR-003 vẫn phải hard-filter dị ứng phía server trước khi lưu. */
interface AiDishSuggestion {
  name: string;
  /**
   * Số nguyên liệu AI bịa ra ngoài danh mục và đã BỊ LOẠI khi kiểm lại (09/08/2026).
   * Hiện ra cho người dùng biết món chưa đủ nguyên liệu — loại âm thầm thì họ chèn vào thực
   * đơn một món thiếu thành phần mà không hay.
   */
  droppedIngredients?: number;
  mealType: MealType;
  ingredients: IngredientRow[];
  kcal: string;
  protein: string;
  carb: string;
  fat: string;
  rationale: string;
}

/** Cắt code fence ```json ... ``` nếu model bọc thêm, rồi parse mảng gợi ý món. */
/**
 * Chuẩn hoá tên nguyên liệu trước khi so với danh mục.
 *
 * <p>So sánh thô bằng `toLowerCase()` từng làm hỏng cả tính năng gợi ý AI (đo trên trình duyệt
 * 09/08/2026): model trả `"Thịt heo nạc (kg)"`, danh mục ghi `"Thịt heo nạc"` ⇒ loại sạch mọi
 * nguyên liệu dù chúng đều hợp lệ. Ở đây bỏ phần trong ngoặc, gom khoảng trắng, và chuẩn hoá
 * Unicode về NFC (tiếng Việt có hai cách mã hoá dấu — chuỗi từ model không nhất thiết cùng dạng
 * với chuỗi từ Postgres).
 *
 * <p>Cố ý KHÔNG bỏ dấu tiếng Việt: "cà" và "cá" là hai nguyên liệu khác nhau, khớp nhầm còn tệ
 * hơn không khớp.
 */
function normalizeIngredientName(name: string | undefined | null): string {
  return (name ?? "")
    .normalize("NFC")
    .replace(/\([^)]*\)/g, " ")
    .replace(/\s+/g, " ")
    .trim()
    .toLowerCase();
}

function parseAiSuggestions(reply: string): AiDishSuggestion[] {
  const cleaned = reply.replace(/```json\s*|```\s*/g, "").trim();
  const start = cleaned.indexOf("[");
  const end = cleaned.lastIndexOf("]");
  if (start === -1 || end === -1 || end < start) {
    throw new Error("AI không trả về mảng JSON hợp lệ.");
  }
  const parsed = JSON.parse(cleaned.slice(start, end + 1));
  if (!Array.isArray(parsed)) throw new Error("AI không trả về mảng JSON hợp lệ.");
  return parsed
    .filter((s) => s && typeof s.name === "string" && Array.isArray(s.ingredients))
    .map((s) => ({
      name: String(s.name),
      mealType: (["BREAKFAST", "LUNCH", "SNACK"].includes(s.mealType) ? s.mealType : "LUNCH") as MealType,
      ingredients: (s.ingredients as unknown[]).map((i) => {
        const ing = i as { name?: unknown; quantity?: unknown; unit?: unknown };
        return { name: String(ing.name ?? ""), quantity: String(ing.quantity ?? ""), unit: String(ing.unit ?? "g") };
      }),
      kcal: String(s.kcal ?? ""),
      protein: String(s.protein ?? ""),
      carb: String(s.carb ?? ""),
      fat: String(s.fat ?? ""),
      rationale: String(s.rationale ?? ""),
    }));
}

/* ================= Component: bảng thành phần + định lượng ================= */

/**
 * Bảng thành phần + ĐỊNH LƯỢNG + `ingredientId`. Export vì màn Bữa ăn đặc biệt
 * phải khai suất thay thế bằng đúng cấu trúc này — trước đây màn đó nhập bằng
 * Textarea phẩy-ngăn-cách nên `quantity` rỗng: bếp không xuất kho được gì cho
 * suất đặc biệt và không truy xuất được lô (BR-069/076).
 */
export function IngredientEditor({
  rows,
  onChange,
  servings = 0,
  catalogOverride,
}: {
  rows: IngredientRow[];
  onChange: (rows: IngredientRow[]) => void;
  /** Số suất của bữa — để quy đổi định lượng/suất ra lượng mua và thành tiền. */
  servings?: number;
  /**
   * Khi truyền vào (school-scoped — VD "món trong thực đơn"), TÊN GÕ VÀO chỉ khớp được
   * `ingredientId` nếu nằm trong danh mục NÀY — nguyên liệu ngoài danh mục NCC đã hợp tác
   * với trường rơi vào đúng nhánh cảnh báo "Ngoài danh mục" có sẵn (không tạo state mới),
   * tức không dùng được cho việc mua/xuất kho, đúng tinh thần "không cho chọn ngoài danh
   * mục" mà không phá `Dish` (thư viện món dùng chung toàn hệ thống, xem `Dish.java`).
   * `undefined` = giữ hành vi cũ (thư viện món dùng chung — `dishes/page.tsx`).
   */
  catalogOverride?: { id?: string; name?: string; unit?: string }[];
}) {
  // ⚠️ NGOÀI PHẠM VI MF04 (03/08/2026): `GET /partners` chỉ SYSTEM_ADMIN/SCHOOL_ADMIN —
  // SERVICE_CATERERS (dùng ở service-caterers/menus) gọi vào sẽ 403 (real-resources.ts).
  // Chưa tự mở rộng @PreAuthorize, cần chủ màn (Thảo, MF05) xác nhận hướng xử lý.
  const { data: partnersPage } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const partners = partnersPage?.items ?? [];
  // Danh mục TOÀN HỆ THỐNG — vẫn cần dù có `catalogOverride`, vì refuseFactor (lượng
  // mua thực tế sau hao hụt, dòng dưới) chỉ có ở đây, `CatalogItemResponse` không có field này.
  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");
  // Danh mục dùng để GÁN `ingredientId` khi gõ tên — bị giới hạn khi có `catalogOverride`.
  const selectionCatalog = catalogOverride ?? ingredientCatalog;
  const { data: supplierProductsPage } = usePagedList<SupplierProductDto>("supplier-products", { pageSize: 300 });
  const supplierProducts = useMemo(
    () => supplierProductDtosToMock(supplierProductsPage?.items ?? []),
    [supplierProductsPage]
  );
  // Danh mục đơn vị THẬT (`GET /measure-units`) — xem `lib/measure-units.ts`.
  const measureUnits = useMeasureUnits();
  /*
   * Ô thành phần là SearchableSelect (13/08/2026) chứ không còn `<Input list={datalistId}>`.
   * `<datalist>` chỉ GỢI Ý: người dùng gõ gần đúng rồi bỏ qua gợi ý là `ingredientId` rỗng —
   * đúng cái bẫy mà cả hai màn (thư viện món, lập thực đơn) đang phải chặn bằng thông báo lỗi
   * lúc lưu. Cho chọn thẳng từ danh mục thì tên LUÔN khớp, khoá join luôn có.
   *
   * Dedupe theo tên vì `value` của combobox là TÊN (chuỗi lưu trong `IngredientRow.name`) —
   * hai mục cùng tên trong danh mục hợp đồng sẽ trùng key.
   */
  /*
   * Mặc định ẨN nguyên liệu CHƯA NCC nào báo giá (Nam, 13/08/2026): chọn phải mục đó thì
   * món lập xong không đặt mua được và chi phí ước tính thiếu đúng phần ấy — cảnh báo vàng
   * dưới ô chỉ nói SAU khi đã chọn, không ngăn được việc chọn.
   *
   * Điều kiện lọc là "CÓ dòng báo giá cho `ingredientId`", KHÔNG dùng `bestPriceOf` — hàm đó
   * còn đòi đối tác `APPROVED`, mà `GET /partners` trả 403 cho SERVICE_CATERERS (chú thích
   * đầu component) ⇒ `partners` rỗng ⇒ lọc theo nó sẽ ẩn SẠCH danh mục. Cùng lý do có
   * `hideUnpriced` bên dưới: chưa tải được `/supplier-products` thì không lọc gì cả.
   */
  const [showUnpriced, setShowUnpriced] = useState(false);
  const pricedIngredientIds = useMemo(
    () => new Set(supplierProducts.map((p) => p.ingredientId)),
    [supplierProducts]
  );
  const hideUnpriced = !showUnpriced && pricedIngredientIds.size > 0;
  const ingredientOptions = useMemo(() => {
    const seen = new Set<string>();
    const out: { value: string; label: string }[] = [];
    for (const c of selectionCatalog) {
      const name = c.name?.trim();
      if (!name || seen.has(name)) continue;
      if (hideUnpriced && !(c.id && pricedIngredientIds.has(c.id))) continue;
      seen.add(name);
      out.push({ value: name, label: c.unit ? `${name} (${c.unit})` : name });
    }
    return out.sort((a, b) => a.label.localeCompare(b.label, "vi"));
  }, [selectionCatalog, hideUnpriced, pricedIngredientIds]);
  const hiddenCount = useMemo(() => {
    if (!hideUnpriced) return 0;
    return new Set(
      selectionCatalog
        .filter((c) => c.name?.trim() && !(c.id && pricedIngredientIds.has(c.id)))
        .map((c) => c.name!.trim())
    ).size;
  }, [selectionCatalog, hideUnpriced, pricedIngredientIds]);
  function update(i: number, patch: Partial<IngredientRow>) {
    onChange(
      rows.map((r, idx) => {
        if (idx !== i) return r;
        const next = { ...r, ...patch };
        if (patch.name !== undefined) {
          const cat = selectionCatalog.find((c) => c.name === patch.name);
          if (cat) next.unit = cat.unit === "kg" ? "g" : cat.unit === "lít" ? "ml" : cat.unit ?? next.unit;
          // BR-076: chốt khoá join khi tên khớp danh mục; gõ tay thì bỏ id cũ đi
          next.ingredientId = cat?.id;
        }
        return next;
      })
    );
  }
  return (
    <div className="space-y-2">
      {rows.map((r, i) => {
        const resolved = r.name.trim()
          ? resolveIngredientAllergens({ name: r.name, ingredientId: r.ingredientId })
          : { allergens: [], source: "unknown" as const };
        const allergens = resolved.allergens;
        return (
          <div key={i} className="grid items-start gap-2 sm:grid-cols-[1.6fr_90px_80px_auto_36px]">
            <div className="space-y-1">
              <SearchableSelect
                value={r.name}
                onChange={(name) => update(i, { name })}
                options={ingredientOptions}
                placeholder="— Chọn thành phần —"
                searchPlaceholder="Gõ để tìm nguyên liệu…"
              />
              {/* BR-076: nguyên liệu ngoài danh mục → không truy xuất được lô, và
                  allergen chỉ là phỏng đoán từ khoá. Phải báo, không im lặng. */}
              {r.name.trim() && !r.ingredientId && (
                <p className="text-xs text-warning">
                  {resolved.source === "keyword"
                    ? "Ngoài danh mục — allergen chỉ là phỏng đoán, hãy chọn từ danh mục"
                    : "Ngoài danh mục — không truy xuất được lô, chưa nhận diện allergen"}
                </p>
              )}
              {/* Nguồn mua và phép quy đổi, ngay tại dòng: công thức ghi gam/suất
                  còn nhà cung cấp bán theo kg, nên nếu không hiện ra thì người lập
                  thực đơn không có cách nào biết 60g thịt/suất là bao nhiêu tiền. */}
              {(() => {
                if (!r.ingredientId) return null;
                const best = bestPriceOf(r.ingredientId, supplierProducts, partners);
                if (!best) return null;
                const supplier =
                  partners.find((p) => p.id === best.supplierId)?.name ?? best.supplierId;
                const n = servings;
                const base = toBaseQuantity(Number(r.quantity) || 0, r.unit);
                const cat = ingredientCatalog.find((c) => c.id === r.ingredientId);
                const buy = toPurchaseQuantity(base.qty * n, cat?.refuseFactor, base.unit);
                return (
                  <p className="text-xs text-muted-foreground">
                    {supplier} · {best.unitPrice.toLocaleString("vi-VN")}₫/{best.unit}
                    {n > 0 && base.qty > 0 && (
                      <>
                        {" "}
                        → {buy} {base.unit} cho {n} suất ={" "}
                        <b>{Math.round(buy * best.unitPrice).toLocaleString("vi-VN")}₫</b>
                      </>
                    )}
                  </p>
                );
              })()}
              {/* Nguyên liệu có trong danh mục nhưng KHÔNG nhà cung cấp nào đang bán:
                  món vẫn lập được nhưng đến lúc đặt hàng sẽ không mua nổi, và chi phí
                  ước tính thiếu đúng phần này. Cảnh báo chứ không chặn — bếp có quyền
                  lập thực đơn trước rồi mới đi tìm nguồn. */}
              {r.ingredientId && !bestPriceOf(r.ingredientId, supplierProducts, partners) && (
                <p className="text-xs text-warning">
                  Chưa nhà cung cấp nào đang hợp tác báo giá — chưa đặt mua được và chi
                  phí ước tính chưa gồm nguyên liệu này.
                </p>
              )}
            </div>
            <Input
              type="number"
              min={0}
              value={r.quantity}
              onChange={(e) => update(i, { quantity: e.target.value })}
              placeholder="Định lượng"
              aria-label="Định lượng / suất"
            />
            {/* Nguyên liệu ĐÃ khớp danh mục → khóa đơn vị theo `catalog.unit` (kg→g,
                lít→ml, còn lại giữ nguyên) — không cho đổi tự do nữa, tránh lệch với
                đơn vị KHO ở bước xuất kho/đặt hàng (từng gây sai 1000 lần, xem
                `inventory/issue/page.tsx`). Nguyên liệu NGOÀI danh mục thì không có gì
                để khóa theo, vẫn cho chọn tay như cũ. */}
            {r.ingredientId ? (
              <Input value={r.unit} disabled aria-label="Đơn vị (khóa theo danh mục)" />
            ) : (
              <Select
                value={r.unit}
                onChange={(e) => update(i, { unit: e.target.value })}
                aria-label="Đơn vị"
                disabled={!measureUnits.ready}
              >
                {measureUnits.options.map((u) => (
                  <option key={u}>{u}</option>
                ))}
              </Select>
            )}
            <div className="flex flex-wrap gap-1">
              {allergens.map((a) => (
                <Badge key={a} variant="danger">{a}</Badge>
              ))}
            </div>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              disabled={rows.length === 1}
              onClick={() => onChange(rows.filter((_, idx) => idx !== i))}
              aria-label="Xóa thành phần"
            >
              <Trash2 />
            </Button>
          </div>
        );
      })}
      <div className="flex flex-wrap items-center gap-3">
        <Button
          type="button"
          variant="outline"
          size="sm"
          className="border-primary/40 text-primary hover:bg-primary/10"
          onClick={() => onChange([...rows, emptyIngredient()])}
        >
          <Plus /> Thêm thành phần
        </Button>
        {(hiddenCount > 0 || showUnpriced) && (
          <label className="flex cursor-pointer items-center gap-1.5 text-xs text-muted-foreground">
            <input
              type="checkbox"
              className="size-3.5 accent-primary"
              checked={showUnpriced}
              onChange={(e) => setShowUnpriced(e.target.checked)}
            />
            Hiện cả nguyên liệu chưa có nhà cung cấp báo giá
            {hiddenCount > 0 && ` (${hiddenCount} mục đang ẩn)`}
          </label>
        )}
      </div>
    </div>
  );
}

/* ================= Component: combobox allergen + số HS bị dị ứng ================= */

function AllergenPicker({
  auto,
  manual,
  onChange,
  gradeProfile,
}: {
  auto: string[];
  manual: string[];
  onChange: (manual: string[]) => void;
  gradeProfile: Record<string, number>;
}) {
  const withCount = (a: string) =>
    gradeProfile[a] > 0 ? `${a} · ${gradeProfile[a]} HS` : a;
  return (
    <div className="space-y-1.5">
      <Label className="text-xs text-muted-foreground">
        Tác nhân dị ứng của món (auto từ thành phần + chọn thêm từ combobox — BR-069/BR-063)
      </Label>
      <div className="flex flex-wrap items-center gap-1.5">
        {auto.map((a) => (
          <Badge key={a} variant={gradeProfile[a] > 0 ? "danger" : "neutral"}>
            {withCount(a)} (auto)
          </Badge>
        ))}
        {manual.map((a) => (
          <Badge key={a} variant={gradeProfile[a] > 0 ? "danger" : "warning"}>
            {withCount(a)}
            <button
              type="button"
              className="ml-1 inline-flex"
              onClick={() => onChange(manual.filter((m) => m !== a))}
              aria-label={`Bỏ ${a}`}
            >
              <X className="size-3" />
            </button>
          </Badge>
        ))}
        {/* SearchableSelect (13/08/2026): danh mục tác nhân đủ dài để phải cuộn tìm
            trong `<select>` gốc — gõ vài chữ nhanh hơn. `value=""` để ô luôn quay về
            trạng thái "+ Thêm tác nhân…" sau mỗi lần chọn (chip mới hiện ở trên). */}
        <div className="w-44">
          <SearchableSelect
            value=""
            onChange={(v) => {
              if (v && !manual.includes(v) && !auto.includes(v)) onChange([...manual, v]);
            }}
            options={ALLERGENS.filter((a) => !auto.includes(a) && !manual.includes(a))}
            placeholder="+ Thêm tác nhân…"
            searchPlaceholder="Tìm tác nhân…"
          />
        </div>
      </div>
    </div>
  );
}

/* ================= Form chính (create + edit) ================= */

interface AffectedDetail {
  dishName: string;
  rows: { student: Student; matched: string[] }[];
}

export interface MenuFormProps {
  mode: "create" | "edit";
  /** Bắt buộc khi mode = "edit" — record menu hiện có (kèm blocksData nếu đã lưu). */
  menu?: PersistedMenu;
  /** Prefill khi tạo từ lưới tuần (?grade=&date=). */
  initialGrade?: string;
  initialDate?: string;
  /** Mở sẵn dialog AI Assist — dùng khi vào từ nút "AI đề xuất" ở lưới thực đơn. */
  openAiOnMount?: boolean;
  /** Mở form và cuộn tới đúng buổi cần sửa (?meal=BREAKFAST|LUNCH|SNACK). */
  focusMeal?: string;
  /**
   * Soạn ĐÚNG MỘT buổi ăn — dùng khi mở từ một ô của lịch thực đơn tuần.
   *
   * Nhận xét Minh 23/07/2026: bấm ô "Sáng" của Thứ 4 mà nhảy sang trang khác rồi
   * bày ra cả ba bữa là bắt người dùng tự tìm lại thứ mình vừa bấm. Ở chế độ này
   * form chỉ hiện buổi đó, KHÔNG cho đổi loại bữa/thêm buổi khác; các buổi còn lại
   * của ngày vẫn được giữ nguyên khi lưu (xem `fullBlocks`).
   */
  singleMeal?: MealType;
  /**
   * G9: buổi khởi tạo sẵn khi "Áp dụng mẫu" từ ô lịch — dựng bằng
   * `mealBlockFromTemplate` (món + suất thay thế + templateId đã đổ đủ).
   * Chỉ dùng cùng `singleMeal`, và chỉ khi ngày đó CHƯA có buổi này (buổi đã có
   * món luôn thắng, không để mẫu đè lên dữ liệu thật).
   */
  initialBlock?: MealBlock;
  /** Nhúng trong dialog: bỏ PageHeader, bỏ nút Nộp duyệt, lưu xong không điều hướng. */
  embedded?: boolean;
  /** Gọi sau khi lưu thành công — thay cho điều hướng sang trang chi tiết. */
  onSaved?: (menuId: string) => void;
}

/** Ghép buổi vừa soạn vào danh sách buổi của cả ngày, giữ thứ tự Sáng → Trưa → Xế. */
function mergeBlock(all: MealBlock[], one: MealBlock): MealBlock[] {
  const order = (t: MealType) => MEAL_TYPES.findIndex((m) => m.value === t);
  return [...all.filter((b) => b.mealType !== one.mealType), one].sort(
    (a, b) => order(a.mealType) - order(b.mealType)
  );
}

export function MenuForm({
  mode,
  menu,
  initialGrade,
  initialDate,
  focusMeal,
  openAiOnMount,
  singleMeal,
  initialBlock,
  embedded,
  onSaved,
}: MenuFormProps) {
  // Kho đọc qua API — cảnh báo "lô sắp hết hạn" trước đây tính trên mảng seed nên
  // đứng yên kể cả sau khi bếp đã xuất hết lô đó.
  const { data: batchPage } = usePagedList<InventoryBatchDto>("inventory", { pageSize: 200 });
  const liveBatches = batchPage?.items ?? [];
  /*
   * Danh mục nguyên liệu THẬT — dùng để RÀNG BUỘC gợi ý của AI (sửa 09/08/2026).
   *
   * Trước đó prompt chỉ truyền allergen + lô sắp hết hạn, KHÔNG có danh mục, nên model bịa tên
   * nguyên liệu tự do. Đo thật: nó trả "Gạo", "Nước", "Thịt lợn", "tôm" trong khi danh mục là
   * "Gạo tẻ", "Thịt heo nạc" — "Nước"/"tôm" không tồn tại trong hệ thống.
   *
   * Hậu quả không chỉ là tên xấu:
   *   · tên không khớp danh mục ⇒ `ingredientId` rỗng ⇒ món KHÔNG truy xuất được lô (BR-076),
   *     đúng cảnh báo "Ngoài danh mục" mà chính form này đã hiện;
   *   · nguyên liệu ngoài DANH MỤC HỢP ĐỒNG đã duyệt thì bếp KHÔNG mua được — từ 08/08/2026
   *     `POST /purchase-requests` chặn bằng ING-077. Tức AI có thể vẽ ra món không thể nấu.
   */
  /*
   * Nguồn danh mục cho AI là DANH MỤC HỢP ĐỒNG ĐÃ DUYỆT của trường (`GET /catalog-items`,
   * thêm 09/08/2026), KHÔNG phải danh mục nguyên liệu toàn hệ thống.
   *
   * Đo thật 09/08/2026 trên dữ liệu demo: `/ingredients` có 6 mục, danh mục đã duyệt của
   * SCH-0001 chỉ có 4 (thiếu Cà chua, Trứng gà). Ép AI theo danh sách 6 mục thì nó vẫn đề xuất
   * được món dùng Cà chua — hợp lệ trên màn thực đơn, nhưng tới lúc đặt mua thì
   * `POST /purchase-requests` chặn bằng ING-077 vì mặt hàng đó không có trong hợp đồng nào.
   * Ràng buộc đúng phải là thứ bếp MUA ĐƯỢC.
   */
  // `usePagedList` chứ KHÔNG phải `usePlainList`: `/catalog-items` trả `PagedResponse`
  // ({items,total,…}) trong khi `/ingredients` trả mảng trần. Dùng nhầm hook thì `.length` là
  // `undefined` ⇒ im lặng rơi về danh mục chung, đúng cái bẫy đang sửa.
  const { data: catalogPage } = usePagedList<CatalogItemDto>("catalog-items", { pageSize: 200 });
  const approvedCatalog = catalogPage?.items ?? [];
  const { data: allIngredients = [] } = usePlainList<IngredientDto>("ingredients");
  // `id` giữ nguyên là MÃ nguyên liệu (`ING-…`, D1) ở cả hai nguồn — chỗ áp dụng gợi ý gán nó
  // vào `ingredientId` của món để món nối được về lô.
  const aiIngredientCatalog: { id?: string; name?: string; unit?: string }[] = approvedCatalog.length
    ? approvedCatalog.map((c) => ({
        id: c.ingredientId ?? undefined,
        name: c.ingredientName ?? undefined,
        unit: c.unit ?? undefined,
      }))
    : allIngredients;
  // Trường chưa duyệt mặt hàng nào ⇒ rơi về danh mục chung, nhưng phải NÓI RÕ trên màn:
  // im lặng dùng danh mục rộng hơn chính là cái bẫy vừa gỡ ở trên.
  const catalogIsFallback = approvedCatalog.length === 0;
  /*
   * Danh mục cho việc NGƯỜI DÙNG TỰ CHỌN TAY nguyên liệu của "món trong thực đơn" (chặn
   * 11/08/2026, theo Nam) — cùng nguồn `approvedCatalog` với AI ở trên, khác `aiIngredientCatalog`
   * ở field đơn vị: dùng `ingredientUnit` (đơn vị CHUẨN thật, g/ml — khớp `IngredientEditor` cần
   * cho công thức) thay vì `unit` (đơn vị hợp đồng, VD "bao") mà AI đang dùng.
   *
   * CHỈ áp cho `IngredientEditor` bên trong `MenuForm` (món trong MỘT thực đơn của MỘT
   * trường) — KHÔNG áp cho thư viện món dùng chung (`dishes/page.tsx` không truyền prop này),
   * vì `Dish` cố ý không gắn `school_id` (TDS §5.2) — món phải tái dùng được cho trường khác.
   */
  const schoolApprovedIngredientCatalog: { id?: string; name?: string; unit?: string }[] = approvedCatalog.length
    ? approvedCatalog.map((c) => ({
        id: c.ingredientId ?? undefined,
        name: c.ingredientName ?? undefined,
        unit: c.ingredientUnit ?? c.unit ?? undefined,
      }))
    : allIngredients;
  const router = useRouter();
  const create = useCreate<Partial<PersistedMenu>>("menus");
  // Nối thật 06/08/2026: `dishCostPerServing`/`estimateMenuCostPerServing`/`substituteCostDelta`
  // (lib/mock-data.ts) mặc định đọc SUPPLIER_PRODUCTS/PARTNERS (seed) nếu không truyền — phải
  // truyền dữ liệu THẬT vào đây, không thì chi phí ước tính/so sánh suất thay thế vẫn dựng trên
  // giá seed cũ dù NCC đã báo giá thật khác (⚠️ NGOÀI PHẠM VI MF04 — GET /partners 403 với
  // SERVICE_CATERERS, xem chú thích ở `IngredientEditor`; `partners` rỗng thì mọi giá
  // coi như "chưa có NCC báo giá", không vỡ UI, chỉ thiếu chính xác cho tới khi mở quyền).
  const { data: costPartnersPage } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const costPartners = useMemo(() => costPartnersPage?.items ?? [], [costPartnersPage]);
  const { data: costSupplierProductsPage } = usePagedList<SupplierProductDto>("supplier-products", { pageSize: 300 });
  const costSupplierProducts = useMemo(
    () => supplierProductDtosToMock(costSupplierProductsPage?.items ?? []),
    [costSupplierProductsPage]
  );
  // Trần chi phí là tham số CẤP TRƯỜNG (BR-080), không phải thuộc tính của từng
  // thực đơn — đọc tại đây rồi đóng băng vào bản ghi lúc lưu, để thực đơn đã duyệt
  // giữ nguyên trần tại thời điểm duyệt.
  // Nối thật 06/08/2026 — `GET /schools/{code}/config` (SchoolConfigResponse, 1 object,
  // KHÔNG phân trang) — trước đây gọi `usePagedList("school-config")`, resource không tồn
  // tại ở backend nên luôn đọc mảng seed giả `SCHOOL_CONFIG`.
  // Hook dùng chung `lib/school-config.ts` — cùng nguồn với lịch tuần, và tránh khai lại
  // shape của `SchoolConfigResponse` bằng tay (bản inline cũ thiếu `menuLeadTimeDays`).
  const schoolConfig = useSchoolConfig();
  const classrooms = useClassrooms();
  const gradeLevels = gradeLevelsOf(classrooms);
  const update = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");
  const isPending = create.isPending || update.isPending;

  // Toàn bộ menu trong phiên: nguồn "Sao chép món" + chống tạo trùng ngày×khối
  const { data: menusPage } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  // Memo hoá để `allMenus` không đổi tham chiếu mỗi render (dep của savedVariants useMemo).
  const allMenus = useMemo(() => menusPage?.items ?? [], [menusPage]);
  // Sao chép món chỉ từ thực đơn CHUNG của khối — không gợi ý copy từ thực đơn riêng.
  const copySources = standardMenus(allMenus).filter(
    (m) => m.blocksData?.length && m.id !== menu?.id
  );

  // THƯ VIỆN MÓN — chọn món có sẵn thay vì gõ lại thành phần + kcal mỗi lần lập
  // thực đơn ("Sao chép món từ thực đơn khác" chỉ là cách lách khi chưa có thư viện).
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = (dishesPage?.items ?? []).filter((d) => d.isActive);
  const createDish = useCreate<Partial<Dish>>("dishes");
  // Danh mục nhóm chế độ ăn THẬT (`GET /diet-groups`) — 06/08/2026: KHÔNG còn dùng
  // `DIET_GROUPS` khai tay ở mock-data.ts, mảng đó thiếu nhóm thật (VD "Không gluten").
  const dietGroups = useDietGroups();

  const [grade, setGrade] = useState(menu?.grade ?? initialGrade ?? "");
  /*
   * V44 — `CreateMenuRequest.gradeId` là @NotBlank UUID, còn `grade` ở đây là NHÃN
   * ("Khối 3"). Quy ước D1: nhãn là khoá hiển thị/URL, UUID chỉ xuất hiện ở ranh giới
   * gửi request (`lib/grades.ts`). Màn này trước 08/08/2026 KHÔNG gửi `gradeId` lẫn
   * `mealType` — cùng lỗi đã sửa ở lịch tuần, chỉ chưa ai đi qua đường tạo mới ở đây.
   */
  const gradeId = useGradeId(grade);
  const [menuDate, setMenuDate] = useState(menu?.menuDateIso ?? initialDate ?? "");
  // Số suất mặc định = SĨ SỐ THẬT của khối (cộng từ Classroom.students), không
  // còn hằng 380. Người dùng vẫn sửa được (HS nghỉ, suất khách) nhưng có mốc đúng
  // để đối chiếu — sai số suất sẽ kéo theo sai lượng xuất kho FEFO.
  const [servings, setServings] = useState(String(menu?.servings ?? ""));
  const [servingsTouched, setServingsTouched] = useState(menu?.servings != null);
  const [blocks, setBlocks] = useState<MealBlock[]>(() => {
    // Giữ nguyên templateId/templateName (G9): sửa lại một buổi đã áp mẫu mà làm
    // rơi tham chiếu thì ô lịch mất tên mẫu ngay sau lần lưu đầu tiên.
    const existing =
      menu?.blocksData?.map((b) => ({
        mealType: b.mealType,
        dishes: b.dishes,
        templateId: b.templateId,
        templateName: b.templateName,
        templates: b.templates,
      })) ?? [];
    // Chế độ 1 buổi: chỉ nạp đúng buổi đó; ngày chưa có buổi này thì lấy buổi đổ
    // sẵn từ mẫu (nếu áp mẫu) hoặc mở buổi trống. Buổi ĐÃ CÓ luôn thắng mẫu.
    if (singleMeal) {
      return [
        existing.find((b) => b.mealType === singleMeal) ??
          initialBlock ?? {
            mealType: singleMeal,
            dishes: [emptyDish()],
          },
      ];
    }
    return existing.length ? existing : [{ mealType: "LUNCH", dishes: [emptyDish()] }];
  });
  /*
   * SUẤT RIÊNG THEO CHẾ ĐỘ ĂN — 22/07/2026 đưa vào chính màn lập thực đơn.
   *
   * Trước đó `MenuVariant` chỉ tạo được ở màn Bữa ăn đặc biệt, tức là SAU khi
   * thực đơn đã công bố (7/7 suất riêng trong seed đều gắn vào thực đơn
   * PUBLISHED). Người lập thực đơn không hề thấy khối mình còn nhóm nào chưa có
   * gì ăn, và Ban giám hiệu duyệt xong mới có người thêm suất — duyệt A, nấu A+B.
   *
   * Giữ ở state cục bộ rồi đẩy lên khi lưu, vì thực đơn tạo mới chưa có id để
   * suất riêng trỏ về.
   */
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  // Adapter → shape mock `Student` (nối thật 05/08/2026): `dietCoverageGaps`/
  // `dietGroupsNeededByGrade` viết cho `Student` (field non-optional), chỉ đọc
  // `class`/`specialDiet` — cùng cách `school-admin/special-meals` đã làm.
  const students = (studentsPage?.items ?? []).map(studentDtoToMock);
  // Thực đơn riêng của thực đơn đang sửa — nay là `Menu` (audience != "ALL"), nhìn
  // per-buổi qua projection và lọc về đúng thực đơn chung này.
  const savedVariants = useMemo(
    () => specialMenuMeals(allMenus).filter((v) => v.menuId === menu?.id),
    [allMenus, menu?.id]
  );
  const [draftVariants, setDraftVariants] = useState<MenuVariant[]>([]);
  const variants = [...savedVariants, ...draftVariants];
  const deleteMenu = useDelete("menus");
  const [variantTarget, setVariantTarget] = useState<{ mealType: MealType; groupId: string } | null>(null);

  const [error, setError] = useState("");
  const [aiOpen, setAiOpen] = useState(Boolean(openAiOnMount));
  const [submitOpen, setSubmitOpen] = useState(false);
  const [detail, setDetail] = useState<AffectedDetail | null>(null);

  const gradeProfile = grade ? gradeAllergySummary(grade, {}, students) : {};

  // Nối AI thật (06/08/2026) — trước đây `AI_SUGGESTIONS` là mảng tĩnh viết cứng trong
  // code, dialog "AI Assist" không gọi API nào. Giờ gọi `POST /ai/chat` thật (cùng
  // endpoint chatbot `/ai-chat`), yêu cầu model trả JSON món ăn theo đúng khung để
  // chèn thẳng vào thực đơn — parse lỗi thì hiện nguyên văn câu trả lời thay vì giấu.
  const [aiLoading, setAiLoading] = useState(false);
  const [aiSuggestions, setAiSuggestions] = useState<AiDishSuggestion[] | null>(null);
  const [aiRawText, setAiRawText] = useState("");
  const [aiFetchError, setAiFetchError] = useState("");

  async function fetchAiSuggestions() {
    setAiLoading(true);
    setAiFetchError("");
    setAiSuggestions(null);
    const expiringSoon = liveBatches
      .filter((b) => b.quarantine == null && b.status === "EXPIRING_SOON")
      .map((b) => `${b.ingredient} (${b.remaining ?? b.quantity} ${b.unit}, HSD ${formatVnDate(b.expiryDate)})`);
    const allergenList = Object.entries(gradeProfile)
      .map(([name, count]) => `${name} (${count} học sinh)`)
      .join(", ");
    /*
     * Danh sách nguyên liệu ĐƯỢC PHÉP dùng, kèm đơn vị chuẩn.
     *
     * Đơn vị tách bằng " — đơn vị …" chứ KHÔNG viết "Tên (kg)". Đo 09/08/2026 trên trình duyệt:
     * với dạng ngoặc, model làm đúng y lời dặn "ghi tên CHÍNH XÁC như liệt kê" — nó chép nguyên
     * cả đuôi, trả về `"Thịt heo nạc (kg)"`. Tên đó không khớp danh mục nên TOÀN BỘ nguyên liệu
     * bị bộ lọc loại sạch: màn hình hiện "Đã loại 3 nguyên liệu… / Không còn nguyên liệu nào hợp
     * lệ" trong khi cả ba đều nằm trong danh mục. Lỗi của prompt, không phải của model.
     */
    const allowed = aiIngredientCatalog
      .filter((c) => c.name)
      .map((c) => `${c.name} — đơn vị ${c.unit ?? "kg"}`);
    const prompt = [
      `Đề xuất 2-4 món ăn cho thực đơn bữa trưa/bữa phụ của trường tiểu học, khối "${grade || "chưa xác định"}".`,
      allergenList ? `Học sinh khối này đang có dị ứng: ${allergenList} — TUYỆT ĐỐI không đề xuất món chứa các allergen này.` : "Chưa có dữ liệu dị ứng của khối.",
      /*
       * Ràng buộc CỨNG vào danh mục. Không có dòng này thì model bịa tên nguyên liệu, món
       * không nối được về lô (BR-076) và bếp không mua được nguyên liệu ngoài hợp đồng (ING-077).
       */
      allowed.length
        ? `BẮT BUỘC: chỉ được dùng nguyên liệu trong danh mục hợp đồng sau. Trường "name" của nguyên liệu phải là TÊN đứng trước dấu "—", chép chính xác từng chữ, KHÔNG kèm đơn vị, KHÔNG thêm nguyên liệu ngoài danh sách (kể cả nước/gia vị). Danh mục: ${allowed.join("; ")}.`
        : "",
      expiringSoon.length ? `Ưu tiên tận dụng nguyên liệu sắp hết hạn trong kho: ${expiringSoon.join("; ")}.` : "",
      "Chỉ trả lời DUY NHẤT một mảng JSON hợp lệ, không thêm chữ nào khác, đúng khung:",
      `[{"name":"tên món","mealType":"LUNCH|BREAKFAST|SNACK","ingredients":[{"name":"tên nguyên liệu","quantity":"số","unit":"g|ml|quả..."}],"kcal":"số","protein":"số gram","carb":"số gram","fat":"số gram","rationale":"lý do ngắn gọn"}]`,
    ].filter(Boolean).join("\n");

    try {
      const { data } = await api.post<{ reply: string }>("/ai/chat", { message: prompt });
      setAiRawText(data.reply ?? "");
      const suggestions = parseAiSuggestions(data.reply ?? "");
      if (suggestions.length === 0) throw new Error("AI không đề xuất được món nào phù hợp.");
      /*
       * KHÔNG tin model đã tuân thủ — kiểm lại từng nguyên liệu với danh mục và LOẠI những
       * cái bịa. Ràng buộc trong prompt là lời đề nghị, không phải bảo đảm; một tên trôi lọt
       * là một món không truy xuất được lô.
       */
      const known = new Map(aiIngredientCatalog.map((c) => [normalizeIngredientName(c.name), c.name ?? ""]));
      const cleaned = suggestions.map((d) => {
        // Giữ lại thì CHUẨN HOÁ luôn tên về đúng chính tả trong danh mục — có vậy `ingredientId`
        // mới tra được, và món mới nối được về lô (BR-076).
        const kept = (d.ingredients ?? []).flatMap((i) => {
          const canonical = known.get(normalizeIngredientName(i.name));
          return canonical ? [{ ...i, name: canonical }] : [];
        });
        const dropped = (d.ingredients ?? []).length - kept.length;
        return { ...d, ingredients: kept, droppedIngredients: dropped };
      });
      setAiSuggestions(cleaned);
    } catch (err) {
      setAiFetchError(apiErrorMessage(err, "Không thể lấy gợi ý từ AI."));
    } finally {
      setAiLoading(false);
    }
  }

  useEffect(() => {
    if (aiOpen && aiSuggestions === null && !aiLoading && !aiFetchError) {
      fetchAiSuggestions();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [aiOpen]);

  // BR-061: ngày áp dụng phải cách hôm nay tối thiểu N ngày (mặc định 3).
  // Override bằng NEXT_PUBLIC_MENU_LEAD_TIME_DAYS=0 trong .env khi test/demo.
  const menuLeadTimeDays = Number(process.env.NEXT_PUBLIC_MENU_LEAD_TIME_DAYS ?? 3);
  const minDateIso = menuLeadTimeDays > 0 ? isoAfterDays(menuLeadTimeDays) : "";
  const leadOk = menuDate !== "" && (minDateIso === "" || menuDate >= minDateIso);
  const dayLabel = menuDate ? dayLabelOfIso(menuDate) : "";

  /**
   * 13/08/2026 (Nam) — KHOÁ thực đơn đã qua duyệt. Trước đây mode "edit" cho sửa bất kể trạng
   * thái: mở một bữa đã APPROVED/PUBLISHED vẫn gõ và lưu đè được, trong khi bản đã duyệt chính
   * là căn cứ bếp nấu và là đầu vào tính nhu cầu xuất kho — sửa lén sau lưng BGH thì bản duyệt
   * và bản nấu lệch nhau mà không ai biết.
   *
   * Chỉ DRAFT/REJECTED mới sửa được (cùng tập `EDITABLE_STATUSES` mà lịch tuần dùng để quyết
   * định bữa nào còn nộp duyệt được).
   */
  const lockedByApproval =
    mode === "edit" && menu?.status != null && !MENU_EDITABLE_STATUSES.has(String(menu.status));

  // Mỗi ngày × khối chỉ có 1 thực đơn — tạo trùng bị chặn, hướng sang sửa bản đã có
  const duplicateMenu =
    mode === "create" && grade && menuDate
      ? standardMenus(allMenus).find((m) => m.grade === grade && m.menuDateIso === menuDate)
      : undefined;

  const unusedMealTypes = MEAL_TYPES.filter(
    (m) => !blocks.some((b) => b.mealType === m.value)
  );

  // Cuộn tới buổi cần sửa khi mở từ nút "Sửa bữa này" (?meal=)
  useEffect(() => {
    if (!focusMeal) return;
    document
      .getElementById(`meal-${focusMeal}`)
      ?.scrollIntoView({ behavior: "smooth", block: "start" });
  }, [focusMeal]);

  /*
   * Số suất mặc định theo sĩ số khối — trước đây chỉ điền trong `onChange` của ô
   * chọn khối. Khối đến sẵn từ ngoài (ô lịch tuần, hoặc `?grade=` trên URL) thì
   * onChange không bao giờ chạy: thực đơn lưu ra 0 suất, kéo theo chi phí ước
   * tính 0₫ và nhu cầu xuất kho 0 mà không có cảnh báo nào.
   */
  useEffect(() => {
    if (servingsTouched || servings !== "" || !grade) return;
    const n = gradeEnrollmentOf(grade, classrooms);
    if (n > 0) setServings(String(n));
  }, [grade, classrooms, servings, servingsTouched]);

  function updateBlock(bi: number, patch: Partial<MealBlock>) {
    setBlocks((prev) => prev.map((b, i) => (i === bi ? { ...b, ...patch } : b)));
  }

  /**
   * Đổi tên món. Gõ/chọn trúng TÊN một món trong thư viện thì đổ luôn BOM + kcal
   * + macro và chốt `dishId` — người lập thực đơn không phải gõ lại, đồng thời
   * thành phần chắc chắn khớp catalog nên allergen được nhận diện đúng (BR-069/076).
   * Đã gắn với món nào rồi thì không đổ lại (tránh ghi đè chỉnh tay của người dùng).
   */
  function changeDishName(bi: number, di: number, name: string, current: DishForm) {
    const lib = dishLibrary.find((d) => d.name === name);
    if (!lib || current.dishId === lib.id) {
      // Sửa tên rời khỏi món thư viện → không còn là món đó nữa
      updateDish(bi, di, { name, dishId: lib?.id ?? undefined });
      return;
    }
    updateDish(bi, di, {
      name,
      dishId: lib.id,
      ingredients: lib.ingredients.map((r) => ({ ...r, quantity: String(r.quantity ?? "") })),
      manualAllergens: [...lib.manualAllergens],
      kcal: String(lib.kcal),
      protein: String(lib.protein),
      carb: String(lib.carb),
      fat: String(lib.fat),
    });
  }

  /**
   * Đẩy món vừa gõ tay lên THƯ VIỆN để lần sau chọn lại — khép vòng "gõ một lần".
   * Chỉ mở khi món đã đủ tên + kcal + thành phần có định lượng, vì thư viện thiếu
   * định lượng thì mọi thực đơn lấy món đó về cũng không tính được nhu cầu kho.
   */
  function saveDishToLibrary(bi: number, di: number, dish: DishForm) {
    const rows = dish.ingredients.filter((r) => r.name.trim() && Number(r.quantity) > 0);
    const id = dishIdOfName(dish.name.trim(), dishLibrary.map((d) => d.id));
    createDish.mutate(
      {
        id,
        name: dish.name.trim(),
        category: "MON_MAN",
        kcal: Number(dish.kcal) || 0,
        protein: Number(dish.protein) || 0,
        carb: Number(dish.carb) || 0,
        fat: Number(dish.fat) || 0,
        ingredients: rows,
        manualAllergens: [...dish.manualAllergens],
        isActive: true,
      },
      {
        onSuccess: () => {
          updateDish(bi, di, { dishId: id });
          toast.success(
            `Đã thêm "${dish.name.trim()}" vào Thư viện món — sửa phân loại/NCC tại màn Thư viện món.`
          );
        },
      }
    );
  }

  function updateDish(bi: number, di: number, patch: Partial<DishForm>) {
    setBlocks((prev) =>
      prev.map((b, i) =>
        i === bi
          ? { ...b, dishes: b.dishes.map((d, j) => (j === di ? { ...d, ...patch } : d)) }
          : b
      )
    );
  }

  /**
   * Ghi SUẤT DỊ ỨNG của món — đi qua `upsertSub` nên KHÔNG đụng suất của các nhóm
   * chế độ ăn (do màn "Bữa ăn đặc biệt" quản). Đây là chỗ trước đây gán đè cả ô
   * `substitute` đơn khiến hai màn xoá dữ liệu của nhau.
   */
  function setAllergySub(bi: number, di: number, dish: DishForm, patch: Partial<SubstituteDish>) {
    // G10: mở suất dị ứng LẦN ĐẦU của món có món thay thế mặc định trong thư
    // viện → điền sẵn nguyên bộ (tên + BOM + kcal), chỉ còn ghi đè nếu tuần này
    // khác. Suất đã có thì giữ nguyên, không đè lên chỉnh sửa của người dùng.
    const existingSub = allergySubOf(dish);
    const libDefault = existingSub ? undefined : defaultSubstituteOf(dish, dishLibrary);
    const base =
      existingSub ?? (libDefault ? substituteFromLibraryDish(libDefault, "") : emptySubstitute(""));
    if (libDefault) {
      toast.success(
        `Đã điền sẵn món thay thế mặc định "${libDefault.name}" từ Thư viện món — sửa lại nếu tuần này dùng món khác.`
      );
    }
    // Ghi luôn tác nhân mà suất này phải tránh — trước đây chỉ biết "suất dị ứng"
    // chung chung, không tra được nó sinh ra vì allergen nào.
    const next = upsertSub(dish, {
      ...base,
      ...patch,
      allergenIds: dishState(dish, grade, students).conflicts.map((c) => c.allergen),
    });
    updateDish(bi, di, { substitutes: next.substitutes, isSpecialDish: true });
  }

  /** Gỡ suất dị ứng của món. */
  function clearAllergySub(bi: number, di: number, dish: DishForm) {
    const next = removeSub(dish);
    updateDish(bi, di, {
      substitutes: next.substitutes,
      isSpecialDish: next.isSpecialDish,
    });
  }

  /** Mỗi loại bữa chỉ có 1 buổi trong ngày — đổi loại bữa sang loại đã tồn tại bị chặn. */
  function changeBlockMealType(bi: number, mealType: MealType) {
    if (blocks.some((b, i) => i !== bi && b.mealType === mealType)) {
      toast.error(`${mealLabel(mealType)} đã có trong thực đơn ngày này — mỗi loại bữa chỉ 1 buổi.`);
      return;
    }
    updateBlock(bi, { mealType });
  }

  /** Sao chép toàn bộ buổi/món từ 1 thực đơn ngày khác (giải pháp "lặp lại"). */
  function copyFromMenu(sourceId: string) {
    const src = copySources.find((m) => m.id === sourceId);
    if (!src?.blocksData?.length) return;
    // Chế độ 1 buổi: chỉ lấy đúng buổi đang soạn, không kéo cả 3 bữa của ngày nguồn.
    if (singleMeal) {
      const srcBlock = src.blocksData.find((b) => b.mealType === singleMeal);
      if (!srcBlock) {
        toast.error(
          `${src.week} (${src.grade}) không có ${mealLabel(singleMeal).toLowerCase()} để sao chép.`
        );
        return;
      }
      setBlocks([JSON.parse(JSON.stringify({ mealType: singleMeal, dishes: srcBlock.dishes }))]);
      toast.success(
        `Đã sao chép ${mealLabel(singleMeal).toLowerCase()} từ ${src.week} (${src.grade}) — chỉnh lại món cho ngày này nếu cần.`
      );
      return;
    }
    setBlocks(
      JSON.parse(
        JSON.stringify(src.blocksData.map((b) => ({ mealType: b.mealType, dishes: b.dishes })))
      )
    );
    if (!grade) setGrade(src.grade);
    toast.success(`Đã sao chép ${src.blocksData.length} buổi ăn từ ${src.week} (${src.grade}) — chỉnh lại món cho ngày này nếu cần.`);
  }

  const allDishes = blocks.flatMap((b) => b.dishes);
  /**
   * Toàn bộ buổi ăn của NGÀY — ở chế độ 1 buổi, `blocks` chỉ chứa buổi đang soạn
   * nên mọi con số ghi vào bản ghi (số món, xung đột dị ứng, chi phí/ngày) phải
   * tính trên bản ghép này; nếu không, sửa bữa sáng sẽ xoá sạch bữa trưa/xế.
   */
  const fullBlocks = useMemo<MealBlock[]>(() => {
    if (!singleMeal) return blocks;
    return mergeBlock(menu?.blocksData ?? [], blocks[0]);
  }, [singleMeal, menu?.blocksData, blocks]);

  /**
   * Chi phí ước tính một suất — tính lại mỗi khi sửa món/định lượng/số suất, để
   * người lập thực đơn thấy mình có vượt trần hay không TRƯỚC khi nộp duyệt, thay
   * vì để Ban giám hiệu phát hiện.
   */
  const costEstimate = useMemo(() => {
    const n = Number(servings) || 0;
    if (!grade || n <= 0) return { perServing: 0, missingPrices: [] as string[] };
    return estimateMenuCostPerServing(
      { grade, blocksData: fullBlocks } as unknown as SeedMenu,
      n,
      allIngredients,
      costSupplierProducts,
      costPartners
    );
  }, [grade, servings, fullBlocks, allIngredients, costSupplierProducts, costPartners]);
  const ceiling = schoolConfig?.costCeiling ?? 30000;

  /** BR-062 — chuẩn dinh dưỡng bữa trưa của khối đang soạn (cảnh báo mềm, xem Alert bên dưới). */
  const nutritionTarget = useNutritionTarget(grade || undefined);
  const lunchKcal = blocks
    .filter((b) => b.mealType === "LUNCH")
    .flatMap((b) => b.dishes)
    .reduce((sum, d) => sum + (Number(d.kcal) || 0), 0);
  const lunchKcalGap =
    nutritionTarget && nutritionTarget.kcal > 0 && lunchKcal > 0
      ? (lunchKcal - nutritionTarget.kcal) / nutritionTarget.kcal
      : 0;

  /**
   * Bữa gốc đã có món có TÊN chưa — cửa vào của mọi thứ "đặc biệt".
   *
   * Suất riêng (chế độ ăn) và món phụ (dị ứng) đều được định nghĩa BẰNG bữa gốc:
   * suất riêng giữ lại những món nhóm ăn được rồi thay phần còn lại, món phụ thay
   * đúng món chạm dị ứng. Chưa có món gốc nào để mà giữ/thay thì phần đặc biệt
   * không có nghĩa — mà trước 23/07/2026 vẫn khai được, đẻ ra suất riêng trỏ về
   * một món trắng (Minh: "luôn phải tạo món đầu tiên đã rồi mới có món đặc biệt").
   */
  const baseDishesNamed = blocks.every(
    (b) => b.dishes.length > 0 && b.dishes.every((d) => d.name.trim() !== "")
  );

  const allStates = allDishes.map((d) => dishState(d, grade, students));
  const dietGaps = grade
    ? dietCoverageGaps(grade, menu?.id ?? "(mới)", blocks, variants, dietGroups, students)
    : [];
  const checks = {
    gradeDate: grade !== "" && leadOk,
    noDuplicate: duplicateMenu == null,
    blocksValid: blocks.every((b) => b.dishes.length > 0),
    ingredients: allDishes.every(
      (d) => d.name.trim() !== "" && d.ingredients.some((r) => r.name.trim() && r.quantity)
    ),
    nutrition: allDishes.every((d) => d.kcal.trim() !== ""),
    allergy: allStates.every((s) => s.conflictResolved),
    special: allStates.every((s) => s.specialComplete),
    // BR-065/BR-125 — mọi nhóm chế độ ăn CÓ HỌC SINH trong khối phải ăn được
    // bữa này: hoặc bữa gốc đã hợp, hoặc đã có suất riêng.
    dietCoverage: dietGaps.length === 0,
  };
  const allPassed = Object.values(checks).every(Boolean);

  const totalKcal = allDishes.reduce((sum, d) => sum + (Number(d.kcal) || 0), 0);
  const specialPortions = allDishes.reduce(
    (sum, d) => sum + totalSubPortions(d),
    0
  );

  function saveDraft() {
    // Chốt cuối, không chỉ ẩn nút: mở thẳng route `[id]/edit` của bữa đã duyệt vẫn vào được form.
    if (lockedByApproval) {
      setError(
        `Thực đơn đang ở trạng thái ${menu?.status} — đã qua duyệt thì không sửa trực tiếp được.`
          + " Đề nghị Ban giám hiệu trả lại (BR-048) rồi sửa."
      );
      return;
    }
    if (!grade || !menuDate) {
      setError("Phải chọn Khối và Ngày áp dụng trước khi lưu.");
      return;
    }
    /*
     * Món KHÔNG TÊN không được lưu, kể cả ở bản nháp (23/07/2026 — Minh).
     *
     * Trước đây `saveDraft` chỉ soi khối + ngày, nên bấm Lưu với form trắng vẫn ra
     * một buổi ăn "có món" mà món đó không có gì để gọi tên. Hệ quả kéo dài xuống
     * dưới: `dishIdOfName("")` trả về slug rác `mon-moi`, suất riêng giữ lại món đó
     * từ bữa gốc và lịch tuần bày ra "mon-moi" thay cho tên món.
     */
    const unnamed = blocks.filter((b) => b.dishes.some((d) => d.name.trim() === ""));
    if (unnamed.length > 0) {
      setError(
        `Còn món chưa đặt tên ở ${unnamed.map((b) => mealLabel(b.mealType).toLowerCase()).join(", ")} — đặt tên món (chọn từ thư viện hoặc gõ món mới) rồi mới lưu được. Xoá dòng món thừa nếu không dùng.`
      );
      return;
    }
    setError("");
    persist("DRAFT");
  }

  /**
   * Gỡ MỘT buổi của thực đơn riêng đã lưu. Thực đơn riêng nay là `Menu`: bỏ buổi
   * đó khỏi blocksData; hết buổi thì xóa hẳn thực đơn riêng, còn buổi thì cập nhật.
   */
  function removeSavedMeal(v: MenuVariant) {
    const specialId = specialMenuIdOfMeal(v.id);
    const sm = allMenus.find((m) => m.id === specialId);
    if (!sm) return;
    const remaining = (sm.blocksData ?? []).filter((b) => b.mealType !== v.mealType);
    const groupLabel = dietGroups.find((g) => g.id === v.dietGroupId)?.label ?? v.dietGroupId;
    if (remaining.length === 0) {
      deleteMenu.mutate(specialId, {
        onSuccess: () => toast.info(`Đã gỡ thực đơn riêng ${groupLabel}.`),
      });
    } else {
      update.mutate(
        {
          id: specialId,
          blocksData: remaining,
          dishes: remaining.reduce((s, b) => s + b.dishes.length, 0),
        },
        { onSuccess: () => toast.info(`Đã gỡ ${MEAL_TYPE_LABEL[v.mealType]} khỏi thực đơn riêng ${groupLabel}.`) }
      );
    }
  }

  function persist(status: "DRAFT" | "PENDING_APPROVAL") {
    /*
     * Chặn nguyên liệu ngoài danh mục — cùng lý do với màn Thư viện món: cả chuỗi
     * xuất kho, truy xuất lô và tính chi phí đều nối bằng `ingredientId`, thiếu
     * khoá là món biến mất khỏi phiếu xuất mà không ai biết.
     */
    const offCatalog = [
      ...new Set(
        blocks // chỉ soi buổi đang soạn — buổi khác đã qua cửa này lúc lưu của chúng
          .flatMap((b) => b.dishes)
          .flatMap((d) => [...d.ingredients, ...(d.substitutes ?? []).flatMap((x) => x.ingredients)])
          .filter((r) => r.name.trim() && !r.ingredientId)
          .map((r) => r.name.trim())
      ),
    ];
    if (offCatalog.length > 0) {
      setError(
        `Nguyên liệu ngoài danh mục: ${offCatalog.join(", ")}. Chọn từ danh mục — thiếu khoá thì kho không tính được nhu cầu và không truy xuất được lô (BR-076).`
      );
      return;
    }
    if (!gradeId) {
      setError(
        `Chưa xác định được khối "${grade}" ở máy chủ — tải lại trang. Không gửi thiếu gradeId để server trả "Validation failed".`
      );
      return;
    }
    const newId = mode === "edit" ? menu!.id : `MENU-2026-${String(Date.now()).slice(-4)}`;
    // Số món + xung đột dị ứng là con số của CẢ NGÀY, không riêng buổi đang soạn.
    const fullDishes = fullBlocks.flatMap((b) => b.dishes);
    const fullStates = fullDishes.map((d) => dishState(d, grade, students));
    const payload: Partial<PersistedMenu> = {
      week: dayLabel,
      name: `Thực đơn ${dayLabel.replace(" — ", " ")} — ${grade}`,
      grade,
      gradeId,
      menuDateIso: menuDate,
      status,
      dishes: fullDishes.length,
      allergyConflicts: fullStates.filter(
        (s) => s.conflicts.length > 0 && !s.conflictResolved
      ).length,
      // Chi phí ước tính từ công thức × báo giá của NCC còn hợp tác. Trước
      // 22/07/2026 chỗ này ghi cứng 0, nên mọi thực đơn tạo mới hiện "0₫/30.000₫"
      // ở bảng duyệt của Ban giám hiệu và LUÔN đạt trần — kiểm soát chi phí BR-080
      // chưa từng chặn được gì.
      //
      // Cả hai đều là cột LƯU chứ không dẫn xuất lúc đọc: giá nguyên liệu và trần
      // chi phí đổi theo thời gian, thực đơn đã duyệt phải giữ con số tại thời
      // điểm duyệt (cùng nguyên tắc với `servedDishes`).
      estimatedCost: costEstimate.perServing,
      costCeiling: schoolConfig?.costCeiling ?? 30000,
      submittedAt:
        status === "PENDING_APPROVAL"
          ? new Date().toLocaleDateString("vi-VN")
          : menu?.submittedAt,
      createdBy: "Cty TNHH Suất ăn An Bình",
      blocksData: fullBlocks,
      servings: Number(servings) || 0,
    };

    // `savedId` = thực đơn (buổi) đầu tiên lưu thành công — đích điều hướng sau khi lưu.
    // Trước fan-out chỗ này dùng thẳng `newId`, mà `newId` nay chỉ còn là TIỀN TỐ.
    const onSuccess = (savedId: string) => {
      // Suất riêng khai trong form → THỰC ĐƠN RIÊNG (audience = nhóm) cho khối+ngày
      // này. Gộp các buổi đã khai theo từng nhóm thành một `Menu` đầy đủ; khai lúc
      // thực đơn còn nháp nên để DRAFT, đi kèm thực đơn chung đi duyệt.
      const byGroup = new Map<string, MenuVariant[]>();
      for (const v of draftVariants) {
        const arr = byGroup.get(v.dietGroupId);
        if (arr) arr.push(v);
        else byGroup.set(v.dietGroupId, [v]);
      }
      for (const [group, vs] of byGroup) {
        const label = dietGroups.find((g) => g.id === group)?.label ?? group;
        /*
         * C10 — thực đơn riêng bắt buộc khuôn {menuCode}--{dietGroupCode}, nên phải bám vào
         * MÃ THẬT đã lưu, không phải `newId` (nay chỉ là tiền tố của fan-out).
         *
         * ⚠️ CÒN NỢ (ghi ở 08_E2E_Findings_Backlog mục A2): thực đơn riêng vẫn gửi `merged`
         * nhiều buổi trong MỘT request nên vẫn dính đúng cái `blocks.get(0)` của backend —
         * khai suất riêng cho 2 buổi thì buổi sau rơi. Chưa fan-out ở đây vì mã thực đơn
         * riêng phải gắn với mã thực đơn chung CỦA TỪNG BUỔI, tức đổi cả quy ước đặt mã.
         */
        const specialId = `${savedId}--${group}`;
        const existing = allMenus.find((m) => m.id === specialId);
        const draftBlocks: MealBlock[] = vs.map((v) => ({
          mealType: v.mealType,
          /*
           * Món ngoài thư viện KHÔNG bị vứt: suất riêng giữ lại món của bữa gốc,
           * mà món đó có thể là món gõ tay (không có `Dish` master). Trước đây
           * `.filter(Boolean)` loại thẳng nó đi — thực đơn riêng lưu ra thiếu món
           * mà không báo gì. Lấy lại nguyên bản đã khai ở bữa gốc để giữ đủ thành
           * phần + kcal (BR-069).
           */
          dishes: v.dishes
            .map((d) => {
              const lib = dishLibrary.find((x) => x.id === d.dishId);
              if (lib) return dishFormFromLibrary(lib);
              const base = fullBlocks
                .find((b) => b.mealType === v.mealType)
                ?.dishes.find(
                  (bd) => (bd.dishId ?? dishIdOfName(bd.name.trim())) === d.dishId
                );
              // Suất riêng thay trọn bộ nên không mang theo suất phụ của món gốc.
              return base ? { ...base, isSpecialDish: false, substitutes: [] } : undefined;
            })
            .filter((d): d is DishForm => Boolean(d)),
          note: v.note,
          portions: v.portions,
        }));
        const merged = (
          existing
            ? [...(existing.blocksData ?? []).filter((b) => !draftBlocks.some((db) => db.mealType === b.mealType)), ...draftBlocks]
            : draftBlocks
        ).sort((a, b) => MEAL_SORT[a.mealType] - MEAL_SORT[b.mealType]);
        const specialPayload: Partial<PersistedMenu> = {
          id: specialId,
          week: dayLabel,
          name: `Thực đơn ${dayLabel.replace(" — ", " ")} — ${grade} · ${label}`,
          grade,
          menuDateIso: menuDate,
          status: "DRAFT",
          audience: group,
          dietGroupId: group,
          dishes: merged.reduce((s, b) => s + b.dishes.length, 0),
          allergyConflicts: 0,
          estimatedCost: costEstimate.perServing,
          costCeiling: schoolConfig?.costCeiling ?? 30000,
          createdBy: "Cty TNHH Suất ăn An Bình",
          servings: Math.max(0, ...merged.map((b) => b.portions ?? 0)),
          blocksData: merged,
        };
        if (existing) update.mutate({ id: specialId, ...specialPayload });
        else create.mutate(specialPayload);
      }
      setDraftVariants([]);
      if (status === "PENDING_APPROVAL") {
        toast.success("MSG-039 — Menu submitted for approval successfully. BGH sẽ nhận được yêu cầu phê duyệt.");
      } else if (fullBlocks.length > 1) {
        // Toast tổng kết nhiều buổi đã bắn ở nơi gọi — không bắn thêm cái thứ hai.
      } else if (singleMeal) {
        toast.success(
          `Đã lưu ${mealLabel(singleMeal).toLowerCase()} ${dayLabel} — ${grade} (MSG-07${mode === "edit" ? "4" : "1"}).`
        );
      } else if (mode === "edit") {
        toast.success("MSG-074 — Menu updated successfully.");
      } else {
        toast.success("MSG-071 — Menu draft was saved successfully.");
      }
      // Mở từ ô lịch thì đóng hộp thoại, ở lại lịch — điều hướng sang trang chi
      // tiết ở đây là đúng thứ nhận xét 23/07 muốn bỏ.
      if (onSaved) {
        onSaved(savedId);
        return;
      }
      // Lưu/nộp xong luôn về trang XEM thực đơn ngày đó (yêu cầu Minh 11/07)
      router.push(`/service-caterers/menus/${savedId}`);
    };

    /*
     * FAN-OUT THEO BUỔI (backlog A2, sửa 08/08/2026).
     *
     * `menu-form` là di tích của mô hình cũ "1 thực đơn = 1 NGÀY": nó gửi MỘT request mang
     * `blocksData` nhiều buổi. Nhưng ở backend `MenuServiceImpl.applyBlocksData()` có
     * `MenuBlock block = blocks.get(0);` — buổi thứ 2, thứ 3 bị VỨT IM LẶNG, không lỗi,
     * không cảnh báo. Mô hình thật là 1 thực đơn = (ngày × khối × BUỔI), đúng như lịch tuần
     * đang làm.
     *
     * Sửa ở FE chứ KHÔNG sửa backend: trạng thái duyệt là per-buổi ở khắp nơi
     * (`MenuWorkflowService`, `DietCoverageValidator`, event `MenuPublished` mang `mealType`
     * sang MF09) — thêm khái niệm "duyệt theo ngày" là lan sang MF09.
     *
     * Tuần tự chứ không `Promise.all`: hai buổi cùng (ngày, khối) chạm cùng ràng buộc
     * slot ở server, chạy song song thì lỗi 409 khó quy về đúng buổi nào.
     */
    const targets = fullBlocks.map((block) => {
      // Buổi này đã có thực đơn chung của đúng (ngày, khối) chưa? Có thì SỬA, không thì TẠO.
      const existing =
        mode === "edit" && block.mealType === menu?.blocksData?.[0]?.mealType
          ? menu
          : standardMenus(allMenus).find(
              (m) =>
                m.menuDateIso === menuDate &&
                m.grade === grade &&
                m.blocksData?.[0]?.mealType === block.mealType
            );
      return { block, existingId: existing?.id };
    });

    void (async () => {
      const failed: string[] = [];
      let firstSavedId = "";

      for (const { block, existingId } of targets) {
        const perMeal: Partial<PersistedMenu> = {
          ...payload,
          mealType: block.mealType,
          // Một request = MỘT buổi. Các con số dẫn xuất vẫn tính trên cả ngày (ở trên) vì
          // đó là thứ người duyệt nhìn, nhưng `blocksData` thì đúng một block.
          blocksData: [block],
          name: `Thực đơn ${dayLabel.replace(" — ", " ")} — ${grade} · ${mealLabel(block.mealType)}`,
        };
        try {
          if (existingId) {
            await update.mutateAsync({ id: existingId, ...perMeal });
            firstSavedId ||= existingId;
          } else {
            const id = `${newId}-${block.mealType}`;
            await create.mutateAsync({ id, ...perMeal });
            firstSavedId ||= id;
          }
        } catch (err) {
          failed.push(`${mealLabel(block.mealType).toLowerCase()}: ${apiErrorMessage(err, "lỗi không rõ")}`);
        }
      }

      if (failed.length > 0) {
        // Không điều hướng khi còn buổi lỗi — người dùng phải thấy mình đang mất dữ liệu gì.
        setError(
          `Đã lưu ${targets.length - failed.length}/${targets.length} buổi. Chưa lưu được — ${failed.join(" · ")}`
        );
        toast.error(`Còn ${failed.length}/${targets.length} buổi chưa lưu được.`);
        return;
      }
      if (targets.length > 1) {
        toast.success(`Đã lưu ${targets.length} buổi của ${dayLabel} — ${grade}.`);
      }
      onSuccess(firstSavedId || newId);
    })();
  }

  return (
    <>
      {!embedded && (
      <PageHeader
        title={mode === "edit" ? `Chỉnh sửa — ${menu?.name ?? menu?.id}` : "Tạo thực đơn ngày theo khối"}
        description={
          mode === "edit"
            ? `${menu?.id} · Approved/Published bị khóa (BR-067); lưu xong trạng thái về Nháp — nếu đã nộp thì cần nộp lại`
            : "Mỗi ngày = 1 thực đơn (chốt review 11/07); cần lặp lại thì Sao chép từ thực đơn ngày khác (BR-061/BR-069/BR-063)"
        }
        actions={
          <div className="flex gap-2">
            <Link
              href={
                mode === "edit"
                  ? `/service-caterers/menus/${menu?.id}`
                  : grade && menuDate
                    ? `/service-caterers/menus?grade=${encodeURIComponent(grade)}&week=${weekOffsetOfIso(menuDate)}`
                    : "/service-caterers/menus"
              }
            >
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            <Button variant="outline" onClick={() => setAiOpen(true)}>
              <Sparkles /> AI Assist (UC-075)
            </Button>
          </div>
        }
      />
      )}

      <datalist id="dish-library">
        {dishLibrary.map((d) => (
          <option key={d.id} value={d.name}>
            {categoryLabel(d.category)} · {d.kcal} kcal
          </option>
        ))}
      </datalist>

      {lockedByApproval && (
        <Alert variant="warning" className="mb-4" title="Thực đơn đã duyệt — chỉ xem">
          Bữa này đang ở trạng thái <b>{menu?.status}</b>. Thực đơn đã qua duyệt là căn cứ để bếp
          nấu và để xuất kho, nên không sửa trực tiếp được nữa — muốn đổi món thì đề nghị Ban giám
          hiệu trả lại (BR-048) rồi sửa, hoặc tạo thực đơn cho ngày khác.
        </Alert>
      )}
      {mode === "edit" && menu?.status === "REJECTED" && (
        <Alert variant="danger" className="mb-4" title="Lý do từ chối gần nhất (BR-048)">
          3 món chứa allergen chưa có phương án suất riêng cho học sinh bị ảnh
          hưởng — lập món thay thế rồi nộp lại.
        </Alert>
      )}
      {mode === "edit" && !menu?.blocksData?.length && (
        <Alert variant="info" className="mb-4" title="Bản ghi cũ chưa có chi tiết món">
          Thực đơn này được tạo trước khi hệ thống lưu chi tiết buổi ăn/món — nhập
          bổ sung bên dưới rồi lưu lại.
        </Alert>
      )}

      {error && (
        <Alert variant="danger" className="mb-4" title="Chưa hợp lệ">
          {error}
        </Alert>
      )}

      {duplicateMenu && (
        <Alert variant="danger" className="mb-4" title="Ngày này đã có thực đơn">
          {duplicateMenu.grade} đã có thực đơn cho {duplicateMenu.week} (
          {duplicateMenu.id}) — mỗi ngày × khối chỉ có 1 thực đơn.{" "}
          <Link className="font-medium underline" href={`/service-caterers/menus/${duplicateMenu.id}`}>
            Mở thực đơn đó để xem/sửa từng bữa
          </Link>{" "}
          thay vì tạo mới.
        </Alert>
      )}

      {/* ===== Thông tin chung ===== */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>
            {singleMeal
              ? "Thông tin chung của ngày (đã chọn từ lịch)"
              : "Thông tin chung — thực đơn 1 ngày theo khối"}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-3">
            <div className="space-y-1.5">
              <Label>Khối áp dụng *</Label>
              {/* Mở từ một ô lịch thì khối + ngày là chính ô đó — đổi được ở đây
                  sẽ ghi vào ngày khác với ô người dùng vừa bấm. */}
              <Select
                disabled={Boolean(singleMeal)}
                value={grade}
                onChange={(e) => {
                  const g = e.target.value;
                  setGrade(g);
                  // Chưa sửa tay → tự điền số suất theo sĩ số khối vừa chọn
                  if (!servingsTouched) {
                    const n = gradeEnrollmentOf(g, classrooms);
                    setServings(n > 0 ? String(n) : "");
                  }
                }}
              >
                <option value="">— Chọn khối —</option>
                {gradeLevels.map((g) => (
                  <option key={g}>{g}</option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label className="flex items-center gap-1">
                <CalendarDays className="size-3.5" /> Ngày áp dụng * (BR-061: ≥ hôm nay +3)
              </Label>
              <Input
                type="date"
                disabled={Boolean(singleMeal)}
                value={menuDate}
                min={minDateIso}
                onChange={(e) => setMenuDate(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label>Số suất của khối (mặc định cho từng món)</Label>
              <Input
                type="number"
                value={servings}
                placeholder={grade ? String(gradeEnrollmentOf(grade, classrooms)) : "Chọn khối trước"}
                onChange={(e) => {
                  setServings(e.target.value);
                  setServingsTouched(true);
                }}
              />
              {grade && (
                <p className="text-xs text-muted-foreground">
                  Sĩ số {grade}: <b>{gradeEnrollmentOf(grade, classrooms)}</b> HS · số suất mỗi món
                  để trống sẽ dùng số này
                  {servingsTouched && Number(servings) !== gradeEnrollmentOf(grade, classrooms) && (
                    <>
                      {" · "}
                      <button
                        type="button"
                        className="underline"
                        onClick={() => {
                          setServings(String(gradeEnrollmentOf(grade, classrooms)));
                          setServingsTouched(false);
                        }}
                      >
                        dùng sĩ số
                      </button>
                    </>
                  )}
                </p>
              )}
            </div>
          </div>

          {/*
            BR-062 (13/08/2026, Nam) — đối chiếu dinh dưỡng BỮA TRƯA với chuẩn của khối, ngay
            lúc soạn. Màn "Cấu hình trường" của BGH vẫn ghi "Thực đơn lệch quá ±10% so với chuẩn
            của khối sẽ được đánh dấu ở màn duyệt", nhưng trước nay CHỈ suất riêng
            (`special-meal-dialog`) đối chiếu; thực đơn CHÍNH thì không — lời hứa không ai thực
            hiện. CẢNH BÁO MỀM: đánh dấu để biết, KHÔNG chặn nộp/duyệt (chốt với Nam 13/08).

            So theo BỮA TRƯA vì `nutrition_targets` (V54) là chuẩn của bữa trưa, không phải cả ngày.
          */}
          {nutritionTarget && lunchKcal > 0 && Math.abs(lunchKcalGap) > 0.1 && (
            <Alert
              variant="warning"
              title={`Bữa trưa ${lunchKcal} kcal/suất — lệch ${lunchKcalGap > 0 ? "+" : ""}${Math.round(lunchKcalGap * 100)}% so với chuẩn ${nutritionTarget.gradeName} (${nutritionTarget.kcal} kcal, BR-062)`}
            >
              {nutritionTarget.configured
                ? "Chuẩn do Ban giám hiệu cấu hình ở màn Cấu hình trường."
                : "Khối này chưa đặt chuẩn riêng — đang dùng mặc định hệ thống (nhóm tuổi 6–11)."}{" "}
              Đây là cảnh báo, KHÔNG chặn nộp hay duyệt — ngày đặc biệt vẫn lệch chuẩn được.
            </Alert>
          )}

          {/* Chi phí ước tính — hiện ngay lúc soạn, không đợi Ban giám hiệu phát hiện */}
          {grade && Number(servings) > 0 && (
            <Alert
              variant={costEstimate.perServing > ceiling ? "danger" : "info"}
              title={`Chi phí ước tính ${costEstimate.perServing.toLocaleString("vi-VN")}₫/suất — trần ${ceiling.toLocaleString("vi-VN")}₫ (BR-080)`}
            >
              Tính trên lượng THỰC MUA (đã cộng hệ số thải bỏ) × báo giá của nhà cung
              cấp còn hợp tác.
              {costEstimate.missingPrices.length > 0 && (
                <>
                  {" "}
                  Chưa có báo giá: <b>{costEstimate.missingPrices.join(", ")}</b> — con số
                  trên chưa gồm các nguyên liệu này.
                </>
              )}
              {/*
                13/08/2026 (Nam) — câu cũ: "Vượt trần thì Ban giám hiệu không duyệt được."
                SAI SỰ THẬT và doạ người dùng: backend KHÔNG hề chặn theo costCeiling (chỉ lưu
                số), màn duyệt cũng không khoá nút. Trần chi phí là CẢNH BÁO MỀM — BGH vẫn duyệt
                được nếu ngày đó có lý do (tiệc, lễ). Nói đúng bản chất để người lập không phải
                cắt món oan.
              */}
              {costEstimate.perServing > ceiling &&
                ` Vượt trần ${(costEstimate.perServing - ceiling).toLocaleString("vi-VN")}₫/suất —` +
                  " cảnh báo để Ban giám hiệu cân nhắc, KHÔNG chặn nộp hay duyệt."}
            </Alert>
          )}

          <div className="flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-muted-foreground">
              {menuDate ? (
                <>
                  Áp dụng cho <span className="font-medium text-foreground">{dayLabel}</span>
                  {!leadOk && (
                    <span className="ml-1 font-medium text-destructive">
                      · Ngày áp dụng phải cách hôm nay ≥3 ngày (BR-061)
                    </span>
                  )}
                </>
              ) : (
                "Chưa chọn ngày áp dụng."
              )}
            </p>
            {copySources.length > 0 && (
              <div className="flex items-center gap-1.5">
                <Copy className="size-4 text-muted-foreground" />
                <Select
                  className="h-8 w-72"
                  value=""
                  onChange={(e) => e.target.value && copyFromMenu(e.target.value)}
                  aria-label="Sao chép món từ thực đơn khác"
                >
                  <option value="">Sao chép món từ thực đơn khác…</option>
                  {copySources.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.week} · {m.grade} · {m.id}
                    </option>
                  ))}
                </Select>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Bảng tóm tắt hồ sơ dị ứng của khối đã bỏ 23/07/2026 (Minh): nó liệt kê
          mọi tác nhân của khối kể cả khi thực đơn không đụng tới cái nào, trong khi
          mỗi món chạm dị ứng đã có cảnh báo riêng ngay tại món kèm danh sách học
          sinh — cụ thể hơn và đúng lúc hơn. */}
      {/* ===== Suất riêng theo chế độ ăn — làm NGAY TẠI ĐÂY, không để sau khi công bố ===== */}
      {grade && dietGroupsNeededByGrade(grade, dietGroups, students).length > 0 && (
        <Card className="mb-4">
          <CardHeader>
            <CardTitle className="flex flex-wrap items-center gap-2">
              <Soup className="size-4 text-muted-foreground" />
              Suất riêng theo chế độ ăn — {grade}
              <Badge variant={dietGaps.length === 0 ? "success" : "warning"}>
                {dietGaps.length === 0 ? "Đã phủ đủ" : `Còn ${dietGaps.length} chỗ hở`}
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <p className="text-xs text-muted-foreground">
              Chế độ ăn KHÔNG suy được từ thành phần món như dị ứng — phải khai. Nhóm nào
              ăn được luôn bữa gốc thì không cần suất riêng; chỉ khai khi bữa gốc có món
              nhóm đó không dùng được (BR-065/BR-125).
            </p>

            {/* Chưa có món gốc thì chưa biết nhóm nào vướng gì — nút bị khoá, lý do
                nằm ở tooltip của chính nút đó thay vì một khối Alert chiếm chỗ. */}
            {blocks.map((block) => (
              <div key={block.mealType} className="rounded-lg border p-3">
                <p className="mb-2 text-sm font-medium">{MEAL_TYPE_LABEL[block.mealType]}</p>
                <div className="space-y-1.5">
                  {dietGroupsNeededByGrade(grade, dietGroups, students).map((group) => {
                    const gap = dietGaps.find(
                      (g) => g.mealType === block.mealType && g.group.id === group.id
                    );
                    const variant = variants.find(
                      (v) => v.mealType === block.mealType && v.dietGroupId === group.id
                    );
                    const count = dietGroupMembersInGrade(group.id, grade, dietGroups, students).length;
                    return (
                      <div
                        key={group.id}
                        className="flex flex-wrap items-center gap-2 rounded-md border px-2.5 py-2 text-sm"
                      >
                        <span className="font-medium">{group.label}</span>
                        <span className="text-xs text-muted-foreground">{count} HS</span>
                        {variant ? (
                          <>
                            <Badge variant="success">Có suất riêng</Badge>
                            <span className="min-w-0 flex-1 truncate text-xs text-muted-foreground">
                              {variant.dishes
                                .map((d) => variantDishName(d, dishLibrary))
                                .join(" · ")}
                            </span>
                            {draftVariants.some((d) => d.id === variant.id) ? (
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() =>
                                  setDraftVariants((prev) => prev.filter((d) => d.id !== variant.id))
                                }
                              >
                                Bỏ
                              </Button>
                            ) : (
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => removeSavedMeal(variant)}
                              >
                                Gỡ
                              </Button>
                            )}
                          </>
                        ) : gap ? (
                          <>
                            <Badge variant="danger">Chưa có gì ăn</Badge>
                            <span className="min-w-0 flex-1 text-xs text-muted-foreground">
                              vướng {gap.blockedBy.map((b) => `${b.dish} (${b.reasons.join(", ")})`).join(" · ")}
                            </span>
                            <Button
                              size="sm"
                              disabled={!baseDishesNamed}
                              title={
                                baseDishesNamed
                                  ? undefined
                                  : "Đặt tên món của bữa gốc trước — suất riêng dựng từ bữa gốc: giữ món nhóm ăn được rồi thay phần còn lại"
                              }
                              onClick={() => setVariantTarget({ mealType: block.mealType, groupId: group.id })}
                            >
                              <Plus /> Tạo suất riêng
                            </Button>
                          </>
                        ) : (
                          <>
                            <Badge variant="neutral">Ăn chung được</Badge>
                            <span className="min-w-0 flex-1 text-xs text-muted-foreground">
                              Bữa gốc không có món nào nhóm này phải kiêng
                            </span>
                            <Button
                              variant="outline"
                              size="sm"
                              disabled={!baseDishesNamed}
                              title={
                                baseDishesNamed ? undefined : "Đặt tên món của bữa gốc trước"
                              }
                              onClick={() => setVariantTarget({ mealType: block.mealType, groupId: group.id })}
                            >
                              Vẫn tạo suất riêng
                            </Button>
                          </>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {/* ===== Các buổi ăn trong ngày ===== */}
      {blocks.map((block, bi) => (
        <Card
          key={bi}
          id={`meal-${block.mealType}`}
          className={
            focusMeal === block.mealType
              ? "mb-6 ring-2 ring-primary ring-offset-2"
              : "mb-6"
          }
        >
          <CardHeader className="flex-row items-center justify-between rounded-t-xl bg-muted/50">
            <CardTitle className="flex items-center gap-2">
              <UtensilsCrossed className="size-4" />
              {mealLabel(block.mealType)}
              {menuDate && <span className="text-sm font-normal text-muted-foreground">· {dayLabel}</span>}
            </CardTitle>
            {/* Chế độ 1 buổi: buổi đã do ô lịch quyết định — không cho đổi loại bữa
                (sẽ ghi sang ô khác) cũng không cho xoá (không còn gì để soạn). */}
            {!singleMeal && (
              <div className="flex items-center gap-2">
                <Select
                  className="h-8 w-36"
                  value={block.mealType}
                  onChange={(e) => changeBlockMealType(bi, e.target.value as MealType)}
                  aria-label="Loại bữa"
                >
                  {MEAL_TYPES.map((m) => (
                    <option
                      key={m.value}
                      value={m.value}
                      disabled={m.value !== block.mealType && blocks.some((b) => b.mealType === m.value)}
                    >
                      {m.label}
                    </option>
                  ))}
                </Select>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  disabled={blocks.length === 1}
                  onClick={() => setBlocks((prev) => prev.filter((_, i) => i !== bi))}
                  aria-label="Xóa buổi ăn"
                >
                  <Trash2 />
                </Button>
              </div>
            )}
          </CardHeader>
          <CardContent className="space-y-5 pt-5">
            {block.dishes.map((dish, di) => {
              const st = dishState(dish, grade, students);
              /*
               * BR-065 (13/08/2026, Nam) — món thay thế phải CÙNG LOẠI món gốc. Trước đây ô
               * chọn liệt kê toàn bộ thư viện nên thay món mặn bằng một bát canh vẫn lọt: em
               * dị ứng nhận đủ số suất nhưng bữa mất hẳn món mặn.
               *
               * `DishForm` không mang `category` (chỉ có `dishId` khi chọn từ thư viện, hoặc
               * tên gõ tay) nên tra ngược qua thư viện. Không tra được loại thì KHÔNG lọc —
               * thà cho chọn rộng còn hơn chặn oan món gõ tay.
               */
              const baseLibDish = dishLibrary.find(
                (d) => (dish.dishId && d.id === dish.dishId) || d.name === dish.name
              );
              const baseCategory = baseLibDish?.category;
              const substituteOptions = baseCategory
                ? dishLibrary.filter((d) => d.category === baseCategory)
                : dishLibrary;
              return (
                <div key={di} className="rounded-lg border p-4">
                  <div className="mb-3 grid gap-2 sm:grid-cols-[1fr_36px]">
                    <div className="space-y-1">
                      <Input
                        list="dish-library"
                        placeholder={`Tên món #${di + 1} — chọn từ thư viện hoặc gõ món mới`}
                        value={dish.name}
                        onChange={(e) => changeDishName(bi, di, e.target.value, dish)}
                      />
                      {dish.name.trim() &&
                        (dish.dishId ? (
                          <p className="text-xs text-success">
                            Lấy từ thư viện món — thành phần, kcal và tác nhân dị ứng
                            đã đổ sẵn, sửa lại được cho ngày này.
                          </p>
                        ) : (
                          <p className="flex flex-wrap items-center gap-1.5 text-xs text-muted-foreground">
                            Món mới (ngoài{" "}
                            <Link href="/service-caterers/dishes" className="underline">
                              thư viện
                            </Link>
                            ) — khai đủ thành phần bên dưới.
                            <Button
                              type="button"
                              variant="outline"
                              size="sm"
                              disabled={
                                createDish.isPending ||
                                !dish.kcal.trim() ||
                                !dish.ingredients.some(
                                  (r) => r.name.trim() && Number(r.quantity) > 0
                                )
                              }
                              onClick={() => saveDishToLibrary(bi, di, dish)}
                            >
                              <Plus /> Lưu vào thư viện
                            </Button>
                          </p>
                        ))}
                    </div>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      disabled={block.dishes.length === 1}
                      onClick={() =>
                        updateBlock(bi, { dishes: block.dishes.filter((_, j) => j !== di) })
                      }
                      aria-label="Xóa món"
                    >
                      <Trash2 />
                    </Button>
                  </div>

                  <div className="mb-3">
                    <Label className="mb-1.5 block text-xs text-muted-foreground">
                      Thành phần + định lượng / suất * (BR-069 — khai báo toàn bộ)
                    </Label>
                    {catalogIsFallback && (
                      <p className="mb-1 text-xs text-warning">
                        Trường chưa duyệt danh mục hợp đồng NCC nào — đang cho chọn theo danh mục
                        chung ({allIngredients.length} mục), có thể chọn phải nguyên liệu chưa NCC
                        nào cung ứng cho trường này.
                      </p>
                    )}
                    <IngredientEditor
                      rows={dish.ingredients}
                      onChange={(rows) => updateDish(bi, di, { ingredients: rows })}

                      servings={Number(servings) || 0}
                      catalogOverride={schoolApprovedIngredientCatalog}
                    />
                  </div>

                  <div className="mb-3">
                    <AllergenPicker
                      auto={st.auto}
                      manual={dish.manualAllergens}
                      onChange={(manual) => updateDish(bi, di, { manualAllergens: manual })}
                      gradeProfile={gradeProfile}
                    />
                  </div>

                  <div className="mb-3 grid items-end gap-2 sm:grid-cols-[110px_repeat(4,110px)_1fr]">
                    {/* Số suất của RIÊNG món này — mặc định lấy "Số suất của khối"
                        ở đầu form (placeholder), sửa lại được khi món phục vụ số
                        lượng khác các món cùng bữa. Trống ⇒ dùng mặc định khối. */}
                    <div className="space-y-1">
                      <Label className="text-xs text-muted-foreground">Số suất *</Label>
                      <Input
                        type="number"
                        min={0}
                        value={dish.servings ?? ""}
                        placeholder={
                          servings ||
                          (grade ? String(gradeEnrollmentOf(grade, classrooms)) : "Số suất")
                        }
                        onChange={(e) => updateDish(bi, di, { servings: e.target.value })}
                        aria-label="Số suất của món"
                      />
                    </div>
                    {(
                      [
                        ["kcal", "Kcal * (BR-062)"],
                        ["protein", "Đạm (g)"],
                        ["carb", "Bột đường (g)"],
                        ["fat", "Béo (g)"],
                      ] as const
                    ).map(([field, label]) => (
                      <div key={field} className="space-y-1">
                        <Label className="text-xs text-muted-foreground">{label}</Label>
                        <Input
                          type="number"
                          min={0}
                          value={dish[field]}
                          onChange={(e) => updateDish(bi, di, { [field]: e.target.value })}
                        />
                      </div>
                    ))}
                    {/* Món phụ THAY cho món này — chưa đặt tên món gốc thì chưa có
                        gì để thay, và suất phụ sẽ treo vào một món trắng. */}
                    <label
                      className={cn(
                        "flex h-9 items-center gap-2 text-sm",
                        dish.name.trim() ? "cursor-pointer" : "cursor-not-allowed text-muted-foreground"
                      )}
                      title={dish.name.trim() ? undefined : "Đặt tên món này trước"}
                    >
                      <input
                        type="checkbox"
                        className="size-4 accent-primary"
                        disabled={!dish.name.trim()}
                        checked={dish.isSpecialDish}
                        onChange={(e) => {
                          const checked = e.target.checked;
                          if (checked) {
                            setAllergySub(bi, di, dish, {
                              portions:
                                allergySubOf(dish)?.portions ??
                                (st.affected.length > 0 ? String(st.affected.length) : ""),
                            });
                          } else if (st.conflicts.length === 0) {
                            // Còn xung đột dị ứng thì giữ lại suất để không mất dữ liệu
                            clearAllergySub(bi, di, dish);
                          }
                        }}
                      />
                      Món có suất ăn đặc biệt (mở khối món phụ + số suất bên dưới)
                    </label>
                  </div>

                  {/* Cảnh báo dị ứng CHỈ hiện khi đã chọn khối + thành phần đã chọn xong */}
                  {st.conflicts.length > 0 && (
                    <Alert
                      variant={st.conflictResolved ? "warning" : "danger"}
                      title={`${st.affected.length} học sinh ${grade} dị ứng với món này (BR-063 — MSG-051)`}
                    >
                      <ul className="mb-2 list-inside list-disc space-y-0.5">
                        {st.conflicts.map((c) => (
                          <li key={c.allergen}>
                            <b>{c.allergen}</b>: {c.students} học sinh — lớp{" "}
                            {c.classes.join(", ")}
                          </li>
                        ))}
                      </ul>
                      <div className="flex flex-wrap gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() =>
                            setDetail({
                              dishName: dish.name || `Món #${di + 1}`,
                              rows: st.affected,
                            })
                          }
                        >
                          <Users /> Chi tiết {st.affected.length} học sinh
                        </Button>
                        {!allergySubOf(dish) && (
                          <Button
                            size="sm"
                            onClick={() =>
                              setAllergySub(bi, di, dish, {
                                portions: String(st.affected.length),
                              })
                            }
                          >
                            <Replace /> Lập món phụ thay thế ({st.affected.length} suất)
                          </Button>
                        )}
                      </div>
                      <p className="mt-1.5 text-xs">
                        {st.conflictResolved
                          ? "Đã có món phụ thay thế hợp lệ — đủ điều kiện nộp."
                          : "Phải lập món phụ thay thế (đủ thông tin + số suất) trước khi nộp phê duyệt."}
                      </p>
                    </Alert>
                  )}

                  {/* Khối món phụ + SỐ SUẤT đặc biệt + tổng định lượng */}
                  {st.allergySub && (
                    <div className="mt-3 rounded-lg border border-warning/60 bg-warning/5 p-4">
                      <div className="mb-2 flex items-center justify-between">
                        <p className="text-sm font-semibold">
                          Suất ăn đặc biệt — món phụ thay thế
                          {st.affectedAllAllergens.length > 0 &&
                            ` (tránh TOÀN BỘ dị ứng của các em: ${st.affectedAllAllergens.join(", ")})`}
                        </p>
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          disabled={st.conflicts.length > 0}
                          onClick={() => clearAllergySub(bi, di, dish)}
                        >
                          <Trash2 /> Bỏ món phụ
                        </Button>
                      </div>

                      {/* Chi phí món gốc và các suất thay thế. Suất riêng đắt hơn là
                          chuyện bình thường và thường không đáng kể vì chỉ vài suất —
                          nên hiện chênh lệch TRÊN SỐ SUẤT THẬT, không nhân sĩ số khối,
                          để con số phản ánh đúng mức ảnh hưởng. */}
                      {/*
                        BR-065 (13/08/2026, Nam) — LOẠI món của món gốc, để lọc danh sách món
                        thay thế bên dưới. Tra qua thư viện: `DishForm` không mang `category`,
                        chỉ có `dishId` (món chọn từ thư viện) hoặc tên gõ tay.
                      */}
                      {(() => {
                        const own = dishCostPerServing(dish, allIngredients, costSupplierProducts, costPartners);
                        const subs = subsOf(dish);
                        if (own.missingPrices.length > 0 && subs.length === 0) return null;
                        return (
                          <div className="mb-3 rounded-lg border bg-muted/30 p-2.5 text-xs">
                            <p>
                              Chi phí nguyên liệu:{" "}
                              <b>{own.cost.toLocaleString("vi-VN")}₫/suất</b>
                              {own.missingPrices.length > 0 && (
                                <span className="text-warning">
                                  {" "}
                                  (chưa gồm {own.missingPrices.join(", ")} — thiếu báo giá)
                                </span>
                              )}
                            </p>
                            {subs.map((sub, si) => {
                              const r = substituteCostDelta(dish, sub, allIngredients, costSupplierProducts, costPartners);
                              if (!r.comparable) {
                                return (
                                  <p key={si} className="mt-1 text-muted-foreground">
                                    {sub.name || "suất thay thế"}: chưa đủ báo giá để so chi phí.
                                  </p>
                                );
                              }
                              return (
                                <p key={si} className={r.delta > 0 ? "mt-1 text-warning" : "mt-1 text-muted-foreground"}>
                                  {sub.name || "suất thay thế"}:{" "}
                                  <b>{r.substituteCost.toLocaleString("vi-VN")}₫/suất</b>{" "}
                                  ({r.delta > 0 ? "+" : ""}
                                  {r.delta.toLocaleString("vi-VN")}₫ so với món gốc
                                  {r.portions > 0 && (
                                    <>
                                      {" "}· {r.portions} suất ⇒ {r.extraTotal > 0 ? "+" : ""}
                                      {r.extraTotal.toLocaleString("vi-VN")}₫
                                    </>
                                  )}
                                  )
                                </p>
                              );
                            })}
                          </div>
                        );
                      })()}

                      {/* Chế độ ăn không còn nằm trong thực đơn chung: mỗi nhóm có
                          THỰC ĐƠN RIÊNG (MenuVariant), xem màn Bữa ăn đặc biệt. */}

                      <div className="mb-3 grid gap-2 sm:grid-cols-[1fr_190px_100px_90px]">
                        <div className="space-y-1">
                          <Label className="text-xs text-muted-foreground">Tên món phụ *</Label>
                          {/* SearchableSelect (13/08/2026) thay `<Input list="dish-library">`:
                              gõ tay lệch một ký tự là suất thay thế mất `dishId` — không cộng
                              được dinh dưỡng, không dẫn xuất allergen theo cùng đường món chính
                              (xem `SubstituteDish.dishId`), và người dùng phải khai lại tay cả
                              BOM lẫn kcal. Chọn từ thư viện thì đổ sẵn tất cả. */}
                          <SearchableSelect
                            value={st.allergySub.name}
                            onChange={(name) => {
                              const lib = dishLibrary.find((d) => d.name === name);
                              setAllergySub(
                                bi,
                                di,
                                dish,
                                lib
                                  ? {
                                      ...substituteFromLibraryDish(lib, st.allergySub?.portions ?? ""),
                                      // Giữ nguyên phần người dùng đã khai riêng cho ngày này.
                                      allergenIds: st.allergySub?.allergenIds,
                                      note: st.allergySub?.note ?? "",
                                    }
                                  : { name, dishId: undefined }
                              );
                            }}
                            options={substituteOptions.map((d) => ({
                              value: d.name,
                              label: `${d.name} · ${categoryLabel(d.category)} · ${d.kcal} kcal`,
                            }))}
                            placeholder="— Chọn món thay thế từ thư viện —"
                            searchPlaceholder="Tìm món trong thư viện…"
                          />
                          {/*
                            13/08/2026 (Nam) — chỉ cho thay bằng món CÙNG LOẠI. Trước đây danh
                            sách là toàn bộ thư viện, nên thay món mặn bằng một bát canh vẫn lọt:
                            em dị ứng nhận đủ số suất nhưng bữa mất hẳn món mặn.
                            Không biết loại món gốc (món gõ tay, không có trong thư viện) thì
                            KHÔNG lọc — thà cho chọn rộng còn hơn chặn oan.
                          */}
                          {baseCategory && (
                            <p className="text-xs text-muted-foreground">
                              Chỉ hiện món cùng loại <b>{categoryLabel(baseCategory)}</b> với món gốc
                              ({substituteOptions.length} món).
                            </p>
                          )}
                          {!baseCategory && (
                            <p className="text-xs text-muted-foreground">
                              Món gốc không có trong thư viện nên chưa biết loại — danh sách hiện
                              toàn bộ, tự kiểm tra thay đúng loại món.
                            </p>
                          )}
                        </div>
                        <div className="space-y-1">
                          <Label className="text-xs text-muted-foreground">Thuộc nhóm</Label>
                          {/* Nhóm ở ô này LUÔN là "Dị ứng": suất thay thế cấp MÓN sinh ra
                              do món gốc chạm dị ứng của HS, không phải lựa chọn — nên đây
                              là NHÃN cố định, không phải dropdown khoá (22/07/2026 — Minh).
                              Suất theo chế độ ăn (Chay/bệnh lý) là thứ khác hẳn: trọn bộ
                              cả buổi, lưu ở `MenuVariant`, khai ở Card "Suất riêng theo
                              chế độ ăn" phía trên. Hai bên khác cấu trúc dữ liệu nên không
                              đè nhau. */}
                          <div className="flex h-9 items-center">
                            <Badge variant="warning">Dị ứng</Badge>
                          </div>
                        </div>
                        <div className="space-y-1">
                          <Label className="text-xs text-muted-foreground">Số suất *</Label>
                          <Input
                            type="number"
                            min={1}
                            value={st.allergySub.portions}
                            onChange={(e) => setAllergySub(bi, di, dish, { portions: e.target.value })}
                            placeholder={st.affected.length > 0 ? String(st.affected.length) : "VD: 2"}
                          />
                        </div>
                        <div className="space-y-1">
                          <Label className="text-xs text-muted-foreground">Kcal *</Label>
                          <Input
                            type="number"
                            min={0}
                            value={st.allergySub.kcal}
                            onChange={(e) => setAllergySub(bi, di, dish, { kcal: e.target.value })}
                          />
                        </div>
                      </div>

                      <p className="mb-3 text-xs text-muted-foreground">
                        Suất này phục vụ nhóm <b>Dị ứng</b> (cố định theo tác nhân của món),
                        chỉ thay <b>một món</b>. Suất theo chế độ ăn — Chay, bệnh lý… — là
                        trọn bộ cả buổi, khai ở phần <b>Suất riêng theo chế độ ăn</b> phía
                        trên; nếu thực đơn đã công bố rồi thì khai bổ sung ở màn{" "}
                        <Link href="/service-caterers/menus" className="font-medium underline">
                          Lịch thực đơn tuần
                        </Link>
                        .
                      </p>

                      {st.affected.length > 0 && (
                        <p className="mb-3 text-xs text-muted-foreground">
                          Số suất gợi ý = {st.affected.length} học sinh bị ảnh hưởng (
                          {st.affected.map((r) => `${r.student.name} — ${r.student.class}`).join("; ")}
                          ) —{" "}
                          <button
                            type="button"
                            className="font-medium text-primary underline"
                            onClick={() =>
                              setDetail({ dishName: st.allergySub?.name || "Món phụ", rows: st.affected })
                            }
                          >
                            xem chi tiết
                          </button>
                        </p>
                      )}

                      <div className="mb-3">
                        <Label className="mb-1.5 block text-xs text-muted-foreground">
                          Thành phần + định lượng / 1 suất của món phụ * (BR-069)
                        </Label>
                        <IngredientEditor
                          rows={st.allergySub.ingredients}
                          servings={Number(st.allergySub.portions) || 0}
                          onChange={(rows) =>
                            setAllergySub(bi, di, dish, { ingredients: rows })
                          }

                          catalogOverride={schoolApprovedIngredientCatalog}
                        />
                      </div>

                      {Number(st.allergySub.portions) > 0 &&
                        st.allergySub.ingredients.some((r) => r.name.trim() && r.quantity) && (
                          <p className="mb-3 rounded-md bg-muted/60 px-3 py-2 text-xs">
                            <b>Tổng nguyên liệu cho {st.allergySub.portions} suất đặc biệt:</b>{" "}
                            {st.allergySub.ingredients
                              .filter((r) => r.name.trim() && r.quantity)
                              .map(
                                (r) =>
                                  `${r.name} ${Number(r.quantity) * Number(st.allergySub!.portions)}${r.unit}`
                              )
                              .join(" · ")}
                          </p>
                        )}

                      <AllergenPicker
                        auto={dishAllergens(st.allergySub.ingredients, [])}
                        manual={st.allergySub.manualAllergens}
                        onChange={(manual) =>
                          setAllergySub(bi, di, dish, { manualAllergens: manual })
                        }
                        gradeProfile={gradeProfile}
                      />
                      <div className="mt-3 space-y-1">
                        <Label className="text-xs text-muted-foreground">
                          Ghi chú chế biến riêng (BR-082 — dụng cụ riêng, tránh nhiễm chéo)
                        </Label>
                        <Textarea
                          className="min-h-[40px]"
                          value={st.allergySub.note}
                          onChange={(e) =>
                            setAllergySub(bi, di, dish, { note: e.target.value })
                          }
                          placeholder="VD: nấu nồi riêng khu B, không dùng chung muôi với món gốc…"
                        />
                      </div>
                      {st.subStillConflicts.length > 0 && (
                        <Alert
                          variant="danger"
                          className="mt-3"
                          title="Món phụ vẫn không an toàn cho chính các em bị ảnh hưởng"
                        >
                          Món phụ chứa <b>{st.subStillConflicts.join(", ")}</b> — trùng với hồ sơ
                          dị ứng của học sinh nhận suất này (kiểm tra TOÀN BỘ dị ứng của em đó,
                          không chỉ tác nhân của món gốc — VD em vừa dị ứng tôm vừa dị ứng cá).
                          Phải chọn nguyên liệu khác (zero overlap, BR-063).
                        </Alert>
                      )}
                      {dish.isSpecialDish && !st.subComplete && (
                        <p className="mt-2 text-xs font-medium text-destructive">
                          Món phụ chưa đủ thông tin: cần tên, số suất ≥1, kcal và ít nhất 1
                          thành phần có định lượng.
                        </p>
                      )}
                    </div>
                  )}

                  {/* Cân đối suất: học sinh ăn suất thay thế PHẢI được trừ khỏi suất
                      thường (không nấu 2 phần cho cùng một em). Vì số suất nhập tay
                      nên không tự trừ — chỉ cảnh báo khi suất thường + thay thế ≠ số
                      suất của khối (22/07/2026 — Minh). */}
                  {(() => {
                    const gradeTotal = Number(servings) || 0;
                    const sub = totalSubPortions(dish);
                    if (gradeTotal <= 0 || sub <= 0) return null;
                    // Số suất trống ⇒ món đang dùng mặc định = số suất của khối.
                    const regular = Number(dish.servings) || gradeTotal;
                    const sum = regular + sub;
                    if (sum === gradeTotal) return null;
                    const suggested = gradeTotal - sub;
                    return (
                      <Alert
                        variant="warning"
                        className="mt-3"
                        title="Suất thường + thay thế chưa khớp số suất của khối"
                      >
                        Món này: <b>{regular}</b> suất thường + <b>{sub}</b> suất thay thế ={" "}
                        <b>{sum}</b> — nhưng số suất của khối là <b>{gradeTotal}</b>. Học sinh ăn
                        suất thay thế đã được trừ khỏi suất thường, nên tổng phải bằng số suất khối.
                        {suggested >= 0 ? (
                          <>
                            {" "}
                            Chỉnh “Số suất” của món thành <b>{suggested}</b> (= {gradeTotal} − {sub})
                            để khớp.
                          </>
                        ) : (
                          <>
                            {" "}
                            Số suất thay thế ({sub}) đang vượt cả số suất của khối ({gradeTotal}) —
                            kiểm tra lại.
                          </>
                        )}
                      </Alert>
                    );
                  })()}
                </div>
              );
            })}

            <Button
              type="button"
              size="sm"
              className="border border-primary/40 bg-primary/10 text-primary shadow-none hover:bg-primary/20"
              onClick={() => updateBlock(bi, { dishes: [...block.dishes, emptyDish()] })}
            >
              <Plus /> Thêm món vào {mealLabel(block.mealType).toLowerCase()}
            </Button>
          </CardContent>
        </Card>
      ))}

      {!singleMeal && (
        <div className="mb-6">
          <Button
            type="button"
            disabled={unusedMealTypes.length === 0}
            onClick={() =>
              setBlocks((prev) => [
                ...prev,
                { mealType: unusedMealTypes[0].value, dishes: [emptyDish()] },
              ])
            }
          >
            <Plus />
            {unusedMealTypes.length > 0
              ? `Thêm buổi ăn (${unusedMealTypes.map((m) => m.label).join(" / ")})`
              : "Đã đủ 3 buổi ăn trong ngày"}
          </Button>
        </div>
      )}

      {totalKcal > 0 && (
        <p className="mb-6 text-xs text-muted-foreground">
          {singleMeal ? `Năng lượng ${mealLabel(singleMeal).toLowerCase()}: ` : "Tổng năng lượng cả ngày: "}
          <b>{totalKcal} kcal</b>
          {specialPortions > 0 && (
            <>
              {" "}· Suất ăn đặc biệt (dị ứng): <b>{specialPortions} suất</b>
            </>
          )}{" "}
          — mục tiêu 650–750 kcal cho bữa trưa tiểu học (BR-062)
        </p>
      )}

      {singleMeal ? (
        /*
         * Soạn 1 buổi thì KHÔNG có nút nộp duyệt: điều kiện nộp (BR-063/BR-065,
         * đủ 3 bữa…) xét trên cả ngày, mà ở đây mới thấy một bữa — nộp từ đây sẽ
         * là "đạt" trên dữ liệu chưa nhìn thấy. Nộp duyệt nằm ở cột ngày trên lịch.
         */
        <div className="flex flex-wrap items-center gap-3">
          <Button type="button" disabled={isPending || lockedByApproval} onClick={saveDraft}>
            <Save /> Lưu {mealLabel(singleMeal).toLowerCase()}
          </Button>
          <span className="text-xs text-muted-foreground">
            Nộp duyệt là thao tác của cả ngày — lưu xong bấm nút nộp ở cột ngày trên lịch.
          </span>
        </div>
      ) : (
        <div className="flex gap-2">
          <Button type="button" variant="outline" disabled={isPending || lockedByApproval} onClick={saveDraft}>
            <Save /> {mode === "edit" ? "Lưu thay đổi (MSG-074)" : "Lưu nháp (MSG-071)"}
          </Button>
          <Button type="button" disabled={isPending || lockedByApproval} onClick={() => setSubmitOpen(true)}>
            <Send /> Nộp phê duyệt…
          </Button>
        </div>
      )}

      {/* ===== Popup chi tiết học sinh dị ứng ===== */}
      <Dialog open={detail != null} onOpenChange={(v) => !v && setDetail(null)}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Users className="size-5" /> Học sinh dị ứng — {detail?.dishName}
            </DialogTitle>
            <DialogDescription>
              Hồ sơ dị ứng đã Y tế xác minh của {grade || "khối"} (BR-031/BR-063) — các em
              dưới đây cần suất ăn đặc biệt nếu món này được phục vụ.
            </DialogDescription>
          </DialogHeader>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Học sinh</TableHead>
                <TableHead>Lớp</TableHead>
                <TableHead>Tác nhân trùng món</TableHead>
                <TableHead>Toàn bộ dị ứng của em</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {(detail?.rows ?? []).map(({ student, matched }) => (
                <TableRow key={student.id}>
                  <TableCell className="font-medium">{student.name}</TableCell>
                  <TableCell>{student.class}</TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {matched.map((a) => (
                        <Badge key={a} variant="danger">{a}</Badge>
                      ))}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {student.allergies.map((a) => (
                        <Badge key={a} variant={matched.includes(a) ? "danger" : "warning"}>
                          {a}
                        </Badge>
                      ))}
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <p className="text-xs text-muted-foreground">
            Món phụ thay thế phải tránh <b>toàn bộ</b> tác nhân ở cột cuối — không chỉ
            tác nhân trùng món gốc (chốt review 11/07).
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDetail(null)}>Đóng</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* ===== Dialog AI Assist (UC-075, BR-064/156/157) ===== */}
      <Dialog open={aiOpen} onOpenChange={setAiOpen}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Sparkles className="size-5" /> AI đề xuất món ăn (UC-075)
            </DialogTitle>
            <DialogDescription>
              Ngữ cảnh gửi AI: tồn kho hiện tại, mục tiêu dinh dưỡng {grade || "khối chưa chọn"},
              hồ sơ dị ứng của khối (BR-062/BR-064). AI đề xuất theo ngữ cảnh gửi kèm — vẫn
              phải tự kiểm tra lại dị ứng/dinh dưỡng trước khi nộp duyệt (ADR-003, BR-156).
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3">
            {/* Lô sắp hết hạn — phần duy nhất màn "AI đề xuất" đứng riêng có mà đây
                chưa có. Đưa vào đúng chỗ người dùng đang lập thực đơn thì mới dùng
                được: biết lô nào sắp hết hạn để ưu tiên đưa món dùng nó vào bữa. */}
            {(() => {
              const soon = liveBatches.filter(
                (b) => b.quarantine == null && b.status === "EXPIRING_SOON"
              );
              if (soon.length === 0) return null;
              return (
                <Alert variant="warning" title={`${soon.length} lô sắp hết hạn — ưu tiên dùng trước`}>
                  {soon
                    .map((b) => `${b.ingredient} (${b.remaining ?? b.quantity} ${b.unit}, HSD ${formatVnDate(b.expiryDate)})`)
                    .join(" · ")}
                </Alert>
              );
            })()}

            {/* Nói rõ AI đang bị ràng buộc theo danh mục nào — người dùng phải biết phạm vi gợi ý. */}
            {catalogIsFallback ? (
              <Alert variant="warning" title="Chưa có danh mục hợp đồng nào được duyệt">
                Gợi ý AI đang lấy theo danh mục nguyên liệu chung ({allIngredients.length} mục). Món
                đề xuất có thể KHÔNG mua được vì chưa nằm trong hợp đồng nào (chặn ở ING-077 lúc
                tạo đơn mua) — duyệt danh mục hợp đồng trước để gợi ý sát thực tế.
              </Alert>
            ) : (
              <p className="text-xs text-muted-foreground">
                AI chỉ được dùng {approvedCatalog.length} mặt hàng trong danh mục hợp đồng đã duyệt
                của trường — nguyên liệu ngoài danh mục sẽ bị loại tự động.
              </p>
            )}

            {aiLoading && (
              <div className="flex items-center gap-2 rounded-lg border border-dashed p-4 text-sm text-muted-foreground">
                <Sparkles className="size-4 animate-spin" /> Đang hỏi AI theo ngữ cảnh khối {grade || "—"}…
              </div>
            )}

            {!aiLoading && aiFetchError && (
              <Alert variant="danger" title="Không lấy được gợi ý từ AI (MSG-073)">
                {aiFetchError}
                {aiRawText && (
                  <p className="mt-2 whitespace-pre-wrap rounded-md bg-muted/50 p-2 text-xs text-foreground">
                    {aiRawText}
                  </p>
                )}
                <div className="mt-2">
                  <Button size="sm" variant="outline" onClick={fetchAiSuggestions}>Thử lại</Button>
                </div>
              </Alert>
            )}

            {!aiLoading && aiSuggestions?.length === 0 && (
              <p className="text-sm text-muted-foreground">AI không đề xuất được món nào — thử lại hoặc tạo thủ công.</p>
            )}

            {!aiLoading && aiSuggestions?.map((s, idx) => (
              <div key={`${s.name}-${idx}`} className="rounded-lg border p-3">
                <div className="mb-1 flex items-center justify-between">
                  <p className="text-sm font-semibold">{s.name}</p>
                  <div className="flex gap-1.5">
                    <Badge variant="neutral">{mealLabel(s.mealType)}</Badge>
                    {s.kcal && <Badge variant="info">{s.kcal} kcal</Badge>}
                  </div>
                </div>
                <p className="mb-1 text-xs text-muted-foreground">
                  {s.ingredients.length > 0
                    ? s.ingredients.map((ing) => `${ing.name} ${ing.quantity}${ing.unit}`).join(" · ")
                    : "Không còn nguyên liệu nào hợp lệ."}
                </p>
                {(s.droppedIngredients ?? 0) > 0 && (
                  <p className="mb-1 text-xs text-warning">
                    Đã loại {s.droppedIngredients} nguyên liệu AI đề xuất nhưng KHÔNG có trong danh
                    mục của trường — bổ sung tay nếu cần, hoặc thêm nguyên liệu vào danh mục trước.
                  </p>
                )}
                {s.rationale && <p className="mb-2 text-xs text-muted-foreground">{s.rationale}</p>}
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    const dish: DishForm = {
                      ...emptyDish(),
                      name: s.name,
                      /*
                       * Gắn `ingredientId` theo tên ngay lúc chèn (09/08/2026). Thiếu khoá này
                       * thì món không truy xuất được lô (BR-076) và ước tính chi phí không tra
                       * được giá — form sẽ hiện "Ngoài danh mục". Tên đã được kiểm với danh mục
                       * ở bước trên nên tra chắc chắn khớp.
                       */
                      ingredients: s.ingredients.map((x) => ({
                        ...x,
                        ingredientId:
                          x.ingredientId ??
                          aiIngredientCatalog.find(
                            (c) =>
                              normalizeIngredientName(c.name) === normalizeIngredientName(x.name)
                          )?.id,
                      })),
                      kcal: s.kcal,
                      protein: s.protein,
                      carb: s.carb,
                      fat: s.fat,
                    };
                    setBlocks((prev) => {
                      const bi = prev.findIndex((b) => b.mealType === s.mealType);
                      if (bi >= 0) {
                        return prev.map((b, i) =>
                          i === bi ? { ...b, dishes: [...b.dishes, dish] } : b
                        );
                      }
                      return [...prev, { mealType: s.mealType, dishes: [dish] }];
                    });
                    toast.success(`Đã áp dụng gợi ý "${s.name}" vào ${mealLabel(s.mealType).toLowerCase()}.`);
                  }}
                >
                  Áp dụng vào thực đơn
                </Button>
              </div>
            ))}
          </div>
          <DialogFooter className="items-center justify-between sm:justify-between">
            <p className="text-xs text-muted-foreground">
              Cuộc trao đổi với AI được lưu trữ phục vụ tuân thủ (BR-157).
            </p>
            <Button size="sm" variant="outline" disabled={aiLoading} onClick={fetchAiSuggestions}>
              <Sparkles /> Gợi ý mới
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* ===== Dialog Nộp phê duyệt (Screen 05 — UC-087) ===== */}
      <Dialog open={submitOpen} onOpenChange={setSubmitOpen}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Nộp thực đơn cho Ban giám hiệu (UC-087)</DialogTitle>
            <DialogDescription>
              {grade || "Chưa chọn khối"} · {dayLabel || "chưa chọn ngày"} · {allDishes.length} món ·{" "}
              {servings} suất ·{" "}
              <b className={costEstimate.perServing > ceiling ? "text-destructive" : ""}>
                {costEstimate.perServing.toLocaleString("vi-VN")}₫/suất
              </b>{" "}
              (trần {ceiling.toLocaleString("vi-VN")}₫)
            </DialogDescription>
          </DialogHeader>
          <ul className="space-y-2 text-sm">
            {(
              [
                [checks.gradeDate, "Đã chọn Khối + ngày áp dụng cách hôm nay ≥3 ngày (BR-061)"],
                [checks.noDuplicate, "Ngày × khối chưa có thực đơn khác (mỗi ngày 1 thực đơn)"],
                [checks.blocksValid, "Mỗi buổi ăn có ít nhất 1 món"],
                [checks.ingredients, "Mọi món khai đủ thành phần + định lượng (BR-069)"],
                [checks.nutrition, "Mọi món có thông tin năng lượng kcal (BR-062)"],
                [checks.allergy, "Món xung đột dị ứng có món phụ an toàn với TOÀN BỘ dị ứng của các em (BR-063)"],
                [checks.special, "Món đặc biệt khai đủ món phụ + số suất + định lượng"],
                [
                  checks.dietCoverage,
                  dietGaps.length === 0
                    ? "Mọi nhóm chế độ ăn của khối đều ăn được bữa này (BR-065)"
                    : `Còn ${dietGaps.length} chỗ nhóm chế độ ăn chưa có gì ăn: ` +
                      dietGaps
                        .map((g) => `${MEAL_TYPE_LABEL[g.mealType]}/${g.group.label}`)
                        .join(", "),
                ],
              ] as const
            ).map(([ok, label]) => (
              <li key={label} className="flex items-start gap-2">
                <Badge variant={ok ? "success" : "danger"}>{ok ? "Đạt" : "Chưa"}</Badge>
                <span className={ok ? "" : "text-destructive"}>{label}</span>
              </li>
            ))}
          </ul>
          {!allPassed && (
            <Alert variant="danger" title="Chưa đủ điều kiện nộp (MSG-051 nếu do dị ứng)">
              Hoàn thiện các mục &quot;Chưa&quot; ở trên rồi nộp lại — nút xác nhận đang bị khóa.
            </Alert>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setSubmitOpen(false)}>Hủy</Button>
            <Button
              disabled={!allPassed || isPending}
              onClick={() => {
                setSubmitOpen(false);
                persist("PENDING_APPROVAL");
              }}
            >
              <Send /> Xác nhận nộp (MSG-039)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* ===== Tạo suất riêng cho một nhóm chế độ ăn ===== */}
      {variantTarget && (
        <VariantDialog
          grade={grade}
          mealType={variantTarget.mealType}
          group={dietGroups.find((g) => g.id === variantTarget.groupId)!}
          baseDishes={blocks.find((b) => b.mealType === variantTarget.mealType)?.dishes ?? []}
          dishLibrary={dishLibrary}
          portions={dietGroupMembersInGrade(variantTarget.groupId, grade, dietGroups, students).length}
          onClose={() => setVariantTarget(null)}
          onSave={(dishes, note) => {
            setDraftVariants((prev) => [
              ...prev,
              {
                id: `MV-NEW-${String(Date.now()).slice(-6)}`,
                menuId: menu?.id ?? "",
                mealType: variantTarget.mealType,
                dietGroupId: variantTarget.groupId,
                mode: "FULL",
                portions: dietGroupMembersInGrade(variantTarget.groupId, grade, dietGroups, students).length,
                dishes,
                note,
                status: "PLANNED",
              },
            ]);
            setVariantTarget(null);
            toast.success("Đã thêm suất riêng — lưu thực đơn để ghi lại.");
          }}
        />
      )}
    </>
  );
}

/**
 * Soạn suất riêng cho MỘT nhóm × MỘT bữa.
 *
 * Mặc định lấy nguyên bữa gốc rồi bỏ những món nhóm này phải kiêng — người dùng
 * chỉ việc chọn món thay. Bắt đầu từ trang trắng thì phần lớn suất riêng sẽ khác
 * bữa chính nhiều hơn mức cần thiết, mà mỗi món khác là một nồi nấu riêng.
 */
function VariantDialog({
  grade,
  mealType,
  group,
  baseDishes,
  dishLibrary,
  portions,
  onClose,
  onSave,
}: {
  grade: string;
  mealType: MealBlock["mealType"];
  group: DietGroup;
  baseDishes: DishForm[];
  dishLibrary: Dish[];
  portions: number;
  onClose: () => void;
  /** `dishes` mang cả tên: món ngoài thư viện tra khoá ra rỗng, không có tên thì mất chỗ hiển thị. */
  onSave: (dishes: MenuVariantDish[], note: string) => void;
}) {
  /*
   * Món của bữa gốc mà nhóm này ăn được → giữ lại sẵn trong suất riêng.
   *
   * Bỏ qua món CHƯA ĐẶT TÊN: `dishIdOfName("")` trả về slug rác `mon-moi`, mang
   * khoá đó vào suất riêng thì mọi màn đọc lại chỉ có "mon-moi" để hiện
   * (23/07/2026 — Minh). Tên đi kèm khoá để món gõ tay ngoài thư viện vẫn hiển
   * thị đúng tên.
   */
  const keptFromBase = baseDishes
    .filter((d) => d.name.trim() !== "" && dishConflictsWithDiet(d, group).length === 0)
    .map((d) => ({ id: d.dishId ?? dishIdOfName(d.name.trim()), name: d.name.trim() }));
  const nameOfBaseDish = new Map(keptFromBase.map((d) => [d.id, d.name]));
  const [picked, setPicked] = useState<string[]>(keptFromBase.map((d) => d.id));
  const [note, setNote] = useState("");
  const dropped = baseDishes.filter(
    (d) => d.name.trim() !== "" && dishConflictsWithDiet(d, group).length > 0
  );
  // Món trong thư viện mà nhóm này ăn được — không gợi ý thứ vừa loại ra ở trên.
  const safeLibrary = dishLibrary.filter((d) => dishConflictsWithDiet(d, group).length === 0);
  /*
   * Danh sách chọn = thư viện + món GÕ TAY giữ lại từ bữa gốc. Trước đây chỉ liệt
   * kê thư viện, nên món gõ tay tuy đã được tick sẵn lại không hiện ra: người dùng
   * thấy "3 món" mà đếm trên màn hình chỉ có 2, và không bỏ tick nó được.
   */
  const options = [
    ...keptFromBase
      .filter((k) => !safeLibrary.some((d) => d.id === k.id))
      .map((k) => ({ id: k.id, name: k.name, meta: "món của bữa gốc (ngoài thư viện)" })),
    ...safeLibrary.map((d) => ({
      id: d.id,
      name: d.name,
      meta: `${categoryLabel(d.category)} · ${d.kcal} kcal`,
    })),
  ];

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>
            Suất riêng — {group.label} · {MEAL_TYPE_LABEL[mealType]} · {grade}
          </DialogTitle>
          <DialogDescription>
            {portions} suất. {group.note} Nấu và chia riêng, dụng cụ riêng (BR-082).
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {dropped.length > 0 && (
            <Alert variant="warning" title="Món của bữa gốc nhóm này không dùng được">
              {dropped
                .map((d) => `${d.name} (${dishConflictsWithDiet(d, group).join(", ")})`)
                .join(" · ")}
            </Alert>
          )}
          <div className="space-y-1.5">
            <Label>Món của suất riêng * ({picked.length} món)</Label>
            <div className="max-h-64 space-y-1 overflow-y-auto rounded-lg border p-2">
              {options.map((d) => (
                <label key={d.id} className="flex items-center gap-2 rounded px-1.5 py-1 text-sm hover:bg-muted">
                  <input
                    type="checkbox"
                    className="size-4 accent-primary"
                    checked={picked.includes(d.id)}
                    onChange={(e) =>
                      setPicked((prev) =>
                        e.target.checked ? [...prev, d.id] : prev.filter((x) => x !== d.id)
                      )
                    }
                  />
                  <span className="min-w-0 flex-1">{d.name}</span>
                  <span className="text-xs text-muted-foreground">{d.meta}</span>
                </label>
              ))}
            </div>
            <p className="text-xs text-muted-foreground">
              Chỉ hiện món nhóm này ăn được — món chứa thứ phải kiêng đã lọc sẵn.
            </p>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="mv-note">Ghi chú chế biến</Label>
            <Textarea
              id="mv-note"
              rows={2}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="VD: Nấu trước ở bếp phụ, dùng thớt và nồi riêng"
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button
            disabled={picked.length === 0}
            onClick={() =>
              onSave(
                picked.map((id) => ({
                  dishId: id,
                  name: dishLibrary.find((d) => d.id === id)?.name ?? nameOfBaseDish.get(id),
                })),
                note.trim()
              )
            }
          >
            Lưu suất riêng
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
