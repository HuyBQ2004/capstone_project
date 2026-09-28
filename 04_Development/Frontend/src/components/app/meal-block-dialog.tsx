"use client";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { MenuForm, type MealBlock, type PersistedMenu } from "@/components/app/menu-form";

/**
 * Soạn MỘT buổi ăn của MỘT ngày, ngay tại lịch thực đơn tuần.
 *
 * Nhận xét Minh 23/07/2026: bấm ô "Sáng" của một ngày mà bị đẩy sang trang lập
 * thực đơn — nơi bày ra cả ba bữa — là bắt người dùng tự tìm lại đúng ô mình vừa
 * bấm, và rất dễ gõ nhầm sang bữa khác. Hộp thoại này giữ nguyên form soạn món
 * đầy đủ (thành phần BR-069, cảnh báo dị ứng BR-063, suất riêng theo chế độ ăn
 * BR-065) nhưng khoá vào đúng buổi + đúng ngày của ô được bấm.
 *
 * Các buổi khác của ngày không mất: `MenuForm` ghép lại trước khi lưu.
 */

const MEAL_LABEL: Record<MealBlock["mealType"], string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

const WEEKDAY_LABELS = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];

function dayLabelOf(iso: string): string {
  const d = new Date(`${iso}T00:00:00`);
  const p = (n: number) => String(n).padStart(2, "0");
  return `${WEEKDAY_LABELS[d.getDay()]} ${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

export function MealBlockDialog({
  grade,
  dateIso,
  meal,
  menu,
  initialBlock,
  onClose,
}: {
  grade: string;
  dateIso: string;
  meal: MealBlock["mealType"];
  /** Thực đơn ngày đã có (sửa/thêm buổi); bỏ trống = ngày chưa có thực đơn nào. */
  menu?: PersistedMenu;
  /** G9: buổi đổ sẵn từ Thực đơn mẫu (nút "Áp dụng mẫu" ở ô lịch). */
  initialBlock?: MealBlock;
  onClose: () => void;
}) {
  const existing = menu?.blocksData?.some((b) => b.mealType === meal);
  const fromTemplate = !existing && initialBlock?.templateName;
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-h-[92vh] w-[calc(100%-2rem)] max-w-5xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {existing ? "Sửa" : "Tạo"} {MEAL_LABEL[meal].toLowerCase()} — {dayLabelOf(dateIso)} · {grade}
            {fromTemplate ? ` — theo mẫu "${initialBlock!.templateName}"` : ""}
          </DialogTitle>
          <DialogDescription>
            {fromTemplate
              ? "Món + suất thay thế dị ứng đã đổ sẵn từ mẫu — kiểm tra, chỉnh nếu tuần này khác, rồi Lưu. "
              : ""}
            Chỉ soạn buổi này; các buổi khác của ngày giữ nguyên. Khối và ngày lấy
            theo ô vừa bấm trên lịch nên không sửa ở đây.
          </DialogDescription>
        </DialogHeader>
        <MenuForm
          mode={menu ? "edit" : "create"}
          menu={menu}
          initialGrade={grade}
          initialDate={dateIso}
          singleMeal={meal}
          initialBlock={initialBlock}
          embedded
          onSaved={onClose}
        />
      </DialogContent>
    </Dialog>
  );
}
