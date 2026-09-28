"use client";

import { Suspense, useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowLeft, PackageMinus, ShoppingCart } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Input, Label, Select } from "@/components/ui/input";
import { StatusBadge } from "@/components/app/status-badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import {
  batchDisplayStatus,
  MEAL_TYPE_LABEL,
  isBatchLocked,
  isMenuLive,
  issuedLinesForMeal,
  mealIssuePlan,
  type InventoryBatch,
  type InventoryTransaction,
  type MealType,
  type SeedMenu,
} from "@/lib/mock-data";

/**
 * 29_IssueIngredientsWorkspace — xuất kho theo BỮA, phân bổ FEFO.
 *
 * Đổi 22/07/2026: trước đây chọn MỘT thực đơn (mà thực đơn = ngày × khối) rồi xuất,
 * nên một bữa có N khối là N phiếu riêng. Bếp thì nấu theo bữa: cùng "Cơm trắng"
 * thì Khối 1 và Khối 3 ăn chung một nồi. Chạy FEFO riêng từng khối làm cùng một nồi
 * bị ghi nhận lấy từ hai lô khác nhau — khi một lô nhiễm, bản đồ truy xuất chỉ ra
 * đúng khối "dính" lô đó trong khi thực tế mọi khối đều ăn chung.
 *
 * Nay: chọn NGÀY + BUỔI → gộp nhu cầu mọi khối theo nguyên liệu → FEFO chạy MỘT
 * lần → chia ngược lượng đã phân bổ về từng thực đơn khối, nên mỗi dòng phiếu xuất
 * vẫn neo `menuId` và truy xuất vẫn mịn tới từng khối (BR-076).
 *
 * BR-075 FEFO · BR-077 cảnh báo thiếu kho · BR-205/BR-149 lô hết hạn/cách ly bị khóa.
 *
 * 25/07/2026 (Minh — "tăng liên kết với thực đơn"): thực đơn đọc qua API thay cho
 * mảng seed (`issuableMenus()` cũ đọc `MENUS` tĩnh nên thực đơn dựng từ Thực đơn
 * mẫu trên lịch tuần, sau khi Ban giám hiệu duyệt, KHÔNG BAO GIỜ hiện ra ở đây —
 * đứt đúng khúc giữa của chuỗi thực đơn → xuất kho). Nhận thêm `?date=&meal=` để
 * lịch tuần deep-link sang với ngày + bữa chọn sẵn.
 */
function IssueIngredientsContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const create = useCreate<Partial<InventoryTransaction>>("inventory-transactions");
  // Giao dịch kho SỐNG — các hàm FEFO mặc định dùng mảng seed nếu không truyền vào
  const { data: txPage } = usePagedList<InventoryTransaction>("inventory-transactions", {
    pageSize: 500,
  });
  const liveTx = useMemo(() => txPage?.items ?? [], [txPage]);
  /*
   * Lô SỐNG. Trước 22/07/2026 FEFO lọc lô từ mảng seed `INVENTORY`, mà db được
   * dựng bằng `clone(INVENTORY)` — nên lô Y tế vừa cách ly vẫn nằm trong danh
   * sách ứng viên và bếp xuất kho đúng lô đang bị nghi. Cách ly ghi vào db thật,
   * chỉ là phía đọc nhìn vào bản sao khác. Đúng thứ BR-149 sinh ra để chặn mà
   * lại không chặn, và không có lỗi nào báo ra.
   */
  const { data: batchPage } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
  const liveBatches = useMemo(() => batchPage?.items ?? [], [batchPage]);

  // Chỉ thực đơn ĐÃ CÔNG BỐ mới được xuất kho phục vụ — đọc từ db sống nên thực
  // đơn vừa duyệt trong phiên (kể cả dựng từ Thực đơn mẫu) có mặt ngay.
  const { data: menusPage } = usePagedList<SeedMenu>("menus", { pageSize: 200 });
  const menus = useMemo(
    () => (menusPage?.items ?? []).filter(isMenuLive) as unknown as SeedMenu[],
    [menusPage]
  );
  const [dateIso, setDateIso] = useState("");
  const [mealType, setMealType] = useState<MealType | "">("");
  /** Số suất theo TỪNG thực đơn khối — sửa được khi thực tế khác kế hoạch. */
  const [servings, setServings] = useState<Record<string, string>>({});
  const [error, setError] = useState("");

  /** Các ngày còn thực đơn đã duyệt để xuất kho. */
  const dates = useMemo(
    () => [...new Set(menus.map((m) => m.menuDateIso).filter(Boolean))].sort() as string[],
    [menus]
  );

  /** Mọi thực đơn KHỐI phục vụ ngày đã chọn. */
  const dayMenus = useMemo(
    () => menus.filter((m) => m.menuDateIso === dateIso),
    [menus, dateIso]
  );

  /** Buổi ăn có mặt trong ngày đó (gộp từ mọi khối). */
  const dayMeals = useMemo(() => {
    const set = new Set<MealType>();
    for (const m of dayMenus) for (const b of m.blocksData ?? []) set.add(b.mealType);
    return [...set];
  }, [dayMenus]);

  /** Thực đơn khối thực sự có buổi ăn đang chọn. */
  const mealMenus = useMemo(
    () =>
      mealType
        ? dayMenus.filter((m) => (m.blocksData ?? []).some((b) => b.mealType === mealType))
        : [],
    [dayMenus, mealType]
  );

  function selectDate(iso: string) {
    setDateIso(iso);
    setMealType("");
    setServings(
      Object.fromEntries(
        menus.filter((m) => m.menuDateIso === iso).map((m) => [m.id, String(m.servings ?? "")])
      )
    );
    setError("");
  }

  /**
   * Deep-link từ Lịch thực đơn tuần (`?date=&meal=`) — chọn sẵn ngày + bữa đúng ô
   * vừa xem. Chỉ áp một lần khi thực đơn tải xong; ref chặn effect chạy lại đè lên
   * lựa chọn tay (menusPage đổi tham chiếu sau mỗi lần react-query refetch).
   */
  const presetApplied = useRef(false);
  useEffect(() => {
    if (presetApplied.current || menus.length === 0) return;
    const d = searchParams.get("date");
    if (!d) return;
    presetApplied.current = true;
    if (!menus.some((m) => m.menuDateIso === d)) return; // ngày không có thực đơn công bố
    selectDate(d);
    const m = searchParams.get("meal");
    if (m && ["BREAKFAST", "LUNCH", "SNACK"].includes(m)) setMealType(m as MealType);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [menus, searchParams]);

  const servingsOf = useMemo(
    () => (menu: (typeof menus)[number]) => Number(servings[menu.id]) || 0,
    [servings]
  );

  /** Kế hoạch xuất kho của cả bữa — FEFO chạy một lần trên tổng nhu cầu. */
  const plan = useMemo(
    () =>
      mealType && mealMenus.length > 0
        ? mealIssuePlan(mealMenus, mealType, servingsOf, liveTx, liveBatches)
        : [],
    [mealMenus, mealType, servingsOf, liveTx, liveBatches]
  );

  const shortages = plan.filter((l) => l.shortfall > 0 && !l.unknownIngredient);
  const unknowns = plan.filter((l) => l.unknownIngredient);

  /** Thực đơn khối nào của bữa này đã có phiếu xuất — chặn trừ tồn lần hai. */
  const issuedMenus = useMemo(
    () =>
      mealType
        ? mealMenus.filter((m) => issuedLinesForMeal(m.id, mealType, liveTx).length > 0)
        : [],
    [mealMenus, mealType, liveTx]
  );

  /** Lô bị khóa của đúng các nguyên liệu đang cần — giải thích vì sao FEFO bỏ qua. */
  const lockedBatches = useMemo(() => {
    const ids = new Set(plan.map((l) => l.ingredientId).filter(Boolean));
    return liveBatches.filter((b) => ids.has(b.ingredientId) && isBatchLocked(b));
  }, [plan, liveBatches]);

  /** Dòng phiếu xuất thực tế: mỗi (khối × nguyên liệu × lô) là một dòng. */
  const issuableLines = plan.flatMap((line) =>
    line.perMenu.flatMap((pm) => pm.allocation.map((a) => ({ line, pm, allocation: a })))
  );

  function submit() {
    if (!dateIso || !mealType) {
      setError("Chọn ngày phục vụ và buổi ăn cần xuất nguyên liệu.");
      return;
    }
    if (mealMenus.some((m) => servingsOf(m) <= 0)) {
      setError("Mỗi khối phải có số suất lớn hơn 0.");
      return;
    }
    if (issuedMenus.length > 0) {
      setError(
        `Đã có phiếu xuất cho ${issuedMenus.map((m) => m.grade).join(", ")} ở buổi này. Xuất lại sẽ trừ tồn lần thứ hai — cần bổ sung thì dùng phiếu điều chỉnh kèm mã lý do (BR-204).`
      );
      return;
    }
    if (issuableLines.length === 0) {
      setError("Không có lô khả dụng nào để xuất cho buổi ăn này.");
      return;
    }
    setError("");

    const now = new Date();
    const serviceDate = dateIso.split("-").reverse().join("/");

    issuableLines.forEach(({ line, pm, allocation }, i) => {
      create.mutate({
        id: `TXN-${String(Date.now()).slice(-6)}-${i}`,
        timestamp: now.toLocaleString("vi-VN"),
        type: "Xuất kho (FEFO)",
        batchId: allocation.batch.id,
        ingredientId: line.ingredientId,
        ingredient: allocation.batch.ingredient,
        quantity: `-${allocation.take} ${line.unit}`,
        actor: "Trần Văn Hùng",
        // Vẫn neo từng KHỐI dù FEFO tính gộp cả bữa (BR-076)
        menuId: pm.menuId,
        mealType,
        dishName: pm.dishNames.join(", "),
        serviceDate,
      });
    });

    toast.success(
      `Đã xuất ${issuableLines.length} dòng cho ${MEAL_TYPE_LABEL[mealType]} ngày ${serviceDate} — ${mealMenus.length} khối, FEFO tính gộp một lần (BR-075).`
    );
    router.push("/service-caterers/inventory-transactions");
  }

  return (
    <>
      <PageHeader
        title="Xuất kho theo bữa (FEFO)"
        description="Chọn ngày và buổi — hệ thống gộp nhu cầu của MỌI KHỐI ăn bữa đó, chọn lô hạn gần nhất trước một lần (BR-075), rồi chia phiếu về từng khối để truy xuất nguồn gốc (BR-076)"
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
            <div className="space-y-1.5">
              <Label htmlFor="is-date">Ngày phục vụ *</Label>
              <Select id="is-date" value={dateIso} onChange={(e) => selectDate(e.target.value)}>
                <option value="">— Chọn ngày —</option>
                {dates.map((d) => (
                  <option key={d} value={d}>
                    {d.split("-").reverse().join("/")}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="is-meal">Buổi ăn *</Label>
              <Select
                id="is-meal"
                value={mealType}
                disabled={!dateIso}
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

          {mealMenus.length > 0 && (
            <div>
              <Label className="mb-1.5 block">
                Số suất theo khối ({mealMenus.length} thực đơn cùng ăn bữa này)
              </Label>
              <div className="grid gap-2 sm:grid-cols-3">
                {mealMenus.map((m) => (
                  <div key={m.id} className="flex items-center gap-2 rounded-lg border p-2">
                    <span className="min-w-16 text-sm font-medium">{m.grade}</span>
                    <Input
                      type="number"
                      min={0}
                      className="h-8"
                      value={servings[m.id] ?? ""}
                      onChange={(e) =>
                        setServings((prev) => ({ ...prev, [m.id]: e.target.value }))
                      }
                      aria-label={`Số suất ${m.grade}`}
                    />
                  </div>
                ))}
              </div>
              <p className="mt-1.5 text-xs text-muted-foreground">
                Mặc định theo kế hoạch của từng thực đơn; sửa khi số suất thực tế khác.
                Món trùng nhau giữa các khối được cộng dồn trước khi chọn lô.
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      {issuedMenus.length > 0 && (
        <Alert variant="warning" className="mb-6" title="Buổi ăn này đã có phiếu xuất">
          Đã xuất cho {issuedMenus.map((m) => m.grade).join(", ")}. Bảng dưới vẫn tính
          định mức để đối chiếu, nhưng tồn đã bị trừ nên sẽ báo thiếu — đây là trạng
          thái đúng, không phải lỗi. Cần bổ sung thì dùng phiếu điều chỉnh (BR-204).
        </Alert>
      )}

      {plan.length > 0 && (
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
                  <TableHead>Chia theo khối</TableHead>
                  <TableHead>Tổng xuất kho</TableHead>
                  <TableHead>Lô phân bổ (hạn dùng)</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {plan.map((l, i) => (
                  <TableRow key={`${l.ingredientName}-${i}`}>
                    <TableCell className="font-medium">
                      {l.ingredientName}
                      <span className="block text-xs font-normal text-muted-foreground">
                        {l.perServing}/suất
                      </span>
                    </TableCell>
                    <TableCell className="max-w-64">
                      {l.perMenu.map((pm) => (
                        <span key={pm.menuId} className="block text-xs text-muted-foreground">
                          <b>{pm.grade}</b>: {pm.needed} {l.unit}
                          {pm.allocation.length > 0 && (
                            <> ← {pm.allocation.map((a) => `${a.batch.id} (${a.take})`).join(", ")}</>
                          )}
                        </span>
                      ))}
                    </TableCell>
                    <TableCell className="font-semibold">
                      {l.totalNeeded} {l.unit}
                    </TableCell>
                    <TableCell>
                      {l.allocation.length === 0 ? (
                        <span className="text-muted-foreground">—</span>
                      ) : (
                        <div className="flex flex-wrap gap-1">
                          {l.allocation.map((a) => (
                            <Badge key={a.batch.id} variant="info">
                              {a.batch.id} · {a.take} {l.unit} · HSD {a.batch.expiryDate}
                            </Badge>
                          ))}
                        </div>
                      )}
                    </TableCell>
                    <TableCell>
                      {l.unknownIngredient ? (
                        <Badge variant="critical">Ngoài danh mục</Badge>
                      ) : l.shortfall > 0 ? (
                        <Badge variant="warning">
                          Thiếu {l.shortfall} {l.unit}
                        </Badge>
                      ) : (
                        <Badge variant="success">Đủ</Badge>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {unknowns.length > 0 && (
              <Alert variant="danger" className="mt-4" title="Nguyên liệu chưa có trong danh mục">
                {unknowns.map((u) => u.ingredientName).join(", ")} — không định danh được nên
                không thể gắn lô và không truy xuất được nguồn gốc (BR-076). Cần bổ sung vào
                danh mục nguyên liệu trước khi xuất kho.
              </Alert>
            )}

            {shortages.length > 0 && (
              <Alert variant="warning" className="mt-4" title="Thiếu tồn kho (BR-077)">
                <ul className="list-disc pl-4">
                  {shortages.map((s, i) => (
                    <li key={i}>
                      {s.ingredientName} — thiếu {s.shortfall} {s.unit} (cần cho{" "}
                      {s.perMenu.map((p) => p.grade).join(", ")})
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

            {lockedBatches.length > 0 && (
              <Alert variant="info" className="mt-4" title="Lô bị khóa — FEFO đã bỏ qua">
                <div className="flex flex-wrap gap-1">
                  {lockedBatches.map((b) => (
                    <Badge key={b.id} variant="neutral">
                      {b.id} · {b.ingredient} · <StatusBadge status={batchDisplayStatus(b)} />
                    </Badge>
                  ))}
                </div>
                <p className="mt-2">
                  Lô hết hạn (BR-205) và lô đang cách ly (BR-149) không được đưa vào phân bổ.
                </p>
              </Alert>
            )}
          </CardContent>
        </Card>
      )}

      <Button
        size="lg"
        disabled={
          create.isPending ||
          issuableLines.length === 0 ||
          unknowns.length > 0 ||
          issuedMenus.length > 0
        }
        onClick={submit}
      >
        <PackageMinus /> Xác nhận xuất kho{" "}
        {issuableLines.length > 0 && `(${issuableLines.length} dòng · ${mealMenus.length} khối)`}
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
