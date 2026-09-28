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
import { Input, Label, Select } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  INGREDIENT_DATALIST_ID,
  IngredientCatalogDatalist,
  IngredientEditor,
  emptyIngredient,
  type IngredientRow,
  type PersistedMenu,
} from "@/components/app/menu-form";
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import {
  ALLERGENS,
  dishIdOfName,
  dishLibraryAllergens,
  type Dish,
} from "@/lib/mock-data";

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

const CATEGORIES: Dish["category"][] = [
  "Món chính",
  "Món mặn",
  "Canh",
  "Rau",
  "Bữa phụ",
];

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
}

function draftOf(dish?: Dish): DishDraft {
  return {
    name: dish?.name ?? "",
    category: dish?.category ?? "Món mặn",
    kcal: dish ? String(dish.kcal) : "",
    protein: dish ? String(dish.protein) : "",
    carb: dish ? String(dish.carb) : "",
    fat: dish ? String(dish.fat) : "",
    ingredients: dish?.ingredients.length
      ? dish.ingredients.map((r) => ({ ...r }))
      : [emptyIngredient()],
    manualAllergens: [...(dish?.manualAllergens ?? [])],
    defaultSubstituteId: dish?.defaultSubstituteId ?? "",
  };
}

/* ================= Dialog: tạo / sửa món ================= */

function DishDialog({
  dish,
  cloneOf,
  existing,
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
  onClose: () => void;
}) {
  const create = useCreate<Partial<Dish>>("dishes");
  const update = useUpdate<{ id: string } & Partial<Dish>>("dishes");
  const [d, setD] = useState<DishDraft>(() => {
    if (dish) return draftOf(dish);
    if (!cloneOf) return draftOf();
    const base = draftOf(cloneOf);
    // Tên phải khác vì thư viện chặn trùng tên — gợi ý sẵn để người dùng sửa.
    return { ...base, name: `${cloneOf.name} (bản sao)` };
  });
  const [error, setError] = useState("");

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
    };

    if (dish) {
      update.mutate(
        { id: dish.id, ...fields },
        {
          onSuccess: () => {
            toast.success(`Đã cập nhật món "${name}" trong thư viện.`);
            onClose();
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
          toast.success(`Đã thêm "${name}" vào thư viện — chọn được ngay khi lập thực đơn.`);
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
        <IngredientCatalogDatalist />
        {error && <Alert variant="danger" title="Chưa thể lưu">{error}</Alert>}
        <div className="space-y-4">
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
                  <option key={c}>{c}</option>
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
              datalistId={INGREDIENT_DATALIST_ID}
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
              <Select
                className="h-8 w-44"
                value=""
                onChange={(e) => {
                  const v = e.target.value;
                  if (v && !d.manualAllergens.includes(v)) {
                    patch({ manualAllergens: [...d.manualAllergens, v] });
                  }
                }}
                aria-label="Thêm tác nhân dị ứng"
              >
                <option value="">+ Thêm tác nhân…</option>
                {ALLERGENS.filter((a) => !allergens.includes(a)).map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </Select>
            </div>
          </div>

          {/* G10 — món thay thế mặc định: khai MỘT LẦN ở thư viện, mọi thực đơn
              (khai tay hoặc áp Thực đơn mẫu) tự điền sẵn suất dị ứng từ đây,
              không phải gõ lại "Gà rang gừng → Thịt rim" mỗi tuần. */}
          <div className="space-y-1.5">
            <Label htmlFor="d-sub" className="text-xs text-muted-foreground">
              Món thay thế mặc định khi món này chạm dị ứng (BR-063)
            </Label>
            <Select
              id="d-sub"
              value={d.defaultSubstituteId}
              onChange={(e) => patch({ defaultSubstituteId: e.target.value })}
            >
              <option value="">— Chưa khai (lập thực đơn sẽ phải khai tay) —</option>
              {existing
                .filter((x) => x.isActive && x.id !== dish?.id)
                .map((x) => (
                  <option key={x.id} value={x.id}>{x.name}</option>
                ))}
            </Select>
            {(() => {
              const sub = existing.find((x) => x.id === d.defaultSubstituteId);
              if (!sub) return null;
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
                <option key={c}>{c}</option>
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
                      </TableCell>
                      <TableCell className="text-muted-foreground">{d.category}</TableCell>
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
