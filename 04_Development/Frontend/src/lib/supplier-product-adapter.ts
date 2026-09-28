import type { SupplierProductDto } from "@/lib/api/types";

/**
 * Giai đoạn 3 (07/08/2026) — `SupplierProduct` dẫn xuất trực tiếp từ `SupplierProductDto`
 * bằng `Pick<>` thay vì khai tay độc lập (interface cũ ở `mock-data.ts` đã xoá). Bỏ 2 field
 * mock cũ `brand`/`minimumOrderQty` — xác nhận DEAD (0 nơi đọc, và DTO thật vốn cũng không
 * có 2 field này, xem ghi chú tại `lib/api/types.ts`).
 */
export type SupplierProduct = Pick<
  SupplierProductDto,
  "label" | "packaging" | "conversionToBase" | "conversionNote"
> & {
  id: string;
  supplierId: string;
  /** Khoá về danh mục chuẩn của trường — một nguyên liệu, nhiều NCC cùng bán. */
  ingredientId: string;
  /** Đơn vị báo giá, khớp `CatalogIngredient.unit`. */
  unit: string;
  unitPrice: number;
  effectiveFrom: string;
};

/**
 * Adapter `SupplierProductDto` (thật, `GET /supplier-products`) → `SupplierProduct` (narrow
 * required) — dùng khi gọi `bestPriceOf()`/`sourcePartnerOf()` (`lib/mock-data.ts`). Lọc bỏ
 * dòng thiếu field bắt buộc (không nên xảy ra — server luôn ghi đủ
 * `ingredientId`/`unit`/`unitPrice` lúc tạo, D9) để tránh dòng "giá 0đ" giả.
 */
export function supplierProductDtosToMock(items: SupplierProductDto[]): SupplierProduct[] {
  return items
    .filter((p): p is SupplierProductDto & { id: string; supplierId: string; ingredientId: string; unit: string; unitPrice: number } =>
      p.id != null && p.supplierId != null && p.ingredientId != null && p.unit != null && p.unitPrice != null)
    .map((p) => ({
      id: p.id,
      supplierId: p.supplierId,
      ingredientId: p.ingredientId,
      label: p.label,
      unit: p.unit,
      unitPrice: p.unitPrice,
      packaging: p.packaging,
      effectiveFrom: p.effectiveFrom ?? "",
      conversionToBase: p.conversionToBase,
      conversionNote: p.conversionNote,
    }));
}
