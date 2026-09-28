"use client";

import { useState } from "react";
import { BookOpen, Copy, Pencil, Plus, Replace, Search, X } from "lucide-react";
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
import { Input, Label, SearchableSelect, Select, Textarea } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  IngredientEditor,
  emptyIngredient,
  type IngredientRow,
  type PersistedMenu,
} from "@/components/app/menu-form";
import { useCreate, usePagedList, usePlainList, useUpdate, useUploadMutation } from "@/lib/api/hooks";
import type { DishDto, IngredientDto, PartnerDto, SupplierProductDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { supplierProductDtosToMock, type SupplierProduct } from "@/lib/supplier-product-adapter";
import {
  ALLERGENS,
  categoryLabel,
  DISH_CATEGORIES,
  dishCostPerServing,
  dishIdOfName,
  dishLibraryAllergens,
  substituteCostDelta,
  type Dish,
  type IngredientCatalogRef,
} from "@/lib/domain-utils";

/**
 * Thư viện món (dish master) — Đợt B, 21/07/2026.
 *
 * Trước đây `DishForm.name` là chuỗi tự do: mỗi lần lập thực đơn phải gõ lại
 * thành phần + định lượng + kcal, món chỉ nối với nhau bằng TÊN nên thống kê và
 * truy xuất đều lệch. Màn này là nơi khai món MỘT LẦN rồi dùng lại ở mọi thực đơn.
 *
 * BR-069: thành phần + định lượng · BR-076: `ingredientId` là khoá join về catalog
 * nguyên liệu · BR-063: allergen DẪN XUẤT từ thành phần, không phụ thuộc nhập tay.
 */

const CATEGORIES = DISH_CATEGORIES;

interface DishDraft {
  name: string;
  category: Dish["category"];
  kcal: string;
  protein: string;
  carb: string;
  fat: string;
  ingredients: IngredientRow[];
  manualAllergens: string[];
  /** G10: món thay thế mặc định khi món này chạm dị ứng — "" = chưa khai. */
  defaultSubstituteId: string;
  /** Nối thật 05/08/2026 — mô tả + hướng dẫn chế biến, không bắt buộc. */
  description: string;
  cookingInstructions: string;
}

function draftOf(dish?: Dish): DishDraft {
  return {
    name: dish?.name ?? "",
    category: dish?.category ?? "MON_MAN",
    kcal: dish ? String(dish.kcal) : "",
    protein: dish ? String(dish.protein) : "",
    carb: dish ? String(dish.carb) : "",
    fat: dish ? String(dish.fat) : "",
    // Backend giờ trả `quantity` là số (PROPOSED §Phase B.8, trước là chuỗi) — ép về
    // chuỗi cho state form (input điều khiển), cùng khuôn `kcal: String(dish.kcal)` ở trên.
    ingredients: dish?.ingredients.length
      ? dish.ingredients.map((r) => ({ ...r, quantity: String(r.quantity ?? "") }))
      : [emptyIngredient()],
    manualAllergens: [...(dish?.manualAllergens ?? [])],
    defaultSubstituteId: dish?.defaultSubstituteId ?? "",
    description: dish?.description ?? "",
    cookingInstructions: dish?.cookingInstructions ?? "",
  };
}

/* ================= Dialog: tạo / sửa món ================= */

function DishDialog({
  dish,
  cloneOf,
  existing,
  ingredientCatalog,
  supplierProducts,
  partners,
  onClose,
}: {
  /** undefined = tạo mới. */
  dish?: Dish;
  /**
   * NHÂN BẢN: mở form đã điền sẵn theo món này nhưng TẠO MỚI khi lưu.
   * Học từ hệ thống bán trú Hà Nội — họ nêu thẳng lý do: "để người dùng không phải
   * nhập lại từng thực phẩm khi thêm mới món ăn". Món trong trường thường chỉ khác
   * nhau một hai nguyên liệu (canh bí đỏ nấu tôm / nấu thịt băm / chay), nhập lại
   * cả BOM mỗi lần vừa mất thời gian vừa là nguồn của lệch định lượng.
   */
  cloneOf?: Dish;
  existing: Dish[];
  /**
   * 14/08/2026 (Nam) — "món thay thế có so tương quan chi phí chưa?" Dùng LẠI đúng
   * `dishCostPerServing()`/`substituteCostDelta()` đã có sẵn cho suất thay thế TRONG
   * thực đơn (`menu-form.tsx`) — chỉ khác nơi gọi (thư viện món thay vì thực đơn cụ
   * thể). Đây là ƯỚC TÍNH mềm (giá NCC rẻ nhất còn Approved), không phải giá chốt.
   */
  ingredientCatalog: IngredientCatalogRef[];
  supplierProducts: SupplierProduct[];
  partners: { id?: string; status?: string }[];
  onClose: () => void;
}) {
  const create = useCreate<Partial<Dish>>("dishes");
  const update = useUpdate<{ id: string } & Partial<Dish>>("dishes");
  const uploadImage = useUploadMutation<DishDto>("dishes", "post", { handledError: true });
  const [d, setD] = useState<DishDraft>(() => {
    if (dish) return draftOf(dish);
    if (!cloneOf) return draftOf();
    const base = draftOf(cloneOf);
    // Tên phải khác vì thư viện chặn trùng tên — gợi ý sẵn để người dùng sửa.
    return { ...base, name: `${cloneOf.name} (bản sao)` };
  });
  const [error, setError] = useState("");
  // Ảnh chỉ upload được SAU khi món đã có mã (POST /dishes/{code}/image) — với món MỚI,
  // chọn file trước rồi submit() sẽ upload NGAY SAU khi tạo xong (dùng id vừa sinh).
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(dish?.imageUrl ?? null);

  const patch = (p: Partial<DishDraft>) => setD((prev) => ({ ...prev, ...p }));
  const allergens = dishLibraryAllergens({
    ingredients: d.ingredients,
    manualAllergens: d.manualAllergens,
  });
  const autoAllergens = dishLibraryAllergens({
    ingredients: d.ingredients,
    manualAllergens: [],
  });
  const isPending = create.isPending || update.isPending;

  function submit() {
    const name = d.name.trim();
    if (!name) {
      setError("Nhập tên món.");
      return;
    }
    const clash = existing.find(
      (x) => x.id !== dish?.id && x.name.trim().toLowerCase() === name.toLowerCase()
    );
    if (clash) {
      setError(`Thư viện đã có món "${clash.name}" — sửa món đó thay vì tạo bản trùng tên.`);
      return;
    }
    if (!(Number(d.kcal) > 0)) {
      setError("Nhập kcal / suất (BR-062).");
      return;
    }
    const rows = d.ingredients.filter((r) => r.name.trim());
    if (rows.length === 0) {
      setError("Khai báo thành phần của món (BR-069).");
      return;
    }
    /*
     * CHẶN nguyên liệu ngoài danh mục, không chỉ cảnh báo.
     *
     * Cả chuỗi mua hàng nối bằng `ingredientId`: thiếu khoá là món không sinh được
     * nhu cầu xuất kho, FEFO báo thiếu hàng vĩnh viễn, không tra được lô khi điều
     * tra sự cố và không có báo giá để tính chi phí. Thư viện đã có 8 món dính lỗi
     * này (Hành lá, Hẹ, Súp lơ, Dứa, Me, Xương lợn, Phô mai lát, Đậu xanh) — im
     * lặng suốt vì màn hình chỉ cảnh báo rồi vẫn cho lưu.
     */
    const offCatalog = rows.filter((r) => !r.ingredientId).map((r) => r.name.trim());
    if (offCatalog.length > 0) {
      setError(
        `Nguyên liệu ngoài danh mục: ${offCatalog.join(", ")}. Chọn từ danh mục (gõ để tìm) — hoặc đề nghị bổ sung vào danh mục trước, vì thiếu khoá thì món này không đặt mua và không truy xuất được lô.`
      );
      return;
    }
    // Không định lượng thì món này vào thực đơn sẽ không sinh được nhu cầu xuất kho
    if (rows.some((r) => !(Number(r.quantity) > 0))) {
      setError("Mỗi thành phần phải có định lượng cho 1 suất — thiếu định lượng thì kho không tính được nhu cầu.");
      return;
    }
    setError("");

    const fields: Partial<Dish> = {
      name,
      category: d.category,
      kcal: Number(d.kcal),
      protein: Number(d.protein) || 0,
      carb: Number(d.carb) || 0,
      fat: Number(d.fat) || 0,
      ingredients: rows,
      manualAllergens: d.manualAllergens,
      // "" (bỏ chọn) lưu thành undefined để record sạch — spread ghi đè được giá trị cũ.
      defaultSubstituteId: d.defaultSubstituteId || undefined,
      description: d.description.trim() || undefined,
      cookingInstructions: d.cookingInstructions.trim() || undefined,
    };

    function uploadImageIfAny(dishId: string, onDone: () => void) {
      if (!imageFile) {
        onDone();
        return;
      }
      uploadImage.mutate(
        { path: `${dishId}/image`, parts: { file: imageFile } },
        {
          onSuccess: onDone,
          onError: (err) => {
            toast.error(`Đã lưu món nhưng tải ảnh thất bại: ${apiErrorMessage(err, "lỗi không rõ")}`);
            onDone();
          },
        }
      );
    }

    if (dish) {
      update.mutate(
        { id: dish.id, ...fields },
        {
          onSuccess: () => {
            uploadImageIfAny(dish.id, () => {
              toast.success(`Đã cập nhật món "${name}" trong thư viện.`);
              onClose();
            });
          },
        }
      );
      return;
    }
    // id dạng slug để link /parent/nutrition/[dishId] còn đọc được
    const id = dishIdOfName(name, existing.map((x) => x.id));
    create.mutate(
      { id, isActive: true, ...fields },
      {
        onSuccess: () => {
          uploadImageIfAny(id, () => {
            toast.success(`Đã thêm "${name}" vào thư viện — chọn được ngay khi lập thực đơn.`);
            onClose();
          });
        },
      }
    );
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>
            {dish
              ? `Sửa món — ${dish.name}`
              : cloneOf
                ? `Nhân bản từ "${cloneOf.name}"`
                : "Thêm món vào thư viện"}
          </DialogTitle>
          <DialogDescription>
            Khai một lần, dùng lại ở mọi thực đơn. Tác nhân dị ứng được suy từ
            thành phần nên chọn nguyên liệu từ danh mục là an toàn nhất (BR-063/076).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Chưa thể lưu">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="d-image" className="text-xs text-muted-foreground">
              Ảnh minh hoạ món (jpeg/png/webp, không bắt buộc)
            </Label>
            <div className="flex items-center gap-3">
              {imagePreview && (
                // eslint-disable-next-line @next/next/no-img-element -- ảnh do người dùng tải lên, không phải asset tĩnh
                <img
                  src={imagePreview}
                  alt="Xem trước ảnh món"
                  className="size-16 rounded-lg border object-cover"
                />
              )}
              <Input
                id="d-image"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={(e) => {
                  const file = e.target.files?.[0] ?? null;
                  setImageFile(file);
                  setImagePreview(file ? URL.createObjectURL(file) : (dish?.imageUrl ?? null));
                }}
              />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="d-name">Tên món *</Label>
              <Input
                id="d-name"
                value={d.name}
                onChange={(e) => patch({ name: e.target.value })}
                placeholder="VD: Cá basa sốt cà chua"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="d-cat">Phân loại *</Label>
              <Select
                id="d-cat"
                value={d.category}
                onChange={(e) => patch({ category: e.target.value as Dish["category"] })}
              >
                {CATEGORIES.map((c) => (
                  <option key={c.value} value={c.value}>{c.label}</option>
                ))}
              </Select>
            </div>
          </div>
          <p className="-mt-2 text-xs text-muted-foreground">
            Nhà cung cấp không khai ở đây — hệ thống tự suy từ nguyên liệu bên dưới (mỗi
            nguyên liệu nối về nhà cung cấp đang bán nó), nên luôn khớp thực tế. Món do
            đơn vị suất ăn của bạn chế biến.
          </p>

          <div className="grid gap-2 sm:grid-cols-4">
            {(
              [
                ["kcal", "Kcal / suất *"],
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
                  value={d[field]}
                  onChange={(e) => patch({ [field]: e.target.value } as Partial<DishDraft>)}
                />
              </div>
            ))}
          </div>

          <div className="space-y-1.5">
            <Label className="text-xs text-muted-foreground">
              Thành phần + định lượng cho 1 SUẤT * (BR-069)
            </Label>
            <IngredientEditor
              rows={d.ingredients}
              onChange={(rows) => patch({ ingredients: rows })}
            />
          </div>

          <div className="space-y-1.5">
            <Label className="text-xs text-muted-foreground">
              Tác nhân dị ứng — auto từ thành phần, thêm tay cho nguyên liệu ngoài danh mục
            </Label>
            <div className="flex flex-wrap items-center gap-1.5">
              {autoAllergens.map((a) => (
                <Badge key={a} variant="danger">{a} (auto)</Badge>
              ))}
              {d.manualAllergens
                .filter((a) => !autoAllergens.includes(a))
                .map((a) => (
                  <Badge key={a} variant="warning">
                    {a}
                    <button
                      type="button"
                      className="ml-1 inline-flex"
                      onClick={() =>
                        patch({ manualAllergens: d.manualAllergens.filter((m) => m !== a) })
                      }
                      aria-label={`Bỏ ${a}`}
                    >
                      <X className="size-3" />
                    </button>
                  </Badge>
                ))}
              {/* SearchableSelect (13/08/2026) — cùng lý do ở `AllergenPicker` của
                  menu-form: danh mục tác nhân dài, gõ tìm nhanh hơn cuộn. */}
              <div className="w-44">
                <SearchableSelect
                  value=""
                  onChange={(v) => {
                    if (v && !d.manualAllergens.includes(v)) {
                      patch({ manualAllergens: [...d.manualAllergens, v] });
                    }
                  }}
                  options={ALLERGENS.filter((a) => !allergens.includes(a))}
                  placeholder="+ Thêm tác nhân…"
                  searchPlaceholder="Tìm tác nhân…"
                />
              </div>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="d-desc" className="text-xs text-muted-foreground">
                Mô tả món (không bắt buộc)
              </Label>
              <Textarea
                id="d-desc"
                rows={3}
                value={d.description}
                onChange={(e) => patch({ description: e.target.value })}
                placeholder="VD: Cá basa phi lê áp chảo, sốt cà chua chua ngọt"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="d-cook" className="text-xs text-muted-foreground">
                Hướng dẫn chế biến (không bắt buộc)
              </Label>
              <Textarea
                id="d-cook"
                rows={3}
                value={d.cookingInstructions}
                onChange={(e) => patch({ cookingInstructions: e.target.value })}
                placeholder="VD: Ướp cá 15 phút, áp chảo 2 mặt, om cùng sốt cà chua 5 phút"
              />
            </div>
          </div>

          {/* G10 — món thay thế mặc định: khai MỘT LẦN ở thư viện, mọi thực đơn
              (khai tay hoặc áp Thực đơn mẫu) tự điền sẵn suất dị ứng từ đây,
              không phải gõ lại "Gà rang gừng → Thịt rim" mỗi tuần. */}
          <div className="space-y-1.5">
            <Label className="text-xs text-muted-foreground">
              Món thay thế mặc định khi món này chạm dị ứng (BR-063)
            </Label>
            {/* SearchableSelect (13/08/2026): danh sách này là TOÀN BỘ thư viện món —
                vài trăm mục — nên `<select>` gốc buộc phải cuộn tay để tìm.
                14/08/2026 (Nam) — giới hạn CÙNG LOẠI món (chỉ món mặn thay được món mặn…):
                thay khác loại (VD canh → tráng miệng) không hợp lý về mặt bữa ăn, dù không
                chạm dị ứng nào. */}
            <SearchableSelect
              value={d.defaultSubstituteId}
              onChange={(v) => patch({ defaultSubstituteId: v })}
              options={existing
                .filter((x) => x.isActive && x.id !== dish?.id && x.category === d.category)
                .map((x) => ({ value: x.id, label: `${x.name} · ${categoryLabel(x.category)} · ${x.kcal} kcal` }))}
              placeholder="— Chưa khai (lập thực đơn sẽ phải khai tay) —"
              searchPlaceholder="Tìm món trong thư viện…"
            />
            {(() => {
              const sub = existing.find((x) => x.id === d.defaultSubstituteId);
              if (!sub) return null;
              if (sub.category !== d.category) {
                return (
                  <Alert variant="warning" title="Món thay thế khác loại món gốc">
                    &quot;{sub.name}&quot; là {categoryLabel(sub.category)}, khác loại{" "}
                    {categoryLabel(d.category)} của món gốc — dữ liệu khai từ trước khi có giới
                    hạn này. Chọn lại món cùng loại, hoặc bỏ chọn.
                  </Alert>
                );
              }
              const subAllergens = dishLibraryAllergens(sub);
              const overlap = subAllergens.filter((a) => allergens.includes(a));
              if (overlap.length > 0) {
                return (
                  <Alert variant="warning" title="Món thay thế cùng chạm tác nhân của món gốc">
                    &quot;{sub.name}&quot; cũng chứa {overlap.join(", ")} — không thay thế được
                    cho học sinh dị ứng đúng tác nhân đó. Chọn món khác.
                  </Alert>
                );
              }
              return (
                <p className="text-xs text-muted-foreground">
                  {subAllergens.length === 0
                    ? `"${sub.name}" không mang tác nhân dị ứng nào — thay thế an toàn.`
                    : `Lưu ý: "${sub.name}" vẫn mang ${subAllergens.join(", ")} — hệ thống sẽ kiểm tra lại với hồ sơ dị ứng của từng học sinh bị ảnh hưởng khi lập thực đơn.`}
                </p>
              );
            })()}
            {/*
              14/08/2026 (Nam) — cảnh báo MỀM chênh lệch chi phí (ước tính từ giá NCC rẻ
              nhất còn Approved, KHÔNG phải giá chốt) khi chọn món thay thế mặc định. Không
              chặn lưu — món trong trường thường phải đổi vì dị ứng/hết nguyên liệu bất kể
              đắt hơn bao nhiêu, cảnh báo chỉ để người soạn thực đơn biết trước.
            */}
            {(() => {
              const sub = existing.find((x) => x.id === d.defaultSubstituteId);
              if (!sub) return null;
              const own = dishCostPerServing(d, ingredientCatalog, supplierProducts, partners);
              const r = substituteCostDelta(d, sub, ingredientCatalog, supplierProducts, partners);
              if (!r.comparable) {
                return (
                  <p className="text-xs text-muted-foreground">
                    Chưa đủ báo giá NCC để ước tính chênh lệch chi phí với &quot;{sub.name}&quot;.
                  </p>
                );
              }
              return (
                <p className={r.delta > 0 ? "text-xs text-warning" : "text-xs text-muted-foreground"}>
                  Ước tính chi phí: &quot;{sub.name}&quot; <b>{r.substituteCost.toLocaleString("vi-VN")}₫/suất</b>{" "}
                  so với món gốc <b>{own.cost.toLocaleString("vi-VN")}₫/suất</b> ({r.delta > 0 ? "+" : ""}
                  {r.delta.toLocaleString("vi-VN")}₫) — theo giá NCC rẻ nhất còn hợp tác, không phải giá chốt.
                </p>
              );
            })()}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={isPending} onClick={submit}>
            {dish ? "Lưu thay đổi" : "Thêm vào thư viện"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ================= Trang chính ================= */

export default function DishLibraryPage() {
  const { data: page, isLoading } = usePagedList<Dish>("dishes", { pageSize: 200 });
  // Món đang nằm trong thực đơn nào — ngừng dùng không phá gì (thực đơn cũ vẫn
  // giữ dishId nên truy xuất còn nguyên), nhưng thực đơn CHƯA NỘP mà mất món
  // trong danh sách chọn thì lúc sửa lại người ta không hiểu vì sao.
  const { data: menusPage } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  // Dữ liệu ước tính chi phí món thay thế (14/08/2026) — cùng 3 nguồn menu-form.tsx đã
  // dùng cho suất thay thế trong thực đơn: danh mục nguyên liệu + báo giá NCC + đối tác.
  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");
  const { data: supplierProductsPage } = usePagedList<SupplierProductDto>("supplier-products", { pageSize: 300 });
  const supplierProducts = supplierProductDtosToMock(supplierProductsPage?.items ?? []);
  const { data: partnersPage } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const partners = partnersPage?.items ?? [];
  const menusUsingDish = (dishId: string) =>
    (menusPage?.items ?? []).filter((m) =>
      (m.blocksData ?? []).some((b) =>
        b.dishes.some((d) => d.dishId === dishId || dishIdOfName(d.name) === dishId)
      )
    );
  const dishes = page?.items ?? [];
  const update = useUpdate<{ id: string } & Partial<Dish>>("dishes");

  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const [showInactive, setShowInactive] = useState(false);
  const [editing, setEditing] = useState<Dish | null>(null);
  const [cloning, setCloning] = useState<Dish | null>(null);
  const [creating, setCreating] = useState(false);

  const rows = dishes
    .filter((d) => showInactive || d.isActive)
    .filter((d) => !category || d.category === category)
    .filter((d) => d.name.toLowerCase().includes(search.trim().toLowerCase()));

  /** Ngừng dùng thay cho xoá: thực đơn cũ vẫn tham chiếu `dishId` này. */
  function toggleActive(d: Dish) {
    const inUse = d.isActive
      ? menusUsingDish(d.id).filter((m) => m.status === "DRAFT" || m.status === "PENDING_APPROVAL")
      : [];
    update.mutate(
      { id: d.id, isActive: !d.isActive },
      {
        onSuccess: () => {
          if (!d.isActive) {
            toast.success(`Đã dùng lại "${d.name}".`);
            return;
          }
          toast.success(
            `Đã ngừng dùng "${d.name}" — không còn chọn được khi lập thực đơn mới.`
          );
          // Thực đơn đã công bố không cần nhắc: món cũ vẫn hiển thị và truy xuất
          // được. Chỉ thực đơn chưa chốt mới là việc còn phải làm.
          if (inUse.length > 0) {
            toast.warning(
              `${inUse.length} thực đơn chưa chốt đang dùng món này: ${inUse
                .map((m) => m.id)
                .join(", ")} — mở ra thay món trước khi nộp.`,
              { duration: 8000 }
            );
          }
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Thư viện món"
        description="Khai món một lần — thành phần, định lượng, dinh dưỡng — rồi chọn lại khi lập thực đơn (BR-069/BR-076)"
        actions={
          <Button onClick={() => setCreating(true)}>
            <Plus /> Thêm món
          </Button>
        }
      />

      <Card>
        <CardHeader className="flex-row flex-wrap items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            <BookOpen className="size-4" /> {rows.length} món
          </CardTitle>
          <div className="flex flex-wrap items-center gap-2">
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                className="h-8 w-52 pl-8"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm theo tên món…"
                aria-label="Tìm món"
              />
            </div>
            <Select
              className="h-8 w-36"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              aria-label="Lọc theo phân loại"
            >
              <option value="">Mọi phân loại</option>
              {CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>{c.label}</option>
              ))}
            </Select>
            <label className="flex cursor-pointer items-center gap-1.5 text-sm">
              <input
                type="checkbox"
                className="size-4 accent-primary"
                checked={showInactive}
                onChange={(e) => setShowInactive(e.target.checked)}
              />
              Hiện cả món đã ngừng dùng
            </label>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Món</TableHead>
                <TableHead>Phân loại</TableHead>
                <TableHead>Kcal / suất</TableHead>
                <TableHead>Thành phần</TableHead>
                <TableHead>Tác nhân dị ứng</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={6} className="py-8 text-center text-muted-foreground">
                    Đang tải thư viện món…
                  </TableCell>
                </TableRow>
              ) : rows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="py-8 text-center text-muted-foreground">
                    Không có món nào khớp bộ lọc.
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((d) => {
                  const allergens = dishLibraryAllergens(d);
                  // Nguyên liệu ngoài catalog → không truy xuất được lô khi có sự cố
                  const offCatalog = d.ingredients.filter((r) => !r.ingredientId).length;
                  return (
                    <TableRow key={d.id} className={d.isActive ? "" : "opacity-60"}>
                      <TableCell className="font-medium">
                        <div className="flex items-start gap-2">
                          {d.imageUrl && (
                            // eslint-disable-next-line @next/next/no-img-element -- ảnh do người dùng tải lên
                            <img
                              src={d.imageUrl}
                              alt={d.name}
                              className="size-9 shrink-0 rounded-md border object-cover"
                            />
                          )}
                          <div>
                        {d.name}
                        {!d.isActive && (
                          <Badge variant="neutral" className="ml-1.5">Ngừng dùng</Badge>
                        )}
                        {d.defaultSubstituteId && (
                          <span className="mt-0.5 flex items-center gap-1 text-xs font-normal text-muted-foreground">
                            <Replace className="size-3" />
                            Thay thế:{" "}
                            {dishes.find((x) => x.id === d.defaultSubstituteId)?.name ??
                              d.defaultSubstituteId}
                          </span>
                        )}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell className="text-muted-foreground">{categoryLabel(d.category)}</TableCell>
                      <TableCell>{d.kcal} kcal</TableCell>
                      <TableCell className="max-w-64">
                        <span className="text-sm text-muted-foreground">
                          {d.ingredients.map((r) => r.name).join(", ")}
                        </span>
                        {offCatalog > 0 && (
                          <Badge variant="warning" className="ml-1.5">
                            {offCatalog} ngoài danh mục
                          </Badge>
                        )}
                      </TableCell>
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
                          <Button variant="outline" size="sm" onClick={() => setEditing(d)}>
                            <Pencil /> Sửa
                          </Button>
                          <Button variant="outline" size="sm" onClick={() => setCloning(d)}>
                            <Copy /> Nhân bản
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            disabled={update.isPending}
                            onClick={() => toggleActive(d)}
                          >
                            {d.isActive ? "Ngừng dùng" : "Dùng lại"}
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
            Ngừng dùng thay cho xoá — thực đơn đã lập vẫn trỏ về món này qua{" "}
            <code>dishId</code>, xoá đi là mất truy xuất lịch sử.
          </p>
        </CardContent>
      </Card>

      {(creating || editing || cloning) && (
        <DishDialog
          dish={editing ?? undefined}
          cloneOf={cloning ?? undefined}
          existing={dishes}
          ingredientCatalog={ingredientCatalog}
          supplierProducts={supplierProducts}
          partners={partners}
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
