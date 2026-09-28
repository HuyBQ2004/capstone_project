"use client";

import { useState } from "react";
import { CheckCircle2, Download, FileUp, Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert } from "@/components/ui/alert";
import { DateInput, Input, Label, SearchableSelect } from "@/components/ui/input";
import { useCreate, usePagedList, usePlainList } from "@/lib/api/hooks";
import type { IngredientDto, SupplierBatchDto } from "@/lib/api/types";
import { formatVnDate, toIsoDate } from "@/lib/format";
import type { SupplierProduct } from "@/lib/supplier-product-adapter";
import { useAuthStore } from "@/lib/stores/auth-store";

/**
 * `productLabel()` (lib/mock-data.ts) trả `prod.label`, và nếu NCC chưa tự đặt tên hiển thị
 * cho mặt hàng (field tuỳ chọn khi tạo ở /supplier/products) thì rơi xuống thẳng
 * `ingredientId` — một MÃ nội bộ (VD "ING-061"), không phải tên nguyên liệu đọc được. Đo thật
 * 10/08/2026: phần lớn mặt hàng cũ không có `label` nên dropdown Excel toàn hiện mã, không ai
 * biết là nguyên liệu gì. Tra thêm danh mục `/ingredients` (dùng chung toàn hệ thống, SUPPLIER
 * đọc được) để lấy đúng TÊN nguyên liệu làm phương án cuối trước khi phải hiện mã.
 */
function productDisplayName(p: SupplierProduct, ingredientCatalog: IngredientDto[]): string {
  if (p.label && p.label.trim()) return p.label;
  const ingredient = ingredientCatalog.find((c) => c.id === p.ingredientId);
  return ingredient?.name ?? p.ingredientId ?? p.id;
}

/**
 * Screen Spec 12/13/15 — Ingredient Batches / Batch List / Submit Batch Information.
 * UC-043 (Manage Ingredient Batches), UC-081 (Submit Batch Information).
 * BR-071: lô bắt buộc kèm số lô, NSX, ngày đóng gói, HSD, số lượng.
 * Review 07/2026: "Số lượng" tách thành Số + Đơn vị tính; validate theo từng trường.
 *
 * Nối backend thật 05/08/2026:
 * 1. `SupplierBatchStatus` (Java enum) chỉ có 3 giá trị `ACTIVE`/`DELIVERED`/`EXPIRED` —
 *    KHÔNG có `CANCELLED`. Tính năng "Hủy lô" (mock) không có chỗ lưu ở backend thật —
 *    BỎ tính năng này, để lại làm việc riêng nếu nghiệp vụ thật sự cần huỷ lô đã đăng ký.
 * 2. `POST /supplier-batches` KHÔNG nhận danh sách chứng chỉ — chứng chỉ gắn qua
 *    `POST /supplier-batches/{code}/certificates` RIÊNG, multipart, upload file MỚI (không
 *    tham chiếu tài liệu tuân thủ đã có ở `supplier-documents`). Bỏ bước "chọn chứng chỉ có
 *    sẵn" (không map được sang API thật) — tạo lô trước, đính kèm chứng chỉ là việc riêng
 *    (upload multipart) chưa làm ở đợt này.
 * 3. `productId`/`ingredientId`/`ingredient` trước đây LUÔN null ở response (bug backend,
 *    đã vá cùng đợt — xem `03_FE_Full_Wiring_Master_Plan.md`) — nay đọc trực tiếp field thật.
 */

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="text-xs font-medium text-destructive">{message}</p>;
}

