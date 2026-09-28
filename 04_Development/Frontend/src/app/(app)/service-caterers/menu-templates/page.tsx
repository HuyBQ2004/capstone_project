"use client";

import { useEffect, useState } from "react";
import { Copy, LayoutTemplate, Pencil, Plus, Replace, Search, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCreate, usePagedList, usePlainList, useSingle, useUpdate } from "@/lib/api/hooks";
import { useGradeId, useGrades } from "@/lib/grades";
import {
  MEAL_TYPE_LABEL,
  dishConflictsWithDiet,
  dishIdOfName,
  dishLibraryAllergens,
  type Dish,
  type MealType,
} from "@/lib/domain-utils";
import type { DietGroupDto, GradeSummaryDto, MenuTemplateDto } from "@/lib/api/types";
import { normalizeMenuTemplate, type MenuTemplate } from "@/lib/menu-template-adapter";
import type { DietGroup } from "@/lib/diet-groups";

/**
 * 06/08/2026 — danh mục nhóm chế độ ăn đặc biệt lấy từ `GET /diet-groups` (nguồn Y tế/vận
 * hành cấu hình thật trong DB), KHÔNG còn dùng hằng số `DIET_GROUPS` khai tay ở `mock-data.ts`
 * (phát hiện: mảng đó thiếu hẳn nhóm "Không gluten" đang có thật trong DB — chọn/lọc theo mock
 * sẽ không bao giờ cho phép tạo mẫu cho nhóm này). Field khớp 1-1 với type `DietGroup` (mock)
 * nên tái dùng nguyên `dishConflictsWithDiet()` — chỉ đổi NGUỒN dữ liệu, không đổi logic so khớp.
 */
function toDietGroup(g: DietGroupDto): DietGroup {
  return {
    id: g.id ?? "",
    label: g.label ?? g.id ?? "",
    matchDiet: g.matchDiet,
    excludeAllergens: g.excludeAllergens,
    excludeIngredientKeywords: g.excludeIngredientKeywords,
    note: g.note,
  };
}

/**
 * THỰC ĐƠN MẪU — G9 (review 25/07/2026 + gợi ý Minh).
 *
 * Một mẫu = MỘT BỘ MÓN CỦA MỘT BỮA, đặt tên gọi được: "Thực đơn tiêu chuẩn A =
 * Cơm trắng · Canh mồng tơi · Thịt kho hành". Không gắn ngày/thứ/buổi — nội dung
 * nằm ở mẫu, còn "mẫu nào vào ô nào" quyết định trên Lịch thực đơn tuần (nút
 * "Áp dụng mẫu" tại từng ô, ô đã áp hiện TÊN MẪU, bấm vào mới sổ toàn bộ bữa).
 *
 * Món tham chiếu Thư viện món qua `dishId` — mẫu KHÔNG chép nội dung món, nên
 * sửa BOM/kcal ở thư viện là mọi mẫu cập nhật theo; suất thay thế dị ứng tự đổ
 * từ `Dish.defaultSubstituteId` (G10) lúc áp mẫu, không phải khai lại mỗi tuần.
 */

const STANDARD_AUDIENCE = "ALL";
const MEAL_ORDER: MealType[] = ["BREAKFAST", "LUNCH", "SNACK"];

function audienceLabel(audience: string, groups: DietGroup[]): string {
  if (audience === STANDARD_AUDIENCE) return "Tiêu chuẩn";
  return groups.find((g) => g.id === audience)?.label ?? audience;
}

/* ================= SearchableSelect cho món ================= */

/**
 * Combobox tìm kiếm món — thay thế <Select> thuần: thư viện món có thể có hàng trăm
 * dòng, cuộn dài không tìm được. Gõ tên/danh mục → dropdown lọc live → click để thêm.
 */
