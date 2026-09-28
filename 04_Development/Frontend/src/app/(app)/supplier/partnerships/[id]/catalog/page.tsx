"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, ListPlus, Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { formatVnDate } from "@/lib/format";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList, usePlainList, useCreateAt } from "@/lib/api/hooks";
import type {
  CatalogItemDto,
  ChangeRequestDto,
  IngredientDto,
  PartnershipDto,
  SupplierProductDto,
} from "@/lib/api/types";
import { useAuthStore } from "@/lib/stores/auth-store";

/**
 * [Proposed] Trang riêng cho danh mục sản phẩm-giá đã duyệt của một hợp đồng hợp tác +
 * đề xuất thay đổi — xem `Proposal-Partnership-Catalog-Contract.md`. Tách khỏi dialog
 * ở `supplier/partnerships` (07/08/2026, phản hồi: dialog chật, khó dùng).
 *
 * Sửa 07/08/2026 (phản hồi lần 2): trường hợp phổ biến nhất là NCC vừa ký hợp đồng
 * GIẤY với một trường, cần khai HÀNG LOẠT nguyên liệu + giá đúng theo hợp đồng ngay
 * lập tức — không phải sửa từng dòng một qua form phức tạp. Thêm khối "Thiết lập
 * hàng loạt": tick + sửa giá thẳng trên bảng, lấy mặc định từ "Mặt hàng cung ứng"
 * (danh mục chung, `supplier-products`) đã khai sẵn. Form chi tiết (đơn vị/quy cách
 * đóng gói riêng) giữ lại bên dưới cho ca lẻ/đặc biệt.
 */
/*
 * 13/08/2026 (theo Nam) — trang từng render hết TOÀN BỘ danh mục đã duyệt trong 1 bảng
 * không phân trang (lấy `pageSize:100` rồi in thẳng), dài không kiểm soát khi hợp đồng có
 * nhiều nguyên liệu. Đổi sang `DataTable` (server-side phân trang, cùng cách `supplier/
 * products` đã làm) — CHỈ đổi bảng HIỂN THỊ; state `items` (usePagedList pageSize:100) vẫn
 * giữ nguyên để tính `alreadyCovered`/`bulkCandidates` bên dưới (logic đó cần biết ĐỦ danh
 * mục đã duyệt, không phải chỉ 1 trang).
 */
const CATALOG_COLUMNS: ColumnDef<CatalogItemDto, unknown>[] = [
  {
    accessorKey: "ingredientName",
    header: "Nguyên liệu",
    cell: ({ row }) => <span className="font-medium">{row.original.ingredientName ?? row.original.ingredientId}</span>,
  },
  {
    id: "price",
    header: "Đơn giá",
    cell: ({ row }) => {
      const i = row.original;
      return (
        <div className="text-right">
          {(i.approvedUnitPrice ?? 0).toLocaleString("vi-VN")}₫/{i.unit}
          <span className="block text-xs text-muted-foreground">
            {i.conversionToBase != null && i.conversionToBase !== 1
              ? `1 ${i.unit} = ${i.conversionToBase} ${i.ingredientUnit ?? "đơn vị chuẩn"}`
              : `Cùng đơn vị chuẩn của nguyên liệu${i.ingredientUnit ? ` (${i.ingredientUnit})` : ""}`}
          </span>
        </div>
      );
    },
  },
  {
    accessorKey: "packaging",
    header: "Quy cách",
    // Ràng buộc "chỉ nguyên kiện" quyết định số lượng trường đặt được, nên phải ĐỌC ĐƯỢC ở
    // đây — trước 13/08/2026 nó chỉ tồn tại trong DB, không màn nào hiện ra (Nam hỏi "chưa
    // cho cấu hình để chỉ được phép mua số nguyên theo đơn vị bán à?").
    cell: ({ row }) => {
      const i = row.original;
      return (
        <div className="text-muted-foreground">
          {i.packaging ?? "—"}
          {i.allowPartialPackage === false && (
            <span className="mt-0.5 block text-xs text-warning">
              Chỉ nguyên kiện{i.packagingSize ? ` — bội số ${i.packagingSize} ${i.unit}` : ""}
            </span>
          )}
        </div>
      );
    },
  },
  {
    id: "effective",
    header: "Hiệu lực",
    cell: ({ row }) => {
      const i = row.original;
      return (
        <span className="text-sm text-muted-foreground">
          {i.effectiveFrom ? `từ ${formatVnDate(i.effectiveFrom)}` : "—"}
          {i.effectiveTo ? ` đến ${formatVnDate(i.effectiveTo)}` : ""}
        </span>
      );
    },
  },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
  },
];

