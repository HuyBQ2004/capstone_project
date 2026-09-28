"use client";

import { useState } from "react";
import {
  FlaskConical,
  Plus,
  Pencil,
  Trash2,
  RotateCcw,
  Search,
  Download,
  FileUp,
  CheckCircle2,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Label, Select, SearchableSelect } from "@/components/ui/input";
import { SimpleTable } from "@/components/app/simple-table";
import { usePlainList, useCreate, useUpdate, useDelete } from "@/lib/api/hooks";
import type { IngredientDto } from "@/lib/api/types";
import { useMeasureUnits } from "@/lib/measure-units";
import { ALLERGENS } from "@/lib/domain-utils";
import { cn } from "@/lib/utils";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Pagination, usePagination } from "@/components/app/pagination";

const ZONE_LABEL: Record<string, string> = {
  DRY: "Khô",
  CHILLED: "Lạnh",
  FROZEN: "Đông lạnh",
};

const ZONE_VARIANT: Record<string, "neutral" | "info" | "warning"> = {
  DRY: "neutral",
  CHILLED: "info",
  FROZEN: "warning",
};

const ZONES = ["DRY", "CHILLED", "FROZEN"];

interface FormState {
  code: string;
  name: string;
  unit: string;
  storageZone: string;
  refuseFactor: string;
  /**
   * BR-063 (13/08/2026) — tác nhân dị ứng của nguyên liệu, khai TẠI ĐÂY.
   *
   * Trước đó allergen không phải dữ liệu: frontend đoán từ TỪ KHOÁ trong tên
   * (`ALLERGEN_KEYWORDS`), backend thì chỉ đọc `dishes.manual_allergens`. Hai bên lệch nhau,
   * và cách đoán sai thật — đo trên danh mục hiện tại: "Bánh mỳ" không khớp từ khoá "bánh mì"
   * (i/y) nên mất cảnh báo Gluten; "Nước mắm", "Dầu hào", "Sữa đặc có đường" cũng không khớp
   * gì. Ngược lại "Cá mè" bị gán nhầm "Mè (vừng)".
   */
  allergens: string[];
}

const EMPTY_FORM: FormState = {
  code: "",
  name: "",
  unit: "kg",
  storageZone: "DRY",
  refuseFactor: "0",
  allergens: [],
};

interface ParsedIngredientRow {
  code: string;
  name: string;
  unit: string;
  storageZone: string;
  refuseFactor: string;
  valid: boolean;
  error?: string;
}

