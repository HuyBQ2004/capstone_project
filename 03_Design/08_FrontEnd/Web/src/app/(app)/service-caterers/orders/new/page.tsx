"use client";

import { Suspense, useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowLeft, Plus, Send, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import {
  bestPriceOf,
  type SupplierProduct,
  CURRENT_SCHOOL,
  INGREDIENT_CATALOG,
  MEAL_TYPE_LABEL,
  isMenuLive,
  mealIssueShortages,
  type InventoryTransaction,
  type MealType,
  type PurchaseItem,
  type PurchaseRequest,
  type Partner,
  type SeedMenu,
} from "@/lib/mock-data";

/**
 * Tạo đơn đặt hàng nguyên liệu (đề xuất Minh 07/2026).
 * BR-041/BR-070: chỉ đặt được với NCC đang Approved · BR-071: NCC phải xác
 * nhận lịch ≥24h trước giờ giao → ngày giao yêu cầu tối thiểu là ngày mai.
 * Đơn tạo xong ở trạng thái PENDING — NCC gửi giá theo hợp đồng (không đàm
 * phán), SC duyệt giá, rồi NCC mới Chấp nhận + gán lô hàng.
 */

interface OrderRow {
  ingredient: string;
  quantity: string;
  unit: string;
  note: string;
}

function emptyRow(): OrderRow {
  return { ingredient: "", quantity: "", unit: "kg", note: "" };
}

function CreateOrderContent() {
  const { data: partnersPage } = usePagedList<Partner>("partners", { pageSize: 100 });
  const partners = partnersPage?.items ?? [];
  const router = useRouter();
  const searchParams = useSearchParams();
  const create = useCreate<Partial<PurchaseRequest>>("purchase-requests");
  // Giao dịch kho SỐNG — các hàm tính FEFO/thiếu hàng mặc định dùng mảng SEED,
  // không truyền vào thì màn hình tính trên tồn lúc nạp module.
  const { data: txPage } = usePagedList<InventoryTransaction>("inventory-transactions", {
    pageSize: 500,
  });
  const liveTx = txPage?.items ?? [];

  /**
   * Nhận xét Minh 26/07: đọc thực đơn qua `issuableMenus()` (mảng SEED tĩnh) nên
   * thực đơn tạo/lập từ màn Thực đơn (đi qua API/db sống) không bao giờ thấy ở
   * đây — bấm "Tạo đơn đặt hàng bổ sung" từ màn Xuất kho sang thì luôn ra danh
   * sách rỗng. Đổi sang đọc `menus` qua API, giống cách `inventory/issue` đã sửa.
   */
  const { data: menusPage } = usePagedList<SeedMenu>("menus", { pageSize: 200 });
  const liveMenus = useMemo(
    () => (menusPage?.items ?? []).filter(isMenuLive),
    [menusPage]
  );

  /**
   * Mở từ màn Xuất kho khi thiếu hàng: ?date=&meal= — tính lại phần thiếu ngay tại
   * đây thay vì bê số qua URL, để đơn luôn khớp tồn kho hiện tại (tồn có thể đã đổi
   * giữa lúc bấm và lúc gửi đơn). Thiếu hàng tính theo TỔNG nhu cầu của mọi khối ăn
   * bữa đó — đúng cách bếp lấy hàng, và khớp với màn xuất kho.
   */
  const sourceDate = searchParams.get("date") ?? "";
  const sourceMeal = (searchParams.get("meal") ?? "") as MealType | "";
  const sourceMenus = sourceDate
    ? liveMenus.filter(
        (m) =>
          m.menuDateIso === sourceDate &&
          (m.blocksData ?? []).some((b) => b.mealType === sourceMeal)
      )
    : [];
  // Tính lại mỗi lần render (không dùng useState) — `liveMenus`/`liveTx` tải
  // bất đồng bộ, khởi tạo state 1 lần lúc mount sẽ đông cứng ở kết quả rỗng
  // của lần render đầu, trước khi API kịp trả dữ liệu.
  const allShortages =
    sourceMenus.length > 0 && sourceMeal
      ? mealIssueShortages(sourceMenus, sourceMeal, (m) => m.servings ?? 0, liveTx)
      : [];
  // Dòng đơn chọn nguyên liệu TỪ CATALOG; thứ ngoài danh mục không có option để
  // chọn nên không điền sẵn được — phải nói rõ thay vì lặng lẽ bỏ dòng.
  const prefill = allShortages.filter((l) => l.ingredientId);
  const offCatalog = allShortages.filter((l) => !l.ingredientId);

  const suppliers = partners.filter(
    (p) => p.type === "Raw Food Supplier" && p.status === "APPROVED"
  );
  // Gợi ý NCC = nơi đã giao nhiều nguyên liệu thiếu nhất gần đây (BR-076 dùng
  // ingredientId nên không lệch vì tên lô có hậu tố).
  const suggestedSupplierName = (() => {
    const tally = new Map<string, number>();
    for (const l of prefill) {
      if (!l.suggestedSupplier) continue;
      tally.set(l.suggestedSupplier, (tally.get(l.suggestedSupplier) ?? 0) + 1);
    }
    return [...tally.entries()].sort((a, b) => b[1] - a[1])[0]?.[0];
  })();

  const [supplierId, setSupplierId] = useState("");
  const [deliveryDate, setDeliveryDate] = useState("");
  const [rows, setRows] = useState<OrderRow[]>([emptyRow()]);
  const [error, setError] = useState("");

  /**
   * Áp dòng điền sẵn + NCC gợi ý MỘT LẦN, đúng lúc dữ liệu API (menus/tồn
   * kho/NCC) đã tải xong. Trước đây dùng `useState(() => …)` — hàm khởi tạo
   * đó chỉ chạy ở lần render ĐẦU, lúc `usePagedList` còn `undefined` (chưa
   * fetch xong), nên luôn đông cứng ở kết quả rỗng — bấm "Tạo đơn đặt hàng bổ
   * sung" từ màn Xuất kho sang không bao giờ thấy dòng nào cả.
   */
  const appliedPrefill = useRef(false);
  useEffect(() => {
    if (appliedPrefill.current) return;
    if (sourceDate && sourceMeal && (!menusPage || !txPage || !partnersPage)) return; // API chưa tải xong
    appliedPrefill.current = true;
    if (prefill.length > 0) {
      setRows(
        prefill.map((l) => ({
          ingredient: l.ingredientName,
          quantity: String(l.quantity),
          unit: l.unit,
          note: `Bù thiếu cho ${l.dishes.join(", ")}`,
        }))
      );
    }
    const suggested = suppliers.find(
      (s) => suggestedSupplierName && s.name.includes(suggestedSupplierName)
    );
    if (suggested) setSupplierId(suggested.id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [menusPage, txPage, prefill, suggestedSupplierName, suppliers]);

  /**
   * Mặt hàng nhà cung cấp ĐANG CHỌN thực sự bán.
   *
   * Trước 22/07/2026 ô nguyên liệu đổ ra cả danh mục 46 mục bất kể NCC có bán hay
   * không, nên đặt được "Cá thu" từ HTX Rau Vân Nội — đơn gửi đi cho thứ người ta
   * không kinh doanh, và bên nhận phải gọi điện lại hỏi.
   */
  const { data: productPage } = usePagedList<SupplierProduct>("supplier-products", {
    pageSize: 300,
  });
  const supplierProducts = (productPage?.items ?? []).filter((p) => p.supplierId === supplierId);
  const sellable = supplierProducts
    .map((p) => ({ product: p, catalog: INGREDIENT_CATALOG.find((c) => c.id === p.ingredientId) }))
    .filter((x) => x.catalog != null)
    .sort((a, b) => a.catalog!.name.localeCompare(b.catalog!.name, "vi"));

  /**
   * Nhu cầu thiếu hàng gom theo NHÀ CUNG CẤP RẺ NHẤT của từng nguyên liệu.
   *
   * Một bữa thường thiếu hàng của nhiều nhà cung cấp cùng lúc, mà đơn đặt hàng là
   * chứng từ với MỘT pháp nhân — có giá riêng, xác nhận riêng, chuyến giao riêng.
   * Nên không gộp nhiều NCC vào một đơn, cũng không bắt chọn một NCC duy nhất rồi
   * mua cả thứ họ bán đắt. Gom sẵn theo NCC để người dùng gửi lần lượt.
   */
  const shortageBySupplier = (() => {
    const groups = new Map<string, { supplierId: string; lines: typeof prefill }>();
    for (const l of prefill) {
      const best = bestPriceOf(l.ingredientId);
      if (!best) continue;
      const g = groups.get(best.supplierId) ?? { supplierId: best.supplierId, lines: [] };
      g.lines.push(l);
      groups.set(best.supplierId, g);
    }
    return [...groups.values()].sort((a, b) => b.lines.length - a.lines.length);
  })();

  function loadSupplierLines(sid: string) {
    const g = shortageBySupplier.find((x) => x.supplierId === sid);
    if (!g) return;
    setSupplierId(sid);
    setRows(
      g.lines.map((l) => ({
        ingredient: l.ingredientName,
        quantity: String(l.quantity),
        unit: l.unit,
        note: `Bù thiếu cho ${l.dishes.join(", ")}`,
      }))
    );
    setError("");
  }

  /** Dòng đang chọn nguyên liệu mà NCC này không bán — cảnh báo, không tự xoá. */
  const notSold = rows
    .filter((r) => r.ingredient && !sellable.some((x) => x.catalog!.name === r.ingredient))
    .map((r) => r.ingredient);

  function updateRow(index: number, patch: Partial<OrderRow>) {
    /**
     * CHẶN chọn trùng nguyên liệu (Minh báo 25/07/2026): đơn 2 dòng cùng tên thì
     * phía NCC báo giá + gán lô theo map `tên → lô` — hai dòng đè nhau, một dòng
     * mất lô/mất tiền. Minh chọn hướng báo "đã có sẵn, sửa số lượng" thay vì tự
     * cộng dồn (cộng ngầm dễ ra số lượng người dùng không chủ đích).
     */
    if (patch.ingredient) {
      const dupIndex = rows.findIndex((r, i) => i !== index && r.ingredient === patch.ingredient);
      if (dupIndex >= 0) {
        toast.warning(
          `"${patch.ingredient}" đã có ở dòng #${dupIndex + 1} — sửa số lượng tại dòng đó thay vì chọn thêm lần nữa.`
        );
        return;
      }
    }
    setRows((prev) =>
      prev.map((r, i) => {
        if (i !== index) return r;
        const next = { ...r, ...patch };
        // Chọn nguyên liệu từ catalog → tự điền đơn vị chuẩn
        if (patch.ingredient !== undefined) {
          const cat = INGREDIENT_CATALOG.find((c) => c.name === patch.ingredient);
          if (cat) next.unit = cat.unit;
        }
        return next;
      })
    );
  }

  function submit() {
    const supplier = suppliers.find((s) => s.id === supplierId);
    if (!supplier) {
      setError("Phải chọn nhà cung cấp đang Approved (BR-041/BR-070).");
      return;
    }
    if (notSold.length > 0) {
      setError(
        `${supplier.name} không cung ứng: ${notSold.join(", ")}. Bỏ các dòng này hoặc đặt từ nhà cung cấp khác — gửi đi thì bên nhận cũng phải gọi lại hỏi.`
      );
      return;
    }
    if (!deliveryDate) {
      setError("Ngày giao yêu cầu là bắt buộc.");
      return;
    }
    const minDate = new Date(Date.now() + 24 * 60 * 60 * 1000);
    minDate.setHours(0, 0, 0, 0);
    if (new Date(deliveryDate) < minDate) {
      setError("Ngày giao yêu cầu phải từ ngày mai trở đi — NCC cần ≥24h xác nhận lịch giao (BR-071).");
      return;
    }
    const validRows = rows.filter((r) => r.ingredient.trim());
    if (validRows.length === 0) {
      setError("Đơn phải có ít nhất 1 dòng nguyên liệu.");
      return;
    }
    // Chốt chặn cuối trước khi gửi — trùng tên là mất lô ở phía NCC (map tên → lô)
    const dupNames = [
      ...new Set(
        validRows.map((r) => r.ingredient).filter((n, i, all) => all.indexOf(n) !== i)
      ),
    ];
    if (dupNames.length > 0) {
      setError(
        `Nguyên liệu bị trùng dòng: ${dupNames.join(", ")} — gộp về một dòng và cộng số lượng, nếu không NCC chỉ gán lô được cho một dòng.`
      );
      return;
    }
    for (const [i, r] of rows.entries()) {
      if (!r.ingredient.trim()) continue;
      if (!r.quantity || Number(r.quantity) <= 0) {
        setError(`Dòng #${i + 1} (${r.ingredient}): số lượng phải lớn hơn 0.`);
        return;
      }
    }
    setError("");

    const itemsDetail: PurchaseItem[] = validRows.map((r) => ({
      name: r.ingredient,
      quantity: `${r.quantity} ${r.unit}`,
      note: r.note.trim() || undefined,
    }));

    create.mutate(
      {
        id: `PR-2026-${String(Date.now()).slice(-4)}`,
        school: CURRENT_SCHOOL.shortName,
        supplier: supplier.name,
        // Khoá để NCC tra ĐÚNG bảng giá của mình khi gửi giá theo hợp đồng
        supplierId: supplier.id,
        requestedBy: "Bếp trưởng — Cty Suất ăn An Bình",
        requestedDate: new Date().toLocaleDateString("vi-VN"),
        deliveryDate: new Date(deliveryDate).toLocaleDateString("vi-VN"),
        items: itemsDetail.length,
        // Giá do NCC gửi theo bảng giá hợp đồng sau khi nhận đơn — caterer không tự nhập
        totalValue: "Chờ NCC gửi giá",
        status: "PENDING",
        itemsDetail,
        // Nguồn phát sinh — trả lời được "đơn này đặt vì thực đơn nào" khi đối
        // chiếu sau này; đơn đặt chủ động (không từ màn xuất kho) thì bỏ trống.
        // Bữa ăn gồm nhiều thực đơn khối — ghi thực đơn đầu tiên làm mốc, ngày
        // phục vụ mới là thứ định danh nguồn phát sinh của đơn.
        sourceMenuId: sourceMenus[0]?.id,
        sourceMealType: sourceMeal || undefined,
        serviceDate: sourceDate || undefined,
      },
      {
        onSuccess: (pr) => {
          toast.success(
            `Đã gửi đơn ${pr.id} tới ${supplier.name} — NCC sẽ gửi giá theo hợp đồng để bạn duyệt, rồi gán lô hàng theo từng nguyên liệu.`
          );
          router.push("/service-caterers/orders");
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Tạo đơn đặt hàng nguyên liệu"
        description="Đơn gửi đi ở trạng thái PENDING — NCC gửi giá theo hợp đồng, bạn duyệt giá, NCC gán lô hàng (BR-071) rồi giao; hàng tới nơi mới mở phiên nhận"
        actions={
          <Link href="/service-caterers/orders">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      {error && (
        <Alert variant="danger" className="mb-4" title="Chưa thể gửi đơn">
          {error}
        </Alert>
      )}

      {sourceMenus.length > 0 && sourceMeal && (
        <Alert variant="info" className="mb-4" title="Đơn bù thiếu cho bữa ăn đã lập thực đơn">
          {prefill.length} dòng đã điền sẵn từ phần thiếu của{" "}
          <b>
            {MEAL_TYPE_LABEL[sourceMeal]} ngày {sourceDate.split("-").reverse().join("/")}
          </b>{" "}
          — gộp {sourceMenus.length} khối ({sourceMenus.map((m) => m.grade).join(", ")}).
          Số lượng do hệ thống nổ định mức cả bữa rồi trừ tồn khả dụng, sửa lại được
          trước khi gửi.
          {suggestedSupplierName && (
            <> NCC gợi ý: <b>{suggestedSupplierName}</b> (đã giao các nguyên liệu này gần nhất).</>
          )}
          {offCatalog.length > 0 && (
            <p className="mt-2">
              Không điền sẵn được: <b>{offCatalog.map((l) => l.ingredientName).join(", ")}</b> —
              chưa có trong danh mục nguyên liệu nên không đặt theo mã lô được (BR-076).
              Bổ sung vào danh mục rồi đặt riêng.
            </p>
          )}
        </Alert>
      )}

      {/* Gom nhu cầu theo nhà cung cấp rẻ nhất — mỗi nhóm là một đơn riêng. */}
      {shortageBySupplier.length > 1 && (
        <Alert
          variant="info"
          className="mb-6"
          title={`Nhu cầu này trải ${shortageBySupplier.length} nhà cung cấp — cần ${shortageBySupplier.length} đơn`}
        >
          Đơn đặt hàng là chứng từ với một pháp nhân nên không gộp chung được. Bấm để
          nạp các dòng của từng nhà cung cấp, gửi lần lượt.
          <div className="mt-2 flex flex-wrap gap-2">
            {shortageBySupplier.map((g) => {
              const name = suppliers.find((x) => x.id === g.supplierId)?.name ?? g.supplierId;
              return (
                <Button
                  key={g.supplierId}
                  size="sm"
                  variant={supplierId === g.supplierId ? "default" : "outline"}
                  onClick={() => loadSupplierLines(g.supplierId)}
                >
                  {name} · {g.lines.length} mặt hàng
                </Button>
              );
            })}
          </div>
        </Alert>
      )}

      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Thông tin đơn</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label htmlFor="od-supplier">Nhà cung cấp * (chỉ NCC Approved — BR-041)</Label>
            <Select id="od-supplier" value={supplierId} onChange={(e) => setSupplierId(e.target.value)}>
              <option value="">— Chọn NCC —</option>
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="od-date">Ngày giao yêu cầu * (≥ ngày mai — BR-071)</Label>
            <Input
              id="od-date"
              type="date"
              value={deliveryDate}
              onChange={(e) => setDeliveryDate(e.target.value)}
            />
          </div>
        </CardContent>
      </Card>

      <Card className="mb-6">
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle>Dòng nguyên liệu ({rows.filter((r) => r.ingredient).length})</CardTitle>
          <Button type="button" variant="outline" size="sm" onClick={() => setRows((p) => [...p, emptyRow()])}>
            <Plus /> Thêm dòng
          </Button>
        </CardHeader>
        <CardContent className="space-y-3">
          {rows.map((r, i) => (
            <div key={i} className="grid items-end gap-2 sm:grid-cols-[1fr_110px_90px_1fr_36px]">
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground">Nguyên liệu *</Label>
                <Select value={r.ingredient} onChange={(e) => updateRow(i, { ingredient: e.target.value })}>
                  <option value="">— Chọn mặt hàng nhà cung cấp này bán —</option>
                  {sellable.map(({ product, catalog }) => (
                    <option key={product.id} value={catalog!.name}>
                      {catalog!.name} — {product.unitPrice.toLocaleString("vi-VN")}₫/{product.unit}
                      {product.packaging ? ` · ${product.packaging}` : ""}
                    </option>
                  ))}
                  {/* Giữ lại dòng đã chọn trước khi đổi NCC để người dùng thấy mà xử lý,
                      thay vì ô trống không hiểu vì sao. */}
                  {r.ingredient && !sellable.some((x) => x.catalog!.name === r.ingredient) && (
                    <option value={r.ingredient}>{r.ingredient} — nhà cung cấp này không bán</option>
                  )}
                </Select>
              </div>
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground">Số lượng *</Label>
                <Input
                  type="number"
                  min={0}
                  value={r.quantity}
                  onChange={(e) => updateRow(i, { quantity: e.target.value })}
                />
              </div>
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground">Đơn vị</Label>
                <Select value={r.unit} onChange={(e) => updateRow(i, { unit: e.target.value })}>
                  {["kg", "lít", "quả", "hộp", "ổ", "bó", "thùng"].map((u) => (
                    <option key={u}>{u}</option>
                  ))}
                </Select>
              </div>
              <div className="space-y-1">
                <Label className="text-xs text-muted-foreground">Ghi chú</Label>
                <Input
                  value={r.note}
                  onChange={(e) => updateRow(i, { note: e.target.value })}
                  placeholder="VD: giao trước 06:00"
                />
              </div>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                disabled={rows.length === 1}
                onClick={() => setRows((p) => p.filter((_, idx) => idx !== i))}
                aria-label="Xóa dòng"
              >
                <Trash2 />
              </Button>
            </div>
          ))}
          <Textarea
            className="min-h-[48px]"
            placeholder="Ghi chú chung cho NCC (tùy chọn)…"
            aria-label="Ghi chú chung"
          />
        </CardContent>
      </Card>

      <Button size="lg" disabled={create.isPending} onClick={submit}>
        <Send /> Gửi đơn tới NCC (PENDING)
      </Button>
    </>
  );
}

export default function CreateOrderPage() {
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <CreateOrderContent />
    </Suspense>
  );
}
