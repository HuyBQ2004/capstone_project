"use client";

import { useState } from "react";
import { CheckCircle2, Download, FileUp, Pencil, Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Alert } from "@/components/ui/alert";
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
import { Input, Label, SearchableSelect, Select, type SearchableSelectOption } from "@/components/ui/input";
import { useCreate, usePagedList, usePlainList, useUpdate } from "@/lib/api/hooks";
import type { IngredientDto, SupplierProductDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { useMeasureUnits } from "@/lib/measure-units";
import { useAuthStore } from "@/lib/stores/auth-store";

/**
 * Mặt hàng nhà cung cấp đang cung ứng cho trường.
 *
 * Bổ sung 10/08/2026:
 * 1. Phân trang + search: chuyển sang `DataTable` dùng chung — đã có server-side
 *    pagination/search tích hợp sẵn, thay thế Table thủ công + pageSize: 200.
 * 2. Import Excel — khai hàng loạt mặt hàng từ file template, cùng pattern với
 *    `/supplier/batches`: parse → preview bảng lỗi/hợp lệ → confirm tuần tự.
 */

interface Draft {
  ingredientId: string;
  label: string;
  /** Đơn vị NCC BÁN theo (có thể khác đơn vị chuẩn của nguyên liệu). */
  unit: string;
  /** Bao nhiêu đơn vị chuẩn = 1 `unit` ở trên. */
  conversionToBase: string;
  conversionNote: string;
  unitPrice: string;
  packaging: string;
  effectiveFrom: string;
}

/** yyyy-mm-dd hôm nay — giá trị mặc định cho `<input type="date">` (D4). */
function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

/**
 * Danh sách nguyên liệu cho `SearchableSelect` (11/08/2026 — thay component `IngredientCombobox`
 * tự viết bằng component dùng chung, nay đã hỗ trợ `{value,label}` nên xử lý được value=id ≠
 * label hiển thị).
 */
function ingredientSelectOptions(options: IngredientDto[]): SearchableSelectOption[] {
  return options.filter((c) => c.id).map((c) => ({ value: c.id as string, label: `${c.name} (${c.unit})` }));
}

function ProductDialog({
  product,
  existing,
  ingredientCatalog,
  onClose,
}: {
  product?: SupplierProductDto;
  existing: SupplierProductDto[];
  ingredientCatalog: IngredientDto[];
  onClose: () => void;
}) {
  const create = useCreate<Partial<SupplierProductDto>>("supplier-products");
  const update = useUpdate<{ id: string } & Partial<SupplierProductDto>>("supplier-products");
  const [d, setD] = useState<Draft>({
    ingredientId: product?.ingredientId ?? "",
    label: product?.label ?? "",
    unit: product?.unit ?? "",
    conversionToBase: product?.conversionToBase != null ? String(product.conversionToBase) : "",
    conversionNote: product?.conversionNote ?? "",
    unitPrice: product ? String(product.unitPrice) : "",
    packaging: product?.packaging ?? "",
    effectiveFrom: product?.effectiveFrom ?? todayIso(),
  });
  const [error, setError] = useState("");
  const catalog = ingredientCatalog.find((c) => c.id === d.ingredientId);
  const { options: unitOptions } = useMeasureUnits();
  const sellUnitDiffers = !!d.unit && !!catalog?.unit && d.unit !== catalog.unit;
  const isPending = create.isPending || update.isPending;

  const registeredIds = new Set(existing.map((p) => p.ingredientId));
  const availableIngredients = ingredientCatalog.filter(
    (c) => !!c.id && (!registeredIds.has(c.id) || c.id === product?.ingredientId)
  );

  function submit() {
    if (!d.ingredientId) {
      setError("Chọn nguyên liệu từ danh mục.");
      return;
    }
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
    if (!d.effectiveFrom) {
      setError("Chọn ngày hiệu lực.");
      return;
    }
    const baseUnit = catalog?.unit ?? "";
    const sellUnit = d.unit.trim() || baseUnit || "kg";
    let conversion = 1;
    if (sellUnit !== baseUnit) {
      conversion = Number(d.conversionToBase);
      if (!(conversion > 0)) {
        setError(
          `Bán theo "${sellUnit}" khác đơn vị chuẩn "${baseUnit || "?"}" — phải khai 1 ${sellUnit} = bao nhiêu ${baseUnit || "đơn vị chuẩn"}.`
        );
        return;
      }
    }
    setError("");
    const done = () => {
      toast.success(
        product
          ? `Đã cập nhật ${catalog?.name} — giá mới áp dụng từ ${formatVnDate(d.effectiveFrom)}.`
          : `Đã thêm ${catalog?.name} vào danh sách mặt hàng cung ứng.`
      );
      onClose();
    };
    if (product?.id) {
      update.mutate(
        {
          id: product.id,
          label: d.label.trim() || undefined,
          unit: sellUnit,
          unitPrice: price,
          conversionToBase: conversion,
          conversionNote: d.conversionNote.trim() || undefined,
          packaging: d.packaging.trim() || undefined,
          effectiveFrom: d.effectiveFrom,
        },
        { onSuccess: done }
      );
    } else {
      create.mutate(
        {
          id: `SP-${String(Date.now()).slice(-6)}`,
          ingredientId: d.ingredientId,
          label: d.label.trim() || undefined,
          unit: sellUnit,
          unitPrice: price,
          conversionToBase: conversion,
          conversionNote: d.conversionNote.trim() || undefined,
          packaging: d.packaging.trim() || undefined,
          effectiveFrom: d.effectiveFrom,
        },
        { onSuccess: done }
      );
    }
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
            <SearchableSelect
              value={d.ingredientId}
              options={ingredientSelectOptions(availableIngredients)}
              disabled={product != null}
              placeholder="— Chọn nguyên liệu —"
              searchPlaceholder="Gõ tên nguyên liệu để tìm…"
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
              <Label htmlFor="pr-unit">Đơn vị bán</Label>
              <Select
                id="pr-unit"
                value={d.unit || (catalog?.unit ?? "")}
                onChange={(e) => setD({ ...d, unit: e.target.value })}
              >
                {catalog?.unit && (
                  <option value={catalog.unit}>{catalog.unit} (đơn vị chuẩn của nguyên liệu)</option>
                )}
                {unitOptions
                  .filter((u) => u !== catalog?.unit)
                  .map((u) => (
                    <option key={u} value={u}>{u}</option>
                  ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pr-conv">
                Quy đổi{sellUnitDiffers ? " *" : ""} (1 {d.unit || catalog?.unit || "đơn vị"} = ? {catalog?.unit ?? "đơn vị chuẩn"})
              </Label>
              <Input
                id="pr-conv"
                type="number"
                min={0}
                step="any"
                disabled={!sellUnitDiffers}
                value={sellUnitDiffers ? d.conversionToBase : "1"}
                onChange={(e) => setD({ ...d, conversionToBase: e.target.value })}
                placeholder="VD: 25"
              />
              <p className="text-xs text-muted-foreground">
                {sellUnitDiffers
                  ? "Bắt buộc — trường dùng số này để quy đơn hàng ra đơn vị chuẩn khi nhập kho."
                  : "Bán đúng đơn vị chuẩn nên hệ số luôn là 1."}
              </p>
            </div>
          </div>
          {sellUnitDiffers && (
            <div className="space-y-1.5">
              <Label htmlFor="pr-conv-note">Ghi chú quy đổi</Label>
              <Input
                id="pr-conv-note"
                value={d.conversionNote}
                onChange={(e) => setD({ ...d, conversionNote: e.target.value })}
                placeholder="VD: trung bình, có thể lệch theo mùa vụ"
              />
            </div>
          )}
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="pr-price">Đơn giá * (₫/{d.unit || catalog?.unit || "đơn vị"})</Label>
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
                type="date"
                value={d.effectiveFrom}
                onChange={(e) => setD({ ...d, effectiveFrom: e.target.value })}
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

// ─────────────────────────────────────────────────────────────────────────────
// IMPORT EXCEL
// ─────────────────────────────────────────────────────────────────────────────

interface ParsedProductRow {
  ingredientRaw: string;
  ingredientId: string;
  label: string;
  unit: string;
  unitPrice: string;
  conversionToBase: string;
  conversionNote: string;
  packaging: string;
  effectiveFrom: string; // yyyy-mm-dd
  valid: boolean;
  error?: string;
  /**
   * 13/08/2026 (theo Nam) — trước đây nguyên liệu đã khai bị TỪ CHỐI thẳng ("sửa dòng đó
   * thay vì tạo mới"), nên Excel chỉ dùng để khai mới, không sửa giá hàng loạt được. Nay
   * dòng trùng nguyên liệu chuyển sang CẬP NHẬT giá của sản phẩm đã có (`existingProductId`),
   * thay vì bị chặn — Excel dùng được cho cả 2 việc: khai mới VÀ sửa giá hàng loạt.
   */
  isUpdate: boolean;
  existingProductId?: string;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function getCellStr(cell: any): string {
  if (!cell || cell.value === null || cell.value === undefined) return "";
  const v = cell.value;
  if (v instanceof Date) {
    const p = (n: number) => String(n).padStart(2, "0");
    return `${v.getFullYear()}-${p(v.getMonth() + 1)}-${p(v.getDate())}`;
  }
  if (typeof v === "object") {
    if ("result" in v) return String(v.result ?? "").trim();
    if ("text" in v) return String(v.text ?? "").trim();
    if ("richText" in v) return v.richText.map((t: { text?: string }) => t.text || "").join("").trim();
    return JSON.stringify(v);
  }
  return String(v).trim();
}

/** Chuẩn hoá ngày: "d/m/yyyy" hoặc "dd/mm/yyyy" → "yyyy-mm-dd"; giữ nguyên nếu đã ISO. */
function normalizeDate(raw: string): string {
  const vn = raw.trim().match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
  if (vn) return `${vn[3]}-${vn[2].padStart(2, "0")}-${vn[1].padStart(2, "0")}`;
  if (/^\d{4}-\d{2}-\d{2}$/.test(raw.trim())) return raw.trim();
  return "";
}

function validateProductRow(
  row: Omit<ParsedProductRow, "valid" | "error" | "isUpdate" | "existingProductId">,
  catalog: IngredientDto[],
  existingByIngredientId: Map<string, string>,
  seenIngredients: Set<string>
): ParsedProductRow {
  const ing = catalog.find(
    (c) =>
      (c.id ?? "").toLowerCase() === row.ingredientRaw.toLowerCase() ||
      (c.name ?? "").toLowerCase() === row.ingredientRaw.toLowerCase()
  );
  let error: string | undefined;

  if (!row.ingredientRaw.trim()) {
    error = "Nguyên liệu không được để trống.";
  } else if (!ing) {
    error = `"${row.ingredientRaw}" không có trong danh mục — dùng tên hoặc mã nguyên liệu đúng.`;
  } else if (seenIngredients.has(ing.id!)) {
    error = "Trùng nguyên liệu với dòng khác trong file.";
  } else if (!row.unit.trim()) {
    error = "Đơn vị bán không được để trống.";
  } else if (!row.unitPrice.trim() || !(Number(row.unitPrice) > 0)) {
    error = "Đơn giá phải là số lớn hơn 0.";
  } else if (!row.effectiveFrom || !/^\d{4}-\d{2}-\d{2}$/.test(row.effectiveFrom)) {
    error = "Ngày hiệu lực trống hoặc sai định dạng (dd/mm/yyyy hoặc yyyy-mm-dd).";
  }

  if (!error && ing?.id) seenIngredients.add(ing.id!);
  const existingProductId = ing?.id ? existingByIngredientId.get(ing.id) : undefined;

  return {
    ...row,
    ingredientId: ing?.id ?? "",
    valid: !error,
    error,
    isUpdate: !error && existingProductId != null,
    existingProductId,
  };
}

async function parseProductExcel(
  file: File,
  catalog: IngredientDto[],
  existingByIngredientId: Map<string, string>
): Promise<ParsedProductRow[]> {
  const ExcelJS = await import("exceljs");
  const wb = new ExcelJS.Workbook();
  await wb.xlsx.load(await file.arrayBuffer());
  const ws = wb.getWorksheet(1);
  if (!ws) throw new Error("Không tìm thấy worksheet nào trong file.");

  const rows: ParsedProductRow[] = [];
  const seen = new Set<string>();

  ws.eachRow({ includeEmpty: false }, (row, rowNum) => {
    if (rowNum === 1) return;
    const ingredientRaw = getCellStr(row.getCell(1));
    const label        = getCellStr(row.getCell(2));
    const unit         = getCellStr(row.getCell(3));
    const unitPrice    = getCellStr(row.getCell(4));
    const conversionToBase = getCellStr(row.getCell(5));
    const conversionNote   = getCellStr(row.getCell(6));
    const packaging        = getCellStr(row.getCell(7));
    const effectiveFrom    = normalizeDate(getCellStr(row.getCell(8)));

    if (!ingredientRaw && !unitPrice) return;

    rows.push(
      validateProductRow(
        { ingredientRaw, ingredientId: "", label, unit, unitPrice, conversionToBase, conversionNote, packaging, effectiveFrom },
        catalog, existingByIngredientId, seen
      )
    );
  });

  return rows;
}

async function downloadProductExcelTemplate(catalog: IngredientDto[]) {
  try {
    const ExcelJS = await import("exceljs");
    const wb = new ExcelJS.Workbook();
    const ws  = wb.addWorksheet("Khai báo mặt hàng");
    const ref = wb.addWorksheet("DataRef");
    ref.state = "hidden";

    catalog.forEach((c, i) => { ref.getCell(i + 1, 1).value = c.name ?? c.id; });

    ws.columns = [
      { header: "Nguyên liệu * (chọn từ danh mục chuẩn)", key: "ingredient", width: 36 },
      { header: "Tên trên hoá đơn/bao bì (tuỳ chọn)",     key: "label",      width: 30 },
      { header: "Đơn vị bán *",                            key: "unit",       width: 14 },
      { header: "Đơn giá * (₫/đơn vị bán)",               key: "price",      width: 20 },
      { header: "Hệ số quy đổi (1 nếu bán theo đơn vị chuẩn)", key: "conv",  width: 36 },
      { header: "Ghi chú quy đổi",                         key: "convNote",   width: 26 },
      { header: "Quy cách đóng gói",                       key: "pkg",        width: 22 },
      { header: "Hiệu lực từ * (dd/mm/yyyy)",              key: "eff",        width: 22 },
    ];

    const hr = ws.getRow(1);
    hr.font      = { name: "Arial", size: 11, bold: true, color: { argb: "FFFFFFFF" } };
    hr.fill      = { type: "pattern", pattern: "solid", fgColor: { argb: "FF1E3A8A" } };
    hr.alignment = { vertical: "middle", horizontal: "center" };
    hr.height    = 25;

    if (catalog.length > 0) {
      ws.addRow({
        ingredient: catalog[0].name ?? catalog[0].id,
        label:      "",
        unit:       catalog[0].unit ?? "kg",
        price:      "50000",
        conv:       "1",
        convNote:   "",
        pkg:        "",
        eff:        `01/08/${new Date().getFullYear()}`,
      });
      ws.getRow(2).height = 20;
      ws.getRow(2).alignment = { vertical: "middle" };

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (ws as any).dataValidations.add("A2:A500", {
        type: "list", allowBlank: false,
        formulae: [`DataRef!$A$1:$A$${catalog.length}`],
        showErrorMessage: true,
        errorTitle: "Lỗi nguyên liệu",
        error: "Vui lòng chọn nguyên liệu từ danh mục chuẩn.",
      });
    }

    const buf = await wb.xlsx.writeBuffer();
    const url = URL.createObjectURL(new Blob([buf], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" }));
    const a = document.createElement("a");
    a.href = url; a.download = "template-import-mat-hang.xlsx"; a.click();
    URL.revokeObjectURL(url);
    toast.success("Tải template Excel thành công!");
  } catch (err) {
    console.error(err);
    toast.error("Tải template thất bại.");
  }
}

function ImportProductsDialog({
  open,
  onOpenChange,
  ingredientCatalog,
  existingProducts,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  ingredientCatalog: IngredientDto[];
  existingProducts: SupplierProductDto[];
}) {
  const create = useCreate<Partial<SupplierProductDto>>("supplier-products");
  const update = useUpdate<{ id: string } & Partial<SupplierProductDto>>("supplier-products");
  const [step, setStep] = useState<"upload" | "preview" | "done">("upload");
  const [file, setFile] = useState<File | null>(null);
  const [parsed, setParsed] = useState<ParsedProductRow[]>([]);
  const [parseError, setParseError] = useState("");
  const [importing, setImporting] = useState(false);
  const [successCount, setSuccessCount] = useState(0);

  const existingByIngredientId = new Map(
    existingProducts
      .filter((p) => p.ingredientId && p.id)
      .map((p) => [p.ingredientId as string, p.id as string])
  );

  function resetAll() {
    setStep("upload"); setFile(null); setParsed([]); setParseError(""); setSuccessCount(0);
  }

  function handleFile(f: File) {
    setFile(f);
    setParseError("");
    parseProductExcel(f, ingredientCatalog, existingByIngredientId)
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
    const valid = parsed.filter((r) => r.valid);
    let ok = 0;
    /*
     * TUẦN TỰ (cùng lý do với catalog/page.tsx): CodeGenerator.next() dùng REQUIRES_NEW
     * → mỗi POST cần 2 DB connection; bắn song song làm cạn Hikari pool (10 conn mặc định).
     * Dòng đã có sản phẩm (`isUpdate`) đi PUT, không tốn CodeGenerator nên không bị giới hạn
     * này — vẫn giữ tuần tự chung cho đơn giản, không cần tối ưu riêng.
     */
    for (const r of valid) {
      const body = {
        label: r.label.trim() || undefined,
        unit: r.unit.trim(),
        unitPrice: Number(r.unitPrice),
        conversionToBase: r.conversionToBase.trim() ? Number(r.conversionToBase) : 1,
        conversionNote: r.conversionNote.trim() || undefined,
        packaging: r.packaging.trim() || undefined,
        effectiveFrom: r.effectiveFrom,
      };
      await new Promise<void>((resolve) => {
        if (r.isUpdate && r.existingProductId) {
          update.mutate(
            { id: r.existingProductId, ...body },
            { onSuccess: () => { ok++; resolve(); }, onError: () => resolve() }
          );
        } else {
          create.mutate(
            { id: `SP-${String(Date.now()).slice(-6)}`, ingredientId: r.ingredientId, ...body },
            { onSuccess: () => { ok++; resolve(); }, onError: () => resolve() }
          );
        }
      });
    }
    setSuccessCount(ok);
    setImporting(false);
    setStep("done");
  }

  const validCount   = parsed.filter((r) => r.valid).length;
  const updateCount  = parsed.filter((r) => r.valid && r.isUpdate).length;
  const invalidCount = parsed.length - validCount;
  const nameOf = (id: string) => ingredientCatalog.find((c) => c.id === id)?.name ?? id;

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) resetAll(); onOpenChange(v); }}>
      <DialogContent className={step === "preview" ? "max-w-5xl max-h-[85vh] overflow-y-auto" : "sm:max-w-[520px]"}>
        <DialogHeader>
          <DialogTitle>Nhập mặt hàng từ Excel</DialogTitle>
          <DialogDescription>
            Mỗi dòng khai một nguyên liệu — chọn từ danh mục chuẩn để lô hàng và đơn đặt hàng nối được đúng.
            Nguyên liệu đã khai rồi sẽ được CẬP NHẬT giá/quy cách theo dòng mới, không tạo trùng — dùng được
            để sửa giá hàng loạt.
          </DialogDescription>
        </DialogHeader>

        {step === "upload" && (
          <div className="space-y-4 py-2">
            <div className="flex flex-col items-center justify-center border-2 border-dashed rounded-lg p-6 bg-muted/30">
              <FileUp className="size-10 text-muted-foreground mb-2" />
              <p className="text-sm font-medium">Chọn file Excel danh sách mặt hàng</p>
              <p className="text-xs text-muted-foreground mt-1">Hỗ trợ file `.xlsx`</p>
              <Input
                id="product-excel-file"
                type="file"
                accept=".xlsx"
                className="mt-4"
                onChange={(e) => {
                  const f = e.target.files?.[0];
                  if (f) handleFile(f);
                }}
              />
            </div>
            {parseError && <p className="text-sm font-medium text-destructive">{parseError}</p>}
            <div className="border-t pt-4 flex justify-between items-center">
              <Button
                variant="outline"
                disabled={ingredientCatalog.length === 0}
                onClick={() => downloadProductExcelTemplate(ingredientCatalog)}
              >
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
                {updateCount > 0 && <Badge variant="info">{updateCount} sẽ cập nhật giá</Badge>}
                <Badge variant="danger">{invalidCount} lỗi</Badge>
              </div>
              <p className="text-xs text-muted-foreground">Tên file: {file?.name}</p>
            </div>
            <div className="border rounded-lg overflow-hidden max-h-[45vh] overflow-y-auto">
              <table className="w-full text-sm text-left border-collapse">
                <thead className="bg-muted text-muted-foreground text-xs uppercase font-medium">
                  <tr>
                    <th className="p-3 border-b">Nguyên liệu</th>
                    <th className="p-3 border-b">Tên hoá đơn</th>
                    <th className="p-3 border-b">Đơn vị</th>
                    <th className="p-3 border-b text-right">Đơn giá</th>
                    <th className="p-3 border-b">Quy đổi</th>
                    <th className="p-3 border-b">Quy cách</th>
                    <th className="p-3 border-b">Hiệu lực từ</th>
                    <th className="p-3 border-b">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {parsed.map((r, i) => (
                    <tr key={i} className={r.valid ? "hover:bg-muted/10" : "bg-destructive/5 hover:bg-destructive/10"}>
                      <td className="p-3 border-b font-medium">
                        {r.ingredientId ? nameOf(r.ingredientId) : r.ingredientRaw || "—"}
                      </td>
                      <td className="p-3 border-b text-muted-foreground">{r.label || "—"}</td>
                      <td className="p-3 border-b">{r.unit || "—"}</td>
                      <td className="p-3 border-b text-right">
                        {r.unitPrice ? `${Number(r.unitPrice).toLocaleString("vi-VN")}₫` : "—"}
                      </td>
                      <td className="p-3 border-b">{r.conversionToBase || "1"}</td>
                      <td className="p-3 border-b text-muted-foreground">{r.packaging || "—"}</td>
                      <td className="p-3 border-b">{r.effectiveFrom ? formatVnDate(r.effectiveFrom) : "—"}</td>
                      <td className="p-3 border-b text-right">
                        {!r.valid ? (
                          <span className="text-xs text-destructive">{r.error}</span>
                        ) : r.isUpdate ? (
                          <Badge variant="info">Cập nhật giá</Badge>
                        ) : (
                          <Badge variant="success">Hợp lệ (mới)</Badge>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="flex justify-between items-center border-t pt-4">
              <Button variant="outline" onClick={() => { setStep("upload"); setParsed([]); setFile(null); }}>
                Quay lại
              </Button>
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
              Đã xử lý thành công <span className="font-semibold text-foreground">{successCount}</span> trên tổng số{" "}
              <span className="font-semibold text-foreground">{validCount}</span> dòng hợp lệ
              {updateCount > 0 && ` (gồm ${updateCount} dòng cập nhật giá)`}.
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

// ─────────────────────────────────────────────────────────────────────────────
// TRANG CHÍNH
// ─────────────────────────────────────────────────────────────────────────────

export default function SupplierProductsPage() {
  const partnerId = useAuthStore((s) => s.user?.partnerId);
  const { data: ingredientCatalog = [] } = usePlainList<IngredientDto>("ingredients");

  // Dùng để validate trùng khi import / mở dialog "Sửa" — cần toàn bộ danh sách,
  // không chỉ trang hiện tại (DataTable tự quản page).
  const { data: allProductsPage } = usePagedList<SupplierProductDto>("supplier-products", {
    pageSize: 500,
    supplierId: partnerId,
  });
  const allProducts = allProductsPage?.items ?? [];

  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<SupplierProductDto | null>(null);
  const [importOpen, setImportOpen] = useState(false);

  const nameOf = (id?: string) =>
    ingredientCatalog.find((c) => c.id === id)?.name ?? id ?? "—";

  const columns: ColumnDef<SupplierProductDto, unknown>[] = [
    {
      accessorKey: "ingredientId",
      header: "Nguyên liệu",
      cell: ({ row }) => <span className="font-medium">{nameOf(row.original.ingredientId)}</span>,
    },
    {
      accessorKey: "label",
      header: "Tên trên hoá đơn",
      cell: ({ row }) =>
        row.original.label
          ? <span>{row.original.label}</span>
          : <span className="text-xs text-muted-foreground">— dùng tên danh mục —</span>,
    },
    {
      accessorKey: "unitPrice",
      header: "Đơn giá",
      cell: ({ row }) => (
        <span className="font-medium">
          {(row.original.unitPrice ?? 0).toLocaleString("vi-VN")}₫/{row.original.unit}
        </span>
      ),
    },
    {
      accessorKey: "unit",
      header: "Đơn vị bán",
      cell: ({ row }) => <span className="text-muted-foreground">{row.original.unit ?? "—"}</span>,
    },
    {
      accessorKey: "packaging",
      header: "Quy cách",
      cell: ({ row }) => (
        <span className="text-muted-foreground">{row.original.packaging ?? "—"}</span>
      ),
    },
    {
      accessorKey: "conversionToBase",
      header: "Quy đổi",
      cell: ({ row }) => {
        const p = row.original;
        return Number(p.conversionToBase) === 1 || p.conversionToBase == null ? (
          <span className="text-xs text-muted-foreground">1:1</span>
        ) : (
          <span title={p.conversionNote ?? undefined}>
            1 {p.unit} = {p.conversionToBase}{p.conversionNote ? " ⓘ" : ""}
          </span>
        );
      },
    },
    {
      accessorKey: "effectiveFrom",
      header: "Hiệu lực từ",
      cell: ({ row }) => <Badge variant="neutral">{formatVnDate(row.original.effectiveFrom)}</Badge>,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex justify-end">
          <Button variant="outline" size="sm" onClick={(e) => { e.stopPropagation(); setEditing(row.original); }}>
            <Pencil /> Sửa giá
          </Button>
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Mặt hàng cung ứng"
        description="Danh mục mặt hàng và đơn giá gửi tới trường — cơ sở để trường ước tính chi phí bữa ăn và đặt hàng"
        actions={
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setImportOpen(true)}>
              <FileUp /> Nhập Excel
            </Button>
            <Button onClick={() => setCreating(true)}>
              <Plus /> Thêm mặt hàng
            </Button>
          </div>
        }
      />

      <Alert variant="info" className="mb-6" title="Cách khai mặt hàng cung ứng">
        Chọn nguyên liệu từ danh mục chuẩn của trường để lô hàng nhập kho được nối đúng
        về công thức món ăn và chuỗi truy xuất. Tên riêng của bạn trên hoá đơn/bao bì
        (ví dụ: &ldquo;Tôm sú đông lạnh size 30&rdquo;) khai ở ô &ldquo;Tên trên hoá đơn&rdquo;.
        Các nguyên liệu bạn đã đăng ký không hiện lại trong danh sách — sửa giá thì dùng
        nút &ldquo;Sửa giá&rdquo; ở từng dòng. Danh sách này là danh mục CHUNG của bạn, gửi tới
        mọi trường đang hợp tác — riêng sản phẩm/giá cho TỪNG trường (theo hợp đồng, cần trường
        duyệt trước khi có hiệu lực) nằm ở mục &ldquo;Trường hợp tác → Danh mục &amp; đề xuất&rdquo;.
      </Alert>

      <DataTable<SupplierProductDto>
        resource="supplier-products"
        columns={columns}
        filters={{ supplierId: partnerId }}
        searchPlaceholder="Tìm theo mã, tên hoá đơn…"
        emptyMessage="Chưa khai mặt hàng nào — trường sẽ không đặt hàng được từ đơn vị của bạn."
      />

      {(creating || editing) && (
        <ProductDialog
          product={editing ?? undefined}
          existing={allProducts}
          ingredientCatalog={ingredientCatalog}
          onClose={() => {
            setCreating(false);
            setEditing(null);
          }}
        />
      )}

      <ImportProductsDialog
        open={importOpen}
        onOpenChange={setImportOpen}
        ingredientCatalog={ingredientCatalog}
        existingProducts={allProducts}
      />
    </>
  );
}
