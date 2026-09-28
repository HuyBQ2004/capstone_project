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
import { useQueries } from "@tanstack/react-query";
import { api } from "@/lib/api/client";
import { useCreate, usePagedList, usePlainList } from "@/lib/api/hooks";
import type {
  IngredientDto,
  IssuePlanDto,
  MenuDto,
  PartnerDto,
  PurchaseRequestDto,
  SupplierProductDto,
} from "@/lib/api/types";
import { supplierProductDtosToMock, type SupplierProduct } from "@/lib/supplier-product-adapter";
import {
  bestPriceOf,
  MEAL_TYPE_LABEL,
  type MealType,
  toBaseQuantity,} from "@/lib/domain-utils";

/** `POST /purchase-requests` — server tự tính `totalValue`/`status`, không nhận (D12). */
type CreatePurchaseRequestBody = {
  supplierId: string;
  deliveryDate: string;
  items: { ingredientId: string; quantity: number; unit: string; note?: string }[];
};

/** Dòng thiếu hàng, xây từ `GET /inventory/issue-plan` (server FEFO — xem `inventory/issue/page.tsx`). */
type ShortageLine = {
  ingredientId: string;
  ingredientName: string;
  quantity: number;
  unit: string;
  dishes: string[];
};

/**
 * Tạo đơn đặt hàng nguyên liệu (đề xuất Minh 07/2026).
 * BR-041/BR-070: chỉ đặt được với NCC đang Approved · BR-071: NCC phải xác
 * nhận lịch ≥24h trước giờ giao → ngày giao yêu cầu tối thiểu là ngày mai.
 * Đơn tạo xong ở trạng thái PENDING — NCC gửi giá theo hợp đồng (không đàm
 * phán), SC duyệt giá, rồi NCC mới Chấp nhận + gán lô hàng.
 *
 * CHỌN NCC THEO TỪNG DÒNG (10/08/2026, ý tưởng người dùng phản hồi) — trước đây
 * phải chọn MỘT NCC chung cho cả đơn ở đầu form, rồi ô "Nguyên liệu" chỉ đổ ra
 * đúng mặt hàng của NCC đó; muốn đặt của NCC khác phải bấm chip "gom theo NCC rẻ
 * nhất" để THAY TOÀN BỘ danh sách dòng rồi gửi riêng — lặp lại thao tác cho từng
 * NCC. Nay mỗi dòng tự chọn NCC (mặc định rẻ nhất, đổi được), gộp hết vào MỘT
 * bảng; bấm "Gửi đơn" một lần thì hệ thống tự tách theo NCC và gửi TUẦN TỰ nhiều
 * đơn (không đổi luật "1 đơn = 1 pháp nhân", chỉ tự động hoá bước tách).
 */

interface OrderRow {
  ingredient: string;
  /** Mã NCC đang bán mặt hàng này cho dòng — rỗng = chưa chọn được. */
  supplierId: string;
  quantity: string;
  unit: string;
  note: string;
  /**
   * PROPOSED (plan "Đơn vị đo lường" §Phase B.9) — nhu cầu THẬT theo đơn vị CHUẨN của
   * nguyên liệu (trước khi quy đổi theo cách đóng gói của NCC) — chỉ có khi dòng này đến
   * từ "Bù thiếu" (`issue-plan`), để khi đổi NCC có thể tính lại đúng số lượng thay vì chỉ
   * đổi nhãn đơn vị. Dòng người dùng tự thêm tay thì không có, giữ hành vi cũ.
   */
  baseQuantity?: number;
  /**
   * Số lượng HỆ THỐNG ĐỀ XUẤT lúc nạp từ thực đơn (đơn vị đặt hàng của NCC). Giữ lại để so
   * với số người dùng gõ: khác nhau ⇒ bắt buộc ghi lý do (`adjustReason`).
   *
   * Vì sao bắt buộc: số đề xuất tính từ định lượng món × số suất, trừ tồn kho FEFO. Người
   * đặt sửa tay là có lý do thật (hao hụt sơ chế, mua gộp cho ngày sau, NCC bán nguyên
   * kiện…) — nhưng nếu không ghi lại thì kế toán và bên duyệt giá không đối chiếu được, và
   * lần sau không ai biết con số đề xuất sai ở đâu để chỉnh công thức.
   */
  suggestedQuantity?: number;
  /** Lý do lệch so với `suggestedQuantity` — bắt buộc khi có lệch, đi kèm vào `note`. */
  adjustReason?: string;
}

function emptyRow(): OrderRow {
  return { ingredient: "", supplierId: "", quantity: "", unit: "", note: "" };
}

/** Dòng có lệch so với số hệ thống đề xuất ⇒ phải ghi lý do trước khi gửi. */
function isAdjusted(r: OrderRow): boolean {
  if (r.suggestedQuantity == null) return false;
  const typed = Number(r.quantity);
  if (!Number.isFinite(typed)) return false;
  // So theo sai số nhỏ để không bắt lý do vì chênh lệch làm tròn số thập phân.
  return Math.abs(typed - r.suggestedQuantity) > 0.0005;
}

/** Đơn vị liên tục (đo khối lượng/thể tích) — được đặt số lẻ. Còn lại là đơn vị ĐẾM, phải
 *  làm tròn LÊN số nguyên (không mua được "1.67 bó"). */
