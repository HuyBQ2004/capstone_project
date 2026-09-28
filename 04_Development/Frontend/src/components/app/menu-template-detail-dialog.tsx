"use client";

import Link from "next/link";
import { useState } from "react";
import { ChevronDown, ChevronUp, FileText, LayoutTemplate, PackageMinus, Plus, Replace, Trash2, X } from "lucide-react";
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
import { SearchableSelect } from "@/components/ui/input";
import type { MealBlock } from "@/components/app/menu-form";
import { subsOf } from "@/lib/menu-substitutes";
import {
  dishCostPerServing,
  dishLibraryAllergens,
  type Dish,
  type IngredientCatalogRef,
} from "@/lib/domain-utils";
import type { SupplierProduct } from "@/lib/supplier-product-adapter";

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
  block: MealBlock,
  /** Tên thực đơn (`Menu.name`) — nhãn hiển thị khi buổi chưa gắn thực đơn mẫu nào (bản ghi cũ). */
  menuName?: string,
  /**
   * V55 — `templateId` → tên mẫu, để đặt nhãn cho chip dẫn xuất từ chính các món.
   * Nơi gọi lấy từ danh sách `menu-templates` sẵn có; thiếu tên thì hiện nhãn tạm.
   */
  templateNames?: Map<string, string>
): { templateId: string; templateName: string; readOnly?: boolean }[] {
  /*
   * V55 — nguồn gốc mẫu nay LƯU Ở TỪNG MÓN (`menu_dishes.template_id`), server trả lại qua
   * `MenuBlockDish.templateId`. Ưu tiên dẫn xuất từ món, vì `block.templates[]` là khái
   * niệm CHỈ CÓ Ở FE và KHÔNG round-trip: gửi lên thì `applyBlocksData()` bỏ qua, đọc về
   * thì luôn undefined.
   *
   * Trước 07/08/2026 vì thế mà: ô lịch chỉ hiện MỘT mục dù đã thêm 2–3 mẫu, chống trùng
   * không bao giờ khớp, và "gỡ thực đơn khỏi bữa" không gỡ gì nhưng vẫn báo thành công.
   */
  const fromDishes = new Map<string, string>();
  let hasLooseDish = false;
  for (const d of block.dishes) {
    if (d.templateId) {
      fromDishes.set(d.templateId, templateNames?.get(d.templateId) ?? "Thực đơn mẫu");
    } else {
      hasLooseDish = true;
    }
  }
  if (fromDishes.size > 0) {
    const entries = [...fromDishes].map(([templateId, templateName]) => ({ templateId, templateName }));
    // Món ghép tay lẫn trong ô vẫn phải nhìn thấy để không "biến mất" khỏi UI.
    if (hasLooseDish) {
      entries.push({ templateId: "", templateName: "Món ngoài thực đơn" });
    }
    return entries;
  }
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
  // Ưu tiên tên thực đơn thật — món đầu tiên gần như luôn là "Cơm trắng" nên
  // không dùng làm nhãn đại diện (dễ gây nhầm lẫn giữa các thực đơn khác nhau).
  const label = block.templateName ?? menuName ?? "(chưa đặt tên)";
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
  fullRecordHref,
  onRemove,
  onRemoveDish,
  onAddDish,
  dishLibrary,
  costing,
  nutritionTarget,
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
  /**
   * Deep-link sang HỒ SƠ ĐẦY ĐỦ của thực đơn (khai báo dị ứng theo học sinh, chi phí, toàn bộ
   * bữa trong ngày) — dùng ở màn Ban giám hiệu duyệt thực đơn.
   *
   * <p>Vì sao cần: dialog này cố tình gọn để duyệt nhanh cả tuần, nhưng nó chỉ hiện món của MỘT
   * mục. Trước 09/08/2026 màn `menu-approval/[id]` (550 dòng, đủ hồ sơ + nút Duyệt/Từ chối)
   * KHÔNG có nút nào dẫn tới — `npm run check:routes` bắt được. Ban giám hiệu ký duyệt mà không
   * mở được hồ sơ đầy đủ là đúng lớp lỗi đã sửa cho CAPA hôm 08/08. Nam chốt hướng (a): giữ cả
   * hai, dialog cho thao tác nhanh, link này cho lúc cần soi kỹ.
   */
  fullRecordHref?: string;
  /** Gỡ CẢ MỤC (mọi món của thực đơn mẫu này) khỏi bữa. */
  onRemove?: () => void;
  /**
   * 11/08/2026 — trước đây chỉ gỡ được CẢ mục, muốn bớt một món lẻ phải gỡ hết
   * rồi thêm lại từ đầu. Gỡ ĐÚNG một món (so khớp theo tham chiếu, `dishes` truyền
   * vào là `block.dishes.filter(...)` nên vẫn cùng object với mảng gốc).
   */
  onRemoveDish?: (dish: MealBlock["dishes"][number]) => void;
  /** Thêm một món từ Thư viện món vào ĐÚNG mục đang xem (gắn cùng templateId). */
  onAddDish?: (dish: Dish) => void;
  /** Danh mục món để chọn thêm — chỉ cần khi có `onAddDish`. */
  dishLibrary?: Dish[];
  /**
   * 13/08/2026 (Nam) — dữ liệu để ước tính chi phí (BR-080). Truyền vào thì dialog hiện giá
   * TỪNG MÓN và tổng giá của bữa; không truyền thì phần giá ẩn hẳn (giữ nguyên hành vi cũ cho
   * nơi gọi chưa có sẵn 3 danh mục này).
   */
  costing?: {
    ingredientCatalog: IngredientCatalogRef[];
    supplierProducts: SupplierProduct[];
    partners: { id?: string; status?: string }[];
  };
  /**
   * Chuẩn dinh dưỡng của khối (BR-062) — dùng đối chiếu kcal của bữa. CẢNH BÁO MỀM: lệch quá
   * ±10% thì đánh dấu để người lập/BGH biết, KHÔNG chặn nộp hay duyệt (chốt với Nam 13/08).
   */
  nutritionTarget?: { gradeName: string; kcal: number; configured: boolean };
  onClose: () => void;
}) {
  const [expanded, setExpanded] = useState<number | null>(null);
  const [addingDish, setAddingDish] = useState(false);
  const [pickedDishId, setPickedDishId] = useState("");
  const totalKcal = dishes.reduce((s, d) => s + (Number(d.kcal) || 0), 0);
  /** Giá từng món + tổng của bữa — chỉ tính khi nơi gọi truyền đủ 3 danh mục giá. */
  const dishCosts = costing
    ? dishes.map((d) =>
        dishCostPerServing(d, costing.ingredientCatalog, costing.supplierProducts, costing.partners)
      )
    : [];
  const mealCost = dishCosts.reduce((sum, c) => sum + c.cost, 0);
  const mealCostIncomplete = dishCosts.some((c) => c.missingPrices.length > 0);
  /** Lệch chuẩn kcal quá ±10% (BR-062) — cảnh báo mềm, không chặn. */
  const kcalGap =
    nutritionTarget && nutritionTarget.kcal > 0 && totalKcal > 0
      ? (totalKcal - nutritionTarget.kcal) / nutritionTarget.kcal
      : 0;
  const kcalOff = Math.abs(kcalGap) > 0.1;
  const inEntryDishIds = new Set(dishes.map((d) => d.dishId).filter(Boolean));
  const addableDishes = (dishLibrary ?? []).filter((d) => d.isActive && !inEntryDishIds.has(d.id));
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex flex-wrap items-center gap-1.5">
            <LayoutTemplate className="size-4 text-primary" /> {entryName}
          </DialogTitle>
          <DialogDescription>
            {context} — {dishes.length} món, {totalKcal} kcal/suất
            {costing && (
              <>
                {" · "}
                <b>{mealCost.toLocaleString("vi-VN")}₫/suất</b>
                {mealCostIncomplete && " (chưa đủ báo giá)"}
              </>
            )}
            {note ? ` · ${note}` : ""}
          </DialogDescription>
        </DialogHeader>
        {/*
          BR-062 (13/08/2026, Nam) — đối chiếu kcal của bữa với chuẩn của khối. Màn cấu hình
          trường vẫn ghi "Thực đơn lệch quá ±10% so với chuẩn của khối sẽ được đánh dấu ở màn
          duyệt", nhưng trước nay CHỈ `special-meal-dialog` (suất riêng) làm; thực đơn CHÍNH thì
          không đối chiếu gì — lời hứa trên màn cấu hình không có ai thực hiện.
          CẢNH BÁO MỀM: đánh dấu để biết, KHÔNG chặn nộp/duyệt.
        */}
        {nutritionTarget && kcalOff && (
          <p
            className={
              "rounded-md border px-3 py-2 text-xs " +
              "border-amber-500/50 bg-amber-500/10 text-amber-800 dark:text-amber-400"
            }
          >
            Bữa này {totalKcal} kcal/suất, lệch {kcalGap > 0 ? "+" : ""}
            {Math.round(kcalGap * 100)}% so với chuẩn {nutritionTarget.gradeName} (
            {nutritionTarget.kcal} kcal
            {!nutritionTarget.configured && ", mặc định hệ thống"}). Chỉ là cảnh báo — vẫn nộp và
            duyệt được nếu ngày đó có lý do.
          </p>
        )}
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
            const isExpanded = expanded === i;
            return (
              <li key={i} className="rounded-lg border bg-card p-2.5 text-sm">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0 flex-1">
                    <span className="font-medium">{d.name || "(chưa đặt tên)"}</span>
                    {/*
                     * Số suất của MÓN CHÍNH (08/08/2026). `MenuBlockDish.portions` đã có sẵn ở
                     * backend (`MenuBlockDish.portions` ↔ `DishForm.servings`) nhưng chỉ món THAY THẾ được hiện
                     * số suất — bếp mở chi tiết bữa ra không biết phải nấu bao nhiêu suất mỗi món,
                     * đúng con số cần nhất để chuẩn bị.
                     */}
                    {Number(d.servings) > 0 && (
                      <Badge variant="info" className="ml-1.5">{d.servings} suất</Badge>
                    )}
                    {d.kcal && (
                      <span className="ml-1.5 text-xs text-muted-foreground">{d.kcal} kcal</span>
                    )}
                    {costing && dishCosts[i] && (
                      <span className="ml-1.5 text-xs text-muted-foreground">
                        · {dishCosts[i].cost.toLocaleString("vi-VN")}₫/suất
                        {dishCosts[i].missingPrices.length > 0 && (
                          <span className="text-warning">
                            {" "}(thiếu giá {dishCosts[i].missingPrices.join(", ")})
                          </span>
                        )}
                      </span>
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
                  </div>
                  <div className="flex shrink-0 items-center gap-0.5">
                    {lib && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-7 px-1.5 text-muted-foreground"
                        title={isExpanded ? "Ẩn chi tiết món" : "Xem chi tiết món (thành phần, dinh dưỡng)"}
                        onClick={() => setExpanded(isExpanded ? null : i)}
                      >
                        {isExpanded ? <ChevronUp className="size-3.5" /> : <ChevronDown className="size-3.5" />}
                      </Button>
                    )}
                    {removable && onRemoveDish && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="h-7 px-1.5 text-destructive"
                        title="Gỡ đúng món này khỏi bữa"
                        onClick={() => onRemoveDish(d)}
                      >
                        <X className="size-3.5" />
                      </Button>
                    )}
                  </div>
                </div>
                {isExpanded && lib && (
                  <div className="mt-2 space-y-1.5 border-t pt-2 text-xs text-muted-foreground">
                    <p>
                      Đạm {lib.protein}g · Béo {lib.fat}g · Tinh bột {lib.carb}g · {lib.kcal} kcal/suất
                    </p>
                    {lib.ingredients.length > 0 && (
                      <p>
                        <span className="font-medium text-foreground">Thành phần: </span>
                        {lib.ingredients.map((ing) => `${ing.name}${ing.quantity ? ` (${ing.quantity}${ing.unit ?? ""})` : ""}`).join(", ")}
                      </p>
                    )}
                    {lib.description && (
                      <p><span className="font-medium text-foreground">Mô tả: </span>{lib.description}</p>
                    )}
                    {lib.cookingInstructions && (
                      <p><span className="font-medium text-foreground">Cách chế biến: </span>{lib.cookingInstructions}</p>
                    )}
                  </div>
                )}
              </li>
            );
          })}
        </ul>
        {removable && onAddDish && (
          <div className="rounded-lg border border-dashed p-2.5">
            {addingDish ? (
              <div className="flex items-center gap-2">
                <SearchableSelect
                  value={pickedDishId}
                  onChange={setPickedDishId}
                  options={addableDishes.map((d) => ({ value: d.id, label: `${d.name} (${d.kcal} kcal)` }))}
                  placeholder="— Chọn món từ thư viện —"
                  searchPlaceholder="Tìm món…"
                />
                <Button
                  size="sm"
                  disabled={!pickedDishId}
                  onClick={() => {
                    const dish = addableDishes.find((x) => x.id === pickedDishId);
                    if (!dish) return;
                    onAddDish(dish);
                    setPickedDishId("");
                    setAddingDish(false);
                  }}
                >
                  Thêm
                </Button>
                <Button size="sm" variant="ghost" onClick={() => { setAddingDish(false); setPickedDishId(""); }}>
                  Hủy
                </Button>
              </div>
            ) : (
              <Button type="button" variant="ghost" size="sm" onClick={() => setAddingDish(true)}>
                <Plus className="size-3.5" /> Thêm món vào mục này
              </Button>
            )}
          </div>
        )}
        <DialogFooter>
          {removable && onRemove && (
            <Button variant="outline" onClick={onRemove}>
              <Trash2 /> Gỡ cả mục khỏi bữa
            </Button>
          )}
          {fullRecordHref && (
            <Link href={fullRecordHref}>
              <Button variant="outline">
                <FileText /> Xem hồ sơ đầy đủ
              </Button>
            </Link>
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
