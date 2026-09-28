"use client";

import { useState } from "react";
import { PackageSearch, Pencil, Plus } from "lucide-react";
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
import { Input, Label } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import {
  CURRENT_SUPPLIER_ID,
  INGREDIENT_CATALOG,
  type CatalogIngredient,
  type SupplierProduct,
} from "@/lib/mock-data";

/**
 * Mặt hàng nhà cung cấp đang cung ứng cho trường.
 *
 * Trước 22/07/2026 bảng `supplier-products` chỉ ĐỌC được: nhà cung cấp muốn bán
 * thêm một mặt hàng thì không có đường nào ngoài sửa seed. Mà đây là gốc của cả
 * chuỗi mua hàng — không khai mặt hàng thì không có giá để trường ước tính chi
 * phí bữa ăn, không tạo được lô, và đơn đặt hàng gửi cho thứ mình không bán.
 *
 * Mặt hàng phải chọn từ DANH MỤC CHUẨN của trường, không gõ tên tự do: đó là chỗ
 * hai bên gặp nhau. Nhà cung cấp gọi "Tôm sú đông lạnh" còn thực đơn ghi "Tôm sú"
 * thì nhãn riêng để ở `label`, còn khoá vẫn là một.
 */

interface Draft {
  ingredientId: string;
  label: string;
  unitPrice: string;
  packaging: string;
  effectiveFrom: string;
}