const columns: ColumnDef<SupplierBatchDto, unknown>[] = [
  { accessorKey: "lotNumber", header: "Số lô (BR-071)", cell: ({ row }) => <span className="font-medium">{row.original.lotNumber}</span> },
  { accessorKey: "ingredient", header: "Mặt hàng" },
  {
    accessorKey: "quantity",
    header: "Số lượng",
    cell: ({ row }) => `${row.original.quantity} ${row.original.unit}`,
  },
  {
    accessorKey: "remainingQuantity",
    header: "Còn lại (BR-071)",
    cell: ({ row }) => `${row.original.remainingQuantity ?? row.original.quantity} ${row.original.unit}`,
  },
  {
    accessorKey: "packagingDate",
    header: "Ngày đóng gói",
    cell: ({ row }) => formatVnDate(row.original.packagingDate),
  },
  {
    accessorKey: "expirationDate",
    header: "Hạn sử dụng",
    cell: ({ row }) => formatVnDate(row.original.expirationDate),
  },
  {
    id: "certificates",
    header: "Chứng chỉ liên kết",
    cell: ({ row }) => <Badge variant="info">{(row.original.certificateIds ?? []).length} tài liệu</Badge>,
  },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
];

/**
 * Chuyển "dd/mm/yyyy" (giá trị thật của `DateInput`, KHÔNG phải ISO) → Date; trả null nếu
 * không hợp lệ. Trước đây hàm này parse theo regex ISO "yyyy-mm-dd" — không khớp giá trị thật
 * DateInput trả về nên `mfg`/`pkg`/`exp` luôn null, khiến validate thứ tự ngày (NSX < đóng gói
 * < HSD) âm thầm không bao giờ chạy (đo thật 10/08/2026, cùng đợt lỗi 422 khi submit).
 */
function parseVnDate(value: string): Date | null {
  const m = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!m) return null;
  const d = new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1]));
  return Number.isNaN(d.getTime()) ? null : d;
}

/**
 * Nhập lô hàng loạt từ Excel (bổ sung 10/08/2026) — mặt hàng GIỚI HẠN theo danh mục đã khai
 * báo ở `/supplier/products` (BR-071: lô phải thuộc một sản phẩm đã khai, không gõ tên tự do,
 * cùng ràng buộc với form tạo lô đơn ở trên). Cột "Mặt hàng" trong template là dropdown chọn từ
 * đúng danh sách sản phẩm của NCC đang đăng nhập — không cho gõ tay để khỏi lệch một chữ là lô
 * vào kho không nối được về đâu (như comment ở `SubmitBatchDialog` phía trên).
 */