export default function PartnershipCatalogPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const partnerId = useAuthStore((s) => s.user?.partnerId);

  // Không có GET /supplier-partnerships/{id} — lấy từ danh sách (cùng cách trang cha đọc).
  const { data: partnershipsPage, isLoading: loadingPartnership } = usePagedList<PartnershipDto>(
    "supplier-partnerships",
    { pageSize: 100 }
  );
  const partnership = (partnershipsPage?.items ?? []).find((p) => p.id === id);

  const { data: itemsPage } = usePagedList<CatalogItemDto>(`partnerships/${id}/catalog-items`, {
    pageSize: 100,
  });
  const items = itemsPage?.items ?? [];
  const { data: requestsPage } = usePagedList<ChangeRequestDto>("catalog-change-requests", { pageSize: 200 });
  const requests = (requestsPage?.items ?? []).filter((r) => r.partnershipId === id);
  /*
   * Đề xuất CÒN VIỆC PHẢI THEO DÕI. Đề xuất đã duyệt biến thành dòng trong "Danh mục đã duyệt"
   * nên liệt kê lại chỉ tạo cảm giác dữ liệu bị nhân đôi; bị từ chối thì vẫn phải thấy để còn
   * biết lý do mà sửa.
   */
  const openRequests = requests.filter((r) => r.status !== "APPROVED");

  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");
  const { data: myProductsPage } = usePagedList<SupplierProductDto>("supplier-products", {
    pageSize: 300,
    supplierId: partnerId,
  });
  const myProducts = myProductsPage?.items ?? [];

  // `invalidate` BẮT BUỘC ở đây: hook chỉ tự làm mới `["partnerships"]`, còn hai bảng của
  // màn này đọc bằng key khác hẳn — thiếu nó thì gửi đề xuất xong bảng "Đề xuất đang chờ"
  // vẫn trống cho tới khi F5 (13/08/2026, xem Javadoc `useCreateAt`).
  const createRequest = useCreateAt<Record<string, unknown>, ChangeRequestDto>(
    "partnerships",
    `${id}/catalog-change-requests`,
    { invalidate: ["catalog-change-requests", `partnerships/${id}/catalog-items`] }
  );

  // ───────────────────────── Thiết lập hàng loạt ─────────────────────────
  /*
   * 13/08/2026 (theo Nam) — trước đây MỌI nguyên liệu đã có dòng ACTIVE bị loại khỏi bảng
   * này vĩnh viễn, kể cả khi giá ở "Mặt hàng cung ứng" (supplier-products, danh mục CHUNG
   * của NCC) đã đổi khác giá đang duyệt cho hợp đồng này — không có gợi ý/nút nào để gửi
   * lại đề xuất cập nhật giá, người dùng phải tự nhớ vào "Đề xuất riêng lẻ" tìm đúng dòng.
   * Nay vẫn loại nguyên liệu đã có đề xuất PENDING (tránh gửi trùng), nhưng GIỮ LẠI nguyên
   * liệu ACTIVE nếu giá hiện tại ở "Mặt hàng cung ứng" khác giá đã duyệt — đánh dấu riêng
   * để phân biệt với nguyên liệu hoàn toàn mới.
   */
  const pendingIngredientIds = new Set(requests.filter((r) => r.status === "PENDING").map((r) => r.ingredientId));
  const approvedItemByIngredient = new Map(items.filter((i) => i.status === "ACTIVE").map((i) => [i.ingredientId, i]));
  const approvedPriceByIngredient = new Map(
    items.filter((i) => i.status === "ACTIVE").map((i) => [i.ingredientId, i.approvedUnitPrice])
  );
  function priceChanged(p: SupplierProductDto): boolean {
    if (!approvedPriceByIngredient.has(p.ingredientId)) return false;
    const approved = approvedPriceByIngredient.get(p.ingredientId);
    return approved != null && p.unitPrice != null && Number(approved) !== Number(p.unitPrice);
  }
  const bulkCandidates = myProducts.filter(
    (p) =>
      p.ingredientId &&
      !pendingIngredientIds.has(p.ingredientId) &&
      (!approvedPriceByIngredient.has(p.ingredientId) || priceChanged(p))
  );

  const [selected, setSelected] = useState<Record<string, boolean>>({});
  const [bulkPrices, setBulkPrices] = useState<Record<string, string>>({});
  const [bulkReason, setBulkReason] = useState("Thiết lập theo hợp đồng đã ký với trường");
  const [bulkSubmitting, setBulkSubmitting] = useState(false);

  const selectedCount = Object.values(selected).filter(Boolean).length;
  const allSelected = bulkCandidates.length > 0 && bulkCandidates.every((p) => selected[p.id ?? ""]);

  function priceOf(product: SupplierProductDto): string {
    return bulkPrices[product.id ?? ""] ?? (product.unitPrice != null ? String(product.unitPrice) : "");
  }

  function toggleAll() {
    const next: Record<string, boolean> = {};
    if (!allSelected) {
      for (const p of bulkCandidates) if (p.id) next[p.id] = true;
    }
    setSelected(next);
  }

  async function submitBulk() {
    const rows = bulkCandidates.filter((p) => p.id && selected[p.id]);
    if (rows.length === 0) {
      toast.error("Chọn ít nhất 1 nguyên liệu.");
      return;
    }
    const invalid = rows.find((p) => !(Number(priceOf(p)) > 0));
    if (invalid) {
      toast.error(`Giá của "${ingredientCatalog.find((c) => c.id === invalid.ingredientId)?.name ?? invalid.label}" phải lớn hơn 0.`);
      return;
    }
    setBulkSubmitting(true);
    /*
     * TUẦN TỰ, KHÔNG Promise.allSettled/song song (đo thật 10/08/2026): mỗi
     * `POST /catalog-change-requests` giữ 1 connection cho transaction ngoài + xin thêm 1
     * connection cho `CodeGenerator.next()` (REQUIRES_NEW, sinh mã CCR-xxxx) — 2
     * connection/request. Bắn 16 request song song ⇒ 32 connection cần cùng lúc, vượt xa
     * pool Hikari mặc định (10) ⇒ tự bó tay nhau, phần lớn chết theo timeout 10s của
     * client trong khi backend vẫn treo tới khi Hikari bỏ cuộc ở 30s ("Could not open JPA
     * EntityManager for transaction"). Gửi tuần tự giữ tối đa 2 connection cho tính năng
     * này tại một thời điểm — chậm hơn nhưng không làm sập pool của cả hệ thống.
     */
    const results: { status: "fulfilled" | "rejected" }[] = [];
    for (const p of rows) {
      try {
        // 13/08/2026 — BẮT BUỘC gửi kèm `catalogItemId` khi nguyên liệu đã có dòng ACTIVE
        // (đây là đề xuất CẬP NHẬT GIÁ, không phải khai mới): thiếu field này, lúc trường
        // duyệt backend sẽ tạo MỘT DÒNG DANH MỤC MỚI thay vì cập nhật dòng cũ
        // (`CatalogChangeRequestServiceImpl.applyApprovedItem` — `catalogItemId == null` ⇒
        // `new PartnershipCatalogItem()`), tạo ra 2 dòng ACTIVE trùng nguyên liệu.
        const existing = approvedItemByIngredient.get(p.ingredientId);
        await createRequest.mutateAsync({
          catalogItemId: existing?.id || undefined,
          ingredientId: p.ingredientId,
          requestedUnit: p.unit,
          requestedUnitPrice: Number(priceOf(p)),
          // PROPOSED — quy đổi đã khai sẵn ở "Mặt hàng cung ứng" (mặc định 1 nếu unit
          // trùng đơn vị chuẩn) — chuyển nguyên sang đây, không bắt khai lại lần hai.
          requestedConversionToBase: p.conversionToBase ?? 1,
          requestedConversionNote: p.conversionNote || undefined,
          requestedPackaging: p.packaging || undefined,
          reason:
            bulkReason.trim() ||
            (existing
              ? "Cập nhật giá theo Mặt hàng cung ứng"
              : "Thiết lập theo hợp đồng đã ký với trường"),
        });
        results.push({ status: "fulfilled" });
      } catch {
        results.push({ status: "rejected" });
      }
    }
    setBulkSubmitting(false);
    const ok = results.filter((r) => r.status === "fulfilled").length;
    const failed = results.length - ok;
    if (ok > 0) toast.success(`Đã gửi ${ok} đề xuất — chờ trường duyệt.`);
    if (failed > 0) toast.error(`${failed} đề xuất gửi thất bại — thử lại các dòng còn tick.`);
    setSelected({});
  }

  // ───────────────────────── Form chi tiết (ca lẻ/đặc biệt) ─────────────────────────
  const [formOpen, setFormOpen] = useState(false);
  const [catalogItemId, setCatalogItemId] = useState("");
  const [selectedProductId, setSelectedProductId] = useState("");
  const [ingredientId, setIngredientId] = useState("");
  const [requestedUnit, setRequestedUnit] = useState("");
  const [baseUnit, setBaseUnit] = useState("");
  const [requestedUnitPrice, setRequestedUnitPrice] = useState("");
  const [requestedConversionToBase, setRequestedConversionToBase] = useState("1");
  const [requestedConversionNote, setRequestedConversionNote] = useState("");
  const [requestedPackaging, setRequestedPackaging] = useState("");
  const [requestedPackagingSize, setRequestedPackagingSize] = useState("");
  const [requestedAllowPartialPackage, setRequestedAllowPartialPackage] = useState(true);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  function resetForm() {
    setCatalogItemId("");
    setSelectedProductId("");
    setIngredientId("");
    setRequestedUnit("");
    setBaseUnit("");
    setRequestedUnitPrice("");
    setRequestedConversionToBase("1");
    setRequestedConversionNote("");
    setRequestedPackaging("");
    setRequestedPackagingSize("");
    setRequestedAllowPartialPackage(true);
    setReason("");
    setError("");
  }

  /** Sửa một dòng ĐÃ DUYỆT của chính hợp đồng này — điền sẵn giá trị hiện tại để chỉnh. */
  function pickExistingCatalogItem(itemId: string) {
    setCatalogItemId(itemId);
    const existing = items.find((i) => i.id === itemId);
    if (existing) {
      const matchingProduct = myProducts.find((p) => p.ingredientId === existing.ingredientId);
      setSelectedProductId(matchingProduct?.id ?? "");

      setIngredientId(existing.ingredientId ?? "");
      setRequestedUnit(existing.unit ?? "");
      setBaseUnit(ingredientCatalog.find((c) => c.id === existing.ingredientId)?.unit ?? "");
      setRequestedUnitPrice(existing.approvedUnitPrice != null ? String(existing.approvedUnitPrice) : "");
      setRequestedConversionToBase(existing.conversionToBase != null ? String(existing.conversionToBase) : "1");
      setRequestedConversionNote(existing.conversionNote ?? "");
      setRequestedPackaging(existing.packaging ?? "");
      // Ràng buộc nguyên kiện phải điền sẵn theo dòng đang sửa, không thì mỗi lần chỉnh giá
      // là vô tình gửi kèm `allowPartialPackage = true` và gỡ mất ràng buộc đã duyệt.
      setRequestedPackagingSize(existing.packagingSize != null ? String(existing.packagingSize) : "");
      setRequestedAllowPartialPackage(existing.allowPartialPackage ?? true);
    } else {
      setSelectedProductId("");
      setIngredientId("");
      setRequestedUnit("");
      setBaseUnit("");
      setRequestedUnitPrice("");
      setRequestedConversionToBase("1");
      setRequestedConversionNote("");
      setRequestedPackaging("");
      setRequestedPackagingSize("");
      setRequestedAllowPartialPackage(true);
    }
  }

  const unitDiffersFromBase = !!baseUnit && !!requestedUnit && requestedUnit.trim() !== baseUnit;

  function submit() {
    if (!ingredientId) { setError("Vui lòng chọn một mặt hàng cung ứng."); return; }
    const price = Number(requestedUnitPrice);
    if (!(price > 0)) { setError("Đơn giá đề xuất phải lớn hơn 0."); return; }
    if (!reason.trim()) { setError("Nêu lý do đề xuất để trường xem xét."); return; }
    // Bật "chỉ nguyên kiện" mà không có số lượng/kiện thì backend không làm tròn được gì —
    // đề xuất trông như đã khai ràng buộc nhưng thực tế vô hiệu, im lặng.
    if (!requestedAllowPartialPackage && !(Number(requestedPackagingSize) > 0)) {
      setError("Đã chọn \"chỉ bán nguyên kiện\" thì phải khai số lượng / 1 kiện lớn hơn 0.");
      return;
    }
    setError("");
    createRequest.mutate(
      {
        catalogItemId: catalogItemId || undefined,
        ingredientId,
        requestedUnit: requestedUnit.trim(),
        requestedUnitPrice: price,
        requestedConversionToBase: Number(requestedConversionToBase) > 0 ? Number(requestedConversionToBase) : 1,
        requestedConversionNote: requestedConversionNote.trim() || undefined,
        requestedPackaging: requestedPackaging.trim() || undefined,
        requestedPackagingSize: requestedPackagingSize ? Number(requestedPackagingSize) : undefined,
        requestedAllowPartialPackage,
        reason: reason.trim(),
      },
      {
        onSuccess: () => {
          toast.success("Đã gửi đề xuất — chờ trường duyệt.");
          resetForm();
          setFormOpen(false);
        },
        onError: () => toast.error("Gửi đề xuất thất bại — vui lòng thử lại."),
      }
    );
  }

  if (loadingPartnership) return <p className="text-sm text-muted-foreground">Đang tải…</p>;
  if (!partnership) return <Alert variant="danger" title="Không tìm thấy hợp đồng">Hợp đồng {id} không tồn tại.</Alert>;

  return (
    <>
      <PageHeader
        title={`Danh mục sản phẩm-giá — ${partnership.school}`}
        description="[Proposed] Chỉ các dòng đã duyệt mới được giao cho trường này. Đổi giá/thêm sản phẩm phải gửi đề xuất và chờ trường duyệt."
        actions={
          <Link href="/supplier/partnerships">
            <Button variant="outline"><ArrowLeft /> Trường hợp tác</Button>
          </Link>
        }
      />

      {/*
        13/08/2026 — trước đây "Danh mục đã duyệt" render hết trong 1 bảng không phân trang,
        dài không kiểm soát khi hợp đồng có nhiều nguyên liệu. Đổi sang `DataTable` (server-side
        phân trang + tìm kiếm, cùng cách `supplier/products`) — full-width thay vì chung hàng
        2 cột với "Đề xuất" (danh sách đó thường ngắn, giữ dạng bảng thường bên dưới).
      */}
      <Card className="mb-6">
        <CardHeader><CardTitle>Danh mục đã duyệt</CardTitle></CardHeader>
        <CardContent>
          <DataTable<CatalogItemDto>
            resource={`partnerships/${id}/catalog-items`}
            columns={CATALOG_COLUMNS}
            searchPlaceholder="Tìm theo tên nguyên liệu…"
            emptyMessage="Chưa có sản phẩm nào được duyệt cho trường này."
          />
        </CardContent>
      </Card>

      {/*
        CHỈ hiện đề xuất CHƯA xong việc. Trước 09/08/2026 bảng này liệt kê MỌI đề xuất, kể cả
        đã duyệt — mà đề xuất đã duyệt thì chính là các dòng ở bảng "Danh mục đã duyệt". Kết
        quả: hai bảng lặp lại y hệt dữ liệu, không nói thêm điều gì, chỉ làm người đọc tưởng dữ
        liệu bị nhân đôi.
      */}
      <Card>
        <CardHeader><CardTitle>Đề xuất đang chờ / bị từ chối</CardTitle></CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nguyên liệu</TableHead>
                <TableHead className="text-right">Giá đề xuất</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead>Phản hồi</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {openRequests.length === 0 ? (
                <TableRow><TableCell colSpan={4} className="text-center text-muted-foreground">
                  Không có đề xuất nào đang chờ — các mục đã duyệt nằm ở bảng bên trên.
                </TableCell></TableRow>
              ) : openRequests.map((r) => (
                <TableRow key={r.id}>
                  <TableCell className="font-medium">{r.ingredientName ?? r.ingredientId}</TableCell>
                  <TableCell className="text-right">
                    {(r.requestedUnitPrice ?? 0).toLocaleString("vi-VN")}₫/{r.requestedUnit}
                    {r.requestedConversionToBase != null && r.requestedConversionToBase !== 1 && (
                      <span className="block text-xs text-muted-foreground">
                        1 {r.requestedUnit} = {r.requestedConversionToBase} {r.ingredientUnit ?? "đơn vị chuẩn"}
                      </span>
                    )}
                  </TableCell>
                  <TableCell><StatusBadge status={r.status ?? ""} /></TableCell>
                  <TableCell className="max-w-64 text-sm text-muted-foreground">{r.decisionNote ?? "—"}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ListPlus className="size-4 text-muted-foreground" />
            Thiết lập hàng loạt theo hợp đồng
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Dùng khi vừa ký hợp đồng với trường này — tick các nguyên liệu đã thoả thuận,
            sửa giá nếu khác giá trong &ldquo;Mặt hàng cung ứng&rdquo; chung, rồi gửi một lần.
            Nguyên liệu đã duyệt nhưng giá ở &ldquo;Mặt hàng cung ứng&rdquo; vừa đổi cũng hiện
            lại ở đây (nhãn &ldquo;Giá đã đổi&rdquo;) để gửi đề xuất cập nhật.
          </p>
          {bulkCandidates.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              Mọi mặt hàng trong danh mục cung ứng chung của bạn đã được duyệt đúng giá hiện
              tại hoặc đang chờ duyệt cho trường này. Cần thêm mặt hàng mới? Khai ở &ldquo;Mặt
              hàng cung ứng&rdquo; trước, rồi quay lại đây.
            </p>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-10">
                      <input type="checkbox" checked={allSelected} onChange={toggleAll} aria-label="Chọn tất cả" />
                    </TableHead>
                    <TableHead>Nguyên liệu</TableHead>
                    <TableHead>Đơn giá đề xuất</TableHead>
                    <TableHead>Quy cách</TableHead>
                    <TableHead>Trạng thái</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {bulkCandidates.map((p) => {
                    const name = ingredientCatalog.find((c) => c.id === p.ingredientId)?.name ?? p.label ?? p.ingredientId;
                    const checked = !!selected[p.id ?? ""];
                    const changed = priceChanged(p);
                    const approvedPrice = approvedPriceByIngredient.get(p.ingredientId);
                    return (
                      <TableRow key={p.id}>
                        <TableCell>
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={(e) => setSelected((s) => ({ ...s, [p.id ?? ""]: e.target.checked }))}
                            aria-label={`Chọn ${name}`}
                          />
                        </TableCell>
                        <TableCell className="font-medium">{name}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-1">
                            <Input
                              type="number"
                              min={0}
                              className="w-32"
                              value={priceOf(p)}
                              disabled={!checked}
                              onChange={(e) => setBulkPrices((s) => ({ ...s, [p.id ?? ""]: e.target.value }))}
                            />
                            <span className="text-sm text-muted-foreground">₫/{p.unit}</span>
                          </div>
                          {changed && (
                            <span className="mt-0.5 block text-xs text-warning">
                              Giá đã duyệt: {(approvedPrice ?? 0).toLocaleString("vi-VN")}₫ → mới: {(p.unitPrice ?? 0).toLocaleString("vi-VN")}₫
                            </span>
                          )}
                        </TableCell>
                        <TableCell className="text-muted-foreground">{p.packaging ?? "—"}</TableCell>
                        <TableCell>
                          {changed ? (
                            <span className="rounded bg-warning/15 px-1.5 py-0.5 text-xs font-medium text-warning">Giá đã đổi</span>
                          ) : (
                            <span className="rounded bg-muted px-1.5 py-0.5 text-xs font-medium text-muted-foreground">Mới</span>
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
              <div className="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-end">
                <div className="space-y-1.5">
                  <Label htmlFor="bulk-reason">Lý do (gửi kèm mọi dòng đã tick)</Label>
                  <Input id="bulk-reason" value={bulkReason} onChange={(e) => setBulkReason(e.target.value)} />
                </div>
                <Button onClick={submitBulk} disabled={bulkSubmitting || selectedCount === 0}>
                  Gửi {selectedCount > 0 ? selectedCount : ""} đề xuất
                </Button>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            Đề xuất riêng lẻ (sản phẩm mới, hoặc cần khai quy cách đóng gói riêng)
            <Button size="sm" variant="outline" onClick={() => { resetForm(); setFormOpen((v) => !v); }}>
              <Plus /> {formOpen ? "Đóng" : "Thêm đề xuất riêng"}
            </Button>
          </CardTitle>
        </CardHeader>
        {formOpen && (
          <CardContent className="space-y-4">
            {error && <Alert variant="danger">{error}</Alert>}

            <div className="space-y-1.5">
              <Label htmlFor="ccr-existing">Sửa một dòng ĐÃ DUYỆT của hợp đồng này (bỏ trống = sản phẩm mới)</Label>
              <Select id="ccr-existing" value={catalogItemId} onChange={(e) => pickExistingCatalogItem(e.target.value)}>
                <option value="">— Sản phẩm mới —</option>
                {items.map((i) => (
                  <option key={i.id} value={i.id}>{i.ingredientName ?? i.ingredientId}</option>
                ))}
              </Select>
            </div>

            {!catalogItemId && (
              <div className="space-y-1.5">
                <Label htmlFor="ccr-prod">Chọn mặt hàng từ danh mục cung ứng *</Label>
                <Select
                  id="ccr-prod"
                  value={selectedProductId}
                  onChange={(e) => {
                    const prodId = e.target.value;
                    setSelectedProductId(prodId);
                    const prod = myProducts.find((p) => p.id === prodId);
                    if (prod) {
                      setIngredientId(prod.ingredientId ?? "");
                      setRequestedUnit(prod.unit ?? "");
                      const cat = ingredientCatalog.find((c) => c.id === prod.ingredientId);
                      setBaseUnit(cat?.unit ?? "");
                      setRequestedUnitPrice(prod.unitPrice != null ? String(prod.unitPrice) : "");
                      setRequestedConversionToBase(prod.conversionToBase != null ? String(prod.conversionToBase) : "1");
                      setRequestedConversionNote(prod.conversionNote ?? "");
                      setRequestedPackaging(prod.packaging ?? "");
                    } else {
                      setIngredientId("");
                      setRequestedUnit("");
                      setBaseUnit("");
                      setRequestedUnitPrice("");
                      setRequestedConversionToBase("1");
                      setRequestedConversionNote("");
                      setRequestedPackaging("");
                    }
                  }}
                >
                  <option value="">— Chọn mặt hàng cung ứng —</option>
                  {myProducts.map((p) => {
                    const name = ingredientCatalog.find((c) => c.id === p.ingredientId)?.name ?? p.label ?? p.ingredientId;
                    return (
                      <option key={p.id} value={p.id}>
                        {name} (Đơn vị: {p.unit} - Giá gốc: {p.unitPrice?.toLocaleString("vi-VN")}₫)
                      </option>
                    );
                  })}
                </Select>
              </div>
            )}

            {(!!catalogItemId || !!selectedProductId) && (
              <>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="ccr-ing-name">Nguyên liệu</Label>
                    <Input
                      id="ccr-ing-name"
                      value={ingredientCatalog.find((c) => c.id === ingredientId)?.name ?? ingredientId}
                      disabled
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="ccr-unit">Đơn vị báo giá</Label>
                    <Input id="ccr-unit" value={requestedUnit} disabled />
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="ccr-price">Đơn giá đề xuất mới *</Label>
                    <Input
                      id="ccr-price"
                      type="number"
                      min={0}
                      value={requestedUnitPrice}
                      onChange={(e) => setRequestedUnitPrice(e.target.value)}
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="ccr-pack">Quy cách đóng gói</Label>
                    <Input id="ccr-pack" value={requestedPackaging} disabled />
                  </div>
                </div>

                {/*
                  13/08/2026 (Nam: "chưa cho cấu hình để chỉ được phép mua số nguyên theo đơn vị
                  bán à?") — CÓ, và backend đã enforce từ lâu: `PurchaseRequestService` làm tròn LÊN
                  bội số `packagingSize` khi `allowPartialPackage = false`
                  (`PackagingCalculator.roundedQuantity`). Thiếu duy nhất là ô nhập: hai state
                  `requestedPackagingSize`/`requestedAllowPartialPackage` đã khai và đã gửi trong
                  `submit()`, nhưng KHÔNG có input nào ghi vào chúng ⇒ mọi đề xuất từ UI đều gửi
                  `packagingSize = undefined` + `allowPartialPackage = true`, tức ràng buộc nguyên
                  kiện chưa từng bật được bằng đường giao diện.
                */}
                <div className="space-y-3 rounded-lg border p-3">
                  <label className="flex cursor-pointer items-start gap-2 text-sm">
                    <input
                      type="checkbox"
                      className="mt-0.5 size-4 accent-primary"
                      checked={!requestedAllowPartialPackage}
                      onChange={(e) => setRequestedAllowPartialPackage(!e.target.checked)}
                    />
                    <span>
                      Chỉ bán nguyên kiện — trường phải đặt theo bội số của quy cách
                      <span className="mt-0.5 block text-xs font-normal text-muted-foreground">
                        Dùng cho mặt hàng không tách lẻ được (sữa hộp, dầu ăn đóng can…). Đơn mua
                        lẻ hơn sẽ được làm tròn LÊN bội số gần nhất khi trường lập yêu cầu mua.
                      </span>
                    </span>
                  </label>
                  {!requestedAllowPartialPackage && (
                    <div className="space-y-1.5">
                      <Label htmlFor="ccr-pack-size">
                        Số lượng / 1 kiện (theo đơn vị báo giá{requestedUnit ? ` — ${requestedUnit}` : ""}) *
                      </Label>
                      <Input
                        id="ccr-pack-size"
                        type="number"
                        min={0}
                        step="any"
                        value={requestedPackagingSize}
                        onChange={(e) => setRequestedPackagingSize(e.target.value)}
                        placeholder="VD: 12 (1 thùng = 12 hộp)"
                      />
                      <p className="text-xs text-muted-foreground">
                        Để trống hoặc 0 thì ràng buộc nguyên kiện KHÔNG có tác dụng — backend chỉ làm
                        tròn khi có con số này.
                      </p>
                    </div>
                  )}
                </div>

                {unitDiffersFromBase && (
                  <div className="grid gap-4 rounded-lg border border-warning/40 bg-warning/5 p-3 sm:grid-cols-2">
                    <div className="space-y-1.5">
                      <Label htmlFor="ccr-conv">Hệ số quy đổi chuẩn (1 {requestedUnit} = bao nhiêu {baseUnit}?)</Label>
                      <Input id="ccr-conv" type="number" value={requestedConversionToBase} disabled />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="ccr-conv-note">Ghi chú quy đổi</Label>
                      <Input id="ccr-conv-note" value={requestedConversionNote} disabled />
                    </div>
                  </div>
                )}

                <div className="space-y-1.5">
                  <Label htmlFor="ccr-reason">Lý do đề xuất *</Label>
                  <Input
                    id="ccr-reason"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="VD: giá theo hợp đồng thỏa thuận với trường"
                  />
                </div>

                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => { resetForm(); setFormOpen(false); }}>Hủy</Button>
                  <Button onClick={submit} disabled={createRequest.isPending}>Gửi đề xuất</Button>
                </div>
              </>
            )}
          </CardContent>
        )}
      </Card>
    </>
  );
}
