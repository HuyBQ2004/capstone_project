"use client";

import Link from "next/link";
import { LayoutTemplate, PackageMinus, Replace, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { MealBlock } from "@/components/app/menu-form";
import { subsOf } from "@/lib/menu-substitutes";
import { dishLibraryAllergens, type Dish } from "@/lib/mock-data";

/**
 * Chi tiết MỘT thực đơn mẫu trong một ô (ngày × bữa) — dùng chung cho Lịch thực
 * đơn tuần (Service Caterers) và màn Phê duyệt thực đơn (School Admin): hai bên
 * nhìn cùng một nội dung bữa ăn, chỉ khác quyền (bên SC gỡ được, bên BGH chỉ xem).
 */

/**
 * Danh sách thực đơn mẫu của MỘT buổi. Bản ghi cũ (trước đợt 2 ngày 25/07/2026)
 * không có `templates` — món khai tay hiện thành một mục chỉ-xem để lịch seed cũ
 * vẫn đọc được, không ép người dùng dựng lại.
 */
export function blockEntries(
  block: MealBlock
): { templateId: string; templateName: string; readOnly?: boolean }[] {
  if (block.templates?.length) {
    const entries: { templateId: string; templateName: string; readOnly?: boolean }[] = [
      ...block.templates,
    ];
    // Món không thuộc thực đơn nào (bản ghi ghép tay từ trước) vẫn phải nhìn thấy.
    if (block.dishes.some((d) => !d.templateId)) {
      entries.push({ templateId: "", templateName: "Món ngoài thực đơn", readOnly: true });
    }
    return entries;
  }
  if (block.dishes.length === 0) return [];
  const first = block.dishes[0]?.name || "(chưa đặt tên)";
  const label =
    block.templateName ??
    (block.dishes.length > 1 ? `${first} (+${block.dishes.length - 1})` : first);
  return [{ templateId: block.templateId ?? "", templateName: label, readOnly: true }];
}

/** Món của một mục trong buổi — mục "" gom mọi món không gắn thực đơn nào. */
export function entryDishes(block: MealBlock, templateId: string) {
  return templateId
    ? block.dishes.filter((d) => d.templateId === templateId)
    : block.dishes.filter((d) => !d.templateId);
}

export function TemplateDetailDialog({
  entryName,
  dishes,
  dishById,
  note,
  context,
  removable = false,
  issueHref,
  onRemove,
  onClose,
}: {
  entryName: string;
  dishes: MealBlock["dishes"];
  dishById: Map<string, Dish>;
  note?: string;
  context: string;
  /** Còn gỡ được (thực đơn ở trạng thái Nháp/Bị từ chối và mục này là mẫu thật). */
  removable?: boolean;
  /**
   * Deep-link sang màn Xuất kho với ngày + bữa chọn sẵn — chỉ khi thực đơn ĐÃ
   * CÔNG BỐ (BR-067: chưa duyệt thì chưa được xuất kho phục vụ).
   */
  issueHref?: string;
  onRemove?: () => void;
  onClose: () => void;
}) {
  const totalKcal = dishes.reduce((s, d) => s + (Number(d.kcal) || 0), 0);
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex flex-wrap items-center gap-1.5">
            <LayoutTemplate className="size-4 text-primary" /> {entryName}
          </DialogTitle>
          <DialogDescription>
            {context} — {dishes.length} món, {totalKcal} kcal/suất
            {note ? ` · ${note}` : ""}
          </DialogDescription>
        </DialogHeader>
        <ul className="space-y-2">
          {dishes.map((d, i) => {
            const subs = subsOf(d);
            const lib = d.dishId ? dishById.get(d.dishId) : undefined;
            const allergens = lib ? dishLibraryAllergens(lib) : (d.manualAllergens ?? []);
            /*
             * Món dị ứng luôn hiện MÓN THAY THẾ ngay dưới món (Minh 25/07/2026 —
             * "để người xem yên tâm"): ưu tiên suất đã đổ vào bữa; bữa chưa có
             * suất (khối không có HS dị ứng món này) thì hiện khai báo mặc định
             * từ Thư viện món (G10); không có gì mới cảnh báo thiếu.
             */
            const defaultSub = lib?.defaultSubstituteId
              ? dishById.get(lib.defaultSubstituteId)
              : undefined;
            return (
              <li key={i} className="rounded-lg border bg-card p-2.5 text-sm">
                <span className="font-medium">{d.name || "(chưa đặt tên)"}</span>
                {d.kcal && (
                  <span className="ml-1.5 text-xs text-muted-foreground">{d.kcal} kcal</span>
                )}
                {allergens.map((a) => (
                  <Badge key={a} variant="danger" className="ml-1.5">{a}</Badge>
                ))}
                {/* Món thay thế hiện GỌN: icon ↷ + tên món (Minh 25/07/2026 —
                    câu giải thích dài chuyển vào tooltip). */}
                {subs.length > 0 ? (
                  subs.map((s, si) => (
                    <span
                      key={si}
                      className="mt-1 flex items-center gap-1 text-xs font-medium text-muted-foreground"
                      title={`Học sinh dị ứng được thay thế bằng món này${s.allergenIds?.length ? ` (tránh ${s.allergenIds.join(", ")})` : ""}`}
                    >
                      <Replace className="size-3.5 shrink-0 text-primary" />
                      {s.name || "(chưa chọn món)"}
                      {Number(s.portions) > 0 && (
                        <span className="font-normal">· {s.portions} suất</span>
                      )}
                    </span>
                  ))
                ) : allergens.length > 0 ? (
                  defaultSub ? (
                    <span
                      className="mt-1 flex items-center gap-1 text-xs font-medium text-muted-foreground"
                      title="Học sinh dị ứng được thay thế bằng món này (khai sẵn ở Thư viện món)"
                    >
                      <Replace className="size-3.5 shrink-0 text-primary" />
                      {defaultSub.name}
                    </span>
                  ) : (
                    <span className="mt-1 block text-xs font-medium text-warning">
                      Chưa khai món thay thế cho món này — bổ sung ở Thư viện món
                    </span>
                  )
                ) : null}
              </li>
            );
          })}
        </ul>
        <DialogFooter>
          {removable && onRemove && (
            <Button variant="outline" onClick={onRemove}>
              <Trash2 /> Gỡ khỏi bữa này
            </Button>
          )}
          {issueHref && (
            <Link href={issueHref}>
              <Button variant="outline">
                <PackageMinus /> Xuất kho bữa này (FEFO)
              </Button>
            </Link>
          )}
          <Button variant="outline" onClick={onClose}>Đóng</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