function todayVn(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

/**
 * Combobox có tìm kiếm — thay thế <select> thuần khi danh sách dài (>20 mục).
 * Catalog nguyên liệu có thể tăng theo số trường liên kết nên cần filter nhanh.
 */
function IngredientCombobox({
  id,
  value,
  options,
  disabled,
  onChange,
}: {
  id: string;
  value: string;
  options: CatalogIngredient[];
  disabled?: boolean;
  onChange: (id: string) => void;
}) {
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);

  const selected = options.find((c) => c.id === value)
    ?? INGREDIENT_CATALOG.find((c) => c.id === value); // khi disabled, value có thể không có trong options
  const filtered = query.trim()
    ? options.filter((c) =>
        c.name.toLowerCase().includes(query.trim().toLowerCase())
      )
    : options;

  function pick(c: CatalogIngredient) {
    onChange(c.id);
    setQuery("");
    setOpen(false);
  }

  function handleBlur() {
    // Trì hoãn để click vào option kịp kích hoạt trước khi đóng
    setTimeout(() => setOpen(false), 150);
  }

  if (disabled) {
    return (
      <Input
        id={id}
        value={selected ? `${selected.name} (${selected.unit})` : "—"}
        disabled
      />
    );
  }

  return (
    <div className="relative">
      <Input
        id={id}
        autoComplete="off"
        placeholder="Gõ tên nguyên liệu để tìm…"
        value={open ? query : (selected ? `${selected.name} (${selected.unit})` : "")}
        onFocus={() => { setOpen(true); setQuery(""); }}
        onChange={(e) => setQuery(e.target.value)}
        onBlur={handleBlur}
      />
      {open && (
        <ul
          role="listbox"
          className="absolute z-50 mt-1 max-h-56 w-full overflow-auto rounded-lg border border-input bg-card py-1 shadow-lg text-sm"
        >
          {filtered.length === 0 ? (
            <li className="px-3 py-2 text-muted-foreground">
              Không tìm thấy nguyên liệu nào.
            </li>
          ) : (
            filtered.map((c) => (
              <li
                key={c.id}
                role="option"
                aria-selected={c.id === value}
                onMouseDown={() => pick(c)}
                className="flex cursor-pointer items-center justify-between px-3 py-2 hover:bg-muted aria-selected:bg-muted/60 aria-selected:font-medium"
              >
                <span>{c.name}</span>
                <span className="ml-2 text-xs text-muted-foreground">{c.unit}</span>
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  );
}

function ProductDialog({
  product,
  existing,
  onClose,
}: {
  product?: SupplierProduct;
  existing: SupplierProduct[];
  onClose: () => void;
}) {
  const create = useCreate<Partial<SupplierProduct>>("supplier-products");
  const update = useUpdate<{ id: string } & Partial<SupplierProduct>>("supplier-products");
  const [d, setD] = useState<Draft>({
    ingredientId: product?.ingredientId ?? "",
    label: product?.label ?? "",
    unitPrice: product ? String(product.unitPrice) : "",
    packaging: product?.packaging ?? "",
    effectiveFrom: product?.effectiveFrom ?? todayVn(),
  });
  const [error, setError] = useState("");
  const catalog = INGREDIENT_CATALOG.find((c) => c.id === d.ingredientId);
  const isPending = create.isPending || update.isPending;

  // Các nguyên liệu NCC này CHƯA đăng ký — chỉ cho chọn những thứ chưa khai
  const registeredIds = new Set(existing.map((p) => p.ingredientId));
  const availableIngredients = INGREDIENT_CATALOG.filter(
    (c) => !registeredIds.has(c.id) || c.id === product?.ingredientId
  );

  function submit() {
    if (!d.ingredientId) {
      setError("Chọn nguyên liệu từ danh mục.");
      return;
    }
    // Một nhà cung cấp chỉ có MỘT dòng cho mỗi nguyên liệu: đổi giá là sửa dòng đó
    // kèm ngày hiệu lực mới, không tạo dòng thứ hai — hai dòng cùng mặt hàng thì
    // không ai biết giá nào đang áp dụng.
    const clash = existing.find(
      (p) => p.id !== product?.id && p.ingredientId === d.ingredientId
    );
    if (clash) {
      setError(
        `Đã khai mặt hàng này rồi (${clash.id}) — sửa dòng đó để cập nhật giá, đừng tạo dòng thứ hai.`
      );
      return;
    }
    const price = Number(d.unitPrice);
    if (!(price > 0)) {
      setError("Đơn giá phải lớn hơn 0.");
      return;
    }
    if (!/^\d{2}\/\d{2}\/\d{4}$/.test(d.effectiveFrom)) {
      setError("Ngày hiệu lực theo định dạng dd/mm/yyyy.");
      return;
    }
    setError("");
    const body = {
      supplierId: CURRENT_SUPPLIER_ID,
      ingredientId: d.ingredientId,
      label: d.label.trim() || undefined,
      unit: catalog?.unit ?? "kg",
      unitPrice: price,
      packaging: d.packaging.trim() || undefined,
      effectiveFrom: d.effectiveFrom,
    };
    const done = () => {
      toast.success(
        product
          ? `Đã cập nhật ${catalog?.name} — giá mới áp dụng từ ${d.effectiveFrom}.`
          : `Đã thêm ${catalog?.name} vào danh sách mặt hàng cung ứng.`
      );
      onClose();
    };
    if (product) update.mutate({ id: product.id, ...body }, { onSuccess: done });
    else create.mutate({ id: `SIP-${String(Date.now()).slice(-6)}`, ...body }, { onSuccess: done });
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{product ? `Sửa mặt hàng — ${catalog?.name}` : "Thêm mặt hàng cung ứng"}</DialogTitle>
          <DialogDescription>
            Đơn giá dùng để trường ước tính chi phí mỗi suất ăn khi lập thực đơn (BR-080).
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          {error && <Alert variant="danger">{error}</Alert>}
          <div className="space-y-1.5">
            <Label htmlFor="pr-ing">Nguyên liệu *</Label>
            <IngredientCombobox
              id="pr-ing"
              value={d.ingredientId}
              options={availableIngredients}
              disabled={product != null}
              onChange={(id) => setD({ ...d, ingredientId: id })}
            />
            {!product && availableIngredients.length === 0 && (
              <p className="text-xs text-muted-foreground">
                Bạn đã khai đủ tất cả nguyên liệu trong danh mục. Nếu cần bổ sung
                nguyên liệu mới, hãy liên hệ trường để mở rộng danh mục.
              </p>
            )}
            {product && (
              <p className="text-xs text-muted-foreground">
                Không đổi được nguyên liệu của mặt hàng đã khai — các lô hàng đang trỏ về
                đây. Cần đổi thì ngừng mặt hàng này và khai mặt hàng mới.
              </p>
            )}
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="pr-label">Tên trên hoá đơn/bao bì (nếu khác)</Label>
            <Input
              id="pr-label"
              value={d.label}
              onChange={(e) => setD({ ...d, label: e.target.value })}
              placeholder={catalog ? `Mặc định: ${catalog.name}` : "VD: Tôm sú đông lạnh size 30"}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="pr-price">Đơn giá * (₫/{catalog?.unit ?? "đơn vị"})</Label>
              <Input
                id="pr-price"
                type="number"
                min={0}
                value={d.unitPrice}
                onChange={(e) => setD({ ...d, unitPrice: e.target.value })}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pr-eff">Hiệu lực từ *</Label>
              <Input
                id="pr-eff"
                value={d.effectiveFrom}
                onChange={(e) => setD({ ...d, effectiveFrom: e.target.value })}
                placeholder="dd/mm/yyyy"
              />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="pr-pack">Quy cách đóng gói</Label>
            <Input
              id="pr-pack"
              value={d.packaging}
              onChange={(e) => setD({ ...d, packaging: e.target.value })}
              placeholder="VD: Bao 25kg · Khay 30 quả · Can 5 lít"
            />
            <p className="text-xs text-muted-foreground">
              Dùng khi làm tròn số lượng đặt hàng — trường cần 3.2kg thì đặt tròn theo quy
              cách của bên bán.
            </p>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={isPending} onClick={submit}>
            {product ? "Lưu thay đổi" : "Thêm mặt hàng"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function SupplierProductsPage() {
  const { data, isLoading } = usePagedList<SupplierProduct>("supplier-products", {
    pageSize: 200,
    supplierId: CURRENT_SUPPLIER_ID,
  });
  const products = data?.items ?? [];
  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<SupplierProduct | null>(null);

  const nameOf = (id: string) => INGREDIENT_CATALOG.find((c) => c.id === id)?.name ?? id;

  return (
    <>
      <PageHeader
        title="Mặt hàng cung ứng"
        description="Danh mục mặt hàng và đơn giá gửi tới trường — cơ sở để trường ước tính chi phí bữa ăn và đặt hàng"
        actions={
          <Button onClick={() => setCreating(true)}>
            <Plus /> Thêm mặt hàng
          </Button>
        }
      />

      <Alert variant="info" className="mb-6" title="Cách khai mặt hàng cung ứng">
        Chọn nguyên liệu từ danh mục chuẩn của trường để lô hàng nhập kho được nối đúng
        về công thức món ăn và chuỗi truy xuất. Tên riêng của bạn trên hoá đơn/bao bì
        (ví dụ: &ldquo;Tôm sú đông lạnh size 30&rdquo;) khai ở ô &ldquo;Tên trên hoá đơn&rdquo;.
        Các nguyên liệu bạn đã đăng ký không hiện lại trong danh sách — sửa giá thì dùng
        nút &ldquo;Sửa giá&rdquo; ở từng dòng.
      </Alert>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <PackageSearch className="size-4 text-muted-foreground" />
            {products.length} mặt hàng đang cung ứng
          </CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="h-40 animate-pulse rounded-lg bg-muted" />
          ) : products.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              Chưa khai mặt hàng nào — trường sẽ không đặt hàng được từ đơn vị của bạn.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nguyên liệu</TableHead>
                  <TableHead>Tên trên hoá đơn</TableHead>
                  <TableHead className="text-right">Đơn giá</TableHead>
                  <TableHead>Quy cách</TableHead>
                  <TableHead>Hiệu lực từ</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {products.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">{nameOf(p.ingredientId)}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {p.label ?? <span className="text-xs">— dùng tên danh mục —</span>}
                    </TableCell>
                    <TableCell className="text-right font-medium">
                      {p.unitPrice.toLocaleString("vi-VN")}₫/{p.unit}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {p.packaging ?? "—"}
                    </TableCell>
                    <TableCell>
                      <Badge variant="neutral">{p.effectiveFrom}</Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button variant="outline" size="sm" onClick={() => setEditing(p)}>
                        <Pencil /> Sửa giá
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {(creating || editing) && (
        <ProductDialog
          product={editing ?? undefined}
          existing={products}
          onClose={() => {
            setCreating(false);
            setEditing(null);
          }}
        />
      )}
    </>
  );
}