function normalizeStorageZone(raw: string): string {
  const zone = raw.trim().toUpperCase();
  if (zone === "DRY" || zone === "KHÔ" || zone === "KHO") return "DRY";
  if (zone === "CHILLED" || zone === "LẠNH" || zone === "LANH") return "CHILLED";
  if (zone === "FROZEN" || zone === "ĐÔNG LẠNH" || zone === "DONG LANH" || zone === "ĐÔNG" || zone === "DONG") return "FROZEN";
  return zone;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function getCellValueAsString(cell: any): string {
  if (!cell || cell.value === null || cell.value === undefined) return "";
  if (typeof cell.value === "object") {
    if ("result" in cell.value) {
      return String(cell.value.result ?? "").trim();
    }
    if ("text" in cell.value) {
      return String(cell.value.text ?? "").trim();
    }
    if ("richText" in cell.value) {
      return cell.value.richText.map((t: { text?: string }) => t.text || "").join("").trim();
    }
    return JSON.stringify(cell.value);
  }
  return String(cell.value).trim();
}

function validateIngredientRow(
  row: Omit<ParsedIngredientRow, "valid" | "error">,
  measureUnits: string[],
  seenCodes: Set<string>,
  seenNames: Set<string>,
  dbCodes: Set<string>,
  dbNames: Set<string>
): ParsedIngredientRow {
  const { code, name, unit, storageZone, refuseFactor } = row;
  let error: string | undefined;

  const normalizedZone = normalizeStorageZone(storageZone);

  if (!name.trim()) {
    error = "Tên nguyên liệu không được để trống.";
  } else if (!unit.trim()) {
    error = "Đơn vị tính không được để trống.";
  } else if (!measureUnits.includes(unit.trim())) {
    error = `Đơn vị tính không hợp lệ (chấp nhận: ${measureUnits.join(", ")}).`;
  } else if (!["DRY", "CHILLED", "FROZEN"].includes(normalizedZone)) {
    error = "Khu lưu trữ không hợp lệ (chấp nhận: Khô, Lạnh, Đông lạnh).";
  } else {
    const refuseVal = parseFloat(refuseFactor);
    if (refuseFactor.trim() && (isNaN(refuseVal) || refuseVal < 0 || refuseVal > 99.9)) {
      error = "Hao hụt phải là số từ 0 đến 99.9%.";
    } else if (code.trim() && seenCodes.has(code.trim().toUpperCase())) {
      error = "Mã nguyên liệu trùng trong file.";
    } else if (code.trim() && dbCodes.has(code.trim().toUpperCase())) {
      error = "Mã nguyên liệu đã tồn tại trong hệ thống.";
    } else if (seenNames.has(name.trim().toLowerCase())) {
      error = "Tên nguyên liệu trùng trong file.";
    } else if (dbNames.has(name.trim().toLowerCase())) {
      error = "Tên nguyên liệu đã tồn tại trong hệ thống.";
    }
  }

  if (!error) {
    if (code.trim()) seenCodes.add(code.trim().toUpperCase());
    seenNames.add(name.trim().toLowerCase());
  }

  return {
    code: code.trim(),
    name: name.trim(),
    unit: unit.trim(),
    storageZone: normalizedZone,
    refuseFactor: refuseFactor.trim() || "0",
    valid: !error,
    error,
  };
}

async function parseIngredientExcel(
  file: File,
  validUnits: string[],
  dbCodes: Set<string>,
  dbNames: Set<string>
): Promise<ParsedIngredientRow[]> {
  const ExcelJS = await import("exceljs");
  const workbook = new ExcelJS.Workbook();
  const arrayBuffer = await file.arrayBuffer();
  await workbook.xlsx.load(arrayBuffer);

  const worksheet = workbook.getWorksheet(1);
  if (!worksheet) {
    throw new Error("Không tìm thấy worksheet nào trong file Excel.");
  }

  const rows: ParsedIngredientRow[] = [];
  const seenCodes = new Set<string>();
  const seenNames = new Set<string>();

  worksheet.eachRow({ includeEmpty: false }, (row, rowNumber) => {
    // Bỏ qua dòng tiêu đề
    if (rowNumber === 1) return;

    const code = getCellValueAsString(row.getCell(1));
    const name = getCellValueAsString(row.getCell(2));
    const unit = getCellValueAsString(row.getCell(3));
    const storageZone = getCellValueAsString(row.getCell(4));
    const refuseFactor = getCellValueAsString(row.getCell(5));

    if (!code && !name && !unit && !storageZone && !refuseFactor) {
      return; // Bỏ qua dòng trống
    }

    rows.push(
      validateIngredientRow(
        { code, name, unit, storageZone, refuseFactor },
        validUnits,
        seenCodes,
        seenNames,
        dbCodes,
        dbNames
      )
    );
  });

  return rows;
}

async function downloadIngredientExcelTemplate(validUnits: string[]) {
  try {
    const ExcelJS = await import("exceljs");
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet("Khai báo nguyên liệu");
    const refSheet = workbook.addWorksheet("DataRef");
    refSheet.state = "hidden";

    // Ghi dữ liệu tham chiếu vào sheet ẩn để làm nguồn dropdown (tránh lỗi định dạng phân cách dấu phẩy/phẩy phẩy của Excel tuỳ hệ điều hành)
    const zones = ["Khô", "Lạnh", "Đông lạnh"];
    zones.forEach((z, idx) => {
      refSheet.getCell(idx + 1, 1).value = z;
    });

    const units = validUnits.length > 0 ? validUnits : ["kg", "g", "lít", "ml", "quả", "hộp", "bó", "ổ"];
    units.forEach((u, idx) => {
      refSheet.getCell(idx + 1, 2).value = u;
    });

    worksheet.views = [{ showGridLines: true }];

    worksheet.columns = [
      { header: "Mã nguyên liệu (Tùy chọn)", key: "code", width: 25 },
      { header: "Tên nguyên liệu *", key: "name", width: 30 },
      { header: "Đơn vị tính *", key: "unit", width: 15 },
      { header: "Khu lưu trữ (Khô/Lạnh/Đông lạnh) *", key: "storageZone", width: 35 },
      { header: "Hao hụt sơ chế (%)", key: "refuseFactor", width: 20 },
    ];

    const headerRow = worksheet.getRow(1);
    headerRow.font = { name: "Arial", size: 11, bold: true, color: { argb: "FFFFFFFF" } };
    headerRow.fill = {
      type: "pattern",
      pattern: "solid",
      fgColor: { argb: "FF1E3A8A" }
    };
    headerRow.alignment = { vertical: "middle", horizontal: "center" };
    headerRow.height = 25;

    // Dòng dữ liệu mẫu
    worksheet.addRow({
      code: "ING-HANH-TAY",
      name: "Hành tây",
      unit: units[0],
      storageZone: "Khô",
      refuseFactor: "5.5"
    });

    worksheet.getRow(2).height = 20;
    worksheet.getRow(2).alignment = { vertical: "middle" };

    // Dropdown validations
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (worksheet as any).dataValidations.add("D2:D500", {
      type: "list",
      allowBlank: false,
      formulae: [`DataRef!$A$1:$A$${zones.length}`],
      showErrorMessage: true,
      errorTitle: "Lỗi chọn khu lưu trữ",
      error: "Vui lòng chọn khu lưu trữ từ danh sách: Khô, Lạnh, hoặc Đông lạnh."
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (worksheet as any).dataValidations.add("C2:C500", {
      type: "list",
      allowBlank: false,
      formulae: [`DataRef!$B$1:$B$${units.length}`],
      showErrorMessage: true,
      errorTitle: "Lỗi chọn đơn vị tính",
      error: "Vui lòng chọn một đơn vị tính hợp lệ."
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (worksheet as any).dataValidations.add("E2:E500", {
      type: "decimal",
      operator: "between",
      allowBlank: true,
      formulae: ["0", "99.9"],
      showErrorMessage: true,
      errorTitle: "Lỗi nhập hao hụt",
      error: "Hao hụt sơ chế phải nằm trong khoảng từ 0% đến 99.9%."
    });

    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "template-import-nguyen-lieu.xlsx";
    a.click();
    URL.revokeObjectURL(url);
    toast.success("Tải template Excel thành công!");
  } catch (err) {
    console.error(err);
    toast.error("Tải template thất bại.");
  }
}

async function exportToExcel(data: IngredientDto[]) {
  try {
    const ExcelJS = await import("exceljs");
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet("Danh mục nguyên liệu");

    worksheet.views = [{ showGridLines: true }];

    worksheet.columns = [
      { header: "Mã nguyên liệu", key: "id", width: 25 },
      { header: "Tên nguyên liệu", key: "name", width: 30 },
      { header: "Đơn vị tính", key: "unit", width: 15 },
      { header: "Khu lưu trữ", key: "storageZone", width: 15 },
      { header: "Hao hụt (%)", key: "refuseFactor", width: 15 },
    ];

    const headerRow = worksheet.getRow(1);
    headerRow.font = { name: "Arial", size: 11, bold: true, color: { argb: "FFFFFFFF" } };
    headerRow.fill = {
      type: "pattern",
      pattern: "solid",
      fgColor: { argb: "FF1E3A8A" }
    };
    headerRow.alignment = { vertical: "middle", horizontal: "center" };
    headerRow.height = 25;

    data.forEach((i) => {
      worksheet.addRow({
        id: i.id ?? "",
        name: i.name ?? "",
        unit: i.unit ?? "",
        storageZone: ZONE_LABEL[i.storageZone ?? ""] ?? i.storageZone ?? "",
        refuseFactor: (Number(i.refuseFactor ?? 0) * 100).toFixed(1),
      });
    });

    worksheet.eachRow((row, rowNumber) => {
      if (rowNumber > 1) {
        row.height = 20;
        row.alignment = { vertical: "middle" };
      }
    });

    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `danh-muc-nguyen-lieu-${new Date().toISOString().slice(0, 10)}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success("Xuất Excel thành công!");
  } catch (err) {
    console.error(err);
    toast.error("Xuất Excel thất bại.");
  }
}

const PAGE_SIZE = 10;

export default function IngredientsPage() {
  const { data: ingredients = [], isLoading, isError, error, refetch } = usePlainList<IngredientDto>("ingredients");
  const measureUnits = useMeasureUnits();

  const create = useCreate<IngredientDto>("ingredients", { handledError: true });
  const update = useUpdate<{ id: string } & Partial<IngredientDto>>("ingredients", { handledError: true });
  const remove = useDelete("ingredients", { handledError: true });

  const [search, setSearch] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingCode, setEditingCode] = useState<string | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  // States for importing
  const [showImport, setShowImport] = useState(false);
  const [importFile, setImportFile] = useState<File | null>(null);
  const [importParsed, setImportParsed] = useState<ParsedIngredientRow[]>([]);
  const [importing, setImporting] = useState(false);
  const [importError, setImportError] = useState("");
  const [importSuccessCount, setImportSuccessCount] = useState(0);
  const [importStep, setImportStep] = useState<"upload" | "preview" | "done">("upload");

  const filtered = ingredients.filter(
    (i) =>
      (i.name ?? "").toLowerCase().includes(search.toLowerCase()) ||
      (i.id ?? "").toLowerCase().includes(search.toLowerCase())
  );

  const { rows, page, pageCount, setPage, total } = usePagination(filtered, PAGE_SIZE);

  function openCreate() {
    setEditingCode(null);
    setForm(EMPTY_FORM);
    setShowForm(true);
  }

  function openEdit(ing: IngredientDto) {
    setEditingCode(ing.id ?? null);
    setForm({
      code: ing.id ?? "",
      name: ing.name ?? "",
      unit: ing.unit ?? "",
      storageZone: ing.storageZone ?? "DRY",
      refuseFactor: String(Number((Number(ing.refuseFactor ?? 0) * 100).toFixed(1))),
      allergens: [...(ing.allergens ?? [])],
    });
    setShowForm(true);
  }

  function closeForm() {
    setShowForm(false);
    setEditingCode(null);
    setForm(EMPTY_FORM);
  }

  function patch(key: keyof FormState, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function toggleAllergen(allergen: string) {
    setForm((prev) => ({
      ...prev,
      allergens: prev.allergens.includes(allergen)
        ? prev.allergens.filter((a) => a !== allergen)
        : [...prev.allergens, allergen],
    }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.name.trim()) {
      toast.error("Tên nguyên liệu không được để trống.");
      return;
    }
    setSaving(true);
    const payload = {
      code: form.code.trim() || undefined,
      name: form.name.trim(),
      unit: form.unit.trim(),
      storageZone: form.storageZone,
      refuseFactor: (parseFloat(form.refuseFactor) || 0) / 100,
      allergens: form.allergens,
    };

    if (editingCode) {
      update.mutate(
        { id: editingCode, ...payload },
        {
          onSuccess: () => {
            toast.success(`Đã cập nhật "${form.name}".`);
            closeForm();
            refetch();
          },
          onError: () => toast.error("Cập nhật thất bại. Thử lại sau."),
          onSettled: () => setSaving(false),
        }
      );
    } else {
      create.mutate(payload as unknown as IngredientDto, {
        onSuccess: () => {
          toast.success(`Đã thêm "${form.name}" vào danh mục.`);
          closeForm();
          refetch();
        },
        onError: (err: unknown) => {
          const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
          toast.error(msg ?? "Thêm thất bại. Mã có thể đã tồn tại.");
        },
        onSettled: () => setSaving(false),
      });
    }
  }

  function handleDeactivate(ing: IngredientDto) {
    if (!ing.id) return;
    if (!confirm(`Vô hiệu hoá nguyên liệu "${ing.name}"? Nó sẽ không còn xuất hiện trong danh sách chọn.`)) return;
    remove.mutate(ing.id, {
      onSuccess: () => {
        toast.success(`Đã vô hiệu hoá "${ing.name}".`);
        refetch();
      },
      onError: () => toast.error("Vô hiệu hoá thất bại."),
    });
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Danh mục Nguyên liệu"
        description="Quản lý danh sách nguyên liệu chuẩn toàn hệ thống. Nhà cung cấp chỉ được chào bán nguyên liệu có trong danh mục này."
      />

      {/* Thanh công cụ */}
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div className="relative w-full sm:w-80">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            className="pl-9"
            placeholder="Tìm theo tên hoặc mã…"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(1);
            }}
          />
        </div>
        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto shrink-0">
          <Button variant="outline" onClick={() => { setImportStep("upload"); setShowImport(true); }}>
            <FileUp className="size-4 mr-1.5" /> Nhập Excel
          </Button>
          <Button variant="outline" onClick={() => exportToExcel(filtered)}>
            <Download className="size-4 mr-1.5" /> Xuất Excel
          </Button>
          <Button onClick={openCreate}>
            <Plus className="size-4 mr-1.5" /> Thêm nguyên liệu
          </Button>
        </div>
      </div>

      {/* Dialog Thêm/Sửa */}
      <Dialog open={showForm} onOpenChange={(open) => { if (!open) closeForm(); }}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>
              {editingCode ? `Cập nhật nguyên liệu: ${editingCode}` : "Thêm nguyên liệu mới"}
            </DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4 pt-2">
            {!editingCode && (
              <div className="space-y-1">
                <Label htmlFor="ing-code">Mã nguyên liệu (tuỳ chọn)</Label>
                <Input
                  id="ing-code"
                  placeholder="VD: ING-HANH-TAY (để trống tự sinh)"
                  value={form.code}
                  onChange={(e) => patch("code", e.target.value)}
                />
              </div>
            )}
            <div className="space-y-1">
              <Label htmlFor="ing-name">Tên nguyên liệu *</Label>
              <Input
                id="ing-name"
                placeholder="VD: Hành tây"
                value={form.name}
                onChange={(e) => patch("name", e.target.value)}
                required
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="ing-unit">Đơn vị tính *</Label>
              {measureUnits.ready ? (
                <SearchableSelect
                  value={form.unit}
                  onChange={(val) => patch("unit", val)}
                  options={measureUnits.options}
                  placeholder="— Chọn đơn vị —"
                  searchPlaceholder="Tìm đơn vị..."
                />
              ) : (
                <Select disabled id="ing-unit">
                  <option>Đang tải danh mục đơn vị…</option>
                </Select>
              )}
            </div>
            <div className="space-y-1">
              <Label htmlFor="ing-zone">Khu lưu trữ</Label>
              <SearchableSelect
                value={`${ZONE_LABEL[form.storageZone]} (${form.storageZone})`}
                onChange={(val) => {
                  const selectedZone = ZONES.find((z) => `${ZONE_LABEL[z]} (${z})` === val) || "DRY";
                  patch("storageZone", selectedZone);
                }}
                options={ZONES.map((z) => `${ZONE_LABEL[z]} (${z})`)}
                placeholder="— Chọn khu lưu trữ —"
                searchPlaceholder="Tìm khu lưu trữ..."
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="ing-refuse">Hao hụt sơ chế (%)</Label>
              <Input
                id="ing-refuse"
                type="number"
                min={0}
                max={99.9}
                step={0.1}
                placeholder="0.0"
                value={form.refuseFactor}
                onChange={(e) => patch("refuseFactor", e.target.value)}
              />
              <p className="text-xs text-muted-foreground">Nhập từ 0 đến 99.9 (ví dụ: 5.5 nghĩa là hao hụt 5.5%)</p>
            </div>

            {/*
              BR-063 — khai TÁC NHÂN DỊ ỨNG ở đây (13/08/2026, cột `catalog_ingredients.allergens`
              thêm ở V74). Đây là nơi duy nhất trong hệ thống khai được: món tự suy tác nhân từ
              thành phần, nên khai sai/thiếu ở dòng nào là mọi thực đơn dùng nguyên liệu đó mất
              cảnh báo. Dữ liệu hiện tại được suy từ TÊN khi chạy migration nên CHẮC CHẮN có sai —
              cần rà lại từng dòng.
            */}
            <div className="space-y-1.5">
              <Label>Tác nhân dị ứng (BR-063)</Label>
              <div className="flex flex-wrap gap-1.5">
                {ALLERGENS.map((a) => {
                  const on = form.allergens.includes(a);
                  return (
                    <button
                      key={a}
                      type="button"
                      onClick={() => toggleAllergen(a)}
                      aria-pressed={on}
                      className={cn(
                        "rounded-full border px-2.5 py-1 text-xs transition-colors",
                        on
                          ? "border-destructive bg-destructive/10 font-medium text-destructive"
                          : "border-input text-muted-foreground hover:border-destructive/50"
                      )}
                    >
                      {a}
                    </button>
                  );
                })}
              </div>
              <p className="text-xs text-muted-foreground">
                Bấm để bật/tắt. Không có tác nhân nào thì để trống. Thực đơn chứa nguyên liệu này
                sẽ tự cảnh báo cho học sinh dị ứng đúng tác nhân đã chọn.
              </p>
            </div>

            <DialogFooter className="pt-4 gap-2">
              <Button type="button" variant="outline" onClick={closeForm}>Huỷ</Button>
              <Button type="submit" disabled={saving}>
                {saving ? "Đang lưu…" : editingCode ? "Cập nhật" : "Thêm vào danh mục"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Dialog Nhập Excel */}
      <Dialog open={showImport} onOpenChange={(open) => { if (!open) { setShowImport(false); setImportParsed([]); setImportFile(null); setImportError(""); } }}>
        <DialogContent className={importStep === "preview" ? "max-w-4xl max-h-[85vh] overflow-y-auto" : "sm:max-w-[500px]"}>
          <DialogHeader>
            <DialogTitle>Nhập nguyên liệu từ Excel</DialogTitle>
          </DialogHeader>

          {importStep === "upload" && (
            <div className="space-y-4 py-4">
              <div className="flex flex-col items-center justify-center border-2 border-dashed rounded-lg p-6 bg-muted/30">
                <FileUp className="size-10 text-muted-foreground mb-2" />
                <p className="text-sm font-medium">Chọn file Excel danh sách nguyên liệu</p>
                <p className="text-xs text-muted-foreground mt-1">Hỗ trợ file `.xlsx`</p>
                <Input
                  id="excel-file"
                  type="file"
                  accept=".xlsx"
                  className="mt-4"
                  onChange={(e) => {
                    const file = e.target.files?.[0];
                    if (file) {
                      setImportFile(file);
                      setImportError("");
                      const dbCodes = new Set(ingredients.map(i => (i.id ?? "").toUpperCase()));
                      const dbNames = new Set(ingredients.map(i => (i.name ?? "").trim().toLowerCase()));
                      parseIngredientExcel(file, measureUnits.options, dbCodes, dbNames)
                        .then((rows) => {
                          if (rows.length === 0) {
                            setImportError("File rỗng hoặc không chứa dòng dữ liệu nào.");
                          } else {
                            setImportParsed(rows);
                            setImportStep("preview");
                          }
                        })
                        .catch((err) => {
                          console.error(err);
                          setImportError("Lỗi đọc file Excel. Vui lòng kiểm tra lại định dạng.");
                        });
                    }
                  }}
                />
              </div>

              {importError && (
                <p className="text-sm font-medium text-destructive">{importError}</p>
              )}

              <div className="border-t pt-4 flex justify-between items-center">
                <Button
                  variant="outline"
                  onClick={() => downloadIngredientExcelTemplate(measureUnits.options)}
                >
                  <Download className="size-4 mr-1.5" /> Tải Excel Template
                </Button>
                <Button variant="ghost" onClick={() => setShowImport(false)}>Huỷ</Button>
              </div>
            </div>
          )}

          {importStep === "preview" && (
            <div className="space-y-4 py-4">
              <div className="flex justify-between items-center">
                <div className="space-x-2">
                  <Badge variant="success">{importParsed.filter(r => r.valid).length} hợp lệ</Badge>
                  <Badge variant="danger">{importParsed.filter(r => !r.valid).length} lỗi</Badge>
                </div>
                <p className="text-xs text-muted-foreground">Tên file: {importFile?.name}</p>
              </div>

              <div className="border rounded-lg overflow-hidden max-h-[40vh] overflow-y-auto">
                <table className="w-full text-sm text-left border-collapse">
                  <thead className="bg-muted text-muted-foreground text-xs uppercase font-medium">
                    <tr>
                      <th className="p-3 border-b">Mã</th>
                      <th className="p-3 border-b">Tên nguyên liệu</th>
                      <th className="p-3 border-b">Đơn vị</th>
                      <th className="p-3 border-b">Khu lưu trữ</th>
                      <th className="p-3 border-b">Hao hụt</th>
                      <th className="p-3 border-b">Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {importParsed.map((r, i) => (
                      <tr key={i} className={r.valid ? "hover:bg-muted/10" : "bg-destructive/5 hover:bg-destructive/10"}>
                        <td className="p-3 border-b font-mono text-xs text-muted-foreground">{r.code || "—"}</td>
                        <td className="p-3 border-b font-medium">{r.name}</td>
                        <td className="p-3 border-b">{r.unit}</td>
                        <td className="p-3 border-b">{ZONE_LABEL[r.storageZone] || r.storageZone}</td>
                        <td className="p-3 border-b">{r.refuseFactor}%</td>
                        <td className="p-3 border-b text-right">
                          {r.valid ? (
                            <Badge variant="success">Hợp lệ</Badge>
                          ) : (
                            <span className="text-xs text-destructive">{r.error}</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="flex justify-between items-center border-t pt-4">
                <Button variant="outline" onClick={() => { setImportStep("upload"); setImportParsed([]); setImportFile(null); }}>
                  Quay lại
                </Button>
                <div className="space-x-2">
                  <Button variant="ghost" onClick={() => setShowImport(false)}>Huỷ</Button>
                  <Button
                    disabled={importParsed.filter(r => r.valid).length === 0 || importing}
                    onClick={async () => {
                      setImporting(true);
                      const validRows = importParsed.filter(r => r.valid);
                      let success = 0;
                      for (const row of validRows) {
                        const payload = {
                          code: row.code || undefined,
                          name: row.name,
                          unit: row.unit,
                          storageZone: row.storageZone,
                          refuseFactor: (parseFloat(row.refuseFactor) || 0) / 100,
                        };
                        await new Promise<void>((resolve) => {
                          create.mutate(payload as unknown as IngredientDto, {
                            onSuccess: () => {
                              success++;
                              resolve();
                            },
                            onError: () => {
                              resolve();
                            }
                          });
                        });
                      }
                      setImportSuccessCount(success);
                      setImporting(false);
                      setImportStep("done");
                      refetch();
                    }}
                  >
                    {importing ? "Đang nhập..." : `Xác nhận nhập ${importParsed.filter(r => r.valid).length} dòng`}
                  </Button>
                </div>
              </div>
            </div>
          )}

          {importStep === "done" && (
            <div className="space-y-4 py-4 text-center">
              <div className="mx-auto size-12 bg-success/10 rounded-full flex items-center justify-center text-success mb-2">
                <CheckCircle2 className="size-6" />
              </div>
              <h3 className="text-lg font-semibold">Hoàn thành nhập Excel</h3>
              <p className="text-sm text-muted-foreground">
                Đã thêm thành công <span className="font-semibold text-foreground">{importSuccessCount}</span> trên tổng số{" "}
                <span className="font-semibold text-foreground">{importParsed.filter(r => r.valid).length}</span> dòng hợp lệ.
              </p>
              <div className="border-t pt-4">
                <Button className="w-full" onClick={() => { setShowImport(false); setImportParsed([]); setImportFile(null); setImportStep("upload"); }}>
                  Đóng
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Bảng danh sách */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <FlaskConical className="size-4 text-primary" />
            Danh sách nguyên liệu
            <Badge variant="neutral" className="ml-1">{filtered.length}</Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <SimpleTable<IngredientDto>
            rows={rows}
            rowKey={(i) => i.id ?? ""}
            isLoading={isLoading}
            isError={isError}
            error={error}
            skeletonRows={4}
            emptyMessage={
              search ? "Không tìm thấy nguyên liệu phù hợp." : "Chưa có nguyên liệu nào trong danh mục."
            }
            columns={[
              { header: "Mã", cell: (i) => <span className="font-mono text-xs text-muted-foreground">{i.id}</span> },
              { header: "Tên nguyên liệu", cell: (i) => <span className="font-medium">{i.name}</span> },
              { header: "Đơn vị", cell: (i) => <span className="text-muted-foreground">{i.unit}</span> },
              {
                header: "Khu lưu trữ",
                cell: (i) => (
                  <Badge variant={(i.storageZone ? ZONE_VARIANT[i.storageZone] : undefined) ?? "neutral"}>
                    {(i.storageZone ? ZONE_LABEL[i.storageZone] : undefined) ?? i.storageZone}
                  </Badge>
                ),
              },
              {
                header: "Hao hụt",
                className: "text-right",
                cell: (i) => (
                  <span className="font-mono text-xs text-muted-foreground">
                    {(Number(i.refuseFactor) * 100).toFixed(1)}%
                  </span>
                ),
              },
              {
                header: "Thao tác",
                className: "text-right",
                cell: (i) => (
                  <div className="flex items-center justify-end gap-1">
                    <Button variant="ghost" size="icon" className="size-7" onClick={() => openEdit(i)} title="Sửa">
                      <Pencil className="size-3.5" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="size-7 text-destructive hover:text-destructive"
                      onClick={() => handleDeactivate(i)}
                      title="Vô hiệu hoá"
                    >
                      <Trash2 className="size-3.5" />
                    </Button>
                  </div>
                ),
              },
            ]}
          />
          <div className="p-4 border-t">
            <Pagination
              page={page}
              pageCount={pageCount}
              total={total}
              pageSize={PAGE_SIZE}
              onPageChange={setPage}
            />
          </div>
        </CardContent>
      </Card>

      <p className="text-xs text-muted-foreground flex items-center gap-1">
        <RotateCcw className="size-3" />
        Vô hiệu hoá (xoá) là soft-delete — nguyên liệu không bị xoá cứng, lô hàng / thực đơn cũ vẫn giữ nguyên liên kết.
      </p>
    </div>
  );
}