const CONTINUOUS_UNITS = new Set(["kg", "g", "lít", "ml"]);

/**
 * PROPOSED (plan "Đơn vị đo lường" §Phase B.9) — quy đổi nhu cầu (đơn vị CHUẨN của nguyên
 * liệu) sang số lượng ĐẶT THEO ĐÚNG NCC (purchaseUnit + conversionToBase của NCC đó).
 * `packagingSize`/nguyên kiện (nếu có) vẫn do backend làm tròn tiếp lúc `POST
 * /purchase-requests` — hàm này chỉ lo bước quy đổi ĐƠN VỊ, không lo kiện hàng.
 */
function toSupplierOrderQuantity(baseQuantity: number, conversionToBase: number | undefined, purchaseUnit: string): number {
  const factor = conversionToBase && conversionToBase > 0 ? conversionToBase : 1;
  const converted = baseQuantity / factor;
  return CONTINUOUS_UNITS.has(purchaseUnit) ? Number(converted.toFixed(3)) : Math.ceil(converted);
}

/**
 * Quy đổi số lượng từ đơn vị công thức (recipeUnit) sang đơn vị chuẩn của database (dbUnit)
 * dùng chung bảng tra cứu toBaseQuantity.
 */
function toIngredientDbQuantity(qty: number, recipeUnit: string, dbUnit: string): number {
  const base = toBaseQuantity(qty, recipeUnit);
  const dbBase = toBaseQuantity(1, dbUnit);
  if (base.unit !== dbBase.unit) {
    return qty;
  }
  return base.qty / dbBase.qty;
}

