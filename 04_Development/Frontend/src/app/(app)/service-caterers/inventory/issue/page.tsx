"use client";

import { Suspense, useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowLeft, PackageMinus, ShoppingCart } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useCreateAt, usePagedList, usePlainList, useSingle } from "@/lib/api/hooks";
import type { DishDto, IngredientDto, InventoryTransactionDto, IssuePlanDto, MenuDto, PlannedDishesDto } from "@/lib/api/types";
import { MEAL_TYPE_LABEL, toBaseQuantity, type MealType } from "@/lib/domain-utils";

/**
 * 29_IssueIngredientsWorkspace — xuất kho theo BỮA, phân bổ FEFO.
 *
 * Nối backend thật 05/08/2026: FEFO (chọn lô hạn gần nhất) tính SERVER-SIDE qua
 * `GET /inventory/issue-plan?serviceDate=&mealType=&demand[ingredientId]=qty`.
 *
 * Nối lại 05/08/2026 (Phase 2 — `04_Meal_Substitution_Portion_Pipeline_Design.md`) — nhu cầu
 * nguyên liệu (`demand`) đổi nguồn: TRƯỚC đây gộp từ `blocksData` của thực đơn × số suất GÕ
 * TAY theo từng khối (không biết món thay thế theo dị ứng của từng em). NAY gộp từ
 * `GET /student-distributions/planned-dishes?date=&mealType=` (bỏ `class` ⇒ gộp TOÀN TRƯỜNG
 * theo món, kể cả món thay thế — endpoint MỚI thêm ở Phase 2) × `dishes[].ingredients` (đã nối
 * thật, MF05) — đúng số suất THẬT sẽ phát (học sinh PENDING của đúng ngày/bữa), không phải kế
 * hoạch tĩnh của thực đơn. Bỏ hẳn ô "Số suất theo khối" (không còn ảnh hưởng gì tới nhu cầu).
 *
 * ⚠️ Đánh đổi đã biết: `IssuePlanLine` server trả PHẲNG theo nguyên liệu (không có breakdown
 * allocation theo TỪNG món — `dishId`/`dishName` luôn null, xem `InventoryService.issuePlan()`).
 * Cột "Theo món" hiển thị nhu cầu từng món (tính tại client từ `byDish`), nhưng KHÔNG còn hiện
 * lô nào phục vụ món nào — chỉ hiện tổng lô phân bổ cho cả nguyên liệu đó. `dishId` khi lập
 * phiếu xuất lấy từ món ĐẦU TIÊN dùng nguyên liệu đó (xấp xỉ đã biết, giữ nguyên từ trước).
 *
 * BR-075 FEFO · BR-077 cảnh báo thiếu kho · BR-205/BR-149 lô hết hạn/cách ly bị khóa.
 */

/**
 * Quy đổi số lượng từ đơn vị công thức (recipeUnit) sang đơn vị chuẩn của database (dbUnit)
 * dùng chung bảng tra cứu toBaseQuantity.
 */
function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function toIngredientDbQuantity(qty: number, recipeUnit: string, dbUnit: string): number {
  const base = toBaseQuantity(qty, recipeUnit);
  const dbBase = toBaseQuantity(1, dbUnit);
  if (base.unit !== dbBase.unit) {
    return qty;
  }
  return base.qty / dbBase.qty;
}

type IngredientDemand = {
  ingredientId: string;
  ingredientName: string;
  unit: string;
  total: number;
  perDish: { dishName: string; needed: number; isSubstitute: boolean }[];
  /** Món đầu tiên dùng nguyên liệu này — gán cho dòng phiếu xuất (xấp xỉ đã biết). */
  firstDishId: string;
};

function IssueIngredientsContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const qc = useQueryClient();
  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");
  /**
   * `POST /inventory/issue-slips` (SERVICE_CATERERS) — MỘT phiếu xuất cho cả bữa,
   * server tự ghi sổ `inventory_transactions` (ADR-M06-6).
   */
  const createIssueSlip = useCreateAt<{
    serviceDate: string;
    mealType: MealType;
    menuIds: string[];
    lines: { batchId: string; dishId: string; quantity: number; unit: string }[];
    note?: string;
  }>("inventory", "issue-slips");
  // Giao dịch kho SỐNG — chỉ dùng để biết bữa này ĐÃ có phiếu xuất chưa (chặn trừ tồn lần hai).
  const { data: txPage } = usePagedList<InventoryTransactionDto>("inventory-transactions", {
    pageSize: 500,
  });
  const liveTx = useMemo(() => txPage?.items ?? [], [txPage]);

  // Chỉ thực đơn ĐÃ CÔNG BỐ mới được xuất kho phục vụ — vẫn dùng để chọn NGÀY/BỮA có sẵn và
  // theo dõi "đã có phiếu xuất chưa" (menuId), KHÔNG còn dùng để tính demand (xem Javadoc trên).
  const { data: menusPage } = usePagedList<MenuDto>("menus", { pageSize: 200 });
  const menus = useMemo(
    () => (menusPage?.items ?? []).filter((m) => m.status === "PUBLISHED"),
    [menusPage]
  );
  const { data: dishesPage } = usePagedList<DishDto>("dishes", { pageSize: 500 });
  const dishByCode = useMemo(() => {
    const map = new Map<string, DishDto>();
    for (const d of dishesPage?.items ?? []) if (d.id) map.set(d.id, d);
    return map;
  }, [dishesPage]);

  // Quyết định của Nam, 10/08/2026 — xuất kho CHỈ cho ngày hôm nay (bếp xuất nguyên liệu để
  // nấu ngay trong ngày, không xuất trước/xuất bù cho ngày khác — backend cũng chặn cứng ở
  // `InventoryService.createIssueSlip()`). Bỏ hẳn dropdown chọn ngày, khóa luôn về hôm nay.
  const [dateIso] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType | "">("");
  const [error, setError] = useState<React.ReactNode>("");

  /** Thực đơn đã duyệt CÓ hiệu lực hôm nay — nếu rỗng, chưa có gì để xuất kho hôm nay. */
  const hasMenuToday = useMemo(() => menus.some((m) => m.menuDateIso === dateIso), [menus, dateIso]);

  /** Mọi thực đơn KHỐI phục vụ ngày đã chọn. */
  const dayMenus = useMemo(
    () => menus.filter((m) => m.menuDateIso === dateIso),
    [menus, dateIso]
  );

  /** Buổi ăn có mặt trong ngày đó (gộp từ mọi khối). */
  const dayMeals = useMemo(() => {
    const set = new Set<MealType>();
    for (const m of dayMenus) for (const b of m.blocksData ?? []) {
      if (b.mealType) set.add(b.mealType as MealType);
    }
    return [...set];
  }, [dayMenus]);

  /** Thực đơn khối thực sự có buổi ăn đang chọn — dùng để gắn `menuIds` vào phiếu xuất. */
  const mealMenus = useMemo(
    () =>
      mealType
        ? dayMenus.filter((m) => (m.blocksData ?? []).some((b) => b.mealType === mealType))
        : [],
    [dayMenus, mealType]
  );


  /**
   * Deep-link từ Lịch thực đơn tuần (`?date=&meal=`) — chỉ còn áp dụng khi ngày liên kết
   * ĐÚNG HÔM NAY (10/08/2026: xuất kho không còn chọn ngày, luôn khóa về hôm nay). Liên kết
   * tới ngày khác thì bỏ qua param `date`, chỉ còn chọn sẵn buổi ăn nếu có.
   */
  const presetApplied = useRef(false);
  useEffect(() => {
    if (presetApplied.current || menus.length === 0) return;
    const d = searchParams.get("date");
    if (!d || d !== dateIso) return;
    presetApplied.current = true;
    const m = searchParams.get("meal");
    if (m && ["BREAKFAST", "LUNCH", "SNACK"].includes(m)) setMealType(m as MealType);
  }, [menus, searchParams, dateIso]);

  /** Số suất THẬT theo món (kể cả món thay thế) — Phase 2, bỏ `class` để gộp toàn trường. */
  const { data: planned } = useSingle<PlannedDishesDto>(
    "student-distributions/planned-dishes",
    { date: dateIso, mealType: mealType as string },
    { enabled: Boolean(dateIso && mealType) }
  );

  /** Gộp nhu cầu nguyên liệu từ suất THẬT sẽ phát (không phải kế hoạch tĩnh của thực đơn). */
  const demand = useMemo((): IngredientDemand[] => {
    const byIngredient = new Map<string, IngredientDemand>();
    for (const summary of planned?.byDish ?? []) {
      const dish = summary.dishId ? dishByCode.get(summary.dishId) : undefined;
      if (!dish) continue;
      const count = summary.count ?? 0;
      const hasSubstitute = (summary.substituteCount ?? 0) > 0;
      for (const ing of dish.ingredients ?? []) {
        if (!ing.ingredientId) continue; // ngoài danh mục (gia vị) — không tính vào demand
        const cat = ingredientCatalog.find((c) => c.id === ing.ingredientId);
        const dbUnit = cat?.unit ?? ing.unit ?? "";
        const perServing = toIngredientDbQuantity(Number(ing.quantity) || 0, ing.unit ?? "", dbUnit);
        const needed = perServing * count;
        if (needed <= 0) continue;
        const existing = byIngredient.get(ing.ingredientId);
        const dishName = summary.dishName ?? "";
        if (existing) {
          existing.total += needed;
          existing.perDish.push({ dishName, needed, isSubstitute: hasSubstitute });
        } else {
          byIngredient.set(ing.ingredientId, {
            ingredientId: ing.ingredientId,
            ingredientName: ing.name,
            unit: ing.unit ?? "",
            total: needed,
            perDish: [{ dishName, needed, isSubstitute: hasSubstitute }],
            firstDishId: summary.dishId ?? "",
          });
        }
      }
    }
    return [...byIngredient.values()];
  }, [planned, dishByCode, ingredientCatalog]);

  const { data: issuePlan } = useSingle<IssuePlanDto>(
    "inventory/issue-plan",
    {
      serviceDate: dateIso,
      mealType: mealType as string,
      ...Object.fromEntries(demand.map((d) => [`demand[${d.ingredientId}]`, String(d.total)])),
    },
    { enabled: Boolean(dateIso && mealType && demand.length > 0) }
  );

  const planLines = issuePlan?.lines ?? [];
  const shortages = issuePlan?.shortages ?? [];
  const unknowns = issuePlan?.unknownIngredients ?? [];

  /** Thực đơn khối nào của bữa này đã có phiếu xuất — chặn trừ tồn lần hai. */
  const issuedMenus = useMemo(
    () =>
      mealType
        ? mealMenus.filter((m) =>
            liveTx.some((t) => t.menuId === m.id && t.mealType === mealType && t.type === "ISSUE")
          )
        : [],
    [mealMenus, mealType, liveTx]
  );

  /** Dòng phiếu xuất thực tế: mỗi (nguyên liệu × lô phân bổ) là một dòng. */
  const issuableLines = planLines.flatMap((line) =>
    (line.allocation ?? []).map((a) => ({ line, allocation: a }))
  );

  function submit() {
    if (!dateIso || !mealType) {
      setError("Chọn ngày phục vụ và buổi ăn cần xuất nguyên liệu.");
      return;
    }
    if (demand.length === 0) {
      setError("Chưa có học sinh nào đang chờ phát suất (PENDING) cho ngày/bữa này — không có gì để xuất kho.");
      return;
    }
    if (issuedMenus.length > 0) {
      setError(
        <>
          Đã có phiếu xuất cho {issuedMenus.map((m) => m.grade).join(", ")} ở buổi này. Xuất lại
          sẽ trừ tồn lần thứ hai — cần bổ sung thì dùng{" "}
          <Link href="/service-caterers/inventory" className="font-medium underline">
            phiếu điều chỉnh kèm mã lý do (BR-204)
          </Link>
          , không xuất lại.
        </>
      );
      return;
    }
    if (issuableLines.length === 0) {
      setError("Không có lô khả dụng nào để xuất cho buổi ăn này.");
      return;
    }
    setError("");

    const menuIds = mealMenus.map((m) => m.id ?? "");
    createIssueSlip.mutate(
      {
        serviceDate: dateIso,
        mealType: mealType as MealType,
        menuIds,
        lines: issuableLines.map(({ line, allocation }) => {
          const demandLine = demand.find((d) => d.ingredientId === line.ingredientId);
          return {
            batchId: allocation.batchId ?? "",
            dishId: demandLine?.firstDishId ?? "",
            quantity: allocation.take ?? 0,
            unit: line.unit ?? "",
          };
        }),
      },
      {
        onSuccess: () => {
          toast.success(
            `Đã xuất ${issuableLines.length} dòng cho ${MEAL_TYPE_LABEL[mealType as MealType]} ngày ${dateIso.split("-").reverse().join("/")} — tính theo đúng suất PENDING thật, FEFO gộp một lần (BR-075).`
          );
          // `useCreateAt("inventory", "issue-slips")` chỉ invalidate cache resource "inventory"
          // — KHÔNG invalidate "inventory-transactions" (resource khác), nên màn Kiểm thực
          // (dropdown chọn bữa) và chính bảng giao dịch kho vẫn đọc danh sách CŨ (chưa có phiếu
          // vừa xuất) cho tới khi F5 xóa cache. Invalidate thẳng ở đây thay vì sửa hành vi
          // chung của `useCreateAt` (có thể ảnh hưởng caller khác).
          qc.invalidateQueries({ queryKey: ["inventory-transactions"] });
          router.push("/service-caterers/inventory-transactions");
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Xuất kho theo bữa (FEFO)"
        description="Chọn ngày và buổi — hệ thống gộp nhu cầu theo ĐÚNG suất thật sẽ phát (kể cả món thay thế theo dị ứng), chọn lô hạn gần nhất trước một lần (BR-075)"
        actions={
          <Link href="/service-caterers/inventory">
            <Button variant="outline">
              <ArrowLeft /> Quay lại kho
            </Button>
          </Link>
        }
      />

      {error && (
        <Alert variant="danger" className="mb-4" title="Không thể xuất kho">
          {error}
        </Alert>
      )}

      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Bữa ăn cần chuẩn bị</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1.5 block text-sm font-medium">Ngày phục vụ</label>
              <p className="flex h-9 items-center rounded-md border bg-muted px-3 text-sm text-muted-foreground">
                Hôm nay — {dateIso.split("-").reverse().join("/")}
              </p>
            </div>
            <div>
              <label className="mb-1.5 block text-sm font-medium" htmlFor="is-meal">Buổi ăn *</label>
              <Select
                id="is-meal"
                value={mealType}
                disabled={!hasMenuToday}
                onChange={(e) => setMealType(e.target.value as MealType)}
              >
                <option value="">— Chọn buổi —</option>
                {dayMeals.map((mt) => (
                  <option key={mt} value={mt}>
                    {MEAL_TYPE_LABEL[mt]}
                  </option>
                ))}
              </Select>
            </div>
          </div>

          {!hasMenuToday && (
            <Alert variant="warning" title="Chưa có thực đơn đã duyệt cho hôm nay">
              Xuất kho chỉ thực hiện được cho ngày hôm nay — chưa có thực đơn PUBLISHED cho{" "}
              {dateIso.split("-").reverse().join("/")} nên chưa có gì để xuất.
            </Alert>
          )}

          {mealType && (
            <p className="text-xs text-muted-foreground">
              {(planned?.byDish ?? []).length > 0
                ? `${(planned?.perStudent ?? []).length} suất PENDING thật sẽ phát, gộp thành ${(planned?.byDish ?? []).length} món (tự tính, không sửa tay được).`
                : "Chưa có học sinh nào đang chờ phát suất (PENDING) cho ngày/bữa này."}
            </p>
          )}
        </CardContent>
      </Card>

      {issuedMenus.length > 0 && (
        <Alert variant="warning" className="mb-6" title="Buổi ăn này đã có phiếu xuất">
          Đã xuất cho {issuedMenus.map((m) => m.grade).join(", ")}. Bảng dưới vẫn tính
          định mức để đối chiếu, nhưng tồn đã bị trừ nên sẽ báo thiếu — đây là trạng
          thái đúng, không phải lỗi. Nấu thực tế cần thêm nguyên liệu (VD hao hụt sơ chế
          cao hơn dự tính) thì dùng{" "}
          <Link href="/service-caterers/inventory" className="font-medium underline">
            phiếu điều chỉnh — mã EXTRA_USAGE (BR-204)
          </Link>
          , không xuất lại phiếu này.
        </Alert>
      )}

      {planLines.length > 0 && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>
              Định mức gộp cả bữa &amp; phân bổ FEFO — hạn gần nhất xuất trước (BR-075)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nguyên liệu</TableHead>
                  <TableHead>Theo món</TableHead>
                  <TableHead>Tổng xuất kho</TableHead>
                  <TableHead>Lô phân bổ (hạn dùng)</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {planLines.map((l, i) => {
                  const demandLine = demand.find((d) => d.ingredientId === l.ingredientId);
                  return (
                    <TableRow key={`${l.ingredientId}-${i}`}>
                      <TableCell className="font-medium">{l.ingredientName}</TableCell>
                      <TableCell className="max-w-64">
                        {(demandLine?.perDish ?? []).map((pd, j) => (
                          <span key={`${pd.dishName}-${j}`} className="block text-xs text-muted-foreground">
                            <b>{pd.dishName}</b>: {pd.needed} {l.unit}
                            {pd.isSubstitute && <Badge variant="info" className="ml-1">có suất thay thế</Badge>}
                          </span>
                        ))}
                      </TableCell>
                      <TableCell className="font-semibold">
                        {l.needed} {l.unit}
                      </TableCell>
                      <TableCell>
                        {(l.allocation ?? []).length === 0 ? (
                          <span className="text-muted-foreground">—</span>
                        ) : (
                          <div className="flex flex-wrap gap-1">
                            {(l.allocation ?? []).map((a) => (
                              <Badge key={a.batchId} variant="info">
                                {a.batchId} · {a.take} {l.unit} · HSD {a.expiryDate}
                              </Badge>
                            ))}
                          </div>
                        )}
                      </TableCell>
                      <TableCell>
                        {(l.shortfall ?? 0) > 0 ? (
                          <Badge variant="warning">
                            Thiếu {l.shortfall} {l.unit}
                          </Badge>
                        ) : (
                          <Badge variant="success">Đủ</Badge>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>

            {unknowns.length > 0 && (
              <Alert variant="danger" className="mt-4" title="Nguyên liệu chưa có trong danh mục">
                {unknowns.join(", ")} — không định danh được nên không thể gắn lô và không
                truy xuất được nguồn gốc (BR-076). Cần bổ sung vào danh mục nguyên liệu
                trước khi xuất kho.
              </Alert>
            )}

            {shortages.length > 0 && (
              <Alert variant="warning" className="mt-4" title="Thiếu tồn kho (BR-077)">
                <ul className="list-disc pl-4">
                  {shortages.map((s, i) => (
                    <li key={i}>
                      {s.ingredientName} — thiếu {s.shortfall} {s.unit}
                    </li>
                  ))}
                </ul>
                <p className="mt-2">
                  Các dòng đủ tồn vẫn xuất được ngay. Phần thiếu đã tính theo TỔNG nhu cầu
                  cả bữa — bấm dưới đây để sang màn đặt hàng với dòng điền sẵn.
                </p>
                <Link
                  href={`/service-caterers/orders/new?date=${dateIso}&meal=${mealType}`}
                  className="mt-2 inline-block"
                >
                  <Button size="sm">
                    <ShoppingCart /> Tạo đơn đặt hàng bổ sung ({shortages.length} nguyên liệu)
                  </Button>
                </Link>
              </Alert>
            )}
          </CardContent>
        </Card>
      )}

      <Button
        size="lg"
        disabled={
          createIssueSlip.isPending ||
          issuableLines.length === 0 ||
          unknowns.length > 0 ||
          issuedMenus.length > 0
        }
        onClick={submit}
      >
        <PackageMinus /> Xác nhận xuất kho{" "}
        {issuableLines.length > 0 && `(${issuableLines.length} dòng)`}
      </Button>
    </>
  );
}

export default function IssueIngredientsPage() {
  // useSearchParams (deep-link ?date=&meal= từ lịch tuần) bắt buộc bọc Suspense.
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <IssueIngredientsContent />
    </Suspense>
  );
}
