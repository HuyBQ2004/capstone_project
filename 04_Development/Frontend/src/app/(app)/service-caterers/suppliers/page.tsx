"use client";

import { useState } from "react";
import {
  Building2,
  CheckCircle2,
  Mail,
  MapPin,
  PackageSearch,
  Phone,
  Search,
  Store,
  Tag,
  XCircle,
} from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useDetail, usePagedList, usePlainList } from "@/lib/api/hooks";
import type { IngredientDto, PartnerDetailDto, PartnerDto, SupplierProductDto } from "@/lib/api/types";

function fmtMoney(amount?: number | null) {
  if (amount == null) return "—";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(amount);
}

function SupplierProductList({ supplier }: { supplier: PartnerDto }) {
  const [productSearch, setProductSearch] = useState("");

  const { data: supplierDetail } = useDetail<PartnerDetailDto>("partners", supplier.id ?? "");
  const { data: productPage, isLoading } = usePagedList<SupplierProductDto>("supplier-products", {
    supplierId: supplier.id ?? "",
    pageSize: 200,
  });
  const { data: ingredientList } = usePlainList<IngredientDto>("ingredients");

  const products = productPage?.items ?? [];
  const ingredients = ingredientList ?? [];
  const ingredientMap = new Map((ingredients as IngredientDto[]).map((i: IngredientDto) => [i.id ?? "", i.name ?? ""]));

  const detail = supplierDetail ?? (supplier as unknown as PartnerDetailDto);

  const filteredProducts = products.filter(
    (p: SupplierProductDto) =>
      p.label?.toLowerCase().includes(productSearch.trim().toLowerCase()) ||
      p.id?.toLowerCase().includes(productSearch.trim().toLowerCase()) ||
      p.ingredientId?.toLowerCase().includes(productSearch.trim().toLowerCase())
  );

  return (
    <div className="space-y-4">
      {/* Thông tin nhà cung cấp */}
      <div className="rounded-xl border bg-muted/40 p-4 space-y-2 text-sm">
        <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
          <div className="flex items-center gap-2">
            <Building2 className="size-4 text-muted-foreground shrink-0" />
            <span className="font-semibold text-foreground">{detail.name || supplier.name}</span>
            <span className="text-xs text-muted-foreground">({supplier.id})</span>
          </div>
          {detail.taxId && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Tag className="size-4 shrink-0" />
              <span>MST: {detail.taxId}</span>
            </div>
          )}
          {detail.phone && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Phone className="size-4 shrink-0" />
              <span>{detail.phone}</span>
            </div>
          )}
          {detail.email && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Mail className="size-4 shrink-0" />
              <span>{detail.email}</span>
            </div>
          )}
        </div>
        {detail.address && (
          <div className="flex items-start gap-2 pt-1 text-xs text-muted-foreground">
            <MapPin className="size-3.5 mt-0.5 shrink-0" />
            <span>{detail.address}</span>
          </div>
        )}
      </div>

      {/* Thanh tìm kiếm sản phẩm */}
      <div className="flex items-center justify-between gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            className="h-8 pl-8 text-sm"
            placeholder="Tìm theo tên mặt hàng, mã nguyên liệu…"
            value={productSearch}
            onChange={(e) => setProductSearch(e.target.value)}
          />
        </div>
        <div className="text-xs text-muted-foreground font-medium">
          Hiển thị {filteredProducts.length} / {products.length} mặt hàng
        </div>
      </div>

      {/* Bảng danh mục sản phẩm */}
      <div className="overflow-hidden rounded-xl border bg-card">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/60">
              <TableHead className="w-12 text-center">STT</TableHead>
              <TableHead>Mặt hàng chào bán</TableHead>
              <TableHead>Nguyên liệu chuẩn</TableHead>
              <TableHead className="text-right">Đơn giá (VNĐ)</TableHead>
              <TableHead className="w-24">Đơn vị</TableHead>
              <TableHead className="w-32">Quy cách</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center text-muted-foreground">
                  Đang nạp danh mục sản phẩm…
                </TableCell>
              </TableRow>
            ) : filteredProducts.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center text-muted-foreground">
                  Chưa có mặt hàng cung ứng nào từ nhà cung cấp này.
                </TableCell>
              </TableRow>
            ) : (
              filteredProducts.map((p: SupplierProductDto, idx: number) => {
                const ingName = ingredientMap.get(p.ingredientId ?? "") ?? p.ingredientId;
                return (
                  <TableRow key={p.id || idx}>
                    <TableCell className="text-center text-xs text-muted-foreground">
                      {idx + 1}
                    </TableCell>
                    <TableCell className="font-medium">
                      <div>{p.label || "—"}</div>
                      <div className="text-[11px] text-muted-foreground font-mono">{p.id}</div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="primary" className="gap-1 font-normal">
                        <Tag className="size-3 text-primary" />
                        {ingName || "—"}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right font-semibold text-emerald-600 dark:text-emerald-400">
                      {fmtMoney(p.unitPrice)}
                    </TableCell>
                    <TableCell className="text-sm">{p.unit || "—"}</TableCell>
                    <TableCell className="text-xs text-muted-foreground">{p.packaging || "—"}</TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}

export default function CatererSuppliersPage() {
  const [search, setSearch] = useState("");
  const [selectedSupplier, setSelectedSupplier] = useState<PartnerDto | null>(null);

  /*
   * Lấy danh sách Nhà cung cấp hợp tác với trường.
   *
   * `type` phải là "RAW_FOOD_SUPPLIER" — giá trị D6 thật của `PartnerType`
   * (`ck_partner_type CHECK (partner_type IN ('RAW_FOOD_SUPPLIER','SERVICE_CATERER'))`).
   * Trước 08/08/2026 chỗ này gửi "SUPPLIER": server lọc EXACT nên khớp 0 dòng và màn LUÔN
   * ghi "Hợp tác (0) — Không tìm thấy nhà cung cấp hợp tác nào", kể cả khi trường đang có
   * hợp tác APPROVED. Lỗi im lặng: không 4xx, chỉ là danh sách rỗng.
   */
  const { data: partnerPage, isLoading } = usePagedList<PartnerDto>("partners", {
    type: "RAW_FOOD_SUPPLIER",
    pageSize: 100,
  });

  const suppliers = partnerPage?.items ?? [];

  const filteredSuppliers = suppliers.filter(
    (s: PartnerDto) =>
      s.name?.toLowerCase().includes(search.trim().toLowerCase()) ||
      s.id?.toLowerCase().includes(search.trim().toLowerCase())
  );

  return (
    <>
      <PageHeader
        title="Nhà cung cấp & Danh mục Sản phẩm"
        description="Tra cứu danh sách các đối tác cung cấp thực đơn / nguyên liệu thô đã kết nối hợp tác với trường và bảng giá mặt hàng chào bán"
      />

      <Card>
        <CardHeader className="flex-row flex-wrap items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2 text-lg">
            <Store className="size-5 text-primary" />
            <span>Đối tác Cung ứng Hợp tác ({filteredSuppliers.length})</span>
          </CardTitle>
          <div className="relative w-72">
            <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              className="h-9 pl-8 text-sm"
              placeholder="Tìm theo tên nhà cung cấp, mã đối tác…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="py-12 text-center text-muted-foreground animate-pulse">
              Đang tải danh sách nhà cung cấp hợp tác…
            </div>
          ) : filteredSuppliers.length === 0 ? (
            <div className="py-12 text-center text-muted-foreground">
              Không tìm thấy nhà cung cấp hợp tác nào.
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
              {filteredSuppliers.map((supplier: PartnerDto, idx: number) => {
                const isActive = supplier.status === "APPROVED" || supplier.status === "ACTIVE";
                return (
                  <Card
                    key={supplier.id || idx}
                    className="flex flex-col justify-between overflow-hidden transition-all hover:border-primary/50 hover:shadow-md"
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <CardTitle className="text-base font-semibold leading-tight">
                            {supplier.name}
                          </CardTitle>
                          <CardDescription className="font-mono text-xs mt-0.5">
                            {supplier.id}
                          </CardDescription>
                        </div>
                        <Badge variant={isActive ? "success" : "neutral"} className="shrink-0">
                          {isActive ? (
                            <span className="flex items-center gap-1">
                              <CheckCircle2 className="size-3" /> Hợp tác
                            </span>
                          ) : (
                            <span className="flex items-center gap-1">
                              <XCircle className="size-3" /> {supplier.status || "Khác"}
                            </span>
                          )}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-2.5 text-xs text-muted-foreground">
                      <div className="flex items-center gap-2">
                        <Tag className="size-3.5 shrink-0" />
                        <span>Loại: {supplier.type === "RAW_FOOD_SUPPLIER" ? "Nhà cung cấp nguyên liệu" : supplier.type || "—"}</span>
                      </div>
                      {supplier.certStatus && (
                        <div className="flex items-center gap-2">
                          <CheckCircle2 className="size-3.5 shrink-0 text-emerald-500" />
                          <span>Chứng nhận ATTP: {supplier.certStatus}</span>
                        </div>
                      )}
                    </CardContent>
                    <div className="border-t bg-muted/20 p-3 text-right">
                      <Button
                        size="sm"
                        variant="outline"
                        className="w-full gap-2 font-medium"
                        onClick={() => setSelectedSupplier(supplier)}
                      >
                        <PackageSearch className="size-4 text-primary" />
                        Xem danh mục sản phẩm
                      </Button>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialog xem danh mục sản phẩm chào bán của NCC được chọn */}
      <Dialog open={selectedSupplier != null} onOpenChange={(open) => !open && setSelectedSupplier(null)}>
        <DialogContent className="max-w-4xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-lg">
              <PackageSearch className="size-5 text-primary" />
              <span>Danh mục Sản phẩm Cung ứng — {selectedSupplier?.name}</span>
            </DialogTitle>
            <DialogDescription>
              Bảng giá và danh mục mặt hàng nguyên liệu chào bán của {selectedSupplier?.name} ({selectedSupplier?.id})
            </DialogDescription>
          </DialogHeader>

          {selectedSupplier && <SupplierProductList supplier={selectedSupplier} />}
        </DialogContent>
      </Dialog>
    </>
  );
}