function CreateOrderContent() {
  // ⚠️ NGOÀI PHẠM VI MF04 (03/08/2026): `GET /partners` chỉ
  // `hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')` (PartnerController) — SERVICE_CATERERS
  // gọi vào sẽ 403. Chưa tự mở rộng @PreAuthorize (đọc "toàn bộ đối tác" không scope
  // theo trường là lỗ hổng lộ dữ liệu xuyên trường) — cần Mạnh (MF04)/Huy (MF06 sở hữu
  // màn này) chốt: hoặc thêm biến thể "danh sách rút gọn" cho vai trò Caterer, hoặc giữ
  // đọc mảng tĩnh `PARTNERS` ở màn này như trước.
  const { data: partnersPage } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const partners = partnersPage?.items ?? [];
  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");
  const router = useRouter();
  const searchParams = useSearchParams();
  const create = useCreate<CreatePurchaseRequestBody, PurchaseRequestDto>("purchase-requests");

  /**
   * Nhận xét Minh 26/07: đọc thực đơn qua `issuableMenus()` (mảng SEED tĩnh) nên
   * thực đơn tạo/lập từ màn Thực đơn (đi qua API/db sống) không bao giờ thấy ở
   * đây — bấm "Tạo đơn đặt hàng bổ sung" từ màn Xuất kho sang luôn ra danh
   * sách rỗng. Đổi sang đọc `menus` qua API, giống cách `inventory/issue` đã sửa.
   */
  const { data: menusPage } = usePagedList<MenuDto>("menus", { pageSize: 200 });
  const liveMenus = useMemo(
    () => (menusPage?.items ?? []).filter((m) => m.status === "PUBLISHED"),
    [menusPage]
  );

  /**
   * Mở từ màn Xuất kho khi thiếu hàng: ?date=&meal= — tính lại phần thiếu ngay tại
   * đây thay vì bê số qua URL, để đơn luôn khớp tồn kho hiện tại. Nối backend thật
   * 05/08/2026: cùng cách `inventory/issue/page.tsx` — gộp nhu cầu từ `blocksData`
   * (đã nối thật) rồi gọi `GET /inventory/issue-plan` (FEFO server-side) thay cho
   * `mealIssueShortages()` (mock, đọc mảng seed).
   */
  /*
   * ĐẶT HÀNG THEO THỰC ĐƠN NHIỀU NGÀY (Nam chốt 08/08/2026).
   *
   * Trước bản này màn chỉ tính được nhu cầu cho ĐÚNG MỘT (ngày × bữa), và chỉ khi được
   * deep-link từ màn Xuất kho bằng `?date=&meal=` — vào thẳng màn này thì không có đường nào
   * lấy nhu cầu từ thực đơn cả. Thực tế bếp đặt hàng theo đợt cho vài ngày tới, không phải
   * từng bữa một.
   *
   * `slotKey` = "yyyy-mm-dd|MEALTYPE". Deep-link cũ vẫn chạy: nó nạp sẵn đúng một slot.
   */
  const linkedDate = searchParams.get("date") ?? "";
  const linkedMeal = (searchParams.get("meal") ?? "") as MealType | "";
  const [selectedSlots, setSelectedSlots] = useState<string[]>(
    linkedDate && linkedMeal ? [`${linkedDate}|${linkedMeal}`] : []
  );

  /** Mọi (ngày × bữa) CÓ thực đơn đã công bố, từ hôm nay trở đi — nguồn cho danh sách chọn. */
  const availableSlots = useMemo(() => {
    const today = new Date().toISOString().slice(0, 10);
    const set = new Map<string, { date: string; meal: MealType; menuNames: string[] }>();
    for (const m of liveMenus) {
      const date = m.menuDateIso ?? "";
      if (!date || date < today) continue;
      for (const b of m.blocksData ?? []) {
        const meal = (b.mealType ?? "") as MealType;
        if (!meal) continue;
        const key = `${date}|${meal}`;
        const cur = set.get(key) ?? { date, meal, menuNames: [] };
        cur.menuNames.push(m.name ?? m.id ?? "");
        set.set(key, cur);
      }
    }
    return [...set.entries()]
      .map(([key, v]) => ({ key, ...v }))
      .sort((a, b) => a.key.localeCompare(b.key));
  }, [liveMenus]);


  /** Thực đơn đã công bố thuộc các (ngày × bữa) ĐANG CHỌN. */
  const sourceMenus = useMemo(
    () =>
      liveMenus.flatMap((m) =>
        (m.blocksData ?? [])
          .filter((b) => selectedSlots.includes(`${m.menuDateIso ?? ""}|${b.mealType ?? ""}`))
          .map((b) => ({ menu: m, block: b }))
      ),
    [liveMenus, selectedSlots]
  );

  /**
   * Nổ định lượng món (BOM) → nhu cầu theo nguyên liệu, GỘP toàn bộ các buổi đang chọn.
   *
   * QUY ĐỔI VỀ ĐƠN VỊ CHUẨN của database (catalog_ingredients.unit) trước khi nhân số suất:
   * Tránh unit mismatch giữa frontend và backend (ví dụ: recipe ghi "180 ml" nhưng database dùng
   * "ml", frontend không được tự tiện quy đổi thành "lít" vì backend issue-plan so khớp thô không
   * tự quy đổi đơn vị).
   */
  const demand = useMemo(() => {
    const byIngredient = new Map<string, { ingredientId: string; total: number; dishes: Set<string> }>();
    for (const { menu, block } of sourceMenus) {
      const servings = menu.servings ?? 0;
      for (const dish of block.dishes ?? []) {
        for (const ing of dish.ingredients ?? []) {
          if (!ing.ingredientId) continue;
          const cat = ingredientCatalog.find((c) => c.id === ing.ingredientId);
          const dbUnit = cat?.unit ?? ing.unit ?? "";
          const needed = toIngredientDbQuantity(Number(ing.quantity) || 0, ing.unit ?? "", dbUnit) * servings;
          if (needed <= 0) continue;
          const existing = byIngredient.get(ing.ingredientId);
          if (existing) {
            existing.total += needed;
            existing.dishes.add(dish.name ?? "");
          } else {
            byIngredient.set(ing.ingredientId, {
              ingredientId: ing.ingredientId,
              total: needed,
              dishes: new Set([dish.name ?? ""]),
            });
          }
        }
      }
    }
    return [...byIngredient.values()];
  }, [sourceMenus, ingredientCatalog]);

  /** Tính nhu cầu nguyên liệu cho MỘT buổi (date × meal). */
  const getDemandForSlot = useMemo(() => {
    return (date: string, meal: MealType) => {
      const byIngredient = new Map<string, { ingredientId: string; total: number; dishes: Set<string> }>();
      const slotMenus = liveMenus.flatMap((m) =>
        m.menuDateIso === date
          ? (m.blocksData ?? [])
              .filter((b) => b.mealType === meal)
              .map((b) => ({ menu: m, block: b }))
          : []
      );
      for (const { menu, block } of slotMenus) {
        const servings = menu.servings ?? 0;
        for (const dish of block.dishes ?? []) {
          for (const ing of dish.ingredients ?? []) {
            if (!ing.ingredientId) continue;
            const cat = ingredientCatalog.find((c) => c.id === ing.ingredientId);
            const dbUnit = cat?.unit ?? ing.unit ?? "";
            const needed = toIngredientDbQuantity(Number(ing.quantity) || 0, ing.unit ?? "", dbUnit) * servings;
            if (needed <= 0) continue;
            const existing = byIngredient.get(ing.ingredientId);
            if (existing) {
              existing.total += needed;
              existing.dishes.add(dish.name ?? "");
            } else {
              byIngredient.set(ing.ingredientId, {
                ingredientId: ing.ingredientId,
                total: needed,
                dishes: new Set([dish.name ?? ""]),
              });
            }
          }
        }
      }
      return [...byIngredient.values()];
    };
  }, [liveMenus, ingredientCatalog]);

  /**
   * Trừ tồn kho: `GET /inventory/issue-plan` (FEFO server-side) gọi cho TỪNG buổi đang chọn
   * rồi cộng phần thiếu lại.
   *
   * Vì sao không gộp một lời gọi: endpoint nhận đúng một cặp `serviceDate`/`mealType` và tính
   * FEFO theo bữa đó. Gộp thành một lời gọi sẽ phải bịa ra một ngày đại diện — sai với ý
   * nghĩa "lô nào dùng cho bữa nào" mà chính endpoint này bảo đảm.
   */
  const slotPlans = useQueries({
    queries: selectedSlots.map((key) => {
      const [date, meal] = key.split("|") as [string, MealType];
      const slotDemand = getDemandForSlot(date, meal);
      return {
        queryKey: ["inventory", "issue-plan", key, slotDemand.map((d) => `${d.ingredientId}:${d.total}`)],
        queryFn: async () => {
          const params: Record<string, string> = { serviceDate: date, mealType: meal };
          for (const d of slotDemand) params[`demand[${d.ingredientId}]`] = String(d.total);
          const { data } = await api.get<IssuePlanDto>("/inventory/issue-plan", { params });
          return data;
        },
        enabled: slotDemand.length > 0,
      };
    }),
  });

  const plansReady = selectedSlots.length > 0 && slotPlans.every((q) => q.isSuccess);

  /** Nguyên liệu công thức có nhưng danh mục chưa có — gộp mọi buổi, bỏ trùng. */
  const unknownIngredients = [
    ...new Set(slotPlans.flatMap((q) => q.data?.unknownIngredients ?? [])),
  ];

  /**
   * Phần THIẾU sau khi trừ tồn, gộp mọi buổi. Cùng nguyên liệu xuất hiện ở nhiều buổi thì
   * cộng dồn — bếp đặt một dòng cho cả đợt, không đặt lặp từng bữa.
   */
  const prefill: ShortageLine[] = useMemo(() => {
    const byIngredient = new Map<string, ShortageLine>();
    for (const q of slotPlans) {
      for (const sh of q.data?.shortages ?? []) {
        const id = sh.ingredientId ?? "";
        if (!id) continue;
        const cur = byIngredient.get(id);
        if (cur) {
          cur.quantity += sh.shortfall ?? 0;
        } else {
          byIngredient.set(id, {
            ingredientId: id,
            ingredientName: sh.ingredientName ?? "",
            quantity: sh.shortfall ?? 0,
            unit: sh.unit ?? "",
            dishes: [...(demand.find((d) => d.ingredientId === id)?.dishes ?? [])],
          });
        }
      }
    }
    return [...byIngredient.values()].map((l) => ({ ...l, quantity: Number(l.quantity.toFixed(3)) }));
    /* `slotPlans` là mảng mới mỗi render nên khoá theo dấu thời gian dữ liệu, không theo tham chiếu. */
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [slotPlans.map((q) => q.dataUpdatedAt).join(","), demand]);

  const suppliers = partners.filter(
    (p) => p.type === "RAW_FOOD_SUPPLIER" && p.status === "APPROVED"
  );

  /**
   * Mặt hàng của MỌI NCC đang Approved — khác bản cũ (lọc theo một `supplierId` chọn ở
   * đầu form). Giờ mỗi DÒNG tự chọn NCC nên cần thấy hết ai bán gì để gợi ý.
   */
  const { data: productPage } = usePagedList<SupplierProductDto>("supplier-products", {
    pageSize: 300,
  });
  const allSupplierProducts = supplierProductDtosToMock(productPage?.items ?? []);

  /** Chỉ tính sản phẩm của NCC đang Approved — NCC bị đình chỉ không được gợi ý/chọn. */
  const approvedProducts = allSupplierProducts.filter((p) =>
    suppliers.some((s) => s.id === p.supplierId)
  );

  /**
   * Danh mục nguyên liệu cho Ô "Nguyên liệu" — trước 22/07/2026 đổ ra cả danh mục 46 mục
   * bất kể có NCC nào bán không, nên đặt được "Cá thu" từ chỗ không kinh doanh nó. Nay chỉ
   * liệt kê nguyên liệu có ÍT NHẤT MỘT NCC Approved bán, kèm giá rẻ nhất làm gợi ý.
   */
  const ingredientOptions = useMemo(() => {
    const byName = new Map<string, { catalog: IngredientDto; best: SupplierProduct }>();
    for (const p of approvedProducts) {
      const cat = ingredientCatalog.find((c) => c.id === p.ingredientId);
      const name = cat?.name;
      if (!cat || !name) continue;
      const cur = byName.get(name);
      if (!cur || p.unitPrice < cur.best.unitPrice) byName.set(name, { catalog: cat, best: p });
    }
    return [...byName.values()].sort((a, b) => (a.catalog.name ?? "").localeCompare(b.catalog.name ?? "", "vi"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [approvedProducts.length, ingredientCatalog.length]);

  /** Mọi NCC Approved đang bán ĐÚNG nguyên liệu này, giá thấp → cao (mặc định = rẻ nhất). */
  function suppliersForIngredient(ingredientName: string) {
    return approvedProducts
      .filter((p) => ingredientCatalog.find((c) => c.id === p.ingredientId)?.name === ingredientName)
      .map((p) => ({ product: p, supplierName: suppliers.find((s) => s.id === p.supplierId)?.name ?? p.supplierId }))
      .sort((a, b) => a.product.unitPrice - b.product.unitPrice);
  }

  const [deliveryDate, setDeliveryDate] = useState("");
  const [rows, setRows] = useState<OrderRow[]>([emptyRow()]);
  const [error, setError] = useState("");

  /**
   * Áp dòng điền sẵn MỘT LẦN, đúng lúc dữ liệu API (menus/tồn kho/NCC) đã tải xong. Mỗi
   * dòng tự gán sẵn NCC rẻ nhất (`bestPriceOf`) — người dùng đổi được ở cột "Nhà cung cấp".
   * Trước đây dùng `useState(() => …)` — hàm khởi tạo đó chỉ chạy ở lần render ĐẦU, lúc
   * `usePagedList` còn `undefined` (chưa fetch xong), nên luôn đông cứng ở kết quả rỗng.
   */
  const appliedKey = useRef("");
  useEffect(() => {
    if (!plansReady) return;
    // Nạp lại MỖI KHI đổi tập buổi đang chọn. Khoá theo tập buổi để không ghi đè khi người
    // dùng đang gõ.
    const key = selectedSlots.join(",");
    if (appliedKey.current === key) return;
    appliedKey.current = key;
    setRows(
      prefill.length > 0
        ? prefill.map((l) => {
            const best = bestPriceOf(l.ingredientId, allSupplierProducts, partners);
            const unit = best?.unit ?? l.unit;
            const qty = best ? toSupplierOrderQuantity(l.quantity, best.conversionToBase, best.unit) : l.quantity;
            return {
              ingredient: l.ingredientName,
              supplierId: best?.supplierId ?? "",
              quantity: String(qty),
              unit,
              note: `Cho ${l.dishes.join(", ")}`,
              baseQuantity: l.quantity,
              suggestedQuantity: qty,
            };
          })
        : [emptyRow()]
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [plansReady, prefill, selectedSlots]);

  /** Dòng đã chọn nguyên liệu nhưng chưa có NCC nào gán được (không ai bán, hoặc bị bỏ chọn). */
  const unassignedRows = rows.filter((r) => r.ingredient && !r.supplierId);

  /** Gom các dòng ĐÃ có NCC theo từng NCC — mỗi nhóm sẽ thành MỘT đơn riêng lúc gửi. */
  const supplierGroups = useMemo(() => {
    const map = new Map<string, OrderRow[]>();
    for (const r of rows) {
      if (!r.ingredient || !r.supplierId) continue;
      const list = map.get(r.supplierId) ?? [];
      list.push(r);
      map.set(r.supplierId, list);
    }
    return [...map.entries()].map(([supplierId, list]) => ({
      supplierId,
      rows: list,
      name: suppliers.find((s) => s.id === supplierId)?.name ?? supplierId,
    }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rows, suppliers.length]);

  function updateRow(index: number, patch: Partial<OrderRow>) {
    /**
     * CHẶN chọn trùng (nguyên liệu × NCC) (Minh báo 25/07/2026, mở rộng 10/08/2026 khi mỗi
     * dòng có NCC riêng): hai dòng CÙNG nguyên liệu CÙNG NCC thì phía NCC báo giá + gán lô
     * theo map `tên → lô` trong CÙNG một đơn — hai dòng đè nhau, một dòng mất lô/mất tiền.
     * Cùng nguyên liệu nhưng KHÁC NCC thì hợp lệ (sẽ tách thành 2 đơn khác nhau).
     */
    if (patch.ingredient) {
      const nextSupplierId = patch.supplierId ?? rows[index]?.supplierId ?? "";
      const dupIndex = rows.findIndex(
        (r, i) => i !== index && r.ingredient === patch.ingredient && r.supplierId === nextSupplierId && nextSupplierId
      );
      if (dupIndex >= 0) {
        toast.warning(
          `"${patch.ingredient}" (cùng NCC) đã có ở dòng #${dupIndex + 1} — sửa số lượng tại dòng đó thay vì chọn thêm lần nữa.`
        );
        return;
      }
    }
    setRows((prev) =>
      prev.map((r, i) => {
        if (i !== index) return r;
        const next = { ...r, ...patch };
        // Chọn nguyên liệu MỚI → gán sẵn NCC RẺ NHẤT + khóa đơn vị theo đúng dòng giá NCC đó
        // (không phải đơn vị chuẩn của danh mục) — giá quote theo unit nào thì đặt hàng phải
        // cùng unit đó, nếu không tổng tiền tính sai âm thầm.
        if (patch.ingredient !== undefined) {
          const options = suppliersForIngredient(patch.ingredient);
          const def = options[0];
          const cat = ingredientCatalog.find((c) => c.name === patch.ingredient);
          next.supplierId = def?.product.supplierId ?? "";
          next.unit = def?.product.unit ?? cat?.unit ?? "";
        }
        return next;
      })
    );
  }

  /** Đổi NCC CỦA RIÊNG một dòng — tính lại đơn vị/số lượng theo đúng cách NCC mới quy đổi. */
  function updateRowSupplier(index: number, supplierId: string) {
    setRows((prev) =>
      prev.map((r, i) => {
        if (i !== index) return r;
        const options = suppliersForIngredient(r.ingredient);
        const match = options.find((x) => x.product.supplierId === supplierId);
        if (!match) return { ...r, supplierId };
        if (r.baseQuantity != null) {
          const qty = toSupplierOrderQuantity(r.baseQuantity, match.product.conversionToBase, match.product.unit);
          return { ...r, supplierId, unit: match.product.unit, quantity: String(qty), suggestedQuantity: qty };
        }
        // Dòng tự gõ tay (không có `baseQuantity` neo) — số lượng đã gõ là theo ĐƠN VỊ CŨ,
        // đổi NCC đổi luôn đơn vị nên số đó không còn đúng nghĩa — yêu cầu gõ lại thay vì
        // giữ nguyên con số cũ gắn nhầm đơn vị mới.
        return { ...r, supplierId, unit: match.product.unit, quantity: "" };
      })
    );
  }

  async function submit() {
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
    const unassigned = validRows.filter((r) => !r.supplierId);
    if (unassigned.length > 0) {
      setError(
        `Chưa chọn nhà cung cấp cho: ${unassigned.map((r) => r.ingredient).join(", ")} — chọn NCC ở cột "Nhà cung cấp" của từng dòng trước khi gửi.`
      );
      return;
    }
    /*
     * Sửa số lượng khác đề xuất thì PHẢI ghi lý do (Nam chốt 08/08/2026). Con số đề xuất
     * tính từ định lượng món × số suất rồi trừ tồn FEFO; ai sửa tay đều có lý do thật, nhưng
     * không ghi lại thì bên duyệt giá và kế toán không đối chiếu được, và lần sau không ai
     * biết công thức sai ở đâu để sửa.
     */
    const missingReason = validRows.filter((r) => isAdjusted(r) && !(r.adjustReason ?? "").trim());
    if (missingReason.length > 0) {
      setError(
        `Đã sửa số lượng khác đề xuất ở: ${missingReason.map((r) => r.ingredient).join(", ")}. `
          + "Ghi rõ lý do điều chỉnh cho từng dòng (ô “Lý do điều chỉnh”) trước khi gửi."
      );
      return;
    }
    // Chốt chặn cuối trước khi gửi — trùng (nguyên liệu × NCC) là mất lô ở phía NCC (map
    // tên → lô, trong CÙNG một đơn); trùng nguyên liệu nhưng KHÁC NCC thì hợp lệ.
    const seen = new Set<string>();
    const dupNames: string[] = [];
    for (const r of validRows) {
      const key = `${r.ingredient}::${r.supplierId}`;
      if (seen.has(key)) dupNames.push(r.ingredient);
      else seen.add(key);
    }
    if (dupNames.length > 0) {
      setError(
        `Nguyên liệu bị trùng dòng CÙNG một NCC: ${[...new Set(dupNames)].join(", ")} — gộp về một dòng và cộng số lượng.`
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

    /**
     * `POST /purchase-requests` thật chỉ nhận `{supplierId, deliveryDate, items:
     * [{ingredientId, quantity, unit, note}]}` — server tự tính `totalValue`/
     * `requestedDate`/`status=PENDING` (D9/D12). `ingredientId` tra từ
     * `GET /ingredients` thật (nối 06/08/2026, `IngredientController`) — `id` chính là
     * `catalog_ingredients.code`, không còn lệch với mảng mock cũ nữa.
     *
     * Gom theo NCC rồi gửi TUẦN TỰ, KHÔNG song song (đo thật 10/08/2026, cùng lớp lỗi với
     * bulk-submit ở `/supplier/partnerships/[id]/catalog`): mỗi `POST /purchase-requests`
     * cần 2 connection cùng lúc (transaction ngoài + `CodeGenerator.next()` REQUIRES_NEW
     * sinh mã PR-xxxx) — bắn N đơn song song dễ cạn Hikari pool khi N đủ lớn.
     */
    const groups = new Map<string, OrderRow[]>();
    for (const r of validRows) {
      const list = groups.get(r.supplierId) ?? [];
      list.push(r);
      groups.set(r.supplierId, list);
    }

    let okCount = 0;
    const failedNames: string[] = [];
    for (const [supplierId, groupRows] of groups) {
      const supplier = suppliers.find((s) => s.id === supplierId);
      if (!supplier?.id) {
        failedNames.push(supplierId);
        continue;
      }
      const items = groupRows
        .map((r) => ({
          ingredientId: ingredientCatalog.find((c) => c.name === r.ingredient)?.id ?? "",
          quantity: Number(r.quantity),
          unit: r.unit,
          /*
           * Lý do điều chỉnh đi CÙNG `note` — `purchase_items` chỉ có một cột ghi chú, và đây
           * là thứ bên duyệt giá cần đọc ngay tại dòng chứ không phải tra ở chỗ khác.
           */
          note: [r.note.trim(), isAdjusted(r) && r.adjustReason?.trim()
            ? `Điều chỉnh từ ${r.suggestedQuantity} ${r.unit}: ${r.adjustReason.trim()}`
            : ""]
            .filter(Boolean).join(" · ") || undefined,
        }))
        .filter((it) => it.ingredientId);
      if (items.length === 0) continue;
      try {
        await create.mutateAsync({ supplierId: supplier.id, deliveryDate, items });
        okCount++;
      } catch {
        failedNames.push(supplier.name ?? supplierId);
      }
    }

    if (okCount > 0) {
      toast.success(
        okCount > 1
          ? `Đã gửi ${okCount} đơn tới ${okCount} nhà cung cấp — mỗi NCC sẽ gửi giá theo hợp đồng để bạn duyệt.`
          : "Đã gửi đơn — NCC sẽ gửi giá theo hợp đồng để bạn duyệt, rồi gán lô hàng theo từng nguyên liệu."
      );
    }
    if (failedNames.length > 0) {
      setError(`${failedNames.length} đơn gửi thất bại: ${failedNames.join(", ")} — thử lại các dòng của NCC đó.`);
    } else if (okCount > 0) {
      router.push("/service-caterers/orders");
    }
  }

  return (
    <>
      <PageHeader
        title="Tạo đơn đặt hàng nguyên liệu"
        description="Mỗi dòng tự chọn nhà cung cấp (mặc định rẻ nhất, đổi được) — gửi đi ở trạng thái PENDING, tự tách thành nhiều đơn nếu có nhiều NCC; NCC gửi giá theo hợp đồng, bạn duyệt giá, NCC gán lô hàng (BR-071) rồi giao"
        actions={
          <Link href="/service-caterers/orders">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      {/*
        * CHỌN BUỔI CẦN ĐẶT HÀNG — thêm 08/08/2026.
        * Đây là đường vào duy nhất để lấy nhu cầu từ thực đơn khi vào thẳng màn này; trước đó
        * chỉ deep-link `?date=&meal=` từ màn Xuất kho mới tính được, và chỉ một bữa.
        */}
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>1 · Chọn buổi cần đặt nguyên liệu</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {availableSlots.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              Chưa có thực đơn nào đã công bố từ hôm nay trở đi. Lập và công bố thực đơn ở
              “Lịch thực đơn tuần” rồi quay lại, hoặc tự thêm dòng nguyên liệu bên dưới.
            </p>
          ) : (
            <>
              <div className="flex flex-wrap gap-2">
                {availableSlots.map((slot) => {
                  const on = selectedSlots.includes(slot.key);
                  return (
                    <button
                      key={slot.key}
                      type="button"
                      onClick={() =>
                        setSelectedSlots((cur) =>
                          on ? cur.filter((k) => k !== slot.key) : [...cur, slot.key]
                        )
                      }
                      className={
                        on
                          ? "rounded-lg border border-primary bg-primary-soft px-3 py-2 text-left text-sm"
                          : "rounded-lg border px-3 py-2 text-left text-sm hover:bg-muted"
                      }
                      aria-pressed={on}
                    >
                      <span className="block font-medium">
                        {slot.date.split("-").reverse().join("/")} · {MEAL_TYPE_LABEL[slot.meal]}
                      </span>
                      <span className="block text-xs text-muted-foreground">
                        {slot.menuNames.length} thực đơn
                      </span>
                    </button>
                  );
                })}
              </div>
              <p className="text-xs text-muted-foreground">
                Chọn được nhiều buổi — nhu cầu của các buổi được CỘNG DỒN thành một đơn, mỗi
                nguyên liệu một dòng. Số lượng = định lượng món × số suất, trừ tồn kho khả dụng.
              </p>
              {selectedSlots.length > 0 && !plansReady && (
                <p className="text-xs text-muted-foreground">Đang tính nhu cầu…</p>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {error && (
        <Alert variant="danger" className="mb-4" title="Chưa thể gửi đơn">
          {error}
        </Alert>
      )}

      {selectedSlots.length > 0 && sourceMenus.length > 0 && (
        <Alert variant="info" className="mb-4" title="Nhu cầu tính từ thực đơn đã công bố">
          {prefill.length} dòng điền sẵn cho <b>{selectedSlots.length} buổi</b> đang chọn — hệ
          thống nổ định lượng từng món × số suất, rồi trừ tồn kho khả dụng (FEFO), và tự gán
          sẵn NCC rẻ nhất cho từng dòng (đổi được ở cột “Nhà cung cấp”). Sửa lại được, nhưng
          sửa khác số đề xuất thì phải ghi lý do.
          {unknownIngredients.length > 0 && (
            <p className="mt-2">
              Không điền sẵn được: <b>{unknownIngredients.join(", ")}</b> — chưa có trong danh
              mục nguyên liệu nên không đặt theo mã lô được (BR-076). Bổ sung vào danh mục rồi
              đặt riêng.
            </p>
          )}
        </Alert>
      )}

      {unassignedRows.length > 0 && (
        <Alert variant="warning" className="mb-4" title="Còn dòng chưa chọn được nhà cung cấp">
          {unassignedRows.map((r) => r.ingredient).join(", ")} — chưa NCC Approved nào bán mặt
          hàng này. Chọn NCC khác cho dòng đó ở cột “Nhà cung cấp”, hoặc bỏ dòng nếu chưa cần
          đặt ngay.
        </Alert>
      )}

      {/* Tự tách theo NCC lúc gửi — mỗi nhóm là một đơn riêng (1 đơn = 1 pháp nhân). */}
      {supplierGroups.length > 0 && (
        <Alert
          variant="info"
          className="mb-6"
          title={
            supplierGroups.length > 1
              ? `Sẽ tách thành ${supplierGroups.length} đơn — mỗi nhà cung cấp một đơn riêng`
              : "Đơn sẽ gửi tới 1 nhà cung cấp"
          }
        >
          Đơn đặt hàng là chứng từ với một pháp nhân nên không gộp nhiều NCC vào một đơn — bấm
          “Gửi đơn” một lần, hệ thống tự tách và gửi lần lượt theo nhóm dưới đây.
          <ul className="mt-2 list-inside list-disc">
            {supplierGroups.map((g) => (
              <li key={g.supplierId}>
                {g.name} — {g.rows.length} mặt hàng
              </li>
            ))}
          </ul>
        </Alert>
      )}

      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Thông tin đơn</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="max-w-xs space-y-1.5">
            <Label htmlFor="od-date">Ngày giao yêu cầu * (≥ ngày mai — BR-071)</Label>
            <Input
              id="od-date"
              type="date"
              value={deliveryDate}
              onChange={(e) => setDeliveryDate(e.target.value)}
            />
            <p className="text-xs text-muted-foreground">Áp dụng cho tất cả đơn được tách ra.</p>
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
          {rows.map((r, i) => {
            const supplierOptions = r.ingredient ? suppliersForIngredient(r.ingredient) : [];
            return (
              /*
               * UI (10/08/2026, phản hồi): trước dùng `items-end` (canh theo ĐÁY cả hàng) —
               * dòng nào có thêm 2 dòng chữ gợi ý bên dưới ô "Số lượng" (Đề xuất/Nhu cầu quy
               * đổi) thì cả hàng đó bị đẩy lệch so với dòng ít chữ hơn, các ô input không còn
               * thẳng hàng ngang giữa các dòng. Đổi sang `items-start` + mỗi cột đều có Label
               * (kể cả nút xoá, Label ẩn `invisible` để giữ chiều cao) — input luôn nằm ngay
               * dưới label bất kể bên dưới có bao nhiêu dòng chữ phụ.
               */
              <div key={i} className="grid items-start gap-2 sm:grid-cols-[1.2fr_1.1fr_90px_80px_1fr_36px]">
                <div className="space-y-1">
                  <Label className="text-xs text-muted-foreground">Nguyên liệu *</Label>
                  <Select value={r.ingredient} onChange={(e) => updateRow(i, { ingredient: e.target.value })}>
                    <option value="">— Chọn nguyên liệu —</option>
                    {ingredientOptions.map(({ catalog, best }) => (
                      <option key={catalog.id} value={catalog.name}>
                        {catalog.name} — từ {best.unitPrice.toLocaleString("vi-VN")}₫/{best.unit}
                      </option>
                    ))}
                    {/* Giữ lại dòng đã chọn nếu không NCC nào (còn) bán, để người dùng thấy
                        mà xử lý thay vì ô trống không hiểu vì sao. */}
                    {r.ingredient && !ingredientOptions.some((x) => x.catalog.name === r.ingredient) && (
                      <option value={r.ingredient}>{r.ingredient} — chưa NCC nào bán</option>
                    )}
                  </Select>
                </div>
                <div className="space-y-1">
                  <Label className="text-xs text-muted-foreground">Nhà cung cấp *</Label>
                  <Select
                    value={r.supplierId}
                    disabled={!r.ingredient}
                    onChange={(e) => updateRowSupplier(i, e.target.value)}
                    className={!r.supplierId && r.ingredient ? "border-warning" : undefined}
                  >
                    <option value="">— Chọn NCC —</option>
                    {supplierOptions.map(({ product, supplierName }) => (
                      <option key={product.supplierId} value={product.supplierId}>
                        {supplierName} — {product.unitPrice.toLocaleString("vi-VN")}₫/{product.unit}
                        {product.packaging ? ` · ${product.packaging}` : ""}
                      </option>
                    ))}
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
                  {r.suggestedQuantity != null && (
                    <p className={isAdjusted(r) ? "text-xs font-medium text-warning" : "text-xs text-muted-foreground"}>
                      Đề xuất {r.suggestedQuantity} {r.unit}
                      {isAdjusted(r) ? " — bạn đã sửa, cần ghi lý do" : ""}
                    </p>
                  )}
                  {r.baseQuantity != null && String(r.baseQuantity) !== r.quantity && (
                    <p className="text-xs text-muted-foreground">
                      Nhu cầu {r.baseQuantity} — đã quy đổi theo cách NCC đóng gói (Proposed)
                    </p>
                  )}
                </div>
                <div className="space-y-1">
                  <Label className="text-xs text-muted-foreground">Đơn vị</Label>
                  {/* Khóa theo đúng đơn vị NCC báo giá (không cho gõ/chọn tự do) — nếu
                      lệch với đơn vị đặt hàng thì thành tiền tính sai âm thầm (VD giá
                      theo "kg" mà đặt "thùng" thì server nhân giá/kg với số lượng thùng). */}
                  <Input value={r.unit || "—"} disabled />
                </div>
                <div className="space-y-1">
                  <Label className="text-xs text-muted-foreground">
                    {isAdjusted(r) ? "Lý do điều chỉnh *" : "Ghi chú"}
                  </Label>
                  {/*
                    * Sửa số lượng khác đề xuất ⇒ ô này đổi thành "Lý do điều chỉnh" và BẮT BUỘC
                    * (Nam chốt 08/08/2026). Không gộp vào ô ghi chú chung vì bên duyệt giá cần
                    * đọc đúng lý do lệch ngay tại dòng, tách khỏi ghi chú giao nhận.
                    */}
                  <Input
                    value={isAdjusted(r) ? (r.adjustReason ?? "") : r.note}
                    onChange={(e) =>
                      updateRow(i, isAdjusted(r) ? { adjustReason: e.target.value } : { note: e.target.value })
                    }
                    className={isAdjusted(r) && !(r.adjustReason ?? "").trim() ? "border-warning" : ""}
                    placeholder={
                      isAdjusted(r)
                        ? "VD: mua gộp cho thứ 3, hoặc NCC bán nguyên thùng 20kg"
                        : "VD: giao trước 06:00"
                    }
                  />
                </div>
                <div className="space-y-1">
                  <Label className="invisible text-xs">Xoá</Label>
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
              </div>
            );
          })}
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