function DishSearchableSelect({
  dishes,
  pickedIds,
  onPick,
}: {
  dishes: Dish[];
  /** Các dishId đã chọn rồi — ẩn khỏi kết quả tìm kiếm. */
  pickedIds: string[];
  onPick: (dishId: string) => void;
}) {
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);

  const available = dishes.filter((x) => x.isActive && !pickedIds.includes(x.id));
  const filtered = query.trim()
    ? available.filter(
        (x) =>
          x.name.toLowerCase().includes(query.trim().toLowerCase()) ||
          (x.category ?? "").toLowerCase().includes(query.trim().toLowerCase())
      )
    : available;

  function pick(dish: Dish) {
    onPick(dish.id);
    setQuery("");
    setOpen(false);
  }

  function handleBlur() {
    setTimeout(() => setOpen(false), 150);
  }

  return (
    <div className="relative">
      <div className="relative">
        <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground pointer-events-none" />
        <Input
          autoComplete="off"
          placeholder="Gõ tên hoặc danh mục món để tìm…"
          className="pl-8"
          value={query}
          onFocus={() => setOpen(true)}
          onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
          onBlur={handleBlur}
          aria-label="Tìm và thêm món vào mẫu"
        />
      </div>
      {open && (
        <ul
          role="listbox"
          className="absolute z-50 mt-1 max-h-64 w-full overflow-auto rounded-lg border border-input bg-card py-1 shadow-lg text-sm"
        >
          {filtered.length === 0 ? (
            <li className="px-3 py-2 text-muted-foreground">
              {available.length === 0
                ? "Tất cả món đã được chọn."
                : "Không tìm thấy món nào khớp."}
            </li>
          ) : (
            filtered.map((x) => (
              <li
                key={x.id}
                role="option"
                aria-selected={false}
                onMouseDown={() => pick(x)}
                className="flex cursor-pointer items-center justify-between gap-2 px-3 py-2 hover:bg-muted"
              >
                <span className="font-medium">{x.name}</span>
                <span className="text-xs text-muted-foreground shrink-0">
                  {x.category} · {x.kcal} kcal
                </span>
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  );
}

/* ================= Dialog: tạo / sửa mẫu ================= */

interface TemplateDraft {
  name: string;
  mealType: MealType;
  audience: string;
  /** "" = dùng chung mọi khối (mặc định, không đổi hành vi cũ). */
  grade: string;
  note: string;
  dishIds: string[];
}

function draftOf(tpl?: MenuTemplate): TemplateDraft {
  return {
    name: tpl?.name ?? "",
    mealType: tpl?.mealType ?? "LUNCH",
    audience: tpl?.audience ?? STANDARD_AUDIENCE,
    grade: tpl?.grade ?? "",
    note: tpl?.note ?? "",
    dishIds: [...(tpl?.dishIds ?? [])],
  };
}

function TemplateDialog({
  template,
  cloneOf,
  existing,
  dishes,
  dietGroups,
  gradeLevels,
  onClose,
}: {
  /** undefined = tạo mới. */
  template?: MenuTemplate;
  /** Nhân bản: form điền sẵn theo mẫu này nhưng TẠO MỚI khi lưu. */
  cloneOf?: MenuTemplate;
  existing: MenuTemplate[];
  dishes: Dish[];
  /** Danh mục THẬT từ `GET /diet-groups` (Y tế/vận hành cấu hình), không phải mock. */
  dietGroups: DietGroup[];
  /** Khối THẬT dẫn xuất từ cấu hình lớp học (`gradeLevelsOf(classrooms)`), không phải hằng số mock. */
  gradeLevels: string[];
  onClose: () => void;
}) {
  const create = useCreate<Partial<MenuTemplate>>("menu-templates");
  const update = useUpdate<{ id: string } & Partial<MenuTemplate>>("menu-templates");
  const [d, setD] = useState<TemplateDraft>(() => {
    if (template) return draftOf(template);
    if (!cloneOf) return draftOf();
    return { ...draftOf(cloneOf), name: `${cloneOf.name} (bản sao)` };
  });
  const [error, setError] = useState("");

  // 05_Menu_Grade_Diet_Coverage_Dish_Media_Design.md mục 1 — cảnh báo TRƯỚC dị ứng/chế độ ăn
  // của khối đang chọn, để cân nhắc món/suất phù hợp ngay lúc soạn mẫu, không phải đợi tới
  // lúc áp mẫu vào lịch mới phát hiện xung đột.
  /*
   * V44 — `GET /menus/grade-summary` khai `@RequestParam UUID gradeId`. Trước 07/08/2026
   * FE truyền nhãn ("Khối 3") nên endpoint **luôn 400**: cảnh báo dị ứng/chế độ ăn theo
   * khối chưa từng hiển thị được.
   */
  const dialogGradeId = useGradeId(d.grade || undefined);
  const { data: gradeSummary } = useSingle<GradeSummaryDto>(
    "menus/grade-summary",
    { gradeId: dialogGradeId },
    { enabled: Boolean(dialogGradeId) }
  );
  // 06/08/2026 — CHỈ hiện nhóm chế độ ăn ĐANG CÓ HS đã duyệt trong ĐÚNG khối đang chọn (trước
  // đây lấy từ `special-meals` toàn trường, không lọc theo khối — mẫu "Chay" vẫn hiện dù khối
  // đang chọn không có em nào ăn chay). Chưa chọn khối thì chưa biết cần nhóm nào, tạm hiện
  // cả danh mục (giữ hành vi cũ cho trường hợp "Dùng chung mọi khối").
  const activeDietGroups = d.grade
    ? dietGroups.filter((g) =>
        (gradeSummary?.dietGroups ?? []).some((dg) => dg.dietGroupCode === g.id)
      )
    : dietGroups;

  // Đổi khối mà nhóm đang chọn không còn hợp lệ với khối mới → về Tiêu chuẩn, tránh lưu nhầm
  // 1 mẫu audience riêng cho khối không hề có HS cần nhóm đó.
  useEffect(() => {
    if (d.audience === STANDARD_AUDIENCE) return;
    if (activeDietGroups.some((g) => g.id === d.audience)) return;
    setD((prev) => ({ ...prev, audience: STANDARD_AUDIENCE }));
    toast.info(`Khối "${d.grade || "dùng chung"}" hiện không có HS cần nhóm đã chọn — đã đổi về Tiêu chuẩn.`);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [d.grade, gradeSummary]);

  const patch = (p: Partial<TemplateDraft>) => setD((prev) => ({ ...prev, ...p }));
  const dishById = new Map(dishes.map((x) => [x.id, x]));
  const pickedDishes = d.dishIds
    .map((id) => dishById.get(id))
    .filter((x): x is Dish => x != null);
  const totalKcal = pickedDishes.reduce((sum, x) => sum + x.kcal, 0);
  const dietGroup =
    d.audience !== STANDARD_AUDIENCE ? dietGroups.find((g) => g.id === d.audience) : undefined;
  /** Món vướng chế độ ăn của chính mẫu (vd món thịt trong mẫu chay) — chặn lưu. */
  const dietConflicts = dietGroup
    ? pickedDishes
        .map((x) => ({ dish: x, reasons: dishConflictsWithDiet(x, dietGroup) }))
        .filter((c) => c.reasons.length > 0)
    : [];
  const isPending = create.isPending || update.isPending;

  function submit() {
    const name = d.name.trim();
    if (!name) {
      setError("Đặt tên mẫu — tên là khóa tìm kiếm khi áp mẫu vào lịch (VD: Thực đơn tiêu chuẩn A).");
      return;
    }
    const clash = existing.find(
      (x) => x.id !== template?.id && x.name.trim().toLowerCase() === name.toLowerCase()
    );
    if (clash) {
      setError(`Đã có mẫu "${clash.name}" — sửa mẫu đó thay vì tạo bản trùng tên.`);
      return;
    }
    if (d.dishIds.length === 0) {
      setError("Chọn ít nhất 1 món từ Thư viện món.");
      return;
    }
    if (dietConflicts.length > 0) {
      setError(
        `Món không hợp chế độ ăn "${audienceLabel(d.audience, dietGroups)}": ${dietConflicts
          .map((c) => `${c.dish.name} (${c.reasons.join(", ")})`)
          .join("; ")}.`
      );
      return;
    }
    setError("");

    const fields: Partial<MenuTemplate> = {
      name,
      mealType: d.mealType,
      audience: d.audience,
      // V44 — `CreateMenuTemplateRequest.gradeId` là UUID. Trước 07/08/2026 gửi nhãn
      // ("Khối 3"); vì field NULLABLE nên KHÔNG lỗi, chỉ lưu `grade_id = NULL` im lặng:
      // người dùng chọn khối, hệ thống lưu "dùng chung mọi khối".
      gradeId: dialogGradeId,
      dishIds: d.dishIds,
      note: d.note.trim() || undefined,
    };
    if (template) {
      update.mutate(
        { id: template.id, ...fields },
        {
          onSuccess: () => {
            toast.success(
              `Đã cập nhật mẫu "${name}" — ô lịch áp mẫu SAU thời điểm này dùng bộ món mới; ô đã áp trước đó giữ nguyên món đã đổ.`
            );
            onClose();
          },
        }
      );
      return;
    }
    const id = `TPL-${dishIdOfName(name, existing.map((x) => x.id.replace(/^TPL-/, "")))}`;
    create.mutate(
      { id, isActive: true, ...fields },
      {
        onSuccess: () => {
          toast.success(`Đã tạo mẫu "${name}" — chọn được ngay ở nút "Áp dụng mẫu" trên Lịch thực đơn tuần.`);
          onClose();
        },
      }
    );
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>
            {template
              ? `Sửa mẫu — ${template.name}`
              : cloneOf
                ? `Nhân bản từ "${cloneOf.name}"`
                : "Tạo thực đơn mẫu"}
          </DialogTitle>
          <DialogDescription>
            Một mẫu = bộ món của MỘT bữa, không gắn ngày/buổi — xếp mẫu nào vào ô nào
            quyết định trên Lịch thực đơn tuần. Món lấy từ Thư viện món để giữ truy
            xuất lô + dinh dưỡng (BR-069/076).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Chưa thể lưu">{error}</Alert>}
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-4">
            <div className="space-y-1.5">
              <Label htmlFor="t-name">Tên mẫu *</Label>
              <Input
                id="t-name"
                value={d.name}
                onChange={(e) => patch({ name: e.target.value })}
                placeholder="VD: Thực đơn tiêu chuẩn A"
              />
            </div>
            {/* Mẫu thuộc BỮA nào — ô lịch "+ Thêm thực đơn" chỉ liệt kê mẫu đúng bữa. */}
            <div className="space-y-1.5">
              <Label htmlFor="t-meal">Bữa *</Label>
              <Select
                id="t-meal"
                value={d.mealType}
                onChange={(e) => patch({ mealType: e.target.value as MealType })}
              >
                {MEAL_ORDER.map((m) => (
                  <option key={m} value={m}>{MEAL_TYPE_LABEL[m]}</option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="t-aud">Dành cho *</Label>
              <Select
                id="t-aud"
                value={d.audience}
                onChange={(e) => patch({ audience: e.target.value })}
              >
                <option value={STANDARD_AUDIENCE}>Tiêu chuẩn (cả khối)</option>
                {activeDietGroups.map((g) => (
                  <option key={g.id} value={g.id}>{g.label}</option>
                ))}
              </Select>
            </div>
            {/* Nối thật 05/08/2026 — nhu cầu dinh dưỡng khác nhau theo khối
                (05_Menu_Grade_Diet_Coverage_Dish_Media_Design.md mục 1). Trống = dùng chung. */}
            <div className="space-y-1.5">
              <Label htmlFor="t-grade">Khối</Label>
              <Select
                id="t-grade"
                value={d.grade}
                onChange={(e) => patch({ grade: e.target.value })}
              >
                <option value="">Dùng chung mọi khối</option>
                {gradeLevels.map((g) => (
                  <option key={g} value={g}>{g}</option>
                ))}
              </Select>
            </div>
          </div>

          {d.grade && gradeSummary && (gradeSummary.allergens?.length || gradeSummary.dietGroups?.length) ? (
            <Alert variant="warning" title={`Cảnh báo trước — ${d.grade}`}>
              <div className="space-y-1 text-sm">
                {(gradeSummary.allergens?.length ?? 0) > 0 && (
                  <p>
                    Dị ứng đã xác minh:{" "}
                    {gradeSummary.allergens!.map((a) => `${a.allergen} (${a.count} HS)`).join(", ")}
                  </p>
                )}
                {(gradeSummary.dietGroups?.length ?? 0) > 0 && (
                  <p>
                    Chế độ ăn đã duyệt:{" "}
                    {gradeSummary.dietGroups!
                      .map((g) => `${g.dietGroupLabel} (${g.students} HS)`)
                      .join(", ")}
                  </p>
                )}
              </div>
            </Alert>
          ) : null}

          <div className="space-y-1.5">
            <Label className="text-xs text-muted-foreground">
              Món của bữa * — chọn từ Thư viện món, theo thứ tự dọn
            </Label>
            {pickedDishes.length === 0 ? (
              <p className="rounded-lg border border-dashed p-3 text-sm text-muted-foreground">
                Chưa có món nào — chọn ở ô bên dưới.
              </p>
            ) : (
              <ul className="space-y-1.5">
                {pickedDishes.map((x) => {
                  const allergens = dishLibraryAllergens(x);
                  const sub = x.defaultSubstituteId
                    ? dishById.get(x.defaultSubstituteId)
                    : undefined;
                  const conflict = dietConflicts.find((c) => c.dish.id === x.id);
                  return (
                    <li
                      key={x.id}
                      className="flex flex-wrap items-center gap-1.5 rounded-lg border bg-card p-2 text-sm"
                    >
                      <span className="font-medium">{x.name}</span>
                      <span className="text-xs text-muted-foreground">
                        {x.category} · {x.kcal} kcal
                      </span>
                      {allergens.map((a) => (
                        <Badge key={a} variant="danger">{a}</Badge>
                      ))}
                      {/* G10: mẫu chứa món dị ứng vẫn ổn NẾU thư viện đã khai món
                          thay thế mặc định — áp mẫu sẽ tự đổ suất riêng. Chưa khai
                          thì cảnh báo ngay ở đây, đừng đợi đến lúc xếp lịch. */}
                      {allergens.length > 0 &&
                        (sub ? (
                          <span className="flex items-center gap-1 text-xs text-muted-foreground">
                            <Replace className="size-3" /> thay bằng {sub.name}
                          </span>
                        ) : (
                          <Badge variant="warning">Chưa có món thay thế mặc định</Badge>
                        ))}
                      {conflict && (
                        <Badge variant="danger">
                          Vướng {audienceLabel(d.audience, dietGroups)}: {conflict.reasons.join(", ")}
                        </Badge>
                      )}
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="ml-auto h-7 px-1.5"
                        aria-label={`Bỏ ${x.name}`}
                        onClick={() =>
                          patch({ dishIds: d.dishIds.filter((id) => id !== x.id) })
                        }
                      >
                        <Trash2 />
                      </Button>
                    </li>
                  );
                })}
              </ul>
            )}
            <DishSearchableSelect
              dishes={dishes}
              pickedIds={d.dishIds}
              onPick={(id) => patch({ dishIds: [...d.dishIds, id] })}
            />
            {pickedDishes.length > 0 && (
              <p className="text-xs text-muted-foreground">
                Tổng {totalKcal} kcal / suất · {pickedDishes.length} món. Định lượng +
                thành phần lấy theo Thư viện món tại thời điểm áp mẫu.
              </p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="t-note" className="text-xs text-muted-foreground">
              Ghi chú chế biến (tùy chọn)
            </Label>
            <Textarea
              id="t-note"
              rows={2}
              value={d.note}
              onChange={(e) => patch({ note: e.target.value })}
              placeholder="VD: nấu nồi riêng, không dùng chung muôi với món mặn"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={isPending} onClick={submit}>
            {template ? "Lưu thay đổi" : "Tạo mẫu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ================= Trang chính ================= */

export default function MenuTemplatesPage() {
  const { data: page, isLoading } = usePagedList<MenuTemplateDto>("menu-templates", {
    pageSize: 100,
  });
  const templates = (page?.items ?? []).map(normalizeMenuTemplate);
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishes = dishesPage?.items ?? [];
  const dishById = new Map(dishes.map((x) => [x.id, x]));
  const update = useUpdate<{ id: string } & Partial<MenuTemplate>>("menu-templates");
  // Danh mục THẬT (Y tế/vận hành cấu hình trong DB) — xem `toDietGroup()` phía trên.
  const { data: dietGroupDtos = [] } = usePlainList<DietGroupDto>("diet-groups");
  const dietGroups = dietGroupDtos.map(toDietGroup);
  // Khối THẬT dẫn xuất từ cấu hình lớp học — `usePagedList("classrooms")` như các
  // màn khác (`cook-sheet`, `menu-approval`, `meal-service`, `service-caterers/menus`).
  // Hằng seed `GRADE_LEVELS`/`CLASSROOMS` (luôn rỗng) đã xoá 07/08/2026.
  // Màn GHI ⇒ nguồn khối là `GET /grades` (V44) để mọi nhãn chọn được đều resolve ra UUID.
  const gradeLevels = useGrades().map((g) => g.name);

  const [search, setSearch] = useState("");
  const [mealFilter, setMealFilter] = useState("");
  const [audience, setAudience] = useState("");
  const [gradeFilter, setGradeFilter] = useState("");
  const [showInactive, setShowInactive] = useState(false);
  const [editing, setEditing] = useState<MenuTemplate | null>(null);
  const [cloning, setCloning] = useState<MenuTemplate | null>(null);
  const [creating, setCreating] = useState(false);

  // 06/08/2026 — lọc theo ĐÚNG khối đang chọn ở dropdown "Lọc theo khối" (cùng nguồn
  // `menus/grade-summary` dùng trong `TemplateDialog`), thay vì trước đây lấy nhóm chế độ ăn
  // duyệt TOÀN TRƯỜNG không phân biệt khối. Chưa chọn khối (hoặc chọn "Dùng chung") thì hiện
  // cả danh mục — không đủ thông tin để lọc.
  const gradeFilterId = useGradeId(gradeFilter && gradeFilter !== "SHARED" ? gradeFilter : undefined);
  const { data: gradeFilterSummary } = useSingle<GradeSummaryDto>(
    "menus/grade-summary",
    { gradeId: gradeFilterId },
    { enabled: Boolean(gradeFilterId) }
  );
  const activeDietGroups = gradeFilter && gradeFilter !== "SHARED"
    ? dietGroups.filter((g) =>
        (gradeFilterSummary?.dietGroups ?? []).some((dg) => dg.dietGroupCode === g.id)
      )
    : dietGroups;

  const rows = templates
    .filter((t) => showInactive || t.isActive)
    .filter((t) => !mealFilter || t.mealType === mealFilter)
    .filter((t) => !audience || t.audience === audience)
    .filter((t) => !gradeFilter || (gradeFilter === "SHARED" ? !t.grade : t.grade === gradeFilter))
    .filter((t) => t.name.toLowerCase().includes(search.trim().toLowerCase()))
    // Nhóm theo bữa cho dễ soát: Sáng → Trưa → Chiều, trong bữa xếp theo tên.
    .sort(
      (a, b) =>
        MEAL_ORDER.indexOf(a.mealType) - MEAL_ORDER.indexOf(b.mealType) ||
        a.name.localeCompare(b.name)
    );

  return (
    <>
      <PageHeader
        title="Thực đơn mẫu"
        description="Nơi DUY NHẤT lên món cho bữa ăn: ghép món từ Thư viện món thành thực đơn đặt tên (Tiêu chuẩn A/B/C, Ăn chay A1…) theo từng bữa — Lịch thực đơn tuần chỉ chọn thực đơn, không soạn món"
        actions={
          <Button onClick={() => setCreating(true)}>
            <Plus /> Tạo mẫu
          </Button>
        }
      />

      <Card>
        <CardHeader className="flex-row flex-wrap items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            <LayoutTemplate className="size-4" /> {rows.length} mẫu
          </CardTitle>
          <div className="flex flex-wrap items-center gap-2">
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                className="h-8 w-52 pl-8"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm theo tên mẫu…"
                aria-label="Tìm mẫu"
              />
            </div>
            <Select
              className="h-8 w-32"
              value={mealFilter}
              onChange={(e) => setMealFilter(e.target.value)}
              aria-label="Lọc theo bữa"
            >
              <option value="">Mọi bữa</option>
              {MEAL_ORDER.map((m) => (
                <option key={m} value={m}>{MEAL_TYPE_LABEL[m]}</option>
              ))}
            </Select>
            <Select
              className="h-8 w-44"
              value={audience}
              onChange={(e) => setAudience(e.target.value)}
              aria-label="Lọc theo chế độ ăn"
            >
              <option value="">Mọi chế độ ăn</option>
              <option value="ALL">Tiêu chuẩn</option>
              {activeDietGroups.map((g) => (
                <option key={g.id} value={g.id}>{g.label}</option>
              ))}
            </Select>
            <Select
              className="h-8 w-40"
              value={gradeFilter}
              onChange={(e) => setGradeFilter(e.target.value)}
              aria-label="Lọc theo khối"
            >
              <option value="">Mọi khối</option>
              <option value="SHARED">Dùng chung mọi khối</option>
              {gradeLevels.map((g) => (
                <option key={g} value={g}>{g}</option>
              ))}
            </Select>
            <label className="flex cursor-pointer items-center gap-1.5 text-sm">
              <input
                type="checkbox"
                className="size-4 accent-primary"
                checked={showInactive}
                onChange={(e) => setShowInactive(e.target.checked)}
              />
              Hiện cả mẫu đã ngừng dùng
            </label>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Mẫu</TableHead>
                <TableHead>Bữa</TableHead>
                <TableHead>Dành cho</TableHead>
                <TableHead>Khối</TableHead>
                <TableHead>Món của bữa</TableHead>
                <TableHead>Kcal / suất</TableHead>
                <TableHead>Tác nhân dị ứng</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={8} className="py-8 text-center text-muted-foreground">
                    Đang tải thực đơn mẫu…
                  </TableCell>
                </TableRow>
              ) : rows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="py-8 text-center text-muted-foreground">
                    Không có mẫu nào khớp bộ lọc.
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((t) => {
                  const tplDishes = t.dishIds
                    .map((id) => dishById.get(id))
                    .filter((x): x is Dish => x != null);
                  const missing = t.dishIds.length - tplDishes.length;
                  const totalKcal = tplDishes.reduce((sum, x) => sum + x.kcal, 0);
                  const allergens = [
                    ...new Set(tplDishes.flatMap((x) => dishLibraryAllergens(x))),
                  ];
                  return (
                    <TableRow key={t.id} className={t.isActive ? "" : "opacity-60"}>
                      <TableCell className="font-medium">
                        {t.name}
                        {!t.isActive && (
                          <Badge variant="neutral" className="ml-1.5">Ngừng dùng</Badge>
                        )}
                        {t.note && (
                          <span className="mt-0.5 block text-xs font-normal text-muted-foreground">
                            {t.note}
                          </span>
                        )}
                      </TableCell>
                      <TableCell>
                        <Badge variant="primary">{MEAL_TYPE_LABEL[t.mealType] ?? "—"}</Badge>
                      </TableCell>
                      <TableCell>
                        {t.audience === "ALL" ? (
                          <span className="text-muted-foreground">Tiêu chuẩn</span>
                        ) : (
                          <Badge variant="info">{audienceLabel(t.audience, dietGroups)}</Badge>
                        )}
                      </TableCell>
                      <TableCell>
                        {t.grade ? (
                          <Badge variant="primary">{t.grade}</Badge>
                        ) : (
                          <span className="text-muted-foreground">Dùng chung</span>
                        )}
                      </TableCell>
                      <TableCell className="max-w-72">
                        <span className="text-sm text-muted-foreground">
                          {tplDishes.map((x) => x.name).join(" · ")}
                        </span>
                        {missing > 0 && (
                          <Badge variant="warning" className="ml-1.5">
                            {missing} món không còn trong thư viện
                          </Badge>
                        )}
                      </TableCell>
                      <TableCell>{totalKcal} kcal</TableCell>
                      <TableCell className="max-w-44">
                        {allergens.length === 0 ? (
                          <Badge variant="success">Không có</Badge>
                        ) : (
                          <div className="flex flex-wrap gap-1">
                            {allergens.map((a) => (
                              <Badge key={a} variant="danger">{a}</Badge>
                            ))}
                          </div>
                        )}
                      </TableCell>
                      <TableCell>
                        <div className="flex justify-end gap-1.5">
                          <Button variant="outline" size="sm" onClick={() => setEditing(t)}>
                            <Pencil /> Sửa
                          </Button>
                          <Button variant="outline" size="sm" onClick={() => setCloning(t)}>
                            <Copy /> Nhân bản
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            disabled={update.isPending}
                            onClick={() =>
                              update.mutate(
                                { id: t.id, isActive: !t.isActive },
                                {
                                  onSuccess: () =>
                                    toast.success(
                                      t.isActive
                                        ? `Đã ngừng dùng "${t.name}" — không còn hiện ở nút "Áp dụng mẫu"; ô lịch đã áp trước đó không đổi.`
                                        : `Đã dùng lại "${t.name}".`
                                    ),
                                }
                              )
                            }
                          >
                            {t.isActive ? "Ngừng dùng" : "Dùng lại"}
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
          <p className="mt-2 text-xs text-muted-foreground">
            Mẫu không gắn ngày/buổi — áp mẫu nào vào ô nào làm trên Lịch thực đơn tuần.
            Ngừng dùng thay cho xoá: ô đã áp mẫu vẫn giữ tham chiếu để đối chiếu.
          </p>
        </CardContent>
      </Card>

      {(creating || editing || cloning) && (
        <TemplateDialog
          template={editing ?? undefined}
          cloneOf={cloning ?? undefined}
          existing={templates}
          dishes={dishes}
          dietGroups={dietGroups}
          gradeLevels={gradeLevels}
          onClose={() => {
            setCreating(false);
            setEditing(null);
            setCloning(null);
          }}
        />
      )}
    </>
  );
}
