import type { MenuTemplateDto } from "@/lib/api/types";
import type { MealType } from "@/lib/domain-utils";

/**
 * `MenuTemplateResponse` khai MỌI field optional (springdoc không đọc `required`) — 5 trang
 * dùng chung type này (Thực đơn mẫu CRUD, Lịch thực đơn tuần SC, Phê duyệt SCHOOL_ADMIN,
 * Kiểm thực Bữa ăn, `mealBlockFromTemplate()`) đều đọc `id`/`name`/`dishIds`/`isActive`/
 * `mealType` như luôn có mặt. Narrow về "Draft" NGAY TẠI ranh giới nạp dữ liệu
 * (`normalizeMenuTemplate`) thay vì rải `?? ""` khắp 5 nơi (Giai đoạn 3, Handoff-Mock-Data-
 * Removal.md, pattern "Draft type" thiết lập ở phiên 4).
 */
export type MenuTemplate = Pick<MenuTemplateDto, "grade" | "note"> & {
  /**
   * V44 — UUID của khối, khoá GHI của `CreateMenuTemplateRequest`. `grade` (nhãn) giữ cho
   * chiều ĐỌC. Trước 07/08/2026 FE gửi nhãn vào chỗ đòi UUID; vì field nullable nên KHÔNG
   * lỗi mà lưu `grade_id = NULL` im lặng — người dùng chọn khối, hệ thống lưu "dùng chung".
   */
  gradeId?: string;
  id: string;
  name: string;
  mealType: MealType;
  audience: string;
  dishIds: string[];
  isActive: boolean;
};

export function normalizeMenuTemplate(t: MenuTemplateDto): MenuTemplate {
  return {
    id: t.id ?? "",
    name: t.name ?? "",
    mealType: (t.mealType as MealType) ?? "LUNCH",
    audience: t.audience ?? "ALL",
    dishIds: t.dishIds ?? [],
    isActive: t.isActive ?? true,
    grade: t.grade,
    note: t.note,
  };
}