interface ParsedBatchRow {
  productRaw: string;
  productId: string;
  lotNumber: string;
  manufacturingDate: string;
  packagingDate: string;
  expirationDate: string;
  quantity: string;
  unit: string;
  valid: boolean;
  error?: string;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function getCellValueAsString(cell: any): string {
  if (!cell || cell.value === null || cell.value === undefined) return "";
  const v = cell.value;
  if (v instanceof Date) {
    return `${String(v.getDate()).padStart(2, "0")}/${String(v.getMonth() + 1).padStart(2, "0")}/${v.getFullYear()}`;
  }
  if (typeof v === "object") {
    if ("result" in v) return String(v.result ?? "").trim();
    if ("text" in v) return String(v.text ?? "").trim();
    if ("richText" in v) return v.richText.map((t: { text?: string }) => t.text || "").join("").trim();
    return JSON.stringify(v);
  }
  return String(v).trim();
}

/** "yyyy-mm-dd" (Excel đôi khi tự chuẩn hoá) → "dd/mm/yyyy"; giữ nguyên nếu không khớp. */
function excelDateToVn(raw: string): string {
  const iso = raw.trim().match(/^(\d{4})-(\d{2})-(\d{2})$/);
  return iso ? `${iso[3]}/${iso[2]}/${iso[1]}` : raw.trim();
}

/** Dropdown template ghi "MÃ — Tên nguyên liệu"; chấp nhận cả gõ đúng mã trần. */
function productDropdownLabel(p: SupplierProduct, ingredientCatalog: IngredientDto[]): string {
  return `${p.id} — ${productDisplayName(p, ingredientCatalog)}`;
}

function resolveProduct(raw: string, products: SupplierProduct[], ingredientCatalog: IngredientDto[]): SupplierProduct | null {
  const value = raw.trim();
  if (!value) return null;
  const code = value.split("—")[0]?.trim() || value;
  return (
    products.find((p) => p.id.toLowerCase() === code.toLowerCase()) ??
    products.find((p) => p.id.toLowerCase() === value.toLowerCase()) ??
    products.find((p) => productDisplayName(p, ingredientCatalog).toLowerCase() === value.toLowerCase()) ??
    null
  );
}

function validateBatchRow(
  row: Omit<ParsedBatchRow, "productId" | "unit" | "valid" | "error">,
  products: SupplierProduct[],
  ingredientCatalog: IngredientDto[],
  seenLots: Set<string>,
  dbLots: Set<string>
): ParsedBatchRow {
  const { productRaw, lotNumber, manufacturingDate, packagingDate, expirationDate, quantity } = row;
  let error: string | undefined;

  const product = resolveProduct(productRaw, products, ingredientCatalog);
  const mfg = parseVnDate(manufacturingDate);
  const pkg = parseVnDate(packagingDate);
  const exp = parseVnDate(expirationDate);
  const qty = Number(quantity.trim().replace(",", "."));
  const lotKey = product ? `${product.id}::${lotNumber.trim().toLowerCase()}` : "";

  if (!product) {
    error = "Mặt hàng không có trong danh mục đã khai báo (/supplier/products) — chọn từ dropdown.";
  } else if (!lotNumber.trim()) {
    error = "Số lô không được để trống.";
  } else if (!manufacturingDate.trim() || !mfg) {
    error = "Ngày sản xuất trống hoặc sai định dạng dd/mm/yyyy.";
  } else if (!packagingDate.trim() || !pkg) {
    error = "Ngày đóng gói trống hoặc sai định dạng dd/mm/yyyy.";
  } else if (pkg < mfg) {
    error = "Ngày đóng gói phải sau ngày sản xuất (UC-081).";
  } else if (!expirationDate.trim() || !exp) {
    error = "Hạn sử dụng trống hoặc sai định dạng dd/mm/yyyy.";
  } else if (exp <= pkg || exp <= mfg) {
    error = "Hạn sử dụng phải sau ngày sản xuất và ngày đóng gói (UC-081).";
  } else if (!quantity.trim() || !Number.isFinite(qty) || qty <= 0) {
    error = "Số lượng phải là số lớn hơn 0.";
  } else if (seenLots.has(lotKey)) {
    error = "Số lô trùng với một dòng khác trong file (cùng mặt hàng).";
  } else if (dbLots.has(lotKey)) {
    error = "Số lô đã tồn tại cho mặt hàng này trong hệ thống.";
  }

  if (!error && product) seenLots.add(lotKey);

  return {
    productRaw: productRaw.trim(),
    productId: product?.id ?? "",
    lotNumber: lotNumber.trim(),
    manufacturingDate: manufacturingDate.trim(),
    packagingDate: packagingDate.trim(),
    expirationDate: expirationDate.trim(),
    quantity: quantity.trim(),
    unit: product?.unit ?? "",
    valid: !error,
    error,
  };
}

async function parseBatchExcel(
  file: File,
  products: SupplierProduct[],
  ingredientCatalog: IngredientDto[],
  dbLots: Set<string>
): Promise<ParsedBatchRow[]> {
  const ExcelJS = await import("exceljs");
  const workbook = new ExcelJS.Workbook();
  const arrayBuffer = await file.arrayBuffer();
  await workbook.xlsx.load(arrayBuffer);

  const worksheet = workbook.getWorksheet(1);
  if (!worksheet) throw new Error("Không tìm thấy worksheet nào trong file Excel.");

  const rows: ParsedBatchRow[] = [];
  const seenLots = new Set<string>();

  worksheet.eachRow({ includeEmpty: false }, (row, rowNumber) => {
    if (rowNumber === 1) return; // bỏ dòng tiêu đề

    const productRaw = getCellValueAsString(row.getCell(1));
    const lotNumber = getCellValueAsString(row.getCell(2));
    const manufacturingDate = excelDateToVn(getCellValueAsString(row.getCell(3)));
    const packagingDate = excelDateToVn(getCellValueAsString(row.getCell(4)));
    const expirationDate = excelDateToVn(getCellValueAsString(row.getCell(5)));
    const quantity = getCellValueAsString(row.getCell(6));

    if (!productRaw && !lotNumber && !manufacturingDate && !packagingDate && !expirationDate && !quantity) return;

    rows.push(
      validateBatchRow(
        { productRaw, lotNumber, manufacturingDate, packagingDate, expirationDate, quantity },
        products,
        ingredientCatalog,
        seenLots,
        dbLots
      )
    );
  });

  return rows;
}

async function downloadBatchExcelTemplate(products: SupplierProduct[], ingredientCatalog: IngredientDto[]) {
  try {
    const ExcelJS = await import("exceljs");
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet("Khai báo lô hàng");
    const refSheet = workbook.addWorksheet("DataRef");
    refSheet.state = "hidden";

    const dropdownValues = products.map((p) => productDropdownLabel(p, ingredientCatalog));
    dropdownValues.forEach((v, idx) => {
      refSheet.getCell(idx + 1, 1).value = v;
    });

    worksheet.views = [{ showGridLines: true }];
    worksheet.columns = [
      { header: "Mặt hàng * (chọn từ danh sách đã khai báo)", key: "product", width: 40 },
      { header: "Số lô *", key: "lot", width: 22 },
      { header: "Ngày sản xuất * (dd/mm/yyyy)", key: "mfg", width: 24 },
      { header: "Ngày đóng gói * (dd/mm/yyyy)", key: "pkg", width: 24 },
      { header: "Hạn sử dụng * (dd/mm/yyyy)", key: "exp", width: 24 },
      { header: "Số lượng *", key: "qty", width: 15 },
    ];

    const headerRow = worksheet.getRow(1);
    headerRow.font = { name: "Arial", size: 11, bold: true, color: { argb: "FFFFFFFF" } };
    headerRow.fill = { type: "pattern", pattern: "solid", fgColor: { argb: "FF1E3A8A" } };
    headerRow.alignment = { vertical: "middle", horizontal: "center" };
    headerRow.height = 25;

    if (dropdownValues.length > 0) {
      worksheet.addRow({
        product: dropdownValues[0],
        lot: "GF-RC-260703",
        mfg: "01/08/2026",
        pkg: "02/08/2026",
        exp: "10/08/2026",
        qty: "30",
      });
    }
    worksheet.getRow(2).height = 20;
    worksheet.getRow(2).alignment = { vertical: "middle" };

    if (dropdownValues.length > 0) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (worksheet as any).dataValidations.add("A2:A500", {
        type: "list",
        allowBlank: false,
        formulae: [`DataRef!$A$1:$A$${dropdownValues.length}`],
        showErrorMessage: true,
        errorTitle: "Lỗi chọn mặt hàng",
        error: "Vui lòng chọn mặt hàng từ danh sách đã khai báo ở /supplier/products.",
      });
    }

    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "template-import-lo-hang.xlsx";
    a.click();
    URL.revokeObjectURL(url);
    toast.success("Tải template Excel thành công!");
  } catch (err) {
    console.error(err);
    toast.error("Tải template thất bại.");
  }
}

function ImportBatchesDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const partnerId = useAuthStore((s) => s.user?.partnerId);
  const { data: productPage } = usePagedList<SupplierProduct>("supplier-products", {
    pageSize: 200,
    supplierId: partnerId,
  });
  const products = productPage?.items ?? [];
  // Chỉ để dò trùng số lô cùng mặt hàng — backend đã tự scope theo NCC đang đăng nhập.
  const { data: batchPage } = usePagedList<SupplierBatchDto>("supplier-batches", { pageSize: 500 });
  const existingBatches = batchPage?.items ?? [];
  // Danh mục nguyên liệu dùng chung toàn hệ thống — tra TÊN thật khi `SupplierProduct.label`
  // trống, tránh dropdown/preview chỉ hiện mã ingredientId không đọc được (BR-080).
  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");

  const create = useCreate<
    {
      productId: string;
      lotNumber: string;
      manufacturingDate: string;
      packagingDate: string;
      expirationDate: string;
      quantity: number;
      unit: string;
    },
    SupplierBatchDto
  >("supplier-batches");

  const [step, setStep] = useState<"upload" | "preview" | "done">("upload");
  const [file, setFile] = useState<File | null>(null);
  const [parsed, setParsed] = useState<ParsedBatchRow[]>([]);
  const [parseError, setParseError] = useState("");
  const [importing, setImporting] = useState(false);
  const [successCount, setSuccessCount] = useState(0);

  function resetAll() {
    setStep("upload"); setFile(null); setParsed([]); setParseError(""); setSuccessCount(0);
  }

  function handleFile(f: File) {
    setFile(f);
    setParseError("");
    const dbLots = new Set(
      existingBatches
        .filter((b) => b.productId)
        .map((b) => `${b.productId}::${(b.lotNumber ?? "").trim().toLowerCase()}`)
    );
    parseBatchExcel(f, products, ingredientCatalog, dbLots)
      .then((rows) => {
        if (rows.length === 0) setParseError("File rỗng hoặc không chứa dòng dữ liệu nào.");
        else { setParsed(rows); setStep("preview"); }
      })
      .catch((err) => {
        console.error(err);
        setParseError("Lỗi đọc file Excel. Vui lòng kiểm tra lại định dạng.");
      });
  }

  async function confirmImport() {
    setImporting(true);
    const validRows = parsed.filter((r) => r.valid);
    let success = 0;
    for (const row of validRows) {
      await new Promise<void>((resolve) => {
        create.mutate(
          {
            productId: row.productId,
            lotNumber: row.lotNumber,
            manufacturingDate: toIsoDate(row.manufacturingDate) ?? row.manufacturingDate,
            packagingDate: toIsoDate(row.packagingDate) ?? row.packagingDate,
            expirationDate: toIsoDate(row.expirationDate) ?? row.expirationDate,
            quantity: Number(row.quantity.replace(",", ".")),
            unit: row.unit,
          },
          {
            onSuccess: () => { success++; resolve(); },
            onError: () => resolve(),
          }
        );
      });
    }
    setSuccessCount(success);
    setImporting(false);
    setStep("done");
  }

  const validCount = parsed.filter((r) => r.valid).length;
  const invalidCount = parsed.length - validCount;

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) resetAll(); onOpenChange(v); }}>
      <DialogContent className={step === "preview" ? "max-w-4xl max-h-[85vh] overflow-y-auto" : "sm:max-w-[520px]"}>
        <DialogHeader>
          <DialogTitle>Nhập lô hàng từ Excel</DialogTitle>
          <DialogDescription>
            BR-071 — mặt hàng của mỗi dòng phải nằm trong danh mục đã khai báo ở{" "}
            <a href="/supplier/products" className="underline">/supplier/products</a>.
          </DialogDescription>
        </DialogHeader>

        {step === "upload" && (
          <div className="space-y-4 py-2">
            {products.length === 0 ? (
              <Alert variant="warning" title="Chưa có mặt hàng nào được khai báo">
                Khai báo ít nhất một mặt hàng ở <a href="/supplier/products" className="underline">/supplier/products</a> trước
                khi nhập lô từ Excel — cột &quot;Mặt hàng&quot; của template cần danh sách đó để tạo dropdown.
              </Alert>
            ) : (
              <div className="flex flex-col items-center justify-center border-2 border-dashed rounded-lg p-6 bg-muted/30">
                <FileUp className="size-10 text-muted-foreground mb-2" />
                <p className="text-sm font-medium">Chọn file Excel danh sách lô hàng</p>
                <p className="text-xs text-muted-foreground mt-1">Hỗ trợ file `.xlsx`</p>
                <Input
                  id="batch-excel-file"
                  type="file"
                  accept=".xlsx"
                  className="mt-4"
                  onChange={(e) => {
                    const f = e.target.files?.[0];
                    if (f) handleFile(f);
                  }}
                />
              </div>
            )}
            {parseError && <p className="text-sm font-medium text-destructive">{parseError}</p>}
            <div className="border-t pt-4 flex justify-between items-center">
              <Button variant="outline" disabled={products.length === 0} onClick={() => downloadBatchExcelTemplate(products, ingredientCatalog)}>
                <Download className="size-4 mr-1.5" /> Tải Excel Template
              </Button>
              <Button variant="ghost" onClick={() => onOpenChange(false)}>Hủy</Button>
            </div>
          </div>
        )}

        {step === "preview" && (
          <div className="space-y-4 py-2">
            <div className="flex justify-between items-center">
              <div className="space-x-2">
                <Badge variant="success">{validCount} hợp lệ</Badge>
                <Badge variant="danger">{invalidCount} lỗi</Badge>
              </div>
              <p className="text-xs text-muted-foreground">Tên file: {file?.name}</p>
            </div>
            <div className="border rounded-lg overflow-hidden max-h-[40vh] overflow-y-auto">
              <table className="w-full text-sm text-left border-collapse">
                <thead className="bg-muted text-muted-foreground text-xs uppercase font-medium">
                  <tr>
                    <th className="p-3 border-b">Mặt hàng</th>
                    <th className="p-3 border-b">Số lô</th>
                    <th className="p-3 border-b">NSX</th>
                    <th className="p-3 border-b">Đóng gói</th>
                    <th className="p-3 border-b">HSD</th>
                    <th className="p-3 border-b">Số lượng</th>
                    <th className="p-3 border-b">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {parsed.map((r, i) => (
                    <tr key={i} className={r.valid ? "hover:bg-muted/10" : "bg-destructive/5 hover:bg-destructive/10"}>
                      <td className="p-3 border-b">
                        {r.productId
                          ? `${r.productId} — ${productDisplayName(products.find((p) => p.id === r.productId)!, ingredientCatalog)}`
                          : r.productRaw || "—"}
                      </td>
                      <td className="p-3 border-b font-medium">{r.lotNumber || "—"}</td>
                      <td className="p-3 border-b">{r.manufacturingDate || "—"}</td>
                      <td className="p-3 border-b">{r.packagingDate || "—"}</td>
                      <td className="p-3 border-b">{r.expirationDate || "—"}</td>
                      <td className="p-3 border-b">{r.quantity ? `${r.quantity} ${r.unit}` : "—"}</td>
                      <td className="p-3 border-b text-right">
                        {r.valid ? <Badge variant="success">Hợp lệ</Badge> : <span className="text-xs text-destructive">{r.error}</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="flex justify-between items-center border-t pt-4">
              <Button variant="outline" onClick={() => { setStep("upload"); setParsed([]); setFile(null); }}>Quay lại</Button>
              <div className="space-x-2">
                <Button variant="ghost" onClick={() => onOpenChange(false)}>Hủy</Button>
                <Button disabled={validCount === 0 || importing} onClick={confirmImport}>
                  {importing ? "Đang nhập…" : `Xác nhận nhập ${validCount} dòng`}
                </Button>
              </div>
            </div>
          </div>
        )}

        {step === "done" && (
          <div className="space-y-4 py-4 text-center">
            <div className="mx-auto size-12 bg-success/10 rounded-full flex items-center justify-center text-success mb-2">
              <CheckCircle2 className="size-6" />
            </div>
            <h3 className="text-lg font-semibold">Hoàn thành nhập Excel</h3>
            <p className="text-sm text-muted-foreground">
              Đã tạo thành công <span className="font-semibold text-foreground">{successCount}</span> trên tổng số{" "}
              <span className="font-semibold text-foreground">{validCount}</span> dòng hợp lệ.
            </p>
            <div className="border-t pt-4">
              <Button className="w-full" onClick={() => { resetAll(); onOpenChange(false); }}>Đóng</Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

function SubmitBatchDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const partnerId = useAuthStore((s) => s.user?.partnerId);
  const create = useCreate<
    {
      productId: string;
      lotNumber: string;
      manufacturingDate: string;
      packagingDate: string;
      expirationDate: string;
      quantity: number;
      unit: string;
    },
    SupplierBatchDto
  >("supplier-batches");
  /*
   * Lô phải thuộc về một SẢN PHẨM đã khai, không gõ tên tự do. Tên tự do là chỗ
   * đứt của cả chuỗi truy xuất: trường nhận hàng phải dò tên để đoán ra nguyên
   * liệu nào trong danh mục, lệch một chữ là lô vào kho mà không nối được về đâu.
   */
  const { data: productPage } = usePagedList<SupplierProduct>("supplier-products", {
    pageSize: 200,
    supplierId: partnerId,
  });
  const products = productPage?.items ?? [];
  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");
  const [productId, setProductId] = useState("");
  // Đơn vị số lượng lô KHÓA theo đúng đơn vị của mặt hàng đang chọn — không cho gõ/chọn
  // tự do (Review 08/2026, sau khi rà lỗi lệch đơn vị y hệt các màn khác: kg/g/thùng...).
  const selectedProduct = products.find((p) => p.id === productId);
  const quantityUnit = selectedProduct?.unit ?? "";
  const [lotNumber, setLotNumber] = useState("");
  const [quantityValue, setQuantityValue] = useState("");
  const [manufacturingDate, setManufacturingDate] = useState("");
  const [packagingDate, setPackagingDate] = useState("");
  const [expirationDate, setExpirationDate] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function reset() {
    setProductId(""); setLotNumber(""); setQuantityValue("");
    setManufacturingDate(""); setPackagingDate(""); setExpirationDate("");
    setErrors({});
  }

  function submit() {
    const next: Record<string, string> = {};
    if (!productId) next.name = "Chọn sản phẩm — lô hàng phải thuộc về một mặt hàng đã khai.";
    if (!lotNumber.trim()) next.lot = "Số lô không được để trống.";

    if (!quantityValue.trim()) next.qty = "Số lượng không được để trống.";
    else if (!/^\d+([.,]\d+)?$/.test(quantityValue.trim()) || Number(quantityValue.replace(",", ".")) <= 0)
      next.qty = "Số lượng phải là số lớn hơn 0.";

    const mfg = parseVnDate(manufacturingDate);
    const pkg = parseVnDate(packagingDate);
    const exp = parseVnDate(expirationDate);
    if (!manufacturingDate.trim()) next.mfg = "Ngày sản xuất không được để trống.";
    if (!packagingDate.trim()) next.pkg = "Ngày đóng gói không được để trống.";
    else if (mfg && pkg && pkg < mfg) next.pkg = "Ngày đóng gói phải sau ngày sản xuất (UC-081).";
    if (!expirationDate.trim()) next.exp = "Hạn sử dụng không được để trống.";
    else if ((pkg && exp && exp <= pkg) || (mfg && exp && exp <= mfg))
      next.exp = "Hạn sử dụng phải sau ngày sản xuất và ngày đóng gói (UC-081).";

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;
    create.mutate(
      {
        productId,
        lotNumber,
        // D4 — backend đòi ISO-8601 cho LocalDate; DateInput lưu "dd/mm/yyyy" để hiển thị
        // kiểu Việt Nam, gửi thẳng chuỗi đó lên sẽ bị 422 "malformed request body".
        manufacturingDate: toIsoDate(manufacturingDate) ?? manufacturingDate,
        packagingDate: toIsoDate(packagingDate) ?? packagingDate,
        expirationDate: toIsoDate(expirationDate) ?? expirationDate,
        quantity: Number(quantityValue.trim().replace(",", ".")),
        unit: quantityUnit,
      },
      {
        onSuccess: (batch) => {
          toast.success(`Đã đăng ký lô ${batch.id} (BR-071) — đính kèm chứng chỉ ở trang chi tiết lô.`);
          onOpenChange(false);
          reset();
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Nộp thông tin lô nguyên liệu</DialogTitle>
          <DialogDescription>
            UC-081 — mỗi lô bắt buộc kèm: số lô, ngày sản xuất, ngày đóng gói, hạn sử
            dụng, số lượng (BR-071).
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="b-name">Mặt hàng *</Label>
              {/*
                13/08/2026 (Nam) — đổi từ `<select>` gốc sang ô CÓ TÌM KIẾM. Danh mục mặt hàng
                của NCC dài hàng trăm dòng, mà `<select>` chỉ nhảy theo ký tự ĐẦU của nhãn: muốn
                tìm "Cá lóc" mà gõ "lóc" là trượt, phải cuộn tay.
              */}
              <SearchableSelect
                value={productId}
                onChange={(v) => { setProductId(v); clearError("name"); }}
                placeholder="— Chọn mặt hàng đang cung cấp —"
                searchPlaceholder="Gõ tên mặt hàng để tìm…"
                options={products.map((p) => ({
                  value: p.id,
                  label: `${productDisplayName(p, ingredientCatalog)} · ${p.unitPrice.toLocaleString("vi-VN")}₫/${p.unit}`,
                }))}
              />
              <FieldError message={errors.name} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="b-lot">Số lô *</Label>
              <Input
                id="b-lot"
                value={lotNumber}
                onChange={(e) => { setLotNumber(e.target.value); clearError("lot"); }}
                aria-invalid={!!errors.lot}
                className={errors.lot ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="VD: GF-RC-260703"
              />
              <FieldError message={errors.lot} />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="b-mfg">Ngày sản xuất *</Label>
              <DateInput id="b-mfg" value={manufacturingDate} onChange={(e) => { setManufacturingDate(e.target.value); clearError("mfg"); }} />
              <FieldError message={errors.mfg} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="b-pkg">Ngày đóng gói *</Label>
              <DateInput id="b-pkg" value={packagingDate} onChange={(e) => { setPackagingDate(e.target.value); clearError("pkg"); }} />
              <p className="text-xs text-muted-foreground">Phải sau ngày sản xuất</p>
              <FieldError message={errors.pkg} />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="b-exp">Hạn sử dụng *</Label>
              <DateInput id="b-exp" value={expirationDate} onChange={(e) => { setExpirationDate(e.target.value); clearError("exp"); }} />
              <p className="text-xs text-muted-foreground">Phải sau ngày sản xuất & ngày đóng gói</p>
              <FieldError message={errors.exp} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="b-qty">Số lượng *</Label>
              <div className="flex gap-2">
                <Input
                  id="b-qty"
                  inputMode="decimal"
                  value={quantityValue}
                  onChange={(e) => { setQuantityValue(e.target.value); clearError("qty"); }}
                  aria-invalid={!!errors.qty}
                  className={`flex-1 ${errors.qty ? "border-destructive focus-visible:ring-destructive" : ""}`}
                  placeholder="VD: 30"
                />
                <Input
                  aria-label="Đơn vị tính (khóa theo mặt hàng đã chọn)"
                  value={quantityUnit || "—"}
                  disabled
                  className="w-24"
                />
              </div>
              <FieldError message={errors.qty} />
            </div>
          </div>
          <Alert variant="info" title="Chứng chỉ đính kèm">
            Sau khi tạo lô, vào trang chi tiết lô để tải lên chứng chỉ liên quan (BR-046) —
            việc này giờ là bước riêng, không còn chọn từ danh sách tài liệu tuân thủ có sẵn.
          </Alert>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo lô hàng</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function BatchesPage() {
  const [open, setOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  return (
    <>
      <PageHeader
        title="Lô nguyên liệu"
        description="Lô xuất đi đăng ký trước khi giao — truy xuất nguồn gốc theo số lô (BR-071/BR-076); một lô có thể được giao nhiều đợt/nhiều trường, chỉ đóng (Đã giao) khi hết sạch số lượng còn lại"
        actions={
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setImportOpen(true)}>
              <FileUp /> Nhập Excel
            </Button>
            <Button onClick={() => setOpen(true)}>
              <Plus /> Tạo lô hàng mới
            </Button>
          </div>
        }
      />
      <DataTable<SupplierBatchDto>
        resource="supplier-batches"
        columns={columns}
        searchPlaceholder="Tìm theo mã lô, số lô…"
        rowHref={(b) => `/supplier/batches/${b.id}`}
      />
      <SubmitBatchDialog open={open} onOpenChange={setOpen} />
      <ImportBatchesDialog open={importOpen} onOpenChange={setImportOpen} />
    </>
  );
}
