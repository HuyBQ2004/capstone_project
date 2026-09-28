"use client";

import { useState } from "react";
import { Soup } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import type { DishForm, MealBlock, PersistedMenu } from "@/components/app/menu-form";
import {
  DIET_GROUPS,
  NUTRITION_TARGET,
  allergenTouches,
  dietGroupMembersInGrade,
  dishLibraryAllergens,
  gradeLevelsOf,
  isAllergyVerified,
  standardMenus,
  variantNutrition,
  type Classroom,
  type Dish,
  type MenuVariant,
} from "@/lib/mock-data";

/** THƯ VIỆN MÓN (`Dish`) → `DishForm` để nhét thẳng vào blocksData của thực đơn riêng. */
function dishToForm(d: Dish): DishForm {
  return {
    dishId: d.id,
    name: d.name,
    ingredients: d.ingredients.map((i) => ({ name: i.name, quantity: i.quantity, unit: i.unit, ingredientId: i.ingredientId })),
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
 * Dialog lập/sửa THỰC ĐƠN RIÊNG cho một nhóm chế độ ăn (MenuVariant mode FULL).
 *
 * Tách khỏi màn "Bữa ăn đặc biệt" ngày 23/07/2026 (nhận xét Minh): hai màn Thực
 * đơn và Bữa ăn đặc biệt gộp thành MỘT lịch thời khóa biểu, nên hộp thoại này
 * phải mở được từ đúng ô lịch (ngày + buổi + nhóm đã chọn sẵn) thay vì bắt người
 * dùng chọn lại từ đầu.
 *
 * BR-065/BR-066: nhóm chế độ ăn phát sinh từ hồ sơ HS đã Y tế xác minh ·
 * BR-082: chế biến riêng biệt, dụng cụ riêng · BR-062: đối chiếu chuẩn dinh dưỡng.
 */

const MEAL_LABEL: Record<MealBlock["mealType"], string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

const MEAL_SORT: Record<MealBlock["mealType"], number> = {
  BREAKFAST: 0,
  LUNCH: 1,
  SNACK: 2,
};

export function SpecialMealDialog({
  initialGrade,
  menus,
  weekIsos,
  initialMenuId,
  initialGroup,
  initialMeal,
  initialDishIds,
  editVariant,
  onClose,
}: {
  initialGrade: string;
  menus: PersistedMenu[];
  weekIsos: string[];
  /** Mở từ một ô lịch — chọn sẵn thực đơn ngày. */
  initialMenuId?: string;
  initialGroup?: string;
  initialMeal?: MealBlock["mealType"];
  /**
   * G9: món tick sẵn khi mở từ nút "Áp dụng mẫu" của ô nhóm chế độ ăn trên lịch
   * tuần (mẫu có audience = nhóm, vd "Thực đơn ăn chay A1"). Người dùng vẫn
   * tick/bỏ tick như thường — mẫu chỉ thay công chọn ban đầu.
   */
  initialDishIds?: string[];
  /** Có giá trị = chế độ SỬA suất riêng đã lập (không đổi ngày/buổi/nhóm). */
  editVariant?: MenuVariant;
  onClose: () => void;
}) {
  const isEdit = editVariant != null;

  // Cùng nguồn khối với các màn khác — cấu hình lớp thật, không hằng số.
  const { data: classroomPage } = usePagedList<Classroom>("classrooms", { pageSize: 200 });
  const gradeLevels = gradeLevelsOf(classroomPage?.items ?? []);
  // 22/07/2026: thực đơn riêng đã GỘP vào bảng menus — ghi thẳng một `Menu`
  // (audience = nhóm), không còn resource "menu-variants".
  const createMenu = useCreate<Partial<PersistedMenu>>("menus");
  const updateMenu = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = (dishesPage?.items ?? []).filter((d) => d.isActive);

  /*
   * Chốt 22/07/2026: bữa ăn đặc biệt LÀ MỘT THỰC ĐƠN RIÊNG cho nhóm chế độ ăn —
   * không còn lựa chọn "thay vài món lẻ". Dị ứng thì thay đúng món chạm tác nhân,
   * ngay tại màn Thực đơn. Hai việc khác nhau nên không dùng chung một cơ chế:
   * chế độ ăn đổi cả cấu trúc bữa, ép vào mô hình thay-từng-món thì bếp phải tự
   * suy ra bữa hoàn chỉnh và không ai cộng được tổng dinh dưỡng của suất đó.
   */
  const [grade, setGrade] = useState(initialGrade);
  const [menuId, setMenuId] = useState(editVariant?.menuId ?? initialMenuId ?? "");
  const [variantMeal, setVariantMeal] = useState<MealBlock["mealType"] | "">(
    editVariant?.mealType ?? initialMeal ?? ""
  );
  const [groupSel, setGroupSel] = useState(editVariant?.dietGroupId ?? initialGroup ?? "");
  const [variantDishIds, setVariantDishIds] = useState<string[]>(
    editVariant?.dishes.map((d) => d.dishId) ?? initialDishIds ?? []
  );
  const [subPortions, setSubPortions] = useState(() => {
    if (editVariant) return String(editVariant.portions);
    const g = initialGroup;
    if (!g) return "";
    const count = dietGroupMembersInGrade(g, initialGrade).length;
    return count > 0 ? String(count) : "";
  });
  const [subNote, setSubNote] = useState(editVariant?.note ?? "");
  // BR-082: bắt buộc xác nhận chế biến riêng biệt, dụng cụ riêng trước khi lưu.
  // Suất đã lập thì điều kiện này đã được xác nhận một lần rồi.
  const [separatePrep, setSeparatePrep] = useState(isEdit);
  const [reason, setReason] = useState(editVariant?.reason ?? "");
  const [error, setError] = useState("");

  // Chỉ chọn thực đơn CHUNG của khối để gắn thực đơn riêng; thực đơn riêng khác
  // (audience != "ALL") không xuất hiện trong danh sách chọn ngày.
  const weekMenus = standardMenus(menus)
    .filter((m) => m.grade === grade && m.menuDateIso && weekIsos.includes(m.menuDateIso))
    .sort((a, b) => (a.menuDateIso ?? "").localeCompare(b.menuDateIso ?? ""));
  const menu = standardMenus(menus).find((m) => m.id === menuId);
  /** Thực đơn còn sửa được thì suất riêng đi kèm thực đơn đi duyệt, không cần duyệt riêng. */
  const isMenuEditable = menu?.status === "DRAFT" || menu?.status === "REJECTED";
  /**
   * Chỉ nhóm CÓ học sinh trong khối này mới được lập suất — lập cho nhóm 0 HS là
   * sinh "thực đơn rác": bếp nấu thừa, kho xuất thừa mà không ai ăn. Nhóm chế độ
   * ăn phải xuất phát từ hồ sơ học sinh đã Y tế xác minh (BR-065/066) nên màn này
   * KHÔNG có lựa chọn "tạo nhóm mới" tuỳ ý — muốn thêm nhóm thì khai ở hồ sơ HS.
   */
  const eligibleGroups = DIET_GROUPS.map((g) => ({
    ...g,
    members: dietGroupMembersInGrade(g.id, grade),
  })).filter((g) => g.members.length > 0);
  const groupLabel = DIET_GROUPS.find((g) => g.id === groupSel)?.label ?? groupSel;
  const members = groupSel ? dietGroupMembersInGrade(groupSel, grade) : [];

  /**
   * Tác nhân dị ứng ĐÃ XÁC MINH của chính các em trong nhóm+khối này. Món của
   * suất riêng phải an toàn với cả hồ sơ dị ứng, không chỉ hợp chế độ ăn: HS ăn
   * chay mà dị ứng đậu nành thì "Đậu phụ sốt" vẫn là món cấm.
   */
  const memberAllergens = [
    ...new Set(members.filter(isAllergyVerified).flatMap((s) => s.allergies)),
  ];

  const variantDishes = variantDishIds.map((id) => ({ dishId: id }));
  const variantNut = variantNutrition({ dishes: variantDishes });
  const variantConflicts = [
    ...new Set(
      variantDishIds.flatMap((id) => {
        const d = dishLibrary.find((x) => x.id === id);
        if (!d) return [];
        const al = dishLibraryAllergens(d);
        return memberAllergens.filter((sa) => al.some((da) => allergenTouches(sa, da)));
      })
    ),
  ];

  function changeGrade(g: string) {
    setGrade(g);
    // Đổi khối → thực đơn phải chọn lại theo khối mới
    setMenuId("");
    setVariantMeal("");
    // Nhóm có thể không còn học sinh nào ở khối mới → bỏ chọn thay vì giữ nhóm rỗng
    const count = groupSel ? dietGroupMembersInGrade(groupSel, g).length : 0;
    if (groupSel && count === 0) setGroupSel("");
    if (count > 0) setSubPortions(String(count));
  }

  function changeGroup(v: string) {
    setGroupSel(v);
    if (v) {
      const count = dietGroupMembersInGrade(v, grade).length;
      if (count > 0) setSubPortions(String(count));
    }
  }

  /**
   * Thêm/sửa suất riêng của thực đơn ĐÃ NỘP/ĐÃ CÔNG BỐ là thay đổi thứ Ban giám
   * hiệu đã duyệt. Không cấm hẳn: học sinh nhập học giữa kỳ, hoặc Y tế vừa xác
   * minh xong hồ sơ, là chuyện thường ngày — cấm thì bếp sẽ lách bằng đường khác
   * và hệ thống mất dấu. Nên: cho thêm, nhưng phải nêu lý do và chờ duyệt.
   */
  const supplemental = !isMenuEditable;

  function submit() {
    if (!menu) {
      setError("Chọn thực đơn ngày trước.");
      return;
    }
    if (!groupSel || members.length === 0) {
      setError(
        groupSel
          ? `Nhóm "${groupLabel}" không có học sinh nào ở ${grade} — không lập suất cho nhóm rỗng (BR-065/066).`
          : "Chọn nhóm chế độ ăn."
      );
      return;
    }
    if (!variantMeal) {
      setError("Chọn buổi ăn được thay trọn bộ.");
      return;
    }
    if (variantDishIds.length === 0) {
      setError("Chọn các món của suất trọn bộ từ thư viện món.");
      return;
    }
    if (!(Number(subPortions) > 0)) {
      setError("Số suất phải ≥ 1.");
      return;
    }
    if (variantConflicts.length > 0) {
      setError(
        `Suất trọn bộ chứa ${variantConflicts.join(", ")} — chạm hồ sơ dị ứng đã xác minh của học sinh nhóm "${groupLabel}" (BR-063/065).`
      );
      return;
    }
    if (!separatePrep) {
      setError("Phải tích xác nhận CHẾ BIẾN RIÊNG BIỆT, DỤNG CỤ RIÊNG (BR-082).");
      return;
    }
    if (supplemental && reason.trim().length < 10) {
      setError(
        "Thực đơn này đã nộp/công bố — phải ghi rõ vì sao phát sinh muộn (ít nhất 10 ký tự). Suất sẽ chờ Ban giám hiệu duyệt trước khi nấu."
      );
      return;
    }
    setError("");

    // Bộ món của buổi này = MÓN ĐẦY ĐỦ (không delta) dựng từ thư viện.
    const selectedForms = variantDishIds
      .map((id) => dishLibrary.find((x) => x.id === id))
      .filter((d): d is Dish => Boolean(d))
      .map(dishToForm);
    const newBlock: MealBlock = {
      mealType: variantMeal,
      dishes: selectedForms,
      note: subNote.trim() || undefined,
      portions: Number(subPortions),
    };

    /*
     * GỘP vào bảng menus (22/07/2026): thực đơn riêng của (khối + ngày + nhóm) là
     * MỘT `Menu` với audience = nhóm. Lập/sửa MỘT buổi = upsert block của buổi đó
     * vào thực đơn riêng; các buổi khác của nhóm giữ nguyên. Không còn "suất lẻ".
     */
    const specialMenuId = `${menu.id}--${groupSel}`;
    const existing = menus.find((m) => m.id === specialMenuId);
    const mergedBlocks: MealBlock[] = (
      existing
        ? [...(existing.blocksData ?? []).filter((b) => b.mealType !== variantMeal), newBlock]
        : [newBlock]
    ).sort((a, b) => MEAL_SORT[a.mealType] - MEAL_SORT[b.mealType]);
    const dishCount = mergedBlocks.reduce((s, b) => s + b.dishes.length, 0);
    const totalPortions = Math.max(0, ...mergedBlocks.map((b) => b.portions ?? 0));

    // Suất bị TỪ CHỐI mà sửa xong phải quay lại hàng chờ duyệt; thực đơn còn nháp
    // thì đi kèm thực đơn chung đi duyệt (DRAFT); bổ sung muộn thì chờ duyệt riêng.
    const nextStatus: PersistedMenu["status"] = existing
      ? existing.status === "REJECTED"
        ? "PENDING_APPROVAL"
        : existing.status
      : supplemental
        ? "PENDING_APPROVAL"
        : "DRAFT";
    const nextReason = supplemental ? reason.trim() : existing?.reason;

    const done = () => {
      toast.success(
        nextStatus === "PENDING_APPROVAL"
          ? `Đã gửi thực đơn riêng nhóm "${groupLabel}" — chờ Ban giám hiệu duyệt mới được nấu (BR-065).`
          : `Đã lưu thực đơn riêng nhóm "${groupLabel}" — ${MEAL_LABEL[variantMeal]} ${menu.week}, ${selectedForms.length} món, ${variantNut.kcal} kcal × ${subPortions} suất.`
      );
      onClose();
    };

    if (existing) {
      updateMenu.mutate(
        { id: specialMenuId, blocksData: mergedBlocks, dishes: dishCount, servings: totalPortions, status: nextStatus, reason: nextReason },
        { onSuccess: done }
      );
      return;
    }

    createMenu.mutate(
      {
        id: specialMenuId,
        week: menu.week,
        name: `${menu.name} · ${groupLabel}`,
        grade: menu.grade,
        menuDateIso: menu.menuDateIso,
        status: nextStatus,
        audience: groupSel,
        dietGroupId: groupSel,
        dishes: dishCount,
        allergyConflicts: 0,
        estimatedCost: menu.estimatedCost,
        costCeiling: menu.costCeiling,
        createdBy: menu.createdBy,
        createdBySupplierId: menu.createdBySupplierId,
        servings: totalPortions,
        reason: nextReason,
        createdAt: new Date().toLocaleString("vi-VN"),
        blocksData: mergedBlocks,
      },
      { onSuccess: done }
    );
  }

  const pending = createMenu.isPending || updateMenu.isPending;

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>
            {isEdit ? "Sửa" : "Tạo"} thực đơn riêng cho nhóm chế độ ăn — {grade}
          </DialogTitle>
          <DialogDescription>
            Suất theo chế độ ăn là <b>một bữa trọn bộ</b> thay cho bữa chuẩn của khối
            (không phải thay từng món). Chọn ngày → buổi → nhóm → bộ món từ thư viện.
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Chưa thể lưu">{error}</Alert>}
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-3">
            <div className="space-y-1.5">
              <Label htmlFor="cs-grade">1. Khối *</Label>
              <Select
                id="cs-grade"
                value={grade}
                onChange={(e) => changeGrade(e.target.value)}
                disabled={isEdit}
              >
                {gradeLevels.map((g) => (
                  <option key={g}>{g}</option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cs-menu">2. Thực đơn ngày trong tuần *</Label>
              <Select
                id="cs-menu"
                value={menuId}
                onChange={(e) => setMenuId(e.target.value)}
                disabled={isEdit}
              >
                <option value="">
                  {weekMenus.length === 0
                    ? `— ${grade} chưa có thực đơn trong tuần này —`
                    : "— Chọn thực đơn —"}
                </option>
                {weekMenus.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.week} · {m.id}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cs-vmeal">3. Buổi ăn thay trọn bộ *</Label>
              <Select
                id="cs-vmeal"
                value={variantMeal}
                onChange={(e) => setVariantMeal(e.target.value as MealBlock["mealType"])}
                disabled={!menu || isEdit}
              >
                <option value="">— Chọn buổi ăn —</option>
                {(menu?.blocksData ?? []).map((b) => (
                  <option key={b.mealType} value={b.mealType}>
                    {MEAL_LABEL[b.mealType]} ({b.dishes.length} món của khối)
                  </option>
                ))}
              </Select>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="cs-group">4. Nhóm chế độ ăn *</Label>
              <Select
                id="cs-group"
                value={groupSel}
                onChange={(e) => changeGroup(e.target.value)}
                disabled={eligibleGroups.length === 0 || isEdit}
              >
                <option value="">
                  {eligibleGroups.length === 0
                    ? `— ${grade} chưa có học sinh thuộc nhóm chế độ ăn nào —`
                    : "— Chọn nhóm —"}
                </option>
                {eligibleGroups.map((g) => (
                  <option key={g.id} value={g.id}>
                    {g.label} ({g.members.length} HS {grade})
                  </option>
                ))}
              </Select>
              <p className="text-xs text-muted-foreground">
                Chỉ liệt kê nhóm có học sinh ở {grade}. Nhóm chế độ ăn phát sinh từ
                hồ sơ học sinh đã Y tế xác minh (BR-065/066) — cần nhóm mới thì khai
                ở hồ sơ học sinh, không tạo tại đây.
              </p>
            </div>
            {members.length > 0 && (
              <div className="space-y-1.5">
                <Label>Phạm vi áp dụng ({grade})</Label>
                <p className="rounded-lg border bg-muted/40 px-3 py-2 text-sm text-muted-foreground">
                  {[...new Set(members.map((m) => m.class))].sort().join(", ")} ·{" "}
                  {members.length} suất
                </p>
              </div>
            )}
          </div>

          <div className="rounded-lg border border-warning/60 bg-warning/5 p-4">
            <p className="mb-1 text-sm font-semibold">5. Bộ món của suất trọn bộ</p>
            <p className="mb-3 text-xs text-muted-foreground">
              Chọn từ Thư viện món — dinh dưỡng lấy thẳng từ đó nên tổng kcal/đạm
              của suất này kiểm được ngay, không phụ thuộc nhập tay.
            </p>
            <div className="mb-3 grid gap-2 sm:grid-cols-[130px_1fr]">
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground">Số suất *</Label>
                <Input
                  type="number"
                  min={1}
                  value={subPortions}
                  onChange={(e) => setSubPortions(e.target.value)}
                  placeholder={members.length > 0 ? String(members.length) : "VD: 1"}
                />
              </div>
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground">Thêm món vào suất</Label>
                <Select
                  value=""
                  onChange={(e) => {
                    const v = e.target.value;
                    if (v && !variantDishIds.includes(v)) {
                      setVariantDishIds([...variantDishIds, v]);
                    }
                  }}
                  aria-label="Thêm món từ thư viện"
                >
                  <option value="">— Chọn món từ thư viện —</option>
                  {dishLibrary
                    .filter((d) => !variantDishIds.includes(d.id))
                    .map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.category} — {d.name} ({d.kcal} kcal)
                      </option>
                    ))}
                </Select>
              </div>
            </div>

            {variantDishIds.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa chọn món nào.</p>
            ) : (
              <ul className="mb-3 space-y-1">
                {variantDishIds.map((id) => {
                  const d = dishLibrary.find((x) => x.id === id);
                  return (
                    <li key={id} className="flex items-center gap-2 text-sm">
                      <Soup className="size-3.5 shrink-0 text-warning" />
                      <span className="font-medium">{d?.name ?? id}</span>
                      <span className="text-xs text-muted-foreground">
                        {d?.kcal} kcal · {d?.protein}g đạm
                      </span>
                      <button
                        type="button"
                        className="ml-auto text-xs underline text-muted-foreground"
                        onClick={() =>
                          setVariantDishIds(variantDishIds.filter((x) => x !== id))
                        }
                      >
                        Bỏ
                      </button>
                    </li>
                  );
                })}
              </ul>
            )}

            {variantDishIds.length > 0 && (
              <div className="mb-3 rounded-lg border bg-card p-3 text-sm">
                <p className="mb-1 font-medium">
                  Tổng suất này: {variantNut.kcal} kcal · {variantNut.protein}g đạm ·{" "}
                  {variantNut.carb}g bột đường · {variantNut.fat}g béo
                </p>
                <p className="text-xs text-muted-foreground">
                  Chuẩn bữa trưa 6–11 tuổi (BR-062): {NUTRITION_TARGET.kcal} kcal ·{" "}
                  {NUTRITION_TARGET.protein}g đạm.{" "}
                  {variantNut.kcal < NUTRITION_TARGET.kcal * 0.9
                    ? "Suất này THẤP hơn chuẩn — cân nhắc bổ sung món."
                    : variantNut.kcal > NUTRITION_TARGET.kcal * 1.1
                      ? "Suất này CAO hơn chuẩn."
                      : "Đạt chuẩn."}
                </p>
              </div>
            )}

            {variantConflicts.length > 0 && (
              <Alert variant="danger" className="mb-3" title="Suất trọn bộ chạm dị ứng của nhóm">
                Bộ món chứa <b>{variantConflicts.join(", ")}</b> — trùng hồ sơ dị ứng
                đã xác minh của học sinh nhóm &quot;{groupLabel}&quot;. Đổi món khác
                (BR-063/065).
              </Alert>
            )}

            <div className="mb-3 space-y-1">
              <Label className="text-xs text-muted-foreground">
                Ghi chú chế biến riêng (BR-082)
              </Label>
              <Textarea
                className="min-h-[40px]"
                value={subNote}
                onChange={(e) => setSubNote(e.target.value)}
                placeholder="VD: giảm tinh bột, bỏ món ngọt, nấu nồi riêng…"
              />
            </div>

            <label className="flex cursor-pointer items-start gap-2 text-sm font-medium">
              <input
                type="checkbox"
                className="mt-0.5 size-4 accent-primary"
                checked={separatePrep}
                onChange={(e) => setSeparatePrep(e.target.checked)}
              />
              Xác nhận suất đặc biệt được CHẾ BIẾN RIÊNG BIỆT, DỤNG CỤ RIÊNG * (BR-082)
            </label>
          </div>

          {menu && supplemental && (
            <div className="space-y-1.5 rounded-lg border border-warning/40 bg-warning/5 p-3">
              <Label htmlFor="mv-reason">
                Lý do phát sinh muộn * — thực đơn {menu.id} đã{" "}
                <Badge variant="warning">{String(menu.status)}</Badge>
              </Label>
              <Textarea
                id="mv-reason"
                rows={2}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="VD: HS Nguyễn Văn A nhập học ngày 21/07, Y tế xác minh chế độ ăn chay ngày 22/07"
              />
              <p className="text-xs text-muted-foreground">
                Suất này KHÔNG nằm trong thực đơn Ban giám hiệu đã duyệt, nên phải chờ
                duyệt bổ sung mới được nấu. Nếu thực đơn còn nháp thì không cần bước này —
                khai thẳng trong màn lập thực đơn là gọn hơn.
              </p>
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={pending || variantConflicts.length > 0} onClick={submit}>
            {isEdit ? "Lưu thay đổi" : "Tạo thực đơn riêng cho nhóm"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
